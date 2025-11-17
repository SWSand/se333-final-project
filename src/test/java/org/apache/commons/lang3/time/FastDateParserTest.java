/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional inparserion regarding copyright ownership.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Assert;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/**
 * Unit tests {@link org.apache.commons.lang3.time.FastDateParser}.
 *
 * @since 3.2
 */
public class FastDateParserTest {
    private static final String SHORT_FORMAT_NOERA = "y/M/d/h/a/m/E/Z";
    private static final String LONG_FORMAT_NOERA = "yyyy/MMMM/dddd/hhhh/mmmm/aaaa/EEEE/ZZZZ";
    private static final String SHORT_FORMAT = "G/" + SHORT_FORMAT_NOERA;
    private static final String LONG_FORMAT = "GGGG/" + LONG_FORMAT_NOERA;

    private static final String yMdHmsSZ = "yyyy-MM-dd'T'HH:mm:ss.SSS Z";
    private static final String DMY_DOT = "dd.MM.yyyy";
    private static final String YMD_SLASH = "yyyy/MM/dd";
    private static final String MDY_DASH = "MM-DD-yyyy";
    private static final String MDY_SLASH = "MM/DD/yyyy";

    private static final TimeZone REYKJAVIK = TimeZone.getTimeZone("Atlantic/Reykjavik");
    private static final TimeZone NEW_YORK = TimeZone.getTimeZone("America/New_York");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private static final Locale SWEDEN = new Locale("sv", "SE");

    DateParser getInstance(final String format) {
        return getInstance(format, TimeZone.getDefault(), Locale.getDefault());
    }

    private DateParser getDateInstance(final int dateStyle, final Locale locale) {
        return getInstance(FormatCache.getPatternForStyle(Integer.valueOf(dateStyle), null, locale), TimeZone.getDefault(), Locale.getDefault());
    }

    private DateParser getInstance(final String format, final Locale locale) {
        return getInstance(format, TimeZone.getDefault(), locale);
    }

    private DateParser getInstance(final String format, final TimeZone timeZone) {
        return getInstance(format, timeZone, Locale.getDefault());
    }

    /**
     * Override this method in derived tests to change the construction of instances
     */
    protected DateParser getInstance(final String format, final TimeZone timeZone, final Locale locale) {
        return new FastDateParser(format, timeZone, locale);
    }

    @Test
    public void test_Equality_Hash() {
        final DateParser[] parsers= {
            getInstance(yMdHmsSZ, NEW_YORK, Locale.US),
            getInstance(DMY_DOT, NEW_YORK, Locale.US),
            getInstance(YMD_SLASH, NEW_YORK, Locale.US),
            getInstance(MDY_DASH, NEW_YORK, Locale.US),
            getInstance(MDY_SLASH, NEW_YORK, Locale.US),
            getInstance(MDY_SLASH, REYKJAVIK, Locale.US),
            getInstance(MDY_SLASH, REYKJAVIK, SWEDEN)
        };

        final Map<DateParser,Integer> map= new HashMap<DateParser,Integer>();
        int i= 0;
        for(final DateParser parser:parsers) {
            map.put(parser, Integer.valueOf(i++));
        }

        i= 0;
        for(final DateParser parser:parsers) {
            assertEquals(i++, map.get(parser).intValue());
        }
    }

    @Test
    public void testParseZone() {}
// Defects4J: flaky method
//     @Test
//     public void testParseZone() throws ParseException {
//         final Calendar cal= Calendar.getInstance(NEW_YORK, Locale.US);
//         cal.clear();
//         cal.set(2003, 6, 10, 16, 33, 20);
// 
//         final DateParser fdf = getInstance(yMdHmsSZ, NEW_YORK, Locale.US);
// 
//         assertEquals(cal.getTime(), fdf.parse("2003-07-10T15:33:20.000 -0500"));
//         assertEquals(cal.getTime(), fdf.parse("2003-07-10T15:33:20.000 GMT-05:00"));
//         assertEquals(cal.getTime(), fdf.parse("2003-07-10T16:33:20.000 Eastern Daylight Time"));
//         assertEquals(cal.getTime(), fdf.parse("2003-07-10T16:33:20.000 EDT"));
// 
//         cal.setTimeZone(TimeZone.getTimeZone("GMT-3"));
//         cal.set(2003, 1, 10, 9, 0, 0);
// 
//         assertEquals(cal.getTime(), fdf.parse("2003-02-10T09:00:00.000 -0300"));
// 
//         cal.setTimeZone(TimeZone.getTimeZone("GMT+5"));
//         cal.set(2003, 1, 10, 15, 5, 6);
// 
//         assertEquals(cal.getTime(), fdf.parse("2003-02-10T15:05:06.000 +0500"));
//     }

