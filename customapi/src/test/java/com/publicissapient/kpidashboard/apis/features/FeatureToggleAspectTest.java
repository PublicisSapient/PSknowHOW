/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.features;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.util.NamedFeature;

/**
 * @author anisingh4
 */
@RunWith(MockitoJUnitRunner.class)
public class FeatureToggleAspectTest {

	@InjectMocks
	FeatureToggleAspect featureToggleAspect;
	@Mock
	private FeatureManager featureManager;
	@Mock
	private ProceedingJoinPoint proceedingJoinPoint;
	@Mock
	private EnableFeatureToggle enableFeatureToggle;

	@Test
	public void invoke_FeatureEnabled() throws Throwable {
		when(enableFeatureToggle.name()).thenReturn("TEST");
		when(featureManager.isActive(any(NamedFeature.class))).thenReturn(true);
		featureToggleAspect.invoke(proceedingJoinPoint, enableFeatureToggle);
		verify(proceedingJoinPoint, times(1)).proceed();
	}

	@Test
	public void invoke_FeatureDisabled() throws Throwable {
		when(enableFeatureToggle.name()).thenReturn("TEST");
		when(featureManager.isActive(any(NamedFeature.class))).thenReturn(false);
		featureToggleAspect.invoke(proceedingJoinPoint, enableFeatureToggle);
		verify(proceedingJoinPoint, never()).proceed();
	}
}