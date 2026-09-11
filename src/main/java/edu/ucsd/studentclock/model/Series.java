package edu.ucsd.studentclock.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Series {
    private LocalDateTime startDate;
    private String name;
    private final List<Assignment> assignments = new ArrayList<>();
    private int activeIndex = -1;
    private boolean completed = false;

    public Series(LocalDateTime startDate, String name) {
        this.startDate = startDate;
        this.name = name;
    }

    public LocalDateTime getStartDate() {
        return this.startDate;
    }

    public String getName() {
        return this.name;
    }

    public Assignment getAssignment(String id) {
        for (Assignment a : assignments) {
            if (a.getId().equals(id)) return a;
        }
        return null;
    }

    public Assignment getActiveAssignment() {
        if (activeIndex == -1 || assignments.isEmpty()) {
            return null;
        }
        if (activeIndex >= assignments.size()) {
            this.activeIndex = -1;
            return null;
        }
        return assignments.get(activeIndex);
    }

    public void updateActiveAssignment(LocalDateTime now) {
        if (activeIndex == -1 && !completed) {
            if (!getStartDate().isAfter(now) && !assignments.isEmpty()) {
                advanceActiveAssignment();
            } else {
                return;
            }
        }

        while (true) {
            Assignment as = getActiveAssignment();
            if (as == null) break;
            if (!now.isAfter(as.getDueDate())) break;
            advanceActiveAssignment();
        }
    }

    public void advanceActiveAssignment() {
        if (activeIndex + 1 >= assignments.size()) {
            activeIndex = -1;
            completed = true;
        } else {
            activeIndex += 1;
        }
    }

    public int getActiveIndex() { 
        return activeIndex; 
    }
    
    public void setActiveIndex(int index) { 
        this.activeIndex = index; 
    }

    public boolean isCompleted() { 
        return completed; 
    }
    
    public void setCompleted(boolean completed) { 
        this.completed = completed; 
    }

    public List<Assignment> getAssignments() {
        return new ArrayList<>(assignments);
    }

    public LocalDateTime getAssignmentStart() {
        if (activeIndex == -1 || activeIndex >= assignments.size()) {
            return null;
        }
        if (activeIndex == 0) {
            return getStartDate();
        }
        return assignments.get(activeIndex - 1).getDueDate();
    }

    public long getMinutesBehind(LocalDateTime now) {
        Assignment as = getActiveAssignment();
        if (as == null) {
            return 0;
        }

        LocalDateTime start = getAssignmentStart();
        LocalDateTime due = as.getDueDate();
        
        if (start == null || due == null) return 0;

        long totalTime = Duration.between(start, due).toMinutes();
        if (totalTime <= 0) totalTime = 23 * 60 + 59;
        
        long elapsed = Duration.between(start, now).toMinutes();
        long expected = ((elapsed * as.getMinutesEstimated()) / totalTime);
        long behind = expected - as.getMinutesWorked();
        
        return Math.max(0, behind);
    }

    public double getPercentBehind(LocalDateTime now) {
        Assignment as = getActiveAssignment();
        if (as == null || as.getMinutesEstimated() <= 0) return 0;
        return (double) getMinutesBehind(now) / as.getMinutesEstimated();
    }

    public void addAssignment(Assignment as) {
        if (completed) {
            completed = false;
        }
        
        if (assignments.size() == 1) {
            assignments.get(0).setName(this.name + " 1");
        }
        
        assignments.add(as);
        
        if (assignments.size() > 1) {
            as.setName(this.name + " " + assignments.size());
        } else {
            as.setName(this.name);
        }
    }
}