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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.CustomCaptcha;

import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class CaptchaServiceTest {

	@Mock
	CustomApiConfig customApiConfig;

	@InjectMocks
	private CaptchaServiceImpl captchaService;

	@InjectMocks
	private CaptchaValidationServiceImpl captchaValidationService;



	@Before
	public void setUp() {
		Mockito.when(customApiConfig.getAesKeyValue()).thenReturn(
				Arrays.asList('T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'));
	}
	@Test
	public void testCaptcha() {

		CustomCaptcha captcha = captchaService.getCaptcha();

		assertNotNull(captcha);
	}

	@Test
	public void captchaValidation() {

		boolean result = captchaValidationService.validateCaptcha("ABCD", "ABCD");
		assertTrue(result == false);

	}

	@Test
	public void captchaValidationMatch() {
		boolean result = captchaValidationService.validateCaptcha("h/1n6ZXwVUz948xw8y+pmA==", "ABCD");
		assertTrue(result == true);

	}

	@Test
	public void testCaptchaRequired() {
		Mockito.when(customApiConfig.isCaptchaRequired()).thenReturn(true);
		CustomCaptcha captcha = captchaService.getCaptcha();
		assertTrue(captcha.isCaptchaRequired());

	}
}
