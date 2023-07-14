package com.publicissapient.kpidashboard.azure.client.sprint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.azure.adapter.AzureAdapter;
import com.publicissapient.kpidashboard.azure.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.azure.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.azure.model.AzureProcessor;
import com.publicissapient.kpidashboard.azure.model.AzureServer;
import com.publicissapient.kpidashboard.azure.model.AzureToolConfig;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.azure.repository.AzureProcessorRepository;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@ExtendWith(SpringExtension.class)
public class SprintClientImplTest {

	@Mock
	private SprintRepository sprintRepository;

	@InjectMocks
	private SprintClientImpl sprintClientImpl;

	@Mock
	private AzureAdapter azureAdapter;

	@Mock
	private AzureProcessorRepository azureProcessorRepository;

	@Mock
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Mock
	private JiraIssueRepository jiraIssueRepository;

	private SprintDetails sprintDetails1;
	private SprintDetails sprintDetails2;

	private SprintDetails sprintDetails3;

	private Set<SprintDetails> sprintDetailsSet = new HashSet<>();
	private List<SprintDetails> dbSprintDetailsList = new ArrayList<>();

	private AzureToolConfig azureToolConfig = new AzureToolConfig();

	private ProjectConfFieldMapping projectConfig;

	private ProjectToolConfig projectToolConfig = new ProjectToolConfig();

	private List<JiraIssue> jiraIssueList = new ArrayList<>();

	@BeforeEach
	public void setUp() throws Exception {
		sprintDetails1 = new SprintDetails();

		sprintDetails1.setSprintID("abc-testProject");
		sprintDetails1.setSprintName("testSprint1");
		sprintDetails1.setOriginalSprintId("abc");
		sprintDetails1.setState("Closed");

		sprintDetails2 = new SprintDetails();
		sprintDetails2.setSprintID("testSprint_TestAzure_5ba8e182d3735010e7f1fa45");
		sprintDetails2.setSprintName("testSprint2");
		sprintDetails2.setOriginalSprintId("xyz");
		sprintDetails2.setState("Active");

		sprintDetails3 = new SprintDetails();
		sprintDetails3.setSprintID("pqr-testProject");
		sprintDetails3.setSprintName("testSprint3");
		sprintDetails3.setOriginalSprintId("pqr");
		sprintDetails3.setState("Future");
		sprintDetailsSet.add(sprintDetails1);
		sprintDetailsSet.add(sprintDetails2);
		sprintDetailsSet.add(sprintDetails3);

		azureToolConfig.setBasicProjectConfigId("5ba8e182d3735010e7f1fa45");
		Connection conn = new Connection();
		conn.setId(new ObjectId("6225b5c93f59bf41fe9f1b9d"));
		conn.setOffline(Boolean.FALSE);
		conn.setBaseUrl("https://test.com/testUser/Test%20ProjectOne");
		conn.setUsername("testUser");
		conn.setPat("testPat");
		azureToolConfig.setConnection(conn);

		projectConfig = ProjectConfFieldMapping.builder().build();
		projectConfig.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectConfig.setAzure(azureToolConfig);
		projectConfig.setAzureBoardToolConfigId(new ObjectId("61e4f7852747353d4405c762"));
		projectToolConfig.setApiVersion("5.1");
		projectToolConfig.setAzureIterationStatusFieldUpdate(true);
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		List<String> jiraIterationCompletionStatusCustomField = new ArrayList<>();
		jiraIterationCompletionStatusCustomField.add("Done");
		fieldMapping.setJiraIterationCompletionStatusCustomField(jiraIterationCompletionStatusCustomField);
		projectConfig.setFieldMapping(fieldMapping);
		projectConfig.setProjectToolConfig(projectToolConfig);

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		jiraIssueList = jiraIssueDataFactory.getJiraIssues();

		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
		dbSprintDetailsList = sprintDetailsDataFactory.getSprintDetails();
	}

	@Test
	void collectIssuesAndPrepareSprintReportForActiveSprint() throws Exception {

		AzureProcessor processor = new AzureProcessor();
		processor.setId(new ObjectId("61d6b74df0d2833694dcceb7"));

		when(sprintRepository.findBySprintID(sprintDetails2.getSprintID())).thenReturn(dbSprintDetailsList.get(0));
		when(azureProcessorRepository.findByProcessorName(anyString())).thenReturn(processor);
		List<String> issueItemList = new ArrayList<>();
		issueItemList.add("1");
		issueItemList.add("2");
		issueItemList.add("3");
		when(azureAdapter.getIssuesBySprint(prepareAzureServer(), "testSprint_TestAzure_5ba8e182d3735010e7f1fa45"))
				.thenReturn(issueItemList);

		when(jiraIssueRepository.findByNumberInAndBasicProjectConfigId(anyList(), anyString()))
				.thenReturn(jiraIssueList);
		when(projectToolConfigRepository.findById(anyString())).thenReturn(projectToolConfig);
		when(sprintRepository.findByBasicProjectConfigId(any())).thenReturn(new ArrayList<>(sprintDetailsSet));

		sprintClientImpl.prepareSprintReport(projectConfig, sprintDetailsSet, azureAdapter, prepareAzureServer());
	}

	@Test
	void collectIssuesAndPrepareSprintReportForFirstTime() throws Exception {

		AzureProcessor processor = new AzureProcessor();
		processor.setId(new ObjectId("61d6b74df0d2833694dcceb7"));
		when(azureProcessorRepository.findByProcessorName(anyString())).thenReturn(processor);

		when(sprintRepository.findBySprintID(any())).thenReturn(null);
		List<String> issueItemList = new ArrayList<>();
		issueItemList.add("88");
		issueItemList.add("89");
		issueItemList.add("90");
		when(azureAdapter.getIssuesBySprint(prepareAzureServer(), "sprint1")).thenReturn(issueItemList);
		when(jiraIssueRepository.findByNumberInAndBasicProjectConfigId(anyList(), anyString()))
				.thenReturn(jiraIssueList);

		when(projectToolConfigRepository.findById(anyString())).thenReturn(projectToolConfig);
		when(sprintRepository.findByBasicProjectConfigId(any())).thenReturn(new ArrayList<>(sprintDetailsSet));

		sprintClientImpl.prepareSprintReport(projectConfig, sprintDetailsSet, azureAdapter, prepareAzureServer());
	}

	private AzureServer prepareAzureServer() {
		AzureServer azureServer = new AzureServer();
		azureServer.setPat("TestUser@123");
		azureServer.setUrl("https://test.com/testUser/testProject");
		azureServer.setApiVersion("5.1");
		azureServer.setUsername("");
		return azureServer;

	}

}
