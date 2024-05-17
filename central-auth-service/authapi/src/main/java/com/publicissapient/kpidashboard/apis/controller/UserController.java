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

package com.publicissapient.kpidashboard.apis.controller;

import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.service.*;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;
import com.publicissapient.kpidashboard.common.model.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

import static com.publicissapient.kpidashboard.apis.constant.CommonConstant.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@AllArgsConstructor
@Slf4j
@SuppressWarnings("java:S3740")
public class UserController {

	private final UserService userService;

	private final TokenAuthenticationService tokenAuthenticationService;

	private final MessageService messageService;

	/**
	 * Retrieve user info of the current user from the cookie
	 *
	 * @param request request
	 * @return ResponseEntity
	 */
	@GetMapping(value = "/user-info", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> fetchUserInfoFromAuthCookie(HttpServletRequest request) {
		try {
			ServiceResponse response = new ServiceResponse(false, messageService.getMessage(ERROR_UNAUTHORIZED_USER),
														   null
			);

			Optional<String> authToken = CookieUtil.getCookieValue(request, CookieUtil.COOKIE_NAME);
			if (authToken.isPresent() && StringUtils.isNotEmpty(authToken.get())) {
				String userName = tokenAuthenticationService.getSubject(authToken.get());

				Optional<User> user = userService.findByUsername(userName);
				UserDTO userDTO = userService.getUserDTO(user.get());

				response = new ServiceResponse(true, messageService.getMessage(SUCCESS_VALID_TOKEN), userDTO);

				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
			}
		} catch (Exception e) {
			ServiceResponse serviceResponse = new ServiceResponse(false, messageService.getMessage(ERROR_INVALID_USER),
																  null
			);
			return ResponseEntity.ok(serviceResponse);
		}
	}

	/**
	 * Put method to update user profile
	 *
	 * @param username username whose profile will be updated
	 * @param request  updated data
	 * @return
	 */
	@PutMapping(value = "/users/{username}/updateProfile", consumes = APPLICATION_JSON_VALUE,
				produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> updateUserProfile(@PathVariable("username") String username,
															 @Valid @RequestBody UserDTO request) {
		boolean isSuccess = userService.updateUserProfile(username, request);

		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(isSuccess, isSuccess ?
				messageService.getMessage("success_profile_user") :
				messageService.getMessage("error_update_profile"), null));
	}
}
