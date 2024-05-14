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

import com.publicissapient.kpidashboard.apis.constant.CommonConstant;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.enums.NotificationCustomDataEnum;
import com.publicissapient.kpidashboard.apis.errors.GenericException;
import com.publicissapient.kpidashboard.apis.repository.UserRepository;
import com.publicissapient.kpidashboard.apis.service.*;
import com.publicissapient.kpidashboard.common.model.UserAccessRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author hargupta15
 */
@Slf4j
@AllArgsConstructor
@Service
public class UserApprovalServiceImpl implements UserApprovalService {
	private final UserService userService;

	private final UserRepository userRepository;

	private final TokenAuthenticationService tokenAuthenticationService;

	private final CommonService commonService;

	private final MessageService messageService;

	/**
	 * Method To fetch Details of all unapproved users
	 * 
	 * @return
	 */
	@Override
	public List<UserAccessRequest> findAllUnapprovedUsers() {
		List<User> users = userService.findAllUnapprovedUsers();
		List<UserAccessRequest> pendingApprovals = new ArrayList<>();
		users.forEach(user -> {
			UserAccessRequest userAccessRequest = new UserAccessRequest();
			userAccessRequest.setUsername(user.getUsername());
			userAccessRequest.setEmail(user.getEmail().toLowerCase());
			pendingApprovals.add(userAccessRequest);
		});
		return pendingApprovals;
	}

	/**
	 * Method To update details of approved User
	 *
	 * @param username
	 * @return
	 */
	@Override
	public boolean approveUser(String username) {
		Optional<User> user = userService.findByUserName(username);
		try {
			if (user.isPresent() && user.get().isUserVerified() && !user.get().isApproved()) {
				User userData = user.get();
				userData.setApproved(true);
				userData.setModifiedDate(LocalDateTime.now());
				userRepository.save(userData);
//				tokenAuthenticationService.updateExpiryDate(username, LocalDateTime.now().toString());
				List<String> emailAddresses = new ArrayList<>();
				emailAddresses.add(userData.getEmail());
				String serverPath = getServerPath();
				List<String> superAdminEmailList = commonService
						.getEmailAddressBasedOnRoles(Arrays.asList(CommonConstant.ROLE_SUPERADMIN));
				// logic needed to changes if that paticular superadmin send to mail who is
				// approved User.
				Map<String, String> customData = createCustomData(username, userData.getEmail(), serverPath,
						superAdminEmailList.get(0));
				commonService.sendEmailNotification(emailAddresses, customData,
						CommonConstant.APPROVAL_NOTIFICATION_KEY, CommonConstant.APPROVAL_SUCCESS_TEMPLATE_KEY);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new GenericException(messageService.getMessage("error_request_approve"));
		}

	}

	/**
	 * * create custom data for email
	 *
	 * @param username
	 * @param email
	 * @param serverPath
	 * @param adminEmail
	 * @return
	 */
	private Map<String, String> createCustomData(String username, String email, String serverPath, String adminEmail) {
		Map<String, String> customData = new HashMap<>();
		customData.put(NotificationCustomDataEnum.USER_NAME.getValue(), username);
		customData.put(NotificationCustomDataEnum.USER_EMAIL.getValue(), email);
		customData.put(NotificationCustomDataEnum.SERVER_HOST.getValue(), serverPath);
		customData.put(NotificationCustomDataEnum.ADMIN_EMAIL.getValue(), adminEmail);
		return customData;
	}

	private String getServerPath() {
		String serverPath = "";
		try {
			serverPath = commonService.getApiHost();
		} catch (UnknownHostException e) {
			log.error("ApproveRequestController: Server Host name is not bind with Approval Request mail ");
		}
		return serverPath;
	}

	/**
	 * Method to delete rejected user
	 * 
	 * @param username
	 * @return
	 */
	@Override
	public boolean rejectUser(String username) {
		return userService.deleteByUserName(username);
	}

}
