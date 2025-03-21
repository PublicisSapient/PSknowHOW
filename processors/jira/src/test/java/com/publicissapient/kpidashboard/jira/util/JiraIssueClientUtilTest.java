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

package com.publicissapient.kpidashboard.jira.util;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.utils.Asserts.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.domain.*;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;

@RunWith(MockitoJUnitRunner.class)
public class JiraIssueClientUtilTest {
	@InjectMocks
	private JiraIssueClientUtil jiraIssueClientUtil;

	List<Issue> issues = new ArrayList<>();
	List<IssueField> issueFieldList = new ArrayList<>();

	@Before
	public void setup() throws URISyntaxException, JSONException {
		createIssuefieldsList();
	}

	@Test
	public void testSprintComparator() {
		SprintDetails sprint1 = mock(SprintDetails.class);
		SprintDetails sprint2 = mock(SprintDetails.class);

		when(sprint1.getStartDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 1)));
		when(sprint1.getEndDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 15)));

		when(sprint2.getStartDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 1)));
		when(sprint2.getEndDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 20)));

		int result = jiraIssueClientUtil.SPRINT_COMPARATOR.compare(sprint1, sprint2);

		assertEquals(-1, result, "Sprint 1 should come before Sprint 2");
	}

	@Test
	public void testSprintComparator_EqualDates() {
		SprintDetails sprint1 = mock(SprintDetails.class);
		SprintDetails sprint2 = mock(SprintDetails.class);

		when(sprint1.getStartDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 1)));
		when(sprint1.getEndDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 15)));

		when(sprint2.getStartDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 1)));
		when(sprint2.getEndDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 15)));

		int result = jiraIssueClientUtil.SPRINT_COMPARATOR.compare(sprint1, sprint2);

		assertEquals(0, result, "Sprints should be equal");
	}

	@Test
	public void testSprintComparator_DifferentStartDates() {
		SprintDetails sprint1 = mock(SprintDetails.class);
		SprintDetails sprint2 = mock(SprintDetails.class);

		when(sprint1.getStartDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 1)));
		when(sprint2.getStartDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 2)));
		int result = jiraIssueClientUtil.SPRINT_COMPARATOR.compare(sprint1, sprint2);

		assertEquals(-1, result, "Sprint 1 should come before Sprint 2");
	}

	@Test
	public void testSprintComparator_DifferentEndDates() {
		SprintDetails sprint1 = mock(SprintDetails.class);
		SprintDetails sprint2 = mock(SprintDetails.class);

		when(sprint1.getStartDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 1)));
		when(sprint1.getEndDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 15)));

		when(sprint2.getStartDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 1)));
		when(sprint2.getEndDate()).thenReturn(String.valueOf(LocalDate.of(2023, 1, 20)));
		int result = jiraIssueClientUtil.SPRINT_COMPARATOR.compare(sprint1, sprint2);

		assertEquals(-1, result, "Sprint 1 should come before Sprint 2");
	}

	@Test
	public void testGetListFromJson_JSONObject() {
		Collection result = jiraIssueClientUtil.getListFromJson(issueFieldList.get(0));
		assertEquals(1, result.size(), "Expected size: 1");
	}

	@Test
	public void testGetListFromJson_WithJSONArray() throws org.json.JSONException, JSONException {
		// Mock JSONArray
		JSONArray jsonArray = new JSONArray();
		jsonArray.add(new JSONObject().put("value", "Value1"));
		jsonArray.add(new JSONObject().put("value", "Value2"));

		// Mock IssueField
		IssueField issueField = mock(IssueField.class);
		when(issueField.getValue()).thenReturn(jsonArray);

		Collection result = jiraIssueClientUtil.getListFromJson(issueField);

		assertEquals(2, result.size(), "Expected size: 2");
	}

	@Test
	public void testGetListFromJson_EmptyValue() {
		IssueField issueField = new IssueField("customfield_19121", "Component", null, new ArrayList<>());

		Collection result = jiraIssueClientUtil.getListFromJson(issueField);

		assertNotNull(result, "Result should not be null");
		assertTrue(result.isEmpty(), "Result should be empty");
	}

	@Test
	public void testBuildFieldMap_NullFields() {
		Iterable<IssueField> fields = null;

		Map<String, IssueField> result = jiraIssueClientUtil.buildFieldMap(fields);

		assertNotNull(result, "Result should not be null");
		assertTrue(result.isEmpty(), "Result should be empty for null fields");
	}

	@Test
	public void testBuildFieldMap_EmptyFields() {
		Iterable<IssueField> fields = Collections.emptyList();

		Map<String, IssueField> result = jiraIssueClientUtil.buildFieldMap(fields);

		assertNotNull(result, "Result should not be null");
		assertTrue(result.isEmpty(), "Result should be empty for empty fields");
	}

	@Test
	public void testBuildFieldMap_NonEmptyFields() {
		IssueField field1 = new IssueField("id1", "", "", "Value1");
		IssueField field2 = new IssueField("id2", "", "", "Value2");
		List<IssueField> fields = Arrays.asList(field1, field2);

		Map<String, IssueField> result = jiraIssueClientUtil.buildFieldMap(fields);

		assertNotNull(result, "Result should not be null");
		assertEquals(2, result.size(), "Result size should be 2");

		assertTrue(result.containsKey("id1"), "Result should contain key 'id1'");
		assertEquals(field1, result.get("id1"), "Retrieved value should match field1");

		assertTrue(result.containsKey("id2"), "Result should contain key 'id2'");
		assertEquals(field2, result.get("id2"), "Retrieved value should match field2");
	}

	@Test
	public void testSortChangeLogGroup_NullChangelog() {
		Issue issue = Mockito.mock(Issue.class);
		Mockito.when(issue.getChangelog()).thenReturn(null);

		List<ChangelogGroup> result = jiraIssueClientUtil.sortChangeLogGroup(issue);

		assertNotNull(result, "");
		assertTrue(result.isEmpty());
	}

	@Test
	public void testSortChangeLogGroup_EmptyChangelog() {
		Issue issue = Mockito.mock(Issue.class);
		Mockito.when(issue.getChangelog()).thenReturn(new ArrayList<>());

		List<ChangelogGroup> result = jiraIssueClientUtil.sortChangeLogGroup(issue);

		assertNotNull(result, "");
		assertTrue(result.isEmpty());
	}

	@Test
	public void testSortChangeLogGroup_SortedChangelog() throws URISyntaxException {
		ChangelogGroup changelogGroup;
		List<ChangelogGroup> changeLogList = new ArrayList<>();
		changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "n1", "", ""),
				new DateTime("2023-03-28T03:57:59.000+0000"),
				Arrays.asList(new ChangelogItem(FieldType.JIRA, "status", "10003", "In Development", "15752", "Code Review")));
		changeLogList.add(changelogGroup);
		changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "n2", "", ""),
				new DateTime("2023-03-29T03:57:59.000+0000"),
				Arrays.asList(new ChangelogItem(FieldType.JIRA, "priority", "10003", "P1", "15752", "P2")));
		changeLogList.add(changelogGroup);
		changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "n3", "", ""),
				new DateTime("2023-03-30T03:57:59.000+0000"),
				Arrays.asList(new ChangelogItem(FieldType.JIRA, "assignee", "10003", "Harsh", "15752", "Akshat")));
		changeLogList.add(changelogGroup);
		Issue issue = Mockito.mock(Issue.class);
		Mockito.when(issue.getChangelog()).thenReturn(changeLogList);

		List<ChangelogGroup> result = jiraIssueClientUtil.sortChangeLogGroup(issue);

		assertNotNull(result, "");
		assertFalse(result.isEmpty());
		assertEquals(changeLogList.get(0), result.get(0));
		assertEquals(changeLogList.get(1), result.get(1));
		assertEquals(changeLogList.get(2), result.get(2));
	}

	@Test
	public void testGetKanbanAccountHierarchy_EmptyRepository() {
		KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo = Mockito.mock(KanbanAccountHierarchyRepository.class);
		when(kanbanAccountHierarchyRepo.findAll()).thenReturn(new ArrayList<>());

		Map<Pair<String, String>, KanbanAccountHierarchy> result = jiraIssueClientUtil
				.getKanbanAccountHierarchy(kanbanAccountHierarchyRepo);

		assertNotNull(result, "");
		assertTrue(result.isEmpty());
	}

	private void createIssuefieldsList() throws JSONException {
		Map<String, Object> map = new HashMap<>();
		map.put("customfield_12121", "Client Testing (UAT)");
		map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
		map.put("value", "Client Testing (UAT)");
		map.put("id", "12121");
		IssueField issueField = new IssueField("customfield_12121", "UAT", null,
				new org.codehaus.jettison.json.JSONObject(map));
		issueFieldList.add(issueField);
	}
}
