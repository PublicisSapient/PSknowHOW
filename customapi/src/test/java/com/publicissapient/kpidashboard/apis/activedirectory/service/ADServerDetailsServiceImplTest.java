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
package com.publicissapient.kpidashboard.apis.activedirectory.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

/**
 * @author sansharm13
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ADServerDetailsServiceImplTest {
	List<GlobalConfig> globalConfigs = new ArrayList<>();
	ADServerDetail adUserDetail = new ADServerDetail();
	@InjectMocks
	private ADServerDetailsServiceImpl adUserDetailsServiceImpl;
	@Mock
	private GlobalConfigRepository globalConfigRepository;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@Mock
	private AesEncryptionService aesEncryptionService;
	@Mock
	private CustomApiConfig customApiConfig;

	/**
	 * method includes preprocesses for test cases
	 */
	@Before
	public void setUp() {
		adUserDetail.setDomain("domain");
		adUserDetail.setHost("host");
		adUserDetail.setPassword("password");
		adUserDetail.setRootDn("rootDn");
		adUserDetail.setUserDn("userDn");
		adUserDetail.setUsername("username");
		GlobalConfig globalConfig = new GlobalConfig();
		globalConfig.setAdServerDetail(adUserDetail);
		globalConfigs.add(globalConfig);
	}

	/**
	 * test addUpdateactive directoryUser
	 */

	@Test
	public void addUpdateADUser() {
		String plainText = "password";
		String encryptedString = "ahjh=kj=hgmn";
		when(globalConfigRepository.findAll()).thenReturn(globalConfigs);
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		when(aesEncryptionService.encrypt(Mockito.any(), Mockito.any())).thenReturn(encryptedString);
		ServiceResponse response = adUserDetailsServiceImpl.addUpdateADServerDetails(adUserDetail);
		assertThat("status: ", response.getSuccess(), equalTo(true));
	}

	/**
	 * test get active directoryUser
	 */
	@Test
	public void getADUserDetails() {
		when(globalConfigRepository.findAll()).thenReturn(globalConfigs);
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		ServiceResponse response = adUserDetailsServiceImpl.getADServerDetails();
		assertThat("status: ", response.getSuccess(), equalTo(true));
	}

	/**
	 * test NonSuperAdmin
	 */
	@Test
	public void getADUserNonSuperAdmin() {
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		ServiceResponse response = adUserDetailsServiceImpl.getADServerDetails();
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void udateAddADUserNonSuperAdmin() {
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		ServiceResponse response = adUserDetailsServiceImpl.getADServerDetails();
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateGetADServerDetails_Null() {
		globalConfigs.get(0).setAdServerDetail(null);
		when(globalConfigRepository.findAll()).thenReturn(globalConfigs);
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		ServiceResponse response = adUserDetailsServiceImpl.getADServerDetails();
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateGetADServerConfig() {
		when(globalConfigRepository.findAll()).thenReturn(globalConfigs);
		ADServerDetail result = adUserDetailsServiceImpl.getADServerConfig();
		assertNotNull(result);
	}

	@Test
	public void validateAddUpdateADUser_NotSuperAdmin() {
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		ServiceResponse response = adUserDetailsServiceImpl.addUpdateADServerDetails(adUserDetail);
		assertThat("status: ", response.getSuccess(), equalTo(true));
	}
}
