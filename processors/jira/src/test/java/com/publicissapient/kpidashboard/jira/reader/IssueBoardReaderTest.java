package com.publicissapient.kpidashboard.jira.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.StatusCategory;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.BasicVotes;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfigurationImpl;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.dataFactories.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ToolConfigDataFactory;
import com.publicissapient.kpidashboard.jira.helper.ReaderRetryHelper;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import com.publicissapient.kpidashboard.jira.service.FetchEpicData;
import com.publicissapient.kpidashboard.jira.service.JiraCommonService;

@RunWith(MockitoJUnitRunner.class)
public class IssueBoardReaderTest {

	@Mock
	private FetchProjectConfigurationImpl fetchProjectConfiguration;

	@Mock
	private JiraClient jiraClient;

	@Mock
	private JiraCommonService jiraCommonService;

	@Mock
	private JiraProcessorConfig jiraProcessorConfig;

	@Mock
	private FetchEpicData fetchEpicData;

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

	@Mock
	private FieldMappingRepository fieldMappingRepository;

	@Mock
	private ProjectToolConfigRepository toolRepository;

	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;

	@Mock
	private ConnectionRepository connectionRepository;

	@Mock
	private SprintRepository sprintRepository;

	@Mock
	KerberosClient krb5Client;

	@Mock
	ProcessorJiraRestClient client;

	@InjectMocks
	IssueBoardReader issueBoardReader;
	String boardId = "123";
	Issue issue;
	Issue issue1;
	List<Issue> issues = new ArrayList<>();
	Map<String, Map<String, String>> projectBoardWiseDeltaDate = new HashMap<>();
	int boardIssueSize = 0;
	Boolean fetchLastIssue = false;
	@Mock
	private ReaderRetryHelper retryHelper;
	private Iterator<BoardDetails> boardIterator;
	private Iterator<Issue> issueIterator;
	ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
	@Mock
	ReaderRetryHelper.RetryableOperation<List<Issue>> mockRetryableOperation;
	private String projectId = "63bfa0d5b7617e260763ca21";

	private String connectionId = "5fd99f7bc8b51a7b55aec836";
	List<ProjectBasicConfig> projectConfigsList;
	List<ProjectToolConfig> projectToolConfigs;
	Optional<Connection> connection;

	FieldMapping fieldMapping = new FieldMapping();

	@Before
	public void setup() throws Exception, IOException, InterruptedException {
		projectToolConfigs = getMockProjectToolConfig();
		projectConfigsList = getMockProjectConfig();
		connection = getMockConnection();
		fieldMapping = getMockFieldMapping();
		createProjectConfigMap();
		createIssue();
		boardIterator = projectConfFieldMapping.getProjectToolConfig().getBoards().iterator();
		issueIterator = issues.iterator();
		jiraProcessorConfig = new JiraProcessorConfig();
		jiraProcessorConfig.setPageSize(50);
		when(fetchProjectConfiguration.fetchConfiguration(null)).thenReturn(projectConfFieldMapping);

	}

	@Test
	public void testReadData() throws Exception {
		when(mockRetryableOperation.execute()).thenReturn(issues);
		when(retryHelper.executeWithRetry(any())).thenReturn(issues);
		when(jiraClient.getClient(projectConfFieldMapping, krb5Client)).thenReturn(client);
		when(jiraCommonService.fetchIssueBasedOnBoard(any(), any(), anyInt(), anyString(), anyString()))
				.thenReturn(issues);
		when(fetchEpicData.fetchEpic(any(), anyString(), any(), any())).thenReturn(issues);
		// Arrange
		ReadData mockReadData = getMockReadData();

		// Act
		ReadData result = issueBoardReader.read();

		// Assert
		assertEquals(mockReadData.getIssue(), result.getIssue());
	}

	@Test
	public void testReadDataNoDataFound() throws Exception {
		when(jiraClient.getClient(projectConfFieldMapping, krb5Client)).thenReturn(client);
		when(mockRetryableOperation.execute()).thenReturn(null);
		when(retryHelper.executeWithRetry(any())).thenReturn(null);

		// Assert
		assertThrows(NullPointerException.class, () -> issueBoardReader.read());
	}

	private ReadData getMockReadData() {
		ReadData readData = new ReadData();
		readData.setBoardId(boardId);
		readData.setIssue(issue);
		readData.setProjectConfFieldMapping(projectConfFieldMapping);
		readData.setSprintFetch(true);
		return readData;
	}

