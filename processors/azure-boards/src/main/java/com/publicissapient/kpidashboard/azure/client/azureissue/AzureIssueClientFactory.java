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

package com.publicissapient.kpidashboard.azure.client.azureissue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;

@Component
public class AzureIssueClientFactory {

	@Autowired
	private KanbanAzureIssueClientImpl kanbanAzureIssueClient;

	@Autowired
	private ScrumAzureIssueClientImpl scrumAzureIssueClient;

	/**
	 * Gets AzureIssue Client based on the Project type (Kanban or Scrum)
	 *
	 * @param projectConfig
	 *            user provided project Configuration mapping
	 * @return KanbanAzureIssueClient if isKanban true else ScrumAzureIssueClient
	 */
	public AzureIssueClient getAzureIssueDataClient(ProjectConfFieldMapping projectConfig) {
		if (projectConfig.isKanban()) {
			return kanbanAzureIssueClient;
		} else {
			return scrumAzureIssueClient;
		}
	}
}
