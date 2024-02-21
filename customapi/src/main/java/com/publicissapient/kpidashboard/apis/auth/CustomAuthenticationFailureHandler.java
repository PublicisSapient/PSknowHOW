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

package com.publicissapient.kpidashboard.apis.auth;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.common.service.UserLoginHistoryService;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This class will handle authentication failed exception like Bad credentials
 *
 * @author anisingh4
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
	private static final String WRONGCREDENTIALS = "Login Failed: The username or password entered is incorrect";
	public static final String USERNAME = "username";
	public static final String FAIL = "FAIL";
	@Autowired
	private UserLoginHistoryService userLoginHistoryService;
	@Autowired
	private UserInfoRepository userInfoRepository;
	@Override
	public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			AuthenticationException exception) throws IOException, ServletException {
		httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
		httpServletResponse.setContentType("application/json");

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("timestamp", Calendar.getInstance().getTime());

		data.put("status", HttpStatus.UNAUTHORIZED.value());

		data.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
		if (exception.getMessage().contains("error code 49 - 80090308")) {
			data.put("message", "Authentication Failed: " + WRONGCREDENTIALS);
		} else {
			data.put("message", "Authentication Failed: " + exception.getMessage());
		}
		data.put("path", httpServletRequest.getRequestURI());

		String username = httpServletRequest.getParameter(USERNAME);
		UserInfo userinfo = userInfoRepository.findByUsername(username);
		if(userinfo != null) {
			userLoginHistoryService.createUserLoginHistoryInfo(userinfo, FAIL);
		}

		ObjectMapper objectMapper = new ObjectMapper();
		httpServletResponse.getOutputStream().println(objectMapper.writeValueAsString(data));
	}
}
