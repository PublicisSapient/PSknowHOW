package com.publicissapient.kpidashboard.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnsafeDeleteExceptionTest {

    @Test
    public void testConstructorWithoutArguments() {
        UnsafeDeleteException exception = new UnsafeDeleteException();
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testConstructorWithMessage() {
        String message = "Test Message";
        UnsafeDeleteException exception = new UnsafeDeleteException(message);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testConstructorWithMessageAndThrowable() {
        String message = "Test Message";
        Throwable cause = new RuntimeException("Test Cause");
        UnsafeDeleteException exception = new UnsafeDeleteException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConstructorWithThrowable() {
        Throwable cause = new RuntimeException("Test Cause");
        UnsafeDeleteException exception = new UnsafeDeleteException(cause);
        assertEquals(cause, exception.getCause());
        assertEquals("java.lang.RuntimeException: Test Cause", exception.getMessage());
    }
}
