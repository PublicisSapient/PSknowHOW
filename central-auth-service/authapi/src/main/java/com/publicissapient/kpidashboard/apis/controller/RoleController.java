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
package com.publicissapient.kpidashboard.apis.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.service.RoleService;
import com.publicissapient.kpidashboard.common.model.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller for handling Role APIs
 *
 * @author hargupta15
 */
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/roles")
@SuppressWarnings("java:S3740")
public class RoleController {

	private final RoleService roleService;

	/**
	 * Fetch all roles data.
	 *
	 * @return the roles
	 */
	@GetMapping(value = "/{resourceName}")
	public ResponseEntity<ServiceResponse> getAllRoles(@PathVariable String resourceName) {
		log.info("Fetching all roles");
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponse<>(true, "", roleService.getResourceAllRoles(resourceName)));
	}

}