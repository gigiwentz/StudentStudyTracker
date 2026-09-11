package edu.ucsd.studentclock.config;

import java.time.format.DateTimeFormatter;

/**
 * Centralized configuration for all date/time formats used in the application.
 * Makes it easy to change formats across the entire app.
 */
public class DateTimeFormatConfig {
    // Long date format: "March 11, 2026"
    public static final DateTimeFormatter DATE_LONG = 
        DateTimeFormatter.ofPattern(getProperty("format.date.long", "MMMM d, yyyy"));
    
    // Short date format: "Mar 11"
    public static final DateTimeFormatter DATE_SHORT = 
        DateTimeFormatter.ofPattern(getProperty("format.date.short", "MMM d"));
    
    // Time format: "3:45 PM"
    public static final DateTimeFormatter TIME_SHORT = 
        DateTimeFormatter.ofPattern(getProperty("format.time.short", "h:mm a"));
    
    // Full date-time format: "2026-03-11 15:45"
    public static final DateTimeFormatter DATE_TIME_FULL = 
        DateTimeFormatter.ofPattern(getProperty("format.datetime.full", "yyyy-MM-dd HH:mm"));
    
    // For tooltips: "Mar 11, 2026"
    public static final DateTimeFormatter TOOLTIP_DATE = 
        DateTimeFormatter.ofPattern(getProperty("format.tooltip.date", "MMM d, yyyy"));
    
    private static String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
    
    // Prevent instantiation
    private DateTimeFormatConfig() {}
}