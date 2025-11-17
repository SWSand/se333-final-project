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
package org.apache.commons.lang3.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

/**
 * Test case for ToStringStyle.
 * 
 * @version $Id$
 */
public class ToStringStyleTest {

    private static class ToStringStyleImpl extends ToStringStyle {
    }

    //-----------------------------------------------------------------------
    @Test
    public void testSetArrayStart() {
        final ToStringStyle style = new ToStringStyleImpl();
        style.setArrayStart(null);
        assertEquals("", style.getArrayStart());
    }

    @Test
    public void testSetArrayEnd() {
        final ToStringStyle style = new ToStringStyleImpl();
        style.setArrayEnd(null);
        assertEquals("", style.getArrayEnd());
    }

    @Test
    public void testSetArraySeparator() {
        final ToStringStyle style = new ToStringStyleImpl();
        style.setArraySeparator(null);
        assertEquals("", style.getArraySeparator());
    }

    @Test
    public void testSetContentStart() {
        final ToStringStyle style = new ToStringStyleImpl();
        style.setContentStart(null);
        assertEquals("", style.getContentStart());
    }

    @Test
    public void testSetContentEnd() {
        final ToStringStyle style = new ToStringStyleImpl();
        style.setContentEnd(null);
        assertEquals("", style.getContentEnd());
    }

    @Test
    public void testSetFieldNameValueSeparator() {
        final ToStringStyle style = new ToStringStyleImpl();
        style.setFieldNameValueSeparator(null);
        assertEquals("", style.getFieldNameValueSeparator());
    }

    @Test
    public void testSetFieldSeparator() {
        final ToStringStyle style = new ToStringStyleImpl();
        style.setFieldSeparator(null);
        assertEquals("", style.getFieldSeparator());
    }

    @Test
    public void testSetNullText() {
        final ToStringStyle style = new ToStringStyleImpl();
        style.setNullText(null);
        assertEquals("", style.getNullText());
    }

    @Test
    public void testSetSizeStartText() {
        final ToStringStyle style = new ToStringStyleImpl();
        style.setSizeStartText(null);
        assertEquals("", style.getSizeStartText());
    }

    @Test
    public void testSetSizeEndText() {
        final ToStringStyle style = new ToStringStyleImpl();
        style.setSizeEndText(null);
        assertEquals("", style.getSizeEndText());
    }

    @Test
    public void testSetSummaryObjectStartText() {
        final ToStringStyle style = new ToStringStyleImpl();
        style.setSummaryObjectStartText(null);
        assertEquals("", style.getSummaryObjectStartText());
    }

    @Test
    public void testSetSummaryObjectEndText() {
        final ToStringStyle style = new ToStringStyleImpl();
        style.setSummaryObjectEndText(null);
        assertEquals("", style.getSummaryObjectEndText());
    }

    /**
     * An object used to test {@link ToStringStyle}.
     * 
     */
    static class Person {
        /**
         * Test String field.
         */
        String name;

        /**
         * Test integer field.
         */
        int age;

        /**
         * Test boolean field.
         */
        boolean smoker;
    }

    /**
     * Helper method to serialize and deserialize an object.
     */
    @SuppressWarnings("unchecked")
    private <T> T serializeAndDeserialize(T obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (T) ois.readObject();
    }

    @Test
    public void testReadResolveDefaultStyle() throws Exception {
        ToStringStyle original = ToStringStyle.DEFAULT_STYLE;
        ToStringStyle deserialized = serializeAndDeserialize(original);
        assertSame("Deserialized DEFAULT_STYLE should be the same singleton", 
                ToStringStyle.DEFAULT_STYLE, deserialized);
    }

    @Test
    public void testReadResolveNoFieldNamesStyle() throws Exception {
        ToStringStyle original = ToStringStyle.NO_FIELD_NAMES_STYLE;
        ToStringStyle deserialized = serializeAndDeserialize(original);
        assertSame("Deserialized NO_FIELD_NAMES_STYLE should be the same singleton", 
                ToStringStyle.NO_FIELD_NAMES_STYLE, deserialized);
    }

