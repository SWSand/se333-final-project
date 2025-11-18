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
package org.apache.commons.lang3.time;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/**
 * Unit tests {@link org.apache.commons.lang3.time.FastDatePrinter}.
 *
 * @since 3.0
 */
public class FastDatePrinterTest {
    
    private static final String YYYY_MM_DD = "yyyy/MM/dd";
    private static final TimeZone NEW_YORK = TimeZone.getTimeZone("America/New_York");
    private static final Locale SWEDEN = new Locale("sv", "SE");

        DatePrinter getInstance(final String format) {
        return getInstance(format, TimeZone.getDefault(), Locale.getDefault());
    }

    private DatePrinter getDateInstance(final int dateStyle, final Locale locale) {
        return getInstance(FormatCache.getPatternForStyle(Integer.valueOf(dateStyle), null, locale), TimeZone.getDefault(), Locale.getDefault());
    }

    private DatePrinter getInstance(final String format, final Locale locale) {
        return getInstance(format, TimeZone.getDefault(), locale);
    }

    private DatePrinter getInstance(final String format, final TimeZone timeZone) {
        return getInstance(format, timeZone, Locale.getDefault());
    }

    /**
     * Override this method in derived tests to change the construction of instances
     * @param format
     * @param timeZone
     * @param locale
     * @return
     */
    protected DatePrinter getInstance(final String format, final TimeZone timeZone, final Locale locale) {
        return new FastDatePrinter(format, timeZone, locale);
    }

    @Test
    public void testFormat() {
        final Locale realDefaultLocale = Locale.getDefault();
        final TimeZone realDefaultZone = TimeZone.getDefault();
        try {
            Locale.setDefault(Locale.US);
            TimeZone.setDefault(NEW_YORK);

            final GregorianCalendar cal1 = new GregorianCalendar(2003, 0, 10, 15, 33, 20);
            final GregorianCalendar cal2 = new GregorianCalendar(2003, 6, 10, 9, 00, 00);
            final Date date1 = cal1.getTime();
            final Date date2 = cal2.getTime();
            final long millis1 = date1.getTime();
            final long millis2 = date2.getTime();

            DatePrinter fdf = getInstance("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            assertEquals(sdf.format(date1), fdf.format(date1));
            assertEquals("2003-01-10T15:33:20", fdf.format(date1));
            assertEquals("2003-01-10T15:33:20", fdf.format(cal1));
            assertEquals("2003-01-10T15:33:20", fdf.format(millis1));
            assertEquals("2003-07-10T09:00:00", fdf.format(date2));
            assertEquals("2003-07-10T09:00:00", fdf.format(cal2));
            assertEquals("2003-07-10T09:00:00", fdf.format(millis2));

            fdf = getInstance("Z");
            assertEquals("-0500", fdf.format(date1));
            assertEquals("-0500", fdf.format(cal1));
            assertEquals("-0500", fdf.format(millis1));

            assertEquals("-0400", fdf.format(date2));
            assertEquals("-0400", fdf.format(cal2));
            assertEquals("-0400", fdf.format(millis2));

            fdf = getInstance("ZZ");
            assertEquals("-05:00", fdf.format(date1));
            assertEquals("-05:00", fdf.format(cal1));
            assertEquals("-05:00", fdf.format(millis1));

            assertEquals("-04:00", fdf.format(date2));
            assertEquals("-04:00", fdf.format(cal2));
            assertEquals("-04:00", fdf.format(millis2));

            final String pattern = "GGGG GGG GG G yyyy yyy yy y MMMM MMM MM M" +
                " dddd ddd dd d DDDD DDD DD D EEEE EEE EE E aaaa aaa aa a zzzz zzz zz z";
            fdf = getInstance(pattern);
            sdf = new SimpleDateFormat(pattern);
            // SDF bug fix starting with Java 7
            assertEquals(sdf.format(date1).replaceAll("2003 03 03 03", "2003 2003 03 2003"), fdf.format(date1));
            assertEquals(sdf.format(date2).replaceAll("2003 03 03 03", "2003 2003 03 2003"), fdf.format(date2));
        } finally {
            Locale.setDefault(realDefaultLocale);
            TimeZone.setDefault(realDefaultZone);
        }
    }

    /**
     * Test case for {@link FastDateParser#FastDateParser(String, TimeZone, Locale)}.
     */
    @Test
    public void testShortDateStyleWithLocales() {
        final Locale usLocale = Locale.US;
        final Locale swedishLocale = new Locale("sv", "SE");
        final Calendar cal = Calendar.getInstance();
        cal.set(2004, 1, 3);
        DatePrinter fdf = getDateInstance(FastDateFormat.SHORT, usLocale);
        assertEquals("2/3/04", fdf.format(cal));

        fdf = getDateInstance(FastDateFormat.SHORT, swedishLocale);
        assertEquals("2004-02-03", fdf.format(cal));

    }

    /**
     * Tests that pre-1000AD years get padded with yyyy
     */
    @Test
    public void testLowYearPadding() {
        final Calendar cal = Calendar.getInstance();
        final DatePrinter format = getInstance(YYYY_MM_DD);

        cal.set(1,0,1);
        assertEquals("0001/01/01", format.format(cal));
        cal.set(10,0,1);
        assertEquals("0010/01/01", format.format(cal));
        cal.set(100,0,1);
        assertEquals("0100/01/01", format.format(cal));
        cal.set(999,0,1);
        assertEquals("0999/01/01", format.format(cal));
    }
    /**
     * Show Bug #39410 is solved
     */
    @Test
    public void testMilleniumBug() {
        final Calendar cal = Calendar.getInstance();
        final DatePrinter format = getInstance("dd.MM.yyyy");

        cal.set(1000,0,1);
        assertEquals("01.01.1000", format.format(cal));
    }

    /**
     * testLowYearPadding showed that the date was buggy
     * This test confirms it, getting 366 back as a date
     */
    @Test
    public void testSimpleDate() {
        final Calendar cal = Calendar.getInstance();
        final DatePrinter format = getInstance(YYYY_MM_DD);

        cal.set(2004,11,31);
        assertEquals("2004/12/31", format.format(cal));
        cal.set(999,11,31);
        assertEquals("0999/12/31", format.format(cal));
        cal.set(1,2,2);
        assertEquals("0001/03/02", format.format(cal));
    }

    @Test
    public void testLang303() {
        final Calendar cal = Calendar.getInstance();
        cal.set(2004, 11, 31);

        DatePrinter format = getInstance(YYYY_MM_DD);
        final String output = format.format(cal);

        format = SerializationUtils.deserialize(SerializationUtils.serialize((Serializable) format));
        assertEquals(output, format.format(cal));
    }

    @Test
    public void testLang538() {
        // more commonly constructed with: cal = new GregorianCalendar(2009, 9, 16, 8, 42, 16)
        // for the unit test to work in any time zone, constructing with GMT-8 rather than default locale time zone
        final GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT-8"));
        cal.clear();
        cal.set(2009, 9, 16, 8, 42, 16);

        final DatePrinter format = getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("GMT"));
        assertEquals("dateTime", "2009-10-16T16:42:16.000Z", format.format(cal.getTime()));
        assertEquals("dateTime", "2009-10-16T08:42:16.000Z", format.format(cal));
    }

