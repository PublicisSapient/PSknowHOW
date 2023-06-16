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

package com.publicissapient.kpidashboard.azurerepo.processor.service.impl.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

import com.publicissapient.kpidashboard.azurerepo.config.AzureRepoConfig;
import com.publicissapient.kpidashboard.azurerepo.constants.AzureRepoConstants;
import com.publicissapient.kpidashboard.azurerepo.util.AzureRepoRestOperations;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

import lombok.extern.slf4j.Slf4j;

/**
 * BasicAzureRepoClient.
 */
@Slf4j
public class BasicAzureRepoClient {

	private static final String DUMMY_USER = "dummyUser";
	/** The rest operations. */
	protected final RestOperations restOperations;

	/** The config. */
	protected final AzureRepoConfig config;

	private AesEncryptionService aesEncryptionService;

	/**
	 * Instantiates a new basic azure repo client.
	 *
	 * @param config
	 *            the config
	 * @param azurerepoRestOperations
	 *            the rest operations supplier
	 * 
	 * @param aesEncryptionService
	 *            aesEncryptionService
	 */
	@Autowired
	public BasicAzureRepoClient(AzureRepoConfig config, AzureRepoRestOperations azurerepoRestOperations,
			AesEncryptionService aesEncryptionService) {
		this.config = config;
		this.restOperations = azurerepoRestOperations.getTypeInstance();
		this.aesEncryptionService = aesEncryptionService;
	}

	/**
	 * Decrypt pat.
	 * 
	 * @param encryptedPat
	 *            encrypted pat
	 * @return plain text pat
	 */
	public String decryptPat(String encryptedPat) {
		return StringUtils.isNotEmpty(encryptedPat)
				? aesEncryptionService.decrypt(encryptedPat, config.getAesEncryptionKey())
				: "";
	}

	/**
	 * Gets the response.
	 *
	 * @param decryptedPat
	 *            the pat
	 * @param url
	 *            the url
	 * @return the response
	 */
	protected ResponseEntity<String> getResponse(String decryptedPat, String url) {
		HttpEntity<HttpHeaders> httpEntity = null;
		if (decryptedPat != null) {
			String credentials = DUMMY_USER + ":" + decryptedPat;
			final byte[] encodedCred = Base64.encodeBase64(credentials.getBytes(StandardCharsets.US_ASCII));
			final String authHeader = new StringBuilder(AzureRepoConstants.BASIC_AUTH_PREFIX)
					.append(new String(encodedCred)).toString();

			final HttpHeaders authHeaders = new HttpHeaders();
			authHeaders.set(AzureRepoConstants.HTTP_AUTHORIZATION_HEADER, authHeader);

			httpEntity = new HttpEntity<>(authHeaders);
		}
		String decodeUrl = decodeUrlUsingUTF8(url);
		if (decodeUrl != null) {
			return restOperations.exchange(decodeUrl, HttpMethod.GET, httpEntity, String.class);
		} else {
			return null;
		}
	}

	private String decodeUrlUsingUTF8(String encodeUrl) {
		try {
			String decode = URLDecoder.decode(encodeUrl, "UTF-8");
			return decode;
		} catch (UnsupportedEncodingException ex) {
			log.error("ERROR - Invalid URL for Azure {}", encodeUrl);
		}
		return null;
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
