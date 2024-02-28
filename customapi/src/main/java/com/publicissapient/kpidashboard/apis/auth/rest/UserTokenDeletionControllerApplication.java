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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.service.UserTokenDeletionService;
import com.publicissapient.kpidashboard.apis.auth.token.CookieUtil;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Rest controller to handle logout requests.
 * 
 * @author anisingh4
 */
@Slf4j
@RestController
public class UserTokenDeletionControllerApplication {
	@Autowired
	private AuthProperties authProperties;

	@Autowired
	private CookieUtil cookieUtil;

	@Autowired
	private UserInfoService userInfoService;

	private final UserTokenDeletionService userTokenDeletionService;


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
	 * Logout user from central service.
	 *
	 * @param request
	 *            the request
	 */
	@RequestMapping(value = "/centralUserlogout", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> deleteUserToken(HttpServletRequest request, HttpServletResponse response) {
		Cookie authCookie = cookieUtil.getAuthCookie(request);
		String authCookieToken = authCookie.getValue();
		authCookie.setMaxAge(0);
		String apiKey = authProperties.getResourceAPIKey();
		HttpSession session;
		SecurityContextHolder.clearContext();
		session = request.getSession(false);
		if(session != null) {
			session.invalidate();
		}
		boolean cookieClear = userInfoService.getCentralAuthUserDeleteUserToken(authCookieToken, apiKey);
		cookieUtil.deleteCookie(request, response, CookieUtil.AUTH_COOKIE);
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(true, "Logout Successfully", cookieClear));
	}

	/**
	 * Logout user from local auth.
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
