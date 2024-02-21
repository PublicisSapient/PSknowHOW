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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.model.AccountFilterRequest;
import com.publicissapient.kpidashboard.apis.model.AccountFilteredData;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * class managing all requests to the Aggregated Dashboard KPIs
 *
 * @author pkum34
 */
@Service
@Slf4j
public class AccountHierarchyServiceKanbanImpl// NOPMD
		implements AccountHierarchyService<List<AccountHierarchyDataKanban>, Set<AccountFilteredData>> {

	@Autowired
	private KanbanAccountHierarchyRepository accountHierarchyRepository;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;

	@Autowired
	private UserAuthorizedProjectsService authorizedProjectsService;

	@Autowired
	private FilterHelperService filterHelperService;

	@Override
	public String getQualifierType() {
		return "Kanban";
	}

	@SuppressWarnings("unchecked")
	public Set<AccountFilteredData> getFilteredList(AccountFilterRequest request) {
		List<AccountHierarchyDataKanban> hierarchyDataAll = (List<AccountHierarchyDataKanban>) cacheService
				.cacheAccountHierarchyKanbanData();
		Set<String> basicProjectConfigIds = tokenAuthenticationService.getUserProjects();
		if (!authorizedProjectsService.ifSuperAdminUser() && CollectionUtils.isNotEmpty(hierarchyDataAll)) {
			hierarchyDataAll = hierarchyDataAll.stream()
					.filter(data -> basicProjectConfigIds.contains(data.getBasicProjectConfigId().toHexString()))
					.collect(Collectors.toList());
		}
		return processAccountFilteredResponse(hierarchyDataAll);
	}

	private Set<AccountFilteredData> processAccountFilteredResponse(
			List<AccountHierarchyDataKanban> accountHierarchyDataList) {
		Set<AccountFilteredData> result = new HashSet<>();
		accountHierarchyDataList.forEach(accountHierarchyData -> accountHierarchyData.getNode().forEach(
				node -> result.add(getAccountFilteredResponse(node.getAccountHierarchyKanban(), node.getLevel()))));
		return result;
	}

	private AccountFilteredData getAccountFilteredResponse(KanbanAccountHierarchy acc, int level) {
		AccountFilteredData data = null;
		if (null != acc) {
			data = AccountFilteredData.builder().nodeId(acc.getNodeId()).nodeName(acc.getNodeName())
					.labelName(acc.getLabelName()).parentId(acc.getParentId()).path(acc.getPath()).level(level).build();

			if (acc.getLabelName().equalsIgnoreCase(CommonConstant.PROJECT)) {
				data.setBasicProjectConfigId(acc.getBasicProjectConfigId());
			}
		}

		return data;
	}

	/**
	 * {Inherit Doc}
	 */
	@Override
	public List<AccountHierarchyDataKanban> createHierarchyData() {

		List<KanbanAccountHierarchy> filterDataList = accountHierarchyRepository.findAll();
		Map<String, List<KanbanAccountHierarchy>> parentWiseMap = filterDataList.stream()
				.filter(fd -> fd.getParentId() != null)
				.collect(Collectors.groupingBy(KanbanAccountHierarchy::getParentId));
		// Java 8 merge function is used to handle duplicates in the map
		String firstLevel = filterHelperService.getFirstHierarachyLevel();

		Map<String, Integer> hierarchyLevelIdMap = filterHelperService.getHierarchyIdLevelMap(true);

		List<AccountHierarchyDataKanban> listHierarchyData = new ArrayList<>();
		if (firstLevel != null) {
			filterDataList.stream().filter(fd -> fd.getLabelName().equalsIgnoreCase(firstLevel)).forEach(rootData -> {
				AccountHierarchyDataKanban accountHierarchyData = new AccountHierarchyDataKanban();
				createHierarchyData(rootData, accountHierarchyData, hierarchyLevelIdMap);
				traverseRootToLeaf(rootData, parentWiseMap, listHierarchyData, accountHierarchyData,
						hierarchyLevelIdMap);
			});
		}
		return listHierarchyData;
	}

	/**
	 * Traverse from root node of Account hierarchy till child node to create a
	 * Account Hierarchy data from that route
	 * 
	 * @param hierarchy
	 * @param parentWiseMap
	 * @param listHierarchyData
	 * @param accountHierarchyData
	 * @param hierarchyLevelIdMap
	 */
	private void traverseRootToLeaf(KanbanAccountHierarchy hierarchy,
			Map<String, List<KanbanAccountHierarchy>> parentWiseMap, List<AccountHierarchyDataKanban> listHierarchyData,
			AccountHierarchyDataKanban accountHierarchyData, Map<String, Integer> hierarchyLevelIdMap) {
		// Check if the current node has child nodes
		if (parentWiseMap.containsKey(hierarchy.getNodeId())) {
			parentWiseMap.get(hierarchy.getNodeId()).stream().filter(child -> isCurrentNodeChild(hierarchy, child))
					.forEach(child -> {
						AccountHierarchyDataKanban accountHierarchyDataClone = (AccountHierarchyDataKanban) SerializationUtils
								.clone(accountHierarchyData);
						createHierarchyData(child, accountHierarchyDataClone, hierarchyLevelIdMap);
						traverseRootToLeaf(child, parentWiseMap, listHierarchyData, accountHierarchyDataClone,
								hierarchyLevelIdMap);
					});
		} else {
			accountHierarchyData.setBasicProjectConfigId(hierarchy.getBasicProjectConfigId());
			accountHierarchyData.setLabelName(hierarchy.getLabelName());
			listHierarchyData.add(accountHierarchyData);
		}
	}

	/**
	 * Check is current node is Child Node
	 *
	 * @param accountHierarchy
	 * @param childAccountHierarchy
	 * @return
	 */
	private boolean isCurrentNodeChild(KanbanAccountHierarchy accountHierarchy,
			KanbanAccountHierarchy childAccountHierarchy) {
		if (childAccountHierarchy.getLabelName() == null) {
			return false;
		}
		String parentLabel = accountHierarchy.getNodeId();
		return StringUtils.equalsIgnoreCase(parentLabel, childAccountHierarchy.getParentId());
	}

	/**
	 * Creates account hierarchy data
	 *
	 * @param hierarchy
	 * @param accountHierarchyData
	 */
	private void createHierarchyData(KanbanAccountHierarchy hierarchy, AccountHierarchyDataKanban accountHierarchyData,
			Map<String, Integer> hierarchyLevelIdMap) {
		Node node = new Node(0, hierarchy.getNodeId(), hierarchy.getNodeName(), hierarchy.getParentId(),
				hierarchy.getLabelName(), hierarchy);
		node.setLevel(hierarchyLevelIdMap.get(hierarchy.getLabelName()));
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