package edu.ucsd.studentclock.api;

public final class StudyHoursDto {
    private final String remainingHHMM;
    private final long remainingMinutes;

    public StudyHoursDto(String remainingHHMM, long remainingMinutes) {
        this.remainingHHMM = remainingHHMM;
        this.remainingMinutes = remainingMinutes;
    }

    public String getRemainingHHMM() { return remainingHHMM; }
    public long getRemainingMinutes() { return remainingMinutes; }
}
