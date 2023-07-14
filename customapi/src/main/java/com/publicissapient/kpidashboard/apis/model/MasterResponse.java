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

package com.publicissapient.kpidashboard.apis.model;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.application.KpiMaster;

/**
 * Represents Master response.
 *
 * @author prigupta8
 */
public class MasterResponse {
	private List<KpiMaster> kpiList;

	/**
	 * Instantiates a new Master response.
	 */
	public MasterResponse() {
	}

	/**
	 * Instantiates a new Master response.
	 *
	 * @param kpiList
	 *            the kpi list
	 */
	public MasterResponse(List<KpiMaster> kpiList) {
		this.kpiList = kpiList;
	}

	/**
	 * Gets kpi list.
	 *
	 * @return the kpi list
	 */
	public List<KpiMaster> getKpiList() {
		return kpiList;
	}

	/**
	 * Sets kpi list.
	 *
	 * @param kpiList
	 *            the kpi list
	 */
	public void setKpiList(List<KpiMaster> kpiList) {
		this.kpiList = kpiList;
	}
}
