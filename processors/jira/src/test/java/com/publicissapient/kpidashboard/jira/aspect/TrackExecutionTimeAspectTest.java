package com.publicissapient.kpidashboard.jira.aspect;

import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.testng.AssertJUnit.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TrackExecutionTimeAspectTest {
	@Mock
	private ProceedingJoinPoint proceedingJoinPoint;
	@Mock
	private MethodSignature methodSignature;
	@Mock
	private TrackExecutionTime trackExecutionTimeAnnotation;
	@InjectMocks
	private PerformanceLoggingAspect performanceLoggingAspect;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
    public void testExecutionTime() throws Throwable {
// Mock method signature details
        when(proceedingJoinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getDeclaringType()).thenReturn(getClass());
        when(methodSignature.getName()).thenReturn("testMethod");
// Mock StopWatch
        StopWatch stopWatch = mock(StopWatch.class);

// Mock proceedingJoinPoint.proceed() result
        Object expectedResult = new Object(); // Mock your expected result
        when(proceedingJoinPoint.proceed()).thenReturn(expectedResult);
// Call the method
        Object actualResult = performanceLoggingAspect.executionTime(proceedingJoinPoint);
// Add assertions for the actual and expected results
        assertEquals(expectedResult, actualResult);
    }
}