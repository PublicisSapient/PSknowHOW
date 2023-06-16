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

package com.publicissapient.kpidashboard.apis.appsetting.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.appsetting.service.GlobalConfigService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

/**
 * Controller for global configuration.
 * 
 * @author pansharm5
 *
 */
@RestController
@RequestMapping("/globalconfigurations")
public class GlobalConfigurationController {

	@Autowired
	private GlobalConfigService globalConfigService;

	@RequestMapping(value = "/zephyrcloudurl", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> getZephyrCloudUrl() {
		return ResponseEntity.status(HttpStatus.OK).body(globalConfigService.getZephyrCloudUrlDetails());

	}
}