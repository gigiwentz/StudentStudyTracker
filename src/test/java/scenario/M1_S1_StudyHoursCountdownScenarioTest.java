package scenario;

import edu.ucsd.studentclock.datasource.InMemoryDataSource;
import edu.ucsd.studentclock.model.*;
import edu.ucsd.studentclock.repository.ExampleRepository;
import edu.ucsd.studentclock.util.AppClock;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

class M1_S1_StudyHoursCountdownScenarioTest {

    @Test
    void studyHoursDecreaseAfterWorkSession() {

    // GIVEN a term with a weekly target of 10 hours
    Term term = new Term(10 * 60);
    InMemoryDataSource dataSource = new InMemoryDataSource();
    ExampleRepository repo = new ExampleRepository(dataSource);
    Model model = new Model(repo, term);

    AppClock clock = new AppClock();

    // AND no work sessions exist
    assertEquals("10:00", model.getRemainingHHMM(clock));

    // WHEN a 1 hour work session is logged during this week
    LocalDateTime now = clock.now();

    WorkSession session = new WorkSession(
            now.minusMinutes(60),
            now,
            null
    );

    model.addWorkSession(session);

    // THEN remaining time decreases
    String remaining = model.getRemainingHHMM(clock);
    assertEquals("9:00", remaining);
    }

}
