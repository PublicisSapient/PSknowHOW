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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.autoapprove.service.AutoApproveAccessService;
import com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NotificationEnum;
import com.publicissapient.kpidashboard.common.model.rbac.AccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.NotificationDataDTO;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.rbac.AccessRequestsRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * This class provides various methods related to operations on
 * AccessRequestsData
 *
 * @author anamital
 */
@Service
@Slf4j
public class AccessRequestsHelperServiceImpl implements AccessRequestsHelperService {

	private static final String SUPERADMINROLENAME = "ROLE_SUPERADMIN";
	/**
	 * Repeated String used in logging info
	 */
	private static String infoMandatoryFieldsNotEmpty = "Mandatory fields cannot be empty";
	@Autowired
	AutoApproveAccessService autoApproveService;
	@Autowired
	private AccessRequestsRepository repository;
	@Autowired
	private UserInfoServiceImpl userInfoServiceImpl;
	@Autowired
	private AuthenticationRepository authenticationRepository;
	@Autowired
	private ProjectAccessManager accessManager;

	/**
	 * Fetch all access requests data.
	 *
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	@Override
	@RequestMapping(method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ServiceResponse getAllAccessRequests() {
		List<AccessRequest> accessRequest = repository.findAll();

		if (CollectionUtils.isEmpty(accessRequest)) {
			log.info("No requests in access request db");
			return new ServiceResponse(true, "No access requests in db", accessRequest);
		}
		log.info("Fetched access requests successfully");
		return new ServiceResponse(true, "Found all access requests", accessRequest);
	}

	/**
	 * Fetch a access request data by @param id.
	 *
	 * @param id
	 *
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	@Override
	public ServiceResponse getAccessRequestById(String id) {
		try {
			if (!ObjectId.isValid(id)) {
				log.info("Id not valid");
				return new ServiceResponse(false, "Invalid access_request@" + id, null);
			}
		} catch (IllegalArgumentException e) {
			log.info("Id cannot be empty");
			return new ServiceResponse(false, "invalid Id", null);
		}

		Optional<AccessRequest> accessRequest = repository.findById(new ObjectId(id));
		if (accessRequest.isPresent()) {
			log.info("Successfully Found access request@{}", id);
			return new ServiceResponse(true, "Found access_request@" + id, Arrays.asList(accessRequest));
		} else {
			log.info("Db returned null");
			return new ServiceResponse(false, "Not exist access_request@" + id, null);
		}

	}

	/**
	 * Fetch all access requests data under the user @param username.
	 *
	 * @param username
	 *
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	@Override
	public ServiceResponse getAccessRequestByUsername(String username) {

		if (StringUtils.isEmpty(username)) {
			log.info("username cannot be empty");
			return new ServiceResponse(false, infoMandatoryFieldsNotEmpty, null);
		}

		List<AccessRequest> accessRequest = repository.findByUsername(username);

		if (CollectionUtils.isEmpty(accessRequest)) {
			log.info("No requests under user {}", username);
			return new ServiceResponse(true, "access_requests do not exist for username " + username, accessRequest);
		}
		log.info("Successfully found requests under user {}", username);
		return new ServiceResponse(true, "Found access_requests under username " + username, accessRequest);
	}

	/**
	 * Fetch all access requests data with current status @param status.
	 *
	 * @param status
	 *            status
	 *
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	@Override
	public ServiceResponse getAccessRequestByStatus(String status) {
		if (StringUtils.isEmpty(status)) {
			log.info("status is empty");
			return new ServiceResponse(false, infoMandatoryFieldsNotEmpty, null);
		}

		List<AccessRequest> accessRequest = getAccessRequestBasedonStatusAndRole(status);
		if (CollectionUtils.isEmpty(accessRequest)) {
			log.info("No requests with current status {}", status);
			return new ServiceResponse(true, "access requests do not exist for status " + status, accessRequest);
		}
		log.info("Successfully found requests with current status {}", status);
		return new ServiceResponse(true, "Found access_requests for status " + status, accessRequest);
	}

	/**
	 * 
	 * @param status
	 *            status
	 * @return list of access Request
	 */
	private List<AccessRequest> getAccessRequestBasedonStatusAndRole(String status) {
		List<AccessRequest> accessRequest = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserInfo userInfo = userInfoServiceImpl.getUserInfo((String) auth.getPrincipal());

		if (userInfo.getAuthorities().contains(SUPERADMINROLENAME)) {
			accessRequest = repository.findByStatus(status);
		} else {
			List<String> roleList = Arrays.asList(Constant.ROLE_PROJECT_ADMIN);
			accessRequest = fetchAccessRequestBasedOnUserInfoAndRole(userInfo, roleList, status);
		}
		return accessRequest;
	}

	private List<AccessRequest> fetchAccessRequestBasedOnUserInfoAndRole(UserInfo user, List<String> roleList,
			String status) {
		List<String> basicConfigList = accessManager.getProjectBasicOnRoleList(user, roleList);
		List<AccessRequest> pendingAccessRequest = repository.findByStatusAndAccessLevel(status,
				CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);
		return filterProjectLevelRequest(basicConfigList, pendingAccessRequest);
	}

