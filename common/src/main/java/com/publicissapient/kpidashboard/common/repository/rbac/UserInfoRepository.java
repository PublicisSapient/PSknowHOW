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

package com.publicissapient.kpidashboard.common.repository.rbac;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsAccess;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;

/**
 * Repository for {@link UserInfo}.
 */
public interface UserInfoRepository extends CrudRepository<UserInfo, ObjectId> {

	/**
	 * Find by username UserInfo.
	 *
	 * @param username
	 *            the username
	 * @return the UserInfo
	 */

	UserInfo findByUsername(String username);

	/**
	 * Finds by username and auth type.
	 *
	 * @param username
	 *            the username
	 * @param authType
	 *            the auth type
	 * @return the user info
	 */
	UserInfo findByUsernameAndAuthType(String username, AuthType authType);

	/**
	 * Finds by user role.
	 *
	 * @param roleAdmin
	 *            the role admin
	 * @return the collection of UserInfo
	 */
	List<UserInfo> findByAuthoritiesIn(List<String> roleAdmin);

	/**
	 * Finds by order by user name asc.
	 *
	 * @return the iterable of UserInfo
	 */
	Iterable<UserInfo> findByOrderByUsernameAsc();

	/**
	 * delete projectAccess by username
	 * 
	 * @param username
	 */
	void deleteByUsernameAndProjectsAccessIn(String username, List<ProjectsAccess> projectsAccesses);

	/**
	 * delete User by userName
	 *
	 * @param userName
	 */
	void deleteByUsername(String userName);

	/**
	 * Find by authType and authorities in
	 * 
	 * @param authType
	 *            authType
	 * @param roles
	 *            roles
	 * @return list of users
	 */
	List<UserInfo> findByAuthTypeAndAuthoritiesIn(String authType, List<String> roles);

	/**
	 * find all the users by auth type
	 * 
	 * @param authType
	 *            auth types
	 * @return list of users
	 */
	List<UserInfo> findByAuthType(String authType);

	/**
	 * find all the users
	 */
	List<UserInfo> findAll();
}
