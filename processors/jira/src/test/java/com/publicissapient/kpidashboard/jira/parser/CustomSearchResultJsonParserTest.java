package com.publicissapient.kpidashboard.jira.parser;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.json.GenericJsonArrayParser;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class CustomSearchResultJsonParserTest {
	@Test
	public void testParse() throws JSONException {
		// Arrange
		JSONObject json = new JSONObject();
		json.put("startAt", 0);
		json.put("id", 0);
		json.put("expand", "");
		json.put("fields", new JSONObject());
		json.put("summary", new Object());
		json.put("maxResults", 10);
		json.put("total", 2);
		json.put("schema", new JSONObject());
		json.put("names", new JSONObject());
		json.put("self", "http://example.com/rest/api/2/user/101");
		JSONArray issuesJsonArray = new JSONArray();
		json.put("name", "John Doe");
		json.put("key", "johndoe");
        issuesJsonArray.put(json);

		json.put("issues", issuesJsonArray);
		GenericJsonArrayParser<Issue> mockIssuesParser = Mockito.mock(GenericJsonArrayParser.class);
		//when(mockIssuesParser.parse(issuesJsonArray)).thenReturn(Collections.emptyList()); // Adjust based on your needs

		// Inject the mocked parser into CustomSearchResultJsonParser
		CustomSearchResultJsonParser parser = new CustomSearchResultJsonParser();

		// Act
		//SearchResult result = parser.parse(json);
		// Assert
		//assertEquals(10, result.getMaxResults());
	}

	@Test
	public void testParse1() throws JSONException {
		// Arrange
		JSONObject json = new JSONObject();
		json.put("startAt", 0);
		json.put("maxResults", 10);
		json.put("total", 2);
		json.put("names", 1);
		JSONArray issuesJsonArray = new JSONArray();

		json.put("issues", issuesJsonArray);

		CustomSearchResultJsonParser parser = new CustomSearchResultJsonParser();

		// Act
		SearchResult result = parser.parse(json);

		// Assuming you have properly implemented CustomIssueJsonParser
		assertNotNull(result.getIssues());
		assertEquals(0, result.getIssues().spliterator().getExactSizeIfKnown());
	}
}
