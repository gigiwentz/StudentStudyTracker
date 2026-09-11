package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentTest {

    private Assignment createAssignment(long minutesEstimated) {
        return new Assignment(
                "assignment1",
                "HW1",
                LocalDateTime.of(2026, 2, 20, 23, 59),
                minutesEstimated
        );
    }

    @Test
    void constructor_initializesFieldsCorrectly() {
        LocalDateTime due = LocalDateTime.of(2026, 3, 1, 12, 0);

        Assignment assignment = new Assignment("id1", "Project", due, 120);

        assertEquals("id1", assignment.getId());
        assertEquals("Project", assignment.getName());
        assertEquals(due, assignment.getDueDate());
        assertEquals(120, assignment.getMinutesEstimated());
        assertEquals(0, assignment.getMinutesWorked());
    }

    @Test
    void updateMinutesWorked_addsSessionDuration() {
        Assignment assignment = createAssignment(100);

        LocalDateTime start = LocalDateTime.of(2026, 2, 12, 10, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 2, 12, 10, 30);

        WorkSession session = new WorkSession(start, end);

        assignment.updateMinutesWorked(session);

        assertEquals(30, assignment.getMinutesWorked());
    }

    @Test
    void updateMinutesWorked_multipleSessions_accumulates() {
        Assignment assignment = createAssignment(120);

        WorkSession session1 = new WorkSession(
                LocalDateTime.of(2026, 2, 12, 10, 0),
                LocalDateTime.of(2026, 2, 12, 10, 30));

        WorkSession session2 = new WorkSession(
                LocalDateTime.of(2026, 2, 12, 11, 0),
                LocalDateTime.of(2026, 2, 12, 11, 20));

        assignment.updateMinutesWorked(session1);
        assignment.updateMinutesWorked(session2);

        assertEquals(50, assignment.getMinutesWorked());
    }

    @Test
    void getMinutesLeft_returnsEstimatedMinusWorked() {
        Assignment assignment = createAssignment(60);

        WorkSession session = new WorkSession(
                LocalDateTime.of(2026, 2, 12, 9, 0),
                LocalDateTime.of(2026, 2, 12, 9, 20)
        );

        assignment.updateMinutesWorked(session);

        assertEquals(40, assignment.getMinutesLeft());
    }

    @Test
    void getMinutesLeft_neverReturnsNegative() {
        Assignment assignment = createAssignment(30);

        WorkSession session = new WorkSession(
                LocalDateTime.of(2026, 2, 12, 9, 0),
                LocalDateTime.of(2026, 2, 12, 10, 0)
        ); // 60 minutes

        assignment.updateMinutesWorked(session);

        assertEquals(0, assignment.getMinutesLeft());
    }

    @Test 
    void setName_changesName() {
        LocalDateTime due = LocalDateTime.of(2026, 3, 1, 12, 0);
        Assignment assignment = new Assignment("id1", "Project", due, 120);

        assignment.setName("Project 1");

        assertEquals("Project 1", assignment.getName());
    }
}