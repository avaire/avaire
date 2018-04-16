package com.avairebot.language;

import com.avairebot.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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

            assertEquals(defaultStrings.size(), strings.size(), "Comparing keys sizes for the default language and " + entry.getLanguage().getCode());
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
                assertNotNull(entry.getConfig().getString(str));
            }
        }
    }

    private Set<String> getKeys(LanguageHolder locale) {
        return locale.getConfig().getKeys(true);
    }
}
