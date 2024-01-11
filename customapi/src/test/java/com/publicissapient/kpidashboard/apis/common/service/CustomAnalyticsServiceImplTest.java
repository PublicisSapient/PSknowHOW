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

package com.publicissapient.kpidashboard.apis.common.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.common.service.impl.CustomAnalyticsServiceImpl;
import com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.RoleWiseProjects;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

@RunWith(MockitoJUnitRunner.class)
public class CustomAnalyticsServiceImplTest {

	@Mock
	UserAuthorizedProjectsService userAuthorizedProjectsService;
	Authentication authentication;
	UserInfo user;
	RoleWiseProjects roleWiseProjects;
	List<RoleWiseProjects> listRoleWiseProjects = new ArrayList<>();
	@InjectMocks
	private CustomAnalyticsServiceImpl customAnalyticsServiceImpl;
	@Mock
	private UserInfoRepository userInfoRepository;
	@Mock
	private AuthenticationRepository authenticationRepository;
	@Mock
	private CustomApiConfig customAPISettings;
	@Mock
	private ProjectAccessManager projectAccessManager;
	@Mock
	private UserInfoServiceImpl service;
	@Mock
	private UserLoginHistoryService userLoginHistoryService;
	@Test
	public void testAddAnalyticsData() {
		HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
		user = new UserInfo();
		user.setUsername("user");
		user.setAuthType(AuthType.STANDARD);
		user.setAuthorities(Lists.newArrayList("ROLE_VIEWER"));
		user.setId(new ObjectId("6373796960277453212bc610"));
		authentication = new Authentication();
		authentication.setEmail("email");
		roleWiseProjects = new RoleWiseProjects();

		when(userInfoRepository.findByUsername(Mockito.anyString())).thenReturn(user);
		when(authenticationRepository.findByUsername(Mockito.anyString())).thenReturn(authentication);
		when(projectAccessManager.getProjectAccessesWithRole(Mockito.anyString())).thenReturn(listRoleWiseProjects);
		JSONObject json = customAnalyticsServiceImpl.addAnalyticsData(resp, "test");
		assertEquals("test", json.get("user_name"));
		assertEquals(json.get("authorities"), user.getAuthorities());

	}

	@Test
	public void getAnalyticsSwitch() {
		when(customAPISettings.isAnalyticsSwitch()).thenReturn(true);
		JSONObject json = customAnalyticsServiceImpl.getAnalyticsCheck();
		assertEquals(true, json.get("analyticsSwitch"));
	}
}
