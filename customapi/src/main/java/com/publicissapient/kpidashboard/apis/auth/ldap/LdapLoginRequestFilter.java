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

package com.publicissapient.kpidashboard.apis.auth.ldap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.publicissapient.kpidashboard.apis.activedirectory.service.ADServerDetailsService;
import com.publicissapient.kpidashboard.apis.auth.AuthenticationResultHandler;
import com.publicissapient.kpidashboard.apis.auth.CustomAuthenticationFailureHandler;
import com.publicissapient.kpidashboard.apis.auth.service.AuthTypesConfigService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The type Ldap login request filter. Fills need for ldap based configurations.
 */
public class LdapLoginRequestFilter extends UsernamePasswordAuthenticationFilter {

	private static final String LDAP_NOT_CONFIGURED = "Please contact your Superadmin or "
			+ "dojosupport@publicissapient.com to get AD login configured";

	CustomApiConfig customApiConfig;
	private ADServerDetailsService adServerDetailsService;

	private AuthTypesConfigService authTypesConfigService;

	/**
	 * Instantiates a new Ldap login request filter.
	 *
	 * @param path
	 *            the path
	 * @param authenticationManager
	 *            the authentication manager
	 * @param authenticationResultHandler
	 *            the authentication result handler
	 * @param authenticationFailureHandler
	 *            authenticationFailureHandler
	 * @param customApiConfig
	 *            customApiConfig
	 * @param adServerDetailsService
	 *            adServerDetailsService
	 * 
	 */
	public LdapLoginRequestFilter(String path, AuthenticationManager authenticationManager,
			AuthenticationResultHandler authenticationResultHandler,
			CustomAuthenticationFailureHandler authenticationFailureHandler, CustomApiConfig customApiConfig,
			ADServerDetailsService adServerDetailsService, AuthTypesConfigService authTypesConfigService) {
		super();
		setAuthenticationSuccessHandler(authenticationResultHandler);
		setAuthenticationFailureHandler(authenticationFailureHandler);
		setAuthenticationManager(authenticationManager);
		setFilterProcessesUrl(path);
		setAuthenticationDetailsSource(new LdapAuthenticationDetailsSource());
		this.customApiConfig = customApiConfig;
		this.adServerDetailsService = adServerDetailsService;
		this.authTypesConfigService = authTypesConfigService;
	}

	/**
	 * Attempts Authentication
	 *
	 * @param request
	 *            request
	 * @param response
	 *            response
	 * @return Authentication
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

		AuthTypeStatus authTypesStatus = authTypesConfigService.getAuthTypesStatus();
		if (authTypesStatus != null && !authTypesStatus.isAdLogin()) {
			throw new AuthenticationServiceException("Active Directory login is disabled");
		}

		if (!request.getMethod().equals("POST")) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}

		String username = obtainUsername(request);
		String password = obtainPassword(request);

		if (username == null) {
			username = "";
		}

		if (password == null) {
			password = StringUtils.EMPTY;
		}

		username = username.trim();

		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
		authRequest.setDetails(AuthType.LDAP);
		// Allow subclasses to set the "details" property
		setDetails(request, authRequest);

		ADServerDetail adServerDetail = adServerDetailsService.getADServerConfig();
		if (adServerDetail == null || StringUtils.isBlank(adServerDetail.getHost())) {
			throw new ProviderNotFoundException(LDAP_NOT_CONFIGURED);
		}
		return this.getAuthenticationManager().authenticate(authRequest);

	}

}
