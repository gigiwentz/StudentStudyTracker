package edu.ucsd.studentclock.view;

import edu.ucsd.studentclock.config.DateTimeFormatConfig;
import edu.ucsd.studentclock.config.ViewStyleConfig;
import edu.ucsd.studentclock.service.BigPictureDataBuilder;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BigPictureChart extends LineChart<Number, Number> {
    private static final long MINUTES_PER_DAY = 60L * 24L;
    
    // 使用配置类中的格式
    private static final DateTimeFormatter TICK_FMT = DateTimeFormatConfig.DATE_SHORT;
    private static final DateTimeFormatter TOOLTIP_DATE_FMT = DateTimeFormatConfig.TOOLTIP_DATE;
    
    private static final Duration TOOLTIP_SHOW_DELAY = Duration.millis(40);
    private static final Duration TOOLTIP_HIDE_DELAY = Duration.millis(0);
    private static final Duration TOOLTIP_SHOW_DURATION = Duration.seconds(30);
    
    // 使用配置类中的样式
    private static final String TARGET_LINE_STYLE = ViewStyleConfig.CHART_TARGET_LINE_STYLE;

    private final List<Long> startMarkerMinutes = new ArrayList<>();
    private final List<Line> startMarkers = new ArrayList<>();
    private final List<Path> assignmentPaths = new ArrayList<>();
    private final List<Circle> assignmentDots = new ArrayList<>();
    private final List<BigPictureDataBuilder.AssignmentLine> assignmentLinesToDraw = new ArrayList<>();
    
    private static final double DOT_RADIUS = ViewStyleConfig.RADIUS_DOT;
    private Line targetLineNode;
    private BigPictureDataBuilder.TargetLine targetLine;

    /** Dummy series so the chart has data and lays out the plot area; line is hidden in layout. */
    private static final String DUMMY_SERIES_NAME = " ";

    public BigPictureChart() {
        super(new NumberAxis(), new NumberAxis());
        
        setAnimated(false);
        setCreateSymbols(false);
        setLegendVisible(false);

        NumberAxis xAxis = (NumberAxis) getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit(MINUTES_PER_DAY);
        xAxis.setMinorTickVisible(false);
        xAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number object) {
                if (object == null) return "";
                long epochMinutes = object.longValue();
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(epochMinutes * 60L),
                    ZoneOffset.UTC
                );
                return dateTime.format(TICK_FMT);
            }

            @Override
            public Number fromString(String string) {
                return 0;
            }
        });

        NumberAxis yAxis = (NumberAxis) getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setMinorTickVisible(false);
        yAxis.setLabel("Remaining hours");

        setHorizontalGridLinesVisible(true);
        setVerticalGridLinesVisible(false);
    }

    public void setRange(BigPictureDataBuilder.Range range) {
        long minX = toEpochMinutes(range.getStartDateTime());
        long maxX = toEpochMinutes(range.getEndDateTime());
        
        NumberAxis xAxis = (NumberAxis) getXAxis();
        xAxis.setLowerBound(minX);
        xAxis.setUpperBound(maxX);
    }

    public void setYMax(double yMax) {
        NumberAxis yAxis = (NumberAxis) getYAxis();
        double max = Math.max(1.0, yMax);
        yAxis.setLowerBound(0.0);
        yAxis.setUpperBound(niceCeil(max));
        yAxis.setTickUnit(Math.max(0.5, niceCeil(max) / 5.0));
    }

    public void setTargetLine(BigPictureDataBuilder.TargetLine targetLine) {
        this.targetLine = targetLine;
    }

    public void setStartMarkers(List<Long> epochMinutes) {
        startMarkerMinutes.clear();
        if (epochMinutes != null) startMarkerMinutes.addAll(epochMinutes);
    }

    public void setLines(List<BigPictureDataBuilder.AssignmentLine> lines) {
        getData().clear();
        assignmentLinesToDraw.clear();
        assignmentLinesToDraw.addAll(lines);

        // One dummy series so the chart lays out the plot area
        NumberAxis xAxis = (NumberAxis) getXAxis();
        NumberAxis yAxis = (NumberAxis) getYAxis();
        
        double minX = xAxis.getLowerBound();
        double maxX = xAxis.getUpperBound();
        double maxY = yAxis.getUpperBound();

        XYChart.Series<Number, Number> dummy = new XYChart.Series<>();
        dummy.setName(DUMMY_SERIES_NAME);
        dummy.getData().add(new XYChart.Data<>(minX, 0.0));
        dummy.getData().add(new XYChart.Data<>(maxX, maxY));

        getData().add(dummy);
        refreshLayout();
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();

        // Hide the dummy series line
        if (!getData().isEmpty() && getData().get(0).getName() != null && 
            DUMMY_SERIES_NAME.equals(getData().get(0).getName())) {
            for (Node node : getPlotChildren()) {
                if (node instanceof Path) {
                    node.setVisible(false);
                    break;
                }
            }
        }

        layoutAssignmentLines();
        layoutStartMarkers();
        layoutTargetLine();
    }

    private void refreshLayout() {
        applyCss();
        layout();
    }

    private void layoutAssignmentLines() {
        getPlotChildren().removeAll(assignmentPaths);
        getPlotChildren().removeAll(assignmentDots);
        assignmentPaths.clear();
        assignmentDots.clear();

        for (BigPictureDataBuilder.AssignmentLine meta : assignmentLinesToDraw) {
            List<BigPictureDataBuilder.Point> points = meta.getPoints();
            if (points.size() < 2) continue;

            List<String> segmentColors = meta.getSegmentColors();
            List<String> segmentTooltips = meta.getSegmentTooltips();
            String defaultTooltipText = buildTooltipText(meta);
            
            boolean perSegmentColor = segmentColors != null && segmentColors.size() >= points.size() - 1;
            boolean perSegmentTooltip = segmentTooltips != null && segmentTooltips.size() >= points.size() - 1;

            if (perSegmentColor) {
                for (int i = 0; i < points.size() - 1; i++) {
                    double x1 = getXAxis().getDisplayPosition(points.get(i).getEpochMinutes());
                    double y1 = getYAxis().getDisplayPosition(points.get(i).getRemainingHours());
                    double x2 = getXAxis().getDisplayPosition(points.get(i + 1).getEpochMinutes());
                    double y2 = getYAxis().getDisplayPosition(points.get(i + 1).getRemainingHours());

                    if (Double.isNaN(x1) || Double.isNaN(y1) || Double.isNaN(x2) || Double.isNaN(y2)) continue;

                    String color = segmentColors.get(i);
                    String tip = perSegmentTooltip ? segmentTooltips.get(i) : defaultTooltipText;

                    Path segment = new Path();
                    segment.getElements().add(new MoveTo(x1, y1));
                    segment.getElements().add(new LineTo(x2, y2));
                    segment.setStyle(String.format(ViewStyleConfig.CHART_SEGMENT_STYLE, color));
                    segment.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
                    segment.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
                    
                    Tooltip.install(segment, createTooltip(tip));
                    assignmentPaths.add(segment);
                }

                // Dots at each vertex between work sessions
                for (int i = 1; i < points.size() - 1; i++) {
                    double cx = getXAxis().getDisplayPosition(points.get(i).getEpochMinutes());
                    double cy = getYAxis().getDisplayPosition(points.get(i).getRemainingHours());

                    if (Double.isNaN(cx) || Double.isNaN(cy)) continue;

                    Circle dot = new Circle(DOT_RADIUS);
                    dot.setCenterX(cx);
                    dot.setCenterY(cy);
                    dot.setStyle(ViewStyleConfig.CHART_DOT_STYLE);
                    
                    assignmentDots.add(dot);
                }
            } else {
                String color = meta.getCourseColor();
                
                double x0 = getXAxis().getDisplayPosition(points.get(0).getEpochMinutes());
                double y0 = getYAxis().getDisplayPosition(points.get(0).getRemainingHours());

                if (Double.isNaN(x0) || Double.isNaN(y0)) continue;

                Path path = new Path();
                path.getElements().add(new MoveTo(x0, y0));

                for (int i = 1; i < points.size(); i++) {
                    double x = getXAxis().getDisplayPosition(points.get(i).getEpochMinutes());
                    double y = getYAxis().getDisplayPosition(points.get(i).getRemainingHours());
                    
                    if (Double.isNaN(x) || Double.isNaN(y)) continue;
                    
                    path.getElements().add(new LineTo(x, y));
                }

                if (path.getElements().size() < 2) continue;

                path.setStyle(String.format(ViewStyleConfig.CHART_SEGMENT_STYLE, color));
                path.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
                path.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
                
                Tooltip.install(path, createTooltip(defaultTooltipText));
                assignmentPaths.add(path);
            }
        }

        getPlotChildren().addAll(assignmentPaths);
        getPlotChildren().addAll(assignmentDots);
    }

    private static Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(TOOLTIP_SHOW_DELAY);
        tooltip.setHideDelay(TOOLTIP_HIDE_DELAY);
        tooltip.setShowDuration(TOOLTIP_SHOW_DURATION);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(360);
        return tooltip;
    }

    private void layoutStartMarkers() {
        getPlotChildren().removeAll(startMarkers);
        startMarkers.clear();
        // Vertical dashed start lines removed per user request
    }

    private void layoutTargetLine() {
        if (targetLineNode != null) {
            getPlotChildren().remove(targetLineNode);
            targetLineNode = null;
        }

        if (targetLine == null) return;

        double x1 = getXAxis().getDisplayPosition(targetLine.getXStartEpochMinutes());
        double x2 = getXAxis().getDisplayPosition(targetLine.getXEndEpochMinutes());
        double y1 = getYAxis().getDisplayPosition(targetLine.getYStart());
        double y2 = getYAxis().getDisplayPosition(targetLine.getYEnd());

        if (Double.isNaN(x1) || Double.isNaN(x2) || Double.isNaN(y1) || Double.isNaN(y2)) return;

        targetLineNode = new Line(x1, y1, x2, y2);
        targetLineNode.setStyle(TARGET_LINE_STYLE);
        
        getPlotChildren().add(targetLineNode);
    }

    private Bounds getPlotAreaBounds() {
        Node plotArea = lookup(".chart-plot-background");
        if (plotArea != null) return plotArea.getBoundsInParent();
        return getBoundsInLocal();
    }

    private static String buildTooltipText(BigPictureDataBuilder.AssignmentLine meta) {
        String behindText;
        if (meta.getBehindPercent() < 0) {
            behindText = String.format("%.0f%% behind", Math.min(100.0, -meta.getBehindPercent()));
        } else if (meta.getBehindPercent() > 0) {
            behindText = String.format("%.0f%% ahead", meta.getBehindPercent());
        } else {
            behindText = "On track";
        }

        return meta.getCourseName() + "\n"
            + meta.getTitle() + "\n\n"
            + String.format("Estimated: %.1f h\n", meta.getEstimatedHours())
            + String.format("Finished: %.1f h\n", meta.getFinishedHours())
            + String.format("Remaining: %.1f h\n", meta.getRemainingHours())
            + "Due: " + meta.getDueDate().toLocalDate().format(TOOLTIP_DATE_FMT) + "\n"
            + behindText;
    }

    private static double niceCeil(double value) {
        if (value <= 1.0) return 1.0;
        double pow = Math.pow(10, Math.floor(Math.log10(value)));
        double n = value / pow;
        double rounded = n <= 1 ? 1 : n <= 2 ? 2 : n <= 5 ? 5 : 10;
        return rounded * pow;
    }

    private static long toEpochMinutes(LocalDateTime time) {
        return time.toEpochSecond(ZoneOffset.UTC) / 60;
    }
}