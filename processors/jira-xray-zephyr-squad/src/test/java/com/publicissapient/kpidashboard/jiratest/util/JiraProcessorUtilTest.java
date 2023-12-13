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

package com.publicissapient.kpidashboard.jiratest.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;

@ExtendWith(SpringExtension.class)
public class JiraProcessorUtilTest {

	@Test
	public void createJql() {
		String result = "project IN ('XYZ') AND ((issuetype IN ('Test1') AND updatedDate>='2020-08-24') OR"
				+ " (issuetype IN ('Test2') AND updatedDate>='2020-08-23')) ORDER BY updated DESC";

		Map<String, String> startDateTimeStrByIssueType = new LinkedHashMap<>();
		startDateTimeStrByIssueType.put("Test1", "2020-08-24");
		startDateTimeStrByIssueType.put("Test2", "2020-08-23");
		String actual = JiraProcessorUtil.createJql("XYZ", startDateTimeStrByIssueType);
		Assert.assertEquals(result, actual);

	}

	@Test
	public void createJql_Null() {
		String actual = JiraProcessorUtil.createJql(null, null);
		Assert.assertEquals("", actual);

	}

	@Test
	public void deodeUTF8String() throws URISyntaxException {
		BasicUser basicUser = new BasicUser(new URI("self"), "testUser", "testUser", "accountId");
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
		fieldMapping.setJiraDorKPI171(new ArrayList<>());
		Object jiraResponse = fieldMapping.getJiraDorKPI171();
		assertNotNull(JiraProcessorUtil.deodeUTF8String(jiraResponse));

	}

	@Test
	public void deodeUTF8StringEmptyNull() throws URISyntaxException {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setJiraDorKPI171(null);
		Object jiraResponse = fieldMapping.getJiraDorKPI171();
		assertNotNull(JiraProcessorUtil.deodeUTF8String(jiraResponse));

	}

	@Test
	public void getFormattedDate() {
		String date = "07-09-2021";
		assertNotNull(JiraProcessorUtil.getFormattedDate(date));

	}

}