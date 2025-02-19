/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.List;

import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;

/**
 * @author purgupta2
 */
public interface JiraNonTrendKPIServiceR {

	/**
	 * @param kpiRequest
	 *          kpiRequest
	 * @return List of KpiElement
	 * @throws EntityNotFoundException
	 *           EntityNotFoundException
	 */
	List<KpiElement> process(KpiRequest kpiRequest) throws EntityNotFoundException;

	/**
	 * This method is called when the request for kpi is done from exposed API
	 *
	 * @param kpiRequest
	 *          JIRA KPI request true if flow for precalculated, false for direct
	 *          flow.
	 * @return List of KPI data
	 * @throws EntityNotFoundException
	 *           EntityNotFoundException
	 */
	List<KpiElement> processWithExposedApiToken(KpiRequest kpiRequest) throws EntityNotFoundException;
}
