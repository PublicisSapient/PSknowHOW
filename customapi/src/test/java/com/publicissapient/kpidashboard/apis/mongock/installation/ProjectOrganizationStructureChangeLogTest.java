package com.publicissapient.kpidashboard.apis.mongock.installation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

@ExtendWith(MockitoExtension.class)
class ProjectOrganizationStructureChangeLogTest {

	MongoTemplate mongoTemplate = Mockito.mock(MongoTemplate.class);
	@InjectMocks
	private ProjectOrganizationStructureChangeLog projectOrganizationStructureChangeLog;

	@BeforeEach
	public void setUp() {
		MongoCollection collectionMock = mock(MongoCollection.class);
		Mockito.when(mongoTemplate.getCollection(Mockito.any())).thenReturn(collectionMock);
		Mockito.when(collectionMock.countDocuments()).thenReturn(0L);
	}

	@Test
	void testExecuteProjectOrganizationStructureWhenHierarchyLevelsNotInitializedThenInitializeHierarchyLevels() {
		List<Document> hierarchyLevels = Arrays.asList(
				new Document("level", 1).append("hierarchyLevelId", "hierarchyLevelOne").append("hierarchyLevelName",
						"Organization"),
				new Document("level", 2).append("hierarchyLevelId", "hierarchyLevelTwo").append("hierarchyLevelName",
						"Business Unit"),
				new Document("level", 3).append("hierarchyLevelId", "hierarchyLevelThree").append("hierarchyLevelName",
						"Portfolio"));

		projectOrganizationStructureChangeLog.executeProjectOrganizationStructure();

		verify(mongoTemplate, times(0)).insert(hierarchyLevels, "hierarchy_levels");
	}

	@Test
	void testExecuteProjectOrganizationStructureWhenHierarchyLevelSuggestionsNotInitializedThenInitializeHierarchyLevelSuggestions() {
		List<Document> levelSuggestions = Arrays.asList(
				new Document("hierarchyLevelId", "hierarchyLevelOne").append("values",
						Collections.singletonList("Organization")),
				new Document("hierarchyLevelId", "hierarchyLevelTwo").append("values",
						Arrays.asList("Business Unit 1", "Business Unit 2")),
				new Document("hierarchyLevelId", "hierarchyLevelThree").append("values",
						Arrays.asList("Portfolio 1", "Portfolio 2")));

		projectOrganizationStructureChangeLog.executeProjectOrganizationStructure();

		verify(mongoTemplate, times(0)).insert(levelSuggestions, "hierarchy_level_suggestions");
	}

	@Test
	void testExecuteProjectOrganizationStructureWhenAdditionalFilterCategoriesNotInitializedThenInitializeAdditionalFilterCategories() {
		Document filterCategory = new Document("level", 1).append("filterCategoryId", "afOne")
				.append("filterCategoryName", "Teams");

		projectOrganizationStructureChangeLog.executeProjectOrganizationStructure();

		verify(mongoTemplate, times(0)).insert(filterCategory, "additional_filter_categories");
	}
}