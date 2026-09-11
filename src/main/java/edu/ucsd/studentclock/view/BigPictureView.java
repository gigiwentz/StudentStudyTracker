package edu.ucsd.studentclock.view;

import edu.ucsd.studentclock.config.ViewStyleConfig;
import edu.ucsd.studentclock.service.BigPictureDataBuilder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

public class BigPictureView extends BorderPane {
    private final Button backButton = new Button("Dashboard");
    private final HBox legendBox = new HBox(12);
    private final BigPictureChart chart = new BigPictureChart();
    private final Label emptyLabel = new Label("No open assignments.");

    public BigPictureView() {
        setStyle("-fx-background-color: " + ViewStyleConfig.COLOR_WHITE + ";");
        setPadding(new Insets(15, 20, 15, 20));

        Text title = new Text("Study Big Picture");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: " + ViewStyleConfig.FONT_SIZE_TITLE + ";");

        legendBox.setAlignment(Pos.CENTER_LEFT);
        legendBox.setPadding(new Insets(6, 0, 0, 0));

        HBox navRow = new HBox(12, backButton);
        navRow.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(10, navRow, title, legendBox);
        header.setPadding(new Insets(5, 0, 10, 0));

        emptyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + ViewStyleConfig.COLOR_GRAY + ";");

        StackPane center = new StackPane(chart, emptyLabel);
        StackPane.setAlignment(emptyLabel, Pos.CENTER);
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        setTop(header);
        setCenter(center);
    }

    public Button getBackButton() {
        return backButton;
    }

    public void render(BigPictureDataBuilder.Result result) {
        if (result == null) return;

        chart.setRange(result.getRange());
        chart.setYMax(result.getYMax());
        chart.setLines(result.getLines());
        chart.setStartMarkers(result.getStartMarkerEpochMinutes());
        chart.setTargetLine(result.getTargetLine());

        renderLegend(result.getLegend());

        boolean isEmpty = result.getLines().isEmpty();
        emptyLabel.setVisible(isEmpty);
        emptyLabel.setManaged(isEmpty);
        chart.setVisible(!isEmpty);
        chart.setManaged(!isEmpty);
    }

    private void renderLegend(List<BigPictureDataBuilder.LegendItem> items) {
        legendBox.getChildren().clear();

        for (BigPictureDataBuilder.LegendItem item : items) {
            Region dot = new Region();
            dot.setPrefSize(10, 10);
            dot.setMinSize(10, 10);
            dot.setStyle("-fx-background-color: " + item.getColor() + "; -fx-background-radius: 5px;");

            Label name = new Label(item.getCourseName());
            name.setStyle("-fx-font-size: " + ViewStyleConfig.FONT_SIZE_LABEL + "px; -fx-text-fill: " + ViewStyleConfig.COLOR_DARK_GRAY + ";");

            HBox entry = new HBox(6, dot, name);
            entry.setAlignment(Pos.CENTER_LEFT);

            legendBox.getChildren().add(entry);
        }
    }
}