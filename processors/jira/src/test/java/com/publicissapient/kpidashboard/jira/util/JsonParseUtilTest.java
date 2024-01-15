//package com.publicissapient.kpidashboard.jira.util;
//
//import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
//import org.joda.time.DateTime;
//import org.json.JSONException;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.net.URI;
//import java.util.Collection;
//import org.codehaus.jettison.json.JSONArray;
//import org.codehaus.jettison.json.JSONObject;
//
//import static org.junit.Assert.assertEquals;
//
//@RunWith(MockitoJUnitRunner.class)
//public class JsonParseUtilTest {
//    @Mock
//    private JsonObjectParser<Object> jsonObjectParser;
//
//    @InjectMocks
//    private JsonParseUtil jsonParseUtil;
//
//    @Test
//    public void testParseJsonArray_SuccessfulParsing() throws JSONException {
//        // Arrange
//        JSONArray jsonArray = new JSONArray("[{\"key\":\"value1\"}, {\"key\":\"value2\"}]");
//
//        // Act
//        Collection<Object> result = jsonParseUtil.parseJsonArray(jsonArray, jsonObjectParser);
//
//        // Assert
//        assertEquals(2, result.size());
//        // Additional assertions based on your actual implementation
//    }
//
//    @Test
//    public void testOptSelfUri_ValidUri() throws JSONException {
//        // Arrange
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.("self", "https://example.com");
//
//        // Act
//        URI result = jsonParseUtil.optSelfUri(jsonObject, URI.create("https://default.com"));
//
//        // Assert
//        assertEquals(URI.create("https://example.com"), result);
//    }
//
//    @Test
//    public void testParseDateTime_SuccessfulParsing() throws JSONException {
//        // Arrange
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("dateAttribute", "2022-01-01T12:00:00.000Z");
//
//        // Act
//        DateTime result = jsonParseUtil.parseDateTime(jsonObject, "dateAttribute");
//
//        // Assert
//        // Additional assertions based on your actual implementation
//    }
//
//    @Test
//    public void testGetOptionalString_StringPresent() throws JSONException {
//        // Arrange
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("attributeName", "value");
//
//        // Act
//        String result = jsonParseUtil.getOptionalString(jsonObject, "attributeName");
//
//        // Assert
//        assertEquals("value", result);
//    }
//
//    @Test
//    public void testGetOptionalString_StringNotPresent() throws JSONException {
//        // Arrange
//        JSONObject jsonObject = new JSONObject();
//
//        // Act
//        String result = jsonParseUtil.getOptionalString(jsonObject, "nonExistentAttribute");
//
//        // Assert
//        assertEquals(null, result);
//    }
//
//}
