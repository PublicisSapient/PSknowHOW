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

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;

/**
 * The interface Field mapping repository.
 *
 * @author prigupta8
 */
@Repository
public interface FieldMappingRepository extends MongoRepository<FieldMapping, ObjectId> {

	@Override
	FieldMapping save(FieldMapping fieldMapping);

	/**
	 * Find by project config id field mapping.
	 *
	 * @param projectConfigId
	 *            the project config id
	 * @return the field mapping
	 */
	FieldMapping findByBasicProjectConfigId(ObjectId projectConfigId);

	/**
	 * Find by project id field mapping.
	 *
	 * @param projectId
	 *            the project id
	 * @return the field mapping
	 */
	FieldMapping findByProjectId(String projectId);

	/**
	 * Delete by project config id.
	 *
	 * @param basicProjectConfigId
	 *            the project config id
	 */
	void deleteByBasicProjectConfigId(ObjectId basicProjectConfigId);

	/**
	 * Find by projectToolConfigId
	 * 
	 * @param projectToolConfigId
	 * @return field mapping of the tool
	 */
	FieldMapping findByProjectToolConfigId(ObjectId projectToolConfigId);

	/**
	 * Find by id
	 * 
	 * @param id
	 *            field mapping id
	 * @return field mapping of the tool
	 */
	Optional<FieldMapping> findById(ObjectId id);
}
