package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WorkSessionTest {

    private static final LocalDateTime START = LocalDateTime.of(2025, 2, 3, 10, 0);
    private static final LocalDateTime END_30MIN = START.plusMinutes(30);

    @Test
    void twoArgConstructor_setsAssignmentIdNull() {
        WorkSession s = new WorkSession(START, END_30MIN);
        assertNull(s.getAssignmentId());
        assertEquals(30, s.getDurationMinutes());
    }

    @Test
    void threeArgConstructor_storesAssignmentId() {
        WorkSession s = new WorkSession(START, END_30MIN, "assign-1");
        assertEquals("assign-1", s.getAssignmentId());
        assertEquals(30, s.getDurationMinutes());
    }

    @Test
    void getDurationMinutes_withEnd_returnsMinutesBetween() {
        assertEquals(30, new WorkSession(START, END_30MIN, "a1").getDurationMinutes());
        assertEquals(60, new WorkSession(START, START.plusMinutes(60), null).getDurationMinutes());
    }

    @Test
    void getDurationMinutes_noEnd_returnsZero() {
        WorkSession s = new WorkSession(START, null, "a1");
        assertEquals(0, s.getDurationMinutes());
    }

    @Test
    void getDurationMinutes_invalidEnd_returnsZero() {
        assertEquals(0, new WorkSession(START, START, "a1").getDurationMinutes());
    }

    @Test
    void getDurationMinutes_endBeforeStart_returnsZero() {
        WorkSession s = new WorkSession(
                START,
                START.minusMinutes(10),
                "a1"
        );

        assertEquals(0, s.getDurationMinutes());
    }

    @Test
    void getStart_getEnd_returnGivenValues() {
        WorkSession s = new WorkSession(START, END_30MIN, "a1");
        assertEquals(START, s.getStart());
        assertEquals(END_30MIN, s.getEnd());
    }
}
