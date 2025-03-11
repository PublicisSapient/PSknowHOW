package com.publicissapient.kpidashboard.rally.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TrackExecutionTimeAspectTest {

    @InjectMocks
    private PerformanceLoggingAspect performanceLoggingAspect;

    private ProceedingJoinPoint proceedingJoinPoint;
    private MethodSignature methodSignature;

    @BeforeEach
    public void setup() {
        proceedingJoinPoint = mock(ProceedingJoinPoint.class);
        methodSignature = mock(MethodSignature.class);
    }

    @Test
    public void testExecutionTime() throws Throwable {
        // Setup
        String className = "TestClass";
        String methodName = "testMethod";
        String returnValue = "Test Result";

        // Mock method signature
        when(proceedingJoinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getDeclaringType()).thenReturn(TestClass.class);
        when(methodSignature.getName()).thenReturn(methodName);
        when(proceedingJoinPoint.proceed()).thenReturn(returnValue);

        // Execute
        Object result = performanceLoggingAspect.executionTime(proceedingJoinPoint);

        // Verify
        assertEquals(returnValue, result);
    }

    @Test
    public void testExecutionTimeWithException() throws Throwable {
        // Setup
        String className = "TestClass";
        String methodName = "testMethod";
        RuntimeException exception = new RuntimeException("Test Exception");

        // Mock method signature
        when(proceedingJoinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getDeclaringType()).thenReturn(TestClass.class);
        when(methodSignature.getName()).thenReturn(methodName);
        when(proceedingJoinPoint.proceed()).thenThrow(exception);

        try {
            // Execute
            performanceLoggingAspect.executionTime(proceedingJoinPoint);
        } catch (RuntimeException e) {
            // Verify
            assertEquals(exception, e);
        }
    }

    private static class TestClass {
    }
}
