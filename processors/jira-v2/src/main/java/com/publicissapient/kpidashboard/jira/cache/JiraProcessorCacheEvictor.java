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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 *
 */
@Service
@Slf4j
public class JiraProcessorCacheEvictor {

	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;

	/**
	 * @param cacheEndPoint
	 *            cacheEndPoint
	 * @param cacheName
	 *            cacheName
	 * @return boolean
	 */
	public boolean evictCache(String cacheEndPoint, String cacheName) {
		boolean cleaned = false;
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(jiraProcessorConfig.getCustomApiBaseUrl());
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
			log.error("[JIRA-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			cleaned = true;
			log.info("[JIRA-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache {}", cacheName);
		} else {
			log.error("[JIRA-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache {}", cacheName);
		}
		return cleaned;
	}

}