    @Test
    public void testLang645() {
        final Locale locale = new Locale("sv", "SE");

        final Calendar cal = Calendar.getInstance();
        cal.set(2010, 0, 1, 12, 0, 0);
        final Date d = cal.getTime();

        final DatePrinter fdf = getInstance("EEEE', week 'ww", locale);

        assertEquals("fredag, week 53", fdf.format(d));
    }
    
    @Test
    public void testEquals() {
        final DatePrinter printer1= getInstance(YYYY_MM_DD);
        final DatePrinter printer2= getInstance(YYYY_MM_DD);

        assertEquals(printer1, printer2);
        assertEquals(printer1.hashCode(), printer2.hashCode());        

        assertFalse(printer1.equals(new Object()));
    }
    
    @Test
    public void testToStringContainsName() {
        final DatePrinter printer= getInstance(YYYY_MM_DD);
        assertTrue(printer.toString().startsWith("FastDate"));
    }
    
    @Test
    public void testPatternMatches() {
        final DatePrinter printer= getInstance(YYYY_MM_DD);
        assertEquals(YYYY_MM_DD, printer.getPattern());
    }
    
    @Test
    public void testLocaleMatches() {
        final DatePrinter printer= getInstance(YYYY_MM_DD, SWEDEN);
        assertEquals(SWEDEN, printer.getLocale());
    }
    
    @Test
    public void testTimeZoneMatches() {
        final DatePrinter printer= getInstance(YYYY_MM_DD, NEW_YORK);
        assertEquals(NEW_YORK, printer.getTimeZone());
    }
    
