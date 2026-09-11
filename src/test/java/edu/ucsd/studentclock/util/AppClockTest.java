package edu.ucsd.studentclock.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppClockTest {

    @Test
    void whenUseMockFalse_nowReturnsSystemTime() {
        AppClock clock = new AppClock();
        clock.setUseMock(false);
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime result = clock.now();
        LocalDateTime after = LocalDateTime.now();
        assertFalse(clock.isUseMock());
        assertTrue(!result.isBefore(before) && !result.isAfter(after));
    }

    @Test
    void whenUseMockTrue_andSetMock_nowReturnsSetValue() {
        AppClock clock = new AppClock();
        LocalDateTime fixed = LocalDateTime.of(2026, 2, 15, 14, 30);
        clock.setUseMock(true);
        clock.setMock(fixed);
        assertTrue(clock.isUseMock());
        assertEquals(fixed, clock.now());
    }

    @Test
    void setUseMockTogglesIsUseMock() {
        AppClock clock = new AppClock();
        clock.setUseMock(true);
        assertTrue(clock.isUseMock());
        clock.setUseMock(false);
        assertFalse(clock.isUseMock());
    }

    @Test
    void setMockUpdatesReturnedTimeWhenUseMockTrue() {
        AppClock clock = new AppClock();
        clock.setUseMock(true);
        clock.setMock(LocalDateTime.of(2026, 1, 1, 12, 0));
        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), clock.now());
        clock.setMock(LocalDateTime.of(2026, 3, 10, 9, 15));
        assertEquals(LocalDateTime.of(2026, 3, 10, 9, 15), clock.now());
    }
}
