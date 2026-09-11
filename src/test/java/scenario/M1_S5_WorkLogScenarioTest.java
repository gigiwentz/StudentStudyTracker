package scenario;

import edu.ucsd.studentclock.model.*;
import edu.ucsd.studentclock.datasource.InMemoryDataSource;
import edu.ucsd.studentclock.repository.ExampleRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class M1_S5_WorkLogScenarioTest {

    @Test
    void workLogsAreReturnedChronologically() {

        // GIVEN a repository with work sessions
        InMemoryDataSource dataSource = new InMemoryDataSource();
        ExampleRepository repo = new ExampleRepository(dataSource);
        Term term = Term.createDefault();
        Model model = new Model(repo, term);

        LocalDateTime date = LocalDateTime.of(2026,1,1,10,0);
        WorkSession s1 = new WorkSession(
                date.minusHours(2),
                date.minusHours(1),
                "a1"
        );

        WorkSession s2 = new WorkSession(
                date.minusHours(1),
                date,
                "a1"
        );

        repo.addWorkSession(s1);
        repo.addWorkSession(s2);

        // WHEN work logs are retrieved
        List<WorkLog> logs = model.getWorkLogsForAssignment("a1");

        // THEN logs should be chronological
        assertEquals(2, logs.size());
        assertTrue(logs.get(0).getLogDate().isBefore(logs.get(1).getLogDate()));
    }

}
