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
package org.apache.commons.lang3.concurrent;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Test class for {@link ConcurrentException} and {@link ConcurrentRuntimeException}.
 *
 * @version $Id$
 */
public class ConcurrentExceptionTest {

    /**
     * Tests the protected no-arg constructor of ConcurrentException.
     * This constructor is accessible from the same package.
     */
    @Test
    public void testConcurrentExceptionProtectedConstructor() {
        ConcurrentException ex = new ConcurrentException();
        assertNotNull("Exception should be created", ex);
        assertNull("Cause should be null", ex.getCause());
        assertNull("Message should be null", ex.getMessage());
    }

    /**
     * Tests the protected no-arg constructor of ConcurrentRuntimeException.
     * This constructor is accessible from the same package.
     */
    @Test
    public void testConcurrentRuntimeExceptionProtectedConstructor() {
        ConcurrentRuntimeException ex = new ConcurrentRuntimeException();
        assertNotNull("Exception should be created", ex);
        assertNull("Cause should be null", ex.getCause());
        assertNull("Message should be null", ex.getMessage());
    }
}

