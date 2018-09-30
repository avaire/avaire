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

package com.avairebot.utilities;

import com.avairebot.BaseTest;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComparatorTests extends BaseTest {

    @Test
    public void testCanFindFuzzyTruths() {
        for (String arg : new String[]{"yes", "y", "on", "enable", "true", "confirm", "1"}) {
            assertTrue(ComparatorUtil.isFuzzyTrue(arg), arg);
        }

        for (String arg : new String[]{"Yes", "Y", "oN", "EnAbLe", "TRuE", "ConFIRM", "1"}) {
            assertTrue(ComparatorUtil.isFuzzyTrue(arg), arg);
        }
    }

    @Test
    public void testCanFindFuzzyFalses() {
        for (String arg : new String[]{"no", "n", "off", "disable", "false", "0"}) {
            assertTrue(ComparatorUtil.isFuzzyFalse(arg), arg);
        }

        for (String arg : new String[]{"nO", "N", "OFf", "DiSAbLE", "FaLSE", "0"}) {
            assertTrue(ComparatorUtil.isFuzzyFalse(arg), arg);
        }
    }

    @Test
    public void testCanFindComparatorsByName() {
        assertTrue(ComparatorUtil.getFuzzyType("yes").getValue(), "yes");
        assertTrue(ComparatorUtil.getFuzzyType("y").getValue(), "y");
        assertTrue(ComparatorUtil.getFuzzyType("on").getValue(), "on");
        assertFalse(ComparatorUtil.getFuzzyType("no").getValue(), "no");
        assertFalse(ComparatorUtil.getFuzzyType("n").getValue(), "n");
        assertFalse(ComparatorUtil.getFuzzyType("off").getValue(), "off");
    }

    @Test
    public void testUnknownTypesDefaultsToFalse() {
        assertFalse(ComparatorUtil.isFuzzyTrue("unknown type"));
        assertFalse(ComparatorUtil.isFuzzyFalse("unknown type"));
        assertFalse(ComparatorUtil.getFuzzyType("unknown type").getValue());
    }

    @Test
    public void testNullsReturnsFalse() {
        assertFalse(ComparatorUtil.isFuzzyTrue(null));
        assertFalse(ComparatorUtil.isFuzzyFalse(null));
        assertFalse(ComparatorUtil.getFuzzyType(null).getValue());
    }
}
