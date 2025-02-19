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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1010;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * updated hirarchy_levels to add helper text
 *
 * @author tejgorip
 */
@ChangeUnit(id = "r_hierarchy_helpertext_enh", order = "010107", author = "tejgorip", systemVersion = "10.1.0")
public class HierarchyHelpertextEnh {

	private final MongoTemplate mongoTemplate;
	private static final String HIERARCHY_INFO = "hierarchyInfo";
	private static final String HIERARCHY_LEVELS = "hierarchy_levels";
	private static final String LEVEL = "level";
	private static final String SET = "$set";
	private static final String UNSET = "$unset";

	public HierarchyHelpertextEnh(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		updateHyrarchyInfo();
	}

	public void updateHyrarchyInfo() {

		Document newField = new Document(HIERARCHY_INFO, null);
		Document update1 = new Document(UNSET, newField);
		mongoTemplate.getCollection(HIERARCHY_LEVELS).updateMany(new Document(), update1);
	}

	@RollbackExecution
	public void rollback() {
		// no implementation required
		Document newField = new Document(HIERARCHY_INFO, "Business Unit");
		Document query1 = new Document(LEVEL, 1);
		Document update1 = new Document(SET, newField);
		mongoTemplate.getCollection(HIERARCHY_LEVELS).updateOne(query1, update1);

		Document newField2 = new Document(HIERARCHY_INFO, "Industry");
		Document query2 = new Document(LEVEL, 2);
		Document update2 = new Document(SET, newField2);
		mongoTemplate.getCollection(HIERARCHY_LEVELS).updateOne(query2, update2);

		Document newField3 = new Document(HIERARCHY_INFO, "Account");
		Document query3 = new Document(LEVEL, 3);
		Document update3 = new Document(SET, newField3);
		mongoTemplate.getCollection(HIERARCHY_LEVELS).updateOne(query3, update3);

		Document newField4 = new Document(HIERARCHY_INFO, "Engagement");
		Document query4 = new Document(LEVEL, 4);
		Document update4 = new Document(SET, newField4);
		mongoTemplate.getCollection(HIERARCHY_LEVELS).updateOne(query4, update4);
	}
}
