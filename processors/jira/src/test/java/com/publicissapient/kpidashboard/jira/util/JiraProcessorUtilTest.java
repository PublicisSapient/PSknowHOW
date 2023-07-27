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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;

/**
 * @author anisingh4
 */
@ExtendWith(SpringExtension.class)
public class JiraProcessorUtilTest {

	@Test
	public void createJql() {
		String result = "project IN ('TEST') AND ((issuetype IN ('Story') AND updatedDate>='2020-08-24') OR (issuetype IN ('Defect') AND updatedDate>='2020-08-23')) ORDER BY updated DESC";

		Map<String, String> startDateTimeStrByIssueType = new LinkedHashMap<>();
		startDateTimeStrByIssueType.put("Story", "2020-08-24");
		startDateTimeStrByIssueType.put("Defect", "2020-08-23");
		String actual = JiraProcessorUtil.createJql("TEST", startDateTimeStrByIssueType);// TODO resolve
		Assert.assertEquals(result, actual);

	}

	@Test
	public void createJql_Null() {
		String actual = JiraProcessorUtil.createJql(null, null);// TODO resolve
		Assert.assertEquals("", actual);

	}

	/**
	 * process jql while data not exist in db and updated date present in query
	 * 
	 */
	@Test
	public void processJql_dataNotExist2() {
		String query = "project IN ('TEST') and updatedDate > '20-21-22' ORDER BY updated DESC";
		String expected = "project in ('test') and updateddate > '20-21-22'   ORDER BY updated DESC";
		Map<String, String> startDateTimeStrByIssueType = new LinkedHashMap<>();
		startDateTimeStrByIssueType.put("Story", "2020-08-24");
		startDateTimeStrByIssueType.put("Defect", "2020-08-23");
		String actual = JiraProcessorUtil.processJql(query, startDateTimeStrByIssueType, false);// TODO resolve
		assertEquals(expected, actual);
	}

	@Test
	public void deodeUTF8String() throws URISyntaxException {
		BasicUser basicUser = new BasicUser(new URI("self"), "basicuser", "basicuser", "accountId");
		Object jiraResponse = basicUser;
		assertNotNull(JiraProcessorUtil.deodeUTF8String(jiraResponse));

	}

	@Test
	public void deodeUTF8StringNull() {
		Object jiraResponse = null;
		assertNotNull(JiraProcessorUtil.deodeUTF8String(jiraResponse));

	}

	@Test
	public void deodeUTF8StringEmpty() throws URISyntaxException {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setJiraDorKPI3("");
		Object jiraResponse = fieldMapping.getJiraDorKPI3();
		assertNotNull(JiraProcessorUtil.deodeUTF8String(jiraResponse));

	}

	@Test
	public void deodeUTF8StringEmptyNull() throws URISyntaxException {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setJiraDorKPI3(null);
		Object jiraResponse = fieldMapping.getJiraDorKPI3();
		assertNotNull(JiraProcessorUtil.deodeUTF8String(jiraResponse));

	}

	@Test
	public void getFormattedDate() {
		String date = "07-09-2021";
		assertNotNull(JiraProcessorUtil.getFormattedDate(date));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void processSprintDetail() throws ParseException, JSONException {
		List<String> list = new ArrayList<String>();
		list.add("User1");
		list.add("User2");
		list.add("User3");
		JSONArray array = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			JSONObject d = new JSONObject();
			d.put(i, list.get(i));
			array.add(d);
		}
		Object data = array;

		assertNotNull(JiraProcessorUtil.processSprintDetail(data));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void processSprintDetailNull() throws ParseException, JSONException {
		List<String> list = new ArrayList<String>();
		list.add("User1");
		list.add("User2");
		list.add(null);
		JSONArray array = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			JSONObject d = new JSONObject();
			d.put(i, list.get(i));
			array.add(d);
		}
		Object data = array;

		assertNotNull(JiraProcessorUtil.processSprintDetail(data));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void processSprintDetail1() throws ParseException, JSONException {
		List<String> list = new ArrayList<String>();
		list.add("User1");
		list.add("User2");
		list.add("User3");
		org.codehaus.jettison.json.JSONArray array = new org.codehaus.jettison.json.JSONArray();
		for (int i = 0; i < list.size(); i++) {
			JSONObject d = new JSONObject();
			d.put(i, list.get(i));
			array.put(d);
		}
		Object data = array;

		assertNotNull(JiraProcessorUtil.processSprintDetail(data));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void processSprintDetail1Null() throws ParseException, JSONException {
		List<String> list = new ArrayList<String>();
		list.add("User1");
		list.add("User2");
		list.add(null);
		org.codehaus.jettison.json.JSONArray array = new org.codehaus.jettison.json.JSONArray();
		for (int i = 0; i < list.size(); i++) {
			JSONObject d = new JSONObject();
			d.put(i, list.get(i));
			array.put(d);
		}
		Object data = array;

		assertNotNull(JiraProcessorUtil.processSprintDetail(data));

	}

	@Test
	public void getFormattedDateTime() {
		String date = "2022-08-18T06:35:15.0000000";
		assertNotNull(JiraProcessorUtil.getFormattedDateTime(date));

	}

	@SuppressWarnings("deprecation")
	@Test
	public void getFormattedDateString() {
		Date date = new Date();
		date.getDate();
		assertNotNull(JiraProcessorUtil.getFormattedDateString(date));

	}

	@SuppressWarnings("deprecation")
	@Test
	public void getTimeAdjustedDate() {
		Date date = new Date();
		date.getDate();
		int minutes = 30;
		assertNotNull(JiraProcessorUtil.getTimeAdjustedDate(date, minutes));

	}

	@Test
	void processJqlForSprintFetch() {
		// Arrange
		List<String> issueKeys = Arrays.asList("KEY-1", "KEY-2", "KEY-3");
		String expected = "issueKey in (KEY-1, KEY-2, KEY-3)";

		// Act
		String actual = JiraProcessorUtil.processJqlForSprintFetch(issueKeys);

		// Assert
		assertEquals(expected, actual);
	}
}