package com.chickencenter.ui.controllers;

import com.chickencenter.database.DatabaseConnection;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DashboardController {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox mainContainer;
    @FXML private VBox donutSection;
    @FXML private Label donutTitle;
    @FXML private Label donutSubtitle;
    @FXML private StackPane donutContainer;
    @FXML private VBox donutLegend;
    @FXML private VBox lineSection;
    @FXML private Label lineTitle;
    @FXML private Label lineSubtitle;
    @FXML private StackPane lineContainer;

    private static final Color[] CHART_COLORS = {
            Color.valueOf("#3B82F6"), // Primary Blue
            Color.valueOf("#10B981"), // Green
            Color.valueOf("#F59E0B"), // Orange
            Color.valueOf("#EF4444"), // Red
            Color.valueOf("#8B5CF6"), // Purple
            Color.valueOf("#06B6D4"), // Sky Blue
            Color.valueOf("#EC4899"), // Pink
            Color.valueOf("#84CC16"), // Lime Green
            Color.valueOf("#F97316"), // Deep Orange
            Color.valueOf("#14B8A6"), // Teal
            Color.valueOf("#A855F7"), // Violet
            Color.valueOf("#EAB308"), // Gold
            Color.valueOf("#6366F1"), // Indigo
            Color.valueOf("#22C55E"), // Bright Green
            Color.valueOf("#0EA5E9"), // Light Blue
            Color.valueOf("#D946EF"), // Magenta
            Color.valueOf("#F43F5E"), // Rose
            Color.valueOf("#65A30D"), // Olive Green
            Color.valueOf("#C2410C"), // Burnt Orange
            Color.valueOf("#4338CA")  // Dark Indigo
    };

    @FXML
    public void initialize() {
        applyStyles();
        animateEntry();
        loadData();
    }

    private void applyStyles() {
        if (mainContainer != null) {
            mainContainer.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 24 28 32 28;");
            mainContainer.setSpacing(28);
        }
    }

    private void animateEntry() {
        List<Node> sections = new ArrayList<>();
        if (donutSection != null) sections.add(donutSection);
        if (lineSection != null) sections.add(lineSection);

        for (int i = 0; i < sections.size(); i++) {
            Node s = sections.get(i);
            s.setOpacity(0);
            s.setTranslateY(20);

            FadeTransition fade = new FadeTransition(Duration.millis(400), s);
            fade.setFromValue(0); fade.setToValue(1);
            fade.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition slide = new TranslateTransition(Duration.millis(400), s);
            slide.setFromY(20); slide.setToY(0);
            slide.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition pt = new ParallelTransition(fade, slide);
            pt.setDelay(Duration.millis(60 + i * 100));
            pt.play();
        }
    }

    public void refreshDashboard() {
        loadData();
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            try {
                List<DonutSegment> donutData = getDonutData();
                List<DailyData> dailyData = getDailySales(7);

                Platform.runLater(() -> {
                    renderDonut(donutData);
                    renderLineChart(dailyData);
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private List<DonutSegment> getDonutData() throws SQLException {
        List<DonutSegment> list = new ArrayList<>();
        String sql = "SELECT p.product_name, COALESCE(SUM(si.quantity),0) as qty " +
                     "FROM products p LEFT JOIN sale_items si ON p.id = si.item_id " +
                     "WHERE p.is_active = 1 GROUP BY p.id, p.product_name HAVING qty > 0 ORDER BY qty DESC LIMIT 8";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new DonutSegment(rs.getString("product_name"), rs.getDouble("qty")));
        }
        return list;
    }

    private List<DailyData> getDailySales(int days) throws SQLException {
        List<DailyData> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        try (Connection conn = DatabaseConnection.getConnection()) {
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                int count = 0;
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM sales WHERE sale_date=?")) {
                    ps.setString(1, date.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) count = rs.getInt(1);
                    }
                }
                list.add(new DailyData(date, count));
            }
        }
        return list;
    }

    /* ===== DONUT CHART ===== */

    private VBox tooltipBox = null;
    private final List<DonutSegment> currentData = new ArrayList<>();
    private final List<Arc> originalSlices = new ArrayList<>();

    private void renderDonut(List<DonutSegment> data) {
        donutContainer.getChildren().clear();
        donutLegend.getChildren().clear();
        originalSlices.clear();
        currentData.clear();
        currentData.addAll(data);

        if (data.isEmpty()) {
            Label empty = new Label("No product sales data");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13; -fx-font-family: 'Segoe UI'; -fx-padding: 40 0;");
            donutContainer.getChildren().add(empty);
            return;
        }

        double total = data.stream().mapToDouble(d -> d.qty).sum();

        double chartSize = 200;
        double cx = chartSize / 2;
        double cy = chartSize / 2;
        double outerR = 120;
        double innerR = 75;
        double separatorGap = 1.5;

        Pane chartPane = new Pane();
        chartPane.setPrefSize(chartSize, chartSize);

        Pane overlayLayer = new Pane();
        overlayLayer.setMouseTransparent(true);
        overlayLayer.setPickOnBounds(false);

        tooltipBox = new VBox();
        tooltipBox.setSpacing(3);
        tooltipBox.setAlignment(Pos.CENTER_LEFT);
        tooltipBox.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8; -fx-padding: 10 14;");
        tooltipBox.setEffect(new DropShadow(8, 0, 2, Color.rgb(0, 0, 0, 0.3)));
        tooltipBox.setVisible(false);
        tooltipBox.setMouseTransparent(true);

        Circle baseHole = new Circle(cx, cy, innerR, Color.WHITE);
        chartPane.getChildren().add(baseHole);

        double startAngle = -90;

        for (int i = 0; i < data.size(); i++) {
            DonutSegment seg = data.get(i);
            double pct = total > 0 ? (seg.qty / total) * 100 : 0;
            double sweep = total > 0 ? (seg.qty / total) * 360 : 0;
            if (sweep < 0.5) { startAngle += sweep; continue; }

            Color c = CHART_COLORS[i % CHART_COLORS.length];
            final int fr = (int)(c.getRed() * 255), fg = (int)(c.getGreen() * 255), fb = (int)(c.getBlue() * 255);
            final double fStartAngle = startAngle;
            final double fSweep = sweep;
            final double fPct = pct;

            Arc pieSlice = new Arc(cx, cy, outerR, outerR, startAngle + separatorGap, sweep - separatorGap * 2);
            pieSlice.setType(ArcType.ROUND);
            pieSlice.setFill(Color.rgb(fr, fg, fb));
            pieSlice.setCursor(javafx.scene.Cursor.HAND);
            pieSlice.setSmooth(true);

            final Arc fSlice = pieSlice;

            pieSlice.setOnMouseEntered(e -> {
                for (Arc os : originalSlices) {
                    os.setOpacity(0.45);
                }
                fSlice.setOpacity(1.0);

                Arc expanded = new Arc(cx, cy, outerR + 5, outerR + 5, fStartAngle + separatorGap * 0.5, fSweep - separatorGap);
                expanded.setType(ArcType.ROUND);
                expanded.setFill(Color.rgb(fr, fg, fb));
                expanded.setMouseTransparent(true);
                expanded.setSmooth(true);

                Circle expandedHole = new Circle(cx, cy, innerR - 3, Color.WHITE);
                expandedHole.setMouseTransparent(true);

                overlayLayer.getChildren().clear();
                overlayLayer.getChildren().addAll(expanded, expandedHole);

                tooltipBox.getChildren().clear();
                Label nameLbl = new Label(seg.name);
                nameLbl.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Segoe UI';");
                Label qtyLbl = new Label(String.format("%.0f Units", seg.qty));
                qtyLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #94a3b8; -fx-font-family: 'Segoe UI';");
                Label pctLbl = new Label(String.format("%.1f%%", fPct));
                pctLbl.setStyle("-fx-font-size: 11; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
                tooltipBox.getChildren().addAll(nameLbl, qtyLbl, pctLbl);
                tooltipBox.setVisible(true);
                positionTooltip(e.getScreenX(), e.getScreenY(), chartPane);
            });

            pieSlice.setOnMouseMoved(e -> {
                if (tooltipBox != null && tooltipBox.isVisible()) {
                    positionTooltip(e.getScreenX(), e.getScreenY(), chartPane);
                }
            });

            pieSlice.setOnMouseExited(e -> {
                overlayLayer.getChildren().clear();
                for (Arc os : originalSlices) {
                    os.setOpacity(1.0);
                }
                tooltipBox.setVisible(false);
            });

            originalSlices.add(pieSlice);
            pieSlice.setOpacity(0);
            chartPane.getChildren().add(pieSlice);

            final int fIdx = i;
            Platform.runLater(() -> {
                FadeTransition ft = new FadeTransition(Duration.millis(400), fSlice);
                ft.setToValue(1);
                ft.setDelay(Duration.millis(80 + fIdx * 60));
                ft.play();
            });

            startAngle += sweep;
        }

        Circle finalHole = new Circle(cx, cy, innerR, Color.WHITE);
        finalHole.setMouseTransparent(true);
        chartPane.getChildren().add(finalHole);

        chartPane.getChildren().add(overlayLayer);
        chartPane.getChildren().add(tooltipBox);

        HBox chartWrapper = new HBox(chartPane);
        chartWrapper.setAlignment(Pos.CENTER);
        chartWrapper.setMaxWidth(Double.MAX_VALUE);
        chartWrapper.setPadding(new Insets(10, 0, 10, 0));

        donutContainer.getChildren().add(chartWrapper);

        for (int i = 0; i < data.size(); i++) {
            DonutSegment seg = data.get(i);
            double pct = total > 0 ? (seg.qty / total) * 100 : 0;
            Color c = CHART_COLORS[i % CHART_COLORS.length];

            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setSpacing(8);
            row.setMaxWidth(HBox.USE_PREF_SIZE);

            Circle dot = new Circle(5, Color.rgb((int)(c.getRed() * 255), (int)(c.getGreen() * 255), (int)(c.getBlue() * 255)));
            Label name = new Label(truncate(seg.name, 16));
            name.setStyle("-fx-font-size: 11; -fx-text-fill: #475569; -fx-font-family: 'Segoe UI';");
            Label pctLabel = new Label(String.format("%.1f%%", pct));
            pctLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: " + String.format("#%02x%02x%02x", (int)(c.getRed() * 255), (int)(c.getGreen() * 255), (int)(c.getBlue() * 255)) + "; -fx-font-family: 'Segoe UI';");

            row.getChildren().addAll(dot, name, pctLabel);
            donutLegend.getChildren().add(row);
        }
    }

    private void positionTooltip(double screenX, double screenY, Pane chartPane) {
        if (tooltipBox == null) return;
        tooltipBox.applyCss();
        tooltipBox.layout();
        double tw = tooltipBox.getLayoutBounds().getWidth();
        double th = tooltipBox.getLayoutBounds().getHeight();

        javafx.geometry.Point2D local = chartPane.screenToLocal(screenX, screenY);
        double tx = local.getX() + 15;
        double ty = local.getY() - th - 10;

        if (tx + tw > chartPane.getWidth() - 5) tx = local.getX() - tw - 15;
        if (ty < 5) ty = local.getY() + 15;
        if (ty + th > chartPane.getHeight() - 5) ty = chartPane.getHeight() / 2 - th / 2;
        if (tx < 5) tx = 5;

        tooltipBox.setLayoutX(tx);
        tooltipBox.setLayoutY(ty);
    }

    /* ===== LINE CHART ===== */

    private void renderLineChart(List<DailyData> data) {
        lineContainer.getChildren().clear();

        if (data.isEmpty()) {
            Label empty = new Label("No sales trend available");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13; -fx-font-family: 'Segoe UI'; -fx-padding: 40 0;");
            lineContainer.getChildren().add(empty);
            return;
        }

        boolean hasData = data.stream().anyMatch(d -> d.sales > 0);
        if (!hasData) {
            Label empty = new Label("No sales trend available");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13; -fx-font-family: 'Segoe UI'; -fx-padding: 40 0;");
            lineContainer.getChildren().add(empty);
            return;
        }

        double maxVal = data.stream().mapToDouble(d -> d.sales).max().orElse(1);
        if (maxVal == 0) maxVal = 1;

        double maxRounded = Math.ceil(maxVal / 5.0) * 5;
        if (maxRounded < maxVal) maxRounded += 5;

        double W = 700, H = 300;
        double padL = 55, padR = 25, padT = 20, padB = 45;
        double plotW = W - padL - padR;
        double plotH = H - padT - padB;

        Pane chart = new Pane();
        chart.setPrefSize(W, H);
        chart.setMaxSize(W, H);

        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            double y = padT + (plotH / gridLines) * i;
            Line line = new Line(padL, y, W - padR, y);
            line.setStroke(Color.valueOf("#f0f0f5"));
            line.setStrokeWidth(1);
            chart.getChildren().add(line);

            double val = maxRounded * (gridLines - i) / gridLines;
            Label label = new Label(String.format("%.0f", val));
            label.setStyle("-fx-font-size: 9; -fx-text-fill: #a0a0b0; -fx-font-family: 'Segoe UI';");
            label.setLayoutX(0);
            label.setLayoutY(y - 6);
            label.setPrefWidth(padL - 5);
            label.setAlignment(Pos.CENTER_RIGHT);
            chart.getChildren().add(label);
        }

        double stepX = plotW / (data.size() - 1 > 0 ? data.size() - 1 : 1);

        Path linePath = new Path();
        linePath.setStroke(Color.valueOf("#6366f1"));
        linePath.setStrokeWidth(2.5);
        linePath.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        Path areaPath = new Path();
        areaPath.setFill(Color.valueOf("#6366f120"));

        List<Point> points = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            double x = padL + i * stepX;
            double y = padT + plotH - (data.get(i).sales / maxRounded) * plotH;
            points.add(new Point(x, y));
        }

        if (points.size() > 1) {
            linePath.getElements().add(new MoveTo(points.get(0).x, points.get(0).y));
            areaPath.getElements().add(new MoveTo(points.get(0).x, padT + plotH));
            areaPath.getElements().add(new LineTo(points.get(0).x, points.get(0).y));
            for (int i = 1; i < points.size(); i++) {
                linePath.getElements().add(new CubicCurveTo(
                    points.get(i-1).x + stepX/3, points.get(i-1).y,
                    points.get(i).x - stepX/3, points.get(i).y,
                    points.get(i).x, points.get(i).y));
                areaPath.getElements().add(new LineTo(points.get(i).x, points.get(i).y));
            }
            areaPath.getElements().add(new LineTo(points.get(points.size() - 1).x, padT + plotH));
            areaPath.getElements().add(new ClosePath());
        } else if (points.size() == 1) {
            Circle dot = new Circle(4, Color.valueOf("#6366f1"));
            dot.setLayoutX(points.get(0).x);
            dot.setLayoutY(points.get(0).y);
            dot.setOpacity(0);
            chart.getChildren().add(dot);
            FadeTransition df = new FadeTransition(Duration.millis(400), dot);
            df.setToValue(1); df.setDelay(Duration.millis(300)); df.play();
        }

        chart.getChildren().add(areaPath);
        chart.getChildren().add(linePath);

        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            Circle dot = new Circle(4, Color.WHITE);
            dot.setStroke(Color.valueOf("#6366f1"));
            dot.setStrokeWidth(2.5);
            dot.setLayoutX(p.x);
            dot.setLayoutY(p.y);
            dot.setOpacity(0);
            dot.setScaleX(0);
            dot.setScaleY(0);
            dot.setCursor(javafx.scene.Cursor.HAND);
            chart.getChildren().add(dot);

            final int idx = i;
            Platform.runLater(() -> {
                FadeTransition df = new FadeTransition(Duration.millis(300), dot);
                df.setToValue(1); df.setDelay(Duration.millis(200 + idx * 80)); df.play();

                ScaleTransition ds = new ScaleTransition(Duration.millis(300), dot);
                ds.setToX(1); ds.setToY(1); ds.setDelay(Duration.millis(200 + idx * 80)); ds.play();
            });

            VBox pointTooltip = new VBox();
            pointTooltip.setSpacing(2);
            pointTooltip.setAlignment(Pos.CENTER);
            pointTooltip.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8; -fx-padding: 8 12;");
            pointTooltip.setEffect(new DropShadow(6, 0, 2, Color.rgb(0, 0, 0, 0.25)));
            pointTooltip.setVisible(false);
            pointTooltip.setMouseTransparent(true);

            final String dateStr = data.get(i).date.format(DateTimeFormatter.ofPattern("dd MMM"));
            final int salesCount = (int) data.get(i).sales;

            Label tlDate = new Label(dateStr);
            tlDate.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Segoe UI';");
            Label tlSales = new Label(salesCount + " sales");
            tlSales.setStyle("-fx-font-size: 10; -fx-text-fill: #94a3b8; -fx-font-family: 'Segoe UI';");
            pointTooltip.getChildren().addAll(tlDate, tlSales);
            chart.getChildren().add(pointTooltip);

            dot.setOnMouseEntered(e -> {
                dot.setRadius(6);
                dot.setFill(Color.valueOf("#6366f1"));

                pointTooltip.applyCss();
                pointTooltip.layout();
                double tw = pointTooltip.getLayoutBounds().getWidth();
                double th = pointTooltip.getLayoutBounds().getHeight();
                double tx = p.x - tw / 2;
                double ty = p.y - th - 14;
                if (ty < 5) ty = p.y + 14;
                if (tx < 5) tx = 5;
                if (tx + tw > W - 5) tx = W - tw - 5;
                pointTooltip.setLayoutX(tx);
                pointTooltip.setLayoutY(ty);
                pointTooltip.setVisible(true);
            });

            dot.setOnMouseExited(e -> {
                dot.setRadius(4);
                dot.setFill(Color.WHITE);
                pointTooltip.setVisible(false);
            });

            String dlStr = data.get(i).date.format(DateTimeFormatter.ofPattern("dd MMM"));
            Label dl = new Label(dlStr);
            dl.setStyle("-fx-font-size: 10; -fx-text-fill: #94a3b8; -fx-font-family: 'Segoe UI';");
            dl.setLayoutX(p.x - 18);
            dl.setLayoutY(H - padB + 8);
            dl.setPrefWidth(36);
            chart.getChildren().add(dl);

            if (data.get(i).sales > 0) {
                Label sv = new Label(String.format("%.0f", data.get(i).sales));
                sv.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #6366f1; -fx-font-family: 'Segoe UI';");
                sv.setLayoutX(p.x - 10);
                sv.setLayoutY(p.y - 18);
                sv.setPrefWidth(20);
                sv.setAlignment(Pos.CENTER);
                sv.setOpacity(0);
                chart.getChildren().add(sv);

                FadeTransition svf = new FadeTransition(Duration.millis(250), sv);
                svf.setToValue(1); svf.setDelay(Duration.millis(400 + i * 80)); svf.play();
            }
        }

        Line base = new Line(padL, padT + plotH, W - padR, padT + plotH);
        base.setStroke(Color.valueOf("#e0e0e8"));
        base.setStrokeWidth(1.5);
        chart.getChildren().add(base);

        lineContainer.getChildren().add(chart);
    }

    private String truncate(String s, int len) {
        return s == null ? "" : (s.length() > len ? s.substring(0, len) + "..." : s);
    }

    /* ===== DATA MODELS ===== */

    static class DonutSegment {
        String name; double qty;
        DonutSegment(String n, double q) { name = n; qty = q; }
    }

    static class DailyData {
        LocalDate date; double sales;
        DailyData(LocalDate d, double s) { date = d; sales = s; }
    }

    static class Point {
        double x, y;
        Point(double x, double y) { this.x = x; this.y = y; }
    }
}
