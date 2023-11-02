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
package com.publicissapient.kpidashboard.apis.mongock.installation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author bogolesw
 */
@ChangeUnit(id = "ddl6", order = "006", author = "PSKnowHOW")
public class ProjectOrganizationStructureChangeLog {
	private final MongoTemplate mongoTemplate;
	private static final String HIERARCHY_LEVELS = "hierarchy_levels";
	private static final String HIERARCHY_LEVEL_SUGGESTIONS = "hierarchy_level_suggestions";
	private static final String ADDITIONAL_FILTER_CATEGORIES = "additional_filter_categories";
	private static final String LEVEL = "level";
	private static final String HIERARCHY_LEVEL_ID = "hierarchyLevelId";
	private static final String HIERARCHY_LEVEL_NAME = "hierarchyLevelName";
	private static final String VALUES = "values";

	public ProjectOrganizationStructureChangeLog(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void executeProjectOrganizationStructure() {
		initializeHierarchyLevels();
		initializeHierarchyLevelSuggestions();
		initializeAdditionalFilterCategories();
	}

	public void initializeHierarchyLevels() {
		if (mongoTemplate.getCollection(HIERARCHY_LEVELS).countDocuments() == 0) {
			List<Document> hierarchyLevels = Arrays.asList(
					new Document(LEVEL, 1).append(HIERARCHY_LEVEL_ID, "hierarchyLevelOne").append(HIERARCHY_LEVEL_NAME,
							"Organization"),
					new Document(LEVEL, 2).append(HIERARCHY_LEVEL_ID, "hierarchyLevelTwo").append(HIERARCHY_LEVEL_NAME,
							"Business Unit"),
					new Document(LEVEL, 3).append(HIERARCHY_LEVEL_ID, "hierarchyLevelThree")
							.append(HIERARCHY_LEVEL_NAME, "Portfolio"));
			mongoTemplate.getCollection(HIERARCHY_LEVELS).insertMany(hierarchyLevels);
		}
	}

	public void initializeHierarchyLevelSuggestions() {
		if (mongoTemplate.getCollection(HIERARCHY_LEVEL_SUGGESTIONS).countDocuments() == 0) {
			List<Document> levelSuggestions = Arrays.asList(
					new Document(HIERARCHY_LEVEL_ID, "hierarchyLevelOne").append(VALUES,
							Collections.singletonList("Organization")),
					new Document(HIERARCHY_LEVEL_ID, "hierarchyLevelTwo").append(VALUES,
							Arrays.asList("Business Unit 1", "Business Unit 2")),
					new Document(HIERARCHY_LEVEL_ID, "hierarchyLevelThree").append(VALUES,
							Arrays.asList("Portfolio 1", "Portfolio 2")));
			mongoTemplate.getCollection(HIERARCHY_LEVEL_SUGGESTIONS).insertMany(levelSuggestions);
		}
	}

	public void initializeAdditionalFilterCategories() {
		if (mongoTemplate.getCollection(ADDITIONAL_FILTER_CATEGORIES).countDocuments() == 0) {
			Document filterCategory = new Document(LEVEL, 1).append("filterCategoryId", "afOne")
					.append("filterCategoryName", "Teams");
			mongoTemplate.getCollection(ADDITIONAL_FILTER_CATEGORIES).insertOne(filterCategory);
		}
	}

	@RollbackExecution
	public void rollback() {
		// We are inserting the documents through DDL, no rollback to any collections.
	}
}
