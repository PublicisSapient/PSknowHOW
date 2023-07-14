package com.publicissapient.kpidashboard.apis.jira.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.jira.model.BoardDetailsDTO;
import com.publicissapient.kpidashboard.apis.jira.model.BoardRequestDTO;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.dto.AssigneeResponseDTO;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;

/**
 *
 * @author Hirenkumar babariya
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JiraToolConfigServiceImplTest {

	private static final String RESOURCE_JIRA_BOARD_ENDPOINT = "https://test.server.com/jira/rest/agile/1.0/board?projectKeyOrId=testProjectKey&startAt=0&type=scrum";
	final ObjectMapper mapper = new ObjectMapper();
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private RestAPIUtils restAPIUtils;
	@Mock
	private ConnectionRepository connectionRepository;
	@Mock
	private ProjectBasicConfigRepository projectBasicConfigRepository;
	@Mock
	private ProjectToolConfigRepository projectToolConfigRepository;
	@InjectMocks
	private JiraToolConfigServiceImpl jiraToolConfigService;
	@Mock
	private AssigneeDetailsRepository assigneeDetailsRepository;
	private Optional<Connection> testConnectionOpt;
	private Optional<ProjectBasicConfig> basicConfig;
	private ProjectBasicConfig projectBasicConfig;
	private Connection connection1;
	private String connectionId;
	private List<BoardDetailsDTO> responseList = new ArrayList<>();
	private List<ProjectToolConfig> projectToolConfigs;
	private ProjectToolConfig projectTool;
	private BoardRequestDTO boardRequestDTO;
	private String basicConfigId;

	@Before
	public void setup() {
		connectionId = "5fc4d61f80b6350f048a93e5";
		connection1 = new Connection();
		connection1.setId(new ObjectId(connectionId));
		connection1.setBaseUrl("https://test.server.com/jira");
		connection1.setUsername("testDummyUser");
		connection1.setPassword("encryptKey");
		connection1.setCloudEnv(false);
		testConnectionOpt = Optional.ofNullable(connection1);

		boardRequestDTO = new BoardRequestDTO();
		boardRequestDTO.setBoardType("scrum");
		boardRequestDTO.setConnectionId("5fc4d61f80b6350f048a93e5");
		boardRequestDTO.setProjectKey("testProjectKey");

		connection1.setApiEndPoint("rest/api/2/");
		basicConfigId = "634fdf4ec859a424263dc035";
		projectBasicConfig = new ProjectBasicConfig();
		projectToolConfigs = new ArrayList<>();
		basicConfig = Optional.ofNullable(projectBasicConfig);
		projectTool = new ProjectToolConfig();
		projectTool.setBasicProjectConfigId(new ObjectId(basicConfigId));
		projectTool.setConnectionId(new ObjectId(connectionId));
		projectTool.setProjectKey("ABC");
		projectToolConfigs.add(projectTool);

	}

	@Test
	public void getJiraBoardDetailsListTestSuccess() throws IOException, ParseException {
		when(connectionRepository.findById(new ObjectId(connectionId))).thenReturn(testConnectionOpt);
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		assertEquals(optConnection, testConnectionOpt);

		when(restAPIUtils.decryptPassword(connection1.getPassword())).thenReturn("decryptKey");
		HttpHeaders header = new HttpHeaders();
		header.add("Authorization", "Basic base64str");
		when(restAPIUtils.getHeaders(connection1.getUsername(), "decryptKey")).thenReturn(header);
		HttpEntity<?> httpEntity = new HttpEntity<>(header);

		doReturn(new ResponseEntity<>(getServerResponseFromJson("jiraBoardListResponse.json"), HttpStatus.OK))
				.when(restTemplate)
				.exchange(eq(RESOURCE_JIRA_BOARD_ENDPOINT), eq(HttpMethod.GET), eq(httpEntity), eq(String.class));
		BoardDetailsDTO boardRequestDTO1 = new BoardDetailsDTO();
		boardRequestDTO1.setBoardId(11862);
		boardRequestDTO1.setBoardName("Test | XYZ | Scrum Board");
		BoardDetailsDTO boardRequestDTO2 = new BoardDetailsDTO();
		boardRequestDTO2.setBoardId(14857);
		boardRequestDTO2.setBoardName("Test | ABC Support | Scrum Board");
		responseList.add(boardRequestDTO1);
		responseList.add(boardRequestDTO2);
		Assert.assertEquals(jiraToolConfigService.getJiraBoardDetailsList(boardRequestDTO).size(), responseList.size());
	}

	@Test
	public void getJiraBoardDetailsListNull() throws IOException, ParseException {
		when(connectionRepository.findById(new ObjectId(connectionId))).thenReturn(testConnectionOpt);
		Optional<Connection> optConnection = connectionRepository.findById(new ObjectId(connectionId));
		assertEquals(optConnection, testConnectionOpt);
		when(restAPIUtils.decryptPassword(connection1.getPassword())).thenReturn("decryptKey");
		HttpHeaders header = new HttpHeaders();
		header.add("Authorization", "base64str");
		when(restAPIUtils.getHeaders(connection1.getUsername(), "decryptKey")).thenReturn(header);
		HttpEntity<?> httpEntity = new HttpEntity<>(header);
		doReturn(new ResponseEntity<>(null, null, HttpStatus.NO_CONTENT)).when(restTemplate)
				.exchange(eq(RESOURCE_JIRA_BOARD_ENDPOINT), eq(HttpMethod.GET), eq(httpEntity), eq(String.class));
		Assert.assertEquals(0, jiraToolConfigService.getJiraBoardDetailsList(boardRequestDTO).size());
	}

	private String getServerResponseFromJson(String fileName) throws IOException {
		String filePath = "src/test/resources/json/toolConfig/" + fileName;
		return new String(Files.readAllBytes(Paths.get(filePath)));
	}

	@Test
	public void getProjectAssigneeDetailsSuccess() {
		AssigneeDetails assigneeDetails = new AssigneeDetails();
		assigneeDetails.setBasicProjectConfigId("634fdf4ec859a424263dc035");
		assigneeDetails.setSource("Jira");
		Set<Assignee> assigneeSet = new HashSet<>();
		assigneeSet.add(new Assignee("ankbhard", "Ankita sharma"));
		assigneeSet.add(new Assignee("llid", "displayName"));
		assigneeDetails.setAssignee(assigneeSet);
		when(assigneeDetailsRepository.findByBasicProjectConfigId(any())).thenReturn(assigneeDetails);
		AssigneeResponseDTO assigneeResponseDTO = jiraToolConfigService.getProjectAssigneeDetails(basicConfigId);
		assertEquals(2, assigneeResponseDTO.getAssigneeDetailsList().size());
	}

}
