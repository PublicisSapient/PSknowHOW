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

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;

public interface ProjectHierarchyService {

	Map<String, ProjectHierarchy> getProjectHierarchyMapByConfigId(String projectConfigId);

	Map<String, ProjectHierarchy> getProjectHierarchyMapByConfigIdAndHierarchyLevelId(String projectConfigId,
			String hierarchyLevelId);

	void saveAll(Set<ProjectHierarchy> projectHierarchies);

	List<ProjectHierarchy> findAllByBasicProjectConfigIds(List<ObjectId> basicProjectConfigIdList);

	List<ProjectHierarchy> findAll();
}
