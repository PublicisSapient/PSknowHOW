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
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.UserLoginHistoryService;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UsersLoginHistory;
import com.publicissapient.kpidashboard.common.repository.rbac.UserLoginHistoryRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserLoginHistoryServiceImpl implements UserLoginHistoryService {

	@Autowired
	private UserLoginHistoryRepository userLoginHistoryRepository;

	/**
	 *
	 * @param userInfo
	 * @param status
	 * @return
	 */
	@Override
	public UsersLoginHistory createUserLoginHistoryInfo(UserInfo userInfo, String status) {
		UsersLoginHistory usersLoginHistoryInfo = new UsersLoginHistory();
		usersLoginHistoryInfo.setUserId(userInfo.getId());
		usersLoginHistoryInfo.setUserName(userInfo.getUsername());
		usersLoginHistoryInfo.setEmailId(userInfo.getEmailAddress());
		usersLoginHistoryInfo.setLoginType(userInfo.getAuthType().toString());
		usersLoginHistoryInfo.setStatus(status);
		LocalDateTime localDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		usersLoginHistoryInfo.setDateTime(localDateTime.format(formatter));
		return userLoginHistoryRepository.save(usersLoginHistoryInfo);
	}
}
