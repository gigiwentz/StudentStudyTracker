package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.datasource.ITermDataSource;
import edu.ucsd.studentclock.model.Term;
import edu.ucsd.studentclock.model.WorkSession;

import java.util.List;

/**
 * Repository delegates to a data source (Lab 4 style: depend on abstraction,
 * not concrete storage). Enables swapping InMemoryDataSource vs SqlDataSource in App.
 */
public class ExampleRepository {

    private final ITermDataSource dataSource;

    public ExampleRepository(ITermDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Term loadTerm() {
        return dataSource.loadTerm();
    }

    public void saveTerm(Term term) {
        dataSource.saveTerm(term);
    }

    public List<WorkSession> getWorkSessions() {
        return dataSource.getWorkSessions();
    }

    public boolean addWorkSession(WorkSession session) {
        if (session == null || session.getStart() == null) return false;
        return dataSource.addWorkSession(session);
    }
}
