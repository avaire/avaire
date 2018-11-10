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

import javax.annotation.Nonnull;
import java.io.InputStreamReader;

public class LanguageContainer {

    private final Language language;
    private final YamlConfiguration config;

    LanguageContainer(@Nonnull Language language) {
        this.language = language;

        config = YamlConfiguration.loadConfiguration(new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(("langs/" + language.getCode() + ".yml"))
        ));
    }

    /**
     * Gets the language representation of the language container, both the English and
     * the native name for the language can be loaded through that, as well as the
     * language code and other names that can be associated with the language.
     *
     * @return The language representation the current language container.
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Gets the language configuration, the config can be used to
     * load strings, lists, and values directly off the language.
     *
     * @return The language configuration.
     */
    public YamlConfiguration getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return language.getNativeName();
    }
}
