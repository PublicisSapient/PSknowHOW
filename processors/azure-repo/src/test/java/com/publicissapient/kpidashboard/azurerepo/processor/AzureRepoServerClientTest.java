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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.azurerepo.config.AzureRepoConfig;
import com.publicissapient.kpidashboard.azurerepo.model.AzureRepoModel;
import com.publicissapient.kpidashboard.azurerepo.processor.service.impl.AzureRepoServerClient;
import com.publicissapient.kpidashboard.azurerepo.processor.service.impl.AzureRepoServerURIBuilder;
import com.publicissapient.kpidashboard.azurerepo.util.AzureRepoRestOperations;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

@ExtendWith(SpringExtension.class)
class AzureRepoServerClientTest {

	@Mock
	private AzureRepoConfig config;

	@Mock
	private RestOperations restTemplate;

	@Mock
	private AzureRepoRestOperations azurerepoRestOperations;

	private AzureRepoServerClient stashClient;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@BeforeEach
	public void init() throws Exception {

		when(azurerepoRestOperations.getTypeInstance()).thenReturn(restTemplate);

		config = new AzureRepoConfig();
		config.setApi("_apis/git/repositories");
		config.setPageSize(2);
		config.setInitialPageSize(25);
		config.setAesEncryptionKey("708C150A5363290AAE3F579BF3746AD5");

		stashClient = new AzureRepoServerClient(config, azurerepoRestOperations, aesEncryptionService);
	}

	@Test
	void testFetchCommits() throws Exception {
		String serverResponse = getServerResponse("/com/stashresponse.json");

		String filePath = "src/test/resources/com/processoritem.json";
		File file = new File(filePath);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<AzureRepoModel> repo = Arrays.asList(objectMapper.readValue(file, AzureRepoModel[].class));
		ProcessorToolConnection azureRepoProcessorInfo = new ProcessorToolConnection();
		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		azureRepoProcessorInfo.setApiVersion("5.1");
		azureRepoProcessorInfo.setBranch("master");
		azureRepoProcessorInfo.setUrl("https://test.com/test/testProject");
		azureRepoProcessorInfo.setPat("testPat");
		azureRepoProcessorInfo.setRepoSlug("testProject");
		repo.get(0).setRepoUrl("https://test.com/test/testProject");
		repo.get(0).setPat("testPat");
		String restUri = new AzureRepoServerURIBuilder(repo.get(0), config, azureRepoProcessorInfo).build();
		when(stashClient.decryptPat(repo.get(0).getPat())).thenReturn("test");

		when(restTemplate.exchange(eq(restUri), eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				eq(String.class))).thenReturn(new ResponseEntity<String>(serverResponse, HttpStatus.OK));
		List<CommitDetails> commits = stashClient.fetchAllCommits(repo.get(0), true, azureRepoProcessorInfo,
				projectBasicConfig);
		Assert.assertEquals(2, commits.size());

		CommitDetails azureRepoCommit = commits.get(0);
		Assert.assertEquals("Merged PR 2: Updated test file master", azureRepoCommit.getCommitLog());

	}

	private String getServerResponse(String resource) throws Exception {
		return IOUtils.toString(this.getClass().getResourceAsStream(resource));
	}

}
