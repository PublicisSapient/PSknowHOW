package com.publicissapient.kpidashboard.apis.auth.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeConfig;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeStatus;
import com.publicissapient.kpidashboard.common.model.application.ValidationMessage;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

@RunWith(MockitoJUnitRunner.class)
public class AuthTypeConfigValidatorTest {

	@Mock
	private UserInfoRepository userInfoRepository;

	@InjectMocks
	private AuthTypeConfigValidator authTypeConfigValidator;

	@Test
	public void validateConfig_allDisable() {
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(false, false, null);
		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);
		assertFalse(validationMessage.isValid());
	}

	@Test
	public void validateConfig_StandardLoginOnly_Valid() {
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(true, false, null);
		when(userInfoRepository.findByAuthTypeAndAuthoritiesIn(anyString(), anyList()))
				.thenReturn(getStandardUsers(Constant.ROLE_SUPERADMIN));
		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);

		assertTrue(validationMessage.isValid());
	}

	@Test
	public void validateConfig_StandardLoginOnly_Invalid() {
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(true, false, null);
		when(userInfoRepository.findByAuthTypeAndAuthoritiesIn(anyString(), anyList())).thenReturn(new ArrayList<>());
		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);

		assertFalse(validationMessage.isValid());
	}

	@Test
	public void validateConfig_AdLoginOnly_Valid() {
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(false, true, createAdServerDetails());
		when(userInfoRepository.findByAuthTypeAndAuthoritiesIn(anyString(), anyList()))
				.thenReturn(getAdUsers(Constant.ROLE_SUPERADMIN));
		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);

		assertTrue(validationMessage.isValid());
	}

	@Test
	public void validateConfig_AdLoginOnly_Invalid() {
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(false, true, createAdServerDetails());
		when(userInfoRepository.findByAuthTypeAndAuthoritiesIn(anyString(), anyList())).thenReturn(new ArrayList<>());
		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);

		assertFalse(validationMessage.isValid());
	}

	@Test
	public void validateConfig_AdLoginOnly_NoAdDetails() {
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(false, true, null);

		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);

		assertFalse(validationMessage.isValid());
	}

	@Test
	public void validateConfig_AdLoginOnly_EmptyUsername() {
		ADServerDetail adServerDetails = createAdServerDetails();
		adServerDetails.setUsername("");
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(false, true, adServerDetails);

		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);

		assertFalse(validationMessage.isValid());
	}

	@Test
	public void validateConfig_AdLoginOnly_EmptyPassword() {
		ADServerDetail adServerDetails = createAdServerDetails();
		adServerDetails.setPassword("");
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(false, true, adServerDetails);

		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);

		assertFalse(validationMessage.isValid());
	}

	@Test
	public void validateConfig_AdLoginOnly_EmptyHost() {
		ADServerDetail adServerDetails = createAdServerDetails();
		adServerDetails.setHost("");
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(false, true, adServerDetails);

		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);

		assertFalse(validationMessage.isValid());
	}

	@Test
	public void validateConfig_AdLoginOnly_EmptyRootDn() {
		ADServerDetail adServerDetails = createAdServerDetails();
		adServerDetails.setRootDn("");
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(false, true, adServerDetails);

		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);

		assertFalse(validationMessage.isValid());
	}

	@Test
	public void validateConfig_AdLoginOnly_EmptyDomain() {
		ADServerDetail adServerDetails = createAdServerDetails();
		adServerDetails.setDomain("");
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(false, true, adServerDetails);

		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);

		assertFalse(validationMessage.isValid());
	}

	@Test
	public void validateConfig_AdLoginOnly_EmptyPort() {
		ADServerDetail adServerDetails = createAdServerDetails();
		adServerDetails.setPort(0);
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(false, true, adServerDetails);

		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);

		assertFalse(validationMessage.isValid());
	}

	@Test
	public void validateConfig_StandardAndAdLogin_Valid() {
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(true, true, createAdServerDetails());

		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);
		assertTrue(validationMessage.isValid());
	}

	@Test
	public void validateConfig_StandardAndAdLogin_Invalid() {
		ADServerDetail adServerDetails = createAdServerDetails();
		adServerDetails.setDomain("");
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(true, true, adServerDetails);

		ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);
		assertFalse(validationMessage.isValid());
	}

	private AuthTypeConfig createAuthTypeConfig(boolean standardLogin, boolean adLogin, ADServerDetail adServerDetail) {
		AuthTypeConfig authTypeConfig = new AuthTypeConfig();
		authTypeConfig.setAuthTypeStatus(createAuthTypeStatus(standardLogin, adLogin));
		authTypeConfig.setAdServerDetail(adServerDetail);
		return authTypeConfig;
	}

	private ADServerDetail createAdServerDetails() {
		ADServerDetail adServerDetail = new ADServerDetail();
		adServerDetail.setUsername("TestUser");
		adServerDetail.setPassword("Test@123");
		adServerDetail.setHost("testHost");
		adServerDetail.setRootDn("testRootDn");
		adServerDetail.setDomain("testDomain");
		adServerDetail.setPort(100);

		return adServerDetail;
	}

	private AuthTypeStatus createAuthTypeStatus(boolean standardLogin, boolean adLogin) {
		AuthTypeStatus authTypeStatus = new AuthTypeStatus();
		authTypeStatus.setStandardLogin(standardLogin);
		authTypeStatus.setAdLogin(adLogin);
		return authTypeStatus;
	}

	private List<UserInfo> getStandardUsers(String role) {

		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user1");
		userInfo.setAuthType(AuthType.STANDARD);
		userInfo.setAuthorities(Arrays.asList(role));
		return Arrays.asList(userInfo);
	}

	private List<UserInfo> getAdUsers(String role) {

		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user2");
		userInfo.setAuthType(AuthType.LDAP);
		userInfo.setAuthorities(Arrays.asList(role));
		return Arrays.asList(userInfo);
	}

}