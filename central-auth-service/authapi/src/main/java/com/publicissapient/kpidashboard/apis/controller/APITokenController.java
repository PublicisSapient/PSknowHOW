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

package com.publicissapient.kpidashboard.apis.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;
import com.publicissapient.kpidashboard.common.model.GenerateAPIKeyRequestDTO;
import com.publicissapient.kpidashboard.common.model.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Rest Controller to handle authentication Client and provide to auth API key
 *
 * @author hirenkumar Babariya
 */
@RestController
@AllArgsConstructor
@Slf4j
public class APITokenController {

	private final TokenAuthenticationService tokenAuthenticationService;

	/**
	 * API to generate token for Action Policy Rule push by resource and generate
	 * token has permission only root user
	 *
	 * @param generateAPIKeyRequestDTO
	 * @return
	 */
	@RequestMapping(value = "/generateAPIKey", method = RequestMethod.POST) // NOSONAR
	public ResponseEntity<ServiceResponse> generateAndSaveToken(
			@RequestBody GenerateAPIKeyRequestDTO generateAPIKeyRequestDTO) {
		return ResponseEntity.status(HttpStatus.OK)
							 .body(tokenAuthenticationService.generateAndSaveToken(generateAPIKeyRequestDTO.getResource()));
	}
}
