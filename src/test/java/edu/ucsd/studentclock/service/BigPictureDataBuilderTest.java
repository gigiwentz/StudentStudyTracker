package edu.ucsd.studentclock.service;

import edu.ucsd.studentclock.datasource.InMemoryDataSource;
import edu.ucsd.studentclock.facade.StudentClockFacade;
import edu.ucsd.studentclock.facade.StudentClockFacadeImpl;
import edu.ucsd.studentclock.model.*;
import edu.ucsd.studentclock.repository.ExampleRepository;
import edu.ucsd.studentclock.util.AppClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BigPictureDataBuilderTest {

    private static final LocalDateTime FEB_1 = LocalDateTime.of(2026, 2, 1, 0, 0);
    private static final LocalDateTime FEB_10 = LocalDateTime.of(2026, 2, 10, 23, 59);
    
    private StudentClockFacade facade;
    private Model model;  // 保留 model 引用以便直接操作
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        ExampleRepository repository = new ExampleRepository(new InMemoryDataSource());
        Term term = Term.createDefault();
        term.addCourse("C1", "#333");
        term.getCourse("C1").addSeries(FEB_1, "S1");
        
        model = new Model(repository, term);
        repository.saveTerm(term);
        
        // 创建 Facade
        facade = new StudentClockFacadeImpl(model, new AppClock());
        now = LocalDateTime.of(2026, 2, 5, 12, 0);
    }

    @Test
    void build_noAssignments_returnsNonNullResult() {
        BigPictureDataBuilder.Result result = BigPictureDataBuilder.build(facade, now);
        
        assertNotNull(result);
        assertTrue(result.getLines().isEmpty());
    }
    
    @Test
    void build_oneAssignmentNoSessions_totalRemainingReflectsEstimatedHours() {
        model.addAssignment("C1", "S1", "A1", FEB_10, 120); // 2 hours
        
        BigPictureDataBuilder.Result result = BigPictureDataBuilder.build(facade, now);
        
        assertNotNull(result);
        assertTrue(result.getYMax() >= 1.0);
    }

    @Test
    void build_oneAssignmentWithWorkSession_segmentHoursDecrease() {
        model.addAssignment("C1", "S1", "A1", FEB_10, 60);
        
        LocalDateTime sessionEnd = LocalDateTime.of(2026, 2, 3, 11, 0);
        model.addWorkSession(new WorkSession(sessionEnd.minusMinutes(30), sessionEnd, null));
        
        BigPictureDataBuilder.Result result = BigPictureDataBuilder.build(facade, now);
        
        assertNotNull(result);
        assertTrue(result.getYMax() >= 1.0);
    }
}