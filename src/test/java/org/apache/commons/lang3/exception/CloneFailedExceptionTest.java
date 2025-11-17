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
package org.apache.commons.lang3.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * Test class for {@link CloneFailedException}.
 *
 * @version $Id$
 */
public class CloneFailedExceptionTest {

    @Test
    public void testConstructorWithMessage() {
        final String message = "Test message";
        final CloneFailedException ex = new CloneFailedException(message);
        assertNotNull("Exception should not be null", ex);
        assertEquals("Message should match", message, ex.getMessage());
        assertNull("Cause should be null", ex.getCause());
    }

    @Test
    public void testConstructorWithCause() {
        final Throwable cause = new Exception("Test Cause");
        final CloneFailedException ex = new CloneFailedException(cause);
        assertNotNull("Exception should not be null", ex);
        assertSame("Cause should be the same", cause, ex.getCause());
        assertNotNull("Message should not be null", ex.getMessage());
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        final String message = "Test message";
        final Throwable cause = new Exception("Test Cause");
        final CloneFailedException ex = new CloneFailedException(message, cause);
        assertNotNull("Exception should not be null", ex);
        assertEquals("Message should match", message, ex.getMessage());
        assertSame("Cause should be the same", cause, ex.getCause());
    }
}

