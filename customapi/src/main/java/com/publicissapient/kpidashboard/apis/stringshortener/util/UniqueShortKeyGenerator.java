/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.stringshortener.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class UniqueShortKeyGenerator {

	private UniqueShortKeyGenerator() {
		throw new IllegalStateException("Utility class");
	}

	public static String generateShortKey(String input) {
		if (input == null) {
			throw new IllegalArgumentException("Input cannot be null");
		}
		log.info("Generating short key for input: {}", input.replaceAll("[^a-zA-Z0-9-_]", ""));
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			String shortKey = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
			// Return first 8 characters for a short key
			return shortKey.substring(0, 8);
		} catch (NoSuchAlgorithmException e) {
			log.error("Error generating short key for input: {}", input.replaceAll("[^a-zA-Z0-9-_]", ""), e);
			throw new IllegalStateException("Error generating short key", e);
		}
	}

}