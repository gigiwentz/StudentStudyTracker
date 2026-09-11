package edu.ucsd.studentclock.util;

// format minutes as H:MM (grading wants this everywhere)
public final class TimeFormatUtil {

    private TimeFormatUtil() {
    }
    public static String formatMinutesToHHMM(int totalMinutes) {
        return formatMinutesToHHMM((long) totalMinutes);
    }

    public static String formatMinutesToHHMM(long totalMinutes) {
        long hours = totalMinutes / 60;
        int minutes = (int) (totalMinutes % 60);
        return String.format("%d:%02d", hours, minutes);
    }
}
