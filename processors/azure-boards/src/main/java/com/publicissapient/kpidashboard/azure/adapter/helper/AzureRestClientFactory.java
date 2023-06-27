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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.publicissapient.kpidashboard.azure.config.AzureProcessorConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AzureRestClientFactory {
	@Autowired
	private AzureProcessorConfig azureProcessorConfig;

	/**
	 * Cleans the cache in th Custom API
	 * 
	 * @param cacheEndPoint
	 *            URL end point where Custom API cache is created
	 * @param cacheName
	 *            Name of the Custom API cache
	 */
	public void cacheRestClient(String cacheEndPoint, String cacheName) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(azureProcessorConfig.getCustomApiBaseUrl());
		uriBuilder.path("/");
		uriBuilder.path(cacheEndPoint);
		uriBuilder.path("/");
		uriBuilder.path(cacheName);

		HttpEntity<?> entity = new HttpEntity<>(headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, String.class);
		} catch (RuntimeException e) {
			log.error("[AZURE-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service {}", e.getMessage());
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			log.info("[AZURE-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache {}", cacheName);
		} else {
			log.error("[AZURE-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache {}", cacheName);
		}
	}
}