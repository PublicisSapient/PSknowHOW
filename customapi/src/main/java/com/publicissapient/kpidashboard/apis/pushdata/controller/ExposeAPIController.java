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

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.ExposeAPITokenRequestDTO;
import com.publicissapient.kpidashboard.apis.pushdata.service.AuthExposeAPIService;

import lombok.extern.slf4j.Slf4j;

@Validated
@RestController
@RequestMapping("/exposeAPI")
@Slf4j
public class ExposeAPIController {

	@Autowired
	private AuthExposeAPIService authExposeAPIService;

	/**
	 * API to generate token for push data based and generate token has permission
	 * only superadmin and project admin of particular project
	 * 
	 * @param exposeAPITokenRequestDTO
	 * @return
	 */
	@PreAuthorize("hasPermission(#exposeAPITokenRequestDTO.basicProjectConfigId, 'SAVE_PROJECT_TOOL')")
	@RequestMapping(value = "/generateToken", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> generateAndSaveToken(
			@RequestBody @Valid ExposeAPITokenRequestDTO exposeAPITokenRequestDTO) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(authExposeAPIService.generateAndSaveToken(exposeAPITokenRequestDTO));
	}
}
