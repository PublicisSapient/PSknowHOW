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

package com.publicissapient.kpidashboard.apis.kpicolumnconfig.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.kpicolumnconfig.service.KpiColumnConfigService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.KpiColumnConfigDTO;

@RestController
@RequestMapping("/kpi-column-config")
public class KpiColumnConfigController {

	@Autowired
	KpiColumnConfigService kpiColumnConfigService;

	/**
	 * Api to get kpi column configurations
	 *
	 * @return response
	 */
	@GetMapping(value = "/{basicProjectConfigId}/{kpiId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getKpiColumnConfiguration(@PathVariable String basicProjectConfigId,
			@PathVariable String kpiId) {
		KpiColumnConfigDTO kpiColumnConfigDTO = kpiColumnConfigService.getByKpiColumnConfig(basicProjectConfigId,
				kpiId);
		ServiceResponse response = new ServiceResponse(false, "No data found", null);
		if (null != kpiColumnConfigDTO) {
			response = new ServiceResponse(true, "Fetched successfully", kpiColumnConfigDTO);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * Api to save kpi column config
	 *
	 * @param kpiColumnConfigDTO *
	 * @return response
	 */
	@PostMapping(value = "/kpiColumnConfig", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> saveKpiColumnConfig(
			@Valid @RequestBody KpiColumnConfigDTO kpiColumnConfigDTO) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(kpiColumnConfigService.saveKpiColumnConfig(kpiColumnConfigDTO));
	}
}
