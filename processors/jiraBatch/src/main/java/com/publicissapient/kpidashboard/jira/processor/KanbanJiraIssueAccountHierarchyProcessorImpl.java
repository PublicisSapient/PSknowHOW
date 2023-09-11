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
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
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
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;
	@Autowired
	private HierarchyLevelService hierarchyLevelService;

	@Override
	public Set<KanbanAccountHierarchy> createKanbanAccountHierarchy(KanbanJiraIssue kanbanJiraIssue,
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
		List<KanbanAccountHierarchy> accountHierarchyList = kanbanAccountHierarchyRepo.findAll();
		Map<Pair<String, String>, KanbanAccountHierarchy> existingKanbanHierarchy = accountHierarchyList.stream()
				.collect(Collectors.toMap(p -> Pair.of(p.getNodeId(), p.getPath()), p -> p));

		Set<KanbanAccountHierarchy> accHierarchyToSave = new HashSet<>();
		if (StringUtils.isNotBlank(kanbanJiraIssue.getProjectName())) {
			KanbanAccountHierarchy projectHierarchy = kanbanAccountHierarchyRepo
					.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT,
							new ObjectId(kanbanJiraIssue.getBasicProjectConfigId()))
					.get(0);

			List<KanbanAccountHierarchy> additionalFiltersHierarchies = accountHierarchiesForAdditionalFilters(
					kanbanJiraIssue, projectHierarchy, additionalFilterCategoryIds);

			additionalFiltersHierarchies.forEach(accountHierarchy -> accHierarchyToSave(accountHierarchy,
					existingKanbanHierarchy, accHierarchyToSave));

		}

		return accHierarchyToSave;

	}

	private List<KanbanAccountHierarchy> accountHierarchiesForAdditionalFilters(KanbanJiraIssue jiraIssue,
			KanbanAccountHierarchy projectHierarchy, List<String> additionalFilterCategoryIds) {

		List<KanbanAccountHierarchy> accountHierarchies = new ArrayList<>();
		List<AdditionalFilter> additionalFilters = ListUtils.emptyIfNull(jiraIssue.getAdditionalFilters());

		additionalFilters.forEach(additionalFilter -> {
			if (additionalFilterCategoryIds.contains(additionalFilter.getFilterId())) {
				String labelName = additionalFilter.getFilterId();
				additionalFilter.getFilterValues().forEach(additionalFilterValue -> {
					KanbanAccountHierarchy adFilterAccountHierarchy = new KanbanAccountHierarchy();
					adFilterAccountHierarchy.setLabelName(labelName);
					adFilterAccountHierarchy.setNodeId(additionalFilterValue.getValueId());
					adFilterAccountHierarchy.setNodeName(additionalFilterValue.getValue());
					adFilterAccountHierarchy.setParentId(projectHierarchy.getNodeId());
					adFilterAccountHierarchy.setPath(projectHierarchy.getNodeId()
							+ CommonConstant.ACC_HIERARCHY_PATH_SPLITTER + projectHierarchy.getPath());
					adFilterAccountHierarchy.setBasicProjectConfigId(new ObjectId(jiraIssue.getBasicProjectConfigId()));
					accountHierarchies.add(adFilterAccountHierarchy);
				});
			}

		});

		return accountHierarchies;
	}

	private void accHierarchyToSave(KanbanAccountHierarchy accountHierarchy,
			Map<Pair<String, String>, KanbanAccountHierarchy> existingKanbanHierarchy,
			Set<KanbanAccountHierarchy> accHierarchyToSave) {
		if (StringUtils.isNotBlank(accountHierarchy.getParentId())
				|| (StringUtils.isBlank(accountHierarchy.getParentId()))) {
			KanbanAccountHierarchy exHiery = existingKanbanHierarchy
					.get(Pair.of(accountHierarchy.getNodeId(), accountHierarchy.getPath()));

			if (null == exHiery) {
				accountHierarchy.setCreatedDate(LocalDateTime.now());
				accHierarchyToSave.add(accountHierarchy);
			}
		}
	}

}
