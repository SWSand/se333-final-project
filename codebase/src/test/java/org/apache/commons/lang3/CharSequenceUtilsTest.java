/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Assert;

import org.junit.Test;

/**
 * Tests CharSequenceUtils
 *
 * @version $Id: CharSequenceUtilsTest.java 1066341 2011-02-02 06:21:53Z bayard $
 */
public class CharSequenceUtilsTest {

    //-----------------------------------------------------------------------
    @Test
    public void testConstructor() {
        assertNotNull(new CharSequenceUtils());
        final Constructor<?>[] cons = CharSequenceUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(CharSequenceUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(CharSequenceUtils.class.getModifiers()));
    }
    
    //-----------------------------------------------------------------------
    @Test
    public void testSubSequence() {
        //
        // null input
        //
        Assert.assertEquals(null, CharSequenceUtils.subSequence(null, -1));
        Assert.assertEquals(null, CharSequenceUtils.subSequence(null, 0));
        Assert.assertEquals(null, CharSequenceUtils.subSequence(null, 1));
        //
        // non-null input
        //
        Assert.assertEquals(StringUtils.EMPTY, CharSequenceUtils.subSequence(StringUtils.EMPTY, 0));
        Assert.assertEquals("012", CharSequenceUtils.subSequence("012", 0));
        Assert.assertEquals("12", CharSequenceUtils.subSequence("012", 1));
        Assert.assertEquals("2", CharSequenceUtils.subSequence("012", 2));
        Assert.assertEquals(StringUtils.EMPTY, CharSequenceUtils.subSequence("012", 3));
        //
        // Exception expected
        //
        try {
            Assert.assertEquals(null, CharSequenceUtils.subSequence(StringUtils.EMPTY, -1));
            Assert.fail("Expected " + IndexOutOfBoundsException.class.getName());
        } catch (final IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            Assert.assertEquals(null, CharSequenceUtils.subSequence(StringUtils.EMPTY, 1));
            Assert.fail("Expected " + IndexOutOfBoundsException.class.getName());
        } catch (final IndexOutOfBoundsException e) {
            // Expected
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testToCharArray() throws Exception {
        // Test toCharArray using reflection (package-private method)
        final java.lang.reflect.Method method = CharSequenceUtils.class.getDeclaredMethod(
            "toCharArray", CharSequence.class);
        method.setAccessible(true);
        
        // Test with String (should use String.toCharArray())
        final String str = "Hello";
        final char[] result1 = (char[]) method.invoke(null, str);
        assertNotNull("Result should not be null", result1);
        assertEquals("Length should match", str.length(), result1.length);
        assertEquals("First char should match", 'H', result1[0]);
        
        // Test with non-String CharSequence (should use loop)
        final java.lang.StringBuilder sb = new java.lang.StringBuilder("World");
        final char[] result2 = (char[]) method.invoke(null, sb);
        assertNotNull("Result should not be null", result2);
        assertEquals("Length should match", sb.length(), result2.length);
        assertEquals("First char should match", 'W', result2[0]);
        
        // Test with empty CharSequence
        final java.lang.StringBuilder empty = new java.lang.StringBuilder();
        final char[] result3 = (char[]) method.invoke(null, empty);
        assertNotNull("Result should not be null", result3);
        assertEquals("Length should be 0", 0, result3.length);
        
        // Test with single char
        final java.lang.StringBuilder single = new java.lang.StringBuilder("A");
        final char[] result4 = (char[]) method.invoke(null, single);
        assertNotNull("Result should not be null", result4);
        assertEquals("Length should be 1", 1, result4.length);
        assertEquals("Char should match", 'A', result4[0]);
    }

    //-----------------------------------------------------------------------
    @Test
    public void testIndexOf() throws Exception {
        // Test indexOf(CharSequence, int, int) using reflection
        final java.lang.reflect.Method method = CharSequenceUtils.class.getDeclaredMethod(
            "indexOf", CharSequence.class, int.class, int.class);
        method.setAccessible(true);
        
        // Test with String (should use String.indexOf())
        final String str = "Hello World";
        final int result1 = (Integer) method.invoke(null, str, (int)'o', 0);
        assertEquals("Should find 'o' at index 4", 4, result1);
        
        final int result2 = (Integer) method.invoke(null, str, (int)'o', 5);
        assertEquals("Should find 'o' at index 7", 7, result2);
        
        final int result3 = (Integer) method.invoke(null, str, (int)'x', 0);
        assertEquals("Should not find 'x'", -1, result3);
        
        // Test with non-String CharSequence (should use loop)
        final java.lang.StringBuilder sb = new java.lang.StringBuilder("Test");
        final int result4 = (Integer) method.invoke(null, sb, (int)'e', 0);
        assertEquals("Should find 'e' at index 1", 1, result4);
        
        final int result5 = (Integer) method.invoke(null, sb, (int)'e', 2);
        assertEquals("Should not find 'e' after index 2", -1, result5);
        
        // Test with negative start (should be set to 0)
        final int result6 = (Integer) method.invoke(null, str, (int)'H', -1);
        assertEquals("Should find 'H' at index 0", 0, result6);
        
        // Test with start beyond length
        final int result7 = (Integer) method.invoke(null, str, (int)'H', 100);
        assertEquals("Should not find 'H' beyond length", -1, result7);
    }

    //-----------------------------------------------------------------------
    @Test
    public void testLastIndexOf() throws Exception {
        // Test lastIndexOf(CharSequence, int, int) using reflection
        final java.lang.reflect.Method method = CharSequenceUtils.class.getDeclaredMethod(
            "lastIndexOf", CharSequence.class, int.class, int.class);
        method.setAccessible(true);
        
        // Test with String (should use String.lastIndexOf())
        final String str = "Hello World";
        final int result1 = (Integer) method.invoke(null, str, (int)'o', str.length());
        assertEquals("Should find last 'o' at index 7", 7, result1);
        
        final int result2 = (Integer) method.invoke(null, str, (int)'o', 5);
        assertEquals("Should find 'o' at index 4", 4, result2);
        
        final int result3 = (Integer) method.invoke(null, str, (int)'x', str.length());
        assertEquals("Should not find 'x'", -1, result3);
        
        // Test with non-String CharSequence (should use loop)
        final java.lang.StringBuilder sb = new java.lang.StringBuilder("Test");
        final int result4 = (Integer) method.invoke(null, sb, (int)'t', sb.length());
        assertEquals("Should find last 't' at index 3", 3, result4);
        
        final int result5 = (Integer) method.invoke(null, sb, (int)'T', 2);
        assertEquals("Should find 'T' at index 0", 0, result5);
        
        final int result5b = (Integer) method.invoke(null, sb, (int)'t', 2);
        assertEquals("Should not find 't' before index 2", -1, result5b);
        
        // Test with negative start (should return -1)
        final int result6 = (Integer) method.invoke(null, str, (int)'H', -1);
        assertEquals("Should return -1 for negative start", -1, result6);
        
        // Test with start beyond length (should be set to length-1)
        final int result7 = (Integer) method.invoke(null, str, (int)'d', 100);
        assertEquals("Should find 'd' at end", str.length() - 1, result7);
    }

    //-----------------------------------------------------------------------
    @Test
    public void testRegionMatches() throws Exception {
        // Test regionMatches using reflection
        final java.lang.reflect.Method method = CharSequenceUtils.class.getDeclaredMethod(
            "regionMatches", CharSequence.class, boolean.class, int.class,
            CharSequence.class, int.class, int.class);
        method.setAccessible(true);
        
        // Test with both String (should use String.regionMatches())
        final String str1 = "Hello World";
        final String str2 = "Hello";
        final boolean result1 = (Boolean) method.invoke(null, str1, false, 0, str2, 0, 5);
        assertTrue("Should match at start", result1);
        
        final boolean result2 = (Boolean) method.invoke(null, str1, false, 6, "World", 0, 5);
        assertTrue("Should match at offset", result2);
        
        final boolean result3 = (Boolean) method.invoke(null, str1, false, 0, "Different", 0, 5);
        assertFalse("Should not match different strings", result3);
        
        // Test case-insensitive with String
        final boolean result4 = (Boolean) method.invoke(null, "Hello", true, 0, "HELLO", 0, 5);
        assertTrue("Should match case-insensitive", result4);
        
        // Test with non-String CharSequence (should use loop)
        final java.lang.StringBuilder sb1 = new java.lang.StringBuilder("Test");
        final java.lang.StringBuilder sb2 = new java.lang.StringBuilder("est");
        final boolean result5 = (Boolean) method.invoke(null, sb1, false, 1, sb2, 0, 3);
        assertTrue("Should match StringBuilder", result5);
        
        // Test case-insensitive with non-String
        final java.lang.StringBuilder sb3 = new java.lang.StringBuilder("TEST");
        final boolean result6 = (Boolean) method.invoke(null, sb1, true, 0, sb3, 0, 4);
        assertTrue("Should match case-insensitive StringBuilder", result6);
        
        // Test with different case that should not match (case-sensitive)
        final boolean result7 = (Boolean) method.invoke(null, "Hello", false, 0, "HELLO", 0, 5);
        assertFalse("Should not match different case when case-sensitive", result7);
        
        // Test with characters that have different upper/lower case
        final boolean result8 = (Boolean) method.invoke(null, "a", true, 0, "A", 0, 1);
        assertTrue("Should match case-insensitive for a/A", result8);
        
        // Test with length 0
        final boolean result9 = (Boolean) method.invoke(null, "Hello", false, 0, "World", 0, 0);
        assertTrue("Should match zero length", result9);
    }

    //-----------------------------------------------------------------------
    @Test
    public void testIndexOfCharSequence() throws Exception {
        // Test indexOf(CharSequence, CharSequence, int) using reflection
        final java.lang.reflect.Method method = CharSequenceUtils.class.getDeclaredMethod(
            "indexOf", CharSequence.class, CharSequence.class, int.class);
        method.setAccessible(true);
        
        final String str = "Hello World";
        final String search = "World";
        final int result1 = (Integer) method.invoke(null, str, search, 0);
        assertEquals("Should find 'World' at index 6", 6, result1);
        
        final int result2 = (Integer) method.invoke(null, str, "Hello", 0);
        assertEquals("Should find 'Hello' at index 0", 0, result2);
        
        final int result3 = (Integer) method.invoke(null, str, "Not Found", 0);
        assertEquals("Should not find 'Not Found'", -1, result3);
        
        // Test with non-String CharSequence
        final java.lang.StringBuilder sb = new java.lang.StringBuilder("Test String");
        final java.lang.StringBuilder searchSb = new java.lang.StringBuilder("String");
        final int result4 = (Integer) method.invoke(null, sb, searchSb, 0);
        assertEquals("Should find 'String' in StringBuilder", 5, result4);
    }

    //-----------------------------------------------------------------------
    @Test
    public void testLastIndexOfCharSequence() throws Exception {
        // Test lastIndexOf(CharSequence, CharSequence, int) using reflection
        final java.lang.reflect.Method method = CharSequenceUtils.class.getDeclaredMethod(
            "lastIndexOf", CharSequence.class, CharSequence.class, int.class);
        method.setAccessible(true);
        
        final String str = "Hello World Hello";
        final String search = "Hello";
        final int result1 = (Integer) method.invoke(null, str, search, str.length());
        assertEquals("Should find last 'Hello' at index 12", 12, result1);
        
        final int result2 = (Integer) method.invoke(null, str, "World", str.length());
        assertEquals("Should find 'World' at index 6", 6, result2);
        
        final int result3 = (Integer) method.invoke(null, str, "Not Found", str.length());
        assertEquals("Should not find 'Not Found'", -1, result3);
        
        // Test with non-String CharSequence
        final java.lang.StringBuilder sb = new java.lang.StringBuilder("Test Test");
        final java.lang.StringBuilder searchSb = new java.lang.StringBuilder("Test");
        final int result4 = (Integer) method.invoke(null, sb, searchSb, sb.length());
        assertEquals("Should find last 'Test' in StringBuilder", 5, result4);
    }

}
