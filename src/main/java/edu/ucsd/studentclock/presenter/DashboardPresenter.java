package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.BiConsumer;

import edu.ucsd.studentclock.config.StudyTargetConfig;
import edu.ucsd.studentclock.facade.StudentClockFacade;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.WorkSession;
import edu.ucsd.studentclock.service.StudyHoursService;
import edu.ucsd.studentclock.util.AppClock;
import edu.ucsd.studentclock.util.AssignmentColorUtil;
import edu.ucsd.studentclock.util.TimeFormatUtil;
import edu.ucsd.studentclock.view.DashboardView;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DashboardPresenter extends AbstractPresenter<DashboardView> {
    private final AppClock clock;
    private Runnable onNavigateBack;
    private Runnable onNavigateToBigPicture;
    private Runnable onSetWeeklyHoursRequested;
    private BiConsumer<Series, Assignment> onLogWorkRequested;
    private Runnable onBigPictureRefresh;

    private enum DisplayMode {
        URGENT,
        OPEN
    }

    private DisplayMode mode = DisplayMode.URGENT;

    public DashboardPresenter(StudentClockFacade facade, DashboardView view, AppClock clock) {
        super(facade, view, clock);
        this.clock = clock;
        
        this.view.getBackButton().setOnAction(e -> {
            if (onNavigateBack != null) onNavigateBack.run();
        });
        
        this.view.getToggleModeButton().setOnAction(e -> toggleDisplayMode());
        
        this.view.getBigPictureButton().setOnAction(e -> {
            if (onNavigateToBigPicture != null) onNavigateToBigPicture.run();
        });

        this.view.getSetWeeklyHoursButton().setOnAction(e -> {
            if (onSetWeeklyHoursRequested != null) {
                onSetWeeklyHoursRequested.run();
            } else {
                showSetWeeklyHoursDialog();
            }
        });

        attachMockTimeHandlers();
        refresh();
    }

    private void attachMockTimeHandlers() {
        view.getUseMockButton().setOnAction(e -> {
            boolean useMock = !clock.isUseMock();
            clock.setUseMock(useMock);
            view.getMockControlsBox().setVisible(useMock);
            view.getMockControlsBox().setManaged(useMock);
            view.getUseMockButton().setText(useMock ? "Use real time" : "Use mock time");
            
            if (useMock) {
                LocalDateTime now = clock.now();
                view.getMockDatePicker().setValue(now.toLocalDate());
                view.getHourSpinner().getValueFactory().setValue(now.getHour());
                view.getMinuteSpinner().getValueFactory().setValue(now.getMinute());
            }
            
            refresh();
            if (onBigPictureRefresh != null) onBigPictureRefresh.run();
        });

        Runnable updateMockFromPickers = () -> {
            LocalDate d = view.getMockDatePicker().getValue();
            if (d == null) return;
            int h = view.getHourSpinner().getValue();
            int m = view.getMinuteSpinner().getValue();
            clock.setMock(LocalDateTime.of(d.getYear(), d.getMonth(), d.getDayOfMonth(), h, m, 0));
            refresh();
            if (onBigPictureRefresh != null) onBigPictureRefresh.run();
        };

        view.getMockDatePicker().valueProperty().addListener((o, oldVal, newVal) -> 
            updateMockFromPickers.run());
        view.getHourSpinner().valueProperty().addListener((o, oldVal, newVal) -> 
            updateMockFromPickers.run());
        view.getMinuteSpinner().valueProperty().addListener((o, oldVal, newVal) -> 
            updateMockFromPickers.run());
    }

    private void toggleDisplayMode() {
        if (mode == DisplayMode.URGENT) {
            mode = DisplayMode.OPEN;
            view.getToggleModeButton().setText("Urgent Assignments");
        } else {
            mode = DisplayMode.URGENT;
            view.getToggleModeButton().setText("Open Assignments");
        }
        refresh();
    }

    public void setOnNavigateBack(Runnable action) {
        this.onNavigateBack = action;
    }

    public void setOnNavigateToBigPicture(Runnable action) {
        this.onNavigateToBigPicture = action;
    }

    public void setOnSetWeeklyHoursRequested(Runnable action) {
        this.onSetWeeklyHoursRequested = action;
    }

    public void setOnLogWorkRequested(BiConsumer<Series, Assignment> action) {
        this.onLogWorkRequested = action;
    }

    public void setOnBigPictureRefresh(Runnable action) {
        this.onBigPictureRefresh = action;
    }

    @Override
    public void refresh() {
        facade.updateAllActive(clock.now());
        view.clearList();

        long remaining = facade.getRemainingMinutes(clock.now());
        String remainingHHMM = TimeFormatUtil.formatMinutesToHHMM(remaining);
        
        view.setStudyHoursStatus(
            remainingHHMM,
            getStudyStatusColor(),
            getStudyStatusPercent()
        );

        List<Series> assignments = mode == DisplayMode.OPEN
            ? facade.getOpenAssignments()
            : facade.getUrgentAssignments(clock.now());

        String emptyMessage = mode == DisplayMode.OPEN ? "No Open Assignments" : "No Urgent Assignments";
        renderAssignmentList(assignments, emptyMessage);
    }

    @Override
    public String getTitle() {
        return "Dashboard";
    }

    private void renderAssignmentList(List<Series> assignments, String emptyMessage) {
        if (assignments.isEmpty()) {
            view.showEmptyMessage(emptyMessage);
            return;
        }

        for (Series s : assignments) {
            final Series series = s;
            final Assignment assignment = s.getActiveAssignment();
            
            if (assignment == null) continue;

            String color = AssignmentColorUtil.getAssignmentDisplayColor(s, assignment, 
                                                                        s.getActiveIndex(), clock.now());
            String remainingHhmm = TimeFormatUtil.formatMinutesToHHMM(assignment.getMinutesLeft());
            String percentStatus = formatAssignmentPercent(s.getPercentBehind(clock.now()));

            Runnable logWorkHandler = () -> {
                if (onLogWorkRequested != null) {
                    onLogWorkRequested.accept(series, assignment);
                } else {
                    showLogWorkDialog(assignment, series);
                }
            };

            view.addAssignment(
                assignment.getName(),
                assignment.getDueDate().toLocalDate().toString(),
                remainingHhmm,
                percentStatus,
                color,
                logWorkHandler,
                () -> {
                    assignment.setMinutesWorked(assignment.getMinutesEstimated());
                    series.advanceActiveAssignment();
                    facade.saveTerm();
                    refresh();
                    if (onBigPictureRefresh != null) onBigPictureRefresh.run();
                }
            );
        }
    }

    private void showSetWeeklyHoursDialog() {
        int currentMinutes = facade.getTerm().getWeeklyTargetMinutes();
        int currentHours = currentMinutes / 60;

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Set weekly study hours");
        dialog.setHeaderText("Enter how many hours you want to study per week " +
            "(default " + StudyTargetConfig.DEFAULT_WEEKLY_HOURS + ").");

        Spinner<Integer> hoursSpinner = new Spinner<>(
            StudyTargetConfig.MIN_WEEKLY_HOURS, 
            StudyTargetConfig.MAX_WEEKLY_HOURS, 
            currentHours
        );
        hoursSpinner.setEditable(true);
        hoursSpinner.setMaxWidth(120);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(new Label("Hours per week:"), 0, 0);
        grid.add(hoursSpinner, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            try {
                return hoursSpinner.getValue();
            } catch (Exception e) {
                return null;
            }
        });

        dialog.showAndWait().ifPresent(hours -> {
            if (hours != null && hours >= StudyTargetConfig.MIN_WEEKLY_HOURS && 
                hours <= StudyTargetConfig.MAX_WEEKLY_HOURS) {
                facade.setWeeklyTargetMinutes(hours * 60);
                refresh();
                new Alert(Alert.AlertType.INFORMATION,
                    "Weekly study hours updated to " + hours + " hours per week.")
                    .showAndWait();
            }
        });
    }

    private void showLogWorkDialog(Assignment assignment, Series series) {
        Dialog<WorkSession> dialog = new Dialog<>();
        dialog.setTitle("Report Work");
        dialog.setHeaderText("Enter date, start time, and end time");

        LocalDate today = clock.now().toLocalDate();
        DatePicker datePicker = new DatePicker(today);
        datePicker.setMaxWidth(Double.MAX_VALUE);

        Spinner<Integer> startHour = new Spinner<>(0, 23, 9);
        startHour.setEditable(true);
        Spinner<Integer> startMin = new Spinner<>(0, 59, 0);
        startMin.setEditable(true);

        Spinner<Integer> endHour = new Spinner<>(0, 23, 10);
        endHour.setEditable(true);
        Spinner<Integer> endMin = new Spinner<>(0, 59, 0);
        endMin.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(new Label("Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Start time:"), 0, 1);
        grid.add(new HBox(5, startHour, new Label(":"), startMin), 1, 1);
        grid.add(new Label("End time:"), 0, 2);
        grid.add(new HBox(5, endHour, new Label(":"), endMin), 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

            LocalDate date = datePicker.getValue();
            if (date == null) return null;

            LocalDateTime start = LocalDateTime.of(date, LocalTime.of(startHour.getValue(), startMin.getValue()));
            LocalDateTime end = LocalDateTime.of(date, LocalTime.of(endHour.getValue(), endMin.getValue()));

            if (!end.isAfter(start)) {
                new Alert(Alert.AlertType.WARNING, "End time must be after start time.").showAndWait();
                return null;
            }

            String courseName = facade.getCourseNameForAssignment(assignment.getId());
            long durationMinutes = ChronoUnit.MINUTES.between(start, end);

            return new WorkSession(start, end, assignment.getId(), courseName, durationMinutes);
        });

        dialog.showAndWait().ifPresent(session -> {
            if (session != null) {
                assignment.addWorkSession(session);
                
                if (facade.addWorkSession(session)) {
                    facade.saveTerm();
                    
                    if (assignment.getMinutesWorked() >= assignment.getMinutesEstimated()) {
                        series.advanceActiveAssignment();
                        facade.saveTerm();
                    }
                }
                
                refresh();
                if (onBigPictureRefresh != null) onBigPictureRefresh.run();
            }
        });
    }

    private String getStudyStatusColor() {
        int targetMinutes = facade.getTerm().getWeeklyTargetMinutes();
        if (targetMinutes <= 0) return StudyTargetConfig.COLOR_COMPLETED;

        long workedMinutes = StudyHoursService.getWorkedMinutesThisWeek(
            facade.getWorkSessions(), clock.now());
        double completed = Math.min(1.0, workedMinutes / (double) targetMinutes);

        if (completed >= StudyTargetConfig.STATUS_COMPLETED_THRESHOLD) 
            return StudyTargetConfig.COLOR_COMPLETED;
        if (completed >= StudyTargetConfig.STATUS_HIGH_THRESHOLD) 
            return StudyTargetConfig.COLOR_HIGH;
        if (completed >= StudyTargetConfig.STATUS_MEDIUM_THRESHOLD) 
            return StudyTargetConfig.COLOR_MEDIUM;
        return StudyTargetConfig.COLOR_LOW;
    }

    private String getStudyStatusPercent() {
        int targetMinutes = facade.getTerm().getWeeklyTargetMinutes();
        if (targetMinutes <= 0) return "";

        long workedMinutes = StudyHoursService.getWorkedMinutesThisWeek(facade.getWorkSessions(), clock.now());
        double completedPct = Math.min(100.0, (workedMinutes * 100.0) / targetMinutes);
        
        return String.format("%.0f%% completed", completedPct);
    }

    private String formatAssignmentPercent(Double percent) {
        if (percent == null) return "---";
        if (percent > 0) return String.format("%.0f%% behind", Math.min(100, percent * 100));
        if (percent < 0) return String.format("%.0f%% ahead", -percent * 100);
        return "On track";
    }
}