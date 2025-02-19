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

package com.publicissapient.kpidashboard.jira.client;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.config.JiraOAuthProperties;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.factory.ProcessorAsynchJiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.model.JiraInfo;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;

@RunWith(MockitoJUnitRunner.class)
public class JiraClientTest {

	private static String baseUrlValid = "https://example.com/";

	private static String baseUrlNotValid = "https://www.mockdummyurl.com/";

	@Mock
	private ProjectConfFieldMapping projectConfFieldMapping;

	@Mock
	private KerberosClient krb5Client;

	@Mock
	private JiraCommonService jiraCommonService;

	@Mock
	private JiraOAuthProperties jiraOAuthProperties;

	@Mock
	private JiraOAuthClient jiraOAuthClient;

	@Mock
	private ConnectionRepository connectionRepository;

	@Mock
	private ProcessorAsynchJiraRestClientFactory processorAsynchJiraRestClientFactory;

	@Mock
	private DisposableHttpClient disposableHttpClient;

	@Mock
	private JiraProcessorConfig jiraProcessorConfig;

	@Mock
	private KerberosClient kerberosClient;

	@Mock
	private JiraInfo jiraInfo;

	@Mock
	private ToolCredentialProvider toolCredentialProvider;

	@InjectMocks
	private JiraClient jiraClient;

	@Test(expected = NullPointerException.class)
	public void getClientNullTest() {
		Connection connection = new Connection();
		connection.setIsOAuth(true);
		JiraToolConfig jiraToolConfig = new JiraToolConfig();
		jiraToolConfig.setConnection(Optional.of(connection));
		when(projectConfFieldMapping.getJira()).thenReturn(jiraToolConfig);
		jiraClient.getClient(projectConfFieldMapping, krb5Client);
	}

	@Test
	public void getClientTest() throws ExecutionException, InterruptedException {
		Connection connection = new Connection();
		connection.setIsOAuth(false);
		connection.setVault(true);
		connection.setBaseUrl(baseUrlValid);
		connection.setUsername("uName");
		connection.setPassword("pass123");
		connection.setBearerToken(true);
		JiraToolConfig jiraToolConfig = new JiraToolConfig();
		jiraToolConfig.setConnection(Optional.of(connection));
		when(projectConfFieldMapping.getJira()).thenReturn(jiraToolConfig);
		ToolCredential toolCredential = new ToolCredential("uName", "pass123", "uname@dummy.com");
		when(toolCredentialProvider.findCredential(connection.getUsername())).thenReturn(toolCredential);
		ProcessorJiraRestClient processorJiraRestClient = jiraClient.getClient(projectConfFieldMapping, krb5Client);
		assertNotNull(processorJiraRestClient.getUserClient());
		assertNotNull(processorJiraRestClient.getSearchClient());
		assertNotNull(processorJiraRestClient.getSearchClient().getFavouriteFilters());
	}

	@Test
	public void getJiraClientTest() throws URISyntaxException {
		JiraInfo jiraInfo = getJiraInfo("uName", "password", baseUrlValid, "", "", "", true);
		ProcessorAsynchJiraRestClient processorAsynchJiraRestClient = new ProcessorAsynchJiraRestClient(
				new URI(baseUrlValid), disposableHttpClient);
		assertNotNull(jiraClient.getJiraClient(jiraInfo));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getJiraClientProxyTest() throws URISyntaxException {
		JiraInfo jiraInfo = getJiraInfo("uName", "password", baseUrlValid, "https://www.proxyurl.com/", "1771", "token",
				true);
		jiraClient.getJiraClient(jiraInfo);
	}

	@Test
	public void getJiraClientIExceptionTest() throws URISyntaxException {
		JiraInfo jiraInfo = getJiraInfo("uName", "password", baseUrlNotValid, "", "", "", true);
		jiraClient.getJiraClient(jiraInfo);
	}

	@Test
	public void getJiraOAuthClientTest() throws URISyntaxException {
		JiraInfo jiraInfo = getJiraInfo("uName", "password", "https://www.baseurl.com/", "", "", "", true);
		assertNotNull(jiraClient.getJiraOAuthClient(jiraInfo));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getJiraOAuthClientProxyTest() throws URISyntaxException {
		JiraInfo jiraInfo = getJiraInfo("uName", "password", "https://www.baseurl.com/", "https://www.proxyurl.com/",
				"1771", "token", true);
		jiraClient.getJiraOAuthClient(jiraInfo);
	}

	@Test
	public void getJiraOAuthClientExceptionTest() throws URISyntaxException {
		JiraInfo jiraInfo = getJiraInfo("uName", "password", baseUrlNotValid, "", "", "", true);
		jiraClient.getJiraOAuthClient(jiraInfo);
	}

	@Test(expected = NullPointerException.class)
	public void getJiraOAuthClientNullPointerExceptionTest() throws URISyntaxException {
		JiraInfo jiraInfo = getJiraInfo("uName", "password", "", "baseUrlValid", "1234", "", true);
		jiraClient.getJiraOAuthClient(jiraInfo);
	}

	@Test
	public void getJiraOAuthClientURIExceptionTest() throws URISyntaxException {
		JiraInfo jiraInfo = getJiraInfo("uName", "password", "baseUrlNotValid", "baseUrlNotValid", "proxyport", "", true);
		assertNotNull(jiraClient.getJiraOAuthClient(jiraInfo));
	}

	@Test
	public void getJiraOAuthClientTokenFalseTest() throws URISyntaxException {
		JiraInfo jiraInfo = getJiraInfo("uName", "password", "https://www.baseurl.com/", "", "", "", false);
		ProcessorAsynchJiraRestClient processorAsynchJiraRestClient = new ProcessorAsynchJiraRestClient(
				new URI("https://www.baseurl.com/"), disposableHttpClient);
		assertNotNull(jiraClient.getJiraClient(jiraInfo));
	}

	@Test(expected = NullPointerException.class)
	public void getSpnegoSamlClientTest() {
		jiraClient.getSpnegoSamlClient(kerberosClient);
	}

	private JiraInfo getJiraInfo(String userName, String password, String jiraConfigBaseUrl, String jiraConfigProxyUrl,
			String jiraConfigProxyPort, String jiraConfigAccessToken, boolean bearerToken) {
		JiraInfo jiraInfo = JiraInfo.builder().username(userName).password(password).jiraConfigBaseUrl(jiraConfigBaseUrl)
				.jiraConfigProxyUrl(jiraConfigProxyUrl).jiraConfigProxyPort(jiraConfigProxyPort)
				.jiraConfigAccessToken(jiraConfigAccessToken).bearerToken(bearerToken).build();
		return jiraInfo;
	}
}
