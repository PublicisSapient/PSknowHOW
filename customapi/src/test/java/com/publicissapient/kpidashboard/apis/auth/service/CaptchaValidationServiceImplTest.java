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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.util.AESEncryption;

/**
 * Thi service tests captcha input text with the actual result
 * 
 * @author rku204
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CaptchaValidationServiceImplTest {
	private static final String DATA_TO_ENCRYPT = "Encrypt this sample string for test case";

	@InjectMocks
	private CaptchaValidationServiceImpl captchaValidationServiceImpl;

	@Mock
	private CustomApiConfig customApiConfig;

	@Test
	public void validateCaptcha() throws IOException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		List<Character> aesKeyValue = Arrays.asList('T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y');
		String encryptedString = AESEncryption.encrypt(DATA_TO_ENCRYPT, aesKeyValue);
		Mockito.when(customApiConfig.getAesKeyValue()).thenReturn(aesKeyValue);
		boolean resultDecrypted = captchaValidationServiceImpl.validateCaptcha(encryptedString, DATA_TO_ENCRYPT);
		assertTrue(resultDecrypted);

	}
}
