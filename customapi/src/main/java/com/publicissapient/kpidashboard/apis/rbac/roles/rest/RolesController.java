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

package com.publicissapient.kpidashboard.apis.rbac.roles.rest;

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

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.rbac.roles.service.RolesHelperService;
import com.publicissapient.kpidashboard.common.model.rbac.RoleData;
import com.publicissapient.kpidashboard.common.model.rbac.RoleDataDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Rest Controller for all roles requests.
 *
 * @author anamital
 */
@RestController
@RequestMapping("/roles")
@Slf4j
public class RolesController {

	@Autowired
	private RolesHelperService rolesHelperService;

	/**
	 * Fetch all roles data.
	 *
	 * @return the roles
	 */
	@RequestMapping(method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> getAllRoles() {
		log.info("Fetching all roles");
		return ResponseEntity.status(HttpStatus.OK).body(rolesHelperService.getAllRoles());
	}

	/**
	 * Fetch a role by id.
	 * 
	 * @param id
	 *            unique object id already present in the database
	 * @return responseEntity with data,message and status
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> getRoleById(@PathVariable("id") String id) {
		log.info("Fetching role@{}", id);
		return ResponseEntity.status(HttpStatus.OK).body(rolesHelperService.getRoleById(id));
	}

	/**
	 * Modify/Update a role by id.
	 * 
	 * @param id
	 *            unique object_id present in the database
	 * @param roleDTO
	 *            request object that replaces the role data present at object_id
	 *            id.
	 *
	 * @return responseEntity with data,message and status
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> modifyRoleById(@PathVariable("id") String id,
			@Valid @RequestBody RoleDataDTO roleDTO) {
		final ModelMapper modelMapper = new ModelMapper();
		RoleData role = modelMapper.map(roleDTO, RoleData.class);

		log.info("role@{} updated", id);
		return ResponseEntity.status(HttpStatus.OK).body(rolesHelperService.modifyRoleById(id, role));
	}

	/**
	 * Create a role in the database.
	 *
	 * @param roleDTO
	 *            request object that is created in the database.
	 *
	 * @return responseEntity with data,message and status
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> createRole(@Valid @RequestBody RoleDataDTO roleDTO) {
		final ModelMapper modelMapper = new ModelMapper();
		final RoleData role = modelMapper.map(roleDTO, RoleData.class);

		log.info("created new role");
		return ResponseEntity.status(HttpStatus.OK).body(rolesHelperService.createRole(role));
	}

}