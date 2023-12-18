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

package com.publicissapient.kpidashboard.apis.common.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.common.service.CustomAnalyticsService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.common.service.UserLoginHistoryService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.model.rbac.CentralUserInfoDTO;
import com.publicissapient.kpidashboard.common.model.rbac.RoleWiseProjects;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Implements CustomAnalyticsService interface.
 *
 * @author prijain3
 *
 */
@Service
@Slf4j
public class CustomAnalyticsServiceImpl implements CustomAnalyticsService {
	private static final String USER_NAME = "user_name";
	private static final String USER_EMAIL = "user_email";
	private static final String USER_ID = "user_id";
	private static final String PROJECTS_ACCESS = "projectsAccess";
	private static final String AUTH_RESPONSE_HEADER = "X-Authentication-Token";
	private static final Object USER_AUTHORITIES = "authorities";
	public static final String SUCCESS = "SUCCESS";

	@Autowired
	UserAuthorizedProjectsService userAuthorizedProjectsService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	private UserInfoRepository userInfoRepository;
	@Autowired
	private AuthenticationRepository authenticationRepository;
	@Autowired
	private CustomApiConfig settings;
	@Autowired
	private ProjectAccessManager projectAccessManager;
	@Autowired
	private UserLoginHistoryService userLoginHistoryService;

	final ModelMapper modelMapper = new ModelMapper();

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject addAnalyticsData(HttpServletResponse httpServletResponse, String username, String authToken) {
		JSONObject json = new JSONObject();
		httpServletResponse.setContentType("application/json");
		UserInfo userinfoKnowHow = userInfoRepository.findByUsername(username);
		httpServletResponse.setCharacterEncoding("UTF-8");
		if (Objects.isNull(userinfoKnowHow)) {
			CentralUserInfoDTO centralUserInfoDTO = userInfoService.getCentralAuthUserInfoDetails(username, authToken);
			UserInfo centralUserInfo = new UserInfo();
			if (Objects.nonNull(centralUserInfoDTO)) {
				setUserDetailsFromCentralAuth(username, centralUserInfoDTO, centralUserInfo);
				userinfoKnowHow = centralUserInfo;
				Authentication authenticationCentral = new Authentication();
				setAuthenticationFromCentralAuth(username, centralUserInfoDTO, authenticationCentral);
			}
		}
		Authentication authentication = authenticationRepository.findByUsername(username);
		json.put(USER_NAME, username);
		if (Objects.nonNull(userinfoKnowHow)) {
			String email = authentication == null ? userinfoKnowHow.getEmailAddress().toLowerCase() : authentication.getEmail().toLowerCase();
			json.put(USER_EMAIL, email);
			json.put(USER_ID, userinfoKnowHow.getId().toString());
			json.put(USER_AUTHORITIES, userinfoKnowHow.getAuthorities());
			Gson gson = new Gson();
			List<RoleWiseProjects> projectAccessesWithRole = projectAccessManager.getProjectAccessesWithRole(username);
			if (projectAccessesWithRole != null) {
				JsonElement element = gson.toJsonTree(projectAccessesWithRole, new TypeToken<List<RoleWiseProjects>>() {
				}.getType());
				json.put(PROJECTS_ACCESS, element.getAsJsonArray());
			} else {
				json.put(PROJECTS_ACCESS, new JSONArray());
			}
			userLoginHistoryService.createUserLoginHistoryInfo(userinfoKnowHow, SUCCESS);
		}
		json.put(AUTH_RESPONSE_HEADER, httpServletResponse.getHeader(AUTH_RESPONSE_HEADER));

		log.info("Successfully added Google Analytics data to Response.");
		return json;

	}

	/**
	 *
	 * @param username
	 * @param centralUserInfoDTO
	 * @param authenticationCentral
	 */
	private void setAuthenticationFromCentralAuth(String username, CentralUserInfoDTO centralUserInfoDTO,
			Authentication authenticationCentral) {
		authenticationCentral.setUsername(username);
		authenticationCentral.setPassword(centralUserInfoDTO.getPassword());
		authenticationCentral.setApproved(centralUserInfoDTO.isApproved());
		authenticationCentral.setEmail(centralUserInfoDTO.getEmail());
		authenticationCentral.setLastUnsuccessfulLoginTime(centralUserInfoDTO.getLastUnsuccessfulLoginTime());
		authenticationCentral.setUserRole(Constant.ROLE_VIEWER);
		authenticationCentral.setLoginAttemptCount(centralUserInfoDTO.getLoginAttemptCount());
		authenticationRepository.save(authenticationCentral);
	}

	/**
	 *
	 * @param username
	 * @param centralUserInfoDTO
	 * @param centralUserInfo
	 */
	private void setUserDetailsFromCentralAuth(String username, CentralUserInfoDTO centralUserInfoDTO,
			UserInfo centralUserInfo) {
		centralUserInfo.setUsername(username);
		centralUserInfo.setAuthType(centralUserInfoDTO.getAuthType());
		centralUserInfo.setAuthorities(Collections.singletonList(Constant.ROLE_VIEWER));
		centralUserInfo.setProjectsAccess(Collections.emptyList());
		centralUserInfo.setEmailAddress(centralUserInfoDTO.getEmail().toLowerCase());
		centralUserInfo.setFirstName(centralUserInfoDTO.getFirstName());
		centralUserInfo.setLastName(centralUserInfoDTO.getLastName());
		centralUserInfo.setDisplayName(centralUserInfoDTO.getDisplayName());
		centralUserInfo.setCreatedOn((new Date()).toString());
		userInfoRepository.save(centralUserInfo);
	}

	@Override
	public JSONObject getAnalyticsCheck() {
		JSONObject json = new JSONObject();
		json.put("analyticsSwitch", settings.isAnalyticsSwitch());
		return json;
	}

}