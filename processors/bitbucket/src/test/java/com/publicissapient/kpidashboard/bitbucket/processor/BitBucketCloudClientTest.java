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

import static org.mockito.Mockito.when;

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

import com.publicissapient.kpidashboard.bitbucket.config.BitBucketConfig;
import com.publicissapient.kpidashboard.bitbucket.model.BitbucketRepo;
import com.publicissapient.kpidashboard.bitbucket.processor.service.impl.BitBucketCloudClient;
import com.publicissapient.kpidashboard.bitbucket.processor.service.impl.BitBucketServerURIBuilder;
import com.publicissapient.kpidashboard.bitbucket.util.BitbucketRestOperations;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

@ExtendWith(SpringExtension.class)
class BitBucketCloudClientTest {

	@Mock
	private BitBucketConfig config;

	@Mock
	private RestOperations restTemplate;

	@Mock
	private BitbucketRestOperations bitbucketRestOperations;

	private BitBucketCloudClient bucketCloudClient;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@BeforeEach
	public void init() throws Exception {

		when(bitbucketRestOperations.getTypeInstance()).thenReturn(restTemplate);

		config = new BitBucketConfig();
		config.setApi("/rest/api/1.0/");
		config.setHost("localhost");
		config.setPageSize(2);
		config.setAesEncryptionKey("708C150A5363290AAE3F579BF3746AD5");

		bucketCloudClient = new BitBucketCloudClient(config, bitbucketRestOperations, aesEncryptionService);
	}

	@Test
	void testFetchCommits() throws Exception {
		String serverResponse = getServerResponse("/bitbucket-server/stashresponse.json");
		ResponseEntity<String> responseEntity = new ResponseEntity<String>(serverResponse, HttpStatus.ACCEPTED);
		BitbucketRepo repo = new BitbucketRepo();
		ProjectBasicConfig proBasicConfig = new ProjectBasicConfig();
		proBasicConfig.setSaveAssigneeDetails(true);
		repo.setRepoUrl("http://localhost:9999/scm/testproject/comp-proj.git");
		repo.setBranch("release/core-r4.4");
		repo.getToolDetailsMap().put("bitbucketApi", "/rest/api/1.0/");
		repo.setUserId("userID");
		repo.setPassword("testPasswordString");
		ProcessorToolConnection connectionDetail = new ProcessorToolConnection();
		connectionDetail.setBranch("release/core-r4.4");
		connectionDetail.setPassword("testPasswordString");
		connectionDetail.setUrl("http://localhost:9999/scm/testproject/comp-proj.git");
		connectionDetail.setApiEndPoint("/rest/api/1.0/");
		connectionDetail.setUsername("User");
		String restUri = new BitBucketServerURIBuilder(repo, config, connectionDetail).build();
		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<String>>any()))
						.thenReturn(responseEntity);
		List<CommitDetails> commits = bucketCloudClient.fetchAllCommits(repo, true, connectionDetail, proBasicConfig);
		Assert.assertEquals(2, commits.size());
	}

	private String getServerResponse(String resource) throws Exception {
		return IOUtils.toString(this.getClass().getResourceAsStream(resource));
	}
}
