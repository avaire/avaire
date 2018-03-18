package com.avairebot.language;

import com.avairebot.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
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

        for (Map.Entry<String, LanguageLocale> entry : I18n.LANGS.entrySet()) {
            Set<String> strings = getKeys(entry.getValue());

            assertEquals(defaultStrings.size(), strings.size(), "Comparing keys sizes for the default language and " + entry.getKey());
        }
    }

    @Test
    public void testSubLanguagesHasAllTheSameKeysAsTheDefaultLanguage() {
        Set<String> defaultStrings = getKeys(I18n.DEFAULT);

        for (Map.Entry<String, LanguageLocale> entry : I18n.LANGS.entrySet()) {
            for (String str : getKeys(entry.getValue())) {
                assertTrue(defaultStrings.contains(str), "Checking the \"" + str + "\" string in the \"" + entry.getKey() + "\" language file");
            }
        }
    }

    @Test
    public void testLanguagesDoesNotReturnNull() {
        Set<String> defaultStrings = getKeys(I18n.DEFAULT);

        for (Map.Entry<String, LanguageLocale> entry : I18n.LANGS.entrySet()) {
            for (String str : defaultStrings) {
                assertNotNull(entry.getValue().getConfig().getString(str));
            }
        }
    }

    private Set<String> getKeys(LanguageLocale locale) {
        return locale.getConfig().getKeys(true);
    }
}
