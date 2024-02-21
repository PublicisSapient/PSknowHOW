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

package com.publicissapient.kpidashboard.apis.auth.apitoken;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.publicissapient.kpidashboard.apis.auth.AuthenticationResultHandler;
import com.publicissapient.kpidashboard.common.constant.AuthType;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Processor for authentication htttp request.
 * 
 * @author anisingh4
 */
public class ApiTokenRequestFilter extends AbstractAuthenticationProcessingFilter {

	/**
	 * Class Constructor
	 */
	public ApiTokenRequestFilter() {
		super(new AntPathRequestMatcher("/**", "POST"));
	}

	public ApiTokenRequestFilter(String path, AuthenticationManager authManager,
			AuthenticationResultHandler authenticationResultHandler) {
		this();
		setAuthenticationManager(authManager);
		setAuthenticationSuccessHandler(authenticationResultHandler);
		setFilterProcessesUrl(path);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		String apiUser = request.getHeader("apiUser");
		String authHeader = request.getHeader("Authorization");

		if (StringUtils.isEmpty(apiUser) || StringUtils.isEmpty(authHeader)) {
			chain.doFilter(request, response);
		} else {
			super.doFilter(req, res, chain);
		}
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException { // NOSONAR //NOPMD

		String apiUser = request.getHeader("apiUser");
		String apikey = "";

		String authHeader = request.getHeader("Authorization");

		String encodedAuthStr = authHeader.substring(authHeader.indexOf(' '), authHeader.length());
		byte[] encodedAuthbytes = encodedAuthStr.getBytes();
		String decodedAuthStr = new String(Base64.decodeBase64(encodedAuthbytes));
		String decodedAuthJson = decodedAuthStr.substring(decodedAuthStr.indexOf(':') + 1, decodedAuthStr.length());

		JSONParser jsonParser = new JSONParser();
		try {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(decodedAuthJson);
			apikey = (String) jsonObject.get("apiKey");
		} catch (ParseException e) {
			throw new AuthenticationServiceException("Unable to parse apikey token.", e);
		}

		ApiTokenAuthenticationToken authRequest = new ApiTokenAuthenticationToken(apiUser, apikey);

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