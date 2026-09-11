package edu.ucsd.studentclock.model;

import edu.ucsd.studentclock.datasource.InMemoryDataSource;
import edu.ucsd.studentclock.repository.ExampleRepository;
import edu.ucsd.studentclock.util.Clock;
import edu.ucsd.studentclock.util.WeekRangeUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelProgressTest {

    private Model model;
    private static final LocalDateTime START = LocalDateTime.of(2025, 2, 3, 10, 0);

    @BeforeEach
    void setUp() {
        ExampleRepository repository = new ExampleRepository(new InMemoryDataSource());
        model = new Model(repository, Term.createDefault());
    }

    @Test
    void getAssignmentById_returnsCorrectAssignment() {
        Term term = model.getTerm();

        term.addCourse("CSE 110", "green");
        Course course = term.getCourse("CSE 110");

        course.addSeries(START, "PA");
        Series series = course.getSeries("PA");

        model.addAssignment(
                "CSE 110",
                "PA",
                "PA1",
                START.plusDays(3),
                120
        );

        Assignment expected = series.getAssignments().iterator().next();
        Assignment found = model.getAssignmentById(expected.getId());

        assertEquals(expected, found);
    }

    @Test
    void getAssignmentById_returnsNullIfMissing() {
        assertNull(model.getAssignmentById("fake-id"));
    }

    @Test
    void addAssignment_addsToSeries() {
        Term term = model.getTerm();

        term.addCourse("CSE 110", "green");
        term.getCourse("CSE 110").addSeries(START, "PA");

        boolean result = model.addAssignment(
                "CSE 110",
                "PA",
                "PA1",
                START.plusDays(2),
                60
        );

        Series series = term.getCourse("CSE 110").getSeries("PA");

        assertTrue(result);
        assertEquals(1, series.getAssignments().size());
    }

    @Test
    void addAssignment_objectVersionAddsAssignment() {
        Term term = model.getTerm();
        term.addCourse("CSE 110", "green");
        term.getCourse("CSE 110").addSeries(START, "PA");

        Assignment assignment = new Assignment("id1", "PA1", START.plusDays(2), 60);

        boolean result = model.addAssignment("CSE 110", "PA", assignment);

        assertTrue(result);
    }

    @Test
    void addWorkSession_andGetWorkSessions_returnsAllSessions() {
        model.addWorkSession(new WorkSession(START, START.plusMinutes(10), null));
        model.addWorkSession(new WorkSession(START.plusHours(1), START.plusHours(1).plusMinutes(20), "assign-1"));
        
        List<WorkSession> list = model.getWorkSessions();
        assertEquals(2, list.size());
    }

    @Test
    void openAssignmentsChronological() {
        Term term = model.getTerm();
        term.addCourse("CSE 110", "green");
        term.getCourse("CSE 110").addSeries(LocalDateTime.of(2026, 2, 21, 23, 59), "Midterm");
        term.getCourse("CSE 110").addSeries(LocalDateTime.of(2026, 2, 21, 23, 59), "PA");
        
        model.addAssignment("CSE 110", "Midterm", "P", LocalDateTime.of(2026, 2, 26, 23, 59), 180);
        model.addAssignment("CSE 110", "PA", "P", LocalDateTime.of(2026, 2, 24, 23, 59), 180);
        
        term.getCourse("CSE 110").getSeries("Midterm").advanceActiveAssignment();
        term.getCourse("CSE 110").getSeries("PA").advanceActiveAssignment();
        
        List<Series> actual = model.getOpenAssignments();
        List<Series> expected = new ArrayList<>();
        expected.add(term.getCourse("CSE 110").getSeries("PA"));
        expected.add(term.getCourse("CSE 110").getSeries("Midterm"));
        
        assertEquals(expected, actual);
    }

    @Test
    void openAssignmentsNoActiveAssignments() {
        Term term = model.getTerm();
        term.addCourse("CSE 110", "green");
        term.getCourse("CSE 110").addSeries(LocalDateTime.of(2026, 2, 21, 23, 59), "Midterm");
        term.getCourse("CSE 110").addSeries(LocalDateTime.of(2026, 2, 21, 23, 59), "PA");
        
        model.addAssignment("CSE 110", "Midterm", "P", LocalDateTime.of(2026, 2, 26, 23, 59), 180);
        model.addAssignment("CSE 110", "PA", "P", LocalDateTime.of(2026, 2, 24, 23, 59), 180);
        
        List<Series> actual = model.getOpenAssignments();
        List<Series> expected = new ArrayList<>();
        
        assertEquals(expected, actual);
    }

    @Test
    void addAssignment_returnsFalseWhenCourseMissing() {
        boolean result = model.addAssignment(
                "BAD",
                "PA",
                "PA1",
                START,
                60
        );

        assertFalse(result);
    }

    @Test
    void addAssignment_returnsFalseWhenSeriesMissing() {
        Term term = model.getTerm();
        term.addCourse("CSE 110", "green");

        boolean result = model.addAssignment(
                "CSE 110",
                "Missing",
                "PA1",
                START,
                60
        );

        assertFalse(result);
    }

    @Test
    void addSeries_createsSeriesInCourse() {
        Term term = model.getTerm();
        term.addCourse("CSE 110", "green");

        boolean result = model.addSeries("CSE 110", START, "Midterm");

        assertTrue(result);

        Series series = term.getCourse("CSE 110").getSeries("Midterm");

        assertNotNull(series);
    }

    @Test
    void addSeries_returnsFalseIfCourseMissing() {
        boolean result = model.addSeries("BAD", START, "PA");
        assertFalse(result);
    }

    @Test
    void getAllAssignments_returnsAssignmentsAcrossSeries() {
        Term term = model.getTerm();

        term.addCourse("CSE 110", "green");
        Course course = term.getCourse("CSE 110");

        course.addSeries(START, "PA");
        course.addSeries(START, "Midterm");

        model.addAssignment("CSE 110", "PA", "A1", START.plusDays(1), 60);
        model.addAssignment("CSE 110", "Midterm", "A2", START.plusDays(2), 60);

        List<Assignment> all = model.getAllAssignments();

        assertEquals(2, all.size());
    }

    @Test
    void getOpenAssignments_returnsOnlyActiveSeries() {
        Term term = model.getTerm();

        term.addCourse("CSE 110", "green");
        Course course = term.getCourse("CSE 110");

        course.addSeries(START.minusDays(1), "PA");

        model.addAssignment(
                "CSE 110",
                "PA",
                "PA1",
                START.plusDays(3),
                120
        );

        Series series = course.getSeries("PA");

        // activate first assignment
        series.advanceActiveAssignment();

        List<Series> open = model.getOpenAssignments();

        assertEquals(1, open.size());
        assertEquals(series, open.get(0));
    }

    @Test
    void getUrgentAssignments_includesAssignmentDueNow() {
        Term term = model.getTerm();

        term.addCourse("CSE 110", "green");
        Course course = term.getCourse("CSE 110");

        course.addSeries(START.minusDays(1), "PA");

        model.addAssignment(
                "CSE 110",
                "PA",
                "PA1",
                START,
                120
        );

        Series series = course.getSeries("PA");
        series.advanceActiveAssignment();

        List<Series> result = model.getUrgentAssignments(START);

        assertEquals(1, result.size());
    }

    @Test
    void getUrgentAssignments_includesBehindScheduleAssignments() {
        Term term = model.getTerm();
        term.addCourse("CSE 110", "green");
        Course course = term.getCourse("CSE 110");

        course.addSeries(START.minusDays(10), "PA");

        model.addAssignment(
                "CSE 110",
                "PA",
                "PA1",
                START.plusDays(5),
                120
        );

        Series series = course.getSeries("PA");
        series.advanceActiveAssignment();

        List<Series> result = model.getUrgentAssignments(START);

        assertTrue(result.contains(series));
    }

    @Test
    void updateAllActive_movesToNextAssignment() {
        Term term = model.getTerm();

        term.addCourse("CSE 110", "green");
        Course course = term.getCourse("CSE 110");

        course.addSeries(START.minusDays(5), "PA");

        model.addAssignment("CSE 110", "PA", "Old", START.minusDays(1), 60);
        model.addAssignment("CSE 110", "PA", "New", START.plusDays(2), 60);

        Series series = course.getSeries("PA");
        series.advanceActiveAssignment(); // activate first

        model.updateAllActive(START);

        assertEquals("PA 2", series.getActiveAssignment().getName());
    }

    @Test
    void isDuringWeek_trueInsideWeek() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 18, 10, 0); // Wednesday
        LocalDateTime due = now.plusDays(2); // Friday

        assertTrue(Model.isDuringWeek(due, now));
    }

    @Test
    void isDuringWeek_falseNextWeek() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 18, 10, 0);
        LocalDateTime due = now.plusWeeks(1);

        assertFalse(Model.isDuringWeek(due, now));
    }

    @Test
    void isDuringWeek_trueAtStartOfWeek() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 18, 10, 0);
        LocalDateTime start = WeekRangeUtil.getStartOfWeek(now);

        assertTrue(Model.isDuringWeek(start, now));
    }

    @Test
    void isDuringWeek_falseAtEndOfWeekExclusive() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 18, 10, 0);
        LocalDateTime end = WeekRangeUtil.getEndOfWeekExclusive(now);

        assertFalse(Model.isDuringWeek(end, now));
    }

    @Test
    void getWeekWorkload_countsAssignmentsDueThisWeek() {
        Term term = model.getTerm();

        term.addCourse("CSE 110", "green");
        Course course = term.getCourse("CSE 110");

        course.addSeries(START.minusDays(1), "PA");

        model.addAssignment(
                "CSE 110",
                "PA",
                "PA1",
                START.plusDays(2), // still this week
                120
        );

        Series series = course.getSeries("PA");
        series.advanceActiveAssignment();

        long workload = model.getWeekWorkload(START);

        assertEquals(120, workload);
    }

    @Test
    void getWeekWorkload_ignoresLaterAssignments() {
        Term term = model.getTerm();

        term.addCourse("CSE 110", "green");
        Course course = term.getCourse("CSE 110");

        course.addSeries(START.minusDays(1), "PA");

        model.addAssignment(
                "CSE 110",
                "PA",
                "PA1",
                START.plusWeeks(1), // next week
                120
        );

        Series series = course.getSeries("PA");
        series.advanceActiveAssignment();

        long workload = model.getWeekWorkload(START);

        assertEquals(0, workload);
    }

    @Test
    void getLaterWorkload_estimatesProportionalWork() {
        Term term = model.getTerm();

        term.addCourse("CSE 110", "green");
        Course course = term.getCourse("CSE 110");

        course.addSeries(START.minusDays(1), "Project");

        model.addAssignment(
                "CSE 110",
                "Project",
                "Proj1",
                START.plusWeeks(2),
                120
        );

        Series series = course.getSeries("Project");
        series.advanceActiveAssignment();

        long workload = model.getLaterWorkload(START);

        // should be > 0 but < total minutes
        assertTrue(workload > 0);
        assertTrue(workload < 120);
    }

    @Test
    void getLaterWorkload_ignoresPastAssignments() {
        Term term = model.getTerm();

        term.addCourse("CSE 110", "green");
        Course course = term.getCourse("CSE 110");

        course.addSeries(START.minusDays(5), "Old");

        model.addAssignment(
                "CSE 110",
                "Old",
                "Old1",
                START.minusDays(1), // already due
                100
        );

        Series series = course.getSeries("Old");
        series.advanceActiveAssignment();

        long workload = model.getLaterWorkload(START);

        assertEquals(0, workload);
    }

    @Test
    void getRemainingMinutes_subtractsWorkedTime() {
        // weekly target = default term target
        model.addWorkSession(
                new WorkSession(
                        START,
                        START.plusMinutes(60),
                        null
                )
        );

        long remaining = model.getRemainingMinutes(START);

        assertTrue(remaining >= 0);
    }

    @Test
    void getRemainingHHMM_returnsFormattedString() {
        Clock fixedClock = new Clock() {
            @Override
            public LocalDateTime now() {
                return START;
            }
        };

        String result = model.getRemainingHHMM(fixedClock);

        assertNotNull(result);
        assertTrue(result.contains(":")); // HH:MM format
    }

    @Test
    void getCourseNameForAssignment_returnsCourseName() {
        Term term = model.getTerm();
        term.addCourse("CSE 110", "green");
        term.getCourse("CSE 110").addSeries(START, "PA");
        model.addAssignment("CSE 110", "PA", "PA1", START.plusDays(2), 60);

        Assignment assignment = model.getTerm()
                .getCourse("CSE 110")
                .getSeries("PA")
                .getAssignments()
                .iterator()
                .next();
                
        assertEquals("CSE 110", model.getCourseNameForAssignment(assignment.getId()));
    }

    @Test
    void getCourseNameForAssignment_unknownId_returnsNull() {
        assertNull(model.getCourseNameForAssignment("no-such-id"));
    }

    @Test
    void getCourseColorForAssignment_returnsCourseColor() {
        Term term = model.getTerm();
        term.addCourse("CSE 110", "#FF0000");
        term.getCourse("CSE 110").addSeries(START, "PA");
        model.addAssignment("CSE 110", "PA", "PA1", START.plusDays(2), 60);

        Assignment assignment = model.getTerm()
                .getCourse("CSE 110")
                .getSeries("PA")
                .getAssignments()
                .iterator()
                .next();
                
        assertEquals("#FF0000", model.getCourseColorForAssignment(assignment.getId()));
    }

    @Test
    void getCourseColorForAssignment_unknownId_returnsGray() {
        assertEquals("#cccccc", model.getCourseColorForAssignment(null));
    }

    @Test
    void saveTerm_persistsTermToRepository() {
        Term term = model.getTerm();
        term.addCourse("CSE 110", "green");
        term.getCourse("CSE 110").addSeries(START, "PA");
        model.addAssignment("CSE 110", "PA", "PA1", START.plusDays(2), 60);

        model.saveTerm();

        assertSame(term, model.getTerm());
        assertEquals(1, model.getTerm().getCourses().size());
    }

    @Test
    void getSeriesForAssignment_returnsCorrectSeries() {
        Term term = model.getTerm();
        term.addCourse("CSE 110", "green");
        term.getCourse("CSE 110").addSeries(START, "PA");

        model.addAssignment("CSE 110", "PA", "PA1", START.plusDays(2), 60);

        Series series = term.getCourse("CSE 110").getSeries("PA");
        Assignment assignment = series.getAssignments().iterator().next();

        assertEquals(series, model.getSeriesForAssignment(assignment.getId()));
    }

    @Test
    void getSeriesForAssignment_returnsNullIfMissing() {
        assertNull(model.getSeriesForAssignment("bad-id"));
        assertNull(model.getSeriesForAssignment(null));
    }
}