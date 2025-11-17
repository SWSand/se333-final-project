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
package org.apache.commons.lang3.text.translate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;

/**
 * Unit tests for {@link org.apache.commons.lang3.text.translate.CharSequenceTranslator}.
 * @version $Id$
 */
public class CharSequenceTranslatorTest {

    @Test
    public void testTranslateNullInput() {
        // Test translate(CharSequence) with null input
        final CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
                out.write("test");
                return input.length();
            }
        };
        
        final String result = translator.translate((CharSequence) null);
        assertNull("Null input should return null", result);
    }

    @Test
    public void testTranslateIOException() {
        // Test translate(CharSequence) IOException path
        // Create a translator that throws IOException
        final CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
                throw new IOException("Test IOException");
            }
        };
        
        try {
            translator.translate("test");
            fail("Should have thrown RuntimeException");
        } catch (final RuntimeException e) {
            assertTrue("Should wrap IOException", e.getCause() instanceof IOException);
            assertEquals("Test IOException", e.getCause().getMessage());
        }
    }

    @Test
    public void testTranslateWithWriterNullWriter() throws Exception {
        final CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
                out.write("test");
                return input.length();
            }
        };
        
        try {
            translator.translate("test", (Writer) null);
            fail("Should have thrown IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            assertTrue("Exception message should mention Writer", e.getMessage().contains("Writer"));
        }
    }

    @Test
    public void testTranslateWithWriterNullInput() throws Exception {
        final CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
                out.write("test");
                return input.length();
            }
        };
        
        final StringWriter writer = new StringWriter();
        translator.translate((CharSequence) null, writer);
        // Should not throw exception and writer should be unchanged
        assertEquals("", writer.toString());
    }
}

