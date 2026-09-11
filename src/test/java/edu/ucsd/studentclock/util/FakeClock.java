package edu.ucsd.studentclock.util;

import java.time.LocalDateTime;

/** Fixed time for deterministic tests. */
public final class FakeClock implements Clock {
    private LocalDateTime t;

    public FakeClock(LocalDateTime t) {
        this.t = t;
    }

    @Override
    public LocalDateTime now() {
        return t;
    }

    public void set(LocalDateTime t) {
        this.t = t;
    }
}
