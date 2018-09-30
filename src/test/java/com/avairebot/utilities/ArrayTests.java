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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayTests extends BaseTest {

    @Test
    public void testStringIsSplitBySpaces() {
        // 8 Words sentence
        String message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

        assertEquals(8, ArrayUtil.toArguments(message).length);
    }

    @Test
    public void testStringIsSplitByQuotes() {
        // 17 in total, with 7 of them being in quotes, should equal to 12 words by method
        String message = "Excepteur \"sint occaecat cupidatat\" non proident, sunt in culpa \"qui officia deserunt mollit\" anim id est laborum.";

        assertEquals(12, ArrayUtil.toArguments(message).length);
    }
}
