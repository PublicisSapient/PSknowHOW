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

package com.publicissapient.kpidashboard.apis.auth.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.auth.access.Admin;
import com.publicissapient.kpidashboard.apis.auth.model.ApiToken;
import com.publicissapient.kpidashboard.apis.auth.service.ApiTokenRequest;
import com.publicissapient.kpidashboard.apis.auth.service.ApiTokenService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.common.util.EncryptionException;

//import com.sapient.

/**
 * Rest Controller to handle admin request
 */
@RestController
@RequestMapping("/admin")
@Admin
public class AdminController {

	private final ApiTokenService apiTokenService;
	@Autowired
	private UserInfoService userInfoService;

	/**
	 * Class Constructor specifying UserInfoService and ApiTokenService
	 *
	 * @param apiTokenService
	 */
	@Autowired
	public AdminController(ApiTokenService apiTokenService) {
		this.apiTokenService = apiTokenService;
	}

	/**
	 * Creates access token.
	 *
	 * @param apiTokenRequest
	 * @return api access token
	 */
	@RequestMapping(value = "/createToken", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	// NOSONAR
	public ResponseEntity<String> createToken(@Valid @RequestBody ApiTokenRequest apiTokenRequest) {
		try {
			return ResponseEntity.status(HttpStatus.OK)
					.body(apiTokenService.getApiToken(apiTokenRequest.getApiUser(), apiTokenRequest.getExpirationDt()));
		} catch (EncryptionException | com.publicissapient.kpidashboard.common.exceptions.ApplicationException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	/**
	 * Returns list of tokens
	 *
	 * @return list of tokens
	 */
	@RequestMapping(path = "/apitokens", method = RequestMethod.GET) // NOSONAR
	public ResponseEntity<Collection<ApiToken>> getApiTokens() {
		return ResponseEntity.status(HttpStatus.OK).body(apiTokenService.getApiTokens());
	}
}
