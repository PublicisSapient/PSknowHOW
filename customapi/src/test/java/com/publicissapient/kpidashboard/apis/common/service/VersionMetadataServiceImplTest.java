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

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.common.service.impl.VersionMetadataServiceImpl;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.VersionDetails;

@RunWith(MockitoJUnitRunner.class)
public class VersionMetadataServiceImplTest {

	private final ResourceLoader resourceLoader = new DefaultResourceLoader();

	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private VersionMetadataServiceImpl versionMetadataServiceImpl;

	@Test
	public void testGetVersionMetadata() {

		VersionDetails details = versionMetadataServiceImpl.getVersionMetadata();
		Assert.assertNotNull(details);
	}

	@Test
	public void testGetVersionMetadataNull() {
		VersionDetails details = versionMetadataServiceImpl.getVersionMetadata();
		Assert.assertNotNull(details);
	}

	@Test
	public void testGetVersionMetadataC() {
		String pathAccountData = "classpath:\\com\\publicissapient\\kpidashboard\\apis\\filter\\service\\accountDataList";
		File file = null;
		try {
			Resource resource = resourceLoader.getResource(pathAccountData);
			file = resource.getFile();
		} catch (IOException e) {
			Assert.assertEquals(null, file);
		}

		versionMetadataServiceImpl.getVersionMetadata();
	}

}
