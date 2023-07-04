package com.publicissapient.kpidashboard.jira.client.jiraissue;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.ExpandableProperty;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.impl.OfflineAdapter;
import com.publicissapient.kpidashboard.jira.client.sprint.SprintClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraProcessor;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.AdditionalFilterHelper;

@RunWith(MockitoJUnitRunner.class)
public class ScrumJiraIssueClientImplTest {

	@InjectMocks
	private ScrumJiraIssueClientImpl scrumJiraIssueClient;

	@Mock
	private HandleJiraHistory handleJiraHistory;

	@Mock
	private JiraIssueRepository jiraIssueRepository;

	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Mock
	private JiraProcessorConfig jiraProcessorConfig;

	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Mock
	private JiraProcessorRepository jiraProcessorRepository;

	@Mock
	private HierarchyLevelService hierarchyLevelService;

	@Mock
	private AccountHierarchyRepository accountHierarchyRepository;

	@Mock
	private SprintClient sprintClient;

	@Mock
	private AdditionalFilterHelper additionalFilterHelper;

	@Mock
	private AssigneeDetailsRepository assigneeDetailsRepository;


	private static JiraToolConfig getJiraToolConfig(FieldMapping fieldMapping) {
		JiraToolConfig jiraToolConfig = new JiraToolConfig();
		jiraToolConfig.setBasicProjectConfigId("632eb205e0fd283f9bb747ad");
		jiraToolConfig.setProjectId("123");
		jiraToolConfig.setProjectKey("123");
		jiraToolConfig.setFieldMapping(fieldMapping);
		jiraToolConfig.setCreatedAt("2021-07-26T10:22:12.0000000");
		jiraToolConfig.setUpdatedAt("2021-07-26T10:22:12.0000000");
		jiraToolConfig.setQueryEnabled(false);
		jiraToolConfig.setBoardQuery("query");
		Connection connection = getConnectionObject();
		jiraToolConfig.setConnection(Optional.of(connection));
		BoardDetails boardDetails = new BoardDetails();
		boardDetails.setBoardName("TestHOW");
		boardDetails.setBoardId("123");
		ArrayList<BoardDetails> al = new ArrayList<>();
		al.add(boardDetails);
		jiraToolConfig.setBoards(al);
		return jiraToolConfig;
	}

	private static ProjectConfFieldMapping getProjectConfFieldMapping(FieldMapping fieldMapping,
			ProjectToolConfig projectToolConfig, ProjectBasicConfig projectBasicConfig, JiraToolConfig jiraToolConfig) {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		projectConfFieldMapping.setJira(getJiraToolConfig(fieldMapping));
		projectConfFieldMapping.setFieldMapping(fieldMapping);
		projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
		projectConfFieldMapping.setIssueCount(2);
		projectConfFieldMapping.setKanban(true);
		projectConfFieldMapping.setSprintCount(12);
		projectConfFieldMapping.setProjectName("TestHOW");
		projectConfFieldMapping.setProjectToolConfig(projectToolConfig);
		projectConfFieldMapping.setJiraToolConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
		projectConfFieldMapping.setProjectBasicConfig(projectBasicConfig);
		projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
		return projectConfFieldMapping;
	}

	private static Connection getConnectionObject() {
		Connection connection = new Connection();
		connection.setType("Defect");
		connection.setConnectionName("TEST");
		connection.setCloudEnv(true);
		connection.setBaseUrl("url");
		connection.setUsername("test");
		connection.setPassword("testPassword");
		connection.setApiEndPoint("url");
		connection.setConsumerKey("123");
		connection.setPrivateKey("123");
		connection.setApiKey("123");
		connection.setClientSecretKey("999");
		connection.setIsOAuth(true);
		connection.setClientId("111");
		connection.setTenantId("111");
		connection.setPat("pat");
		connection.setApiKeyFieldName("apiKey");
		connection.setAccessToken("accessToken");
		connection.setOffline(true);
		connection.setOfflineFilePath("offlineFilePath");
		connection.setCreatedAt("now");
		connection.setUpdatedAt("later");
		connection.setUpdatedBy("TestHOW");
		connection.setConnPrivate(true);
		connection.setUpdatedBy("TestHOW");
		ArrayList<String> alStrings = new ArrayList<>();
		alStrings.add("TestHOW");
		connection.setConnectionUsers(alStrings);
		return connection;
	}

	@Test
	public void purgeJiraIssuesTest() {
		List<Issue> issuesList = new ArrayList<>();
		Issue issue = new Issue("summary", null, "key", 121L, null, null,
				new Status(null, null, "TestHOW", null, null, null), "description", null, null, null, null, null,
				DateTime.now(), DateTime.now(), null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, null, null);
		issuesList.add(issue);
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
		String[] srs = new String[2];
		srs[0] = "TestHOW";
		srs[1] = "TestHOW1";
		fieldMapping.setJiraIssueTypeNames(srs);
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
		BoardDetails board = new BoardDetails();
		board.setBoardId("1111");
		board.setBoardName("test board");
		List<BoardDetails> boardList = new ArrayList<>();
		boardList.add(board);
		projectToolConfig.setBoards(boardList);
		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("632eb205e0fd283f9bb747ad"));
		JiraToolConfig jiraToolConfig = getJiraToolConfig(fieldMapping);
		ProjectConfFieldMapping projectConfFieldMapping = getProjectConfFieldMapping(fieldMapping, projectToolConfig,
				projectBasicConfig, jiraToolConfig);
		List<JiraIssue> jiraIssues = new ArrayList<>();
		JiraIssue jiraIssue = new JiraIssue();
		jiraIssues.add(jiraIssue);
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();
		JiraIssueCustomHistory jiraIssueCustomHistory = new JiraIssueCustomHistory();
		jiraIssueCustomHistories.add(jiraIssueCustomHistory);
		when(jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(jiraIssues);
		when(jiraIssueCustomHistoryRepository.findByStoryIDAndBasicProjectConfigId(Mockito.anyString(),
				Mockito.anyString())).thenReturn(jiraIssueCustomHistories);
		scrumJiraIssueClient.purgeJiraIssues(issuesList, projectConfFieldMapping);
	}

