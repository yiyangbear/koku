package com.example.koku.ui;

import com.example.koku.game.GameDefinition;
import com.example.koku.game.GameRegistry;
import com.example.koku.service.I18nService;
import com.example.koku.service.SettingsService;
import com.example.koku.service.ThemeService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.function.Consumer;

public class GameSelectView extends BorderPane {
    private final SettingsService settingsService;
    private final ThemeService themeService;
    private final I18nService i18nService;
    private final String fontFamily;

    private final Label titleLabel;
    private final Label subtitleLabel;
    private VBox gamesBox;

    public GameSelectView(Consumer<GameDefinition> onSelect) {
        this.settingsService = new SettingsService();
        this.themeService = new ThemeService();
        this.i18nService = new I18nService(settingsService.getSettings().getLanguageMode());
        this.fontFamily = loadFontFamily();

        titleLabel = new Label();
        subtitleLabel = new Label();
        gamesBox = new VBox(12);
        gamesBox.setAlignment(Pos.CENTER);

        VBox center = new VBox(18, titleLabel, subtitleLabel, gamesBox);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(32));

        setCenter(center);

        applyTexts();
        applyTheme();
        buildGameCards(onSelect);
    }

    private void applyTexts() {
        titleLabel.setText(i18nService.text("select.title"));
        subtitleLabel.setText(i18nService.text("select.subtitle"));
    }

    private void applyTheme() {
        ThemeService.Palette palette = themeService.getPalette(settingsService.getSettings().getThemeMode()
                == com.example.koku.config.ThemeMode.DARK);

        setStyle("-fx-background-color: %s; -fx-font-family: \"%s\";".formatted(palette.windowBg(), fontFamily));

        titleLabel.setStyle("""
                -fx-text-fill: %s;
                -fx-font-size: 22px;
                -fx-font-weight: 600;
                """.formatted(palette.primaryText()));

        subtitleLabel.setStyle("""
                -fx-text-fill: %s;
                -fx-font-size: 13px;
                """.formatted(palette.secondaryText()));
    }

    private void buildGameCards(Consumer<GameDefinition> onSelect) {
        ThemeService.Palette palette = themeService.getPalette(settingsService.getSettings().getThemeMode()
                == com.example.koku.config.ThemeMode.DARK);

        gamesBox.getChildren().clear();
        for (GameDefinition definition : GameRegistry.all()) {
            Label title = new Label(i18nService.text(definition.titleKey()));
            Label description = new Label(i18nService.text(definition.descriptionKey()));
            Button play = new Button(i18nService.text("select.play"));

            title.setStyle("""
                -fx-text-fill: %s;
                -fx-font-size: 18px;
                -fx-font-weight: 600;
                """.formatted(palette.primaryText()));

            description.setStyle("""
                -fx-text-fill: %s;
                -fx-font-size: 13px;
                """.formatted(palette.secondaryText()));

            play.setStyle("""
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                -fx-text-fill: %s;
                -fx-font-weight: 600;
                -fx-font-size: 14px;
                -fx-pref-height: 40;
                -fx-padding: 0 16 0 16;
                """.formatted(palette.buttonBg(), palette.buttonBorder(), palette.primaryText()));

            VBox card = new VBox(10, title, description, play);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(18));
            card.setMinWidth(360);
            card.setMaxWidth(420);
            card.setStyle("""
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-radius: 18;
                -fx-background-radius: 18;
                """.formatted(palette.panelBg(), palette.cardBorder()));

            play.setOnAction(event -> onSelect.accept(definition));
            gamesBox.getChildren().add(card);
        }
    }

    private String loadFontFamily() {
        Font font = Font.loadFont(GameSelectView.class.getResourceAsStream("/fonts/SmileySans-Oblique.ttf"), 12);
        if (font == null) {
            return Font.getDefault().getFamily();
        }
        return font.getFamily();
    }
}
