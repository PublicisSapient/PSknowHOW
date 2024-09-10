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
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

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
	@Autowired
	private ProjectHierarchyService projectHierarchyService;

	@Override
	public void processReleaseInfo(ProjectConfFieldMapping projectConfig, KerberosClient krb5Client)
			throws IOException, ParseException {
		boolean isKanban = projectConfig.isKanban();
		log.info("Start Fetching Release Data");
		if (isKanban) {
			saveProjectRelease(projectConfig, krb5Client);
		}

	}

	/**
	 * @param confFieldMapping
	 *
	 * @return
	 */
	private void saveProjectRelease(ProjectConfFieldMapping confFieldMapping, KerberosClient krb5Client)
			throws IOException, ParseException {
		List<ProjectVersion> projectVersionList = jiraCommonService.getVersion(confFieldMapping, krb5Client);

		if (CollectionUtils.isNotEmpty(projectVersionList)) {
			ProjectBasicConfig projectBasicConfig = confFieldMapping.getProjectBasicConfig();
			if (null != projectBasicConfig.getProjectNodeId()) {
				ProjectRelease projectRelease = projectReleaseRepo.findByConfigId(projectBasicConfig.getId());
				projectRelease = projectRelease == null ? new ProjectRelease() : projectRelease;
				projectRelease.setListProjectVersion(projectVersionList);
				projectRelease.setProjectName(projectBasicConfig.getProjectName());
				projectRelease.setProjectId(projectBasicConfig.getProjectNodeId());
				projectRelease.setConfigId(projectBasicConfig.getId());
				saveKanbanAccountHierarchy(projectBasicConfig, projectRelease);
				projectReleaseRepo.save(projectRelease);
			}
			log.info("Version processed for project version{}",
					projectVersionList.stream().map(ProjectVersion::getName).collect(Collectors.toList()));
		}
	}

	private void saveKanbanAccountHierarchy(ProjectBasicConfig projectConfig, ProjectRelease projectRelease) {
		Map<String, ProjectHierarchy> existingHierarchy = projectHierarchyService
				.getProjectHierarchyMapByConfigIdAndHierarchyLevelId(projectConfig.getId().toString(),
						CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
		Set<ProjectHierarchy> setToSave = new HashSet<>();

		List<ProjectHierarchy> hierarchyForRelease = createKanbanHierarchyForRelease(projectRelease, projectConfig);
		if (CollectionUtils.isNotEmpty(hierarchyForRelease)) {
			hierarchyForRelease.forEach(hierarchy -> {
				if (StringUtils.isNotBlank(hierarchy.getParentId())) {
					ProjectHierarchy exHiery = existingHierarchy.get(hierarchy.getNodeId());
					if (null == exHiery) {
						hierarchy.setCreatedDate(LocalDateTime.now());
						setToSave.add(hierarchy);
					} else if (!exHiery.checkReleaseEquality(hierarchy)) {
						exHiery.setBeginDate(hierarchy.getBeginDate());
						exHiery.setNodeName(hierarchy.getNodeName());// release name changed
						exHiery.setEndDate(hierarchy.getEndDate());
						exHiery.setReleaseState(hierarchy.getReleaseState());
						setToSave.add(exHiery);
					}
				}
			});
		}

		if (CollectionUtils.isNotEmpty(setToSave)) {
			projectHierarchyService.saveAll(setToSave);
		}
	}

	/**
	 * create hierarchies for kanban
	 *
	 * @param projectRelease
	 * @param projectBasicConfig
	 * @return
	 */
	private List<ProjectHierarchy> createKanbanHierarchyForRelease(ProjectRelease projectRelease,
			ProjectBasicConfig projectBasicConfig) {
		List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
				.getFullHierarchyLevels(projectBasicConfig.isKanban());
		Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		HierarchyLevel hierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
		List<ProjectHierarchy> accountHierarchies = new ArrayList<>();
		try {
			projectRelease.getListProjectVersion().stream().forEach(projectVersion -> {
				ProjectHierarchy releaseHierarchy = new ProjectHierarchy();
				releaseHierarchy.setBasicProjectConfigId(projectBasicConfig.getId());
				releaseHierarchy.setHierarchyLevelId(hierarchyLevel.getHierarchyLevelId());
				String versionName = projectVersion.getName() + JiraConstants.COMBINE_IDS_SYMBOL;
				String versionId = projectVersion.getId() + JiraConstants.COMBINE_IDS_SYMBOL
						+ projectBasicConfig.getProjectNodeId();
				releaseHierarchy.setNodeId(versionId);
				releaseHierarchy.setNodeName(versionName + projectBasicConfig.getProjectName());
				releaseHierarchy.setNodeDisplayName(versionName + projectBasicConfig.getProjectDisplayName());
				releaseHierarchy.setReleaseState(
						(projectVersion.isReleased()) ? CommonConstant.RELEASED : CommonConstant.UNRELEASED);
				releaseHierarchy.setBeginDate(
						ObjectUtils.isNotEmpty(projectVersion.getStartDate()) ? projectVersion.getStartDate().toString()
								: CommonConstant.BLANK);
				releaseHierarchy.setEndDate(ObjectUtils.isNotEmpty(projectVersion.getReleaseDate())
						? projectVersion.getReleaseDate().toString()
						: CommonConstant.BLANK);
				releaseHierarchy.setParentId(projectBasicConfig.getProjectNodeId());
				accountHierarchies.add(releaseHierarchy);
			});

		} catch (Exception e) {
			log.error("Jira Processor Failed to get Account Hierarchy data {}", e);
		}
		return accountHierarchies;
	}
}
