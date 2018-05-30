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
