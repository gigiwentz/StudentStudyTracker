package edu.ucsd.studentclock.config;

import java.time.DayOfWeek;

/**
 * Configuration for week-related calculations.
 * Allows customizing which day is considered the start of the week.
 */
public class WeekConfig {
    public static final DayOfWeek FIRST_DAY_OF_WEEK;
    
    static {
        String property = System.getProperty("week.start.day", "MONDAY");
        DayOfWeek configuredDay;
        try {
            configuredDay = DayOfWeek.valueOf(property.toUpperCase());
        } catch (IllegalArgumentException e) {
            configuredDay = DayOfWeek.MONDAY; // default to Monday
        }
        FIRST_DAY_OF_WEEK = configuredDay;
    }
    
    public static DayOfWeek getLastDayOfWeek() {
        // Calculate last day based on first day
        int lastDayValue = FIRST_DAY_OF_WEEK.getValue() + 6;
        if (lastDayValue > 7) {
            lastDayValue -= 7;
        }
        return DayOfWeek.of(lastDayValue);
    }
    
    public static boolean isFirstDayOfWeek(DayOfWeek day) {
        return day == FIRST_DAY_OF_WEEK;
    }
    
    private WeekConfig() {}
}