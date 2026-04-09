package com.example.koku.ui;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.ArrayList;
import java.util.List;

public class TopBarView extends BorderPane {
    private final Label titleLabel;
    private final Label blackStatusLabel;
    private final Label whiteStatusLabel;
    private final TimerDisplay blackTimerDisplay;
    private final TimerDisplay whiteTimerDisplay;
    private final Region timerDivider;
    private final Circle blackIndicator;
    private final Circle whiteIndicator;
    private final StackPane statusPill;
    private final Button newMatchButton;
    private final Button undoButton;
    private final Button settingsButton;
    private String statusBaseStyle;
    private String statusEmphasisStyle;
    private String timerBaseStyle;
    private double statusOffset;

    public TopBarView() {
        titleLabel = new Label("Koku / 观子");
        blackStatusLabel = new Label("Black");
        whiteStatusLabel = new Label("White");
        blackTimerDisplay = createTimerDisplay();
        whiteTimerDisplay = createTimerDisplay();
        timerDivider = new Region();
        blackIndicator = new Circle(6);
        whiteIndicator = new Circle(6);
        statusPill = new StackPane();

        newMatchButton = createTopButton("New Match");
        undoButton = createTopButton("Undo");
        settingsButton = createTopButton("Settings");

        blackStatusLabel.setMinWidth(96);
        blackStatusLabel.setAlignment(Pos.CENTER_RIGHT);
        whiteStatusLabel.setMinWidth(96);
        whiteStatusLabel.setAlignment(Pos.CENTER_LEFT);

        HBox statusRow = new HBox(
                10,
                blackStatusLabel,
                blackIndicator,
                blackTimerDisplay.box(),
                timerDivider,
                whiteTimerDisplay.box(),
                whiteIndicator,
                whiteStatusLabel
        );
        statusRow.setAlignment(Pos.CENTER);

        statusPill.getChildren().add(statusRow);
        statusPill.setPadding(new Insets(8, 14, 8, 14));

        VBox centerBox = new VBox(statusPill);
        centerBox.setAlignment(Pos.CENTER);

        HBox leftButtons = new HBox(10, newMatchButton);
        leftButtons.setAlignment(Pos.CENTER_LEFT);

        HBox leftBox = new HBox(16, titleLabel, leftButtons);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        HBox rightBox = new HBox(10, undoButton, settingsButton);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        setLeft(leftBox);
        setCenter(centerBox);
        setRight(rightBox);

        BorderPane.setAlignment(leftBox, Pos.CENTER_LEFT);
        BorderPane.setAlignment(centerBox, Pos.CENTER);
        BorderPane.setAlignment(rightBox, Pos.CENTER_RIGHT);

        setPadding(new Insets(22, 28, 18, 28));
        BorderPane.setMargin(centerBox, new Insets(0, 72, 0, 72));
    }

    private Button createTopButton(String text) {
        Button button = new Button(text);
        button.setPrefHeight(36);
        button.setMinWidth(92);
        button.setFocusTraversable(false);
        return button;
    }

    public void setTexts(String title, String blackStatus, String whiteStatus, String blackTimer, String whiteTimer,
                         String newMatch, String undo, String settings) {
        titleLabel.setText(title);
        blackStatusLabel.setText(blackStatus);
        whiteStatusLabel.setText(whiteStatus);
        applyTimerText(blackTimerDisplay, blackTimer);
        applyTimerText(whiteTimerDisplay, whiteTimer);
        newMatchButton.setText(newMatch);
        undoButton.setText(undo);
        settingsButton.setText(settings);
    }

