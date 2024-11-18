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

package com.publicissapient.kpidashboard.apis.audit;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @author vijkumar18
 *
 */
@Slf4j
public class ApiAuditLogging extends DispatcherServlet {

	private static final ObjectMapper mapper = new ObjectMapper();

	@Override
	protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

		// Create a JSON object to store HTTP logging information
		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("uri", requestWrapper.getRequestURI());
		rootNode.put("clientIp", requestWrapper.getRemoteAddr());
		try {
			rootNode.set("requestHeaders", mapper.valueToTree(getRequestHeaders(requestWrapper)));
		} catch (IllegalArgumentException e) {
			log.error("Exception while processing", e);
		}
		String method = requestWrapper.getMethod();
		rootNode.put("method", method);
		try {
			super.doDispatch(requestWrapper, responseWrapper);
		} catch (Exception e) {
			log.error("Exception while processing", e);
		} finally {
			try {
				if (method.equals("GET") || method.equals("DELETE")) {

					rootNode.set("request", mapper.valueToTree(requestWrapper.getParameterMap()));
				} else {
					JsonNode newNode = mapper.readTree(requestWrapper.getContentAsByteArray());
					rootNode.set("request", newNode);
				}
				rootNode.put("status", responseWrapper.getStatus());
				if (responseWrapper.getContentAsByteArray().length > 0) {
					JsonNode newNode = mapper.readTree(responseWrapper.getContentAsByteArray());
					rootNode.set("response", newNode);
				}
				responseWrapper.copyBodyToResponse();

				rootNode.set("responseHeaders", mapper.valueToTree(getResponsetHeaders(responseWrapper)));
				log.info(rootNode.toString());
			} catch (IllegalArgumentException | IOException e) {
				log.error("Exception", e);
			}
		}
	}

	private Map<String, Object> getRequestHeaders(HttpServletRequest request) {
		Map<String, Object> headers = new HashMap<>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			headers.put(headerName, request.getHeader(headerName));
		}
		return headers;

	}

	private Map<String, Object> getResponsetHeaders(ContentCachingResponseWrapper response) {
		Map<String, Object> headers = new HashMap<>();
		Collection<String> headerNames = response.getHeaderNames();
		for (String headerName : headerNames) {
			headers.put(headerName, response.getHeader(headerName));
		}
		return headers;
	}
}