	@Test
	public void isBugRaisedByValueMatchesRaisedByCustomFieldTest() {
		boolean flag = scrumJiraIssueClient.isBugRaisedByValueMatchesRaisedByCustomField(Arrays.asList("2", "3"),
				new Object());
		assertEquals(false, flag);
	}

	@Test
	public void processesJiraIssues() throws InterruptedException, URISyntaxException {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
		String[] srs = new String[2];
		srs[0] = "TestHOW";
		srs[1] = "TestHOW1";
		fieldMapping.setJiraIssueTypeNames(srs);
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
		BoardDetails board = new BoardDetails();
		board.setBoardId("1111");
		board.setBoardName("test board");
		List<BoardDetails> boardList = new ArrayList<>();
		boardList.add(board);
		projectToolConfig.setBoards(boardList);
		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("632eb205e0fd283f9bb747ad"));
		projectBasicConfig.setSaveAssigneeDetails(true);
		JiraToolConfig jiraToolConfig = getJiraToolConfig(fieldMapping);
		Set<String> stringSet = new HashSet<>();
		stringSet.add("Bug");
		stringSet.add("TestHOW");
		ArrayList<Version> alVersion = new ArrayList<>();
		Version version = null;
		alVersion.add(version);
		ChangelogItem changelogItem = new ChangelogItem(null, "", "", "", "", "");
		List<ChangelogItem> itemList = new ArrayList<>();
		itemList.add(changelogItem);
		List<ChangelogGroup> grouplist = new ArrayList<>();
		grouplist.add(new ChangelogGroup(null, DateTime.now(), itemList));
		BasicProject project = new BasicProject(null, "key", null, null);
		Map<String, URI> userAvtar = new HashMap<>();
		userAvtar.put("48x48", new URI("https://test.com/jira/secure/useravatar?avatarId=10122"));
		User user1 = new User(new URI("https://test.com/jira/rest/api/2/user?username=testUser"), "TestUser", "User",
				"llid", true, new ExpandableProperty<>(0), userAvtar, "");
		Issue issue = new Issue("summary", null, "key", 121L, project,
				new IssueType(null, 11L, "Defect", true, "Description", null),
				new Status(null, null, "TestHOW", null, null, null), "description", null, null, null, null, user1,
				DateTime.now(), DateTime.now(), null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, grouplist, null, stringSet);
		Iterable<Issue> iterable = Arrays.asList(issue);
		SearchResult searchResult = new SearchResult(0, 0, 1, iterable);
		JiraAdapter jiraAdapter = new OfflineAdapter(jiraProcessorConfig, searchResult, alVersion);
		JiraProcessor jiraProcessor = new JiraProcessor();
		jiraProcessor.setId(new ObjectId("632eb205e0fd283f9bb747ad"));
		List<HierarchyLevel> hierarchyLevelList = new ArrayList<>();
		HierarchyLevel hierarchyLevel = new HierarchyLevel();
		hierarchyLevel.setHierarchyLevelId("121");
		hierarchyLevelList.add(hierarchyLevel);
		List<AccountHierarchy> accountHierarchyList = new ArrayList<>();
		AccountHierarchy accountHierarchy = new AccountHierarchy();
		accountHierarchy.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
		accountHierarchyList.add(accountHierarchy);
		Set<SprintDetails> sprintDetailsSet = new LinkedHashSet<>();
		when(jiraProcessorConfig.getStartDate()).thenReturn("2022-09-28 10:22");
		when(jiraProcessorConfig.getPageSize()).thenReturn(2);
		when(hierarchyLevelService.getFullHierarchyLevels(true)).thenReturn(hierarchyLevelList);
		when(jiraProcessorRepository.findByProcessorName(Mockito.anyString())).thenReturn(jiraProcessor);
		when(accountHierarchyRepository.findAll()).thenReturn(accountHierarchyList);
		when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(any(), any())).thenReturn(null);
		doNothing().when(processorExecutionTraceLogService).save(Mockito.any());
		assertEquals(1,
				scrumJiraIssueClient.processesJiraIssues(
						getProjectConfFieldMapping(fieldMapping, projectToolConfig, projectBasicConfig, jiraToolConfig),
						jiraAdapter, true));
	}

	@Test
	public void setTestAutomatedFieldTest() {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setBasicProjectConfigId(new ObjectId("632eb205e0fd283f9bb747ad"));
		String[] srs = new String[2];
		srs[0] = "TestHOW";
		srs[1] = "TestHOW1";
		fieldMapping.setJiraIssueTypeNames(srs);
		JiraIssue jiraIssue = new JiraIssue();
		Map<String, IssueField> map = new HashMap<>();
		IssueField issueField = null;
		map.put("111", issueField);
		Issue issue = new Issue("summary", null, "key", 121L, null, null,
				new Status(null, null, "TestHOW", null, null, null), "description", null, null, null, null, null,
				DateTime.now(), DateTime.now(), null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, null, null);
	}
}
