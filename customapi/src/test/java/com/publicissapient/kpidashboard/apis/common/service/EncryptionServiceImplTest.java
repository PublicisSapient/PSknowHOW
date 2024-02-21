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

package com.publicissapient.kpidashboard.apis.common.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.common.service.impl.EncryptionServiceImpl;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;

@RunWith(MockitoJUnitRunner.class)
public class EncryptionServiceImplTest {

	@Mock
	CustomApiConfig customApiConfig;
	@InjectMocks
	private EncryptionServiceImpl encryptionService;

	@Test
	public void encrypt() {
		Mockito.when(customApiConfig.getAesEncryptionKey()).thenReturn("708C150A5363290AAE3F579BF3746AD5");
		Assert.assertNotNull("Encryption check", encryptionService.encrypt("TestString"));
	}

}
