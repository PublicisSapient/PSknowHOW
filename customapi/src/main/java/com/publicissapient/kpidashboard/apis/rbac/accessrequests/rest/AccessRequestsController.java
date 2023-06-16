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

package com.publicissapient.kpidashboard.apis.rbac.accessrequests.rest;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.abac.AccessRequestListener;
import com.publicissapient.kpidashboard.apis.abac.GrantAccessListener;
import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.abac.RejectAccessListener;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.rbac.accessrequests.service.AccessRequestsHelperService;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequestDTO;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequestDecision;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * Access Requests Controller for all roles requests.
 *
 * @author anamital
 */
@RestController
@RequestMapping("/accessrequests")
@Slf4j
public class AccessRequestsController {

	/**
	 * Instantiates the AccessRequestsHelperService
	 */
	@Autowired
	private AccessRequestsHelperService accessRequestsHelperService;

	@Autowired
	private ProjectAccessManager projectAccessManager;

	/**
	 * Gets all access requests data.
	 *
	 * @return responseEntity with data,message and status
	 */
	@GetMapping
	@PreAuthorize("hasPermission(null, 'GET_ACCESS_REQUESTS')")
	public ResponseEntity<ServiceResponse> getAllAccessRequests() {
		log.info("Getting all requests");
		return ResponseEntity.status(HttpStatus.OK).body(accessRequestsHelperService.getAllAccessRequests());
	}

	/**
	 * Gets access request data at id.
	 * 
	 * @param id
	 *
	 * @return responseEntity with data,message and status
	 */
	@GetMapping(value = "/{id}")
	@PreAuthorize("hasPermission(null,'GET_ACCESS_REQUEST')")
	public ResponseEntity<ServiceResponse> getAccessRequestById(@PathVariable("id") String id) {
		log.info("Getting request@{}", id);
		return ResponseEntity.status(HttpStatus.OK).body(accessRequestsHelperService.getAccessRequestById(id));
	}

	/**
	 * Gets all access requests data under a username.
	 * 
	 * @param username
	 *
	 * @return responseEntity with data,message and status
	 */
	@GetMapping(value = "/user/{username}")
	@PreAuthorize("hasPermission(#username,'GET_ACCESS_REQUESTS_OF_USER')")
	public ResponseEntity<ServiceResponse> getAccessRequestByUsername(@PathVariable("username") String username) {
		log.info("Getting all requests under user {}", username);
		return ResponseEntity.status(HttpStatus.OK)
				.body(accessRequestsHelperService.getAccessRequestByUsername(username));
	}

	/**
	 * Gets all access requests data with a current status.
	 * 
	 * @param status
	 *
	 * @return responseEntity with data,message and status
	 */
	@GetMapping(value = "/status/{status}")
	@PreAuthorize("hasPermission(#status,'ACCESS_REQUEST_STATUS')")
	public ResponseEntity<ServiceResponse> getAccessRequestByStatus(@PathVariable("status") String status) {
		log.info("Getting all requests with current status {}", status);
		return ResponseEntity.status(HttpStatus.OK).body(accessRequestsHelperService.getAccessRequestByStatus(status));
	}

	/**
	 * Modify an access request data by id
	 * 
	 * @param id
	 *            access request id
	 * @param accessRequestDecision
	 *            decision data
	 * @return updated access request
	 */
	@PutMapping(value = "/{id}")
	@PreAuthorize("hasPermission(null , 'GRANT_ACCESS')")
	public ResponseEntity<ServiceResponse> modifyAccessRequestById(@PathVariable("id") String id,
			@Valid @RequestBody AccessRequestDecision accessRequestDecision) {

		ServiceResponse[] serviceResponse = new ServiceResponse[1];

		if (Constant.ACCESS_REQUEST_STATUS_APPROVED.equalsIgnoreCase(accessRequestDecision.getStatus())) {
			log.info("Approve access {}", id);

			projectAccessManager.grantAccess(id, accessRequestDecision, new GrantAccessListener() {
				@Override
				public void onSuccess(UserInfo userInfo) {
					serviceResponse[0] = new ServiceResponse(true, "Granted", null);
				}

				@Override
				public void onFailure(AccessRequest accessRequest, String message) {
					serviceResponse[0] = new ServiceResponse(false, message, null);
				}
			});
		} else if (Constant.ACCESS_REQUEST_STATUS_REJECTED.equalsIgnoreCase(accessRequestDecision.getStatus())) {
			log.info("Reject access {}", id);
			projectAccessManager.rejectAccessRequest(id, accessRequestDecision.getMessage(),
					new RejectAccessListener() {
						@Override
						public void onSuccess(AccessRequest accessRequest) {
							serviceResponse[0] = new ServiceResponse(true, "Rejected Successfully", null);
						}

						@Override
						public void onFailure(AccessRequest accessRequest, String message) {
							serviceResponse[0] = new ServiceResponse(false, message, null);

						}
					});

		}

		return ResponseEntity.status(HttpStatus.OK).body(serviceResponse[0]);
	}

	/**
	 * Create an access requests data.
	 * 
	 * @param accessRequestDTO
	 *
	 * @return responseEntity with data,message and status
	 */
	@PostMapping()
	@PreAuthorize("hasPermission(#accessRequestsDataDTO,'RAISE_ACCESS_REQUEST')")
	public ResponseEntity<ServiceResponse> createAccessRequests(@Valid @RequestBody AccessRequestDTO accessRequestDTO) {
		ModelMapper modelMapper = new ModelMapper();
		AccessRequest accessRequestsData = modelMapper.map(accessRequestDTO, AccessRequest.class);
		log.info("creating new request");
		final ServiceResponse[] serviceResponse = { null };
		projectAccessManager.createAccessRequest(accessRequestsData, new AccessRequestListener() {

			@Override
			public void onSuccess(AccessRequest accessRequest) {
				String msg = accessRequest.getStatus().equals(Constant.ACCESS_REQUEST_STATUS_APPROVED)
						? "Request has been auto-approved. Please login again to start using KnowHOW."
						: "Request submitted.";
				serviceResponse[0] = new ServiceResponse(true, msg, accessRequest);
			}

			@Override
			public void onFailure(String message) {

				serviceResponse[0] = new ServiceResponse(false, message, null);
			}
		});

		return ResponseEntity.status(HttpStatus.OK).body(serviceResponse[0]);

	}

	/**
	 * Gets access request data at id.
	 *
	 * @param id
	 *            id
	 *
	 * @return responseEntity with data,message and status
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<ServiceResponse> deleteAccessRequestById(@PathVariable("id") String id) {
		log.info("request received for deleting access request with id @{}", id);

		id = CommonUtils.handleCrossScriptingTaintedValue(id);
		ServiceResponse response = null;

		if (projectAccessManager.deleteAccessRequestById(id)) {

			response = new ServiceResponse(true, "Sucessfully deleted.", id);

		} else {
			response = new ServiceResponse(false, "Either id is wrong or you are not authorized to delete.", null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * Gets access requests count data with a pending status.
	 *
	 * @param status
	 *            status
	 *
	 * @return responseEntity with data,message and status
	 */
	@RequestMapping(value = "/{status}/notification", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> getNotificationByStatus(@PathVariable("status") String status) {
		log.info("Getting requests count with current status {}", status);
		return ResponseEntity.status(HttpStatus.OK).body(accessRequestsHelperService.getNotificationByStatus(status));
	}

}