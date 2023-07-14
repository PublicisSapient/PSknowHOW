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

package com.publicissapient.kpidashboard.apis.auth.apitoken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.auth.service.ApiTokenService;

/**
 * Api specific implementation of {@link AuthenticationProvider}
 */
@Component
public class ApiTokenAuthenticationProvider implements AuthenticationProvider {

	private final ApiTokenService apiTokenService;

	/**
	 * Class constructor specifying ApiTokenService
	 * 
	 * @param apiTokenService
	 */
	@Autowired
	public ApiTokenAuthenticationProvider(ApiTokenService apiTokenService) {
		this.apiTokenService = apiTokenService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException { // NOSONAR
																									   // //NOPMD
		return apiTokenService.authenticate(authentication.getName(), (String) authentication.getCredentials());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return ApiTokenAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
