package com.example.koku.ui;

import com.example.koku.config.BoardSizeOption;
import com.example.koku.config.LanguageMode;
import com.example.koku.config.RuleConfig;
import com.example.koku.config.ThemeMode;
import com.example.koku.config.TimerOption;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class SettingsPanel extends VBox {
    private final Label titleLabel;
    private final Label hintLabel;

    private final Label boardSizeLabel;
    private final ComboBox<BoardSizeOption> boardSizeBox;

    private final Label forbiddenLabel;
    private final CheckBox forbiddenCheck;

    private final Label timerLabel;
    private final ComboBox<TimerOption> timerBox;

    private final Label appearanceLabel;
    private final ComboBox<ThemeMode> themeBox;

    private final Label languageLabel;
    private final ComboBox<LanguageMode> languageBox;

    private final CheckBox coordinatesCheck;
    private final CheckBox lastMoveMarkerCheck;

    private final Button applyButton;
    private final Button closeButton;

    private String primaryText = "#2B2B2B";
    private String buttonBg = "#F8F8F6";
    private LanguageMode displayLanguageMode = LanguageMode.ZH_CN;

    public SettingsPanel() {
        setSpacing(14);
        setPadding(new Insets(24));
        setPrefWidth(336);
        setMinWidth(336);
        setMaxWidth(336);

        titleLabel = new Label("Settings");
        hintLabel = new Label("");

        boardSizeLabel = new Label("Board Size");
        boardSizeBox = new ComboBox<>();
        boardSizeBox.getItems().addAll(BoardSizeOption.values());

        forbiddenLabel = new Label("Forbidden Moves");
        forbiddenCheck = new CheckBox();

        timerLabel = new Label("Timer");
        timerBox = new ComboBox<>();
        timerBox.getItems().addAll(TimerOption.values());

        appearanceLabel = new Label("Appearance");
        themeBox = new ComboBox<>();
        themeBox.getItems().addAll(ThemeMode.values());

        languageLabel = new Label("Language");
        languageBox = new ComboBox<>();
        languageBox.getItems().addAll(LanguageMode.values());

        coordinatesCheck = new CheckBox("Show Coordinates");
        lastMoveMarkerCheck = new CheckBox("Show Last Move Marker");

        applyButton = new Button("Apply & New Match");
        closeButton = new Button("Close");

        VBox rulesGroup = createGroup(boardSizeLabel, boardSizeBox, forbiddenLabel, forbiddenCheck, timerLabel, timerBox);
        VBox appearanceGroup = createGroup(appearanceLabel, themeBox, languageLabel, languageBox);
        VBox prefGroup = createGroup(coordinatesCheck, lastMoveMarkerCheck);

        VBox buttonBox = new VBox(10, applyButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(titleLabel, hintLabel, rulesGroup, appearanceGroup, prefGroup, buttonBox);
    }

    private VBox createGroup(Node... nodes) {
        VBox box = new VBox(10);
        box.getChildren().addAll(nodes);
        box.setPadding(new Insets(14));
        return box;
    }

    public void setTexts(String title, String hint, String boardSize, String forbidden, String timer,
                         String appearance, String language, String showCoordinates, String showMarker,
                         String apply, String close, LanguageMode languageMode) {
        this.displayLanguageMode = languageMode;

        titleLabel.setText(title);
        hintLabel.setText(hint);
        boardSizeLabel.setText(boardSize);
        forbiddenLabel.setText(forbidden);
        timerLabel.setText(timer);
        appearanceLabel.setText(appearance);
        languageLabel.setText(language);
        coordinatesCheck.setText(showCoordinates);
        lastMoveMarkerCheck.setText(showMarker);
        applyButton.setText(apply);
        closeButton.setText(close);

        refreshComboRenderers();
    }

    public void applyTheme(String panelBg, String primaryText, String secondaryText,
                           String buttonBg, String buttonBorder, String cardBorder) {
        this.primaryText = primaryText;
        this.buttonBg = buttonBg;

        setStyle("""
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-width: 0 0 0 1;
                """.formatted(panelBg, cardBorder));

        titleLabel.setStyle("""
                -fx-text-fill: %s;
                -fx-font-size: 18px;
                -fx-font-weight: 600;
                """.formatted(primaryText));

        hintLabel.setStyle("""
                -fx-text-fill: %s;
                -fx-font-size: 12px;
                """.formatted(secondaryText));

        String labelStyle = """
                -fx-text-fill: %s;
                -fx-font-size: 13px;
                -fx-font-weight: 600;
                """.formatted(primaryText);

        boardSizeLabel.setStyle(labelStyle);
        forbiddenLabel.setStyle(labelStyle);
        timerLabel.setStyle(labelStyle);
        appearanceLabel.setStyle(labelStyle);
        languageLabel.setStyle(labelStyle);

        coordinatesCheck.setStyle("-fx-text-fill: %s;".formatted(primaryText));
        lastMoveMarkerCheck.setStyle("-fx-text-fill: %s;".formatted(primaryText));
        forbiddenCheck.setStyle("-fx-text-fill: %s;".formatted(primaryText));

        String comboStyle = """
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                -fx-font-size: 13px;
                """.formatted(buttonBg, buttonBorder);

        boardSizeBox.setStyle(comboStyle);
        timerBox.setStyle(comboStyle);
        themeBox.setStyle(comboStyle);
        languageBox.setStyle(comboStyle);

        applyButton.setStyle("""
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                -fx-text-fill: %s;
                -fx-font-weight: 600;
                -fx-pref-height: 38;
                """.formatted(buttonBg, buttonBorder, primaryText));

        closeButton.setStyle("""
                -fx-background-color: transparent;
                -fx-border-color: %s;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                -fx-text-fill: %s;
                -fx-font-weight: 500;
                -fx-pref-height: 38;
                """.formatted(buttonBorder, primaryText));

        getChildren().stream()
                .filter(node -> node instanceof VBox)
                .map(node -> (VBox) node)
                .forEach(box -> box.setStyle("""
                        -fx-background-color: %s;
                        -fx-border-color: %s;
                        -fx-border-radius: 18;
                        -fx-background-radius: 18;
                        """.formatted(buttonBg, cardBorder)));

        refreshComboRenderers();
    }

    private void refreshComboRenderers() {
        applyRenderer(boardSizeBox, value -> value == null ? "" : value.displayLabel(displayLanguageMode));
        applyRenderer(timerBox, value -> value == null ? "" : value.displayLabel(displayLanguageMode));
        applyRenderer(themeBox, value -> value == null ? "" : value.displayLabel(displayLanguageMode));
        applyRenderer(languageBox, value -> value == null ? "" : value.displayLabel(displayLanguageMode));
    }

    private <T> void applyRenderer(ComboBox<T> comboBox, java.util.function.Function<T, String> labelProvider) {
        comboBox.setButtonCell(createCell(labelProvider));
        comboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<T> call(javafx.scene.control.ListView<T> param) {
                return createCell(labelProvider);
            }
        });
    }

    private <T> ListCell<T> createCell(java.util.function.Function<T, String> labelProvider) {
        return new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(labelProvider.apply(item));
                }
                setStyle("""
                        -fx-text-fill: %s;
                        -fx-background-color: %s;
                        """.formatted(primaryText, buttonBg));
            }
        };
    }

    public void loadFromState(RuleConfig pendingRuleConfig, ThemeMode themeMode, LanguageMode languageMode,
                              boolean showCoordinates, boolean showLastMoveMarker) {
        boardSizeBox.setValue(pendingRuleConfig.boardSizeOption());
        forbiddenCheck.setSelected(pendingRuleConfig.forbiddenMovesEnabled());
        timerBox.setValue(pendingRuleConfig.timerOption());
        themeBox.setValue(themeMode);
        languageBox.setValue(languageMode);
        coordinatesCheck.setSelected(showCoordinates);
        lastMoveMarkerCheck.setSelected(showLastMoveMarker);
    }

    public RuleConfig buildPendingRuleConfig() {
        return new RuleConfig(
                boardSizeBox.getValue() == null ? BoardSizeOption.SIZE_15 : boardSizeBox.getValue(),
                forbiddenCheck.isSelected(),
                timerBox.getValue() == null ? TimerOption.OFF : timerBox.getValue()
        );
    }

    public ThemeMode selectedThemeMode() {
        return themeBox.getValue() == null ? ThemeMode.LIGHT : themeBox.getValue();
    }

    public LanguageMode selectedLanguageMode() {
        return languageBox.getValue() == null ? LanguageMode.ZH_CN : languageBox.getValue();
    }

    public boolean isShowCoordinatesSelected() {
        return coordinatesCheck.isSelected();
    }

    public boolean isShowLastMoveMarkerSelected() {
        return lastMoveMarkerCheck.isSelected();
    }

    public Button getApplyButton() {
        return applyButton;
    }

    public Button getCloseButton() {
        return closeButton;
    }

    public ComboBox<ThemeMode> getThemeBox() {
        return themeBox;
    }

    public ComboBox<LanguageMode> getLanguageBox() {
        return languageBox;
    }

    public CheckBox getCoordinatesCheck() {
        return coordinatesCheck;
    }

    public CheckBox getLastMoveMarkerCheck() {
        return lastMoveMarkerCheck;
    }
}