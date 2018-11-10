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

import com.avairebot.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LanguageTests extends BaseTest {

    @BeforeAll
    public static void initAll() {
        I18n.start(null);
    }

    @Test
    public void testEveryLanguageAccessorIsUnique() {
        Set<String> accessors = new HashSet<>();
        for (Language language : Language.values()) {
            assertTrue(
                accessors.add(language.getCode()),
                "The " + language.getEnglishName() + "'s language code has already been registered by another language."
            );
            assertTrue(
                accessors.add(language.getNativeName()),
                "The " + language.getEnglishName() + "'s native name has already been registered by another language."
            );

            if (!language.getEnglishName().equalsIgnoreCase(language.getNativeName())) {
                assertTrue(
                    accessors.add(language.getEnglishName()),
                    "The " + language.getEnglishName() + "'s English name has already been registered by another language."
                );

            }
            for (String name : language.getOther()) {
                assertTrue(
                    accessors.add(name),
                    "The " + language.getEnglishName() + "'s \"other names\" has already been registered by another language."
                );
            }
        }
    }

    @Test
    public void testSubLanguagesIsTheSameSizeAsTheDefaultLanguage() {
        Set<String> defaultStrings = getKeys(I18n.getDefaultLanguage());

        for (LanguageContainer entry : I18n.languages) {
            Set<String> strings = getKeys(entry);

            if (defaultStrings.size() != strings.size()) {
                getLogger().error("Comparing keys sizes for the default language and " + entry.getLanguage().getCode() + " failed!\n\nExpected:\t{}\nActual:\t\t{}\n",
                    defaultStrings.size(), strings.size(), new AssertionFailedError()
                );
            }
        }
    }

    @Test
    public void testSubLanguagesHasAllTheSameKeysAsTheDefaultLanguage() {
        Set<String> defaultStrings = getKeys(I18n.getDefaultLanguage());

        for (LanguageContainer entry : I18n.languages) {
            for (String str : getKeys(entry)) {
                assertTrue(defaultStrings.contains(str), "Checking the \"" + str + "\" string in the \"" + entry.getLanguage().getCode() + "\" language file");
            }
        }
    }

    @Test
    public void testLanguagesDoesNotReturnNull() {
        Set<String> defaultStrings = getKeys(I18n.getDefaultLanguage());

        for (LanguageContainer entry : I18n.languages) {
            for (String str : defaultStrings) {
                assertNotNull(entry.getConfig().getString(str), str + " in the " + entry.getLanguage().getEnglishName() + " language files was not found!");
            }
        }
    }

    @Test
    public void testLanguageFormattingFormatsStringsCorrectly() {
        assertEquals(I18n.format("This is a test message"), "This is a test message");
        assertEquals(I18n.format("This is 1 test message"), "This is 1 test message");
        assertEquals(I18n.format("This is {0} test message"), "This is {0} test message");
        assertEquals(I18n.format("This is {0} test message", "a"), "This is a test message");
        assertEquals(I18n.format("{0}, {1}!", "Hello", "World"), "Hello, World!");
        assertEquals(I18n.format("{1}, {0}!", "World", "Hello"), "Hello, World!");
        assertEquals(I18n.format("{0}{1}", "{0}", "test"), "{0}test");
        assertEquals(I18n.format("**{0}** [{1}]({2})", "1", "name", "link"), "**1** [name](link)");
        assertEquals(I18n.format("{0}, {1}", "^$", "string"), "^$, string");
        assertEquals(I18n.format("{0}{1}", "{1}", "test"), "{1}test");
        assertEquals(I18n.format("Hello, {0}", null, "World"), "Hello, World");
        assertEquals(I18n.format("Number {0}", 9), "Number 9");
        assertEquals(I18n.format("Test '{0}", "thing"), "Test 'thing");
        assertEquals(I18n.format("'\""), "'\"");
        assertEquals(I18n.format("{0}: {1}", "thing"), "thing: {1}");
        assertEquals(I18n.format("$"), "$");
    }

    private Set<String> getKeys(LanguageContainer locale) {
        return locale.getConfig().getKeys(true);
    }
}
