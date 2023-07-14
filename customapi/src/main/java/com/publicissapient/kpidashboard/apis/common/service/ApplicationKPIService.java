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

package com.publicissapient.kpidashboard.apis.common.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;

/**
 * This is generic service which every KPI has to implement.
 * 
 * @param <R>
 *            type of kpi value
 * @param <S>
 *            type of kpi trend object
 * @param <T>
 *            type of db object
 * @author tauakram
 *
 */
@Component
public interface ApplicationKPIService<R, S, T> {

	/**
	 * Calculates KPI Metrics
	 * 
	 * @param t
	 *            type of db object
	 * @return KPI value
	 */
	R calculateKPIMetrics(T t);// NOPMD

	/**
	 * Calculates trend value of a KPI.
	 * 
	 * @param t
	 * @return null
	 */
	default S calculateTrendMetrics(T t) {// NOPMD
		return null;
	}

	/**
	 * Fetches KPI Data from DB
	 * 
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return KPI value
	 * @return
	 */
	T fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate, KpiRequest kpiRequest);
}
