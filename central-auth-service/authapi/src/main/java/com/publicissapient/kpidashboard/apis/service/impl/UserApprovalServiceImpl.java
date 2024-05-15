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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.errors.UserNotFoundException;
import com.publicissapient.kpidashboard.apis.service.*;
import com.publicissapient.kpidashboard.apis.service.dto.UnapprovedUserDTO;

@Slf4j
@AllArgsConstructor
@Service
public class UserApprovalServiceImpl implements UserApprovalService {
	private final UserService userService;

	private final NotificationService notificationService;

	@Override
	public List<UnapprovedUserDTO> findAllUnapprovedUsers() {
		return userService.findAllUnapprovedUsers()
				.stream()
				.map(user -> UnapprovedUserDTO.builder()
											  .username(user.getUsername())
											  .email(user.getEmail())
											  .build())
				.collect(Collectors.toList());
	}

	@Override
	public boolean approveUser(String username) throws UserNotFoundException {
		Optional<User> userOptional = userService.findByUserName(username);

		User user = userOptional.orElseThrow(() -> new UserNotFoundException(username, AuthType.STANDARD));

		if (user.isUserVerified() && !user.isApproved()) {
			user.setApproved(true);
			user.setModifiedDate(LocalDateTime.now());

			this.userService.save(user);

			log.info("User: {} has been approved. Sending email confirmation.", user.getUsername());
			this.notificationService.sendUserApprovalEmail(user.getUsername(), user.getEmail());

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean rejectUser(String username) {
		return userService.deleteByUserName(username);
	}

}
