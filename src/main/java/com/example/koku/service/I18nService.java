package com.example.koku.service;

import com.example.koku.config.LanguageMode;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18nService {
    private LanguageMode languageMode;
    private ResourceBundle bundle;

    public I18nService(LanguageMode languageMode) {
        setLanguageMode(languageMode);
    }

    public void setLanguageMode(LanguageMode languageMode) {
        this.languageMode = languageMode;
        Locale locale = languageMode == LanguageMode.ZH_CN
                ? Locale.SIMPLIFIED_CHINESE
                : Locale.US;
        this.bundle = ResourceBundle.getBundle("i18n.messages", locale);
    }

    public LanguageMode getLanguageMode() {
        return languageMode;
    }

    public String text(String key) {
        return bundle.getString(key);
    }
}