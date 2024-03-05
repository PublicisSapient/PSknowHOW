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
package com.publicissapient.kpidashboard.apis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.publicissapient.kpidashboard.apis.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
	List<UserRole> findByUsername(String name);

	@Query(value = "select * from auth.user_role where username = :username and role_id in (select id from auth.role where name = :role and resource_id = (select id from auth.resource where name = :resource ))", nativeQuery = true)
	Optional<UserRole> findByUsernameAndRole(@Param("username") String username, @Param("role") String role,
			@Param("resource") String resource);

	List<UserRole> findByRoleId(Long id);

	@Query(value = "select * from auth.user_role where username = :username and role_id in (select id from auth.role where resource_id = (select id from auth.resource where name = :resource ))", nativeQuery = true)
	List<UserRole> findByUsernameAndResource(@Param("username") String username, @Param("resource") String resource);

	Optional<UserRole> findByUsernameAndRoleId(String username, Long id);

	@Query(value = "select * from auth.user_role where role_id in (select id from auth.role where name in (:roles))", nativeQuery = true)
	List<UserRole> findByRoles(@Param("roles") List<String> roles);
}
