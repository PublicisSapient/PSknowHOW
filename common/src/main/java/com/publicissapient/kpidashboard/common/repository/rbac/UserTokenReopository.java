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

package com.publicissapient.kpidashboard.common.repository.rbac;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

import com.publicissapient.kpidashboard.common.model.rbac.UserTokenData;

/**
 * Repository for {@link UserTokenData}.
 */
public interface UserTokenReopository extends CrudRepository<UserTokenData, ObjectId> {

	/**
	 * Finds by user token.
	 *
	 * @param userToken
	 *            the user token
	 * @return the user token data
	 */
	UserTokenData findByUserToken(String userToken);

	/**
	 * Deletes by user token.
	 *
	 * @param userToken
	 *            the user token
	 */
	void deleteByUserToken(String userToken);

	/**
	 * Deletes by user name.
	 *
	 * @param userName
	 *            the user Name
	 */
	void deleteByuserName(String userName);

	void deleteAllByUserName(String userName);

	void deleteByUserNameIn(List<String> usernames);

	UserTokenData findByUserName(String userName);

	List<UserTokenData> findAllByUserName(String userName);

}
