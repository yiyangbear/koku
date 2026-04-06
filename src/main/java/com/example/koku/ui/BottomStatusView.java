package com.example.koku.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class BottomStatusView extends BorderPane {
    private final Label leftLabel;
    private final Label rightLabel;

    public BottomStatusView() {
        leftLabel = new Label();
        rightLabel = new Label();

        setLeft(leftLabel);
        setRight(rightLabel);

        BorderPane.setAlignment(leftLabel, Pos.CENTER_LEFT);
        BorderPane.setAlignment(rightLabel, Pos.CENTER_RIGHT);

        setPadding(new Insets(16, 28, 20, 28));
    }

    public void setTexts(String left, String right) {
        leftLabel.setText(left);
        rightLabel.setText(right);
    }

    public void applyTheme(String subtleText) {
        String style = """
                -fx-text-fill: %s;
                -fx-font-size: 12px;
                -fx-font-weight: 400;
                """.formatted(subtleText);
        leftLabel.setStyle(style);
        rightLabel.setStyle(style);
    }
}