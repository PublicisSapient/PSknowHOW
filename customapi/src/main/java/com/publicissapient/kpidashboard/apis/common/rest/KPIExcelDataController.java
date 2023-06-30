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

import java.util.Arrays;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.appsetting.service.KPIExcelDataService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelValidationDataResponse;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * This class fetches KPI data for received filter. This API is used by
 * Application Dashboard to fetch Excel Data.
 *
 * @author tauakram
 */
@Slf4j
@RestController
public class KPIExcelDataController {

	@Autowired
	private KPIExcelDataService kpiExcelDataService;

	/**
	 * Fetches KPI validation data (story keys, defect keys) for a specific KPI id.
	 *
	 * @param kpiRequest
	 *            the kpi request
	 * @param kpiID
	 *            the kpi id
	 * @return validation kpi data
	 */
	@RequestMapping(value = "/v1/kpi/{kpiID}", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<KPIExcelValidationDataResponse> getValidationKPIData(
			@NotNull @RequestBody KpiRequest kpiRequest, @NotNull @PathVariable("kpiID") String kpiID) {

		String kpiRequestStr = kpiRequest.toString();
		kpiID = CommonUtils.handleCrossScriptingTaintedValue(kpiID);
		kpiRequestStr = CommonUtils.handleCrossScriptingTaintedValue(kpiRequestStr);
		log.info("[KPI-EXCEL-DATA][]. Received Specific Excel KPI Data request for {} with kpiRequest {}", kpiID,
				kpiRequestStr);

		KPIExcelValidationDataResponse responseList = (KPIExcelValidationDataResponse) kpiExcelDataService
				.process(kpiID, kpiRequest.getLevel(), Arrays.asList(kpiRequest.getIds()), null, kpiRequest, null);
		return ResponseEntity.ok().body(responseList);
	}

}
