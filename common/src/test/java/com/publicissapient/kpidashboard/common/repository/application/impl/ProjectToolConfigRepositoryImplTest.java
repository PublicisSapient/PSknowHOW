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

package com.publicissapient.kpidashboard.common.repository.application.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfigProcessorItem;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;

/**
 * @author shi6
 */
@ExtendWith(SpringExtension.class)
public class ProjectToolConfigRepositoryImplTest {

	@Mock
	private MongoOperations mongoOperations;

	@InjectMocks
	private ProjectToolConfigRepositoryImpl projectToolConfigRepository;

	@Test
	public void testGetToolListWithResults() {
		// Mock data
		Document connection = new Document("httpUrl", "http://example.com").append("baseUrl", "http://baseurl.com");

		Document projectToolConfigProcessorItemDocument = new Document("_id", "123").append("basicProjectConfigId", "456")
				.append("toolName", "SampleTool").append("branch", "main").append("repoSlug", "sample-repo")
				.append("repositoryName", "Sample Repository").append("processorItemList", Arrays.asList("item1", "item2"))
				.append("connection", Arrays.asList(connection));

		List<Document> documents = Arrays.asList(projectToolConfigProcessorItemDocument);

		AggregateIterable<Document> aggregateIterable = mock(AggregateIterable.class);
		MongoCursor<Document> mongoCursor = mock(MongoCursor.class);
		when(aggregateIterable.iterator()).thenReturn(mongoCursor);
		when(mongoCursor.hasNext()).thenReturn(true, false);
		when(mongoCursor.next()).thenReturn(documents.get(0), (Document) null);

		MongoCollection<Document> collection = mock(MongoCollection.class);
		when(mongoOperations.getCollection(anyString())).thenReturn(collection);
		MongoConverter mock = mock(MongoConverter.class);
		when(mongoOperations.getConverter()).thenReturn(mock);

		// Mock behavior
		when(mongoOperations.getCollection("project_tool_configs").aggregate(any(List.class)))
				.thenReturn(aggregateIterable);
		doReturn(createNewProcessorItem()).when(mock).read(eq(ProjectToolConfigProcessorItem.class), any(Document.class));

		// Test
		projectToolConfigRepository.getToolList();
	}

	private ProjectToolConfigProcessorItem createNewProcessorItem() {
		ProjectToolConfigProcessorItem projectToolConfigProcessorItem = new ProjectToolConfigProcessorItem();
		projectToolConfigProcessorItem.setToolName("tool");
		projectToolConfigProcessorItem.setBasicProjectConfigId(new ObjectId("61d6d4235c76563333369f02"));
		projectToolConfigProcessorItem.setConnectionId(new ObjectId("61d6d4235c74563333369f02"));
		projectToolConfigProcessorItem.setProjectId("project1");
		projectToolConfigProcessorItem.setProjectKey("project1");
		projectToolConfigProcessorItem.setJobName("project1");
		projectToolConfigProcessorItem.setBranch("project1");
		projectToolConfigProcessorItem.setEnv("project1");
		projectToolConfigProcessorItem.setRepoSlug("project1");
		projectToolConfigProcessorItem.setRepositoryName("project1");
		projectToolConfigProcessorItem.setRepositoryName("project1");
		projectToolConfigProcessorItem.setBitbucketProjKey("project1");
		projectToolConfigProcessorItem.setApiVersion("project1");
		projectToolConfigProcessorItem.setNewRelicApiQuery("project1");
		projectToolConfigProcessorItem.setUpdatedAt("project1");
		projectToolConfigProcessorItem.setCreatedAt("project1");
		projectToolConfigProcessorItem.setQueryEnabled(Boolean.TRUE);
		projectToolConfigProcessorItem.setBoardQuery("query");
		projectToolConfigProcessorItem.setNewRelicAppNames(Arrays.asList("project1"));

		ProcessorItem processorItem = new ProcessorItem();
		processorItem.setId(new ObjectId("633bcf9e26878c56f03ebd38"));
		processorItem.setProcessorId(new ObjectId("63282180160f5b4eb2ac380b"));

		List<ProcessorItem> processorItemList = new ArrayList<>();
		processorItemList.add(processorItem);
		projectToolConfigProcessorItem.setProcessorItemList(processorItemList);

		Connection conn = new Connection();
		conn.setUsername("user");
		conn.setConnectionName("connection name");
		conn.setBaseUrl("https://abc.com/");
		conn.setApiKeyFieldName("filed");
		conn.setAccessToken("testAccessToken");
		conn.setType("jira");
		conn.setApiKey("key");
		conn.setApiEndPoint("api/2");
		List<Connection> connectionList = new ArrayList<>();
		connectionList.add(conn);

		projectToolConfigProcessorItem.setConnection(connectionList);
		return projectToolConfigProcessorItem;
	}
}
