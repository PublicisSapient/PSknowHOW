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
package com.publicissapient.kpidashboard.jira.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraIssueClientUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FetchKanbanReleaseDataImpl implements FetchKanbanReleaseData {

	@Autowired
	ProjectReleaseRepo projectReleaseRepo;
	@Autowired
	KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;
	@Autowired
	private HierarchyLevelService hierarchyLevelService;
	@Autowired
	private JiraCommonService jiraCommonService;

	@Override
	public ProjectRelease processReleaseInfo(ProjectConfFieldMapping projectConfig, KerberosClient krb5Client)
			throws IOException, ParseException {
		boolean isKanban = projectConfig.isKanban();
		log.info("Start Fetching Release Data");
		ProjectRelease projectRelease = null;
		if (isKanban) {
			List<KanbanAccountHierarchy> kanbanAccountHierarchyList = kanbanAccountHierarchyRepo
					.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT,
							projectConfig.getBasicProjectConfigId());
			KanbanAccountHierarchy kanbanAccountHierarchy = CollectionUtils.isNotEmpty(kanbanAccountHierarchyList)
					? kanbanAccountHierarchyList.get(0)
					: null;
			saveProjectRelease(projectConfig, kanbanAccountHierarchy, projectRelease, krb5Client);
		}

		return projectRelease;
	}

	/**
	 * @param confFieldMapping
	 * @param kanbanAccountHierarchy
	 * @return
	 */
	private void saveProjectRelease(ProjectConfFieldMapping confFieldMapping,
			KanbanAccountHierarchy kanbanAccountHierarchy, ProjectRelease projectRelease, KerberosClient krb5Client)
			throws IOException, ParseException {
		List<ProjectVersion> projectVersionList = jiraCommonService.getVersion(confFieldMapping, krb5Client);

		if (CollectionUtils.isNotEmpty(projectVersionList)) {
			if (null != kanbanAccountHierarchy) {
				projectRelease = projectReleaseRepo.findByConfigId(kanbanAccountHierarchy.getBasicProjectConfigId());
				projectRelease = projectRelease == null ? new ProjectRelease() : projectRelease;
				projectRelease.setListProjectVersion(projectVersionList);
				projectRelease.setProjectName(kanbanAccountHierarchy.getNodeId());
				projectRelease.setProjectId(kanbanAccountHierarchy.getNodeId());
				projectRelease.setConfigId(kanbanAccountHierarchy.getBasicProjectConfigId());
				saveKanbanAccountHierarchy(kanbanAccountHierarchy, confFieldMapping, projectRelease);
				projectReleaseRepo.save(projectRelease);
			}
			log.info("Version processed for project version{}",
					projectVersionList.stream().map(ProjectVersion::getName).collect(Collectors.toList()));
		}
	}

	private void saveKanbanAccountHierarchy(KanbanAccountHierarchy projectData, ProjectConfFieldMapping projectConfig,
			ProjectRelease projectRelease) {
		Map<Pair<String, String>, KanbanAccountHierarchy> existingHierarchy = JiraIssueClientUtil
				.getKanbanAccountHierarchy(kanbanAccountHierarchyRepo);
		Set<KanbanAccountHierarchy> setToSave = new HashSet<>();
		if (projectData != null) {
			List<KanbanAccountHierarchy> hierarchyForRelease = createKanbanHierarchyForRelease(projectRelease,
					projectConfig.getProjectBasicConfig(), projectData);
			if (CollectionUtils.isNotEmpty(hierarchyForRelease)) {
				hierarchyForRelease.forEach(hierarchy -> {
					if (StringUtils.isNotBlank(hierarchy.getParentId())) {
						KanbanAccountHierarchy exHiery = existingHierarchy
								.get(Pair.of(hierarchy.getNodeId(), hierarchy.getPath()));
						if (null == exHiery) {
							hierarchy.setCreatedDate(LocalDateTime.now());
							setToSave.add(hierarchy);
						} else if (!exHiery.equals(hierarchy)) {
							exHiery.setBeginDate(hierarchy.getBeginDate());
							exHiery.setEndDate(hierarchy.getEndDate());
							exHiery.setReleaseState(hierarchy.getReleaseState());
							setToSave.add(exHiery);
						}
					}
				});
			}
		}
		if (CollectionUtils.isNotEmpty(setToSave)) {
			kanbanAccountHierarchyRepo.saveAll(setToSave);
		}
	}

	/**
	 * create hierarchies for kanban
	 *
	 * @param projectRelease
	 * @param projectBasicConfig
	 * @param projectHierarchy
	 * @return
	 */
	private List<KanbanAccountHierarchy> createKanbanHierarchyForRelease(ProjectRelease projectRelease,
			ProjectBasicConfig projectBasicConfig, KanbanAccountHierarchy projectHierarchy) {
		List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
				.getFullHierarchyLevels(projectBasicConfig.isKanban());
		Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		HierarchyLevel hierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
		List<KanbanAccountHierarchy> accountHierarchies = new ArrayList<>();
		try {
			projectRelease.getListProjectVersion().stream().forEach(projectVersion -> {
				KanbanAccountHierarchy kanbanAccountHierarchy = new KanbanAccountHierarchy();
				kanbanAccountHierarchy.setBasicProjectConfigId(projectBasicConfig.getId());
				kanbanAccountHierarchy.setIsDeleted(JiraConstants.FALSE);
				kanbanAccountHierarchy.setLabelName(hierarchyLevel.getHierarchyLevelId());
				String versionName = projectVersion.getName() + JiraConstants.COMBINE_IDS_SYMBOL + projectRelease
						.getProjectName().split(JiraConstants.COMBINE_IDS_SYMBOL + projectRelease.getConfigId())[0];
				String versionId = projectVersion.getId() + JiraConstants.COMBINE_IDS_SYMBOL
						+ projectRelease.getProjectId();
				kanbanAccountHierarchy.setNodeId(versionId);
				kanbanAccountHierarchy.setNodeName(versionName);
				kanbanAccountHierarchy.setReleaseState(
						(projectVersion.isReleased()) ? CommonConstant.RELEASED : CommonConstant.UNRELEASED);
				kanbanAccountHierarchy.setBeginDate(
						ObjectUtils.isNotEmpty(projectVersion.getStartDate()) ? projectVersion.getStartDate().toString()
								: CommonConstant.BLANK);
				kanbanAccountHierarchy.setEndDate(ObjectUtils.isNotEmpty(projectVersion.getReleaseDate())
						? projectVersion.getReleaseDate().toString()
						: CommonConstant.BLANK);
				kanbanAccountHierarchy.setPath(new StringBuffer(56).append(projectHierarchy.getNodeId())
						.append(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER).append(projectHierarchy.getPath())
						.toString());
				kanbanAccountHierarchy.setParentId(projectHierarchy.getNodeId());
				accountHierarchies.add(kanbanAccountHierarchy);
			});

		} catch (Exception e) {
			log.error("Jira Processor Failed to get Account Hierarchy data {}", e);
		}
		return accountHierarchies;
	}
}
