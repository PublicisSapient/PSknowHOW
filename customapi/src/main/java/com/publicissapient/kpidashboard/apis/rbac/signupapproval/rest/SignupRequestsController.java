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

package com.publicissapient.kpidashboard.apis.rbac.signupapproval.rest;

import java.util.List;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.rbac.signupapproval.policy.GrantApprovalListener;
import com.publicissapient.kpidashboard.apis.rbac.signupapproval.policy.RejectApprovalListener;
import com.publicissapient.kpidashboard.apis.rbac.signupapproval.service.SignupManager;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequestDecision;
import com.publicissapient.kpidashboard.common.model.rbac.AuthenticationDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Grant Requests Controller.
 *
 * @author shi6
 */
@RestController
@RequestMapping("/userapprovals")
@Slf4j
public class SignupRequestsController {

	private final ModelMapper mapper = new ModelMapper();
	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	private SignupManager signupManager;

	/**
	 * Gets all unapproved requests data.
	 *
	 * @return responseEntity with data,message and information
	 */
	@GetMapping
	@PreAuthorize("hasPermission(null , 'APPROVE_USER')")
	public ResponseEntity<ServiceResponse> getAllUnapprovedRequests() {
		log.info("Getting all unapproved requests");
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponse(true, "Unapproved User details",
						mapper.map(authenticationService.getAuthenticationByApproved(false),
								new TypeToken<List<AuthenticationDTO>>() {
								}.getType())));
	}

	@GetMapping("/all")
	@PreAuthorize("hasPermission(null , 'APPROVE_USER')")
	public ResponseEntity<ServiceResponse> getAllRequests() {
		log.info("Getting all requests");
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(true, "User details",
				mapper.map(authenticationService.all(), new TypeToken<List<AuthenticationDTO>>() {
				}.getType())));
	}

	/**
	 * Modify an access request data by username
	 *
	 * @param username
	 *            access request id
	 * @param accessRequestDecision
	 *            decision data
	 * @return updated access request
	 */
	@PutMapping("/{username}")
	@PreAuthorize("hasPermission(null , 'APPROVE_USER')")
	public ResponseEntity<ServiceResponse> modifyAccessRequestById(@PathVariable("username") String username,
			@Valid @RequestBody AccessRequestDecision accessRequestDecision) {
		ServiceResponse[] serviceResponse = new ServiceResponse[1];

		if (Constant.ACCESS_REQUEST_STATUS_APPROVED.equalsIgnoreCase(accessRequestDecision.getStatus())) {
			log.info("Approve access {}", username);

			signupManager.grantAccess(username, new GrantApprovalListener() {
				@Override
				public void onSuccess(Authentication authentication) {
					serviceResponse[0] = new ServiceResponse(true, "Granted", null);
				}

				@Override
				public void onFailure(Authentication authentication, String message) {
					serviceResponse[0] = new ServiceResponse(false, message, null);
				}
			});
		} else if (Constant.ACCESS_REQUEST_STATUS_REJECTED.equalsIgnoreCase(accessRequestDecision.getStatus())) {
			log.info("Reject access {}", username);
			signupManager.rejectAccessRequest(username, new RejectApprovalListener() {
				@Override
				public void onSuccess(Authentication authentication) {
					serviceResponse[0] = new ServiceResponse(true, "Rejected Successfully", null);
				}

				@Override
				public void onFailure(Authentication authentication, String message) {
					serviceResponse[0] = new ServiceResponse(false, message, null);

				}
			});

		}
		return ResponseEntity.status(HttpStatus.OK).body(serviceResponse[0]);
	}
}