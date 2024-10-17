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

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.repository.UserRepository;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import com.publicissapient.kpidashboard.apis.service.dto.UserDTO;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	private final TokenAuthenticationService tokenAuthenticationService;

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
			return true;
		} catch (Exception e) {
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
					.firstName(user.getFirstName()).lastName(user.getLastName()).displayName(user.getDisplayName())
					.authType(user.getAuthType()).approved(user.isApproved()).verified(user.isUserVerified()).build();
		}
		return dto;
	}

	/**
	 * Update user profile
	 *
	 * @param request
	 * @return
	 */
	@Override
	public boolean updateUserProfile(UserDTO request) {

		Optional<User> user = userRepository.findByUsername(request.getUsername());
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

	@Override
	public UserDTO getCurrentUser(HttpServletRequest request) {
		try {
			Optional<String> jwt = CookieUtil.getCookieValue(request, CookieUtil.COOKIE_NAME);
			if (jwt.isPresent()) {
				String username = tokenAuthenticationService.getSubject(jwt.get());

				Optional<User> userOptional = findByUsername(username);

				return userOptional.map(this::getUserDTO).orElse(null);
			}

			return null;
		} catch (Exception e) {
			log.debug("Token expired");
			throw e;
		}
	}
}
