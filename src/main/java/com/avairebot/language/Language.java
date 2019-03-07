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

    DA_DK("da", "DK", "Dansk", "Danish"),
    DE_DE("de", "DE", "Deutsch", "German"),
    EN_US("en", "US", "English", "English"),
    ES_ES("es", "ES", "Espanol", "Spanish"),
    FR_FR("fr", "FR", "French", "French"),
    HU_HU("hu", "HU", "Magyar", "Hungarian"),
    NO_NB("no", "NB", "Norsk", "Norwegian"),
    RU_RU("ru", "RU", "Pусский", "Russian"),
    IT_IT("it", "IT", "Italiano", "Italian"),
    NL_NL("nl", "NL", "Nederlands", "Dutch"),
    ZH_SI("zh", "SI", "中文", "Chinese", "Chinese Simplified", "Zhōngwén");

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

    /**
     * Parse the given string, trying to match it with one of the languages.
     *
     * @param string The string representation of the that should be returned.
     * @return Possibly-null, the language matching the given string, or <code>NULL</code>
     * if no languages matched the given string.
     */
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

    /**
     * Gets the language code for the current language.
     *
     * @return The language code for the current language.
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the native name for the current language.
     *
     * @return The native name for the current language.
     */
    public String getNativeName() {
        return nativeName;
    }

    /**
     * Gets the English version of the name for the current language.
     *
     * @return The English version of the name for the current language.
     */
    public String getEnglishName() {
        return englishName;
    }

    /**
     * Gets a list of other names that can be associated with the current language.
     *
     * @return A list of other names associated with the current language.
     */
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
