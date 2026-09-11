package edu.ucsd.studentclock.util;

/**
 * Pure math helpers for chart logic (e.g. point-to-segment distance).
 * No UI dependencies so tests can run headless.
 */
public final class ChartMathUtil {

    private ChartMathUtil() {}

    /** Point-to-segment distance: from (px, py) to line segment (x1,y1)-(x2,y2). */
    public static double distanceToSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        double len2 = dx * dx + dy * dy;
        if (len2 <= 0) return Math.hypot(px - x1, py - y1);
        double t = Math.max(0, Math.min(1, ((px - x1) * dx + (py - y1) * dy) / len2));
        double qx = x1 + t * dx, qy = y1 + t * dy;
        return Math.hypot(px - qx, py - qy);
    }
}
