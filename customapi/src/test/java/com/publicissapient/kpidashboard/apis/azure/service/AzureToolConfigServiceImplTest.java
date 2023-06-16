package com.publicissapient.kpidashboard.apis.azure.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
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
public class AzureToolConfigServiceImplTest {

	@Mock
	private RestAPIUtils restAPIUtils;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private ConnectionRepository connectionRepository;

	@InjectMocks
	private AzureToolConfigServiceImpl azureToolConfigService;

	private Optional<Connection> testConnectionOpt;
	private Connection connection1;
	private String connectionId;
	private List<String> responseProjectList = new ArrayList<>();
	private Connection connection2;
	private Optional<Connection> testConnectionOpt1;

	@Before
	public void setup() {
		connectionId = "5fc4d61f80b6350f048a93e5";
		connection1 = new Connection();
		connection1.setId(new ObjectId(connectionId));
		connection1.setBaseUrl("https://test.server.com/testUser/TestProject");
		connection1.setUsername("testDummyUser");
		connection1.setPat("encryptKey");
		testConnectionOpt = Optional.ofNullable(connection1);
		connectionId = "1290e452fa2b456d5a72099e";
		connection2 = new Connection();
		connection2.setId(new ObjectId(connectionId));
		connection2.setBaseUrl("https://test.server.com/testuser/testProject");
		connection2.setUsername("testDummyUser");
		connection2.setPat("encryptKey");
		testConnectionOpt1 = Optional.ofNullable(connection2);
	}

	@Test
	public void getAzurePipelineNameAndDefinitionIdListTestSuccess() throws IOException, ParseException {
		when(connectionRepository.findById(new ObjectId(connectionId))).thenReturn(testConnectionOpt);
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		assertEquals(optConnection, testConnectionOpt);
		when(restAPIUtils.decryptPassword(connection1.getPat())).thenReturn("decryptKey");
		HttpHeaders header = new HttpHeaders();
		header.add("Authorization", "base64str");
		when(restAPIUtils.getHeaders(connection1.getUsername(), "decryptKey")).thenReturn(header);
		HttpEntity<?> httpEntity = new HttpEntity<>(header);
		doReturn(new ResponseEntity<>(getServerResponseFromJson("azurePipelineAndDefinitions.json"), HttpStatus.OK))
				.when(restTemplate)
				.exchange(eq("https://test.server.com/testUser/TestProject/_apis/build/definitions?api-version=6.0"),
						eq(HttpMethod.GET), eq(httpEntity), eq(String.class));
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", "TEST_PROJECT");
		jsonObject.put("id", "1");
		jsonArray.add(jsonObject);
		responseProjectList.add(jsonObject.toJSONString());
		when(restAPIUtils.convertJSONArrayFromResponse(anyString(), anyString())).thenReturn(jsonArray);
		when(restAPIUtils.convertToString(jsonObject, "name")).thenReturn("TEST_PROJECT");
		when(restAPIUtils.convertToString(jsonObject, "id")).thenReturn("1");
		Assert.assertEquals(azureToolConfigService.getAzurePipelineNameAndDefinitionIdList(connectionId, "6.0").size(),
				responseProjectList.size());
	}

	@Test
	public void getAzurePipelineNameAndDefinitionIdListNull() throws IOException, ParseException {
		when(connectionRepository.findById(new ObjectId(connectionId))).thenReturn(testConnectionOpt);
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		assertEquals(optConnection, testConnectionOpt);
		when(restAPIUtils.decryptPassword(connection1.getPat())).thenReturn("decryptKey");
		HttpHeaders header = new HttpHeaders();
		header.add("Authorization", "base64str");
		when(restAPIUtils.getHeaders(connection1.getUsername(), "decryptKey")).thenReturn(header);
		HttpEntity<?> httpEntity = new HttpEntity<>(header);
		doReturn(new ResponseEntity<>(null, null, HttpStatus.NO_CONTENT)).when(restTemplate).exchange(
				eq("https://test.server.com/testUser/TestProject/_apis/build/definitions?api-version=6.0"),
				eq(HttpMethod.GET), eq(httpEntity), eq(String.class));
		Assert.assertEquals(0,
				azureToolConfigService.getAzurePipelineNameAndDefinitionIdList(connectionId, "6.0").size());
	}

	private String getServerResponseFromJson(String fileName) throws IOException {
		String filePath = "src/test/resources/json/toolConfig/" + fileName;
		return new String(Files.readAllBytes(Paths.get(filePath)));
	}

	@Test
	public void testAzureReleaseNameAndDefinitionId() throws IOException, ParseException {
		when(connectionRepository.findById(new ObjectId(connectionId))).thenReturn(testConnectionOpt1);
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		assertEquals(optConnection, testConnectionOpt1);
		when(restAPIUtils.decryptPassword(connection2.getPat())).thenReturn("decryptKey");
		HttpHeaders header = new HttpHeaders();
		header.add("Authorization", "base64str");
		HttpEntity<?> httpEntity = new HttpEntity<>(header);
		Assert.assertEquals(0, azureToolConfigService.getAzureReleaseNameAndDefinitionIdList(connectionId).size());
	}

}
