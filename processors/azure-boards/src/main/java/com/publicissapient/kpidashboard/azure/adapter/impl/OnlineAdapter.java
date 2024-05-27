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

package com.publicissapient.kpidashboard.azure.adapter.impl;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.azure.adapter.AzureAdapter;
import com.publicissapient.kpidashboard.azure.adapter.impl.async.ProcessorAzureRestClient;
import com.publicissapient.kpidashboard.azure.config.AzureProcessorConfig;
import com.publicissapient.kpidashboard.azure.model.AzureServer;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.azure.util.AzureConstants;
import com.publicissapient.kpidashboard.common.model.azureboards.AzureBoardsWIModel;
import com.publicissapient.kpidashboard.common.model.azureboards.iterations.AzureIterationsModel;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.AzureUpdatesModel;
import com.publicissapient.kpidashboard.common.model.azureboards.wiql.AzureWiqlModel;

import lombok.extern.slf4j.Slf4j;

/**
 * Default JIRA client which interacts with Java JIRA API to extract data for
 * projects based on the configurations provided
 */
@Slf4j
@Service
public class OnlineAdapter implements AzureAdapter {

	private static final String MSG_AZURE_CLIENT_SETUP_FAILED = "Azure client setup failed. No results obtained. Check your azure setup.";
	private static final String ERROR_MSG_NO_RESULT_WAS_AVAILABLE = "No result was available from Azure unexpectedly - defaulting to blank response. The reason for this fault is the following : {}";

	private AzureProcessorConfig azureProcessorConfig;

	private ProcessorAzureRestClient client;

	private AzureServer azureServerObj;

	public OnlineAdapter() {
	}

	/**
	 * @param azureProcessorConfig
	 *            azure processor configuration
	 * @param client
	 *            ProcessorAzureRestClient instance
	 */
	public OnlineAdapter(AzureProcessorConfig azureProcessorConfig, ProcessorAzureRestClient client) {
		this.azureProcessorConfig = azureProcessorConfig;
		this.client = client;

	}

	public OnlineAdapter(AzureProcessorConfig azureProcessorConfig, ProcessorAzureRestClient client,
			AzureServer azureServerObj) {
		this.azureProcessorConfig = azureProcessorConfig;
		this.client = client;
		this.azureServerObj = azureServerObj;
	}

	/**
	 * Gets page size from feature settings
	 *
	 * @return pageSize
	 */
	@Override
	public int getPageSize() {
		return azureProcessorConfig.getPageSize();
	}

