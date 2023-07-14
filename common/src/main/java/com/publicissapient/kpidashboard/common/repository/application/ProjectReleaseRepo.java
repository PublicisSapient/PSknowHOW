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

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;

/**
 * The interface Project release repo.
 */
@Repository
public interface ProjectReleaseRepo extends MongoRepository<ProjectRelease, ObjectId> {

	/**
	 * Find by config id project release.
	 *
	 * @param configId
	 *            the config id
	 * @return the project release
	 */
	ProjectRelease findByConfigId(ObjectId configId);

	/**
	 * Deletes all documents that matches with given basicProjectConfigId.
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 */
	void deleteByConfigId(ObjectId basicProjectConfigId);

	/**
	 * Find by config ids project release.
	 *
	 * @param configIds
	 *            the config ids
	 * @return the project releases
	 */
	List<ProjectRelease> findByConfigIdIn(List<ObjectId> configIds);

}