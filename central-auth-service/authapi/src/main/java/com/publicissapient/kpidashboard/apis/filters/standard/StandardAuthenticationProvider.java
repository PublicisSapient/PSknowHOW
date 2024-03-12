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

package com.publicissapient.kpidashboard.apis.filters.standard;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.config.AuthProperties;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.errors.PendingApprovalException;
import com.publicissapient.kpidashboard.apis.service.UserService;

/**
 * Provides Standard Login Authentication Provider.
 *
 * @author Hiren Babariya
 */
@Component
public class StandardAuthenticationProvider implements AuthenticationProvider {

	private final UserService userService;
	private final AuthProperties authProperties;

	@Autowired
	public StandardAuthenticationProvider(UserService userService, AuthProperties authProperties) {
		this.userService = userService;
		this.authProperties = authProperties;
	}

	/**
	 * Performs Authentication
	 *
	 * @param authentication
	 * @return Authentication
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		try {
			Authentication auth = userService.authenticate(authentication, AuthType.STANDARD.name());
			userService.resetFailAttempts(authentication.getName());
			return auth;
		} catch (BadCredentialsException e) {
			userService.updateFailAttempts(authentication.getName(), LocalDateTime.now());
			throw e;
		} catch (LockedException e) {
			String error = "User account is locked for " + authProperties.getAccountLockedPeriod() + " minutes";
			throw new LockedException(error, e);
		} catch (PendingApprovalException e) {
			throw new PendingApprovalException(e.getMessage());
		}

	}

	/**
	 * @return true if this AuthenticationProvider supports theindicated
	 *         Authentication object.
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return StandardAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
