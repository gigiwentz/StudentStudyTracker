package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents an assignment the student is tracking progress on.
 */
public class Assignment {
    private final String id;
    private String name;
    private final LocalDateTime dueDate;
    private long minutesEstimated;
    private long minutesWorked;
    private List<WorkSession> sessions = new ArrayList<>();

    public Assignment(String id, String name, LocalDateTime dueDate, long minutesEstimated) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = name;
        this.dueDate = dueDate;
        this.minutesEstimated = minutesEstimated;
        this.minutesWorked = 0L;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public long getMinutesEstimated() {
        return minutesEstimated;
    }

    public long getMinutesWorked() {
        return minutesWorked;
    }

    public void setMinutesWorked(long minutesWorked) {
        this.minutesWorked = minutesWorked;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addWorkSession(WorkSession session) {
        sessions.add(session);
        updateMinutesWorked(session);
    }

    /** Load work sessions from storage (e.g. after loadTerm). Keeps chronological order by end time. */
    public void setWorkSessions(List<WorkSession> sortedSessions) {
        sessions.clear();
        if (sortedSessions != null) sessions.addAll(sortedSessions);
    }

    public List<WorkSession> getWorkSessions() {
        return Collections.unmodifiableList(sessions);
    }

    public void updateMinutesWorked(WorkSession session) {
        minutesWorked += session.getDurationMinutes();
    }

    public long getMinutesLeft() {
        return Math.max(0L, minutesEstimated - minutesWorked);
    }
}