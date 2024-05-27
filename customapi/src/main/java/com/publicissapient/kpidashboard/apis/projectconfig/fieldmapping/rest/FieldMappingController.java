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

package com.publicissapient.kpidashboard.apis.projectconfig.fieldmapping.rest;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.abac.ContextAwarePolicyEnforcement;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.projectconfig.fieldmapping.service.FieldMappingService;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.dto.FieldMappingDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@RestController
@Slf4j
public class FieldMappingController {

	@Autowired
	private FieldMappingService fieldMappingService;

	@Autowired
	private ContextAwarePolicyEnforcement policy;

	@Autowired
	private ConfigHelperService configHelperService;

	@RequestMapping(value = "/tools/{projectToolConfigId}/fieldMapping", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> addFieldMapping(@PathVariable String projectToolConfigId,
			@RequestBody FieldMappingDTO fieldMappingDTO) {

		projectToolConfigId = CommonUtils.handleCrossScriptingTaintedValue(projectToolConfigId);

		ProjectBasicConfig projectBasicConfig = fieldMappingService
				.getBasicProjectConfigById(fieldMappingDTO.getBasicProjectConfigId());
		policy.checkPermission(projectBasicConfig, "UPDATE_PROJECT");

		final ModelMapper modelMapper = new ModelMapper();
		FieldMapping fieldMapping = modelMapper.map(fieldMappingDTO, FieldMapping.class);

		FieldMapping resultFieldMapping = fieldMappingService.addFieldMapping(projectToolConfigId, fieldMapping);

		FieldMappingDTO result = modelMapper.map(resultFieldMapping, FieldMappingDTO.class);

		ServiceResponse response = null;
		if (result == null) {
			response = new ServiceResponse(false, "failed to add field mappings", result);
		} else {
			response = new ServiceResponse(true, "field mappings added successfully", result);
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@RequestMapping(value = "/tools/{projectToolConfigId}/saveMapping", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> saveFieldMapping(@PathVariable String projectToolConfigId,
															@RequestBody FieldMappingDTO fieldMappingDTO) {

		projectToolConfigId = CommonUtils.handleCrossScriptingTaintedValue(projectToolConfigId);

		ProjectBasicConfig projectBasicConfig = fieldMappingService
				.getBasicProjectConfigById(fieldMappingDTO.getBasicProjectConfigId());
		policy.checkPermission(projectBasicConfig, "UPDATE_PROJECT");

		final ModelMapper modelMapper = new ModelMapper();
		FieldMapping fieldMapping = modelMapper.map(fieldMappingDTO, FieldMapping.class);

		List<ProjectToolConfig> projectToolConfigs = (List<ProjectToolConfig>) configHelperService
				.loadAllProjectToolConfig();

		String finalProjectToolConfigId = projectToolConfigId;
		Optional<ProjectToolConfig> projectToolConfigOptional = projectToolConfigs.stream()
				.filter(t -> t.getId().toString().equals(finalProjectToolConfigId))
				.findFirst();
		ProjectToolConfig projectToolConfig = projectToolConfigOptional.orElse(null);

		boolean result = fieldMappingService.compareMappingOnSave(projectToolConfigId, fieldMapping);

		ServiceResponse response;
		if (result && projectToolConfig != null
				&& projectToolConfig.getToolName().equalsIgnoreCase(ProcessorConstants.JIRA)
				&& (projectToolConfig.getMetadataTemplateCode()
						.equalsIgnoreCase(CommonConstant.CUSTOM_TEMPLATE_CODE_SCRUM)
						|| projectToolConfig.getMetadataTemplateCode()
								.equalsIgnoreCase(CommonConstant.CUSTOM_TEMPLATE_CODE_KANBAN))) {
			response = new ServiceResponse(true, "changes are made in customize mappings", false);
		} else if (result && projectToolConfig != null
				&& projectToolConfig.getToolName().equalsIgnoreCase(ProcessorConstants.AZURE)) {
			response = new ServiceResponse(true, "changes are made in customize mappings", false);
		} else {
			response = new ServiceResponse(true, "mappings are " + (result ? "not " : "") + "same as "
					+ (projectToolConfig != null ? "already maintained" : "default") + " mapping", result);
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}


	@RequestMapping(value = "/tools/{projectToolConfigId}/fieldMapping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> getFieldMapping(@PathVariable String projectToolConfigId) {

		FieldMappingDTO result = null;
		projectToolConfigId = CommonUtils.handleCrossScriptingTaintedValue(projectToolConfigId);
		FieldMapping resultFieldMapping = fieldMappingService.getFieldMapping(projectToolConfigId);
		if (null != resultFieldMapping && null != resultFieldMapping.getId()) {
			log.info("getFieldMapping resultFieldMapping : {}", resultFieldMapping.toString());
			result = new ModelMapper().map(resultFieldMapping, FieldMappingDTO.class);
		}
		log.info("getFieldMapping result : {}", result);
		ServiceResponse response = null;
		if (result == null) {
			response = new ServiceResponse(false, "no field mapping found for " + projectToolConfigId, null);
		} else {
			response = new ServiceResponse(true, "field mappings", result);
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
