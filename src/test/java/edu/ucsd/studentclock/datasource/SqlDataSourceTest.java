package edu.ucsd.studentclock.datasource;

import edu.ucsd.studentclock.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SqlDataSource: term + work_sessions + series_progress (Mark Done) persistence.
 */
class SqlDataSourceTest {

    @TempDir
    Path tempDir;
    private SqlDataSource dataSource;

    @BeforeEach
    void setUp() {
        String url = "jdbc:sqlite:" + tempDir.resolve("test.db").toAbsolutePath();
        dataSource = new SqlDataSource(url);
    }

    @Test
    void loadTerm_initiallyReturnsNull() {
        assertNull(dataSource.loadTerm());
    }

    @Test
    void saveTerm_thenLoadTerm_restoresWeeklyTarget() {
        Term term = new Term(420);
        dataSource.saveTerm(term);
        
        Term loaded = dataSource.loadTerm();
        assertNotNull(loaded);
        assertEquals(420, loaded.getWeeklyTargetMinutes());
    }

    @Test
    void saveTerm_withCourseAndSeries_thenLoadTerm_restoresStructure() {
        Term term = createBasicTerm();
        dataSource.saveTerm(term);

        Term loaded = dataSource.loadTerm();
        assertNotNull(loaded);
        assertEquals(1, loaded.getCourses().size());
        assertEquals("CSE 110", loaded.getCourses().get(0).getName());
        assertEquals(1, loaded.getCourses().get(0).getSeries().size());
        assertEquals("PA", loaded.getCourses().get(0).getSeries().get(0).getName());
    }

    @Test
    void seriesProgress_saveThenLoad_restoresActiveIndexAndCompleted() {
        Term term = createBasicTerm();
        term.getCourse("CSE 110").addSeries(LocalDateTime.of(2026, 2, 1, 0, 0), "Midterm");
        addAssignmentToModel(term, "CSE 110", "PA", "PA1", LocalDateTime.of(2026, 2, 10, 23, 59), 90);
        addAssignmentToModel(term, "CSE 110", "PA", "PA2", LocalDateTime.of(2026, 2, 20, 23, 59), 90);
        addAssignmentToModel(term, "CSE 110", "Midterm", "MT1", LocalDateTime.of(2026, 2, 15, 23, 59), 60);

        Series pa = term.getCourse("CSE 110").getSeries("PA");
        Series midterm = term.getCourse("CSE 110").getSeries("Midterm");
        
        pa.advanceActiveAssignment(); // active = 0 (PA1)
        pa.advanceActiveAssignment(); // active = 1 (PA2) — "mark PA1 done"
        midterm.advanceActiveAssignment(); // active = 0 (MT1)

        dataSource.saveTerm(term);

        Term loaded = dataSource.loadTerm();
        assertNotNull(loaded);
        
        Series loadedPa = loaded.getCourse("CSE 110").getSeries("PA");
        Series loadedMidterm = loaded.getCourse("CSE 110").getSeries("Midterm");

        assertEquals(1, loadedPa.getActiveIndex());
        assertFalse(loadedPa.isCompleted());
        assertNotNull(loadedPa.getActiveAssignment());
        assertEquals("PA 2", loadedPa.getActiveAssignment().getName());

        assertEquals(0, loadedMidterm.getActiveIndex());
        assertFalse(loadedMidterm.isCompleted());
    }

    @Test
    void seriesProgress_completedSeries_restoresCompletedTrue() {
        Term term = createBasicTerm();
        addAssignmentToModel(term, "CSE 110", "PA", "PA1", LocalDateTime.of(2026, 2, 10, 23, 59), 90);

        Series pa = term.getCourse("CSE 110").getSeries("PA");
        pa.advanceActiveAssignment(); // active = 0
        pa.advanceActiveAssignment(); // active = -1, completed = true

        dataSource.saveTerm(term);

        Term loaded = dataSource.loadTerm();
        Series loadedPa = loaded.getCourse("CSE 110").getSeries("PA");
        
        assertEquals(-1, loadedPa.getActiveIndex());
        assertTrue(loadedPa.isCompleted());
        assertNull(loadedPa.getActiveAssignment());
    }

    @Test
    void addWorkSession_thenGetWorkSessions_returnsSessions() {
        Term term = createBasicTerm();
        addAssignmentToModel(term, "CSE 110", "PA", "A1", LocalDateTime.of(2026, 2, 10, 23, 59), 60);
        
        String assignmentId = term.getCourse("CSE 110")
                .getSeries("PA")
                .getAssignments()
                .iterator()
                .next()
                .getId();
                
        dataSource.saveTerm(term);

        WorkSession workSession = new WorkSession(
                LocalDateTime.of(2026, 2, 5, 10, 0),
                LocalDateTime.of(2026, 2, 5, 10, 30),
                assignmentId
        );
        dataSource.addWorkSession(workSession);

        List<WorkSession> list = dataSource.getWorkSessions();
        assertEquals(1, list.size());
        assertEquals(30, list.get(0).getDurationMinutes());
        assertEquals(assignmentId, list.get(0).getAssignmentId());
    }

    @Test
    void addWorkSession_nullStart_notSaved() {
        dataSource.addWorkSession(new WorkSession(null, LocalDateTime.now(), "assignment1"));
        assertTrue(dataSource.getWorkSessions().isEmpty());
    }

    @Test
    void addMultipleWorkSessions_persistedCorrectly() {
        Term term = new Term(60);
        dataSource.saveTerm(term);

        dataSource.addWorkSession(new WorkSession(
                LocalDateTime.of(2026, 2, 1, 10, 0),
                LocalDateTime.of(2026, 2, 1, 10, 30),
                null));

        dataSource.addWorkSession(new WorkSession(
                LocalDateTime.of(2026, 2, 1, 11, 0),
                LocalDateTime.of(2026, 2, 1, 11, 20),
                null));

        assertEquals(2, dataSource.getWorkSessions().size());
    }

    @Test
    void loadTerm_afterWorkSessions_reconcilesMinutesWorked() {
        Term term = createBasicTerm();
        addAssignmentToModel(term, "CSE 110", "PA", "A1", LocalDateTime.of(2026, 2, 10, 23, 59), 60);
        
        String assignmentId = term.getCourse("CSE 110")
                .getSeries("PA")
                .getAssignments()
                .iterator()
                .next()
                .getId();
                
        dataSource.saveTerm(term);
        
        dataSource.addWorkSession(new WorkSession(
                LocalDateTime.of(2026, 2, 5, 10, 0),
                LocalDateTime.of(2026, 2, 5, 10, 30),
                assignmentId));

        Term loaded = dataSource.loadTerm();
        assertNotNull(loaded);
        
        long minutesWorked = loaded.getCourse("CSE 110")
                .getSeries("PA")
                .getAssignments()
                .iterator()
                .next()
                .getMinutesWorked();
                
        assertEquals(30, minutesWorked);
    }

    // Helper methods
    private Term createBasicTerm() {
        Term term = new Term(60);
        term.addCourse("CSE 110", "#green");
        term.getCourse("CSE 110").addSeries(LocalDateTime.of(2026, 2, 1, 0, 0), "PA");
        return term;
    }

    private void addAssignmentToModel(Term term, String courseName, String seriesName, 
                                     String assignmentName, LocalDateTime dueDate, long minutes) {
        Course course = term.getCourse(courseName);
        Series series = course.getSeries(seriesName);
        Assignment assignment = new Assignment(
                java.util.UUID.randomUUID().toString(), 
                assignmentName, 
                dueDate, 
                minutes);
        series.addAssignment(assignment);
    }
}