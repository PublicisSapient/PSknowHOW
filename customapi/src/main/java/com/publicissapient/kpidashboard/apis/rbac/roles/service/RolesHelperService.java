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

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.rbac.RoleData;

/**
 *
 * @author anamital
 */
public interface RolesHelperService {

	/**
	 * Gets all roles.
	 * 
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse getAllRoles();

	/**
	 * Gets all roles for this Id.
	 * 
	 * @param id
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse getRoleById(String id);

	/**
	 * Modifies a role. Finds role by @param id and replaces it with @param role
	 * 
	 * @param role,
	 *            id
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse modifyRoleById(String id, RoleData role);

	/**
	 * Creates a role.
	 * 
	 * @param role
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse createRole(RoleData role);

}