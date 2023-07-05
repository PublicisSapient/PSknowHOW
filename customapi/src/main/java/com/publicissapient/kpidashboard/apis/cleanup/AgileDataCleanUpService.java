/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.cleanup;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@Service
@Slf4j
public class AgileDataCleanUpService implements ToolDataCleanUpService {

	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private KanbanJiraIssueRepository kanbanJiraIssueRepository;

	@Autowired
	private KanbanJiraIssueHistoryRepository kanbanJiraIssueHistoryRepository;

	@Autowired
	private ProjectBasicConfigService projectBasicConfigService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private ProjectReleaseRepo projectReleaseRepo;

	@Autowired
	private TestCaseDetailsRepository testCaseDetailsRepository;

	@Autowired
	private FieldMappingRepository fieldMappingRepository;

	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;

	@Autowired
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepository;

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Autowired
	private SprintRepository sprintRepository;


	private static void getLevelIds(boolean flag, List<String> levelList, List<HierarchyLevel> accountHierarchyList) {
		for (HierarchyLevel hierarchyLevel : accountHierarchyList) {
			if (flag) {
				levelList.add(hierarchyLevel.getHierarchyLevelId());
			}
			if (StringUtils.isNotEmpty(hierarchyLevel.getHierarchyLevelId()) && hierarchyLevel.getHierarchyLevelId()
					.equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT)) {
				flag = true;
			}
		}
	}

	@Override
	public String getToolCategory() {
		return ProcessorType.AGILE_TOOL.toString();
	}

	@Override
	public void clean(String projectToolConfigId) {

		ProjectToolConfig tool = projectToolConfigRepository.findById(projectToolConfigId);
		deleteJiraIssuesAndHistory(tool);
		deleteReleaseInfo(tool);
		deleteSprintDetailsData(tool);
		clearCache();

	}

	private ProjectBasicConfig getProjectBasicConfig(String basicProjectConfigId) {
		return projectBasicConfigService.getProjectBasicConfigs(basicProjectConfigId);
	}

	private void deleteJiraIssuesAndHistory(ProjectToolConfig tool) {
		if (tool != null) {
			String basicProjectConfigId = tool.getBasicProjectConfigId().toHexString();
			ProjectBasicConfig projectBasicConfig = getProjectBasicConfig(basicProjectConfigId);
			processorExecutionTraceLogRepository.deleteByBasicProjectConfigIdAndProcessorName(basicProjectConfigId,
					tool.getToolName());

			if (projectBasicConfig.getIsKanban()) {
				kanbanJiraIssueRepository.deleteByBasicProjectConfigId(basicProjectConfigId);
				kanbanJiraIssueHistoryRepository.deleteByBasicProjectConfigId(basicProjectConfigId);
				boolean flag = false;
				List<String> levelList = new ArrayList<>();
				List<HierarchyLevel> accountHierarchyList = cacheService.getFullKanbanHierarchyLevel();
				getLevelIds(flag, levelList, accountHierarchyList);
				kanbanAccountHierarchyRepository
						.deleteByBasicProjectConfigIdAndLabelNameIn(tool.getBasicProjectConfigId(), levelList);

			} else {
				jiraIssueRepository.deleteByBasicProjectConfigId(basicProjectConfigId);
				jiraIssueCustomHistoryRepository.deleteByBasicProjectConfigId(basicProjectConfigId);
				boolean flag = false;
				List<String> levelList = new ArrayList<>();
				List<HierarchyLevel> accountHierarchyList = cacheService.getFullHierarchyLevel();
				getLevelIds(flag, levelList, accountHierarchyList);
				accountHierarchyRepository.deleteByBasicProjectConfigIdAndLabelNameIn(tool.getBasicProjectConfigId(),
						levelList);
			}
		}
	}

	private void deleteReleaseInfo(ProjectToolConfig tool) {
		if (tool != null) {
			projectReleaseRepo.deleteByConfigId(tool.getBasicProjectConfigId());
		}
	}

	private void clearCache() {
		cacheService.clearAllCache();
	}

	private void deleteSprintDetailsData(ProjectToolConfig projectToolConfig) {
		if (projectToolConfig != null) {
			sprintRepository.deleteByBasicProjectConfigId(projectToolConfig.getBasicProjectConfigId());
		}
	}
}
