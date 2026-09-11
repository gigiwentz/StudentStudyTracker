package edu.ucsd.studentclock.util;

import edu.ucsd.studentclock.config.RiskLevelConfig;
import edu.ucsd.studentclock.config.StudyTargetConfig;
import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Series;

import java.time.LocalDateTime;

public final class AssignmentColorUtil {
    public static String getRiskColorFromPercent(double percent) {
        return RiskLevelConfig.getLevelForPercent(percent).getColor();
    }

    public static String getAssignmentDisplayColor(Series series, Assignment assignment, 
                                                   int assignmentIndex, LocalDateTime now) {
        int activeIndex = series.getActiveIndex();
        
        if (assignmentIndex < activeIndex) {
            return StudyTargetConfig.COLOR_GREEN_DONE;
        }
        
        if (activeIndex == -1) {
            return series.isCompleted() ? StudyTargetConfig.COLOR_GREEN_DONE : "#FFFFFF";
        }
        
        if (assignmentIndex > activeIndex) {
            return "#FFFFFF";
        }
        
        return getRiskColorFromPercent(series.getPercentBehind(now));
    }
}