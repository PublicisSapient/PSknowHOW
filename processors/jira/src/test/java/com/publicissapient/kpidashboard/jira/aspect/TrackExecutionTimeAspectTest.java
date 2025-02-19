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

package com.publicissapient.kpidashboard.jira.aspect;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.AssertJUnit.assertEquals;

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
		MockitoAnnotations.openMocks(this);
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
