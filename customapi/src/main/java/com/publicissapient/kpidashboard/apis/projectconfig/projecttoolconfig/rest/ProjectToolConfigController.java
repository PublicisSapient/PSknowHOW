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

package com.publicissapient.kpidashboard.apis.projectconfig.projecttoolconfig.rest;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.projectconfig.projecttoolconfig.service.ProjectToolConfigService;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfigDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yasbano
 * @author dilipkr
 *
 */
@RestController
@Slf4j
public class ProjectToolConfigController {

	@Autowired
	private ProjectToolConfigService toolService;

	/**
	 * Fetch all projectToolConfig
	 */
	@RequestMapping(value = "/basicconfigs/{basicConfigId}/tools", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> getProjectTools(@PathVariable String basicConfigId,
			@RequestParam(name = "toolType", required = false) String toolType) {
		ServiceResponse response;
		if (StringUtils.isEmpty(StringUtils.trim(toolType))) {
			log.info("Fetching all tools");
			response = new ServiceResponse(true, "list of tools", toolService.getProjectToolConfigs(basicConfigId));
		} else {
			log.info("Fetching toolType ", toolType);
			response = new ServiceResponse(true, "list of tools",
					toolService.getProjectToolConfigs(basicConfigId, toolType));
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * save/add ProjectToolConfig details *
	 * 
	 * @param projectToolDTO
	 *            request object that is created in the database.
	 * 
	 * 
	 * @return responseEntity with data,message and status
	 */

	@PreAuthorize("hasPermission(#basicProjectConfigId, 'SAVE_PROJECT_TOOL')")
	@RequestMapping(value = "/basicconfigs/{basicProjectConfigId}/tools", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> saveToolDetails(@PathVariable String basicProjectConfigId,
			@Valid @RequestBody ProjectToolConfigDTO projectToolDTO) {
		final ModelMapper modelMapper = new ModelMapper();
		final ProjectToolConfig projectToolConfig = modelMapper.map(projectToolDTO, ProjectToolConfig.class);
		projectToolConfig.setId(null);
		projectToolConfig.setBasicProjectConfigId(new ObjectId(basicProjectConfigId));
		log.info("created and saved new projectToolConfigDTO");
		return ResponseEntity.status(HttpStatus.OK).body(toolService.saveProjectToolDetails(projectToolConfig));
	}

	/**
	 * Modify/Update a projectToolDTO by projectToolId. *
	 * 
	 * @param projectToolDTO
	 *            request object that replaces the project_tool_configs data present
	 *            at object_id projectToolId.
	 *
	 * @return responseEntity with data,message and status
	 */
	@PreAuthorize("hasPermission(#basicProjectConfigId, 'UPDATE_PROJECT_TOOL')")
	@RequestMapping(value = "/basicconfigs/{basicProjectConfigId}/tools/{projectToolId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> modifyConnectionById(@PathVariable String basicProjectConfigId,
			@PathVariable String projectToolId, @Valid @RequestBody ProjectToolConfigDTO projectToolDTO) {
		log.info("projectTool updated", projectToolDTO.getProjectId());
		final ModelMapper modelMapper = new ModelMapper();
		final ProjectToolConfig projectToolConfig = modelMapper.map(projectToolDTO, ProjectToolConfig.class);
		projectToolConfig.setBasicProjectConfigId(new ObjectId(basicProjectConfigId));
		projectToolConfig.setId(new ObjectId(projectToolId));
		return ResponseEntity.status(HttpStatus.OK)
				.body(toolService.modifyProjectToolById(projectToolConfig, projectToolId));
	}

	@PreAuthorize("hasPermission(#basicProjectConfigId, 'DELETE_PROJECT_TOOL')")
	@RequestMapping(value = "/basicconfigs/{basicProjectConfigId}/tools/{projectToolId}", method = RequestMethod.DELETE)
	public ResponseEntity<ServiceResponse> deleteTool(@PathVariable String basicProjectConfigId,
			@PathVariable String projectToolId) {

		boolean isDeleted = toolService.deleteTool(basicProjectConfigId, projectToolId);
		ServiceResponse serviceResponse = null;
		if (isDeleted) {
			serviceResponse = new ServiceResponse(true, "Tool deleted successfully", null);
		} else {
			serviceResponse = new ServiceResponse(false, "Failed to delete tool", null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
	}

	@PreAuthorize("hasPermission(#basicProjectConfigId, 'CLEAN_PROJECT_TOOL_DATA')")
	@DeleteMapping(value = "/basicconfigs/{basicProjectConfigId}/tools/clean/{projectToolId}")
	public ResponseEntity<ServiceResponse> cleanToolData(@PathVariable String basicProjectConfigId,
			@PathVariable String projectToolId) {

		boolean isDeleted = toolService.cleanToolData(basicProjectConfigId, projectToolId);
		ServiceResponse serviceResponse = null;
		if (isDeleted) {
			serviceResponse = new ServiceResponse(true, "Tool Data deleted successfully", null);
		} else {
			serviceResponse = new ServiceResponse(false, "Failed to delete tool data", null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
	}
}
