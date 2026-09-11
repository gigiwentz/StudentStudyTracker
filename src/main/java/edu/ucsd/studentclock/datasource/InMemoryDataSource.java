package edu.ucsd.studentclock.datasource;

import edu.ucsd.studentclock.model.Term;
import edu.ucsd.studentclock.model.WorkSession;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory implementation of ITermDataSource (Lab 4 style).
 * Good for unit tests and no file/DB dependency.
 */
public class InMemoryDataSource implements ITermDataSource {

    private Term term;
    private final List<WorkSession> workSessions = new ArrayList<>();

    @Override
    public Term loadTerm() {
        return term;
    }

    @Override
    public void saveTerm(Term term) {
        this.term = term;
    }

    @Override
    public List<WorkSession> getWorkSessions() {
        return List.copyOf(workSessions);
    }

    @Override
    public boolean addWorkSession(WorkSession session) {
        if (session == null || session.getStart() == null) return false;
        workSessions.add(session);
        return true;
    }
}
