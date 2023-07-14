package com.publicissapient.kpidashboard.github.processor;

import static org.mockito.Mockito.doReturn;

import java.util.List;

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

import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.github.config.GitHubConfig;
import com.publicissapient.kpidashboard.github.model.GitHubProcessorItem;
import com.publicissapient.kpidashboard.github.processor.service.impl.GitHubClientImpl;

/**
 * @author narsingh9
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHubClientImplTest {
	@InjectMocks
	GitHubClientImpl gitHubClient;
	@Mock
	private GitHubConfig gitLabConfig;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private AesEncryptionService aesEncryptionService;

	@Test
	public void testFetchCommits() throws Exception {
		String restURI = "https://test.com/repos/username/repositiryname/commits?per_page=100&sha=develop";
		String serverResponse = getServerResponse("/github-server/commitResponse.json");
		doReturn("abcd").when(gitLabConfig).getAesEncryptionKey();
		doReturn("test").when(aesEncryptionService).decrypt(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
		doReturn(new ResponseEntity<>(serverResponse, HttpStatus.OK)).when(restTemplate).exchange(
				ArgumentMatchers.eq(restURI), ArgumentMatchers.eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(String.class));
		List<CommitDetails> commits = gitHubClient.fetchAllCommits(new GitHubProcessorItem(), true, getToolConnection(),
				new ProjectBasicConfig());
		Assert.assertEquals(11, commits.size());
	}

	@Test
	public void testMergeRequests() throws Exception {
		String restURI = "https://test.com/repos/username/repositiryname/pulls?per_page=100&state=all&base=develop";
		String serverResponse = getServerResponse("/github-server/mergeRequestResponse.json");
		doReturn("abcd").when(gitLabConfig).getAesEncryptionKey();
		doReturn("test").when(aesEncryptionService).decrypt(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
		doReturn(new ResponseEntity<>(serverResponse, HttpStatus.OK)).when(restTemplate).exchange(
				ArgumentMatchers.eq(restURI), ArgumentMatchers.eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(String.class));
		List<MergeRequests> mergeRequests = gitHubClient.fetchMergeRequests(new GitHubProcessorItem(), true,
				getToolConnection(), new ProjectBasicConfig());
		Assert.assertEquals(1, mergeRequests.size());
	}

	private ProcessorToolConnection getToolConnection() {
		ProcessorToolConnection githubToolConnection = new ProcessorToolConnection();
		githubToolConnection.setUrl("https://test.com");
		githubToolConnection.setAccessToken("abcd");
		githubToolConnection.setUsername("username");
		githubToolConnection.setRepositoryName("repositiryname");
		githubToolConnection.setBranch("develop");
		return githubToolConnection;
	}

	private String getServerResponse(String resource) throws Exception {
		return IOUtils.toString(this.getClass().getResourceAsStream(resource));
	}
}
