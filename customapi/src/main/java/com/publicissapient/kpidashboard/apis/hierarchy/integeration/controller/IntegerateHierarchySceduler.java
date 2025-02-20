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

import com.publicissapient.kpidashboard.apis.hierarchy.integeration.dto.HierarchyDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.hierarchy.integeration.service.HierarchyDetailParser;
import com.publicissapient.kpidashboard.apis.hierarchy.integeration.service.IntegerationService;
import com.publicissapient.kpidashboard.apis.hierarchy.integeration.service.SF360Parser;

@Service
public class IntegerateHierarchySceduler {

	@Autowired
	private IntegerationService integerationService;
	@Autowired
	RestTemplate restTemplate;

	// @Scheduled(cron = "${hierarchySync.cron}")
	public void callApi() {
		String apiUrl = "https://hierarchy.tools.publicis.sapient.com/api/data/fetch/hierarchy/MAP/SF360Hierarchy";

		HttpHeaders headers = new HttpHeaders();
		//add x-api-key
	
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity,
					String.class);
			// json response to central hierarchy list
			HierarchyDetailParser hierarchyDetailParser = new SF360Parser();
			HierarchyDetails hierarchyDetails = hierarchyDetailParser.convertToHierachyDetail(response.getBody());
			integerationService
					.syncOrganizationHierarchy(integerationService.convertHieracyResponseToOrganizationHierachy(hierarchyDetails));
		} catch (HttpClientErrorException exception) {

		}
	}
}
