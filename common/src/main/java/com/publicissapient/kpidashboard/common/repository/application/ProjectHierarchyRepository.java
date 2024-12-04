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

package com.publicissapient.kpidashboard.common.repository.application;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;

@Repository
public interface ProjectHierarchyRepository extends MongoRepository<ProjectHierarchy, ObjectId> {

	List<ProjectHierarchy> findByBasicProjectConfigId(ObjectId projectBasicConfig);

	List<ProjectHierarchy> findByBasicProjectConfigIdIn(List<ObjectId> basicProjectConfigIdList);

	@Query(value = "{ 'basicProjectConfigId': ?0, 'nodeId': { $nin: ?1 }, 'hierarchyLevelId': ?2 }", fields = "{ 'nodeId': 1 }")
	List<ProjectHierarchy> findNodeIdsByBasicProjectConfigIdAndNodeIdNotIn(ObjectId basicProjectConfigId,
			List<String> distinctSprintIDs, String hierarchyLevelId);

	void deleteByBasicProjectConfigIdAndNodeIdIn(ObjectId basicProjectConfigId, List<String> nodeIdsToBeDeleted,
			String hierarchyLevelId);
}
