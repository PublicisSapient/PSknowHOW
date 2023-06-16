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

package com.publicissapient.kpidashboard.apis.kpis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.model.MasterResponse;

/**
 * Rest Controller for all kpi master requests.
 *
 * @author prigupta8
 */
@RestController
@RequestMapping("/masterData")
public class KPIMasterController {

	private final KpiHelperService kPIHelperService;

	/**
	 * Instantiates a new Kpi master controller.
	 *
	 * @param kPIHelperService
	 *            the k pi helper service
	 */
	@Autowired
	public KPIMasterController(KpiHelperService kPIHelperService) {
		this.kPIHelperService = kPIHelperService;
	}

	/**
	 * Fetch master data master response.
	 *
	 * @return the master response
	 */
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public MasterResponse fetchMasterData() {
		return kPIHelperService.fetchKpiMasterList();
	}
}
