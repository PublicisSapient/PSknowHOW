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

package com.publicissapient.kpidashboard.apis.projectconfig.fieldmapping.service;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

/**
 * @author anisingh4
 */
public interface FieldMappingService {

	FieldMapping getFieldMapping(String projectToolConfigId);

	FieldMapping addFieldMapping(String projectToolConfigId, FieldMapping fieldMapping);

	boolean compareMappingOnSave(String projectToolConfigId, FieldMapping fieldMapping);

	/**
	 * Gets ProjectBasicConfig object by its id.
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return ProjectBasicConfig
	 */
	ProjectBasicConfig getBasicProjectConfigById(ObjectId basicProjectConfigId);

	/**
	 * Checks if user has project access.
	 * 
	 * @param projectToolConfigId
	 * @return
	 */
	boolean hasProjectAccess(String projectToolConfigId);

	/**
	 * Delete by basic project config id
	 * 
	 * @param basicProjectConfigId
	 *            id
	 */
	void deleteByBasicProjectConfigId(ObjectId basicProjectConfigId);

}
