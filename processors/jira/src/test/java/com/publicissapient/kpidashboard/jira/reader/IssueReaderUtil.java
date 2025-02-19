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

package com.publicissapient.kpidashboard.jira.reader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;

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
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.jira.dataFactories.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ToolConfigDataFactory;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.model.ReadData;

public class IssueReaderUtil {

	public static ReadData getMockReadData(String boardId, ProjectConfFieldMapping projectConfFieldMapping)
			throws URISyntaxException, JSONException {
		ReadData readData = new ReadData();
		readData.setBoardId(boardId);
		readData.setIssue(createIssue().get(1));
		readData.setProjectConfFieldMapping(projectConfFieldMapping);
		readData.setSprintFetch(true);
		readData.setProcessorId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		return readData;
	}

	public static ProjectConfFieldMapping createProjectConfigMap(List<ProjectBasicConfig> projectBasicConfigs,
			Optional<Connection> connection, FieldMapping fieldMapping, List<ProjectToolConfig> projectToolConfigs) {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		ProjectBasicConfig projectConfig = projectBasicConfigs.get(1);
		// Todo: check the beanUtils func changed to import
		// org.springframework.beans.BeanUtils;
		BeanUtils.copyProperties(projectConfig, projectConfFieldMapping);
		projectConfFieldMapping.setProjectBasicConfig(projectConfig);
		projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping.setJira(getJiraToolConfig(connection, projectToolConfigs));
		projectConfFieldMapping.setJiraToolConfigId(projectToolConfigs.get(0).getId());
		projectConfFieldMapping.setFieldMapping(fieldMapping);
		projectConfFieldMapping.setProjectToolConfig(projectToolConfigs.get(0));
		return projectConfFieldMapping;
	}

	public static JiraToolConfig getJiraToolConfig(Optional<Connection> connection,
			List<ProjectToolConfig> projectToolConfigs) {
		JiraToolConfig toolObj = new JiraToolConfig();
		// Todo: check the beanUtils func changed to import
		// org.springframework.beans.BeanUtils;
		BeanUtils.copyProperties(projectToolConfigs.get(0), toolObj);
		toolObj.setConnection(connection);
		return toolObj;
	}

	public static Optional<Connection> getMockConnection(String connectionId) {
		ConnectionsDataFactory connectionDataFactory = ConnectionsDataFactory.newInstance("/json/default/connections.json");
		return connectionDataFactory.findConnectionById(connectionId);
	}

	public static List<ProjectBasicConfig> getMockProjectConfig() {
		ProjectBasicConfigDataFactory projectConfigDataFactory = ProjectBasicConfigDataFactory
				.newInstance("/json/default/project_basic_configs.json");
		return projectConfigDataFactory.getProjectBasicConfigs();
	}

	public static List<ProjectToolConfig> getMockProjectToolConfig(String projectId) {
		ToolConfigDataFactory projectToolConfigDataFactory = ToolConfigDataFactory
				.newInstance("/json/default/project_tool_configs.json");
		return projectToolConfigDataFactory.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA, projectId);
	}

	public static FieldMapping getMockFieldMapping(String projectId) {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/field_mapping.json");
		return fieldMappingDataFactory.findByBasicProjectConfigId(projectId);
	}

	public static List<Issue> createIssue() throws URISyntaxException, JSONException {
		List<Issue> issues = new ArrayList<>();
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
		ChangelogItem changelogItem = new ChangelogItem(FieldType.JIRA, "field1", "from", "fromString", "to", "toString");
		ChangelogGroup changelogGroup = new ChangelogGroup(basicUser, DateTime.now(), Arrays.asList(changelogItem));

		Issue issue = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType1, status1, "story",
				basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(), DateTime.now(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, issueFields, comments, null,
				createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
				Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));
		Issue issue1 = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType2, status1, "Defect",
				basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(), DateTime.now(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, issueFields, comments, null,
				createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
				Arrays.asList(changelogGroup), null, new HashSet<>(Arrays.asList("label1")));
		issues.add(issue);
		issues.add(issue1);
		return issues;
	}

	public static List<IssueLink> createIssueLinkData() throws URISyntaxException {
		List<IssueLink> issueLinkList = new ArrayList<>();
		URI uri = new URI("https://testDomain.com/jira/rest/api/2/issue/12344");
		IssueLinkType linkType = new IssueLinkType("Blocks", "blocks", IssueLinkType.Direction.OUTBOUND);
		IssueLink issueLink = new IssueLink("IssueKey", uri, linkType);
		issueLinkList.add(issueLink);

		return issueLinkList;
	}

	public static List<ProcessorExecutionTraceLog> mockProcessorExecutionTraceLog(String projectId) {
		List<ProcessorExecutionTraceLog> pl = new ArrayList<>();
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.JIRA);
		processorExecutionTraceLog.setLastSuccessfulRun("2023-02-06");
		processorExecutionTraceLog.setBasicProjectConfigId(projectId);
		pl.add(processorExecutionTraceLog);
		return pl;
	}
}
