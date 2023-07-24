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

package com.publicissapient.kpidashboard.jira.adapter.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public class OnlineAdapterTest {

	ProcessorJiraRestClient client = Mockito.mock(ProcessorJiraRestClient.class);
	ProjectConfFieldMapping projectConfFieldMapping;
	ProjectConfFieldMapping projectConfFieldMapping2;
	ProjectConfFieldMapping projectConfFieldMapping3;
	@Mock
	com.google.common.base.Optional<Integer> status;
	private JiraProcessorConfig jiraProcessorConfig = Mockito.mock(JiraProcessorConfig.class);
	private AesEncryptionService aesEncryptionService = Mockito.mock(AesEncryptionService.class);
	private ToolCredentialProvider toolCredentialProvider = Mockito.mock(ToolCredentialProvider.class);
	OnlineAdapter onlineAdapter = new OnlineAdapter(jiraProcessorConfig, client, aesEncryptionService,
			toolCredentialProvider, null);

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		prepareProjectConfigData();
	}

	@Test
	public void getUserTimeZone() {
		Assert.assertEquals("", onlineAdapter.getUserTimeZone(projectConfFieldMapping));
	}

	@Test
	public void getUserTimeZoneException() {
		assertNotNull(onlineAdapter.getUserTimeZone(projectConfFieldMapping2));
	}

	@Test
	public void getUserTimeZoneURIException() {
		assertNotNull(onlineAdapter.getUserTimeZone(projectConfFieldMapping3));
	}

	@Test
	public void getVersion() {
		RestClientException exception = Mockito.mock(RestClientException.class);
		Mockito.when(client.getProjectClient()).thenThrow(exception);
		Mockito.when(jiraProcessorConfig.getJiraVersionApi()).thenReturn("abc");
		// jiraProcessorConfig.setJiraCloudVersionApi("abc");

		Mockito.when(exception.getStatusCode()).thenReturn(status);
		Mockito.when(status.isPresent()).thenReturn(false);
		List<ProjectVersion> projectVersionList = onlineAdapter.getVersion(projectConfFieldMapping);
		assertNotNull(projectVersionList);
	}

	private void prepareProjectConfigData() {
		projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		JiraToolConfig jiraConfig = new JiraToolConfig();
		Optional<Connection> conn = Optional.of(new Connection());
		conn.get().setOffline(Boolean.FALSE);
		conn.get().setBaseUrl("https://abc.com/jira/");
		conn.get().setApiEndPoint("rest/api/2/");
		jiraConfig.setBasicProjectConfigId("5b674d58f47cae8935b1b26f");
		jiraConfig.setConnection(conn);
		jiraConfig.setProjectKey("TEST");

		projectConfFieldMapping.setJira(jiraConfig);

		projectConfFieldMapping3 = ProjectConfFieldMapping.builder().build();
		jiraConfig = new JiraToolConfig();
		jiraConfig.setBasicProjectConfigId("5b719d06a500d00814bfb2b9");
		jiraConfig.setConnection(conn);
		projectConfFieldMapping3.setJira(jiraConfig);

		projectConfFieldMapping2 = ProjectConfFieldMapping.builder().build();
		jiraConfig = new JiraToolConfig();
		// jiraConfig.setJiraCredentials("cml0Z2lyZGg6QWRtaW5AMzIx");
		jiraConfig.setBasicProjectConfigId("5ba8e182d3735010e7f1fa45");
		jiraConfig.setConnection(conn);

		projectConfFieldMapping2.setJira(jiraConfig);
	}

    @Test
    void getIssuesSprint() {
    }

    @Test
    void getSubtask() {
    }
}