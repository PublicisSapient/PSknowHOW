/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.common.exceptions;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ApplicationExceptionTest {

	@Test
	public void testApplicationExceptionWithMessageAndErrorCode() {
		ApplicationException exception = new ApplicationException("Test message", 123);
		assertEquals("Test message", exception.getMessage());
		assertEquals(123, exception.getErrorCode());
	}

	@Test
	public void testApplicationExceptionWithMessageCauseAndErrorCode() {
		Throwable cause = new RuntimeException("Cause");
		ApplicationException exception = new ApplicationException("Test message", cause, 456);
		assertEquals("Test message", exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertEquals(456, exception.getErrorCode());
	}

	@Test
	public void testApplicationExceptionWithCause() {
		Throwable cause = new RuntimeException("Cause");
		ApplicationException exception = new ApplicationException(cause);
		assertEquals(cause, exception.getCause());
	}

	@Test
	public void testApplicationExceptionWithFullConstructor() {
		Throwable cause = new RuntimeException("Cause");
		ApplicationException exception = new ApplicationException("Test message", cause, true, true);
		assertEquals("Test message", exception.getMessage());
		assertEquals(cause, exception.getCause());
		assertTrue(exception.getSuppressed().length == 0); // No suppressed exceptions
		assertTrue(exception.getStackTrace().length > 0); // Stack trace available
	}
}
