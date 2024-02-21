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

package com.publicissapient.kpidashboard.apis.logging;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.publicissapient.kpidashboard.common.model.application.KeyValueLog;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeyValueLoggingFilter implements Filter {

	protected static final String USER_AUTHORITIES = "USER_AUTHORITIES";
	protected static final String USER_DETAILS = "USER_DETAILS";
	protected static final String USER_NAME = "USER_NAME";
	protected static final String SESSION_ID = "SESSION_ID";
	protected static final String STATUS_CODE = "STATUS_CODE";
	protected static final String REQUEST_METHOD = "REQUEST_METHOD";
	protected static final String REQUEST_URL = "REQUEST_URL";
	protected static final String REMOTE_ADDRESS = "REMOTE_ADDRESS";
	protected static final String APPLICATION_NAME = "APPLICATION_NAME";
	protected static final String APPLICATION_VERSION = "APPLICATION_VERSION";

	@Value("${application.name}")
	private String appName;

	@Value("${version.number}")
	private String version;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper((HttpServletRequest) request);
		HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response);

		chain.doFilter(request, response);

		String keyValueLog = getLogEntry(requestWrapper, responseWrapper).toString();
		log.info(keyValueLog);
	}

	private KeyValueLog getLogEntry(HttpServletRequest request, HttpServletResponse response) {

		KeyValueLog log = new KeyValueLog();
		log.with(REMOTE_ADDRESS, request.getRemoteAddr()).with(APPLICATION_NAME, appName)
				.with(APPLICATION_VERSION, version).with(REQUEST_URL, request.getRequestURL().toString())
				.with(REQUEST_METHOD, request.getMethod()).with(STATUS_CODE, response.getStatus());

		HttpSession session = request.getSession(false);
		if (session != null) {
			log.with(SESSION_ID, session.getId());
		}

		Authentication user = SecurityContextHolder.getContext().getAuthentication();
		if (user != null) {
			log.with(USER_NAME, user.getPrincipal()).with(USER_DETAILS, user.getDetails().toString())
					.with(USER_AUTHORITIES, user.getAuthorities().toString());
		}

		return log;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// noop
	}

	@Override
	public void destroy() {
		// noop

	}

}
