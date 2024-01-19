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


package com.publicissapient.kpidashboard.apis.jenkins.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jenkins.service.JenkinsKPIService;

@ExtendWith(SpringExtension.class)
public class JenkinsKPIServiceFactoryTest {

	@InjectMocks
	private JenkinsKPIServiceFactory jenkinsKPIServiceFactory;

	@Mock
	private List<JenkinsKPIService<?, ?, ?>> services;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetJenkinsKPIService() throws ApplicationException, NoSuchFieldException, IllegalAccessException {
		String type = "someType";
		JenkinsKPIService<?, ?, ?> expectedService = mock(JenkinsKPIService.class);

		Field cacheField = JenkinsKPIServiceFactory.class.getDeclaredField("JENKINS_SERVICE_CACHE");
		cacheField.setAccessible(true);
		Map<String, JenkinsKPIService<?, ?, ?>> cache = (Map<String, JenkinsKPIService<?, ?, ?>>) cacheField
				.get(jenkinsKPIServiceFactory);
		cache.put(type, expectedService);

		JenkinsKPIService<?, ?, ?> resultService = JenkinsKPIServiceFactory.getJenkinsKPIService(type);

		assertEquals(expectedService, resultService);
	}

	@Test
	public void testInitMyServiceCache() {
		JenkinsKPIService<?, ?, ?> service1 = mock(JenkinsKPIService.class);
		JenkinsKPIService<?, ?, ?> service2 = mock(JenkinsKPIService.class);
		when(service1.getQualifierType()).thenReturn("type1");
		when(service2.getQualifierType()).thenReturn("type2");
		when(services.iterator()).thenReturn(Arrays.asList(service1, service2).iterator());

		jenkinsKPIServiceFactory.initMyServiceCache();

		try {
			Field cacheField = JenkinsKPIServiceFactory.class.getDeclaredField("JENKINS_SERVICE_CACHE");
			cacheField.setAccessible(true);
			Map<String, JenkinsKPIService<?, ?, ?>> cache = (Map<String, JenkinsKPIService<?, ?, ?>>) cacheField
					.get(jenkinsKPIServiceFactory);

			assertNotNull(cache.get("type1"));
			assertNotNull(cache.get("type2"));
		} catch (NoSuchFieldException | IllegalAccessException e) {
			fail("Failed to access the private field: " + e.getMessage());
		}
	}

}
