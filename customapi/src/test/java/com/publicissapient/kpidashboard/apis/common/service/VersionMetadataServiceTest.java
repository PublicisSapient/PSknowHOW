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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.common.service.impl.VersionMetadataServiceImpl;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.VersionDetails;

@RunWith(MockitoJUnitRunner.class)
public class VersionMetadataServiceTest {

	@Mock
	RestTemplate restTemplate;
	@InjectMocks
	private VersionMetadataServiceImpl versionMetadataServiceImpl;
	@Mock
	private CustomApiConfig customApiConfig;
	private File resourcePath;

	@Before
	public void setUp() throws Exception {

		resourcePath = getVersionMetaDataFile();

	}

	@Test
	public void testGetVersionMetadata() throws Exception {

		VersionMetadataServiceImpl verMetaClass = spy(versionMetadataServiceImpl);

		VersionDetails details = verMetaClass.getVersionMetadata();
		assertNotNull(details);
	}

	private File getVersionMetaDataFile() {
		String absolutePath = new File("").getAbsolutePath();
		String resource = absolutePath + "//src//main//resources//test//" + File.separator + "version.txt";
		return new File(resource);
	}
}
