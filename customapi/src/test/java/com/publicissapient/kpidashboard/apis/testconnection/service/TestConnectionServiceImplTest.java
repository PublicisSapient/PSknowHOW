package com.publicissapient.kpidashboard.apis.testconnection.service;

import static com.mongodb.client.model.Filters.eq;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolsProvider;
import com.publicissapient.kpidashboard.apis.repotools.repository.RepoToolsProviderRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
	 RepoToolsProviderRepository repoToolsProviderRepository;

	@Mock
	RestTemplate restTemplate;

	Connection conn = new Connection();

	KerberosClient client;

	@Before
	public void setup() throws URISyntaxException {
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
	public void validateConnectionJira() throws URISyntaxException {
		when(customApiConfig.getJiraTestConnection()).thenReturn("rest/api/2/issue/createmeta");
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Basic dXNlcjprZXk=");

		when(restTemplate.exchange(new URI("https://abc.com/rest/api/2/issue/createmeta"),
				HttpMethod.GET, new HttpEntity<>(headers), String.class)).thenReturn(new ResponseEntity<>(HttpStatus.OK));


		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_JIRA);
		assertThat("status: ", response.getSuccess(), equalTo(true));
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
	public void validateConnectionZephyr() throws URISyntaxException {
		conn.setBaseUrl("https://abc.com/jira/");
		when(customApiConfig.getZephyrTestConnection()).thenReturn("rest/api/2/issue/createmeta");
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Basic dXNlcjprZXk=");

		when(restTemplate.exchange(new URI("https://abc.com/jira/rest/api/2/issue/createmeta"),
				HttpMethod.GET, new HttpEntity<>(headers), String.class)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_ZEPHYR);
		assertThat("status: ", response.getSuccess(), equalTo(true));
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
	public void validateConnectionSonar_Cloud() throws URISyntaxException {
		when(customApiConfig.getSonarTestConnection()).thenReturn("api/authentication/validate");
		conn.setCloudEnv(false);
		conn.setBaseUrl("https://abc.com");
		conn.setAccessToken("testAccessToken");
		conn.setAccessTokenEnabled(true);
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic dGVzdEFjY2Vzc1Rva2VuOg==");
		Mockito.when(restTemplate.exchange(new URI("https://abc.com/api/authentication/validate"),
				HttpMethod.GET, new HttpEntity<>(headers), String.class)).thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_SONAR);
		assertThat("status: ", response.getSuccess(), equalTo(true));
	}

	@Test
	public void validateConnectionSonar() throws URISyntaxException {
		when(customApiConfig.getSonarTestConnection()).thenReturn("api/authentication/validate");
		conn.setCloudEnv(false);
		conn.setBaseUrl("https://abc.com");
		conn.setAccessToken("testAccessToken");
		conn.setUsername("testUserName");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic dGVzdFVzZXJOYW1lOnRlc3RBY2Nlc3NUb2tlbg==");
		Mockito.when(restTemplate.exchange(new URI("https://abc.com/api/authentication/validate"),
				HttpMethod.GET, new HttpEntity<>(headers), String.class)).thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_SONAR);
		assertThat("status: ", response.getSuccess(), equalTo(true));
	}

	@Test
	public void validateConnectionSonar_Kaath() throws URISyntaxException, IOException {
		when(customApiConfig.getSonarTestConnection()).thenReturn("api/authentication/validate");
		conn.setCloudEnv(false);
		conn.setBaseUrl("https://abc.com");
		conn.setAccessToken("testAccessToken");
		conn.setUsername("testUserName");
		conn.setJaasKrbAuth(true);
		conn.setKrb5ConfigFilePath("filepath");
		conn.setJaasConfigFilePath("filepath");
		conn.setJaasUser("jaasuser");
		conn.setSamlEndPoint("/api/2/");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic dGVzdEFjY2Vzc1Rva2VuOg==");

		when(customApiConfig.getSamlTokenStartString()).thenReturn("samlstart");
		when(customApiConfig.getSamlTokenEndString()).thenReturn("samlend");
		when(customApiConfig.getSamlUrlStartString()).thenReturn("urlStart");
		when(customApiConfig.getSamlUrlEndString()).thenReturn("urlEnd");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_SONAR);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionException() throws URISyntaxException {
		when(customApiConfig.getJiraTestConnection()).thenReturn("rest/api/2/issue/createmeta");
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Basic dXNlcjprZXk=");

		when(restTemplate.exchange(new URI("https://abc.com/rest/api/2/issue/createmeta"),
				HttpMethod.GET, new HttpEntity<>(headers), String.class)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

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
	public void validateConnectionSonarCloud() throws URISyntaxException {
		conn.setCloudEnv(true);
		conn.setBaseUrl("https://abc.com");
		conn.setAccessToken("testAccessToken");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + "testAccessToken");
		ResponseEntity<String> response = new ResponseEntity<>("Success", HttpStatus.OK);
		Mockito.when(restTemplate.exchange(new URI("https://abc.com/api/favorites/search"),
				HttpMethod.GET, new HttpEntity<>(headers), String.class)).thenReturn(response);
		testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_SONAR);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void validateConnectionBitbucketValidURL() throws URISyntaxException {
		conn.setBaseUrl("https://abc.com/bitbucket");
		conn.setApiEndPoint("/bitbucket/rest/api/1.0");
		conn.setUsername("test_auto_user10");
		conn.setPassword("testPassword");
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Basic dGVzdF9hdXRvX3VzZXIxMDp0ZXN0UGFzc3dvcmQ=");
		when(restTemplate.exchange(new URI("https://abc.com/bitbucket/rest/api/1.0/projects/"),
				HttpMethod.GET, new HttpEntity<>(headers), String.class)).thenReturn(new ResponseEntity<>(HttpStatus.OK));


		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_BITBUCKET);
		assertThat("status: ", response.getSuccess(), equalTo(true));
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
	public void validateConnectionBitbucketInvalidUrl() throws URISyntaxException {
		conn.setBaseUrl("https://abc.com/bitbucket");
		conn.setApiEndPoint("/bitbucket/rest/api/1.0/");
		conn.setUsername("test");
		conn.setPassword("testPassword");
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Basic dGVzdDp0ZXN0UGFzc3dvcmQ=");
		when(restTemplate.exchange(new URI("https://abc.com/bitbucket/rest/api/1.0/projects/"),
				HttpMethod.GET, new HttpEntity<>(headers), String.class)).thenReturn(new ResponseEntity<>(HttpStatus.OK));
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_BITBUCKET);
		assertThat("status: ", response.getSuccess(), equalTo(true));
	}

	@Test
	public void validateConnectionAzureInvalidCredentials() throws URISyntaxException {
		conn.setBaseUrl("https://abc.com/testUser/testProject");
		when(customApiConfig.getAzureBoardApi()).thenReturn("_apis/wit/fields");

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Basic dXNlcjprZXk=");

		when(restTemplate.exchange(new URI("https://abc.com/testUser/testProject/_apis/wit/fields"),
				HttpMethod.GET, new HttpEntity<>(headers), String.class)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_AZURE);
		assertThat("status: ", response.getSuccess(), equalTo(true));
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

	@Test
	public void validateRepoTestConnSuccess() {
		conn.setHttpUrl("https://abc.com/gitlab");
		conn.setAccessToken("testAccessToken");
		conn.setRepoToolProvider(Constant.TOOL_GITHUB);
		conn.setUsername("testUserName");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
		RepoToolsProvider provider= new RepoToolsProvider();
		provider.setTestApiUrl("https://www.test.com");
		when(repoToolsProviderRepository.findByToolName(anyString())).thenReturn(provider);

		ServiceResponse serviceResponse = testConnectionServiceImpl.validateConnection(conn, Constant.REPO_TOOLS);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

	}

	@Test
	public void validateRepoTestConnSuccess_BitBucket_Cloud() throws URISyntaxException {
		conn.setHttpUrl("https://abc.com/bitbucket.org");
		conn.setAccessToken("testAccessToken");
		conn.setRepoToolProvider(Constant.TOOL_BITBUCKET);
		conn.setUsername("testUserName");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);
		RepoToolsProvider provider= new RepoToolsProvider();
		provider.setTestApiUrl("https://www.test.com");
		when(repoToolsProviderRepository.findByToolName(anyString())).thenReturn(provider);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Basic dGVzdFVzZXJOYW1lOnRlc3RBY2Nlc3NUb2tlbg==");

		when(restTemplate.exchange(new URI("https://www.test.com"),
				HttpMethod.GET, new HttpEntity<>(headers), String.class)).thenReturn(new ResponseEntity<>(HttpStatus.OK));


		testConnectionServiceImpl.validateConnection(conn, Constant.REPO_TOOLS);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

	}

	@Test
	public void validateRepoTestConnSuccess_BitBucket() {
		conn.setHttpUrl("https://abc.com");
		conn.setAccessToken("testAccessToken");
		conn.setRepoToolProvider(Constant.TOOL_SONAR);
		conn.setUsername("testUserName");
		when(customApiConfig.getGitlabTestConnection()).thenReturn("api/v4/projects");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
		RepoToolsProvider provider= new RepoToolsProvider();
		provider.setTestApiUrl("https://www.test.com");
		when(repoToolsProviderRepository.findByToolName(anyString())).thenReturn(provider);
		ServiceResponse serviceResponse = testConnectionServiceImpl.validateConnection(conn, Constant.REPO_TOOLS);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

	}

	@Test
	public void validateRepoTestConnSuccess_BitBucket_Pat() throws URISyntaxException {
		conn.setHttpUrl("https://abc.com/bitbucket.org");
		conn.setAccessToken("testAccessToken");
		conn.setRepoToolProvider(Constant.TOOL_BITBUCKET);
		conn.setUsername("testUserName");
		conn.setBearerToken(true);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + "testAccessToken");
		headers.add(HttpHeaders.ACCEPT, "*/*");
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
		headers.set("Cookie", "");

		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);
		when(restTemplate.exchange(new URI("https://www.test.com"),
				HttpMethod.GET, new HttpEntity<>(headers), String.class)).thenReturn(responseEntity);
		RepoToolsProvider provider= new RepoToolsProvider();
		provider.setTestApiUrl("https://www.test.com");
		when(repoToolsProviderRepository.findByToolName(anyString())).thenReturn(provider);
		testConnectionServiceImpl.validateConnection(conn, Constant.REPO_TOOLS);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

	}

}
