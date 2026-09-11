package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.datasource.InMemoryDataSource;
import edu.ucsd.studentclock.model.Term;
import edu.ucsd.studentclock.model.WorkSession;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExampleRepositoryTest {

    private static ExampleRepository newRepo() {
        return new ExampleRepository(new InMemoryDataSource());
    }

    private WorkSession makeSession() {
        return new WorkSession(
                LocalDateTime.of(2026, 2, 12, 10, 0),
                LocalDateTime.of(2026, 2, 12, 10, 15)
        );
    }

    @Test
    void saveTerm_thenLoadTerm_returnsSameTerm() {
        ExampleRepository repo = newRepo();
        Term term = new Term(300);

        repo.saveTerm(term);

        assertSame(term, repo.loadTerm());
    }


    @Test
    void addWorkSession_validSession_returnsTrueAndStores() {
        ExampleRepository repo = newRepo();
        WorkSession session = makeSession();

        boolean result = repo.addWorkSession(session);

        assertTrue(result);

        List<WorkSession> sessions = repo.getWorkSessions();
        assertEquals(1, sessions.size());
        assertSame(session, sessions.get(0));
    }

    @Test
    void addWorkSession_null_returnsFalse() {
        ExampleRepository repo = newRepo();

        assertFalse(repo.addWorkSession(null));
        assertTrue(repo.getWorkSessions().isEmpty());
    }

    @Test
    void addWorkSession_nullStart_returnsFalse() {
        ExampleRepository repo = newRepo();

        WorkSession bad =
                new WorkSession(null, LocalDateTime.now());

        assertFalse(repo.addWorkSession(bad));
        assertTrue(repo.getWorkSessions().isEmpty());
    }

    @Test
    void addWorkSession_multipleSessions_allStored() {
        ExampleRepository repo = newRepo();

        repo.addWorkSession(makeSession());
        repo.addWorkSession(makeSession());

        assertEquals(2, repo.getWorkSessions().size());
    }

    @Test
    void getWorkSessions_returnsCopy_notInternalList() {
        ExampleRepository repo = newRepo();
        repo.addWorkSession(makeSession());

        List<WorkSession> sessions = repo.getWorkSessions();

        assertThrows(UnsupportedOperationException.class,
                () -> sessions.add(makeSession()));
    }

    @Test
    void getWorkSessions_initiallyEmpty() {
        ExampleRepository repo = newRepo();
        assertTrue(repo.getWorkSessions().isEmpty());
    }
}