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

import lombok.AllArgsConstructor;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.service.StandardAuthenticationService;

@Component
@AllArgsConstructor
public class StandardAuthenticationProvider implements AuthenticationProvider {
	private final StandardAuthenticationService standardAuthenticationService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		try {
			return standardAuthenticationService.authenticateUser(authentication);
		} catch (BadCredentialsException e) {
			standardAuthenticationService.updateFailAttempts(authentication.getName(), LocalDateTime.now());
			throw e;
		} catch (Exception e) {
			throw new BadCredentialsException("Invalid username or password");
		}
	}

	// TODO: what is this?
	/**
	 * @return true if this AuthenticationProvider supports the indicated Authentication object.
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return StandardAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
