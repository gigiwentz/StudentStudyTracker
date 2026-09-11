package edu.ucsd.studentclock.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for course colors.
 * Colors are assigned cyclically to new courses.
 */
public class CourseColorConfig {
    private static final List<String> DEFAULT_COLORS = Arrays.asList(
        "#4a90d9", // blue
        "#9b59b6", // purple
        "#27ae60", // green
        "#e67e22", // orange
        "#e74c3c"  // red
    );
    
    private static List<String> colors = new ArrayList<>(DEFAULT_COLORS);
    
    // Public immutable view - updated in static block
    public static List<String> COLORS;
    
    static {
        loadFromSystemProperties();
        COLORS = Collections.unmodifiableList(colors);
    }
    
    private static void loadFromSystemProperties() {
        String customColors = System.getProperty("course.colors");
        if (customColors != null && !customColors.isEmpty()) {
            String[] customArray = customColors.split(",");
            if (customArray.length > 0) {
                List<String> newColors = new ArrayList<>();
                for (String color : customArray) {
                    String trimmed = color.trim();
                    if (!trimmed.isEmpty()) {
                        newColors.add(trimmed);
                    }
                }
                if (!newColors.isEmpty()) {
                    colors = newColors;
                }
            }
        }
    }
    
    public static String getColorByIndex(int index) {
        if (colors.isEmpty()) {
            return DEFAULT_COLORS.get(0); // fallback to first default color
        }
        return colors.get(index % colors.size());
    }
    
    // Optional: method to reload configuration
    public static void reload() {
        loadFromSystemProperties();
        COLORS = Collections.unmodifiableList(colors);
    }
    
    private CourseColorConfig() {} // Prevent instantiation
}