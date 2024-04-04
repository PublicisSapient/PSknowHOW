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
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.common.service.ConfigDetailService;
import com.publicissapient.kpidashboard.apis.model.ConfigDetails;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Rest controller to handle configuration properties
 */
@Slf4j
@RestController
public class ConfigDetailsController {

	private final ConfigDetailService configDetailService;

	@Autowired
	public ConfigDetailsController(ConfigDetailService configDetailService) {
		this.configDetailService = configDetailService;
	}

	/**
	 * Returns required properties from application.prop
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/configDetails", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ConfigDetails> getConfigDetails(HttpServletRequest request) {
		log.info("ConfigDetailsController::getConfigDetails start");
		ConfigDetails configDetails = configDetailService.getConfigDetails();
		log.info("ConfigDetailsController::getConfigDetails end");
		return ResponseEntity.status(HttpStatus.OK).body(configDetails);
	}

}
