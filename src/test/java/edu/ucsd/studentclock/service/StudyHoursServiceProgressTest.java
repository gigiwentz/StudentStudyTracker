package edu.ucsd.studentclock.service;

import edu.ucsd.studentclock.model.WorkSession;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StudyHoursServiceProgressTest {

    private static final LocalDateTime MONDAY = LocalDateTime.of(2025, 2, 3, 12, 0);

    @Test
    void getLoggedMinutesByAssignment_emptyList_returnsEmptyMap() {
        Map<String, Long> result = StudyHoursService.getLoggedMinutesByAssignment(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void getLoggedMinutesByAssignment_zeroMinuteSession_countsZero() {
        WorkSession s = new WorkSession(MONDAY, MONDAY, "a1");

        Map<String, Long> result =
                StudyHoursService.getLoggedMinutesByAssignment(List.of(s));

        assertEquals(0L, result.get("a1"));
    }

    @Test
    void getLoggedMinutesByAssignment_sessionsWithNullAssignmentId_omitted() {
        WorkSession unassigned = new WorkSession(MONDAY, MONDAY.plusMinutes(60), null);
        Map<String, Long> result = StudyHoursService.getLoggedMinutesByAssignment(List.of(unassigned));
        assertTrue(result.isEmpty());
    }

    @Test
    void getLoggedMinutesByAssignment_sessionsWithEmptyAssignmentId_omitted() {
        WorkSession empty = new WorkSession(MONDAY, MONDAY.plusMinutes(60), "");
        Map<String, Long> result = StudyHoursService.getLoggedMinutesByAssignment(List.of(empty));
        assertTrue(result.isEmpty());
    }

    @Test
    void getLoggedMinutesByAssignment_singleAssignment_sumsMinutes() {
        WorkSession s1 = new WorkSession(MONDAY, MONDAY.plusMinutes(30), "a1");
        WorkSession s2 = new WorkSession(MONDAY.plusHours(1), MONDAY.plusHours(2), "a1");
        Map<String, Long> result = StudyHoursService.getLoggedMinutesByAssignment(List.of(s1, s2));
        assertEquals(1, result.size());
        assertEquals(90L, result.get("a1"));
    }

    @Test
    void getLoggedMinutesByAssignment_multipleAssignments_separateEntries() {
        WorkSession s1 = new WorkSession(MONDAY, MONDAY.plusMinutes(45), "pa1");
        WorkSession s2 = new WorkSession(MONDAY.plusHours(1), MONDAY.plusHours(1).plusMinutes(30), "pa2");
        WorkSession s3 = new WorkSession(MONDAY.plusHours(2), MONDAY.plusHours(2).plusMinutes(30), "pa1");
        Map<String, Long> result = StudyHoursService.getLoggedMinutesByAssignment(List.of(s1, s2, s3));
        assertEquals(2, result.size());
        assertEquals(75L, result.get("pa1"));  // 45 + 30
        assertEquals(30L, result.get("pa2"));
    }

    @Test
    void getLoggedMinutesByAssignment_mixedAssignedAndUnassigned_onlyCountsAssigned() {
        WorkSession unassigned = new WorkSession(MONDAY, MONDAY.plusMinutes(20), null);
        WorkSession assigned = new WorkSession(MONDAY.plusMinutes(30), MONDAY.plusMinutes(50), "a1");
        Map<String, Long> result = StudyHoursService.getLoggedMinutesByAssignment(List.of(unassigned, assigned));
        assertEquals(1, result.size());
        assertEquals(20L, result.get("a1"));
    }
}
