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

package com.publicissapient.kpidashboard.common.util;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EncryptionExceptionTest {

	@Test
	public void testEmptyConstructor() {
		EncryptionException exception = new EncryptionException();
		assertNull(exception.getMessage());
		assertNull(exception.getCause());
	}

	@Test
	public void testMessageConstructor() {
		String message = "Test Exception";
		EncryptionException exception = new EncryptionException(message);
		assertEquals(message, exception.getMessage());
		assertNull(exception.getCause());
	}

	@Test
	public void testMessageAndThrowableConstructor() {
		String message = "Test Exception";
		Throwable cause = new RuntimeException("Test Cause");
		EncryptionException exception = new EncryptionException(message, cause);
		assertEquals(message, exception.getMessage());
		assertEquals(cause, exception.getCause());
	}
}
