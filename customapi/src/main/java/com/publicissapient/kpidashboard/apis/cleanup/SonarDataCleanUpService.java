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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

/**
 * @author anisingh4
 */
@Service
public class SonarDataCleanUpService implements ToolDataCleanUpService {

	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Autowired
	private ProcessorItemRepository processorItemRepository;

	@Autowired
	private SonarDetailsRepository sonarDetailsRepository;

	@Autowired
	private SonarHistoryRepository sonarHistoryRepository;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	private List<ObjectId> getProcessorItemsIds(ProjectToolConfig tool) {
		List<ProcessorItem> items = processorItemRepository.findByToolConfigId(tool.getId());

		return CollectionUtils.emptyIfNull(items).stream().map(ProcessorItem::getId).collect(Collectors.toList());
	}

	@Override
	public String getToolCategory() {
		return ProcessorType.SONAR_ANALYSIS.toString();
	}

	@Override
	public void clean(String projectToolConfigId) {
		ProjectToolConfig tool = projectToolConfigRepository.findById(projectToolConfigId);
		if (tool != null) {

			List<ObjectId> itemsIds = getProcessorItemsIds(tool);

			sonarDetailsRepository.deleteByProcessorItemIdIn(itemsIds);
			sonarHistoryRepository.deleteByProcessorItemIdIn(itemsIds);
			processorItemRepository.deleteByToolConfigId(tool.getId());

			// delete processors trace logs
			processorExecutionTraceLogRepository.deleteByBasicProjectConfigIdAndProcessorName(
					tool.getBasicProjectConfigId().toHexString(), tool.getToolName());

			cacheService.clearCache(CommonConstant.CACHE_TOOL_CONFIG_MAP);
			cacheService.clearCache(CommonConstant.SONAR_KPI_CACHE);
		}
	}
}
