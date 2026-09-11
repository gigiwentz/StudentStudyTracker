package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    @Test
    void constructor_setsNameAndColor() {
        Course course = new Course("CSE 110", "blue");

        assertEquals("CSE 110", course.getName());
        assertEquals("blue", course.getColor());
    }

    @Test
    void addSeries_createsNewSeriesWithCorrectValues() {
        Course course = new Course("CSE 110", "blue");
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);

        course.addSeries(start, "Week 1");

        Series series = course.getSeries("Week 1");

        assertNotNull(series);
        assertEquals("Week 1", series.getName());
        assertEquals(start, series.getStartDate());
    }

    @Test
    void getSeries_unknownName_returnsNull() {
        Course course = new Course("CSE 110", "blue");

        assertNull(course.getSeries("Missing"));
    }

    @Test
    void getSeries_returnsAllAddedSeries() {
        Course course = new Course("CSE 110", "blue");

        course.addSeries(LocalDateTime.now(), "Week 1");
        course.addSeries(LocalDateTime.now(), "Week 2");

        List<Series> allSeries = course.getSeries();

        assertEquals(2, allSeries.size());
    }
}