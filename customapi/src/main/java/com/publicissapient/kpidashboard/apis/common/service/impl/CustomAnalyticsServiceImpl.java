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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.common.service.CustomAnalyticsService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.common.service.UserLoginHistoryService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.model.rbac.RoleWiseProjects;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserTokenReopository;

import jakarta.servlet.http.HttpServletResponse;
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
	private static final String USER_AUTHORITIES = "authorities";
	private static final String USER_AUTH_TYPE = "authType";
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

	@Autowired
	private UserTokenReopository userTokenReopository;

	final ModelMapper modelMapper = new ModelMapper();

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> addAnalyticsData(HttpServletResponse httpServletResponse, String username) {
		Map<String, Object> userMap = new HashMap<>();
		httpServletResponse.setContentType("application/json");
		UserInfo userinfoKnowHow = userInfoRepository.findByUsername(username);
		httpServletResponse.setCharacterEncoding("UTF-8");
		Authentication authentication = authenticationRepository.findByUsername(username);
		userMap.put(USER_NAME, username);
		if (Objects.nonNull(userinfoKnowHow)) {
			String email = authentication == null ? userinfoKnowHow.getEmailAddress().toLowerCase()
					: authentication.getEmail().toLowerCase();
			userMap.put(USER_EMAIL, email);
			userMap.put(USER_ID, userinfoKnowHow.getId().toString());
			userMap.put(USER_AUTHORITIES, userinfoKnowHow.getAuthorities());
			userMap.put(USER_AUTH_TYPE, userinfoKnowHow.getAuthType());
			List<RoleWiseProjects> projectAccessesWithRole = projectAccessManager.getProjectAccessesWithRole(username);
			if (CollectionUtils.isNotEmpty(projectAccessesWithRole)) {
				userMap.put(PROJECTS_ACCESS, projectAccessesWithRole);
			} else {
				userMap.put(PROJECTS_ACCESS, new JSONArray());
			}
			userLoginHistoryService.createUserLoginHistoryInfo(userinfoKnowHow, SUCCESS);
		}
		userMap.put(AUTH_RESPONSE_HEADER, httpServletResponse.getHeader(AUTH_RESPONSE_HEADER));

		log.info("Successfully added Google Analytics data to Response.");
		return userMap;

	}

	@Override
	public JSONObject getAnalyticsCheck() {
		JSONObject json = new JSONObject();
		json.put("analyticsSwitch", settings.isAnalyticsSwitch());
		return json;
	}

}