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

package com.publicissapient.kpidashboard.apis.connection.rest;

import javax.validation.constraints.NotNull;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.connection.service.TestConnectionService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.connection.ConnectionDTO;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/testconnection")
@Slf4j
public class TestConnectionController {

	@Autowired
	TestConnectionService testConnectionService;

	/**
	 * Validate JIRA connection
	 * 
	 * @param connectionDTO
	 * @return
	 */
	@RequestMapping(path = "/jira", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> validateJiraConnection(@NotNull @RequestBody ConnectionDTO connectionDTO) {
		log.info("validating JIRA connections credentials");
		final ModelMapper modelMapper = new ModelMapper();
		final Connection connection = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK)
				.body(testConnectionService.validateConnection(connection, Constant.TOOL_JIRA));

	}

	/**
	 * Validate Sonar connection
	 * 
	 * @param connectionDTO
	 * @return
	 */
	@RequestMapping(path = "/sonar", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> validateSonarConnection(@NotNull @RequestBody ConnectionDTO connectionDTO) {
		log.info("validating Sonar connections credentials");
		final ModelMapper modelMapper = new ModelMapper();
		final Connection connection = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK)
				.body(testConnectionService.validateConnection(connection, Constant.TOOL_SONAR));

	}

	/**
	 * Validate teamcity connection
	 * 
	 * @param connectionDTO
	 * @return
	 */
	@RequestMapping(path = "/teamcity", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> validateTeamcityConnection(
			@NotNull @RequestBody ConnectionDTO connectionDTO) {
		log.info("validating Teamcity connections credentials");
		final ModelMapper modelMapper = new ModelMapper();
		final Connection connection = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK)
				.body(testConnectionService.validateConnection(connection, Constant.TOOL_TEAMCITY));

	}

	@RequestMapping(path = "/zephyr", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> validateZephyrConnection(@NotNull @RequestBody ConnectionDTO connectionDTO) {
		log.info("validating Zephyr connections credentials");
		final ModelMapper modelMapper = new ModelMapper();
		final Connection connection = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK)
				.body(testConnectionService.validateConnection(connection, Constant.TOOL_ZEPHYR));

	}

	/**
	 * Validate bamboo connection
	 * 
	 * @param connectionDTO
	 * @return
	 */
	@RequestMapping(path = "/bamboo", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> validateBambooConnection(@NotNull @RequestBody ConnectionDTO connectionDTO) {
		log.info("validating Bamboo connections credentials");
		final ModelMapper modelMapper = new ModelMapper();
		final Connection connection = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK)
				.body(testConnectionService.validateConnection(connection, Constant.TOOL_BAMBOO));

	}

	/**
	 * Validate Jenkins connection
	 * 
	 * @param connectionDTO
	 * @return
	 */
	@RequestMapping(path = "/jenkins", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> validateJenkinsConnection(
			@NotNull @RequestBody ConnectionDTO connectionDTO) {
		log.info("validating Jenkins connections credentials");
		final ModelMapper modelMapper = new ModelMapper();
		final Connection connection = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK)
				.body(testConnectionService.validateConnection(connection, Constant.TOOL_JENKINS));

	}

	@PostMapping("/gitlab")
	public ResponseEntity<ServiceResponse> validateGitlabConnection(@NotNull @RequestBody ConnectionDTO connectionDTO) {
		log.info("validating Gitlab connections credentials");
		final ModelMapper modelMapper = new ModelMapper();
		final Connection connection = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK)
				.body(testConnectionService.validateConnection(connection, Constant.TOOL_GITLAB));

	}

	/**
	 * Validate Bitbucket connection
	 * 
	 * @param connectionDTO
	 * @return
	 */
	@RequestMapping(path = "/bitbucket", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> validateBitbucketConnection(
			@NotNull @RequestBody ConnectionDTO connectionDTO) {
		log.info("validating Bitbucket connections credentials");
		final ModelMapper modelMapper = new ModelMapper();
		final Connection connection = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK)
				.body(testConnectionService.validateConnection(connection, Constant.TOOL_BITBUCKET));

	}

	@PostMapping("/azureboard")
	public ResponseEntity<ServiceResponse> validateAzureBoardeConnection(
			@NotNull @RequestBody ConnectionDTO connectionDTO) {
		log.info("validating azure board connections credentials");
		final ModelMapper modelMapper = new ModelMapper();
		final Connection connection = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK)
				.body(testConnectionService.validateConnection(connection, Constant.TOOL_AZURE));

	}

	@PostMapping("/azurerepo")
	public ResponseEntity<ServiceResponse> validateAzureRepoConnection(
			@NotNull @RequestBody ConnectionDTO connectionDTO) {
		log.info("validating azure repo connections credentials");
		final ModelMapper modelMapper = new ModelMapper();
		final Connection connection = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK)
				.body(testConnectionService.validateConnection(connection, Constant.TOOL_AZUREREPO));

	}

	@PostMapping("/azurepipeline")
	public ResponseEntity<ServiceResponse> validateAzurePipelineConnection(
			@NotNull @RequestBody ConnectionDTO connectionDTO) {
		log.info("validating azure pipeline connections credentials");
		final ModelMapper modelMapper = new ModelMapper();
		final Connection connection = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK)
				.body(testConnectionService.validateConnection(connection, Constant.TOOL_AZUREPIPELINE));

	}

	@PostMapping("/github")
	public ResponseEntity<ServiceResponse> validateGitHubConnection(@NotNull @RequestBody ConnectionDTO connectionDTO) {
		log.info("validating Sonar connections credentials");
		final ModelMapper modelMapper = new ModelMapper();
		final Connection connection = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK)
				.body(testConnectionService.validateConnection(connection, Constant.TOOL_GITHUB));

	}

	@PostMapping("/repotool")
	public ResponseEntity<ServiceResponse> validateRepoToolsConnection(@NotNull @RequestBody ConnectionDTO connectionDTO) {
		log.info("validating Sonar connections credentials");
		final ModelMapper modelMapper = new ModelMapper();
		final Connection connection = modelMapper.map(connectionDTO, Connection.class);
		return ResponseEntity.status(HttpStatus.OK).body(testConnectionService.validateConnection(connection, CommonConstant.REPO_TOOLS));

	}

}
