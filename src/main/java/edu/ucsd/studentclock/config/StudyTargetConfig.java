package edu.ucsd.studentclock.config;

public class StudyTargetConfig {
    public static final int DEFAULT_WEEKLY_HOURS = 10;
    public static final int DEFAULT_WEEKLY_MINUTES = DEFAULT_WEEKLY_HOURS * 60;
    public static final int MIN_WEEKLY_HOURS = 1;
    public static final int MAX_WEEKLY_HOURS = 168;
    
    // Status thresholds
    public static final double STATUS_COMPLETED_THRESHOLD = 1.0;
    public static final double STATUS_HIGH_THRESHOLD = 0.75;
    public static final double STATUS_MEDIUM_THRESHOLD = 0.5;
    
    // Status colors
    public static final String COLOR_COMPLETED = "#008000";
    public static final String COLOR_HIGH = "#FFF200";
    public static final String COLOR_MEDIUM = "#CC5500";
    public static final String COLOR_LOW = "#D30000";
    
    // Risk thresholds
    public static final double THRESHOLD_HIGH_RISK = 0.75;
    public static final double THRESHOLD_MEDIUM_RISK = 0.5;
    public static final double THRESHOLD_LOW_RISK = 0.25;
    
    public static final String COLOR_GREEN_DONE = "#0fa018";
}