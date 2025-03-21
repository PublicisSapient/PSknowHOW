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

/** */
package com.publicissapient.kpidashboard.apis.userboardconfig.rest;

import java.util.ArrayList;
import java.util.Optional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.common.service.ConfigDetailService;
import com.publicissapient.kpidashboard.apis.model.ConfigDetails;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.model.UserBoardDTO;
import com.publicissapient.kpidashboard.apis.userboardconfig.service.UserBoardConfigService;
import com.publicissapient.kpidashboard.common.model.userboardconfig.ConfigLevel;
import com.publicissapient.kpidashboard.common.model.userboardconfig.ProjectListRequested;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfigDTO;

/**
 * Rest controller for user board config
 *
 * @author narsingh9
 */
@RestController
@RequestMapping("/user-board-config")
public class UserBoardConfigController {

	private static final String NO_DATA_FOUND = "No data found";
	@Autowired
	UserBoardConfigService userBoardConfigService;
	@Autowired
	private ConfigDetailService configDetailService;

	/**
	 * Api to get user based configurations
	 *
	 * @return response
	 */
	@PostMapping(value = "/getBoardConfig", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getUserBoardConfigurations(
			@Valid @RequestBody ProjectListRequested listOfRequestedProj) {
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigService.getBoardConfig(ConfigLevel.USER,
				listOfRequestedProj);
		if (userBoardConfigDTO == null) {
			return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(false, NO_DATA_FOUND, null));
		}

		ConfigDetails configDetails = configDetailService.getConfigDetails();

		// Create a UserBoardDTO to hold the combined data
		UserBoardDTO userBoardDTO = new UserBoardDTO();
		userBoardDTO.setUserBoardConfigDTO(userBoardConfigDTO);
		userBoardDTO.setConfigDetails(configDetails);

		ServiceResponse response = new ServiceResponse(true, "Project Config Fetched successfully", userBoardDTO);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * Api to save user based config
	 *
	 * @param userBoardConfigDTO
	 *          userBoardConfigDTO
	 * @return response
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> saveUserBoardConfig(
			@Valid @RequestBody UserBoardConfigDTO userBoardConfigDTO) {
		ServiceResponse response = userBoardConfigService.saveBoardConfig(userBoardConfigDTO, ConfigLevel.USER, null);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * Api to get project board config
	 *
	 * @return response
	 */
	@GetMapping(value = "/{basicProjectConfigId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getUserBoardConfigurationAdmin(@PathVariable String basicProjectConfigId) {
		ProjectListRequested projectListRequested = new ProjectListRequested();
		Optional.ofNullable(projectListRequested.getBasicProjectConfigIds()).orElseGet(() -> {
			projectListRequested.setBasicProjectConfigIds(new ArrayList<>());
			return projectListRequested.getBasicProjectConfigIds();
		}).add(basicProjectConfigId);
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigService.getBoardConfig(ConfigLevel.PROJECT,
				projectListRequested);
		ServiceResponse response = new ServiceResponse(false, NO_DATA_FOUND, null);
		if (null != userBoardConfigDTO) {
			response = new ServiceResponse(true, "Project Config Fetched successfully", userBoardConfigDTO);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * Api to save project board config
	 *
	 * @param userBoardConfigDTO
	 *          userBoardConfigDTO
	 * @return response
	 */
	@PostMapping(value = "/saveAdmin/{basicProjectConfigId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> saveUserBoardConfigAdmin(
			@Valid @RequestBody UserBoardConfigDTO userBoardConfigDTO, @PathVariable String basicProjectConfigId) {
		ServiceResponse response = userBoardConfigService.saveBoardConfig(userBoardConfigDTO, ConfigLevel.PROJECT,
				basicProjectConfigId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
