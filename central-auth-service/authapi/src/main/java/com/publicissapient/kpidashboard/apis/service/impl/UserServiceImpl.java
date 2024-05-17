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

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.config.UserInterfacePathsConfig;
import com.publicissapient.kpidashboard.apis.constant.CommonConstant;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.entity.UserVerificationToken;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.enums.NotificationCustomDataEnum;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.apis.errors.GenericException;
import com.publicissapient.kpidashboard.apis.repository.UserRepository;
import com.publicissapient.kpidashboard.apis.repository.UserVerificationTokenRepository;
import com.publicissapient.kpidashboard.apis.service.NotificationService;
import com.publicissapient.kpidashboard.apis.service.MessageService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import com.publicissapient.kpidashboard.common.model.UserDTO;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides method to perform CRUD and validation operations on user
 * authentication data.
 *
 * @author hargupta15
 */
@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;

	@Override
	public User save(@Valid User user) {
		return this.userRepository.save(user);
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	@Transactional
	public boolean deleteByUserName(String username) {
		try {
			userRepository.deleteByUsername(username);
			log.info("User: {} has been deleted.", username);
			return true;
		} catch (Exception e) {
			log.error("Error while deleting user: {}", username, e);
			return false;
		}
	}

	@Override
	public List<User> findAllUnapprovedUsers() {
		return userRepository.findByUserVerifiedAndApprovedOrderByIdDesc(true, false);
	}

	public UserDTO getUserDTO(User user) {
		UserDTO dto = null;
		if (null != user) {
			dto = UserDTO.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail())
						 //                    .approved(user.isApproved())
						 .firstName(user.getFirstName()).lastName(user.getLastName()).displayName(user.getDisplayName())
						 .authType(user.getAuthType())
						 //                         .userVerified(user.isUserVerified())
						 .build();
		}
		return dto;
	}

	/**
	 * Update user profile
	 *
	 * @param username
	 * @param request
	 * @return
	 */
	@Override
	public boolean updateUserProfile(String username, UserDTO request) {

		Optional<User> user = userRepository.findByUsername(username);
		if (user.isPresent()) {
			User userData = user.get();
			userData.setUsername(request.getUsername());
			userData.setEmail(request.getEmail().toLowerCase());
			userData.setFirstName(request.getFirstName());
			userData.setLastName(request.getLastName());
			userData.setDisplayName(request.getDisplayName());
			userRepository.save(userData);
			return true;
		}
		return false;
	}
}
