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
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Test;

/**
 * Test class for {@link IDKey}.
 *
 * @version $Id$
 */
public class IDKeyTest {

    @Test
    public void testEqualsSameObject() {
        final Object obj = new Object();
        final IDKey key1 = new IDKey(obj);
        final IDKey key2 = new IDKey(obj);
        
        // Same object should be equal
        assertTrue("Same object should be equal", key1.equals(key1));
        assertTrue("Same object should be equal", key1.equals(key2));
    }

    @Test
    public void testEqualsDifferentType() {
        final Object obj = new Object();
        final IDKey key = new IDKey(obj);
        
        // Different type should not be equal
        assertFalse("Different type should not be equal", key.equals(obj));
        assertFalse("Different type should not be equal", key.equals("string"));
        assertFalse("Different type should not be equal", key.equals(null));
    }

    @Test
    public void testEqualsDifferentIdentityHashCode() {
        final Object obj1 = new Object();
        final Object obj2 = new Object();
        final IDKey key1 = new IDKey(obj1);
        final IDKey key2 = new IDKey(obj2);
        
        // Different objects with different identity hash codes should not be equal
        // (unless they happen to have the same identity hash code, which is rare)
        // Most of the time they will have different hash codes
        if (System.identityHashCode(obj1) != System.identityHashCode(obj2)) {
            assertFalse("Different identity hash codes should not be equal", key1.equals(key2));
        }
    }

    @Test
    public void testEqualsSameIdentityHashCodeDifferentObjects() {
        // Create two objects that might have the same identity hash code
        // This tests the disambiguation case where ids match but values don't
        // We'll try multiple pairs to increase chance of finding same identity hash code
        boolean foundSameHashCode = false;
        for (int i = 0; i < 1000 && !foundSameHashCode; i++) {
            final Object obj1 = new Object();
            final Object obj2 = new Object();
            final IDKey key1 = new IDKey(obj1);
            final IDKey key2 = new IDKey(obj2);
            
            // If they have the same identity hash code but different objects, equals should return false
            if (key1.hashCode() == key2.hashCode() && obj1 != obj2) {
                assertFalse("Same identity hash code but different objects should not be equal", 
                        key1.equals(key2));
                foundSameHashCode = true;
            }
        }
        
        // Also test the case where id != idKey.id to ensure that branch is covered
        final Object obj1 = new Object();
        final Object obj2 = new Object();
        final IDKey key1 = new IDKey(obj1);
        final IDKey key2 = new IDKey(obj2);
        
        // Most of the time they will have different hash codes
        if (key1.hashCode() != key2.hashCode()) {
            assertFalse("Different identity hash codes should not be equal", key1.equals(key2));
        }
    }

    @Test
    public void testHashCode() {
        final Object obj = new Object();
        final IDKey key = new IDKey(obj);
        
        // Hash code should be the identity hash code of the object
        assertEquals("Hash code should match identity hash code", 
                System.identityHashCode(obj), key.hashCode());
    }

    @Test
    public void testEqualsWithNull() {
        final Object obj = new Object();
        final IDKey key = new IDKey(obj);
        
        // Should handle null gracefully
        assertFalse("Should not equal null", key.equals(null));
    }

    @Test
    public void testEqualsSameIdDifferentValue() throws Exception {
        // Test case where id matches but value doesn't (to cover value == idKey.value returning false)
        // Use reflection to force the condition where id == idKey.id but value != idKey.value
        final Object obj1 = new Object();
        final Object obj2 = new Object();
        
        final IDKey key1 = new IDKey(obj1);
        final IDKey key2 = new IDKey(obj2);
        
        // Use reflection to set the id field to be the same in both keys
        // This forces the condition where id == idKey.id but value != idKey.value
        final java.lang.reflect.Field idField = IDKey.class.getDeclaredField("id");
        idField.setAccessible(true);
        final int id1 = (Integer) idField.get(key1);
        idField.set(key2, id1); // Set key2's id to match key1's id
        
        // Now key1 and key2 have the same id but different values
        // equals should return false because value != idKey.value
        assertFalse("Same id but different values should not be equal", key1.equals(key2));
        
        // Test with same object (same id and same value)
        final IDKey key3 = new IDKey(obj1);
        assertTrue("Same object should be equal", key1.equals(key3));
    }

}

