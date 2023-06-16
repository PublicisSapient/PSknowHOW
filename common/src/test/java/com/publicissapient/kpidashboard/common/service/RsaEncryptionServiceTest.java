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
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author anisingh4
 */
@RunWith(MockitoJUnitRunner.class)
public class RsaEncryptionServiceTest {

	private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCV+CXwDNe+mijxGhOUH+UXBORUJsKSfdu"
			+ "EoDaz8CnGm9QGprvQ1/UDmN4f/cLVun2Q/nK2ZFS6QfHyyTkmG9ztA5gjjt9fC9YI8nKIjmm7mgHQ/Bi8l/V20kMan/iPFmonHcz5bPc"
			+ "05UCLmLglV2wgGgEwfilYj1msCNp4vhIEJwIDAQAB";
	private static final String PRIVATE_KEY = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJX4JfAM176aKPEaE5Qf5"
			+ "RcE5FQmwpJ924SgNrPwKcab1Aamu9DX9QOY3h/9wtW6fZD+crZkVLpB8fLJOSYb3O0DmCOO318L1gjycoiOabuaAdD8GLyX9XbSQxq"
			+ "f+I8WaicdzPls9zTlQIuYuCVXbCAaATB+KViPWawI2ni+EgQnAgMBAAECgYAO3yvL8UoXwavbtO1KHBuYu8h0u99Bra9uKIEkOOW7p"
			+ "0pQWop3fGL0t10XqQ+AKF05WrI/ehWY3t9CZ0f+inbO9rmNiS0oF5EoFclRTTjDOqlIzEjC1v8VdWbMGRoZpnxNETL4VNNw9JviaQ4n"
			+ "lUqVqTzW2rct7VEzLCUWnibrqQJBAOoCLo6JFF+tPzwVt0FDoFwazeqyGlNZqDE3HcuigtIOjYSlwtRQFBh1lQ7yEQhfKICbcnx91"
			+ "QqDu9PT0hQ/Wv0CQQCkECL7HDghYE2YNnyrQCpPzg9BthR4m3leWSx07JHHQUPnYbABo1DayGbh/JIk+YypGDe9wlXESgDbFjg"
			+ "96R7zAkBoLFZ7f4zBVwe6gNRWyns95XTb0TOk/VnBpw6tk4f3aSEY9w33pDp99QJJZ/urJWmLbygVQZMwnhpkn3x7JQUlAkAV8S"
			+ "aMiQikxhl6mwbvbGR8SeXWdwCj5L9FtA1zEdSpXnwzbOg6P9pneFfyL3JoSYvbbaa+1UfdObyFkKIy/YOrAkA7tASXHfve+r3COF"
			+ "JI8cIAfZtwcK7drOhP8aedKlb/CWvuzuo4qjFnMkjrZ92BZ28se/DeVimvGkP47GDEFpgb";
	@InjectMocks
	RsaEncryptionService rsaEncryptionService;

	@Test
	public void cipherTest() {

		String plain = "Test plain text";

		// Encrypt plain as a cipher.
		String cipherContent = rsaEncryptionService.encrypt(plain, PUBLIC_KEY);
		// Decrypt cipher to original plain.
		String decryptResult = rsaEncryptionService.decrypt(cipherContent, PRIVATE_KEY);
		// Assertion of 'plain' and 'decryptResult'.
		assertEquals(plain, decryptResult);

	}

	@Test
	public void generateKeys() {
		Map<String, String> keys = rsaEncryptionService.generateKeys();
		assertNotNull(keys.get("private"));
		assertNotNull(keys.get("public"));
	}

}