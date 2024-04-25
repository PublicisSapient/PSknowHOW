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
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;

public class ProcessorAsynchJiraRestClientTest {

	static URI baseUri;
	// static DisposableHttpClient httpClient;

	static ProcessorAsynchJiraRestClient processorAsynchJiraRestClient;

	@BeforeClass
	public static void processorAsynchJiraRestClientSetup() throws URISyntaxException {
		baseUri = new URI("https://www.baseuri.com/");
		DisposableHttpClient mockHttpClient = mock(DisposableHttpClient.class);
		processorAsynchJiraRestClient = new ProcessorAsynchJiraRestClient(baseUri, mockHttpClient);
		assertNotNull(processorAsynchJiraRestClient);
	}

	@Test
	public void issueRestClientTest() {
		assertNotNull(processorAsynchJiraRestClient.getIssueClient());
	}

	@Test
	public void getProcessorSearchClientTest() {
		assertNotNull(processorAsynchJiraRestClient.getProcessorSearchClient());
	}

	@Test
	public void getCustomIssueClientTest() {
		assertNotNull(processorAsynchJiraRestClient.getCustomIssueClient());
	}

	@AfterClass
	public static void processorAsynchJiraRestClientClean() {
		baseUri = null;
		processorAsynchJiraRestClient = null;
	}
}
