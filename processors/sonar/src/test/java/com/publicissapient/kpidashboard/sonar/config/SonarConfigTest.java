package com.publicissapient.kpidashboard.sonar.config;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class SonarConfigTest {

	@InjectMocks
	private SonarConfig sonarConfig;

	@Value("${aesEncryptionKey}")
	private String aesEncryptionKey;

	@Test
	public void testSonarConfigProperties() {
		// Arrange
		String cron = "0 0 0 * * ?";
		List<String> metrics = Arrays.asList("metric1", "metric2");
		String customApiBaseUrl = "http://example.com/sonar/api";
		int pageSize = 100;

		// Act
		sonarConfig.setCron(cron);
		sonarConfig.setMetrics(metrics);
		sonarConfig.setCustomApiBaseUrl(customApiBaseUrl);
		sonarConfig.setPageSize(pageSize);

		// Assert
		assertEquals(cron, sonarConfig.getCron());
		assertEquals(metrics, sonarConfig.getMetrics());
		assertEquals(customApiBaseUrl, sonarConfig.getCustomApiBaseUrl());
		assertEquals(pageSize, sonarConfig.getPageSize());
	}
}
