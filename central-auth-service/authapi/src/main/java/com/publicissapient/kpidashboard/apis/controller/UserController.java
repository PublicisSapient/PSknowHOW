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

import static com.publicissapient.kpidashboard.apis.constant.CommonConstant.ERROR_INVALID_USER;
import static com.publicissapient.kpidashboard.apis.constant.CommonConstant.SUCCESS_VALID_TOKEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.service.MessageService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import com.publicissapient.kpidashboard.apis.service.dto.ServiceResponseDTO;
import com.publicissapient.kpidashboard.apis.service.dto.UserDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
			return ResponseEntity.ok(new ServiceResponseDTO(true, messageService.getMessage(SUCCESS_VALID_TOKEN), userDTO));
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ServiceResponseDTO(false, messageService.getMessage(ERROR_INVALID_USER), null));
		}
	}

	@PutMapping(value = "/users/updateProfile", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponseDTO> updateUserProfile(@Valid @RequestBody UserDTO request) {
		boolean isSuccess = userService.updateUserProfile(request);

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponseDTO(isSuccess,
						isSuccess
								? messageService.getMessage("success_profile_user")
								: messageService.getMessage("error_update_profile"),
						null));
	}
}
