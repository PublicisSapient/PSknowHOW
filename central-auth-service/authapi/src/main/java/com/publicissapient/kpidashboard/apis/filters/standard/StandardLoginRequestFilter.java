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

package com.publicissapient.kpidashboard.apis.filters.standard;

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.filters.CustomAuthenticationFailureHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Provides Standard Login Request Filter .
 *
 * @author Hiren Babariya
 */
public class StandardLoginRequestFilter extends UsernamePasswordAuthenticationFilter {
	private AuthConfig authProperties;

	/**
	 * @param path
	 * @param standardAuthenticationResultHandler
	 */
	public StandardLoginRequestFilter(
			String path,
			AuthenticationManager authenticationManager,
			AuthenticationResultHandler standardAuthenticationResultHandler,
			CustomAuthenticationFailureHandler authenticationFailureHandler,
			AuthConfig authProperties
	) {
		super();
		super.setAuthenticationManager(authenticationManager);
		setAuthenticationSuccessHandler(standardAuthenticationResultHandler);
		setAuthenticationFailureHandler(authenticationFailureHandler);
		setFilterProcessesUrl(path);
		this.authProperties = authProperties;
	}

	/**
	 * Attempts Authentication
	 *
	 * @param request
	 * @param response
	 * @return Authentication
	 * @throws AuthenticationException
	 */
	@Override
	public Authentication attemptAuthentication(
			HttpServletRequest request,
			HttpServletResponse response
	) throws AuthenticationException {

		if (!authProperties
				.getAuthenticationProviders()
				.contains(AuthType.STANDARD)) {
			throw new AuthenticationServiceException("Standard login is disabled");
		}

		if (!request
				.getMethod()
				.equals("POST")) {
			throw new AuthenticationServiceException("Authentication method not supported: " +
													 request.getMethod());
		}

		String username = obtainUsername(request);
		String password = obtainPassword(request);

		if (username ==
			null) {
			username = "";
		}

		if (password ==
			null) {
			password = StringUtils.EMPTY;
		}

		username = username.trim();

		StandardAuthenticationToken authRequest = new StandardAuthenticationToken(
				username,
				password
		);

		authRequest.setDetails(AuthType.STANDARD);

		return this
				.getAuthenticationManager()
				.authenticate(authRequest);
	}

}
