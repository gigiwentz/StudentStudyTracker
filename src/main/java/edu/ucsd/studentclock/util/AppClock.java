package edu.ucsd.studentclock.util;

import java.time.LocalDateTime;

/**
 * Clock that can use real time or a fixed mock time for testing.
 */
public final class AppClock implements Clock {
    private boolean useMock;
    private LocalDateTime mockNow;

    @Override
    public LocalDateTime now() {
        return useMock ? mockNow : LocalDateTime.now();
    }

    public boolean isUseMock() {
        return useMock;
    }

    public void setUseMock(boolean useMock) {
        this.useMock = useMock;
        if (useMock && mockNow == null) {
            mockNow = LocalDateTime.now();
        }
    }

    public void setMock(LocalDateTime dateTime) {
        this.mockNow = dateTime;
    }
}
