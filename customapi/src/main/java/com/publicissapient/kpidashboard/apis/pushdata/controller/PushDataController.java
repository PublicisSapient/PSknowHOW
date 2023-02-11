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

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.publicissapient.kpidashboard.apis.common.service.PushDataValidationService;
import lombok.extern.slf4j.Slf4j;

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
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeploy;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuildDeployDTO;
import com.publicissapient.kpidashboard.apis.pushdata.service.PushBuildServiceImpl;

@Validated
@RestController
@RequestMapping("/pushData")
@Slf4j
public class PushDataController {

	@Autowired
	PushBuildServiceImpl pushBuildService;

	@Autowired
	PushDataValidationService pushDataValidationService;

	@RequestMapping(value = "/build/{id}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> savePushDataBuilds(@PathVariable("id") String projectConfigId,
																HttpServletResponse response,
			 @RequestBody @Valid PushBuildDeployDTO pushBuildDeployDTO) {
		//Object= pushDataValidationService.validateToken(response);
		final ModelMapper modelMapper = new ModelMapper();
		PushBuildDeploy buildDeploy = modelMapper.map(pushBuildDeployDTO, PushBuildDeploy.class);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponse(true,"Saved Records successfully",pushBuildService.processPushDataInput(buildDeploy,projectConfigId)));
	}

}