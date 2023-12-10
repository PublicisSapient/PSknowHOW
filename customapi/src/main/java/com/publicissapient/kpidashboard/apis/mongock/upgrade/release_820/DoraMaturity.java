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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_820;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
@ChangeUnit(id = "dora_maturity", order = "8201", author = "shi6", systemVersion = "8.2.0")
public class DoraMaturity {

	private final MongoTemplate mongoTemplate;

	public DoraMaturity(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		updateKpiMaster();
	}

	public void updateKpiMaster() {
		MongoCollection<Document> collection = mongoTemplate.getCollection("kpi_master");

		// Update command for kpi161
		updateKPI161(collection);

		// Update command for kpi116
		updateKPI116(collection);

		// Update command for kpi118
		updateKPI118(collection);

		// Update command for kpi156
		updateKPI156(collection);

		// Update command for kpi166
		updateKPI166(collection);
	}

	private static void updateKPI161(MongoCollection<Document> collection) {
		collection.updateOne(new Document("kpiId", "kpi161"),
				new Document("$set", new Document("calculateMaturity", false).append("maturityRange",
						Arrays.asList("-40", "40-60", "60-75", "75-90", "90-"))));
	}

	private static void updateKPI116(MongoCollection<Document> collection) {
		List<Document> documentList = new ArrayList<>();
		documentList.add(new Document("level", "M5").append("bgColor", "#167a26").append("displayRange", "0-15 %"));
		documentList.add(new Document("level", "M4").append("bgColor", "#4ebb1a").append("displayRange", "15-30 %"));
		documentList.add(new Document("level", "M3").append("bgColor", "#ef7643").append("displayRange", "30-45 %"));
		documentList.add(new Document("level", "M2").append("bgColor", "#f53535").append("displayRange", "45-60 %"));
		documentList.add(new Document("level", "M1").append("bgColor", "#c91212").append("displayRange", "60 % and Above"));

		collection.updateOne(new Document("kpiId", "kpi116"),
				new Document("$set",
						new Document("maturityRange", Arrays.asList("-60", "60-45", "45-30", "30-15", "15-"))
								.append("maturityLevel", documentList)));
	}

	private static void updateKPI118(MongoCollection<Document> collection) {
		List<Document> documentList = new ArrayList<>();
		documentList.add(new Document("level", "M5").append("bgColor", "#167a26").append("label", ">= 2 per week").append("displayRange", "8 and Above"));
		documentList.add(new Document("level", "M4").append("bgColor", "#4ebb1a").append("label", "Once per week").append("displayRange", "6,7"));
		documentList.add(new Document("level", "M3").append("bgColor", "#ef7643").append("label", "Once in 2 weeks").append("displayRange", "4,5"));
		documentList.add(new Document("level", "M2").append("bgColor", "#f53535").append("label", "Once in 4 weeks").append("displayRange", "2,3"));
		documentList.add(new Document("level", "M1").append("bgColor", "#c91212").append("label", "< Once in 8 weeks").append("displayRange", "0,1"));

		collection.updateOne(new Document("kpiId", "kpi118"),
				new Document("$set", new Document("maturityRange", Arrays.asList("0-2", "2-4", "4-6", "6-8", "8-"))
						.append("maturityLevel", documentList).append("aggregationCircleCriteria", "sum")));
	}

	private static void updateKPI156(MongoCollection<Document> collection) {

		List<Document> documentList = new ArrayList<>();
		documentList.add(new Document("level", "M5").append("bgColor", "#167a26").append("label", "< 1 Day").append("displayRange", "0-1 Day"));
		documentList.add(new Document("level", "M4").append("bgColor", "#4ebb1a").append("label", "< 7 Days").append("displayRange", "1-7 Days"));
		documentList.add(new Document("level", "M3").append("bgColor", "#ef7643").append("label", "< 30 Days").append("displayRange", "7-30 Days"));
		documentList.add(new Document("level", "M2").append("bgColor", "#f53535").append("label", "< 90 Days").append("displayRange", "30-90 Days"));
		documentList.add(new Document("level", "M1").append("bgColor", "#c91212").append("label", ">= 90 Days").append("displayRange", "90 Days and Above"));
		collection.updateOne(new Document("kpiId", "kpi156"),
				new Document("$set",
						new Document("calculateMaturity", true)
								.append("maturityRange", Arrays.asList("90-", "30-90", "7-30", "1-7", "-1"))
								.append("maturityLevel", documentList)));
	}

	private static void updateKPI166(MongoCollection<Document> collection) {

		List<Document> documentList = new ArrayList<>();
		documentList.add(new Document("level", "M5").append("bgColor", "#167a26").append("displayRange", "0-1 Hour"));
		documentList.add(new Document("level", "M4").append("bgColor", "#4ebb1a").append("displayRange", "1-12 Hours"));
		documentList.add(new Document("level", "M3").append("bgColor", "#ef7643").append("displayRange", "12-24 Hours"));
		documentList.add(new Document("level", "M2").append("bgColor", "#f53535").append("displayRange", "24-48 Hours"));
		documentList.add(new Document("level", "M1").append("bgColor", "#c91212").append("displayRange", "48 Hours and Above"));
		collection.updateOne(new Document("kpiId", "kpi166"),
				new Document("$set",
						new Document("calculateMaturity", true)
								.append("maturityRange", Arrays.asList("48-", "24-48", "12-24", "1-12", "-1"))
								.append("maturityLevel", documentList)));
	}

	@RollbackExecution
	public void rollback() {
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

}
