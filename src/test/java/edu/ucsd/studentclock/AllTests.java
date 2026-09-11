package edu.ucsd.studentclock;

import edu.ucsd.studentclock.model.Term;
import edu.ucsd.studentclock.model.WorkSession;
import edu.ucsd.studentclock.service.StudyHoursService;
import edu.ucsd.studentclock.util.FakeClock;
import edu.ucsd.studentclock.util.TimeFormatUtil;
import edu.ucsd.studentclock.util.WeekRangeUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AllTests {

    private static final LocalDateTime WED_2026_02_04_12 = LocalDateTime.of(2026, 2, 4, 12, 0);

    // --- WeekRangeUtil ---
    @Test
    void givenWednesday_startOfWeekIsThatWeekMondayMidnight_endExclusiveIsNextMondayMidnight() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 4, 12, 0);
        assertEquals(LocalDateTime.of(2026, 2, 2, 0, 0), WeekRangeUtil.getStartOfWeek(now));
        assertEquals(LocalDateTime.of(2026, 2, 9, 0, 0), WeekRangeUtil.getEndOfWeekExclusive(now));
    }

    @Test
    void boundary_mondayMidnight_stillYieldsSameMondayAsStart() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 2, 0, 0);
        assertEquals(LocalDateTime.of(2026, 2, 2, 0, 0), WeekRangeUtil.getStartOfWeek(now));
        assertEquals(LocalDateTime.of(2026, 2, 9, 0, 0), WeekRangeUtil.getEndOfWeekExclusive(now));
    }

    // --- WorkSession duration ---
    @Test
    void workSession_endNull_returnsZeroDuration() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 4, 10, 0);
        assertEquals(0, new WorkSession(start, null).getDurationMinutes());
    }

    @Test
    void workSession_endEqualsStart_returnsZeroDuration() {
        LocalDateTime t = LocalDateTime.of(2026, 2, 4, 10, 0);
        assertEquals(0, new WorkSession(t, t).getDurationMinutes());
    }

    @Test
    void workSession_endBeforeStart_returnsZeroDuration() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 4, 10, 30);
        LocalDateTime end = LocalDateTime.of(2026, 2, 4, 10, 0);
        assertEquals(0, new WorkSession(start, end).getDurationMinutes());
    }

    @Test
    void workSession_normalDuration_30Minutes() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 4, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 4, 10, 30);
        assertEquals(30, new WorkSession(start, end).getDurationMinutes());
    }

    // --- StudyHoursService week filtering ---
    @Test
    void getWorkedMinutesThisWeek_filtersByStartInclusiveExclusive_andCountsDurations() {
        LocalDateTime startOfWeek = LocalDateTime.of(2026, 2, 2, 0, 0);
        LocalDateTime endExclusive = LocalDateTime.of(2026, 2, 9, 0, 0);
        WorkSession inRange60 = new WorkSession(startOfWeek, startOfWeek.plusMinutes(60));
        WorkSession lastMinute1 = new WorkSession(LocalDateTime.of(2026, 2, 8, 23, 59), LocalDateTime.of(2026, 2, 9, 0, 0));
        WorkSession atEndExclusive = new WorkSession(endExclusive, endExclusive.plusMinutes(60));
        WorkSession beforeWeek = new WorkSession(LocalDateTime.of(2026, 2, 1, 23, 59), LocalDateTime.of(2026, 2, 2, 0, 59));
        WorkSession inRangeNullEnd = new WorkSession(LocalDateTime.of(2026, 2, 3, 10, 0), null);
        List<WorkSession> sessions = List.of(inRange60, lastMinute1, atEndExclusive, beforeWeek, inRangeNullEnd);
        assertEquals(61, StudyHoursService.getWorkedMinutesThisWeek(sessions, WED_2026_02_04_12));
    }

    @Test
    void getRemainingMinutes_target120_worked30_returns90() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 2, 0, 0);
        WorkSession session = new WorkSession(start, start.plusMinutes(30));
        assertEquals(90, StudyHoursService.getRemainingMinutes(List.of(session), 120, WED_2026_02_04_12));
    }

    @Test
    void getRemainingMinutes_target120_worked130_returns0Clamped() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 2, 0, 0);
        WorkSession session = new WorkSession(start, start.plusMinutes(130));
        assertEquals(0, StudyHoursService.getRemainingMinutes(List.of(session), 120, WED_2026_02_04_12));
    }

    @Test
    void getRemainingHHMM_withTermUsesFakeClock() {
        Term term = new Term(120);
        LocalDateTime start = LocalDateTime.of(2026, 2, 2, 0, 0);
        WorkSession session = new WorkSession(start, start.plusMinutes(30));
        FakeClock clock = new FakeClock(WED_2026_02_04_12);
        assertEquals("1:30", StudyHoursService.getRemainingHHMM(term, List.of(session), clock));
    }

    // --- TimeFormatUtil ---
    @Test
    void formatMinutesToHHMM_65_returns1_05() {
        assertEquals("1:05", TimeFormatUtil.formatMinutesToHHMM(65));
    }
}