	private List<AccessRequest> filterProjectLevelRequest(List<String> basicConfigList,
			List<AccessRequest> pendingAccessRequest) {
		List<AccessRequest> filteredRequest = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(basicConfigList) && CollectionUtils.isNotEmpty(pendingAccessRequest)) {
			pendingAccessRequest.forEach(request -> {
				if (basicConfigList.contains(request.getAccessNode().getAccessItems().get(0).getItemId())) {
					filteredRequest.add(request);
				}
			});
		}
		return filteredRequest;
	}

	/**
	 * Fetch all access requests data under the user @param username with current
	 * status @param status.
	 *
	 * @param username,
	 *            String
	 *
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	@Override
	public ServiceResponse getAccessRequestByUsernameAndStatus(final String username, final String status) {
		if (StringUtils.isEmpty(status) || StringUtils.isEmpty(username)) {
			log.info("status or username is empty");
			return new ServiceResponse(false, infoMandatoryFieldsNotEmpty, null);
		}

		final List<AccessRequest> accessRequest = repository.findByUsernameAndStatus(username, status);

		if (CollectionUtils.isEmpty(accessRequest)) {
			log.info("No requests under user {} with current status {}", username, status);
			return new ServiceResponse(true,
					"access_requests do not exist for username " + username + " and status " + status, accessRequest);
		}
		log.info("Successfully found requests under username {} and status{}", username, status);
		return new ServiceResponse(true, "Found access_requests under username " + username + " and status " + status,
				accessRequest);
	}

	/**
	 * Fetches access requests count with current status @param status.
	 * 
	 * @param status
	 *            status
	 * 
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	@Override
	public ServiceResponse getNotificationByStatus(String status) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		List<AccessRequest> accessRequest = null;
		String message = "Found Pending Approval Count";
		UserInfo user = userInfoServiceImpl.getUserInfo((String) auth.getPrincipal());
		List<NotificationDataDTO> notificationDataList = new ArrayList<>();
		if (user.getAuthorities().contains(SUPERADMINROLENAME)) {
			accessRequest = repository.findByStatus(status);
			NotificationDataDTO userApprovalNotification = newUserApprovalRequestNotification();
			notificationDataList.add(userApprovalNotification);
		} else if (user.getAuthorities().contains(Constant.ROLE_PROJECT_ADMIN)) {
			List<String> roleList = Arrays.asList(Constant.ROLE_PROJECT_ADMIN);
			accessRequest = fetchAccessRequestBasedOnUserInfoAndRole(user, roleList, status);
		} else {
			accessRequest = repository.findByUsernameAndStatus(user.getUsername(), status);
			if (CollectionUtils.isEmpty(accessRequest)) {
				log.info("No requests under user {} with current status {}", user.getUsername(), status);
				message = "No Pending Raise Request Found For " + user.getUsername();
			} else {
				message = "Found Pending Raise Request Count for " + user.getUsername();
			}
		}
		NotificationDataDTO projectAccessNotification = newProjectAccessRequestNotification(accessRequest);
		notificationDataList.add(projectAccessNotification);
		return new ServiceResponse(true, message, notificationDataList);

	}

	private NotificationDataDTO newProjectAccessRequestNotification(List<AccessRequest> accessRequest) {
		NotificationDataDTO notificationDataDTO = new NotificationDataDTO();
		notificationDataDTO.setType(NotificationEnum.PROJECT_ACCESS.getValue());
		if (CollectionUtils.isEmpty(accessRequest)) {
			notificationDataDTO.setCount(0);
		} else {
			notificationDataDTO.setCount(accessRequest.size());
		}
		return notificationDataDTO;
	}

	private NotificationDataDTO newUserApprovalRequestNotification() {
		List<com.publicissapient.kpidashboard.apis.auth.model.Authentication> nonApprovedUserList = authenticationRepository
				.findByApproved(false);
		NotificationDataDTO notificationDataDTO = new NotificationDataDTO();
		notificationDataDTO.setType(NotificationEnum.USER_APPROVAL.getValue());
		if (CollectionUtils.isEmpty(nonApprovedUserList)) {
			notificationDataDTO.setCount(0);
		} else {
			notificationDataDTO.setCount(nonApprovedUserList.size());
		}
		return notificationDataDTO;
	}

	@Override
	public List<AccessRequest> getAccessRequestsByProject(String basicProjectConfigId) {
		return repository.findPendingAccessRequestsByAccessItemId(basicProjectConfigId);
	}

	@Override
	public AccessRequest updateAccessRequest(AccessRequest accessRequestsData) {
		AccessRequest updatedAccessRequest = null;
		if (accessRequestsData != null) {
			AccessRequest existingAccessRequest = repository.findById(accessRequestsData.getId().toHexString());

			if (existingAccessRequest != null) {
				updatedAccessRequest = repository.save(accessRequestsData);
				repository.save(accessRequestsData);
			}

		}

		return updatedAccessRequest;
	}

}