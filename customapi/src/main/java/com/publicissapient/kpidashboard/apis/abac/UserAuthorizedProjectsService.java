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

package com.publicissapient.kpidashboard.apis.abac;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.SuperAdminRoles;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author neechauh0
 *
 */
@Service
@Slf4j
public class UserAuthorizedProjectsService {

	@Autowired
	TokenAuthenticationService tokenAuthenticationService;
	@Autowired
	private UserInfoServiceImpl userInfoService;
	@Autowired
	private AccountHierarchyRepository accountHierarchyRepo;
	@Autowired
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepository;
	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private CacheService cacheService;

	/**
	 * @return if a user is a SUPERADMIN user or not
	 */
	public boolean ifSuperAdminUser() {
		UserInfo user = userInfoService.getUserInfo(authenticationService.getLoggedInUser());
		boolean isSuperAdmin = false;
		List<String> adminRoles = SuperAdminRoles.getAdminRoles();
		if (user.getAuthorities().stream().anyMatch(adminRoles::contains)) {
			isSuperAdmin = true;
		}
		return isSuperAdmin;
	}

	/**
	 * FOR SCRUM KPIs
	 *
	 * @param filteredAccountDataList
	 * @return if projects coming in requests matches with the assigned user
	 *         Projects
	 */
	public Boolean checkUserAuthForProjects(List<AccountHierarchyData> filteredAccountDataList) {
		Set<String> configIdName = tokenAuthenticationService.getUserProjects();
		List<String> userProject = getProjectNodeIds(configIdName);
		Set<String> requestedProjects = getProjectNodesForRequest(filteredAccountDataList);
		return userProject.containsAll(requestedProjects);
	}

	/**
	 * FOR SCRUM KPIs
	 *
	 * @param projects
	 * @return Node Ids of projects
	 */
	public List<String> getProjectNodeIds(Set<String> projects) {
		List<String> nodeIds = new ArrayList<>();
		List<AccountHierarchy> label = accountHierarchyRepo.findAll();
		if (!projects.isEmpty()) {
			label = label.stream()
					.filter(data -> projects.contains(data.getBasicProjectConfigId().toString())
							&& data.getLabelName().equalsIgnoreCase(CommonConstant.PROJECT))
					.collect(Collectors.toList());

			label.forEach(action -> nodeIds.add(action.getNodeId()));
		}
		return nodeIds;
	}

