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

package com.publicissapient.kpidashboard.apis.common.rest;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.UserTokenAuthenticationDTO;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

/**
 * aksshriv1
 */
@RestController
public class TokenAuthenticationController {

	public static final String AUTH_DETAILS_UPDATED_FLAG = "auth-details-updated";

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;

	@PostMapping(value = "/validateToken")
	public ResponseEntity<ServiceResponse> validateToken(
			@Valid @RequestBody UserTokenAuthenticationDTO tokenAuthenticationDTO, HttpServletResponse response) {
		Authentication authentication = tokenAuthenticationService.getAuthentication(tokenAuthenticationDTO, response);
		ServiceResponse serviceResponse;
		if (null != authentication) {

			UserTokenAuthenticationDTO userData = tokenAuthenticationService.addAuthentication(response,
					authentication);

			Collection<String> authDetails = response.getHeaders(AUTH_DETAILS_UPDATED_FLAG);
			boolean value = authDetails != null && authDetails.stream().anyMatch("true"::equals);
			if (value) {
				serviceResponse = new ServiceResponse(true, "success_valid_token", userData);
			} else {
				serviceResponse = new ServiceResponse(false, "token is expired", null);
			}
		} else {
			serviceResponse = new ServiceResponse(false, "Unauthorized", null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
	}
}
