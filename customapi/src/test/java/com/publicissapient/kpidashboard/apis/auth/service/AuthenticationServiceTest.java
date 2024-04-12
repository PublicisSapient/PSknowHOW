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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.exceptions.PendingApprovalException;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 *
 */
@ExtendWith(SpringExtension.class)
public class AuthenticationServiceTest {

	@Mock
	AuthenticationRepository authRepo;
	Authentication authentication = new Authentication();

	@Mock
	AuthProperties authProperties;

	@Mock
	UserInfoRepository userInfoRepository;
	@InjectMocks
	DefaultAuthenticationServiceImpl authService;

	@BeforeEach
	public void setUp() {
		authentication.setUsername("Test");
		authentication.setPassword("Ps123");
	}

	@Test
	public void testOldPwAuthentication() throws Exception {
		final String pw = "pass1";

		Authentication nonHashPass = new Authentication("u1", pw, "abc@xyz.com");
		Field pwField = nonHashPass.getClass().getDeclaredField("password");
		pwField.setAccessible(true);
		pwField.set(nonHashPass, pw);
		nonHashPass.setApproved(true);
		when(authRepo.findByUsername(Mockito.anyString())).thenReturn(nonHashPass);
		Assertions.assertNotNull(authService.authenticate("u1", "pass1"));
	}

	@Test
	public void testHashedPwAuthentication() throws Exception {
		final String pw = "pass1";

		Authentication auth = new Authentication("u1", pw, "abc@xyz.com");
		auth.setApproved(true);
		when(authRepo.findByUsername(Mockito.anyString())).thenReturn(auth);
		Assertions.assertNotNull(authService.authenticate("u1", "pass1"));
	}

	@Test
	public void getMethodTest() {
		Authentication authentication = new Authentication();
		ObjectId objectId = new ObjectId("5fd9ab0995fe13000165d0ba");
		authentication.setId(objectId);
		when(authRepo.findById(objectId)).thenReturn(Optional.of(authentication));
		Assertions.assertEquals(authService.get(objectId).getId(), objectId);
	}

	@Test
	public void findAllTest() {
		Authentication authentication = new Authentication();
		ObjectId objectId = new ObjectId("5fd9ab0995fe13000165d0ba");
		authentication.setId(objectId);
		List<Authentication> authenticationList = new ArrayList<>();
		authenticationList.add(authentication);
		when(authRepo.findAll()).thenReturn(authenticationList);
		Assertions.assertTrue(authService.all().iterator().hasNext());
	}

	@Test
	public void createAuthTest() {
		when(authRepo.count()).thenReturn(0L);
		when(authRepo.save(any(Authentication.class))).thenReturn(authentication);
		Assertions.assertNotNull(authService.create("test", "Ps123", "ps@test.com").getCredentials());
	}

	@Test
	public void updateAuthWhenUserDoesNotExists() {
		String USER_NOT_FOUND = "User Does not Exist";
		when(authRepo.findByUsername("test")).thenReturn(null);
		Assertions.assertEquals(authService.update("test", "Ps123"), USER_NOT_FOUND);
	}

	@Test
	public void updateAuth() {
		String UPDATE_SUCCESS = "User is updated";
		when(authRepo.findByUsername("test")).thenReturn(authentication);
		Assertions.assertEquals(authService.update("test", "Ps123"), UPDATE_SUCCESS);
	}

	@Test
	public void deleteAuthByIdTest() {
		ObjectId objectId = new ObjectId("5fd9ab0995fe13000165d0ba");
		when(authRepo.findById(objectId)).thenReturn(Optional.ofNullable(authentication));
		authService.delete(objectId);
		Assertions.assertNotNull(authentication);
	}

	@Test
	public void deleteAuthByUsernameTest() {
		when(authRepo.findByUsername("Test")).thenReturn(authentication);
		authService.delete("Test");
		Assertions.assertNotNull(authentication);
	}

	@Test
	public void updateFailAttemptsWhenUserNotFound(){
		when(authRepo.findByUsername("Test")).thenReturn(null);
		Assertions.assertFalse(authService.updateFailAttempts("Test",new DateTime()));
	}

	@Test
	public void updateFailAttemptsWhenUserFound(){
		when(authRepo.findByUsername("Test")).thenReturn(authentication);
		Assertions.assertTrue(authService.updateFailAttempts("Test",new DateTime()));
	}

	@Test
	public void updateFailAttemptsWhenUserFoundLoginCountIsOne() {
		authentication.setLoginAttemptCount(1);
		when(authRepo.findByUsername("Test")).thenReturn(authentication);
		Assertions.assertTrue(authService.updateFailAttempts("Test", new DateTime()));
	}

	@Test
	public void resetFailAttemptsTest(){
		when(authRepo.findByUsername("Test")).thenReturn(authentication);
		authService.resetFailAttempts("Test");
		Assertions.assertNotNull(authentication);

	}

	@Test
	public void getUserAttemptsTest(){
		when(authRepo.findByUsername("Test")).thenReturn(null);
		Assertions.assertNull(authService.getUserAttempts("Test"));
	}

	@Test
	public void getUserAttemptsWhenUserFound(){
		when(authRepo.findByUsername("Test")).thenReturn(authentication);
		authService.getUserAttempts("Test");
		Assertions.assertNotNull(authentication);
	}

