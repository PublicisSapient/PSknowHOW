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

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.UsersSessionService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.constant.AuthenticationEvent;
import com.publicissapient.kpidashboard.common.constant.Status;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UsersSession;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UsersSessionRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UsersSessionServiceImpl implements UsersSessionService {

	@Autowired
	private UsersSessionRepository usersSessionRepository;
	@Autowired
	private UserInfoRepository userInfoRepository;
	@Autowired
	private CustomApiConfig customApiConfig;

	/**
	 * Method to create user login history info
	 *
	 * @param userInfo
	 *            user info
	 * @param status
	 *            event status
	 * @param event
	 *            authentication event
	 * @return user login history
	 */
	@Override
	public UsersSession createUsersSessionInfo(UserInfo userInfo, AuthenticationEvent event, Status status) {
		UsersSession usersSessionInfo = new UsersSession();
		usersSessionInfo.setUserId(userInfo.getId());
		usersSessionInfo.setUserName(userInfo.getUsername());
		usersSessionInfo.setEmailId(userInfo.getEmailAddress());
		usersSessionInfo.setAuthType(userInfo.getAuthType());
		usersSessionInfo.setEvent(event);
		usersSessionInfo.setStatus(status);
		usersSessionInfo.setTimeStamp(LocalDateTime.now());
		usersSessionInfo.setExpiresOn(LocalDateTime.now().plusMonths(customApiConfig.getUserSessionsExpiresOn()));
		return usersSessionRepository.save(usersSessionInfo);
	}

	/**
	 * Method to get last logout of user
	 *
	 * @param username
	 *            username
	 * @return last login time
	 */
	@Override
	public LocalDateTime getLastLogoutTimeOfUser(String username) {
		UsersSession lastLogout = usersSessionRepository
				.findTopByUserNameAndEventOrderByTimeStampDesc(username, AuthenticationEvent.LOGOUT);
		return lastLogout != null ? lastLogout.getTimeStamp() : null;
	}

	/**
	 * Audit logout.
	 *
	 * @param userName
	 *            the userName
	 * @param status
	 *            the status {@link Status}
	 */
	@Override
	public void auditLogout(String userName, Status status) {
		UserInfo userinfo = userInfoRepository.findByUsername(userName);

		if (userinfo != null) {
			this.createUsersSessionInfo(userinfo, AuthenticationEvent.LOGOUT, status);
		}
	}

}
