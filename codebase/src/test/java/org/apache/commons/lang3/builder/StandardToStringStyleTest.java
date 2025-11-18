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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.builder.ToStringStyleTest.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests {@link org.apache.commons.lang3.builder.ToStringStyle}.
 *
 * @version $Id$
 */
public class StandardToStringStyleTest {

    private final Integer base = Integer.valueOf(5);
    private final String baseStr = "Integer";
    
    private static final StandardToStringStyle STYLE = new StandardToStringStyle();
    
    static {
        STYLE.setUseShortClassName(true);
        STYLE.setUseIdentityHashCode(false);
        STYLE.setArrayStart("[");
        STYLE.setArraySeparator(", ");
        STYLE.setArrayEnd("]");
        STYLE.setNullText("%NULL%");
        STYLE.setSizeStartText("%SIZE=");
        STYLE.setSizeEndText("%");
        STYLE.setSummaryObjectStartText("%");
        STYLE.setSummaryObjectEndText("%");
    }
    
    @Before
    public void setUp() throws Exception {
        ToStringBuilder.setDefaultStyle(STYLE);
    }

    @After
    public void tearDown() throws Exception {
        ToStringBuilder.setDefaultStyle(ToStringStyle.DEFAULT_STYLE);
    }

    //----------------------------------------------------------------
    
    @Test
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

