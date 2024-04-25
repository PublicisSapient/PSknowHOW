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
package com.publicissapient.kpidashboard.jira.service;

import java.io.IOException;
import java.util.List;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface FetchEpicData {

	/**
	 *
	 * @param projectConfig
	 *            projectConfig
	 * @param boardId
	 *            boardId
	 * @param clientIncoming
	 *            clientIncoming
	 * @param krb5Client
	 *            krb5Client
	 * @return List of Issue
	 * @throws InterruptedException
	 *             InterruptedException
	 * @throws RestClientException
	 *             RestClientException
	 * @throws IOException
	 *             IOException
	 */
	List<Issue> fetchEpic(ProjectConfFieldMapping projectConfig, String boardId, ProcessorJiraRestClient clientIncoming,
			KerberosClient krb5Client) throws InterruptedException, RestClientException, IOException;
}
