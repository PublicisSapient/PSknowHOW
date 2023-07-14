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

package com.publicissapient.kpidashboard.common.repository.jira;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;

/**
 * Repository for BoardMetadata.
 */
@Repository
public interface BoardMetadataRepository extends CrudRepository<BoardMetadata, ObjectId> {

	/*
	 * This method return metadata based on projectBasicconfigId
	 * 
	 * @param projectBasicConfigId
	 * 
	 * @return BoardMetadata
	 */
	BoardMetadata findByProjectBasicConfigId(ObjectId projectBasicConfigId);

	/*
	 * This method return metadata based on projectBasicconfigId
	 * 
	 * @param projectToolConfigId
	 * 
	 * @return BoardMetadata
	 */
	BoardMetadata findByProjectToolConfigId(ObjectId projectToolConfigId);

	/**
	 * Deletes all documents that matches with given basicProjectConfigId.
	 *
	 * @param projectBasicConfigId
	 *            projectBasicConfigId
	 */
	void deleteByProjectBasicConfigId(ObjectId projectBasicConfigId);

}