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

import com.publicissapient.kpidashboard.apis.entity.UserToken;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.common.model.ServiceResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

import java.util.Date;
import java.util.List;

/**
 * A Contract to add and get authentication.
 *
 * @author anisingh4
 */
public interface TokenAuthenticationService {
	String saveSamlData(Saml2AuthenticatedPrincipal principal, HttpServletResponse response);

	String createApplicationJWT(@NotNull String subject, AuthType authType);

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