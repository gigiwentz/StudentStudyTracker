package edu.ucsd.studentclock.facade;

import edu.ucsd.studentclock.model.Term;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.model.WorkSession;
import edu.ucsd.studentclock.model.WorkLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Facade interface for the Student Clock application.
 * Provides a simplified interface to hide the complexity of the underlying model.
 */
public interface StudentClockFacade {
    
    // ========== Assignment Management ==========
    boolean addAssignment(String courseName, String seriesName, String assignmentName, 
                         LocalDateTime dueDate, long estimatedMinutes);
    
    boolean addAssignment(String courseName, String seriesName, Object assignment);
    
    List<?> getAllAssignments();
    
    Object getAssignmentById(String assignmentId);
    
    // ========== Series Management ==========
    List<Series> getOpenAssignments();
    
    List<Series> getUrgentAssignments(LocalDateTime currentDate);
    
    Series getSeriesForAssignment(String assignmentId);
    
    boolean addSeries(String course, LocalDateTime start, String seriesName);
    
    // ========== Work Session Management ==========
    boolean addWorkSession(WorkSession session);
    
    List<WorkSession> getWorkSessions();
    
    List<WorkLog> getWorkLogsForAssignment(String assignmentId);
    
    // ========== Course Information ==========
    String getCourseNameForAssignment(String assignmentId);
    
    String getCourseColorForAssignment(String assignmentId);
    
    // ========== Term Management ==========
    Term getTerm();
    
    void saveTerm();
    
    void setWeeklyTargetMinutes(int minutes);
    
    // ========== Progress Tracking ==========
    void updateAllActive(LocalDateTime now);
    
    long getRemainingMinutes(LocalDateTime now);
    
    String getRemainingHHMM(LocalDateTime now);
    
    // ========== Workload Calculations ==========
    long getWeekWorkload(LocalDateTime now);
    
    long getLaterWorkload(LocalDateTime now);
}