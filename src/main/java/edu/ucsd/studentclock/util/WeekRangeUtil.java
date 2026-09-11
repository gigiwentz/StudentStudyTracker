package edu.ucsd.studentclock.util;

import edu.ucsd.studentclock.config.WeekConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public final class WeekRangeUtil {
    private WeekRangeUtil() {}

    public static LocalDateTime getStartOfWeek(LocalDateTime now) {
        LocalDate firstDay = now.toLocalDate()
            .with(TemporalAdjusters.previousOrSame(WeekConfig.FIRST_DAY_OF_WEEK));
        return firstDay.atStartOfDay();
    }

    public static LocalDateTime getEndOfWeek(LocalDateTime now) {
        LocalDate lastDay = now.toLocalDate()
            .with(TemporalAdjusters.nextOrSame(WeekConfig.getLastDayOfWeek()));
        return lastDay.atTime(23, 59, 59);
    }

    public static LocalDateTime getEndOfWeekExclusive(LocalDateTime now) {
        return getStartOfWeek(now).plusWeeks(1);
    }
}