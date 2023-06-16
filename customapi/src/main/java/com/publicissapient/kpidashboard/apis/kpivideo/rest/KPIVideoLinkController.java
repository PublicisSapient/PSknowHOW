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

package com.publicissapient.kpidashboard.apis.kpivideo.rest;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.kpivideo.service.KPIVideoLinkService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.kpivideolink.KPIVideoLink;
import com.publicissapient.kpidashboard.common.model.kpivideolink.KPIVideoLinkDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Rest Controller for all KPI video links requests.
 *
 * 
 */
@RestController
@RequestMapping("/kpi")
@Slf4j
public class KPIVideoLinkController {

	@Autowired
	private KPIVideoLinkService kpiLinksService;

	/**
	 * Modify/Update a kpi link by kpiId.
	 * 
	 * @param kpiVideoLinkDTO
	 *            request object that replaces the kpi link data present at id.
	 *
	 * @return responseEntity with data,message and status
	 */
	@RequestMapping(value = "/{kpiId}/video", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> modifyKpiVideoLinkById(@PathVariable String kpiId,
			@Valid @RequestBody KPIVideoLinkDTO kpiVideoLinkDTO) {
		log.info("link@{} updated", kpiId);
		final ModelMapper modelMapper = new ModelMapper();
		final KPIVideoLink kvl = modelMapper.map(kpiVideoLinkDTO, KPIVideoLink.class);
		return ResponseEntity.status(HttpStatus.OK).body(kpiLinksService.update(kpiId, kvl));
	}

}
