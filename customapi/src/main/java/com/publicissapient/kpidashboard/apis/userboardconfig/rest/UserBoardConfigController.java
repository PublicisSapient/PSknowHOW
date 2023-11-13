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

/**
 * 
 */

package com.publicissapient.kpidashboard.apis.userboardconfig.rest;

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

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.userboardconfig.service.UserBoardConfigService;
import com.publicissapient.kpidashboard.common.model.userboardconfig.ProjectListRequested;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfigDTO;

/**
 * Rest controller for user board config
 * 
 * @author narsingh9
 *
 */
@RestController
@RequestMapping("/user-board-config")
public class UserBoardConfigController {

	@Autowired
	UserBoardConfigService userBoardConfigService;

	/**
	 * Api to get user based configurations
	 * 
	 * @return response
	 */
	@PostMapping(value = "/getConfig" ,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getUserBoardConfiguration(@Valid @RequestBody ProjectListRequested listOfRequestedProj) {
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigService.getUserBoardConfig(listOfRequestedProj);
		ServiceResponse response = new ServiceResponse(false, "No data found", null);
		if (null != userBoardConfigDTO) {
			response = new ServiceResponse(true, "Fetched successfully", userBoardConfigDTO);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * Api to save user based config
	 * 
	 * @param userBoardConfigDTO
	 *            userBoardConfigDTO
	 * @return response
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> saveUserBoardConfig(
			@Valid @RequestBody UserBoardConfigDTO userBoardConfigDTO) {
		UserBoardConfigDTO boardConfigDTO = userBoardConfigService.saveUserBoardConfig(userBoardConfigDTO);
		ServiceResponse response = new ServiceResponse(false, "User not logged-in", null);
		if (null != boardConfigDTO) {
			response = new ServiceResponse(true, "Saved user board Configuration", boardConfigDTO);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * Api to get user based configurations for project / super admin
	 *
	 * @return response
	 */
	@GetMapping(value = "/{basicProjectConfigId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getUserBoardConfigurationAdmin(@PathVariable String basicProjectConfigId) {
		UserBoardConfigDTO userBoardConfigDTO = userBoardConfigService.getProjBoardConfigAdmin(basicProjectConfigId);
		ServiceResponse response = new ServiceResponse(false, "No data found", null);
		if (null != userBoardConfigDTO) {
			response = new ServiceResponse(true, "Project Config Fetched successfully", userBoardConfigDTO);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * Api to save board config of admin
	 *
	 * @param userBoardConfigDTO
	 *            userBoardConfigDTO
	 * @return response
	 */
	@PostMapping(value = "/saveAdmin/{basicProjectConfigId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> saveUserBoardConfigAdmin(
			@Valid @RequestBody UserBoardConfigDTO userBoardConfigDTO, @PathVariable String basicProjectConfigId) {
		UserBoardConfigDTO boardConfigDTO = userBoardConfigService.saveUserBoardConfigAdmin(userBoardConfigDTO,
				basicProjectConfigId);
		ServiceResponse response = new ServiceResponse(false, "User not logged-in", null);
		if (null != boardConfigDTO) {
			response = new ServiceResponse(true, "Saved user board Configuration", boardConfigDTO);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
