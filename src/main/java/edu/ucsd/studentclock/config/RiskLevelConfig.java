package edu.ucsd.studentclock.config;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

public class RiskLevelConfig {
    private static final NavigableMap<Double, RiskLevel> LEVELS = new TreeMap<>();
    
    static {
        LEVELS.put(0.75, new RiskLevel(
            "#D30000", "75%+ behind", "75% or more behind expected progress"
        ));
        LEVELS.put(0.5, new RiskLevel(
            "#CC5500", "50-75% behind", "About 50-75% behind expected progress"
        ));
        LEVELS.put(0.25, new RiskLevel(
            "#FFF200", "25-50% behind", "About 25-50% behind expected progress"
        ));
        LEVELS.put(0.0, new RiskLevel(
            "#FFFFFF", "On track", "On track with expected progress"
        ));
    }
    
    public static RiskLevel getLevelForPercent(double percent) {
        return LEVELS.floorEntry(percent).getValue();
    }
    
    public static NavigableMap<Double, RiskLevel> getAllLevels() {
        return Collections.unmodifiableNavigableMap(LEVELS);
    }
    
    public static class RiskLevel {
        private final String color;
        private final String displayName;
        private final String tooltip;
        
        public RiskLevel(String color, String displayName, String tooltip) {
            this.color = color;
            this.displayName = displayName;
            this.tooltip = tooltip;
        }
        
        public String getColor() { return color; }
        public String getDisplayName() { return displayName; }
        public String getTooltip() { return tooltip; }
    }
}