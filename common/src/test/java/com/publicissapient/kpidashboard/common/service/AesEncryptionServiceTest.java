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

package com.publicissapient.kpidashboard.common.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author anisingh4
 */
@ExtendWith(SpringExtension.class)
public class AesEncryptionServiceTest {

	private static final String PLAIN_TEXT = "test";
	private static final String ENCRYPTED_TEXT = "encryptedTest";
	@InjectMocks
	private AesEncryptionService aesEncryptionService;

	@Test
	public void encrypt() {
		String plainText = PLAIN_TEXT;
		String encryptedText = aesEncryptionService.encrypt(plainText, "abc");
		assertNotNull(encryptedText);
	}

	@Test
	public void encrypt_NullText() {
		String encryptedText = aesEncryptionService.encrypt(null, "dfg");
		assertNull(encryptedText);
	}

	@Test
	public void encrypt_NullKey() {
		String encryptedText = aesEncryptionService.encrypt(PLAIN_TEXT, null);
		assertNull(encryptedText);
	}

	@Test
	public void decrypt_NullText() {
		String actualValue = aesEncryptionService.decrypt(null, "jkl");
		assertNull(actualValue);
	}

	@Test
	public void decrypt_NullKey() {
		String actualValue = aesEncryptionService.decrypt(ENCRYPTED_TEXT, null);
		assertNull(actualValue);
	}
}
