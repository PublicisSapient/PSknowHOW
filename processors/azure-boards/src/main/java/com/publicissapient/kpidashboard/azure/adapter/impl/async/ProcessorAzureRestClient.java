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

package com.publicissapient.kpidashboard.azure.adapter.impl.async;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.publicissapient.kpidashboard.azure.model.AzureServer;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.common.model.azureboards.AzureBoardsWIModel;
import com.publicissapient.kpidashboard.common.model.azureboards.iterations.AzureIterationsModel;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.AzureUpdatesModel;
import com.publicissapient.kpidashboard.common.model.azureboards.wiql.AzureWiqlModel;

public interface ProcessorAzureRestClient {

	/**
	 * 
	 * Gets WorkItem Info from Azure API
	 * 
	 * @param azureServer
	 *            for connection detail
	 * @param workItemIds
	 *            issues
	 * @return AzureBoardsWIModel
	 */
	AzureBoardsWIModel getWorkItemInfo(AzureServer azureServer, List<Integer> workItemIds);

	/**
	 * Gets Wiql Response from Azure API
	 * 
	 * 
	 * @param azureServer
	 *            for connection detail
	 * @param startTimesByIssueType
	 *            startTimesByIssueType
	 * @param projectConfig
	 *            projectConfig
	 * @param dataExist
	 *            data present in db or not
	 * @return AzureWiqlModel
	 */
	AzureWiqlModel getWiqlResponse(AzureServer azureServer, Map<String, LocalDateTime> startTimesByIssueType,
			ProjectConfFieldMapping projectConfig, boolean dataExist);

	/**
	 * Gets Iterations Response from Azure API
	 * 
	 * @param azureServer
	 *            for connection detail
	 * @return AzureIterationsModel
	 */
	AzureIterationsModel getIterationsResponse(AzureServer azureServer);

	/**
	 * Gets UpdatesResponse based on issueId
	 * 
	 * @param azureServer
	 *            for connection detail
	 * @param issueId
	 *            issueId
	 * @return AzureUpdatesModel
	 */
	AzureUpdatesModel getUpdatesResponse(AzureServer azureServer, String issueId);

	/**
	 * 
	 * Gets Metadata Json response from Azure boards API
	 * 
	 * @param azureServer
	 *            for connection detail
	 * @param metadataUrlPath
	 *            for api endpoints
	 * @param orgLevelApi
	 *            for switching between project level and organisational level
	 *            endpoint
	 * @return jsonObject response
	 */
	JSONObject getMetadataJson(AzureServer azureServer, String metadataUrlPath, boolean orgLevelApi);

	/**
	 *
	 * @param azureServer
	 * @param sprintId
	 * @return
	 */
	List<String> getIssuesBySprintResponse(AzureServer azureServer, String sprintId);
}