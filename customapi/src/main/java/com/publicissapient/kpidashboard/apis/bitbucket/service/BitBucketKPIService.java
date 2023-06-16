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

package com.publicissapient.kpidashboard.apis.bitbucket.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.publicissapient.kpidashboard.apis.common.service.ApplicationKPIService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.common.service.ToolsKPIService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.model.application.Tool;

/**
 * Bitbucket Kpi service.
 * 
 * @param <R>
 *            type of kpi value
 * @param <S>
 *            type of kpi trend object
 * @param <T>
 *            type of db object
 */
public abstract class BitBucketKPIService<R, S, T> extends ToolsKPIService<R, S>
		implements ApplicationKPIService<R, S, T> {

	private static final String CONNECTOR = " -> ";
	@Autowired
	private CacheService cacheService;
	@Autowired
	private CommonService commonService;

	public abstract String getQualifierType();

	/**
	 * Returns API Request tracker Id to be used for logging/debugging and using it
	 * for maintaining any sort of cache.
	 * 
	 * @return
	 */
	protected String getRequestTrackerId() {
		return cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.BITBUCKET.name());
	}

	protected String getRequestTrackerIdKanban() {
		return cacheService
				.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.BITBUCKETKANBAN.name());
	}

	/**
	 * Calculates the number of commits per day.
	 * 
	 * @param kpiRequest
	 * @param kpiElement
	 * @return
	 * @throws ApplicationException
	 */
	public abstract KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException;

	/**
	 * This method creates branch filters for kpis
	 * 
	 * @param repo
	 *            tool repo
	 * @param projectName
	 *            projectName
	 * @return branch filter
	 */
	public String getBranchSubFilter(Tool repo, String projectName) {
		String subfilter = "";
		if (null != repo.getRepoSlug()) {
			subfilter = repo.getBranch() + CONNECTOR + repo.getRepoSlug() + CONNECTOR + projectName;
		} else if (null != repo.getRepositoryName()) {
			subfilter = repo.getBranch() + CONNECTOR + repo.getRepositoryName() + CONNECTOR + projectName;
		} else {
			subfilter = repo.getBranch() + CONNECTOR + projectName;
		}
		return subfilter;
	}
}
