package com.example.koku.service;

import com.example.koku.config.AppSettings;
import com.example.koku.config.LanguageMode;
import com.example.koku.config.RuleConfig;
import com.example.koku.config.ThemeMode;

public class SettingsService {
    private final AppSettings settings = new AppSettings();

    public AppSettings getSettings() {
        return settings;
    }

    public void setThemeMode(ThemeMode mode) {
        settings.setThemeMode(mode);
    }

    public void setLanguageMode(LanguageMode mode) {
        settings.setLanguageMode(mode);
    }

    public void setShowCoordinates(boolean value) {
        settings.setShowCoordinates(value);
    }

    public void setShowLastMoveMarker(boolean value) {
        settings.setShowLastMoveMarker(value);
    }

    public void updatePendingRuleConfig(RuleConfig ruleConfig) {
        settings.setPendingRuleConfig(ruleConfig);
    }

    public void applyPendingRules() {
        settings.applyPendingRuleConfig();
    }
}