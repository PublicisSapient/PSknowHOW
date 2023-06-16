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

package com.publicissapient.kpidashboard.jira.client.release;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientUtil;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Release data client. Store Release data for the projects in
 * persistence store
 */
@Service
@Data
@Slf4j
public class ScrumReleaseDataClientImpl implements ReleaseDataClient {
	@Autowired
	JiraAdapter jiraAdapter;
	@Autowired
	ProjectReleaseRepo projectReleaseRepo;
	@Autowired
	AccountHierarchyRepository accountHierarchyRepository;
	@Autowired
	private HierarchyLevelService hierarchyLevelService;
	@Autowired
	private JiraRestClientFactory jiraRestClientFactory;

	@Override
	public void processReleaseInfo(ProjectConfFieldMapping projectConfig) {
		PSLogData psLogData = new PSLogData();
		psLogData.setAction(CommonConstant.RELEASE_DATA);
		String projectKey = projectConfig.getJira().getProjectKey();
		boolean isKanban = projectConfig.isKanban();
		psLogData.setProjectKey(projectKey);
		psLogData.setKanban(String.valueOf(isKanban));
		log.info("Start Fetching Release Data", kv(CommonConstant.PSLOGDATA, psLogData));
		try {
			List<AccountHierarchy> accountHierarchyList = accountHierarchyRepository
					.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT,
							projectConfig.getBasicProjectConfigId());
			AccountHierarchy accountHierarchy = CollectionUtils.isNotEmpty(accountHierarchyList)
					? accountHierarchyList.get(0)
					: null;

			saveProjectRelease(projectConfig, accountHierarchy, psLogData);

		} catch (Exception ex) {
			log.error("No hierarchy data found not processing for Version data",
					kv(CommonConstant.PSLOGDATA, psLogData));
		}

	}

	/**
	 * @param confFieldMapping
	 * @param accountHierarchy
	 * @param psLogData
	 */
	private void saveProjectRelease(ProjectConfFieldMapping confFieldMapping, AccountHierarchy accountHierarchy,
			PSLogData psLogData) {
		List<ProjectVersion> projectVersionList = jiraAdapter.getVersion(confFieldMapping);
		if (CollectionUtils.isNotEmpty(projectVersionList)) {
			if (null != accountHierarchy) {
				ProjectRelease projectRelease = projectReleaseRepo
						.findByConfigId(accountHierarchy.getBasicProjectConfigId());
				projectRelease = projectRelease == null ? new ProjectRelease() : projectRelease;
				projectRelease.setListProjectVersion(projectVersionList);
				projectRelease.setProjectName(accountHierarchy.getNodeId());
				projectRelease.setProjectId(accountHierarchy.getNodeId());
				projectRelease.setConfigId(accountHierarchy.getBasicProjectConfigId());
				saveScrumAccountHierarchy(accountHierarchy, confFieldMapping, projectRelease);
				projectReleaseRepo.save(projectRelease);
				jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
						CommonConstant.CACHE_ACCOUNT_HIERARCHY);
				jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
						CommonConstant.JIRA_KPI_CACHE);
			}
			psLogData.setProjectVersion(
					projectVersionList.stream().map(ProjectVersion::getName).collect(Collectors.toList()));
			log.info("Version processed", kv(CommonConstant.PSLOGDATA, psLogData));
		}
	}

	private void saveScrumAccountHierarchy(AccountHierarchy projectData, ProjectConfFieldMapping projectConfig,
			ProjectRelease projectRelease) {
		Map<Pair<String, String>, AccountHierarchy> existingHierarchy = JiraIssueClientUtil
				.getAccountHierarchy(accountHierarchyRepository);
		Set<AccountHierarchy> setToSave = new HashSet<>();
		if (projectData != null) {
			List<AccountHierarchy> hierarchyForRelease = createScrumHierarchyForRelease(projectRelease,
					projectConfig.getProjectBasicConfig(), projectData);
			setToSaveAccountHierarchy(setToSave, hierarchyForRelease, existingHierarchy);
		}
		if (CollectionUtils.isNotEmpty(setToSave)) {
			log.info("Updated Hierarchies", setToSave.size());
			accountHierarchyRepository.saveAll(setToSave);
		}
	}

	/**
	 * @param setToSave
	 * @param accountHierarchy
	 * @param existingHierarchy
	 */
	private void setToSaveAccountHierarchy(Set<AccountHierarchy> setToSave, List<AccountHierarchy> accountHierarchy,
			Map<Pair<String, String>, AccountHierarchy> existingHierarchy) {
		if (CollectionUtils.isNotEmpty(accountHierarchy)) {
			accountHierarchy.forEach(hierarchy -> {
				if (StringUtils.isNotBlank(hierarchy.getParentId())) {
					AccountHierarchy exHiery = existingHierarchy
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

	/**
	 * create hierarchy for scrum
	 * 
	 * @param projectRelease
	 * @param projectBasicConfig
	 * @param projectHierarchy
	 * @return
	 */
	private List<AccountHierarchy> createScrumHierarchyForRelease(ProjectRelease projectRelease,
			ProjectBasicConfig projectBasicConfig, AccountHierarchy projectHierarchy) {
		log.info("Create Account Hierarchy");
		List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
				.getFullHierarchyLevels(projectBasicConfig.isKanban());
		Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		HierarchyLevel hierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
		List<AccountHierarchy> accountHierarchies = new ArrayList<>();
		try {
			projectRelease.getListProjectVersion().stream().forEach(projectVersion -> {
				AccountHierarchy accountHierarchy = new AccountHierarchy();
				accountHierarchy.setBasicProjectConfigId(projectBasicConfig.getId());
				accountHierarchy.setIsDeleted(JiraConstants.FALSE);
				accountHierarchy.setLabelName(hierarchyLevel.getHierarchyLevelId());
				String versionName = projectVersion.getName() + JiraConstants.COMBINE_IDS_SYMBOL
						+ projectRelease.getProjectName().split(JiraConstants.COMBINE_IDS_SYMBOL)[0];
				String versionId = projectVersion.getId() + JiraConstants.COMBINE_IDS_SYMBOL
						+ projectRelease.getProjectId();
				accountHierarchy.setNodeId(versionId);
				accountHierarchy.setNodeName(versionName);
				accountHierarchy.setBeginDate(
						ObjectUtils.isNotEmpty(projectVersion.getStartDate()) ? projectVersion.getStartDate().toString()
								: CommonConstant.BLANK);
				accountHierarchy.setEndDate(ObjectUtils.isNotEmpty(projectVersion.getReleaseDate())
						? projectVersion.getReleaseDate().toString()
						: CommonConstant.BLANK);
				accountHierarchy.setReleaseState(
						(projectVersion.isReleased()) ? CommonConstant.RELEASED : CommonConstant.UNRELEASED);
				accountHierarchy.setPath(new StringBuffer(56).append(projectHierarchy.getNodeId())
						.append(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER).append(projectHierarchy.getPath())
						.toString());
				accountHierarchy.setParentId(projectHierarchy.getNodeId());
				accountHierarchies.add(accountHierarchy);
			});

		} catch (Exception e) {
			log.error("Jira Processor Failed to get Account Hierarchy data {}", e);
		}
		return accountHierarchies;
	}
}
