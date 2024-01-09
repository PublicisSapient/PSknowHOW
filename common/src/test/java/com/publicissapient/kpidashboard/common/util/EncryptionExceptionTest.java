package com.publicissapient.kpidashboard.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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
