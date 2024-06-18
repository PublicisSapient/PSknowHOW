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

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.publicissapient.kpidashboard.apis.service.MessageService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import com.publicissapient.kpidashboard.apis.service.dto.ServiceResponseDTO;
import com.publicissapient.kpidashboard.apis.service.dto.UserDTO;

import static com.publicissapient.kpidashboard.apis.constant.CommonConstant.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@AllArgsConstructor
@Slf4j
@SuppressWarnings("java:S3740")
public class UserController {

	private final UserService userService;

	private final MessageService messageService;

	@GetMapping(value = "/user-info", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponseDTO> fetchUserInfoFromAuthCookie(HttpServletRequest request) {
		UserDTO userDTO = userService.getCurrentUser(request);

		if (Objects.nonNull(userDTO)) {
			return ResponseEntity.ok(
					new ServiceResponseDTO(true, messageService.getMessage(SUCCESS_VALID_TOKEN), userDTO));
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
								 .body(new ServiceResponseDTO(false, messageService.getMessage(ERROR_INVALID_USER), null));
		}
	}

	@PutMapping(value = "/users/{username}/updateProfile", consumes = APPLICATION_JSON_VALUE,
				produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponseDTO> updateUserProfile(@PathVariable("username") String username,
																@Valid @RequestBody UserDTO request) {
		boolean isSuccess = userService.updateUserProfile(username, request);

		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponseDTO(
				isSuccess, isSuccess ?
				messageService.getMessage("success_profile_user") :
				messageService.getMessage("error_update_profile"), null));
	}
}
