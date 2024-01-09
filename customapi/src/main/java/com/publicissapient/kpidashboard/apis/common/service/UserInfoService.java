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

package com.publicissapient.kpidashboard.apis.common.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.UserDetailsResponseDTO;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfoDTO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * An Interface to gets users and authorities.
 *
 * @author anisingh4
 */
public interface UserInfoService {

	/**
	 * Gets authorities.
	 *
	 * @param username
	 *            the username
	 * @return the authorities
	 */
	Collection<GrantedAuthority> getAuthorities(String username);

	/**
	 * Gets user info.
	 *
	 * @param username
	 *            the username
	 * @param authType
	 *            the auth type
	 * @return the user info
	 */
	UserInfo getUserInfo(String username, AuthType authType);

	UserInfo getUserInfo(String username);

	/**
	 * Gets users.
	 *
	 * @return the users
	 */
	Collection<UserInfo> getUsers();

	/**
	 * Gets users and add/delete some fields.
	 *
	 * @return the ServiceResponse
	 */
	ServiceResponse getAllUserInfo();

	/**
	 * Demote from admin user info.
	 *
	 * @param username
	 *            the username
	 * @param authType
	 *            the auth type
	 * @return the user info
	 */
	UserInfo demoteFromAdmin(String username, AuthType authType);

	/**
	 * Is user valid boolean.
	 *
	 * @param userId
	 *            the user id
	 * @param authType
	 *            the auth type
	 * @return the boolean
	 */
	boolean isUserValid(String userId, AuthType authType);

	/**
	 * update user info if already present
	 * 
	 * @param userInfo
	 * @return updated {@link UserInfo} object or null
	 */
	public UserInfo updateUserInfo(UserInfo userInfo);

	/**
	 * This method is updateAccessOfUserInfo() change the role of user
	 * 
	 * @param username
	 * @param userInfo
	 * @return Updated user details
	 **/
	ServiceResponse updateUserRole(String username, UserInfo userInfo);

	/**
	 * Return userinfo along with email in case of ldap or standardlogin
	 * 
	 * @param username
	 *            username
	 * @param authType
	 *            authtype enum
	 * @return userinfo
	 */
	public UserInfo getUserInfoWithEmail(String username, AuthType authType);

	UserInfo save(UserInfo userInfo);

	UserInfo createDefaultUserInfo(String username, AuthType authType, String email);

	/**
	 * This method is for deleting the users
	 *
	 * @param username
	 *            username
	 */
	ServiceResponse deleteUser(String username);

	List<UserInfo> getUserInfoByAuthType(String userType);

	/**
	 * get user details for profile screen and response will be same as login api
	 * 
	 * @param request
	 * @return
	 */
	UserDetailsResponseDTO getUserInfoByToken(HttpServletRequest request);

	/**
	 * This method return user info dto object comparing username,authtype and email
	 * 
	 * @param username
	 * @param authType
	 * @param email
	 * @return user info dto object
	 */
	UserInfoDTO getOrSaveDefaultUserInfo(String username, AuthType authType, String email);

	/**
	 * This method return user info object by comparing username, auth type and
	 * authorities
	 * 
	 * @param userName
	 * @param authType
	 * @param authorities
	 * @return user info object
	 */
	UserInfo getOrSaveUserInfo(String userName, AuthType authType, List<String> authorities);
}
