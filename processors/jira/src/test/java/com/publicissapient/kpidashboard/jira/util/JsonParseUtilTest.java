package com.publicissapient.kpidashboard.jira.util;


import com.atlassian.jira.rest.client.api.ExpandableProperty;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.publicissapient.kpidashboard.jira.util.JsonParseUtil;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class JsonParseUtilTest {

    @Test
    public void testParseJsonArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(new JSONObject().put("name", "John"));
        jsonArray.put(new JSONObject().put("name", "Jane"));

        Collection<String> result = JsonParseUtil.parseJsonArray(jsonArray, json -> json.getString("name"));

        assertEquals(Arrays.asList("John", "Jane"), result);
    }

//    @Test
//    public void testParseOptionalExpandableProperty() throws JSONException {
//        JSONObject json = new JSONObject();
//        json.put("size", 2);
//        json.put("items", new JSONArray().put(new JSONObject().put("id", 1)).put(new JSONObject().put("id", 2)));
//
//        ExpandableProperty<Integer> result = JsonParseUtil.parseOptionalExpandableProperty(json, json -> json.getInt("id"));
//
//        assertEquals(2, result.getSize());
//        assertEquals(Arrays.asList(1, 2), result.getItems());
//    }

    @Test
    public void testParseURI() {
        URI result = JsonParseUtil.parseURI("http://example.com");

        assertEquals(URI.create("http://example.com"), result);
    }

    @Test
    public void testParseBasicUser() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", "John");
        json.put("displayName", "John Doe");

        BasicUser result = JsonParseUtil.parseBasicUser(json);

        assertEquals("John Doe", result.getDisplayName());
    }

    @Test
    public void testParseDateTime() throws JSONException {
        String dateTimeString = "2022-01-01T12:00:00.000Z";
        DateTime result = JsonParseUtil.parseDateTime(dateTimeString);

        assertEquals(new DateTime(2022, 1, 1, 17, 30, 0, 0), result);
    }

    @Test
    public void testGetOptionalString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("attributeName", "value");

        String result = JsonParseUtil.getOptionalString(json, "attributeName");

        assertEquals("value", result);
    }

    @Test
    public void testGetOptionalStringWithNullValue() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("attributeName", JSONObject.NULL);

        String result = JsonParseUtil.getOptionalString(json, "attributeName");

        assertNull(result);
    }
}
