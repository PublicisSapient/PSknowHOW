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
import com.publicissapient.kpidashboard.apis.model.AccountFilterRequest;
import com.publicissapient.kpidashboard.apis.model.AccountFilteredData;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

/**
 * Implementation of {@link AccountHierarchyService} to managing all requests to
 * the Aggregated Dashboard KPIs
 *
 * @author pkum34
 */
@Service
public class AccountHierarchyServiceImpl
		implements AccountHierarchyService<List<AccountHierarchyData>, Set<AccountFilteredData>> {

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

	@Override
	public String getQualifierType() {
		return "Scrum";
	}

	@SuppressWarnings("unchecked")
	public Set<AccountFilteredData> getFilteredList(AccountFilterRequest request) {
		List<AccountHierarchyData> hierarchyDataAll = (List<AccountHierarchyData>) cacheService
				.cacheAccountHierarchyData();
		hierarchyDataAll = filterHelperService
				.getAccountHierarchyDataForRequest(new HashSet<>(request.getSprintIncluded()), hierarchyDataAll);
		Set<String> basicProjectConfigIds = tokenAuthenticationService.getUserProjects();
		if (!authorizedProjectsService.ifSuperAdminUser() && CollectionUtils.isNotEmpty(hierarchyDataAll)) {
			hierarchyDataAll = hierarchyDataAll.stream()
					.filter(data -> basicProjectConfigIds.contains(data.getBasicProjectConfigId().toHexString()))
					.collect(Collectors.toList());
		}
		return processAccountFilteredResponse(hierarchyDataAll);
	}

	private Set<AccountFilteredData> processAccountFilteredResponse(
			List<AccountHierarchyData> accountHierarchyDataList) {
		Set<AccountFilteredData> result = new HashSet<>();
		accountHierarchyDataList.forEach(accountHierarchyData -> accountHierarchyData.getNode()
				.forEach(node -> result.add(getAccountFilteredResponse(node.getAccountHierarchy(), node.getLevel()))));
		return result;
	}

	private AccountFilteredData getAccountFilteredResponse(AccountHierarchy acc, int level) {
		AccountFilteredData data = null;
		if (null != acc) {
			if (acc.getLabelName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE)) {
				data = AccountFilteredData.builder().nodeId(acc.getNodeId()).nodeName(acc.getNodeName())
						.labelName(acc.getLabelName()).parentId(acc.getParentId()).path(acc.getPath())
						.releaseState(acc.getReleaseState()).releaseStartDate(acc.getBeginDate())
						.releaseEndDate(acc.getEndDate()).level(level).build();
			} else {
				data = AccountFilteredData.builder().nodeId(acc.getNodeId()).nodeName(acc.getNodeName())
						.labelName(acc.getLabelName()).parentId(acc.getParentId()).path(acc.getPath())
						.sprintState(acc.getSprintState()).sprintStartDate(acc.getBeginDate())
						.sprintEndDate(acc.getEndDate()).level(level).build();
			}
			if (acc.getLabelName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT)) {
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

		List<AccountHierarchy> filterDataList = accountHierarchyRepository.findAll();
		Map<String, List<AccountHierarchy>> parentWiseMap = filterDataList.stream()
				.filter(fd -> fd.getParentId() != null).collect(Collectors.groupingBy(AccountHierarchy::getParentId));

		List<AccountHierarchyData> listHierarchyData = new ArrayList<>();
		String firstLevel = filterHelperService.getFirstHierarachyLevel();

		Map<String, Integer> hierarchyLevelIdMap = filterHelperService.getHierarchyIdLevelMap(false);

		// create map of sprints with key as parent id of sprint
		Map<String, List<AccountHierarchy>> parentWiseSprintMap = null;
		Map<String, List<AccountHierarchy>> parentWiseReleaseMap = null;
		List<String> sprintIds = filterDataList.stream()
				.filter(x -> CommonConstant.HIERARCHY_LEVEL_ID_SPRINT.equalsIgnoreCase(x.getLabelName()))
				.map(AccountHierarchy::getNodeId).collect(Collectors.toList());
		Map<String, SprintDetails> sprintDetailsMap = fetchSprintDetailsOf(sprintIds);

		parentWiseSprintMap = filterDataList.stream()
				.filter(x -> CommonConstant.HIERARCHY_LEVEL_ID_SPRINT.equalsIgnoreCase(x.getLabelName()))
				.sorted(Comparator.comparing(AccountHierarchy::getBeginDate).reversed())
				.collect(Collectors.groupingBy(AccountHierarchy::getParentId));

		parentWiseReleaseMap = filterDataList.stream()
				.filter(x -> (CommonConstant.HIERARCHY_LEVEL_ID_RELEASE.equalsIgnoreCase(x.getLabelName()))
						&& (StringUtils.isNotEmpty(x.getBeginDate()) || StringUtils.isNotEmpty(x.getEndDate())))
				.collect(Collectors.groupingBy(AccountHierarchy::getParentId));

		// create list of sprints ids that need to be displayed in filter.
		Map<String, List<String>> limitedDisplayMap = new HashMap<>();
		parentWiseSprintMap.entrySet().forEach(
				entry -> limitedDisplayMap.put(entry.getKey(), limitSprints(entry.getValue(), sprintDetailsMap)));

		parentWiseReleaseMap.entrySet().forEach(entry -> {
			List<String> releaseNodeIds = limitRelease(entry.getValue());
			limitedDisplayMap.computeIfPresent(entry.getKey(), (projectId, sprints) -> {
				sprints.addAll(releaseNodeIds);
				return sprints;
			});
			limitedDisplayMap.putIfAbsent(entry.getKey(), releaseNodeIds);

		});

		if (firstLevel != null) {
			filterDataList.stream().filter(fd -> fd.getLabelName().equalsIgnoreCase(firstLevel)).forEach(rootData -> {
				AccountHierarchyData accountHierarchyData = new AccountHierarchyData();
				setValuesInAccountHierarchyData(rootData, accountHierarchyData, null, hierarchyLevelIdMap);
				traverseRootToLeaf(rootData, parentWiseMap, listHierarchyData, accountHierarchyData,
						hierarchyLevelIdMap, limitedDisplayMap, sprintDetailsMap);
			});
		}

		return listHierarchyData;
	}

	private List<String> limitRelease(List<AccountHierarchy> releaseHierarchies) {
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
	private void checkReleasedStatus(List<AccountHierarchy> releaseHierarchies, List<String> releaseNodeId) {
		releaseHierarchies.stream().filter(
				accountHierarchy -> accountHierarchy.getReleaseState().equalsIgnoreCase(CommonConstant.RELEASED))
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
	private void checkUnreleasedStatus(List<AccountHierarchy> releaseHierarchies, List<String> releaseNodeId) {
		releaseHierarchies.stream().filter(
				accountHierarchy -> accountHierarchy.getReleaseState().equalsIgnoreCase(CommonConstant.UNRELEASED))
				.forEach(accountHierarchy -> {
					if (StringUtils.isNotEmpty(accountHierarchy.getEndDate())
							&& DateUtil.stringToLocalDate(accountHierarchy.getEndDate(), DateUtil.TIME_FORMAT)
									.isBefore(LocalDate.now().plusMonths(6).plusDays(1))
							|| (StringUtils.isNotEmpty(accountHierarchy.getBeginDate()) && DateUtil.isWithinDateRange(
									DateUtil.stringToLocalDate(accountHierarchy.getBeginDate(), DateUtil.TIME_FORMAT),
									LocalDate.now(), LocalDate.now().plusMonths(1)))) {
						releaseNodeId.add(accountHierarchy.getNodeId());
					}
				});
	}

	private List<String> limitSprints(List<AccountHierarchy> accountHierarchies,
			Map<String, SprintDetails> sprintDetailsMap) {
		List<AccountHierarchy> withSprintAAccountHierracchiesList = accountHierarchies.stream()
				.filter(x -> CommonConstant.HIERARCHY_LEVEL_ID_SPRINT.equalsIgnoreCase(x.getLabelName()))
				.collect(Collectors.toList());

		// closed sprint limit
		List<String> finalList = withSprintAAccountHierracchiesList.stream()
				.filter(accountHierarchy -> !isNotClosedSprint(accountHierarchy.getNodeId(), sprintDetailsMap))
				.limit(customApiConfig.getSprintCountForFilters()).map(AccountHierarchy::getNodeId)
				.collect(Collectors.toList());

		// not closed sprints
		finalList.addAll(withSprintAAccountHierracchiesList.stream()
				.filter(accountHierarchy -> isNotClosedSprint(accountHierarchy.getNodeId(), sprintDetailsMap))
				.map(AccountHierarchy::getNodeId).collect(Collectors.toList()));

		return finalList;
	}

	private boolean isNotClosedSprint(String nodeId, Map<String, SprintDetails> sprintDetailsMap) {

		SprintDetails sprintDetails = sprintDetailsMap.get(nodeId);

		return sprintDetails == null || sprintDetails.getState() == null
				|| !SprintDetails.SPRINT_STATE_CLOSED.equalsIgnoreCase(sprintDetails.getState());
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
	private boolean showInFilters(List<String> sprintIdListToDisplay, AccountHierarchy accountHierarchy) {
		boolean show = true;
		if (!CollectionUtils.isEmpty(sprintIdListToDisplay) && (accountHierarchy.getLabelName()
				.equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT)
				|| accountHierarchy.getLabelName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE))) {
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
	private boolean isCurrentNodeChild(AccountHierarchy accountHierarchy, AccountHierarchy childAccountHierarchy) {
		if (childAccountHierarchy.getLabelName() == null) {
			return false;
		}
		String parentLabel = accountHierarchy.getNodeId();
		return StringUtils.equalsIgnoreCase(parentLabel, childAccountHierarchy.getParentId());
	}

	private void traverseRootToLeaf(AccountHierarchy hierarchy, Map<String, List<AccountHierarchy>> parentWiseMap,
			List<AccountHierarchyData> listHierarchyData, AccountHierarchyData accountHierarchyData,
			Map<String, Integer> hierarchyLevelIdMap, Map<String, List<String>> sprintIdListToDisplay,
			Map<String, SprintDetails> sprintDetailsMap) {

		if (parentWiseMap.containsKey(hierarchy.getNodeId())) {
			parentWiseMap.get(hierarchy.getNodeId()).stream()
					.filter(child -> showInFilters(sprintIdListToDisplay.get(hierarchy.getNodeId()), child)
							&& isCurrentNodeChild(hierarchy, child) && isParentChildHierarchy(hierarchy, child))
					.forEach(child -> {
						SprintDetails sprintDetails = sprintDetailsMap.get(child.getNodeId());
						if (!child.getLabelName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT)
								|| (child.getLabelName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT)
										&& null != sprintDetails && null != sprintDetails.getState())) {
							AccountHierarchyData accountHierarchyDataClone = (AccountHierarchyData) SerializationUtils
									.clone(accountHierarchyData);
							setValuesInAccountHierarchyData(child, accountHierarchyDataClone, sprintDetails,
									hierarchyLevelIdMap);
							traverseRootToLeaf(child, parentWiseMap, listHierarchyData, accountHierarchyDataClone,
									hierarchyLevelIdMap, sprintIdListToDisplay, sprintDetailsMap);
						} else {
							accountHierarchyData.setBasicProjectConfigId(hierarchy.getBasicProjectConfigId());
							accountHierarchyData.setLabelName(hierarchy.getLabelName());
							listHierarchyData.add(accountHierarchyData);
						}
					});
		} else {
			accountHierarchyData.setBasicProjectConfigId(hierarchy.getBasicProjectConfigId());
			accountHierarchyData.setLabelName(hierarchy.getLabelName());
			listHierarchyData.add(accountHierarchyData);
		}
	}

	private boolean isParentChildHierarchy(AccountHierarchy parent, AccountHierarchy child) {
		if (StringUtils.isEmpty(parent.getPath())) {
			return true;
		} else {
			return child.getPath()
					.contains(parent.getNodeId() + CommonConstant.ACC_HIERARCHY_PATH_SPLITTER + parent.getPath());
		}
	}

	/**
	 * Creates account hierarchy data
	 *
	 * @param hierarchy
	 * @param accountHierarchyData
	 */
	private void setValuesInAccountHierarchyData(AccountHierarchy hierarchy, AccountHierarchyData accountHierarchyData,
			SprintDetails sprintDetails, Map<String, Integer> hierarchyLevelIdMap) {
		if (sprintDetails != null) {
			hierarchy.setSprintState(sprintDetails.getState());
			hierarchy.setBeginDate(sprintDetails.getStartDate());
			hierarchy.setEndDate(sprintDetails.getEndDate());
		}
		Node node = new Node(0, hierarchy.getNodeId(), hierarchy.getNodeName(), hierarchy.getParentId(),
				hierarchy.getLabelName(), hierarchy);
		node.setLevel(hierarchyLevelIdMap.getOrDefault(hierarchy.getLabelName(), 0));
		accountHierarchyData.setLabelName(hierarchy.getLabelName());
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