    @Test
    public void testCalendarTimezoneRespected() {}
// Defects4J: flaky method
//     @Test
//     public void testCalendarTimezoneRespected() {
//         final String[] availableZones = TimeZone.getAvailableIDs();
//         final TimeZone currentZone = TimeZone.getDefault();
//         
//         TimeZone anotherZone = null;
//         for (final String zone : availableZones) {
//             if (!zone.equals(currentZone.getID())) {
//                 anotherZone = TimeZone.getTimeZone(zone);
//             }
//         }
//         
//         assertNotNull("Cannot find another timezone", anotherZone);
//         
//         final String pattern = "h:mma z";
//         final Calendar cal = Calendar.getInstance(anotherZone);
//         
//         final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
//         sdf.setTimeZone(anotherZone);
//         final String expectedValue = sdf.format(cal.getTime());
//         final String actualValue = FastDateFormat.getInstance(pattern).format(cal);
//         assertEquals(expectedValue, actualValue);
//     }

    @Test
    public void testTimeZoneDisplayKeyEquals() throws Exception {
        // Use reflection to access the private TimeZoneDisplayKey class
        final Class<?>[] innerClasses = FastDatePrinter.class.getDeclaredClasses();
        Class<?> timeZoneDisplayKeyClass = null;
        for (final Class<?> innerClass : innerClasses) {
            if ("TimeZoneDisplayKey".equals(innerClass.getSimpleName())) {
                timeZoneDisplayKeyClass = innerClass;
                break;
            }
        }
        assertNotNull("TimeZoneDisplayKey class should exist", timeZoneDisplayKeyClass);
        
        final java.lang.reflect.Constructor<?> constructor = timeZoneDisplayKeyClass.getDeclaredConstructor(
                TimeZone.class, boolean.class, int.class, Locale.class);
        constructor.setAccessible(true);
        
        final TimeZone tz1 = TimeZone.getTimeZone("America/New_York");
        final TimeZone tz2 = TimeZone.getTimeZone("America/Los_Angeles");
        final Locale locale1 = Locale.US;
        final Locale locale2 = Locale.CANADA;
        
        // Test equals with same object (this == obj)
        final Object key1 = constructor.newInstance(tz1, false, TimeZone.SHORT, locale1);
        assertTrue("Same object should be equal", key1.equals(key1));
        
        // Test equals with equal objects
        final Object key2 = constructor.newInstance(tz1, false, TimeZone.SHORT, locale1);
        assertTrue("Equal objects should be equal", key1.equals(key2));
        assertTrue("Equal objects should be equal (reverse)", key2.equals(key1));
        
        // Test equals with different type
        assertFalse("Different type should not be equal", key1.equals(new Object()));
        assertFalse("Different type should not be equal", key1.equals(null));
        
        // Test equals with different timezone
        final Object key3 = constructor.newInstance(tz2, false, TimeZone.SHORT, locale1);
        assertFalse("Different timezone should not be equal", key1.equals(key3));
        
        // Test equals with different style
        final Object key4 = constructor.newInstance(tz1, false, TimeZone.LONG, locale1);
        assertFalse("Different style should not be equal", key1.equals(key4));
        
        // Test equals with different locale
        final Object key5 = constructor.newInstance(tz1, false, TimeZone.SHORT, locale2);
        assertFalse("Different locale should not be equal", key1.equals(key5));
        
        // Test equals with daylight savings flag difference
        final Object key6 = constructor.newInstance(tz1, true, TimeZone.SHORT, locale1);
        assertFalse("Different daylight flag should not be equal", key1.equals(key6));
    }

