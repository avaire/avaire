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
