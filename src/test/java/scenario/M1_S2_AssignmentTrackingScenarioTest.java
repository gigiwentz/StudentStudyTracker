package scenario;

import edu.ucsd.studentclock.model.*;
import edu.ucsd.studentclock.datasource.InMemoryDataSource;
import edu.ucsd.studentclock.repository.ExampleRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class M1_S2_AssignmentTrackingScenarioTest {

    @Test
    void studentCanAddAssignmentToCourseSeries() {

        // GIVEN a term with a course and series
        Term term = Term.createDefault();
        term.addCourse("CSE110", "#ff0000");

        InMemoryDataSource dataSource = new InMemoryDataSource();
        ExampleRepository repo = new ExampleRepository(dataSource);
        Model model = new Model(repo, term);

        LocalDateTime start = LocalDateTime.of(2026,1,1,10,0);
        model.addSeries("CSE110", start, "PA");

        // WHEN the student adds an assignment
        boolean added = model.addAssignment(
                "CSE110",
                "PA",
                "PA1",
                start.plusDays(7),
                180
        );

        // THEN the assignment should exist
        assertTrue(added);
        assertEquals(1, model.getAllAssignments().size());
    }

}
