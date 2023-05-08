package com.publicissapient.kpidashboard.githubaction.processor;

import static org.mockito.Mockito.doReturn;

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

import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.githubaction.config.GitHubActionConfig;
import com.publicissapient.kpidashboard.githubaction.processor.adapter.impl.GitHubActionBuildClient;

@RunWith(MockitoJUnitRunner.class)
public class GitHubActionBuildClientTest {

	@InjectMocks
	GitHubActionBuildClient gitHubActionBuildClient;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private AesEncryptionService aesEncryptionService;
	@Mock
	private GitHubActionConfig gitHubActionConfig;

	@Test
	public void getBuildJobsFromServerTest() throws Exception {
		String restURI = "https://test.com/repos/username/reponame/actions/workflows/8846930/runs";
		String serverResponse = getServerResponse("/githubaction_build.json");
		doReturn("abcd").when(gitHubActionConfig).getAesEncryptionKey();
		doReturn("test").when(aesEncryptionService).decrypt(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
		doReturn(new ResponseEntity<>(serverResponse, HttpStatus.OK)).when(restTemplate).exchange(
				ArgumentMatchers.eq(restURI), ArgumentMatchers.eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(String.class));
		Set<Build> buildsByJob = gitHubActionBuildClient.getBuildJobsFromServer(getToolConnection(),
				new ProjectBasicConfig());

		Assert.assertEquals(2, buildsByJob.size());
	}

	private ProcessorToolConnection getToolConnection() {
		ProcessorToolConnection githubToolConnection = new ProcessorToolConnection();
		githubToolConnection.setUrl("https://test.com");
		githubToolConnection.setAccessToken("abcd");
		githubToolConnection.setUsername("username");
		githubToolConnection.setRepositoryName("reponame");
		githubToolConnection.setWorkflowID("8846930");
		githubToolConnection.setJobType("build");
		return githubToolConnection;
	}

	private String getServerResponse(String resource) throws Exception {
		return IOUtils.toString(this.getClass().getResourceAsStream(resource));
	}
}
