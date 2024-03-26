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
package com.publicissapient.kpidashboard.apis.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.entity.Role;
import com.publicissapient.kpidashboard.apis.errors.GenericException;
import com.publicissapient.kpidashboard.apis.repository.RoleRepository;
import com.publicissapient.kpidashboard.apis.repository.UserRepository;
import com.publicissapient.kpidashboard.apis.repository.UserRoleRepository;
import com.publicissapient.kpidashboard.apis.service.MessageService;
import com.publicissapient.kpidashboard.apis.service.ResourceService;
import com.publicissapient.kpidashboard.apis.service.RoleService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author hargupta15
 */
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;
	@Autowired
	private ResourceService resourceService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	public RoleServiceImpl(@Lazy MessageService messageService) {
	}

	/**
	 * Validate Role details
	 *
	 * @param roleName
	 * @param resourceId
	 */
	public Role validateRole(String roleName, Long resourceId) {
		if (roleName == null || roleName.isEmpty()) {
			log.error("Invalid Role Name : " + roleName);
			throw new GenericException("Please provide a valid Role");
		}
		Role role = roleRepository.findByNameAndResourceId(roleName, resourceId);
		if (role == null) {
			log.error("Role doesn't exist for given resource");
			throw new GenericException("Role doesn't exist for given resource");
		}
		return role;
	}

	/**
	 * fetch resource wise roles name
	 * 
	 * @param resourceName
	 * @return
	 */

	@Override
	public List<String> getResourceAllRoles(String resourceName) {

		return roleRepository.findByResourceId(resourceName).stream().map(Role::getName).collect(Collectors.toList());
	}

	/**
	 * @param resource
	 * @return
	 */
	@Override
	public List<Role> getRootUserForResource(String resource) {
		return roleRepository.getRootRoleforResource(resource);
	}
}
