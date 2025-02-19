/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2024 Sapient Corporation.
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

package com.publicissapient.kpidashboard.apis.util;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl;
import com.publicissapient.kpidashboard.common.model.connection.Connection;

@Component
public class ProjectAccessUtil {
	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;
	@Autowired
	private UserAuthorizedProjectsService userAuthorizedProjectsService;
	@Autowired
	private UserInfoServiceImpl userInfoService;

	@Autowired
	private AuthenticationService authenticationService;

	public boolean configIdHasUserAccess(String basicConfigId) {
		Set<String> basicProjectConfigIds = tokenAuthenticationService.getUserProjects();
		return userAuthorizedProjectsService.ifSuperAdminUser() ||
				(Optional.ofNullable(basicProjectConfigIds).isPresent() && basicProjectConfigIds.contains(basicConfigId));
	}

	public boolean ifConnectionNotAccessible(Connection connection) {
		return !connection.isSharedConnection() &&
				(!(connection.getCreatedBy().equals(authenticationService.getLoggedInUser()) ||
						userAuthorizedProjectsService.ifSuperAdminUser()));
	}
}