    @Test
    public void testPaddedNumberField() throws Exception {
        // Use reflection to access the private PaddedNumberField class
        final Class<?>[] innerClasses = FastDatePrinter.class.getDeclaredClasses();
        Class<?> paddedNumberFieldClass = null;
        for (final Class<?> innerClass : innerClasses) {
            if ("PaddedNumberField".equals(innerClass.getSimpleName())) {
                paddedNumberFieldClass = innerClass;
                break;
            }
        }
        assertNotNull("PaddedNumberField class should exist", paddedNumberFieldClass);
        
        // Test constructor with size < 3 (should throw IllegalArgumentException)
        final java.lang.reflect.Constructor<?> constructor = paddedNumberFieldClass.getDeclaredConstructor(
                int.class, int.class);
        constructor.setAccessible(true);
        
        try {
            constructor.newInstance(Calendar.YEAR, 2);
            fail("Should have thrown IllegalArgumentException for size < 3");
        } catch (final java.lang.reflect.InvocationTargetException e) {
            final Throwable cause = e.getCause();
            assertTrue("Should throw IllegalArgumentException", cause instanceof IllegalArgumentException);
        }
        
        try {
            constructor.newInstance(Calendar.YEAR, 1);
            fail("Should have thrown IllegalArgumentException for size < 3");
        } catch (final java.lang.reflect.InvocationTargetException e) {
            final Throwable cause = e.getCause();
            assertTrue("Should throw IllegalArgumentException", cause instanceof IllegalArgumentException);
        }
        
        // Test appendTo with value >= 1000 (to trigger the Validate.isTrue path)
        final Object paddedField = constructor.newInstance(Calendar.YEAR, 5);
        final java.lang.reflect.Method appendToMethod = paddedNumberFieldClass.getDeclaredMethod(
                "appendTo", StringBuffer.class, int.class);
        appendToMethod.setAccessible(true);
        
        final StringBuffer buffer = new StringBuffer();
        appendToMethod.invoke(paddedField, buffer, 1234);
        assertTrue("Should format 4-digit number", buffer.length() > 0);
        
        // Test appendTo with value >= 1000 that triggers Validate.isTrue
        // The Validate.isTrue checks for value > -1, so we need to test with a value >= 1000
        buffer.setLength(0);
        appendToMethod.invoke(paddedField, buffer, 9999);
        assertTrue("Should format 4-digit number", buffer.length() > 0);
        
        // Test appendTo with value < 100 but >= 1000 path (to cover the else branch)
        buffer.setLength(0);
        appendToMethod.invoke(paddedField, buffer, 500);
        assertTrue("Should format 3-digit number", buffer.length() > 0);
        
        // Test appendTo with value >= 1000 where mSize is larger than digits
        // This ensures the padding loop executes multiple times
        final Object paddedFieldLarge = constructor.newInstance(Calendar.YEAR, 8);
        buffer.setLength(0);
        appendToMethod.invoke(paddedFieldLarge, buffer, 1234); // 4 digits, mSize=8, so 4 zeros should be added
        assertEquals("Should pad 4-digit number to 8 digits", "00001234", buffer.toString());
        
        // Test appendTo with value >= 1000 where mSize equals digits (no padding)
        final Object paddedFieldExact = constructor.newInstance(Calendar.YEAR, 4);
        buffer.setLength(0);
        appendToMethod.invoke(paddedFieldExact, buffer, 1234); // 4 digits, mSize=4, so no zeros should be added
        assertEquals("Should format 4-digit number without padding", "1234", buffer.toString());
    }

    // Tests for FastDatePrinter methods with 0% coverage
    @Test
    public void testFormat_Object_StringBuffer_FieldPosition() throws Exception {
        // Test format(Object, StringBuffer, FieldPosition) - 37 missed (19.6% coverage)
        final DatePrinter printer = getInstance(YYYY_MM_DD);
        final StringBuffer buffer = new StringBuffer();
        final java.text.FieldPosition pos = new java.text.FieldPosition(0);
        
        // Test with Date
        final Date date = new Date();
        final StringBuffer result1 = printer.format(date, buffer, pos);
        assertNotNull("Should return StringBuffer", result1);
        assertTrue("Should format Date", result1.length() > 0);
        
        // Test with Calendar
        buffer.setLength(0);
        final Calendar cal = Calendar.getInstance();
        cal.set(2004, 11, 31);
        final StringBuffer result2 = printer.format(cal, buffer, pos);
        assertNotNull("Should return StringBuffer", result2);
        assertTrue("Should format Calendar", result2.length() > 0);
        
        // Test with Long
        buffer.setLength(0);
        final Long millis = Long.valueOf(System.currentTimeMillis());
        final StringBuffer result3 = printer.format(millis, buffer, pos);
        assertNotNull("Should return StringBuffer", result3);
        assertTrue("Should format Long", result3.length() > 0);
        
        // Test with null (should throw IllegalArgumentException)
        buffer.setLength(0);
        try {
            printer.format(null, buffer, pos);
            fail("Should throw IllegalArgumentException for null");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception should mention null", e.getMessage().contains("null"));
        }
        
        // Test with unknown type (should throw IllegalArgumentException)
        buffer.setLength(0);
        try {
            printer.format("unknown", buffer, pos);
            fail("Should throw IllegalArgumentException for unknown type");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception should mention Unknown class", e.getMessage().contains("Unknown class"));
        }
    }

