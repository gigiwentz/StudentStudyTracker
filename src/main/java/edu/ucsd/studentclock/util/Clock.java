package edu.ucsd.studentclock.util;

import java.time.LocalDateTime;

// so we can swap in mock time for tests/demo
public interface Clock {
    LocalDateTime now();
}
