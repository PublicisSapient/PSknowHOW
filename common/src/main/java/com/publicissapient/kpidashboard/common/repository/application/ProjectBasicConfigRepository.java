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

package com.publicissapient.kpidashboard.common.repository.application;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

/**
 * @author anisingh4
 */
@Repository
public interface ProjectBasicConfigRepository extends MongoRepository<ProjectBasicConfig, ObjectId> {

	/**
	 * Returns ProjectBasicConfig from persistence store by id
	 *
	 * @param id
	 *            id
	 * @return {@link ProjectBasicConfig} object if exist
	 */
	Optional<ProjectBasicConfig> findById(ObjectId id);

	/**
	 * Returns ProjectBasicConfig from persistence store by project name
	 *
	 * @param projectName
	 *            ProjectName
	 * @return {@link ProjectBasicConfig} object if exist
	 */
	ProjectBasicConfig findByProjectName(String projectName);

	/**
	 * Returns ProjectBasicConfig from persistence store by project name with
	 * different id than provided
	 *
	 * @param projectName
	 *            ProjectName
	 * @return {@link ProjectBasicConfig} object if exist
	 */
	ProjectBasicConfig findByProjectNameAndIdNot(String projectName, ObjectId id);

	/**
	 * Returns ProjectBasicConfig from persistence store by list of project Ids
	 *
	 * @param projectBasicConfigIds
	 * @return {@link ProjectBasicConfig} {@code List<ProjectBasicConfig>}
	 */
	List<ProjectBasicConfig> findByIdIn(Set<ObjectId> projectBasicConfigIds);

	@Query("{ 'hierarchy' : { $elemMatch: { 'hierarchyLevel.hierarchyLevelId' : ?0 }} , 'hierarchy.value' : { $in : ?1 } }")
	List<ProjectBasicConfig> findByHierarchyLevelIdAndValues(String accessLevel, List<String> hierarchyLevelValues);

    List<ProjectBasicConfig> findByKanban(boolean isKanban);
}
