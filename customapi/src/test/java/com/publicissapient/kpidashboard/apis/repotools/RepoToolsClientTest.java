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

package com.publicissapient.kpidashboard.apis.repotools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolConfig;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiBulkMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiRequestBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class RepoToolsClientTest {

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private RepoToolsClient repoToolsClient;

	@Test
	public void testEnrollProjectCall_Success() {
		RepoToolConfig repoToolConfig = new RepoToolConfig();
		String repoToolsUrl = "http://example.com";
		String apiKey = "testApiKey";

		Mockito.when(restTemplate.exchange(any(URI.class), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
				Mockito.eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

		int result =repoToolsClient.enrollProjectCall(repoToolConfig, repoToolsUrl, apiKey);

		assertEquals(HttpStatus.OK.value(), result);
	}

	@Test
	public void testEnrollProjectCall_Failure() {
		RepoToolConfig repoToolConfig = new RepoToolConfig(/* initialize with required data */);
		String repoToolsUrl = "http://example.com";
		String apiKey = "testApiKey";

		Mockito.when(restTemplate.exchange(any(URI.class), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
				Mockito.eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR));

		int result = repoToolsClient.enrollProjectCall(repoToolConfig, repoToolsUrl, apiKey);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result);
	}

	@Test
	public void testTriggerScanCall_Success() {
		String projectKey = "testProjectKey";
		String repoToolsUrl = "http://example.com";
		String apiKey = "testApiKey";

		Mockito.when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.GET), any(HttpEntity.class),
				Mockito.eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

		int result = repoToolsClient.triggerScanCall(projectKey, repoToolsUrl, apiKey);

		assertEquals(HttpStatus.OK.value(), result);
	}

	@Test
	public void testTriggerScanCall_Failure() {
		String projectKey = "testProjectKey";
		String repoToolsUrl = "http://example.com";
		String apiKey = "testApiKey";

		Mockito.when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.GET), any(HttpEntity.class),
				Mockito.eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.NOT_FOUND));

		int result = repoToolsClient.triggerScanCall(projectKey, repoToolsUrl, apiKey);

		assertEquals(HttpStatus.NOT_FOUND.value(), result);
	}

	@Test
	public void testKpiMetricCall_Success() {
		String repoToolsUrl = "http://example.com";
		String apiKey = "testApiKey";
		RepoToolKpiRequestBody repoToolKpiRequestBody = new RepoToolKpiRequestBody();

		Mockito.when(restTemplate.exchange(any(URI.class), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
				Mockito.eq(RepoToolKpiBulkMetricResponse.class)))
				.thenReturn(new ResponseEntity<>(new RepoToolKpiBulkMetricResponse(), HttpStatus.OK));

		RepoToolKpiBulkMetricResponse result = repoToolsClient.kpiMetricCall(repoToolsUrl, apiKey,
				repoToolKpiRequestBody);

		assertNotNull(result);
	}

	@Test
	public void testKpiMetricCall_Failure() {
		String repoToolsUrl = "http://example.com";
		String apiKey = "testApiKey";
		RepoToolKpiRequestBody repoToolKpiRequestBody = new RepoToolKpiRequestBody();

		Mockito.when(restTemplate.exchange(any(URI.class), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
				Mockito.eq(RepoToolKpiBulkMetricResponse.class)))
				.thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

		RepoToolKpiBulkMetricResponse result = repoToolsClient.kpiMetricCall(repoToolsUrl, apiKey,
				repoToolKpiRequestBody);

		assertNull(result);
	}

	@Test
	public void testDeleteProject_Success() {
		String repoToolsUrl = "http://example.com";
		String apiKey = "testApiKey";

		Mockito.when(restTemplate.exchange(any(URI.class), Mockito.eq(HttpMethod.DELETE), any(HttpEntity.class),
				Mockito.eq(JsonNode.class)))
				.thenReturn(new ResponseEntity<>(JsonNodeFactory.instance.objectNode(), HttpStatus.NO_CONTENT));

		int result = repoToolsClient.deleteProject(repoToolsUrl, apiKey);

		assertEquals(HttpStatus.NO_CONTENT.value(), result);
	}

	@Test
	public void testDeleteProject_Failure() {
		String repoToolsUrl = "http://example.com";
		String apiKey = "testApiKey";

		Mockito.when(restTemplate.exchange(any(URI.class), Mockito.eq(HttpMethod.DELETE), any(HttpEntity.class),
				Mockito.eq(JsonNode.class))).thenReturn(
						new ResponseEntity<>(JsonNodeFactory.instance.objectNode(), HttpStatus.INTERNAL_SERVER_ERROR));

		int result = repoToolsClient.deleteProject(repoToolsUrl, apiKey);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result);
	}

	@Test
	public void testDeleteRepositories_Success() {
		String deleteRepoUrl = "http://example.com/delete";
		String apiKey = "testApiKey";

		Mockito.when(restTemplate.exchange(any(URI.class), Mockito.eq(HttpMethod.DELETE), any(HttpEntity.class),
				Mockito.eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.NO_CONTENT));

		int result = repoToolsClient.deleteRepositories(deleteRepoUrl, apiKey);

		assertEquals(HttpStatus.NO_CONTENT.value(), result);
	}

	@Test
	public void testDeleteRepositories_Failure() {
		String deleteRepoUrl = "http://example.com/delete";
		String apiKey = "testApiKey";

		Mockito.when(restTemplate.exchange(any(URI.class), Mockito.eq(HttpMethod.DELETE), any(HttpEntity.class),
				Mockito.eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR));

		int result = repoToolsClient.deleteRepositories(deleteRepoUrl, apiKey);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result);
	}

}