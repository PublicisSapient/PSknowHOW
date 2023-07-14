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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author anisingh4
 */
@RunWith(MockitoJUnitRunner.class)
public class AesEncryptionServiceTest {

	private static final String KEY = "1231231231231234";
	private static final String PLAIN_TEXT = "test";
	private static final String ENCRYPTED_TEXT = "5EA3cfu4HV7Cv9Ma2kxKeg==";
	@InjectMocks
	private AesEncryptionService aesEncryptionService;

	@Test
	public void encrypt() {
		String plainText = PLAIN_TEXT;
		String encryptedText = aesEncryptionService.encrypt(plainText, KEY);
		assertEquals(ENCRYPTED_TEXT, encryptedText);
	}

	@Test
	public void encrypt_NullText() {
		String encryptedText = aesEncryptionService.encrypt(null, KEY);
		assertNull(encryptedText);
	}

	@Test
	public void encrypt_NullKey() {
		String encryptedText = aesEncryptionService.encrypt(PLAIN_TEXT, null);
		assertNull(encryptedText);
	}

	@Test
	public void decrypt() {
		String result = PLAIN_TEXT;
		String actualValue = aesEncryptionService.decrypt(ENCRYPTED_TEXT, KEY);
		assertEquals(result, actualValue);
	}

	@Test
	public void decrypt_NullText() {
		String actualValue = aesEncryptionService.decrypt(null, KEY);
		assertNull(actualValue);
	}

	@Test
	public void decrypt_NullKey() {
		String actualValue = aesEncryptionService.decrypt(ENCRYPTED_TEXT, null);
		assertNull(actualValue);
	}

}