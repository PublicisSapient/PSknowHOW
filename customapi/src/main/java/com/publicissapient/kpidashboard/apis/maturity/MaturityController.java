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

package com.publicissapient.kpidashboard.apis.maturity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.pushdata.model.ExposeApiToken;
import com.publicissapient.kpidashboard.apis.pushdata.service.AuthExposeAPIService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author kunkambl
 */
@RestController
@Slf4j
public class MaturityController {

    @Autowired
    private AuthExposeAPIService authExposeAPIService;

    @Autowired
    private MaturityServiceImpl maturityService;

    /**
     * This method handles Jira Scrum KPIs request.
     *
     * @param kpiRequest
     * @return List of KPIs with trend and aggregated data.
     * @throws Exception
     */
	@GetMapping(value = "/maturityValues", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<List<KpiElement>> getMaturityValues(HttpServletRequest request,
			@NotNull @RequestBody KpiRequest kpiRequest) {
		log.info("Received {} request for /maturityValues", request.getMethod());
		ExposeApiToken exposeApiToken = authExposeAPIService.validateToken(request);
		if (Objects.isNull(exposeApiToken)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
		}
		List<KpiElement> responseList = maturityService.getMaturityValues(kpiRequest);
		if (responseList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseList);
		} else {
			return ResponseEntity.ok().body(responseList);
		}
	}

}
