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

package com.publicissapient.kpidashboard.apis.sonar.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarToolConfigServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarServiceKanbanR;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarServiceR;

import lombok.extern.slf4j.Slf4j;

/**
 * @author tauakram
 */

@RestController
@Slf4j
public class SonarController {

	@Autowired
	private CacheService cacheService;

	@Autowired
	private SonarServiceR sonarService;

	@Autowired
	private SonarServiceKanbanR sonarServiceKanban;

	@Autowired
	private SonarToolConfigServiceImpl sonarToolConfigService;
	
	private static final String FETCHED_SUCCESSFULLY = "fetched successfully";

	/**
	 * Gets Sonar Aggregate Metrics for Scrum projects
	 * @param kpiRequest
	 * @return {@code ResponseEntity<List<KpiElement>>}
	 * @throws Exception
	 */
	@RequestMapping(value = "/sonar/kpi", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)//NOSONAR
	//@PreAuthorize("hasPermission(null,'KPI_FILTER')")
	public ResponseEntity<List<KpiElement>> getSonarAggregatedMetrics(@NotNull @RequestBody KpiRequest kpiRequest)
			throws Exception { //NOSONAR

		log.info("[SONAR][{}]. Received Sonar KPI request {}", kpiRequest.getRequestTrackerId(), kpiRequest);

		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.SONAR.name(),
				kpiRequest.getRequestTrackerId());

		if (CollectionUtils.isEmpty(kpiRequest.getKpiList())) {
			throw new MissingServletRequestParameterException("kpiList", "List");
		}

		List<KpiElement> responseList = sonarService.process(kpiRequest);
		if (responseList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseList);
		} else {
			return ResponseEntity.ok().body(responseList);
		}

	}
	/**
	 * Gets Sonar Aggregate Metrics for Kanban projects
	 * @param kpiRequest
	 * @return {@code ResponseEntity<List<KpiElement>>}
	 * @throws Exception
	 */
	@RequestMapping(value = "/sonarkanban/kpi", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)//NOSONAR
	public ResponseEntity<List<KpiElement>> getSonarKanbanAggregatedMetrics(@NotNull @RequestBody KpiRequest kpiRequest)
			throws Exception { //NOSONAR

		log.info("[SONAR KANBAN][{}]. Received Sonar KPI request {}", kpiRequest.getRequestTrackerId(), kpiRequest);

		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.SONARKANBAN.name(),
				kpiRequest.getRequestTrackerId());

		if (CollectionUtils.isEmpty(kpiRequest.getKpiList())) {
			throw new MissingServletRequestParameterException("kpiList", "List");
		}

		List<KpiElement> responseList = sonarServiceKanban.process(kpiRequest);
		if (responseList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseList);
		} else {
			return ResponseEntity.ok().body(responseList);
		}

	}

	/**
	 * Provides the list of Sonar version based on branches Support and type
	 *
	 * @return #{@code ServiceResponse}
	 */
	@GetMapping(value = "/sonar/version", produces = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse getSonarVersionList() {
		return sonarToolConfigService.getSonarVersionList();
	}

	/**
	 * Provides the list of Sonar Project's Key.
	 *
	 * @param connectionId
	 *            the Sonar connection details
	 * @param organizationKey
	 *            in case of Sonar Cloud
	 * @return @{@code ServiceResponse}
	 */
	@GetMapping(value = "/sonar/project/{connectionId}/{organizationKey}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse getSonarProjectList(@PathVariable String connectionId, @PathVariable String organizationKey) {
		ServiceResponse response;
		List<String> projectKeyList = sonarToolConfigService.getSonarProjectKeyList(connectionId, organizationKey);
		if (CollectionUtils.isEmpty(projectKeyList)) {
			response = new ServiceResponse(false,
					"Api version is not supported with provided connection details", null);
		} else {
			response = new ServiceResponse(true, FETCHED_SUCCESSFULLY, projectKeyList);
		}
		return response;
	}

	/**
	 * Provides the list of Sonar Project's Branch
	 * API call only if version is supported.
	 *
	 * @param connectionId
	 *            the Sonar server connection details
	 * @param version
	 *            the Sonar server api version
	 * @param projectKey
	 *            the Sonar server project's key
	 * @return @{@code ServiceResponse}
	 */
	@GetMapping(value = "/sonar/branch/{connectionId}/{version}/{projectKey}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse getSonarProjectBranchList(@PathVariable String connectionId, @PathVariable String version,
			@PathVariable String projectKey) {
		return sonarToolConfigService.getSonarProjectBranchList(connectionId, version, projectKey);
	}

}
