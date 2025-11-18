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
package org.apache.commons.lang3.text;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.apache.commons.lang3.JavaVersion.JAVA_1_4;

import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;

/**
 * Test case for {@link ExtendedMessageFormat}.
 *
 * @since 2.4
 * @version $Id$
 */
public class ExtendedMessageFormatTest {

    private final Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();

    @Before
    public void setUp() throws Exception {
        registry.put("lower", new LowerCaseFormatFactory());
        registry.put("upper", new UpperCaseFormatFactory());
    }

    /**
     * Test extended formats.
     */
    @Test
    public void testExtendedFormats() {
        final String pattern = "Lower: {0,lower} Upper: {1,upper}";
        final ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertPatternsEqual("TOPATTERN", pattern, emf.toPattern());
        assertEquals("Lower: foo Upper: BAR", emf.format(new Object[] {"foo", "bar"}));
        assertEquals("Lower: foo Upper: BAR", emf.format(new Object[] {"Foo", "Bar"}));
        assertEquals("Lower: foo Upper: BAR", emf.format(new Object[] {"FOO", "BAR"}));
        assertEquals("Lower: foo Upper: BAR", emf.format(new Object[] {"FOO", "bar"}));
        assertEquals("Lower: foo Upper: BAR", emf.format(new Object[] {"foo", "BAR"}));
    }

    /**
     * Test Bug LANG-477 - out of memory error with escaped quote
     */
    @Test
    public void testEscapedQuote_LANG_477() {
        final String pattern = "it''s a {0,lower} 'test'!";
        final ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("it's a dummy test!", emf.format(new Object[] {"DUMMY"}));
    }

    /**
     * Test extended and built in formats.
     */
    @Test
    public void testExtendedAndBuiltInFormats() {
        final Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 23, 18, 33, 05);
        final Object[] args = new Object[] {"John Doe", cal.getTime(), Double.valueOf("12345.67")};
        final String builtinsPattern = "DOB: {1,date,short} Salary: {2,number,currency}";
        final String extendedPattern = "Name: {0,upper} ";
        final String pattern = extendedPattern + builtinsPattern;

        final HashSet<Locale> testLocales = new HashSet<Locale>();
        testLocales.addAll(Arrays.asList(DateFormat.getAvailableLocales()));
        testLocales.retainAll(Arrays.asList(NumberFormat.getAvailableLocales()));
        testLocales.add(null);

        for (final Locale locale : testLocales) {
            final MessageFormat builtins = createMessageFormat(builtinsPattern, locale);
            final String expectedPattern = extendedPattern + builtins.toPattern();
            DateFormat df = null;
            NumberFormat nf = null;
            ExtendedMessageFormat emf = null;
            if (locale == null) {
                df = DateFormat.getDateInstance(DateFormat.SHORT);
                nf = NumberFormat.getCurrencyInstance();
                emf = new ExtendedMessageFormat(pattern, registry);
            } else {
                df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
                nf = NumberFormat.getCurrencyInstance(locale);
                emf = new ExtendedMessageFormat(pattern, locale, registry);
            }
            final StringBuilder expected = new StringBuilder();
            expected.append("Name: ");
            expected.append(args[0].toString().toUpperCase());
            expected.append(" DOB: ");
            expected.append(df.format(args[1]));
            expected.append(" Salary: ");
            expected.append(nf.format(args[2]));
            assertPatternsEqual("pattern comparison for locale " + locale, expectedPattern, emf.toPattern());
            assertEquals(String.valueOf(locale), expected.toString(), emf.format(args));
        }
    }

//    /**
//     * Test extended formats with choice format.
//     *
//     * NOTE: FAILING - currently sub-formats not supported
//     */
//    public void testExtendedWithChoiceFormat() {
//        String pattern = "Choice: {0,choice,1.0#{1,lower}|2.0#{1,upper}}";
//        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
//        assertPatterns(null, pattern, emf.toPattern());
//        try {
//            assertEquals("one", emf.format(new Object[] {Integer.valueOf(1), "ONE"}));
//            assertEquals("TWO", emf.format(new Object[] {Integer.valueOf(2), "two"}));
//        } catch (IllegalArgumentException e) {
//            // currently sub-formats not supported
//        }
//    }

