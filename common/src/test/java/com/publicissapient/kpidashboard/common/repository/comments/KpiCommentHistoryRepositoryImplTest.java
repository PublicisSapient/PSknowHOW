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

package com.publicissapient.kpidashboard.common.repository.comments;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.comments.KpiCommentsHistory;

/**
 * @author shi6
 */
@ExtendWith(SpringExtension.class)
public class KpiCommentHistoryRepositoryImplTest {
	@Mock
	private MongoTemplate mongoTemplate;

	@InjectMocks
	private KpiCommentHistoryRepositoryImpl kpiCommentHistoryRepository;

	@Test
	public void testMarkCommentDelete() {
		// Mock data
		String commentId = "testCommentId";

		// Test
		kpiCommentHistoryRepository.markCommentDelete(commentId);

		// Verify that the updateMulti method is called with the correct parameters
		verify(mongoTemplate, times(1)).updateMulti(any(Query.class), any(Update.class), eq(KpiCommentsHistory.class));
	}
}
