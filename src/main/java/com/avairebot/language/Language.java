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
    NO_NB("no", "NB", "Norsk", "Norwegian");

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
