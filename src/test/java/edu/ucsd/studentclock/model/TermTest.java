package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TermTest {

    @Test
    void constructor_setsWeeklyTargetMinutes() {
        Term term = new Term(500);

        assertEquals(500, term.getWeeklyTargetMinutes());
    }

    @Test
    void createDefault_setsWeeklyTargetToTenHours() {
        Term term = Term.createDefault();

        // 10 hours * 60 minutes
        assertEquals(600, term.getWeeklyTargetMinutes());
    }

    @Test
    void addCourse_thenGetCourse_returnsCorrectCourse() {
        Term term = new Term(300);

        term.addCourse("CSE 110", "blue");

        Course course = term.getCourse("CSE 110");

        assertNotNull(course);
        assertEquals("CSE 110", course.getName());
        assertEquals("blue", course.getColor());
    }

    @Test
    void getCourse_unknownName_returnsNull() {
        Term term = new Term(300);

        assertNull(term.getCourse("Missing"));
    }

    @Test
    void getCourses_returnsAllAddedCourses() {
        Term term = new Term(300);

        term.addCourse("CSE 110", "blue");
        term.addCourse("CSE 101", "red");

        List<Course> courses = term.getCourses();

        assertEquals(2, courses.size());
    }
}