    public void applyTheme(String primaryText, String secondaryText, String buttonBg,
                           String buttonBorder, String panelBg, String cardBorder) {
        setStyle("""
                -fx-background-color: %s;
                -fx-border-color: transparent transparent %s transparent;
                -fx-border-width: 0 0 1 0;
                """.formatted(panelBg, cardBorder));

        titleLabel.setStyle("""
                -fx-text-fill: %s;
                -fx-font-size: 19px;
                -fx-font-weight: 600;
                """.formatted(primaryText));

        statusBaseStyle = """
                -fx-text-fill: %s;
                -fx-font-size: 16px;
                -fx-font-weight: 500;
                """.formatted(primaryText);
        statusEmphasisStyle = statusBaseStyle + "-fx-font-weight: 700;";
        blackStatusLabel.setStyle(statusBaseStyle);
        whiteStatusLabel.setStyle(statusBaseStyle);

        timerBaseStyle = """
                -fx-text-fill: %s;
                -fx-font-size: 18px;
                -fx-font-weight: 600;
                """.formatted(primaryText);
        applyTimerStyle(blackTimerDisplay);
        applyTimerStyle(whiteTimerDisplay);

        statusPill.setStyle("""
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-radius: 16;
                -fx-background-radius: 16;
                """.formatted(panelBg, cardBorder));

        timerDivider.setStyle("-fx-background-color: %s;".formatted(cardBorder));
        timerDivider.setPrefWidth(1);
        timerDivider.setMinHeight(18);

        String buttonStyle = """
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                -fx-padding: 0 14 0 14;
                -fx-font-size: 14px;
                -fx-font-weight: 500;
                -fx-text-fill: %s;
                """.formatted(buttonBg, buttonBorder, primaryText);

        newMatchButton.setStyle(buttonStyle);
        undoButton.setStyle(buttonStyle);
        settingsButton.setStyle(buttonStyle);

        applyIndicatorColors();
    }

    public void setStatusOffset(double offset) {
        this.statusOffset = offset;
        statusPill.setTranslateX(statusOffset);
    }

    public double getDividerCenterXInScene() {
        if (timerDivider.getScene() == null) {
            return Double.NaN;
        }
        Bounds bounds = timerDivider.localToScene(timerDivider.getBoundsInLocal());
        return bounds.getMinX() + bounds.getWidth() / 2.0;
    }

    public void applyStatusEmphasis(boolean emphasizeBlack, boolean emphasizeWhite) {
        blackStatusLabel.setStyle(emphasizeBlack ? statusEmphasisStyle : statusBaseStyle);
        whiteStatusLabel.setStyle(emphasizeWhite ? statusEmphasisStyle : statusBaseStyle);
    }

    private void applyIndicatorColors() {
        blackIndicator.setFill(Color.web("#0E1116"));
        blackIndicator.setStroke(Color.web("#CDD6E3"));
        blackIndicator.setStrokeWidth(1.0);
        whiteIndicator.setFill(Color.web("#F5F7FA"));
        whiteIndicator.setStroke(Color.web("#AEB7C4"));
        whiteIndicator.setStrokeWidth(1.2);
    }

    private TimerDisplay createTimerDisplay() {
        HBox box = new HBox(2);
        box.setAlignment(Pos.CENTER);
        List<Label> digits = new ArrayList<>();
        box.getChildren().add(createDigitLabel(digits));
        box.getChildren().add(createDigitLabel(digits));
        box.getChildren().add(createSeparatorLabel());
        box.getChildren().add(createDigitLabel(digits));
        box.getChildren().add(createDigitLabel(digits));
        box.setMinWidth(64);
        return new TimerDisplay(box, digits);
    }

    private Label createDigitLabel(List<Label> digits) {
        Label label = new Label("0");
        label.setMinWidth(12);
        label.setAlignment(Pos.CENTER);
        digits.add(label);
        return label;
    }

    private Label createSeparatorLabel() {
        Label label = new Label(":");
        label.setMinWidth(8);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private void applyTimerText(TimerDisplay display, String timerText) {
        if (timerText == null || timerText.isBlank()) {
            display.box().setVisible(false);
            display.box().setManaged(false);
            return;
        }
        display.box().setVisible(true);
        display.box().setManaged(true);
        String normalized = timerText.replace(":", "");
        char[] chars = normalized.toCharArray();
        int index = 0;
        for (Label label : display.digits()) {
            if (index >= chars.length) {
                label.setText("0");
                index++;
                continue;
            }
            label.setText(String.valueOf(chars[index]));
            index++;
        }
    }

    private void applyTimerStyle(TimerDisplay display) {
        display.digits().forEach(label -> label.setStyle(timerBaseStyle));
        display.box().getChildren().stream()
                .filter(node -> node instanceof Label)
                .map(node -> (Label) node)
                .forEach(label -> label.setStyle(timerBaseStyle));
    }

    private record TimerDisplay(HBox box, List<Label> digits) {}

    public Button getNewMatchButton() {
        return newMatchButton;
    }

    public Button getUndoButton() {
        return undoButton;
    }

    public Button getSettingsButton() {
        return settingsButton;
    }
}
