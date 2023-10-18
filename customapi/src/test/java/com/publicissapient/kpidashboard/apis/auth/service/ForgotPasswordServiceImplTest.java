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

import java.util.ArrayList;
import java.util.List;

import com.publicissapient.kpidashboard.common.service.NotificationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.model.ForgotPasswordToken;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.auth.repository.ForgotPasswordTokenRepository;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.ResetPasswordTokenStatusEnum;
import com.publicissapient.kpidashboard.common.exceptions.ApplicationException;

/**
 * 
 * @author vijmishr1
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ForgotPasswordServiceImplTest {

	@Mock
	private AuthenticationRepository authenticationRepository;

	@Mock
	private ForgotPasswordTokenRepository forgotPasswordTokenRepository;

	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private CommonService commonService;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private ForgotPasswordServiceImpl forgotPasswordService;

	/**
	 * Test processForgotPassword with success result
	 */
	@Test
	public void processForgotPasswordTestOK() {

		String email = "abc@xyz.com";
		String url = "http://localhost:1234";
		List<Authentication> authenticateList = new ArrayList<>();
		Authentication authentication = new Authentication("abc", "xyz", email);
		authenticateList.add(authentication);

		Mockito.when(authenticationRepository.findByEmail(Mockito.anyString())).thenReturn(authenticateList);
		Mockito.when(customApiConfig.getForgotPasswordExpiryInterval()).thenReturn("30");
		Authentication response = forgotPasswordService.processForgotPassword(email, url);
		Assert.assertNotNull(response);
		Assert.assertEquals(response.getUsername(), authentication.getUsername());
	}

	/**
	 * Test processForgotPassword with no registered email
	 */
	@Test
	public void processForgotPasswordTestNull() {

		String email = "abc@xyz.com";
		String url = "http://localhost:1234";
		List<Authentication> authenticateList = new ArrayList<>();
		Authentication authentication = new Authentication("abc", "xyz", email);
		authenticateList.add(authentication);

		Mockito.when(authenticationRepository.findByEmail(email)).thenReturn(null);
		Authentication response = forgotPasswordService.processForgotPassword(email, url);
		Assert.assertNull(response);
	}

	/**
	 * Test validate email token with valid token expiry date
	 */

	@Test
	public void validateEmailTokenTestOk() {
		String token = "abc-xyz";
		ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
		forgotPasswordToken.setExpiryDate(30);
		Mockito.when(forgotPasswordTokenRepository.findByToken(Mockito.any())).thenReturn(forgotPasswordToken);
		ResetPasswordTokenStatusEnum responseEnum = forgotPasswordService.validateEmailToken(token);
		Assert.assertNotNull(responseEnum);
		Assert.assertEquals(ResetPasswordTokenStatusEnum.VALID, responseEnum);

	}

	/**
	 * Test validate email token with invalid token
	 */

	@Test
	public void validateEmailTokenTestNull() {
		String token = "abc-xyz";
		ForgotPasswordToken forgotPasswordToken = null;
		Mockito.when(forgotPasswordTokenRepository.findByToken(Mockito.any())).thenReturn(forgotPasswordToken);
		ResetPasswordTokenStatusEnum responseEnum = forgotPasswordService.validateEmailToken(token);
		Assert.assertNotNull(responseEnum);
		Assert.assertEquals(ResetPasswordTokenStatusEnum.INVALID, responseEnum);

	}

	/**
	 * Test validate email token with expired token
	 */

	@Test
	public void validateEmailTokenTestExpiredToken() {
		String token = "abc-xyz";
		ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
		forgotPasswordToken.setExpiryDate(-30);
		Mockito.when(forgotPasswordTokenRepository.findByToken(Mockito.any())).thenReturn(forgotPasswordToken);
		ResetPasswordTokenStatusEnum responseEnum = forgotPasswordService.validateEmailToken(token);
		Assert.assertNotNull(responseEnum);
		Assert.assertEquals(ResetPasswordTokenStatusEnum.EXPIRED, responseEnum);

	}

	/*
	 * Test with correct data
	 */
	@Test
	public void updatePasswordOK() throws Exception {
		ResetPasswordRequest updatedPasswordRequest = new ResetPasswordRequest();
		updatedPasswordRequest.setPassword("dummyPwd@1");
		updatedPasswordRequest.setResetToken("abc-xyz");
		ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
		forgotPasswordToken.setExpiryDate(30);
		forgotPasswordToken.setUsername("abc");
		String email = "abc@xyz.com";
		List<Authentication> authenticateList = new ArrayList<>();
		Authentication authentication = new Authentication("abc", "xyz", email);
		authenticateList.add(authentication);
		Mockito.when(forgotPasswordTokenRepository.findByToken(Mockito.any())).thenReturn(forgotPasswordToken);
		Mockito.when(authenticationRepository.findByUsername("abc")).thenReturn(authentication);
		Authentication response = forgotPasswordService.resetPassword(updatedPasswordRequest);
		Assert.assertNotNull(response);
		Assert.assertEquals(response.getUsername(), authentication.getUsername());
	}

	/**
	 * Return exception when user not found
	 * 
	 * @throws Exception
	 */
	@Test(expected = Exception.class)
	public void updatePasswordException() throws Exception {
		ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
		resetPasswordRequest.setPassword("abc");
		resetPasswordRequest.setResetToken("abc-xyz");

		ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
		forgotPasswordToken.setExpiryDate(30);
		forgotPasswordToken.setUsername("abc");

		Mockito.when(forgotPasswordTokenRepository.findByToken(Mockito.any())).thenReturn(forgotPasswordToken);

		Mockito.when(authenticationRepository.findByUsername("abc")).thenReturn(null);
		forgotPasswordService.resetPassword(resetPasswordRequest);
	}

	/**
	 * Return exception when token is invalid
	 * 
	 * @throws Exception
	 */
	@Test(expected = Exception.class)
	public void updatePasswordInvalidToken() throws Exception {
		ResetPasswordRequest updatedPasswordRequest = new ResetPasswordRequest();
		updatedPasswordRequest.setPassword("abc");
		updatedPasswordRequest.setResetToken("abc-xyz");

		Authentication authentication = new Authentication("abc", "xyz", "abc@xyz.com");
		Mockito.when(forgotPasswordTokenRepository.findByToken(Mockito.any())).thenReturn(null);

		forgotPasswordService.resetPassword(updatedPasswordRequest);
	}

	/*
	 * Test with incorrect data
	 */
	@Test
	public void updatePasswordNoRuleFollow() {
		ResetPasswordRequest updatedPasswordRequest = new ResetPasswordRequest();
		updatedPasswordRequest.setPassword("xyz");
		updatedPasswordRequest.setResetToken("abc-xyz");
		ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
		forgotPasswordToken.setExpiryDate(30);
		forgotPasswordToken.setUsername("abc");
		String email = "abc@xyz.com";
		List<Authentication> authenticateList = new ArrayList<>();
		Authentication authentication = new Authentication("abc", "xyz", email);
		authenticateList.add(authentication);
		Mockito.when(forgotPasswordTokenRepository.findByToken(Mockito.any())).thenReturn(forgotPasswordToken);
		Mockito.when(authenticationRepository.findByUsername("abc")).thenReturn(authentication);
		Assert.assertThrows(ApplicationException.class, () -> {
			forgotPasswordService.resetPassword(updatedPasswordRequest);
		});
	}

	@Test
	public void updatePasswordSameAsOld() {
		ResetPasswordRequest updatedPasswordRequest = new ResetPasswordRequest();
		updatedPasswordRequest.setPassword("dummyPwd@1");
		updatedPasswordRequest.setResetToken("abc-xyz");
		ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
		forgotPasswordToken.setExpiryDate(30);
		forgotPasswordToken.setUsername("abc");
		String email = "abc@xyz.com";
		List<Authentication> authenticateList = new ArrayList<>();
		Authentication authentication = new Authentication("abc", "dummyPwd@1", email);
		authenticateList.add(authentication);
		Mockito.when(forgotPasswordTokenRepository.findByToken(Mockito.any())).thenReturn(forgotPasswordToken);
		Mockito.when(authenticationRepository.findByUsername("abc")).thenReturn(authentication);
		Assert.assertThrows(ApplicationException.class, () -> {
			forgotPasswordService.resetPassword(updatedPasswordRequest);
		});
	}

	@Test
	public void updatePasswordContainingUserName() {
		ResetPasswordRequest updatedPasswordRequest = new ResetPasswordRequest();
		updatedPasswordRequest.setPassword("Abc@1234");
		updatedPasswordRequest.setResetToken("abc-xyz");
		ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
		forgotPasswordToken.setExpiryDate(30);
		forgotPasswordToken.setUsername("abc");
		String email = "abc@xyz.com";
		List<Authentication> authenticateList = new ArrayList<>();
		Authentication authentication = new Authentication("abc", "dummyPwd@1", email);
		authenticateList.add(authentication);
		Mockito.when(forgotPasswordTokenRepository.findByToken(Mockito.any())).thenReturn(forgotPasswordToken);
		Mockito.when(authenticationRepository.findByUsername("abc")).thenReturn(authentication);
		Assert.assertThrows(ApplicationException.class, () -> {
			forgotPasswordService.resetPassword(updatedPasswordRequest);
		});
	}

}
