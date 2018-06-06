package com.avairebot.language;

import com.avairebot.config.YamlConfiguration;

import java.io.InputStreamReader;

public class LanguageHolder {

    private final Language language;
    private final YamlConfiguration config;

    LanguageHolder(Language language) {
        this.language = language;

        config = YamlConfiguration.loadConfiguration(new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(("langs/" + language.getCode() + ".yml"))
        ));
    }

    public Language getLanguage() {
        return language;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return language.getNativeName();
    }
}
