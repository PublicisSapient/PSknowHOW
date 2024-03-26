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
package com.publicissapient.kpidashboard.apis.filters.saml;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.service.UserService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides for saml login user service .
 *
 * @author Hiren Babariya
 */
@Slf4j
@AllArgsConstructor
public class SamlUserService implements AuthenticationManager {

	private final UserService userTransformerService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		try {
			return userTransformerService.authenticate(authentication, AuthType.SAML.name());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return authentication;
		}
	}
}
