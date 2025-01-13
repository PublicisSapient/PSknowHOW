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

package com.publicissapient.kpidashboard.apis.hierarchy.integeration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.hierarchy.integeration.service.HierarchyDetailParser;
import com.publicissapient.kpidashboard.apis.hierarchy.integeration.service.IntegerationService;

public class IntegerateHierarchySceduler {

	@Autowired
	private HierarchyDetailParser hierarchyDetailParser;
	@Autowired
	private IntegerationService integerationService;
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	private CustomApiConfig customApiConfig;

	// @Scheduled(cron = "${hierarchySync.cron}")
	public void callApi() {
		String apiUrl = "http://example.com/api";
		HttpEntity<?> httpEntity = new HttpEntity<>(new HttpHeaders());
		try {
			ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, httpEntity, String.class);
			// json response to central hierarchy list
			hierarchyDetailParser.convertToHieracyDetail(response.getBody());

			integerationService
					.syncOrganizationHierarchy(integerationService.convertHieracyResponseToOrganizationHierachy());
		} catch (HttpClientErrorException exception) {

		}
	}
}
