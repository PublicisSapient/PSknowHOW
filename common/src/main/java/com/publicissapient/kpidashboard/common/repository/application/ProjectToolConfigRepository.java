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

import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;

/**
 * @author anisingh4
 */
@Repository
public interface ProjectToolConfigRepository extends MongoRepository<ProjectToolConfig, ObjectId> {

	/**
	 * Find by toolType.
	 *
	 * @param tool
	 *            the tool
	 * 
	 * @return the list of ProjectToolConfig
	 */
	List<ProjectToolConfig> findByToolName(String tool);

	/**
	 * Finds by toolId.
	 *
	 * @param toolId
	 *            the toolId
	 * @return the ProjectToolConfig
	 */
	ProjectToolConfig findById(String toolId);

	/**
	 * Finds by connectionId.
	 *
	 * @param connectionId
	 *            the connectionId
	 * @return the ProjectToolConfig
	 */
	List<ProjectToolConfig> findByConnectionId(ObjectId connectionId);

	/**
	 * Find by toolType.
	 *
	 * @param tool
	 *            the tool
	 * @param basicProjectConfigId
	 *            the project basic config id
	 * 
	 * @return the list of ProjectToolConfig
	 */
	List<ProjectToolConfig> findByToolNameAndBasicProjectConfigId(String tool, ObjectId basicProjectConfigId);

	/**
	 * Find tools of the project
	 * 
	 * @param basicProjectConfigId
	 * @return list of tools
	 */
	List<ProjectToolConfig> findByBasicProjectConfigId(ObjectId basicProjectConfigId);

	List<ProjectToolConfig> findByToolNameAndQueryEnabledAndBasicProjectConfigIdIn(String toolName,
			boolean queryEnabled, List<ObjectId> projectConfigsIds);
}
