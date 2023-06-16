package com.publicissapient.kpidashboard.apis.cleanup;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;

@RunWith(MockitoJUnitRunner.class)
public class ZephyrDataCleanUpServiceTest {

	@InjectMocks
	private ZephyrDataCleanUpService zephyrDataCleanUpService;

	@Mock
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Mock
	private TestCaseDetailsRepository testCaseDetailsRepository;

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Mock
	private CacheService cacheService;

	@Test
	public void getToolCategory() {
		String actualResult = zephyrDataCleanUpService.getToolCategory();
		assertEquals(ProcessorType.TESTING_TOOLS.toString(), actualResult);
	}

	@Test
	public void cleanZephyrData() {
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setId(new ObjectId("5e9e4593e4b0c8ece56710c3"));
		projectToolConfig.setBasicProjectConfigId(new ObjectId("5e9db8f1e4b0caefbfa8e0c7"));
		projectToolConfig.setToolName(ProcessorConstants.ZEPHYR);
		when(projectToolConfigRepository.findById(anyString())).thenReturn(projectToolConfig);
		doNothing().when(testCaseDetailsRepository).deleteByBasicProjectConfigId(anyString());
		doNothing().when(processorExecutionTraceLogRepository)
				.deleteByBasicProjectConfigIdAndProcessorName(Mockito.any(), Mockito.anyString());
		zephyrDataCleanUpService.clean("5e9db8f1e4b0caefbfa8e0c7");
		verify(testCaseDetailsRepository, times(1)).deleteByBasicProjectConfigId("5e9db8f1e4b0caefbfa8e0c7");
		verify(processorExecutionTraceLogRepository, times(1))
				.deleteByBasicProjectConfigIdAndProcessorName("5e9db8f1e4b0caefbfa8e0c7", ProcessorConstants.ZEPHYR);
	}
}
