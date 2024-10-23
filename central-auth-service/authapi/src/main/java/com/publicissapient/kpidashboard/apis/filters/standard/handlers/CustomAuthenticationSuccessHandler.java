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

package com.publicissapient.kpidashboard.apis.filters.standard.handlers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.filters.standard.service.AuthenticationResponseService;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	private static final String USER_NAME = "user_name";
	private static final String USER_EMAIL = "user_email";
	private static final String USER_ID = "user_id";
	private static final String USER_TYPE = "user_type";

	private final AuthenticationResponseService authenticationResponseService;

	private final TokenAuthenticationService tokenAuthenticationService;

	private final UserService userService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		authenticationResponseService.handle(response, authentication);

		// sgu106: Google Analytics data population starts
		String username = tokenAuthenticationService.extractUsernameFromAuthentication(authentication);
		JSONObject json = loginJsonData(response, username);
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.print(json.toJSONString());
		// sgu106: Google Analytics data population ends
	}

	public JSONObject loginJsonData(HttpServletResponse httpServletResponse, String username) {
		JSONObject json = new JSONObject();

		httpServletResponse.setContentType("application/json");
		httpServletResponse.setCharacterEncoding("UTF-8");

		Optional<User> userinfo = userService.findByUsername(username);

		if (userinfo.isPresent()) {
			json.put(USER_NAME, username);
			json.put(USER_EMAIL, userinfo.get().getEmail());
			json.put(USER_TYPE, userinfo.get().getAuthType());
			json.put(USER_ID, userinfo.get().getId().toString());
		}

		return json;
	}
}
