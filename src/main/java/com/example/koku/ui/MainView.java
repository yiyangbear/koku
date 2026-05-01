package com.example.koku.ui;

import com.example.koku.config.AppSettings;
import com.example.koku.config.LanguageMode;
import com.example.koku.config.RuleConfig;
import com.example.koku.domain.GameStatus;
import com.example.koku.domain.Player;
import com.example.koku.game.GameDefinition;
import com.example.koku.game.GameRegistry;
import com.example.koku.service.GameSession;
import com.example.koku.service.I18nService;
import com.example.koku.service.SettingsService;
import com.example.koku.service.ThemeService;
import com.example.koku.ui.boards.ConnectFourBoardView;
import com.example.koku.ui.boards.GameBoardView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    private final Runnable onBack;
    private final GameDefinition gameDefinition;

    private GameSession session;

    private final BorderPane mainShell;
    private final StackPane contentStack;
    private final TopBarView topBarView;
    private final BottomStatusView bottomStatusView;
    private final GameBoardView boardView;
    private final SettingsPanel settingsPanel;
    private final ScrollPane settingsScrollPane;
    private final StackPane resultOverlay;
    private final VBox resultCard;
    private final Label resultTitleLabel;
    private final Label resultMessageLabel;
    private final Button resultReturnButton;
    private final Button resultNewMatchButton;
    private StackPane boardWrap;

    private final StackPane panelHost;
    private Timeline panelAnimation;
    private boolean panelVisible;

    private final Timeline uiTicker;
    private boolean gameOverDialogShown;
    private boolean gameOverDialogScheduled;

    public MainView() {
        this(GameRegistry.gomoku(), null);
    }

    public MainView(GameDefinition gameDefinition) {
        this(gameDefinition, null);
    }

    public MainView(GameDefinition gameDefinition, Runnable onBack) {
        this.gameDefinition = gameDefinition;
        this.settingsService = new SettingsService();
        this.themeService = new ThemeService();
        this.i18nService = new I18nService(settingsService.getSettings().getLanguageMode());
        this.onBack = onBack;

        AppSettings settings = settingsService.getSettings();
        this.fontFamily = loadFontFamily();
        this.session = new GameSession(settings.getCurrentRuleConfig(), gameDefinition.engineFactory());

        this.topBarView = new TopBarView();
        this.bottomStatusView = new BottomStatusView();
        this.boardView = gameDefinition.boardViewFactory().create(session);
        this.settingsPanel = new SettingsPanel();
        this.settingsScrollPane = new ScrollPane(settingsPanel);
        this.settingsPanel.configureCapabilities(
                gameDefinition.supportsBoardSize(),
                gameDefinition.supportsForbiddenMoves(),
                supportsCoordinates()
        );

        this.mainShell = new BorderPane();
        this.contentStack = new StackPane();
        this.resultTitleLabel = new Label();
        this.resultMessageLabel = new Label();
        this.resultReturnButton = new Button();
        this.resultNewMatchButton = new Button();
        this.resultCard = new VBox();
        this.resultOverlay = new StackPane(resultCard);
        this.panelHost = new StackPane(settingsScrollPane);
        this.panelVisible = false;
        this.gameOverDialogShown = false;
        this.gameOverDialogScheduled = false;

        buildLayout();
        bindActions();
        refreshAll();

        this.uiTicker = new Timeline(new KeyFrame(Duration.millis(120), event -> onUiTick()));
        this.uiTicker.setCycleCount(Timeline.INDEFINITE);
        this.uiTicker.play();

        bindResponsiveLayout();
    }

    private void buildLayout() {
        VBox centerColumn = new VBox();
        centerColumn.setAlignment(Pos.CENTER);
        centerColumn.setPadding(new Insets(18, 26, 18, 26));
        centerColumn.setFillWidth(true);

        boardWrap = new StackPane(boardView.getNode());
        boardWrap.setAlignment(Pos.CENTER);
        boardWrap.setPadding(new Insets(24));
        boardWrap.setMinSize(320, 320);
        boardWrap.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(boardWrap, Priority.ALWAYS);
        centerColumn.getChildren().add(boardWrap);

        mainShell.setTop(topBarView);
        mainShell.setCenter(centerColumn);
        mainShell.setBottom(bottomStatusView);

        panelHost.setPrefWidth(0);
        panelHost.setMinWidth(0);
        panelHost.setMaxWidth(360);
        panelHost.setOpacity(0);
        panelHost.setMouseTransparent(true);

        settingsScrollPane.setFitToWidth(true);
        settingsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        settingsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        settingsScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        settingsPanel.setResponsiveWidth(360);

        buildResultOverlay();

        contentStack.getChildren().addAll(mainShell, resultOverlay);
        setCenter(contentStack);
        setRight(panelHost);
    }

    private void buildResultOverlay() {
        resultTitleLabel.setText(i18nService.text("status.gameOver"));
        resultMessageLabel.setText("");

        HBox actions = new HBox(10, resultReturnButton, resultNewMatchButton);
        actions.setAlignment(Pos.CENTER);

        resultCard.getChildren().setAll(resultTitleLabel, resultMessageLabel, actions);
        resultCard.setAlignment(Pos.CENTER);
        resultCard.setPadding(new Insets(20, 28, 20, 28));
        resultCard.setSpacing(14);
        resultCard.setMinSize(360, 188);
        resultCard.setPrefSize(360, 188);
        resultCard.setMaxSize(360, 188);

        resultOverlay.setAlignment(Pos.CENTER);
        resultOverlay.setVisible(false);
        resultOverlay.setMouseTransparent(true);
        resultOverlay.setPickOnBounds(false);
    }

    private void bindResponsiveLayout() {
        widthProperty().addListener((obs, oldVal, newVal) -> updateResponsiveLayout());
        heightProperty().addListener((obs, oldVal, newVal) -> updateResponsiveLayout());
        if (boardWrap != null) {
            boardWrap.widthProperty().addListener((obs, oldVal, newVal) -> resizeBoardCanvas());
            boardWrap.heightProperty().addListener((obs, oldVal, newVal) -> resizeBoardCanvas());
        }
        Platform.runLater(() -> {
            updateResponsiveLayout();
            resizeBoardCanvas();
        });
    }

    private void updateResponsiveLayout() {
        double width = getWidth() <= 0 ? 1440 : getWidth();
        double panelWidth = 360;

        settingsPanel.setResponsiveWidth(panelWidth);
        panelHost.setMinWidth(panelVisible ? panelWidth : 0);
        panelHost.setMaxWidth(panelWidth);
        settingsScrollPane.setPrefWidth(panelWidth);
        settingsScrollPane.setMinWidth(panelWidth);
        settingsScrollPane.setMaxWidth(panelWidth);
        settingsScrollPane.setMaxHeight(Math.max(360, getHeight() - 24));

        topBarView.setCompact(width < 1200, width < 980);

        if (panelVisible) {
            panelHost.setPrefWidth(panelWidth);
            panelHost.setOpacity(1.0);
            panelHost.setMouseTransparent(false);
        }
        resizeBoardCanvas();
    }

    private double clamp(double min, double value, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void resizeBoardCanvas() {
        if (boardWrap == null || boardView == null) {
            return;
        }
        double availableWidth = boardWrap.getWidth() - boardWrap.getInsets().getLeft() - boardWrap.getInsets().getRight();
        double availableHeight = boardWrap.getHeight() - boardWrap.getInsets().getTop() - boardWrap.getInsets().getBottom();
        double size = Math.floor(Math.max(280, Math.min(availableWidth, availableHeight)));
        if (!Double.isFinite(size) || size <= 0) {
            return;
        }
        if (boardView.getNode() instanceof javafx.scene.canvas.Canvas canvas) {
            canvas.setWidth(size);
            canvas.setHeight(size);
            boardView.draw();
        }
    }

    private void bindActions() {
        topBarView.getBackButton().setOnAction(event -> {
            if (onBack != null) {
                onBack.run();
            }
        });

        topBarView.getNewMatchButton().setOnAction(event -> {
            startNewMatch();
        });

        topBarView.getUndoButton().setOnAction(event -> {
            session.undo();
            gameOverDialogShown = false;
            gameOverDialogScheduled = false;
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
        resultReturnButton.setOnAction(event -> hideResultOverlay());
        resultNewMatchButton.setOnAction(event -> startNewMatch());

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
            gameOverDialogScheduled = false;
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

        boardView.setOnBoardChanged(() -> {
            refreshBoardAndTexts();
            maybeShowResultDialog();
        });

        if (boardView instanceof ConnectFourBoardView connectFourBoardView) {
            connectFourBoardView.setOnMoveSettled(() -> {
                refreshBoardAndTexts();
                maybeShowResultDialog();
            });
        }
    }

    private void onUiTick() {
        boolean timedOut = session.checkTimeout();
        refreshTextsOnly();

        if (session.isGameOver()) {
            maybeShowResultDialog();
        }

        if (timedOut) {
            boardView.draw();
            maybeShowResultDialog();
        }
    }

    private void showSettingsPanel() {
        panelVisible = true;
        panelHost.setMinWidth(currentPanelWidth());
        animatePanel(currentPanelWidth(), 1.0, false);
    }

    private void hideSettingsPanel() {
        panelVisible = false;
        animatePanel(0, 0.0, true);
    }

    public void requestNewMatch() {
        startNewMatch();
    }

    public void requestUndo() {
        session.undo();
        gameOverDialogShown = false;
        gameOverDialogScheduled = false;
        refreshAll();
    }

    public void requestBackToSelect() {
        if (onBack != null) {
            onBack.run();
        }
    }

    public void requestOpenSettingsPanel() {
        loadPanelState();
        showSettingsPanel();
    }

    public void setShowCoordinatesFromMenu(boolean showCoordinates) {
        if (!supportsCoordinates()) {
            return;
        }
        settingsService.setShowCoordinates(showCoordinates);
        refreshBoardAndTexts();
    }

    public void setShowLastMoveMarkerFromMenu(boolean showLastMoveMarker) {
        settingsService.setShowLastMoveMarker(showLastMoveMarker);
        refreshBoardAndTexts();
    }

    public boolean isShowCoordinatesSelected() {
        return supportsCoordinates() && settingsService.getSettings().isShowCoordinates();
    }

    public boolean isShowLastMoveMarkerSelected() {
        return settingsService.getSettings().isShowLastMoveMarker();
    }

    public boolean supportsCoordinateMenu() {
        return supportsCoordinates();
    }

    public void toggleThemeModeFromMenu() {
        var next = settingsService.getSettings().getThemeMode() == com.example.koku.config.ThemeMode.DARK
                ? com.example.koku.config.ThemeMode.LIGHT
                : com.example.koku.config.ThemeMode.DARK;
        settingsService.setThemeMode(next);
        refreshAppearanceAndTexts();
    }

    public void setLanguageModeFromMenu(LanguageMode languageMode) {
        settingsService.setLanguageMode(languageMode);
        i18nService.setLanguageMode(languageMode);
        refreshAppearanceAndTexts();
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
            if (!panelVisible) {
                panelHost.setMinWidth(0);
            }
        });

        panelAnimation.play();
    }

    private double currentPanelWidth() {
        return 360;
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
        boardView.draw();
    }

    private void refreshAppearanceAndTexts() {
        AppSettings settings = settingsService.getSettings();
        ThemeService.Palette palette = themeService.getPalette(settings.getThemeMode() == com.example.koku.config.ThemeMode.DARK);

        applyShellTheme(palette);
        applyTexts();
        boardView.configure(
                palette,
                supportsCoordinates() && settings.isShowCoordinates(),
                settings.isShowLastMoveMarker(),
                fontFamily
        );
        applyBoardWrapTheme(palette);
    }

    private void refreshBoardAndTexts() {
        AppSettings settings = settingsService.getSettings();
        ThemeService.Palette palette = themeService.getPalette(settings.getThemeMode() == com.example.koku.config.ThemeMode.DARK);

        applyTexts();
        boardView.configure(
                palette,
                supportsCoordinates() && settings.isShowCoordinates(),
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
        applyResultOverlayTheme(palette);

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
                i18nService.text("button.back"),
                i18nService.text("button.newMatch"),
                i18nService.text("button.undo"),
                i18nService.text("button.settings")
        );
        topBarView.applyStatusEmphasis(
                shouldEmphasizeBlack(),
                shouldEmphasizeWhite()
        );

        bottomStatusView.setTexts(
                buildBottomSummary(settings),
                i18nService.text("app.title"),
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

        resultTitleLabel.setText(i18nService.text("status.gameOver"));
        resultReturnButton.setText(i18nService.text("result.returnToBoard"));
        resultNewMatchButton.setText(i18nService.text("result.startNewMatch"));
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
            return "";
        }
        long millis = session.getPlayerRemainingMillis(player);
        int totalSeconds = (int) Math.ceil(millis / 1000.0);
        int minutes = Math.max(0, totalSeconds) / 60;
        int seconds = Math.max(0, totalSeconds) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private String buildBottomSummary(AppSettings settings) {
        return switch (gameDefinition.id()) {
            case "ticTacToe" -> session.boardSizeLabel()
                    + " · "
                    + i18nService.text("summary.threeInRow");
            case "connectFour" -> session.boardSizeLabel()
                    + " · "
                    + i18nService.text("summary.dropStyle")
                    + " · "
                    + i18nService.text("summary.fourInRow");
            case "sixInRow" -> session.boardSizeLabel()
                    + " · "
                    + i18nService.text("summary.sixInRow")
                    + " · "
                    + i18nService.text("summary.twoStonesPerTurn");
            default -> session.boardSizeLabel()
                    + " · "
                    + i18nService.text("summary.fiveInRow")
                    + " · "
                    + i18nService.text(settings.getCurrentRuleConfig().forbiddenMovesEnabled()
                    ? "settings.forbidden.on" : "settings.forbidden.off");
        };
    }

    private boolean supportsCoordinates() {
        return !"ticTacToe".equals(gameDefinition.id());
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

    private void startNewMatch() {
        if (settingsService.getSettings().hasPendingRuleChanges()) {
            settingsService.applyPendingRules();
            session.applyRuleConfigAndNewMatch(settingsService.getSettings().getCurrentRuleConfig());
        } else {
            session.newMatch();
        }
        gameOverDialogShown = false;
        gameOverDialogScheduled = false;
        hideResultOverlay();
        refreshAll();
    }

    private void applyResultOverlayTheme(ThemeService.Palette palette) {
        resultCard.setStyle("""
                -fx-background-color: %sE8;
                -fx-background-radius: 22;
                -fx-border-color: %s;
                -fx-border-radius: 22;
                -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.34), 28, 0.22, 0, 14);
                """.formatted(palette.panelBg(), palette.cardBorder()));

        resultTitleLabel.setStyle("""
                -fx-text-fill: %s;
                -fx-font-size: 15px;
                -fx-font-weight: 500;
                """.formatted(palette.secondaryText()));

        resultMessageLabel.setStyle("""
                -fx-text-fill: %s;
                -fx-font-size: 28px;
                -fx-font-weight: 700;
                """.formatted(palette.primaryText()));

        String primaryButtonStyle = """
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                -fx-text-fill: %s;
                -fx-font-size: 14px;
                -fx-font-weight: 600;
                -fx-pref-height: 38;
                -fx-padding: 0 18 0 18;
                """.formatted(palette.buttonBg(), palette.buttonBorder(), palette.primaryText());
        resultReturnButton.setStyle(primaryButtonStyle);
        resultNewMatchButton.setStyle(primaryButtonStyle);
    }

    private void maybeShowResultDialog() {
        if (!session.isGameOver() || gameOverDialogShown || gameOverDialogScheduled) {
            return;
        }

        gameOverDialogScheduled = true;
        Platform.runLater(() -> {
            gameOverDialogScheduled = false;
            if (!session.isGameOver() || gameOverDialogShown) {
                return;
            }

            gameOverDialogShown = true;
            showResultOverlay();
        });
    }

    private void showResultOverlay() {
        String message = switch (session.getResult().status()) {
            case BLACK_WIN -> i18nService.text("result.blackVictory");
            case WHITE_WIN -> i18nService.text("result.whiteVictory");
            case DRAW -> i18nService.text("result.drawFinal");
            default -> i18nService.text("status.gameOver");
        };

        resultTitleLabel.setText(i18nService.text("status.gameOver"));
        resultMessageLabel.setText(message);
        resultOverlay.setVisible(true);
        resultOverlay.setMouseTransparent(false);
    }

    private void hideResultOverlay() {
        resultOverlay.setVisible(false);
        resultOverlay.setMouseTransparent(true);
    }

    private void showForbiddenRulesDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(i18nService.text("settings.forbidden"));
        alert.setHeaderText(i18nService.text("forbidden.rules.title"));
        alert.setContentText(i18nService.text("forbidden.rules.body"));
        alert.showAndWait();
    }
}
