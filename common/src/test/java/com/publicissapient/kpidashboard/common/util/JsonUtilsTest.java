package com.publicissapient.kpidashboard.common.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JsonUtilsTest {

	String simpleString = "This is a test string";

	String complexString = "com.atlassian.greenhopper.service.sprint.Sprint@77d08fb0[id=36564,rapidViewId=11856,state=CLOSED]";
	String jsonStringObject = "{\"name\":\"JohnSmith\",\"sku\":\"20223\",\"price\":23.95,\"shipTo\":{\"name\":\"JaneSmith\",\"address\":\"123MapleStreet\",\"city\":\"Pretendville\",\"state\":\"NY\",\"zip\":\"12345\"},\"billTo\":{\"name\":\"JohnSmith\",\"address\":\"123MapleStreet\",\"city\":\"Pretendville\",\"state\":\"NY\",\"zip\":\"12345\"}}";
	String jsonStringArray = "[\"Test1\",\"Test2\",\"Test2\"]";
	String jsonStringArrayOfObjects = "[{\"name\":\"Ram\",\"email\":\"ram@gmail.com\",\"age\":23},{\"name\":\"Shyam\",\"email\":\"shyam23@gmail.com\",\"age\":28},{\"name\":\"John\",\"email\":\"john@gmail.com\",\"age\":33},{\"name\":\"Bob\",\"email\":\"bob32@gmail.com\",\"age\":41}]";
	String jsonComplexObject = "{\"name\":\"John\",\"age\":30,\"cars\":[\"CarOne\",\"CarTwo\",\"CarThree\"],\"address\":{\"houseNumber\":12,\"isHome\":true,\"city\":\"testcity\"}}";

	@Test
	public void isJSONValid_ValidObject() {
		boolean isValid = JsonUtils.isValidJSON(jsonStringObject);
		assertTrue(isValid);
	}

	@Test
	public void isJSONValid_ValidArray() {
		boolean isValid = JsonUtils.isValidJSON(jsonStringArray);
		assertTrue(isValid);
	}

	@Test
	public void isJSONValid_ValidArrayOfObjects() {
		boolean isValid = JsonUtils.isValidJSON(jsonStringArrayOfObjects);
		assertTrue(isValid);
	}

	@Test
	public void isJSONValid_ValidComplexObject() {
		boolean isValid = JsonUtils.isValidJSON(jsonComplexObject);
		assertTrue(isValid);
	}

	@Test
	public void isJSONValid_InvalidSimpleString() {
		boolean isValid = JsonUtils.isValidJSON(simpleString);
		assertFalse(isValid);
	}

	@Test
	public void isJSONValid_InvalidComplexString() {
		boolean isValid = JsonUtils.isValidJSON(complexString);
		assertFalse(isValid);
	}

}