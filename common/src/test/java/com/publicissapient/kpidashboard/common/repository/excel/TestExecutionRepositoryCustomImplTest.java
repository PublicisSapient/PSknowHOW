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

package com.publicissapient.kpidashboard.common.repository.excel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.testexecution.TestExecution;

/**
 * @author shi6
 */
@ExtendWith(SpringExtension.class)
public class TestExecutionRepositoryCustomImplTest {
	@Mock
	private MongoOperations mongoOperations;

	@InjectMocks
	private TestExecutionRepositoryCustomImpl testExecutionRepository;

	@Test
	public void testFindTestExecutionDetailByFilters() {
		// Mock data
		Map<String, List<String>> mapOfFilters = new HashMap<>();
		List<String> sprintList = Arrays.asList("sprint1", "sprint2");
		List<String> basicProjectConfigIds = Arrays.asList("config1");
		mapOfFilters.put("sprint_id", sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put("basicConfigId", basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Object> mapOfFilter = new HashMap<>();
		mapOfFilter.put("sprint_id", sprintList.stream().distinct().collect(Collectors.toList()));
		mapOfFilter.put("basicConfigId", basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
		uniqueProjectMap.put("config1", mapOfFilter);
		// Mock behavior
		when(mongoOperations.find(any(Query.class), eq(TestExecution.class))).thenReturn(Collections.emptyList());

		// Test
		List<TestExecution> result = testExecutionRepository.findTestExecutionDetailByFilters(mapOfFilters,
				uniqueProjectMap);

		// Verify that the find method is called with the correct parameters
		verify(mongoOperations, times(1)).find(any(Query.class), eq(TestExecution.class));
	}
}
