package com.example.koku.ui;

import com.example.koku.config.AppSettings;
import com.example.koku.config.LanguageMode;
import com.example.koku.config.RuleConfig;
import com.example.koku.service.GameSession;
import com.example.koku.service.I18nService;
import com.example.koku.service.SettingsService;
import com.example.koku.service.ThemeService;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MainView extends BorderPane {
    private final SettingsService settingsService;
    private final ThemeService themeService;
    private final I18nService i18nService;

    private GameSession session;

    private final BorderPane mainShell;
    private final TopBarView topBarView;
    private final BottomStatusView bottomStatusView;
    private final BoardCanvas boardCanvas;
    private final SettingsPanel settingsPanel;

    private final StackPane panelHost;
    private Timeline panelAnimation;
    private boolean panelVisible;

    private final Timeline uiTicker;
    private boolean gameOverDialogShown;

    public MainView() {
        this.settingsService = new SettingsService();
        this.themeService = new ThemeService();
        this.i18nService = new I18nService(settingsService.getSettings().getLanguageMode());

        AppSettings settings = settingsService.getSettings();
        this.session = new GameSession(settings.getCurrentRuleConfig());

        this.topBarView = new TopBarView();
        this.bottomStatusView = new BottomStatusView();
        this.boardCanvas = new BoardCanvas(session);
        this.settingsPanel = new SettingsPanel();

        this.mainShell = new BorderPane();
        this.panelHost = new StackPane(settingsPanel);
        this.panelVisible = false;
        this.gameOverDialogShown = false;

        buildLayout();
        bindActions();
        refreshAll();

        this.uiTicker = new Timeline(new KeyFrame(Duration.millis(120), event -> onUiTick()));
        this.uiTicker.setCycleCount(Timeline.INDEFINITE);
        this.uiTicker.play();
    }

    private void buildLayout() {
        VBox centerColumn = new VBox();
        centerColumn.setAlignment(Pos.CENTER);
        centerColumn.setPadding(new Insets(18, 26, 18, 26));

        StackPane boardWrap = new StackPane(boardCanvas);
        boardWrap.setAlignment(Pos.CENTER);
        boardWrap.setPadding(new Insets(24));
        centerColumn.getChildren().add(boardWrap);

        mainShell.setTop(topBarView);
        mainShell.setCenter(centerColumn);
        mainShell.setBottom(bottomStatusView);

        panelHost.setPrefWidth(0);
        panelHost.setMinWidth(0);
        panelHost.setMaxWidth(360);
        panelHost.setOpacity(0);
        panelHost.setMouseTransparent(true);

        settingsPanel.setPrefWidth(336);
        settingsPanel.setMaxWidth(336);
        settingsPanel.setMinWidth(336);

        setCenter(mainShell);
        setRight(panelHost);
    }

    private void bindActions() {
        topBarView.getNewMatchButton().setOnAction(event -> {
            if (settingsService.getSettings().hasPendingRuleChanges()) {
                settingsService.applyPendingRules();
                session.applyRuleConfigAndNewMatch(settingsService.getSettings().getCurrentRuleConfig());
            } else {
                session.newMatch();
            }
            gameOverDialogShown = false;
            refreshAll();
        });

        topBarView.getUndoButton().setOnAction(event -> {
            session.undo();
            gameOverDialogShown = false;
            refreshAll();
        });

        topBarView.getSettingsButton().setOnAction(event -> {
            if (panelVisible) {
                hideSettingsPanel();
            } else {
                loadPanelState();
                showSettingsPanel();
            }
        });

        settingsPanel.getCloseButton().setOnAction(event -> hideSettingsPanel());

        settingsPanel.getApplyButton().setOnAction(event -> {
            settingsService.updatePendingRuleConfig(settingsPanel.buildPendingRuleConfig());
            settingsService.setThemeMode(settingsPanel.selectedThemeMode());
            settingsService.setLanguageMode(settingsPanel.selectedLanguageMode());
            settingsService.setShowCoordinates(settingsPanel.isShowCoordinatesSelected());
            settingsService.setShowLastMoveMarker(settingsPanel.isShowLastMoveMarkerSelected());

            i18nService.setLanguageMode(settingsService.getSettings().getLanguageMode());
            settingsService.applyPendingRules();

            RuleConfig applied = settingsService.getSettings().getCurrentRuleConfig();
            session.applyRuleConfigAndNewMatch(applied);

            gameOverDialogShown = false;
            hideSettingsPanel();
            refreshAll();
        });

        settingsPanel.getThemeBox().setOnAction(event -> {
            if (settingsPanel.getThemeBox().getValue() != null) {
                settingsService.setThemeMode(settingsPanel.getThemeBox().getValue());
                refreshAppearanceAndTexts();
            }
        });

        settingsPanel.getLanguageBox().setOnAction(event -> {
            if (settingsPanel.getLanguageBox().getValue() != null) {
                settingsService.setLanguageMode(settingsPanel.getLanguageBox().getValue());
                i18nService.setLanguageMode(settingsPanel.getLanguageBox().getValue());
                refreshAppearanceAndTexts();
            }
        });

        settingsPanel.getCoordinatesCheck().setOnAction(event -> {
            settingsService.setShowCoordinates(settingsPanel.isShowCoordinatesSelected());
            refreshBoardAndTexts();
        });

        settingsPanel.getLastMoveMarkerCheck().setOnAction(event -> {
            settingsService.setShowLastMoveMarker(settingsPanel.isShowLastMoveMarkerSelected());
            refreshBoardAndTexts();
        });

        boardCanvas.setOnBoardChanged(() -> {
            refreshBoardAndTexts();
            maybeShowResultDialog();
        });
    }

    private void onUiTick() {
        boolean timedOut = session.checkTimeout();
        refreshTextsOnly();

        if (timedOut) {
            boardCanvas.draw();
            maybeShowResultDialog();
        }
    }

    private void showSettingsPanel() {
        panelVisible = true;
        animatePanel(336, 1.0, false);
    }

    private void hideSettingsPanel() {
        panelVisible = false;
        animatePanel(0, 0.0, true);
    }

    private void animatePanel(double targetWidth, double targetOpacity, boolean mouseTransparentOnFinish) {
        if (panelAnimation != null) {
            panelAnimation.stop();
        }

        panelHost.setMouseTransparent(false);

        panelAnimation = new Timeline(
                new KeyFrame(Duration.millis(220),
                        new KeyValue(panelHost.prefWidthProperty(), targetWidth),
                        new KeyValue(panelHost.opacityProperty(), targetOpacity))
        );

        panelAnimation.setOnFinished(event -> {
            panelHost.setMouseTransparent(mouseTransparentOnFinish);
        });

        panelAnimation.play();
    }

    private void loadPanelState() {
        AppSettings settings = settingsService.getSettings();
        settingsPanel.loadFromState(
                settings.getPendingRuleConfig(),
                settings.getThemeMode(),
                settings.getLanguageMode(),
                settings.isShowCoordinates(),
                settings.isShowLastMoveMarker()
        );
    }

    private void refreshAll() {
        refreshAppearanceAndTexts();
        boardCanvas.draw();
    }

    private void refreshAppearanceAndTexts() {
        AppSettings settings = settingsService.getSettings();
        ThemeService.Palette palette = themeService.getPalette(settings.getThemeMode() == com.example.koku.config.ThemeMode.DARK);

        applyShellTheme(palette);
        applyTexts();
        boardCanvas.configure(
                palette,
                settings.isShowCoordinates(),
                settings.isShowLastMoveMarker()
        );
        applyBoardWrapTheme(palette);
    }

    private void refreshBoardAndTexts() {
        AppSettings settings = settingsService.getSettings();
        ThemeService.Palette palette = themeService.getPalette(settings.getThemeMode() == com.example.koku.config.ThemeMode.DARK);

        applyTexts();
        boardCanvas.configure(
                palette,
                settings.isShowCoordinates(),
                settings.isShowLastMoveMarker()
        );
    }

    private void refreshTextsOnly() {
        applyTexts();
    }

    private void applyShellTheme(ThemeService.Palette palette) {
        setStyle("-fx-background-color: %s;".formatted(palette.windowBg()));
        mainShell.setStyle("-fx-background-color: %s;".formatted(palette.windowBg()));

        topBarView.applyTheme(
                palette.primaryText(),
                palette.secondaryText(),
                palette.buttonBg(),
                palette.buttonBorder(),
                palette.windowBg(),
                palette.cardBorder()
        );

        bottomStatusView.applyTheme(palette.subtleText());

        settingsPanel.applyTheme(
                palette.panelBg(),
                palette.primaryText(),
                palette.secondaryText(),
                palette.buttonBg(),
                palette.buttonBorder(),
                palette.cardBorder()
        );
    }

    private void applyBoardWrapTheme(ThemeService.Palette palette) {
        Region centerNode = (Region) mainShell.getCenter();
        if (centerNode instanceof VBox centerColumn && !centerColumn.getChildren().isEmpty()) {
            StackPane boardWrap = (StackPane) centerColumn.getChildren().get(0);
            boardWrap.setStyle("""
                    -fx-background-color: %s;
                    -fx-background-radius: 28;
                    -fx-border-color: %s;
                    -fx-border-radius: 28;
                    -fx-padding: 24;
                    """.formatted(palette.panelBg(), palette.cardBorder()));
        }
    }

    private void applyTexts() {
        AppSettings settings = settingsService.getSettings();

        topBarView.setTexts(
                i18nService.text("app.title"),
                i18nService.text(session.getTopStatusKey()),
                buildTopSubStatus(),
                i18nService.text("button.newMatch"),
                i18nService.text("button.undo"),
                i18nService.text("button.settings")
        );

        bottomStatusView.setTexts(
                session.boardSizeLabel()
                        + " · "
                        + i18nService.text(settings.getCurrentRuleConfig().forbiddenMovesEnabled()
                        ? "settings.forbidden.on" : "settings.forbidden.off")
                        + " · "
                        + formatTimerLabel(settings.getCurrentRuleConfig().timerOption().seconds()),
                i18nService.text("status.lastMove") + ": " + session.getLastMoveCoordinate()
        );

        settingsPanel.setTexts(
                i18nService.text("button.settings"),
                i18nService.text("hint.nextGame"),
                i18nService.text("settings.boardSize"),
                i18nService.text("settings.forbidden"),
                i18nService.text("settings.timer"),
                i18nService.text("settings.appearance"),
                i18nService.text("settings.language"),
                i18nService.text("settings.showCoordinates"),
                i18nService.text("settings.showLastMoveMarker"),
                i18nService.text("button.applyNewMatch"),
                i18nService.text("button.close"),
                settings.getLanguageMode()
        );
    }

    private String buildTopSubStatus() {
        String timerText = buildLiveTimerText();
        String hint = settingsService.getSettings().hasPendingRuleChanges()
                ? i18nService.text("hint.nextGame")
                : "";

        if (!timerText.isBlank() && !hint.isBlank()) {
            return timerText + " · " + hint;
        }
        if (!timerText.isBlank()) {
            return timerText;
        }
        return hint;
    }

    private String buildLiveTimerText() {
        if (!session.isTimerEnabled() || session.isGameOver()) {
            return "";
        }

        long millis = session.getTurnRemainingMillis();
        double seconds = millis / 1000.0;

        if (i18nService.getLanguageMode() == LanguageMode.ZH_CN) {
            return i18nService.text("status.turnTimer")
                    + "："
                    + String.format("%.1f秒", seconds);
        }

        return i18nService.text("status.turnTimer")
                + ": "
                + String.format("%.1fs", seconds);
    }

    private String formatTimerLabel(int seconds) {
        if (seconds <= 0) {
            return i18nService.text("settings.timer.off");
        }
        return i18nService.getLanguageMode() == LanguageMode.ZH_CN
                ? "棋钟 " + seconds + "秒"
                : "Timer " + seconds + "s";
    }

    private void maybeShowResultDialog() {
        if (!session.isGameOver() || gameOverDialogShown) {
            return;
        }

        gameOverDialogShown = true;

        String detailKey = session.getResultDetailKey();
        String content;
        if (detailKey != null) {
            content = i18nService.text(detailKey);
        } else {
            content = switch (session.getResult().status()) {
                case BLACK_WIN -> i18nService.text("result.blackWins");
                case WHITE_WIN -> i18nService.text("result.whiteWins");
                case DRAW -> i18nService.text("result.draw");
                default -> session.getLatestMessage();
            };
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(i18nService.text("status.gameOver"));
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}