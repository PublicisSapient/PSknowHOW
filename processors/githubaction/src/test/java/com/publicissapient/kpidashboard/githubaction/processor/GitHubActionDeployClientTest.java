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

package com.publicissapient.kpidashboard.githubaction.processor;

import static org.mockito.Mockito.doReturn;

import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.githubaction.config.GitHubActionConfig;
import com.publicissapient.kpidashboard.githubaction.processor.adapter.impl.GitHubActionDeployClient;

@RunWith(MockitoJUnitRunner.class)
public class GitHubActionDeployClientTest {

	@InjectMocks
	GitHubActionDeployClient gitHubActionDeployClient;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private AesEncryptionService aesEncryptionService;
	@Mock
	private GitHubActionConfig gitHubActionConfig;

	@Test
	public void getBuildJobsFromServerTest() throws Exception {
		String restURI = "https://test.com/repos/username/reponame/deployments";
		String serverResponse = getServerResponse("/githubaction_deploy.json");
		doReturn("abcd").when(gitHubActionConfig).getAesEncryptionKey();
		doReturn("test").when(aesEncryptionService).decrypt(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
		doReturn(new ResponseEntity<>(serverResponse, HttpStatus.OK)).when(restTemplate).exchange(
				ArgumentMatchers.eq(restURI), ArgumentMatchers.eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(String.class));
		Map<Deployment, Set<Deployment>> deploymentsByJob = gitHubActionDeployClient
				.getDeployJobsFromServer(getToolConnection(), new ProjectBasicConfig());

		Assert.assertEquals(2, deploymentsByJob.size());
	}

	private ProcessorToolConnection getToolConnection() {
		ProcessorToolConnection githubToolConnection = new ProcessorToolConnection();
		githubToolConnection.setUrl("https://test.com");
		githubToolConnection.setAccessToken("abcd");
		githubToolConnection.setUsername("username");
		githubToolConnection.setRepositoryName("reponame");
		githubToolConnection.setJobType("build");
		return githubToolConnection;
	}

	private String getServerResponse(String resource) throws Exception {
		return IOUtils.toString(this.getClass().getResourceAsStream(resource));
	}
}
