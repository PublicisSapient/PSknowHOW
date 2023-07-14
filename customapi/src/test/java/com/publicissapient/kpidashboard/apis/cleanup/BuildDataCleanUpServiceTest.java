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

package com.publicissapient.kpidashboard.apis.cleanup;

import static org.junit.Assert.assertEquals;
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
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

/**
 * @author anisingh4
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildDataCleanUpServiceTest {

	@Mock
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Mock
	private ProcessorItemRepository processorItemRepository;

	@Mock
	private BuildRepository buildRepository;

	@Mock
	private DeploymentRepository deploymentRepository;

	@Mock
	private CacheService cacheService;

	@InjectMocks
	private BuildDataCleanUpService buildDataCleanupService;
	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Test
	public void getToolCategory() {
		String actualResult = buildDataCleanupService.getToolCategory();
		assertEquals(ProcessorType.BUILD.toString(), actualResult);
	}

	@Test
	public void clean() {
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setId(new ObjectId("5e9e4593e4b0c8ece56710c3"));
		projectToolConfig.setBasicProjectConfigId(new ObjectId("5e9db8f1e4b0caefbfa8e0c7"));
		projectToolConfig.setToolName(ProcessorConstants.JENKINS);
		when(projectToolConfigRepository.findById(Mockito.anyString())).thenReturn(projectToolConfig);
		doNothing().when(buildRepository).deleteByProjectToolConfigId(projectToolConfig.getId());
		doNothing().when(processorItemRepository).deleteByToolConfigId(Mockito.any(ObjectId.class));
		doNothing().when(processorExecutionTraceLogRepository)
				.deleteByBasicProjectConfigIdAndProcessorName(Mockito.any(), Mockito.anyString());
		doNothing().when(cacheService).clearCache(Mockito.anyString());
		buildDataCleanupService.clean("5e9e4593e4b0c8ece56710c3");
		verify(processorItemRepository, times(1)).deleteByToolConfigId(new ObjectId("5e9e4593e4b0c8ece56710c3"));
		verify(processorExecutionTraceLogRepository, times(1))
				.deleteByBasicProjectConfigIdAndProcessorName("5e9db8f1e4b0caefbfa8e0c7", ProcessorConstants.JENKINS);

	}
}