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

package com.publicissapient.kpidashboard.apis.rbac.userinfo.rest;

import java.util.Objects;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.service.UserTokenDeletionService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.rbac.UserDetailsResponseDTO;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfoDTO;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author narsingh9
 *
 */

@RestController
@RequestMapping("/userinfo")
@Slf4j
public class UserInfoController {

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private UserTokenDeletionService userTokenDeletionService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private UserInfoRepository userInfoRepository;

	/**
	 * Fetch only approved user info data.
	 *
	 * @return the user info
	 */
	@GetMapping
	@PreAuthorize("hasPermission(null, 'GET_USER_INFO')")
	public ResponseEntity<ServiceResponse> getAll() {
		log.info("Fetching all user info data");
		return ResponseEntity.status(HttpStatus.OK).body(this.userInfoService.getAllUserInfo());
	}

	/***
	 * update the role updateAccessOfUserInfo()
	 * 
	 * @param username
	 *            unique object username present in the database
	 * @return responseEntity of userInfo with data,message and status
	 */
	@PreAuthorize("hasPermission(null, 'UPDATE_USER_INFO')")
	@PostMapping("/{username}")
	public ResponseEntity<ServiceResponse> updateUserRole(@PathVariable("username") String username,
			@Valid @RequestBody UserInfoDTO userInfoDto) {
		ModelMapper modelMapper = new ModelMapper();
		UserInfo userInfo = modelMapper.map(userInfoDto, UserInfo.class);

		log.info("user info ");
		ServiceResponse response = userInfoService.updateUserRole(username, userInfo);

		return ResponseEntity.status(HttpStatus.OK).body(response);

	}

	/**
	 * Delete Users based on the userName.
	 *
	 * @return the Service Response
	 */
	/*
	 * @PreAuthorize("hasPermission(null, 'DELETE_USER')")
	 * 
	 * @DeleteMapping(value = "/{userName}") public ServiceResponse
	 * deleteUser(@PathVariable String userName) {
	 * log.info("Inside deleteUser() method of UserInfoController "); String
	 * loggedUserName = authenticationService.getLoggedInUser(); UserInfo userInfo =
	 * userInfoRepository.findByUsername(userName); if
	 * ((!loggedUserName.equals(userName) &&
	 * !userInfo.getAuthorities().contains(Constant.ROLE_SUPERADMIN)) ) {
	 * ServiceResponse response = userInfoService.deleteUser(userName); return new
	 * ServiceResponse(true, userName + " deleted Successfully", response); } else {
	 * log.info("Unauthorized to perform deletion of user " + userName); return new
	 * ServiceResponse(false, "Unauthorized to perform deletion of user",
	 * "Unauthorized"); }
	 * 
	 * }
	 */

	@PreAuthorize("hasPermission(null, 'DELETE_USER')")
	@DeleteMapping(value = "/{userName}")
	public ResponseEntity<ServiceResponse> deleteUser(@PathVariable String userName) {
		log.info("Inside deleteUser() method of UserInfoController ");
		String loggedUserName = authenticationService.getLoggedInUser();
		UserInfo userInfo = userInfoRepository.findByUsername(userName);
		if ((!loggedUserName.equals(userName) && !userInfo.getAuthorities().contains(Constant.ROLE_SUPERADMIN))) {
			ServiceResponse response = userInfoService.deleteUser(userName);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			log.info("Unauthorized to perform deletion of user " + userName);
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ServiceResponse(false, "Unauthorized to perform deletion of user", "Unauthorized"));
		}

	}

	/**
	 * get user details via token
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/userData")
	public ResponseEntity<ServiceResponse> getUserDetails(HttpServletRequest request) {
		UserDetailsResponseDTO userInfo = userInfoService.getUserInfoByToken(request);
		if (Objects.nonNull(userInfo)) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ServiceResponse(true, "get successfully user info details ", userInfo));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(false, "invalid Token or user", null));

		}
	}
}
