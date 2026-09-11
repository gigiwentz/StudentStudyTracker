package edu.ucsd.studentclock.datasource;

import edu.ucsd.studentclock.model.Term;
import edu.ucsd.studentclock.model.WorkSession;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryDataSourceTest {

    private static WorkSession makeSession() {
        return new WorkSession(
                LocalDateTime.of(2026, 2, 12, 10, 0),
                LocalDateTime.of(2026, 2, 12, 10, 15)
        );
    }

    @Test
    void loadTerm_initiallyReturnsNull() {
        InMemoryDataSource ds = new InMemoryDataSource();
        assertNull(ds.loadTerm());
    }

    @Test
    void saveTerm_thenLoadTerm_returnsSameTerm() {
        InMemoryDataSource ds = new InMemoryDataSource();
        Term term = new Term(300);
        ds.saveTerm(term);
        assertSame(term, ds.loadTerm());
    }

    @Test
    void getWorkSessions_initiallyEmpty() {
        InMemoryDataSource ds = new InMemoryDataSource();
        assertTrue(ds.getWorkSessions().isEmpty());
    }

    @Test
    void addWorkSession_validSession_storedAndReturnedByGetWorkSessions() {
        InMemoryDataSource ds = new InMemoryDataSource();
        WorkSession session = makeSession();
        ds.addWorkSession(session);
        List<WorkSession> list = ds.getWorkSessions();
        assertEquals(1, list.size());
        assertEquals(session.getStart(), list.get(0).getStart());
        assertEquals(session.getEnd(), list.get(0).getEnd());
    }

    @Test
    void addWorkSession_null_ignored() {
        InMemoryDataSource ds = new InMemoryDataSource();
        ds.addWorkSession(null);
        assertTrue(ds.getWorkSessions().isEmpty());
    }

    @Test
    void addWorkSession_nullStart_ignored() {
        InMemoryDataSource ds = new InMemoryDataSource();
        ds.addWorkSession(new WorkSession(null, LocalDateTime.now()));
        assertTrue(ds.getWorkSessions().isEmpty());
    }

    @Test
    void getWorkSessions_returnsCopy_notModifiable() {
        InMemoryDataSource ds = new InMemoryDataSource();
        ds.addWorkSession(makeSession());
        List<WorkSession> list = ds.getWorkSessions();
        assertThrows(UnsupportedOperationException.class, () -> list.add(makeSession()));
    }

    @Test
    void addWorkSession_multipleSessions_allStored() {
        InMemoryDataSource ds = new InMemoryDataSource();

        WorkSession s1 = makeSession();
        WorkSession s2 = new WorkSession(
                LocalDateTime.of(2026, 2, 12, 11, 0),
                LocalDateTime.of(2026, 2, 12, 11, 30)
        );

        ds.addWorkSession(s1);
        ds.addWorkSession(s2);

        List<WorkSession> sessions = ds.getWorkSessions();
        assertEquals(2, sessions.size());
    }

    @Test
    void getWorkSessions_modifyingReturnedList_doesNotAffectInternalState() {
        InMemoryDataSource ds = new InMemoryDataSource();
        ds.addWorkSession(makeSession());

        List<WorkSession> list = ds.getWorkSessions();

        assertThrows(UnsupportedOperationException.class,
                () -> list.clear());

        assertEquals(1, ds.getWorkSessions().size());
    }
}
