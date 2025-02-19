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

package com.publicissapient.kpidashboard.jira.processor;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.FetchSprintReport;
import com.publicissapient.kpidashboard.jira.service.JiraClientService;

@RunWith(MockitoJUnitRunner.class)
public class SprintDataProcessorImplTest {

	@Mock
	private FetchSprintReport fetchSprintReport;

	@Mock
	private JiraClientService jiraClientService;

	@InjectMocks
	private SprintDataProcessorImpl sprintDataProcessor;

	ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();

	@Test
	public void testProcessSprintDataWithValidSprintField() throws IOException, JSONException {
		createProjectConfigMap();
		// Arrange
		Issue issue = createMockIssueWithSprintField();
		String boardId = "yourBoardId";

		// Act
		Set<SprintDetails> result = sprintDataProcessor.processSprintData(issue, projectConfFieldMapping, boardId,
				new ObjectId("5e16c126e4b098db673cc372"));

		// Assert
		assertNotNull(result);
	}

	private Issue createMockIssueWithSprintField() throws JSONException {
		List<IssueField> issueFieldList = new ArrayList<>();
		List<Object> sprintList = new ArrayList<>();
		String sprint = "com.atlassian.greenhopper.service.sprint.Sprint@6fc7072e[id=23356,rapidViewId=11649,state=CLOSED,name=TEST | 06 Jan - 19 Jan,startDate=2020-01-06T11:38:31.937Z,endDate=2020-01-19T11:38:00.000Z,completeDate=2020-01-20T11:15:21.528Z,sequence=22778,goal=]";
		sprintList.add(sprint);
		JSONArray array = new JSONArray(sprintList);

		IssueField issueField = new IssueField("customfield_12700", "Sprint", null, array);
		issueFieldList.add(issueField);
		Issue issue = new Issue("summary1", null, "key1", 1l, null, null, null, "story", null, null, new ArrayList<>(),
				null, null, DateTime.now(), DateTime.now(), DateTime.now(), new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>(), null, issueFieldList, null, null, null, null, null, null, Arrays.asList("expandos"), null,
				Arrays.asList(), null, new HashSet<>(Arrays.asList("label1")));
		return issue;
	}

	private void createProjectConfigMap() {
		projectConfFieldMapping.setProjectName("knowhow");
		projectConfFieldMapping.setKanban(false);
		projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("5fd99f7bc8b51a7b55aec836"));
		ProjectBasicConfig basicConfig = new ProjectBasicConfig();
		basicConfig.setProjectName("knowhow");
		basicConfig.setProjectNodeId("abc_uniqueId");
		projectConfFieldMapping.setProjectBasicConfig(basicConfig);
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setSprintName("customfield_12700");
		projectConfFieldMapping.setFieldMapping(fieldMapping);
	}
}
