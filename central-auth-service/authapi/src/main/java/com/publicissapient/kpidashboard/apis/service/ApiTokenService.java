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

import com.publicissapient.kpidashboard.apis.entity.ApiKey;
import com.publicissapient.kpidashboard.apis.entity.Resource;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.common.util.EncryptionException;

/**
 * The interface Api token service.
 */
public interface ApiTokenService {
	/**
	 * Gets api tokens.
	 *
	 * @return the api tokens
	 */
	List<ApiKey> getApiTokens();

	/**
	 * Gets api token.
	 *
	 * @param resourceName
	 *            the api user
	 * @return the api token
	 * @throws EncryptionException
	 *             the encryption exception
	 */
	ApiKey getApiToken(String resourceName);

	/**
	 *
	 * @param apiKeyTokenExist
	 * @param resourceName
	 * @param loginUserDetail
	 */
	void updateAPIKeyForResource(ApiKey apiKeyTokenExist, String resourceName, User loginUserDetail)
			throws EncryptionException;

	/**
	 *
	 * @param loginUserDetail
	 * @param resource
	 * @return
	 */
	ApiKey generateNewAPIKeyForResource(User loginUserDetail, Resource resource) throws EncryptionException;

	/**
	 * Authenticate user.
	 *
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @return the org . springframework . security . core . authentication
	 */
	org.springframework.security.core.Authentication authenticate(String username, String password);
}
