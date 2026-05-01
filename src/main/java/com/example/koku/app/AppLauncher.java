package com.example.koku.app;

import com.example.koku.config.LanguageMode;
import com.example.koku.game.GameDefinition;
import com.example.koku.ui.GameSelectView;
import com.example.koku.ui.MainView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AppLauncher extends Application {
    private BorderPane appRoot;
    private AppMenu appMenu;
    private MainView currentMainView;

    @Override
    public void start(Stage stage) {
        this.appRoot = new BorderPane();
        this.appMenu = new AppMenu(stage, buildMenuActions());

        MenuBar menuBar = appMenu.build();
        appRoot.setTop(menuBar);
        appRoot.setCenter(new GameSelectView(this::showGame));

        Scene scene = new Scene(appRoot, 1440, 900);
        stage.setScene(scene);

        stage.setTitle("Koku / 观子");
        stage.setMinWidth(1280);
        stage.setMinHeight(860);
        stage.setResizable(true);
        stage.fullScreenProperty().addListener((obs, oldValue, newValue) -> updateMenuState());
        stage.show();
        updateMenuState();
    }

    private MenuActions buildMenuActions() {
        return new MenuActions(
                this::newMatchFromMenu,
                this::undoFromMenu,
                this::showSelect,
                this::openSettingsFromMenu,
                this::setShowCoordinatesFromMenu,
                this::setShowLastMoveMarkerFromMenu,
                this::toggleThemeFromMenu,
                this::setLanguageFromMenu,
                this::showGame,
                this::showRulesDialog,
                this::showShortcutsDialog,
                this::showAboutDialog,
                Platform::exit
        );
    }

    private void showSelect() {
        currentMainView = null;
        appRoot.setCenter(new GameSelectView(this::showGame));
        updateMenuState();
    }

    private void showGame(GameDefinition gameDefinition) {
        currentMainView = new MainView(gameDefinition, this::showSelect);
        appRoot.setCenter(currentMainView);
        updateMenuState();
    }

    private void newMatchFromMenu() {
        if (currentMainView != null) {
            currentMainView.requestNewMatch();
            updateMenuState();
        }
    }

    private void undoFromMenu() {
        if (currentMainView != null) {
            currentMainView.requestUndo();
            updateMenuState();
        }
    }

    private void openSettingsFromMenu() {
        if (currentMainView != null) {
            currentMainView.requestOpenSettingsPanel();
            updateMenuState();
        }
    }

    private void setShowCoordinatesFromMenu(boolean selected) {
        if (currentMainView != null) {
            currentMainView.setShowCoordinatesFromMenu(selected);
            updateMenuState();
        }
    }

    private void setShowLastMoveMarkerFromMenu(boolean selected) {
        if (currentMainView != null) {
            currentMainView.setShowLastMoveMarkerFromMenu(selected);
            updateMenuState();
        }
    }

    private void toggleThemeFromMenu() {
        if (currentMainView != null) {
            currentMainView.toggleThemeModeFromMenu();
            updateMenuState();
        }
    }

    private void setLanguageFromMenu(LanguageMode languageMode) {
        if (currentMainView != null) {
            currentMainView.setLanguageModeFromMenu(languageMode);
            updateMenuState();
        }
    }

    private void updateMenuState() {
        boolean inGame = currentMainView != null;
        appMenu.update(
                inGame,
                inGame && currentMainView.supportsCoordinateMenu(),
                inGame && currentMainView.isShowCoordinatesSelected(),
                inGame && currentMainView.isShowLastMoveMarkerSelected()
        );
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于 Koku / 观子");
        alert.setHeaderText("Koku / 观子");
        alert.setContentText("一个简洁的桌面棋类合集。");
        alert.show();
    }

    private void showRulesDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("游戏规则");
        alert.setHeaderText("游戏规则");
        alert.setContentText("""
                三子棋：3x3 棋盘，率先三连获胜。
                四子棋：点击列落子，率先四连获胜。
                五子棋：率先五连获胜，可选禁手规则。
                六子棋：黑方首手一子，之后每回合两子，率先六连获胜。
                """);
        alert.show();
    }

    private void showShortcutsDialog() {
        String modifier = isMac() ? "Command" : "Ctrl";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("快捷键说明");
        alert.setHeaderText("快捷键说明");
        alert.setContentText(String.format("""
                %s + N：新对局
                %s + Z：悔棋
                %s + B：返回选择游戏
                %s + ,：打开设置面板
                %s + Q：退出
                F11：全屏（Windows / Linux）
                """, modifier, modifier, modifier, modifier, modifier));
        alert.show();
    }

    private boolean isMac() {
        return System.getProperty("os.name", "").toLowerCase().contains("mac");
    }

    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "Koku / 观子");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Koku / 观子");
        launch(args);
    }
}
