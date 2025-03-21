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

package com.publicissapient.kpidashboard.jira.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.jira.dataFactories.IssueDataFactory;

@RunWith(MockitoJUnitRunner.class)
public class CustomSearchResultJsonParserTest {

	@InjectMocks
	CustomSearchResultJsonParser customSearchResultJsonParser;

	private List<Issue> issues;

	private List<Object> objects;

	@Before
	public void setUp() {
		objects = getMockIssues();
	}

	@Test
	public void testParse() throws JSONException {
		// Mock data for your JSONObject
		JSONObject json = createMockJson();

		// when(customSearchResultJsonParser.parse(json)).thenReturn(createMockIssueIterable());

		// Set up the parser with the mocked issueParser
		// customSearchResultJsonParser.setIssueParser(issueParser);

		// Call the method to test
		SearchResult result = customSearchResultJsonParser.parse(json);

		// Assert the result
		assertNotNull(result);
		assertEquals(1, result.getStartIndex());
		assertEquals(5, result.getMaxResults());
		assertEquals(2, result.getTotal());
		assertNotNull(result.getIssues());
	}

	private JSONObject createMockJson() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("startAt", 1);
		json.put("maxResults", 5);
		json.put("total", 2);

		JSONArray issuesArray = new JSONArray();
		issuesArray = convertObjectListToJsonArray(objects);
		// Add more issues as needed

		json.put("issues", issuesArray);

		JSONObject namesObject = new JSONObject();
		namesObject.put("key", "Issue Key");
		namesObject.put("summary", "Summary");

		json.put("names", namesObject);

		JSONObject schemaObject = new JSONObject();
		schemaObject.put("fields", createMockFieldJson("summary", "Summary", "main"));

		json.put("schema", schemaObject);

		return json;
	}

	public static JSONArray convertObjectListToJsonArray(List<Object> objectList) throws JSONException {
		ObjectMapper mapper = new ObjectMapper();

		// Convert List<Object> to JSON string
		String jsonString;
		try {
			jsonString = mapper.writeValueAsString(objectList);
		} catch (IOException e) {
			throw new RuntimeException("Error converting List<Object> to JSON string", e);
		}

		// Convert JSON string to JSONArray
		return new JSONArray(jsonString);
	}

	private JSONObject createMockFieldJson(String id, String name, String type) throws JSONException {
		JSONObject fieldJson = new JSONObject();
		fieldJson.put("id", id);
		fieldJson.put("name", name);

		fieldJson.put("type", type);
		// Add more properties as needed
		return fieldJson;
	}

	private List<Object> getMockIssues() {
		IssueDataFactory issueDataFactory = IssueDataFactory.newInstance("/json/default/issues.json");
		return issueDataFactory.getIssues();
	}
}
