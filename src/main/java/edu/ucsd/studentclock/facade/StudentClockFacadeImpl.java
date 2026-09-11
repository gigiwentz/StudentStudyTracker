package edu.ucsd.studentclock.facade;

import edu.ucsd.studentclock.model.*;
import edu.ucsd.studentclock.util.AppClock;
import edu.ucsd.studentclock.util.Clock;  // 添加这个导入！

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of the StudentClockFacade.
 * Hides the complexity of the underlying model objects.
 */
public class StudentClockFacadeImpl implements StudentClockFacade {
    
    private final Model model;
    private final AppClock clock;
    
    public StudentClockFacadeImpl(Model model, AppClock clock) {
        this.model = model;
        this.clock = clock;
    }
    
    // ========== Assignment Management ==========
    
    @Override
    public boolean addAssignment(String courseName, String seriesName, String assignmentName, 
                                LocalDateTime dueDate, long estimatedMinutes) {
        return model.addAssignment(courseName, seriesName, assignmentName, dueDate, estimatedMinutes);
    }
    
    @Override
    public boolean addAssignment(String courseName, String seriesName, Object assignment) {
        if (assignment instanceof Assignment) {
            return model.addAssignment(courseName, seriesName, (Assignment) assignment);
        }
        return false;
    }
    
    @Override
    public List<?> getAllAssignments() {
        return model.getAllAssignments();
    }
    
    @Override
    public Object getAssignmentById(String assignmentId) {
        return model.getAssignmentById(assignmentId);
    }
    
    // ========== Series Management ==========
    
    @Override
    public List<Series> getOpenAssignments() {
        return model.getOpenAssignments();
    }
    
    @Override
    public List<Series> getUrgentAssignments(LocalDateTime currentDate) {
        return model.getUrgentAssignments(currentDate);
    }
    
    @Override
    public Series getSeriesForAssignment(String assignmentId) {
        return model.getSeriesForAssignment(assignmentId);
    }
    
    @Override
    public boolean addSeries(String course, LocalDateTime start, String seriesName) {
        return model.addSeries(course, start, seriesName);
    }
    
    // ========== Work Session Management ==========
    
    @Override
    public boolean addWorkSession(WorkSession session) {
        return model.addWorkSession(session);
    }
    
    @Override
    public List<WorkSession> getWorkSessions() {
        return model.getWorkSessions();
    }
    
    @Override
    public List<WorkLog> getWorkLogsForAssignment(String assignmentId) {
        return model.getWorkLogsForAssignment(assignmentId);
    }
    
    // ========== Course Information ==========
    
    @Override
    public String getCourseNameForAssignment(String assignmentId) {
        return model.getCourseNameForAssignment(assignmentId);
    }
    
    @Override
    public String getCourseColorForAssignment(String assignmentId) {
        return model.getCourseColorForAssignment(assignmentId);
    }
    
    // ========== Term Management ==========
    
    @Override
    public Term getTerm() {
        return model.getTerm();
    }
    
    @Override
    public void saveTerm() {
        model.saveTerm();
    }
    
    @Override
    public void setWeeklyTargetMinutes(int minutes) {
        model.setWeeklyTargetMinutes(minutes);
    }
    
    // ========== Progress Tracking ==========
    
    @Override
    public void updateAllActive(LocalDateTime now) {
        model.updateAllActive(now);
    }
    
    @Override
    public long getRemainingMinutes(LocalDateTime now) {
        return model.getRemainingMinutes(now);
    }
    
    @Override
    public String getRemainingHHMM(LocalDateTime now) {
        // 这里使用了 Clock 接口，需要在文件顶部导入
        return model.getRemainingHHMM(new Clock() {
            @Override
            public LocalDateTime now() {
                return now;
            }
        });
    }
    
    // ========== Workload Calculations ==========
    
    @Override
    public long getWeekWorkload(LocalDateTime now) {
        return model.getWeekWorkload(now);
    }
    
    @Override
    public long getLaterWorkload(LocalDateTime now) {
        return model.getLaterWorkload(now);
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Get the underlying model (for transitional purposes)
     */
    public Model getModel() {
        return this.model;
    }
}