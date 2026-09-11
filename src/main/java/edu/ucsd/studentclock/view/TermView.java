package edu.ucsd.studentclock.view;

import edu.ucsd.studentclock.config.ViewStyleConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class TermView extends BorderPane {
    private Button addCourseButton;
    private Button addSeriesButton;
    private Button addAssignmentButton;
    private Label dateTimeLabel;
    private Button useMockButton;
    private Button dashboardButton;
    private VBox mockControlsBox;
    private DatePicker mockDatePicker;
    private Spinner<Integer> hourSpinner;
    private Spinner<Integer> minuteSpinner;
    private TreeView<String> treeView;

    public TermView() {
        VBox headerBox = new VBox(ViewStyleConfig.SPACING_LARGE);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(ViewStyleConfig.HEADER_PADDING));

        Text title = new Text("Term Page");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: " + ViewStyleConfig.FONT_SIZE_TITLE + ";");

        dateTimeLabel = new Label("");
        dateTimeLabel.setStyle("-fx-font-size: " + ViewStyleConfig.FONT_SIZE_NORMAL + "px;");

        useMockButton = new Button("Use mock time");
        dashboardButton = new Button("Dashboard");

        HBox topBox = new HBox(ViewStyleConfig.SPACING_MEDIUM);
        topBox.setAlignment(Pos.CENTER);
        topBox.getChildren().addAll(useMockButton, dashboardButton);

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
        
        addCourseButton = new Button("Add Course");
        addSeriesButton = new Button("Add New Assignment");
        addAssignmentButton = new Button("Add Instance of an Existing Assignment Type");
        
        navBox.getChildren().addAll(addCourseButton, addSeriesButton, addAssignmentButton);

        headerBox.getChildren().addAll(title, dateTimeLabel, topBox, mockControlsBox, navBox);

        treeView = new TreeView<>();
        treeView.setShowRoot(false);

        setTop(headerBox);
        setCenter(treeView);
    }

    public void setTermTree(TreeItem<String> root) {
        treeView.setRoot(root);
    }

    public Button getAddCourseButton() {
        return addCourseButton;
    }

    public Button getAddSeriesButton() {
        return addSeriesButton;
    }

    public Button getAddAssignmentButton() {
        return addAssignmentButton;
    }

    public Label getDateTimeLabel() { 
        return dateTimeLabel; 
    }

    public Button getUseMockButton() { 
        return useMockButton; 
    }

    public Button getDashboardButton() {
        return dashboardButton;
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
}