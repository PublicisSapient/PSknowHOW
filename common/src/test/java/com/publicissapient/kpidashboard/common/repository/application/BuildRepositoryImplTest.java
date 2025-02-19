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

package com.publicissapient.kpidashboard.common.repository.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.application.Build;

/**
 * @author shi6
 */
@ExtendWith(SpringExtension.class)
public class BuildRepositoryImplTest {
	@Mock
	private MongoOperations mongoOperations;

	@InjectMocks
	private BuildRepositoryImpl buildRepository;

	@Test
	public void testFindBuildList() {
		// Mock data
		Map<String, List<String>> mapOfFilters = new HashMap<>();
		mapOfFilters.put("status", Arrays.asList("SUCCESS", "FAILURE"));
		Set<ObjectId> projectBasicConfigIds = new HashSet<>();
		projectBasicConfigIds.add(new ObjectId("61d6d4235c76563333369f02"));
		projectBasicConfigIds.add(new ObjectId("61d6d4235c76563333369f01"));
		String startDate = "2022-01-01T00:00:00Z";
		String endDate = "2022-01-10T00:00:00Z";

		// Mock behavior
		when(mongoOperations.find(any(Query.class), eq(Build.class))).thenReturn(Collections.emptyList());

		// Test
		buildRepository.findBuildList(mapOfFilters, projectBasicConfigIds, startDate, endDate);

		// Assertions or verifications
		verify(mongoOperations, times(1)).find(any(Query.class), eq(Build.class));
	}

	@Test
	public void testFindBuildListWithEmptyFilters() {
		// Mock data with empty filters
		Map<String, List<String>> emptyFilters = new HashMap<>();
		Set<ObjectId> projectBasicConfigIds = new HashSet<>();
		projectBasicConfigIds.add(new ObjectId("61d6d4235c76563333369f02"));
		projectBasicConfigIds.add(new ObjectId("61d6d4235c76563333369f01"));
		String startDate = "2022-01-01T00:00:00Z";
		String endDate = "2022-01-10T00:00:00Z";

		// Mock behavior
		when(mongoOperations.find(any(Query.class), eq(Build.class))).thenReturn(Collections.emptyList());

		// Test
		buildRepository.findBuildList(emptyFilters, projectBasicConfigIds, startDate, endDate);

		// Assertions or verifications
		verify(mongoOperations, times(1)).find(any(Query.class), eq(Build.class));
	}
}
