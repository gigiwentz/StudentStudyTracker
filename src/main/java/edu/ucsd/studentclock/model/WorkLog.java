package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Represents one "log work" event: a single recorded study session for an assignment.
 * Stored in chronological order (by end time) and reused for charts and reporting.
 */
public class WorkLog {
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String assignmentId;

    public WorkLog(LocalDateTime start, LocalDateTime end, String assignmentId) {
        this.start = Objects.requireNonNull(start, "start");
        this.end = end;
        this.assignmentId = assignmentId;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    /** Best time for ordering: use end when available, else start. */
    public LocalDateTime getLogDate() {
        return end != null && end.isAfter(start) ? end : start;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public long getDurationMinutes() {
        if (end == null || !end.isAfter(start)) return 0;
        return ChronoUnit.MINUTES.between(start, end);
    }

    /** Create from persistence-layer WorkSession for reuse in domain/charts. */
    public static WorkLog fromWorkSession(WorkSession session) {
        if (session == null) return null;
        
        LocalDateTime end = session.getEnd();
        if (end == null && session.getDurationMinutes() > 0) {
            end = session.getStart().plusMinutes(session.getDurationMinutes());
        }
        
        return new WorkLog(session.getStart(), end, session.getAssignmentId());
    }

    /** To persist as WorkSession. */
    public WorkSession toWorkSession() {
        return new WorkSession(start, end, assignmentId);
    }
}