	@Test
	public void authenticateTest() {
		String pendingApprovalMsg = "Login Failed: Your access request is pending for approval";
		when(authRepo.findByUsername("Test")).thenReturn(authentication);
		try {
			authService.authenticate("Test", "Ps123");
		} catch (PendingApprovalException e) {
			Assertions.assertEquals(pendingApprovalMsg, e.getMessage());
		}

	}

	@Test
	public void authenticateForAccountLockException() {
		String lockedExceptionMsg = "Account Locked: Invalid Login Limit Reached " + authentication.getUsername();
		authentication.setLoginAttemptCount(1);
		when(authRepo.findByUsername("Test")).thenReturn(authentication);
		when(authProperties.getAccountLockedThreshold()).thenReturn(1);
		try {
			authService.authenticate("Test", "Ps123");
		} catch (LockedException e) {
			Assertions.assertEquals(lockedExceptionMsg, e.getMessage());
		}

	}

	@Test
	public void authenticateForSuccess() {
		authentication.setApproved(true);
		when(authRepo.findByUsername("Test")).thenReturn(authentication);
		Assertions.assertNotNull(authService.authenticate("Test", "Ps123").getCredentials());
	}

	@Test
	public void authenticateForWrongPassword() {
		String failed_LoginMsg = "Login Failed: The username or password entered is incorrect";
		authentication.setApproved(true);
		authentication.setPassword("WrongPassword");
		when(authRepo.findByUsername("Test")).thenReturn(authentication);
		try {
			authService.authenticate("Test", "Ps123");
		} catch (BadCredentialsException e) {
			Assertions.assertEquals(failed_LoginMsg, e.getMessage());
		}
	}

	@Test
	public void isEmailExistTest() {
		List<Authentication> authenticationList = new ArrayList<>();
		authenticationList.add(authentication);
		when(authRepo.findByEmail("ps@test.com")).thenReturn(authenticationList);
		Assertions.assertTrue(authService.isEmailExist("ps@test.com"));
	}

	@Test
	public void isUsernameExistsInUserInfo() {

		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("test");
		when(userInfoRepository.findByUsername("test")).thenReturn(userInfo);
		Assertions.assertTrue(authService.isUsernameExistsInUserInfo("test"));
	}

	@Test
	public void checkIfValidOldPassword() {
		List<Authentication> authenticationList = new ArrayList<>();
		authentication.setPassword("oldpassword");
		authenticationList.add(authentication);
		when(authRepo.findByEmail("ps@test.com")).thenReturn(authenticationList);
		Assertions.assertTrue(authService.checkIfValidOldPassword("ps@test.com", "oldpassword"));
	}

	@Test
	public void checkIfValidOldPasswordDoesNotMatch() {
		List<Authentication> authenticationList = new ArrayList<>();
		when(authRepo.findByEmail("ps@test.com")).thenReturn(authenticationList);
		Assertions.assertFalse(authService.checkIfValidOldPassword("ps@test.com", "oldpassword"));
	}

	@Test
	public void changePasswordTest() {
		List<Authentication> authenticationList = new ArrayList<>();
		authenticationList.add(authentication);
		when(authRepo.findByEmail("ps@test.com")).thenReturn(authenticationList);
		when(authRepo.save(any(Authentication.class))).thenReturn(authentication);
		Assertions.assertNotNull(authService.changePassword("ps@test.com", "oldpassword").getCredentials());
	}

	@Test
	public void getAuthenticationTest(){
		when(authRepo.findByUsername("test")).thenReturn(authentication);
		Assertions.assertNotNull(authService.getAuthentication("test").getUsername());
	}

	@Test
	public void updateEmailTest(){
		when(authRepo.findByUsername("test")).thenReturn(authentication);
		Assertions.assertTrue(authService.updateEmail("test","ps@test.com"));
	}

	@Test
	public void updateEmailWhenUserNotFound(){
		when(authRepo.findByUsername("test")).thenReturn(null);
		Assertions.assertFalse(authService.updateEmail("test","ps@test.com"));
	}

	@Test
	public void isPasswordIdenticalTest() {
		Assertions.assertTrue(authService.isPasswordIdentical("test", "test"));
	}

	@Test
	public void getUsernameTest() {
		org.springframework.security.core.Authentication authentication1 = new UsernamePasswordAuthenticationToken(
				"test", "TestP");
		Assertions.assertFalse(authService.getUsername(authentication1).isEmpty());
	}

	@Test
	public void getAuthenticationByApprovedTest() {
		List<Authentication> authenticationList = new ArrayList<>();
		authenticationList.add(authentication);
		when(authRepo.findByApproved(true)).thenReturn(authenticationList);
		Assertions.assertTrue(authService.getAuthenticationByApproved(true).iterator().hasNext());
	}

	@Test
	public void getAuthNAuthResponseTest() {
		ResponseEntity<ServiceResponse> responseEntity = new ResponseEntity<ServiceResponse>(
				HttpStatusCode.valueOf(200));
		Assertions.assertNull(authService.getAuthNAuthResponse(responseEntity, "test.com"));
	}

	@Test
	public void getAuthNAuthResponseWhenError() {
		ResponseEntity<ServiceResponse> responseEntity = new ResponseEntity<ServiceResponse>(
				HttpStatusCode.valueOf(503));
		Assertions.assertNotNull(authService.getAuthNAuthResponse(responseEntity, "test.com"));
	}

}
