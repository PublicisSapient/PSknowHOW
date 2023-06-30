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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.atlassian.jira.rest.client.api.SearchRestClient;
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
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import com.atlassian.jira.rest.client.api.domain.Visibility.Type;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.SubProjectConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.client.jiraissue.HandleJiraHistory;
import com.publicissapient.kpidashboard.jira.client.jiraissue.ScrumJiraIssueClientImpl;
import com.publicissapient.kpidashboard.jira.client.sprint.SprintClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraProcessor;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.AdditionalFilterHelper;

import io.atlassian.util.concurrent.Promise;

@ExtendWith(SpringExtension.class)
public class ScrumJiraIssueClientImplTest {

	List<ProjectBasicConfig> scrumProjectList = new ArrayList<>();
	List<ProjectBasicConfig> kanbanProjectlist = new ArrayList<>();

	ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
	ProjectConfFieldMapping projectConfFieldMapping2 = ProjectConfFieldMapping.builder().build();
	List<FieldMapping> fieldMappingList = new ArrayList<>();
	List<ProjectConfFieldMapping> projectConfFieldMappingList = new ArrayList<>();
	List<Issue> issues = new ArrayList<>();

	SearchResult searchResult;
	@Mock
	JiraProcessor jiraProcessor;
	@Mock
	ProcessorJiraRestClient client;
	@Mock
	SearchRestClient searchRestClient;
	@Mock
	Promise<SearchResult> promisedRs;
	@Mock
	SprintClient sprintClient;
	@Mock
	private JiraIssueRepository jiraIssueRepository;
	@Mock
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	private JiraProcessorRepository jiraProcessorRepository;
	@Mock
	private AccountHierarchyRepository accountHierarchyRepository;
	@Mock
	private JiraProcessorConfig jiraProcessorConfig;
	@InjectMocks
	private ScrumJiraIssueClientImpl scrumJiraIssueClientImpl;
	@Mock
	private TestCaseDetailsRepository testCaseDetailsRepository;
	@Mock
	private JiraAdapter jiraAdapter;
	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Mock
	private AdditionalFilterHelper additionalFilterHelper;

	@Mock
	private HierarchyLevelService hierarchyLevelService;

	@Mock
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Mock
	private HandleJiraHistory handleJiraHistory;

	@Mock
	private IssueBacklogRepository issueBacklogRepository;

	@Mock
	private IssueBacklogCustomHistoryRepository issueBacklogCustomHistoryRepository;

	@BeforeEach
	public void setUp() throws Exception {
		prepareProjectData();
		prepareProjectConfig();
		prepareFiledMapping();
		setProjectConfigFieldMap();

	}

