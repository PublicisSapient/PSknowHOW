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

package com.publicissapient.kpidashboard.azure.adapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.publicissapient.kpidashboard.azure.model.AzureServer;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.common.model.azureboards.AzureBoardsWIModel;
import com.publicissapient.kpidashboard.common.model.azureboards.iterations.AzureIterationsModel;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.AzureUpdatesModel;
import com.publicissapient.kpidashboard.common.model.azureboards.wiql.AzureWiqlModel;

/**
 * The Interface AzureAdapter.
 */
public interface AzureAdapter {

	/**
	 * Gets the issues.
	 *
	 * @param pageStart
	 *            the page start
	 * @param azureServer
	 *            the azure server
	 * @param workItemIds
	 *            the work item ids
	 * @return the issues
	 */
	AzureBoardsWIModel getWorkItemInfoForIssues(int pageStart, AzureServer azureServer, List<Integer> workItemIds);

	/**
	 * Gets the wiql model.
	 *
	 * @param azureServer
	 *            the azure server
	 * @param startTimesByIssueType
	 *            the startTimesByIssueType
	 * @param projectConfig
	 *            the project config
	 * @param dataExist
	 *            data exist in db
	 * 
	 * @return the wiql model
	 */
	AzureWiqlModel getWiqlModel(AzureServer azureServer, Map<String, LocalDateTime> startTimesByIssueType,
			ProjectConfFieldMapping projectConfig, boolean dataExist);

	/**
	 * Gets the iterations model.
	 *
	 * @param azureServer
	 *            the azure server
	 * @return the iterations model
	 */
	AzureIterationsModel getIterationsModel(AzureServer azureServer);

	/**
	 * Gets page size from feature settings.
	 *
	 * @return pageSize
	 */
	int getPageSize();

	/**
	 * Get the changeLogs/Updates for a workItem.
	 *
	 * @param azureServer
	 *            the azure server
	 * @param issueId
	 *            the issue id
	 * @return the updates model
	 */
	AzureUpdatesModel getUpdates(AzureServer azureServer, String issueId);

	/**
	 * get IssueType from parsed workitemTypes
	 * 
	 * @return list of issueTypes
	 */
	List<IssueType> getIssueType();

	/**
	 * get all fields from parsed workitem
	 * 
	 * @return list of fields
	 */
	List<Field> getField();

	/**
	 * get Workflow status from parsed workitem
	 * 
	 * @return list of status
	 */
	List<Status> getStatus();

	/**
	 * get IssueTypeLinks from parsed workitemlink
	 * 
	 * @return list of issueLinkTypes
	 */
	List<IssuelinksType> getIssueLinkTypes();

	/**
	 * get all issues based on sprintId
	 * 
	 * @param azureServer
	 * @param sprintId
	 * @return
	 */
	List<String> getIssuesBySprint(AzureServer azureServer, String sprintId);

}
