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

package com.publicissapient.kpidashboard.apis.auth.standard;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.exceptions.PendingApprovalException;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;

@Component
public class StandardAuthenticationProvider implements AuthenticationProvider {

	private final AuthenticationService authService;
	private final AuthProperties authProperties;

	@Autowired
	public StandardAuthenticationProvider(AuthenticationService authService, AuthProperties authProperties) {
		this.authService = authService;
		this.authProperties = authProperties;
	}

	/**
	 * Performs Authentication
	 * 
	 * @param authentication
	 * @return Authentication
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException { // NOSONAR
																									   // //NOPMD
		try {
			Authentication auth = authService.authenticate(authentication.getName(),
					(String) authentication.getCredentials());
			authService.resetFailAttempts(authentication.getName());
			return auth;
		} catch (BadCredentialsException e) {
			DateTime now = DateTime.now(DateTimeZone.UTC);
			authService.updateFailAttempts(authentication.getName(), now);
			throw e;

		} catch (LockedException e) {
			String error = "User account is locked for " + authProperties.getAccountLockedPeriod() + " minutes";
			throw new LockedException(error, e);
		} catch (PendingApprovalException e) {
			throw new PendingApprovalException(e.getMessage());
		}

	}

	/**
	 * 
	 * @return true if this AuthenticationProvider supports theindicated
	 *         Authentication object.
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return StandardAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
