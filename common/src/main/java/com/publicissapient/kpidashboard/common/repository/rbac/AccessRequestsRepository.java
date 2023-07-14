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
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.publicissapient.kpidashboard.common.model.rbac.AccessRequest;

/**
 * Interface for access_requests collection
 */
public interface AccessRequestsRepository extends MongoRepository<AccessRequest, ObjectId> {

	/**
	 * Fetch all access requests by username.
	 *
	 * @param username
	 *            the username
	 * @return Access Request data
	 */
	List<AccessRequest> findByUsername(String username);

	/**
	 * Fetch all access requests by status.
	 *
	 * @param status
	 *            the status
	 * @return List of {@link AccessRequest}
	 */
	List<AccessRequest> findByStatus(String status);

	/**
	 * Fetch all access requests by username and status.
	 *
	 * @param username
	 *            the username
	 * @param status
	 *            the status
	 * @return list of access requests
	 */
	List<AccessRequest> findByUsernameAndStatus(String username, String status);

	/**
	 * Find pending requests which have provided item(project)
	 * 
	 * @param itemId
	 *            item id
	 * @return pending requests which have provided item
	 */
	@Query("{'status':'Pending', 'accessNode.accessItems.itemId': ?0}")
	List<AccessRequest> findPendingAccessRequestsByAccessItemId(String itemId);

	/**
	 * Find pending requests
	 * 
	 * @param status
	 *            status
	 * @param accessLevel
	 *            accessLevel
	 * @return pending requests which have provided item
	 */
	@Query("{'status': ?0, 'accessNode.accessLevel': ?1}")
	List<AccessRequest> findByStatusAndAccessLevel(String status, String accessLevel);

	/**
	 * Fetch access request
	 * 
	 * @param id
	 *            access request id
	 * @return the access request
	 */
	AccessRequest findById(String id);
}