	@Test
	public void testProcessesJiraIssues() throws URISyntaxException, InterruptedException {
		when(jiraIssueRepository.findTopByBasicProjectConfigId(any())).thenReturn(null);
		when(jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA)).thenReturn(jiraProcessor);
		when(jiraProcessor.getId()).thenReturn(new ObjectId("5e16c126e4b098db673cc372"));
		when(jiraAdapter.getPageSize()).thenReturn(30);
		when(jiraAdapter.getUserTimeZone(any())).thenReturn("Indian/Maldives");
		when(jiraProcessorConfig.getMinsToReduce()).thenReturn(30L);
		when(jiraProcessorConfig.getStartDate()).thenReturn("2019-01-07 00:00");
		createIssue();
		when(jiraAdapter.getIssues(any(), any(), any(), any(), anyInt(), anyBoolean())).thenReturn(searchResult);
		projectConfFieldMapping.setProjectName("prName");
		assertEquals(2,
				scrumJiraIssueClientImpl.processesJiraIssues(projectConfFieldMapping, jiraAdapter, Boolean.FALSE));
	}

	private void prepareProjectData() {
		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		// Online Project Config data
		projectConfig.setId(new ObjectId("5b674d58f47cae8935b1b26f"));
		projectConfig.setProjectName("TestProject");
		projectConfig.setSaveAssigneeDetails(true);
		SubProjectConfig subProjectConfig = new SubProjectConfig();
		subProjectConfig.setSubProjectIdentification("CustomField");
		subProjectConfig.setSubProjectIdentSingleValue("customfield_37903");
		List<SubProjectConfig> subProjectList = new ArrayList<>();
		subProjectList.add(subProjectConfig);
		ProjectToolConfig jiraConfig = new ProjectToolConfig();

		BoardDetails board = new BoardDetails();
		board.setBoardId("1111");
		board.setBoardName("test board");
		List<BoardDetails> boardList = new ArrayList<>();
		boardList.add(board);
		jiraConfig.setBoards(boardList);
		projectConfig.setIsKanban(false);
		scrumProjectList.add(projectConfig);
	}

	private void prepareProjectConfig() {
		JiraToolConfig jiraConfig = new JiraToolConfig();
		Optional<Connection> conn = Optional.of(new Connection());
		conn.get().setOffline(Boolean.TRUE);
		conn.get().setBaseUrl("https://abc.com/jira/");
		conn.get().setApiEndPoint("rest/api/2/");
		jiraConfig.setBasicProjectConfigId("5b674d58f47cae8935b1b26f");
		jiraConfig.setConnection(conn);
		projectConfFieldMapping.setJira(jiraConfig);
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
		fieldMapping.setJiraDefectCountlIssueTypeRCA(jiraType);
		fieldMapping.setJiraDefectCountlIssueTypeDC(jiraType);
		fieldMapping.setJiraIntakeToDorIssueType(jiraType);
		fieldMapping.setJiraBugRaisedByCustomField("customfield_12121");

		fieldMapping.setJiraTechDebtIdentification(CommonConstant.CUSTOM_FIELD);
		fieldMapping.setJiraTechDebtCustomField("customfield_14141");

		jiraType = new ArrayList<>();
		jiraType.add("TECH_DEBT");
		fieldMapping.setJiraTechDebtValue(jiraType);
		fieldMapping.setJiraDefectRejectionStatusDRR("Dropped");
		fieldMapping.setJiraDefectRejectionStatusDIR("Dropped");
		fieldMapping.setJiraDefectRejectionStatusAVR("Dropped");
		fieldMapping.setJiraDefectRejectionStatusDC("Dropped");
		fieldMapping.setJiraDefectRejectionStatusDRE("Dropped");
		fieldMapping.setJiraDefectRejectionStatusDSR("Dropped");
		fieldMapping.setJiraDefectRejectionStatusFTPR("Dropped");
		fieldMapping.setJiraDefectRejectionStatusIFTPR("Dropped");
		fieldMapping.setJiraDefectRejectionStatusQADD("Dropped");
		fieldMapping.setJiraDefectRejectionStatusQS("Dropped");
		fieldMapping.setJiraDefectRejectionStatusRCA("Dropped");
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
		fieldMapping.setJiraSprintVelocityIssueTypeBR(jiraType);
		fieldMapping.setJiraSprintVelocityIssueTypeSV(jiraType);

		jiraType = new ArrayList<>(Arrays.asList(new String[] { "Story", "Defect", "Pre Story", "Feature" }));
		fieldMapping.setJiraSprintCapacityIssueType(jiraType);

		jiraType = new ArrayList<>();
		jiraType.add("Closed");
		fieldMapping.setJiraIssueDeliverdStatusAVR(jiraType);
		fieldMapping.setJiraIssueDeliverdStatusBR(jiraType);
		fieldMapping.setJiraIssueDeliverdStatusCVR(jiraType);
		fieldMapping.setJiraIssueDeliverdStatusFTPR(jiraType);
		fieldMapping.setJiraIssueDeliverdStatusSV(jiraType);

		fieldMapping.setJiraDor("In Progress");
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
		fieldMapping.setJiraStatusForDevelopmentAVR(jiraType);

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

		fieldMapping.setJiraDefectRejectionStatusDRR("Dropped");
		fieldMapping.setJiraDefectRejectionStatusDIR("Dropped");
		fieldMapping.setJiraDefectRejectionStatusAVR("Dropped");
		fieldMapping.setJiraDefectRejectionStatusDC("Dropped");
		fieldMapping.setJiraDefectRejectionStatusDRE("Dropped");
		fieldMapping.setJiraDefectRejectionStatusDSR("Dropped");
		fieldMapping.setJiraDefectRejectionStatusFTPR("Dropped");
		fieldMapping.setJiraDefectRejectionStatusIFTPR("Dropped");
		fieldMapping.setJiraDefectRejectionStatusQADD("Dropped");
		fieldMapping.setJiraDefectRejectionStatusQS("Dropped");
		fieldMapping.setJiraDefectRejectionStatusRCA("Dropped");
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
		fieldMapping.setJiraStatusForDevelopmentAVR(jiraSegData);

		jiraSegData = new ArrayList<>();
		jiraSegData.add("Ready for Testing");
		fieldMapping.setJiraStatusForQa(jiraSegData);
		fieldMapping.setDevicePlatform("customfield_18181");

		jiraSegData = new ArrayList<>();
		jiraSegData.add("segregationLabel");
		fieldMappingList.add(fieldMapping);

	}

	private void setProjectConfigFieldMap() throws IllegalAccessException, InvocationTargetException {

		BeanUtils.copyProperties(projectConfFieldMapping, scrumProjectList.get(0));
		projectConfFieldMapping.setBasicProjectConfigId(scrumProjectList.get(0).getId());
		projectConfFieldMapping.setFieldMapping(fieldMappingList.get(0));
		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setSaveAssigneeDetails(true);
		projectConfFieldMapping.setProjectBasicConfig(projectBasicConfig);

		/*
		 * BeanUtils.copyProperties(projectConfFieldMapping2, kanbanProjectlist.get(0));
		 * projectConfFieldMapping2.setBasicProjectConfigId(kanbanProjectlist.get(0).
		 * getId()); projectConfFieldMapping2.setFieldMapping(fieldMappingList.get(1));
		 */
		ProjectToolConfig jiraConfig = new ProjectToolConfig();
		BoardDetails board = new BoardDetails();
		board.setBoardId("1111");
		board.setBoardName("test board");
		List<BoardDetails> boardList = new ArrayList<>();
		boardList.add(board);
		jiraConfig.setBoards(boardList);
		projectConfFieldMapping.setProjectToolConfig(jiraConfig);
		projectConfFieldMappingList.add(projectConfFieldMapping);
		// projectConfFieldMappingList.add(projectConfFieldMapping2);
	}

	private void createIssue() throws URISyntaxException {
		BasicProject basicProj = new BasicProject(new URI("self"), "proj1", 1l, "project1");
		IssueType issueType1 = new IssueType(new URI("self"), 1l, "Story", false, "desc", new URI("iconURI"));
		IssueType issueType2 = new IssueType(new URI("self"), 2l, "Defect", false, "desc", new URI("iconURI"));
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
		map.put("self", "https://testDomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Component");
		map.put("id", "20810");
		JSONObject value = new JSONObject(map);
		IssueField issueField = new IssueField("20810", "Component", null, value);
		List<IssueField> issueFields = Arrays.asList(issueField);
		Comment comment = new Comment(new URI("self"), "body", null, null, DateTime.now(), DateTime.now(),
				new Visibility(Type.ROLE, "abc"), 1l);
		List<Comment> comments = Arrays.asList(comment);
		BasicVotes basicVotes = new BasicVotes(new URI("self"), 1, true);
		BasicUser basicUser = new BasicUser(new URI("self"), "basicuser", "basicuser", "accountId");
		Worklog worklog = new Worklog(new URI("self"), new URI("self"), basicUser, basicUser, null, DateTime.now(),
				DateTime.now(), DateTime.now(), 60, null);
		List<Worklog> workLogs = Arrays.asList(worklog);
		ChangelogItem changelogItem = new ChangelogItem(FieldType.JIRA, "field1", "from", "fromString", "to",
				"toString");
		ChangelogGroup changelogGroup = new ChangelogGroup(basicUser, DateTime.now(), Arrays.asList(changelogItem));

		Issue issue = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType1, status1, "story",
				basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(),
				DateTime.now(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, issueFields, comments,
				null, createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
				Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));
		Issue issue1 = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType2, status1, "Defect",
				basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(),
				DateTime.now(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, issueFields, comments,
				null, createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
				Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));
		issues.add(issue);
		issues.add(issue1);

		searchResult = new SearchResult(0, 10, 2, issues);

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