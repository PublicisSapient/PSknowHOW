/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.jira.processor.mode.impl.online;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.StatusCategory;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.User;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.SubProjectConfig;
import com.publicissapient.kpidashboard.common.model.application.Subproject;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.Identifier;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueHistory;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.SubProjectRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.adapter.impl.OnlineAdapter;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClient;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientFactory;
import com.publicissapient.kpidashboard.jira.client.jiraissue.KanbanJiraIssueClientImpl;
import com.publicissapient.kpidashboard.jira.client.jiraissue.ScrumJiraIssueClientImpl;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraInfo;
import com.publicissapient.kpidashboard.jira.model.JiraProcessor;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthClient;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthProperties;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;

import io.atlassian.util.concurrent.Promise;

@ExtendWith(SpringExtension.class)
public class OnlineDataProcessorImplTest {
	private static final String PLAIN_TEXT_PASSWORD = "TestPlainPassword";
	List<ProjectBasicConfig> scrumProjectList = new ArrayList<>();
	List<ProjectBasicConfig> kanbanProjectlist = new ArrayList<>();
	List<FieldMapping> fieldMappingList = new ArrayList<>();
	@Mock
	JiraProcessor jiraProcessor;
	@Mock
	Promise<SearchResult> promisedRs;
	@Mock
	ProcessorJiraRestClient client;
	@InjectMocks
	JiraIssueClient kanbanJiraIssueClient = new KanbanJiraIssueClientImpl();
	@InjectMocks
	JiraIssueClient scrumJiraIssueClient = new ScrumJiraIssueClientImpl();
	@InjectMocks
	OnlineDataProcessorImpl onlineDataProcessor;
	@Mock
	User user;
	@Mock
	Collection<ChangelogGroup> changelogGroup;
	@Mock
	Collection<ChangelogGroup> changelogGroup2;
	@Mock
	Promise<Project> projectPromise;
	@Mock
	Promise<Iterable<Field>> metaDataFieldPromise;
	@Mock
	Promise<Iterable<IssueType>> metaDataIssueTypePromise;
	@Mock
	Promise<Iterable<Status>> metaDataStatusPromise;
	AccountHierarchy accountHierarchy;
	List<Issue> issues = new ArrayList<>();
	Iterable<Issue> issueIterable;
	List<Issue> kanbanIssues = new ArrayList<>();
	Iterable<Issue> issueIterableKanban;
	List<ProjectConfFieldMapping> projectConfFieldMappingList = new ArrayList<>();
	ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
	ProjectConfFieldMapping projectConfFieldMapping2 = ProjectConfFieldMapping.builder().build();
	@Mock
	SearchRestClient searchRestClient;
	@Mock
	private JiraProcessorConfig jiraProcessorConfig;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	private FieldMappingRepository fieldMappingRepository;

	@Mock
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;
	@Mock
	private KanbanJiraIssueHistoryRepository kanbanIssueHistoryRepo;
	@Mock
	private KanbanJiraIssueRepository kanbanJiraRepo;
	@Mock
	private ProjectReleaseRepo projectReleaseRepo;
	@Mock
	private AccountHierarchyRepository accountHierarchyRepository;
	@Mock
	private JiraProcessorRepository jiraProcessorRepository;
	@Mock
	private JiraRestClientFactory jiraRestClientFactory;
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private JiraIssueClientFactory jiraIssueClientFactory;
	@Mock
	private ProjectToolConfigRepository toolRepository;
	@Mock
	private ConnectionRepository connectionRepository;
	@Mock
	private SubProjectRepository subProjectRepository;

	@Mock
	private JiraOAuthClient jiraOAuthClient;
	@Mock
	private BoardMetadataRepository boardMetadataRepository;
	@Mock
	private MetadataIdentifierRepository metadataIdentifierRepository;
	@Mock
	private JiraOAuthProperties jiraOAuthProperties;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@Mock
	private OnlineAdapter onlineAdapter;

