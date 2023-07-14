/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.projectconfig.basic.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.tracelog.DeleteProjectTraceLog;
import com.publicissapient.kpidashboard.common.repository.tracelog.DeleteProjectTraceLogRepository;

/**
 * @author anisingh4
 */
@Service
public class DeleteProjectTraceLogServiceImpl implements DeleteProjectTraceLogService {

	@Autowired
	private DeleteProjectTraceLogRepository deleteProjectTraceLogRepository;

	@Autowired
	private AuthenticationService authenticationService;

	@Override
	public void save(ProjectBasicConfig projectBasicConfig) {
		String loggedInUser = authenticationService.getLoggedInUser();
		DeleteProjectTraceLog traceLog = createTraceLog(projectBasicConfig, loggedInUser);

		deleteProjectTraceLogRepository.save(traceLog);
	}

	private DeleteProjectTraceLog createTraceLog(ProjectBasicConfig projectBasicConfig, String username) {
		DeleteProjectTraceLog traceLog = new DeleteProjectTraceLog();
		traceLog.setDeletedBy(username);
		traceLog.setProjectBasicConfig(projectBasicConfig);
		traceLog.setDeletionDate(LocalDateTime.now());
		return traceLog;
	}

}
