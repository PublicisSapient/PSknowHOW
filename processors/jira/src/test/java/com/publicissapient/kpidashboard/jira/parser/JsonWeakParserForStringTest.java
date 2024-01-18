//package com.publicissapient.kpidashboard.jira.parser;
//
//import org.codehaus.jettison.json.JSONException;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.junit.MockitoJUnitRunner;
//
//@RunWith(MockitoJUnitRunner.class)
//public class JsonWeakParserForStringTest {
//
//	@InjectMocks
//	JsonWeakParserForString jsonWeakParserForString;
//
//	@Test
//	public void testParseWithStringInput() throws JSONException {
//		String inputString = "TestString";
//
//		try {
//			String result = jsonWeakParserForString.parse(inputString);
//			Assert.assertEquals("Parsed string should be equal to input string", inputString, result);
//		} catch (JSONException e) {
//			Assert.fail("Unexpected JSONException: " + e.getMessage());
//		}
//	}
//
//	@Test(expected = JSONException.class)
//	public void testParseWithNonStringInput() throws JSONException {
//		Integer nonStringInput = 123;
//
//		jsonWeakParserForString.parse(nonStringInput);
//		// If the code reaches this point, the test should fail because it should throw a JSONException.
//	}
//}
