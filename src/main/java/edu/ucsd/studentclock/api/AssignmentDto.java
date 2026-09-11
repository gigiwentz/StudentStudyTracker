package edu.ucsd.studentclock.api;

public final class AssignmentDto {
    private final String id;
    private final String name;

    public AssignmentDto(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
