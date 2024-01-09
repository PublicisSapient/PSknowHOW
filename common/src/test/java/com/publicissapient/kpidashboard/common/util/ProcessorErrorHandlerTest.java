package com.publicissapient.kpidashboard.common.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class ProcessorErrorHandlerTest {

	@InjectMocks
	private ProcessorErrorHandler errorHandler;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testHandleError() {
		Throwable mockThrowable = mock(Throwable.class);
		when(mockThrowable.getMessage()).thenReturn("Test error message");
		when(mockThrowable.getCause()).thenReturn(new RuntimeException("Test cause"));
		errorHandler.handleError(mockThrowable);
	}
}
