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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Validation Data response. Variables to be added to serve the need of other
 * KPI's.
 *
 * @author tauakram
 */
@JsonInclude(Include.NON_NULL)
public class KPIExcelValidationDataResponse {

	private String kpiName;
	private String kpiId;
	@JsonProperty("columns")
	private List<String> excelColumns;
	@JsonProperty("excelData")
	private List<KPIExcelData> excelData;

	public List<String> getExcelColumns() {
		return excelColumns;
	}

	public void setExcelColumns(List<String> excelColumns) {
		this.excelColumns = excelColumns;
	}

	/**
	 * Gets kpi name.
	 *
	 * @return the kpi name
	 */
	public String getKpiName() {
		return kpiName;
	}

	/**
	 * Sets kpi name.
	 *
	 * @param kpiName
	 *            the kpi name
	 */
	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}

	/**
	 * Gets kpi id.
	 *
	 * @return the kpi id
	 */
	public String getKpiId() {
		return kpiId;
	}

	/**
	 * Sets kpi id.
	 *
	 * @param kpiId
	 *            the kpi id
	 */
	public void setKpiId(String kpiId) {
		this.kpiId = kpiId;
	}

	public List<KPIExcelData> getExcelData() {
		return excelData;
	}

	public void setExcelData(List<KPIExcelData> excelData) {
		this.excelData = excelData;
	}

}