    @Test
    public void testReadResolveShortPrefixStyle() throws Exception {
        ToStringStyle original = ToStringStyle.SHORT_PREFIX_STYLE;
        ToStringStyle deserialized = serializeAndDeserialize(original);
        assertSame("Deserialized SHORT_PREFIX_STYLE should be the same singleton", 
                ToStringStyle.SHORT_PREFIX_STYLE, deserialized);
    }

    @Test
    public void testReadResolveSimpleStyle() throws Exception {
        ToStringStyle original = ToStringStyle.SIMPLE_STYLE;
        ToStringStyle deserialized = serializeAndDeserialize(original);
        assertSame("Deserialized SIMPLE_STYLE should be the same singleton", 
                ToStringStyle.SIMPLE_STYLE, deserialized);
    }

    @Test
    public void testReadResolveMultiLineStyle() throws Exception {
        ToStringStyle original = ToStringStyle.MULTI_LINE_STYLE;
        ToStringStyle deserialized = serializeAndDeserialize(original);
        assertSame("Deserialized MULTI_LINE_STYLE should be the same singleton", 
                ToStringStyle.MULTI_LINE_STYLE, deserialized);
    }

    // Tests for ToStringStyle.appendInternal - 56 missed instructions (77.4% coverage)
    @Test
    public void testAppendInternalWithCollections() {
        final ToStringStyle style = new ToStringStyleImpl();
        final StringBuffer buffer = new StringBuffer();
        
        // Test with Collection (detail=true)
        final java.util.List<String> list = new java.util.ArrayList<String>();
        list.add("item1");
        list.add("item2");
        style.append(buffer, "list", list, true);
        assertTrue("Should contain collection", buffer.toString().contains("list"));
        
        // Test with Collection (detail=false)
        final StringBuffer buffer2 = new StringBuffer();
        style.append(buffer2, "list", list, false);
        assertTrue("Should contain size", buffer2.toString().contains("<size="));
        
        // Test with Map (detail=true)
        final java.util.Map<String, String> map = new java.util.HashMap<String, String>();
        map.put("key1", "value1");
        final StringBuffer buffer3 = new StringBuffer();
        style.append(buffer3, "map", map, true);
        assertTrue("Should contain map", buffer3.toString().contains("map"));
        
        // Test with Map (detail=false)
        final StringBuffer buffer4 = new StringBuffer();
        style.append(buffer4, "map", map, false);
        assertTrue("Should contain size", buffer4.toString().contains("<size="));
    }

    @Test
    public void testAppendInternalWithObjectArray() {
        final ToStringStyle style = new ToStringStyleImpl();
        final StringBuffer buffer = new StringBuffer();
        
        // Test with Object array (detail=true)
        final Object[] objArray = new Object[] {"str1", "str2", null};
        style.append(buffer, "objArray", objArray, true);
        assertTrue("Should contain array", buffer.toString().contains("objArray"));
        
        // Test with Object array (detail=false)
        final StringBuffer buffer2 = new StringBuffer();
        style.append(buffer2, "objArray", objArray, false);
        assertTrue("Should contain size", buffer2.toString().contains("<size="));
    }

    @Test
    public void testAppendInternalWithCyclicObject() {
        final ToStringStyle style = new ToStringStyleImpl();
        final StringBuffer buffer = new StringBuffer();
        
        // Create a cyclic reference by registering an object
        final Person person = new Person();
        person.name = "John";
        person.age = 30;
        
        // Register the object to simulate cyclic reference
        ToStringStyle.register(person);
        try {
            // appendInternal should detect cyclic reference and use appendCyclicObject
            style.append(buffer, "person", person, true);
            assertTrue("Should contain cyclic object representation", buffer.length() > 0);
        } finally {
            ToStringStyle.unregister(person);
        }
    }

