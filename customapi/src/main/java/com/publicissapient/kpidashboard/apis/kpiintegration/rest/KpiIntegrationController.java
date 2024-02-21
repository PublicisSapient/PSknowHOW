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

package com.publicissapient.kpidashboard.apis.kpiintegration.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.kpiintegration.service.KpiIntegrationServiceImpl;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * exposed API for fetching kpi data
 * @author kunkambl
 */
@RestController
@Slf4j
public class KpiIntegrationController {

	private static final String TOKEN_KEY = "X-Api-Key";

    @Autowired
    private KpiIntegrationServiceImpl kpiIntegrationService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private RestAPIUtils restAPIUtils;

    /**
     * This method handles Scrum KPIs request.
     *
     * @param kpiRequest kpi request object
     * @return List of KPIs with trend and aggregated data.
     */
	@PostMapping(value = "/kpiIntegrationValues", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<List<KpiElement>> getMaturityValues(HttpServletRequest request,
			@NotNull @RequestBody KpiRequest kpiRequest) {
		log.info("Received {} request for /kpiIntegrationValues", request.getMethod());
		Boolean isApiAuth = restAPIUtils.decryptPassword(customApiConfig.getxApiKey())
				.equalsIgnoreCase(request.getHeader(TOKEN_KEY));
		if (Boolean.FALSE.equals(isApiAuth)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
		}
		List<KpiElement> responseList = kpiIntegrationService.getKpiResponses(kpiRequest);
		if (responseList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseList);
		} else {
			return ResponseEntity.ok().body(responseList);
		}
	}


}
