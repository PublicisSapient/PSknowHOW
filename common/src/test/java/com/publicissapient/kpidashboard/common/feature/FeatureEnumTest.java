package com.publicissapient.kpidashboard.common.feature;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.spring.boot.actuate.TogglzEndpoint;
import org.togglz.spring.util.ContextClassLoaderApplicationContextHolder;

import java.util.Set;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestTogglzConfig.class)
public class FeatureEnumTest {

	@Autowired
	private FeatureManager featureManager;

	@Test
	public void testDailyStandupFeatureIsActive() {
		assertTrue(featureManager.isActive(FeatureEnum.DAILY_STANDUP));
	}

}