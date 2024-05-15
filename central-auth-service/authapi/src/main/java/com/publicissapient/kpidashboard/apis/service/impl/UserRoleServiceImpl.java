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

import com.publicissapient.kpidashboard.apis.entity.Role;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.entity.UserRole;
import com.publicissapient.kpidashboard.apis.repository.RoleRepository;
import com.publicissapient.kpidashboard.apis.repository.UserRoleRepository;
import com.publicissapient.kpidashboard.apis.service.UserRoleService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {
	private final UserRoleRepository userRoleRepository;

	private final RoleRepository roleRepository;
	private final UserService userService;

	public Collection<GrantedAuthority> getAuthorities(String username) {
		List<UserRole> userPermissionList = userRoleRepository.findByUsername(username);

		if(CollectionUtils.isNotEmpty(userPermissionList)) {
			userPermissionList = userPermissionList.stream().filter(userRole -> userRole.getRole() != null).collect(Collectors.toList());
			return createAuthorities(CollectionUtils.emptyIfNull(userPermissionList).stream()
					.map(userPermission -> userPermission.getRole().getName()).collect(Collectors.toList()));
		} else {
			List<UserRole> userPermissionDummy = new ArrayList<>();
			UserRole dummyRole = new UserRole();
			dummyRole.setUsername(username);
			Role naRole = roleRepository.findByNameAndResourceId("ROLE_NA" , 1L);
			dummyRole.setRole(naRole);
			userPermissionDummy = userPermissionDummy.stream().filter(userRole -> userRole.getRole() != null).collect(Collectors.toList());
			return createAuthorities(CollectionUtils.emptyIfNull(userPermissionDummy).stream()
					.map(userPermission -> userPermission.getRole().getName()).collect(Collectors.toList()));
		}
	}

	@Override
	public boolean isRootUser(String username, String resource) {
		Optional<UserRole> rolesOptional = CollectionUtils
				.emptyIfNull(userRoleRepository.findByUsernameAndResource(username, resource)).stream()
				.filter(userPermission -> userPermission.getRole() != null && userPermission.getRole().isRootUser())
				.findFirst();
		return rolesOptional.isPresent();
	}

	/**
	 *
	 * @param username
	 * @return
	 */
	@Override
	public List<UserRole> createAndGetDefaultUserRole(String username) {
		List<UserRole> userRoles = new ArrayList<>();
		Optional<User> user = userService.findByUserName(username);

		if (user.isPresent()) {
			List<Role> roles = roleRepository.getAllDefaultRole();
			roles.forEach(role -> {
				UserRole usrRole = createUserRole(user.get().getUsername(), role, user.get());
				userRoles.add(usrRole);
			});
		}

		return userRoles;
	}

	/**
	 * method to create new user role
	 *
	 * @param username
	 *            username
	 * @param role
	 *            role
	 * @param user
	 *            user
	 * @return UserRole
	 */

	@Override
	public UserRole createUserRole(String username, Role role, User user) {
		UserRole userRole = new UserRole();
		userRole.setRole(role);
		userRole.setUsername(username);
		userRole.setCreatedBy(user);
		userRole.setModifiedBy(user);
		userRole.setCreatedDate(LocalDate.now());
		userRole.setModifiedDate(LocalDate.now());
		return userRoleRepository.save(userRole);
	}

	/**
	 *
	 * @param loggedUser
	 *            loggedUser
	 * @param resource
	 *            resource
	 * @return
	 */
	@Override
	public List<UserRole> findUserRoleByUsernameAndResource(String loggedUser, String resource) {
		return userRoleRepository.findByUsernameAndResource(loggedUser, resource);
	}

	private Collection<GrantedAuthority> createAuthorities(List<String> roles) {
		Collection<GrantedAuthority> grantedAuthorities = new HashSet<>();
		if (CollectionUtils.isNotEmpty(roles)) {
			roles.forEach(authority -> grantedAuthorities.add(new SimpleGrantedAuthority(authority)));
		}
		return grantedAuthorities;
	}

}
