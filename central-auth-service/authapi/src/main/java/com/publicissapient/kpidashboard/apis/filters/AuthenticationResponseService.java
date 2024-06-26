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

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;

import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.common.model.LoginResponse;

/**
 * Interface to handle authentication service response.
 *
 * @author Hiren Babariya
 */
public interface AuthenticationResponseService {

	/**
	 * handle authentication response.
	 * 
	 * @param response
	 * @param authentication
	 */
	String handle(HttpServletResponse response, Authentication authentication, AuthType authType);

	LoginResponse createLoginResponse(Authentication authentication);

}
