package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.config.CourseColorConfig;
import edu.ucsd.studentclock.config.DateTimeFormatConfig;
import edu.ucsd.studentclock.facade.StudentClockFacade;
import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Course;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.util.AppClock;
import edu.ucsd.studentclock.util.AssignmentColorUtil;
import edu.ucsd.studentclock.view.TermView;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TermPagePresenter extends AbstractPresenter<TermView> {
    private final AppClock clock;
    private Runnable onNavigateToDashboard;
    private Runnable onBigPictureRefresh;

    public TermPagePresenter(StudentClockFacade facade, TermView view, AppClock clock) {
        super(facade, view, clock);
        this.clock = clock;
        
        attachHandlers();
        attachMockTimeHandlers();
        startDateTimeRefresh();
        
        this.view.getDashboardButton().setOnAction(e -> {
            if (onNavigateToDashboard != null)
                onNavigateToDashboard.run();
        });
        
        refresh();
    }

    @Override
    public String getTitle() {
        return "Term Page";
    }

    @Override
    public void refresh() {
        refreshTree();
    }

    public void setOnNavigateToDashboard(Runnable action) {
        this.onNavigateToDashboard = action;
    }

    public void setOnBigPictureRefresh(Runnable action) {
        this.onBigPictureRefresh = action;
    }

    private void refreshTree() {
        facade.updateAllActive(clock.now());
        
        TreeItem<String> root = new TreeItem<>("Term");
        
        for (Course c : facade.getTerm().getCourses()) {
            TreeItem<String> courseNode = new TreeItem<>("" + c.getName());
            
            for (Series s : c.getSeries()) {
                TreeItem<String> seriesNode = new TreeItem<>("" + s.getName());
                
                for (Assignment a : s.getAssignments()) {
                    if (isPastDue(a)) continue;
                    
                    int idx = s.getAssignments().indexOf(a);
                    String rowColor = AssignmentColorUtil.getAssignmentDisplayColor(s, a, idx, clock.now());
                    
                    Region assignDot = new Region();
                    assignDot.setPrefSize(10, 10);
                    assignDot.setMinSize(10, 10);
                    assignDot.setStyle("-fx-background-color: " + rowColor + "; -fx-background-radius: 5px;");
                    
                    String text = a.getName() + " | Due Date: " + a.getDueDate() + 
                                 " | Minutes Left: " + a.getMinutesLeft();
                    
                    HBox assignRow = new HBox(6, assignDot, new Label(text));
                    assignRow.setAlignment(Pos.CENTER_LEFT);
                    
                    TreeItem<String> assignmentNode = new TreeItem<>("");
                    assignmentNode.setGraphic(assignRow);
                    
                    seriesNode.getChildren().add(assignmentNode);
                }
                
                if (!seriesNode.getChildren().isEmpty()) {
                    courseNode.getChildren().add(seriesNode);
                }
            }
            
            if (!courseNode.getChildren().isEmpty()) {
                root.getChildren().add(courseNode);
            }
        }
        
        view.setTermTree(root);
    }

    private boolean isPastDue(Assignment a) {
        return a.getDueDate() != null && clock.now().isAfter(a.getDueDate());
    }

    private void attachHandlers() {
        view.getAddCourseButton().setOnAction(e -> showAddCourseDialog());
        view.getAddSeriesButton().setOnAction(e -> showAddSeriesDialog());
        view.getAddAssignmentButton().setOnAction(e -> showAddAssignmentDialog());
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

    private void startDateTimeRefresh() {
        Runnable update = () -> {
            LocalDateTime now = clock.now();
            view.getDateTimeLabel().setText(
                now.format(DateTimeFormatConfig.DATE_LONG) + " " + 
                now.format(DateTimeFormatConfig.TIME_SHORT)
            );
        };
        
        update.run();
        
        Timeline t = new Timeline(new KeyFrame(Duration.seconds(1), e -> update.run()));
        t.setCycleCount(Timeline.INDEFINITE);
        t.play();
    }

    private void showAddCourseDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Course");
        dialog.setHeaderText("Enter course name:");
        dialog.setContentText("Course name:");
        
        dialog.showAndWait().ifPresent(name -> {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                int idx = facade.getTerm().getCourses().size();
                String color = CourseColorConfig.getColorByIndex(idx);
                facade.getTerm().addCourse(trimmed, color);
                refresh();
            }
        });
    }

    private void showAddSeriesDialog() {
        List<Course> courses = facade.getTerm().getCourses();
        if (courses.isEmpty()) {
            return;
        }

        List<String> courseNames = courses.stream()
                .map(Course::getName)
                .collect(Collectors.toList());

        ChoiceDialog<String> courseDialog = new ChoiceDialog<>(courseNames.get(0), courseNames);
        courseDialog.setTitle("Add New Assignment");
        courseDialog.setHeaderText("Choose a course for this assignment");
        courseDialog.setContentText("Course:");

        var courseChoice = courseDialog.showAndWait();
        if (courseChoice.isEmpty()) return;

        String courseName = courseChoice.get();
        if (facade.getTerm().getCourse(courseName) == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Assignment");

        TextField nameField = new TextField();
        nameField.setPromptText("Assignment Name");
        
        DatePicker startPicker = new DatePicker();
        DatePicker duePicker = new DatePicker();
        
        TextField minutesField = new TextField();
        minutesField.setPromptText("Estimated minutes");

        VBox box = new VBox(10,
            new Label("Assignment Name:"), nameField,
            new Label("Start Date:"), startPicker,
            new Label("Due Date:"), duePicker,
            new Label("Estimated Minutes:"), minutesField
        );

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String seriesName = nameField.getText().trim();
                    LocalDateTime startDate = startPicker.getValue().atStartOfDay();
                    LocalDateTime due = duePicker.getValue().atTime(23, 59, 0);
                    long minutes = Long.parseLong(minutesField.getText().trim());

                    if (seriesName.isEmpty()) return;

                    facade.addSeries(courseName, startDate, seriesName);
                    facade.addAssignment(courseName, seriesName, seriesName, due, minutes);
                    refresh();
                } catch (Exception e) {
                    // invalid input
                }
            }
        });
    }

    private void showAddAssignmentDialog() {
        List<Course> courses = facade.getTerm().getCourses();
        if (courses.isEmpty()) {
            return;
        }

        List<String> courseNames = courses.stream()
                .map(Course::getName)
                .collect(Collectors.toList());

        ChoiceDialog<String> courseDialog = new ChoiceDialog<>(courseNames.get(0), courseNames);
        courseDialog.setTitle("Add Instance of an Existing Assignment Type");
        courseDialog.setHeaderText("Choose a course for this assignment");
        courseDialog.setContentText("Course:");

        var courseChoice = courseDialog.showAndWait();
        if (courseChoice.isEmpty()) return;

        String courseName = courseChoice.get();
        Course course = facade.getTerm().getCourse(courseName);
        if (course == null) {
            return;
        }

        List<Series> seriesList = course.getSeries();
        if (seriesList.isEmpty()) {
            return;
        }

        List<String> seriesNames = seriesList.stream()
                .map(Series::getName)
                .collect(Collectors.toList());

        ChoiceDialog<String> seriesDialog = new ChoiceDialog<>(seriesNames.get(0), seriesNames);
        seriesDialog.setTitle("Add Instance of an Existing Assignment Type");
        seriesDialog.setHeaderText("Choose the assignment type");
        seriesDialog.setContentText("Existing assignment types:");

        var seriesChoice = seriesDialog.showAndWait();
        if (seriesChoice.isEmpty()) return;

        String seriesName = seriesChoice.get();
        if (course.getSeries(seriesName) == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Instance of an Existing Assignment Type");

        DatePicker duePicker = new DatePicker();
        TextField minutesField = new TextField();
        minutesField.setPromptText("Estimated minutes");

        VBox box = new VBox(10,
            new Label("Due Date:"), duePicker,
            new Label("Estimated Minutes:"), minutesField
        );

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    LocalDateTime due = duePicker.getValue().atTime(23, 59, 59);
                    long minutes = Long.parseLong(minutesField.getText().trim());
                    facade.addAssignment(courseName, seriesName, null, due, minutes);
                    refresh();
                } catch (Exception e) {
                    // invalid input
                }
            }
        });
    }
}