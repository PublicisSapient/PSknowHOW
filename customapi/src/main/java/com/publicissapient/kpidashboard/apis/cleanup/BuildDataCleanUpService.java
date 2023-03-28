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

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.CACHE_TOOL_CONFIG_MAP;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
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
@Service
public class BuildDataCleanUpService implements ToolDataCleanUpService {

	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Autowired
	private ProcessorItemRepository processorItemRepository;

	@Autowired
	private BuildRepository buildRepository;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private DeploymentRepository deploymentRepository;

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Override
	public String getToolCategory() {
		return ProcessorType.BUILD.toString();
	}

	@Override
	public void clean(String projectToolConfigId) {
		ProjectToolConfig tool = projectToolConfigRepository.findById(projectToolConfigId);
		if (tool != null) {
			// delete corresponding deployment details from deployments
			deploymentRepository.deleteDeploymentByProjectToolConfigId(tool.getId());

			// delete corresponding documents from build_details
			buildRepository.deleteByProjectToolConfigId(tool.getId());

			// delete corresponding documents from processor_items
			processorItemRepository.deleteByToolConfigId(tool.getId());

			// delete processors trace logs
			processorExecutionTraceLogRepository.deleteByBasicProjectConfigIdAndProcessorName(
					tool.getBasicProjectConfigId().toHexString(), tool.getToolName());

			cacheService.clearCache(CACHE_TOOL_CONFIG_MAP);
			cacheService.clearCache(CommonConstant.JENKINS_KPI_CACHE);

		}
	}

}
