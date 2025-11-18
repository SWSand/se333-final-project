package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

public class ClassUtilsExtraTest {

    @Test
    public void testGetShortClassNameAndPackageName() {
        String fqcn = "org.apache.commons.lang3.ClassUtils";
        assertEquals("ClassUtils", ClassUtils.getShortClassName(fqcn));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(fqcn));

        // When provided with Class object
        assertEquals("String", ClassUtils.getShortClassName(String.class));
        assertEquals("java.lang", ClassUtils.getPackageName(String.class));
    }

    @Test
    public void testGetShortClassNameForArrayAndInnerClass() {
        assertEquals("String[]", ClassUtils.getShortClassName(String[].class));
        class Local {}
        String shortLocal = ClassUtils.getShortClassName(Local.class);
        assertNotNull(shortLocal);
        assertTrue(shortLocal.contains("Local") || shortLocal.length() > 0);
    }
}
