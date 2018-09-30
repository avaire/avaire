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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public enum Language {

    EN_US("en", "US", "English", "English"),
    EN_PT("en", "PT", "Pirate", "Pirate English", "arrr"),
    DE_DE("de", "DE", "Deutsch", "German"),
    FR_FR("fr", "FR", "French", "French"),
    HU_HU("hu", "HU", "Magyar", "Hungarian"),
    NO_NB("no", "NB", "Norsk", "Norwegian"),
    RU_RU("ru", "RU", "Russian", "Russian"),
    ES_ES("es", "ES", "Espanol", "Spanish");

    private final String code;
    private final String nativeName;
    private final String englishName;
    private final List<String> other;

    Language(String language, String country, String nativeName, String englishName, String... other) {
        this.code = language + "_" + country;
        this.nativeName = nativeName;
        this.englishName = englishName;
        this.other = new LanguageList(Arrays.asList(other));
    }

    @Nullable
    public static Language parse(@Nonnull String string) {
        for (Language language : values()) {
            if (language.getEnglishName().equalsIgnoreCase(string)
                || language.getNativeName().equalsIgnoreCase(string)
                || language.getCode().equalsIgnoreCase(string)
                || language.getOther().contains(string)) {
                return language;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getNativeName() {
        return nativeName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public List<String> getOther() {
        return other;
    }

    private class LanguageList extends ArrayList<String> {

        LanguageList(Collection<? extends String> c) {
            super(c);
        }

        @Override
        public boolean contains(Object o) {
            String argument = (String) o;
            for (String item : this) {
                if (argument.equalsIgnoreCase(item)) {
                    return true;
                }
            }
            return false;
        }
    }
}
