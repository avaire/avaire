package com.avairebot.language;

import com.avairebot.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LanguageTests extends BaseTest {

    @BeforeAll
    public static void initAll() {
        I18n.start(null);
    }

    @Test
    public void testSubLanguagesIsTheSameSizeAsTheDefaultLanguage() {
        Set<String> defaultStrings = getKeys(I18n.DEFAULT);

        for (LanguageHolder entry : I18n.LANGS) {
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
        Set<String> defaultStrings = getKeys(I18n.DEFAULT);

        for (LanguageHolder entry : I18n.LANGS) {
            for (String str : getKeys(entry)) {
                assertTrue(defaultStrings.contains(str), "Checking the \"" + str + "\" string in the \"" + entry.getLanguage().getCode() + "\" language file");
            }
        }
    }

    @Test
    public void testLanguagesDoesNotReturnNull() {
        Set<String> defaultStrings = getKeys(I18n.DEFAULT);

        for (LanguageHolder entry : I18n.LANGS) {
            for (String str : defaultStrings) {
                assertNotNull(entry.getConfig().getString(str), str + " in the " + entry.getLanguage().getEnglishName() + " language files was not found!");
            }
        }
    }

    private Set<String> getKeys(LanguageHolder locale) {
        return locale.getConfig().getKeys(true);
    }
}
