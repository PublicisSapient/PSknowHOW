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

package com.publicissapient.kpidashboard.apis.capacity.rest;

import javax.validation.Valid;

import com.publicissapient.kpidashboard.apis.abac.ContextAwarePolicyEnforcement;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.capacity.service.CapacityMasterService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.constant.Role;
import com.publicissapient.kpidashboard.common.model.application.CapacityMaster;

@RestController
@RequestMapping("/assignee")
@Slf4j
public class ProjectAssigneeController {

	@Autowired
	CapacityMasterService capacityMasterService;

	@Autowired
	private ContextAwarePolicyEnforcement policy;

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> saveOrUpdateAssignee(@Valid @RequestBody CapacityMaster capacityMaster) {
		policy.checkPermission(capacityMaster, "SAVE_UPDATE_CAPACITY");
		ServiceResponse response = new ServiceResponse(false, "Failed to add Capacity Data", null);
		try {
			capacityMaster = capacityMasterService.processCapacityData(capacityMaster);
			if (null != capacityMaster) {
				response = new ServiceResponse(true, "Successfully added Capacity Data", capacityMaster);
			}
		} catch (AccessDeniedException ade) {
			response = new ServiceResponse(false, "Unauthorized", null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/roles")
	public ResponseEntity<ServiceResponse> assigneeRolesSuggestion() {
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(true, "All Roles", Role.getAllRoles()));
	}

}