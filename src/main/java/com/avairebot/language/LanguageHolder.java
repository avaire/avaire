/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

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
