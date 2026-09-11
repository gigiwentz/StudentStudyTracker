package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Course {
    private final String name;
    private final String color;
    private final List<Series> series = new ArrayList<>();

    public Course(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return this.name;
    }

    public String getColor() {
        return this.color;
    }

    public List<Series> getSeries() {
        return this.series;
    }

    public Series getSeries(String name) {
        for (Series s : series) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    public void addSeries(LocalDateTime start, String name) {
        this.series.add(new Series(start, name));
    }
}