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

package com.publicissapient.kpidashboard.azure.adapter.helper;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.azure.adapter.impl.async.ProcessorAzureRestClient;
import com.publicissapient.kpidashboard.azure.config.AzureProcessorConfig;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

@ExtendWith(SpringExtension.class)
public class AzureRestClientFactoryTest {

	@Mock
	AzureProcessorConfig azureProcessorConfig;
	@InjectMocks
	AzureRestClientFactory azureRestClientFactory;

	@Mock
	RestTemplate restTemplate;

	@Mock
	ProcessorAzureRestClient restClient;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void cacheRestClient() throws Exception { // NOSONAR
		Mockito.when(azureProcessorConfig.getCustomApiBaseUrl()).thenReturn("http://localhost:8080/");
		PowerMockito.whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = new ResponseEntity<>("Success", HttpStatus.OK);
		Mockito.when(restTemplate.exchange(ArgumentMatchers.any(URI.class), ArgumentMatchers.eq(HttpMethod.GET),
				ArgumentMatchers.eq(entity), ArgumentMatchers.eq(String.class))).thenReturn(response);
		azureRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.AZURE_KPI_CACHE);
	}

	@Test
	public void cacheRestClientResponseNull() throws Exception { // NOSONAR
		Mockito.when(azureProcessorConfig.getCustomApiBaseUrl()).thenReturn("http://localhost:8080/");
		PowerMockito.whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		Mockito.when(restTemplate.exchange(new URI("http://localhost:8080/api/cache/clearCache/azureKpiCache"),
				HttpMethod.GET, entity, String.class)).thenReturn(null);
		azureRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.AZURE_KPI_CACHE);
	}
}
