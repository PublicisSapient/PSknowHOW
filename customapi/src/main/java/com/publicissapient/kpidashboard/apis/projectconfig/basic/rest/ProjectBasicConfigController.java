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

package com.publicissapient.kpidashboard.apis.projectconfig.basic.rest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.abac.ContextAwarePolicyEnforcement;
import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ProjectConfigResponse;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.model.HierarchyResponseDTO;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.dto.ProjectBasicConfigDTO;
import com.publicissapient.kpidashboard.common.model.rbac.RoleWiseProjects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for CRUD operation for project basic details.
 * 
 * @author narsingh9
 *
 */
@RestController
@RequestMapping("/basicconfigs")
@Slf4j
public class ProjectBasicConfigController {

	public static final String ADDING_PROJECT_CONFIGURATIONS = "Adding project configurations: {}";
	public static final String UPDATING_PROJECT_CONFIGURATIONS = "Updating project configurations: {}";
	private static final String AUTH_RESPONSE_HEADER = "X-Authentication-Token";
	public static final String FETCHED_SUCCESSFULLY = "Fetched successfully";
	@Autowired
	HttpServletRequest contextreq;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	private ProjectBasicConfigService projectBasicConfigService;
	@Autowired
	private ContextAwarePolicyEnforcement policy;
	@Autowired
	private ProjectAccessManager projectAccessManager;

	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
	private CustomApiConfig customApiConfig;

