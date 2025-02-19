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
package com.publicissapient.kpidashboard.common.repository.jira;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

/*
author @shi6
 */
@ExtendWith(SpringExtension.class)
public class SprintRepositoryCustomImplTest {

	@InjectMocks
	private SprintRepositoryCustomImpl sprintRepositoryCustomImpl;

	@Mock
	private MongoOperations operations;

	@Test
	public void testFindByBasicProjectConfigIdInAndStateInOrderByStartDateDesc() {
		// Set up test data
		Set<ObjectId> basicProjectConfigIds = new HashSet<>();
		// Add ObjectIds to basicProjectConfigIds

		List<String> sprintStatusList = Arrays.asList("ACTIVE", "CLOSED");

		long limit = 5; // Set the desired limit
		when(operations.aggregate(any(Aggregation.class), anyString(), any())).thenReturn(mock(AggregationResults.class));

		// Call the method and assert the result
		List<SprintDetails> result = sprintRepositoryCustomImpl
				.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(basicProjectConfigIds, sprintStatusList, limit);

		// Assert the result or perform further verifications
		assertEquals(Collections.emptyList(), result);
	}
}
