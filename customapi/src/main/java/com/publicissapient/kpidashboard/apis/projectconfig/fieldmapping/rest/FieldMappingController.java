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

import static com.publicissapient.kpidashboard.apis.projectconfig.fieldmapping.service.FieldMappingServiceImpl.INVALID_PROJECT_TOOL_CONFIG_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
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
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.projectconfig.fieldmapping.service.FieldMappingService;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.FieldMappingMeta;
import com.publicissapient.kpidashboard.common.model.application.FieldMappingResponse;
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

	/*
	 * save import functionality
	 */
	@RequestMapping(value = "/tools/{projectToolConfigId}/fieldMapping", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> addFieldMapping(@PathVariable String projectToolConfigId,
			@RequestBody FieldMappingMeta fieldMappingMeta) {

		projectToolConfigId = CommonUtils.handleCrossScriptingTaintedValue(projectToolConfigId);

		Optional<ProjectToolConfig> projectToolConfigOptional = getProjectToolConfig(projectToolConfigId);

		if (projectToolConfigOptional.isPresent()) {
			// checking the permission to update the fieldmapping
			ProjectToolConfig projectToolConfig = projectToolConfigOptional.get();
			ProjectBasicConfig projectBasicConfig = fieldMappingService
					.getBasicProjectConfigById(projectToolConfig.getBasicProjectConfigId());
			policy.checkPermission(projectBasicConfig, "UPDATE_PROJECT");

			ServiceResponse response;
			try {
				FieldMapping fieldMapping = new FieldMapping();
				boolean allfieldFound = fieldMappingService
						.convertToFieldMappingAndCheckIsFieldPresent(fieldMappingMeta.getFieldMappingRequests(), fieldMapping);
				fieldMappingService.addFieldMapping(projectToolConfigId, fieldMapping,
						projectToolConfig.getBasicProjectConfigId());
				if (!allfieldFound) {
					response = new ServiceResponse(true, "field mappings added successfully", null);
				} else {
					response = new ServiceResponse(false,
							"field mappings added successfully but some fields are missing, please verify your imported fields",
							null);
				}
			} catch (Exception ex) {
				response = new ServiceResponse(false, "failed to add field mappings", null);
			}

			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(false, "No Tool Configuration Found", ""));
	}

	/*
	 * export fieldmapping
	 */
	@RequestMapping(value = "/tools/{projectToolConfigId}/fieldMapping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> getFieldMapping(@PathVariable String projectToolConfigId) {

		FieldMappingDTO result = null;
		projectToolConfigId = CommonUtils.handleCrossScriptingTaintedValue(projectToolConfigId);
		FieldMapping resultFieldMapping = fieldMappingService.getFieldMapping(projectToolConfigId);
		if (null != resultFieldMapping && null != resultFieldMapping.getId()) {
			log.info("getFieldMapping resultFieldMapping : {}", resultFieldMapping);
			result = new ModelMapper().map(resultFieldMapping, FieldMappingDTO.class);
		}
		log.info("getFieldMapping result : {}", result);
		ServiceResponse response;
		if (result == null) {
			response = new ServiceResponse(false, "no field mapping found for " + projectToolConfigId, null);
		} else {
			response = new ServiceResponse(true, "field mappings", result);
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/*
	 * save kpiwise fieldmapping
	 */
	@RequestMapping(value = "/tools/fieldMapping/{projectToolConfigId}/{kpiId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> getFieldMapping(@PathVariable String projectToolConfigId,
			@PathVariable String kpiId, @RequestBody FieldMappingMeta requestData) {
		projectToolConfigId = CommonUtils.handleCrossScriptingTaintedValue(projectToolConfigId);
		Optional<ProjectToolConfig> projectToolConfigOptional = getProjectToolConfig(projectToolConfigId);

		ServiceResponse response = null;
		if (projectToolConfigOptional.isPresent()) {
			ProjectToolConfig projectToolConfig = projectToolConfigOptional.get();
			KPICode kpi = KPICode.getKPI(kpiId);
			List<FieldMappingResponse> kpiSpecificFieldsAndHistory = new ArrayList<>();
			if (!Objects.equals(kpi.getKpiId(), KPICode.INVALID.getKpiId())) {
				try {
					kpiSpecificFieldsAndHistory = fieldMappingService.getKpiSpecificFieldsAndHistory(kpi, projectToolConfig,
							requestData);
				} catch (NoSuchFieldException | IllegalAccessException e) {
					log.error("Field/ Class not found in FieldMapping collection");
				}
			}
			log.info("getFieldMapping result : {}", kpiSpecificFieldsAndHistory);

			if (CollectionUtils.isEmpty(kpiSpecificFieldsAndHistory)) {
				response = new ServiceResponse(false, "no field mapping found for " + projectToolConfigId, null);
			} else if (checkTool(projectToolConfig)) {
				FieldMappingMeta fieldMappingMeta = new FieldMappingMeta(kpiSpecificFieldsAndHistory,
						projectToolConfig.getMetadataTemplateCode());
				response = new ServiceResponse(true, "field mappings", fieldMappingMeta);
			}
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/*
	 * get kpiwise fieldmapping
	 */
	@RequestMapping(value = "/tools/saveMapping/{projectToolConfigId}/{kpiId}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> saveKpiWiseSpecificFieldmAPPING(@PathVariable String projectToolConfigId,
			@PathVariable String kpiId, @RequestBody FieldMappingMeta fieldMappingMeta)
			throws NoSuchFieldException, IllegalAccessException {

		projectToolConfigId = CommonUtils.handleCrossScriptingTaintedValue(projectToolConfigId);

		Optional<ProjectToolConfig> projectToolConfigOptional = getProjectToolConfig(projectToolConfigId);

		if (projectToolConfigOptional.isPresent()) {
			// checking the permission to update the fieldmapping

			ProjectToolConfig projectToolConfig = projectToolConfigOptional.get();
			ProjectBasicConfig projectBasicConfig = fieldMappingService
					.getBasicProjectConfigById(projectToolConfig.getBasicProjectConfigId());
			policy.checkPermission(projectBasicConfig, "UPDATE_PROJECT");

			// validating kpicode
			KPICode kpi = KPICode.getKPI(kpiId);
			if (!Objects.equals(kpi.getKpiId(), KPICode.INVALID.getKpiId())) {
				fieldMappingService.updateSpecificFieldsAndHistory(kpi, projectToolConfig, fieldMappingMeta);
				ServiceResponse response;
				if (checkTool(projectToolConfig) && checkCustomTemplateCode(projectToolConfig)) {
					response = new ServiceResponse(true, "changes are made in customize mappings", false);
				} else {
					response = new ServiceResponse(true, "mappings are not same as already maintained mapping", true);
				}
				return ResponseEntity.status(HttpStatus.OK).body(response);
			}
		}
		if (!ObjectId.isValid(projectToolConfigId)) {
			throw new IllegalArgumentException(INVALID_PROJECT_TOOL_CONFIG_ID);
		}
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(true, "", ""));
	}

	private boolean checkTool(ProjectToolConfig projectToolConfig) {
		return (projectToolConfig.getToolName().equalsIgnoreCase(ProcessorConstants.JIRA) ||
				projectToolConfig.getToolName().equalsIgnoreCase(ProcessorConstants.AZURE));
	}

	private boolean checkCustomTemplateCode(ProjectToolConfig projectToolConfig) {
		return projectToolConfig.getMetadataTemplateCode().equalsIgnoreCase(CommonConstant.CUSTOM_TEMPLATE_CODE_SCRUM) ||
				projectToolConfig.getMetadataTemplateCode().equalsIgnoreCase(CommonConstant.CUSTOM_TEMPLATE_CODE_KANBAN);
	}

	private Optional<ProjectToolConfig> getProjectToolConfig(String projectToolConfigId) {
		List<ProjectToolConfig> projectToolConfigs = (List<ProjectToolConfig>) configHelperService
				.loadAllProjectToolConfig();
		String finalProjectToolConfigId = projectToolConfigId;
		return projectToolConfigs.stream().filter(t -> t.getId().toString().equals(finalProjectToolConfigId)).findFirst();
	}
}
