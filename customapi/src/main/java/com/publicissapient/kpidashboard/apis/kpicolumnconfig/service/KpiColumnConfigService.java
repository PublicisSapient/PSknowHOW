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

package com.publicissapient.kpidashboard.apis.kpicolumnconfig.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.KpiColumnConfigDTO;

public interface KpiColumnConfigService {

	/**
	 * Create and save KpiConfig
	 *
	 * @param kpiColumnConfigDTO*
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is saved else false
	 */
	ServiceResponse saveKpiColumnConfig(KpiColumnConfigDTO kpiColumnConfigDTO);

	/**
	 * Gets Config of columns for kpi
	 *
	 * @param basicProjectConfigId*
	 * @param kpiId*
	 * @return config of kpi column of that project if exist else return default
	 *         config of kpi where basicProjectConfigId is null.
	 */
	KpiColumnConfigDTO getByKpiColumnConfig(String basicProjectConfigId, String kpiId);
}