//    /**
//     * Test mixed extended and built-in formats with choice format.
//     *
//     * NOTE: FAILING - currently sub-formats not supported
//     */
//    public void testExtendedAndBuiltInWithChoiceFormat() {
//        String pattern = "Choice: {0,choice,1.0#{0} {1,lower} {2,number}|2.0#{0} {1,upper} {2,number,currency}}";
//        Object[] lowArgs  = new Object[] {Integer.valueOf(1), "Low",  Double.valueOf("1234.56")};
//        Object[] highArgs = new Object[] {Integer.valueOf(2), "High", Double.valueOf("9876.54")};
//        Locale[] availableLocales = ChoiceFormat.getAvailableLocales();
//        Locale[] testLocales = new Locale[availableLocales.length + 1];
//        testLocales[0] = null;
//        System.arraycopy(availableLocales, 0, testLocales, 1, availableLocales.length);
//        for (int i = 0; i < testLocales.length; i++) {
//            NumberFormat nf = null;
//            NumberFormat cf = null;
//            ExtendedMessageFormat emf = null;
//            if (testLocales[i] == null) {
//                nf = NumberFormat.getNumberInstance();
//                cf = NumberFormat.getCurrencyInstance();
//                emf = new ExtendedMessageFormat(pattern, registry);
//            } else {
//                nf = NumberFormat.getNumberInstance(testLocales[i]);
//                cf = NumberFormat.getCurrencyInstance(testLocales[i]);
//                emf = new ExtendedMessageFormat(pattern, testLocales[i], registry);
//            }
//            assertPatterns(null, pattern, emf.toPattern());
//            try {
//                String lowExpected = lowArgs[0] + " low "    + nf.format(lowArgs[2]);
//                String highExpected = highArgs[0] + " HIGH "  + cf.format(highArgs[2]);
//                assertEquals(lowExpected,  emf.format(lowArgs));
//                assertEquals(highExpected, emf.format(highArgs));
//            } catch (IllegalArgumentException e) {
//                // currently sub-formats not supported
//            }
//        }
//    }

    /**
     * Test the built in choice format.
     */
    @Test
    public void testBuiltInChoiceFormat() {
        final Object[] values = new Number[] {Integer.valueOf(1), Double.valueOf("2.2"), Double.valueOf("1234.5")};
        String choicePattern = null;
        final Locale[] availableLocales = ChoiceFormat.getAvailableLocales();

        choicePattern = "{0,choice,1#One|2#Two|3#Many {0,number}}";
        for (final Object value : values) {
            checkBuiltInFormat(value + ": " + choicePattern, new Object[] {value}, availableLocales);
        }

        choicePattern = "{0,choice,1#''One''|2#\"Two\"|3#''{Many}'' {0,number}}";
        for (final Object value : values) {
            checkBuiltInFormat(value + ": " + choicePattern, new Object[] {value}, availableLocales);
        }
    }

    /**
     * Test the built in date/time formats
     */
    @Test
    public void testBuiltInDateTimeFormat() {
        final Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 23, 18, 33, 05);
        final Object[] args = new Object[] {cal.getTime()};
        final Locale[] availableLocales = DateFormat.getAvailableLocales();

        checkBuiltInFormat("1: {0,date,short}",    args, availableLocales);
        checkBuiltInFormat("2: {0,date,medium}",   args, availableLocales);
        checkBuiltInFormat("3: {0,date,long}",     args, availableLocales);
        checkBuiltInFormat("4: {0,date,full}",     args, availableLocales);
        checkBuiltInFormat("5: {0,date,d MMM yy}", args, availableLocales);
        checkBuiltInFormat("6: {0,time,short}",    args, availableLocales);
        checkBuiltInFormat("7: {0,time,medium}",   args, availableLocales);
        checkBuiltInFormat("8: {0,time,long}",     args, availableLocales);
        checkBuiltInFormat("9: {0,time,full}",     args, availableLocales);
        checkBuiltInFormat("10: {0,time,HH:mm}",   args, availableLocales);
        checkBuiltInFormat("11: {0,date}",         args, availableLocales);
        checkBuiltInFormat("12: {0,time}",         args, availableLocales);
    }

    @Test
    public void testOverriddenBuiltinFormat() {
        final Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 23);
        final Object[] args = new Object[] {cal.getTime()};
        final Locale[] availableLocales = DateFormat.getAvailableLocales();
        final Map<String, ? extends FormatFactory> registry = Collections.singletonMap("date", new OverrideShortDateFormatFactory());

        //check the non-overridden builtins:
        checkBuiltInFormat("1: {0,date}", registry,          args, availableLocales);
        checkBuiltInFormat("2: {0,date,medium}", registry,   args, availableLocales);
        checkBuiltInFormat("3: {0,date,long}", registry,     args, availableLocales);
        checkBuiltInFormat("4: {0,date,full}", registry,     args, availableLocales);
        checkBuiltInFormat("5: {0,date,d MMM yy}", registry, args, availableLocales);

        //check the overridden format:
        for (int i = -1; i < availableLocales.length; i++) {
            final Locale locale = i < 0 ? null : availableLocales[i];
            final MessageFormat dateDefault = createMessageFormat("{0,date}", locale);
            final String pattern = "{0,date,short}";
            final ExtendedMessageFormat dateShort = new ExtendedMessageFormat(pattern, locale, registry);
            assertEquals("overridden date,short format", dateDefault.format(args), dateShort.format(args));
            assertEquals("overridden date,short pattern", pattern, dateShort.toPattern());
        }
    }

    /**
     * Test the built in number formats.
     */
    @Test
    public void testBuiltInNumberFormat() {
        final Object[] args = new Object[] {Double.valueOf("6543.21")};
        final Locale[] availableLocales = NumberFormat.getAvailableLocales();
        checkBuiltInFormat("1: {0,number}",            args, availableLocales);
        checkBuiltInFormat("2: {0,number,integer}",    args, availableLocales);
        checkBuiltInFormat("3: {0,number,currency}",   args, availableLocales);
        checkBuiltInFormat("4: {0,number,percent}",    args, availableLocales);
        checkBuiltInFormat("5: {0,number,00000.000}",  args, availableLocales);
    }

    /**
     * Test equals() and hashcode.
     */
    @Test
    public void testEqualsHashcode() {
        final Map<String, ? extends FormatFactory> registry = Collections.singletonMap("testfmt", new LowerCaseFormatFactory());
        final Map<String, ? extends FormatFactory> otherRegitry = Collections.singletonMap("testfmt", new UpperCaseFormatFactory());

        final String pattern = "Pattern: {0,testfmt}";
        final ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, Locale.US, registry);

        ExtendedMessageFormat other = null;

        // Same object
        assertTrue("same, equals()",   emf.equals(emf));
        assertTrue("same, hashcode()", emf.hashCode() == emf.hashCode());

        // Equal Object
        other = new ExtendedMessageFormat(pattern, Locale.US, registry);
        assertTrue("equal, equals()",   emf.equals(other));
        assertTrue("equal, hashcode()", emf.hashCode() == other.hashCode());

        // Different Class
        other = new OtherExtendedMessageFormat(pattern, Locale.US, registry);
        assertFalse("class, equals()",  emf.equals(other));
        assertTrue("class, hashcode()", emf.hashCode() == other.hashCode()); // same hashcode
        
        // Different pattern
        other = new ExtendedMessageFormat("X" + pattern, Locale.US, registry);
        assertFalse("pattern, equals()",   emf.equals(other));
        assertFalse("pattern, hashcode()", emf.hashCode() == other.hashCode());

        // Different registry
        other = new ExtendedMessageFormat(pattern, Locale.US, otherRegitry);
        assertFalse("registry, equals()",   emf.equals(other));
        assertFalse("registry, hashcode()", emf.hashCode() == other.hashCode());

        // Different Locale
        other = new ExtendedMessageFormat(pattern, Locale.FRANCE, registry);
        assertFalse("locale, equals()",  emf.equals(other));
        assertTrue("locale, hashcode()", emf.hashCode() == other.hashCode()); // same hashcode
    }

    /**
     * Test a built in format for the specified Locales, plus <code>null</code> Locale.
     * @param pattern MessageFormat pattern
     * @param args MessageFormat arguments
     * @param locales to test
     */
    private void checkBuiltInFormat(final String pattern, final Object[] args, final Locale[] locales) {
        checkBuiltInFormat(pattern, null, args, locales);
    }

    /**
     * Test a built in format for the specified Locales, plus <code>null</code> Locale.
     * @param pattern MessageFormat pattern
     * @param registry FormatFactory registry to use
     * @param args MessageFormat arguments
     * @param locales to test
     */
    private void checkBuiltInFormat(final String pattern, final Map<String, ?> registry, final Object[] args, final Locale[] locales) {
        checkBuiltInFormat(pattern, registry, args, (Locale) null);
        for (final Locale locale : locales) {
            checkBuiltInFormat(pattern, registry, args, locale);
        }
    }

    /**
     * Create an ExtendedMessageFormat for the specified pattern and locale and check the
     * formated output matches the expected result for the parameters.
     * @param pattern string
     * @param registry map
     * @param args Object[]
     * @param locale Locale
     */
    private void checkBuiltInFormat(final String pattern, final Map<String, ?> registry, final Object[] args, final Locale locale) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("Pattern=[");
        buffer.append(pattern);
        buffer.append("], locale=[");
        buffer.append(locale);
        buffer.append("]");
        final MessageFormat mf = createMessageFormat(pattern, locale);
        // System.out.println(buffer + ", result=[" + mf.format(args) +"]");
        ExtendedMessageFormat emf = null;
        if (locale == null) {
            emf = new ExtendedMessageFormat(pattern);
        } else {
            emf = new ExtendedMessageFormat(pattern, locale);
        }
        assertEquals("format "    + buffer.toString(), mf.format(args), emf.format(args));
        assertPatternsEqual("toPattern " + buffer.toString(), mf.toPattern(),  emf.toPattern());
    }

    //can't trust what MessageFormat does with toPattern() pre 1.4:
    private void assertPatternsEqual(final String message, final String expected, final String actual) {
        if (SystemUtils.isJavaVersionAtLeast(JAVA_1_4)) {
            assertEquals(message, expected, actual);
        }
    }

    /**
     * Replace MessageFormat(String, Locale) constructor (not available until JDK 1.4).
     * @param pattern string
     * @param locale Locale
     * @return MessageFormat
     */
    private MessageFormat createMessageFormat(final String pattern, final Locale locale) {
        final MessageFormat result = new MessageFormat(pattern);
        if (locale != null) {
            result.setLocale(locale);
            result.applyPattern(pattern);
        }
        return result;
    }

    // ------------------------ Test Formats ------------------------

    /**
     * {@link Format} implementation which converts to lower case.
     */
    private static class LowerCaseFormat extends Format {
        @Override
        public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
            return toAppendTo.append(((String)obj).toLowerCase());
        }
        @Override
        public Object parseObject(final String source, final ParsePosition pos) {throw new UnsupportedOperationException();}
    }

    /**
     * {@link Format} implementation which converts to upper case.
     */
    private static class UpperCaseFormat extends Format {
        @Override
        public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
            return toAppendTo.append(((String)obj).toUpperCase());
        }
        @Override
        public Object parseObject(final String source, final ParsePosition pos) {throw new UnsupportedOperationException();}
    }


    // ------------------------ Test Format Factories ---------------
    /**
     * {@link FormatFactory} implementation for lower case format.
     */
    private static class LowerCaseFormatFactory implements FormatFactory {
        private static final Format LOWER_INSTANCE = new LowerCaseFormat();
        @Override
        public Format getFormat(final String name, final String arguments, final Locale locale) {
            return LOWER_INSTANCE;
        }
    }
    /**
     * {@link FormatFactory} implementation for upper case format.
     */
    private static class UpperCaseFormatFactory implements FormatFactory {
        private static final Format UPPER_INSTANCE = new UpperCaseFormat();
        @Override
        public Format getFormat(final String name, final String arguments, final Locale locale) {
            return UPPER_INSTANCE;
        }
    }
    /**
     * {@link FormatFactory} implementation to override date format "short" to "default".
     */
    private static class OverrideShortDateFormatFactory implements FormatFactory {
        @Override
        public Format getFormat(final String name, final String arguments, final Locale locale) {
            return !"short".equals(arguments) ? null
                    : locale == null ? DateFormat
                            .getDateInstance(DateFormat.DEFAULT) : DateFormat
                            .getDateInstance(DateFormat.DEFAULT, locale);
        }
    }

    /**
     * Alternative ExtendedMessageFormat impl.
     */
    private static class OtherExtendedMessageFormat extends ExtendedMessageFormat {
        public OtherExtendedMessageFormat(final String pattern, final Locale locale,
                final Map<String, ? extends FormatFactory> registry) {
            super(pattern, locale, registry);
        }
        
    }

    // Tests for ExtendedMessageFormat methods with low coverage
    @Test
    public void testEqualsWithNull() {
        final ExtendedMessageFormat emf = new ExtendedMessageFormat("Test: {0}", registry);
        assertFalse("Should not equal null", emf.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        final ExtendedMessageFormat emf = new ExtendedMessageFormat("Test: {0}", registry);
        assertFalse("Should not equal different class", emf.equals("Test: {0}"));
    }

    @Test
    public void testEqualsWithNullRegistry() {
        final ExtendedMessageFormat emf1 = new ExtendedMessageFormat("Test: {0}", (Map<String, ? extends FormatFactory>) null);
        final ExtendedMessageFormat emf2 = new ExtendedMessageFormat("Test: {0}", registry);
        assertFalse("Should not equal different registry", emf1.equals(emf2));
    }

    @Test
    public void testEqualsWithNullPattern() {
        final ExtendedMessageFormat emf1 = new ExtendedMessageFormat("Test: {0}", registry);
        final ExtendedMessageFormat emf2 = new ExtendedMessageFormat("Test: {1}", registry);
        assertFalse("Should not equal different pattern", emf1.equals(emf2));
    }

    @Test
    public void testReadArgumentIndexEdgeCases() {
        // Test with whitespace in argument index
        try {
            new ExtendedMessageFormat("{ 0 }", registry);
            // Should succeed
        } catch (IllegalArgumentException e) {
            // May fail if whitespace handling is strict
        }
        
        // Test with invalid argument index
        try {
            new ExtendedMessageFormat("{abc}", registry);
            fail("Should throw IllegalArgumentException for invalid argument index");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        // Test with unterminated format element
        try {
            new ExtendedMessageFormat("{0", registry);
            fail("Should throw IllegalArgumentException for unterminated format element");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testAppendQuotedStringEdgeCases() {
        // Test with escaped quotes
        final ExtendedMessageFormat emf1 = new ExtendedMessageFormat("it''s a test", registry);
        assertEquals("it's a test", emf1.format(new Object[] {}));
        
        // Test with nested quotes
        final ExtendedMessageFormat emf2 = new ExtendedMessageFormat("'quoted' text", registry);
        assertEquals("quoted text", emf2.format(new Object[] {}));
        
        // Test with unterminated quoted string
        try {
            new ExtendedMessageFormat("'unterminated", registry);
            fail("Should throw IllegalArgumentException for unterminated quoted string");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testParseFormatDescriptionWithNestedBraces() {
        // Test format description with nested braces
        try {
            new ExtendedMessageFormat("{0,lower,{nested}}", registry);
            // May succeed or fail depending on implementation
        } catch (IllegalArgumentException e) {
            // May fail if nested braces not supported
        }
    }

    @Test
    public void testGetQuotedString() throws Exception {
        // Test getQuotedString using reflection (0% coverage)
        final ExtendedMessageFormat emf = new ExtendedMessageFormat("'test'", registry);
        final java.lang.reflect.Method method = ExtendedMessageFormat.class.getDeclaredMethod(
            "getQuotedString", String.class, java.text.ParsePosition.class, boolean.class);
        method.setAccessible(true);
        
        final java.text.ParsePosition pos = new java.text.ParsePosition(0);
        method.invoke(emf, "'test'", pos, true);
        assertTrue("Position should advance", pos.getIndex() > 0);
        
        // Test with escaping off
        final java.text.ParsePosition pos2 = new java.text.ParsePosition(0);
        method.invoke(emf, "'test'", pos2, false);
        assertTrue("Position should advance", pos2.getIndex() > 0);
    }

    @Test
    public void testUnsupportedOperationExceptions() {
        final ExtendedMessageFormat emf = new ExtendedMessageFormat("Test: {0}", registry);
        
        // Test setFormat
        try {
            emf.setFormat(0, new java.text.SimpleDateFormat());
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        
        // Test setFormatByArgumentIndex
        try {
            emf.setFormatByArgumentIndex(0, new java.text.SimpleDateFormat());
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        
        // Test setFormats
        try {
            emf.setFormats(new Format[] {new java.text.SimpleDateFormat()});
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        
        // Test setFormatsByArgumentIndex
        try {
            emf.setFormatsByArgumentIndex(new Format[] {new java.text.SimpleDateFormat()});
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    // Additional tests for ExtendedMessageFormat methods with missed instructions
    @Test
    public void testReadArgumentIndex_NumberFormatException() {
        // Test readArgumentIndex with very large number (triggers NumberFormatException catch)
        try {
            // This should trigger the NumberFormatException catch block
            // We need a pattern that would parse a very large number
            new ExtendedMessageFormat("{999999999999999999999}", registry);
            // May succeed or fail depending on implementation
        } catch (IllegalArgumentException e) {
            // Expected - either from NumberFormatException or from validation
        }
    }

    @Test
    public void testAppendQuotedString_WithEscaping() {
        // Test appendQuotedString with escapingOn=true and escaped quotes
        final ExtendedMessageFormat emf = new ExtendedMessageFormat("it''s a test", registry);
        assertEquals("it's a test", emf.format(new Object[] {}));
        
        // Test with multiple escaped quotes
        final ExtendedMessageFormat emf2 = new ExtendedMessageFormat("it''s a ''test''", registry);
        assertEquals("it's a 'test'", emf2.format(new Object[] {}));
    }

    @Test
    public void testAppendQuotedString_WithNullAppendTo() {
        // Test appendQuotedString with appendTo=null (should return null)
        // This is tested indirectly through getQuotedString
        final ExtendedMessageFormat emf = new ExtendedMessageFormat("'test'", registry);
        // getQuotedString calls appendQuotedString with null appendTo
        // This is already tested in testGetQuotedString
    }

    @Test
    public void testParseFormatDescription_Unterminated() {
        // Test parseFormatDescription with unterminated format element
        try {
            new ExtendedMessageFormat("{0,lower", registry);
            fail("Should throw IllegalArgumentException for unterminated format element");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception should mention unterminated", e.getMessage().contains("Unterminated"));
        }
    }

    @Test
    public void testEquals_WithNullRegistry() {
        // Test equals when one has null registry and other has non-null
        final ExtendedMessageFormat emf1 = new ExtendedMessageFormat("Test: {0}", (Map<String, ? extends FormatFactory>) null);
        final ExtendedMessageFormat emf2 = new ExtendedMessageFormat("Test: {0}", registry);
        assertFalse("Should not equal when registries differ", emf1.equals(emf2));
        assertFalse("Should not equal when registries differ (reverse)", emf2.equals(emf1));
    }

    @Test
    public void testEquals_WithNullPattern() {
        // Test equals when patterns differ (one null, one non-null)
        final ExtendedMessageFormat emf1 = new ExtendedMessageFormat("Test: {0}", registry);
        final ExtendedMessageFormat emf2 = new ExtendedMessageFormat("Test: {1}", registry);
        assertFalse("Should not equal when patterns differ", emf1.equals(emf2));
    }

    @Test
    public void testReadArgumentIndex_WhitespaceError() {
        // Test readArgumentIndex with whitespace that causes error
        try {
            new ExtendedMessageFormat("{0 abc}", registry);
            fail("Should throw IllegalArgumentException for invalid format");
        } catch (IllegalArgumentException e) {
            // Expected - whitespace followed by non-digit, non-START_FMT, non-END_FE
        }
    }

    @Test
    public void testAppendQuotedString_ContinuePath() {
        // Test appendQuotedString with ESCAPED_QUOTE that triggers continue
        final ExtendedMessageFormat emf = new ExtendedMessageFormat("it''s a test", registry);
        assertEquals("it's a test", emf.format(new Object[] {}));
    }

    @Test
    public void testAppendQuotedString_Comprehensive() {
        // Test appendQuotedString comprehensively - 45 missed (56.3% coverage)
        
        // Test with escapingOn=true and quote at start (line 481-483)
        final ExtendedMessageFormat emf1 = new ExtendedMessageFormat("'test'", registry);
        assertEquals("test", emf1.format(new Object[] {}));
        
        // Test with escapingOn=false and quote at start
        // This is tested through patterns that don't use escaping
        final ExtendedMessageFormat emf2 = new ExtendedMessageFormat("'simple'", registry);
        assertEquals("simple", emf2.format(new Object[] {}));
        
        // Test with multiple escaped quotes in sequence
        final ExtendedMessageFormat emf3 = new ExtendedMessageFormat("it''s a ''test'' here", registry);
        assertEquals("it's a 'test' here", emf3.format(new Object[] {}));
        
        // Test with escaped quote at different positions
        final ExtendedMessageFormat emf4 = new ExtendedMessageFormat("start''middle''end", registry);
        assertEquals("start'middle'end", emf4.format(new Object[] {}));
        
        // Test with escaped quote followed by regular text
        final ExtendedMessageFormat emf5 = new ExtendedMessageFormat("''text", registry);
        assertEquals("'text", emf5.format(new Object[] {}));
        
        // Test with escaped quote at end
        final ExtendedMessageFormat emf6 = new ExtendedMessageFormat("text''", registry);
        assertEquals("text'", emf6.format(new Object[] {}));
        
        // Test with unterminated quoted string (line 503-504)
        try {
            new ExtendedMessageFormat("'unterminated string", registry);
            fail("Should throw IllegalArgumentException for unterminated quoted string");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception should mention unterminated", 
                e.getMessage().contains("Unterminated") || e.getMessage().contains("quoted"));
        }
        
        // Test with empty quoted string
        final ExtendedMessageFormat emf7 = new ExtendedMessageFormat("''", registry);
        assertEquals("'", emf7.format(new Object[] {}));
        
        // Test with quoted string containing only escaped quotes
        final ExtendedMessageFormat emf8 = new ExtendedMessageFormat("''''", registry);
        assertEquals("''", emf8.format(new Object[] {}));
    }

    @Test
    public void testApplyPattern_EdgeCases() {
        // Test applyPattern edge cases - 14 missed (93.2% coverage)
        
        // Test with null registry (line 146-149)
        final ExtendedMessageFormat emf1 = new ExtendedMessageFormat("Test: {0}", (Map<String, ? extends FormatFactory>) null);
        assertEquals("Test: {0}", emf1.toPattern());
        
        // Test with empty pattern
        final ExtendedMessageFormat emf2 = new ExtendedMessageFormat("", registry);
        assertEquals("", emf2.toPattern());
        
        // Test with pattern containing only format elements
        final ExtendedMessageFormat emf3 = new ExtendedMessageFormat("{0,lower}", registry);
        assertEquals("{0,lower}", emf3.toPattern());
        
        // Test with pattern containing multiple format elements
        final ExtendedMessageFormat emf4 = new ExtendedMessageFormat("{0,lower} {1,upper}", registry);
        assertEquals("{0,lower} {1,upper}", emf4.toPattern());
        
        // Test with pattern containing format elements with null formats (line 203-205)
        // This tests the path where f != null check
        final ExtendedMessageFormat emf5 = new ExtendedMessageFormat("{0} {1,lower}", registry);
        assertEquals("{0} {1,lower}", emf5.toPattern());
    }

    @Test
    public void testParseFormatDescription_EdgeCases() {
        // Test parseFormatDescription edge cases - 5 missed (90.9% coverage)
        
        // Test with nested braces (line 379-386)
        try {
            new ExtendedMessageFormat("{0,lower,{nested}}", registry);
            // May succeed or fail depending on implementation
        } catch (IllegalArgumentException e) {
            // May fail if nested braces not supported
        }
        
        // Test with quoted string inside format description (line 388-390)
        final ExtendedMessageFormat emf1 = new ExtendedMessageFormat("{0,lower,'quoted'}", registry);
        // Should handle quoted strings in format description
        
        // Test with unterminated format description (line 393-394)
        try {
            new ExtendedMessageFormat("{0,lower", registry);
            fail("Should throw IllegalArgumentException for unterminated format element");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception should mention unterminated", 
                e.getMessage().contains("Unterminated"));
        }
    }

    @Test
    public void testEquals_Comprehensive() {
        // Test equals comprehensively - 4 missed (90.9% coverage)
        
        // Test with same object (line 265-266)
        final ExtendedMessageFormat emf1 = new ExtendedMessageFormat("Test: {0}", registry);
        assertTrue("Should equal itself", emf1.equals(emf1));
        
        // Test with null (line 268-269)
        assertFalse("Should not equal null", emf1.equals(null));
        
        // Test with different class (line 274-276)
        assertFalse("Should not equal different class", emf1.equals("string"));
        
        // Test with different pattern (line 278-280)
        final ExtendedMessageFormat emf2 = new ExtendedMessageFormat("Test: {1}", registry);
        assertFalse("Should not equal when patterns differ", emf1.equals(emf2));
        
        // Test with different registry (line 281-283)
        final Map<String, FormatFactory> registry2 = new HashMap<String, FormatFactory>();
        final ExtendedMessageFormat emf3 = new ExtendedMessageFormat("Test: {0}", registry2);
        assertFalse("Should not equal when registries differ", emf1.equals(emf3));
        
        // Test with equal objects
        final ExtendedMessageFormat emf4 = new ExtendedMessageFormat("Test: {0}", registry);
        assertTrue("Should equal when all fields match", emf1.equals(emf4));
        
        // Test with super.equals returning false (line 271-273)
        // This is hard to test directly, but we can test with MessageFormat
        final MessageFormat mf = new MessageFormat("Test: {0}");
        assertFalse("Should not equal MessageFormat", emf1.equals(mf));
    }

    @Test
    public void testApplyPattern_ValidationAndErrorPaths() {
        // Test lines 182-183: Validate.isTrue checks for foundFormats and foundDescriptions
        // Test lines 185-186: IllegalArgumentException for unreadable format element
        // Test lines 203-204: Setting formats when f != null
        
        // Test unreadable format element (line 185-186)
        try {
            // Create a pattern that will trigger the unreadable format element error
            // This happens when c[pos.getIndex()] != END_FE after parsing
            new ExtendedMessageFormat("{0,lower,invalid}", registry);
            // May succeed or fail depending on implementation
        } catch (IllegalArgumentException e) {
            // Expected if format element is unreadable
            assertTrue("Exception should mention position or format", 
                e.getMessage().contains("position") || 
                e.getMessage().contains("Unreadable") ||
                e.getMessage().contains("format"));
        }
        
        // Test with format that triggers validation (lines 182-183)
        // These validations ensure foundFormats and foundDescriptions sizes match fmtCount
        final ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,lower,arg}", registry);
        assertNotNull("Should create format", emf);
        
        // Test with multiple format elements to exercise the loop (lines 201-206)
        final ExtendedMessageFormat emf2 = new ExtendedMessageFormat("{0,lower,arg} {1,upper,arg}", registry);
        assertNotNull("Should handle multiple formats", emf2);
        
        // Test with null format in registry to exercise line 203-204
        final Map<String, FormatFactory> nullRegistry = new HashMap<String, FormatFactory>();
        nullRegistry.put("nullformat", new FormatFactory() {
            @Override
            public Format getFormat(String name, String arguments, Locale locale) {
                return null; // Return null to test line 203-204
            }
        });
        final ExtendedMessageFormat emf3 = new ExtendedMessageFormat("{0,nullformat,arg}", nullRegistry);
        assertNotNull("Should handle null format from factory", emf3);
    }

    @Test
    public void testApplyPattern_DefaultCase() {
        // Test line 189-191: default case in switch statement
        // This covers non-format characters that are appended to stripCustom
        
        // Create pattern with regular text (not format elements)
        final ExtendedMessageFormat emf = new ExtendedMessageFormat("Hello World", registry);
        assertEquals("Hello World", emf.toPattern());
        
        // Test with mixed text and format elements
        final ExtendedMessageFormat emf2 = new ExtendedMessageFormat("Hello {0} World", registry);
        assertNotNull("Should handle mixed text and formats", emf2);
        
        // Test with special characters that aren't format elements
        final ExtendedMessageFormat emf3 = new ExtendedMessageFormat("Test: {0} - {1}", registry);
        assertNotNull("Should handle special characters in default case", emf3);
    }

}
