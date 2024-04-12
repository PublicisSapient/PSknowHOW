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

package com.publicissapient.kpidashboard.apis.auth;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.common.constant.AuthType;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * This class call repository method to save the user authentication.
 *
 * @author prijain3
 *
 */
@Component
@Slf4j
public class DefaultAuthenticationResponseService implements AuthenticationResponseService {

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;

	@Autowired
	private UserInfoService userInfoService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(HttpServletResponse response, Authentication authentication) {
		String username = authentication.getPrincipal().toString();

		Collection<GrantedAuthority> authorities = userInfoService.getAuthorities(username);
		AbstractAuthenticationToken authenticationWithAuthorities = new UsernamePasswordAuthenticationToken(
				authentication.getPrincipal(), authentication.getCredentials(), authorities);
		authenticationWithAuthorities.setDetails(AuthType.STANDARD);
		tokenAuthenticationService.addAuthentication(response, authenticationWithAuthorities);

	}

}