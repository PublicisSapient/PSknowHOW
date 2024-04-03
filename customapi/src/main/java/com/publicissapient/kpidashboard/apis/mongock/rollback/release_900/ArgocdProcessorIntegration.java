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
package com.publicissapient.kpidashboard.apis.mongock.rollback.release_900;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collections;

@ChangeUnit(id = "r_argocd_processor_integration", order = "09010", author = "hargupta15", systemVersion = "9.0.0")
public class ArgocdProcessorIntegration {
	private final MongoTemplate mongoTemplate;
	private static final String CLASS_KEY = "_class";
	Document processorData = createProcessor("ArgoCD", "BUILD",
			"com.publicissapient.kpidashboard.argocd.model.ArgoCDProcessor");

	public ArgocdProcessorIntegration(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		Document query = new Document("processorName", "ArgoCD");
		mongoTemplate.getCollection("processor").deleteOne(query);
	}

	private Document createProcessor(String processorName, String processorType, String className) {
		return new Document().append("processorName", processorName).append("processorType", processorType)
				.append("isActive", true).append("isOnline", true).append("errors", Collections.emptyList())
				.append("isLastSuccess", false).append(CLASS_KEY, className);
	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.getCollection("processor").insertOne(processorData);
	}
}
