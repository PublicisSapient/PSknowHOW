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
package com.publicissapient.kpidashboard.jira.processor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * @author purgupta2
 *
 */
@Slf4j
@Service
public class KanbanJiraIssueAccountHierarchyProcessorImpl implements KanbanJiraIssueAccountHierarchyProcessor {
	@Autowired
	private HierarchyLevelService hierarchyLevelService;
	@Autowired
	private ProjectHierarchyService projectHierarchyService;

	@Override
	public Set<ProjectHierarchy> createKanbanAccountHierarchy(KanbanJiraIssue kanbanJiraIssue,
			ProjectConfFieldMapping projectConfig) {
		log.info("Creating kanban_account_hierarchy for the project : {}", projectConfig.getProjectName());
		List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
				.getFullHierarchyLevels(projectConfig.isKanban());
		Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		HierarchyLevel projectHierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

		List<String> additionalFilterCategoryIds = hierarchyLevelList.stream()
				.filter(x -> x.getLevel() > projectHierarchyLevel.getLevel()).map(HierarchyLevel::getHierarchyLevelId)
				.collect(Collectors.toList());

		log.info("Fetching all hierarchy levels");

		ProjectBasicConfig projectBasicConfig = projectConfig.getProjectBasicConfig();
		// get all the hierarchies related to the selected project from project
		// hierarchies collection
		Map<String, ProjectHierarchy> existingHierarchy = projectHierarchyService
				.getProjectHierarchyMapByConfigId(projectConfig.getBasicProjectConfigId().toString());

		Set<ProjectHierarchy> accHierarchyToSave = new HashSet<>();
		if (StringUtils.isNotBlank(kanbanJiraIssue.getProjectName())) {

			List<ProjectHierarchy> additionalFiltersHierarchies = accountHierarchiesForAdditionalFilters(
					kanbanJiraIssue, projectBasicConfig, additionalFilterCategoryIds);
			additionalFiltersHierarchies.forEach(
					accountHierarchy -> accHierarchyToSave(accountHierarchy, existingHierarchy, accHierarchyToSave));
		}

		return accHierarchyToSave;

	}

	private List<ProjectHierarchy> accountHierarchiesForAdditionalFilters(KanbanJiraIssue jiraIssue,
			ProjectBasicConfig projectBasicConfig, List<String> additionalFilterCategoryIds) {

		List<ProjectHierarchy> projectHierarchyList = new ArrayList<>();
		List<AdditionalFilter> additionalFilters = ListUtils.emptyIfNull(jiraIssue.getAdditionalFilters());

		additionalFilters.forEach(additionalFilter -> {
			if (additionalFilterCategoryIds.contains(additionalFilter.getFilterId())) {
				String labelName = additionalFilter.getFilterId();
				additionalFilter.getFilterValues().forEach(additionalFilterValue -> {
					ProjectHierarchy adFilterAccountHierarchy = new ProjectHierarchy();
					adFilterAccountHierarchy.setHierarchyLevelId(labelName);
					adFilterAccountHierarchy.setNodeId(additionalFilterValue.getValueId());
					adFilterAccountHierarchy.setNodeName(additionalFilterValue.getValue());
					adFilterAccountHierarchy.setNodeDisplayName(additionalFilterValue.getValue());
					adFilterAccountHierarchy.setParentId(projectBasicConfig.getProjectNodeId());
					adFilterAccountHierarchy.setBasicProjectConfigId(new ObjectId(jiraIssue.getBasicProjectConfigId()));
					projectHierarchyList.add(adFilterAccountHierarchy);
				});
			}

		});

		return projectHierarchyList;
	}

	private void accHierarchyToSave(ProjectHierarchy accountHierarchy,
			Map<String, ProjectHierarchy> existingSquadHierarchy, Set<ProjectHierarchy> accHierarchyToSave) {
		if (StringUtils.isNotBlank(accountHierarchy.getParentId())) {
			ProjectHierarchy exHiery = existingSquadHierarchy.get(accountHierarchy.getNodeId());
			if (null == exHiery || !exHiery.getParentId().equalsIgnoreCase(accountHierarchy.getParentId())) {
				accountHierarchy.setCreatedDate(LocalDateTime.now());
				accHierarchyToSave.add(accountHierarchy);
			}
		}
	}

}
