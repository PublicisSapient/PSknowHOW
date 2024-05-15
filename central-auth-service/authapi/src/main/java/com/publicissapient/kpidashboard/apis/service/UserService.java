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
package com.publicissapient.kpidashboard.apis.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.validation.Valid;

import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.common.model.UserDTO;

public interface UserService {

	User save(@Valid User user);

	Optional<User> findByUsername(String username);

	/**
	 * Gets username from authentication object
	 *
	 * @param authentication authentication object
	 * @return username
	 */
	String getUsername(org.springframework.security.core.Authentication authentication);

	boolean isEmailExist(String email);

	Optional<User> findByUserName(String userName);

	boolean deleteByUserName(String userName);

	List<User> findAllUnapprovedUsers();

	boolean registerUser(UserDTO request);

	void deleteUnVerifiedUser(UUID token);

	/**
	 * update user profile
	 *
	 * @param username
	 * @param request
	 * @return
	 */
	boolean updateUserProfile(String username, UserDTO request);

	/**
	 * check new password is not same as old password
	 *
	 * @param oldPassword oldpassword
	 * @param newPassword newpassword
	 * @return true if new password is not same as old password
	 */
	boolean isPasswordIdentical(String oldPassword, String newPassword);

	/**
	 * Change password and saves it to the store
	 *
	 * @param email    email of user
	 * @param password password of user
	 * @return newly created Authentication object
	 */
	org.springframework.security.core.Authentication changePassword(String email, String password);

	/**
	 * remove secure info
	 *
	 * @param user
	 * @return
	 */
	UserDTO getUserDTO(User user);

	ResetPasswordTokenStatusEnum verifyUserToken(String token);
}
