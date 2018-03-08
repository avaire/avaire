package com.avairebot.language;

import com.avairebot.config.YamlConfiguration;

import java.io.InputStreamReader;
import java.util.Locale;

public class LanguageLocale {

    private final Locale locale;
    private final String code;
    private final String nativeName;

    private final YamlConfiguration config;

    public LanguageLocale(Locale locale, String code, String nativeName) {
        this.locale = locale;
        this.code = code;
        this.nativeName = nativeName;

        config = YamlConfiguration.loadConfiguration(new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(("langs/" + code + ".yml"))
        ));
    }

    public Locale getLocale() {
        return locale;
    }

    public String getCode() {
        return code;
    }

    public String getNativeName() {
        return nativeName;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return nativeName;
    }
}
