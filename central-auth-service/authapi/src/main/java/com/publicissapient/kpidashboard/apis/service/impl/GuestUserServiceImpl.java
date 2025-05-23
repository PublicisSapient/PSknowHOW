/*
 *  Copyright 2024 <Sapient Corporation>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the
 *  License.
 */

package com.publicissapient.kpidashboard.apis.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.service.GuestUserService;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class GuestUserServiceImpl implements GuestUserService {

	private static final String ROLE_GUEST = "GUEST";

	private final TokenAuthenticationService tokenAuthenticationService;

	@Override
	public void loginUserAsGuest(String guestDisplayName, HttpServletResponse response) {
		String jwt = tokenAuthenticationService.createJWT(UUID.randomUUID().toString(), AuthType.STANDARD,
				tokenAuthenticationService.createAuthorities(List.of(ROLE_GUEST)));
		tokenAuthenticationService.addGuestCookies(guestDisplayName, jwt, response);
	}

	@Override
	public void logoutGuestUser(HttpServletRequest request, HttpServletResponse response) {
		tokenAuthenticationService.deleteGuestCookies(request, response);
	}
}