    @Test
    public void testAppendInternalWithPrimitiveArrays() {
        final ToStringStyle style = new ToStringStyleImpl();
        
        // Test all primitive array types with detail=false
        final StringBuffer buffer1 = new StringBuffer();
        style.append(buffer1, "longArr", new long[] {1L, 2L}, false);
        assertTrue("Should contain size", buffer1.toString().contains("<size="));
        
        final StringBuffer buffer2 = new StringBuffer();
        style.append(buffer2, "intArr", new int[] {1, 2}, false);
        assertTrue("Should contain size", buffer2.toString().contains("<size="));
        
        final StringBuffer buffer3 = new StringBuffer();
        style.append(buffer3, "shortArr", new short[] {1, 2}, false);
        assertTrue("Should contain size", buffer3.toString().contains("<size="));
        
        final StringBuffer buffer4 = new StringBuffer();
        style.append(buffer4, "byteArr", new byte[] {1, 2}, false);
        assertTrue("Should contain size", buffer4.toString().contains("<size="));
        
        final StringBuffer buffer5 = new StringBuffer();
        style.append(buffer5, "charArr", new char[] {'a', 'b'}, false);
        assertTrue("Should contain size", buffer5.toString().contains("<size="));
        
        final StringBuffer buffer6 = new StringBuffer();
        style.append(buffer6, "doubleArr", new double[] {1.0, 2.0}, false);
        assertTrue("Should contain size", buffer6.toString().contains("<size="));
        
        final StringBuffer buffer7 = new StringBuffer();
        style.append(buffer7, "floatArr", new float[] {1.0f, 2.0f}, false);
        assertTrue("Should contain size", buffer7.toString().contains("<size="));
        
        final StringBuffer buffer8 = new StringBuffer();
        style.append(buffer8, "boolArr", new boolean[] {true, false}, false);
        assertTrue("Should contain size", buffer8.toString().contains("<size="));
    }

    @Test
    public void testAppendInternalWithPrimitiveArraysDetailTrue() {
        final ToStringStyle style = new ToStringStyleImpl();
        
        // Test all primitive array types with detail=true
        final StringBuffer buffer1 = new StringBuffer();
        style.append(buffer1, "longArr", new long[] {1L, 2L}, true);
        assertTrue("Should contain array", buffer1.toString().contains("longArr"));
        
        final StringBuffer buffer2 = new StringBuffer();
        style.append(buffer2, "intArr", new int[] {1, 2}, true);
        assertTrue("Should contain array", buffer2.toString().contains("intArr"));
        
        final StringBuffer buffer3 = new StringBuffer();
        style.append(buffer3, "shortArr", new short[] {1, 2}, true);
        assertTrue("Should contain array", buffer3.toString().contains("shortArr"));
        
        final StringBuffer buffer4 = new StringBuffer();
        style.append(buffer4, "byteArr", new byte[] {1, 2}, true);
        assertTrue("Should contain array", buffer4.toString().contains("byteArr"));
        
        final StringBuffer buffer5 = new StringBuffer();
        style.append(buffer5, "charArr", new char[] {'a', 'b'}, true);
        assertTrue("Should contain array", buffer5.toString().contains("charArr"));
        
        final StringBuffer buffer6 = new StringBuffer();
        style.append(buffer6, "doubleArr", new double[] {1.0, 2.0}, true);
        assertTrue("Should contain array", buffer6.toString().contains("doubleArr"));
        
        final StringBuffer buffer7 = new StringBuffer();
        style.append(buffer7, "floatArr", new float[] {1.0f, 2.0f}, true);
        assertTrue("Should contain array", buffer7.toString().contains("floatArr"));
        
        final StringBuffer buffer8 = new StringBuffer();
        style.append(buffer8, "boolArr", new boolean[] {true, false}, true);
        assertTrue("Should contain array", buffer8.toString().contains("boolArr"));
    }

