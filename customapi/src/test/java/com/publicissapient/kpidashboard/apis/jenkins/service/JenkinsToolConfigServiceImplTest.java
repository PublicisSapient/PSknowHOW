package com.publicissapient.kpidashboard.apis.jenkins.service;

import static org.junit.Assert.assertEquals;
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
public class JenkinsToolConfigServiceImplTest {

	@Mock
	private RestAPIUtils restAPIUtils;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private ConnectionRepository connectionRepository;

	@InjectMocks
	private JenkinsToolConfigServiceImpl jenkinsToolConfigService;

	private Optional<Connection> testConnectionOpt;
	private Connection connection;
	private String connectionId;
	private List<String> responseProjectList = new ArrayList<>();

	private String jenkinsUrl = "http://test.abc.com/api/json?tree=jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url,jobs[url]]]]]]]]]]]]";

	@Before
	public void setup() {
		connectionId = "5fc4d61f80b6350f048a93e5";
		connection = new Connection();
		connection.setId(new ObjectId(connectionId));
		connection.setBaseUrl("http://test.abc.com");
		connection.setUsername("username");
		connection.setApiKey("encryptKey");
		testConnectionOpt = Optional.ofNullable(connection);
	}

	@Test
	public void getJenkinsJobNameListTestSuccess() throws IOException, ParseException {
		when(connectionRepository.findById(new ObjectId(connectionId))).thenReturn(testConnectionOpt);
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		assertEquals(optConnection, testConnectionOpt);
		when(restAPIUtils.decryptPassword(connection.getApiKey())).thenReturn("decryptKey");
		HttpHeaders header = new HttpHeaders();
		header.add("Authorization", "base64str");
		when(restAPIUtils.getHeaders(connection.getUsername(), "decryptKey")).thenReturn(header);
		HttpEntity<?> httpEntity = new HttpEntity<>(header);
		doReturn(new ResponseEntity<>(getServerResponseFromJson("jenkinsJobNameList.json"), HttpStatus.OK))
				.when(restTemplate).exchange(eq(jenkinsUrl), eq(HttpMethod.GET), eq(httpEntity), eq(String.class));
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("url", "http://dummyServer.com/job/API_Build/");
		jsonObject.put("_class", "org.jenkinsci.plugins.workflow.job.WorkflowJob");
		jsonArray.add(jsonObject);
		responseProjectList.add(jsonObject.toJSONString());
		when(restAPIUtils.convertJSONArrayFromResponse(getServerResponseFromJson("jenkinsJobNameList.json"), "jobs"))
				.thenReturn(jsonArray);
		when(restAPIUtils.convertListFromMultipleArray(jsonArray, "url")).thenReturn(responseProjectList);
		Assert.assertEquals(jenkinsToolConfigService.getJenkinsJobNameList(connectionId).size(),
				responseProjectList.size());
	}

	@Test
	public void getJenkinsJobNameListNull() throws IOException, ParseException {
		when(connectionRepository.findById(new ObjectId(connectionId))).thenReturn(testConnectionOpt);
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		assertEquals(optConnection, testConnectionOpt);
		when(restAPIUtils.decryptPassword(connection.getApiKey())).thenReturn("decryptKey");
		HttpHeaders header = new HttpHeaders();
		header.add("Authorization", "base64str");
		when(restAPIUtils.getHeaders(connection.getUsername(), "decryptKey")).thenReturn(header);
		HttpEntity<?> httpEntity = new HttpEntity<>(header);
		doReturn(new ResponseEntity<>(null, null, HttpStatus.NO_CONTENT)).when(restTemplate).exchange(eq(jenkinsUrl),
				eq(HttpMethod.GET), eq(httpEntity), eq(String.class));
		Assert.assertEquals(0, jenkinsToolConfigService.getJenkinsJobNameList(connectionId).size());
	}

	private String getServerResponseFromJson(String fileName) throws IOException {
		String filePath = "src/test/resources/json/toolConfig/" + fileName;
		return new String(Files.readAllBytes(Paths.get(filePath)));
	}
}
