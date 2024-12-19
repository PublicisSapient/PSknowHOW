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

package com.publicissapient.kpidashboard.apis.datamigration.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.datamigration.model.HierarchyValueDup;
import com.publicissapient.kpidashboard.apis.datamigration.model.MigrateData;
import com.publicissapient.kpidashboard.apis.datamigration.model.ProjectBasicDup;
import com.publicissapient.kpidashboard.apis.datamigration.util.InconsistentDataException;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.application.SprintTraceLog;
import com.publicissapient.kpidashboard.common.model.comments.KPIComments;
import com.publicissapient.kpidashboard.common.model.comments.KpiCommentsHistory;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.rbac.AccessItem;
import com.publicissapient.kpidashboard.common.model.rbac.AccessNode;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsAccess;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.testexecution.KanbanTestExecution;
import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.AdditionalFilterCategoryRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanTestExecutionRepository;
import com.publicissapient.kpidashboard.common.repository.application.OrganizationHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.application.SprintTraceLogRepository;
import com.publicissapient.kpidashboard.common.repository.application.TestExecutionRepository;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.comments.KpiCommentsRepository;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.excel.KanbanCapacityRepository;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.AccessRequestsRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataMigrationService {

	@Autowired
	private ProjectBasicConfigRepository basicConfigRepository;
	@Autowired
	private OrganizationHierarchyRepository organizationHierarchyRepository;
	@Autowired
	public SaveService saveService;

	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;
	@Autowired
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepository;
	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private CapacityKpiDataRepository capacityKpiDataRepository;
	@Autowired
	private KanbanCapacityRepository kanbanCapacityRepository;

	@Autowired
	private HappinessKpiDataRepository happinessKpiDataRepository;
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private KanbanJiraIssueRepository kanbanJiraIssueRepository;
	@Autowired
	private AdditionalFilterCategoryRepository additionalFilterCategoryRepository;
	@Autowired
	private TestExecutionRepository testExecutionRepository;
	@Autowired
	private KanbanTestExecutionRepository kanbanTestExecutionRepository;
	@Autowired
	private ProjectReleaseRepo projectReleaseRepo;
	@Autowired
	private SprintTraceLogRepository sprintTraceLogRepository;
	@Autowired
	private UserInfoRepository userInfoRepository;
	@Autowired
	private AccessRequestsRepository accessRequestsRepository;
	@Autowired
	private KpiCommentsRepository kpiCommentsRepository;
	@Autowired
	private KpiCommentsHistoryRepository kpiCommentsHistoryRepository;

	protected Map<String, OrganizationHierarchy> nodeWiseOrganizationHierarchy;
	protected List<ProjectBasicConfig> projectBasicConfigList;
	protected List<ProjectBasicDup> projectBasicDupList;

	public List<MigrateData> dataMigration() {
		List<MigrateData> failureData = new ArrayList<>();
		log.info("Fetching basic Config");
		projectBasicConfigList = basicConfigRepository.findAll();
		projectBasicDupList = duplicateProject(projectBasicConfigList);
		updateCustomizedName(projectBasicDupList);

		nodeWiseOrganizationHierarchy = new HashMap<>();

		for (ProjectBasicDup project : projectBasicDupList) {
			List<HierarchyValueDup> hierarchyList = project.getHierarchy();
			if (hierarchyList == null || hierarchyList.isEmpty())
				continue;
			// copy mainOrganzation Hierarchy
			Map<String, OrganizationHierarchy> projectHierarchyMap = new HashMap<>(nodeWiseOrganizationHierarchy);

			hierarchyList.sort(
					(h1, h2) -> Integer.compare(h2.getHierarchyLevel().getLevel(), h1.getHierarchyLevel().getLevel()));

			try {
				// Creating project node
				int projectAboveLevel = hierarchyList.get(0).getHierarchyLevel().getLevel();
				String projectParentId = checkParent(projectAboveLevel, hierarchyList, projectHierarchyMap);
				OrganizationHierarchy projectHierarchy = new OrganizationHierarchy();
				projectHierarchy.setNodeName(project.getProjectName());
				projectHierarchy.setNodeDisplayName(project.getProjectName());
				projectHierarchy.setHierarchyLevelId("project");
				projectHierarchy.setCreatedDate(LocalDateTime.now());
				projectHierarchy.setParentId(projectParentId);
				projectHierarchy.setNodeId(UUID.randomUUID().toString());
				projectHierarchyMap.put(projectAboveLevel - 1 + ":" + project.getProjectName(), projectHierarchy);
				nodeWiseOrganizationHierarchy.putAll(projectHierarchyMap);

			} catch (InconsistentDataException e) {
				log.error("Error in project: " + project.getProjectName() + " -> " + e.getMessage());
				String[] message = e.getMessage().split(":");
				failureData.add(new MigrateData(project.getProjectName(), message[0], message[1]));
			}
		}

		return failureData;
	}

	private void updateCustomizedName(List<ProjectBasicDup> projectBasicDupList) {
		log.info("Start of Coping Name of Parent to Child");
		for (int i = 0; i < projectBasicDupList.size(); i++) {
			ProjectBasicDup projectBasicDup = projectBasicDupList.get(i);
			projectBasicDup.getHierarchy().sort(
					(h1, h2) -> Integer.compare(h2.getHierarchyLevel().getLevel(), h1.getHierarchyLevel().getLevel()));

			// recuursion
			updateNameWithParent(projectBasicDup.getHierarchy().get(0).getHierarchyLevel().getLevel(),
					projectBasicDup.getHierarchy());

		}
		log.info("End of Coping Name of Parent to Child");

	}

	private String updateNameWithParent(int level, List<HierarchyValueDup> hierarchyList) {
		// Find current level hierarchy value
		HierarchyValueDup currentHierarchy = hierarchyList.stream()
				.filter(hv -> hv.getHierarchyLevel().getLevel() == level).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No hierarchy found for level: " + level));
		if (level == 1) {
			return currentHierarchy.getValue();
		} else {
			currentHierarchy.setCustomizedValue(
					currentHierarchy.getValue() + "-" + updateNameWithParent(level - 1, hierarchyList));
		}

		return currentHierarchy.getCustomizedValue();
	}

	private List<ProjectBasicDup> duplicateProject(List<ProjectBasicConfig> projectBasicConfigList) {
		ModelMapper mapper = new ModelMapper();
		List<ProjectBasicDup> projectBasicDupList = new ArrayList<>();
		for (ProjectBasicConfig projectBasicConfig : projectBasicConfigList) {
			ProjectBasicDup projectBasicDup = mapper.map(projectBasicConfig, ProjectBasicDup.class);
			List<HierarchyValueDup> hierarchyValueDupList = projectBasicDup.getHierarchy();
			if (CollectionUtils.isNotEmpty(hierarchyValueDupList)) {
				for (HierarchyValueDup values : hierarchyValueDupList) {
					values.setCustomizedValue(values.getCustomizedValue());
				}
				projectBasicDupList.add(projectBasicDup);
			}
		}

		return projectBasicDupList;

	}

	private static String checkParent(int level, List<HierarchyValueDup> hierarchyList,
			Map<String, OrganizationHierarchy> nodeWiseOrganizationHierachy) throws InconsistentDataException {

		if (level < 1) {
			throw new IllegalArgumentException("Level cannot be less than 1");
		}

		// Find current level hierarchy value
		HierarchyValueDup currentHierarchy = hierarchyList.stream()
				.filter(hv -> hv.getHierarchyLevel().getLevel() == level).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No hierarchy found for level: " + level));

		String key = level + ":"
				+ (StringUtils.isEmpty(currentHierarchy.getCustomizedValue()) ? currentHierarchy.getValue()
						: currentHierarchy.getCustomizedValue());
		OrganizationHierarchy organizationHierarchy = nodeWiseOrganizationHierachy.get(key);

		if (organizationHierarchy == null) {
			// Create a new node if not found
			organizationHierarchy = new OrganizationHierarchy();
			organizationHierarchy.setNodeName(currentHierarchy.getValue());
			organizationHierarchy.setNodeDisplayName(currentHierarchy.getValue());
			organizationHierarchy.setHierarchyLevelId(currentHierarchy.getHierarchyLevel().getHierarchyLevelId());
			organizationHierarchy.setCreatedDate(LocalDateTime.now());

			// Recursively set parent
			if (level > 1) {
				organizationHierarchy.setParentId(checkParent(level - 1, hierarchyList, nodeWiseOrganizationHierachy));
			}

			organizationHierarchy.setNodeId(UUID.randomUUID().toString());
			currentHierarchy.setOrgHierarchyNodeId(organizationHierarchy.getNodeId());
			nodeWiseOrganizationHierachy.put(key, organizationHierarchy);
		} else {
			// Validate parent ID consistency
			String expectedParentId = level > 1 ? checkParent(level - 1, hierarchyList, nodeWiseOrganizationHierachy)
					: null;
			if (expectedParentId != null && !expectedParentId.equals(organizationHierarchy.getParentId())) {
				throw new InconsistentDataException(level + ":" + currentHierarchy.getValue());
			}
		}
		currentHierarchy.setOrgHierarchyNodeId(organizationHierarchy.getNodeId());
		return organizationHierarchy.getNodeId();
	}

	/*
	 * create organization hierarchy and update project basic hierarchy
	 */
	public void populateOrganizationHierarchy() {
		if (MapUtils.isEmpty(nodeWiseOrganizationHierarchy)) {
			log.info("Calling the validation process");
			List<MigrateData> failureData = dataMigration();
			// Proceed only if there are no failures and the map is populated
			if (CollectionUtils.isEmpty(failureData) && MapUtils.isNotEmpty(nodeWiseOrganizationHierarchy)) {
				saveOrganizationHierarchyAndUpdateProjectBasic(new ArrayList<>(nodeWiseOrganizationHierarchy.values()),
						projectBasicConfigList, projectBasicDupList);
			}

		} else {
			saveOrganizationHierarchyAndUpdateProjectBasic(new ArrayList<>(nodeWiseOrganizationHierarchy.values()),
					projectBasicConfigList, projectBasicDupList);
		}
	}

	public void saveOrganizationHierarchyAndUpdateProjectBasic(List<OrganizationHierarchy> organizationHierarchyList,
			List<ProjectBasicConfig> projectBasicConfigList, List<ProjectBasicDup> projectBasicDupList) {
		log.info("Start - Updating Node Names to Original Values");

		try {
			// Update node names to match their display names

			organizationHierarchyList
					.forEach(orgHierarchy -> orgHierarchy.setNodeName(orgHierarchy.getNodeDisplayName()));

			// Map project names to their unique node IDs
			Map<String, String> projectNameWiseUniqueId = organizationHierarchyList.stream()
					.filter(orgHierarchy -> "project".equalsIgnoreCase(orgHierarchy.getHierarchyLevelId()))
					.collect(Collectors.toMap(OrganizationHierarchy::getNodeDisplayName,
							OrganizationHierarchy::getNodeId));

			Map<String, List<HierarchyValueDup>> collect = projectBasicDupList.stream()
					.collect(Collectors.toMap(ProjectBasicDup::getProjectName, ProjectBasicDup::getHierarchy));

			// Update project basic config list with unique projectNodeId
			projectBasicConfigList.forEach(projectBasicConfig -> {
				List<HierarchyValueDup> hierarchyValueDups = collect.get(projectBasicConfig.getProjectName());

				projectBasicConfig.getHierarchy()
						.forEach(
								hierarchyValue -> hierarchyValue.setOrgHierarchyNodeId(hierarchyValueDups.stream()
										.filter(dup -> hierarchyValue.getHierarchyLevel().getLevel() == dup
												.getHierarchyLevel().getLevel())
										.toList().get(0).getOrgHierarchyNodeId()));

				projectBasicConfig.setProjectNodeId(projectNameWiseUniqueId.get(projectBasicConfig.getProjectName()));
				projectBasicConfig.setProjectDisplayName(projectBasicConfig.getProjectName());

			});

			// projectHierarchy
			Map<ObjectId, String> projectIdWiseUniqueId = projectBasicConfigList.stream()
					.collect(Collectors.toMap(ProjectBasicConfig::getId, ProjectBasicConfig::getProjectNodeId));
			List<AccountHierarchy> accountHierarchyRepositoryAll = accountHierarchyRepository.findAll();
			List<KanbanAccountHierarchy> kanbanAccountHierarchyList = kanbanAccountHierarchyRepository.findAll();
			List<SprintDetails> allSprintDetails = sprintRepository.findAll();
			Map<String, Object> dataToSave = createDataToSave(organizationHierarchyList, projectBasicConfigList,
					projectIdWiseUniqueId, accountHierarchyRepositoryAll, kanbanAccountHierarchyList, allSprintDetails);

			dataToSave.put("ORGANIZATION_HIERARCHY", organizationHierarchyList);
			dataToSave.put("PROJECT_BASIC", projectBasicConfigList);
			// Save all data to the repository
			saveService.saveToDatabase(dataToSave);
			log.info("Data successfully saved to the database.");

		} catch (DuplicateKeyException ex) {
			log.error("Duplicate project name found in organization hierarchy: {}", ex.getMessage());
			throw new DuplicateKeyException(
					"Duplicate project name found in organization hierarchy: " + ex.getMessage(), ex);

		} catch (DataIntegrityViolationException ex) {
			log.error("Data integrity violation occurred: {}", ex.getMessage());
			throw new DataIntegrityViolationException(
					"Data integrity violation while saving organization hierarchy or project basic config: "
							+ ex.getMessage(),
					ex);

		} catch (Exception ex) {
			log.error("An unexpected error occurred: {}", ex.getMessage());
			throw new IllegalStateException("An unexpected error occurred while saving data: " + ex.getMessage(), ex);
		}
	}

	private Map<String, Object> createDataToSave(List<OrganizationHierarchy> organizationHierarchyList,
			List<ProjectBasicConfig> projectBasicConfigList, Map<ObjectId, String> projectIdWiseUniqueId,
			List<AccountHierarchy> accountHierarchyRepositoryAll,
			List<KanbanAccountHierarchy> kanbanAccountHierarchyList, List<SprintDetails> allSprintDetails) {
		log.info("Start Processing Data");
		Map<String, Object> dataSetToSave = new HashMap<>();
		createSprintHierarchy(accountHierarchyRepositoryAll, projectIdWiseUniqueId, allSprintDetails, dataSetToSave);

		createReleaseHierarchy(accountHierarchyRepositoryAll, kanbanAccountHierarchyList, projectIdWiseUniqueId,
				dataSetToSave);
		createAdditinalFilterHierarchy(accountHierarchyRepositoryAll, kanbanAccountHierarchyList, projectIdWiseUniqueId,
				dataSetToSave);
		updateCapacity(projectIdWiseUniqueId, dataSetToSave);
		updateHappieness(dataSetToSave);
		updateJiraIssue(projectIdWiseUniqueId, dataSetToSave);
		updateTestExecution(projectIdWiseUniqueId, dataSetToSave);
		updateProjectRelease(projectBasicConfigList, dataSetToSave);
		updateSprintTraceLog(dataSetToSave);
		updateUserInfo(organizationHierarchyList, dataSetToSave);
		updateAccessRequest(organizationHierarchyList, dataSetToSave);
		updateKpiComments(projectBasicConfigList, dataSetToSave);
		return dataSetToSave;
	}

	private void updateKpiComments(List<ProjectBasicConfig> projectBasicConfigList, Map<String, Object> dataSetToSave) {

		Map<String, String> projectNameWiseNodeId = projectBasicConfigList.stream().collect(
				Collectors.toMap(a -> (a.getProjectName() + "_" + a.getId()), ProjectBasicConfig::getProjectNodeId));
		Map<String, String> sprintNodeHistory = (Map<String, String>) dataSetToSave.get("SPRINT_HISTORY");
		List<KpiCommentsHistory> allHistory = kpiCommentsHistoryRepository.findAll();
		List<KPIComments> all = kpiCommentsRepository.findAll();

		log.info("KPI Comments Data Processing Started");
		List<KPIComments> finalKpiComment = new ArrayList<>();
		List<KpiCommentsHistory> finalKpiHistoryComment = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(all)) {
			for (KPIComments kpiComment : all) {
				if (projectNameWiseNodeId.containsKey(kpiComment.getNode())) {
					kpiComment.setNode(projectNameWiseNodeId.get(kpiComment.getNode()));
					if (StringUtils.isNotEmpty(kpiComment.getNodeChildId())
							&& sprintNodeHistory.containsKey(kpiComment.getNodeChildId())) {
						kpiComment.setNodeChildId(sprintNodeHistory.get(kpiComment.getNodeChildId()));
						finalKpiComment.add(kpiComment);
					}

					else if (StringUtils.isEmpty(kpiComment.getNodeChildId())) {
						finalKpiComment.add(kpiComment);
					}

				}
			}
		}
		log.info("KPI Comments Data Processing Completed");
		log.info("KPI Comments History Data Processing Started");
		if (CollectionUtils.isNotEmpty(allHistory)) {
			for (KpiCommentsHistory kpiComment : allHistory) {
				if (projectNameWiseNodeId.containsKey(kpiComment.getNode())) {
					kpiComment.setNode(projectNameWiseNodeId.get(kpiComment.getNode()));
					if (StringUtils.isNotEmpty(kpiComment.getNodeChildId())
							&& sprintNodeHistory.containsKey(kpiComment.getNodeChildId())) {
						kpiComment.setNodeChildId(sprintNodeHistory.get(kpiComment.getNodeChildId()));
						finalKpiHistoryComment.add(kpiComment);
					}

					else if (StringUtils.isEmpty(kpiComment.getNodeChildId())) {
						finalKpiHistoryComment.add(kpiComment);
					}

				}
			}
		}
		log.info("KPI Comments History Data Processing Completed");
		dataSetToSave.put("KPI_COMMENT", finalKpiComment);
		dataSetToSave.put("KPI_COMMENT_HISTORY", finalKpiHistoryComment);

	}

	private void updateAccessRequest(List<OrganizationHierarchy> organizationHierarchyList,
			Map<String, Object> dataSetToSave) {
		Map<String, List<String>> map = organizationHierarchyList.stream()
				.collect(Collectors.groupingBy(org -> org.getHierarchyLevelId() + "_" + org.getNodeDisplayName(),
						Collectors.mapping(OrganizationHierarchy::getNodeId, Collectors.toList()) // Value: list of
				// nodeIds
				));

		List<AccessRequest> accessRequest = accessRequestsRepository.findAll();
		log.info("Access Request Data Processing Started");
		if (CollectionUtils.isNotEmpty(accessRequest)) {
			for (AccessRequest request : accessRequest) {
				AccessNode node = request.getAccessNode();
				String level = node.getAccessLevel();
				if (CollectionUtils.isNotEmpty(node.getAccessItems())) {
					List<AccessItem> newAccessItem = new ArrayList<>();
					for (AccessItem item : node.getAccessItems()) {
						if (map.containsKey(level + "_" + item.getItemName())) {
							List<String> strings = map.get(level + "_" + item.getItemName());
							for (String str : strings) {
								AccessItem item1 = new AccessItem();
								item1.setItemId(str);
								newAccessItem.add(item1);
							}
						}
					}
					node.setAccessItems(newAccessItem);
				}

			}
		}
		log.info("Access Request Data Processing Completed");

		dataSetToSave.put("ACCESS_REQUEST", accessRequest);

	}

	private void updateUserInfo(List<OrganizationHierarchy> organizationHierarchyList,
			Map<String, Object> dataSetToSave) {
		Map<String, List<String>> map = organizationHierarchyList.stream()
				.collect(Collectors.groupingBy(org -> org.getHierarchyLevelId() + "_" + org.getNodeDisplayName(),
						Collectors.mapping(OrganizationHierarchy::getNodeId, Collectors.toList())));
		List<UserInfo> newUserInfo = userInfoRepository.findAll();
		log.info("User Info Data Processing Started");
		if (CollectionUtils.isNotEmpty(newUserInfo)) {
			for (UserInfo userInfo : newUserInfo) {
				List<ProjectsAccess> projectsAccess = userInfo.getProjectsAccess();
				if (CollectionUtils.isNotEmpty(projectsAccess)) {
					for (ProjectsAccess access : projectsAccess) {
						List<AccessNode> accessNodes = access.getAccessNodes();
						if (CollectionUtils.isNotEmpty(accessNodes)) {
							for (AccessNode node : accessNodes) {
								String level = node.getAccessLevel();
								if (CollectionUtils.isNotEmpty(node.getAccessItems())) {
									List<AccessItem> newAccessItem = new ArrayList<>();
									for (AccessItem item : node.getAccessItems()) {
										if (map.containsKey(level + "_" + item.getItemName())) {
											List<String> strings = map.get(level + "_" + item.getItemName());
											for (String str : strings) {
												AccessItem item1 = new AccessItem();
												item1.setItemId(str);
												newAccessItem.add(item1);
											}
										}
									}
									node.setAccessItems(newAccessItem);
								}

							}
						}

					}
				}

			}

		}
		log.info("User Info Data Processing Completed");
		dataSetToSave.put("USER_INFO", newUserInfo);

	}

	private void updateSprintTraceLog(Map<String, Object> dataSetToSave) {
		List<SprintTraceLog> sprintTraceLogList = sprintTraceLogRepository.findAll();
		List<SprintTraceLog> sprintTraceLogFinalList = new ArrayList<>();
		Map<String, String> sprintNodeHistory = (Map<String, String>) dataSetToSave.get("SPRINT_HISTORY");
		log.info("Sprint Trace Log Data Processing Data Started");
		if (CollectionUtils.isNotEmpty(sprintTraceLogList)) {
			for (SprintTraceLog sprintTraceLog : sprintTraceLogList) {
				if (sprintNodeHistory.containsKey(sprintTraceLog.getSprintId())) {
					sprintTraceLog.setSprintId(sprintNodeHistory.get(sprintTraceLog.getSprintId()));
					sprintTraceLogFinalList.add(sprintTraceLog);
				}
			}
		}
		log.info("Sprint Trace Log Data Processing Data Completd");
		dataSetToSave.put("SPRINT_TRACELOG", sprintTraceLogFinalList);
	}

	private void updateProjectRelease(List<ProjectBasicConfig> projectBasicConfigList,
			Map<String, Object> dataSetToSave) {
		List<ProjectRelease> projectReleaseList = projectReleaseRepo.findAll();

		Map<ObjectId, Pair<String, String>> projectWiseIdName = projectBasicConfigList.stream().collect(
				Collectors.toMap(ProjectBasicConfig::getId, a -> Pair.of(a.getProjectNodeId(), a.getProjectName())));
		List<ProjectRelease> projectReleaseFinalList = new ArrayList<>();
		log.info("Project Release Data Processing Data Started");
		if (CollectionUtils.isNotEmpty(projectReleaseList)) {
			for (ProjectRelease projectRelease : projectReleaseList) {
				if (projectWiseIdName.containsKey(projectRelease.getConfigId())) {
					projectRelease.setProjectId(projectWiseIdName.get(projectRelease.getConfigId()).getKey());
					projectRelease.setProjectName(projectWiseIdName.get(projectRelease.getConfigId()).getValue());
					projectReleaseFinalList.add(projectRelease);
				}
			}
			dataSetToSave.put("PROJECT_RELEASE", projectReleaseFinalList);
		}
		log.info("Project Release Data Processing Data Completed");

	}

	private void updateTestExecution(Map<ObjectId, String> projectIdWiseUniqueId, Map<String, Object> dataSetToSave) {
		List<TestExecution> testExecutionList = (List<TestExecution>) testExecutionRepository.findAll();
		List<KanbanTestExecution> kanbanTestExecutionList = (List<KanbanTestExecution>) kanbanTestExecutionRepository
				.findAll();
		List<TestExecution> testExecutionFinalList = new ArrayList<>();
		List<KanbanTestExecution> kanbanTestExecutionFinalList = new ArrayList<>();
		Map<String, String> sprintNodeHistory = (Map<String, String>) dataSetToSave.get("SPRINT_HISTORY");
		log.info("Scrum TestExecution Data Processing Data Started");
		if (CollectionUtils.isNotEmpty(testExecutionList)) {
			for (TestExecution testExecution : testExecutionList) {
				if (projectIdWiseUniqueId.containsKey(new ObjectId(testExecution.getBasicProjectConfigId()))
						&& sprintNodeHistory.containsKey(testExecution.getSprintId())) {
					testExecution.setSprintId(sprintNodeHistory.get(testExecution.getSprintId()));
					testExecution.setProjectId(
							projectIdWiseUniqueId.get(new ObjectId(testExecution.getBasicProjectConfigId())));
					testExecutionFinalList.add(testExecution);
				}
			}
			dataSetToSave.put("TEST_EXECUTION_SCRUM", testExecutionFinalList);
		}
		log.info("Scrum TestExecution Data Processing Data Completed");
		log.info("Kanban TestExecution Data Processing Data Started");
		if (CollectionUtils.isNotEmpty(kanbanTestExecutionList)) {
			for (KanbanTestExecution testExecution : kanbanTestExecutionList) {
				if (projectIdWiseUniqueId.containsKey(new ObjectId(testExecution.getBasicProjectConfigId()))) {
					testExecution.setProjectNodeId(
							projectIdWiseUniqueId.get(new ObjectId(testExecution.getBasicProjectConfigId())));
					kanbanTestExecutionFinalList.add(testExecution);
				}
			}
			dataSetToSave.put("TEST_EXECUTION_KANBAN", kanbanTestExecutionFinalList);
		}
		log.info("Kanban TestExecution Data Processing Data Completed");

	}

	private void updateJiraIssue(Map<ObjectId, String> projectIdWiseUniqueId, Map<String, Object> dataSetToSave) {
		List<JiraIssue> jiraIssueRepositoryAll = (List<JiraIssue>) jiraIssueRepository.findAll();
		List<KanbanJiraIssue> kanbanJiraIssueHistoryRepositoryAll = kanbanJiraIssueRepository.findAll();

		Map<String, String> sprintNodeHistory = (Map<String, String>) dataSetToSave.get("SPRINT_HISTORY");
		List<JiraIssue> jiraIssueList = new ArrayList<>();
		List<KanbanJiraIssue> kanbanJiraIssueList = new ArrayList<>();
		log.info("Scrum Jira Issue Data Processing Data Started");
		if (CollectionUtils.isNotEmpty(jiraIssueRepositoryAll)) {
			for (JiraIssue jiraIssue : jiraIssueRepositoryAll) {
				if (sprintNodeHistory.containsKey(jiraIssue.getSprintID())) {
					jiraIssue.setSprintID(sprintNodeHistory.get(jiraIssue.getSprintID()));
					jiraIssueList.add(jiraIssue);
				}
			}

			dataSetToSave.put("SCRUM_JIRA_ISSUE", jiraIssueList);
		}
		log.info("Scrum Jira Issue Data Processing Data Completed");
		log.info("Kanban Jira Issue Data Processing Data Started");
		if (CollectionUtils.isNotEmpty(kanbanJiraIssueHistoryRepositoryAll)) {
			for (KanbanJiraIssue jiraIssue : kanbanJiraIssueHistoryRepositoryAll) {
				if (projectIdWiseUniqueId.containsKey(new ObjectId(jiraIssue.getBasicProjectConfigId()))) {
					jiraIssue
							.setProjectID(projectIdWiseUniqueId.get(new ObjectId(jiraIssue.getBasicProjectConfigId())));
					kanbanJiraIssueList.add(jiraIssue);
				}
			}
			dataSetToSave.put("KANBAN_JIRA_ISSUE", kanbanJiraIssueList);
		}
		log.info("Kanban Jira Issue Data Processing Data Completed");
	}

	private void updateHappieness(Map<String, Object> dataSetToSave) {
		List<HappinessKpiData> happienessCapacity = happinessKpiDataRepository.findAll();
		Map<String, String> sprintNodeHistory = (Map<String, String>) dataSetToSave.get("SPRINT_HISTORY");
		List<HappinessKpiData> happinessKpiDataList = new ArrayList<>();
		log.info("Happienes Data Processing Data Started");
		if (CollectionUtils.isNotEmpty(happienessCapacity)) {
			for (HappinessKpiData happinessKpiData : happienessCapacity) {
				if (sprintNodeHistory.containsKey(happinessKpiData.getSprintID())) {
					happinessKpiData.setSprintID(sprintNodeHistory.get(happinessKpiData.getSprintID()));
					happinessKpiDataList.add(happinessKpiData);
				}
			}
		}

		log.info("Happienes Data Processing Data Completed");
		dataSetToSave.put("HAPPIENSS", happinessKpiDataList);

	}

	private void updateCapacity(Map<ObjectId, String> projectIdWiseUniqueId, Map<String, Object> dataSetToSave) {
		List<CapacityKpiData> capacityKpiDataList = (List<CapacityKpiData>) capacityKpiDataRepository.findAll();
		List<KanbanCapacity> kanbanCapacityList = (List<KanbanCapacity>) kanbanCapacityRepository.findAll();
		Map<String, String> sprintNodeHistory = (Map<String, String>) dataSetToSave.get("SPRINT_HISTORY");
		List<CapacityKpiData> scrumCapacityKpi = new ArrayList<>();
		List<KanbanCapacity> kanbanCapacities = new ArrayList<>();
		log.info("Scrum Capacity  Processing Data Completed");
		for (CapacityKpiData capacityKpiData : capacityKpiDataList) {
			if (projectIdWiseUniqueId.containsKey(capacityKpiData.getBasicProjectConfigId())
					&& sprintNodeHistory.containsKey(capacityKpiData.getSprintID())) {
				capacityKpiData.setSprintID(sprintNodeHistory.get(capacityKpiData.getSprintID()));
				capacityKpiData.setProjectId(projectIdWiseUniqueId.get(capacityKpiData.getBasicProjectConfigId()));
				scrumCapacityKpi.add(capacityKpiData);
			}
		}
		log.info("Scrum Capacity  Processing Data Completed");
		log.info("Kanban Capacity  Processing Data Started");
		for (KanbanCapacity kanbanCapacity : kanbanCapacityList) {
			if (projectIdWiseUniqueId.containsKey(kanbanCapacity.getBasicProjectConfigId())) {
				kanbanCapacity.setProjectId(projectIdWiseUniqueId.get(kanbanCapacity.getBasicProjectConfigId()));
				kanbanCapacities.add(kanbanCapacity);
			}
		}
		log.info("Kanban Capacity  Processing Data Completed");

		dataSetToSave.put("SCRUM_CAPACITY", scrumCapacityKpi);
		dataSetToSave.put("KANBAN_CAPACITY", kanbanCapacities);

	}

	private void createSprintHierarchy(List<AccountHierarchy> accountHierarchyRepositoryAll,
			Map<ObjectId, String> projectIdWiseUniqueId, List<SprintDetails> allSprintDetails,
			Map<String, Object> dataSetToSave) {

		Map<ObjectId, List<AccountHierarchy>> projectWiseSprints = accountHierarchyRepositoryAll.stream()
				.filter(label -> label.getLabelName().equalsIgnoreCase("sprint"))
				.collect(Collectors.groupingBy(AccountHierarchy::getBasicProjectConfigId));

		Map<ObjectId, List<SprintDetails>> projectWiseSprintDetails = allSprintDetails.stream()
				.collect(Collectors.groupingBy(SprintDetails::getBasicProjectConfigId));

		List<ProjectHierarchy> sprintHierarchyList = new ArrayList<>();
		List<SprintDetails> sprintDetailsList = new ArrayList<>();

		Map<String, String> sprintNodeHistory = new HashMap<>();

		projectWiseSprints.forEach((project, hierarchies) -> {
			// work only if the projects sprints are present
			if (projectIdWiseUniqueId.containsKey(project)) {
				String projectUniqueId = projectIdWiseUniqueId.get(project);
				for (AccountHierarchy accountHierarchy : hierarchies) {
					ProjectHierarchy sprintHierarchy = new ProjectHierarchy();
					sprintHierarchy.setBasicProjectConfigId(accountHierarchy.getBasicProjectConfigId());
					sprintHierarchy.setBeginDate(accountHierarchy.getBeginDate());
					sprintHierarchy.setEndDate(accountHierarchy.getEndDate());
					sprintHierarchy.setHierarchyLevelId(accountHierarchy.getLabelName());
					sprintHierarchy.setNodeName(accountHierarchy.getNodeName());
					sprintHierarchy.setNodeDisplayName(accountHierarchy.getNodeName());
					sprintHierarchy.setParentId(projectUniqueId);
					sprintHierarchy.setNodeId(accountHierarchy.getNodeId().replace(accountHierarchy.getParentId(), "")
							.concat(projectUniqueId));
					sprintNodeHistory.put(accountHierarchy.getNodeId(), sprintHierarchy.getNodeId());
					sprintHierarchyList.add(sprintHierarchy);
				}

				// update sprint details
				List<SprintDetails> sprintDetails = projectWiseSprintDetails.getOrDefault(project, new ArrayList<>());
				for (SprintDetails sprintDetail : sprintDetails) {
					sprintDetail.setSprintID(sprintNodeHistory.getOrDefault(sprintDetail.getSprintID(),
							sprintDetail.getOriginalSprintId() + "_" + projectUniqueId));
				}
				sprintDetailsList.addAll(sprintDetails);

			}

		});
		log.info("Sprint Details  Processing Data Completed");
		dataSetToSave.put("PROJECT_HIERARCHY", sprintHierarchyList);
		dataSetToSave.put("SPRINT_DETAILS", sprintDetailsList);
		dataSetToSave.put("SPRINT_HISTORY", sprintNodeHistory);

	}

	private void createReleaseHierarchy(List<AccountHierarchy> accountHierarchyRepositoryAll,
			List<KanbanAccountHierarchy> kanbanAccountHierarchyList, Map<ObjectId, String> projectIdWiseUniqueId,
			Map<String, Object> dataSetToSave) {
		log.info("Scrum Release Hierarchy Details  Processing Data Started");
		Map<ObjectId, List<AccountHierarchy>> projectWiseScrumReleases = accountHierarchyRepositoryAll.stream()
				.filter(label -> label.getLabelName().equalsIgnoreCase("release"))
				.collect(Collectors.groupingBy(AccountHierarchy::getBasicProjectConfigId));

		Map<ObjectId, List<KanbanAccountHierarchy>> projectWiseKanbanReleases = kanbanAccountHierarchyList.stream()
				.filter(label -> label.getLabelName().equalsIgnoreCase("release"))
				.collect(Collectors.groupingBy(KanbanAccountHierarchy::getBasicProjectConfigId));
		// for scrum
		List<ProjectHierarchy> projectHierarchyList = new ArrayList<>();
		Map<String, String> releaseNodeHistory = new HashMap<>();

		projectWiseScrumReleases.forEach((project, hierarchies) -> {
			if (projectIdWiseUniqueId.containsKey(project)) {
				String projectUniqueId = projectIdWiseUniqueId.get(project);
				for (AccountHierarchy accountHierarchy : hierarchies) {
					ProjectHierarchy releaseHierarchy = new ProjectHierarchy();
					releaseHierarchy.setBasicProjectConfigId(accountHierarchy.getBasicProjectConfigId());
					releaseHierarchy.setReleaseState(accountHierarchy.getReleaseState());
					releaseHierarchy.setBeginDate(accountHierarchy.getBeginDate());
					releaseHierarchy.setEndDate(accountHierarchy.getEndDate());
					releaseHierarchy.setNodeId(accountHierarchy.getNodeId().replace(accountHierarchy.getParentId(), "")
							.concat(projectUniqueId));
					releaseHierarchy.setHierarchyLevelId(accountHierarchy.getLabelName());
					releaseHierarchy.setNodeName(accountHierarchy.getNodeName());
					releaseHierarchy.setNodeDisplayName(accountHierarchy.getNodeName());
					releaseHierarchy.setCreatedDate(accountHierarchy.getCreatedDate());
					releaseHierarchy.setParentId(projectUniqueId);
					releaseNodeHistory.put(accountHierarchy.getNodeId(), releaseHierarchy.getNodeId());
					projectHierarchyList.add(releaseHierarchy);
				}
			}
		});
		log.info("Scrum Release Hierarchy Details  Processing Data Completed");
		log.info("Kanban Release Hierarchy Details  Processing Data Started");
		// kanban projects
		projectWiseKanbanReleases.forEach((project, hierarchies) -> {
			if (projectIdWiseUniqueId.containsKey(project)) {
				String projectUniqueId = projectIdWiseUniqueId.get(project);
				for (KanbanAccountHierarchy accountHierarchy : hierarchies) {
					ProjectHierarchy releaseHierarchy = new ProjectHierarchy();
					releaseHierarchy.setBasicProjectConfigId(accountHierarchy.getBasicProjectConfigId());
					releaseHierarchy.setReleaseState(accountHierarchy.getReleaseState());
					releaseHierarchy.setBeginDate(accountHierarchy.getBeginDate());
					releaseHierarchy.setEndDate(accountHierarchy.getEndDate());
					releaseHierarchy.setNodeId(accountHierarchy.getNodeId().replace(accountHierarchy.getParentId(), "")
							.concat(projectUniqueId));
					releaseHierarchy.setHierarchyLevelId(accountHierarchy.getLabelName());
					releaseHierarchy.setNodeName(accountHierarchy.getNodeName());
					releaseHierarchy.setNodeDisplayName(accountHierarchy.getNodeName());
					releaseHierarchy.setCreatedDate(accountHierarchy.getCreatedDate());
					releaseHierarchy.setParentId(projectUniqueId);
					releaseNodeHistory.put(accountHierarchy.getNodeId(), releaseHierarchy.getNodeId());
					projectHierarchyList.add(releaseHierarchy);
				}
			}
		});

		dataSetToSave.putIfAbsent("PROJECT_HIERARCHY", projectHierarchyList);
		dataSetToSave.computeIfPresent("PROJECT_HIERARCHY", (k, v) -> {
			((List<ProjectHierarchy>) v).addAll(projectHierarchyList);
			return v;
		});
		log.info("Kanban Release Hierarchy Details  Processing Data Completed");
		dataSetToSave.put("RELEASE_HISTORY", releaseNodeHistory);

	}

	private void createAdditinalFilterHierarchy(List<AccountHierarchy> accountHierarchyRepositoryAll,
			List<KanbanAccountHierarchy> kanbanAccountHierarchyList, Map<ObjectId, String> projectIdWiseUniqueId,
			Map<String, Object> dataSetToSave) {
		log.info("Scrum Additional Hierarchy Details  Processing Data Started");
		List<String> additonalFilterCategoryList = additionalFilterCategoryRepository.findAll().stream()
				.map(AdditionalFilterCategory::getFilterCategoryId).toList();

		Map<ObjectId, List<AccountHierarchy>> projectWiseScrumAdditonalFilter = accountHierarchyRepositoryAll.stream()
				.filter(label -> additonalFilterCategoryList.contains(label.getLabelName()))
				.collect(Collectors.groupingBy(AccountHierarchy::getBasicProjectConfigId));

		Map<ObjectId, List<KanbanAccountHierarchy>> projectWiseKanbanAdditonalFilter = kanbanAccountHierarchyList
				.stream().filter(label -> additonalFilterCategoryList.contains(label.getLabelName()))
				.collect(Collectors.groupingBy(KanbanAccountHierarchy::getBasicProjectConfigId));

		// for scrum
		List<ProjectHierarchy> projectHierarchyList = new ArrayList<>();
		Map<String, String> sprintNodeHistory = (Map<String, String>) dataSetToSave.get("SPRINT_HISTORY");

		if (MapUtils.isNotEmpty(sprintNodeHistory)) {
			projectWiseScrumAdditonalFilter.forEach((project, hierarchies) -> {
				if (projectIdWiseUniqueId.containsKey(project)) {
					for (AccountHierarchy accountHierarchy : hierarchies) {
						if (sprintNodeHistory.containsKey(accountHierarchy.getParentId())) {
							ProjectHierarchy additionalHierachy = new ProjectHierarchy();
							additionalHierachy.setBasicProjectConfigId(accountHierarchy.getBasicProjectConfigId());
							additionalHierachy.setNodeId(accountHierarchy.getNodeId());
							additionalHierachy.setHierarchyLevelId(accountHierarchy.getLabelName());
							additionalHierachy.setNodeName(accountHierarchy.getNodeName());
							additionalHierachy.setNodeDisplayName(accountHierarchy.getNodeName());
							additionalHierachy.setCreatedDate(accountHierarchy.getCreatedDate());
							additionalHierachy.setParentId(sprintNodeHistory.get(accountHierarchy.getParentId()));
							projectHierarchyList.add(additionalHierachy);
						}
					}
				}
			});
			log.info("Scrum Additional Hierarchy Details  Processing Data Completed");
			log.info("Kanban Additional Hierarchy Details  Processing Data Started");

			// for kanban
			projectWiseKanbanAdditonalFilter.forEach((project, hierarchies) -> {
				if (projectIdWiseUniqueId.containsKey(project)) {
					for (KanbanAccountHierarchy accountHierarchy : hierarchies) {
						ProjectHierarchy additionalHierachy = new ProjectHierarchy();
						additionalHierachy.setBasicProjectConfigId(accountHierarchy.getBasicProjectConfigId());
						additionalHierachy.setNodeId(accountHierarchy.getNodeId());
						additionalHierachy.setHierarchyLevelId(accountHierarchy.getLabelName());
						additionalHierachy.setNodeName(accountHierarchy.getNodeName());
						additionalHierachy.setNodeDisplayName(accountHierarchy.getNodeName());
						additionalHierachy.setCreatedDate(accountHierarchy.getCreatedDate());
						additionalHierachy.setParentId(projectIdWiseUniqueId.get(project));
						projectHierarchyList.add(additionalHierachy);
					}

				}
			});
		}
		log.info("Kanban Additional Hierarchy Details  Processing Data Completed");
	}

}
