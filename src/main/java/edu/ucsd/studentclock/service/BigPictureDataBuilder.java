package edu.ucsd.studentclock.service;

import edu.ucsd.studentclock.config.ViewStyleConfig;
import edu.ucsd.studentclock.config.CourseColorConfig;
import edu.ucsd.studentclock.facade.StudentClockFacade;
import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Course;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.model.WorkLog;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public final class BigPictureDataBuilder {
    private BigPictureDataBuilder() {}

    // ========== 内部类定义 ==========
    
    public static final class Range {
        private final LocalDate start;
        private final LocalDate end;

        public Range(LocalDate start, LocalDate end) {
            this.start = Objects.requireNonNull(start, "start");
            this.end = Objects.requireNonNull(end, "end");
        }

        public LocalDate getStart() { return start; }
        public LocalDate getEnd() { return end; }

        public LocalDateTime getStartDateTime() {
            return start.atStartOfDay();
        }

        public LocalDateTime getEndDateTime() {
            return end.atTime(LocalTime.of(23, 59, 59));
        }
    }

    public static final class LegendItem {
        private final String courseName;
        private final String color;

        public LegendItem(String courseName, String color) {
            this.courseName = courseName;
            this.color = color;
        }

        public String getCourseName() { return courseName; }
        public String getColor() { return color; }
    }

    public static final class Point {
        private final long epochMinutes;
        private final double remainingHours;

        public Point(long epochMinutes, double remainingHours) {
            this.epochMinutes = epochMinutes;
            this.remainingHours = remainingHours;
        }

        public long getEpochMinutes() { return epochMinutes; }
        public double getRemainingHours() { return remainingHours; }
    }

    public static final class AssignmentLine {
        private final String assignmentId;
        private final String title;
        private final String courseName;
        private final String courseColor;
        private final LocalDateTime startDate;
        private final LocalDateTime dueDate;
        private final double estimatedHours;
        private final double finishedHours;
        private final double remainingHours;
        private final double behindPercent;
        private final List<Point> points;
        private final List<String> segmentColors;
        private final List<String> segmentTooltips;

        public AssignmentLine(
                String assignmentId,
                String title,
                String courseName,
                String courseColor,
                LocalDateTime startDate,
                LocalDateTime dueDate,
                double estimatedHours,
                double finishedHours,
                double remainingHours,
                double behindPercent,
                List<Point> points,
                List<String> segmentColors,
                List<String> segmentTooltips
        ) {
            this.assignmentId = assignmentId;
            this.title = title;
            this.courseName = courseName;
            this.courseColor = courseColor;
            this.startDate = startDate;
            this.dueDate = dueDate;
            this.estimatedHours = estimatedHours;
            this.finishedHours = finishedHours;
            this.remainingHours = remainingHours;
            this.behindPercent = behindPercent;
            this.points = points != null ? new ArrayList<>(points) : new ArrayList<>();
            this.segmentColors = segmentColors != null ? new ArrayList<>(segmentColors) : new ArrayList<>();
            this.segmentTooltips = segmentTooltips != null ? new ArrayList<>(segmentTooltips) : new ArrayList<>();
        }

        public String getAssignmentId() { return assignmentId; }
        public String getTitle() { return title; }
        public String getCourseName() { return courseName; }
        public String getCourseColor() { return courseColor; }
        public LocalDateTime getStartDate() { return startDate; }
        public LocalDateTime getDueDate() { return dueDate; }
        public double getEstimatedHours() { return estimatedHours; }
        public double getFinishedHours() { return finishedHours; }
        public double getRemainingHours() { return remainingHours; }
        public double getBehindPercent() { return behindPercent; }
        public List<Point> getPoints() { return Collections.unmodifiableList(points); }
        public List<String> getSegmentColors() { return Collections.unmodifiableList(segmentColors); }
        public List<String> getSegmentTooltips() { return Collections.unmodifiableList(segmentTooltips); }
    }

    public static final class TargetLine {
        private final long xStartEpochMinutes;
        private final double yStart;
        private final long xEndEpochMinutes;
        private final double yEnd;

        public TargetLine(long xStartEpochMinutes, double yStart, long xEndEpochMinutes, double yEnd) {
            this.xStartEpochMinutes = xStartEpochMinutes;
            this.yStart = yStart;
            this.xEndEpochMinutes = xEndEpochMinutes;
            this.yEnd = yEnd;
        }

        public long getXStartEpochMinutes() { return xStartEpochMinutes; }
        public double getYStart() { return yStart; }
        public long getXEndEpochMinutes() { return xEndEpochMinutes; }
        public double getYEnd() { return yEnd; }
    }

    public static final class Result {
        private final Range range;
        private final List<LegendItem> legend;
        private final List<AssignmentLine> lines;
        private final List<Long> startMarkerEpochMinutes;
        private final double yMax;
        private final TargetLine targetLine;

        public Result(Range range, List<LegendItem> legend, List<AssignmentLine> lines,
                      List<Long> startMarkerEpochMinutes, double yMax, TargetLine targetLine) {
            this.range = range;
            this.legend = legend != null ? new ArrayList<>(legend) : new ArrayList<>();
            this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
            this.startMarkerEpochMinutes = startMarkerEpochMinutes != null ? new ArrayList<>(startMarkerEpochMinutes) : new ArrayList<>();
            this.yMax = yMax;
            this.targetLine = targetLine;
        }

        public Range getRange() { return range; }
        public List<LegendItem> getLegend() { return Collections.unmodifiableList(legend); }
        public List<AssignmentLine> getLines() { return Collections.unmodifiableList(lines); }
        public List<Long> getStartMarkerEpochMinutes() { return Collections.unmodifiableList(startMarkerEpochMinutes); }
        public double getYMax() { return yMax; }
        public TargetLine getTargetLine() { return targetLine; }
    }

    // ========== 常量定义 ==========
    
    private static final int RANGE_PADDING_DAYS = 2;
    private static final int FALLBACK_DAYS_BACK = 14;
    private static final int FALLBACK_DAYS_FORWARD = 60;

    // ========== 私有辅助类 ==========
    
    private static final class Event {
        final LocalDateTime time;
        final double remainingAfter;

        Event(LocalDateTime time, double remainingAfter) {
            this.time = time;
            this.remainingAfter = remainingAfter;
        }
    }

    private static final class AssignmentData {
        final String assignmentName;
        final LocalDateTime start;
        final LocalDateTime due;
        final double estimatedHours;
        final List<WorkLog> logsByEnd;
        final String courseColor;

        AssignmentData(String assignmentName, LocalDateTime start, LocalDateTime due, 
                      double estimatedHours, List<WorkLog> logsByEnd, String courseColor) {
            this.assignmentName = assignmentName != null ? assignmentName : "";
            this.start = start;
            this.due = due;
            this.estimatedHours = estimatedHours;
            this.logsByEnd = logsByEnd != null ? new ArrayList<>(logsByEnd) : new ArrayList<>();
            // 使用配置类中的 fallback 颜色
            this.courseColor = courseColor != null ? courseColor : ViewStyleConfig.COLOR_FALLBACK;
        }

        double getFinishedHours() {
            double hours = 0.0;
            for (WorkLog log : logsByEnd) {
                hours += log.getDurationMinutes() / 60.0;
            }
            return hours;
        }

        double getRemainingAt(LocalDateTime time) {
            if (time.toLocalDate().isBefore(start.toLocalDate()) || time.isAfter(due)) return 0.0;
            
            double finished = 0.0;
            for (WorkLog log : logsByEnd) {
                LocalDateTime end = log.getEnd();
                if (end == null || end.isAfter(time)) break;
                finished += log.getDurationMinutes() / 60.0;
            }
            return Math.max(0.0, estimatedHours - finished);
        }
    }

    private static final class AggregateLineResult {
        final List<Point> points;
        final List<String> segmentColors;
        final List<String> segmentTooltips;

        AggregateLineResult(List<Point> points, List<String> segmentColors, List<String> segmentTooltips) {
            this.points = points;
            this.segmentColors = segmentColors != null ? segmentColors : new ArrayList<>();
            this.segmentTooltips = segmentTooltips != null ? segmentTooltips : new ArrayList<>();
        }
    }

    // ========== 公开的 build 方法 ==========
    
    public static Result build(StudentClockFacade facade, LocalDateTime now) {
        Objects.requireNonNull(facade, "facade");
        Objects.requireNonNull(now, "now");

        LocalDateTime minStart = null;
        LocalDateTime maxDue = null;
        
        for (Series series : facade.getOpenAssignments()) {
            Assignment assignment = series.getActiveAssignment();
            if (assignment == null) continue;
            
            LocalDateTime startDate = series.getAssignmentStart();
            LocalDateTime dueDate = assignment.getDueDate();
            
            if (startDate == null || dueDate == null) continue;
            
            if (minStart == null || startDate.isBefore(minStart)) minStart = startDate;
            if (maxDue == null || dueDate.isAfter(maxDue)) maxDue = dueDate;
        }
        
        LocalDateTime rangeStart;
        LocalDateTime rangeEnd;
        
        if (minStart != null && maxDue != null) {
            rangeStart = minStart.toLocalDate().atStartOfDay();
            rangeEnd = maxDue.toLocalDate().plusDays(RANGE_PADDING_DAYS).atTime(LocalTime.of(23, 59, 59));
        } else {
            LocalDate today = now.toLocalDate();
            rangeStart = today.minusDays(FALLBACK_DAYS_BACK).atStartOfDay();
            rangeEnd = today.plusDays(FALLBACK_DAYS_FORWARD).atTime(LocalTime.of(23, 59, 59));
        }
        
        Range range = new Range(rangeStart.toLocalDate(), rangeEnd.toLocalDate());

        Map<String, LegendItem> legendByCourse = new LinkedHashMap<>();
        List<AssignmentData> assignmentDataList = new ArrayList<>();
        List<Long> startMarkerEpochMinutes = new ArrayList<>();
        LocalDateTime maxDueDate = null;

        for (Series series : facade.getOpenAssignments()) {
            Assignment assignment = series.getActiveAssignment();
            if (assignment == null) continue;

            Course course = findCourseForAssignment(facade, assignment.getId());
            String courseName = course != null ? course.getName() : "(Unknown course)";
            String courseColor = course != null ? course.getColor() : ViewStyleConfig.COLOR_FALLBACK;

            LocalDateTime startDate = series.getAssignmentStart();
            LocalDateTime dueDate = assignment.getDueDate();
            
            if (startDate == null || dueDate == null) continue;
            if (startDate.isAfter(rangeEnd)) continue;
            if (dueDate.isBefore(rangeStart)) continue;

            double estimatedHours = Math.max(0.0, assignment.getMinutesEstimated() / 60.0);
            if (estimatedHours <= 0.0) continue;

            List<WorkLog> logs = facade.getWorkLogsForAssignment(assignment.getId());
            List<WorkLog> logsInRange = new ArrayList<>();
            
            for (WorkLog log : logs) {
                if (log.getEnd() != null && !log.getEnd().isBefore(startDate) && !log.getEnd().isAfter(dueDate)) {
                    logsInRange.add(log);
                }
            }

            legendByCourse.putIfAbsent(courseName, new LegendItem(courseName, courseColor));
            
            if (maxDueDate == null || dueDate.isAfter(maxDueDate)) maxDueDate = dueDate;
            
            startMarkerEpochMinutes.add(toEpochMinutes(startDate));
            assignmentDataList.add(new AssignmentData(assignment.getName(), startDate, dueDate, 
                                                     estimatedHours, logsInRange, courseColor));
        }

        AggregateLineResult aggregate = buildAggregatePoints(assignmentDataList, rangeStart, rangeEnd, now);
        List<AssignmentLine> lines = new ArrayList<>();
        
        double totalEstimatedHours = 0.0;
        for (AssignmentData data : assignmentDataList) {
            totalEstimatedHours += data.estimatedHours;
        }
        
        double yMax = Math.max(1.0, totalEstimatedHours);
        
        if (!aggregate.points.isEmpty()) {
            double lastRemaining = aggregate.points.get(aggregate.points.size() - 1).getRemainingHours();
            lines.add(new AssignmentLine(
                    "",
                    "Total remaining",
                    "All",
                    ViewStyleConfig.COLOR_BLUE,  // 使用配置颜色
                    rangeStart,
                    maxDueDate != null ? maxDueDate : rangeEnd,
                    0.0,
                    0.0,
                    lastRemaining,
                    0.0,
                    aggregate.points,
                    aggregate.segmentColors,
                    aggregate.segmentTooltips
            ));
        }

        long rangeStartEpoch = toEpochMinutes(rangeStart);
        long endEpoch = maxDueDate != null ? toEpochMinutes(maxDueDate) : toEpochMinutes(rangeEnd);
        
        if (endEpoch < rangeStartEpoch) endEpoch = rangeStartEpoch;
        
        TargetLine targetLine = new TargetLine(rangeStartEpoch, yMax, endEpoch, 0.0);
        
        return new Result(range, new ArrayList<>(legendByCourse.values()), lines, 
                         startMarkerEpochMinutes, yMax, targetLine);
    }

    // ========== 私有辅助方法 ==========
    
    private static Course findCourseForAssignment(StudentClockFacade facade, String assignmentId) {
        for (Course course : facade.getTerm().getCourses()) {
            for (Series series : course.getSeries()) {
                for (Assignment assignment : series.getAssignments()) {
                    if (assignment != null && assignment.getId().equals(assignmentId)) return course;
                }
            }
        }
        return null;
    }

    private static AggregateLineResult buildAggregatePoints(
            List<AssignmentData> assignments,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            LocalDateTime now
    ) {
        if (assignments.isEmpty()) {
            return new AggregateLineResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        Set<LocalDateTime> eventTimes = new TreeSet<>();
        eventTimes.add(rangeStart);
        eventTimes.add(rangeEnd);
        eventTimes.add(now);
        
        for (AssignmentData data : assignments) {
            eventTimes.add(data.start.toLocalDate().atStartOfDay());
            eventTimes.add(data.due);
            for (WorkLog log : data.logsByEnd) {
                if (log.getEnd() != null) eventTimes.add(log.getEnd());
            }
        }

        List<Point> points = new ArrayList<>();
        LocalDateTime previousTime = null;
        double previousY = 0.0;
        
        for (LocalDateTime time : eventTimes) {
            if (time.isBefore(rangeStart) || time.isAfter(rangeEnd)) continue;
            
            double total = 0.0;
            for (AssignmentData data : assignments) {
                total += data.getRemainingAt(time);
            }
            total = Math.max(0.0, total);
            
            if (previousTime != null && time.isAfter(previousTime) && total > previousY) {
                points.add(new Point(toEpochMinutes(time), previousY));
            }
            
            points.add(new Point(toEpochMinutes(time), total));
            previousTime = time;
            previousY = total;
        }

        List<Point> out = new ArrayList<>();
        for (Point point : points) {
            long x = point.getEpochMinutes();
            double y = Math.max(0.0, point.getRemainingHours());
            
            if (!out.isEmpty()) {
                Point last = out.get(out.size() - 1);
                if (last.getEpochMinutes() == x && last.getRemainingHours() == y) continue;
            }
            out.add(new Point(x, y));
        }
        
        if (out.size() < 2) {
            out.clear();
            double y0 = 0.0;
            for (AssignmentData data : assignments) y0 += data.getRemainingAt(rangeStart);
            out.add(new Point(toEpochMinutes(rangeStart), y0));
            
            double y1 = 0.0;
            for (AssignmentData data : assignments) y1 += data.getRemainingAt(rangeEnd);
            out.add(new Point(toEpochMinutes(rangeEnd), y1));
        }

        long nowEpoch = toEpochMinutes(now);
        List<Point> clipped = new ArrayList<>();
        for (Point point : out) {
            if (point.getEpochMinutes() <= nowEpoch) clipped.add(point);
        }
        
        double totalAtNow = 0.0;
        for (AssignmentData data : assignments) totalAtNow += data.getRemainingAt(now);
        totalAtNow = Math.max(0.0, totalAtNow);
        
        if (clipped.isEmpty()) {
            clipped.add(new Point(toEpochMinutes(rangeStart), 0.0));
            clipped.add(new Point(nowEpoch, totalAtNow));
        } else if (clipped.get(clipped.size() - 1).getEpochMinutes() < nowEpoch) {
            clipped.add(new Point(nowEpoch, totalAtNow));
        }
        out = clipped;

        List<String> segmentColors = new ArrayList<>();
        List<String> segmentTooltips = new ArrayList<>();
        
        for (int i = 0; i < out.size() - 1; i++) {
            long timeEpoch = out.get(i + 1).getEpochMinutes();
            double segYPrev = out.get(i).getRemainingHours();
            double yCurrent = out.get(i + 1).getRemainingHours();
            
            List<Integer> indices = getEventAssignmentIndicesAt(assignments, timeEpoch, segYPrev, yCurrent);
            String color = buildSegmentColor(assignments, indices);
            String tooltip = buildSegmentTooltip(assignments, indices, segYPrev, yCurrent);
            
            segmentColors.add(color);
            segmentTooltips.add(tooltip);
        }
        
        return new AggregateLineResult(out, segmentColors, segmentTooltips);
    }

    private static String buildSegmentColor(List<AssignmentData> assignments, List<Integer> indices) {
        if (indices == null || indices.isEmpty()) return ViewStyleConfig.COLOR_GRAY;  // 使用配置颜色
        if (indices.size() == 1) return assignments.get(indices.get(0)).courseColor;
        
        StringBuilder sb = new StringBuilder("linear-gradient(to right, ");
        for (int k = 0; k < indices.size(); k++) {
            if (k > 0) sb.append(", ");
            sb.append(assignments.get(indices.get(k)).courseColor);
            sb.append(" ").append(100 * k / (indices.size() - 1)).append("%");
        }
        sb.append(")");
        return sb.toString();
    }

    private static String buildSegmentTooltip(List<AssignmentData> assignments, List<Integer> indices, 
                                             double segYPrev, double yCurrent) {
        if (indices == null || indices.isEmpty()) return "Work Session";
        
        if (indices.size() == 1) {
            AssignmentData data = assignments.get(indices.get(0));
            if (yCurrent > segYPrev) {
                return "New Assignment " + data.assignmentName + " starts";
            }
            return String.format(
                    "Work Session:\nworked on: %s\nestimated hours: %.1f\nhours finished: %.1f",
                    data.assignmentName, data.estimatedHours, data.getFinishedHours());
        }
        
        StringBuilder sb = new StringBuilder();
        if (yCurrent > segYPrev) {
            sb.append("New assignments start:\n");
        } else {
            sb.append("Assignments active (no work this segment):\n");
        }
        
        for (int index : indices) {
            AssignmentData data = assignments.get(index);
            sb.append("• ").append(data.assignmentName).append("\n");
        }
        
        return sb.toString().trim();
    }

    private static List<Integer> getEventAssignmentIndicesAt(
            List<AssignmentData> assignments,
            long timeEpochMinutes,
            double yPrev,
            double yCurrent
    ) {
        LocalDateTime time = fromEpochMinutes(timeEpochMinutes);
        List<Integer> result = new ArrayList<>();
        
        if (yCurrent < yPrev) {
            for (int j = 0; j < assignments.size(); j++) {
                for (WorkLog log : assignments.get(j).logsByEnd) {
                    if (log.getEnd() != null && toEpochMinutes(log.getEnd()) == timeEpochMinutes) {
                        result.add(j);
                        return result;
                    }
                }
            }
        }
        
        if (yCurrent > yPrev) {
            for (int j = 0; j < assignments.size(); j++) {
                if (toEpochMinutes(assignments.get(j).start.toLocalDate().atStartOfDay()) == timeEpochMinutes) {
                    result.add(j);
                }
            }
            if (!result.isEmpty()) return result;
        }
        
        for (int j = 0; j < assignments.size(); j++) {
            if (assignments.get(j).getRemainingAt(time) > 0) result.add(j);
        }
        
        return result;
    }

    private static LocalDateTime fromEpochMinutes(long epochMinutes) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochMinutes * 60), ZoneOffset.UTC);
    }

    private static long toEpochMinutes(LocalDateTime time) {
        return time.toEpochSecond(ZoneOffset.UTC) / 60;
    }

    private static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    private static double niceCeil(double v) {
        if (v <= 1.0) return 1.0;
        double pow = Math.pow(10, Math.floor(Math.log10(v)));
        double n = v / pow;
        double rounded = n <= 1 ? 1 : n <= 2 ? 2 : n <= 5 ? 5 : 10;
        return rounded * pow;
    }
}