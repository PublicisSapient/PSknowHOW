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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.application.dto.SprintRefreshLogDTO;
import com.publicissapient.kpidashboard.common.model.azure.AzureSprintReportLog;
import com.publicissapient.kpidashboard.common.model.azure.RefreshAuditDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.azure.AzureSprintReportLogRepository;

/**
 * Service class for Azure Sprint Report Log
 * 
 * @author shunaray
 */
@Service
public class AzureSprintReportLogServiceImpl implements AzureSprintReportLogService {

	@Autowired
	private AzureSprintReportLogRepository azureSprintReportLogRepository;

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
	@Override
	public void saveSprintRefreshLog(SprintDetails sprintDetails, ObjectId basicProjectConfigId, long refreshOn,
			String refreshBy) {

		RefreshAuditDetails refreshAuditDetails = new RefreshAuditDetails(refreshOn, refreshBy);

		Optional<AzureSprintReportLog> existingLogOptional = azureSprintReportLogRepository
				.findByBasicProjectConfigIdAndSprintId(basicProjectConfigId, sprintDetails.getSprintID());

		AzureSprintReportLog azureSprintReportLog = existingLogOptional.orElseGet(() -> {
			AzureSprintReportLog newLog = new AzureSprintReportLog();
			newLog.setBasicProjectConfigId(basicProjectConfigId);
			newLog.setSprintId(sprintDetails.getSprintID());
			newLog.setSprintName(sprintDetails.getSprintName());
			newLog.setSprintStartDate(sprintDetails.getStartDate());
			newLog.setSprintEndDate(sprintDetails.getEndDate());
			newLog.setRefreshAuditDetails(Collections.singletonList(refreshAuditDetails));
			return newLog;
		});

		if (existingLogOptional.isPresent()) {
			List<RefreshAuditDetails> existingRefreshDetails = azureSprintReportLog.getRefreshAuditDetails();
			existingRefreshDetails.add(refreshAuditDetails);
			azureSprintReportLog.setRefreshAuditDetails(existingRefreshDetails);
		}

		azureSprintReportLogRepository.save(azureSprintReportLog);
	}

	/**
	 * Get Sprint Refresh Logs
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return List of SprintRefreshLogDTO
	 */
	@Override
	public List<SprintRefreshLogDTO> getSprintRefreshLogs(ObjectId basicProjectConfigId) {
		return Optional.ofNullable(azureSprintReportLogRepository.findByBasicProjectConfigId(basicProjectConfigId))
				.orElse(Collections.emptyList()).stream()
				.flatMap(log -> Optional.ofNullable(log.getRefreshAuditDetails()).orElse(Collections.emptyList())
						.stream().map(detail -> {
							SprintRefreshLogDTO dto = new SprintRefreshLogDTO();
							dto.setSprintName(log.getSprintName());
							dto.setSprintStartDate(log.getSprintStartDate());
							dto.setSprintEndDate(log.getSprintEndDate());
							dto.setRefreshedOn(detail.getRefreshedOn());
							dto.setRefreshBy(detail.getRefreshBy());
							return dto;
						}))
				.sorted(Comparator.comparing(SprintRefreshLogDTO::getRefreshedOn).reversed()).toList();
	}
}
