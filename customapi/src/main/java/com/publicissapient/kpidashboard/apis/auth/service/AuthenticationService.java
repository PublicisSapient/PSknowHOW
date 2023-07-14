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

package com.publicissapient.kpidashboard.apis.auth.service;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;

import com.publicissapient.kpidashboard.apis.auth.model.Authentication;

/**
 * An Interface to provide authentication service.
 */
public interface AuthenticationService {

	/**
	 * Fetches all registered users, sorted.
	 *
	 * @return all users
	 */
	Iterable<Authentication> all();

	/**
	 * Fetches an AuthenticationObject.
	 *
	 * @param id
	 *            authentication unique identifier
	 * @return Authentication instance
	 */
	Authentication get(ObjectId id);

	/**
	 * Creates a new Users and saves it to the store.
	 *
	 * @param username
	 *            new Authentication to createCollectorItem
	 * @return newly created Authentication object
	 */
	org.springframework.security.core.Authentication create(String username, String password, String email);

	/**
	 * Updates an existing quthentication instance.
	 *
	 * @param username
	 *            Authentication to update
	 * @return updated Authentication instance
	 */
	String update(String username, String password);

	/**
	 * Deletes an existing Authentication instance.
	 *
	 * @param id
	 *            unique identifier of authentication to delete
	 */
	void delete(ObjectId id);

	/**
	 * Deletes an existing authentication instance
	 */
	void delete(String username);

	/**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	org.springframework.security.core.Authentication authenticate(String username, String password);

	/**
	 * update failed attempt and date
	 * 
	 * @param userName
	 * @param unsuccessAttemptTime
	 * @return status
	 */
	Boolean updateFailAttempts(String userName, DateTime unsuccessAttemptTime);

	/**
	 * reset user attempt and date
	 * 
	 * @param userName
	 */
	void resetFailAttempts(String userName);

	/**
	 * get user attempts
	 * 
	 * @param userName
	 * @return
	 */
	Integer getUserAttempts(String userName);

	/**
	 * check email is exist in db
	 * 
	 * @param email
	 * @return
	 */
	boolean isEmailExist(String email);

	/**
	 * Checks if username already exists in db
	 * 
	 * @param username
	 *            the username
	 * @return true if username already exists in db
	 */
	boolean isUsernameExists(String username);

	/**
	 * Checks if username already exists in USERINFO collection
	 * 
	 * @param username
	 *            the username
	 * @return true if username already exists in db
	 */
	boolean isUsernameExistsInUserInfo(String username);

	/**
	 * Check if valid old Password
	 * 
	 * @param email
	 *            email id
	 * @param oldPassword
	 *            password
	 * @return true/false
	 */
	boolean checkIfValidOldPassword(String email, String oldPassword);

	/**
	 * Change password and saves it to the store
	 * 
	 * @param email
	 *            email of user
	 * @param password
	 *            password of user
	 * @return newly created Authentication object
	 */
	org.springframework.security.core.Authentication changePassword(String email, String password);

	/**
	 * Gets authentication object
	 * 
	 * @param username
	 *            username
	 * @return authentication
	 */
	Authentication getAuthentication(String username);

	/**
	 * Update email id of the user
	 * 
	 * @param username
	 *            the username
	 * @param email
	 *            the email
	 * @return true if successfully updated
	 */
	boolean updateEmail(String username, String email);

	/**
	 * check new password is not same as old password
	 * 
	 * @param oldPassword
	 *            oldpassword
	 * @param newPassword
	 *            newpassword
	 * @return true if new password is not same as old password
	 */
	boolean isPasswordIdentical(String oldPassword, String newPassword);

	/**
	 * Gets logged in user's username
	 * 
	 * @return logged in user
	 */
	String getLoggedInUser();

	/**
	 * Gets username from authentication object
	 * 
	 * @param authentication
	 *            authentication object
	 * @return username
	 */
	String getUsername(org.springframework.security.core.Authentication authentication);

	Iterable<Authentication> getAuthenticationByApproved(boolean approved);

}
