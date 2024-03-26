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

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;

import com.publicissapient.kpidashboard.apis.entity.UserToken;
import com.publicissapient.kpidashboard.common.model.ServiceResponse;

/**
 * A Contract to add and get authentication.
 *
 * @author Hiren Babariya
 */
public interface TokenAuthenticationService {

	/**
	 * Add authentication.
	 *
	 * @param response
	 *            the response
	 * @param authentication
	 *            the authentication
	 */
	String addAuthentication(HttpServletResponse response, Authentication authentication);

	/**
	 * Gets authentication.
	 *
	 * @param request
	 *            the request
	 * @return the authentication
	 */
	Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response);

	String setUpdateAuthFlag(List<UserToken> userTokenDataList, Date tokenExpiration);

	String getSubject(String token);

	Object getClaim(String token, String claimKey);

	void updateExpiryDate(String username, String expiryDate);

	UserToken getLatestTokenByUser(String userName);

	/**
	 * generate And SaveToken for every client/resource system
	 * 
	 * @param resource
	 * @return
	 */
	ServiceResponse<?> generateAndSaveToken(String resource);

}