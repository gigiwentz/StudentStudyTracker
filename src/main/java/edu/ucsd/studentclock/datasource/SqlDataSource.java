package edu.ucsd.studentclock.datasource;

import edu.ucsd.studentclock.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite persistence (Lab 4 style: JDBC, try-with-resources, PreparedStatement).
 */
public class SqlDataSource implements ITermDataSource {
    private static final String DEFAULT_URL = "jdbc:sqlite:studentclock.db";
    private static final int TERM_ID = 1;

    private final String url;

    public SqlDataSource() {
        this(DEFAULT_URL);
    }

    /** For tests: use in-memory or temp DB via custom url. */
    public SqlDataSource(String dbUrl) {
        this.url = dbUrl != null ? dbUrl : DEFAULT_URL;
        
        try (Connection conn = DriverManager.getConnection(this.url)) {
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS term (id INTEGER PRIMARY KEY, weekly_target_minutes INTEGER)");
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS courses (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, color TEXT NOT NULL, term_id INTEGER NOT NULL)");
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS series (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, start_date TEXT NOT NULL, course_id INTEGER NOT NULL, active_index INTEGER DEFAULT -1, completed INTEGER DEFAULT 0)");
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS assignments (id TEXT PRIMARY KEY, name TEXT NOT NULL, due_date TEXT, minutes_estimated INTEGER NOT NULL, minutes_worked INTEGER NOT NULL, series_id INTEGER NOT NULL)");
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS work_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT, start TEXT NOT NULL, end TEXT, assignment_id TEXT, course_name TEXT, duration_minutes INTEGER)");
            
            try { 
                conn.createStatement().execute("ALTER TABLE work_sessions ADD COLUMN course_name TEXT"); 
            } catch (SQLException ignored) { }
            
            try { 
                conn.createStatement().execute("ALTER TABLE work_sessions ADD COLUMN duration_minutes INTEGER"); 
            } catch (SQLException ignored) { }
            
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS series_progress (course_name TEXT NOT NULL, series_name TEXT NOT NULL, active_index INTEGER NOT NULL, completed INTEGER NOT NULL, PRIMARY KEY (course_name, series_name))");
            
            try { 
                conn.createStatement().execute("ALTER TABLE series ADD COLUMN active_index INTEGER DEFAULT -1"); 
            } catch (SQLException ignored) { }
            
            try { 
                conn.createStatement().execute("ALTER TABLE series ADD COLUMN completed INTEGER DEFAULT 0"); 
            } catch (SQLException ignored) { }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Term loadTerm() {
        try (Connection conn = DriverManager.getConnection(url)) {
            ensureSeriesProgressColumns(conn);
            
            Integer weekly = getWeeklyTargetMinutes(conn);
            if (weekly == null) return null;

            Term term = new Term(weekly);
            List<Integer> courseIds = loadCourses(conn, term);
            loadSeriesAndAssignments(conn, term, courseIds);
            loadSeriesProgress(conn, term);
            reconcileMinutesWorkedFromSessions(conn, term);
            populateAssignmentSessions(conn, term);
            
            return term;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void ensureSeriesProgressColumns(Connection conn) throws SQLException {
        try { 
            conn.createStatement().execute("ALTER TABLE series ADD COLUMN active_index INTEGER DEFAULT -1"); 
        } catch (SQLException ignored) { }
        
        try { 
            conn.createStatement().execute("ALTER TABLE series ADD COLUMN completed INTEGER DEFAULT 0"); 
        } catch (SQLException ignored) { }
    }

    /** Overwrite each series's active_index and completed from series_progress (never wiped by saveTerm). */
    private void loadSeriesProgress(Connection conn, Term term) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT course_name, series_name, active_index, completed FROM series_progress")) {
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String courseName = rs.getString(1);
                String seriesName = rs.getString(2);
                int activeIndex = rs.getInt(3);
                boolean completed = rs.getInt(4) != 0;
                
                Course course = term.getCourse(courseName);
                if (course == null) continue;
                
                Series series = course.getSeries(seriesName);
                if (series == null) continue;
                
                series.setActiveIndex(activeIndex);
                series.setCompleted(completed);
            }
        }
    }