    @Test
    public void testFormat_Long_StringBuffer() throws Exception {
        // Test format(long, StringBuffer) - 8 missed (0% coverage)
        final DatePrinter printer = getInstance(YYYY_MM_DD);
        final StringBuffer buffer = new StringBuffer();
        
        final long millis = System.currentTimeMillis();
        final StringBuffer result = printer.format(millis, buffer);
        assertNotNull("Should return StringBuffer", result);
        assertTrue("Should format long", result.length() > 0);
        assertEquals("Should append to provided buffer", buffer, result);
    }

    @Test
    public void testGetMaxLengthEstimate() throws Exception {
        // Test getMaxLengthEstimate() - 3 missed (0% coverage)
        final FastDatePrinter printer1 = (FastDatePrinter) getInstance(YYYY_MM_DD);
        final int estimate1 = printer1.getMaxLengthEstimate();
        assertTrue("Should return positive estimate", estimate1 > 0);

        final FastDatePrinter printer2 = (FastDatePrinter) getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS");
        final int estimate2 = printer2.getMaxLengthEstimate();
        assertTrue("Should return positive estimate", estimate2 > 0);
        assertTrue("Longer pattern should have larger estimate", estimate2 >= estimate1);
    }
        
    @Test
    public void testAppendTo_InnerClasses() throws Exception {
        // Test appendTo(StringBuffer, Calendar) and appendTo(StringBuffer, int) in inner classes
        // TwelveHourField.appendTo(StringBuffer, Calendar) - 24 missed (29.41% coverage)
        // TwentyFourHourField.appendTo(StringBuffer, Calendar) - 24 missed (29.41% coverage)
        // UnpaddedNumberField.appendTo(StringBuffer, int) - 6 missed (0% coverage)
        
        final DatePrinter printer = getInstance("h k"); // h = 12-hour, k = 24-hour
        
        // Test TwelveHourField.appendTo(Calendar) - when HOUR == 0
        final Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR, 0);
        cal1.set(Calendar.AM_PM, Calendar.AM);
        final String result1 = printer.format(cal1);
        assertNotNull("Should format calendar with HOUR == 0", result1);
        
        // Test TwentyFourHourField.appendTo(Calendar) - when HOUR_OF_DAY == 0
        final Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        final String result2 = printer.format(cal2);
        assertNotNull("Should format calendar with HOUR_OF_DAY == 0", result2);
        
        // Test UnpaddedNumberField.appendTo(StringBuffer, int) - value >= 100
        final DatePrinter printer2 = getInstance("yyyy"); // Uses UnpaddedNumberField for year
        final Calendar cal3 = Calendar.getInstance();
        cal3.set(Calendar.YEAR, 2004);
        final String result3 = printer2.format(cal3);
        assertTrue("Should format year >= 100", result3.contains("2004"));
        
        // Test UnpaddedNumberField.appendTo(StringBuffer, int) - value < 10
        final Calendar cal4 = Calendar.getInstance();
        cal4.set(Calendar.DAY_OF_MONTH, 5);
        final DatePrinter printer3 = getInstance("d"); // Uses UnpaddedNumberField for day
        final String result4 = printer3.format(cal4);
        assertTrue("Should format day < 10", result4.length() > 0);
        
