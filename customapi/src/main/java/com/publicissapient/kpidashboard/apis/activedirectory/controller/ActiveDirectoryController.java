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
package com.publicissapient.kpidashboard.apis.activedirectory.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.activedirectory.service.ADServerDetailsService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;

/**
 * @author sansharm13
 *
 */
@RestController
@RequestMapping("/activedirectory")
public class ActiveDirectoryController {

	@Autowired
	private ADServerDetailsService adServerDetailsService;

	/**
	 * This api saves active directory User Details.
	 * 
	 * @param adServerDetails
	 *            data to be saved
	 * @return service response entity
	 */
	@PostMapping
	@PreAuthorize("hasPermission(null,'SAVE_AD_SETTING')")
	public ResponseEntity<ServiceResponse> addADDetails(@RequestBody ADServerDetail adServerDetails) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(adServerDetailsService.addUpdateADServerDetails(adServerDetails));

	}

	/**
	 * This api gives active directory User Details.
	 * 
	 * @return service response entity
	 */
	@GetMapping
	@PreAuthorize("hasPermission(null,'GET_AD_SETTING')")
	public ResponseEntity<ServiceResponse> getADServerDetails() {
		return ResponseEntity.status(HttpStatus.OK).body(adServerDetailsService.getADServerDetails());
	}
}
