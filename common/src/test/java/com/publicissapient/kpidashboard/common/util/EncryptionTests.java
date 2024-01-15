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

package com.publicissapient.kpidashboard.common.util;

import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.security.GeneralSecurityException;

public class EncryptionTests {

	@Test
	public void testGetStringKey() throws EncryptionException {
		String key = Encryption.getStringKey();
		assertNotEquals(null, key);
		assertNotEquals("", key);
	}


	@Test
	public void testAesEncryptionAndDecryption() throws GeneralSecurityException, EncryptionException {
		String plainText = "Hello, World!";
		String key = Encryption.getStringKey();

		String encryptedText = Encryption.aesEncryptString(plainText, key);
		String decryptedText = Encryption.aesDecryptString(encryptedText, key);

		Assertions.assertEquals(plainText, decryptedText);
	}

	@Test
	public void testAesEncryptionAndDecryptionWithInvalidKey() {
		String plainText = "Hello, World!";
		String key = "InvalidKey";

		Assertions.assertThrows(GeneralSecurityException.class, () -> {
			Encryption.aesEncryptString(plainText, key);
		});

		Assertions.assertThrows(GeneralSecurityException.class, () -> {
			Encryption.aesDecryptString(plainText, key);
		});
	}

}
