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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.publicissapient.kpidashboard.apis.entity.Role;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

	@Query(value = "select * from auth.role r where r.resource_id = (select rs.id from auth.resource rs where rs.name = :name )",
		   nativeQuery = true)
	List<Role> findByResourceId(@Param("name") String name);

	@Query(value = "SELECT * FROM auth.role r WHERE r.resource_id = (SELECT id FROM auth.resource WHERE name = :resourceName) AND r.default_role = true ",
		   nativeQuery = true)
	List<Role> getDefaultRoleForResource(@Param("resourceName") String resourceName);

	Role findByNameAndResourceId(String roleName, Long id);

	@Query(value = "SELECT * FROM auth.role r WHERE r.resource_id = (SELECT id FROM auth.resource WHERE name = :resource) AND r.root_user = true ",
		   nativeQuery = true)
	List<Role> getRootRoleforResource(String resource);

	@Query(value = "SELECT * FROM auth.role r WHERE r.default_role = true ", nativeQuery = true)
	List<Role> getAllDefaultRole();

	@Query(value = "SELECT name FROM auth.role r WHERE r.id IN (:roleIDs)", nativeQuery = true)
	List<String> findByIdIn(@Param("roleIDs") List<Long> roleIDs);
}
