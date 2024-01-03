package com.publicissapient.kpidashboard.common.config;

import com.publicissapient.kpidashboard.common.util.ProcessorErrorHandler;
import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class TaskSchedulerConfigurationTest {

	private TaskSchedulerConfiguration taskSchedulerConfiguration;

	@Before
	public void setUp() {
		ProcessorErrorHandler processorErrorHandler = mock(ProcessorErrorHandler.class);
		taskSchedulerConfiguration = new TaskSchedulerConfiguration();
	}

	@Test
	public void testTaskSchedulerBean() {
		TaskScheduler taskScheduler = taskSchedulerConfiguration.taskScheduler();
		assertNotNull(taskScheduler);
		assertTrue(taskScheduler instanceof ThreadPoolTaskScheduler);
	}
}
