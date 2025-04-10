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

package com.publicissapient.kpidashboard.apis.service;

import com.publicissapient.kpidashboard.apis.enums.AuthType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public interface TokenAuthenticationService {

	String createJWT(@NotNull String subject, AuthType authType, Collection<? extends GrantedAuthority> authorities);

	// will create the two cookies: authCookie & authCookie_EXPIRY
	void addStandardCookies(String jwt, HttpServletResponse response);

	// will create the three cookies: authCookie & authCookie_EXPIRY &
	// samlUsernameCookie
	void addSamlCookies(String username, String jwt, HttpServletResponse response);

	String getSubject(String token);

	Collection<GrantedAuthority> createAuthorities(List<String> roles);

	String extractUsernameFromAuthentication(Authentication authentication);

	// will create the following cookies: authCookie, authCookie_EXPIRY and guestDisplayName
	void addGuestCookies(String guestDisplayName, String jwt, HttpServletResponse response);

	// will delete the following cookies: authCookie, authCookie_EXPIRY and guestDisplayName
	void deleteGuestCookies(HttpServletRequest request, HttpServletResponse response);
}
