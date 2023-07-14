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

package com.publicissapient.kpidashboard.apis.auth.model;

import java.nio.charset.StandardCharsets;

import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.hash.Hashing;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The type Api token.
 */
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "apitoken")
public class ApiToken extends BasicModel {

	/**
	 * The Hash prefix.
	 */
	private static final String HASH_PREFIX = "sha512:";
	private String apiUser;
	private String apiKey;
	private Long expirationDt;

	/**
	 * Instantiates a new Api token.
	 *
	 * @param apiUser
	 *            the api user
	 * @param apiKey
	 *            the api key
	 * @param expirationDt
	 *            the expiration dt
	 */
	public ApiToken(String apiUser, String apiKey, Long expirationDt) {
		super();
		this.apiUser = apiUser;
		this.apiKey = hash(apiKey);
		this.expirationDt = expirationDt;
	}

	/**
	 * Hash string.
	 *
	 * @param apiKey
	 *            the api key
	 * @return the string
	 */
	private static String hash(String apiKey) {
		if (!apiKey.startsWith(HASH_PREFIX)) {
			return HASH_PREFIX + Hashing.sha512().hashString(apiKey, StandardCharsets.UTF_8).toString();
		}
		return apiKey;
	}

	/**
	 * Is hashed boolean.
	 *
	 * @return the boolean
	 */
	public boolean isHashed() {
		return apiKey.startsWith(HASH_PREFIX);
	}

	/**
	 * Check api key boolean.
	 *
	 * @param apiKey
	 *            the api key
	 * @return the boolean
	 */
	public boolean checkApiKey(String apiKey) {
		return hash(this.apiKey).equals(hash(apiKey));
	}

	@Override
	public String toString() {
		return "ApiToken [apiUser=" + apiUser + ", apiKey=" + apiKey + "]";
	}
}
