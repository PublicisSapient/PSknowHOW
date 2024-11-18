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

package com.publicissapient.kpidashboard.apis.jira.service;

import com.publicissapient.kpidashboard.apis.common.service.ApplicationKPIService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.ToolsKPIService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is extention of ApplicationKPIService. All Jira KPIs service have
 * to implement this class {@link ApplicationKPIService}
 *
 * @param <R>
 *            KPIs calculated value type
 * @param <S>
 *            Maturity Value Type not applicable in every case
 * @param <T>
 *            Bind DB data with type
 * @author tauakram
 */
public abstract class JiraKPIService<R, S, T> extends ToolsKPIService<R, S> implements ApplicationKPIService<R, S, T> {

	public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String BASIC_PROJECT_CONFIG_ID = "basicProjectConfigId";
	@Autowired
	private CacheService cacheService;
	@Autowired
	private JiraServiceR jiraService;

	/**
	 * Gets qualifier type
	 *
	 * @return qualifier type
	 */
	public abstract String getQualifierType();

	/**
	 * Gets Kpi data based on kpi request
	 *
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return kpi data
	 * @throws ApplicationException
	 */
	public abstract KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException;

	/**
	 * Returns API Request tracker Id to be used for logging/debugging and using it
	 * for maintaining any sort of cache.
	 *
	 * @return Scrum Request Tracker Id
	 */
	public String getRequestTrackerId() {
		return cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name());
	}

	/**
	 * Returns API Request tracker Id to be used for logging/debugging and using it
	 * for maintaining any sort of cache.
	 *
	 * @return Kanban Request Tracker Id
	 */
	public String getKanbanRequestTrackerId() {
		return cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRAKANBAN.name());
	}

	public Map<String, Double> getLastNMonth(int count) {
		Map<String, Double> lastNMonth = new LinkedHashMap<>();
		DateTime currentDate = DateTime.now();
		String currentDateStr = currentDate.getYear() + Constant.DASH + currentDate.getMonthOfYear();
		lastNMonth.put(currentDateStr, 0.0);
		DateTime lastMonth = DateTime.now();
		for (int i = 1; i < count; i++) {
			lastMonth = lastMonth.minusMonths(1);
			String lastMonthStr = lastMonth.getYear() + Constant.DASH + lastMonth.getMonthOfYear();
			lastNMonth.put(lastMonthStr, 0.0);

		}
		return lastNMonth;
	}

	/**
	 * to maintain values upto 2 places of decimal
	 *
	 * @param value
	 * @return
	 */
	public double roundingOff(double value) {
		return (double) Math.round(value * 100) / 100;
	}

}
