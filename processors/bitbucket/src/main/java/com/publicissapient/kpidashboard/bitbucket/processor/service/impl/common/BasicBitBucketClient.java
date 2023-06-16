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

package com.publicissapient.kpidashboard.bitbucket.processor.service.impl.common;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.bitbucket.config.BitBucketConfig;
import com.publicissapient.kpidashboard.bitbucket.constants.BitBucketConstants;
import com.publicissapient.kpidashboard.bitbucket.util.BitbucketRestOperations;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

import lombok.extern.slf4j.Slf4j;

/**
 * BasicBitBucketClient.
 */
@Slf4j
public class BasicBitBucketClient {

	/** The rest operations. */
	protected final RestOperations restOperations;

	/** The config. */
	protected final BitBucketConfig config;

	private AesEncryptionService aesEncryptionService;

	/**
	 * Instantiates a new basic bit bucket client.
	 *
	 * @param config
	 *            the config
	 * @param bitbucketRestOperations
	 *            the rest operations supplier
	 * @param aesEncryptionService
	 *            the aesEncryptionService
	 */
	@Autowired
	public BasicBitBucketClient(BitBucketConfig config, BitbucketRestOperations bitbucketRestOperations,
			AesEncryptionService aesEncryptionService) {
		this.config = config;
		this.restOperations = bitbucketRestOperations.getTypeInstance();
		this.aesEncryptionService = aesEncryptionService;
	}

	/**
	 * Decrypt password.
	 * 
	 * @param encryptedPassword
	 *            encrypted password
	 * @return plain text password
	 */
	public String decryptPassword(String encryptedPassword) {
		return StringUtils.isNotEmpty(encryptedPassword)
				? aesEncryptionService.decrypt(encryptedPassword, config.getAesEncryptionKey())
				: "";
	}

	/**
	 * Gets the response.
	 *
	 * @param userName
	 *            the user name
	 * @param password
	 *            the password
	 * @param url
	 *            the url
	 * @return the response
	 */
	protected ResponseEntity<String> getResponse(String userName, String password, String url) {
		HttpEntity<HttpHeaders> httpEntity = null;
		if (userName != null && password != null) {
			final String credentials = new StringBuilder(userName).append(':').append(password).toString();
			final byte[] encodedCred = Base64.encodeBase64(credentials.getBytes(StandardCharsets.US_ASCII));
			final String authHeader = new StringBuilder(BitBucketConstants.BASIC_AUTH_PREFIX)
					.append(new String(encodedCred)).toString();

			final HttpHeaders authHeaders = new HttpHeaders();
			authHeaders.set(BitBucketConstants.HTTP_AUTHORIZATION_HEADER, authHeader);

			httpEntity = new HttpEntity<>(authHeaders);
		}
		log.info("fetching data for user = {}, url = {}", userName, url);
		return restOperations.exchange(url, HttpMethod.GET, httpEntity, String.class);
	}

	/**
	 * Gets the JSON from response.
	 *
	 * @param payload
	 *            the payload
	 * @return the JSON from response
	 * @throws ParseException
	 *             the ParseException
	 */
	protected JSONObject getJSONFromResponse(String payload) throws ParseException {
		return (JSONObject) new JSONParser().parse(payload);
	}

	/**
	 * Gets the string.
	 *
	 * @param jsonObject
	 *            the json object
	 * @param key
	 *            the key
	 * @return the string
	 */
	protected String getString(JSONObject jsonObject, String key) {
		String val = null;
		final Object objectVal = jsonObject.get(key);
		if (objectVal != null) {
			val = objectVal.toString();
		}
		return val;
	}

}
