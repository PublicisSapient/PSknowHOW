package com.publicissapient.kpidashboard.apis.jenkins.factory;

import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jenkins.service.JenkinsKPIService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JenkinsKPIServiceFactoryTest {

	@InjectMocks
	private JenkinsKPIServiceFactory jenkinsKPIServiceFactory;

	@Mock
	private List<JenkinsKPIService<?, ?, ?>> services;

	@Before
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
