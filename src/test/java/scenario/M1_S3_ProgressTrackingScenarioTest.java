package scenario;

import edu.ucsd.studentclock.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class M1_S3_ProgressTrackingScenarioTest {

    @Test
    void loggingWorkUpdatesAssignmentProgress() {

        // GIVEN an assignment with estimated work
        LocalDateTime start = LocalDateTime.of(2026,1,1,10,0);
        Assignment assignment = new Assignment(
                "a1",
                "HW1",
                start.plusDays(5),
                300
        );

        // AND no work logged
        assertEquals(0, assignment.getMinutesWorked());

        // WHEN a work session is added
        WorkSession session = new WorkSession(
                start,
                start.plusMinutes(60),
                "a1"
        );

        assignment.addWorkSession(session);

        // THEN the minutes worked should increase
        assertEquals(60, assignment.getMinutesWorked());

        // AND minutes left should decrease
        assertEquals(240, assignment.getMinutesLeft());
    }

}
