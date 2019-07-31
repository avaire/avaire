/*
 * Copyright (c) 2019.
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

package com.avairebot.modlog;

import com.avairebot.BaseTest;
import com.avairebot.language.I18n;
import com.avairebot.language.LanguageContainer;
import com.google.common.base.CaseFormat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModlogTests extends BaseTest {

    @BeforeAll
    public static void initAll() {
        I18n.start(null);
    }

    @Test
    public void testModlogNameWorksWithNullGuild() {
        for (ModlogType type : ModlogType.values()) {
            assertNotNull(type.getName(null));
        }
    }

    @Test
    public void testModlogActionsExistsInLanguageFiles() {
        // We only need to check the default language since the language tests
        // will make sure every key that exists in the default language also
        // exists in all the other language files.
        LanguageContainer defaultLanguage = I18n.getDefaultLanguage();

        for (ModlogType type : ModlogType.values()) {
            String path = String.format("modlog-types.%s",
                CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, type.name())
            );

            assertTrue(defaultLanguage.getConfig().contains(path + ".name"));
            assertTrue(defaultLanguage.getConfig().contains(path + ".action"));
        }
    }
}