    /** @return weekly_target_minutes or null if term row missing */
    private Integer getWeeklyTargetMinutes(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT weekly_target_minutes FROM term WHERE id = ?")) {
            ps.setInt(1, TERM_ID);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("weekly_target_minutes") : null;
        }
    }

    /** Load courses into term; returns list of DB course ids in same order as term.getCourses(). */
    private List<Integer> loadCourses(Connection conn, Term term) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name, color FROM courses WHERE term_id = ? ORDER BY id")) {
            ps.setInt(1, TERM_ID);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                term.addCourse(rs.getString("name"), rs.getString("color"));
                ids.add(rs.getInt("id"));
            }
        }
        
        return ids;
    }

    private void loadSeriesAndAssignments(Connection conn, Term term, List<Integer> courseIds) throws SQLException {
        int courseIndex = 0;
        
        for (Course course : term.getCourses()) {
            int courseId = courseIds.get(courseIndex);
            List<Integer> seriesIds = new ArrayList<>();
            
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, name, start_date, active_index, completed FROM series WHERE course_id = ? ORDER BY id")) {
                ps.setInt(1, courseId);
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    String startStr = rs.getString("start_date");
                    LocalDateTime start = parseDateTime(startStr);
                    
                    course.addSeries(start, rs.getString("name"));
                    
                    Series series = course.getSeries().get(course.getSeries().size() - 1);
                    series.setActiveIndex(rs.getInt(4));
                    series.setCompleted(rs.getInt(5) != 0);
                    
                    seriesIds.add(rs.getInt("id"));
                }
            }
            
            int seriesIndex = 0;
            for (Series series : course.getSeries()) {
                loadAssignments(conn, series, seriesIds.get(seriesIndex));
                seriesIndex++;
            }
            
            courseIndex++;
        }
    }

    private void loadAssignments(Connection conn, Series series, int seriesId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name, due_date, minutes_estimated, minutes_worked FROM assignments WHERE series_id = ?")) {
            ps.setInt(1, seriesId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Assignment assignment = new Assignment(
                        rs.getString("id"),
                        rs.getString("name"),
                        parseDateTime(rs.getString("due_date")),
                        rs.getLong("minutes_estimated"));
                
                assignment.setMinutesWorked(rs.getLong("minutes_worked"));
                series.addAssignment(assignment);
            }
        }
    }

    /** Load work_sessions into each assignment in chronological order (by end time) for reuse in charts. */
    private void populateAssignmentSessions(Connection conn, Term term) throws SQLException {
        Map<String, List<WorkSession>> byAssignmentId = new HashMap<>();
        
        try (ResultSet rs = conn.createStatement().executeQuery(
                "SELECT assignment_id, start, end, course_name, duration_minutes FROM work_sessions ORDER BY end")) {
            
            while (rs.next()) {
                String assignmentId = rs.getString("assignment_id");
                if (assignmentId == null) continue;
                
                LocalDateTime start = parseDateTime(rs.getString("start"));
                LocalDateTime end = parseDateTime(rs.getString("end"));
                
                if (start == null || end == null || !end.isAfter(start)) continue;
                
                String courseName = rs.getString("course_name");
                long durationMinutes = getLongOrMinusOne(rs, "duration_minutes");
                
                byAssignmentId.computeIfAbsent(assignmentId, k -> new ArrayList<>())
                        .add(new WorkSession(start, end, assignmentId, courseName, durationMinutes));
            }
        }
        
        for (Course course : term.getCourses()) {
            for (Series series : course.getSeries()) {
                for (Assignment assignment : series.getAssignments()) {
                    List<WorkSession> list = byAssignmentId.get(assignment.getId());
                    assignment.setWorkSessions(list != null ? list : new ArrayList<>());
                }
            }
        }
    }

    /** Overwrite each assignment's minutes_worked with the sum of work_sessions for that assignment. */
    private void reconcileMinutesWorkedFromSessions(Connection conn, Term term) throws SQLException {
        Map<String, Long> workedByAssignmentId = new HashMap<>();
        
        try (ResultSet rs = conn.createStatement().executeQuery(
                "SELECT assignment_id, start, end, duration_minutes FROM work_sessions")) {
            
            while (rs.next()) {
                String assignmentId = rs.getString("assignment_id");
                if (assignmentId == null) continue;
                
                LocalDateTime start = parseDateTime(rs.getString("start"));
                LocalDateTime end = parseDateTime(rs.getString("end"));
                
                if (start == null || end == null || !end.isAfter(start)) continue;
                
                long minutes = getLongOrMinusOne(rs, "duration_minutes");
                if (minutes < 0) minutes = java.time.temporal.ChronoUnit.MINUTES.between(start, end);
                
                workedByAssignmentId.merge(assignmentId, minutes, Long::sum);
            }
        }
        
        for (Course course : term.getCourses()) {
            for (Series series : course.getSeries()) {
                for (Assignment assignment : series.getAssignments()) {
                    Long sum = workedByAssignmentId.get(assignment.getId());
                    if (sum != null) assignment.setMinutesWorked(sum);
                }
            }
        }
    }

    private static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isEmpty()) return null;
        return LocalDateTime.parse(s);
    }

    private static long getLongOrMinusOne(ResultSet rs, String column) throws SQLException {
        try {
            long value = rs.getLong(column);
            return rs.wasNull() ? -1L : value;
        } catch (SQLException e) {
            return -1L;
        }
    }

    private static String toDateTimeString(LocalDateTime time) {
        return time == null ? null : time.toString();
    }

    @Override
    public void saveTerm(Term term) {
        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);
            
            try {
                ensureSeriesProgressColumns(conn);
                deleteTermData(conn);
                upsertTermRow(conn, term.getWeeklyTargetMinutes());
                
                Map<String, Integer> courseIds = insertCourses(conn, term);
                Map<String, Map<String, Integer>> seriesIds = insertSeries(conn, term, courseIds);
                insertAssignments(conn, term, seriesIds);
                saveSeriesProgress(conn, term);
                
                conn.commit();
            } catch (SQLException e) {
                try { 
                    conn.rollback(); 
                } catch (SQLException ignored) { }
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteTermData(Connection conn) throws SQLException {
        try (PreparedStatement deleteAssignments = conn.prepareStatement("DELETE FROM assignments");
             PreparedStatement deleteSeries = conn.prepareStatement("DELETE FROM series");
             PreparedStatement deleteCourses = conn.prepareStatement("DELETE FROM courses")) {
            
            deleteAssignments.executeUpdate();
            deleteSeries.executeUpdate();
            deleteCourses.executeUpdate();
        }
    }

    private void upsertTermRow(Connection conn, int weeklyTargetMinutes) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR REPLACE INTO term (id, weekly_target_minutes) VALUES (?, ?)")) {
            ps.setInt(1, TERM_ID);
            ps.setInt(2, weeklyTargetMinutes);
            ps.executeUpdate();
        }
    }

    private Map<String, Integer> insertCourses(Connection conn, Term term) throws SQLException {
        Map<String, Integer> courseIds = new HashMap<>();
        
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO courses (name, color, term_id) VALUES (?, ?, ?)", 
                Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(3, TERM_ID);
            
            for (Course course : term.getCourses()) {
                ps.setString(1, course.getName());
                ps.setString(2, course.getColor());
                ps.executeUpdate();
                
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) courseIds.put(course.getName(), keys.getInt(1));
                }
            }
        }
        
        return courseIds;
    }

    /** Returns map: courseName -> (seriesName -> seriesId). */
    private Map<String, Map<String, Integer>> insertSeries(Connection conn, Term term, 
                                                           Map<String, Integer> courseIds) throws SQLException {
        Map<String, Map<String, Integer>> seriesIds = new HashMap<>();
        
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO series (name, start_date, course_id, active_index, completed) VALUES (?, ?, ?, ?, ?)", 
                Statement.RETURN_GENERATED_KEYS)) {
            
            for (Course course : term.getCourses()) {
                int courseId = courseIds.get(course.getName());
                seriesIds.put(course.getName(), new HashMap<>());
                
                for (Series series : course.getSeries()) {
                    ps.setString(1, series.getName());
                    
                    String startStr = toDateTimeString(series.getStartDate());
                    ps.setString(2, startStr != null ? startStr : "");
                    ps.setInt(3, courseId);
                    ps.setInt(4, series.getActiveIndex());
                    ps.setInt(5, series.isCompleted() ? 1 : 0);
                    ps.executeUpdate();
                    
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            seriesIds.get(course.getName()).put(series.getName(), keys.getInt(1));
                        }
                    }
                }
            }
        }
        
        return seriesIds;
    }

    /** Persist active_index and completed to series_progress (this table is never deleted). */
    private void saveSeriesProgress(Connection conn, Term term) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR REPLACE INTO series_progress (course_name, series_name, active_index, completed) VALUES (?, ?, ?, ?)")) {
            
            for (Course course : term.getCourses()) {
                for (Series series : course.getSeries()) {
                    ps.setString(1, course.getName());
                    ps.setString(2, series.getName());
                    ps.setInt(3, series.getActiveIndex());
                    ps.setInt(4, series.isCompleted() ? 1 : 0);
                    ps.executeUpdate();
                }
            }
        }
    }

    private void insertAssignments(Connection conn, Term term, 
                                   Map<String, Map<String, Integer>> seriesIds) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO assignments (id, name, due_date, minutes_estimated, minutes_worked, series_id) VALUES (?, ?, ?, ?, ?, ?)")) {
            
            for (Course course : term.getCourses()) {
                Map<String, Integer> bySeries = seriesIds.get(course.getName());
                
                for (Series series : course.getSeries()) {
                    int seriesId = bySeries.get(series.getName());
                    
                    for (Assignment assignment : series.getAssignments()) {
                        ps.setString(1, assignment.getId());
                        ps.setString(2, assignment.getName());
                        ps.setString(3, toDateTimeString(assignment.getDueDate()));
                        ps.setLong(4, assignment.getMinutesEstimated());
                        ps.setLong(5, assignment.getMinutesWorked());
                        ps.setInt(6, seriesId);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    @Override
    public List<WorkSession> getWorkSessions() {
        List<WorkSession> list = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(url);
             ResultSet rs = conn.createStatement().executeQuery(
                     "SELECT start, end, assignment_id, course_name, duration_minutes FROM work_sessions ORDER BY end")) {
            
            while (rs.next()) {
                String startStr = rs.getString("start");
                if (startStr == null || startStr.isEmpty()) continue;
                
                LocalDateTime start = LocalDateTime.parse(startStr);
                LocalDateTime end = parseDateTime(rs.getString("end"));
                String assignmentId = rs.getString("assignment_id");
                String courseName = rs.getString("course_name");
                long durationMinutes = getLongOrMinusOne(rs, "duration_minutes");
                
                list.add(new WorkSession(start, end, assignmentId, courseName, durationMinutes));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }

    /**
     * Persist one work session immediately (real-time update).
     * Commits and closes the connection so the row is visible to other readers and flushed to disk;
     * no need to exit the app to see the new session.
     */
    @Override
    public boolean addWorkSession(WorkSession session) {
        if (session == null || session.getStart() == null) return false;
        
        try (Connection conn = DriverManager.getConnection(url)) {
            conn.createStatement().execute("PRAGMA synchronous = FULL");
            conn.setAutoCommit(false);
            
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO work_sessions (start, end, assignment_id, course_name, duration_minutes) VALUES (?, ?, ?, ?, ?)")) {
                    
                    ps.setString(1, session.getStart().toString());
                    ps.setString(2, session.getEnd() != null ? session.getEnd().toString() : null);
                    
                    String assignmentId = session.getAssignmentId();
                    if (assignmentId != null) {
                        ps.setString(3, assignmentId);
                    } else {
                        ps.setNull(3, Types.VARCHAR);
                    }
                    
                    String courseName = session.getCourseName();
                    if (courseName != null) {
                        ps.setString(4, courseName);
                    } else {
                        ps.setNull(4, Types.VARCHAR);
                    }
                    
                    long duration = session.getDurationMinutes();
                    if (duration >= 0) {
                        ps.setLong(5, duration);
                    } else {
                        ps.setNull(5, Types.INTEGER);
                    }
                    
                    ps.executeUpdate();
                }
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}