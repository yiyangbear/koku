package com.example.koku.config;

public class AppSettings {
    private ThemeMode themeMode;
    private LanguageMode languageMode;
    private boolean showCoordinates;
    private boolean showLastMoveMarker;

    private RuleConfig currentRuleConfig;
    private RuleConfig pendingRuleConfig;

    public AppSettings() {
        this.themeMode = ThemeMode.DARK;
        this.languageMode = LanguageMode.ZH_CN;
        this.showCoordinates = true;
        this.showLastMoveMarker = true;
        this.currentRuleConfig = RuleConfig.defaultConfig();
        this.pendingRuleConfig = this.currentRuleConfig;
    }

    public ThemeMode getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;
    }

    public LanguageMode getLanguageMode() {
        return languageMode;
    }

    public void setLanguageMode(LanguageMode languageMode) {
        this.languageMode = languageMode;
    }

    public boolean isShowCoordinates() {
        return showCoordinates;
    }

    public void setShowCoordinates(boolean showCoordinates) {
        this.showCoordinates = showCoordinates;
    }

    public boolean isShowLastMoveMarker() {
        return showLastMoveMarker;
    }

    public void setShowLastMoveMarker(boolean showLastMoveMarker) {
        this.showLastMoveMarker = showLastMoveMarker;
    }

    public RuleConfig getCurrentRuleConfig() {
        return currentRuleConfig;
    }

    public RuleConfig getPendingRuleConfig() {
        return pendingRuleConfig;
    }

    public void setPendingRuleConfig(RuleConfig pendingRuleConfig) {
        this.pendingRuleConfig = pendingRuleConfig;
    }

    public void applyPendingRuleConfig() {
        this.currentRuleConfig = this.pendingRuleConfig;
    }

    public boolean hasPendingRuleChanges() {
        return !currentRuleConfig.equals(pendingRuleConfig);
    }
}
