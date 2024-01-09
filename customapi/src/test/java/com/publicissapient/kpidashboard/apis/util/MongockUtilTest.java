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

package com.publicissapient.kpidashboard.apis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

/*
 author @shi6
 */
@RunWith(MockitoJUnitRunner.class)
public class MongockUtilTest {

	@Mock
	private MongoTemplate mongoTemplate;

	@InjectMocks
	private MongockUtil mongoUtils;

	@Test
	public void testGetOrCreateCollection_WhenCollectionDoesNotExist_ShouldCreateCollection() {
		String collectionName = "testCollection";

		when(mongoTemplate.collectionExists(collectionName)).thenReturn(false);
		when(mongoTemplate.createCollection(collectionName)).thenReturn(mock(MongoCollection.class));

		MongoCollection<Document> result = mongoUtils.getOrCreateCollection(mongoTemplate, collectionName);

		assertNotNull(result);
		verify(mongoTemplate, times(1)).collectionExists(collectionName);
		verify(mongoTemplate, times(1)).createCollection(collectionName);
	}

	@Test
	public void testGetOrCreateCollection_WhenCollectionExists_ShouldReturnCollection() {
		String collectionName = "existingCollection";
		MongoCollection<Document> existingCollection = mock(MongoCollection.class);

		when(mongoTemplate.collectionExists(collectionName)).thenReturn(true);
		when(mongoTemplate.getCollection(collectionName)).thenReturn(existingCollection);

		MongoCollection<Document> result = mongoUtils.getOrCreateCollection(mongoTemplate, collectionName);

		assertNotNull(result);
		verify(mongoTemplate, times(1)).collectionExists(collectionName);
		verify(mongoTemplate, never()).createCollection(collectionName);
	}

	@Test
	public void testSaveListToDB_WhenCollectionIsEmpty_ShouldInsertData() {
		String collectionName = "testCollection";
		List<String> dataList = Arrays.asList("test1", "test2");

		MongoCollection<Document> collection = mock(MongoCollection.class);
		when(collection.countDocuments()).thenReturn(0L);
		doReturn(collection).when(mongoTemplate).createCollection(anyString());

		mongoUtils.saveListToDB(dataList, collectionName, mongoTemplate);

		verify(collection, times(1)).countDocuments();
		verify(mongoTemplate, times(1)).insert(anyList(), eq(collectionName));
	}

	@Test
	public void testCreateFieldMapping_ShouldCreateDocumentWithProvidedValues() {
		String fieldName = "testField";
		String fieldLabel = "Test Field";
		String section = "Test Section";
		String fieldCategory = "Test Category";
		String fieldType = "Test Type";
		String tooltipDefinition = "Test Tooltip";

		Document result = mongoUtils.createFieldMapping(fieldName, fieldLabel, section, fieldCategory, fieldType,
				tooltipDefinition);

		assertEquals(fieldName, result.getString("fieldName"));
		assertEquals(fieldLabel, result.getString("fieldLabel"));
		assertEquals(section, result.getString("section"));
		assertEquals(fieldCategory, result.getString("fieldCategory"));
		assertEquals(fieldType, result.getString("fieldType"));

		Document tooltip = result.get("tooltip", Document.class);
		assertNotNull(tooltip);
		assertEquals(tooltipDefinition, tooltip.getString("definition"));
	}

}