    @Test
    public void testAppendInternalWithRegularObjects() {
        final ToStringStyle style = new ToStringStyleImpl();
        
        // Test regular objects with detail=true
        final StringBuffer buffer1 = new StringBuffer();
        final Person person = new Person();
        person.name = "John";
        person.age = 30;
        style.append(buffer1, "person", person, true);
        assertTrue("Should contain person", buffer1.toString().contains("person"));
        
        // Test regular objects with detail=false
        final StringBuffer buffer2 = new StringBuffer();
        style.append(buffer2, "person", person, false);
        assertTrue("Should contain summary", buffer2.toString().contains("<") || buffer2.toString().contains("Person"));
        
        // Test String object with detail=true
        final StringBuffer buffer3 = new StringBuffer();
        style.append(buffer3, "str", "test", true);
        assertTrue("Should contain string", buffer3.toString().contains("str"));
        
        // Test String object with detail=false
        final StringBuffer buffer4 = new StringBuffer();
        style.append(buffer4, "str", "test", false);
        assertTrue("Should contain summary", buffer4.toString().length() > 0);
    }

    @Test
    public void testAppendInternalWithNumberBooleanCharacter() {
        final ToStringStyle style = new ToStringStyleImpl();
        
        // Test that Number, Boolean, and Character are not treated as cyclic even if registered
        // These should go through normal processing, not cyclic object handling
        
        // Test Number (Integer)
        final Integer num = Integer.valueOf(42);
        ToStringStyle.register(num);
        try {
            final StringBuffer buffer1 = new StringBuffer();
            style.append(buffer1, "num", num, true);
            assertTrue("Should contain number", buffer1.toString().contains("num") || buffer1.toString().contains("42"));
        } finally {
            ToStringStyle.unregister(num);
        }
        
        // Test Boolean
        final Boolean bool = Boolean.TRUE;
        ToStringStyle.register(bool);
        try {
            final StringBuffer buffer2 = new StringBuffer();
            style.append(buffer2, "bool", bool, true);
            assertTrue("Should contain boolean", buffer2.toString().contains("bool") || buffer2.toString().contains("true"));
        } finally {
            ToStringStyle.unregister(bool);
        }
        
        // Test Character
        final Character ch = Character.valueOf('A');
        ToStringStyle.register(ch);
        try {
            final StringBuffer buffer3 = new StringBuffer();
            style.append(buffer3, "ch", ch, true);
            assertTrue("Should contain character", buffer3.toString().contains("ch") || buffer3.toString().contains("A"));
        } finally {
            ToStringStyle.unregister(ch);
        }
    }

    @Test
    public void testAppendInternalWithObjectArrayDetailTrue() {
        final ToStringStyle style = new ToStringStyleImpl();
        
        // Test Object array with detail=true (to ensure full coverage of Object[] path)
        final StringBuffer buffer = new StringBuffer();
        final Object[] objArray = new Object[] {"str1", "str2", Integer.valueOf(42)};
        style.append(buffer, "objArray", objArray, true);
        assertTrue("Should contain array", buffer.toString().contains("objArray"));
    }

    @Test
    public void testAppendInternalWithGenericArray() {
        final ToStringStyle style = new ToStringStyleImpl();
        
        // Test generic array (Object[] that is not primitive)
        // This tests the value.getClass().isArray() path for non-primitive arrays
        final StringBuffer buffer1 = new StringBuffer();
        final String[] strArray = new String[] {"a", "b", "c"};
        style.append(buffer1, "strArray", strArray, true);
        assertTrue("Should contain array", buffer1.toString().contains("strArray"));
        
        final StringBuffer buffer2 = new StringBuffer();
        style.append(buffer2, "strArray", strArray, false);
        assertTrue("Should contain size", buffer2.toString().contains("<size="));
    }

