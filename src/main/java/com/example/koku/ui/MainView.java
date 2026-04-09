package com.example.koku.ui;

import com.example.koku.config.AppSettings;
import com.example.koku.config.LanguageMode;
import com.example.koku.config.RuleConfig;
import com.example.koku.domain.GameStatus;
import com.example.koku.domain.Player;
import com.example.koku.service.GameSession;
import com.example.koku.service.I18nService;
import com.example.koku.service.SettingsService;
import com.example.koku.service.ThemeService;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class MainView extends BorderPane {
    private final SettingsService settingsService;
    private final ThemeService themeService;
    private final I18nService i18nService;
    private final String fontFamily;

    private GameSession session;

    private final BorderPane mainShell;
    private final TopBarView topBarView;
    private final BottomStatusView bottomStatusView;
    private final BoardCanvas boardCanvas;
    private final SettingsPanel settingsPanel;
    private StackPane boardWrap;

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
        this.fontFamily = loadFontFamily();
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

        bindAlignment();
    }

    private void buildLayout() {
        VBox centerColumn = new VBox();
        centerColumn.setAlignment(Pos.CENTER);
        centerColumn.setPadding(new Insets(18, 26, 18, 26));

        boardWrap = new StackPane(boardCanvas);
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

    private void bindAlignment() {
        widthProperty().addListener((obs, oldVal, newVal) -> scheduleTopBarAlignment());
        heightProperty().addListener((obs, oldVal, newVal) -> scheduleTopBarAlignment());
        panelHost.widthProperty().addListener((obs, oldVal, newVal) -> scheduleTopBarAlignment());
        if (boardWrap != null) {
            boardWrap.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> scheduleTopBarAlignment());
        }
        topBarView.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> scheduleTopBarAlignment());
        sceneProperty().addListener((obs, oldVal, newVal) -> scheduleTopBarAlignment());
        scheduleTopBarAlignment();
    }

    private void scheduleTopBarAlignment() {
        Platform.runLater(this::updateTopBarAlignment);
    }

    private void updateTopBarAlignment() {
        if (boardWrap == null || boardWrap.getScene() == null || topBarView.getScene() == null) {
            return;
        }
        var boardBounds = boardWrap.localToScene(boardWrap.getBoundsInLocal());
        double boardCenterX = boardBounds.getMinX() + boardBounds.getWidth() / 2.0;
        double dividerCenterX = topBarView.getDividerCenterXInScene();
        if (Double.isNaN(dividerCenterX)) {
            return;
        }
        topBarView.setStatusOffset(boardCenterX - dividerCenterX);
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
        settingsPanel.getForbiddenInfoButton().setOnAction(event -> showForbiddenRulesDialog());

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
                settings.isShowLastMoveMarker(),
                fontFamily
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
                settings.isShowLastMoveMarker(),
                fontFamily
        );
    }

    private void refreshTextsOnly() {
        applyTexts();
    }

    private void applyShellTheme(ThemeService.Palette palette) {
        setStyle("-fx-background-color: %s; -fx-font-family: \"%s\";".formatted(palette.windowBg(), fontFamily));
        mainShell.setStyle("-fx-background-color: %s; -fx-font-family: \"%s\";".formatted(palette.windowBg(), fontFamily));

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

    private String loadFontFamily() {
        Font font = Font.loadFont(MainView.class.getResourceAsStream("/fonts/SmileySans-Oblique.ttf"), 12);
        if (font == null) {
            return Font.getDefault().getFamily();
        }
        return font.getFamily();
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
                buildBlackStatusText(),
                buildWhiteStatusText(),
                buildTimerText(Player.BLACK),
                buildTimerText(Player.WHITE),
                i18nService.text("button.newMatch"),
                i18nService.text("button.undo"),
                i18nService.text("button.settings")
        );
        topBarView.applyStatusEmphasis(
                shouldEmphasizeBlack(),
                shouldEmphasizeWhite()
        );

        bottomStatusView.setTexts(
                session.boardSizeLabel()
                        + " · "
                        + i18nService.text(settings.getCurrentRuleConfig().forbiddenMovesEnabled()
                        ? "settings.forbidden.on" : "settings.forbidden.off")
                        + " · "
                        + formatTimerLabel(settings.getCurrentRuleConfig()),
                i18nService.text("status.lastMove") + ": " + session.getLastMoveCoordinate()
        );

        settingsPanel.setTexts(
                i18nService.text("button.settings"),
                i18nService.text("hint.nextGame"),
                i18nService.text("settings.boardSize"),
                i18nService.text("settings.forbidden"),
                i18nService.text("settings.timer.perMove"),
                i18nService.text("settings.timer.total"),
                i18nService.text("settings.timer.minutes"),
                i18nService.text("settings.timer.seconds"),
                i18nService.text("settings.appearance"),
                i18nService.text("settings.language"),
                i18nService.text("settings.showCoordinates"),
                i18nService.text("settings.showLastMoveMarker"),
                i18nService.text("button.applyNewMatch"),
                i18nService.text("button.close"),
                settings.getLanguageMode()
        );
    }

    private String buildBlackStatusText() {
        GameStatus status = session.getResult().status();
        if (status == GameStatus.BLACK_WIN) {
            return i18nService.text("result.blackWins");
        }
        if (status == GameStatus.WHITE_WIN) {
            return "";
        }
        if (status == GameStatus.DRAW) {
            return i18nService.text("result.draw");
        }
        return session.getCurrentPlayer() == Player.BLACK
                ? i18nService.text("status.blackToMove")
                : "";
    }

    private String buildWhiteStatusText() {
        GameStatus status = session.getResult().status();
        if (status == GameStatus.WHITE_WIN) {
            return i18nService.text("result.whiteWins");
        }
        if (status == GameStatus.BLACK_WIN) {
            return "";
        }
        if (status == GameStatus.DRAW) {
            return "";
        }
        return session.getCurrentPlayer() == Player.WHITE
                ? i18nService.text("status.whiteToMove")
                : "";
    }

    private boolean shouldEmphasizeBlack() {
        GameStatus status = session.getResult().status();
        if (status == GameStatus.BLACK_WIN) {
            return true;
        }
        if (status == GameStatus.WHITE_WIN || status == GameStatus.DRAW) {
            return false;
        }
        return session.getCurrentPlayer() == Player.BLACK;
    }

    private boolean shouldEmphasizeWhite() {
        GameStatus status = session.getResult().status();
        if (status == GameStatus.WHITE_WIN) {
            return true;
        }
        if (status == GameStatus.BLACK_WIN || status == GameStatus.DRAW) {
            return false;
        }
        return session.getCurrentPlayer() == Player.WHITE;
    }

    private String buildTimerText(Player player) {
        if (!session.isTimerEnabled() || session.isGameOver()) {
            return "00:00";
        }
        long millis = session.getPlayerRemainingMillis(player);
        int totalSeconds = (int) Math.ceil(millis / 1000.0);
        int minutes = Math.max(0, totalSeconds) / 60;
        int seconds = Math.max(0, totalSeconds) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private String formatTimerLabel(RuleConfig ruleConfig) {
        if (ruleConfig.timerMode() == com.example.koku.config.TimerMode.PER_MOVE) {
            int seconds = resolvePerMoveSeconds(ruleConfig);
            if (seconds <= 0) {
                return i18nService.text("settings.timer.off");
            }
            return i18nService.getLanguageMode() == LanguageMode.ZH_CN
                    ? "每手 " + formatClock(seconds)
                    : "Per Move " + formatClock(seconds);
        }
        int seconds = resolveTotalSeconds(ruleConfig);
        if (seconds <= 0) {
            return i18nService.text("settings.timer.off");
        }
        return i18nService.getLanguageMode() == LanguageMode.ZH_CN
                ? "全局 " + formatClock(seconds)
                : "Total " + formatClock(seconds);
    }

    private int resolvePerMoveSeconds(RuleConfig ruleConfig) {
        if (ruleConfig.perMoveTimerOption() == com.example.koku.config.TimerOption.CUSTOM) {
            return ruleConfig.perMoveCustomMinutes() * 60 + ruleConfig.perMoveCustomSeconds();
        }
        return ruleConfig.perMoveTimerOption().seconds();
    }

    private int resolveTotalSeconds(RuleConfig ruleConfig) {
        if (ruleConfig.totalTimerOption() == com.example.koku.config.TotalTimerOption.CUSTOM) {
            return ruleConfig.totalCustomMinutes() * 60 + ruleConfig.totalCustomSeconds();
        }
        return ruleConfig.totalTimerOption().seconds();
    }

    private String formatClock(int totalSeconds) {
        int minutes = Math.max(0, totalSeconds) / 60;
        int seconds = Math.max(0, totalSeconds) % 60;
        return String.format("%02d:%02d", minutes, seconds);
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

    private void showForbiddenRulesDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(i18nService.text("settings.forbidden"));
        alert.setHeaderText(i18nService.text("forbidden.rules.title"));
        alert.setContentText(i18nService.text("forbidden.rules.body"));
        alert.showAndWait();
    }
}
