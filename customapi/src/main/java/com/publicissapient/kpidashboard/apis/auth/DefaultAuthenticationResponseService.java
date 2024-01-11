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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.auth.model.CustomUserDetails;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;

import jakarta.servlet.http.HttpServletResponse;

/**
 * This class call repository method to save the user authentication.
 * 
 * @author prijain3
 *
 */
@Component
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
		String emailAddress = StringUtils.EMPTY;
		String username;

		if (authentication.getPrincipal() instanceof CustomUserDetails) {
			emailAddress = ((CustomUserDetails) authentication.getPrincipal()).getEmailAddress();
			username = ((CustomUserDetails) authentication.getPrincipal()).getUsername();

		} else {
			username = authentication.getPrincipal().toString();
		}

		AuthType authType = authentication.getDetails() == null ? AuthType.STANDARD
				: (AuthType) authentication.getDetails();

		if (authType.equals(AuthType.LDAP) && userInfoService.getUserInfo(username) == null) {
			UserInfo defaultUserInfo = userInfoService.createDefaultUserInfo(username, authType, emailAddress);
			userInfoService.save(defaultUserInfo);
		}

		Collection<GrantedAuthority> authorities = userInfoService.getAuthorities(username);
		AbstractAuthenticationToken authenticationWithAuthorities = new UsernamePasswordAuthenticationToken(
				authentication.getPrincipal(), authentication.getCredentials(), authorities);
		authenticationWithAuthorities.setDetails(authType);
		tokenAuthenticationService.addAuthentication(response, authenticationWithAuthorities);

	}

}
