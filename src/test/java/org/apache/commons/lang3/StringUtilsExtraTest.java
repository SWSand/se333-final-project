package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

public class StringUtilsExtraTest {

    @Test
    public void testIsEmptyAndIsBlank() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty("a"));

        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(" "));
        assertFalse(StringUtils.isBlank(" a "));
    }

    @Test
    public void testJoinReverseAndSubstringBetween() {
        String[] arr = new String[] {"a","b","c"};
        assertEquals("a-b-c", StringUtils.join(arr, "-"));

        assertEquals("cba", StringUtils.reverse("abc"));

        String s = "foo[bar]baz";
        assertEquals("bar", StringUtils.substringBetween(s, "[", "]"));
        assertNull(StringUtils.substringBetween("no brackets", "[", "]"));
    }

    @Test
    public void testContainsStartsEnds() {
        String t = "hello";
        assertTrue(StringUtils.contains(t, "ell"));
        assertFalse(StringUtils.contains(t, "xyz"));

        assertTrue(StringUtils.startsWith(t, "he"));
        assertFalse(StringUtils.startsWith(t, "ll"));

        assertTrue(StringUtils.endsWith(t, "lo"));
        assertFalse(StringUtils.endsWith(t, "he"));
    }
}
