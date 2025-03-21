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

package com.publicissapient.kpidashboard.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class PropertyUtilsTest {

	@Test
	public void testTrimProps() throws NoSuchFieldException, IllegalAccessException {
		PropertyUtils propertyUtils = new PropertyUtils();

		// Create a mock object with fields
		TestObject testObject = new TestObject();
		testObject.setStringField("Test String");
		testObject.setListField(createListWithSpaces());
		testObject.setMapField(createMapWithSpaces());
		testObject.setNestedMapField(createNestedMapWithSpaces());

		// Get all fields including private fields
		Field[] fields = TestObject.class.getDeclaredFields();

		// Trim the properties
		propertyUtils.trimProps(fields, testObject);

		// Assert that the properties are trimmed
		assertEquals("Test String", testObject.getStringField());

		List<String> expectedList = createListWithSpaces();
		List<String> actualList = testObject.getListField();
		assertCollectionsEqual(expectedList, actualList);

		Map<String, String> expectedMap = createMapWithSpaces();
		Map<String, String> actualMap = testObject.getMapField();
		assertMapsEqual(expectedMap, actualMap);

		Map<String, List<String>> expectedNestedMap = createNestedMapWithSpaces();
		Map<String, List<String>> actualNestedMap = testObject.getNestedMapField();
		assertNestedMapsEqual(expectedNestedMap, actualNestedMap);
	}

	private void assertCollectionsEqual(Collection<String> expected, Collection<String> actual) {
		assertEquals(expected.size(), actual.size());
		Iterator<String> expectedIterator = expected.iterator();
		Iterator<String> actualIterator = actual.iterator();
		while (expectedIterator.hasNext() && actualIterator.hasNext()) {
			assertEquals(expectedIterator.next(), actualIterator.next());
		}
	}

	private void assertMapsEqual(Map<String, String> expected, Map<String, String> actual) {
		assertEquals(expected.size(), actual.size());
		for (Map.Entry<String, String> entry : expected.entrySet()) {
			assertEquals(entry.getValue(), actual.get(entry.getKey()));
		}
	}

	private void assertNestedMapsEqual(Map<String, List<String>> expected, Map<String, List<String>> actual) {
		assertEquals(expected.size(), actual.size());
		for (Map.Entry<String, List<String>> entry : expected.entrySet()) {
			List<String> expectedList = entry.getValue();
			List<String> actualList = actual.get(entry.getKey());
			assertCollectionsEqual(expectedList, actualList);
		}
	}

	private List<String> createListWithSpaces() {
		List<String> list = new ArrayList<>();
		list.add("Item1");
		list.add("Item2");
		return list;
	}

	private Map<String, String> createMapWithSpaces() {
		Map<String, String> map = new HashMap<>();
		map.put("Key1", "Value1");
		map.put("Key2", "Value2");
		return map;
	}

	private Map<String, List<String>> createNestedMapWithSpaces() {
		Map<String, List<String>> nestedMap = new HashMap<>();
		List<String> nestedList1 = createListWithSpaces();
		List<String> nestedList2 = createListWithSpaces();
		nestedMap.put("NestedKey1", nestedList1);
		nestedMap.put("NestedKey2", nestedList2);
		return nestedMap;
	}

	// Class to test
	private static class TestObject {
		private String stringField;
		private List<String> listField;
		private Map<String, String> mapField;
		private Map<String, List<String>> nestedMapField;

		public String getStringField() {
			return stringField;
		}

		public void setStringField(String stringField) {
			this.stringField = stringField;
		}

		public List<String> getListField() {
			return listField;
		}

		public void setListField(List<String> listField) {
			this.listField = listField;
		}

		public Map<String, String> getMapField() {
			return mapField;
		}

		public void setMapField(Map<String, String> mapField) {
			this.mapField = mapField;
		}

		public Map<String, List<String>> getNestedMapField() {
			return nestedMapField;
		}

		public void setNestedMapField(Map<String, List<String>> nestedMapField) {
			this.nestedMapField = nestedMapField;
		}
	}
}
