package com.publicissapient.kpidashboard.apis.testconnection.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.net.URI;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import org.apache.commons.validator.routines.UrlValidator;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.connection.service.TestConnectionServiceImpl;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.service.RsaEncryptionService;

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
	RsaEncryptionService rsaEncryptionService;

	@Mock
	UrlValidator urlValidator;

	@Mock
	RestTemplate restTemplate;

	Connection conn = new Connection();

	KerberosClient client;

	@Before
	public void setup() {
		when(customApiConfig.getRsaPrivateKey()).thenReturn("rsaKey");
		conn.setUsername("user");
		conn.setConnectionName("connection name");
		conn.setBaseUrl("https://tools.publicis.sapient.com/sonar/");
		conn.setApiKeyFieldName("filed");
		conn.setAccessToken("Azureib5eiyh2py3klgih5ancwudhdsxajkttr4723qab7gucdycmcv3a");
		conn.setType("jira");
		conn.setApiKey("key");
		conn.setApiEndPoint("api/2");
	}

	@Test
	public void validateConnectionJira() {
		when(customApiConfig.getJiraTestConnection()).thenReturn("rest/api/2/issue/createmeta");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_JIRA);
		assertThat("status: ", response.getSuccess(), equalTo(true));
	}

	@Test
	public void validateConnectionJiraSaml() {
		conn.setJaasKrbAuth(true);
		client = new KerberosClient(conn.getJaasConfigFilePath(), conn.getKrb5ConfigFilePath(), conn.getJaasUser(),
				conn.getSamlEndPoint(), conn.getBaseUrl());
//		when(client.login(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
//				ArgumentMatchers.anyString())).thenReturn("true");
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
		conn.setBaseUrl("https://tools.publicis.sapient.com/jira/");
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
		conn.setBaseUrl("https://api.zephyrscale.smartbear.com/v2/");
		conn.setAccessToken("yJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI3MjdiNzhkZS04YTQ3LTM2ODgtODk3NC1kMGQ1NGFjNDY2N2QiLCJjb250ZXh0Ijp7ImJhc2VVcmwiOiJodHRwczpcL1wvdGV0cmFwYWstc21hcnRzYWxlcy5hdGxhc3NpYW4ubmV0IiwidXNlciI6eyJhY2NvdW50SWQiOiI2MGQxNzhmN2IyMTU2MTAwNjlmZGYxOTEifX0sImlzcyI6ImNvbS5rYW5vYWgudGVzdC1tYW5hZ2VyIiwiZXhwIjoxNjgxMzg0MzYwLCJpYXQiOjE2NDk4NDgzNjB9.H_U72R6zx4f2gFO0n0GAaivQhg7lhEnrwkXTN5G4CQI");
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
		conn.setBaseUrl("https://sonarcloud.io");
		conn.setAccessToken("ohCUCDbHEzigg68bw4lgEZuLueyPrOjcAOCyrWWTM5Lmso0f5y+8tDrIb294UdNlDFgVXvoWjbdIu9Mmzrjql8wYqK+1g2Mjwt2EtJ3zSMe0aM8HMIuwJlkHU/KztoKsHadkl3G2AJTkdMjIEhfsV4JkFLZHaoqe/QScUPKZgtRY8lj3lXD2bogVpOhSQWyZxy91oeUw3aGeFR/hG3VME2s+raqjJopOgiUZw/WUQrAJzdgmJRkzMdgLksThLIJ38/TWmI7I+RywRO9SL4P/Drdv2c4TzWGLawrsRniLoTZaUsE0myLwcl5//fNuYtqz91Xr4xkyVygrSk2ke6qESw==");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_SONAR);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	public void validateConnectionBitbucketValidURL() {
		conn.setBaseUrl("https://tools.publicis.sapient.com/bitbucket/scm/know/knowhowrepo.git");
		conn.setApiEndPoint("/bitbucket/rest/api/1.0");
		conn.setUsername("test_auto_user10");
		conn.setPassword("cvPYAIyrxeH5KX1XyMfAJtSIgFBRQUS1ZF8X7FD1duYqs0XIIwE+j+Tq2ZeyTMbU");
		when(rsaEncryptionService.decrypt(anyString(), anyString())).thenReturn("Worklight@2021!");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_BITBUCKET);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionBitbucketEmptyToolName() {
		conn.setBaseUrl("https://tools.publicis.sapient.com/bitbucket/scm/know/knowhowrepo.git");
		conn.setApiEndPoint("/bitbucket/rest/api/1.0");
		conn.setUsername("test_auto_user10");
		conn.setPassword("cvPYAIyrxeH5KX1XyMfAJtSIgFBRQUS1ZF8X7FD1duYqs0XIIwE+j+Tq2ZeyTMbU");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, "");
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionBitbucketInvalidUrl() {
		conn.setBaseUrl("https://tools.publicis.sapient.com/scm/know/knowhowrepo.git");
		conn.setApiEndPoint("/bitbucket/rest/api/1.0/");
		conn.setUsername("tst-dojo-kshatt");
		conn.setPassword("jF1OFf1gVqdgI4DUrPIqkQ==");
		when(rsaEncryptionService.decrypt(anyString(), anyString())).thenReturn("Publicis@2021");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_BITBUCKET);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	// @Test
	public void validateConnectionBitbucketHttpClientErrorException() {
		conn.setBaseUrl("https://tools.publicis.sapient.com/bitbucket/scm/know/knowhowrepo.git");
		conn.setApiEndPoint("/bitbucket/rest/api/1.0/");
		conn.setUsername("tst-dojo-kshatt");
		conn.setPassword("jF1OFf1gVqdgI4DUrPIqkQ==");
		when(rsaEncryptionService.decrypt(anyString(), anyString())).thenReturn(null);
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_BITBUCKET);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionAzureInvalidCredentials() {
		conn.setBaseUrl("https://dev.azure.com/sundeepm/AzureSpeedy");
		when(customApiConfig.getAzureBoardApi()).thenReturn("_apis/wit/fields");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_AZURE);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionAzureRepoInvalidCredentials() {
		conn.setBaseUrl("https://dev.azure.com/ankbhard/KnowHOW");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_AZUREREPO);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateConnectionAzurePipelineInvalidCredentials() {
		conn.setBaseUrl("https://dev.azure.com/sundeepm/AzureSpeedy");
		ServiceResponse response = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_AZUREPIPELINE);
		assertThat("status: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void validateGitHubConnection_Success() {
		conn.setBaseUrl("https://api.github.com");
		conn.setAccessToken(
				"GjW7DaLEOM3kNP761iSF6JMd0VnzhEbn++9gTjeGkwEDobgInWwT1XsTuuMulMpMqxqb+QQ6wVA5JKCYyp2rKOlsC9x9cABtIN2SrniE1/JqFQeYw/wZRFaJ3wbuJ1v+3CpLI9GuoGbJHC11KxcRRJU2oLeEzDLWhktwUUgXG/cks/1k+r6Z3MyaTxXOIHxWOZ6q2nKAv/gf/cU+wRmSeYm7MSJiI4TDsllZ/wExLBhtysUdQWJD9iEQphlxN9fTHwfU5g6LVBTWnXQP0rqpZa+b+Ur06v1lgSR9B5nzQ0mQMhNCpR6+A1lEPbKW5EziO1tqGQnoPgqnXXWRVL3awg==");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
		ServiceResponse serviceResponse = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_GITHUB);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

	}

	@Test
	public void validateGitHubConnection_Failure() {
		conn.setBaseUrl("https://api.github.com");
		conn.setAccessToken(
				"GjW7DaLEOM3kNP761iSF6JMd0VnzhEbn++9gTjeGkwEDobgInWwT1XsTuuMulMpMqxqb+QQ6wVA5JKCYyp2rKOlsC9x9cABtIN2SrniE1/JqFQeYw/wZRFaJ3wbuJ1v+3CpLI9GuoGbJHC11KxcRRJU2oLeEzDLWhktwUUgXG/cks/1k+r6Z3MyaTxXOIHxWOZ6q2nKAv/gf/cU+wRmSeYm7MSJiI4TDsllZ/wExLBhtysUdQWJD9iEQphlxN9fTHwfU5g6LVBTWnXQP0rqpZa+b+Ur06v1lgSR9B5nzQ0mQMhNCpR6+A1lEPbKW5EziO1tqGQnoPgqnXXWRVL3awg==");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
		ServiceResponse serviceResponse = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_GITHUB);
		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

	}

	@Test
	public void validateGitLabTestConnSuccess() {
		conn.setBaseUrl("https://pscode.lioncloud.net");
		conn.setAccessToken("QcA4x+h0W+Rlr9YT6qJncy92eyN+1LPjuJOyTUDgMYNxEaSGFbbPGQwH0GJA7qaTfadmje5HjjGFeLrmW3zVuwNEoOSirzBfUj62FlqXeHwJn7on0KUSW9PJ0MerWjMkU2UJM22PFyaYhsQoF6vdF5EI3kZb0gUod2pUyRIi/YjLK6NxP5UU2PwNsK0UOCMtLA+f33AVemhLY/Oteyy4ZJzfzyAnUH20sWX0+ggBNW9r4TtlbFKCEBmfVRDUmo0YdDGlZfsPVElLF4aHyOOGZbSko18KoSMA7M1bFFlBBtFheLg44REFJHgQQfF0lX5unE1baK7FGolGNIFMT8h0pw==");
		when(customApiConfig.getGitlabTestConnection()).thenReturn("api/v4/projects");
		when(rsaEncryptionService.decrypt(anyString(), anyString())).thenReturn("2VdT3wHyzW-oztUqx2gz");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
		ServiceResponse serviceResponse = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_GITLAB);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

	}

	@Test
	public void validateGitLabTestConnFailure() {
		conn.setBaseUrl("https://pscode.lioncloud.net");
		conn.setAccessToken("QcA4x+h0W+Rlr9YT63tnwhtrzBfUj62FlqXeHwJn7on0KUSW9PJ0MerWjMkU2UJM22PFyaYhsQoF6vdF5EI3kZb0gUod2pUyRIi/YjLK6NxP5UU2PwNsK0UOCMtLA+f33AVemhLY/Oteyy4ZJzfzyAnUH20sWX0+ggBNW9r4TtlbFKCEBmfVRDUmo0YdDGlZfsPVElLF4aHyOOGZbSko18KoSMA7M1bFFlBBtFheLg44REFJHgQQfF0lX5unE1baK7FGolGNIFMT8h0pw==");
		when(customApiConfig.getGitlabTestConnection()).thenReturn("api/v4/projects");
		when(rsaEncryptionService.decrypt(anyString(), anyString())).thenReturn("fweqfbgvyi");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
				ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
		ServiceResponse serviceResponse = testConnectionServiceImpl.validateConnection(conn, Constant.TOOL_GITLAB);
		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

	}

}
