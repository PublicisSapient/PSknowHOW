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

package com.publicissapient.kpidashboard.apis.filters.apitoken;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.publicissapient.kpidashboard.apis.enums.AuthType;

/**
 * Processor for authentication htttp request.
 * 
 * @author Hiren Babariya
 */
public class ApiTokenRequestFilter extends AbstractAuthenticationProcessingFilter {

	/**
	 * Class Constructor
	 */
	public ApiTokenRequestFilter() {
		super(new AntPathRequestMatcher("/**", "POST"));
	}

	public ApiTokenRequestFilter(String path, AuthenticationManager authManager) {
		this();
		setAuthenticationManager(authManager);
		setFilterProcessesUrl(path);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		String resourceAPIKey = request.getHeader("x-api-key");
		String resource = request.getHeader("resource");

		if (StringUtils.isNotEmpty(resourceAPIKey) && StringUtils.isNotEmpty(resource)) {
			super.doFilter(req, res, chain);
		} else {
			chain.doFilter(request, res);
		}
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException { // NOSONAR //NOPMD

		String resourceAPIKey = request.getHeader("x-api-key");
		String resource = request.getHeader("resource");

		ApiTokenAuthenticationToken authRequest = new ApiTokenAuthenticationToken(resource, resourceAPIKey);

		authRequest.setDetails(AuthType.APIKEY);

		Authentication authentication = this.getAuthenticationManager().authenticate(authRequest);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		return authentication;
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		SecurityContextHolder.getContext().setAuthentication(authResult);
		chain.doFilter(request, response);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "ApiToken Authentication Failed");
	}

}