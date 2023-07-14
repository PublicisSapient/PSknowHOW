package com.publicissapient.kpidashboard.apis.auth.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.activedirectory.service.ADServerDetailsService;
import com.publicissapient.kpidashboard.apis.auth.exceptions.InvalidAuthTypeConfigException;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeConfig;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeStatus;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.model.application.ValidationMessage;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

@RunWith(MockitoJUnitRunner.class)
public class AuthTypesConfigServiceImplTest {

	@Mock
	private ADServerDetailsService adServerDetailsService;

	@Mock
	private GlobalConfigRepository globalConfigRepository;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private AuthTypeConfigValidator authTypeConfigValidator;

	@Mock
	private TokenAuthenticationService tokenAuthenticationService;

	@Mock
	private UserInfoService userInfoService;

	@InjectMocks
	private AuthTypesConfigServiceImpl authTypesConfigService;

	@Test
	public void saveAuthTypeConfig_OnlyAdLogin() {
		ValidationMessage validationMessage = new ValidationMessage();
		validationMessage.setValid(true);
		when(authTypeConfigValidator.validateConfig(any(AuthTypeConfig.class))).thenReturn(validationMessage);
		when(globalConfigRepository.findAll()).thenReturn(createDefaultGlobalConfigCollection());
		when(globalConfigRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
		when(userInfoService.getUserInfoByAuthType(AuthType.STANDARD.name())).thenReturn(getStandardUsers());
		doNothing().when(tokenAuthenticationService).invalidateAuthToken(anyList());
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(false, true, createAdServerDetails());
		AuthTypeConfig result = authTypesConfigService.saveAuthTypeConfig(authTypeConfig);
		assertNotNull(result);

	}

	@Test
	public void saveAuthTypeConfig_OnlyStandardLogin() {
		ValidationMessage validationMessage = new ValidationMessage();
		validationMessage.setValid(true);
		when(authTypeConfigValidator.validateConfig(any(AuthTypeConfig.class))).thenReturn(validationMessage);
		when(globalConfigRepository.findAll()).thenReturn(createDefaultGlobalConfigCollection());
		when(globalConfigRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
		when(userInfoService.getUserInfoByAuthType(AuthType.LDAP.name())).thenReturn(getAdUsers());
		doNothing().when(tokenAuthenticationService).invalidateAuthToken(anyList());
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(true, false, null);
		AuthTypeConfig result = authTypesConfigService.saveAuthTypeConfig(authTypeConfig);
		assertNotNull(result);

	}

	@Test
	public void saveAuthTypeConfig_StandardAndAdLogin() {
		ValidationMessage validationMessage = new ValidationMessage();
		validationMessage.setValid(true);
		when(authTypeConfigValidator.validateConfig(any(AuthTypeConfig.class))).thenReturn(validationMessage);
		when(globalConfigRepository.findAll()).thenReturn(createDefaultGlobalConfigCollection());
		when(globalConfigRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
		doNothing().when(tokenAuthenticationService).invalidateAuthToken(anyList());
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(true, true, createAdServerDetails());
		AuthTypeConfig result = authTypesConfigService.saveAuthTypeConfig(authTypeConfig);
		assertNotNull(result);

	}

	@Test(expected = InvalidAuthTypeConfigException.class)
	public void saveAuthTypeConfig_Invalid() {
		ValidationMessage validationMessage = new ValidationMessage();
		validationMessage.setValid(false);
		validationMessage.setMessage("Invalid input");
		AuthTypeConfig authTypeConfig = createAuthTypeConfig(true, true, createAdServerDetails());
		when(authTypeConfigValidator.validateConfig(any(AuthTypeConfig.class))).thenReturn(validationMessage);

		authTypesConfigService.saveAuthTypeConfig(authTypeConfig);
	}

	@Test
	public void getAuthTypeConfig() {
		when(globalConfigRepository.findAll()).thenReturn(createDefaultGlobalConfigCollection());
		AuthTypeConfig authTypeConfig = authTypesConfigService.getAuthTypeConfig();
		assertNotNull(authTypeConfig);
	}

	@Test
	public void getAuthTypesStatus() {

		when(globalConfigRepository.findAll()).thenReturn(createDefaultGlobalConfigCollection());
		AuthTypeStatus authTypesStatus = authTypesConfigService.getAuthTypesStatus();
		assertNotNull(authTypesStatus);
	}

	private List<GlobalConfig> createDefaultGlobalConfigCollection() {
		GlobalConfig globalConfig = new GlobalConfig();

		globalConfig.setAuthTypeStatus(createAuthTypeStatus(true, false));
		globalConfig.setAdServerDetail(null);
		return Arrays.asList(globalConfig);
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
		adServerDetail.setPort(100);

		return adServerDetail;
	}

	private AuthTypeStatus createAuthTypeStatus(boolean standardLogin, boolean adLogin) {
		AuthTypeStatus authTypeStatus = new AuthTypeStatus();
		authTypeStatus.setStandardLogin(standardLogin);
		authTypeStatus.setAdLogin(adLogin);
		return authTypeStatus;
	}

	private List<UserInfo> getStandardUsers() {

		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user1");
		userInfo.setAuthType(AuthType.STANDARD);
		return Arrays.asList(userInfo);
	}

	private List<UserInfo> getAdUsers() {

		UserInfo userInfo = new UserInfo();
		userInfo.setUsername("user2");
		userInfo.setAuthType(AuthType.LDAP);
		return Arrays.asList(userInfo);
	}
}