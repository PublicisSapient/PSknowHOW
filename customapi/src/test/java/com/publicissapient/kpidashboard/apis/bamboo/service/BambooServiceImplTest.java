package com.publicissapient.kpidashboard.apis.bamboo.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;

@RunWith(MockitoJUnitRunner.class)
public class BambooServiceImplTest {

	@Mock
	private RestAPIUtils restAPIUtils;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private ConnectionRepository connectionRepository;

	@InjectMocks
	private BambooToolConfigServiceImpl bambooToolConfigService;

	private Optional<Connection> testConnectionOpt;
	private Connection connection;
	private String connectionId;
	private List<String> responseProjectList = new ArrayList<>();

	@Before
	public void setup() {
		connectionId = "5fc4d61f80b6350f048a93e5";
		connection = new Connection();
		connection.setId(new ObjectId(connectionId));
		connection.setBaseUrl("https://test.server.com/bamboo");
		connection.setUsername("tst-ll-SystemAdmin");
		connection.setPassword("decryptPassword");
		testConnectionOpt = Optional.ofNullable(connection);
	}

	@Test
	public void getDeploymentProjectListTestSuccess() throws IOException, ParseException {
		when(connectionRepository.findById(new ObjectId(connectionId))).thenReturn(testConnectionOpt);
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		assertEquals(optConnection, testConnectionOpt);
		when(restAPIUtils.decryptPassword(connection.getPassword())).thenReturn("decryptPassword");

		HttpHeaders header = new HttpHeaders();
		header.add("Authorization", "base64str");
		HttpEntity<?> httpEntity = new HttpEntity<>(header);
		when(restAPIUtils.getHeaders("tst-ll-SystemAdmin", "decryptPassword")).thenReturn(header);
		when(restTemplate.exchange(
				"https://test.server.com/bamboo/rest/api/latest/search/deployments.json?max-result=2000",
				HttpMethod.GET, httpEntity, String.class)).thenReturn(
						new ResponseEntity<>(getServerResponseFromJson("bambooDeploymentJson.json"), HttpStatus.OK));

		responseProjectList.add(createJsonObject("Chat bot web", "18120708"));
		responseProjectList.add(createJsonObject(" Deploy_akka-profile-microservice", "61898790"));
		responseProjectList.add(createJsonObject(" Deploy_akka_account-microservice", "61898782"));
		responseProjectList.add(createJsonObject(" Deploy_Assembler", "61898783"));
		when(restAPIUtils.convertToString(any(), eq("projectName"))).thenReturn("Chat bot web");
		when(restAPIUtils.convertToString(any(), eq("key"))).thenReturn("18120708");
		Assert.assertEquals(bambooToolConfigService.getDeploymentProjectList(connectionId).size(),
				responseProjectList.size());
	}

	@Test
	public void getBambooBranchesNameAndKeysSuccess() throws IOException, ParseException {
		when(connectionRepository.findById(new ObjectId(connectionId))).thenReturn(testConnectionOpt);
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		assertEquals(optConnection, testConnectionOpt);
		when(restAPIUtils.decryptPassword(connection.getPassword())).thenReturn("decryptPassword");

		HttpHeaders header = new HttpHeaders();
		header.add("Authorization", "base64str");
		HttpEntity<?> httpEntity = new HttpEntity<>(header);
		when(restAPIUtils.getHeaders("tst-ll-SystemAdmin", "decryptPassword")).thenReturn(header);
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", "AZURE_KNOWHOW");
		jsonObject.put("id", "1");
		jsonArray.add(jsonObject);
		when(restTemplate.exchange(
				"https://test.server.com/bamboo/rest/api/latest/plan/COOP-CC/branch.json?max-result=2000",
				HttpMethod.GET, httpEntity, String.class)).thenReturn(
						new ResponseEntity<>(getServerResponseFromJson("bambooBranchListJson.json"), HttpStatus.OK));
		when(restAPIUtils.getJsonArrayFromJSONObj(any(), anyString())).thenReturn(jsonArray);
		Assert.assertEquals(1, bambooToolConfigService.getBambooBranchesNameAndKeys(connectionId, "COOP-CC").size());
	}

	@Test
	public void getProjectsAndPlanKeyListSuccess() throws IOException, ParseException {
		when(connectionRepository.findById(new ObjectId(connectionId))).thenReturn(testConnectionOpt);
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		assertEquals(optConnection, testConnectionOpt);
		when(restAPIUtils.decryptPassword(connection.getPassword())).thenReturn("decryptPassword");

		HttpHeaders header = new HttpHeaders();
		header.add("Authorization", "base64str");
		HttpEntity<?> httpEntity = new HttpEntity<>(header);
		when(restAPIUtils.getHeaders("tst-ll-SystemAdmin", "decryptPassword")).thenReturn(header);
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", "AZURE_KNOWHOW");
		jsonObject.put("id", "1");
		jsonArray.add(jsonObject);
		when(restTemplate.exchange(
				"https://test.server.com/bamboo/rest/api/latest/plan.json?expand=plans&max-result=2000", HttpMethod.GET,
				httpEntity, String.class)).thenReturn(
						new ResponseEntity<>(getServerResponseFromJson("bambooPlanListJson.json"), HttpStatus.OK));
		when(restAPIUtils.getJsonArrayFromJSONObj(any(), anyString())).thenReturn(jsonArray);
		Assert.assertEquals(1, bambooToolConfigService.getProjectsAndPlanKeyList(connectionId).size());
	}

	private String createJsonObject(String projectName, String projectId) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("projectName", projectName);
		jsonObject.put("key", projectId);
		return jsonObject.toJSONString();
	}

	private String getServerResponseFromJson(String fileName) throws IOException {
		String filePath = "src/test/resources/json/toolConfig/" + fileName;
		return new String(Files.readAllBytes(Paths.get(filePath)));
	}

}
