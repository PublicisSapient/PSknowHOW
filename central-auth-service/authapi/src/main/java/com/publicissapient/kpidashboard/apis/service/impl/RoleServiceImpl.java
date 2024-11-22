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

import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.entity.Role;
import com.publicissapient.kpidashboard.apis.repository.RoleRepository;
import com.publicissapient.kpidashboard.apis.service.RoleService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

	private final RoleRepository roleRepository;

	@Override
	public List<String> getAllRolesByResourceName(String resourceName) {

		return roleRepository.findByResource_Name(resourceName).stream().map(Role::getName).collect(Collectors.toList());
	}
}