	/**
	 * 
	 * Returns the list of project's basic configuration.
	 * 
	 * @param basicProjectConfigId
	 *            basic project config id
	 * @return ResponseEntity
	 */
	@GetMapping(value = { "/{id}" })
	public ResponseEntity<ServiceResponse> getProjectBasicConfig(@PathVariable("id") String basicProjectConfigId) {
		basicProjectConfigId = CommonUtils.handleCrossScriptingTaintedValue(basicProjectConfigId);
		log.info("List project configuration request recieved for : {}", basicProjectConfigId);
		boolean isSuccess = true;
		String message = FETCHED_SUCCESSFULLY;
		Object returnObj = null;

		if (Optional.ofNullable(basicProjectConfigId).isPresent()) {
			ProjectBasicConfig projectConfig = projectBasicConfigService.getProjectBasicConfigs(basicProjectConfigId);
			if (Optional.ofNullable(projectConfig).isPresent()) {
				returnObj = projectConfig;
			} else {
				isSuccess = false;
				message = "No record found for the projectId:" + basicProjectConfigId + " mentioned!";
			}

		} else {
			returnObj = projectBasicConfigService.getFilteredProjectsBasicConfigs(Boolean.TRUE);

		}
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(isSuccess, message, returnObj));
	}

	/**
	 * 
	 * Returns the list of project's basic configuration.
	 * 
	 * @return ResponseEntity
	 */
	@GetMapping
	public ResponseEntity<ServiceResponse> getProjectBasicConfig(@RequestParam(value = "includeAll", defaultValue = "true") Boolean includeAll) {
		try {
			// Call the service layer
			Object result = projectBasicConfigService.getFilteredProjectsBasicConfigs(includeAll);

			// Return a successful response
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ServiceResponse(true, FETCHED_SUCCESSFULLY, result));

		} catch (Exception ex) {
			// Handle unexpected exceptions
			String message = "An error occurred while fetching project configurations.";
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ServiceResponse(false, message, null));
		}
	}

	/**
	 * @param projectBasicConfigDTO
	 * @param response
	 * @return ResponseEntity
	 */
	@PostMapping
	public ResponseEntity<ProjectConfigResponse> addBasicConfig(
			@RequestBody ProjectBasicConfigDTO projectBasicConfigDTO, HttpServletResponse response) {

		policy.checkPermission(projectBasicConfigDTO, "ADD_PROJECT");

		log.info(ADDING_PROJECT_CONFIGURATIONS, projectBasicConfigDTO.toString());

		ServiceResponse serviceResp = projectBasicConfigService.addBasicConfig(projectBasicConfigDTO);

		List<RoleWiseProjects> projectAccess = projectAccessManager
				.getProjectAccessesWithRole(authenticationService.getLoggedInUser());
		ProjectConfigResponse projectConfigResponse = new ProjectConfigResponse(
				response.getHeader(AUTH_RESPONSE_HEADER), serviceResp, projectAccess);
		return ResponseEntity.status(HttpStatus.OK).body(projectConfigResponse);
	}

	/**
	 * @param basicConfigId
	 * @param projectBasicConfigDTO
	 * @param response
	 * @return ResponseEntity
	 */
	@PutMapping(value = "/{id}")
	public ResponseEntity<ProjectConfigResponse> updateBasicConfig(@PathVariable("id") String basicConfigId,
			@RequestBody ProjectBasicConfigDTO projectBasicConfigDTO, HttpServletResponse response) {

		basicConfigId = CommonUtils.handleCrossScriptingTaintedValue(basicConfigId);
		policy.checkPermission(projectBasicConfigDTO, "UPDATE_PROJECT");

		log.info(UPDATING_PROJECT_CONFIGURATIONS, projectBasicConfigDTO.toString());

		ServiceResponse serviceResp = projectBasicConfigService.updateBasicConfig(basicConfigId, projectBasicConfigDTO);

		ProjectConfigResponse projectConfigResponse = new ProjectConfigResponse(
				response.getHeader(AUTH_RESPONSE_HEADER), serviceResp, Lists.newArrayList());
		return ResponseEntity.status(HttpStatus.OK).body(projectConfigResponse);
	}

	/**
	 * 
	 * Gets All ProjectsList
	 * 
	 * @return list of project list
	 */
	@GetMapping(value = "/all")
	public ServiceResponse getAllProjectsList() {

		List<ProjectBasicConfigDTO> configsList = projectBasicConfigService
				.getAllProjectsBasicConfigsDTOWithoutPermission();

		ServiceResponse response = new ServiceResponse(false, "No record found", null);
		if (CollectionUtils.isNotEmpty(configsList)) {
			response = new ServiceResponse(true, FETCHED_SUCCESSFULLY, configsList);
		}
		return response;
	}

	/**
	 * Delete project
	 * 
	 * @param basicProjectConfigId
	 *            id
	 * @return ServiceResponse
	 */
	@PreAuthorize("hasPermission(#basicProjectConfigId, 'DELETE_PROJECT')")
	@DeleteMapping(value = "/{basicProjectConfigId}")
	public ResponseEntity<ServiceResponse> deleteProject(@PathVariable String basicProjectConfigId) {
		ProjectBasicConfig projectBasicConfig = projectBasicConfigService.deleteProject(basicProjectConfigId);
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(true,
				projectBasicConfig.getProjectDisplayName() + " deleted successfully", projectBasicConfig));
	}

	/**
	 *
	 * Gets All Scrum ProjectsList with hierarchy details this method is only use
	 * for specific purpose for Expose API
	 *
	 * @return list of Scrum project list with hierarchy details at least one
	 *         connected tool
	 */
	@GetMapping(value = "/hierarchyResponses")
	public ResponseEntity<List<HierarchyResponseDTO>> getAllHierarchyResponse(HttpServletRequest request) {
		String apiKey = customApiConfig.getxApiKey();
		boolean isApiAuth = StringUtils.isNotEmpty(apiKey)
				&& apiKey.equalsIgnoreCase(request.getHeader(Constant.TOKEN_KEY));
		if (isApiAuth) {
			List<HierarchyResponseDTO> hierarchyData = projectBasicConfigService.getHierarchyData();
			List<HierarchyResponseDTO> filteredHierarchyData = projectBasicConfigService
					.filterHierarchyDTOsWithConnectedTools(hierarchyData);
			return ResponseEntity.status(HttpStatus.OK).body(filteredHierarchyData);
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
		}
	}
}
