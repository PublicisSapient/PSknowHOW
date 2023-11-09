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

package com.publicissapient.kpidashboard.apis.sprinttracelog.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.SprintTraceLog;
import com.publicissapient.kpidashboard.common.repository.application.SprintTraceLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SprintTraceLogServiceImpl implements SprintTraceLogService {
	@Autowired
	SprintTraceLogRepository sprintTraceLogRepository;

	@Override
	public ServiceResponse getActiveSprintFetchStatus(String sprintId) {
		// validating sprintId is string & not empty
		if (StringUtils.isEmpty(sprintId)) {
			log.info("sprintId is empty");
			return new ServiceResponse(false, "sprintId cannot be empty", null);
		}
		// fetching the latest record from db
		SprintTraceLog fetchRecord = sprintTraceLogRepository
				.findFirstBySprintId(sprintId);

		// checking if fetchRecord is not null
		if (fetchRecord != null) {
			log.info("Successfully fetched sprintTraceLog from db for sprint {}", sprintId);
			return new ServiceResponse(true, "Sprint trace log", fetchRecord);
		} else {
			log.info("fetchRecord is null");
			return new ServiceResponse(true, "No sync record found.", null);
		}

	}
}
