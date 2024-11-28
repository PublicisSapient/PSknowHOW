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

import java.time.LocalDateTime;

import com.publicissapient.kpidashboard.common.constant.AuthenticationEvent;
import com.publicissapient.kpidashboard.common.constant.Status;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UsersSession;

public interface UsersSessionService {

	/**
	 * Method to create user login history info
	 * 
	 * @param userInfo
	 *            user info
	 * @param status
	 *            event status {@link Status}
	 * @param event
	 *            authentication event {@link AuthenticationEvent}
	 * @return user login history
	 */
	UsersSession createUsersSessionInfo(UserInfo userInfo, AuthenticationEvent event, Status status);

	/**
	 * Method to get last logout of user
	 *
	 * @param username
	 *            username
	 * @return last login time
	 */
	LocalDateTime getLastLogoutTimeOfUser(String username);

	/**
	 * Method to audit the logout of user
	 * @param userName username
	 * @param status {@link Status}
	 */
	void auditLogout(String userName, Status status);
}
