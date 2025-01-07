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

package com.publicissapient.kpidashboard.apis.appsetting.rest;

import java.util.List;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolsStatusResponse;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.appsetting.service.ProcessorService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.context.ExecutionLogContext;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionBasicConfig;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller for CRUD operations related to all processors details running on
 * the instance
 *
 * @author pansharm5, swati.lamba
 */
@RestController
@RequestMapping("/processor")
@Slf4j
public class ProcessorController {

	@Autowired
	private ProcessorService processorService;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Autowired
	private CustomApiConfig customApiConfig;

	/**
	 * Gets details of all processors on the running instance including: Last
	 * executed time of the processor to fetch new data and Status Success/Failure
	 *
	 * @return {@code ResponseEntity<ServiceResponse>} with Processor object
	 */
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission(null, 'GET_PROCESSORS')")
	public ResponseEntity<ServiceResponse> getAllProcessorDetails() {
		// NOSONAR
		ServiceResponse response = processorService.getAllProcessorDetails();
		HttpStatus responseStatus = HttpStatus.OK;
		if (null == response || !response.getSuccess()) {
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			log.warn("Did not get successful reponse from the service: {}", response);
		}
		return ResponseEntity.status(responseStatus).body(response);

	}

	/**
	 * Triggers the processor to run the job to fetch the latest data from the tool
	 * 
	 * @return {@code ResponseEntity<ServiceResponse>} with true is triggered
	 *         successfully success
	 */
	@PostMapping(path = "/trigger/{processorName}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission(#projectBasicConfigIds, 'TRIGGER_PROCESSOR')")
	public ResponseEntity<ServiceResponse> triggerProcessor(@PathVariable String processorName,
			@RequestBody List<String> projectBasicConfigIds) {
		ProcessorExecutionBasicConfig processorExecutionBasicConfig = new ProcessorExecutionBasicConfig();
		processorExecutionBasicConfig.setProjectBasicConfigIds(projectBasicConfigIds);
		processorExecutionBasicConfig.setLogContext(ExecutionLogContext.getContext());

		// NOSONAR
		ServiceResponse response = processorService.runProcessor(processorName, processorExecutionBasicConfig);

		HttpStatus responseStatus = HttpStatus.OK;
		if (null == response) {
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			log.warn("Did not get successful reponse from the service: {}", response);
		}
		return ResponseEntity.status(responseStatus).body(response);

	}

	@GetMapping("/tracelog")
	public ResponseEntity<ServiceResponse> getProcessorTraceLog(@RequestParam(required = false) String processorName,
			@RequestParam(required = false) String basicProjectConfigId) {

		List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService.getTraceLogs(processorName,
				basicProjectConfigId);

		ServiceResponse response = new ServiceResponse(true, "Processor trace logs", traceLogs);

		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	@PostMapping(path = "/fetchSprint/{sprintId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission(#sprintId, 'TRIGGER_SPRINT_FETCH')")
	public ResponseEntity<ServiceResponse> triggerSprintFetch(@PathVariable String sprintId) {


		ServiceResponse response = processorService.fetchActiveSprint(sprintId);

		HttpStatus responseStatus = HttpStatus.OK;
		if (null == response) {
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			log.warn("Did not get successful response from the service: {} ", response);
		}
		return ResponseEntity.status(responseStatus).body(response);

	}

	/**
	 * to be hit by repo tool platform to save repo tool tracelogs
	 * @param request
	 * 			http request with api key
	 * @param repoToolsStatusResponse
	 * 			status to be saved in trace logs
	 * @return {@code ResponseEntity<>} with HttpStatus OK if authorized
	 */
	@PostMapping(path = "/saveRepoToolsStatus", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> saveRepoToolsStatus(HttpServletRequest request,
			@NonNull @RequestBody RepoToolsStatusResponse repoToolsStatusResponse) {
		log.info("Received {} request for /saveRepoToolsStatus", request.getMethod());
		Boolean isApiAuth = customApiConfig.getxApiKey().equalsIgnoreCase(request.getHeader(Constant.TOKEN_KEY));
		if (Boolean.FALSE.equals(isApiAuth)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		processorService.saveRepoToolTraceLogs(repoToolsStatusResponse);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PostMapping(path = "/metadata/step/{projectBasicConfigId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission(#projectBasicConfigIds, 'TRIGGER_PROCESSOR')")
	public ResponseEntity<ServiceResponse> triggerMetaDataStep(@PathVariable String projectBasicConfigId) {
		ServiceResponse response = processorService.runMetadataStep(projectBasicConfigId);
		HttpStatus responseStatus = HttpStatus.OK;
		if (null == response) {
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			log.warn("Did not get successful response from the service: {} ", response);
		}
		return ResponseEntity.status(responseStatus).body(response);

	}

}