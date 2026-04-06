package com.example.koku.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TopBarView extends BorderPane {
    private final Label titleLabel;
    private final Label statusLabel;
    private final Label subStatusLabel;
    private final Button newMatchButton;
    private final Button undoButton;
    private final Button settingsButton;

    public TopBarView() {
        titleLabel = new Label("Koku / 观子");
        statusLabel = new Label("Black to Move");
        subStatusLabel = new Label("");

        newMatchButton = createTopButton("New Match");
        undoButton = createTopButton("Undo");
        settingsButton = createTopButton("Settings");

        VBox centerBox = new VBox(2, statusLabel, subStatusLabel);
        centerBox.setAlignment(Pos.CENTER);

        HBox rightBox = new HBox(10, newMatchButton, undoButton, settingsButton);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        setLeft(titleLabel);
        setCenter(centerBox);
        setRight(rightBox);

        BorderPane.setAlignment(titleLabel, Pos.CENTER_LEFT);
        BorderPane.setAlignment(centerBox, Pos.CENTER);
        BorderPane.setAlignment(rightBox, Pos.CENTER_RIGHT);

        setPadding(new Insets(22, 28, 18, 28));
    }

    private Button createTopButton(String text) {
        Button button = new Button(text);
        button.setPrefHeight(36);
        button.setMinWidth(92);
        button.setFocusTraversable(false);
        return button;
    }

    public void setTexts(String title, String status, String subStatus,
                         String newMatch, String undo, String settings) {
        titleLabel.setText(title);
        statusLabel.setText(status);
        subStatusLabel.setText(subStatus);
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
                -fx-font-size: 20px;
                -fx-font-weight: 600;
                """.formatted(primaryText));

        statusLabel.setStyle("""
                -fx-text-fill: %s;
                -fx-font-size: 18px;
                -fx-font-weight: 600;
                """.formatted(primaryText));

        subStatusLabel.setStyle("""
                -fx-text-fill: %s;
                -fx-font-size: 12px;
                """.formatted(secondaryText));

        String buttonStyle = """
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                -fx-padding: 0 14 0 14;
                -fx-font-size: 13px;
                -fx-font-weight: 500;
                -fx-text-fill: %s;
                """.formatted(buttonBg, buttonBorder, primaryText);

        newMatchButton.setStyle(buttonStyle);
        undoButton.setStyle(buttonStyle);
        settingsButton.setStyle(buttonStyle);
    }

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