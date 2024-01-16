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

package com.publicissapient.kpidashboard.apis.auth.token;

import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
import org.springframework.security.core.Authentication;

import com.publicissapient.kpidashboard.common.model.rbac.RoleWiseProjects;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UserTokenData;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A Contract to add and get authentication.
 *
 * @author anisingh4
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
	void addAuthentication(HttpServletResponse response, Authentication authentication);

	/**
	 * Gets authentication.
	 *
	 * @param request
	 *            the request
	 * @return the authentication
	 */
	Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response);

	/**
	 * This method returns Projects related to user
	 * 
	 * @return set of projects
	 */
	Set<String> getUserProjects();

	/*
	 * This method refresh the token with new project and save in db
	 * 
	 * @param HttpServletRequest
	 * 
	 * @param HttpServletResponse
	 * 
	 * @return List<ProjectsAccess>
	 */
	List<RoleWiseProjects> refreshToken(HttpServletRequest req, HttpServletResponse resp);

	/**
	 * Invalidate(Remove) auth tokens for provided users
	 * 
	 * @param users
	 *            list of users (usernames)
	 */
	void invalidateAuthToken(List<String> users);

	void updateExpiryDate(String username, String expiryDate);

	String setUpdateAuthFlag(List<UserTokenData> userTokenData);

	JSONObject getOrSaveUserByToken(HttpServletRequest request, Authentication authentication);

	JSONObject createAuthDetailsJson(UserInfo userInfo);
}
