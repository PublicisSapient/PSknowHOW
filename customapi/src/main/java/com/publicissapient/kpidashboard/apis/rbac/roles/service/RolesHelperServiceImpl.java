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

package com.publicissapient.kpidashboard.apis.rbac.roles.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.rbac.RoleData;
import com.publicissapient.kpidashboard.common.repository.rbac.RolesRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * This class provides various methods related to operations on RoleData
 *
 * @author anamital
 */
@Service
@Slf4j
public class RolesHelperServiceImpl implements RolesHelperService {

	@Autowired
	private RolesRepository repository;

	/**
	 * Fetch all roles data.
	 *
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	@Override
	public ServiceResponse getAllRoles() {
		final List<RoleData> roles = repository.findAll();

		if (CollectionUtils.isEmpty(roles)) {
			log.info("Db has no roles");
			return new ServiceResponse(true, "No roles in roles db", roles);
		}
		log.info("Successfully fetched all roles");
		return new ServiceResponse(true, "Found all roles", roles);
	}

	/**
	 * Fetch a role by id.
	 * 
	 * @param id
	 *
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	@Override
	public ServiceResponse getRoleById(String id) {
		try {
			if (!ObjectId.isValid(id)) {
				log.info("Id not valid");
				return new ServiceResponse(false, "Invalid access_request@" + id, null);
			}
		} catch (IllegalArgumentException e) {
			log.info("Id cannot be empty");
			return new ServiceResponse(false, "invalid Id", null);
		}
		Optional<RoleData> role = repository.findById(new ObjectId(id));
		if (role.isPresent()) {
			log.info("Successfully found role@{}", id);
			return new ServiceResponse(true, "Found role@" + id, Arrays.asList(role));
		} else {
			log.info("Roles Db returned null");
			return new ServiceResponse(false, "role@" + id + " does not exist", null);
		}

	}

	/**
	 * Modify/Update a role by id.
	 * 
	 * @param id,
	 *            RoleData
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	@Override
	public ServiceResponse modifyRoleById(String id, RoleData role) {
		try {
			if (!ObjectId.isValid(id)) {
				log.info("Id not valid");
				return new ServiceResponse(false, "Invalid role@" + id, null);
			}
		} catch (IllegalArgumentException e) {
			log.info("Id cannot be empty");
			return new ServiceResponse(false, "invalid Id", null);
		}
		if (!isRoleDataValid(role)) {

			log.info("rolename, role description or Permissions are empty");
			return new ServiceResponse(false, "Mandatory fields cannot be empty", null);
		}

		role.setLastModifiedDate(new Date());
		repository.save(role);
		log.info("Successfully modified role@{}", id);
		return new ServiceResponse(true, "modified role@" + id, Arrays.asList(role));
	}

	/**
	 * Create a role in the database.
	 *
	 * @param role
	 *
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	@Override
	public ServiceResponse createRole(RoleData role) {
		role.setId(new ObjectId());

		if (!isRoleDataValid(role)) {

			log.info("rolename, role description or Permissions are empty");
			return new ServiceResponse(false, "Mandatory fields cannot be empty", null);
		}

		log.info("Successfully pushed role into roles db");
		repository.save(role);
		return new ServiceResponse(true, "created new role", Arrays.asList(role));
	}

	/**
	 * Checks if @param RoleData has non empty roleName, RoleDescription and
	 * Permissions
	 *
	 * @param role
	 *
	 * @return Boolean
	 */
	private Boolean isRoleDataValid(RoleData role) {

		if (StringUtils.isEmpty(role.getRoleName()) || StringUtils.isEmpty(role.getRoleDescription())
				|| CollectionUtils.isEmpty(role.getPermissions())) {
			log.info("Mandatory fields need to filled");
			return false;
		}
		log.info("Valid role object");
		return true;
	}

}