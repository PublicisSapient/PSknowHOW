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

package com.publicissapient.kpidashboard.jira.cache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;

@RunWith(MockitoJUnitRunner.class)
public class JiraProcessorCacheEvictorTest {

	@InjectMocks
	private JiraProcessorCacheEvictor jiraProcessorCacheEvictor;
	@Mock
	private JiraProcessorConfig jiraProcessorConfig;
	@Mock
	private RestTemplate restTemplate;

	@Test
	public void testEvictCache_SuccessfulEviction() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);

		ResponseEntity<String> responseEntity = new ResponseEntity<>("Success", HttpStatus.OK);

		when(jiraProcessorConfig.getCustomApiBaseUrl()).thenReturn("http://example.com");

		boolean cleaned = jiraProcessorCacheEvictor.evictCache("cacheEndPoint", "cacheName");
		assertTrue(!cleaned);
	}

	@Test
	public void testEvictCache_UnsuccessfulEviction() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);

		ResponseEntity<String> responseEntity = new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);

		when(jiraProcessorConfig.getCustomApiBaseUrl()).thenReturn("http://example.com");

		boolean cleaned = jiraProcessorCacheEvictor.evictCache("cacheEndPoint", "cacheName");

		assertFalse(cleaned);
	}

	@Test
	public void testEvictCache_FailedEviction() {
		// Arrange
		String cacheEndPoint = "yourCacheEndPoint";
		String cacheName = "yourCacheName";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		// Mocking the configuration values
		when(jiraProcessorConfig.getCustomApiBaseUrl()).thenReturn("http://your-custom-api-base-url");

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(jiraProcessorConfig.getCustomApiBaseUrl());
		uriBuilder.path("/");
		uriBuilder.path(cacheEndPoint);
		uriBuilder.path("/");
		uriBuilder.path(cacheName);

		HttpEntity<?> entity = new HttpEntity<>(headers);

		// Act
		boolean result = jiraProcessorCacheEvictor.evictCache(cacheEndPoint, cacheName);

		// Assert
		assertFalse(result);
	}

	@Test
	public void testEvictCache_SuccessfulEviction2() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);

		ResponseEntity<String> responseEntity = new ResponseEntity<>("Success", HttpStatus.OK);

		when(jiraProcessorConfig.getCustomApiBaseUrl()).thenReturn("http://example.com");

		boolean cleaned = jiraProcessorCacheEvictor.evictCache("cacheEndPoint", "projectId", "cacheName");
		assertTrue(!cleaned);
	}
}
