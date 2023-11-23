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

package com.publicissapient.kpidashboard.apis.abac;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.abac.policy.PolicyEnforcement;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;

@Component
public class ContextAwarePolicyEnforcement {
	@Autowired
	protected PolicyEnforcement policy;

	@Autowired
	private UserInfoServiceImpl userInfoService;

	@Autowired
	private ProjectAccessManager projectAccessManager;

	@Autowired
	private AuthenticationService authenticationService;

	public void checkPermission(Object resource, String permission) {

		Map<String, Object> environment = new HashMap<>();

		environment.put("time", new Date());

		UserInfo user = userInfoService.getUserInfo(authenticationService.getLoggedInUser());

		if (!policy.check(projectAccessManager, user, resource, permission, environment))
			throw new AccessDeniedException("Access is denied");
	}
}
