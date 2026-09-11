package scenario;

import edu.ucsd.studentclock.model.*;
import edu.ucsd.studentclock.datasource.SqlDataSource;
import edu.ucsd.studentclock.repository.ExampleRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class M1_S6_PersistenceScenarioTest {

@Test
void termDataIsSavedAndReloaded() {

    SqlDataSource dataSource = new SqlDataSource();

    // GIVEN a model with a term
    Term term = Term.createDefault();
    term.addCourse("CSE110", "#ff0000");

    ExampleRepository repo1 = new ExampleRepository(dataSource);
    Model model1 = new Model(repo1, term);

    // WHEN the term is saved
    model1.saveTerm();

    // AND a new repository is created
    ExampleRepository repo2 = new ExampleRepository(dataSource);

    // THEN the saved term can be loaded
    Term loaded = repo2.loadTerm();

    assertNotNull(loaded);
    assertEquals(1, loaded.getCourses().size());
}

}