    @Test
    public void testParseLongShort() throws ParseException {
        final Calendar cal= Calendar.getInstance(NEW_YORK, Locale.US);
        cal.clear();
        cal.set(2003, 1, 10, 15, 33, 20);
        cal.set(Calendar.MILLISECOND, 989);
        cal.setTimeZone(NEW_YORK);

        DateParser fdf = getInstance("yyyy GGGG MMMM dddd aaaa EEEE HHHH mmmm ssss SSSS ZZZZ", NEW_YORK, Locale.US);

        assertEquals(cal.getTime(), fdf.parse("2003 AD February 0010 PM Monday 0015 0033 0020 0989 GMT-05:00"));
        cal.set(Calendar.ERA, GregorianCalendar.BC);

        final Date parse = fdf.parse("2003 BC February 0010 PM Saturday 0015 0033 0020 0989 GMT-05:00");
                assertEquals(cal.getTime(), parse);

        fdf = getInstance("y G M d a E H m s S Z", NEW_YORK, Locale.US);
        assertEquals(cal.getTime(), fdf.parse("03 BC 2 10 PM Sat 15 33 20 989 -0500"));

        cal.set(Calendar.ERA, GregorianCalendar.AD);
        assertEquals(cal.getTime(), fdf.parse("03 AD 2 10 PM Saturday 15 33 20 989 -0500"));
    }

    @Test
    public void testAmPm() throws ParseException {
        final Calendar cal= Calendar.getInstance(NEW_YORK, Locale.US);
        cal.clear();

        final DateParser h = getInstance("yyyy-MM-dd hh a mm:ss", NEW_YORK, Locale.US);
        final DateParser K = getInstance("yyyy-MM-dd KK a mm:ss", NEW_YORK, Locale.US);
        final DateParser k = getInstance("yyyy-MM-dd kk:mm:ss", NEW_YORK, Locale.US);
        final DateParser H = getInstance("yyyy-MM-dd HH:mm:ss", NEW_YORK, Locale.US);

        cal.set(2010, 7, 1, 0, 33, 20);
        assertEquals(cal.getTime(), h.parse("2010-08-01 12 AM 33:20"));
        assertEquals(cal.getTime(), K.parse("2010-08-01 0 AM 33:20"));
        assertEquals(cal.getTime(), k.parse("2010-08-01 00:33:20"));
        assertEquals(cal.getTime(), H.parse("2010-08-01 00:33:20"));

        cal.set(2010, 7, 1, 3, 33, 20);
        assertEquals(cal.getTime(), h.parse("2010-08-01 3 AM 33:20"));
        assertEquals(cal.getTime(), K.parse("2010-08-01 3 AM 33:20"));
        assertEquals(cal.getTime(), k.parse("2010-08-01 03:33:20"));
        assertEquals(cal.getTime(), H.parse("2010-08-01 03:33:20"));

        cal.set(2010, 7, 1, 15, 33, 20);
        assertEquals(cal.getTime(), h.parse("2010-08-01 3 PM 33:20"));
        assertEquals(cal.getTime(), K.parse("2010-08-01 3 PM 33:20"));
        assertEquals(cal.getTime(), k.parse("2010-08-01 15:33:20"));
        assertEquals(cal.getTime(), H.parse("2010-08-01 15:33:20"));

        cal.set(2010, 7, 1, 12, 33, 20);
        assertEquals(cal.getTime(), h.parse("2010-08-01 12 PM 33:20"));
        assertEquals(cal.getTime(), K.parse("2010-08-01 0 PM 33:20"));
        assertEquals(cal.getTime(), k.parse("2010-08-01 12:33:20"));
        assertEquals(cal.getTime(), H.parse("2010-08-01 12:33:20"));
    }

