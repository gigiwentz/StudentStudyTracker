package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Collections;

import edu.ucsd.studentclock.repository.ExampleRepository;
import edu.ucsd.studentclock.util.WeekRangeUtil;
import edu.ucsd.studentclock.service.StudyHoursService;
import edu.ucsd.studentclock.util.Clock;

public class Model {
    private final ExampleRepository repository;
    private Term term;

    public Model(ExampleRepository repository, Term term) {
        this.repository = repository;
        this.term = term;
    }

    public Term getTerm() {
        return term;
    }

    public List<WorkSession> getWorkSessions() {
        return repository.getWorkSessions();
    }

    /**
     * Work logs for one assignment, sorted by log time (end time) ascending.
     * Single source for charts and reuse; reads from repository and returns domain WorkLogs.
     */
    public List<WorkLog> getWorkLogsForAssignment(String assignmentId) {
        if (assignmentId == null) return Collections.emptyList();
        
        List<WorkSession> all = repository.getWorkSessions();
        List<WorkLog> out = new ArrayList<>();
        
        for (WorkSession ws : all) {
            if (!assignmentId.equals(ws.getAssignmentId())) continue;
            
            boolean hasEnd = ws.getEnd() != null && ws.getEnd().isAfter(ws.getStart());
            boolean hasDuration = ws.getDurationMinutes() > 0;
            
            if (hasEnd || hasDuration) {
                WorkLog log = WorkLog.fromWorkSession(ws);
                if (log.getEnd() != null) out.add(log);
            }
        }
        
        out.sort(Comparator.comparing(WorkLog::getLogDate));
        return out;
    }

    /** Location (course + series + assignment) for an assignment id, or null if not found. */
    private AssignmentLocation findAssignmentLocation(String assignmentId) {
        if (assignmentId == null) return null;
        
        for (Course c : term.getCourses()) {
            for (Series s : c.getSeries()) {
                for (Assignment a : s.getAssignments()) {
                    if (a.getId().equals(assignmentId)) {
                        return new AssignmentLocation(c, s, a);
                    }
                }
            }
        }
        return null;
    }

    public Assignment getAssignmentById(String id) {
        AssignmentLocation loc = findAssignmentLocation(id);
        return loc != null ? loc.assignment : null;
    }

    /** Course name for this assignment, or null if not found. */
    public String getCourseNameForAssignment(String assignmentId) {
        AssignmentLocation loc = findAssignmentLocation(assignmentId);
        return loc != null ? loc.course.getName() : null;
    }

    /** Course color for this assignment, or gray if not found. */
    public String getCourseColorForAssignment(String assignmentId) {
        AssignmentLocation loc = findAssignmentLocation(assignmentId);
        return loc != null ? loc.course.getColor() : "#cccccc";
    }

    /** Series that contains this assignment, or null if not found. */
    public Series getSeriesForAssignment(String assignmentId) {
        AssignmentLocation loc = findAssignmentLocation(assignmentId);
        return loc != null ? loc.series : null;
    }

    private static final class AssignmentLocation {
        final Course course;
        final Series series;
        final Assignment assignment;

        AssignmentLocation(Course course, Series series, Assignment assignment) {
            this.course = course;
            this.series = series;
            this.assignment = assignment;
        }
    }

    public boolean addAssignment(String courseName, String seriesName, Assignment assignment) {
        Course c = term.getCourse(courseName);
        if (c == null) {
            return false;
        }

        Series s = c.getSeries(seriesName);
        if (s == null) {
            return false;
        }

        s.addAssignment(assignment);
        repository.saveTerm(term);
        return true;
    }

    public boolean addAssignment(String courseName, String seriesName, String assignmentName, 
                                 LocalDateTime dueDate, long estimatedMinutes) {
        String id = UUID.randomUUID().toString();
        Assignment a = new Assignment(id, assignmentName, dueDate, estimatedMinutes);
        return addAssignment(courseName, seriesName, a);
    }

    public boolean addSeries(String course, LocalDateTime start, String seriesName) {
        Course c = term.getCourse(course);
        if (c == null) {return false;}
        c.addSeries(start, seriesName);
        repository.saveTerm(term);
        return true;
    }

    public boolean addWorkSession(WorkSession session) {
        return repository.addWorkSession(session);
    }

    public void saveTerm() {
        repository.saveTerm(term);
    }

    /** Set weekly study hour target (in minutes) and persist. */
    public void setWeeklyTargetMinutes(int minutes) {
        term.setWeeklyTargetMinutes(minutes);
        repository.saveTerm(term);
    }

    public List<Assignment> getAllAssignments() {
        return term.getCourses().stream()
                .flatMap(course -> course.getSeries().stream())
                .flatMap(series -> series.getAssignments().stream())
                .collect(Collectors.toList());
    }

    private static final Comparator<Series> BY_ACTIVE_DUE = 
        Comparator.comparing(s -> s.getActiveAssignment().getDueDate());

    public List<Series> getOpenAssignments() {
        List<Series> list = new ArrayList<>();
        for (Course c : term.getCourses()) {
            for (Series s : c.getSeries()) {
                if (s.getActiveAssignment() != null) list.add(s);
            }
        }
        list.sort(BY_ACTIVE_DUE);
        return list;
    }

    public List<Series> getUrgentAssignments(LocalDateTime currentDate) {
        List<Series> list = new ArrayList<>();
        for (Course c : term.getCourses()) {
            for (Series s : c.getSeries()) {
                Assignment a = s.getActiveAssignment();
                if (a != null && (s.getPercentBehind(currentDate) >= 0.25 || 
                                  a.getDueDate().equals(currentDate))) {
                    list.add(s);
                }
            }
        }
        list.sort(BY_ACTIVE_DUE);
        return list;
    }

    public void updateAllActive(LocalDateTime now) {
        for (Course c : term.getCourses()) {
            for (Series s : c.getSeries()) {
                s.updateActiveAssignment(now);
            }
        }
    }

    public long getWeekWorkload(LocalDateTime now) {
        long workload = 0;
        for (Series s : getOpenAssignments()) {
            Assignment a = s.getActiveAssignment();
            if (a != null && isDuringWeek(a.getDueDate(), now)) {
                workload += a.getMinutesLeft();
            }
        }
        return workload;
    }

    public long getLaterWorkload(LocalDateTime now) {
        long workload = 0;
        LocalDateTime endOfWeek = WeekRangeUtil.getEndOfWeekExclusive(now);
        
        for (Series s : getOpenAssignments()) {
            Assignment a = s.getActiveAssignment();
            if (!isDuringWeek(a.getDueDate(), now)) {
                long week = Duration.between(now, endOfWeek).toMinutes();
                long total = Duration.between(now, a.getDueDate()).toMinutes();
                if (total > 0) {
                    long thisWeekEstimate = (week * a.getMinutesLeft()) / total;
                    workload += thisWeekEstimate;
                }
            }
        }
        return workload;
    }

    // utilizes WeekRangeUtil.java to calculate current week
    public static boolean isDuringWeek(LocalDateTime date, LocalDateTime now) {
        LocalDateTime start = WeekRangeUtil.getStartOfWeek(now);
        LocalDateTime end = WeekRangeUtil.getEndOfWeekExclusive(now);
        return !date.isBefore(start) && date.isBefore(end);
    }

    public String getRemainingHHMM(Clock clock) {
        return StudyHoursService.getRemainingHHMM(term, getWorkSessions(), clock);
    }

    public long getRemainingMinutes(LocalDateTime now) {
        return StudyHoursService.getRemainingMinutes(getWorkSessions(), 
                                                     term.getWeeklyTargetMinutes(), now);
    }
}