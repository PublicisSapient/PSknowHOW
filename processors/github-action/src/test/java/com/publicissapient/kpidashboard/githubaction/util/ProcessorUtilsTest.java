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

package com.publicissapient.kpidashboard.githubaction.util;

import static org.junit.Assert.assertEquals;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ProcessorUtilsTest {

	@SuppressWarnings("java:S2699")
	@Test
	void firstCulprit() {
		JSONObject d = new JSONObject();
		ProcessorUtils.authorName(d);
	}

	@Test
	void getString() {
		JSONObject obj = new JSONObject();
		obj.put("id", 1);
		obj.put("html_url", "https://test.com/testUser/testProject/actions/runs/956576842");
		assertEquals("https://test.com/testUser/testProject/actions/runs/956576842",
				ProcessorUtils.getString(obj, "html_url"));
	}

	@Test
	void getJsonArray() {
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray();
		array.add(1);
		array.add(2);
		obj.put("id", 1);
		obj.put("html_url", "https://test.com/testUser/testProject/actions/runs/956576842");
		obj.put("workflow_id", array);
		assertEquals(array, ProcessorUtils.getJsonArray(obj, "workflow_id"));
	}

}
