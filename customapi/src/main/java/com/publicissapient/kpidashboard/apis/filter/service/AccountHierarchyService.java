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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.apis.hierarchy.service.OrganizationHierarchyService;
import com.publicissapient.kpidashboard.apis.model.AccountFilterRequest;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;

/**
 * Interface to managing all requests to the Aggregated Dashboard KPIs
 *
 * @author pkum34
 */
public interface AccountHierarchyService<R, S> {

	/**
	 * Gets Filter options.
	 *
	 * @param filter
	 * @return {@code List<AccountFilterResponse>}
	 */
	S getFilteredList(AccountFilterRequest filter);

	/**
	 * Creates and Returns hierarchy data.
	 *
	 * @return hierarchy data
	 */
	R createHierarchyData();

	/**
	 * Gets qualifier type. For Example "Scrum" or "Kanban"
	 *
	 * @return qualifier type
	 */
	String getQualifierType();

	/**
	 * get Configure projects of Organization Hierarchies Node and Project
	 * Hierarchies Nodes using ProjectBasicConfig
	 *
	 * @param projectBasicConfigList
	 * @param organizationHierarchyService
	 * @param projectHierarchyService
	 * @return
	 */
	default List<ProjectHierarchy> getConfigureProjectsHierarchies(List<ProjectBasicConfig> projectBasicConfigList,
			OrganizationHierarchyService organizationHierarchyService, ProjectHierarchyService projectHierarchyService) {
		List<ObjectId> projectBasicConfigIds = projectBasicConfigList.stream().map(ProjectBasicConfig::getId)
				.collect(Collectors.toList());

		// required basicConfigId in final response list on project level but
		// OrganizationHierarchy do not have basicConfigId.
		Map<String, ObjectId> projectNodeWiseBasicConfigIdMap = projectBasicConfigList.stream()
				.collect(Collectors.toMap(ProjectBasicConfig::getProjectNodeId, ProjectBasicConfig::getId));

		// required only configured Project Nodes and Above their Hierarchy Nodes
		// filters from OrganizationHierarchy Collections
		Set<String> hierarchyNodes = projectBasicConfigList.stream().flatMap(a -> a.getHierarchy().stream())
				.map(hierarchyValue -> hierarchyValue.getOrgHierarchyNodeId()).collect(Collectors.toSet());

		hierarchyNodes
				.addAll(projectBasicConfigList.stream().map(ProjectBasicConfig::getProjectNodeId).collect(Collectors.toSet()));

		List<OrganizationHierarchy> configureOrganizationHierarchyList = organizationHierarchyService.findAll().stream()
				.filter(organizationHierarchy -> hierarchyNodes.contains(organizationHierarchy.getNodeId())).toList();

		// required only configured Project Below filters like sprint , squad , releases
		List<ProjectHierarchy> configureHierarchies = projectHierarchyService
				.findAllByBasicProjectConfigIds(projectBasicConfigIds);
		projectHierarchyService.appendProjectName(projectBasicConfigList, configureHierarchies);
		// configureOrganizationHierarchyList and projectHierarchyList merge into single
		// list
		configureOrganizationHierarchyList.stream().map(orgHierarchy -> {
			if (orgHierarchy.getHierarchyLevelId().equalsIgnoreCase(CommonConstant.PROJECT)) {
				ObjectId projectBasicId = projectNodeWiseBasicConfigIdMap.get(orgHierarchy.getNodeId());
				return new ProjectHierarchy(orgHierarchy.getNodeId(), orgHierarchy.getNodeName(),
						orgHierarchy.getNodeDisplayName(), orgHierarchy.getHierarchyLevelId(), orgHierarchy.getParentId(),
						orgHierarchy.getCreatedDate(), orgHierarchy.getModifiedDate(), projectBasicId);
			} else {
				return new ProjectHierarchy(orgHierarchy.getNodeId(), orgHierarchy.getNodeName(),
						orgHierarchy.getNodeDisplayName(), orgHierarchy.getHierarchyLevelId(), orgHierarchy.getParentId(),
						orgHierarchy.getCreatedDate(), orgHierarchy.getModifiedDate(), null);
			}
		}).forEach(configureHierarchies::add);
		return configureHierarchies;
	}
}
