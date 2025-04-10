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

package com.publicissapient.kpidashboard.apis.hierarchy.integration.controller;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.hierarchy.integration.dto.HierarchyDetails;
import com.publicissapient.kpidashboard.apis.hierarchy.integration.helper.ReaderRetryHelper;
import com.publicissapient.kpidashboard.apis.hierarchy.integration.service.HierarchyDetailParser;
import com.publicissapient.kpidashboard.apis.hierarchy.integration.service.IntegrationService;
import com.publicissapient.kpidashboard.apis.hierarchy.integration.service.SF360Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class IntegrateHierarchyScheduler {

    @Autowired
    private IntegrationService integrationService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private ReaderRetryHelper retryHelper;
    @Autowired
    private CustomApiConfig customApiConfig;

    public void callApi() {
        String apiUrl = customApiConfig.getCentralHierarchyUrl();

        HttpHeaders headers = new HttpHeaders();
        // add x-api-keyx
        headers.add("x-api-key", customApiConfig.getCentralHierarchyApiKey());

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ReaderRetryHelper.RetryableOperation<ResponseEntity<String>> retryableOperation = () -> restTemplate
                .exchange(apiUrl, HttpMethod.GET, requestEntity, String.class);

        try {
            ResponseEntity<String> response = retryHelper.executeWithRetry(retryableOperation);
            if (response.getStatusCode().is2xxSuccessful()) {
                HierarchyDetailParser hierarchyDetailParser = new SF360Parser();
                HierarchyDetails hierarchyDetails = hierarchyDetailParser.convertToHierachyDetail(response.getBody());
                integrationService.convertHieracyResponseToOrganizationHierachy(hierarchyDetails);
            } else {
                throw new HttpServerErrorException(response.getStatusCode(), "API call failed");
            }
        } catch (Exception e) {
            log.error("API call failed after retries. Error: {}", e.getMessage());
        }
    }
}
