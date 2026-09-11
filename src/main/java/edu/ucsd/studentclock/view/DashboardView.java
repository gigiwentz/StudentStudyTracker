package edu.ucsd.studentclock.view;

import edu.ucsd.studentclock.config.RiskLevelConfig;
import edu.ucsd.studentclock.config.ViewStyleConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class DashboardView extends BorderPane {
    private Button backButton = new Button("Term Page");
    private Button toggleModeButton = new Button("Open Assignments");
    private Button bigPictureButton = new Button("Big Picture");
    private VBox assignmentList = new VBox(ViewStyleConfig.SPACING_SMALL);
    private HBox studyStatusBox;
    private Label studyStatusLabel;
    private Region studyStatusIcon;
    private Button setWeeklyHoursButton;
    private Button useMockButton;
    private VBox mockControlsBox;
    private DatePicker mockDatePicker;
    private Spinner<Integer> hourSpinner;
    private Spinner<Integer> minuteSpinner;

    public DashboardView() {
        VBox headerBox = new VBox(ViewStyleConfig.SPACING_LARGE);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(ViewStyleConfig.HEADER_PADDING));

        Text title = new Text("Dashboard");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: " + ViewStyleConfig.FONT_SIZE_TITLE + ";");

        studyStatusIcon = new Region();
        studyStatusIcon.setPrefSize(16, 16);
        studyStatusIcon.setStyle(
            "-fx-background-radius: 7px; " +
            "-fx-background-color: " + ViewStyleConfig.COLOR_GRAY + ";" +
            "-fx-border-color: " + ViewStyleConfig.COLOR_BLACK + "; -fx-border-radius: 8px;"
        );

        studyStatusLabel = new Label("Study hours left this week: --:--");
        studyStatusLabel.setStyle("-fx-font-size: " + ViewStyleConfig.FONT_SIZE_NORMAL + "px; -fx-font-weight: bold;");

        setWeeklyHoursButton = new Button("Set weekly study hours");
        setWeeklyHoursButton.setStyle("-fx-font-size: " + ViewStyleConfig.FONT_SIZE_SMALL + "px;");

        studyStatusBox = new HBox(8, studyStatusIcon, studyStatusLabel);
        studyStatusBox.setAlignment(Pos.CENTER);

        VBox studyStatusArea = new VBox(6, studyStatusBox, setWeeklyHoursButton);
        studyStatusArea.setAlignment(Pos.CENTER);

        useMockButton = new Button("Use mock time");

        mockDatePicker = new DatePicker();
        hourSpinner = new Spinner<>(0, 23, 12);
        hourSpinner.setEditable(true);
        minuteSpinner = new Spinner<>(0, 59, 0);
        minuteSpinner.setEditable(true);

        mockControlsBox = new VBox(8,
            new Label("Mock date:"), mockDatePicker,
            new Label("Mock time (hour:min):"), new HBox(8, hourSpinner, new Label(":"), minuteSpinner));
        mockControlsBox.setVisible(false);
        mockControlsBox.setManaged(false);

        HBox navBox = new HBox(ViewStyleConfig.SPACING_MEDIUM);
        navBox.setAlignment(Pos.CENTER);
        navBox.getChildren().addAll(backButton, toggleModeButton, bigPictureButton);

        HBox riskLegend = buildRiskLegend();
        riskLegend.setAlignment(Pos.CENTER);
        riskLegend.setSpacing(20);
        riskLegend.setPadding(new Insets(4, 0, 0, 0));

        headerBox.getChildren().addAll(title, studyStatusArea, riskLegend, navBox);

        ScrollPane scroller = new ScrollPane(assignmentList);
        scroller.setFitToWidth(true);
        scroller.setStyle("-fx-background-color: transparent; -fx-background: " + ViewStyleConfig.COLOR_WHITE + ";");

        VBox centerColumn = new VBox(ViewStyleConfig.SPACING_MEDIUM, scroller);

        this.setTop(headerBox);
        this.setCenter(centerColumn);
        this.setPadding(new Insets(0, 25, 20, 25));
        this.setStyle("-fx-background-color: " + ViewStyleConfig.COLOR_WHITE + ";");
    }

    private static Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(40));
        tooltip.setHideDelay(Duration.millis(0));
        tooltip.setShowDuration(Duration.seconds(30));
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(360);
        return tooltip;
    }

    private static HBox buildRiskLegend() {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);

        for (RiskLevelConfig.RiskLevel level : RiskLevelConfig.getAllLevels().values()) {
            Region circle = new Region();
            circle.setPrefSize(12, 12);
            circle.setMinSize(12, 12);
            circle.setStyle("-fx-background-color: " + level.getColor() + 
                          "; -fx-background-radius: " + ViewStyleConfig.RADIUS_LEGEND_DOT + "px;");

            Label lbl = new Label(level.getDisplayName());
            lbl.setStyle("-fx-font-size: " + ViewStyleConfig.FONT_SIZE_SMALL + "px; -fx-text-fill: " + ViewStyleConfig.COLOR_DARK_GRAY + ";");

            HBox pair = new HBox(6, circle, lbl);
            pair.setAlignment(Pos.CENTER);
            
            Tooltip.install(pair, createTooltip(level.getTooltip()));
            
            row.getChildren().add(pair);
        }

        return row;
    }

    public Button getBackButton() {
        return backButton;
    }

    public Button getToggleModeButton() {
        return toggleModeButton;
    }

    public Button getBigPictureButton() {
        return bigPictureButton;
    }

    public void clearList() {
        assignmentList.getChildren().clear();
    }

    public void addAssignment(String name, String dueDate, String remainingHhmm, 
                              String percentStatus, String color, 
                              Runnable onLogWork, Runnable onDoneClicked) {
        HBox row = new HBox(10);
        row.setPrefSize(450, 40);
        row.setPadding(new Insets(5, 10, 5, 10));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + color + "; -fx-border-color: " + ViewStyleConfig.COLOR_BORDER_GRAY + "; " +
                     "-fx-border-width: 0 0 1 0; -fx-font-weight: bold; " +
                     "-fx-border-radius: 5px; -fx-background-radius: 5px;");

        Label nameL = new Label(name);
        nameL.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        HBox nameBox = new HBox(nameL);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        Label dueL = new Label("Due: " + dueDate);
        dueL.setStyle("-fx-font-weight: normal; -fx-font-size: " + ViewStyleConfig.FONT_SIZE_SMALL + "px;");

        Label leftL = new Label(remainingHhmm + " left");
        leftL.setStyle("-fx-font-weight: normal; -fx-font-size: " + ViewStyleConfig.FONT_SIZE_SMALL + "px;");

        VBox infoBox = new VBox(2, dueL, leftL);
        
        if (percentStatus != null && !percentStatus.isEmpty()) {
            Label statusL = new Label(percentStatus);
            statusL.setStyle("-fx-font-weight: normal; -fx-font-size: " + ViewStyleConfig.FONT_SIZE_SMALL + "px;");
            infoBox.getChildren().add(statusL);
        }
        
        infoBox.setAlignment(Pos.CENTER);

        String tipText = name + "\nDue: " + dueDate + "\n" + remainingHhmm + " left"
                        + (percentStatus != null && !percentStatus.isEmpty() ? "\n" + percentStatus : "");
        
        Tooltip.install(nameBox, createTooltip(tipText));
        Tooltip.install(infoBox, createTooltip(tipText));

        Button logButton = new Button("Log Work");
        logButton.setOnAction(e -> onLogWork.run());

        Button doneButton = new Button("Done");
        doneButton.setStyle("-fx-background-color: " + ViewStyleConfig.COLOR_GREEN_DONE + "; -fx-text-fill: " + 
                           ViewStyleConfig.COLOR_WHITE + "; -fx-font-size: " + ViewStyleConfig.FONT_SIZE_SMALL + "px;");
        doneButton.setOnAction(e -> onDoneClicked.run());

        HBox buttonBox = new HBox(8, logButton, doneButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        row.getChildren().addAll(nameBox, spacer1, infoBox, spacer2, buttonBox);
        assignmentList.getChildren().add(row);
    }

    public void setStudyHoursStatus(String remainingHHMM, String color, String percentStatus) {
        String text = "Study hours left this week: " + remainingHHMM;
        if (percentStatus != null && !percentStatus.isEmpty()) {
            text += " (" + percentStatus + ")";
        }
        
        studyStatusLabel.setText(text);
        studyStatusIcon.setStyle(
            "-fx-background-radius: 7px;" +
            "-fx-background-color: " + color + ";"
        );
    }

    public Button getSetWeeklyHoursButton() { 
        return setWeeklyHoursButton; 
    }

    public Button getUseMockButton() { 
        return useMockButton; 
    }

    public VBox getMockControlsBox() { 
        return mockControlsBox; 
    }

    public DatePicker getMockDatePicker() { 
        return mockDatePicker; 
    }

    public Spinner<Integer> getHourSpinner() { 
        return hourSpinner; 
    }

    public Spinner<Integer> getMinuteSpinner() { 
        return minuteSpinner; 
    }

    public void showEmptyMessage(String message) {
        Label empty = new Label(message);
        empty.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + ViewStyleConfig.COLOR_GRAY + ";"
        );
        
        VBox wrapper = new VBox(empty);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(40, 0, 0, 0));
        
        assignmentList.getChildren().add(wrapper);
    }
}