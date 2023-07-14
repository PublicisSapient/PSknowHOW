package com.publicissapient.kpidashboard.zephyr.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.zephyr.client.ZephyrClient;
import com.publicissapient.kpidashboard.zephyr.config.ZephyrConfig;
import com.publicissapient.kpidashboard.zephyr.factory.ZephyrClientFactory;
import com.publicissapient.kpidashboard.zephyr.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.zephyr.model.ZephyrProcessor;
import com.publicissapient.kpidashboard.zephyr.processor.service.ZephyrDBService;
import com.publicissapient.kpidashboard.zephyr.processor.service.impl.ZephyrCloudImpl;
import com.publicissapient.kpidashboard.zephyr.processor.service.impl.ZephyrServerImpl;
import com.publicissapient.kpidashboard.zephyr.repository.ZephyrProcessorRepository;

@ExtendWith(SpringExtension.class)
public class ZephyrProcessorJobExecutorTest {

	private final ObjectId PROCESSOR_ID = new ObjectId("5e16dc92f1aab3fbb1b198f3");

	@InjectMocks
	private ZephyrProcessorJobExecutor zephyrProcessorJobExecutor;

	@Mock
	private ZephyrProcessorRepository zephyrProcessorRepository;

	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;

	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;

	@Mock
	private ZephyrProcessor zephyrProcessor;

	@Mock
	private TaskScheduler taskScheduler;

	@Mock
	private ZephyrServerImpl zephyrServer;

	@Mock
	private ZephyrCloudImpl zephyrCloud;

	@Mock
	private ZephyrClient zephyrClient;

	@Mock
	private ZephyrConfig zephyrConfig;

	@Mock
	private ZephyrClientFactory zephyrClientFactory;

	@Mock
	private ZephyrDBService zephyrDBService;

	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();

	private ProjectConfFieldMapping projectConfFieldMapping;

	private ProjectBasicConfig projectBasicConfig;

	private List<ProcessorToolConnection> toolList = new ArrayList<>();

	private ProcessorToolConnection toolInfo;

	@BeforeEach
	public void init() {
		toolInfo = new ProcessorToolConnection();
		toolInfo.setBasicProjectConfigId(new ObjectId("625fd013572701449a44b3de"));
		toolInfo.setUrl("https://test.com/jira");
		toolInfo.setApiEndPoint("/rest/atm/1.0");
		toolInfo.setUsername("test");
		toolInfo.setPassword("password");
		toolInfo.setProjectKey("TEST");
		toolInfo.setConnectionId(new ObjectId("625d0d9d10ce157f45918b5c"));
		toolInfo.setCloudEnv(false);
		toolList.add(toolInfo);
		zephyrProcessorJobExecutor = new ZephyrProcessorJobExecutor(taskScheduler);
		this.zephyrProcessor = zephyrProcessorJobExecutor.getProcessor();
		MockitoAnnotations.openMocks(this);
		projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("604092b52b424d5e90d39342"));

		projectBasicConfig.setConsumerCreatedOn("consumerCreatedOn");
		projectBasicConfig.setIsKanban(true);
		projectBasicConfig.setProjectName("projectName");
		projectBasicConfig.setUpdatedAt("updatedAt");
		projectConfigList.add(projectBasicConfig);

		projectConfFieldMapping = new ProjectConfFieldMapping();
		projectConfFieldMapping.setProjectKey("TEST");

	}

	@Test
	public void getProcessorTest() {
		assertNotNull(zephyrProcessorJobExecutor.getProcessor());
	}

	@Test
	public void getProcessorRepositoryTest() {
		assertEquals(zephyrProcessorRepository, zephyrProcessorJobExecutor.getProcessorRepository());
	}

	@Test
	public void execute() {
		ZephyrProcessor zephyrProcessor = new ZephyrProcessor();
		zephyrProcessor.setId(PROCESSOR_ID);
		zephyrProcessorJobExecutor.setProjectsBasicConfigIds(
				Arrays.asList("604092b52b424d5e90d39342", "604092b52b424d5e90d39343", "604092b52b424d5e90d39344"));
		when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(ProcessorConstants.ZEPHYR,
				new ObjectId("604092b52b424d5e90d39342"))).thenReturn(toolList);
		when(zephyrClientFactory.getClient(false)).thenReturn(zephyrServer);
		assertTrue(zephyrProcessorJobExecutor.execute(zephyrProcessor));
	}

	@Test
	public void testCron() {
		when(zephyrConfig.getCron()).thenReturn("0 0 0 0 0");
		assertNotNull(zephyrProcessorJobExecutor.getCron());
	}
}
