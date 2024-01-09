package com.publicissapient.kpidashboard.common.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
