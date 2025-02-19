/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.publicissapient.kpidashboard.common.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.repository.application.ProjectHierarchyRepository;

@Service
public class ProjectHierarchyServiceImpl implements ProjectHierarchyService {

	@Autowired
	ProjectHierarchyRepository projectHierarchyRepository;

	@Override
	public List<ProjectHierarchy> findAll() {
		return projectHierarchyRepository.findAll();
	}

	@Override
	public void deleteByBasicProjectConfigId(ObjectId projectBasicConfigId) {
		projectHierarchyRepository.deleteByBasicProjectConfigId(projectBasicConfigId);
	}

	@Override
	public Map<String, ProjectHierarchy> getProjectHierarchyMapByConfigId(String projectConfigId) {
		return getProjectRelatedHierachy(projectConfigId).stream().collect(
				Collectors.toMap(OrganizationHierarchy::getNodeId, p -> p, (existingValue, newValue) -> existingValue));
	}

	@Override
	public Map<String, List<ProjectHierarchy>> getProjectHierarchyMapByConfig(String projectConfigId) {
		return getProjectRelatedHierachy(projectConfigId).stream()
				.collect(Collectors.groupingBy(OrganizationHierarchy::getNodeId));
	}

	@Override
	public Map<String, ProjectHierarchy> getProjectHierarchyMapByConfigIdAndHierarchyLevelId(String projectConfigId,
			String hierarchyLevelId) {
		return getProjectRelatedHierachy(projectConfigId).stream()
				.filter(hierarchy -> hierarchy.getHierarchyLevelId().equalsIgnoreCase(hierarchyLevelId)).collect(
						Collectors.toMap(OrganizationHierarchy::getNodeId, p -> p, (existingValue, newValue) -> existingValue));
	}

	@Override
	public void saveAll(Set<ProjectHierarchy> projectHierarchies) {
		projectHierarchyRepository.saveAll(projectHierarchies);
	}

	private List<ProjectHierarchy> getProjectRelatedHierachy(String projectConfigId) {
		return projectHierarchyRepository.findByBasicProjectConfigId(new ObjectId(projectConfigId));
	}

	@Override
	public List<ProjectHierarchy> findAllByBasicProjectConfigIds(List<ObjectId> basicProjectConfigIdList) {
		return projectHierarchyRepository.findByBasicProjectConfigIdIn(basicProjectConfigIdList);
	}

	@Override
	public void appendProjectName(List<ProjectBasicConfig> projectBasicConfigList,
			List<ProjectHierarchy> projectHierarchyList) {
		// Create maps for quick lookups
		Map<ObjectId, ProjectBasicConfig> projectBasicMap = projectBasicConfigList.stream()
				.collect(Collectors.toMap(ProjectBasicConfig::getId, Function.identity()));

		Set<String> hierarchyLevelIds = Set.of(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE,
				CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);

		// Iterate through project hierarchies and modify them based on project basic
		// config
		for (ProjectHierarchy hierarchy : projectHierarchyList) {
			ProjectBasicConfig projectBasicConfig = projectBasicMap.get(hierarchy.getBasicProjectConfigId());

			if (projectBasicConfig != null && hierarchyLevelIds.contains(hierarchy.getHierarchyLevelId())) {
				String projectName = projectBasicConfig.getProjectName();
				String projectDisplayName = projectBasicConfig.getProjectDisplayName();

				// Update the node name and display name
				hierarchy.setNodeName(updateNodeName(hierarchy.getNodeName(), projectName));
				// Update node display name
				hierarchy
						.setNodeDisplayName(updateNodeDisplayName(hierarchy.getNodeDisplayName(), projectName, projectDisplayName));
			}
		}
	}

	private String updateNodeName(String currentName, String projectName) {
		String newName = currentName + CommonConstant.ADDITIONAL_FILTER_VALUE_ID_SEPARATOR + projectName;
		int index = currentName.lastIndexOf(CommonConstant.ADDITIONAL_FILTER_VALUE_ID_SEPARATOR + projectName);
		return (index != -1)
				? currentName.substring(0, index) + CommonConstant.ADDITIONAL_FILTER_VALUE_ID_SEPARATOR + projectName
				: newName;
	}

	private String updateNodeDisplayName(String currentName, String projectName, String projectDisplayName) {
		String newName = CommonConstant.ADDITIONAL_FILTER_VALUE_ID_SEPARATOR + projectDisplayName;

		int indexBeforeRename = currentName.lastIndexOf(CommonConstant.ADDITIONAL_FILTER_VALUE_ID_SEPARATOR + projectName);
		int indexAfterRename = currentName
				.lastIndexOf(CommonConstant.ADDITIONAL_FILTER_VALUE_ID_SEPARATOR + projectDisplayName);

		if (indexBeforeRename != -1) {
			return currentName.substring(0, indexBeforeRename) + newName;
		}
		return (indexAfterRename == -1) ? currentName + newName : currentName.substring(0, indexAfterRename) + newName;
	}
}
