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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.apis.auth.model.ApiToken;
import com.publicissapient.kpidashboard.apis.auth.repository.ApiTokenRepository;
import com.publicissapient.kpidashboard.common.exceptions.ApplicationException;
import com.publicissapient.kpidashboard.common.util.Encryption;
import com.publicissapient.kpidashboard.common.util.EncryptionException;

@Component
public class ApiTokenServiceImpl implements ApiTokenService {

	private final ApiTokenRepository apiTokenRepository;

	@Autowired
	public ApiTokenServiceImpl(ApiTokenRepository apiTokenRepository) {
		this.apiTokenRepository = apiTokenRepository;
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
	private static int compareDates(Date argA, Date argB) {

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
	public Collection<ApiToken> getApiTokens() {
		return Sets.newHashSet(apiTokenRepository.findAll());
	}

	@Override
	public String getApiToken(String apiUser, Long expirationDt) throws EncryptionException, ApplicationException {
		ApiToken apiToken = apiTokenRepository.findByApiUserAndExpirationDt(apiUser, expirationDt);
		String apiKey = "";
		if (apiToken == null) {
			apiKey = Encryption.getStringKey();
			apiToken = new ApiToken(apiUser, apiKey, expirationDt);
			apiTokenRepository.save(apiToken);
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
			throw new ApplicationException("Token already exists for " + apiUser + " expiring "
					+ sdf.format(new Date(apiToken.getExpirationDt())), ApplicationException.DUPLICATE_DATA);
		}
		return apiKey;
	}

	@Override
	public org.springframework.security.core.Authentication authenticate(String username, String password) {
		List<ApiToken> apiTokens = apiTokenRepository.findByApiUser(username);
		for (ApiToken apiToken : apiTokens) {
			if (isUserExists(username, apiToken) && apiToken.checkApiKey(password)) {

				Date sysdate = Calendar.getInstance().getTime();
				Date expDt = new Date(apiToken.getExpirationDt());
				if (compareDates(sysdate, expDt) <= 0) {

					Collection<String> roles = new ArrayList<>();
					roles.add("ProjectViewer");

					return new UsernamePasswordAuthenticationToken(username, password, createAuthorities(roles));
				}

			}
		}

		throw new BadCredentialsException("Login Failed: The username or password entered is incorrect");
	}

	/**
	 * check if user exists
	 *
	 * @param username
	 * @param apiToken
	 * @return
	 */
	private boolean isUserExists(String username, ApiToken apiToken) {
		return apiToken != null && username.equalsIgnoreCase(apiToken.getApiUser());
	}

	private Collection<? extends GrantedAuthority> createAuthorities(Collection<String> authorities) {
		Collection<GrantedAuthority> grantedAuthorities = new HashSet<>();
		authorities.forEach(authority -> grantedAuthorities.add(new SimpleGrantedAuthority(authority)));

		return grantedAuthorities;
	}
}