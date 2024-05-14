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

package com.publicissapient.kpidashboard.apis.filters;

import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.UserRoleService;
import com.publicissapient.kpidashboard.common.model.LoginResponse;
import com.publicissapient.kpidashboard.common.model.UserDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * This class call repository method to save the user authentication.
 *
 */
@Service
@AllArgsConstructor
@Slf4j
public class DefaultAuthenticationResponseService implements AuthenticationResponseService {
	private final TokenAuthenticationService tokenAuthenticationService;

	private final UserRoleService userRoleService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String handle(HttpServletResponse response, org.springframework.security.core.Authentication authentication,
						 AuthType authType) {

		UserDTO auth = (UserDTO) authentication.getPrincipal();
		Collection<GrantedAuthority> authorities = userRoleService.getAuthorities(auth.getUsername());
		AbstractAuthenticationToken authenticationWithAuthorities = new UsernamePasswordAuthenticationToken(
				authentication.getPrincipal(), authentication.getCredentials(), authorities);
		authenticationWithAuthorities.setDetails(authType);
		return tokenAuthenticationService.addAuthentication(response, authenticationWithAuthorities);
	}

	// Creating Login Response
	@Override
	public LoginResponse createLoginResponse(org.springframework.security.core.Authentication authentication) {
		UserDTO auth = (UserDTO) authentication.getPrincipal();
		String username = auth.getUsername();
		LoginResponse loginResponse = new LoginResponse();
		loginResponse.setId(auth.getId());
		loginResponse.setUsername(username);
		loginResponse.setEmail(auth.getEmail().toLowerCase());
		loginResponse.setFirstName(auth.getFirstName());
		loginResponse.setLastName(auth.getLastName());
		loginResponse.setDisplayName(auth.getDisplayName());
		return loginResponse;
	}
}