	@Mock
	private ToolCredentialProvider toolCredentialProvider;

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		prepareProjectData();
		prepareProjectConfig();
		prepareFiledMapping();
		setProjectConfigFieldMap();
		when(aesEncryptionService.decrypt(anyString(), anyString())).thenReturn(PLAIN_TEXT_PASSWORD);

	}

	@Test
	public void validateAndCollectIssuesScrum() throws URISyntaxException {
		List<ProjectToolConfig> projectToolConfigList = new ArrayList<>();
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectToolConfig.setConnectionId(new ObjectId("5b719d06a500d00814bfb2b9"));
		projectToolConfig.setToolName(ProcessorConstants.JIRA);
		projectToolConfigList.add(projectToolConfig);
		Optional<Connection> conn = Optional.of(new Connection());
		conn.get().setOffline(Boolean.FALSE);

		List<Subproject> subProjectsList = new ArrayList<>();
		Subproject subproject = new Subproject();
		subproject.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		subproject.setToolConfigId(new ObjectId("5b674d58f47cae8935b1b26f"));
		subProjectsList.add(subproject);

		prepareIssuesData();
		prepareAccountHierarchy();
		JiraInfo jiraInfo = JiraInfo.builder()
				.jiraConfigBaseUrl(projectConfFieldMapping.getJira().getConnection().get().getBaseUrl())
				.username(projectConfFieldMapping.getJira().getConnection().get().getUsername())
				.password(projectConfFieldMapping.getJira().getConnection().get().getPassword())
				.jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build();

		JiraInfo jiraInfoOAuth = JiraInfo.builder().jiraConfigBaseUrl(jiraOAuthProperties.getJiraBaseURL())
				.jiraConfigAccessToken(jiraOAuthProperties.getAccessToken())
				.username(projectConfFieldMapping2.getJira().getConnection().get().getUsername())
				.password(projectConfFieldMapping2.getJira().getConnection().get().getPassword())
				.jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build();
		when(jiraProcessorConfig.getThreadPoolSize()).thenReturn(3);
		when(jiraRestClientFactory.getJiraClient(jiraInfo)).thenReturn(client);
		when(jiraRestClientFactory.getJiraClient(jiraInfoOAuth)).thenReturn(client);
		when(jiraProcessorConfig.getStartDate()).thenReturn("2020-01-01T00:00:00.000000");
		when(jiraIssueClientFactory.getJiraIssueDataClient(any())).thenReturn(scrumJiraIssueClient);

		when(jiraProcessorConfig.getMinsToReduce()).thenReturn(30L);
		when(jiraProcessorConfig.getPageSize()).thenReturn(4);
		when(client.getProcessorSearchClient()).thenReturn(searchRestClient);
		when(searchRestClient.searchJql(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anySet()))
				.thenReturn(promisedRs);
		SearchResult sr = Mockito.mock(SearchResult.class);
		when(promisedRs.claim()).thenReturn(sr);
		when(sr.getIssues()).thenReturn(issueIterable);
		when(jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA)).thenReturn(jiraProcessor);
		when(jiraProcessor.getId()).thenReturn(new ObjectId("5e16c126e4b098db673cc372"));
		when(jiraProcessorConfig.getEstimationCriteria()).thenReturn("StoryPoints");
		when(jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(any(), any()))
				.thenReturn(new ArrayList<JiraIssue>());
		when(jiraIssueCustomHistoryRepository.findByStoryIDAndBasicProjectConfigId(any(), any()))
				.thenReturn(new ArrayList<JiraIssueCustomHistory>());
		when(user.getName()).thenReturn("First LastName");
		when(user.getDisplayName()).thenReturn("First LastName");
		issueMockData();
		when(accountHierarchyRepository.findByLabelNameAndBasicProjectConfigId("Project",
				scrumProjectList.get(0).getId())).thenReturn(Arrays.asList(accountHierarchy));
		ProjectRestClient projectRestClient = mock(ProjectRestClient.class);
		when(client.getProjectClient()).thenReturn(projectRestClient);

		MetadataRestClient metadataRestClient = mock(MetadataRestClient.class);
		when(client.getMetadataClient()).thenReturn(metadataRestClient);

		Field field1 = new Field("Story Points", "customfield_20803", FieldType.JIRA, true, true, true, null);
		Field field2 = new Field("Sprint", "customfield_12700", FieldType.JIRA, true, true, true, null);
		Field field3 = new Field("Root Cause", "customfield_19121", FieldType.JIRA, true, true, true, null);
		Field field4 = new Field("Tech Debt", "customfield_59601", FieldType.JIRA, true, true, true, null);
		Field field5 = new Field("UAT", "UAT", FieldType.JIRA, true, true, true, null);
		List<Field> fields = Arrays.asList(field1, field2, field3, field4, field5);

		Iterable<Field> fieldItr = fields;
		when(metadataRestClient.getFields()).thenReturn(metaDataFieldPromise);
		when(metaDataFieldPromise.claim()).thenReturn(fieldItr);

		IssueType issueType1 = new IssueType(new URI("self"), 1l, "Story", false, "desc", new URI("iconURI"));
		IssueType issueType2 = new IssueType(new URI("self"), 1l, "Enabler Story", false, "desc", new URI("iconURI"));
		IssueType issueType3 = new IssueType(new URI("self"), 1l, "Tech Story", false, "desc", new URI("iconURI"));
		IssueType issueType4 = new IssueType(new URI("self"), 1l, "Change request", false, "desc", new URI("iconURI"));
		IssueType issueType5 = new IssueType(new URI("self"), 1l, "Defect", false, "desc", new URI("iconURI"));
		IssueType issueType6 = new IssueType(new URI("self"), 1l, "Epic", false, "desc", new URI("iconURI"));
		IssueType issueType7 = new IssueType(new URI("self"), 1l, "UAT Defect", false, "desc", new URI("iconURI"));
		List<IssueType> issueTypes = Arrays.asList(issueType1, issueType2, issueType3, issueType4, issueType5,
				issueType6, issueType7);

		Iterable<IssueType> issueTypeItr = issueTypes;
		when(metadataRestClient.getIssueTypes()).thenReturn(metaDataIssueTypePromise);
		when(metaDataIssueTypePromise.claim()).thenReturn(issueTypeItr);

		Status status1 = new Status(new URI("self"), 1l, "Ready for Sprint Planning", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		Status status2 = new Status(new URI("self"), 1l, "Closed", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		Status status3 = new Status(new URI("self"), 1l, "Implementing", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		Status status4 = new Status(new URI("self"), 1l, "In Testing", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		List<Status> statuses = Arrays.asList(status1, status2, status3, status4);
		Iterable<Status> statusItr = statuses;
		when(metadataRestClient.getStatuses()).thenReturn(metaDataStatusPromise);
		when(metaDataStatusPromise.claim()).thenReturn(statusItr);

		MetadataIdentifier metadataIdentifier = createMetaDataIdentifier();
		when(onlineAdapter.getUserTimeZone(any())).thenReturn("Indian/Maldives");
		try {
			PowerMockito.whenNew(OnlineAdapter.class).withAnyArguments().thenReturn(onlineAdapter);
		} catch (Exception e) {

		}

		when(metadataIdentifierRepository.findByTemplateCodeAndToolAndIsKanban(any(), any(), any()))
				.thenReturn(metadataIdentifier);

		when(projectRestClient.getProject("TEST")).thenReturn(projectPromise);
		when(toolRepository.findByToolNameAndBasicProjectConfigId(any(), any())).thenReturn(projectToolConfigList);
		when(connectionRepository.findById(any())).thenReturn(conn);
		when(subProjectRepository.findBybasicProjectConfigIdIn(any())).thenReturn(subProjectsList);
		Project project = mock(Project.class);
		when(projectPromise.claim()).thenReturn(project);
		when(jiraProcessorConfig.isFetchMetadata()).thenReturn(Boolean.TRUE);
		when(jiraProcessorConfig.getJiraServerGetUserApi()).thenReturn("user/search?username=");
		when(boardMetadataRepository.findByProjectBasicConfigId(any())).thenReturn(null);
		onlineDataProcessor.validateAndCollectIssues(scrumProjectList);
	}

	@Test
	public void validateAndCollectIssuesDOJOKanban() throws URISyntaxException {
		List<ProjectToolConfig> projectToolConfigList = new ArrayList<>();
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectToolConfig.setConnectionId(new ObjectId("5b719d06a500d00814bfb2b9"));
		projectToolConfig.setToolName(ProcessorConstants.JIRA);
		projectToolConfigList.add(projectToolConfig);
		Optional<Connection> conn = Optional.of(new Connection());
		conn.get().setOffline(Boolean.FALSE);

		List<Subproject> subProjectsList = new ArrayList<>();
		Subproject subproject = new Subproject();
		subproject.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		subproject.setToolConfigId(new ObjectId("5b674d58f47cae8935b1b26f"));
		subProjectsList.add(subproject);

		prepareIssuesData();
		prepareAccountHierarchy();
		JiraInfo jiraInfo = JiraInfo.builder()
				.jiraConfigBaseUrl(projectConfFieldMapping.getJira().getConnection().get().getBaseUrl())
				.username(projectConfFieldMapping.getJira().getConnection().get().getUsername())
				.password(projectConfFieldMapping.getJira().getConnection().get().getPassword())
				.jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build();

		JiraInfo jiraInfoOAuth = JiraInfo.builder().jiraConfigBaseUrl(jiraOAuthProperties.getJiraBaseURL())
				.jiraConfigAccessToken(jiraOAuthProperties.getAccessToken())
				.username(projectConfFieldMapping2.getJira().getConnection().get().getUsername())
				.password(projectConfFieldMapping2.getJira().getConnection().get().getPassword())
				.jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build();
		when(jiraProcessorConfig.getThreadPoolSize()).thenReturn(3);
		when(jiraRestClientFactory.getJiraClient(jiraInfo)).thenReturn(client);
		when(jiraRestClientFactory.getJiraClient(jiraInfoOAuth)).thenReturn(client);
		when(jiraProcessorConfig.getStartDate()).thenReturn("2020-01-01T00:00:00.000000");
		when(jiraIssueClientFactory.getJiraIssueDataClient(any())).thenReturn(scrumJiraIssueClient);

		when(jiraProcessorConfig.getMinsToReduce()).thenReturn(30L);
		when(jiraProcessorConfig.getPageSize()).thenReturn(4);
		when(client.getProcessorSearchClient()).thenReturn(searchRestClient);
		when(searchRestClient.searchJql(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anySet()))
				.thenReturn(promisedRs);
		SearchResult sr = Mockito.mock(SearchResult.class);
		when(promisedRs.claim()).thenReturn(sr);
		when(sr.getIssues()).thenReturn(issueIterable);
		when(jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA)).thenReturn(jiraProcessor);
		when(jiraProcessor.getId()).thenReturn(new ObjectId("5e16c126e4b098db673cc372"));
		when(jiraProcessorConfig.getEstimationCriteria()).thenReturn("StoryPoints");
		when(jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(any(), any()))
				.thenReturn(new ArrayList<JiraIssue>());
		when(jiraIssueCustomHistoryRepository.findByStoryIDAndBasicProjectConfigId(any(), any()))
				.thenReturn(new ArrayList<JiraIssueCustomHistory>());
		when(user.getName()).thenReturn("First LastName");
		when(user.getDisplayName()).thenReturn("First LastName");
		issueMockData();
		when(accountHierarchyRepository.findByLabelNameAndBasicProjectConfigId("Project",
				scrumProjectList.get(0).getId())).thenReturn(Arrays.asList(accountHierarchy));
		ProjectRestClient projectRestClient = mock(ProjectRestClient.class);
		when(client.getProjectClient()).thenReturn(projectRestClient);

		MetadataRestClient metadataRestClient = mock(MetadataRestClient.class);
		when(client.getMetadataClient()).thenReturn(metadataRestClient);

		Field field1 = new Field("Story Points", "customfield_20803", FieldType.JIRA, true, true, true, null);
		Field field2 = new Field("Sprint", "customfield_12700", FieldType.JIRA, true, true, true, null);
		Field field3 = new Field("Root Cause", "customfield_19121", FieldType.JIRA, true, true, true, null);
		Field field4 = new Field("Tech Debt", "customfield_59601", FieldType.JIRA, true, true, true, null);
		Field field5 = new Field("UAT", "UAT", FieldType.JIRA, true, true, true, null);
		List<Field> fields = Arrays.asList(field1, field2, field3, field4, field5);

		Iterable<Field> fieldItr = fields;
		when(metadataRestClient.getFields()).thenReturn(metaDataFieldPromise);
		when(metaDataFieldPromise.claim()).thenReturn(fieldItr);

		IssueType issueType1 = new IssueType(new URI("self"), 1l, "Story", false, "desc", new URI("iconURI"));
		IssueType issueType2 = new IssueType(new URI("self"), 1l, "Enabler Story", false, "desc", new URI("iconURI"));
		IssueType issueType3 = new IssueType(new URI("self"), 1l, "Tech Story", false, "desc", new URI("iconURI"));
		IssueType issueType4 = new IssueType(new URI("self"), 1l, "Change request", false, "desc", new URI("iconURI"));
		IssueType issueType5 = new IssueType(new URI("self"), 1l, "Defect", false, "desc", new URI("iconURI"));
		IssueType issueType6 = new IssueType(new URI("self"), 1l, "Epic", false, "desc", new URI("iconURI"));
		IssueType issueType7 = new IssueType(new URI("self"), 1l, "UAT Defect", false, "desc", new URI("iconURI"));
		List<IssueType> issueTypes = Arrays.asList(issueType1, issueType2, issueType3, issueType4, issueType5,
				issueType6, issueType7);

		Iterable<IssueType> issueTypeItr = issueTypes;
		when(metadataRestClient.getIssueTypes()).thenReturn(metaDataIssueTypePromise);
		when(metaDataIssueTypePromise.claim()).thenReturn(issueTypeItr);

		Status status1 = new Status(new URI("self"), 1l, "Ready for Sprint Planning", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		Status status2 = new Status(new URI("self"), 1l, "Closed", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		Status status3 = new Status(new URI("self"), 1l, "Implementing", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		Status status4 = new Status(new URI("self"), 1l, "In Testing", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		List<Status> statuses = Arrays.asList(status1, status2, status3, status4);
		Iterable<Status> statusItr = statuses;
		when(metadataRestClient.getStatuses()).thenReturn(metaDataStatusPromise);
		when(metaDataStatusPromise.claim()).thenReturn(statusItr);

		MetadataIdentifier metadataIdentifier = createKanbanMetaDataIdentifier();
		when(onlineAdapter.getUserTimeZone(any())).thenReturn("Indian/Maldives");
		try {
			PowerMockito.whenNew(OnlineAdapter.class).withAnyArguments().thenReturn(onlineAdapter);
		} catch (Exception e) {

		}

		when(metadataIdentifierRepository.findByTemplateCodeAndToolAndIsKanban(any(), any(), any()))
				.thenReturn(metadataIdentifier);

		when(projectRestClient.getProject("TEST")).thenReturn(projectPromise);
		when(toolRepository.findByToolNameAndBasicProjectConfigId(any(), any())).thenReturn(projectToolConfigList);
		when(connectionRepository.findById(any())).thenReturn(conn);
		when(subProjectRepository.findBybasicProjectConfigIdIn(any())).thenReturn(subProjectsList);
		Project project = mock(Project.class);
		when(projectPromise.claim()).thenReturn(project);
		when(jiraProcessorConfig.isFetchMetadata()).thenReturn(Boolean.TRUE);
		when(jiraProcessorConfig.getJiraServerGetUserApi()).thenReturn("user/search?username=");
		when(boardMetadataRepository.findByProjectBasicConfigId(any())).thenReturn(null);
		onlineDataProcessor.validateAndCollectIssues(scrumProjectList);
	}

	@Test
	public void validateAndCollectIssuesScrumWithBearerToken() throws URISyntaxException {
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

		List<Subproject> subProjectsList = new ArrayList<>();
		Subproject subproject = new Subproject();
		subproject.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		subproject.setToolConfigId(new ObjectId("5b674d58f47cae8935b1b26f"));
		subProjectsList.add(subproject);

		prepareIssuesData();
		prepareAccountHierarchy();
		JiraInfo jiraInfo = JiraInfo.builder()
				.jiraConfigBaseUrl(projectConfFieldMapping.getJira().getConnection().get().getBaseUrl())
				.username(projectConfFieldMapping.getJira().getConnection().get().getUsername())
				.password(projectConfFieldMapping.getJira().getConnection().get().getPassword())
				.jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build();

		JiraInfo jiraInfoOAuth = JiraInfo.builder().jiraConfigBaseUrl(jiraOAuthProperties.getJiraBaseURL())
				.jiraConfigAccessToken(jiraOAuthProperties.getAccessToken())
				.username(projectConfFieldMapping2.getJira().getConnection().get().getUsername())
				.password(projectConfFieldMapping2.getJira().getConnection().get().getPassword())
				.jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build();
		when(fieldMappingRepository.findAll()).thenReturn(fieldMappingList);
		when(jiraProcessorConfig.getThreadPoolSize()).thenReturn(3);
		when(jiraRestClientFactory.getJiraClient(jiraInfo)).thenReturn(client);
		when(jiraRestClientFactory.getJiraClient(jiraInfoOAuth)).thenReturn(client);
		when(jiraProcessorConfig.getStartDate()).thenReturn("2019-01-07T00:00:00.0000000");
		when(jiraProcessorConfig.getAesEncryptionKey()).thenReturn("708C150A5363290AAE3F579BF3746AD5");
		when(jiraIssueClientFactory.getJiraIssueDataClient(any())).thenReturn(scrumJiraIssueClient);

		when(jiraProcessorConfig.getMinsToReduce()).thenReturn(30L);
		when(jiraProcessorConfig.getPageSize()).thenReturn(4);
		when(client.getProcessorSearchClient()).thenReturn(searchRestClient);
		when(searchRestClient.searchJql(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anySet()))
				.thenReturn(promisedRs);
		SearchResult sr = Mockito.mock(SearchResult.class);
		when(promisedRs.claim()).thenReturn(sr);
		when(sr.getIssues()).thenReturn(issueIterable);
		when(jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA)).thenReturn(jiraProcessor);
		when(jiraProcessor.getId()).thenReturn(new ObjectId("5e16c126e4b098db673cc372"));
		when(jiraProcessorConfig.getEstimationCriteria()).thenReturn("StoryPoints");
		when(jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(any(), any()))
				.thenReturn(new ArrayList<JiraIssue>());
		when(jiraIssueCustomHistoryRepository.findByStoryIDAndBasicProjectConfigId(any(), any()))
				.thenReturn(new ArrayList<JiraIssueCustomHistory>());
		when(user.getName()).thenReturn("First LastName");
		when(user.getDisplayName()).thenReturn("First LastName");
		issueMockData();
		when(accountHierarchyRepository.findByLabelNameAndBasicProjectConfigId("Project",
				scrumProjectList.get(0).getId())).thenReturn(Arrays.asList(accountHierarchy));
		ProjectRestClient projectRestClient = mock(ProjectRestClient.class);
		when(client.getProjectClient()).thenReturn(projectRestClient);
		when(projectRestClient.getProject("TEST")).thenReturn(projectPromise);
		when(toolRepository.findByToolNameAndBasicProjectConfigId(any(), any())).thenReturn(projectToolConfigList);
		when(connectionRepository.findById(any())).thenReturn(conn);
		when(subProjectRepository.findBybasicProjectConfigIdIn(any())).thenReturn(subProjectsList);
		Project project = mock(Project.class);
		when(projectPromise.claim()).thenReturn(project);
		try {
			when(jiraOAuthClient.getAccessToken(any(), any())).thenReturn("token");
		} catch (IOException e) {

		}
		onlineDataProcessor.validateAndCollectIssues(scrumProjectList);
	}

	@Test
	public void validateAndCollectIssuesKanban() throws URISyntaxException {
		List<ProjectToolConfig> projectToolConfigList = new ArrayList<>();
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectToolConfig.setConnectionId(new ObjectId("5b719d06a500d00814bfb2b9"));
		projectToolConfig.setToolName(ProcessorConstants.JIRA);
		projectToolConfigList.add(projectToolConfig);
		Optional<Connection> conn = Optional.of(new Connection());
		conn.get().setOffline(Boolean.TRUE);

		List<Subproject> subProjectsList = new ArrayList<>();
		Subproject subproject = new Subproject();
		subproject.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		subproject.setToolConfigId(new ObjectId("5b674d58f47cae8935b1b26f"));
		subProjectsList.add(subproject);

		prepareKanbanIssuesData();
		JiraInfo jiraInfo = JiraInfo.builder()
				.jiraConfigBaseUrl(projectConfFieldMapping2.getJira().getConnection().get().getBaseUrl())
				.username(projectConfFieldMapping2.getJira().getConnection().get().getUsername())
				.password(projectConfFieldMapping2.getJira().getConnection().get().getPassword())
				.jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build();

		JiraInfo jiraInfoOAuth = JiraInfo.builder().jiraConfigBaseUrl(jiraOAuthProperties.getJiraBaseURL())
				.jiraConfigAccessToken(jiraOAuthProperties.getAccessToken())
				.username(projectConfFieldMapping2.getJira().getConnection().get().getUsername())
				.password(projectConfFieldMapping2.getJira().getConnection().get().getPassword())
				.jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build();
		when(fieldMappingRepository.findAll()).thenReturn(fieldMappingList);
		when(jiraProcessorConfig.getThreadPoolSize()).thenReturn(3);
		when(jiraRestClientFactory.getJiraClient(jiraInfo)).thenReturn(client);
		when(jiraRestClientFactory.getJiraClient(jiraInfoOAuth)).thenReturn(client);
		when(jiraProcessorConfig.getStartDate()).thenReturn("2019-01-07T00:00:00.0000000");

		when(jiraIssueClientFactory.getJiraIssueDataClient(projectConfFieldMapping2)).thenReturn(kanbanJiraIssueClient);

		when(jiraProcessorConfig.getMinsToReduce()).thenReturn(30L);
		when(jiraProcessorConfig.getPageSize()).thenReturn(4);
		when(client.getProcessorSearchClient()).thenReturn(searchRestClient);
		when(jiraProcessorConfig.getEstimationCriteria()).thenReturn("StoryPoints");
		when(searchRestClient.searchJql(anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anySet()))
				.thenReturn(promisedRs);
		when(toolRepository.findByToolNameAndBasicProjectConfigId(any(), any())).thenReturn(projectToolConfigList);
		when(connectionRepository.findById(any())).thenReturn(conn);
		when(subProjectRepository.findBybasicProjectConfigIdIn(any())).thenReturn(subProjectsList);
		SearchResult sr = Mockito.mock(SearchResult.class);
		when(promisedRs.claim()).thenReturn(sr);
		when(sr.getIssues()).thenReturn(issueIterableKanban);
		when(jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA)).thenReturn(jiraProcessor);
		kanbanIssueMockData();
		onlineDataProcessor.validateAndCollectIssues(kanbanProjectlist);
	}

	private void kanbanIssueMockData() throws URISyntaxException {

		when(kanbanIssues.get(0).getId()).thenReturn(Long.parseLong("7171682"));
		when(kanbanIssues.get(0).getKey()).thenReturn("TEST-1234");
		Map<String, String> map = new HashMap<>();
		map.put("customfield_12121", "Client Testing (UAT)");
		map.put("self", "https://test.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Component");
		map.put("id", "20810");
		JSONObject value = new JSONObject(map);
		IssueField issueField = new IssueField("20810", "Component", null, value);
		when(kanbanIssues.get(0).getField("customfield_20810")).thenReturn(issueField);
		IssueType issueType = Mockito.mock(IssueType.class);

		when(kanbanIssues.get(0).getFields()).thenReturn(createIssueFieldIterable(false));
		when(kanbanIssues.get(0).getIssueType()).thenReturn(issueType);
		when(issueType.getName()).thenReturn("Support Request");
		Status status = Mockito.mock(Status.class);
		when(kanbanIssues.get(0).getStatus()).thenReturn(status);
		Set<String> labels = new HashSet<>();
		labels.add("segregationLabel");
		labels.add("sub-project");
		labels.add("BuildNo");
		when(kanbanIssues.get(0).getLabels()).thenReturn(labels);
		when(status.getName()).thenReturn("Open");
		when(kanbanIssues.get(0).getCreationDate()).thenReturn(DateTime.now());
		when(kanbanIssues.get(0).getUpdateDate()).thenReturn(DateTime.now());
		BasicProject project = Mockito.mock(BasicProject.class);
		when(kanbanIssues.get(0).getProject()).thenReturn(project);
		when(project.getKey()).thenReturn("TEST");
		when(kanbanIssues.get(0).getChangelog()).thenReturn(changelogGroup);
		when(changelogGroup.iterator()).thenReturn(createChangeLogGroupData());

		// 2nd KanbanIssue
		when(kanbanIssues.get(1).getId()).thenReturn(Long.parseLong("4328643"));
		when(kanbanIssues.get(1).getKey()).thenReturn("TEST-9876");
		when(kanbanIssueHistoryRepo.findByStoryIDAndBasicProjectConfigId(any(), any()))
				.thenReturn(createKanbanJiraIssueHistoryData());
		User user = mock(User.class);
		when(kanbanIssues.get(1).getAssignee()).thenReturn(user);
		when(user.getName()).thenReturn("User Name");
		when(user.getDisplayName()).thenReturn("Display Name");
		map = new HashMap<>();
		map.put("customfield_12121", "Client Testing (UAT)");
		map.put("self", "https://test.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Component");
		map.put("id", "20810");
		value = new JSONObject(map);
		issueField = new IssueField("20810", "Component", null, value);
		when(kanbanIssues.get(1).getField("customfield_20810")).thenReturn(issueField);
		issueType = Mockito.mock(IssueType.class);
		when(kanbanIssues.get(1).getChangelog()).thenReturn(changelogGroup2);
		when(changelogGroup2.iterator()).thenReturn(createChangeLogGroupData());
		when(kanbanIssues.get(1).getFields()).thenReturn(createIssueFieldIterable(false));
		when(kanbanIssues.get(1).getIssueType()).thenReturn(issueType);
		labels = new HashSet<>();
		labels.add("segregationLabel");
		labels.add("sub-project");
		when(kanbanIssues.get(1).getLabels()).thenReturn(labels);
		when(issueType.getName()).thenReturn("Test Case");
		status = Mockito.mock(Status.class);
		when(kanbanIssues.get(1).getStatus()).thenReturn(status);
		when(status.getName()).thenReturn("In Progress");
		when(project.getKey()).thenReturn("TEST");

		when(kanbanIssues.get(1).getCreationDate()).thenReturn(DateTime.now());
		when(kanbanIssues.get(1).getUpdateDate()).thenReturn(DateTime.now());
		project = Mockito.mock(BasicProject.class);
		when(kanbanIssues.get(1).getProject()).thenReturn(project);

		// 3rd KanbanIssue
		when(kanbanIssues.get(2).getId()).thenReturn(Long.parseLong("4328643"));
		when(kanbanIssues.get(2).getKey()).thenReturn("TEST-9876");
		when(kanbanIssueHistoryRepo.findByStoryIDAndBasicProjectConfigId(any(), any()))
				.thenReturn(createKanbanJiraIssueHistoryData());
		user = mock(User.class);
		when(kanbanIssues.get(2).getAssignee()).thenReturn(user);
		when(user.getName()).thenReturn("User Name");
		when(user.getDisplayName()).thenReturn("Display Name");
		map = new HashMap<>();
		map.put("customfield_12121", "Client Testing (UAT)");
		map.put("self", "https://test.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Component");
		map.put("id", "20810");
		value = new JSONObject(map);
		issueField = new IssueField("20810", "Component", null, value);
		when(kanbanIssues.get(2).getField("customfield_20810")).thenReturn(issueField);
		issueType = Mockito.mock(IssueType.class);
		when(kanbanIssues.get(2).getChangelog()).thenReturn(changelogGroup2);
		when(changelogGroup2.iterator()).thenReturn(createChangeLogGroupData());
		when(kanbanIssues.get(2).getFields()).thenReturn(createIssueFieldIterable(false));
		when(kanbanIssues.get(2).getIssueType()).thenReturn(issueType);
		labels = new HashSet<>();
		labels.add("noLabel");
		labels.add("sub-project");
		when(kanbanIssues.get(2).getLabels()).thenReturn(labels);
		when(issueType.getName()).thenReturn("Task");
		status = Mockito.mock(Status.class);
		when(kanbanIssues.get(2).getStatus()).thenReturn(status);
		when(status.getName()).thenReturn("In Progress");
		when(project.getKey()).thenReturn("TEST");

		when(kanbanIssues.get(2).getCreationDate()).thenReturn(DateTime.now());
		when(kanbanIssues.get(2).getUpdateDate()).thenReturn(DateTime.now());
		project = Mockito.mock(BasicProject.class);
		when(kanbanIssues.get(2).getProject()).thenReturn(project);

		// 4rd KanbanIssue
		when(kanbanIssues.get(3).getId()).thenReturn(Long.parseLong("4328643"));
		when(kanbanIssues.get(3).getKey()).thenReturn("TEST-9876");
		when(kanbanIssueHistoryRepo.findByStoryIDAndBasicProjectConfigId(any(), any()))
				.thenReturn(createKanbanJiraIssueHistoryData());
		user = mock(User.class);
		when(kanbanIssues.get(3).getAssignee()).thenReturn(user);
		when(user.getName()).thenReturn("User Name");
		when(user.getDisplayName()).thenReturn("Display Name");
		map = new HashMap<>();
		map.put("customfield_12121", "Client Testing (UAT)");
		map.put("self", "https://test.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Component");
		map.put("id", "20810");
		value = new JSONObject(map);
		issueField = new IssueField("20810", "Component", null, value);
		when(kanbanIssues.get(3).getField("customfield_20810")).thenReturn(issueField);
		issueType = Mockito.mock(IssueType.class);
		when(kanbanIssues.get(3).getChangelog()).thenReturn(changelogGroup2);
		when(changelogGroup2.iterator()).thenReturn(createChangeLogGroupData());
		when(kanbanIssues.get(3).getFields()).thenReturn(createIssueFieldIterable(false));
		when(kanbanIssues.get(3).getIssueType()).thenReturn(issueType);
		labels = new HashSet<>();
		labels.add("noLabel");
		labels.add("sub-project");
		when(kanbanIssues.get(3).getLabels()).thenReturn(labels);
		when(issueType.getName()).thenReturn("Defect");
		status = Mockito.mock(Status.class);
		when(kanbanIssues.get(3).getStatus()).thenReturn(status);
		when(status.getName()).thenReturn("Ready for Testing");
		when(project.getKey()).thenReturn("TEST");

		when(kanbanIssues.get(3).getCreationDate()).thenReturn(DateTime.now());
		when(kanbanIssues.get(3).getUpdateDate()).thenReturn(DateTime.now());
		project = Mockito.mock(BasicProject.class);
		when(kanbanIssues.get(3).getProject()).thenReturn(project);

	}

	private List<KanbanIssueCustomHistory> createKanbanJiraIssueHistoryData() {
		KanbanIssueCustomHistory jiraIssueCustomHistory = new KanbanIssueCustomHistory();
		jiraIssueCustomHistory.setStoryID("TEST-9876");
		jiraIssueCustomHistory.setStoryType("Incident");
		jiraIssueCustomHistory.setProjectKey("TEST");
		jiraIssueCustomHistory.setProjectID("TEST Project Internal");

		List<KanbanIssueHistory> issueSprintList = new ArrayList<>();
		KanbanIssueHistory issueHistory = new KanbanIssueHistory();
		issueHistory.setActivityDate(DateTime.now().toString());
		issueHistory.setStatus("In Development");
		issueSprintList.add(issueHistory);

		jiraIssueCustomHistory.setHistoryDetails(issueSprintList);
		List<KanbanIssueCustomHistory> issueList = new ArrayList<>();
		issueList.add(jiraIssueCustomHistory);
		return issueList;
	}

	private void issueMockData() throws URISyntaxException {
		when(issues.get(0).getId()).thenReturn(Long.parseLong("7171682"));
		IssueType issueType = Mockito.mock(IssueType.class);

		when(issues.get(0).getFields()).thenReturn(createIssueFieldIterable(false));
		when(issues.get(0).getIssueType()).thenReturn(issueType);
		when(issueType.getName()).thenReturn("Defect");
		Status status = Mockito.mock(Status.class);
		when(issues.get(0).getStatus()).thenReturn(status);
		when(status.getName()).thenReturn("Open");
		User user = mock(User.class);
		when(issues.get(0).getAssignee()).thenReturn(user);
		when(user.getName()).thenReturn("User Name");
		when(user.getDisplayName()).thenReturn("Display Name");
		when(issues.get(0).getIssueLinks()).thenReturn(createIssueLinkData());
		when(issues.get(0).getStatus()).thenReturn(status);
		when(status.getName()).thenReturn("Open");
		when(issues.get(0).getCreationDate()).thenReturn(DateTime.now());
		when(issues.get(0).getUpdateDate()).thenReturn(DateTime.now());
		BasicProject project = Mockito.mock(BasicProject.class);
		when(issues.get(0).getProject()).thenReturn(project);
		when(project.getKey()).thenReturn("KEY-12");
		Set<String> labels = new HashSet<>();
		labels.add("segregationLabel");
		labels.add("squad");
		labels.add("BuildNo");
		when(issues.get(0).getLabels()).thenReturn(labels);

		Map<String, String> map = new HashMap<>();
		map.put("customfield_12121", "Client Testing (UAT)");
		map.put("self", "https://test.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Component");
		map.put("id", "20810");
		JSONObject value = new JSONObject(map);
		IssueField issueField = new IssueField("20810", "Component", null, value);
		when(issues.get(0).getField("customfield_20810")).thenReturn(issueField);

		when(issues.get(0).getChangelog()).thenReturn(changelogGroup);
		when(changelogGroup.iterator()).thenReturn(createChangeLogGroupData());

		when(issues.get(1).getId()).thenReturn(Long.parseLong("4328643"));
		issueType = Mockito.mock(IssueType.class);
		when(issues.get(1).getFields()).thenReturn(createIssueFieldIterable(true));
		when(issues.get(1).getIssueType()).thenReturn(issueType);
		when(issueType.getName()).thenReturn("Story");
		status = Mockito.mock(Status.class);
		when(issues.get(1).getStatus()).thenReturn(status);
		when(status.getName()).thenReturn("Open");
		when(issues.get(1).getIssueLinks()).thenReturn(createIssueLinkData());
		when(issues.get(1).getStatus()).thenReturn(status);

		when(issues.get(1).getKey()).thenReturn("TEST-7906");

		when(jiraIssueCustomHistoryRepository.findByStoryIDAndBasicProjectConfigId(any(), any()))
				.thenReturn(createJiraIssueHistoryData());
		when(status.getName()).thenReturn("Open");
		when(issues.get(1).getCreationDate()).thenReturn(DateTime.now());
		when(issues.get(1).getUpdateDate()).thenReturn(DateTime.now());
		project = Mockito.mock(BasicProject.class);
		when(issues.get(1).getProject()).thenReturn(project);
		when(project.getKey()).thenReturn("KEY-12");
		map = new HashMap<>();
		map.put("customfield_12121", "Client Testing (UAT)");
		map.put("self", "https://test.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Component");
		map.put("id", "20810");
		value = new JSONObject(map);
		issueField = new IssueField("customfield_20810", "Component", null, value);
		when(issues.get(1).getField("customfield_20810")).thenReturn(issueField);

		when(issues.get(1).getChangelog()).thenReturn(changelogGroup2);
		when(changelogGroup2.iterator()).thenReturn(createChangeLogGroupData());
		issueType = Mockito.mock(IssueType.class);
		when(issues.get(2).getFields()).thenReturn(createIssueFieldIterable(false));
		when(issues.get(2).getIssueType()).thenReturn(issueType);
		user = mock(User.class);
		when(issues.get(2).getAssignee()).thenReturn(user);
		when(user.getName()).thenReturn("User Name");
		when(user.getDisplayName()).thenReturn("Display Name");
		when(issueType.getName()).thenReturn("Tech Story");
		status = Mockito.mock(Status.class);
		when(issues.get(2).getStatus()).thenReturn(status);
		when(status.getName()).thenReturn("Open");
		when(issues.get(2).getIssueLinks()).thenReturn(createIssueLinkData());
		when(issues.get(2).getStatus()).thenReturn(status);
		when(status.getName()).thenReturn("Open");
		when(issues.get(2).getCreationDate()).thenReturn(DateTime.now());
		when(issues.get(2).getUpdateDate()).thenReturn(DateTime.now());
		project = Mockito.mock(BasicProject.class);
		when(issues.get(2).getProject()).thenReturn(project);
		when(project.getKey()).thenReturn("KEY-12");
		map = new HashMap<>();
		map.put("customfield_12121", "Client Testing (UAT)");
		map.put("self", "https://test.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Component");
		map.put("id", "20810");
		value = new JSONObject(map);
		issueField = new IssueField("20810", "Component", null, value);
		when(issues.get(2).getField("customfield_20810")).thenReturn(issueField);

		when(issues.get(2).getChangelog()).thenReturn(changelogGroup);
		when(changelogGroup.iterator()).thenReturn(createChangeLogGroupData());

	}

	private List<JiraIssueCustomHistory> createJiraIssueHistoryData() {
		JiraIssueCustomHistory jiraIssueCustomHistory = new JiraIssueCustomHistory();
		jiraIssueCustomHistory.setStoryID("TEST-7906");
		jiraIssueCustomHistory.setStoryType("Defect");
		jiraIssueCustomHistory.setProjectKey("TEST");
		jiraIssueCustomHistory.setProjectID("TEST Project Internal");

		List<JiraHistoryChangeLog> issueSprintList = new ArrayList<>();
		JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
		jiraHistoryChangeLog.setChangedFrom("");
		jiraHistoryChangeLog.setUpdatedOn(LocalDateTime.now());
		jiraHistoryChangeLog.setChangedTo("In Development");
		issueSprintList.add(jiraHistoryChangeLog);

		jiraIssueCustomHistory.setStatusUpdationLog(issueSprintList);
		List<JiraIssueCustomHistory> issueList = new ArrayList<>();
		issueList.add(jiraIssueCustomHistory);
		return issueList;
	}

	private Iterator<ChangelogGroup> createChangeLogGroupData() throws URISyntaxException {
		List<ChangelogGroup> groupList = new ArrayList<>();
		URI uri = new URI("https://test.com/jira/rest/api/2/issue/12344");
		BasicUser user = new BasicUser(uri, "firstName", "firstName");
		ChangelogGroup changelogGroup = new ChangelogGroup(user, DateTime.now(), createChangeLogItemData());
		groupList.add(changelogGroup);
		return groupList.iterator();
	}

	private Iterable<ChangelogItem> createChangeLogItemData() {
		List<ChangelogItem> logItemList = new ArrayList<>();
		ChangelogItem changelogItem = new ChangelogItem(FieldType.JIRA, "assignee", "FromUser", "FromUser", "ToUser",
				"ToUser");
		logItemList.add(changelogItem);

		changelogItem = new ChangelogItem(FieldType.JIRA, JiraConstants.TEST_AUTOMATED, "FromUser", "FromUser", "Yes",
				"ToUser");
		logItemList.add(changelogItem);

		changelogItem = new ChangelogItem(FieldType.JIRA, "Sprint", null, null, "23356", "TEST | 06 Jan - 19 Jan");
		logItemList.add(changelogItem);
		changelogItem = new ChangelogItem(FieldType.JIRA, "status", "1", "Open", "10139", "In Analysis");
		logItemList.add(changelogItem);
		Iterable<ChangelogItem> iterable = new Iterable<ChangelogItem>() {
			@Override
			public Iterator<ChangelogItem> iterator() {
				return logItemList.iterator();
			}
		};
		return iterable;
	}

	private Iterable<IssueLink> createIssueLinkData() throws URISyntaxException {
		List<IssueLink> issueLinkList = new ArrayList<>();
		URI uri = new URI("https://test.com/jira/rest/api/2/issue/12344");
		IssueLinkType linkType = new IssueLinkType("Blocks", "blocks", IssueLinkType.Direction.OUTBOUND);
		IssueLink issueLink = new IssueLink("IssueKey", uri, linkType);
		issueLinkList.add(issueLink);

		Iterable<IssueLink> issueLinkIterable = new Iterable<IssueLink>() {
			@Override
			public Iterator<IssueLink> iterator() {
				return issueLinkList.iterator();
			}
		};
		return issueLinkIterable;
	}

	private Iterable<IssueField> createIssueFieldIterable(boolean sprintStatus) {
		List<IssueField> issueFieldList = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		map.put("self", "https://test.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Client Testing (UAT)");
		map.put("id", "12121");
		IssueField issueField = new IssueField("customfield_12121", "UAT", null, new JSONObject(map));
		issueFieldList.add(issueField);
		JSONArray array = null;
		if (sprintStatus) {
			List<Object> sprintList = new ArrayList<>();
			String sprint = "com.atlassian.greenhopper.service.sprint.Sprint@6fc7072e[id=23356,rapidViewId=11649,state=CLOSED,name=TEST | 06 Jan - 19 Jan,startDate=2020-01-06T11:38:31.937Z,endDate=2020-01-19T11:38:00.000Z,completeDate=2020-01-20T11:15:21.528Z,sequence=22778,goal=]";
			sprintList.add(sprint);
			array = new JSONArray(sprintList);
		}
		issueField = new IssueField("customfield_12700", "SprintprocessSprintData", null, array);
		issueFieldList.add(issueField);

		List<String> list = new ArrayList<>();
		list.add("BrandName-12");
		issueField = new IssueField("customfield_48531", "Bran", null, new JSONArray(list));
		issueFieldList.add(issueField);

		issueField = new IssueField("customfield_56789", "StoryPoints", null, Integer.parseInt("5"));
		issueFieldList.add(issueField);

		map = new HashMap<>();
		map.put("self", "https://test.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "TECH_DEBT");
		map.put("id", "14141");
		issueField = new IssueField("customfield_14141", "StoryPoints", null, new JSONObject(map));
		issueFieldList.add(issueField);

		map = new HashMap<>();
		map.put("self", "https://test.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Mobile");
		map.put("id", "18181");
		issueField = new IssueField("customfield_18181", "Device Platform", null, new JSONObject(map));
		issueFieldList.add(issueField);

		map = new HashMap<>();
		map.put("self", "https://test.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "code");
		map.put("id", "19121");
		issueField = new IssueField("customfield_19121", "code_issue", null, new JSONObject(map));
		issueFieldList.add(issueField);

		map = new HashMap<>();
		map.put("self", "https://test.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "stage");
		map.put("id", "13131");
		issueField = new IssueField("customfield_13131", "stage", null, new JSONObject(map));
		issueFieldList.add(issueField);

		List<JSONObject> jsonArrayList = new ArrayList<>();
		map = new HashMap<>();
		map.put("self", "https://test.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "40");
		map.put("id", "Test_Automation");
		JSONObject jsonObject = new JSONObject(map);
		jsonArrayList.add(jsonObject);
		issueField = new IssueField("40", "Test_Automation", null, new JSONArray(jsonArrayList));
		issueFieldList.add(issueField);

		Iterable<IssueField> issueFieldIterable = new Iterable<IssueField>() {
			@Override
			public Iterator<IssueField> iterator() {
				return issueFieldList.iterator();
			}
		};
		return issueFieldIterable;
	}

	private void prepareProjectConfig() {
		projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		JiraToolConfig jiraConfig = new JiraToolConfig();
		Optional<Connection> conn = Optional.of(new Connection());
		conn.get().setOffline(Boolean.TRUE);
		conn.get().setBaseUrl("https://test.com/jira/");
		conn.get().setApiEndPoint("rest/api/2/");
		jiraConfig.setBasicProjectConfigId("5b674d58f47cae8935b1b26f");
		jiraConfig.setConnection(conn);

		projectConfFieldMapping.setJira(jiraConfig);

		projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		jiraConfig = new JiraToolConfig();
		jiraConfig.setBasicProjectConfigId("5b719d06a500d00814bfb2b9");
		jiraConfig.setConnection(conn);
		projectConfFieldMapping.setJira(jiraConfig);

		projectConfFieldMapping2 = ProjectConfFieldMapping.builder().build();
		jiraConfig = new JiraToolConfig();
		jiraConfig.setBasicProjectConfigId("5ba8e182d3735010e7f1fa45");
		jiraConfig.setConnection(conn);

		projectConfFieldMapping2.setJira(jiraConfig);
	}

	private void setProjectConfigFieldMap() throws IllegalAccessException, InvocationTargetException {

		BeanUtils.copyProperties(projectConfFieldMapping, scrumProjectList.get(0));
		projectConfFieldMapping.setBasicProjectConfigId(scrumProjectList.get(0).getId());
		projectConfFieldMapping.setFieldMapping(fieldMappingList.get(0));

		BeanUtils.copyProperties(projectConfFieldMapping2, kanbanProjectlist.get(0));
		projectConfFieldMapping2.setBasicProjectConfigId(kanbanProjectlist.get(0).getId());
		projectConfFieldMapping2.setFieldMapping(fieldMappingList.get(1));

		projectConfFieldMappingList.add(projectConfFieldMapping);
		projectConfFieldMappingList.add(projectConfFieldMapping2);
	}

	private void prepareFiledMapping() {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setBasicProjectConfigId(new ObjectId("5b674d58f47cae8935b1b26f"));
		fieldMapping.setSprintName("customfield_12700");
		List<String> jiraType = new ArrayList<>();
		jiraType.add("Defect");
		fieldMapping.setJiradefecttype(jiraType);
		jiraType = new ArrayList<>(
				Arrays.asList(new String[] { "Story", "Defect", "Pre Story", "Feature", "Enabler Story" }));
		String[] jiraIssueType = new String[] { "Story", "Defect", "Pre Story", "Feature", "Enabler Story" };
		fieldMapping.setJiraIssueTypeNames(jiraIssueType);
		fieldMapping.setRootCause("customfield_19121");

		jiraType = new ArrayList<>();
		jiraType.add("Story");
		fieldMapping.setJiraDefectInjectionIssueType(jiraType);
		fieldMapping.setJiraTechDebtIssueType(jiraType);
		fieldMapping.setJiraDefectSeepageIssueType(jiraType);
		fieldMapping.setJiraDefectRemovalStatus(jiraType);
		fieldMapping.setJiraDefectRejectionlIssueType(jiraType);
		fieldMapping.setJiraTestAutomationIssueType(jiraType);
		fieldMapping.setJiraDefectRejectionlIssueType(jiraType);
		fieldMapping.setJiraDefectCountlIssueType(jiraType);
		fieldMapping.setJiraIntakeToDorIssueTypeLT(jiraType);
		fieldMapping.setJiraBugRaisedByCustomField("customfield_12121");

		fieldMapping.setJiraTechDebtIdentification(CommonConstant.CUSTOM_FIELD);
		fieldMapping.setJiraTechDebtCustomField("customfield_14141");

		jiraType = new ArrayList<>();
		jiraType.add("TECH_DEBT");
		fieldMapping.setJiraTechDebtValue(jiraType);
		fieldMapping.setJiraDefectRejectionStatus("Dropped");
		fieldMapping.setJiraBugRaisedByIdentification("CustomField");

		jiraType = new ArrayList<>();
		jiraType.add("Ready for Sign-off");
		fieldMapping.setJiraDod(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Closed");
		fieldMapping.setJiraDefectRemovalStatus(jiraType);

		fieldMapping.setJiraStoryPointsCustomField("customfield_56789");

		jiraType = new ArrayList<>();
		jiraType.add("40");

		jiraType = new ArrayList<>();
		jiraType.add("Client Testing (UAT)");
		fieldMapping.setJiraBugRaisedByValue(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Story");
		jiraType.add("Feature");
		fieldMapping.setJiraSprintVelocityIssueType(jiraType);

		jiraType = new ArrayList<>(Arrays.asList(new String[] { "Story", "Defect", "Pre Story", "Feature" }));
		fieldMapping.setJiraSprintCapacityIssueType(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Closed");
		fieldMapping.setJiraIssueDeliverdStatus(jiraType);

		fieldMapping.setJiraDorLT("In Progress");
		fieldMapping.setJiraLiveStatus("Closed");
		fieldMapping.setRootCauseValue(Arrays.asList("Coding", "None"));

		jiraType = new ArrayList<>(Arrays.asList(new String[] { "Story", "Pre Story" }));
		fieldMapping.setJiraStoryIdentification(jiraType);

		fieldMapping.setJiraDefectCreatedStatus("Open");

		jiraType = new ArrayList<>();
		jiraType.add("Ready for Sign-off");
		fieldMapping.setJiraDod(jiraType);
		fieldMapping.setStoryFirstStatus("In Analysis");
		jiraType = new ArrayList<>();
		jiraType.add("In Analysis");
		jiraType.add("In Development");
		fieldMapping.setJiraStatusForDevelopment(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Ready for Testing");
		fieldMapping.setJiraStatusForQa(jiraType);

		List<String> jiraSegData = new ArrayList<>();
		jiraSegData.add("Tech Story");
		jiraSegData.add("Task");

		jiraSegData = new ArrayList<>();
		jiraSegData.add("Tech Story");
		fieldMappingList.add(fieldMapping);

		// FieldMapping on 2nd project

		fieldMapping = new FieldMapping();
		fieldMapping.setBasicProjectConfigId(new ObjectId("5b719d06a500d00814bfb2b9"));
		jiraType = new ArrayList<>();
		jiraType.add("Defect");
		fieldMapping.setJiradefecttype(jiraType);

		jiraIssueType = new String[] { "Support Request", "Incident", "Project Request", "Member Account Request",
				"TEST Consulting Request", "Test Case" };
		fieldMapping.setJiraIssueTypeNames(jiraIssueType);
		fieldMapping.setStoryFirstStatus("Open");

		fieldMapping.setRootCause("customfield_19121");

		fieldMapping.setJiraDefectRejectionStatus("Dropped");
		fieldMapping.setJiraBugRaisedByIdentification("CustomField");

		jiraType = new ArrayList<>();
		jiraType.add("Ready for Sign-off");
		fieldMapping.setJiraDod(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Closed");
		fieldMapping.setJiraDefectRemovalStatus(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("40");

		fieldMapping.setJiraStoryPointsCustomField("customfield_56789");
		fieldMapping.setJiraTechDebtIdentification("CustomField");

		jiraType = new ArrayList<>(Arrays.asList(new String[] { "Support Request", "Incident", "Project Request",
				"Member Account Request", "TEST Consulting Request", "Test Case" }));
		fieldMapping.setTicketCountIssueType(jiraType);
		fieldMapping.setEnvImpacted("customfield_13131");
		fieldMapping.setJiraTicketVelocityIssueType(jiraType);
		fieldMapping.setKanbanJiraTechDebtIssueType(jiraType);
		fieldMapping.setKanbanCycleTimeIssueType(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Resolved");
		fieldMapping.setTicketDeliverdStatus(jiraType);
		fieldMapping.setJiraTicketResolvedStatus(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Reopen");
		fieldMapping.setTicketReopenStatus(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Closed");
		fieldMapping.setJiraTicketClosedStatus(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Assigned");
		fieldMapping.setJiraTicketTriagedStatus(jiraType);

		fieldMapping.setJiraLiveStatus("Closed");
		fieldMapping.setRootCauseValue(Arrays.asList("Coding", "None"));

		fieldMapping.setEpicName("customfield_14502");
		jiraType = new ArrayList<>();
		jiraType.add("Ready for Sign-off");
		fieldMapping.setJiraDod(jiraType);

		jiraSegData = new ArrayList<>();
		jiraSegData.add("Tech Story");
		jiraSegData.add("Task");

		jiraSegData = new ArrayList<>();
		jiraSegData.add("In Analysis");
		jiraSegData.add("In Development");
		fieldMapping.setJiraStatusForDevelopment(jiraSegData);

		jiraSegData = new ArrayList<>();
		jiraSegData.add("Ready for Testing");
		fieldMapping.setJiraStatusForQa(jiraSegData);
		fieldMapping.setDevicePlatform("customfield_18181");

		jiraSegData = new ArrayList<>();
		jiraSegData.add("segregationLabel");
		fieldMappingList.add(fieldMapping);

	}

	private void prepareIssuesData() throws URISyntaxException {
		Issue issue = Mockito.mock(Issue.class);
		issues.add(issue);
		issue = Mockito.mock(Issue.class);
		issues.add(issue);
		issue = Mockito.mock(Issue.class);
		issues.add(issue);
		issueIterable = new Iterable<Issue>() {
			@Override
			public Iterator<Issue> iterator() {
				return issues.iterator();
			}
		};
	}

	private void prepareKanbanIssuesData() throws URISyntaxException {
		Issue issue = Mockito.mock(Issue.class);
		kanbanIssues.add(issue);
		issue = Mockito.mock(Issue.class);
		kanbanIssues.add(issue);
		issue = Mockito.mock(Issue.class);
		kanbanIssues.add(issue);
		issue = Mockito.mock(Issue.class);
		kanbanIssues.add(issue);
		issueIterableKanban = new Iterable<Issue>() {
			@Override
			public Iterator<Issue> iterator() {
				return kanbanIssues.iterator();
			}
		};
	}

	void prepareAccountHierarchy() {

		accountHierarchy = new AccountHierarchy();
		accountHierarchy.setId(new ObjectId("5e15d9d5e4b098db674614b8"));
		accountHierarchy.setNodeId("TEST_1234_TEST");
		accountHierarchy.setNodeName("TEST");
		accountHierarchy.setLabelName("Project");
		accountHierarchy.setFilterCategoryId(new ObjectId("5e15d7262b6a0532e258ce9c"));
		accountHierarchy.setParentId("Test Project");
		accountHierarchy.setBasicProjectConfigId(new ObjectId("5e15d8b195fe1300014538ce"));
		accountHierarchy.setIsDeleted("False");
		accountHierarchy.setPath(("Test Project, KPIdashboard"));
		accountHierarchy.setBasicProjectConfigId(new ObjectId("5cf632361ed7970009226af9"));
	}

	private void prepareProjectData() {
		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		// Online Project Config data
		projectConfig.setId(new ObjectId("5b674d58f47cae8935b1b26f"));
		projectConfig.setProjectName("TestProject");
		SubProjectConfig subProjectConfig = new SubProjectConfig();
		subProjectConfig.setSubProjectIdentification("CustomField");
		subProjectConfig.setSubProjectIdentSingleValue("customfield_37903");
		List<SubProjectConfig> subProjectList = new ArrayList<>();
		subProjectList.add(subProjectConfig);
		projectConfig.setIsKanban(false);
		scrumProjectList.add(projectConfig);

		ProjectBasicConfig projectConfig1 = new ProjectBasicConfig();
		// Online Project Config data
		projectConfig1.setId(new ObjectId("5b719d06a500d00814bfb2b9"));
		projectConfig1.setProjectName("TestProject");
		SubProjectConfig subProjectConfig1 = new SubProjectConfig();
		subProjectConfig1.setSubProjectIdentification("CustomField");
		subProjectConfig1.setSubProjectIdentSingleValue("customfield_37903");
		List<SubProjectConfig> subProjectList1 = new ArrayList<>();

		projectConfig1.setIsKanban(true);
		kanbanProjectlist.add(projectConfig1);

	}

	private MetadataIdentifier createMetaDataIdentifier() {
		String tool = "Jira";
		Boolean isKanban = Boolean.FALSE;
		String templateName = "DOJO Safe Template";
		String templateCode = "6";

		Identifier issue1 = createIdentifier("story",
				Arrays.asList("Story", "Enabler Story", "Tech Story", "Change request"));
		Identifier issue2 = createIdentifier("bug", Arrays.asList("Defect", "Bug"));
		Identifier issue3 = createIdentifier("epic", Arrays.asList("Epic"));
		Identifier issue4 = createIdentifier("issuetype",
				Arrays.asList("Story", "Enabler Story", "Tech Story", "Change request", "Defect", "Bug", "Epic"));
		Identifier issue5 = createIdentifier("uatdefect", Arrays.asList("UAT Defect"));
		List<Identifier> issuesIdentifier = Arrays.asList(issue1, issue2, issue3, issue4, issue5);

		Identifier customField1 = createIdentifier("storypoint", Arrays.asList("storypoint"));
		Identifier customField2 = createIdentifier("sprint", Arrays.asList("Sprint"));
		Identifier customField3 = createIdentifier("rootcause", Arrays.asList("Root Cause"));
		Identifier customField4 = createIdentifier("techdebt", Arrays.asList("Tech Debt"));
		Identifier customField5 = createIdentifier("uat", Arrays.asList("UAT"));
		Identifier customField6 = createIdentifier("timeCriticality", Arrays.asList("Time Criticality"));
		Identifier customField7 = createIdentifier("wsjf", Arrays.asList("WSJF"));
		Identifier customField8 = createIdentifier("costOfDelay", Arrays.asList("Cost of Delay"));
		Identifier customField9 = createIdentifier("businessValue", Arrays.asList("User-Business Value"));
		Identifier customField10 = createIdentifier("riskReduction",
				Arrays.asList("Risk Reduction-Opportunity Enablement Value"));
		Identifier customField11 = createIdentifier("jobSize", Arrays.asList("Job Size"));
		List<Identifier> customfieldIdentifer = Arrays.asList(customField1, customField2, customField3, customField4,
				customField5, customField6, customField7, customField8, customField9, customField10, customField11);

		Identifier workflow1 = createIdentifier("dor", Arrays.asList("Ready for Sprint Planning", "In Progress"));
		Identifier workflow2 = createIdentifier("dod", Arrays.asList("Closed", "Resolved", "Ready for Delivery"));
		Identifier workflow3 = createIdentifier("qa", Arrays.asList("In Testing"));
		Identifier workflow4 = createIdentifier("firststatus", Arrays.asList("Open"));
		Identifier workflow5 = createIdentifier("rejection", Arrays.asList("Closed", "Rejected"));
		Identifier workflow6 = createIdentifier("delivered",
				Arrays.asList("Closed", "Resolved", "Ready for Delivery", "Ready for Release"));
		Identifier workflow7 = createIdentifier("firststatus", Arrays.asList("Open"));
		List<Identifier> workflowIdentifer = Arrays.asList(workflow1, workflow2, workflow3, workflow4, workflow5,
				workflow6, workflow7);

		Identifier valuestoidentify1 = createIdentifier("rootCauseValue", Arrays.asList("Coding"));
		Identifier valuestoidentify2 = createIdentifier("rejectionResolution",
				Arrays.asList("Invalid", "Duplicate", "Unrequired"));
		Identifier valuestoidentify3 = createIdentifier("qaRootCause",
				Arrays.asList("Coding", "Configuration", "Regression", "Data"));
		List<Identifier> valuestoidentifyIdentifer = Arrays.asList(valuestoidentify1, valuestoidentify2,
				valuestoidentify3);

		List<Identifier> issuelinkIdentifer = new ArrayList<>();
		return new MetadataIdentifier(tool, templateName, templateCode, isKanban, false, issuesIdentifier,
				customfieldIdentifer, workflowIdentifer, issuelinkIdentifer, valuestoidentifyIdentifer);

	}

	private MetadataIdentifier createKanbanMetaDataIdentifier() {
		String tool = "Jira";
		Boolean isKanban = Boolean.TRUE;
		String templateName = "Standard Template";
		String templateCode = "6";

		Identifier issue1 = createIdentifier("story",
				Arrays.asList("Story", "Enabler Story", "Tech Story", "Change request"));
		Identifier issue2 = createIdentifier("bug", Arrays.asList("Defect", "Bug"));
		Identifier issue3 = createIdentifier("epic", Arrays.asList("Epic"));
		Identifier issue4 = createIdentifier("issuetype",
				Arrays.asList("Story", "Enabler Story", "Tech Story", "Change request", "Defect", "Bug", "Epic"));
		Identifier issue5 = createIdentifier("uatdefect", Arrays.asList("UAT Defect"));
		List<Identifier> issuesIdentifier = Arrays.asList(issue1, issue2, issue3, issue4, issue5);

		Identifier customField1 = createIdentifier("storypoint", Arrays.asList("storypoint"));
		Identifier customField2 = createIdentifier("sprint", Arrays.asList("Sprint"));
		Identifier customField3 = createIdentifier("rootcause", Arrays.asList("Root Cause"));
		Identifier customField4 = createIdentifier("techdebt", Arrays.asList("Tech Debt"));
		Identifier customField5 = createIdentifier("uat", Arrays.asList("UAT"));
		Identifier customField6 = createIdentifier("timeCriticality", Arrays.asList("Time Criticality"));
		Identifier customField7 = createIdentifier("wsjf", Arrays.asList("WSJF"));
		Identifier customField8 = createIdentifier("costOfDelay", Arrays.asList("Cost of Delay"));
		Identifier customField9 = createIdentifier("businessValue", Arrays.asList("User-Business Value"));
		Identifier customField10 = createIdentifier("riskReduction",
				Arrays.asList("Risk Reduction-Opportunity Enablement Value"));
		Identifier customField11 = createIdentifier("jobSize", Arrays.asList("Job Size"));
		List<Identifier> customfieldIdentifer = Arrays.asList(customField1, customField2, customField3, customField4,
				customField5, customField6, customField7, customField8, customField9, customField10, customField11);

		Identifier workflow1 = createIdentifier("dor", Arrays.asList("Ready for Sprint Planning", "In Progress"));
		Identifier workflow2 = createIdentifier("dod", Arrays.asList("Closed", "Resolved", "Ready for Delivery"));
		Identifier workflow3 = createIdentifier("qa", Arrays.asList("In Testing"));
		Identifier workflow4 = createIdentifier("firststatus", Arrays.asList("Open"));
		Identifier workflow5 = createIdentifier("rejection", Arrays.asList("Closed", "Rejected"));
		Identifier workflow6 = createIdentifier("delivered",
				Arrays.asList("Closed", "Resolved", "Ready for Delivery", "Ready for Release"));
		Identifier workflow7 = createIdentifier("firststatus", Arrays.asList("Open"));
		List<Identifier> workflowIdentifer = Arrays.asList(workflow1, workflow2, workflow3, workflow4, workflow5,
				workflow6, workflow7);

		Identifier valuestoidentify1 = createIdentifier("rootCauseValue", Arrays.asList("Coding"));
		Identifier valuestoidentify2 = createIdentifier("rejectionResolution",
				Arrays.asList("Invalid", "Duplicate", "Unrequired"));
		Identifier valuestoidentify3 = createIdentifier("qaRootCause",
				Arrays.asList("Coding", "Configuration", "Regression", "Data"));
		List<Identifier> valuestoidentifyIdentifer = Arrays.asList(valuestoidentify1, valuestoidentify2,
				valuestoidentify3);

		List<Identifier> issuelinkIdentifer = new ArrayList<>();
		return new MetadataIdentifier(tool, templateName, templateCode, isKanban, false, issuesIdentifier,
				customfieldIdentifer, workflowIdentifer, issuelinkIdentifer, valuestoidentifyIdentifer);

	}

	private Identifier createIdentifier(String type, List<String> value) {
		Identifier identifier = new Identifier();
		identifier.setType(type);
		identifier.setValue(value);
		return identifier;
	}
}