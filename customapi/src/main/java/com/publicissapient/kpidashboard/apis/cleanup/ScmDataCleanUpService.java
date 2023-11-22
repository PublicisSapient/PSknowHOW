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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;
import com.publicissapient.kpidashboard.common.repository.scm.CommitRepository;
import com.publicissapient.kpidashboard.common.repository.scm.MergeRequestRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

/**
 * @author anisingh4
 */
@Service
public class ScmDataCleanUpService implements ToolDataCleanUpService {

	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Autowired
	private ProcessorItemRepository processorItemRepository;

	@Autowired
	private CommitRepository commitRepository;

	@Autowired
	private MergeRequestRepository mergReqRepo;

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
		return ProcessorType.SCM.toString();
	}

	@Override
	public void clean(String projectToolConfigId) {
		ProjectToolConfig tool = projectToolConfigRepository.findById(projectToolConfigId);
		if (tool != null) {
			List<ObjectId> itemsIds = getProcessorItemsIds(tool);

			// delete corresponding documents from commit_details
			commitRepository.deleteByProcessorItemIdIn(itemsIds);

			// delete corresponding documents from merge_requests
			mergReqRepo.deleteByProcessorItemIdIn(itemsIds);

			// delete corresponding documents from processor_items
			processorItemRepository.deleteByToolConfigId(tool.getId());

			// delete processors trace logs
			processorExecutionTraceLogRepository.deleteByBasicProjectConfigIdAndProcessorName(
					tool.getBasicProjectConfigId().toHexString(), tool.getToolName());

			cleanCache(tool);

		}
	}

	private void cleanCache(ProjectToolConfig tool) {
		cacheService.clearCache(CACHE_TOOL_CONFIG_MAP);
		if (tool.getToolName().equalsIgnoreCase(ProcessorConstants.BITBUCKET)
				|| tool.getToolName().equalsIgnoreCase(ProcessorConstants.GITLAB)
				|| tool.getToolName().equalsIgnoreCase(ProcessorConstants.GITHUB)
				|| tool.getToolName().equalsIgnoreCase(ProcessorConstants.AZUREREPO)
				|| tool.getToolName().equalsIgnoreCase(ProcessorConstants.REPO_TOOLS)) {
			cacheService.clearCache(CommonConstant.BITBUCKET_KPI_CACHE);
		}
		if (tool.getToolName().equalsIgnoreCase(ProcessorConstants.GITLAB)) {
			cacheService.clearCache(CommonConstant.GITLAB_KPI_CACHE);
		}
		if (tool.getToolName().equalsIgnoreCase(ProcessorConstants.AZUREREPO)) {
			cacheService.clearCache(CommonConstant.AZURE_KPI_CACHE);
		}

	}
}
