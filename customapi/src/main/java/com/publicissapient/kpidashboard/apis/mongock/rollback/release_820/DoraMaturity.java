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
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_820;

import java.util.Arrays;
import java.util.Collections;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "r_dora_maturity", order = "08201", author = "shi6", systemVersion = "8.2.0")
public class DoraMaturity {

	private final MongoTemplate mongoTemplate;

	public DoraMaturity(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		scopeChurnFilter();
	}

	public void scopeChurnFilter() {
		MongoCollection<Document> collection = mongoTemplate.getCollection("kpi_master");

		// Update command for kpi116
		collection.updateOne(new Document("kpiId", "kpi116"),
				new Document("$set",
						new Document("maturityRange", Arrays.asList("-50", "50-30", "30-20", "20-10", "10-"))
								.append("maturityLevel", null)));

		// Update command for kpi118

		collection.updateOne(new Document("kpiId", "kpi118"),
				new Document("$set", new Document("maturityRange", Arrays.asList("-1", "1-2", "2-5", "5-10", "10-"))
						.append("maturityLevel", null).append("aggregationCircleCriteria", "average")));

		// Update command for kpi156
		collection.updateOne(new Document("kpiId", "kpi156"),
				new Document("$set",
						new Document("calculateMaturity", false).append("maturityRange", Collections.emptyList())
								.append("maturityLevel", Collections.emptyList())
								.append("kpiInfo.maturityLevels", Collections.emptyList())));

		// Update command for kpi166
		collection.updateOne(new Document("kpiId", "kpi166"),
				new Document("$set",
						new Document("calculateMaturity", false).append("maturityRange", Collections.emptyList())
								.append("maturityLevel", Collections.emptyList())
								.append("kpiInfo.maturityLevels", Collections.emptyList())));
	}

	@RollbackExecution
	public void rollback() {
		// not required

	}

}
