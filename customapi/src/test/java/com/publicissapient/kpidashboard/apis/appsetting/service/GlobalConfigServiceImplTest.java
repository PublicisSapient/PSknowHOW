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

package com.publicissapient.kpidashboard.apis.appsetting.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;

@RunWith(MockitoJUnitRunner.class)
public class GlobalConfigServiceImplTest {

	@Mock
	private GlobalConfigRepository globalConfigRepository;

	@InjectMocks
	private GlobalConfigServiceImpl globalConfigService;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetZephyrCloudUrlDetailsWhenGlobalConfigsEmpty() {
		ServiceResponse result = globalConfigService.getZephyrCloudUrlDetails();
		assertFalse(result.getSuccess());
		assertEquals("Fetched Zephyr Cloud Base Url successfully", result.getMessage());
		assertNull(result.getData());
	}

	@Test
	public void testGetZephyrCloudUrlDetailsWhenZephyrCloudBaseUrlIsNull() {
		// Arrange
		GlobalConfig globalConfig = new GlobalConfig();
		globalConfig.setZephyrCloudBaseUrl(null);
		// Act
		ServiceResponse result = globalConfigService.getZephyrCloudUrlDetails();

		// Assert
		assertFalse(result.getSuccess());
		assertEquals("Fetched Zephyr Cloud Base Url successfully", result.getMessage());
		assertNull(result.getData());
	}

}