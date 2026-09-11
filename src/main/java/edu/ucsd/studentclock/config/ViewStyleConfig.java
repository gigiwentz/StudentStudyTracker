package edu.ucsd.studentclock.config;

public class ViewStyleConfig {
    // ========== Chart Styles ==========
    public static final String CHART_TARGET_LINE_STYLE = 
        getProperty("chart.target.line.style", "-fx-stroke: #B8860B; -fx-stroke-width: 3px; -fx-stroke-dash-array: 10 5;");
    
    public static final String CHART_SEGMENT_STYLE = 
        getProperty("chart.segment.style", "-fx-stroke: %s; -fx-stroke-width: 3px;");
    
    public static final String CHART_DOT_STYLE = 
        getProperty("chart.dot.style", "-fx-fill: #333333; -fx-stroke: #ffffff; -fx-stroke-width: 1.5px;");
    
    // ========== Colors ==========
    public static final String COLOR_GRAY = 
        getProperty("color.gray", "#666666");
    
    public static final String COLOR_BLUE = 
        getProperty("color.blue", "#2563eb");
    
    public static final String COLOR_DOT_FILL = 
        getProperty("color.dot.fill", "#333333");
    
    public static final String COLOR_DOT_STROKE = 
        getProperty("color.dot.stroke", "#ffffff");
    
    public static final String COLOR_TARGET_LINE = 
        getProperty("color.target.line", "#B8860B");
    
    public static final String COLOR_FALLBACK = 
        getProperty("color.fallback", "#cccccc");
    
    public static final String COLOR_BLACK = 
        getProperty("color.black", "#000000");
    
    public static final String COLOR_DARK_GRAY = 
        getProperty("color.dark.gray", "#555555");
    
    public static final String COLOR_LIGHT_GRAY = 
        getProperty("color.light.gray", "#D3D3D3");
    
    public static final String COLOR_GREEN_DONE = 
        getProperty("color.green.done", "#0fa018");
    
    public static final String COLOR_WHITE = 
        getProperty("color.white", "#FFFFFF");
    
    public static final String COLOR_BORDER_GRAY = 
        getProperty("color.border.gray", "#D3D3D3");
    
    public static final String COLOR_PAST_DUE = 
        getProperty("color.past.due", "#cccccc");
    
    public static final String COLOR_DEFAULT_BACKGROUND = 
        getProperty("color.default.background", "#FFFFFF");
    
    // ========== Font Sizes ==========
    public static final int FONT_SIZE_TITLE = 
        Integer.parseInt(getProperty("font.size.title", "20"));
    
    public static final int FONT_SIZE_NORMAL = 
        Integer.parseInt(getProperty("font.size.normal", "14"));
    
    public static final int FONT_SIZE_SMALL = 
        Integer.parseInt(getProperty("font.size.small", "11"));
    
    public static final int FONT_SIZE_LABEL = 
        Integer.parseInt(getProperty("font.size.label", "12"));
    
    // ========== Spacing ==========
    public static final int HEADER_SPACING = 
        Integer.parseInt(getProperty("layout.header.spacing", "15"));
    
    public static final int HEADER_PADDING = 
        Integer.parseInt(getProperty("layout.header.padding", "20"));
    
    public static final int NODE_SPACING = 
        Integer.parseInt(getProperty("layout.node.spacing", "6"));
    
    public static final int SPACING_SMALL = 
        Integer.parseInt(getProperty("spacing.small", "5"));
    
    public static final int SPACING_MEDIUM = 
        Integer.parseInt(getProperty("spacing.medium", "10"));
    
    public static final int SPACING_LARGE = 
        Integer.parseInt(getProperty("spacing.large", "15"));
    
    // ========== Radius ==========
    public static final int RADIUS_DOT = 
        Integer.parseInt(getProperty("radius.dot", "5"));
    
    public static final int RADIUS_LEGEND_DOT = 
        Integer.parseInt(getProperty("radius.legend.dot", "6"));
    
    // ========== Assignment Dot Styles ==========
    public static final String ASSIGNMENT_DOT_STYLE = 
        getProperty("style.assignment.dot", "-fx-background-color: %s; -fx-background-radius: 5px;");
    
    public static final double ASSIGNMENT_DOT_SIZE = 
        Double.parseDouble(getProperty("style.assignment.dot.size", "10"));
    
    // ========== Tree Node Format ==========
    public static final String TREE_NODE_TEXT_FORMAT = 
        getProperty("style.tree.node.format", "%s | Due Date: %s | Minutes Left: %d");
    
    // ========== Helper ==========
    private static String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
    
    private ViewStyleConfig() {}
}