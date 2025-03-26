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
package com.publicissapient.kpidashboard.rally.processor;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 */
@Slf4j
@Service
public class RallyIssueAccountHierarchyProcessorImpl implements RallyIssueAccountHierarchyProcessor {

	@Autowired
	private HierarchyLevelService hierarchyLevelService;

	@Autowired
	private ProjectHierarchyService projectHierarchyService;

	@Override
	public Set<ProjectHierarchy> createAccountHierarchy(JiraIssue jiraIssue, ProjectConfFieldMapping projectConfig,
			Set<SprintDetails> sprintDetailsSet) {

		log.info("Creating account_hierarchy for the project : {}", projectConfig.getProjectName());
		List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService.getFullHierarchyLevels(projectConfig.isKanban());

		Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));

		HierarchyLevel sprintHierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);

		List<String> additionalFilterCategoryIds = hierarchyLevelList.stream()
				.filter(x -> x.getLevel() > sprintHierarchyLevel.getLevel()).map(HierarchyLevel::getHierarchyLevelId)
				.collect(Collectors.toList());

		Set<ProjectHierarchy> setToSave = new HashSet<>();
		if (projectConfig.getProjectBasicConfig().getProjectNodeId() != null &&
				StringUtils.isNotBlank(jiraIssue.getProjectName()) && StringUtils.isNotBlank(jiraIssue.getSprintName()) &&
				StringUtils.isNotBlank(jiraIssue.getSprintBeginDate()) &&
				StringUtils.isNotBlank(jiraIssue.getSprintEndDate())) {
			// get all the hierarchies related to the selected project from project
			// hierarchies collection
			Map<String, List<ProjectHierarchy>> existingHierarchy = projectHierarchyService
					.getProjectHierarchyMapByConfig(projectConfig.getBasicProjectConfigId().toString());

			ObjectId basicProjectConfigId = new ObjectId(jiraIssue.getBasicProjectConfigId());
			Map<String, SprintDetails> sprintDetailsMap = sprintDetailsSet.stream()
					.filter(sprintDetails -> sprintDetails.getBasicProjectConfigId().equals(basicProjectConfigId))
					.collect(Collectors.toMap(SprintDetails::getOriginalSprintId, sprintDetails -> sprintDetails));
			if (jiraIssue.getSprintIdList() != null) {
				for (String sprintId : jiraIssue.getSprintIdList()) {
					SprintDetails sprintDetails = sprintDetailsMap.get(sprintId);
					if (sprintDetails != null) {
						ProjectHierarchy sprintHierarchy = createHierarchyForSprint(sprintDetails,
								projectConfig.getProjectBasicConfig(), sprintHierarchyLevel);
						setToSaveAccountHierarchy(setToSave, sprintHierarchy, existingHierarchy);
						List<ProjectHierarchy> additionalFiltersHierarchies = accountHierarchiesForAdditionalFilters(jiraIssue,
								sprintHierarchy, additionalFilterCategoryIds);
						additionalFiltersHierarchies
								.forEach(accountHierarchy -> setToSaveAccountHierarchy(setToSave, accountHierarchy, existingHierarchy));
					}
				}
			}else {
				SprintDetails sprintDetails = new SprintDetails();
				sprintDetails.setSprintName(jiraIssue.getSprintName());
				sprintDetails.setSprintID(jiraIssue.getSprintID());
				sprintDetails.setStartDate(jiraIssue.getSprintBeginDate());
				sprintDetails.setEndDate(jiraIssue.getSprintEndDate());
				sprintDetails.setState(jiraIssue.getState());
				ProjectHierarchy sprintHierarchy = createHierarchyForSprint(sprintDetails,
						projectConfig.getProjectBasicConfig(), sprintHierarchyLevel);
				setToSave.add(sprintHierarchy);
				setToSaveAccountHierarchy(setToSave, sprintHierarchy, existingHierarchy);
			}
		}
		return setToSave;
	}

	private void setToSaveAccountHierarchy(Set<ProjectHierarchy> setToSave, ProjectHierarchy sprintHierarchy,
			Map<String, List<ProjectHierarchy>> existingHierarchy) {
		if (StringUtils.isNotBlank(sprintHierarchy.getParentId())) {
			List<ProjectHierarchy> exHieryList = existingHierarchy.get(sprintHierarchy.getNodeId());
			if (CollectionUtils.isEmpty(exHieryList)) {
				sprintHierarchy.setCreatedDate(LocalDateTime.now());
				setToSave.add(sprintHierarchy);
			} else {
				Map<String, ProjectHierarchy> exHiery = exHieryList.stream()
						.collect(Collectors.toMap(OrganizationHierarchy::getParentId, p -> p, (existing, newPair) -> existing));
				ProjectHierarchy projectHierarchy = exHiery.get(sprintHierarchy.getParentId());
				if (projectHierarchy == null) {
					sprintHierarchy.setCreatedDate(LocalDateTime.now());
					setToSave.add(sprintHierarchy);
				} else if (!projectHierarchy.equals(sprintHierarchy)) {
					projectHierarchy.setBeginDate(sprintHierarchy.getBeginDate());
					projectHierarchy.setNodeName(sprintHierarchy.getNodeName()); // sprint name changed
					projectHierarchy.setEndDate(sprintHierarchy.getEndDate());
					projectHierarchy.setSprintState(sprintHierarchy.getSprintState());
					setToSave.add(projectHierarchy);
				}
			}
		}
	}

	private ProjectHierarchy createHierarchyForSprint(SprintDetails sprintDetails, ProjectBasicConfig projectBasicConfig,
			HierarchyLevel hierarchyLevel) {
		ProjectHierarchy projectHierachy = new ProjectHierarchy();
        projectHierachy.setBasicProjectConfigId(projectBasicConfig.getId());
        projectHierachy.setHierarchyLevelId(hierarchyLevel.getHierarchyLevelId());
        projectHierachy.setNodeId(sprintDetails.getSprintID());
        projectHierachy.setNodeName(sprintDetails.getSprintName());
        projectHierachy.setNodeDisplayName(sprintDetails.getSprintName());
        projectHierachy.setSprintState(sprintDetails.getState());
        projectHierachy.setBeginDate(sprintDetails.getStartDate());
        projectHierachy.setEndDate(sprintDetails.getEndDate());
        projectHierachy.setParentId(projectBasicConfig.getProjectNodeId());

        return projectHierachy;
	}

	private List<ProjectHierarchy> accountHierarchiesForAdditionalFilters(JiraIssue jiraIssue,
			ProjectHierarchy sprintHierarchy, List<String> additionalFilterCategoryIds) {

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
					adFilterAccountHierarchy.setParentId(sprintHierarchy.getNodeId());
					adFilterAccountHierarchy.setBasicProjectConfigId(sprintHierarchy.getBasicProjectConfigId());
					projectHierarchyList.add(adFilterAccountHierarchy);
				});
			}
		});

		return projectHierarchyList;
	}
}
