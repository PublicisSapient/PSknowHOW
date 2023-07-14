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

package com.publicissapient.kpidashboard.jira.adapter.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.factory.ProcessorAsynchJiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.impl.ProcessorAsynchJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraInfo;

@ExtendWith(SpringExtension.class)
@PrepareForTest({ JiraRestClientFactory.class, ProcessorAsynchJiraRestClientFactory.class })
public class JiraRestClientFactoryTest {

	private static final String PLAIN_TEXT_PASSWORD = "TestPassword";
	private static final String AES_ENCRYPTED_PASSWORD = "testEncryptedPassword";
	private static final String USERNAME = "test";
	@Mock
	JiraProcessorConfig jiraProcessorConfig;
	@InjectMocks
	JiraRestClientFactory jiraRestClientFactory;
	@Mock
	RestTemplate restTemplate;
	private JiraInfo jiraInfo;
	@Mock
	private ProcessorAsynchJiraRestClient restClient;
	@Mock
	private AesEncryptionService aesEncryptionService;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(aesEncryptionService.decrypt(anyString(), anyString())).thenReturn(PLAIN_TEXT_PASSWORD);
	}

	@Test
	public void getJiraClient() throws Exception {
		prepareJiraInfo();
		ProcessorAsynchJiraRestClientFactory jiraRestClient = Mockito.mock(ProcessorAsynchJiraRestClientFactory.class);
		PowerMockito.whenNew(ProcessorAsynchJiraRestClientFactory.class).withAnyArguments().thenReturn(jiraRestClient);
		Mockito.when(jiraRestClient.createWithBasicHttpAuthentication(Mockito.any(URI.class), Mockito.anyString(),
				Mockito.anyString(), Mockito.any(JiraProcessorConfig.class))).thenReturn(restClient);
		Assert.assertEquals(restClient.getClass().getSuperclass(),
				jiraRestClientFactory.getJiraClient(jiraInfo).getClass());
	}

	@Test
	public void getJiraClientProxyURL() throws Exception {
		prepareJiraInfoProxyURL();
		ProcessorAsynchJiraRestClientFactory jiraRestClient = Mockito.mock(ProcessorAsynchJiraRestClientFactory.class);
		PowerMockito.whenNew(ProcessorAsynchJiraRestClientFactory.class).withAnyArguments().thenReturn(jiraRestClient);
		Mockito.when(jiraRestClient.createWithBasicHttpAuthentication(Mockito.any(URI.class), Mockito.anyString(),
				Mockito.anyString(), Mockito.any(JiraProcessorConfig.class))).thenReturn(restClient);
		Assert.assertEquals(restClient.getClass().getSuperclass(),
				jiraRestClientFactory.getJiraClient(jiraInfo).getClass());
	}

	@Test
	public void cacheRestClient() throws Exception {
		Mockito.when(jiraProcessorConfig.getCustomApiBaseUrl()).thenReturn("http://localhost:9090/");
		PowerMockito.whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = new ResponseEntity<>("Success", HttpStatus.OK);
		Mockito.when(restTemplate.exchange(new URI("http://localhost:9090/api/cache/clearCache/GenericCache"),
				HttpMethod.GET, entity, String.class)).thenReturn(response);
		assertEquals(false, jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.JIRA_KPI_CACHE));
	}

	@Test
	public void cacheRestClientResponseNull() throws Exception {
		Mockito.when(jiraProcessorConfig.getCustomApiBaseUrl()).thenReturn("http://localhost:9090/");
		PowerMockito.whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		Mockito.when(restTemplate.exchange(new URI("http://localhost:9090/api/cache/clearCache/GenericCache"),
				HttpMethod.GET, entity, String.class)).thenReturn(null);
		assertEquals(false, jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.JIRA_KPI_CACHE));
	}

	private void prepareJiraInfo() {
		jiraInfo = JiraInfo.builder().build();
		jiraInfo.setUsername(USERNAME);
		jiraInfo.setPassword(AES_ENCRYPTED_PASSWORD);
		jiraInfo.setJiraConfigBaseUrl("https://www.abc.com//jira/");
	}

	private void prepareJiraInfoProxyURL() {
		jiraInfo = JiraInfo.builder().build();
		jiraInfo.setUsername(USERNAME);
		jiraInfo.setPassword(AES_ENCRYPTED_PASSWORD);
		jiraInfo.setJiraConfigBaseUrl("https://www.abc.com/jira/");
		jiraInfo.setJiraConfigProxyPort("8888");
		jiraInfo.setJiraConfigProxyUrl("https://user.proxyurl.com");
	}
}
