package edu.ucsd.studentclock.api;

public final class ProgressItemDto {
    private final String assignmentId;
    private final String assignmentName;
    private final long loggedMinutes;

    public ProgressItemDto(String assignmentId, String assignmentName, long loggedMinutes) {
        this.assignmentId = assignmentId;
        this.assignmentName = assignmentName;
        this.loggedMinutes = loggedMinutes;
    }

    public String getAssignmentId() { return assignmentId; }
    public String getAssignmentName() { return assignmentName; }
    public long getLoggedMinutes() { return loggedMinutes; }
}