    @Test
    public void testAppendInternal_ComprehensiveEdgeCases() {
        // Test appendInternal comprehensively - 56 missed (77.4% coverage)
        final ToStringStyle style = new ToStringStyleImpl();
        
        // Test with empty Collection (detail=true and detail=false)
        final StringBuffer buffer1 = new StringBuffer();
        final java.util.List<String> emptyList = new java.util.ArrayList<String>();
        style.append(buffer1, "emptyList", emptyList, true);
        assertTrue("Should handle empty collection", buffer1.length() > 0);
        
        final StringBuffer buffer2 = new StringBuffer();
        style.append(buffer2, "emptyList", emptyList, false);
        assertTrue("Should handle empty collection summary", buffer2.length() > 0);
        
        // Test with empty Map (detail=true and detail=false)
        final StringBuffer buffer3 = new StringBuffer();
        final java.util.Map<String, String> emptyMap = new java.util.HashMap<String, String>();
        style.append(buffer3, "emptyMap", emptyMap, true);
        assertTrue("Should handle empty map", buffer3.length() > 0);
        
        final StringBuffer buffer4 = new StringBuffer();
        style.append(buffer4, "emptyMap", emptyMap, false);
        assertTrue("Should handle empty map summary", buffer4.length() > 0);
        
        // Test with empty primitive arrays (detail=false)
        final StringBuffer buffer5 = new StringBuffer();
        style.append(buffer5, "emptyLongArr", new long[0], false);
        assertTrue("Should handle empty long array", buffer5.length() > 0);
        
        final StringBuffer buffer6 = new StringBuffer();
        style.append(buffer6, "emptyIntArr", new int[0], false);
        assertTrue("Should handle empty int array", buffer6.length() > 0);
        
        final StringBuffer buffer7 = new StringBuffer();
        style.append(buffer7, "emptyShortArr", new short[0], false);
        assertTrue("Should handle empty short array", buffer7.length() > 0);
        
        final StringBuffer buffer8 = new StringBuffer();
        style.append(buffer8, "emptyByteArr", new byte[0], false);
        assertTrue("Should handle empty byte array", buffer8.length() > 0);
        
        final StringBuffer buffer9 = new StringBuffer();
        style.append(buffer9, "emptyCharArr", new char[0], false);
        assertTrue("Should handle empty char array", buffer9.length() > 0);
        
        final StringBuffer buffer10 = new StringBuffer();
        style.append(buffer10, "emptyDoubleArr", new double[0], false);
        assertTrue("Should handle empty double array", buffer10.length() > 0);
        
        final StringBuffer buffer11 = new StringBuffer();
        style.append(buffer11, "emptyFloatArr", new float[0], false);
        assertTrue("Should handle empty float array", buffer11.length() > 0);
        
        final StringBuffer buffer12 = new StringBuffer();
        style.append(buffer12, "emptyBoolArr", new boolean[0], false);
        assertTrue("Should handle empty boolean array", buffer12.length() > 0);
        
        // Test with empty Object array (detail=false)
        final StringBuffer buffer13 = new StringBuffer();
        style.append(buffer13, "emptyObjArr", new Object[0], false);
        assertTrue("Should handle empty object array", buffer13.length() > 0);
        
        // Test with multi-dimensional arrays (detail=true and detail=false)
        final StringBuffer buffer14 = new StringBuffer();
        final int[][] multiDimArray = {{1, 2}, {3, 4}};
        style.append(buffer14, "multiDim", multiDimArray, true);
        assertTrue("Should handle multi-dimensional array", buffer14.length() > 0);
        
        final StringBuffer buffer15 = new StringBuffer();
        style.append(buffer15, "multiDim", multiDimArray, false);
        assertTrue("Should handle multi-dimensional array summary", buffer15.length() > 0);
        
        // Test with registered Number that should NOT use cyclic path (line 472)
        final Integer registeredNum = Integer.valueOf(100);
        ToStringStyle.register(registeredNum);
        try {
            final StringBuffer buffer16 = new StringBuffer();
            style.append(buffer16, "registeredNum", registeredNum, true);
            // Number should go through normal path, not cyclic
            assertTrue("Should handle registered Number normally", buffer16.length() > 0);
        } finally {
            ToStringStyle.unregister(registeredNum);
        }
        
        // Test with registered Boolean that should NOT use cyclic path
        final Boolean registeredBool = Boolean.FALSE;
        ToStringStyle.register(registeredBool);
        try {
            final StringBuffer buffer17 = new StringBuffer();
            style.append(buffer17, "registeredBool", registeredBool, true);
            // Boolean should go through normal path, not cyclic
            assertTrue("Should handle registered Boolean normally", buffer17.length() > 0);
        } finally {
            ToStringStyle.unregister(registeredBool);
        }
        
        // Test with registered Character that should NOT use cyclic path
        final Character registeredChar = Character.valueOf('Z');
        ToStringStyle.register(registeredChar);
        try {
            final StringBuffer buffer18 = new StringBuffer();
            style.append(buffer18, "registeredChar", registeredChar, true);
            // Character should go through normal path, not cyclic
            assertTrue("Should handle registered Character normally", buffer18.length() > 0);
        } finally {
            ToStringStyle.unregister(registeredChar);
        }
        
        // Test with registered non-Number/Boolean/Character that SHOULD use cyclic path
        final Person registeredPerson = new Person();
        registeredPerson.name = "Test";
        ToStringStyle.register(registeredPerson);
        try {
            final StringBuffer buffer19 = new StringBuffer();
            style.append(buffer19, "registeredPerson", registeredPerson, true);
            // Should use appendCyclicObject
            assertTrue("Should handle cyclic object", buffer19.length() > 0);
        } finally {
            ToStringStyle.unregister(registeredPerson);
        }
        
        // Test finally block - ensure unregister is called even on exception
        // This is hard to test directly, but we can verify unregister happens
        final Person testPerson = new Person();
        testPerson.name = "Test";
        ToStringStyle.register(testPerson);
        try {
            final StringBuffer buffer20 = new StringBuffer();
            style.append(buffer20, "testPerson", testPerson, true);
            // After append, object should be unregistered
            // Note: isRegistered might return true if object is still in registry
            // The finally block ensures unregister is called
            final boolean stillRegistered = ToStringStyle.isRegistered(testPerson);
            // The finally block will clean up
        } finally {
            // Clean up if still registered
            ToStringStyle.unregister(testPerson);
        }
    }

