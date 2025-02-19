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

package com.publicissapient.kpidashboard.jira.helper;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.*;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.json.simple.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.*;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

@RunWith(MockitoJUnitRunner.class)
public class JiraHelperTest {

	@Mock
	Issue issue;

	@Mock
	ChangelogGroup changelogGroup;

	@Test
	public void SprintComparatorTest() {
		SprintDetails sprintDetails1 = getSprintDetails("Sprint1", "01-03-2023", "15-03-2023");
		SprintDetails sprintDetails2 = getSprintDetails("Sprint2", "09-01-2023", "21-01-2023");
		SprintDetails sprintDetails3 = getSprintDetails("Sprint3", "01-02-2023", "15-03-2023");

		List<SprintDetails> list = new ArrayList<>(Arrays.asList(sprintDetails1, sprintDetails2, sprintDetails3));
		list.sort(JiraHelper.SPRINT_COMPARATOR);
		assertEquals("Sprint3", list.get(0).getSprintName());
	}

	@Test
	public void testBuildFieldMap() {
		List<IssueField> fields = Arrays.asList(new IssueField("1", "Field1", "h", new Object()),
				new IssueField("2", "Field2", "h", new Object()));

		Map<String, IssueField> fieldMap = JiraHelper.buildFieldMap(fields);

		assertEquals(fields.size(), fieldMap.size());
		for (IssueField field : fields) {
			assertTrue(fieldMap.containsKey(field.getId()));
			assertEquals(field, fieldMap.get(field.getId()));
		}
	}

	@Test
	public void testBuildFieldMapWithNull() {
		Map<String, IssueField> fieldMap = JiraHelper.buildFieldMap(null);

		assertNotNull(fieldMap);
		assertTrue(fieldMap.isEmpty());
	}

	@Test
	public void getAffectedVersionsTest() {
		Version v = new Version(null, 1234567L, "v1", "desc", true, false, null);
		Iterable<Version> iterable = new Iterable() {
			@Override
			public Iterator<Version> iterator() {
				return Collections.singletonList(v).iterator();
			}
		};
		when(issue.getAffectedVersions()).thenReturn(iterable);

		assertEquals(1, JiraHelper.getAffectedVersions(issue).size());
	}

	@Test
	public void getFieldValueTest() {
		IssueField issueField1 = new IssueField("123", "fname", "Double", 77d);
		IssueField issueField2 = new IssueField("124", "fname1", "String", "str");
		IssueField issueField3 = new IssueField("125", "fname2", "JSONObject", "Obj");
		Map<String, IssueField> map = new HashMap<>();
		map.put("123", issueField1);
		map.put("1234", issueField2);
		map.put("125", issueField3);
		assertEquals("77.0", JiraHelper.getFieldValue("123", map));
		assertEquals("str", JiraHelper.getFieldValue("1234", map));
		assertEquals("Obj", JiraHelper.getFieldValue("125", map));
	}

	// @Test
	// public void testGetLabelsList() {
	//
	// issue.setLabels(Arrays.asList("Label1", "Label2"));
	//
	// List<String> labels = JiraHelper.getLabelsList(issue);
	//
	// assertEquals(issue.getLabels().size(), labels.size());
	// assertTrue(labels.containsAll(issue.getLabels()));
	// }

	@Test
	public void testGetLabelsListWithNullLabels() {

		List<String> labels = JiraHelper.getLabelsList(issue);

		assertNotNull(labels);
		assertTrue(labels.isEmpty());
	}

	// Add more test methods for other methods in JiraHelper...

	@Test
	public void testSortChangeLogGroup() {

		when(issue.getChangelog()).thenReturn(Arrays.asList(changelogGroup));

		List<ChangelogGroup> sortedChangeLogList = JiraHelper.sortChangeLogGroup(issue);

		assertEquals(Arrays.asList(changelogGroup), sortedChangeLogList);
	}

	@Test
	public void testGetIssuesFromResult() {
		SearchResult searchResult = mock(SearchResult.class);

		when(searchResult.getIssues()).thenReturn(Arrays.asList(issue));

		List<Issue> issues = JiraHelper.getIssuesFromResult(searchResult);

		assertEquals(Arrays.asList(issue), issues);
	}

	@Test
	public void getIssuesFromResultTest() {

		List<Issue> issues = JiraHelper.getIssuesFromResult(null);
		assertEquals(0, issues.size());
	}

	@Test
	public void testHash() {
		String input = "TestInput";
		String hashedValue = JiraHelper.hash(input);

		assertNotNull(hashedValue);
		assertEquals(String.valueOf(Objects.hash(input)), hashedValue);
	}

	@Test(expected = NullPointerException.class)
	public void getAssigneeTest() {
		Map<String, URI> avatarUris = new HashMap<>();
		avatarUris.put("", null);
		User user = new User(null, "uName", "uName", "accId", "123@ss.com", true, null, avatarUris, null);
		JiraHelper.getAssignee(user);
	}

	@Test
	public void getListFromJsonTest() throws JSONException {
		JSONObject jsonObject1 = new JSONObject();
		jsonObject1.put("value", "val1");
		jsonObject1.put("key2", "val2");
		JSONObject jsonObject2 = new JSONObject();
		jsonObject2.put("value", "val3");
		jsonObject2.put("key4", "val4");
		JSONArray array = new JSONArray();
		array.add(jsonObject1);
		array.add(jsonObject2);
		IssueField issueField1 = new IssueField("123", "IssueFieldName", "JSONArray", array);
		assertEquals(2, JiraHelper.getListFromJson(issueField1).size());
		IssueField issueField2 = new IssueField("123", "IssueFieldName", "JSONObject", jsonObject1);
		assertEquals(1, JiraHelper.getListFromJson(issueField2).size());
	}

	@Test
	public void getStatusTest() {
		List<Status> statusList = JiraHelper.getStatus(null);
		assertEquals(0, statusList.size());
	}

	@Test
	public void exceptionBlockProcessTest() {
		RestClientException restClientException1 = new RestClientException(new Throwable(), 401);
		JiraHelper.exceptionBlockProcess(restClientException1);
		RestClientException restClientException2 = new RestClientException(new Throwable(), 500);
		JiraHelper.exceptionBlockProcess(restClientException2);
	}

	@Test
	public void testConvertDateToCustomFormat() {
		long currentTimeMillis = System.currentTimeMillis();
		String formattedDate = JiraHelper.convertDateToCustomFormat(currentTimeMillis);

		assertNotNull(formattedDate);
		// Add assertions based on your expected output format
	}

	public SprintDetails getSprintDetails(String sprintName, String startDate, String endDate) {
		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintName(sprintName);
		sprintDetails.setStartDate(startDate);
		sprintDetails.setEndDate(endDate);

		return sprintDetails;
	}
}
