package edu.ucsd.studentclock.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChartMathUtilTest {

    @Test
    void distanceToSegment_pointOnSegment_returnsZero() {
        double d = ChartMathUtil.distanceToSegment(5, 0, 0, 0, 10, 0);
        assertEquals(0.0, d, 1e-9);
    }

    @Test
    void distanceToSegment_pointAtEndpoint_returnsZero() {
        double d = ChartMathUtil.distanceToSegment(10, 0, 0, 0, 10, 0);
        assertEquals(0.0, d, 1e-9);
    }

    @Test
    void distanceToSegment_pointPerpendicularToMidpoint_returnsDistance() {
        double d = ChartMathUtil.distanceToSegment(5, 3, 0, 0, 10, 0);
        assertEquals(3.0, d, 1e-9);
    }

    @Test
    void distanceToSegment_pointBeyondSegment_returnsDistanceToNearestEndpoint() {
        double d = ChartMathUtil.distanceToSegment(15, 0, 0, 0, 10, 0);
        assertEquals(5.0, d, 1e-9);
    }

    @Test
    void distanceToSegment_pointBeforeSegment_returnsDistanceToStart() {
        double d = ChartMathUtil.distanceToSegment(-3, 0, 0, 0, 10, 0);
        assertEquals(3.0, d, 1e-9);
    }

    @Test
    void distanceToSegment_diagonalSegment_pointOnLine_returnsZero() {
        double on = ChartMathUtil.distanceToSegment(5, 5, 0, 0, 10, 10);
        assertEquals(0.0, on, 1e-9);
    }

    @Test
    void distanceToSegment_diagonalSegment_pointOffLine_returnsPerpendicularDistance() {
        double off = ChartMathUtil.distanceToSegment(5, 7, 0, 0, 10, 10);
        assertEquals(Math.sqrt(2), off, 1e-9);
    }

    @Test
    void distanceToSegment_degenerateSegment_singlePoint() {
        double d = ChartMathUtil.distanceToSegment(3, 4, 1, 1, 1, 1);
        assertEquals(Math.hypot(2, 3), d, 1e-9);
    }
}
