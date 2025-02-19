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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_930;

import java.util.Collections;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * add release field mapping for burnup kpi
 *
 * @author aksshriv1
 */
@ChangeUnit(id = "r_release_burnup", order = "09301", author = "aksshriv1", systemVersion = "9.3.0")
public class ReleaseBurnUPEnhc {

	private final MongoTemplate mongoTemplate;
	private static final String FIELD_NAME = "fieldName";

	public ReleaseBurnUPEnhc(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");
		fieldMappingStructure
				.deleteOne(new Document(FIELD_NAME, new Document("$in", Collections.singletonList("releaseListKPI150"))));
	}

	@RollbackExecution
	public void rollback() {
		// no implementation required
	}
}
