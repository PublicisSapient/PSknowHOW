/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.common.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.dto.SprintRefreshLogDTO;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

/**
 * AzureSprintReportLogService
 * 
 * @author shunaray
 */
public interface AzureSprintReportLogService {
	/**
	 * Save Sprint Refresh Log
	 * 
	 * @param sprintDetails
	 *            sprintDetails
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @param refreshOn
	 *            time of refresh
	 * @param refreshBy
	 *            user who refreshed
	 */
	void saveSprintRefreshLog(SprintDetails sprintDetails, ObjectId basicProjectConfigId, long refreshOn,
			String refreshBy);

	/**
	 * Get Sprint Refresh Logs
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return List of SprintRefreshLogDTO
	 */
	List<SprintRefreshLogDTO> getSprintRefreshLogs(ObjectId basicProjectConfigId);
}
