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
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
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
	public Map<String, ProjectHierarchy> getProjectHierarchyMapByConfigId(String projectConfigId) {
		return getProjectRelatedHierachy(projectConfigId).stream().collect(
				Collectors.toMap(OrganizationHierarchy::getNodeId, p -> p, (existingValue, newValue) -> existingValue));

	}

	@Override
	public Map<String, ProjectHierarchy> getProjectHierarchyMapByConfigIdAndHierarchyLevelId(String projectConfigId,
			String hierarchyLevelId) {
		return getProjectRelatedHierachy(projectConfigId).stream()
				.filter(hierarchy -> hierarchy.getHierarchyLevelId().equalsIgnoreCase(hierarchyLevelId))
				.collect(Collectors.toMap(OrganizationHierarchy::getNodeId, p -> p,
						(existingValue, newValue) -> existingValue));

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
}