	private void createProjectConfigMap() {
		ProjectBasicConfig projectConfig = projectConfigsList.get(1);
		try {
			BeanUtils.copyProperties(projectConfFieldMapping, projectConfig);
		} catch (IllegalAccessException | InvocationTargetException e) {
		}
		projectConfFieldMapping.setProjectBasicConfig(projectConfig);
		projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping.setJira(getJiraToolConfig());
		projectConfFieldMapping.setJiraToolConfigId(projectToolConfigs.get(0).getId());
		projectConfFieldMapping.setFieldMapping(fieldMapping);
		projectConfFieldMapping.setProjectToolConfig(projectToolConfigs.get(0));
	}

	private JiraToolConfig getJiraToolConfig() {
		JiraToolConfig toolObj = new JiraToolConfig();
		try {
			BeanUtils.copyProperties(toolObj, projectToolConfigs.get(0));
		} catch (IllegalAccessException | InvocationTargetException e) {

		}
		toolObj.setConnection(connection);
		return toolObj;
	}

	private Optional<Connection> getMockConnection() {
		ConnectionsDataFactory connectionDataFactory = ConnectionsDataFactory
				.newInstance("/json/default/connections.json");
		return connectionDataFactory.findConnectionById(connectionId);
	}

	private List<ProjectBasicConfig> getMockProjectConfig() {
		ProjectBasicConfigDataFactory projectConfigDataFactory = ProjectBasicConfigDataFactory
				.newInstance("/json/default/project_basic_configs.json");
		return projectConfigDataFactory.getProjectBasicConfigs();
	}

	private List<ProjectToolConfig> getMockProjectToolConfig() {
		ToolConfigDataFactory projectToolConfigDataFactory = ToolConfigDataFactory
				.newInstance("/json/default/project_tool_configs.json");
		return projectToolConfigDataFactory.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA, projectId);
	}

	private FieldMapping getMockFieldMapping() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/field_mapping.json");
		return fieldMappingDataFactory.findByBasicProjectConfigId(projectId);
	}

	private void createIssue() throws URISyntaxException {
		BasicProject basicProj = new BasicProject(new URI("self"), "proj1", 1l, "project1");
		IssueType issueType1 = new IssueType(new URI("self"), 1l, "Epic", false, "desc", new URI("iconURI"));
		IssueType issueType2 = new IssueType(new URI("self"), 2l, "Epic", false, "desc", new URI("iconURI"));
		Status status1 = new Status(new URI("self"), 1l, "Ready for Sprint Planning", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		BasicPriority basicPriority = new BasicPriority(new URI("self"), 1l, "priority1");
		Resolution resolution = new Resolution(new URI("self"), 1l, "resolution", "resolution");
		Map<String, URI> avatarMap = new HashMap<>();
		avatarMap.put("48x48", new URI("value"));
		User user1 = new User(new URI("self"), "user1", "user1", "userAccount", "user1@xyz.com", true, null, avatarMap,
				null);
		Map<String, String> map = new HashMap<>();
		map.put("customfield_12121", "Client Testing (UAT)");
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Component");
		map.put("id", "20810");
		JSONObject value = new JSONObject(map);
		IssueField issueField = new IssueField("20810", "Component", null, value);
		List<IssueField> issueFields = Arrays.asList(issueField);
		Comment comment = new Comment(new URI("self"), "body", null, null, DateTime.now(), DateTime.now(),
				new Visibility(Visibility.Type.ROLE, "abc"), 1l);
		List<Comment> comments = Arrays.asList(comment);
		BasicVotes basicVotes = new BasicVotes(new URI("self"), 1, true);
		BasicUser basicUser = new BasicUser(new URI("self"), "basicuser", "basicuser", "accountId");
		Worklog worklog = new Worklog(new URI("self"), new URI("self"), basicUser, basicUser, null, DateTime.now(),
				DateTime.now(), DateTime.now(), 60, null);
		List<Worklog> workLogs = Arrays.asList(worklog);
		ChangelogItem changelogItem = new ChangelogItem(FieldType.JIRA, "field1", "from", "fromString", "to",
				"toString");
		ChangelogGroup changelogGroup = new ChangelogGroup(basicUser, DateTime.now(), Arrays.asList(changelogItem));

		issue = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType1, status1, "story",
				basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(),
				DateTime.now(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, issueFields, comments,
				null, createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
				Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));
		issue1 = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType2, status1, "Defect",
				basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(),
				DateTime.now(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, issueFields, comments,
				null, createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
				Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));
		issues.add(issue);
		issues.add(issue1);

	}

	private List<IssueLink> createIssueLinkData() throws URISyntaxException {
		List<IssueLink> issueLinkList = new ArrayList<>();
		URI uri = new URI("https://testDomain.com/jira/rest/api/2/issue/12344");
		IssueLinkType linkType = new IssueLinkType("Blocks", "blocks", IssueLinkType.Direction.OUTBOUND);
		IssueLink issueLink = new IssueLink("IssueKey", uri, linkType);
		issueLinkList.add(issueLink);

		return issueLinkList;
	}
}
