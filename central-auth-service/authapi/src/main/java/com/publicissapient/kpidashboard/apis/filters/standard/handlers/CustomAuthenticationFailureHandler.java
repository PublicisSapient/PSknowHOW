/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.filters.standard.handlers;

import static com.publicissapient.kpidashboard.apis.constant.CommonConstant.WRONG_CREDENTIALS_ERROR_MESSAGE;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			AuthenticationException exception) throws IOException {
		httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
		httpServletResponse.setContentType("application/json");

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("timestamp", Instant.now());

		data.put("status", HttpStatus.UNAUTHORIZED.value());

		data.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());

		if (exception.getMessage().contains("error code 49 - 80090308")) {
			data.put("message", WRONG_CREDENTIALS_ERROR_MESSAGE);
		} else {
			data.put("message", "Authentication Failed: " + exception.getMessage());
		}
		data.put("path", httpServletRequest.getRequestURI());

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		httpServletResponse.getOutputStream().println(objectMapper.writeValueAsString(data));
	}
}
