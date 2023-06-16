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

package com.publicissapient.kpidashboard.azurerepo.processor;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.azurerepo.config.AzureRepoConfig;
import com.publicissapient.kpidashboard.azurerepo.model.AzureRepoModel;
import com.publicissapient.kpidashboard.azurerepo.processor.service.impl.AzureRepoServerURIBuilder;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;

@ExtendWith(SpringExtension.class)
class AzureRepoServerURIBuilderTests {

	@Mock
	private AzureRepoModel azureRepoRepo;

	@Mock
	private AzureRepoConfig config;

	private AzureRepoServerURIBuilder uriBuilder;

	@BeforeEach
	public void init() {

		Map<String, Object> options = new HashMap<>();
		options.put("repositoryName", "testRepo");
		options.put("apiVersion", "5.1");

		when(config.getApi()).thenReturn("_apis/git/repositories");
		ProcessorToolConnection azureRepoProcessorInfo = new ProcessorToolConnection();
		azureRepoProcessorInfo.setApiVersion("5.1");
		azureRepoProcessorInfo.setBranch("master");
		azureRepoProcessorInfo.setUrl("https://test.com/testUser/testRepo");
		azureRepoProcessorInfo.setPat("testPat");
		azureRepoProcessorInfo.setRepositoryName("testRepo");

		uriBuilder = new AzureRepoServerURIBuilder(azureRepoRepo, config, azureRepoProcessorInfo);
	}

	@Test
	public void testBuild() throws Exception {
		String url = uriBuilder.build();
		String expected = "https://test.com/testUser/testRepo/_apis/git/repositories/testRepo/commits?searchCriteria.itemVersion.version=master&api.Version=5.1";
		Assert.assertEquals(expected, url);
	}

}
