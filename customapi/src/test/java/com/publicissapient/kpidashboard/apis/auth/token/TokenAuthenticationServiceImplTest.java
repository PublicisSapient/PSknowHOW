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

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.publicissapient.kpidashboard.common.constant.AuthType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.AuthenticationFixture;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.common.model.rbac.AccessItem;
import com.publicissapient.kpidashboard.common.model.rbac.AccessNode;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsAccess;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsForAccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.RoleWiseProjects;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UserTokenData;
import com.publicissapient.kpidashboard.common.repository.rbac.UserTokenReopository;

@RunWith(MockitoJUnitRunner.class)
public class TokenAuthenticationServiceImplTest {

	private static final String USERNAME = "username";
	private static final String AUTHORIZATION = "Authorization";
	private static final String AUTH_PREFIX_W_SPACE = "Bearer ";
	private static final String AUTH_RESPONSE_HEADER = "X-Authentication-Token";

	@InjectMocks
	private TokenAuthenticationServiceImpl service;

	@Mock
	private AuthProperties tokenAuthProperties;

	@Mock
	private HttpServletResponse response;

	@Mock
	private HttpServletRequest request;

	@Mock
	UserTokenReopository userTokenReopository;

	@Mock
	Authentication authentication;

	@Mock
	SecurityContext securityContext;

	@Mock
	private UserInfoService userInfoService;

	@Mock
	private ProjectAccessManager projectAccessManager;

	@Mock
	private AuthenticationService authenticationService;

	@Mock
	private CookieUtil cookieUtil;

	@Mock
	private Cookie cookie;

	List<AccessNode> listAccessNode = new ArrayList<>();


