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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CustomAnalyticsService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** aksshriv1 */
@RestController
public class TokenAuthenticationController {
	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;
	@Autowired
	private CustomAnalyticsService customAnalyticsService;

	/**
	 * Fetch user details from the Central Auth on the first login and save them
	 * into the KnowHow database. For subsequent logins, retrieve user details from
	 * the KnowHow database
	 *
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @return
	 */
	@GetMapping(value = "/fetchUserDetails")
	public ResponseEntity<ServiceResponse> fetchUserDetails(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {

		String authToken = tokenAuthenticationService.getAuthToken(httpServletRequest);
		String userName = tokenAuthenticationService.getUserNameFromToken(authToken);
		ServiceResponse serviceResponse;
		if (null != authToken) {
			Map<String, Object> userMap = customAnalyticsService.addAnalyticsDataAndSaveCentralUser(httpServletResponse,
					userName, authToken);
			serviceResponse = new ServiceResponse(true, "User Details fetched successfully", userMap);
			return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
		} else {
			serviceResponse = new ServiceResponse(false, "token is expired", null);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(serviceResponse);
		}
	}
}
