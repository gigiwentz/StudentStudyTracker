package edu.ucsd.studentclock.model;

import edu.ucsd.studentclock.config.StudyTargetConfig;
import java.util.*;

public class Term {
    private int weeklyTargetMinutes;
    private List<Course> courses;

    public Term(int weeklyTargetMinutes) {
        this.weeklyTargetMinutes = weeklyTargetMinutes;
        this.courses = new ArrayList<>();
    }

    public static Term createDefault() {
        return new Term(StudyTargetConfig.DEFAULT_WEEKLY_MINUTES);
    }

    public int getWeeklyTargetMinutes() {
        return weeklyTargetMinutes;
    }

    public void setWeeklyTargetMinutes(int minutes) {
        this.weeklyTargetMinutes = minutes;
    }

    public List<Course> getCourses() {
        return this.courses;
    }

    public Course getCourse(String name) {
        for (Course c : courses) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    public void addCourse(String name, String color) {
        this.courses.add(new Course(name, color));
    }
}