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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;

/**
 * @author shi6
 */
@ExtendWith(SpringExtension.class)
public class KanbanCapacityRepositoryImplTest {
	@Mock
	private MongoOperations mongoOperations;

	@InjectMocks
	private KanbanCapacityRepositoryImpl kanbanCapacityRepository;

	@Test
	public void testFindIssuesByType() {
		// Mock data
		Map<String, Object> mapOfFilters = new HashMap<>();
		List<ObjectId> projectList = new ArrayList<>();
		projectList.add(new ObjectId("61d6d4235c76563333369f02"));
		mapOfFilters.put("additionalFilterCapacityList.filterId", Arrays.asList("sqd"));
		mapOfFilters.put("additionalFilterCapacityList.nodeCapacityList.additionalFilterId",
				Arrays.asList("Java_61d6d4235c76563333369f02"));
		mapOfFilters.put("projectId", projectList.stream().distinct().collect(Collectors.toList()));

		String dateFrom = "2022-01-01";
		String dateTo = "2022-01-10";

		// Mock behavior
		when(mongoOperations.find(any(Query.class), eq(KanbanCapacity.class))).thenReturn(Collections.emptyList());

		// Test
		kanbanCapacityRepository.findIssuesByType(mapOfFilters, dateFrom, dateTo);

		// Verify that the find method is called with the correct parameters
		verify(mongoOperations, times(1)).find(any(Query.class), eq(KanbanCapacity.class));
	}

	@Test
	public void testFindByFilterMapAndDate() {
		// Mock data
		Map<String, String> mapOfFilters = new HashMap<>();
		mapOfFilters.put("config1", "date1");
		String dateFrom = "2022-01-01";

		// Mock behavior
		when(mongoOperations.find(any(Query.class), eq(KanbanCapacity.class))).thenReturn(Collections.emptyList());

		// Test
		kanbanCapacityRepository.findByFilterMapAndDate(mapOfFilters, dateFrom);

		// Verify that the find method is called with the correct parameters
		verify(mongoOperations, times(1)).find(any(Query.class), eq(KanbanCapacity.class));
	}
}