	@Override
	public AzureWiqlModel getWiqlModel(AzureServer azureServer, Map<String, LocalDateTime> startTimesByIssueType,
			ProjectConfFieldMapping projectConfig, boolean dataExist) {
		AzureWiqlModel azureWiqlModel = new AzureWiqlModel();
		if (client == null) {
			log.warn(MSG_AZURE_CLIENT_SETUP_FAILED);
		} else {
			try {
				azureWiqlModel = client.getWiqlResponse(azureServer, startTimesByIssueType, projectConfig, dataExist);
			} catch (RestClientException rce) {
				log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, rce.getMessage());

			}
		}
		return azureWiqlModel;
	}

	/**
	 * Gets list of Azure issues.
	 *
	 * @param pageStart
	 *            Index where to start the search query at
	 * @param azureServer
	 *            the azure server
	 * @param workItemIds
	 *            the work item ids
	 * @return List of issues
	 */
	@Override
	public AzureBoardsWIModel getWorkItemInfoForIssues(int pageStart, // NOSONAR
			AzureServer azureServer, List<Integer> workItemIds) {
		AzureBoardsWIModel azureBoardsWIModel = new AzureBoardsWIModel();
		if (client == null) {
			log.warn(MSG_AZURE_CLIENT_SETUP_FAILED);
		} else {
			try {
				azureBoardsWIModel = client.getWorkItemInfo(azureServer, workItemIds);
			} catch (RestClientException rce) {
				log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, rce.getMessage());

			}
		}

		return azureBoardsWIModel;
	}

	@Override
	public AzureIterationsModel getIterationsModel(AzureServer azureServer) {
		AzureIterationsModel azureIterationsModel = new AzureIterationsModel();
		if (client == null) {
			log.warn(MSG_AZURE_CLIENT_SETUP_FAILED);
		} else {
			try {
				azureIterationsModel = client.getIterationsResponse(azureServer);
			} catch (RestClientException rce) {
				log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, rce.getMessage());

			}
		}
		return azureIterationsModel;
	}

	@Override
	public AzureUpdatesModel getUpdates(AzureServer azureServer, String issueId) {
		AzureUpdatesModel azureUpdatesModel = new AzureUpdatesModel();
		if (client == null) {
			log.warn(MSG_AZURE_CLIENT_SETUP_FAILED);
		} else {

			try {
				azureUpdatesModel = client.getUpdatesResponse(azureServer, issueId);
			} catch (RestClientException rce) {
				log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, rce.getMessage());

			}
		}
		return azureUpdatesModel;
	}

	/**
	 * @return list of IssueType
	 */
	@Override
	public List<IssueType> getIssueType() {
		List<IssueType> issueType = null;
		if (client == null) {
			log.warn(MSG_AZURE_CLIENT_SETUP_FAILED);
		} else {

			try {
				issueType = parseWorkItemTypes(client.getMetadataJson(azureServerObj,
						azureProcessorConfig.getApiWorkItemTypesEndPoint(), false));

			} catch (RestClientException rce) {
				log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, rce.getMessage());

			}
		}
		return issueType;
	}

	/**
	 * @return parsed issuetype list
	 */
	private List<IssueType> parseWorkItemTypes(JSONObject workItemTypeJsonObject) {
		List<IssueType> issueTypeList = null;

		if (workItemTypeJsonObject != null) {
			issueTypeList = Lists.newArrayList();
			JSONArray jsonArray = (JSONArray) workItemTypeJsonObject.get(AzureConstants.VALUE);
			for (int i = 0; i < jsonArray.size(); i++) {
				try {
					JSONObject innerObject = (JSONObject) jsonArray.get(i);
					IssueType issueType = new IssueType(URI.create(innerObject.get(AzureConstants.URL).toString()), 0L,
							innerObject.get(AzureConstants.NAME).toString(), false,
							innerObject.get(AzureConstants.DESCRIPTION).toString(), null);
					issueTypeList.add(issueType);
				} catch (Exception e) {
					log.error("Some exception occured in parseWorkItemTypes ", e);
				}

			}
		}

		return issueTypeList;
	}

	/**
	 * @return list of Field
	 */
	@Override
	public List<Field> getField() {
		List<Field> field = null;
		if (client == null) {
			log.warn(MSG_AZURE_CLIENT_SETUP_FAILED);
		} else {

			try {
				field = parseField(
						client.getMetadataJson(azureServerObj, azureProcessorConfig.getApiFieldsEndPoint(), false));

			} catch (RestClientException rce) {
				log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, rce.getMessage());

			}
		}
		return field;
	}

	/**
	 * @return parsed field list
	 */
	private List<Field> parseField(JSONObject fieldsJsonObject) {
		List<Field> fieldList = null;

		if (fieldsJsonObject != null) {
			fieldList = Lists.newArrayList();
			JSONArray jsonArray = (JSONArray) fieldsJsonObject.get(AzureConstants.VALUE);
			for (int i = 0; i < jsonArray.size(); i++) {

				JSONObject innerObject = (JSONObject) jsonArray.get(i);
				Field field = new Field(innerObject.get(AzureConstants.REFERENCENAME).toString(),
						innerObject.get(AzureConstants.NAME).toString(), FieldType.JIRA, false, false, false, null);
				fieldList.add(field);

			}
		}

		return fieldList;
	}

	/**
	 * @return list of Status
	 */
	@Override
	public List<Status> getStatus() {
		List<Status> status = null;
		if (client == null) {
			log.warn(MSG_AZURE_CLIENT_SETUP_FAILED);
		} else {
			try {
				status = parseStatus(
						client.getMetadataJson(azureServerObj, azureProcessorConfig.getApiStatusEndPoint(), false));

			} catch (RestClientException rce) {
				log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, rce.getMessage());

			}
		}
		return status;
	}

	/**
	 * @return parsed status list
	 */
	private List<Status> parseStatus(JSONObject statusJsonObject) {
		List<Status> statusList = null;

		if (statusJsonObject != null) {
			statusList = Lists.newArrayList();
			JSONArray jsonArray = (JSONArray) statusJsonObject.get(AzureConstants.STATES);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject innerObject = (JSONObject) jsonArray.get(i);
				Status field = new Status(null, 0L, innerObject.get(AzureConstants.NAME).toString(),
						innerObject.get(AzureConstants.CATEGORY).toString(), null);
				statusList.add(field);

			}
		}

		return statusList;
	}

	/**
	 * @return list of IssuelinksType
	 */
	@Override
	public List<IssuelinksType> getIssueLinkTypes() {
		List<IssuelinksType> issueLinksType = null;
		if (client == null) {
			log.warn(MSG_AZURE_CLIENT_SETUP_FAILED);
		} else {

			try {
				issueLinksType = parseIssueLinkTypes(client.getMetadataJson(azureServerObj,
						azureProcessorConfig.getApiEndpointWorkItemRelationTypes(), true));

			} catch (RestClientException rce) {
				log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, rce.getMessage());

			}
		}
		return issueLinksType;
	}

	@Override
	public List<String> getIssuesBySprint(AzureServer azureServer, String sprintId) {
		List<String> sprintWiseItemIdList = new ArrayList<>();
		if (client == null) {
			log.warn(MSG_AZURE_CLIENT_SETUP_FAILED);
		} else {
			try {
				sprintWiseItemIdList = client.getIssuesBySprintResponse(azureServer, sprintId);
			} catch (RestClientException rce) {
				log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, rce.getMessage());

			}
		}
		return sprintWiseItemIdList;
	}

	/**
	 * @return parsed issuelinktype list
	 */
	private List<IssuelinksType> parseIssueLinkTypes(JSONObject issueLinkTypeJsonObject) {
		List<IssuelinksType> issueLinkTypeList = null;

		if (issueLinkTypeJsonObject != null) {
			issueLinkTypeList = Lists.newArrayList();
			JSONArray jsonArray = (JSONArray) issueLinkTypeJsonObject.get(AzureConstants.VALUE);
			for (int i = 0; i < jsonArray.size(); i++) {

				JSONObject innerObject = (JSONObject) jsonArray.get(i);
				IssuelinksType issuelinksType = new IssuelinksType(
						URI.create(innerObject.get(AzureConstants.URL).toString()),
						innerObject.get(AzureConstants.REFERENCENAME).toString(),
						innerObject.get(AzureConstants.NAME).toString(),
						innerObject.get(AzureConstants.NAME).toString(),
						innerObject.get(AzureConstants.NAME).toString());
				issueLinkTypeList.add(issuelinksType);

			}
		}

		return issueLinkTypeList;
	}
}