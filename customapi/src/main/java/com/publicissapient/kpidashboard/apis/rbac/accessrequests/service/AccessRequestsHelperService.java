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

package com.publicissapient.kpidashboard.apis.rbac.accessrequests.service;

import java.util.List;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequest;

/**
 *
 * @author anamital
 */
public interface AccessRequestsHelperService {

	/**
	 * Gets all access requests.
	 * 
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse getAllAccessRequests();

	/**
	 * Gets all access requests for that Id.
	 * 
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse getAccessRequestById(String id);

	/**
	 * Gets all access requests created by @param username.
	 * 
	 * @param username
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse getAccessRequestByUsername(String username);

	/**
	 * Gets all access requests created by @param username with @param status.
	 * 
	 * @param username
	 * 
	 * @param status
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse getAccessRequestByUsernameAndStatus(String username, String status);

	/**
	 * Gets all access requests with current status @param status.
	 * 
	 * @param status
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse getAccessRequestByStatus(String status);

	/**
	 * Gets access requests count with current status @param status.
	 * 
	 * @param status
	 *            status
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	ServiceResponse getNotificationByStatus(String status);

	/**
	 * Gets access request which has the project id
	 * 
	 * @param basicProjectConfigId
	 * @return access requests
	 */
	List<AccessRequest> getAccessRequestsByProject(String basicProjectConfigId);

	/**
	 * Update access request
	 * 
	 * @param accessRequestsData
	 *            access request
	 * @return updated access request
	 */
	AccessRequest updateAccessRequest(AccessRequest accessRequestsData);

}