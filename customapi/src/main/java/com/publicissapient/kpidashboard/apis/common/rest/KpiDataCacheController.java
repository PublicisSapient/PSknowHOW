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

package com.publicissapient.kpidashboard.apis.common.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;

/**
 * REST controller managing all cache request
 *
 * @author prijain3
 */
@RestController
public class KpiDataCacheController {

	@Autowired
	private KpiDataCacheService service;

	/**
	 * Clear Specified cache.
	 *
	 * @param kpiId
	 *          the cache name
	 */
	@RequestMapping(value = "/cache/kpi/{kpiId}/clear", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public void clearCache(@PathVariable String kpiId) {
		service.clearCache(kpiId);
	}

	/**
	 * Clear Specified cache.
	 *
	 * @param kpiId
	 *          the cache name
	 */
	@RequestMapping(value = "/cache/project/{projectId}/kpi/{kpiId}/clear", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public void clearCache(@PathVariable("projectId") String basicProjectConfigId, @PathVariable("kpiId") String kpiId) {
		service.clearCache(basicProjectConfigId, kpiId);
	}

	/**
	 * Clear Specified cache.
	 *
	 * @param basicProjectConfigId
	 *          the project basic config id
	 */
	@RequestMapping(value = "/cache/project/{projectId}/clear", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public void clearCacheForProject(@PathVariable("projectId") String basicProjectConfigId) {
		service.clearCacheForProject(basicProjectConfigId);
	}

	/**
	 * Clear Specified cache.
	 *
	 * @param source
	 *          kpi source
	 */
	@RequestMapping(value = "/cache/source/{source}/clear", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public void clearCacheForSource(@PathVariable("source") String source) {
		service.clearCacheForSource(source);
	}

	/**
	 * Clear Specified cache.
	 *
	 * @param source
	 *          kpi source
	 */
	@RequestMapping(value = "/cache/project/{projectId}/source/{source}/clear", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public void clearCacheForProjectAndSource(@PathVariable("source") String source,
			@PathVariable("projectId") String projectId) {
		List<String> kpiList = service.getKpiBasedOnSource(source);
		kpiList.forEach(kpiId -> service.clearCache(projectId, kpiId));
	}
}
