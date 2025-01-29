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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.application.dto.SprintRefreshLogDTO;
import com.publicissapient.kpidashboard.common.model.azure.AzureSprintReportLog;
import com.publicissapient.kpidashboard.common.model.azure.RefreshAuditDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.azure.AzureSprintReportLogRepository;

@ExtendWith(SpringExtension.class)
public class AzureSprintReportLogServiceImplTest {
	@Mock
	private AzureSprintReportLogRepository azureSprintReportLogRepository;

	@InjectMocks
	private AzureSprintReportLogServiceImpl azureSprintReportLogService;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testSaveSprintRefreshLog() {
		ObjectId basicProjectConfigId = new ObjectId("62177593904d2839684f5d68");
		long refreshOn = 1649241013929L;
		String refreshBy = "user1";

		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintID("sprint1");
		sprintDetails.setSprintName("Sprint 1");
		sprintDetails.setStartDate("2022-01-01");
		sprintDetails.setEndDate("2022-01-15");

		AzureSprintReportLog existingLog = new AzureSprintReportLog();
		existingLog.setBasicProjectConfigId(basicProjectConfigId);
		existingLog.setSprintId("sprint1");
		existingLog.setRefreshAuditDetails(
				new ArrayList<>(Collections.singletonList(new RefreshAuditDetails(1649241013929L, "user1"))));

		when(azureSprintReportLogRepository.findByBasicProjectConfigIdAndSprintId(any(ObjectId.class),
				any(String.class))).thenReturn(Optional.of(existingLog));

		azureSprintReportLogService.saveSprintRefreshLog(sprintDetails, basicProjectConfigId, refreshOn, refreshBy);

		verify(azureSprintReportLogRepository, times(1)).save(any(AzureSprintReportLog.class));
	}

	@Test
	public void testGetSprintRefreshLogs() {
		ObjectId basicProjectConfigId = new ObjectId("62177593904d2839684f5d68");

		final AzureSprintReportLog azureSprintReportLog = getAzureSprintReportLog(basicProjectConfigId);

		when(azureSprintReportLogRepository.findByBasicProjectConfigId(any(ObjectId.class)))
				.thenReturn(Collections.singletonList(azureSprintReportLog));

		List<SprintRefreshLogDTO> sprintRefreshLogs = azureSprintReportLogService
				.getSprintRefreshLogs(basicProjectConfigId);

		assertEquals(1, sprintRefreshLogs.size());
		assertEquals("Sprint 1", sprintRefreshLogs.get(0).getSprintName());
		assertEquals("2022-01-01", sprintRefreshLogs.get(0).getSprintStartDate());
		assertEquals("2022-01-15", sprintRefreshLogs.get(0).getSprintEndDate());
		assertEquals(1649241013929L, sprintRefreshLogs.get(0).getRefreshedOn());
		assertEquals("user1", sprintRefreshLogs.get(0).getRefreshBy());
	}

	private static AzureSprintReportLog getAzureSprintReportLog(ObjectId basicProjectConfigId) {
		RefreshAuditDetails refreshAuditDetails = new RefreshAuditDetails(1649241013929L, "user1");
		AzureSprintReportLog azureSprintReportLog = new AzureSprintReportLog();
		azureSprintReportLog.setBasicProjectConfigId(basicProjectConfigId);
		azureSprintReportLog.setSprintId("sprint1");
		azureSprintReportLog.setSprintName("Sprint 1");
		azureSprintReportLog.setSprintStartDate("2022-01-01");
		azureSprintReportLog.setSprintEndDate("2022-01-15");
		azureSprintReportLog.setRefreshAuditDetails(Collections.singletonList(refreshAuditDetails));
		return azureSprintReportLog;
	}

	@Test
	public void testSaveSprintRefreshLog_NewLogCreation() {
		ObjectId basicProjectConfigId = new ObjectId("62177593904d2839684f5d68");
		long refreshOn = 1649241013929L;
		String refreshBy = "user1";

		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintID("sprint1");
		sprintDetails.setSprintName("Sprint 1");
		sprintDetails.setStartDate("2022-01-01");
		sprintDetails.setEndDate("2022-01-15");

		when(azureSprintReportLogRepository.findByBasicProjectConfigIdAndSprintId(any(ObjectId.class),
				any(String.class))).thenReturn(Optional.empty());

		azureSprintReportLogService.saveSprintRefreshLog(sprintDetails, basicProjectConfigId, refreshOn, refreshBy);

		verify(azureSprintReportLogRepository, times(1)).save(any(AzureSprintReportLog.class));
	}
}