package com.avairebot.language;

import com.avairebot.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LanguageTests extends BaseTest {

    @BeforeAll
    public static void initAll() {
        I18n.start(null);
    }

    @Test
    public void testSubLanguagesIsTheSameSizeAsTheDefaultLanguage() {
        Set<String> defaultStrings = getKeys(I18n.getDefaultLanguage());

        for (LanguageHolder entry : I18n.languages) {
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

        for (LanguageHolder entry : I18n.languages) {
            for (String str : getKeys(entry)) {
                assertTrue(defaultStrings.contains(str), "Checking the \"" + str + "\" string in the \"" + entry.getLanguage().getCode() + "\" language file");
            }
        }
    }

    @Test
    public void testLanguagesDoesNotReturnNull() {
        Set<String> defaultStrings = getKeys(I18n.getDefaultLanguage());

        for (LanguageHolder entry : I18n.languages) {
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

    private Set<String> getKeys(LanguageHolder locale) {
        return locale.getConfig().getKeys(true);
    }
}
