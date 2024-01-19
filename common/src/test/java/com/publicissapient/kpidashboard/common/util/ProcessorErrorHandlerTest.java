package com.publicissapient.kpidashboard.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProcessorErrorHandlerTest {

    @InjectMocks
    private ProcessorErrorHandler errorHandler;

    @BeforeEach
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
