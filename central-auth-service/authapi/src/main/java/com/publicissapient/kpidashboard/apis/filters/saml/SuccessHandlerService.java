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
package com.publicissapient.kpidashboard.apis.filters.saml;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.config.AuthProperties;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.filters.AuthenticationResponseService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides SuccessHandler for saml login .
 *
 * @author Hiren Babariya
 */
@Slf4j
@Service
@AllArgsConstructor
public class SuccessHandlerService extends SimpleUrlAuthenticationSuccessHandler {

	@Autowired
	private final AuthProperties authConfigurationProperties;
	@Autowired
	private final CookieUtil cookieUtil;
	@Autowired
	private final UserService userService;
	@Autowired
	private AuthenticationResponseService authenticationResponseService;

	@Override
	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) {
		String authToken = authenticationResponseService.handle(response, authentication, AuthType.SAML);

		if (Objects.nonNull(authToken)) {
			return getSuccessPage(authToken);
		} else {
			return getSuccessPage(null);
		}
	}

	public String getSuccessPage(String authToken) {
		return String.format(authConfigurationProperties.getLoginSuccessPageFormat(), authToken);
	}

}
