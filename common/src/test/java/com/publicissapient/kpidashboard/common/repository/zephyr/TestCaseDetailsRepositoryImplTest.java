/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.common.repository.zephyr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;

/*
author @shi6
 */
@ExtendWith(SpringExtension.class)
public class TestCaseDetailsRepositoryImplTest {

	@Mock
	private MongoTemplate operations;

	@InjectMocks
	TestCaseDetailsRepositoryImpl testCaseDetailsRepository;

	@Test
	public void testFindNonRegressionTestDetails() {
		Map<String, List<String>> mapOfFilters = Collections.singletonMap("projectKey", Arrays.asList("PROJ1", "PROJ2"));

		Map<String, Object> map = new HashMap<>();
		map.put("storyType", Arrays.asList(Pattern.compile("Story")));
		map.put("testCaseStatus", Arrays.asList(Pattern.compile("Passed")));
		map.put("labels", Arrays.asList(Pattern.compile("java")));
		Map<String, Map<String, Object>> uniqueProjectMap = Collections.singletonMap("PROJ1", map);
		when(operations.find(any(Query.class), eq(TestCaseDetails.class))).thenReturn(Collections.emptyList());
		List<TestCaseDetails> result = testCaseDetailsRepository.findNonRegressionTestDetails(mapOfFilters,
				uniqueProjectMap, "criteria");
		testCaseDetailsRepository.findNonRegressionTestDetails(mapOfFilters, uniqueProjectMap, "nin");
		testCaseDetailsRepository.findTestDetails(mapOfFilters, uniqueProjectMap, "criteria");
		testCaseDetailsRepository.findTestDetails(mapOfFilters, uniqueProjectMap, "nin");
		assertEquals(Collections.emptyList(), result);
	}
}