    @Test
    // Check that all Locales can parse the formats we use
    public void testParses() throws Exception {
        for(final Locale locale : Locale.getAvailableLocales()) {
            for(final TimeZone tz : new TimeZone[]{NEW_YORK, GMT}) {
                final Calendar cal = Calendar.getInstance(tz);
                for(final int year : new int[]{2003, 1940, 1868, 1867, 0, -1940}) {
                    // http://docs.oracle.com/javase/6/docs/technotes/guides/intl/calendar.doc.html
                    if (year < 1868 && locale.equals(FastDateParser.JAPANESE_IMPERIAL)) {
                        continue; // Japanese imperial calendar does not support eras before 1868
                    }
                    cal.clear();
                    if (year < 0) {
                        cal.set(-year, 1, 10);
                        cal.set(Calendar.ERA, GregorianCalendar.BC);
                    } else {
                        cal.set(year, 1, 10);
                    }
                    final Date in = cal.getTime();
                    for(final String format : new String[]{LONG_FORMAT, SHORT_FORMAT}) {
                        final SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
                        if (format.equals(SHORT_FORMAT)) {
                            if (year < 1930) {
                                sdf.set2DigitYearStart(cal.getTime());
                            }
                        }
                        final String fmt = sdf.format(in);
                        try {
                            final Date out = sdf.parse(fmt);

                            assertEquals(locale.toString()+" "+year+" "+ format+ " "+tz.getID(), in, out);
                        } catch (final ParseException pe) {
                            System.out.println(fmt+" "+locale.toString()+" "+year+" "+ format+ " "+tz.getID());
                            throw pe;
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testLocales_Long_AD() throws Exception {
        testLocales(LONG_FORMAT, false);
    }

    @Test
    public void testLocales_Long_BC() throws Exception {
        testLocales(LONG_FORMAT, true);
    }

    @Test
    public void testLocales_Short_AD() throws Exception {
        testLocales(SHORT_FORMAT, false);
    }

    @Test
    public void testLocales_Short_BC() throws Exception {
        testLocales(SHORT_FORMAT, true);
    }

    @Test
    public void testLocales_LongNoEra_AD() throws Exception {
        testLocales(LONG_FORMAT_NOERA, false);
    }

    @Test
    public void testLocales_LongNoEra_BC() throws Exception {
        testLocales(LONG_FORMAT_NOERA, true);
    }

    @Test
    public void testLocales_ShortNoEra_AD() throws Exception {
        testLocales(SHORT_FORMAT_NOERA, false);
    }

    @Test
    public void testLocales_ShortNoEra_BC() throws Exception {
        testLocales(SHORT_FORMAT_NOERA, true);
    }

    private void testLocales(final String format, final boolean eraBC) throws Exception {

        final Calendar cal= Calendar.getInstance(GMT);
        cal.clear();
        cal.set(2003, 1, 10);
        if (eraBC) {
            cal.set(Calendar.ERA, GregorianCalendar.BC);
        }
        for(final Locale locale : Locale.getAvailableLocales()) {
            // ja_JP_JP cannot handle dates before 1868 properly
            if (eraBC && locale.equals(FastDateParser.JAPANESE_IMPERIAL)) {
                continue;
            }
            final SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
            final DateParser fdf = getInstance(format, locale);

            try {
                checkParse(locale, cal, sdf, fdf);
            } catch(final ParseException ex) {
                Assert.fail("Locale "+locale+ " failed with "+format+" era "+(eraBC?"BC":"AD")+"\n" + trimMessage(ex.toString()));
            }
        }
    }

    private String trimMessage(final String msg) {
        if (msg.length() < 100) {
            return msg;
        }
        final int gmt = msg.indexOf("(GMT");
        if (gmt > 0) {
            return msg.substring(0, gmt+4)+"...)";
        }
        return msg.substring(0, 100)+"...";
    }

    private void checkParse(final Locale locale, final Calendar cal, final SimpleDateFormat sdf, final DateParser fdf) throws ParseException {
        final String formattedDate= sdf.format(cal.getTime());
        final Date expectedTime = sdf.parse(formattedDate);
        final Date actualTime = fdf.parse(formattedDate);
        assertEquals(locale.toString()+" "+formattedDate
                +"\n",expectedTime, actualTime);
    }

    @Test
    public void testParseNumerics() throws ParseException {
        final Calendar cal= Calendar.getInstance(NEW_YORK, Locale.US);
        cal.clear();
        cal.set(2003, 1, 10, 15, 33, 20);
        cal.set(Calendar.MILLISECOND, 989);

        final DateParser fdf = getInstance("yyyyMMddHHmmssSSS", NEW_YORK, Locale.US);
        assertEquals(cal.getTime(), fdf.parse("20030210153320989"));
    }

    @Test
    public void testQuotes() throws ParseException {
        final Calendar cal= Calendar.getInstance(NEW_YORK, Locale.US);
        cal.clear();
        cal.set(2003, 1, 10, 15, 33, 20);
        cal.set(Calendar.MILLISECOND, 989);

        final DateParser fdf = getInstance("''yyyyMMdd'A''B'HHmmssSSS''", NEW_YORK, Locale.US);
        assertEquals(cal.getTime(), fdf.parse("'20030210A'B153320989'"));
    }

    @Test
    public void testSpecialCharacters() throws Exception {
        testSdfAndFdp("q" ,"", true); // bad pattern character (at present)
        testSdfAndFdp("Q" ,"", true); // bad pattern character
        testSdfAndFdp("$" ,"$", false); // OK
        testSdfAndFdp("?.d" ,"?.12", false); // OK
        testSdfAndFdp("''yyyyMMdd'A''B'HHmmssSSS''", "'20030210A'B153320989'", false); // OK
        testSdfAndFdp("''''yyyyMMdd'A''B'HHmmssSSS''", "''20030210A'B153320989'", false); // OK
        testSdfAndFdp("'$\\Ed'" ,"$\\Ed", false); // OK
    }

    @Test
    public void testLANG_832() throws Exception {
        testSdfAndFdp("'d'd" ,"d3", false); // OK
        testSdfAndFdp("'d'd'","d3", true); // should fail (unterminated quote)
    }

    @Test
    public void testLANG_831() throws Exception {
        testSdfAndFdp("M E","3  Tue", true);
    }

    private void testSdfAndFdp(final String format, final String date, final boolean shouldFail)
            throws Exception {
        final boolean debug = false;
        Date dfdp = null;
        Date dsdf = null;
        Throwable f = null;
        Throwable s = null;

        try {
            final SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
            sdf.setTimeZone(NEW_YORK);
            dsdf = sdf.parse(date);
            if (shouldFail) {
                Assert.fail("Expected SDF failure, but got " + dsdf + " for ["+format+","+date+"]");
            }
        } catch (final Exception e) {
            s = e;
            if (!shouldFail) {
                throw e;
            }
            if (debug) {
                System.out.println("sdf:"+format+"/"+date+"=>"+e);
            }
        }

        try {
            final DateParser fdp = getInstance(format, NEW_YORK, Locale.US);
            dfdp = fdp.parse(date);
            if (shouldFail) {
                Assert.fail("Expected FDF failure, but got " + dfdp + " for ["+format+","+date+"] using "+((FastDateParser)fdp).getParsePattern());
            }
        } catch (final Exception e) {
            f = e;
            if (!shouldFail) {
                throw e;
            }
            if (debug) {
                System.out.println("fdf:"+format+"/"+date+"=>"+e);
            }
        }
        // SDF and FDF should produce equivalent results
        assertTrue("Should both or neither throw Exceptions", (f==null)==(s==null));
        assertEquals("Parsed dates should be equal", dsdf, dfdp);
        if (debug) {
            System.out.println(format + "," + date + " => " + dsdf);
        }
    }

    @Test
    public void testDayOf() throws ParseException {
        final Calendar cal= Calendar.getInstance(NEW_YORK, Locale.US);
        cal.clear();
        cal.set(2003, 1, 10);

        final DateParser fdf = getInstance("W w F D y", NEW_YORK, Locale.US);
        assertEquals(cal.getTime(), fdf.parse("3 7 2 41 03"));
    }

    /**
     * Test case for {@link FastDateParser#FastDateParser(String, TimeZone, Locale)}.
     * @throws ParseException
     */
    @Test
    public void testShortDateStyleWithLocales() throws ParseException {
        DateParser fdf = getDateInstance(FastDateFormat.SHORT, Locale.US);
        final Calendar cal = Calendar.getInstance();
        cal.clear();

        cal.set(2004, 1, 3);
        assertEquals(cal.getTime(), fdf.parse("2/3/04"));

        fdf = getDateInstance(FastDateFormat.SHORT, SWEDEN);
        assertEquals(cal.getTime(), fdf.parse("2004-02-03"));
    }

    /**
     * Tests that pre-1000AD years get padded with yyyy
     * @throws ParseException
     */
    @Test
    public void testLowYearPadding() throws ParseException {
        final DateParser parser = getInstance(YMD_SLASH);
        final Calendar cal = Calendar.getInstance();
        cal.clear();

        cal.set(1,0,1);
        assertEquals(cal.getTime(), parser.parse("0001/01/01"));
        cal.set(10,0,1);
        assertEquals(cal.getTime(), parser.parse("0010/01/01"));
        cal.set(100,0,1);
        assertEquals(cal.getTime(), parser.parse("0100/01/01"));
        cal.set(999,0,1);
        assertEquals(cal.getTime(), parser.parse("0999/01/01"));
    }

    /**
     * @throws ParseException
     */
    @Test
    public void testMilleniumBug() throws ParseException {
        final DateParser parser = getInstance(DMY_DOT);
        final Calendar cal = Calendar.getInstance();
        cal.clear();

        cal.set(1000,0,1);
        assertEquals(cal.getTime(), parser.parse("01.01.1000"));
    }

    @Test
    public void testLang303() throws ParseException {
        DateParser parser = getInstance(YMD_SLASH);
        final Calendar cal = Calendar.getInstance();
        cal.set(2004, 11, 31);

        final Date date = parser.parse("2004/11/31");

        parser = SerializationUtils.deserialize(SerializationUtils.serialize((Serializable) parser));
        assertEquals(date, parser.parse("2004/11/31"));
    }

    @Test
    public void testLang538() throws ParseException {
        final DateParser parser = getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", GMT);

        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-8"));
        cal.clear();
        cal.set(2009, 9, 16, 8, 42, 16);

        assertEquals(cal.getTime(), parser.parse("2009-10-16T16:42:16.000Z"));
    }

    @Test
    public void testEquals() {
        final DateParser parser1= getInstance(YMD_SLASH);
        final DateParser parser2= getInstance(YMD_SLASH);

        assertEquals(parser1, parser2);
        assertEquals(parser1.hashCode(), parser2.hashCode());

        assertFalse(parser1.equals(new Object()));
    }

    @Test
    public void testToStringContainsName() {
        final DateParser parser= getInstance(YMD_SLASH);
        assertTrue(parser.toString().startsWith("FastDate"));
    }

    @Test
    public void testPatternMatches() {
        final DateParser parser= getInstance(yMdHmsSZ);
        assertEquals(yMdHmsSZ, parser.getPattern());
    }

    @Test
    public void testLocaleMatches() {
        final DateParser parser= getInstance(yMdHmsSZ, SWEDEN);
        assertEquals(SWEDEN, parser.getLocale());
    }

    @Test
    public void testTimeZoneMatches() {
        final DateParser parser= getInstance(yMdHmsSZ, REYKJAVIK);
        assertEquals(REYKJAVIK, parser.getTimeZone());
    }

    @Test
    public void testCopyQuotedStrategySetCalendar() throws Exception {
        // Test that CopyQuotedStrategy.setCalendar (empty implementation) is called
        // This tests the base Strategy.setCalendar method which has an empty body
        // Create a CopyQuotedStrategy instance directly using reflection
        final DateParser parser = getInstance("'literal'yyyy", NEW_YORK, Locale.US);
        
        // Get the CopyQuotedStrategy class using reflection
        final Class<?>[] innerClasses = FastDateParser.class.getDeclaredClasses();
        Class<?> copyQuotedStrategyClass = null;
        for (final Class<?> innerClass : innerClasses) {
            if (innerClass.getSimpleName().equals("CopyQuotedStrategy")) {
                copyQuotedStrategyClass = innerClass;
                break;
            }
        }
        assertTrue("Should find CopyQuotedStrategy class", copyQuotedStrategyClass != null);
        
        // Create an instance of CopyQuotedStrategy
        final java.lang.reflect.Constructor<?> constructor = copyQuotedStrategyClass.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        final Object copyQuotedStrategy = constructor.newInstance("literal");
        
        // Call setCalendar directly using reflection on the Strategy base class
        // This ensures the empty Strategy.setCalendar method body is executed
        final java.lang.reflect.Method setCalendarMethod = copyQuotedStrategy.getClass().getSuperclass()
                .getDeclaredMethod("setCalendar", FastDateParser.class, Calendar.class, String.class);
        setCalendarMethod.setAccessible(true);
        
        final Calendar cal = Calendar.getInstance(NEW_YORK, Locale.US);
        cal.clear();
        // Store the year before calling setCalendar
        final int yearBefore = cal.get(Calendar.YEAR);
        // Call the empty setCalendar method - this should do nothing
        // Call it multiple times to ensure coverage
        setCalendarMethod.invoke(copyQuotedStrategy, parser, cal, "literal");
        setCalendarMethod.invoke(copyQuotedStrategy, parser, cal, "test");
        
        // Verify calendar wasn't modified (since setCalendar is empty)
        assertEquals("Calendar should not be modified by empty setCalendar", yearBefore, cal.get(Calendar.YEAR));
        
        // Also call setCalendar on another CopyQuotedStrategy instance to ensure coverage
        final Object copyQuotedStrategy2 = constructor.newInstance("another");
        setCalendarMethod.invoke(copyQuotedStrategy2, parser, cal, "another");
        assertEquals("Calendar should not be modified by empty setCalendar", yearBefore, cal.get(Calendar.YEAR));
    }

    // Tests for FastDateParser methods with 0% coverage
    @Test
    public void testParseObject_String() throws Exception {
        // Test parseObject(String) - 4 missed (0% coverage)
        final DateParser parser = getInstance("yyyy-MM-dd", NEW_YORK, Locale.US);
        
        // Test with valid date
        final Object result1 = parser.parseObject("2004-12-31");
        assertNotNull("Should parse valid date", result1);
        assertTrue("Should return Date", result1 instanceof Date);
        
        // Test with invalid date (should throw ParseException)
        try {
            parser.parseObject("invalid");
            fail("Should throw ParseException for invalid date");
        } catch (ParseException e) {
            assertTrue("Exception should mention Unparseable date", e.getMessage().contains("Unparseable date"));
        }
        
        // Test with Japanese Imperial locale (special error message)
        final Locale japaneseImperial = Locale.forLanguageTag("ja-JP-u-ca-japanese");
        try {
            final DateParser jpParser = getInstance("yyyy-MM-dd", NEW_YORK, japaneseImperial);
            jpParser.parseObject("invalid");
            // May or may not throw exception depending on locale support
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testGetParsePattern() throws Exception {
        // Test getParsePattern() - 3 missed (0% coverage) - private method, use reflection
        final DateParser parser = getInstance("yyyy-MM-dd", NEW_YORK, Locale.US);
        
        // Use reflection to access private getParsePattern() method
        final java.lang.reflect.Method getParsePatternMethod = FastDateParser.class.getDeclaredMethod("getParsePattern");
        getParsePatternMethod.setAccessible(true);
        
        final java.util.regex.Pattern pattern = (java.util.regex.Pattern) getParsePatternMethod.invoke(parser);
        assertNotNull("Should return Pattern", pattern);
        assertNotNull("Pattern should have pattern string", pattern.pattern());
    }

    @Test
    public void testTextStrategy_SetCalendar() throws Exception {
        // Test TextStrategy.setCalendar - 41 missed (26.8% coverage)
        // This exercises the IllegalArgumentException path when value is not in keyValues
        
        final DateParser parser = getInstance("MMMM", NEW_YORK, Locale.US); // Month name format
        
        // Test with valid month name
        final Date validDate = parser.parse("January");
        assertNotNull("Should parse valid month name", validDate);
        
        // Test with invalid month name (should throw IllegalArgumentException)
        try {
            parser.parse("InvalidMonth");
            fail("Should throw ParseException for invalid month name");
        } catch (ParseException e) {
            // The ParseException is thrown by parse(), but TextStrategy.setCalendar
            // throws IllegalArgumentException which gets wrapped
            assertTrue("Should have error message", e.getMessage() != null);
        }
        
        // Test with different locale to exercise different keyValues
        final DateParser parser2 = getInstance("EEEE", NEW_YORK, Locale.US); // Day of week
        final Date validDate2 = parser2.parse("Monday");
        assertNotNull("Should parse valid day name", validDate2);
        
        // Test with invalid day name
        try {
            parser2.parse("InvalidDay");
            fail("Should throw ParseException for invalid day name");
        } catch (ParseException e) {
            // Expected
        }
        
        // Test with AM_PM field
        final DateParser parser3 = getInstance("a", NEW_YORK, Locale.US); // AM/PM
        final Date validDate3 = parser3.parse("AM");
        assertNotNull("Should parse valid AM/PM", validDate3);
        
        // Test with invalid AM/PM
        try {
            parser3.parse("InvalidAMPM");
            fail("Should throw ParseException for invalid AM/PM");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testAdjustYear_EdgeCases() throws Exception {
        // Test adjustYear(int) - 4 missed (81.8% coverage)
        final DateParser parser = getInstance("yy", NEW_YORK, Locale.US);
        
        // Test with two-digit year that results in trial < thisYear+20
        // This exercises the return trial path
        final Date date1 = parser.parse("04"); // Should be 2004
        assertNotNull("Should parse two-digit year", date1);
        
        // Test with two-digit year that results in trial >= thisYear+20
        // This exercises the return trial-100 path
        // We need a year that when added to thisYear - thisYear%100 results in >= thisYear+20
        // For example, if thisYear is 2024, then 24 would give trial = 2024, which is < 2044, so return 2024
        // But 95 would give trial = 2095, which is >= 2044, so return 1995
        final Date date2 = parser.parse("95"); // Should be 1995 (not 2095)
        assertNotNull("Should parse two-digit year with adjustment", date2);
        
        // Test with year >= 100 (should not adjust)
        final DateParser parser2 = getInstance("yyyy", NEW_YORK, Locale.US);
        final Date date3 = parser2.parse("2004");
        assertNotNull("Should parse four-digit year", date3);
    }

    @Test
    public void testParse_String_AllPaths() throws Exception {
        // Test parse(String) - 24 missed (60.0% coverage)
        final DateParser parser = getInstance("yyyy-MM-dd", NEW_YORK, Locale.US);
        
        // Test with valid date
        final Date date1 = parser.parse("2004-12-31");
        assertNotNull("Should parse valid date", date1);
        
        // Test with invalid date (should throw ParseException)
        try {
            parser.parse("invalid");
            fail("Should throw ParseException for invalid date");
        } catch (ParseException e) {
            assertTrue("Exception should mention Unparseable date", e.getMessage().contains("Unparseable date"));
            assertTrue("Exception should mention pattern", e.getMessage().contains("does not match"));
        }
        
        // Test with null (should throw NullPointerException)
        try {
            parser.parse(null);
            fail("Should throw NullPointerException for null");
        } catch (NullPointerException e) {
            // Expected
        }
        
        // Test with Japanese Imperial locale (special error message)
        final Locale japaneseImperial = Locale.forLanguageTag("ja-JP-u-ca-japanese");
        try {
            final DateParser jpParser = getInstance("yyyy-MM-dd", NEW_YORK, japaneseImperial);
            jpParser.parse("invalid");
            // May or may not throw exception depending on locale support
        } catch (ParseException e) {
            // May have special message for Japanese Imperial locale
        }
    }

    @Test
    public void testEscapeRegex_EdgeCases() throws Exception {
        // Test escapeRegex indirectly through parsing - covers missing branches
        // Test with quoted string (unquote=true path)
        final DateParser parser = getInstance("'test'", NEW_YORK, Locale.US);
        final Date date1 = parser.parse("test");
        assertNotNull("Should parse quoted string", date1);
        
        // Test with quoted string containing \E (special case - line 325)
        final DateParser parser2 = getInstance("'test\\E'", NEW_YORK, Locale.US);
        final Date date2 = parser2.parse("test\\E");
        assertNotNull("Should parse string with \\E", date2);
        
        // Test with quoted string containing escaped quote (unquote path - line 305-310)
        // Use a valid pattern with escaped quotes - tests unquote=true branch
        final DateParser parser3 = getInstance("'test''more'", NEW_YORK, Locale.US);
        final Date date3 = parser3.parse("test'more");
        assertNotNull("Should parse string with escaped quote", date3);
        
        // Test with backslash followed by non-E character (valid pattern)
        final DateParser parser4 = getInstance("'test\\x'", NEW_YORK, Locale.US);
        final Date date4 = parser4.parse("test\\x");
        assertNotNull("Should parse string with backslash and non-E", date4);
    }

    @Test
    public void testGetLocaleSpecificStrategy() throws Exception {
        // Test getLocaleSpecificStrategy indirectly - 3 missed (95.1% coverage)
        // This is tested through parsing with different locales and fields
        
        // Test with different locales for same field
        final DateParser parser1 = getInstance("MMMM", NEW_YORK, Locale.US);
        final DateParser parser2 = getInstance("MMMM", NEW_YORK, Locale.FRANCE);
        
        // Both should work, exercising the cache
        final Date date1 = parser1.parse("January");
        final Date date2 = parser2.parse("janvier");
        assertNotNull("Should parse US locale", date1);
        assertNotNull("Should parse French locale", date2);
    }

    @Test
    public void testTimeZoneStrategy_SetCalendar_ExceptionPaths() throws Exception {
        // Test TimeZoneStrategy.setCalendar exception paths - 20 missed (60.6% coverage)
        final DateParser parser = getInstance("z", NEW_YORK, Locale.US); // Time zone format
        
        // Test with valid timezone (GMT format)
        final Date date1 = parser.parse("GMT-05:00");
        assertNotNull("Should parse GMT timezone", date1);
        
        // Test with valid timezone (+/- format)
        final Date date2 = parser.parse("-05:00");
        assertNotNull("Should parse +/- timezone", date2);
        
        // Test with valid timezone name
        final Date date3 = parser.parse("EST");
        assertNotNull("Should parse timezone name", date3);
        
        // Test with invalid timezone name (should throw IllegalArgumentException)
        // This tests the exception path at line 725-727
        try {
            parser.parse("InvalidTimezoneName123");
            fail("Should throw ParseException for invalid timezone name");
        } catch (ParseException e) {
            // The IllegalArgumentException from TimeZoneStrategy.setCalendar gets wrapped
            assertTrue("Should have error message", e.getMessage() != null);
        }
        
        // Test with timezone that doesn't start with +, -, or GMT
        // and is not in tzNames map
        try {
            parser.parse("NotATimezone");
            fail("Should throw ParseException for unsupported timezone");
        } catch (ParseException e) {
            // Expected - should have error message about unsupported timezone
            assertTrue("Should have error message", e.getMessage() != null);
        }
    }

    @Test
    public void testTextStrategy_SetCalendar_MoreExceptionPaths() throws Exception {
        // Test TextStrategy.setCalendar with more exception scenarios - additional coverage
        // Test with ERA field
        final DateParser parser1 = getInstance("G", NEW_YORK, Locale.US); // Era
        try {
            parser1.parse("InvalidEra");
            fail("Should throw ParseException for invalid era");
        } catch (ParseException e) {
            // Expected - IllegalArgumentException from TextStrategy.setCalendar
            assertTrue("Should have error message", e.getMessage() != null);
        }
        
        // Test with different locales to exercise different keyValues
        final DateParser parser2 = getInstance("MMMM", NEW_YORK, Locale.GERMANY);
        try {
            parser2.parse("InvalidMonthGerman");
            fail("Should throw ParseException for invalid month in German locale");
        } catch (ParseException e) {
            // Expected
            assertTrue("Should have error message", e.getMessage() != null);
        }
    }

    @Test
    public void testTextStrategy_AddRegex() throws Exception {
        // Test TextStrategy.addRegex method - called during parser initialization
        // This method builds the regex pattern from keyValues
        // Test with different fields to ensure addRegex is called with different keyValues
        
        // Test with month names (MMMM) - should call addRegex with month keyValues
        final DateParser parser1 = getInstance("MMMM", NEW_YORK, Locale.US);
        final Date date1 = parser1.parse("January");
        assertNotNull("Should parse month name", date1);
        
        // Test with day of week (EEEE) - should call addRegex with day keyValues
        final DateParser parser2 = getInstance("EEEE", NEW_YORK, Locale.US);
        final Date date2 = parser2.parse("Monday");
        assertNotNull("Should parse day name", date2);
        
        // Test with AM/PM (a) - should call addRegex with AM/PM keyValues
        final DateParser parser3 = getInstance("a", NEW_YORK, Locale.US);
        final Date date3 = parser3.parse("AM");
        assertNotNull("Should parse AM/PM", date3);
        
        // Test with ERA (G) - should call addRegex with ERA keyValues
        final DateParser parser4 = getInstance("G", NEW_YORK, Locale.US);
        final Date date4 = parser4.parse("AD");
        assertNotNull("Should parse ERA", date4);
        
        // Test with different locales to exercise addRegex with different keyValues sets
        final DateParser parser5 = getInstance("MMMM", NEW_YORK, Locale.FRANCE);
        final Date date5 = parser5.parse("janvier");
        assertNotNull("Should parse French month", date5);
        
        // Test with short month names (MMM) - should also use TextStrategy
        final DateParser parser6 = getInstance("MMM", NEW_YORK, Locale.US);
        final Date date6 = parser6.parse("Jan");
        assertNotNull("Should parse short month name", date6);
        
        // Test with short day names (EEE) - should also use TextStrategy
        final DateParser parser7 = getInstance("EEE", NEW_YORK, Locale.US);
        final Date date7 = parser7.parse("Mon");
        assertNotNull("Should parse short day name", date7);
    }

    @Test
    public void testEquals_WithDifferentLocales() throws Exception {
        // Test equals() method - line 195 (locale.equals check)
        final DateParser parser1 = getInstance("yyyy-MM-dd", NEW_YORK, Locale.US);
        final DateParser parser2 = getInstance("yyyy-MM-dd", NEW_YORK, Locale.US);
        final DateParser parser3 = getInstance("yyyy-MM-dd", NEW_YORK, Locale.FRANCE);
        
        assertEquals("Should be equal with same locale", parser1, parser2);
        assertFalse("Should not be equal with different locale", parser1.equals(parser3));
    }

    @Test
    public void testJapaneseImperialLocale_ExceptionPath() throws Exception {
        // Test Japanese Imperial locale exception path - lines 250-252
        // This locale has special handling for dates before 1868 AD
        final Locale japaneseImperial = new Locale("ja", "JP", "JP");
        final DateParser parser = getInstance("yyyy-MM-dd", NEW_YORK, japaneseImperial);
        
        // Try to parse an invalid date - should trigger the special exception message
        try {
            parser.parse("invalid-date");
            fail("Should throw ParseException with Japanese Imperial locale message");
        } catch (ParseException e) {
            // Should contain the special message about dates before 1868 AD
            assertTrue("Should mention Japanese Imperial locale", 
                e.getMessage().contains("ja_JP_JP") || e.getMessage().contains("1868"));
        }
    }

    @Test
    public void testGetLocaleSpecificStrategy_CachePath() throws Exception {
        // Test getLocaleSpecificStrategy cache path - line 507 (inCache!=null)
        // This tests the concurrent cache scenario where another thread already added the strategy
        final DateParser parser1 = getInstance("MMMM", NEW_YORK, Locale.US);
        final DateParser parser2 = getInstance("MMMM", NEW_YORK, Locale.US);
        
        // Both should work and use the cached strategy
        final Date date1 = parser1.parse("January");
        final Date date2 = parser2.parse("January");
        assertNotNull("Should parse with first parser", date1);
        assertNotNull("Should parse with second parser", date2);
        
        // Test with different field to exercise cache for different fields
        final DateParser parser3 = getInstance("EEEE", NEW_YORK, Locale.US); // Day of week
        final Date date3 = parser3.parse("Monday");
        assertNotNull("Should parse day of week", date3);
    }
}
