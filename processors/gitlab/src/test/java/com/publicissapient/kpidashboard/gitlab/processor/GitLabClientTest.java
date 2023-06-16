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

package com.publicissapient.kpidashboard.gitlab.processor;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.net.URLDecoder;
import java.util.List;

import org.apache.commons.io.IOUtils;
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

import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.gitlab.config.GitLabConfig;
import com.publicissapient.kpidashboard.gitlab.model.GitLabRepo;
import com.publicissapient.kpidashboard.gitlab.processor.service.impl.GitLabClient;
import com.publicissapient.kpidashboard.gitlab.processor.service.impl.GitLabURIBuilder;
import com.publicissapient.kpidashboard.gitlab.util.GitLabRestOperations;

@RunWith(MockitoJUnitRunner.class)
public class GitLabClientTest {

	ProcessorToolConnection gitLabInfo = new ProcessorToolConnection();
	ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
	@Mock
	private GitLabConfig gitLabConfig;
	@Mock
	private GitLabRestOperations gitLabRestOperations;
	@Mock
	private RestOperations restTemplate;
	@InjectMocks
	private GitLabClient gitLabClient;
	@Mock
	private GitLabRepo repo;
	@Mock
	private AesEncryptionService aesEncryptionService;

	@BeforeEach
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(gitLabRestOperations.getTypeInstance()).thenReturn(restTemplate);

		gitLabInfo.setBranch("release/core-r4.4");
		gitLabInfo.setPassword("testPassword");
		gitLabInfo.setUrl("http://localhost:9999/scm/testproject/test.git");
		gitLabInfo.setApiEndPoint("/rest/api/1.0/");
		gitLabInfo.setUsername("User");

		gitLabConfig = new GitLabConfig();
		gitLabConfig.setApi("/rest/api/1.0/");
		gitLabConfig.setAesEncryptionKey("708C150A5363290AAE3F579BF3746AD5");
		when(repo.getGitLabProjectId()).thenReturn("577");
	}

	@Test
	public void testFetchCommits() throws Exception {
		String serverResponse = getServerResponse("/gitlab-server/stashresponse.json");
		String restUrl = new GitLabURIBuilder(repo, gitLabConfig, gitLabInfo).build();
		restUrl = URLDecoder.decode(restUrl, "UTF-8");
		projectBasicConfig.setSaveAssigneeDetails(true);
		when(restTemplate.exchange(eq(restUrl), eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				eq(String.class))).thenReturn(new ResponseEntity<String>(serverResponse, HttpStatus.OK));
		List<CommitDetails> commits = gitLabClient.fetchAllCommits(repo, gitLabInfo, projectBasicConfig);
		Assert.assertEquals(2, commits.size());
		CommitDetails gitLabCommit = commits.get(0);
		Assert.assertEquals("userab", gitLabCommit.getAuthor());
	}

	private String getServerResponse(String resource) throws Exception {
		return IOUtils.toString(this.getClass().getResourceAsStream(resource));
	}

}
