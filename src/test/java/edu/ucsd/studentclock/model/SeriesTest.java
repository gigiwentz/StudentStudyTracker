package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class SeriesTest {

    private Assignment createAssignment(String id) {
        return new Assignment(
                id,
                "Assignment " + id,
                LocalDateTime.of(2026, 3, 10, 23, 59),
                90
        );
    }

    private Assignment createAssignment(
            String id,
            LocalDateTime due,
            long estimate
    ) {
        return new Assignment(id, "Assignment " + id, due, estimate);
    }

    @Test
    void constructor_setsNameAndStartDate() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 8, 0);

        Series series = new Series(start, "Winter Quarter");

        assertEquals(start, series.getStartDate());
        assertEquals("Winter Quarter", series.getName());
    }

    @Test
    void addAssignment_thenGetAssignment_returnsSameObject() {
        Series series = new Series(LocalDateTime.now(), "Test Series");
        Assignment assignment1 = createAssignment("a1");

        series.addAssignment(assignment1);

        Assignment result = series.getAssignment("a1");

        assertSame(assignment1, result);
    }

    @Test
    void getAssignment_unknownId_returnsNull() {
        Series series = new Series(LocalDateTime.now(), "Test Series");

        assertNull(series.getAssignment("missing"));
    }

    @Test
    void getAssignments_returnsAllAddedAssignments() {
        Series series = new Series(LocalDateTime.now(), "Test Series");

        Assignment assignment1 = createAssignment("a1");
        Assignment assignment2 = createAssignment("a2");

        series.addAssignment(assignment1);
        series.addAssignment(assignment2);

        Collection<Assignment> all = series.getAssignments();

        assertEquals(2, all.size());
        assertTrue(all.contains(assignment1));
        assertTrue(all.contains(assignment2));
    }

    @Test
    void activeStartsNull() {
        Series series = new Series(LocalDateTime.now(), "Test Series");
        Assignment assignment1 = createAssignment("a1");
        Assignment assignment2 = createAssignment("a2");

        series.addAssignment(assignment1);
        series.addAssignment(assignment2);

        assertNull(series.getActiveAssignment());
    }

    @Test
    void activeGivesAssignment() {
        Series series = new Series(LocalDateTime.now(), "Test Series");
        Assignment assignment1 = createAssignment("a1");
        Assignment assignment2 = createAssignment("a2");

        series.addAssignment(assignment1);
        series.addAssignment(assignment2);

        series.advanceActiveAssignment();
        Assignment result = series.getActiveAssignment();
        
        assertSame(assignment1, result);
    }

    @Test
    void getAssignmentStart_noActiveAssignment_returnsNull() {
        Series series = new Series(LocalDateTime.now(), "Test");

        assertNull(series.getAssignmentStart());
    }

    @Test
    void getAssignmentStart_firstAssignment_returnsSeriesStartDate() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 8, 0);

        Series series = new Series(start, "Test");

        Assignment assignment1 = createAssignment("a1");
        series.addAssignment(assignment1);

        series.advanceActiveAssignment(); // active = 0

        assertEquals(start, series.getAssignmentStart());
    }

    @Test
    void getAssignmentStart_secondAssignment_returnsPreviousDueDate() {
        Series series = new Series(
                LocalDateTime.of(2026, 2, 1, 8, 0),
                "Test"
        );

        Assignment assignment1 = new Assignment(
                "a1",
                "A1",
                LocalDateTime.of(2026, 2, 5, 23, 59),
                60
        );

        Assignment assignment2 = createAssignment("a2");

        series.addAssignment(assignment1);
        series.addAssignment(assignment2);

        series.advanceActiveAssignment(); // a1 active
        series.advanceActiveAssignment(); // a2 active

        assertEquals(assignment1.getDueDate(), series.getAssignmentStart());
    }

    @Test
    void getMinutesBehind_noActiveAssignment_returnsZero() {
        Series series = new Series(LocalDateTime.now(), "Test");

        long result = series.getMinutesBehind(LocalDateTime.now());

        assertEquals(0, result);
    }

    @Test
    void getMinutesBehind_beforeStart_returnsZero() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 10, 0);

        Series series = new Series(start, "Test");

        Assignment assignment1 = createAssignment("a1", start.plusHours(10), 100);

        series.addAssignment(assignment1);
        series.advanceActiveAssignment();

        LocalDateTime beforeStart = start.minusHours(1);

        assertEquals(0, series.getMinutesBehind(beforeStart));
    }

    @Test
    void getMinutesBehind_whenAhead_returnsZero() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 8, 0);

        Series series = new Series(start, "Test");

        Assignment assignment1 = createAssignment("a1", start.plusMinutes(100), 100);

        series.addAssignment(assignment1);
        series.advanceActiveAssignment();

        // lots of work already completed
        assignment1.updateMinutesWorked(
                new WorkSession(start, start.plusMinutes(80), "a1"));

        LocalDateTime now = start.plusMinutes(50);

        assertEquals(0, series.getMinutesBehind(now));
    }

    @Test
    void getMinutesBehind_computesExpectedValueCorrectly() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 8, 0);

        Series series = new Series(start, "Test");

        Assignment assignment1 = createAssignment("a1", start.plusMinutes(100), 100);

        series.addAssignment(assignment1);
        series.advanceActiveAssignment();

        // worked 20 minutes
        assignment1.updateMinutesWorked(
                new WorkSession(start, start.plusMinutes(20), "a1"));

        // halfway through timeline
        LocalDateTime now = start.plusMinutes(50);

        long behind = series.getMinutesBehind(now);

        /*
        Correct calculation:
        estimated = 100 minutes
        worked = 20 minutes
        elapsed = 50 minutes
        totalTime = 100 minutes
        
        expected = (elapsed * estimated) / totalTime = (50 * 100) / 100 = 50
        behind = expected - worked = 50 - 20 = 30
        */
        assertEquals(30, behind);
    }

    @Test
    void getPercentBehind_onSchedule_returnsZero() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 8, 0);

        Series series = new Series(start, "Test");

        Assignment assignment1 = createAssignment("a1", start.plusMinutes(100), 100);

        series.addAssignment(assignment1);
        series.advanceActiveAssignment();

        assignment1.updateMinutesWorked(
                new WorkSession(start, start.plusMinutes(50), "a1"));

        LocalDateTime now = start.plusMinutes(50);

        assertEquals(0.0, series.getPercentBehind(now), 0.0001);
    }

    @Test
    void getPercentBehind_returnsCorrectFraction() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 8, 0);

        Series series = new Series(start, "Test");

        Assignment assignment1 = createAssignment("a1", start.plusMinutes(100), 100);

        series.addAssignment(assignment1);
        series.advanceActiveAssignment();

        // worked 25 instead of expected 50
        assignment1.updateMinutesWorked(
                new WorkSession(start, start.plusMinutes(25), "a1"));

        LocalDateTime now = start.plusMinutes(50);

        double percent = series.getPercentBehind(now);

        assertEquals(0.25, percent, 0.0001);
    }

    @Test
    void getPercentBehind_noActiveAssignment_returnsZero() {
        Series series = new Series(LocalDateTime.now(), "Test");

        assertEquals(0.0, series.getPercentBehind(LocalDateTime.now()), 0.0001);
    }

    @Test
    void updateActiveAssignment_beforeSeriesStart_staysInactive() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 10, 0);

        Series series = new Series(start, "Test");

        Assignment assignment1 = createAssignment("a1", start.plusHours(2), 60);

        series.addAssignment(assignment1);

        // before start date
        series.updateActiveAssignment(start.minusMinutes(1));

        assertNull(series.getActiveAssignment());
    }

    @Test
    void updateActiveAssignment_atStart_activatesFirstAssignment() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 10, 0);

        Series series = new Series(start, "Test");

        Assignment assignment1 = createAssignment("a1", start.plusHours(2), 60);

        series.addAssignment(assignment1);

        series.updateActiveAssignment(start);

        assertSame(assignment1, series.getActiveAssignment());
    }

    @Test
    void updateActiveAssignment_beforeDueDate_doesNotAdvance() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 8, 0);

        Series series = new Series(start, "Test");

        Assignment assignment1 = createAssignment("a1", start.plusHours(2), 60);

        series.addAssignment(assignment1);

        series.updateActiveAssignment(start);
        series.updateActiveAssignment(start.plusMinutes(30));

        assertSame(assignment1, series.getActiveAssignment());
    }

    @Test
    void updateActiveAssignment_skipsMultipleExpiredAssignments() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 8, 0);

        Series series = new Series(start, "Test");

        Assignment assignment1 = createAssignment("a1", start.plusMinutes(10), 60);
        Assignment assignment2 = createAssignment("a2", start.plusMinutes(20), 60);
        Assignment assignment3 = createAssignment("a3", start.plusHours(2), 60);

        series.addAssignment(assignment1);
        series.addAssignment(assignment2);
        series.addAssignment(assignment3);

        // far in future
        series.updateActiveAssignment(start.plusMinutes(30));

        assertSame(assignment3, series.getActiveAssignment());
    }

    @Test
    void updateActiveAssignment_afterLastAssignment_doesNotCrash() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 8, 0);
        Series series = new Series(start, "Test");
        Assignment assignment1 = createAssignment("a1", start.plusMinutes(10), 60);
        
        series.addAssignment(assignment1);
        series.updateActiveAssignment(start.plusHours(5)); // way past due

        assertNull(series.getActiveAssignment());
        assertTrue(series.isCompleted());
    }

    // --- Mark Done persistence (setActiveIndex / setCompleted) ---
    @Test
    void setActiveIndex_thenGetActiveAssignment_returnsCorrectAssignment() {
        Series series = new Series(LocalDateTime.now(), "PA");
        Assignment assignment1 = createAssignment("a1");
        Assignment assignment2 = createAssignment("a2");
        
        series.addAssignment(assignment1);
        series.addAssignment(assignment2);

        series.setActiveIndex(0);
        assertSame(assignment1, series.getActiveAssignment());
        
        series.setActiveIndex(1);
        assertSame(assignment2, series.getActiveAssignment());
    }

    @Test
    void setActiveIndexMinusOne_getActiveAssignment_returnsNull() {
        Series series = new Series(LocalDateTime.now(), "PA");
        series.addAssignment(createAssignment("a1"));
        
        series.setActiveIndex(0);
        series.setActiveIndex(-1);
        
        assertNull(series.getActiveAssignment());
    }

    @Test
    void setCompleted_true_thenIsCompleted_returnsTrue() {
        Series series = new Series(LocalDateTime.now(), "PA");
        series.addAssignment(createAssignment("a1"));
        
        series.setCompleted(true);
        
        assertTrue(series.isCompleted());
    }

    @Test
    void getActiveIndex_and_setActiveIndex_roundtrip() {
        Series series = new Series(LocalDateTime.now(), "PA");
        series.addAssignment(createAssignment("a1"));
        series.addAssignment(createAssignment("a2"));
        
        series.setActiveIndex(1);
        
        assertEquals(1, series.getActiveIndex());
    }

    @Test
    void advanceActiveAssignment_noAssignments_doesNothing() {
        Series series = new Series(LocalDateTime.now(), "Test");

        series.advanceActiveAssignment();

        assertNull(series.getActiveAssignment());
    }
}