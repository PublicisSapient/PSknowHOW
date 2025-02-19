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
package com.publicissapient.kpidashboard.common.repository.scm;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mongodb.BasicDBList;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;

/*
author @shi6
 */
@ExtendWith(SpringExtension.class)
public class CommitRepositoryCustomImplTest {

	@InjectMocks
	private CommitRepositoryCustomImpl commitRepositoryCustomImpl;

	@Mock
	private MongoTemplate mongoTemplate;

	@Test
	public void testFindCommitList() {
		// Set up test data
		Document connection = new Document("httpUrl", "http://example.com").append("baseUrl", "http://baseurl.com");
		Document projectToolConfigProcessorItemDocument = new Document("_id", "123").append("basicProjectConfigId", "456")
				.append("toolName", "SampleTool").append("branch", "main").append("repoSlug", "sample-repo")
				.append("repositoryName", "Sample Repository").append("processorItemList", Arrays.asList("item1", "item2"))
				.append("connection", Arrays.asList(connection));

		List<Document> documents = Arrays.asList(projectToolConfigProcessorItemDocument);

		List<ObjectId> collectorItemIdList = Arrays.asList(new ObjectId("5fd9ab0995fe13000165d0ba"),
				new ObjectId("5fd9ab0995fe13000165d0bb"));
		Long startDate = System.currentTimeMillis() - 86400000; // 24 hours ago
		Long endDate = System.currentTimeMillis();
		BasicDBList filterList = new BasicDBList();
		// Add filter criteria to filterList

		// Mock the MongoTemplate
		MongoCollection mock = mock(MongoCollection.class);
		AggregateIterable aggregate = mock(AggregateIterable.class);
		MongoCursor mongocursor = mock(MongoCursor.class);

		when(mongocursor.hasNext()).thenReturn(true, false);
		when(mongocursor.next()).thenReturn(documents.get(0), (Document) null);

		MongoConverter converter = mock(MongoConverter.class);
		doReturn(mock).when(mongoTemplate).getCollection(anyString());
		doReturn(aggregate).when(mock).aggregate(anyList());
		doReturn(mongocursor).when(aggregate).iterator();
		doReturn(converter).when(mongoTemplate).getConverter();
		doReturn(new CommitDetails()).when(converter).read(eq(CommitDetails.class), any(Document.class));

		// Call the method and assert the result
		List<CommitDetails> result = commitRepositoryCustomImpl.findCommitList(collectorItemIdList, startDate, endDate,
				filterList);

		// Assert the result or perform further verifications
		assertNotNull(result);
		// Additional assertions or verifications based on the actual logic of the
		// method
	}
}
