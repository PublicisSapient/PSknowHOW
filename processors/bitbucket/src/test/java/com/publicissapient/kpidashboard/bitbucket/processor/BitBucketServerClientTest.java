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

package com.publicissapient.kpidashboard.bitbucket.processor;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.net.URLDecoder;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.bitbucket.config.BitBucketConfig;
import com.publicissapient.kpidashboard.bitbucket.model.BitbucketRepo;
import com.publicissapient.kpidashboard.bitbucket.processor.service.impl.BitBucketServerClient;
import com.publicissapient.kpidashboard.bitbucket.processor.service.impl.BitBucketServerURIBuilder;
import com.publicissapient.kpidashboard.bitbucket.util.BitbucketRestOperations;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

@RunWith(MockitoJUnitRunner.class)
public class BitBucketServerClientTest {

	@Mock
	private BitBucketConfig config;

	@Mock
	private RestOperations restTemplate;

	@Mock
	private BitbucketRestOperations bitbucketRestOperations;

	@InjectMocks
	private BitBucketServerClient stashClient;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@BeforeEach
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(bitbucketRestOperations.getTypeInstance()).thenReturn(restTemplate);

		config = new BitBucketConfig();
		config.setApi("/rest/api/1.0/");
		config.setPageSize(2);

		stashClient = new BitBucketServerClient(config, bitbucketRestOperations, aesEncryptionService);
	}

	@Test
	public void testFetchCommits() throws Exception {
		String serverResponse = getServerResponse("/bitbucket-server/stashresponse.json");

		BitbucketRepo repo = new BitbucketRepo();
		repo.setRepoUrl("http://localhost:9999/scm/testproject/test.git");
		repo.setBranch("release/core-r4.4");
		repo.getToolDetailsMap().put("bitbucketApi", "/rest/api/1.0/");
		ProcessorToolConnection connectionDetail = new ProcessorToolConnection();
		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setSaveAssigneeDetails(true);
		projectBasicConfig.setId(new ObjectId("5e2ac020e4b098db0edf5145"));
		connectionDetail.setBranch("release/core-r4.4");
		connectionDetail.setPassword("testPassword");
		connectionDetail.setUrl("http://localhost:9999/scm/testproject/test.git");
		connectionDetail.setApiEndPoint("/rest/api/1.0/");
		connectionDetail.setUsername("User");
		String restUri = new BitBucketServerURIBuilder(repo, config, connectionDetail).build();
		when(stashClient.decryptPassword(connectionDetail.getPassword())).thenReturn("test");

		when(restTemplate.exchange(eq(URLDecoder.decode(restUri, "UTF-8")), eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<String>(serverResponse, HttpStatus.OK));
		List<CommitDetails> commits = stashClient.fetchAllCommits(repo, true, connectionDetail, projectBasicConfig);
		Assert.assertEquals(2, commits.size());

		CommitDetails bitBucketCommit = commits.get(0);
		Assert.assertEquals("Test User One", bitBucketCommit.getAuthor());
		Assert.assertEquals("Commit message one", bitBucketCommit.getCommitLog());
	}

	private String getServerResponse(String resource) throws Exception {
		return IOUtils.toString(this.getClass().getResourceAsStream(resource));
	}

}
