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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.config.AuthProperties;
import com.publicissapient.kpidashboard.apis.entity.ApiKey;
import com.publicissapient.kpidashboard.apis.entity.Resource;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.repository.ApiKeyRepository;
import com.publicissapient.kpidashboard.apis.service.ApiTokenService;
import com.publicissapient.kpidashboard.common.util.Encryption;
import com.publicissapient.kpidashboard.common.util.EncryptionException;

@Component
public class ApiTokenServiceImpl implements ApiTokenService {

	private final ApiKeyRepository apiKeyRepository;

	@Autowired
	AuthProperties tokenAuthProperties;

	@Autowired
	public ApiTokenServiceImpl(ApiKeyRepository apiKeyRepository) {
		this.apiKeyRepository = apiKeyRepository;
	}

	/**
	 *
	 * @param argA
	 *            firstDate
	 * @param argB
	 *            secondDate
	 * @return 0 = equal, -1 = firstDate is before secondDate, 1 = firstDate is
	 *         after secondDate
	 */
	private static int compareDates(LocalDate argA, LocalDate argB) {

		if (argA == null || argB == null) {
			return -1;
		}

		int retVal = argA.compareTo(argB);
		if (retVal == 0) { // if dates are equal.
			return 0;
		} else if (retVal < 0) { // if argA is before argument.
			return -1;
		} else { // if argA is after argument.
			return 1;
		}

	}

	@Override
	public List<ApiKey> getApiTokens() {
		return apiKeyRepository.findAll();
	}

	@Override
	public ApiKey getApiToken(String resourceName) {
		return apiKeyRepository.findByResource(resourceName);
	}

	@Override
	public org.springframework.security.core.Authentication authenticate(String resource, String resourceAPIKey) {
		ApiKey apiToken = apiKeyRepository.findByResource(resource);
		if (isResourceAPIKeyExists(resourceAPIKey, apiToken)) {

			LocalDate sysdate = LocalDate.now();
			LocalDate expDt = apiToken.getExpiryDate();
			if (compareDates(sysdate, expDt) <= 0) {

				Collection<String> roles = new ArrayList<>();
				roles.add("NA");

				return new UsernamePasswordAuthenticationToken(resource, resourceAPIKey, createAuthorities(roles));
			} else {
				throw new BadCredentialsException(
						"API Call Failed: The API-key is Expired , Please generate New API Key");
			}
		}

		throw new BadCredentialsException("API Call Failed: The resource or API-key is incorrect");
	}

	/**
	 * check if API-Key exists
	 *
	 * @param resourceAPIKey
	 * @param apiToken
	 * @return
	 */
	private boolean isResourceAPIKeyExists(String resourceAPIKey, ApiKey apiToken) {
		return apiToken != null && resourceAPIKey.equalsIgnoreCase(apiToken.getKey());
	}

	private Collection<? extends GrantedAuthority> createAuthorities(Collection<String> authorities) {
		Collection<GrantedAuthority> grantedAuthorities = new HashSet<>();
		authorities.forEach(authority -> grantedAuthorities.add(new SimpleGrantedAuthority(authority)));

		return grantedAuthorities;
	}

	@Override
	public void updateAPIKeyForResource(ApiKey apiKeyTokenExist, String resourceName, User loginUserDetail)
			throws EncryptionException {
		String apiAccessToken = Encryption.getStringKey();
		apiKeyTokenExist.setKey(apiAccessToken);
		apiKeyTokenExist.setExpiryDate(LocalDate.now().plusDays(tokenAuthProperties.getExposeAPITokenExpiryDays()));
		apiKeyTokenExist.setModifiedBy(loginUserDetail);
		apiKeyTokenExist.setModifiedDate(LocalDate.now());
		apiKeyRepository.save(apiKeyTokenExist);
	}

	@Override
	public ApiKey generateNewAPIKeyForResource(User loginUserDetail, Resource resource) throws EncryptionException {
		ApiKey apiKeyToken = new ApiKey();
		String apiAccessToken = Encryption.getStringKey();
		apiKeyToken.setResource(resource);
		apiKeyToken.setKey(apiAccessToken);
		apiKeyToken.setExpiryDate(LocalDate.now().plusDays(tokenAuthProperties.getExposeAPITokenExpiryDays()));
		apiKeyToken.setCreatedDate(LocalDate.now());
		apiKeyToken.setCreatedBy(loginUserDetail);
		apiKeyToken.setModifiedBy(loginUserDetail);
		apiKeyToken.setModifiedDate(LocalDate.now());
		apiKeyRepository.save(apiKeyToken);
		return apiKeyToken;
	}
}