    @Test
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).appendSuper("Integer@8888[%NULL%]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[%NULL%,a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[%NULL%]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }
    
    @Test
    public void testObject() {
        final Integer i3 = Integer.valueOf(3);
        final Integer i4 = Integer.valueOf(4);
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[a=%NULL%]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[a=%Integer%]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[a=%SIZE=0%]", new ToStringBuilder(base).append("a", new ArrayList<Object>(), false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", new ArrayList<Object>(), true).toString());
        assertEquals(baseStr + "[a=%SIZE=0%]", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", new HashMap<Object, Object>(), true).toString());
        assertEquals(baseStr + "[a=%SIZE=0%]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

    @Test
    public void testPerson() {
        final Person p = new Person();
        p.name = "Suzy Queue";
        p.age = 19;
        p.smoker = false;
        final String pBaseStr = "ToStringStyleTest.Person";
        assertEquals(pBaseStr + "[name=Suzy Queue,age=19,smoker=false]", new ToStringBuilder(p).append("name", p.name).append("age", p.age).append("smoker", p.smoker).toString());
    }

    @Test
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

    @Test
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[[%NULL%, 5, [3, 6]]]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[[%NULL%, 5, [3, 6]]]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) array).toString());
    }

    @Test
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[[1, 2, -3, 4]]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[[1, 2, -3, 4]]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) array).toString());
    }

    @Test
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[[[1, 2], %NULL%, [5]]]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[[[1, 2], %NULL%, [5]]]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) array).toString());
    }

    // Tests for StandardToStringStyle setter methods - these have 0% coverage
    @Test
    public void testSetUseClassName() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setUseClassName(true);
        assertEquals(true, style.isUseClassName());
        style.setUseClassName(false);
        assertEquals(false, style.isUseClassName());
    }

    @Test
    public void testSetUseFieldNames() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setUseFieldNames(true);
        assertEquals(true, style.isUseFieldNames());
        style.setUseFieldNames(false);
        assertEquals(false, style.isUseFieldNames());
    }

    @Test
    public void testSetDefaultFullDetail() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setDefaultFullDetail(true);
        assertEquals(true, style.isDefaultFullDetail());
        style.setDefaultFullDetail(false);
        assertEquals(false, style.isDefaultFullDetail());
    }

    @Test
    public void testSetArrayContentDetail() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setArrayContentDetail(true);
        assertEquals(true, style.isArrayContentDetail());
        style.setArrayContentDetail(false);
        assertEquals(false, style.isArrayContentDetail());
    }

    @Test
    public void testSetContentStart() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setContentStart("[");
        assertEquals("[", style.getContentStart());
        style.setContentStart(null);
        assertEquals("", style.getContentStart()); // null is converted to empty string
    }

    @Test
    public void testSetContentEnd() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setContentEnd("]");
        assertEquals("]", style.getContentEnd());
        style.setContentEnd(null);
        assertEquals("", style.getContentEnd()); // null is converted to empty string
    }

    @Test
    public void testSetFieldNameValueSeparator() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setFieldNameValueSeparator("=");
        assertEquals("=", style.getFieldNameValueSeparator());
        style.setFieldNameValueSeparator(null);
        assertEquals("", style.getFieldNameValueSeparator()); // null is converted to empty string
    }

    @Test
    public void testSetFieldSeparator() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setFieldSeparator(",");
        assertEquals(",", style.getFieldSeparator());
        style.setFieldSeparator(null);
        assertEquals("", style.getFieldSeparator()); // null is converted to empty string
    }

    @Test
    public void testSetFieldSeparatorAtStart() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setFieldSeparatorAtStart(true);
        assertEquals(true, style.isFieldSeparatorAtStart());
        style.setFieldSeparatorAtStart(false);
        assertEquals(false, style.isFieldSeparatorAtStart());
    }

    @Test
    public void testSetFieldSeparatorAtEnd() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setFieldSeparatorAtEnd(true);
        assertEquals(true, style.isFieldSeparatorAtEnd());
        style.setFieldSeparatorAtEnd(false);
        assertEquals(false, style.isFieldSeparatorAtEnd());
    }

    @Test
    public void testSetUseShortClassName() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setUseShortClassName(true);
        assertEquals(true, style.isUseShortClassName());
        style.setUseShortClassName(false);
        assertEquals(false, style.isUseShortClassName());
    }

    @Test
    public void testSetUseIdentityHashCode() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setUseIdentityHashCode(true);
        assertEquals(true, style.isUseIdentityHashCode());
        style.setUseIdentityHashCode(false);
        assertEquals(false, style.isUseIdentityHashCode());
    }

    @Test
    public void testSetArrayStart() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setArrayStart("{");
        assertEquals("{", style.getArrayStart());
        style.setArrayStart(null);
        assertEquals("", style.getArrayStart()); // null is converted to empty string
    }

    @Test
    public void testSetArrayEnd() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setArrayEnd("}");
        assertEquals("}", style.getArrayEnd());
        style.setArrayEnd(null);
        assertEquals("", style.getArrayEnd()); // null is converted to empty string
    }

    @Test
    public void testSetArraySeparator() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setArraySeparator(",");
        assertEquals(",", style.getArraySeparator());
        style.setArraySeparator(null);
        assertEquals("", style.getArraySeparator()); // null is converted to empty string
    }

    @Test
    public void testSetNullText() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setNullText("<null>");
        assertEquals("<null>", style.getNullText());
        style.setNullText(null);
        assertEquals("", style.getNullText()); // null is converted to empty string
    }

    @Test
    public void testSetSizeStartText() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setSizeStartText("<size=");
        assertEquals("<size=", style.getSizeStartText());
        style.setSizeStartText(null);
        assertEquals("", style.getSizeStartText()); // null is converted to empty string
    }

    @Test
    public void testSetSizeEndText() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setSizeEndText(">");
        assertEquals(">", style.getSizeEndText());
        style.setSizeEndText(null);
        assertEquals("", style.getSizeEndText()); // null is converted to empty string
    }

    @Test
    public void testSetSummaryObjectStartText() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setSummaryObjectStartText("<");
        assertEquals("<", style.getSummaryObjectStartText());
        style.setSummaryObjectStartText(null);
        assertEquals("", style.getSummaryObjectStartText()); // null is converted to empty string
    }

    @Test
    public void testSetSummaryObjectEndText() {
        final StandardToStringStyle style = new StandardToStringStyle();
        style.setSummaryObjectEndText(">");
        assertEquals(">", style.getSummaryObjectEndText());
        style.setSummaryObjectEndText(null);
        assertEquals("", style.getSummaryObjectEndText()); // null is converted to empty string
    }

}
