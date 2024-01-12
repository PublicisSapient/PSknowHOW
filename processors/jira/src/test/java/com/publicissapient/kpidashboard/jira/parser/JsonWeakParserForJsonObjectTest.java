package com.publicissapient.kpidashboard.jira.parser;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JsonWeakParserForJsonObjectTest {

	@InjectMocks
	JsonWeakParserForJsonObject jsonWeakParserForJsonObject;

	@Mock
	private JsonObjectParser<String> mockJsonObjectParser;

	@Test
	public void testParseWithJsonObjectInput() throws JSONException {
		MockitoAnnotations.openMocks(this);

		JsonWeakParserForJsonObject<String> parser = new JsonWeakParserForJsonObject<>(mockJsonObjectParser);
		JSONObject jsonObject = new JSONObject();

		try {
			parser.parse(jsonObject);
			Mockito.verify(mockJsonObjectParser).parse(jsonObject);
		} catch (JSONException e) {
			Assert.fail("Unexpected JSONException: " + e.getMessage());
		}
	}

	@Test(expected = JSONException.class)
	public void testParseWithNonJsonObjectInput() throws JSONException {
		MockitoAnnotations.openMocks(this);

		JsonWeakParserForJsonObject<String> parser = new JsonWeakParserForJsonObject<>(mockJsonObjectParser);
		String nonJsonObjectInput = "TestString";

		parser.parse(nonJsonObjectInput);
		// If the code reaches this point, the test should fail because it should throw a JSONException.
	}


}