	AccessNode accessNodes;
	AccessItem accessItem;
	List<AccessItem> accessItems = new ArrayList<>();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		SecurityContextHolder.clearContext();
		when(cookieUtil.getAuthCookie(any(HttpServletRequest.class)))
				.thenReturn(new Cookie("authCookie", AuthenticationFixture.getJwtToken(USERNAME, "userTokenData", 100000L)));

	}

	@Test
	public void testAddAuthentication() {
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(tokenAuthProperties.getExpirationTime()).thenReturn(0l);
		when(tokenAuthProperties.getSecret()).thenReturn("userTokenData");

		service.addAuthentication(response, AuthenticationFixture.getAuthentication(USERNAME));
		verify(response).addHeader(eq(AUTH_RESPONSE_HEADER), anyString());
	}

	@Test
	public void testGetAuthentication_noHeader() {

		assertNull(service.getAuthentication(request, response));
	}

	@Test
	public void testGetAuthentication_expiredToken() {
		assertNull(service.getAuthentication(request, response));
	}

	@Test
	public void testGetAuthentication() {
		when(tokenAuthProperties.getSecret()).thenReturn("userTokenData");
		when(userTokenReopository.findByUserToken(anyString())).thenReturn(new UserTokenData());
		Authentication result = service.getAuthentication(request, response);
		assertNotNull(result);
	}

	@Test
	public void validateGetUserProjects() {
		ProjectsForAccessRequest par = new ProjectsForAccessRequest();
		par.setProjectId("pID");
		List<ProjectsForAccessRequest> parList = new ArrayList<>();
		parList.add(par);
		accessItem = new AccessItem();
		accessItem.setItemId("itemId");
		accessItem.setItemName("itemName");
		accessItems.add(accessItem);

		accessNodes = new AccessNode();
		accessNodes.setAccessLevel("accessLevel");
		accessNodes.setAccessItems(accessItems);
		listAccessNode.add(accessNodes);
		ProjectsAccess pa = new ProjectsAccess();
		pa.setAccessNodes(listAccessNode);
		pa.setRole("Role");

		List<ProjectsAccess> paList = new ArrayList<>();
		paList.add(pa);
		UserInfo testUser = new UserInfo();
		testUser.setUsername("User");
		testUser.setProjectsAccess(paList);

		SecurityContextHolder.setContext(securityContext);
		when(authenticationService.getLoggedInUser()).thenReturn("SUPERADMIN");
		Set<String> result = service.getUserProjects();
		assertNotNull(result);
	}

	@Test
	public void validateRefreshToken() {
		ProjectsForAccessRequest par = new ProjectsForAccessRequest();
		par.setProjectId("pID");
		List<ProjectsForAccessRequest> parList = new ArrayList<>();
		parList.add(par);

		accessItem = new AccessItem();
		accessItem.setItemId("itemId");
		accessItem.setItemName("itemName");
		accessItems.add(accessItem);

		accessNodes = new AccessNode();
		accessNodes.setAccessLevel("accessLevel");
		accessNodes.setAccessItems(accessItems);
		listAccessNode.add(accessNodes);
		ProjectsAccess pa = new ProjectsAccess();
		pa.setAccessNodes(listAccessNode);
		pa.setRole("Role");

		List<ProjectsAccess> paList = new ArrayList<>();
		paList.add(pa);
		UserInfo testUser = new UserInfo();
		testUser.setUsername("User");
		testUser.setProjectsAccess(paList);
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);

		SecurityContextHolder.setContext(securityContext);
		when(authenticationService.getLoggedInUser()).thenReturn("SUPERADMIN");
		List<RoleWiseProjects> result = service.refreshToken(request, response);
		assertEquals(result.size(), 0);
	}

	@Test
	public void invalidateAuthToken(){

		List<String> users = Arrays.asList("Test");
		doNothing().when(userTokenReopository).deleteByUserNameIn(users);

		service.invalidateAuthToken(users);
		verify(userTokenReopository, times(1)).deleteByUserNameIn(users);
	}

	@Test
	public void setUpdateAuthFlagForExpDateNull() {
		UserTokenData userTokenData = new UserTokenData(USERNAME, "userTokenData", null);
		assertEquals(service.setUpdateAuthFlag(userTokenData), Boolean.toString(false));
	}

	@Test
	public void getOrSaveUserByToken() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		UserTokenData userTokenData = new UserTokenData(USERNAME, cookieUtil.getAuthCookie(request).getValue(),
				"2023-01-19T12:33:14.013");
		accessItem = new AccessItem();
		accessItem.setItemId("itemId");
		accessItem.setItemName("itemName");
		accessItems.add(accessItem);

		accessNodes = new AccessNode();
		accessNodes.setAccessLevel("accessLevel");
		accessNodes.setAccessItems(accessItems);
		listAccessNode.add(accessNodes);
		ProjectsAccess pa = new ProjectsAccess();
		pa.setAccessNodes(listAccessNode);
		pa.setRole("Role");

		List<ProjectsAccess> paList = new ArrayList<>();
		paList.add(pa);
		UserInfo testUser = new UserInfo();
		Object auth = "STANDARD";
		testUser.setUsername(USERNAME);
		testUser.setProjectsAccess(paList);
		when(userTokenReopository.findByUserToken(cookieUtil.getAuthCookie(request).getValue()))
				.thenReturn(userTokenData);
		when(authentication.getDetails()).thenReturn(auth);
		when(userInfoService.getOrSaveUserInfo(USERNAME, AuthType.STANDARD, new ArrayList<>())).thenReturn(testUser);
		assertEquals(service.getOrSaveUserByToken(request, authentication), testUser);
		;
	}

	@Test
	public void getOrSaveUserByTokenNull() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		Object auth = "STANDARD";
		when(authentication.getDetails()).thenReturn(auth);
		when(userTokenReopository.findByUserToken(cookieUtil.getAuthCookie(request).getValue())).thenReturn(null);
		when(authenticationService.getLoggedInUser()).thenReturn(USERNAME);
		service.getOrSaveUserByToken(request, authentication);
		verify(userTokenReopository, times(1))
				.save(new UserTokenData(USERNAME, cookieUtil.getAuthCookie(request).getValue(), null));
	}

}
