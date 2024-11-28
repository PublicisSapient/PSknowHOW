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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.constant.AuthenticationEvent;
import com.publicissapient.kpidashboard.common.constant.Status;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UsersLoginHistory;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserLoginHistoryRepository;

import jakarta.servlet.http.HttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class UserLoginHistoryServiceImplTest {

	@Mock
	private UserLoginHistoryRepository userLoginHistoryRepository;

	@InjectMocks
	private UserLoginHistoryServiceImpl userLoginHistoryService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private UserInfoRepository userInfoRepository;

	@Mock
	private Authentication authentication;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	public void testCreateUserLoginHistoryInfo_ValidInput_Success() {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(new ObjectId());
		userInfo.setUsername("username");
		userInfo.setEmailAddress("emailAdd");
		userInfo.setAuthType(AuthType.STANDARD);

		when(userLoginHistoryRepository.save(any(UsersLoginHistory.class))).thenReturn(new UsersLoginHistory());

		UsersLoginHistory result = userLoginHistoryService.createUserLoginHistoryInfo(userInfo,
				AuthenticationEvent.LOGIN, Status.SUCCESS);

		assertNotNull(result);
	}

	@Test
	public void testGetLastLogoutTimeOfUser_UserHasLogoutHistory_ReturnsLastLogoutTime() {
		LocalDateTime expectedLogoutTime = LocalDateTime.now().minusDays(1);
		UsersLoginHistory lastLogout = new UsersLoginHistory();
		lastLogout.setTimeStamp(expectedLogoutTime);

		when(userLoginHistoryRepository.findTopByUserNameAndEventOrderByTimeStampDesc(anyString(),
				any(AuthenticationEvent.class))).thenReturn(lastLogout);

		LocalDateTime actualLogoutTime = userLoginHistoryService.getLastLogoutTimeOfUser("username");

		assertEquals(expectedLogoutTime, actualLogoutTime);
	}

	@Test
	public void testGetLastLogoutTimeOfUser_UserHasNoLogoutHistory_ReturnsNull() {
		when(userLoginHistoryRepository.findTopByUserNameAndEventOrderByTimeStampDesc(anyString(),
				any(AuthenticationEvent.class))).thenReturn(null);

		LocalDateTime actualLogoutTime = userLoginHistoryService.getLastLogoutTimeOfUser("username");

		assertNull(actualLogoutTime);
	}

	@Test
	public void testAuditLogout() {
		// Arrange
		String userName = "testUser";
		UserInfo userInfo = new UserInfo();
		when(userInfoRepository.findByUsername(userName)).thenReturn(userInfo);
		when(userLoginHistoryRepository.save(any())).thenReturn(new UsersLoginHistory());

		// Act
		userLoginHistoryService.auditLogout(userName, Status.SUCCESS);

		// Assert
		verify(userInfoRepository, times(1)).findByUsername(userName);
		verify(userLoginHistoryRepository, times(1)).save(any());
	}

	@Test
	public void testAuditLogout_HeaderUsernameProvided_UserInfoExists() {
		// Arrange
		String userName = "testUser";
		UserInfo userInfo = new UserInfo();
		when(userInfoRepository.findByUsername(userName)).thenReturn(userInfo);
		when(userLoginHistoryRepository.save(any())).thenReturn(new UsersLoginHistory());
		// Act
		userLoginHistoryService.auditLogout("testUser", Status.SUCCESS);

		// Assert
		verify(userInfoRepository, times(1)).findByUsername(userName);
		verify(userLoginHistoryRepository, times(1)).save(any());
	}

	@Test
	public void testAuditLogout_HeaderUsernameProvided_UserInfoDoesNotExist() {
		// Arrange
		String userName = "testUser";
		when(userInfoRepository.findByUsername(userName)).thenReturn(null);
		// Act
		userLoginHistoryService.auditLogout("testUser", Status.SUCCESS);

		// Assert
		verify(userInfoRepository, times(1)).findByUsername(userName);
		verify(userLoginHistoryRepository, times(0)).save(any());
	}

	@Test
	public void testAuditLogout_UserInfoExists() {
		// Arrange
		String userName = "testUser";
		UserInfo userInfo = new UserInfo();
		when(userInfoRepository.findByUsername(userName)).thenReturn(userInfo);
		when(userLoginHistoryRepository.save(any())).thenReturn(new UsersLoginHistory());
		// Act
		userLoginHistoryService.auditLogout("testUser", Status.SUCCESS);

		// Assert
		verify(userInfoRepository, times(1)).findByUsername(userName);
		verify(userLoginHistoryRepository, times(1)).save(any());
	}
}