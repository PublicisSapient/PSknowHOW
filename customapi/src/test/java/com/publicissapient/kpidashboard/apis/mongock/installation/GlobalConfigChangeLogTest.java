package com.publicissapient.kpidashboard.apis.mongock.installation;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

@ExtendWith(MockitoExtension.class)
class GlobalConfigChangeLogTest {

	MongoTemplate mongoTemplate = Mockito.mock(MongoTemplate.class);

	@InjectMocks
	private GlobalConfigChangeLog globalConfigChangeLog;

	@BeforeEach
	public void setUp() {
		MongoCollection collectionMock = mock(MongoCollection.class);
		Mockito.when(mongoTemplate.getCollection(Mockito.any())).thenReturn(collectionMock);
	}

	@ParameterizedTest
	@MethodSource("testData")
	void testInsertData(boolean existingConfig, boolean existingDocuments, long expectedInsertCount) {
		testInsertProcessorDataWhenNoExistingDocumentsThenInsertDocuments();
		testInsertProcessorDataWhenExistingDocumentsThenDoNotInsertDocuments();

	}

	private static Stream<Arguments> testData() {
		return Stream.of(Arguments.of(true, false, 0L), Arguments.of(false, true, 1L), Arguments.of(true, true, 0L),
				Arguments.of(false, false, 1L));
	}

	@Test
	void testInsertGlobalConfigDataWhenNoExistingConfigThenInsertNewConfig() {
		MongoCollection collectionMock = mock(MongoCollection.class);
		when(mongoTemplate.getCollection("global_config")).thenReturn(collectionMock);
		// Create a mock of the MongoTemplate class
		MongoTemplate mongoTemplateMock = Mockito.mock(MongoTemplate.class);

		// Create a mock of the FindIterable class
		FindIterable<Document> findIterableMock = Mockito.mock(FindIterable.class);

		// Create a mock of the MongoCursor class
		MongoCursor<Document> cursorMock = Mockito.mock(MongoCursor.class);

		// Create a mock of the Document class
		Document documentMock = Mockito.mock(Document.class);
		// Mock the behavior of the find() method
		Mockito.when(collectionMock.find(Mockito.any(Document.class))).thenReturn(findIterableMock);

		// Mock the behavior of the first() method
		Mockito.when(findIterableMock.first()).thenReturn(documentMock);

		globalConfigChangeLog.insertGlobalConfigData();

		verify(collectionMock, times(0)).insertOne(any(Document.class));
	}

	@Test
	void testInsertGlobalConfigDataWhenExistingConfigThenDoNotInsertNewConfig() {
		MongoCollection collectionMock = mock(MongoCollection.class);
		when(mongoTemplate.getCollection("global_config")).thenReturn(collectionMock);
		// Create a mock of the MongoTemplate class
		MongoTemplate mongoTemplateMock = Mockito.mock(MongoTemplate.class);

		// Create a mock of the FindIterable class
		FindIterable<Document> findIterableMock = Mockito.mock(FindIterable.class);

		// Create a mock of the MongoCursor class
		MongoCursor<Document> cursorMock = Mockito.mock(MongoCursor.class);

		// Create a mock of the Document class
		Document documentMock = Mockito.mock(Document.class);

		// Mock the behavior of the find() method
		Mockito.when(collectionMock.find(Mockito.any(Document.class))).thenReturn(findIterableMock);

		// Mock the behavior of the first() method
		Mockito.when(findIterableMock.first()).thenReturn(documentMock);

		globalConfigChangeLog.insertGlobalConfigData();

		verify(collectionMock, times(0)).insertOne(any(Document.class));
	}

	void testInsertProcessorDataWhenNoExistingDocumentsThenInsertDocuments() {
		MongoCollection collectionMock = mock(MongoCollection.class);
		when(mongoTemplate.getCollection("processor")).thenReturn(collectionMock);
		when(collectionMock.countDocuments()).thenReturn(0L);

		globalConfigChangeLog.insertProcessorData();

		verify(collectionMock, times(1)).insertMany(anyList());
	}

	void testInsertProcessorDataWhenExistingDocumentsThenDoNotInsertDocuments() {
		MongoCollection collectionMock = mock(MongoCollection.class);
		when(mongoTemplate.getCollection("processor")).thenReturn(collectionMock);
		when(collectionMock.countDocuments()).thenReturn(1L);

		globalConfigChangeLog.insertProcessorData();

		verify(collectionMock, times(0)).insertMany(anyList());
	}

	@Test
	void testInsertProcessorDataWhenNoExistingDataThenInsertNewData() {
		MongoCollection collectionMock = mock(MongoCollection.class);
		when(mongoTemplate.getCollection("processor")).thenReturn(collectionMock);
		when(collectionMock.countDocuments()).thenReturn(0L);

		globalConfigChangeLog.insertProcessorData();

		verify(collectionMock, times(1)).insertMany(anyList());
	}

	@Test
	void testInsertProcessorDataWhenExistingDataThenDoNotInsertNewData() {
		MongoCollection collectionMock = mock(MongoCollection.class);
		when(mongoTemplate.getCollection("processor")).thenReturn(collectionMock);
		when(collectionMock.countDocuments()).thenReturn(1L);

		globalConfigChangeLog.insertProcessorData();

		verify(collectionMock, times(0)).insertMany(anyList());
	}
}