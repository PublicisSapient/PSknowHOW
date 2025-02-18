package com.publicissapient.kpidashboard.common.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.togglz.core.manager.FeatureManager;

@ExtendWith(SpringExtension.class)
public class FeaturesAspectTest {

	@InjectMocks
	FeaturesAspect aspect;
	@Mock
	private FeatureManager manager;
	@Mock
	private ProceedingJoinPoint proceedingJoinPoint;
	@Mock
	private FeatureAssociation enableFeatureToggle;

	@Test
	public void shouldProceedWhenFeatureIsActive() throws Throwable {

		ProceedingJoinPoint mockJoinPoint = mock(ProceedingJoinPoint.class);
		when(mockJoinPoint.proceed()).thenReturn("Test value");

		FeatureEnum mock = mock(FeatureEnum.class);
		when(enableFeatureToggle.value()).thenReturn(mock);
		when(mock.isActive()).thenReturn(Boolean.TRUE);

		// Call the aspect method
		Object result = aspect.checkAspect(mockJoinPoint, enableFeatureToggle);

		// Assert that the join point was proceeded and the result is correct
		assertEquals("Test value", result);
	}

	@Test
	public void shouldProceedWhenFeatureIsInActive() throws Throwable {

		ProceedingJoinPoint mockJoinPoint = mock(ProceedingJoinPoint.class);

		FeatureEnum mock = mock(FeatureEnum.class);
		when(enableFeatureToggle.value()).thenReturn(mock);
		when(mock.isActive()).thenReturn(Boolean.FALSE);

		// Call the aspect method
		Object result = aspect.checkAspect(mockJoinPoint, enableFeatureToggle);

		// Assert that the join point was proceeded and the result is correct
		assertNull(result);
	}
}
