package scenario;

import edu.ucsd.studentclock.model.*;
import edu.ucsd.studentclock.datasource.InMemoryDataSource;
import edu.ucsd.studentclock.repository.ExampleRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class M1_S4_DashboardBehindScenarioTest {

    @Test
    void dashboardShowsAssignmentsStudentIsBehindOn() {

        // GIVEN a course and assignment
        Term term = Term.createDefault();
        term.addCourse("CSE110", "#ff0000");

        InMemoryDataSource dataSource = new InMemoryDataSource();
        ExampleRepository repo = new ExampleRepository(dataSource);
        Model model = new Model(repo, term);

        LocalDateTime date = LocalDateTime.of(2026,1,1,10,0);
        model.addSeries("CSE110", date.minusDays(5), "PA");

        model.addAssignment(
                "CSE110",
                "PA",
                "PA1",
                date.plusDays(2),
                300
        );

        model.updateAllActive(date);

        // WHEN dashboard assignments are retrieved
        List<Series> dashboard = model.getOpenAssignments();

        // THEN the dashboard contains the assignment series
        assertEquals(1, dashboard.size());
        assertEquals("PA", dashboard.get(0).getName());
    }

}
