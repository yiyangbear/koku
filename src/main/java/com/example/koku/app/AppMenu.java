package com.example.koku.app;

import com.example.koku.config.LanguageMode;
import com.example.koku.game.GameDefinition;
import com.example.koku.game.GameRegistry;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class AppMenu {
    private final Stage stage;
    private final MenuActions actions;
    private final boolean mac;

    private MenuItem newMatchItem;
    private MenuItem undoItem;
    private MenuItem backToSelectItem;
    private MenuItem preferencesItem;
    private MenuItem openSettingsItem;
    private CheckMenuItem showCoordinatesItem;
    private CheckMenuItem showLastMoveMarkerItem;

    public AppMenu(Stage stage, MenuActions actions) {
        this.stage = stage;
        this.actions = actions;
        this.mac = System.getProperty("os.name", "").toLowerCase().contains("mac");
    }

    public MenuBar build() {
        setMacApplicationName();

        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(mac);

        if (mac) {
            menuBar.getMenus().add(applicationMenu());
        }
        menuBar.getMenus().addAll(fileMenu(), gameMenu(), viewMenu(), settingsMenu(), helpMenu());
        return menuBar;
    }

    public void update(boolean inGame, boolean coordinatesSupported,
                       boolean coordinatesSelected, boolean lastMoveMarkerSelected) {
        newMatchItem.setDisable(!inGame);
        undoItem.setDisable(!inGame);
        backToSelectItem.setDisable(!inGame);
        if (preferencesItem != null) {
            preferencesItem.setDisable(!inGame);
        }
        openSettingsItem.setDisable(!inGame);
        showCoordinatesItem.setDisable(!inGame || !coordinatesSupported);
        showLastMoveMarkerItem.setDisable(!inGame);
        showCoordinatesItem.setSelected(inGame && coordinatesSelected);
        showLastMoveMarkerItem.setSelected(inGame && lastMoveMarkerSelected);
    }

    private Menu fileMenu() {
        Menu file = new Menu("文件");
        newMatchItem = item("新对局", "Shortcut+N", actions.newMatch());
        backToSelectItem = item("返回选择游戏", "Shortcut+B", actions.backToSelect());
        MenuItem quitItem = item("退出 Koku / 观子", "Shortcut+Q", actions.quit());
        file.getItems().addAll(newMatchItem, backToSelectItem, new SeparatorMenuItem(), quitItem);
        return file;
    }

    private Menu applicationMenu() {
        Menu app = new Menu("Koku / 观子");
        MenuItem aboutItem = item("关于 Koku / 观子", null, actions.showAbout());
        preferencesItem = item("偏好设置...", "Shortcut+,", actions.openSettings());
        MenuItem quitItem = item("退出 Koku / 观子", "Shortcut+Q", actions.quit());
        app.getItems().addAll(aboutItem, preferencesItem, new SeparatorMenuItem(), quitItem);
        return app;
    }

    private Menu gameMenu() {
        Menu game = new Menu("游戏");
        undoItem = item("悔棋", "Shortcut+Z", actions.undo());
        game.getItems().addAll(
                undoItem,
                new SeparatorMenuItem(),
                gameItem("三子棋", GameRegistry.ticTacToe()),
                gameItem("四子棋", GameRegistry.connectFour()),
                gameItem("五子棋", GameRegistry.gomoku()),
                gameItem("六子棋", GameRegistry.sixInRow())
        );
        return game;
    }

    private Menu viewMenu() {
        Menu view = new Menu("视图");
        showCoordinatesItem = new CheckMenuItem("显示坐标");
        showLastMoveMarkerItem = new CheckMenuItem("显示最近一步标记");
        CheckMenuItem fullScreenItem = new CheckMenuItem("全屏");

        showCoordinatesItem.setOnAction(event -> actions.showCoordinates().accept(showCoordinatesItem.isSelected()));
        showLastMoveMarkerItem.setOnAction(event -> actions.showLastMoveMarker().accept(showLastMoveMarkerItem.isSelected()));
        fullScreenItem.setOnAction(event -> stage.setFullScreen(fullScreenItem.isSelected()));
        if (!mac) {
            fullScreenItem.setAccelerator(KeyCombination.keyCombination("F11"));
        }
        stage.fullScreenProperty().addListener((obs, oldValue, newValue) -> fullScreenItem.setSelected(newValue));

        view.getItems().addAll(showCoordinatesItem, showLastMoveMarkerItem, new SeparatorMenuItem(), fullScreenItem);
        return view;
    }

    private Menu settingsMenu() {
        Menu settings = new Menu("设置");
        openSettingsItem = item("打开设置面板", "Shortcut+,", actions.openSettings());
        MenuItem toggleThemeItem = item("切换深色 / 浅色模式", null, actions.toggleTheme());

        Menu languageMenu = new Menu("语言设置");
        ToggleGroup languageGroup = new ToggleGroup();
        RadioMenuItem zhItem = new RadioMenuItem("简体中文");
        RadioMenuItem enItem = new RadioMenuItem("English");
        zhItem.setToggleGroup(languageGroup);
        enItem.setToggleGroup(languageGroup);
        zhItem.setSelected(true);
        zhItem.setOnAction(event -> actions.setLanguage().accept(LanguageMode.ZH_CN));
        enItem.setOnAction(event -> actions.setLanguage().accept(LanguageMode.EN_US));
        languageMenu.getItems().addAll(zhItem, enItem);

        settings.getItems().addAll(openSettingsItem, toggleThemeItem, languageMenu);
        return settings;
    }

    private Menu helpMenu() {
        Menu help = new Menu("帮助");
        help.getItems().addAll(
                item("游戏规则", null, actions.showRules()),
                item("快捷键说明", null, actions.showShortcuts()),
                new SeparatorMenuItem(),
                item("关于 Koku / 观子", null, actions.showAbout())
        );
        return help;
    }

    private MenuItem gameItem(String text, GameDefinition definition) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(event -> actions.switchGame().accept(definition));
        return item;
    }

    private MenuItem item(String text, String accelerator, Runnable action) {
        MenuItem item = new MenuItem(text);
        if (accelerator != null) {
            item.setAccelerator(KeyCombination.keyCombination(accelerator));
        }
        item.setOnAction(event -> action.run());
        return item;
    }

    private void setMacApplicationName() {
        System.setProperty("apple.awt.application.name", "Koku / 观子");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Koku / 观子");
    }
}
