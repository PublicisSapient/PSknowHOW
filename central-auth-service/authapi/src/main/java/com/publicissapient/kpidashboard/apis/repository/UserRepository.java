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

import com.publicissapient.kpidashboard.apis.entity.User;

/**
 * The interface Authentication repository.
 *
 * @author Hiren Babariya
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * Find by username authentication.
	 *
	 * @param username
	 *            the username
	 * @return the authentication
	 */
	User findByUsername(String username);

	void deleteByUsername(String username);

	/**
	 * Find by email list.
	 *
	 * @param email
	 *            the email
	 * @return the authentication object
	 */
	User findByEmail(String email);

	/**
	 * @param userName
	 * @return the list of authentication object
	 */
	List<User> findByUsernameIn(List<String> userName);

	/**
	 * @param approved
	 * @return
	 */
	List<User> findByUserVerifiedAndApprovedOrderByIdDesc(Boolean userVerified, Boolean approved);

}