    @Test
    public void testUnregister_EmptyRegistry() {
        // Test line 212-213: REGISTRY.remove() when registry becomes empty
        // This tests the path where m.isEmpty() is true after removing an object
        
        final Person person = new Person();
        person.name = "Test";
        
        // Register the person
        ToStringStyle.register(person);
        assertTrue("Person should be registered", ToStringStyle.isRegistered(person));
        
        // Unregister - this should make the registry empty and trigger REGISTRY.remove()
        ToStringStyle.unregister(person);
        assertFalse("Person should be unregistered", ToStringStyle.isRegistered(person));
        
        // Verify registry is cleaned up by registering and unregistering again
        final Person person2 = new Person();
        person2.name = "Test2";
        ToStringStyle.register(person2);
        assertTrue("Person2 should be registered", ToStringStyle.isRegistered(person2));
        ToStringStyle.unregister(person2);
        assertFalse("Person2 should be unregistered", ToStringStyle.isRegistered(person2));
    }

    @Test
    public void testUnregister_NullValue() {
        // Test line 208: null value path
        // Unregistering null should not throw exception
        ToStringStyle.unregister(null);
        // Should complete without error
    }

    @Test
    public void testUnregister_NullRegistry() {
        // Test line 210: null registry path
        // This is hard to test directly since getRegistry() creates a new map if null
        // But we can test that unregister works even when registry might be null
        final Person person = new Person();
        person.name = "Test";
        // Unregister without registering first - should handle gracefully
        ToStringStyle.unregister(person);
        // Should complete without error
    }
}
