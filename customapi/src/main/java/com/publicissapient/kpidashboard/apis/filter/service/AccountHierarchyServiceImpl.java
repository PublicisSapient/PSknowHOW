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

package com.publicissapient.kpidashboard.apis.filter.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.hierarchy.service.OrganizationHierarchyService;
import com.publicissapient.kpidashboard.apis.model.AccountFilterRequest;
import com.publicissapient.kpidashboard.apis.model.AccountFilteredData;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;
import com.publicissapient.kpidashboard.common.util.DateUtil;

/**
 * Implementation of {@link AccountHierarchyService} to managing all requests to
 * the Aggregated Dashboard KPIs
 *
 * @author pkum34
 */
@Service
public class AccountHierarchyServiceImpl
		implements
			AccountHierarchyService<List<AccountHierarchyData>, Set<AccountFilteredData>> {

	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;

	@Autowired
	private UserAuthorizedProjectsService authorizedProjectsService;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private FilterHelperService filterHelperService;

	@Autowired
	private OrganizationHierarchyService organizationHierarchyService;

	@Autowired
	private ProjectBasicConfigService projectBasicConfigService;

	@Autowired
	private ProjectHierarchyService projectHierarchyService;

	@Override
	public String getQualifierType() {
		return "Scrum";
	}

	@SuppressWarnings("unchecked")
	public Set<AccountFilteredData> getFilteredList(AccountFilterRequest request) {
		List<AccountHierarchyData> hierarchyDataAll = (List<AccountHierarchyData>) cacheService.cacheAccountHierarchyData();
		hierarchyDataAll = filterHelperService.getAccountHierarchyDataForRequest(new HashSet<>(request.getSprintIncluded()),
				hierarchyDataAll);
		Set<String> basicProjectConfigIds = tokenAuthenticationService.getUserProjects();
		if (!authorizedProjectsService.ifSuperAdminUser() && CollectionUtils.isNotEmpty(hierarchyDataAll)) {
			hierarchyDataAll = hierarchyDataAll.stream()
					.filter(data -> basicProjectConfigIds.contains(data.getBasicProjectConfigId().toHexString()))
					.collect(Collectors.toList());
		}
		return processAccountFilteredResponse(hierarchyDataAll);
	}

	private Set<AccountFilteredData> processAccountFilteredResponse(List<AccountHierarchyData> accountHierarchyDataList) {
		Set<AccountFilteredData> result = new HashSet<>();
		accountHierarchyDataList.forEach(accountHierarchyData -> accountHierarchyData.getNode()
				.forEach(node -> result.add(getAccountFilteredResponse(node.getProjectHierarchy(), node.getLevel()))));
		return result;
	}

	private AccountFilteredData getAccountFilteredResponse(ProjectHierarchy acc, int level) {
		AccountFilteredData data = null;
		if (null != acc) {
			if (acc.getHierarchyLevelId().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE)) {
				data = AccountFilteredData.builder().nodeId(acc.getNodeId()).nodeName(acc.getNodeName())
						.nodeDisplayName(acc.getNodeDisplayName()).labelName(acc.getHierarchyLevelId()).parentId(acc.getParentId())
						.releaseState(acc.getReleaseState()).releaseStartDate(acc.getBeginDate()).releaseEndDate(acc.getEndDate())
						.level(level).build();
			} else {
				data = AccountFilteredData.builder().nodeId(acc.getNodeId()).nodeName(acc.getNodeName())
						.nodeDisplayName(acc.getNodeDisplayName()).labelName(acc.getHierarchyLevelId()).parentId(acc.getParentId())
						.sprintState(acc.getSprintState()).sprintStartDate(acc.getBeginDate()).sprintEndDate(acc.getEndDate())
						.level(level).build();
			}
			if (acc.getHierarchyLevelId().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT)) {
				data.setBasicProjectConfigId(acc.getBasicProjectConfigId());
			}
		}
		return data;
	}

	/**
	 * Returns list of AccountHierarchyData
	 *
	 * @return list of {@link AccountHierarchyData}
	 */
	@Override
	public List<AccountHierarchyData> createHierarchyData() {

		List<ProjectBasicConfig> projectBasicConfigList = projectBasicConfigService.getAllProjectsBasicConfigs(false);

		List<ProjectHierarchy> configureHierarchies = getConfigureProjectsHierarchies(projectBasicConfigList,
				organizationHierarchyService, projectHierarchyService);

		Map<String, List<ProjectHierarchy>> parentWiseMap = configureHierarchies.stream()
				.filter(fd -> fd.getParentId() != null).collect(Collectors.groupingBy(ProjectHierarchy::getParentId));

		List<AccountHierarchyData> listHierarchyData = new ArrayList<>();
		String firstLevel = filterHelperService.getFirstHierarachyLevel();

		Map<String, Integer> hierarchyLevelIdMap = filterHelperService.getHierarchyIdLevelMap(false);

		// create map of sprints with key as parent id of sprint
		Map<String, List<ProjectHierarchy>> parentWiseSprintMap = null;
		Map<String, List<ProjectHierarchy>> parentWiseReleaseMap = null;

		List<String> sprintIds = configureHierarchies.stream()
				.filter(x -> CommonConstant.HIERARCHY_LEVEL_ID_SPRINT.equalsIgnoreCase(x.getHierarchyLevelId()))
				.map(ProjectHierarchy::getNodeId).collect(Collectors.toList());
		Map<String, SprintDetails> sprintDetailsMap = fetchSprintDetailsOf(sprintIds);

		parentWiseSprintMap = configureHierarchies.stream()
				.filter(x -> CommonConstant.HIERARCHY_LEVEL_ID_SPRINT.equalsIgnoreCase(x.getHierarchyLevelId()))
				.sorted(Comparator.comparing(ProjectHierarchy::getBeginDate).reversed())
				.collect(Collectors.groupingBy(ProjectHierarchy::getParentId));

		parentWiseReleaseMap = configureHierarchies.stream()
				.filter(x -> (CommonConstant.HIERARCHY_LEVEL_ID_RELEASE.equalsIgnoreCase(x.getHierarchyLevelId())) &&
						(StringUtils.isNotEmpty(x.getBeginDate()) || StringUtils.isNotEmpty(x.getEndDate())))
				.collect(Collectors.groupingBy(ProjectHierarchy::getParentId));

		// create list of sprints ids that need to be displayed in filter.
		Map<String, List<String>> limitedDisplayMap = new HashMap<>();
		parentWiseSprintMap.entrySet()
				.forEach(entry -> limitedDisplayMap.put(entry.getKey(), limitSprints(entry.getValue(), sprintDetailsMap)));

		parentWiseReleaseMap.entrySet().forEach(entry -> {
			List<String> releaseNodeIds = limitRelease(entry.getValue());
			limitedDisplayMap.computeIfPresent(entry.getKey(), (projectId, sprints) -> {
				sprints.addAll(releaseNodeIds);
				return sprints;
			});
			limitedDisplayMap.putIfAbsent(entry.getKey(), releaseNodeIds);
		});

		if (firstLevel != null) {
			configureHierarchies.stream().filter(fd -> fd.getHierarchyLevelId().equalsIgnoreCase(firstLevel))
					.forEach(rootData -> {
						AccountHierarchyData accountHierarchyData = new AccountHierarchyData();
						setValuesInAccountHierarchyData(rootData, accountHierarchyData, null, hierarchyLevelIdMap);
						traverseRootToLeaf(rootData, parentWiseMap, listHierarchyData, accountHierarchyData, hierarchyLevelIdMap,
								limitedDisplayMap, sprintDetailsMap);
					});
		}

		return listHierarchyData;
	}

	private List<String> limitRelease(List<ProjectHierarchy> releaseHierarchies) {
		List<String> releaseNodeId = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(releaseHierarchies)) {
			checkUnreleasedStatus(releaseHierarchies, releaseNodeId);
			checkReleasedStatus(releaseHierarchies, releaseNodeId);
		}
		return releaseNodeId;
	}

	/**
	 * endtime should be within today-2months and today
	 *
	 * @param releaseHierarchies
	 * @param releaseNodeId
	 */
	private void checkReleasedStatus(List<ProjectHierarchy> releaseHierarchies, List<String> releaseNodeId) {
		releaseHierarchies.stream()
				.filter(accountHierarchy -> accountHierarchy.getReleaseState().equalsIgnoreCase(CommonConstant.RELEASED))
				.forEach(accountHierarchy -> {
					if (StringUtils.isNotEmpty(accountHierarchy.getEndDate()) && DateUtil.isWithinDateRange(
							DateUtil.stringToLocalDate(accountHierarchy.getEndDate(), DateUtil.TIME_FORMAT),
							LocalDate.now().minusMonths(2), LocalDate.now())) {
						releaseNodeId.add(accountHierarchy.getNodeId());
					}
				});
	}

	/**
	 * endtime of release should not be greater than todays+6months or starttime of
	 * release should be within today's and today+2months
	 *
	 * @param releaseHierarchies
	 * @param releaseNodeId
	 */
	private void checkUnreleasedStatus(List<ProjectHierarchy> releaseHierarchies, List<String> releaseNodeId) {
		releaseHierarchies.stream()
				.filter(accountHierarchy -> accountHierarchy.getReleaseState().equalsIgnoreCase(CommonConstant.UNRELEASED))
				.forEach(accountHierarchy -> {
					if (StringUtils.isNotEmpty(accountHierarchy.getEndDate()) &&
							DateUtil.stringToLocalDate(accountHierarchy.getEndDate(), DateUtil.TIME_FORMAT)
									.isBefore(LocalDate.now().plusMonths(6).plusDays(1)) ||
							(StringUtils.isNotEmpty(accountHierarchy.getBeginDate()) && DateUtil.isWithinDateRange(
									DateUtil.stringToLocalDate(accountHierarchy.getBeginDate(), DateUtil.TIME_FORMAT), LocalDate.now(),
									LocalDate.now().plusMonths(1)))) {
						releaseNodeId.add(accountHierarchy.getNodeId());
					}
				});
	}

	private List<String> limitSprints(List<ProjectHierarchy> accountHierarchies,
			Map<String, SprintDetails> sprintDetailsMap) {
		// TODO: category check
		List<ProjectHierarchy> withSprintAAccountHierracchiesList = accountHierarchies.stream()
				.filter(x -> CommonConstant.HIERARCHY_LEVEL_ID_SPRINT.equalsIgnoreCase(x.getHierarchyLevelId()))
				.collect(Collectors.toList());

		// closed sprint limit
		List<String> finalList = withSprintAAccountHierracchiesList.stream()
				.filter(accountHierarchy -> !isNotClosedSprint(accountHierarchy.getNodeId(), sprintDetailsMap))
				.limit(customApiConfig.getSprintCountForFilters()).map(ProjectHierarchy::getNodeId)
				.collect(Collectors.toList());

		// not closed sprints
		finalList.addAll(withSprintAAccountHierracchiesList.stream()
				.filter(accountHierarchy -> isNotClosedSprint(accountHierarchy.getNodeId(), sprintDetailsMap))
				.map(ProjectHierarchy::getNodeId).collect(Collectors.toList()));

		return finalList;
	}

	private boolean isNotClosedSprint(String nodeId, Map<String, SprintDetails> sprintDetailsMap) {

		SprintDetails sprintDetails = sprintDetailsMap.get(nodeId);

		return sprintDetails == null || sprintDetails.getState() == null ||
				!SprintDetails.SPRINT_STATE_CLOSED.equalsIgnoreCase(sprintDetails.getState());
	}

	private Map<String, SprintDetails> fetchSprintDetailsOf(List<String> sprintIds) {
		List<SprintDetails> sprintDetailsList = sprintRepository.findBySprintIDInGetStatus(sprintIds);

		return sprintDetailsList.stream().collect(Collectors.toMap(SprintDetails::getSprintID, Function.identity()));
	}

	/**
	 * Checks if should show in filter or not
	 *
	 * @param sprintIdListToDisplay
	 * @param accountHierarchy
	 * @return true if needs to show in filter
	 */
	// TODO: category check
	private boolean showInFilters(List<String> sprintIdListToDisplay, OrganizationHierarchy accountHierarchy) {
		boolean show = true;
		if (!CollectionUtils.isEmpty(sprintIdListToDisplay) &&
				(accountHierarchy.getHierarchyLevelId().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT) ||
						accountHierarchy.getHierarchyLevelId().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE))) {
			show = sprintIdListToDisplay.contains(accountHierarchy.getNodeId());
		}
		return show;
	}

	/**
	 * Check is current node is Child Node
	 *
	 * @param accountHierarchy
	 * @param childAccountHierarchy
	 * @return true if current node label is child node label
	 */
	private boolean isCurrentNodeChild(ProjectHierarchy accountHierarchy, ProjectHierarchy childAccountHierarchy) {
		if (childAccountHierarchy.getHierarchyLevelId() == null) {
			return false;
		}
		String parentLabel = accountHierarchy.getNodeId();
		return StringUtils.equalsIgnoreCase(parentLabel, childAccountHierarchy.getParentId());
	}

	private void traverseRootToLeaf(ProjectHierarchy hierarchy, Map<String, List<ProjectHierarchy>> parentWiseMap,
			List<AccountHierarchyData> listHierarchyData, AccountHierarchyData accountHierarchyData,
			Map<String, Integer> hierarchyLevelIdMap, Map<String, List<String>> sprintIdListToDisplay,
			Map<String, SprintDetails> sprintDetailsMap) {
		if (parentWiseMap.containsKey(hierarchy.getNodeId())) {
			parentWiseMap.get(hierarchy.getNodeId()).stream()
					.filter(child -> showInFilters(sprintIdListToDisplay.get(hierarchy.getNodeId()), child)
					// todo remove isParentChildHierarchy
							&& isCurrentNodeChild(hierarchy, child))
					.forEach(child -> {
						SprintDetails sprintDetails = sprintDetailsMap.get(child.getNodeId());
						if (!child.getHierarchyLevelId().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT) ||
								(child.getHierarchyLevelId().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT) &&
										null != sprintDetails && null != sprintDetails.getState())) {
							AccountHierarchyData accountHierarchyDataClone = (AccountHierarchyData) SerializationUtils
									.clone(accountHierarchyData);
							setValuesInAccountHierarchyData(child, accountHierarchyDataClone, sprintDetails, hierarchyLevelIdMap);
							traverseRootToLeaf(child, parentWiseMap, listHierarchyData, accountHierarchyDataClone,
									hierarchyLevelIdMap, sprintIdListToDisplay, sprintDetailsMap);
						} else {
							accountHierarchyData.setBasicProjectConfigId(hierarchy.getBasicProjectConfigId());
							accountHierarchyData.setLabelName(hierarchy.getHierarchyLevelId());
							listHierarchyData.add(accountHierarchyData);
						}
					});
		} else {
			accountHierarchyData.setBasicProjectConfigId(hierarchy.getBasicProjectConfigId());
			accountHierarchyData.setLabelName(hierarchy.getHierarchyLevelId());
			listHierarchyData.add(accountHierarchyData);
		}
	}

	/**
	 * Creates account hierarchy data
	 *
	 * @param hierarchy
	 * @param accountHierarchyData
	 */
	private void setValuesInAccountHierarchyData(ProjectHierarchy hierarchy, AccountHierarchyData accountHierarchyData,
			SprintDetails sprintDetails, Map<String, Integer> hierarchyLevelIdMap) {
		if (sprintDetails != null) {
			hierarchy.setSprintState(sprintDetails.getState());
			hierarchy.setBeginDate(sprintDetails.getStartDate());
			hierarchy.setEndDate(sprintDetails.getEndDate());
		}
		Node node = new Node(0, hierarchy.getNodeId(), hierarchy.getNodeDisplayName(), hierarchy.getParentId(),
				hierarchy.getHierarchyLevelId(), hierarchy);
		node.setLevel(hierarchyLevelIdMap.getOrDefault(hierarchy.getHierarchyLevelId(), 0));
		accountHierarchyData.setLabelName(hierarchy.getHierarchyLevelId());
		accountHierarchyData.setLeafNodeId(hierarchy.getNodeId());
		if (CollectionUtils.isEmpty(accountHierarchyData.getNode())) {
			List<Node> dnode = new ArrayList<>();
			dnode.add(node);
			accountHierarchyData.setNode(dnode);
		} else {
			accountHierarchyData.getNode().add(node);
		}
	}
}
