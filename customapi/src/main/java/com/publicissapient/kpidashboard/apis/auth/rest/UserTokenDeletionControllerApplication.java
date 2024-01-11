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
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.auth.service.UserTokenDeletionService;
import com.publicissapient.kpidashboard.apis.auth.token.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Rest controller to handle logout requests.
 * 
 * @author anisingh4
 */
@Slf4j
@RestController
public class UserTokenDeletionControllerApplication {

	private final UserTokenDeletionService userTokenDeletionService;
	@Autowired
	private CookieUtil cookieUtil;

	/**
	 * Instantiates a new User token deletion controller.
	 *
	 * @param userTokenDeletionService
	 *            the user token deletion service
	 */
	@Autowired
	public UserTokenDeletionControllerApplication(UserTokenDeletionService userTokenDeletionService) {
		this.userTokenDeletionService = userTokenDeletionService;
	}

	/**
	 * Logout user.
	 *
	 * @param request
	 *            the request
	 */
	@RequestMapping(value = "/userlogout", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity deleteUserToken(HttpServletRequest request) {
		log.info("UserTokenDeletionController::deleteUserToken start");
		String token = StringUtils.removeStart(request.getHeader("Authorization"), "Bearer ");
		userTokenDeletionService.deleteUserDetails(token);
		ResponseCookie authCookie = cookieUtil.deleteAccessTokenCookie();
		log.info("UserTokenDeletionController::deleteUserToken end");
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, authCookie.toString()).build();
	}

}
