package com.publicissapient.kpidashboard.jira.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.jira.data.SprintDetailsDataFactory;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.SprintTraceLogRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.client.jiraissue.ScrumJiraIssueClientImpl;
import com.publicissapient.kpidashboard.jira.client.sprint.SprintClientImpl;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthClient;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthProperties;

@ExtendWith(SpringExtension.class)
class FetchSprintDataServiceImplTest {
	private static final String PLAIN_TEXT_PASSWORD = "TestPlainPassword";
	@InjectMocks
	FetchSprintDataServiceImpl fetchSprintDataService;
	@Mock
	SprintClientImpl sprintClient;
	@Mock
	ProjectBasicConfigRepository projectBasicConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	private ConnectionRepository connectionRepository;
	@Mock
	private ToolCredentialProvider toolCredentialProvider;
	@Mock
	private AesEncryptionService aesEncryptionService;
	@Mock
	private JiraProcessorConfig jiraProcessorConfig;

	@Mock
	private ProjectToolConfigRepository toolRepository;
	@Mock
	private JiraRestClientFactory jiraRestClientFactory;
	@Mock
	private JiraOAuthProperties jiraOAuthProperties;
	@Mock
	private JiraOAuthClient jiraOAuthClient;
	@Mock
	SprintRepository sprintRepository;
	@Mock
	ScrumJiraIssueClientImpl scrumJiraIssueClientImpl;
	@Mock
	SprintTraceLogRepository sprintTraceLogRepository;
	@Mock
	private FieldMapping fieldMapping;
	@Mock
	private SprintDetails sprintDetails;

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/field_mapping.json");
		fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);

		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
		sprintDetails = sprintDetailsDataFactory.getSprintDetails().get(0);

		when(aesEncryptionService.decrypt(anyString(), anyString())).thenReturn(PLAIN_TEXT_PASSWORD);
	}

	@Test
	void testFetchSprintData_Success() {
		String sprintID = "sprint123";

		List<ProjectToolConfig> projectToolConfigList = new ArrayList<>();
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectToolConfig.setConnectionId(new ObjectId("5b719d06a500d00814bfb2b9"));
		projectToolConfig.setToolName(ProcessorConstants.JIRA);
		projectToolConfigList.add(projectToolConfig);

		Optional<Connection> conn = Optional.of(new Connection());
		conn.get().setOffline(Boolean.FALSE);
		conn.get().setIsOAuth(Boolean.TRUE);
		conn.get().setUsername("xyz");
		conn.get().setBearerToken(true);
		conn.get().setPatOAuthToken("testPassword");

		when(sprintRepository.findBySprintID(sprintID)).thenReturn(sprintDetails);

		ProjectBasicConfig projectBasicConfig = ProjectBasicConfig.builder().build();
		projectBasicConfig.setId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectBasicConfig.setProjectName("test-project");
		when(projectBasicConfigRepository.findById(any())).thenReturn(Optional.of(projectBasicConfig));
		fieldMapping.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		when(fieldMappingRepository.findByBasicProjectConfigId(any())).thenReturn(fieldMapping);
		when(toolRepository.findByToolNameAndBasicProjectConfigId(any(), any())).thenReturn(projectToolConfigList);
		when(connectionRepository.findById(any())).thenReturn(conn);
		when(sprintClient.getSprints(any(), any(), any())).thenReturn(new ArrayList<>(getSprintDetails()));

		boolean result = fetchSprintDataService.fetchSprintData(sprintID);

		assertTrue(result);
	}
	private Set<SprintDetails> getSprintDetails() {
		Set<SprintDetails> set = new HashSet<>();
		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintID("asprintid");
		sprintDetails.setState("ACTIVE");
		List<String> list = new ArrayList<>();
		list.add("1111");
		sprintDetails.setOriginBoardId(list);
		set.add(sprintDetails);
		return set;
	}

}