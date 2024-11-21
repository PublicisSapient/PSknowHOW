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

package com.publicissapient.kpidashboard.azure.service;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class AzureSprintReportRefreshServiceTest {

	@InjectMocks
	private AzureSprintReportRefreshService azureSprintReportRefreshService;

	@Mock
	private AzureSprintReportLogRepositoryCustom azureSprintReportLogRepositoryCustom;

	@Test
	public void TestaddUpdateTimesInBulk() {
		ObjectId projectId = new ObjectId("6708db481d29a66c68fe2f90");
		Map<String, LocalDateTime> sprintUpdateMap = Map.of("Sprint1", LocalDateTime.parse("2024-11-14T12:00:00"),
				"Sprint2", LocalDateTime.parse("2024-11-14T13:00:00"));
		Map<ObjectId, Map<String, LocalDateTime>> projectWiseSprintRefreshToggleMap = Map.of(projectId,
				sprintUpdateMap);
		azureSprintReportRefreshService.addUpdateTimesInBulk(projectWiseSprintRefreshToggleMap);
		// Assert: Verify that the repository method was called with the correct
		// parameters
		verify(azureSprintReportLogRepositoryCustom).addUpdateTimesInBulk(projectWiseSprintRefreshToggleMap);

	}

}