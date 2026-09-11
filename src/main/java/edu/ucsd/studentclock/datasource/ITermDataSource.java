package edu.ucsd.studentclock.datasource;

import edu.ucsd.studentclock.model.Term;
import edu.ucsd.studentclock.model.WorkSession;

import java.util.List;

/**
 * Contract for term and work-session storage (Lab 4 style).
 * Enables swapping in-memory or SQLite (Lab 4 style) without changing Presenter/View.
 */
public interface ITermDataSource {

    /** Load the current term; null if none stored yet. */
    Term loadTerm();

    /** Persist and use this term as current. */
    void saveTerm(Term term);

    /** All work sessions (read-only view). */
    List<WorkSession> getWorkSessions();

    /** Append a work session. Returns true if saved successfully, false on failure. */
    boolean addWorkSession(WorkSession session);
}
