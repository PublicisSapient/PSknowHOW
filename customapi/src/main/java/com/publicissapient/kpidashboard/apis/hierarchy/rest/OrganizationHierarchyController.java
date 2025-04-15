/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.hierarchy.rest;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.hierarchy.dto.CreateHierarchyRequest;
import com.publicissapient.kpidashboard.apis.hierarchy.dto.UpdateHierarchyRequest;
import com.publicissapient.kpidashboard.apis.hierarchy.service.HierarchyOptionService;
import com.publicissapient.kpidashboard.apis.hierarchy.service.OrganizationHierarchyService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/hierarchy")
public class OrganizationHierarchyController {

	@Autowired
	private OrganizationHierarchyService organizationHierarchyService;

	@Autowired
	private HierarchyOptionService hierarchyOptionService;

	@GetMapping
	public ResponseEntity<ServiceResponse> getHierarchyLevel() {

		List<OrganizationHierarchy> organizationHierarchies = organizationHierarchyService.findAll();

		if (CollectionUtils.isNotEmpty(organizationHierarchies)) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ServiceResponse(true, "Fetched organization Hierarchies Successfully.", organizationHierarchies));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(
					new ServiceResponse(false, "Organization hierarchy is not set up. Please configure it to proceed.", null));
		}
	}

	@PostMapping({"", "/{parentid}"})
	public ServiceResponse addHierarchyOption(@PathVariable(required = false) String parentid,
			@RequestBody @Valid CreateHierarchyRequest createHierarchyRequest) {
		return hierarchyOptionService.addHierarchyOption(createHierarchyRequest, parentid);
	}

	@PutMapping("/{id}")
	public ServiceResponse updateHierarchy(@PathVariable String id, @Valid @RequestBody UpdateHierarchyRequest request) {
		return organizationHierarchyService.updateName(request.getDisplayName(), id);
	}
}
