package edu.ucsd.studentclock.service;

import edu.ucsd.studentclock.model.Term;
import edu.ucsd.studentclock.model.WorkSession;
import edu.ucsd.studentclock.util.Clock;
import edu.ucsd.studentclock.util.TimeFormatUtil;
import edu.ucsd.studentclock.util.WeekRangeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class StudyHoursService {
    private StudyHoursService() {}

    public static long getWorkedMinutesThisWeek(List<WorkSession> sessions, LocalDateTime now) {
        LocalDateTime startOfWeek = WeekRangeUtil.getStartOfWeek(now);
        LocalDateTime endExclusive = WeekRangeUtil.getEndOfWeekExclusive(now);
        
        return sessions.stream()
                .filter(session -> !session.getStart().isBefore(startOfWeek) && 
                                  session.getStart().isBefore(endExclusive))
                .mapToLong(WorkSession::getDurationMinutes)
                .sum();
    }

    public static long getRemainingMinutes(List<WorkSession> sessions, int weeklyTargetMinutes, LocalDateTime now) {
        long worked = getWorkedMinutesThisWeek(sessions, now);
        return Math.max(0, weeklyTargetMinutes - worked);
    }

    public static String getRemainingHHMM(List<WorkSession> sessions, int weeklyTargetMinutes, LocalDateTime now) {
        long remaining = getRemainingMinutes(sessions, weeklyTargetMinutes, now);
        return TimeFormatUtil.formatMinutesToHHMM(remaining);
    }

    public static String getRemainingHHMM(Term term, List<WorkSession> sessions, Clock clock) {
        return getRemainingHHMM(sessions, term.getWeeklyTargetMinutes(), clock.now());
    }

    public static Map<String, Long> getLoggedMinutesByAssignment(List<WorkSession> sessions) {
        return sessions.stream()
                .filter(session -> session.getAssignmentId() != null && !session.getAssignmentId().isEmpty())
                .collect(Collectors.groupingBy(WorkSession::getAssignmentId,
                        Collectors.summingLong(WorkSession::getDurationMinutes)));
    }
}