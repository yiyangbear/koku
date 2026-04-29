package com.example.koku.ui;

import com.example.koku.config.BoardSizeOption;
import com.example.koku.config.LanguageMode;
import com.example.koku.config.RuleConfig;
import com.example.koku.config.ThemeMode;
import com.example.koku.config.TimerMode;
import com.example.koku.config.TimerOption;
import com.example.koku.config.TotalTimerOption;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class SettingsPanel extends VBox {
    private final Label titleLabel;
    private final Label hintLabel;

    private final Label boardSizeLabel;
    private final ComboBox<BoardSizeOption> boardSizeBox;
    private final VBox boardSizeSection;

    private final Label forbiddenLabel;
    private final Button forbiddenInfoButton;
    private final CheckBox forbiddenCheck;
    private final HBox forbiddenRow;
    private final VBox forbiddenSection;

    private final CheckBox perMoveEnableCheck;
    private final Label timerLabel;
    private final ComboBox<TimerOption> timerBox;
    private final HBox perMoveCustomRow;
    private final Label perMoveMinutesLabel;
    private final TextField perMoveMinutesField;
    private final Label perMoveSecondsLabel;
    private final TextField perMoveSecondsField;

    private final CheckBox totalEnableCheck;
    private final Label totalTimerLabel;
    private final ComboBox<TotalTimerOption> totalTimerBox;
    private final HBox totalCustomRow;
    private final Label totalMinutesLabel;
    private final TextField totalMinutesField;
    private final Label totalSecondsLabel;
    private final TextField totalSecondsField;

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
        setSpacing(16);
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
        forbiddenInfoButton = createInfoButton();
        forbiddenCheck = new CheckBox();

        perMoveEnableCheck = new CheckBox("Per Move Timer");

        timerLabel = new Label("Timer");
        timerBox = new ComboBox<>();
        timerBox.getItems().addAll(TimerOption.values());

        perMoveMinutesLabel = new Label("Min");
        perMoveMinutesField = createTimeField();
        perMoveSecondsLabel = new Label("Sec");
        perMoveSecondsField = createTimeField();
        perMoveCustomRow = new HBox(8, perMoveMinutesLabel, perMoveMinutesField, perMoveSecondsLabel, perMoveSecondsField);
        perMoveCustomRow.setAlignment(Pos.CENTER_LEFT);

        totalEnableCheck = new CheckBox("Total Timer");
        totalTimerLabel = new Label("Total Timer");
        totalTimerBox = new ComboBox<>();
        totalTimerBox.getItems().addAll(TotalTimerOption.values());

        totalMinutesLabel = new Label("Min");
        totalMinutesField = createTimeField();
        totalSecondsLabel = new Label("Sec");
        totalSecondsField = createTimeField();
        totalCustomRow = new HBox(8, totalMinutesLabel, totalMinutesField, totalSecondsLabel, totalSecondsField);
        totalCustomRow.setAlignment(Pos.CENTER_LEFT);

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

        forbiddenRow = new HBox(6, forbiddenLabel, forbiddenInfoButton);
        forbiddenRow.setAlignment(Pos.CENTER_LEFT);
        boardSizeSection = new VBox(8, boardSizeLabel, boardSizeBox);
        forbiddenSection = new VBox(8, forbiddenRow, forbiddenCheck);

        VBox perMoveSection = new VBox(8, perMoveEnableCheck, timerLabel, timerBox, perMoveCustomRow);
        VBox totalSection = new VBox(8, totalEnableCheck, totalTimerLabel, totalTimerBox, totalCustomRow);

        VBox rulesGroup = createGroup(boardSizeSection, forbiddenSection, perMoveSection, totalSection);
        VBox appearanceGroup = createGroup(appearanceLabel, themeBox, languageLabel, languageBox);
        VBox prefGroup = createGroup(coordinatesCheck, lastMoveMarkerCheck);

        VBox buttonBox = new VBox(10, applyButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(titleLabel, hintLabel, rulesGroup, appearanceGroup, prefGroup, buttonBox);

        bindTimerControls();
        updateTimerSectionState();
    }

    private TextField createTimeField() {
        TextField field = new TextField();
        field.setPrefWidth(44);
        return field;
    }

    private VBox createGroup(Node... nodes) {
        VBox box = new VBox(12);
        box.getChildren().addAll(nodes);
        box.setPadding(new Insets(16));
        return box;
    }

    private Button createInfoButton() {
        Button button = new Button("?");
        button.setPrefSize(18, 18);
        button.setMinSize(18, 18);
        button.setFocusTraversable(false);
        return button;
    }

    private void bindTimerControls() {
        perMoveEnableCheck.setOnAction(event -> {
            if (perMoveEnableCheck.isSelected()) {
                totalEnableCheck.setSelected(false);
            }
            updateTimerSectionState();
        });

        totalEnableCheck.setOnAction(event -> {
            if (totalEnableCheck.isSelected()) {
                perMoveEnableCheck.setSelected(false);
            }
            updateTimerSectionState();
        });

        timerBox.setOnAction(event -> updateTimerSectionState());
        totalTimerBox.setOnAction(event -> updateTimerSectionState());
    }

    private void updateTimerSectionState() {
        boolean perMoveEnabled = perMoveEnableCheck.isSelected();
        boolean totalEnabled = totalEnableCheck.isSelected();

        timerLabel.setDisable(!perMoveEnabled);
        timerBox.setDisable(!perMoveEnabled);
        perMoveCustomRow.setDisable(!perMoveEnabled);

        totalTimerLabel.setDisable(!totalEnabled);
        totalTimerBox.setDisable(!totalEnabled);
        totalCustomRow.setDisable(!totalEnabled);

        boolean showPerMoveCustom = perMoveEnabled && timerBox.getValue() == TimerOption.CUSTOM;
        boolean showTotalCustom = totalEnabled && totalTimerBox.getValue() == TotalTimerOption.CUSTOM;

        perMoveCustomRow.setVisible(showPerMoveCustom);
        perMoveCustomRow.setManaged(showPerMoveCustom);
        totalCustomRow.setVisible(showTotalCustom);
        totalCustomRow.setManaged(showTotalCustom);

        if (showPerMoveCustom) {
            ensureDefaultTime(perMoveMinutesField, perMoveSecondsField, "01", "00");
        }
        if (showTotalCustom) {
            ensureDefaultTime(totalMinutesField, totalSecondsField, "10", "00");
        }
    }

    private void applyTimerModeState(TimerMode timerMode) {
        if (timerMode == TimerMode.TOTAL) {
            totalEnableCheck.setSelected(true);
            perMoveEnableCheck.setSelected(false);
            return;
        }
        perMoveEnableCheck.setSelected(true);
        totalEnableCheck.setSelected(false);
    }

    private void ensureDefaultTime(TextField minutesField, TextField secondsField,
                                   String minutes, String seconds) {
        if (minutesField.getText().isBlank()) {
            minutesField.setText(minutes);
        }
        if (secondsField.getText().isBlank()) {
            secondsField.setText(seconds);
        }
    }

    private int parseTimeField(TextField field) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            if (value < 0) {
                return 0;
            }
            return Math.min(99, value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void loadCustomTime(TextField minutesField, TextField secondsField, int minutes, int seconds) {
        if (minutes > 0 || seconds > 0) {
            minutesField.setText(String.format("%02d", Math.min(99, minutes)));
            secondsField.setText(String.format("%02d", Math.min(99, seconds)));
        } else {
            minutesField.setText("");
            secondsField.setText("");
        }
    }
    public void setTexts(String title, String hint, String boardSize, String forbidden, String perMoveTimer,
                         String totalTimer, String minutesLabel, String secondsLabel,
                         String appearance, String language, String showCoordinates, String showMarker,
                         String apply, String close, LanguageMode languageMode) {
        this.displayLanguageMode = languageMode;

        titleLabel.setText(title);
        hintLabel.setText(hint);
        boardSizeLabel.setText(boardSize);
        forbiddenLabel.setText(forbidden);
        perMoveEnableCheck.setText(perMoveTimer);
        timerLabel.setText(perMoveTimer);
        totalEnableCheck.setText(totalTimer);
        totalTimerLabel.setText(totalTimer);
        perMoveMinutesLabel.setText(minutesLabel);
        perMoveSecondsLabel.setText(secondsLabel);
        totalMinutesLabel.setText(minutesLabel);
        totalSecondsLabel.setText(secondsLabel);
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
                -fx-font-size: 20px;
                -fx-font-weight: 600;
                """.formatted(primaryText));

        hintLabel.setStyle("""
                -fx-text-fill: %s;
                -fx-font-size: 13px;
                """.formatted(secondaryText));

        String labelStyle = """
                -fx-text-fill: %s;
                -fx-font-size: 14px;
                -fx-font-weight: 600;
                """.formatted(primaryText);

        boardSizeLabel.setStyle(labelStyle);
        forbiddenLabel.setStyle(labelStyle);
        timerLabel.setStyle(labelStyle);
        totalTimerLabel.setStyle(labelStyle);
        appearanceLabel.setStyle(labelStyle);
        languageLabel.setStyle(labelStyle);

        coordinatesCheck.setStyle("-fx-text-fill: %s; -fx-font-size: 13px;".formatted(primaryText));
        lastMoveMarkerCheck.setStyle("-fx-text-fill: %s; -fx-font-size: 13px;".formatted(primaryText));
        forbiddenCheck.setStyle("-fx-text-fill: %s; -fx-font-size: 13px;".formatted(primaryText));
        perMoveEnableCheck.setStyle("-fx-text-fill: %s; -fx-font-size: 13px;".formatted(primaryText));
        totalEnableCheck.setStyle("-fx-text-fill: %s; -fx-font-size: 13px;".formatted(primaryText));
        perMoveMinutesLabel.setStyle("-fx-text-fill: %s; -fx-font-size: 12px;".formatted(primaryText));
        perMoveSecondsLabel.setStyle("-fx-text-fill: %s; -fx-font-size: 12px;".formatted(primaryText));
        totalMinutesLabel.setStyle("-fx-text-fill: %s; -fx-font-size: 12px;".formatted(primaryText));
        totalSecondsLabel.setStyle("-fx-text-fill: %s; -fx-font-size: 12px;".formatted(primaryText));

        String comboStyle = """
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                -fx-font-size: 14px;
                """.formatted(buttonBg, buttonBorder);

        boardSizeBox.setStyle(comboStyle);
        timerBox.setStyle(comboStyle);
        totalTimerBox.setStyle(comboStyle);
        themeBox.setStyle(comboStyle);
        languageBox.setStyle(comboStyle);

        applyButton.setStyle("""
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                -fx-text-fill: %s;
                -fx-font-weight: 600;
                -fx-font-size: 14px;
                -fx-pref-height: 40;
                """.formatted(buttonBg, buttonBorder, primaryText));

        closeButton.setStyle("""
                -fx-background-color: transparent;
                -fx-border-color: %s;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                -fx-text-fill: %s;
                -fx-font-weight: 500;
                -fx-font-size: 14px;
                -fx-pref-height: 40;
                """.formatted(buttonBorder, primaryText));

        forbiddenInfoButton.setStyle("""
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-radius: 9;
                -fx-background-radius: 9;
                -fx-text-fill: %s;
                -fx-font-size: 10px;
                -fx-font-weight: 700;
                -fx-padding: 0;
                """.formatted(buttonBg, buttonBorder, primaryText));

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
        applyRenderer(totalTimerBox, value -> value == null ? "" : value.displayLabel(displayLanguageMode));
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
        timerBox.setValue(pendingRuleConfig.perMoveTimerOption());
        totalTimerBox.setValue(pendingRuleConfig.totalTimerOption());
        themeBox.setValue(themeMode);
        languageBox.setValue(languageMode);
        coordinatesCheck.setSelected(showCoordinates);
        lastMoveMarkerCheck.setSelected(showLastMoveMarker);

        applyTimerModeState(pendingRuleConfig.timerMode());
        if (pendingRuleConfig.timerMode() == TimerMode.PER_MOVE
                && pendingRuleConfig.perMoveTimerOption() == TimerOption.OFF) {
            perMoveEnableCheck.setSelected(false);
        }
        if (pendingRuleConfig.timerMode() == TimerMode.TOTAL
                && pendingRuleConfig.totalTimerOption() == TotalTimerOption.OFF) {
            totalEnableCheck.setSelected(false);
        }
        loadCustomTime(perMoveMinutesField, perMoveSecondsField,
                pendingRuleConfig.perMoveCustomMinutes(), pendingRuleConfig.perMoveCustomSeconds());
        loadCustomTime(totalMinutesField, totalSecondsField,
                pendingRuleConfig.totalCustomMinutes(), pendingRuleConfig.totalCustomSeconds());
        updateTimerSectionState();
    }

    public void configureCapabilities(boolean supportsBoardSize, boolean supportsForbiddenMoves) {
        boardSizeSection.setManaged(supportsBoardSize);
        boardSizeSection.setVisible(supportsBoardSize);
        forbiddenSection.setManaged(supportsForbiddenMoves);
        forbiddenSection.setVisible(supportsForbiddenMoves);
    }

    public RuleConfig buildPendingRuleConfig() {
        int perMoveMinutes = parseTimeField(perMoveMinutesField);
        int perMoveSeconds = parseTimeField(perMoveSecondsField);
        int totalMinutes = parseTimeField(totalMinutesField);
        int totalSeconds = parseTimeField(totalSecondsField);

        TimerMode timerMode = TimerMode.PER_MOVE;
        if (totalEnableCheck.isSelected()) {
            timerMode = TimerMode.TOTAL;
        } else if (!perMoveEnableCheck.isSelected()) {
            timerMode = TimerMode.PER_MOVE;
            timerBox.setValue(TimerOption.OFF);
            totalTimerBox.setValue(TotalTimerOption.OFF);
            perMoveMinutes = 0;
            perMoveSeconds = 0;
            totalMinutes = 0;
            totalSeconds = 0;
        }

        return new RuleConfig(
            boardSizeBox.getValue() == null ? BoardSizeOption.SIZE_15 : boardSizeBox.getValue(),
            forbiddenCheck.isSelected(),
            timerBox.getValue() == null ? TimerOption.OFF : timerBox.getValue(),
            perMoveMinutes,
            perMoveSeconds,
            totalTimerBox.getValue() == null ? TotalTimerOption.OFF : totalTimerBox.getValue(),
            totalMinutes,
            totalSeconds,
            timerMode
        );
    }

    public TimerMode selectedTimerMode() {
        if (totalEnableCheck.isSelected()) {
            return TimerMode.TOTAL;
        }
        if (perMoveEnableCheck.isSelected()) {
            return TimerMode.PER_MOVE;
        }
        return TimerMode.PER_MOVE;
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

    public boolean isPerMoveTimerEnabled() {
        return perMoveEnableCheck.isSelected();
    }

    public boolean isTotalTimerEnabled() {
        return totalEnableCheck.isSelected();
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

    public Button getForbiddenInfoButton() {
        return forbiddenInfoButton;
    }
}
