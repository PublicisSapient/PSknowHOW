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

package com.publicissapient.kpidashboard.apis.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.publicissapient.kpidashboard.apis.constant.CORSConstants;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.context.ExecutionLogContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Component
@Slf4j
public class CorsFilter extends OncePerRequestFilter {

	@Autowired
	private CustomApiConfig apiSettings;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// Added code to handle the CORS security
		if (isValidCORSRequest(request)) {
			setCORSHeaders(request, response);
		} else {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
		if ("OPTIONS".equals(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			filterChain.doFilter(request, response);
		}
	}

	/**
	 * <p>
	 * This method checks whether the request is a valid CORS request
	 * </p>
	 * 
	 * @param request
	 * @return Boolean
	 * @throws MalformedURLException
	 */
	private Boolean isValidCORSRequest(HttpServletRequest request) throws MalformedURLException {
		List<String> originWhiteList = apiSettings.getCorsFilterValidOrigin();
		Boolean result = Boolean.FALSE;
		String origin = request.getHeader(CORSConstants.HEADER_VALUE_ACCESS_CONTROL_ORIGIN);
		log.debug("Value of Origin header in request : {}", origin);
		if (StringUtils.isNotBlank(origin)) {
			if (CollectionUtils.isNotEmpty(originWhiteList)) {
				result = validateOriginWithWhitelist(originWhiteList, result, origin);
			} else {
				log.error("CORS logError : whitelist not set for validation");
				result = Boolean.TRUE;
			}
		} else {
			result = Boolean.TRUE;
		}
		log.debug("Result of matching the origin with whitelist : {}", result);
		return result;
	}

	/**
	 * <p>
	 * This method validates the origin with whitelist of CORS valid addresses
	 * </p>
	 * 
	 * @param originWhiteList
	 * @param theResult
	 * @param origin
	 * @return
	 * @throws MalformedURLException
	 */
	private Boolean validateOriginWithWhitelist(List<String> originWhiteList, Boolean theResult, String origin)
			throws MalformedURLException {
		Boolean result = theResult;
		String originHost = new URL(origin).getHost();
		log.debug("value of orignHost : {}", originHost);
		for (String allowedOrigin : originWhiteList) {
			if (StringUtils.equalsIgnoreCase(originHost, allowedOrigin)) {
				result = Boolean.TRUE;
				break;
			}
		}
		return result;
	}

	/**
	 * <p>
	 * This method sets the headers in reponse to enable CORS
	 * </p>
	 * 
	 * @param request
	 * @param response
	 */
	private void setCORSHeaders(HttpServletRequest request, HttpServletResponse response) {
		// moved the header keys and values to CORSConstants.java
		String orign = CommonUtils
				.handleCrossScriptingTaintedValue(request.getHeader(CORSConstants.HEADER_VALUE_ACCESS_CONTROL_ORIGIN));
		response.setHeader(CORSConstants.HEADER_NAME_ACCESS_CONTROL_ALLOW_ORIGIN, orign);
		response.setHeader(CORSConstants.HEADER_NAME_ACCESS_CONTROL_ALLOW_METHODS,
				CORSConstants.HEADER_VALUE_ALLOWED_METHODS);
		response.setHeader(CORSConstants.HEADER_NAME_ACCESS_CONTROL_MAX_AGE, CORSConstants.HEADER_VALUE_MAX_AGE);
		response.setHeader(CORSConstants.HEADER_NAME_ACCESS_CONTROL_ALLOW_HEADERS,
				CORSConstants.HEADER_VALUE_ALLOWED_HEADERS);
		response.addHeader(CORSConstants.HEADER_NAME_ACCESS_CONTROL_EXPOSE_HEADERS,
				CORSConstants.HEADER_VALUE_EXPOSE_HEADERS);
		response.setHeader("Access-Control-Allow-Credentials", "true");
		ExecutionLogContext executionLogContext = new ExecutionLogContext();
		executionLogContext.setRequestId(request.getHeader(CORSConstants.REQUEST_ID));
		executionLogContext.setEnvironment(orign);
		ExecutionLogContext.set(executionLogContext);

	}
}