package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class WorkSession {
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String assignmentId;
    private final String courseName;
    private final long durationMinutes;

    public WorkSession(LocalDateTime start, LocalDateTime end) {
        this(start, end, null);
    }

    public WorkSession(LocalDateTime start, LocalDateTime end, String assignmentId) {
        this(start, end, assignmentId, null, -1L);
    }

    /** Full constructor for persistence: course and duration stored for plot. */
    public WorkSession(LocalDateTime start, LocalDateTime end, String assignmentId, 
                      String courseName, long durationMinutes) {
        this.start = start;
        this.end = end;
        this.assignmentId = assignmentId;
        this.courseName = courseName;
        this.durationMinutes = durationMinutes >= 0 ? durationMinutes : -1L;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    /** Stored duration if set (>= 0), else computed from start/end. */
    public long getDurationMinutes() {
        if (durationMinutes >= 0) return durationMinutes;
        if (end == null) return 0;
        if (!end.isAfter(start)) return 0;
        return ChronoUnit.MINUTES.between(start, end);
    }

    /** Optional assignment this session is attributed to; null if general work. */
    public String getAssignmentId() {
        return assignmentId;
    }

    /** Course name for this assignment; null if unknown. Persisted for plot. */
    public String getCourseName() {
        return courseName;
    }
}