	/**
	 * FOR SCRUM KPIs
	 *
	 * @param filteredAccountDataList
	 * @return
	 */
	public Set<String> getProjectNodesForRequest(List<AccountHierarchyData> filteredAccountDataList) {

		Set<String> projectNodes = new HashSet<>();
		filteredAccountDataList.forEach(element -> projectNodes.addAll(element.getNode().stream().filter(
				projectNode -> projectNode.getGroupName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT))
				.map(Node::getId).collect(Collectors.toSet())));
		return projectNodes;

	}
	//

	/**
	 * FOR SCRUM KPIs
	 *
	 * @param filteredAccountDataList
	 * @return
	 */
	public List<AccountHierarchyData> filterProjects(List<AccountHierarchyData> filteredAccountDataList) {
		List<AccountHierarchyData> filteredAccountData;
		Set<String> projects = tokenAuthenticationService.getUserProjects();
		filteredAccountData = filteredAccountDataList.stream()
				.filter(projectId -> projects.contains(projectId.getBasicProjectConfigId().toString()))
				.collect(Collectors.toList());

		return filteredAccountData;

	}

	/*
	 * FOR SCRUM KPIs
	 *
	 * @param filteredAccountDataList
	 *
	 * @param kpiRequest
	 *
	 * @return
	 */
	public String[] getProjectKey(List<AccountHierarchyData> filteredAccountDataList, KpiRequest kpiRequest) {

		Set<String> projects = getProjectNodesForRequest(filteredAccountDataList);
		List<String> ids = Arrays.asList(kpiRequest.getIds());
		List<String> keys = Stream.concat(projects.stream(), ids.stream()).collect(Collectors.toList());
		if(kpiRequest.getSelectedMap().get(Constant.DATE) != null)
			keys.addAll(kpiRequest.getSelectedMap().get(Constant.DATE));
		return keys.stream().toArray(String[]::new);
	}

	/**
	 * For Kanban KPIs
	 *
	 * @param filteredAccountDataList
	 * @return
	 */
	public boolean checkKanbanUserAuthForProjects(List<AccountHierarchyDataKanban> filteredAccountDataList) {
		Set<String> projects = tokenAuthenticationService.getUserProjects();
		List<String> userProject = getKanbanProjectNodeIds(projects);
		Set<String> requestedProjects = getKanbanProjectNodesForRequest(filteredAccountDataList);
		return userProject.containsAll(requestedProjects);
	}

	/**
	 * For Kanban KPIs
	 *
	 * @param filteredAccountDataList
	 * @return
	 */
	public List<AccountHierarchyDataKanban> filterKanbanProjects(
			List<AccountHierarchyDataKanban> filteredAccountDataList) {
		List<AccountHierarchyDataKanban> filteredAccountData;
		Set<String> projects = tokenAuthenticationService.getUserProjects();
		filteredAccountData = filteredAccountDataList.stream()
				.filter(projectId -> projects.contains(projectId.getBasicProjectConfigId().toString()))
				.collect(Collectors.toList());
		return filteredAccountData;
	}

	/**
	 * For Kanban KPIs
	 *
	 * @param projects
	 * @return
	 */
	private List<String> getKanbanProjectNodeIds(Set<String> projects) {
		List<KanbanAccountHierarchy> label = kanbanAccountHierarchyRepository.findAll();
		label = label.stream().filter(data -> projects.contains(data.getBasicProjectConfigId().toString())
				&& data.getLabelName().equalsIgnoreCase(CommonConstant.PROJECT)).collect(Collectors.toList());
		List<String> nodeIds = new ArrayList<>();
		label.forEach(action -> nodeIds.add(action.getNodeId()));
		return nodeIds;
	}

	/**
	 * For Kanban KPIs
	 *
	 * @param filteredAccountDataList
	 * @return
	 */
	public Set<String> getKanbanProjectNodesForRequest(List<AccountHierarchyDataKanban> filteredAccountDataList) {

		Set<String> projectNodes = new HashSet<>();
		filteredAccountDataList.forEach(element -> projectNodes.addAll(element.getNode().stream().filter(
				projectNode -> projectNode.getGroupName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT))
				.map(Node::getId).collect(Collectors.toSet())));
		return projectNodes;

	}

	/**
	 * For Kanban KPIs
	 *
	 * @param filteredAccountDataList
	 * @param kpiRequest
	 * @return
	 */
	public String[] getKanbanProjectKey(List<AccountHierarchyDataKanban> filteredAccountDataList,
			KpiRequest kpiRequest) {

		Set<String> projects = getSelectedIds(kpiRequest);
		List<String> ids = Arrays.asList(kpiRequest.getIds());
		log.debug("{}" + filteredAccountDataList.size());
		List<String> keys = Stream.concat(projects.stream(), ids.stream()).collect(Collectors.toList());
		List<String> dateFilters = new ArrayList<>();
		dateFilters.add(LocalDate.now().toString());
		dateFilters.addAll(kpiRequest.getSelectedMap().get(Constant.DATE));
		keys.addAll(dateFilters);
		String[] projectKeys = new String[keys.size()];
		int index = 0;
		for (String str : keys)
			projectKeys[index++] = str;

		return projectKeys;

	}

	/**
	 * This method populate id in kanban scenario
	 * 
	 * @param kpiRequest
	 *            kpiRequest
	 */
	private Set<String> getSelectedIds(KpiRequest kpiRequest) {
		Set<String> ids = new HashSet<>();
		Map<String, List<String>> selectedMap = kpiRequest.getSelectedMap();

		List<HierarchyLevel> hiearachyLevel = cacheService.getFullKanbanHierarchyLevel();
		List<String> kanbanHierarchyOrder = Lists.reverse(hiearachyLevel).stream()
				.map(HierarchyLevel::getHierarchyLevelId).collect(Collectors.toList());

		for (String hierarchyLevel : kanbanHierarchyOrder) {
			if (CollectionUtils.isNotEmpty(selectedMap.get(hierarchyLevel))) {
				ids.addAll(selectedMap.get(hierarchyLevel));
				break;
			}
		}
		return ids;
	}

}
