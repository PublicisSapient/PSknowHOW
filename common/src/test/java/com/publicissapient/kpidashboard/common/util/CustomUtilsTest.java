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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class CustomUtilsTest {

	@Test
	public void testPrimaryKeyGenerator() {
		String key1 = CustomUtils.primaryKeygenerator();
		String key2 = CustomUtils.primaryKeygenerator();

		assertNotNull(key1);
		assertNotNull(key2);
		assertNotEquals(key1, key2);
	}

	@Test
	public void testPrimaryKeyOnName() {
		String name = "Test Name";
		String key = CustomUtils.primaryKeyOnName(name);

		assertEquals("test name", key);
	}

	@Test
	public void testPrimaryKeyOnNameWithMap() {
		Map<String, String> nodeNameMap = new HashMap<>();
		nodeNameMap.put("node1", "Node One");
		nodeNameMap.put("node2", "Node Two");

		String key = CustomUtils.primaryKeyOnName(nodeNameMap);

		assertEquals("node two_node one", key);
	}

	@Test
	public void testPrimaryKeyOnNameWithEmptyMap() {
		Map<String, String> emptyMap = new HashMap<>();

		String key = CustomUtils.primaryKeyOnName(emptyMap);

		assertEquals("", key);
	}
}
