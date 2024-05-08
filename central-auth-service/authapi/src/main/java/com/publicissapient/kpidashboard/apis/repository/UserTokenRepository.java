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

package com.publicissapient.kpidashboard.apis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.apis.entity.UserToken;

/**
 * Repository for {@link UserToken}.
 */
@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

	/**
	 * Finds by user token.
	 *
	 * @param userToken
	 *            the user token
	 * @return the user token data
	 */
	UserToken findByToken(String userToken);

	/**
	 * Deletes by user token.
	 *
	 * @param userToken
	 *            the user token
	 */
	void deleteByToken(String userToken);

	/**
	 * Deletes by user name.
	 *
	 * @param userName
	 *            the user Name
	 */
	void deleteByUsername(String userName);

	void deleteAllByUsername(String userName);

	void deleteByUsernameIn(List<String> usernames);

	UserToken findByUsername(String userName);

	List<UserToken> findAllByUsername(String userName);

	UserToken findByUsernameAndExpiryDateBefore(String username, String date);
}
