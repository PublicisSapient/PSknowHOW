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

package com.publicissapient.kpidashboard.apis.pushdata.controller;

import java.util.List;

import javax.validation.Valid;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.ExposeApiToken;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeploy;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataTraceLog;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuildDeployDTO;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushDataTraceLogDTO;
import com.publicissapient.kpidashboard.apis.pushdata.service.AuthExposeAPIService;
import com.publicissapient.kpidashboard.apis.pushdata.service.PushBaseService;
import com.publicissapient.kpidashboard.apis.pushdata.service.PushDataTraceLogService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Validated
@RestController
@RequestMapping("/pushData")
@Slf4j
public class PushDataController {

	final ModelMapper modelMapper = new ModelMapper();
	@Autowired
	PushBaseService pushBuildService;
	@Autowired
	AuthExposeAPIService authExposeAPIService;
	@Autowired
	private PushDataTraceLogService pushDataTraceLogService;

	/**
	 * push data api for build tools
	 * 
	 * @param request
	 * @param pushBuildDeployDTO
	 * @return
	 */

	@RequestMapping(value = "/build", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> savePushDataBuilds(HttpServletRequest request,
			@RequestBody @Valid PushBuildDeployDTO pushBuildDeployDTO) {
		PushDataTraceLog instance = PushDataTraceLog.getInstance();
		instance.setPushApiSource("build");
		ExposeApiToken exposeApiToken = authExposeAPIService.validateToken(request);
		PushBuildDeploy buildDeploy = modelMapper.map(pushBuildDeployDTO, PushBuildDeploy.class);
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(true, "Saved Records successfully",
				pushBuildService.processPushDataInput(buildDeploy, exposeApiToken.getBasicProjectConfigId())));
	}

	@RequestMapping(value = "/tracelog/{basicConfigId}", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> getTraceLog(@PathVariable String basicConfigId) {
		List<PushDataTraceLogDTO> allLogs = pushDataTraceLogService.getByProjectConfigId(new ObjectId(basicConfigId));
		ServiceResponse response;
		if (CollectionUtils.isNotEmpty(allLogs)) {
			log.info("Fetching all logs of configId " + basicConfigId);
			response = new ServiceResponse(true, "Found Logs", allLogs);
		} else {
			response = new ServiceResponse(false, "No Logs Present", null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}