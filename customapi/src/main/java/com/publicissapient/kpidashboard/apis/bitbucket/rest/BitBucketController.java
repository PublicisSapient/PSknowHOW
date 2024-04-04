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

package com.publicissapient.kpidashboard.apis.bitbucket.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.bitbucket.service.BitBucketServiceKanbanR;
import com.publicissapient.kpidashboard.apis.bitbucket.service.BitBucketServiceR;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * Rest Controller to handle bit bucket specific requests.
 *
 * @author pkum34
 */
@Slf4j
@RestController
public class BitBucketController {

	@Autowired
	private BitBucketServiceR bitbucketService;

	@Autowired
	private BitBucketServiceKanbanR bitbucketServiceKanban;

	@Autowired
	private CacheService cacheService;

	/**
	 * Gets bit bucket aggregated metrics.
	 *
	 * @param kpiRequest
	 *            the kpi request
	 * @return the bit bucket aggregated metrics
	 * @throws Exception
	 *             the exception
	 */
	@RequestMapping(value = "/bitbucket/kpi", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<List<KpiElement>> getBitBucketAggregatedMetrics(@NotNull @RequestBody KpiRequest kpiRequest)
			throws Exception { // NOSONAR
		MDC.put("BitbucketKpiRequest", kpiRequest.getRequestTrackerId());
		log.info("Received BitBucket KPI request {}", kpiRequest);
		long bitbucketRequestStartTime = System.currentTimeMillis();
		MDC.put("BitbucketRequestStartTime", String.valueOf(bitbucketRequestStartTime));
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.BITBUCKET.name(),
				kpiRequest.getRequestTrackerId());

		if (CollectionUtils.isEmpty(kpiRequest.getKpiList())) {
			throw new MissingServletRequestParameterException("kpiList", "List");
		}

		List<KpiElement> responseList = bitbucketService.process(kpiRequest);
		MDC.put("TotalBitbucketRequestTime", String.valueOf(System.currentTimeMillis() - bitbucketRequestStartTime));

		log.info("");
		MDC.clear();
		if (responseList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseList);
		}
		return ResponseEntity.ok().body(responseList);

	}

	/**
	 * Gets bit bucket kanban aggregated metrics.
	 *
	 * @param kpiRequest
	 *            the kpi request
	 * @return the bit bucket kanban aggregated metrics
	 * @throws Exception
	 *             the exception
	 */
	@RequestMapping(value = "/bitbucketkanban/kpi", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<List<KpiElement>> getBitBucketKanbanAggregatedMetrics(
			@NotNull @RequestBody KpiRequest kpiRequest) throws Exception { // NOSONAR
		MDC.put("BitbucketKpiRequest", kpiRequest.getRequestTrackerId());
		log.info(" Received BitBucket KPI request {}", kpiRequest);
		long bitbucketKanbanRequestStartTime = System.currentTimeMillis();
		MDC.put("BitbucketKanbanRequestStartTime", String.valueOf(bitbucketKanbanRequestStartTime));
		cacheService.setIntoApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.BITBUCKETKANBAN.name(),
				kpiRequest.getRequestTrackerId());

		if (CollectionUtils.isEmpty(kpiRequest.getKpiList())) {
			throw new MissingServletRequestParameterException("kpiList", "List");
		}

		List<KpiElement> responseList = bitbucketServiceKanban.process(kpiRequest);
		MDC.put("TotalBitbucketKanbanRequestTime",
				String.valueOf(System.currentTimeMillis() - bitbucketKanbanRequestStartTime));

		log.info("");
		MDC.clear();
		if (responseList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseList);
		}
		return ResponseEntity.ok().body(responseList);

	}

}