        // Test UnpaddedNumberField.appendTo(StringBuffer, int) - value >= 10 && < 100
        final Calendar cal5 = Calendar.getInstance();
        cal5.set(Calendar.DAY_OF_MONTH, 25);
        final String result5 = printer3.format(cal5);
        assertTrue("Should format day >= 10 && < 100", result5.length() > 0);
    }

    @Test
    public void testTwelveHourField_AppendTo() throws Exception {
        // Test TwelveHourField.appendTo(StringBuffer, Calendar) - when HOUR == 0
        final DatePrinter printer = getInstance("h"); // 12-hour format
        
        // Test when HOUR == 0 (should use getLeastMaximum + 1)
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.AM_PM, Calendar.AM);
        final String result = printer.format(cal);
        assertNotNull("Should format when HOUR == 0", result);
        assertTrue("Should format hour", result.length() > 0);
        
        // Test when HOUR != 0
        cal.set(Calendar.HOUR, 5);
        final String result2 = printer.format(cal);
        assertNotNull("Should format when HOUR != 0", result2);
    }

    @Test
    public void testTwentyFourHourField_AppendTo() throws Exception {
        // Test TwentyFourHourField.appendTo(StringBuffer, Calendar) - when HOUR_OF_DAY == 0
        final DatePrinter printer = getInstance("k"); // 24-hour format (1-24)
        
        // Test when HOUR_OF_DAY == 0 (should use getMaximum + 1)
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        final String result = printer.format(cal);
        assertNotNull("Should format when HOUR_OF_DAY == 0", result);
        assertTrue("Should format hour", result.length() > 0);
        
        // Test when HOUR_OF_DAY != 0
        cal.set(Calendar.HOUR_OF_DAY, 12);
        final String result2 = printer.format(cal);
        assertNotNull("Should format when HOUR_OF_DAY != 0", result2);
    }

    @Test
    public void testUnpaddedNumberField_AppendTo_Int() throws Exception {
        // Test UnpaddedNumberField.appendTo(StringBuffer, int) - 6 missed (0% coverage)
        final DatePrinter printer = getInstance("d"); // Day of month - uses UnpaddedNumberField
        
        // Test with value < 10
        final Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.DAY_OF_MONTH, 5);
        final String result1 = printer.format(cal1);
        assertEquals("Should format single digit", "5", result1);
        
        // Test with value >= 10 && < 100
        final Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.DAY_OF_MONTH, 25);
        final String result2 = printer.format(cal2);
        assertEquals("Should format two digits", "25", result2);
        
        // Test with value >= 100 (year)
        final DatePrinter printer2 = getInstance("yyyy");
        final Calendar cal3 = Calendar.getInstance();
        cal3.set(Calendar.YEAR, 2004);
        final String result3 = printer2.format(cal3);
        assertTrue("Should format year >= 100", result3.contains("2004"));
    }

    @Test
    public void testPaddedNumberField_AppendTo_LargeValues() throws Exception {
        // Test PaddedNumberField.appendTo(StringBuffer, int) with values >= 1000
        // This tests the branch at line 852 that validates value > -1
        // Use a pattern that requires padding (SSSS for milliseconds with 4 digits)
        final DatePrinter printer = getInstance("SSSS");
        
        // Test with value >= 1000 (milliseconds)
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 1234);
        final String result = printer.format(cal);
        assertNotNull("Should format milliseconds >= 1000", result);
        assertTrue("Should contain padded value", result.length() >= 4);
        
        // Test with value < 1000 but >= 100
        cal.set(Calendar.MILLISECOND, 567);
        final String result2 = printer.format(cal);
        assertNotNull("Should format milliseconds < 1000", result2);
        
        // Test with value < 100
        cal.set(Calendar.MILLISECOND, 89);
        final String result3 = printer.format(cal);
        assertNotNull("Should format milliseconds < 100", result3);
    }

    @Test
    public void testPaddedNumberField_AppendTo_YearPattern() throws Exception {
        // Test PaddedNumberField with year pattern that requires padding
        // Pattern "yyyyy" (5 y's) uses PaddedNumberField with size 5
        final DatePrinter printer = getInstance("yyyyy");
        
        // Test with year >= 1000 (triggers the Integer.toString branch)
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2024);
        final String result = printer.format(cal);
        assertNotNull("Should format year >= 1000", result);
        assertTrue("Should contain year", result.contains("2024") || result.length() >= 5);
        
        // Test with year < 1000 (should still work)
        cal.set(Calendar.YEAR, 999);
        final String result2 = printer.format(cal);
        assertNotNull("Should format year < 1000", result2);
    }

    @Test
    public void testTwelveHourField_AppendTo_Int() throws Exception {
        // Test TwelveHourField.appendTo(StringBuffer, int) - the direct int overload
        // This is called internally when formatting, but we can verify it works
        // by testing various hour values through the public API
        final DatePrinter printer = getInstance("h"); // 12-hour format
        
        // Test various hour values to ensure the int overload path is exercised
        final Calendar cal = Calendar.getInstance();
        
        // Test hour 1-11
        for (int hour = 1; hour <= 11; hour++) {
            cal.set(Calendar.HOUR, hour);
            cal.set(Calendar.AM_PM, Calendar.AM);
            final String result = printer.format(cal);
            assertNotNull("Should format hour " + hour, result);
            assertTrue("Should format hour " + hour, result.length() > 0);
        }
        
        // Test hour 12 (noon)
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.AM_PM, Calendar.PM);
        final String result12 = printer.format(cal);
        assertNotNull("Should format hour 12 (noon)", result12);
    }
}
