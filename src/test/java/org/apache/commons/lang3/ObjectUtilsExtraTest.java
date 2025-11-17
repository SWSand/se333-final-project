package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

public class ObjectUtilsExtraTest {

    @Test
    public void testDefaultIfNullAndFirstNonNull() {
        String def = ObjectUtils.defaultIfNull(null, "fallback");
        assertEquals("fallback", def);

        String first = ObjectUtils.firstNonNull(null, null, "firstNonNull", "second");
        assertEquals("firstNonNull", first);

        // When all null, firstNonNull may throw NullPointerException in some versions; handle via try/catch
        try {
            ObjectUtils.firstNonNull((Object) null, (Object) null);
            fail("Expected NullPointerException when all args are null");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test
    public void testIdentityToStringNotNull() {
        Object o = new Object();
        String s = ObjectUtils.identityToString(o);
        assertNotNull(s);
        assertTrue(s.length() > 0);
    }
}
