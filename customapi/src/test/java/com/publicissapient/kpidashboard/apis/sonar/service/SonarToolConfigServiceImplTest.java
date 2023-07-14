
package com.publicissapient.kpidashboard.apis.sonar.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

/**
 * @author hiren babariya
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class SonarToolConfigServiceImplTest {

	private static final String RESOURCE_PROJECT_ENDPOINT = "/api/components/search?qualifiers=TRK&p=1&ps=300";
	private static final String RESOURCE_CLOUD_PROJECT_ENDPOINT = "/api/components/search?qualifiers=TRK&organization=racv-ict&p=1&ps=300";
	private static final String RESOURCE_BRANCH_ENDPOINT = "/api/project_branches/list?project=%s";
	private static final String SONAR_URL = "https://abc.com/sonar";
	private static final String SONAR_CLOUD_URL = "https://abc.com";
	private static final String EXCEPTION = "rest client exception";
	private static final String USER_NAME = "test";
	private static final String PROJECT_KEY = "SURVEY_APP_API";
	private static final String ORG_KEY = "racv-ict";
	@InjectMocks
	private SonarToolConfigServiceImpl sonarToolConfigService;
	@Mock
	private ConnectionRepository connectionRepository;
	@Mock
	private AesEncryptionService aesEncryptionService;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private RestTemplate restTemplate;
	private String connectionId;
	private Optional<Connection> testConnectionOpt;
	private Connection connection;
	private String versionAbove;
	private List<String> responseProjectList = new ArrayList<>();
	private List<String> cloudResponseProjectList = new ArrayList<>();
	private List<String> responseBranchList = new ArrayList<>();

	@Before
	public void setup() {
		versionAbove = "7.9";
		connectionId = "5fc4d61f80b6350f048a93e5";
		connection = new Connection();
		connection.setPassword("password");
		connection.setAccessToken("token");
		testConnectionOpt = Optional.ofNullable(connection);

		connection.setId(new ObjectId("5fc4d61f80b6350f048a93e5"));
		connection.setBaseUrl(SONAR_URL);
		connection.setUsername(USER_NAME);
		List<String> projectKeys = Arrays.asList("ENGINEERING.KPIDASHBOARD.PROCESSORS", "ENGINEERING.KPIDASHBOARD.UI",
				"ENGINEERING.KPIDASHBOARD.CUSTOMAPI", "SURVEY_APP_UI", "SURVEY_APP_API");
		responseProjectList.addAll(projectKeys);
		List<String> cloudProjectKeys = Arrays.asList("racv.storybook", "com.aem.racv:racv");
		cloudResponseProjectList.addAll(cloudProjectKeys);
		List<String> branches = Arrays.asList("master", "develop");
		responseBranchList.addAll(branches);
	}

	@Test
	public void testGetSonarProjectListSuccess() throws Exception {
		String projectJson = getJSONDataResponse("sonarV8Dot9Projects.json");
		String projectsUrl = SONAR_URL + RESOURCE_PROJECT_ENDPOINT;
		Mockito.doReturn(new ResponseEntity<>(projectJson, HttpStatus.OK)).when(restTemplate).exchange(
				Mockito.eq(projectsUrl), Mockito.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				Mockito.eq(String.class));
		List<String> projectList = sonarToolConfigService.getSonarProjectKeyList(connection, "");
		Assert.assertEquals(projectList.size(), responseProjectList.size());
	}

	@Test
	public void testGetSonarProjectListSuccess_Cloud() throws Exception {
		String projectJson = getJSONDataResponse("sonarCloudResponse.json");
		String projectsUrl = String
				.format(new StringBuilder(SONAR_CLOUD_URL).append(RESOURCE_CLOUD_PROJECT_ENDPOINT).toString(), ORG_KEY);
		connection.setBaseUrl(SONAR_CLOUD_URL);
		connection.setUsername(null);
		connection.setPassword(null);
		connection.setCloudEnv(true);
		Mockito.doReturn(new ResponseEntity<>(projectJson, HttpStatus.OK)).when(restTemplate).exchange(
				Mockito.eq(projectsUrl), Mockito.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				Mockito.eq(String.class));
		when(aesEncryptionService.decrypt(connection.getAccessToken(), customApiConfig.getAesEncryptionKey()))
				.thenReturn("decryptTestKey");
		List<String> projectList = sonarToolConfigService.getSonarProjectKeyList(connection, ORG_KEY);
		Assert.assertEquals(projectList.size(), cloudResponseProjectList.size());
	}

	@Test
	public void testProjectsExceptionOrNull() throws Exception {
		String projectsUrl = SONAR_URL + RESOURCE_PROJECT_ENDPOINT;

		Mockito.doThrow(new RestClientException(EXCEPTION)).when(restTemplate).exchange(Mockito.eq(projectsUrl),
				Mockito.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class), Mockito.eq(String.class));

		try {
			List<String> projectList = sonarToolConfigService.getSonarProjectKeyList(connection, "");
			Assert.assertEquals(projectList.size(), new ArrayList<>().size());
		} catch (RestClientException exception) {
			Assert.assertEquals(EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void testGetSonarBranchListSuccess() throws Exception {
		String branchJson = getJSONDataResponse("sonarV8Dot9Branches.json");
		String branchUrl = String.format(new StringBuilder(SONAR_URL).append(RESOURCE_BRANCH_ENDPOINT).toString(),
				PROJECT_KEY);
		Mockito.doReturn(new ResponseEntity<>(branchJson, HttpStatus.OK)).when(restTemplate).exchange(
				Mockito.eq(branchUrl), Mockito.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				Mockito.eq(String.class));
		when(connectionRepository.findById(new ObjectId(connectionId))).thenReturn(testConnectionOpt);
		ServiceResponse response = sonarToolConfigService.getSonarProjectBranchList(connectionId, versionAbove,
				PROJECT_KEY);
		Assert.assertTrue(((List<String>) response.getData()).size() > 0);
	}

	@Test
	public void testBranchesExceptionOrNull() throws Exception {
		String branchUrl = String.format(new StringBuilder(SONAR_URL).append(RESOURCE_BRANCH_ENDPOINT).toString(),
				PROJECT_KEY);

		Mockito.doThrow(new RestClientException(EXCEPTION)).when(restTemplate).exchange(Mockito.eq(branchUrl),
				Mockito.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class), Mockito.eq(String.class));

		try {
			List<String> branchList = sonarToolConfigService.getSonarProjectBranchList(connection, PROJECT_KEY);
			Assert.assertEquals(branchList.size(), new ArrayList<>().size());
		} catch (RestClientException exception) {
			Assert.assertEquals(EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void getSonarProjectKeyListTest1() throws IOException {
		String projectJson = getJSONDataResponse("sonarV8Dot9Projects.json");
		String projectsUrl = SONAR_URL + RESOURCE_PROJECT_ENDPOINT;
		Mockito.doReturn(new ResponseEntity<>(projectJson, HttpStatus.OK)).when(restTemplate).exchange(
				Mockito.eq(projectsUrl), Mockito.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				Mockito.eq(String.class));
		when(connectionRepository.findById(new ObjectId(connectionId))).thenReturn(testConnectionOpt);
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		assertEquals(optConnection, testConnectionOpt);
		List<String> projectList = sonarToolConfigService.getSonarProjectKeyList(connectionId, "");
		Assert.assertEquals(projectList.size(), responseProjectList.size());
	}

	@Test
	public void getSonarProjectBranchList() throws IOException {
		String branchJson = getJSONDataResponse("sonarV8Dot9Branches.json");
		String branchUrl = String.format(new StringBuilder(SONAR_URL).append(RESOURCE_BRANCH_ENDPOINT).toString(),
				PROJECT_KEY);
		Mockito.doReturn(new ResponseEntity<>(branchJson, HttpStatus.OK)).when(restTemplate).exchange(
				Mockito.eq(branchUrl), Mockito.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				Mockito.eq(String.class));

		when(connectionRepository.findById(new ObjectId(connectionId))).thenReturn(testConnectionOpt);
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		assertEquals(optConnection, testConnectionOpt);
		List<String> branchList = sonarToolConfigService.getSonarProjectBranchList(connection, PROJECT_KEY);
		Assert.assertEquals(branchList.size(), responseBranchList.size());
	}

	private String getJSONDataResponse(String fileName) throws IOException {
		String filePath = "src/test/resources/json/toolConfig/" + fileName;
		return new String(Files.readAllBytes(Paths.get(filePath)));
	}

}
