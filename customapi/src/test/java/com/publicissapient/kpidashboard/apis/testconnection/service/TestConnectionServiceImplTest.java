package com.publicissapient.kpidashboard.apis.testconnection.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Before;
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

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.connection.service.TestConnectionServiceImpl;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.connection.Connection;

/**
 * @author sansharm13
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestConnectionServiceImplTest {
	@InjectMocks
	TestConnectionServiceImpl testConnectionServiceImpl;

	@Mock
	CustomApiConfig customApiConfig;

	@Mock
	RestTemplate restTemplate;

	Connection conn = new Connection();

	KerberosClient client;

	@Before
	public void setup() {
		conn.setUsername("user");
		conn.setConnectionName("connection name");
		conn.setBaseUrl("https://abc.com/");
		conn.setApiKeyFieldName("filed");
		conn.setAccessToken("testAccessToken");
		conn.setType("jira");
		conn.setApiKey("key");
		conn.setApiEndPoint("api/2");
	}

	@Test
	public void validateConnectionJira() {
		when(customApiConfig.getJiraTestConnection()).thenReturn("rest/api/2/issue/createmeta");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_JIRA);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionJiraSaml() {
		conn.setJaasKrbAuth(true);
		client = new KerberosClient(conn.getJaasConfigFilePath(), conn.getKrb5ConfigFilePath(), conn.getJaasUser(),
				conn.getSamlEndPoint(), conn.getBaseUrl());
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_JIRA);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionBamboo() {
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_BAMBOO);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionZephyr() {
		conn.setBaseUrl("https://abc.com/jira/");
		when(customApiConfig.getZephyrTestConnection()).thenReturn("rest/api/2/issue/createmeta");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_ZEPHYR);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionTeamCity() {
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_TEAMCITY);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionBitbucket() {
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_BITBUCKET);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionJenkins() {
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_JENKINS);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionSonar() {
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_SONAR);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionException() {
		when(customApiConfig.getJiraTestConnection()).thenReturn("rest/api/2/issue/createmeta");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_JIRA);
	}

	@Test
	public void validateConnectionApiKeyMissing() {
		when(customApiConfig.getJiraTestConnection()).thenReturn("rest/api/2/issue/createmeta");
		conn.setApiKey("");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_JIRA);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionZephyrCloud() {
		conn.setCloudEnv(true);
		conn.setBaseUrl("https://abc.com/v2/");
		conn.setAccessToken("testAccessToken");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_ZEPHYR);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	public void validateConnectionSonarCloud() {
		conn.setCloudEnv(true);
		conn.setBaseUrl("https:/abc.com");
		conn.setAccessToken("testAccessToken");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_SONAR);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	public void validateConnectionBitbucketValidURL() {
		conn.setBaseUrl("https://abc.com/bitbucket");
		conn.setApiEndPoint("/bitbucket/rest/api/1.0");
		conn.setUsername("test_auto_user10");
		conn.setPassword("testPassword");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_BITBUCKET);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionBitbucketEmptyToolName() {
		conn.setBaseUrl("https://abc.com/bitbucket");
		conn.setApiEndPoint("/bitbucket/rest/api/1.0");
		conn.setUsername("test_auto_user10");
		conn.setPassword("testPassword");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, "");
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionBitbucketInvalidUrl() {
		conn.setBaseUrl("https://abc.com/bitbucket");
		conn.setApiEndPoint("/bitbucket/rest/api/1.0/");
		conn.setUsername("test");
		conn.setPassword("testPassword");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_BITBUCKET);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	// @Test
	public void validateConnectionBitbucketHttpClientErrorException() {
		conn.setBaseUrl("https://test.com/bitbucket");
		conn.setApiEndPoint("/bitbucket/rest/api/1.0/");
		conn.setUsername("test");
		conn.setPassword("test");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_BITBUCKET);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionAzureInvalidCredentials() {
		conn.setBaseUrl("https://abc.com/testUser/testProject");
		when(customApiConfig.getAzureBoardApi()).thenReturn("_apis/wit/fields");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_AZURE);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionAzureRepoInvalidCredentials() {
		conn.setBaseUrl("https://abc/testUser/testProject");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_AZUREREPO);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionAzurePipelineInvalidCredentials() {
		conn.setBaseUrl("https://abc/testUser/testProject");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_AZUREPIPELINE);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateGitHubConnection_Success() {
		conn.setBaseUrl("https://api.github.com");
		conn.setAccessToken("testAccessToken");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
		ServiceResponse serviceResponse = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_GITHUB);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

	}

	@Test
	public void validateGitHubConnection_Failure() {
		conn.setBaseUrl("https://api.github.com");
		conn.setAccessToken("testAccessToken");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
		ServiceResponse serviceResponse = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_GITHUB);
		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

	}

	@Test
	public void validateGitLabTestConnSuccess() {
		conn.setBaseUrl("https://abc.com/gitlab");
		conn.setAccessToken("testAccessToken");
		when(customApiConfig.getGitlabTestConnection()).thenReturn("api/v4/projects");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
		ServiceResponse serviceResponse = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_GITLAB);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

	}

	@Test
	public void validateGitLabTestConnFailure() {
		conn.setBaseUrl("https://abc.com/gitlab");
		conn.setAccessToken("testAccessToken");
		when(customApiConfig.getGitlabTestConnection()).thenReturn("api/v4/projects");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
		ServiceResponse serviceResponse = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_GITLAB);
		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

	}

}
