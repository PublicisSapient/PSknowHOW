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

import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.common.model.application.FieldMappingResponse;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;

import java.util.List;

/**
 * @author anisingh4
 */
public interface FieldMappingService {

	FieldMapping getFieldMapping(String projectToolConfigId);

	FieldMapping addFieldMapping(String projectToolConfigId, FieldMapping fieldMapping, ObjectId basicProjectConfigId);

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

	/**
	 *
	 * @param kpi
	 *            valid KpiId
	 * @param projectToolConfigId
	 *            projectToolConfigId
	 * @return list of FieldMappingResponse
	 * @throws NoSuchFieldException
	 *             NoSuchFieldException
	 * @throws IllegalAccessException
	 *             IllegalAccessException
	 */
	List<FieldMappingResponse> getKpiSpecificFieldsAndHistory(KPICode kpi, String projectToolConfigId)
			throws NoSuchFieldException, IllegalAccessException;

	void updateSpecificFieldsAndHistory(KPICode kpi, ProjectToolConfig projectToolConfigId,
			List<FieldMappingResponse> fieldMappingResponseList) throws NoSuchFieldException, IllegalAccessException;

	/**
	 *
	 * @param fieldMappingResponseList
	 *            fieldMappingResponseList
	 * @param fieldMapping
	 *            fieldMapping
	 * @return boolean
	 * @throws IllegalAccessException
	 *             IllegalAccessException
	 */
	boolean convertToFieldMappingAndCheckIsFieldPresent(List<FieldMappingResponse> fieldMappingResponseList, FieldMapping fieldMapping) throws IllegalAccessException;
}
