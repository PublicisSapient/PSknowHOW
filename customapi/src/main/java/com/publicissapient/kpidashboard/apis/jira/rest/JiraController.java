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

package com.publicissapient.kpidashboard.apis.jira.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import javax.validation.constraints.NotNull;

import com.publicissapient.kpidashboard.apis.jira.model.BoardDetailsDTO;
import com.publicissapient.kpidashboard.apis.jira.model.BoardRequestDTO;
import com.publicissapient.kpidashboard.apis.jira.service.JiraToolConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceKanbanR;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;

/**
 * This controller class handles Jira KPIs request. It handles all KPIs of Scrum
 * and Kanban.
 * 
 * @author tauakram
 */

@RestController
public class JiraController {

	private static final Logger LOGGER = LoggerFactory.getLogger(JiraController.class);

	@Autowired
	private JiraServiceR jiraService;

	@Autowired
	private JiraServiceKanbanR jiraServiceKanban;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private JiraToolConfigServiceImpl jiraToolConfigService;

	/**
	 * This method handles Jira Scrum KPIs request.
	 * 
	 * @param kpiRequest
	 * @return List of KPIs with trend and aggregated data.
	 * @throws Exception
	 */
	@RequestMapping(value = "/jira/kpi", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE) // NOSONAR
	//@PreAuthorize("hasPermission(null,'KPI_FILTER')")
	public ResponseEntity<List<KpiElement>> getJiraAggregatedMetrics(@NotNull @RequestBody KpiRequest kpiRequest)
			throws Exception {// NOSONAR

		MDC.put("JiraScrumKpiRequest", kpiRequest.getRequestTrackerId());
		LOGGER.info("Received Jira KPI request {}", kpiRequest);

		long jiraRequestStartTime = System.currentTimeMillis();
		MDC.put("JiraRequestStartTime", String.valueOf(jiraRequestStartTime));
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name(),
				kpiRequest.getRequestTrackerId());

		if (CollectionUtils.isEmpty(kpiRequest.getKpiList())) {
			throw new MissingServletRequestParameterException("kpiList", "List");
		}

		List<KpiElement> responseList = jiraService.process(kpiRequest);
		MDC.put("TotalJiraRequestTime", String.valueOf(System.currentTimeMillis() - jiraRequestStartTime));

		LOGGER.info("");
		MDC.clear();
		if (responseList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseList);
		} else {
			return ResponseEntity.ok().body(responseList);
		}
	}

	/**
	 * This method handles Jira Kanban KPIs request.
	 * 
	 * @param kpiRequest
	 * @return List of KPIs with trend and aggregated data.
	 * @throws Exception
	 */
	@RequestMapping(value = "/jirakanban/kpi", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE) // NOSONAR
	//@PreAuthorize("hasPermission(null,'KPI_FILTER')")
	public ResponseEntity<List<KpiElement>> getJiraKanbanAggregatedMetrics(@NotNull @RequestBody KpiRequest kpiRequest)
			throws Exception { // NOSONAR
		MDC.put("JiraScrumKpiRequest", kpiRequest.getRequestTrackerId());
		LOGGER.info("Received Jira Kanban KPI request {}", kpiRequest);
		long jiraKanbanRequestStartTime = System.currentTimeMillis();
		MDC.put("JiraKanbanRequestStartTime", String.valueOf(jiraKanbanRequestStartTime));
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRAKANBAN.name(),
				kpiRequest.getRequestTrackerId());

		if (CollectionUtils.isEmpty(kpiRequest.getKpiList())) {
			throw new MissingServletRequestParameterException("kpiList", "List");
		} 

		List<KpiElement> responseList = jiraServiceKanban.process(kpiRequest);
		MDC.put("TotalJiraKanbanRequestTime", String.valueOf(System.currentTimeMillis() - jiraKanbanRequestStartTime));

		LOGGER.info("");
		MDC.clear();

		if (responseList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseList);
		} else {
			return ResponseEntity.ok().body(responseList);
		}
	}

	@PostMapping(value = "/jira/board", produces = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse getJiraBoardDetailsList(@RequestBody BoardRequestDTO boardRequestDTO) {
		ServiceResponse response;
		List<BoardDetailsDTO> boardDetailsList = jiraToolConfigService.getJiraBoardDetailsList(boardRequestDTO);
		if (CollectionUtils.isEmpty(boardDetailsList)) {
			response = new ServiceResponse(false,
					"Not found any configure board details with provided connection details", null);
		} else {
			response = new ServiceResponse(true, "Successfully fetched board details list", boardDetailsList);
		}
		return response;
	}
}
