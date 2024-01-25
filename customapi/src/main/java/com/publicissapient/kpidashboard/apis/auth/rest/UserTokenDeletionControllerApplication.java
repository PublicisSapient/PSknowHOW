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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.service.UserTokenDeletionService;
import com.publicissapient.kpidashboard.apis.auth.token.CookieUtil;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Rest controller to handle logout requests.
 * 
 * @author anisingh4
 */
@Slf4j
@RestController
public class UserTokenDeletionControllerApplication {

	private static final String AUTH_RESPONSE_HEADER = "X-Authentication-Token";

	@Autowired
	private AuthProperties authProperties;

	@Autowired
	private CookieUtil cookieUtil;

	@Autowired
	private UserInfoService userInfoService;

	/**
	 * Instantiates a new User token deletion controller.
	 *
	 * @param userTokenDeletionService
	 *            the user token deletion service
	 */
	@Autowired
	public UserTokenDeletionControllerApplication(UserTokenDeletionService userTokenDeletionService) {
	}

	/**
	 * Logout user.
	 *
	 * @param request
	 *            the request
	 */
	@RequestMapping(value = "/userlogout", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> deleteUserToken(HttpServletRequest request, HttpServletResponse response) {
		log.info("UserTokenDeletionController::deleteUserToken start");
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
		//Cookie authCookieRemove = new Cookie("authCookie", "");
		//resetHeader(response, "", authCookieRemove);
		log.info("UserTokenDeletionController::deleteUserToken end");
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(true, "Logout Successfully", cookieClear));
	}

	private void resetHeader(HttpServletResponse response, String authToken, Cookie cookie) {
		response.addHeader(AUTH_RESPONSE_HEADER, authToken);
		response.addHeader("CLEAR_VIA_KNOWHOW" , "true");
		response.addCookie(cookie);
		cookieUtil.addSameSiteCookieAttribute(response);
		log.info("UserTokenDeletionController::resetHeader end");
	}

}
