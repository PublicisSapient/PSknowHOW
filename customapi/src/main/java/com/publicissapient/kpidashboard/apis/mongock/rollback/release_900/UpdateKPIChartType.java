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

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/***
 * @author rendk
 */
@ChangeUnit(id = "r_kpi_chartType_update", order = "09006", author = "rendk", systemVersion = "9.0.0")
public class UpdateKPIChartType {
	private final MongoTemplate mongoTemplate;

	private static final String CHART_TYPE = "chartType";
	private static final String KPI_ID = "kpiId";
	private static final String XAXIS_LABEL = "xAxisLabel";
	private static final String YAXIS_LABEL = "yAxisLabel";

	public UpdateKPIChartType(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		Document query = new Document(KPI_ID, "kpi142");
		Document update = new Document("$set",
				new Document().append(CHART_TYPE, "pieChart").append(XAXIS_LABEL, "").append(YAXIS_LABEL, ""));
		mongoTemplate.getCollection("kpi_master").updateOne(query, update);

	}

	@RollbackExecution
	public void rollBack() {
		Document query = new Document(KPI_ID, "kpi142");
		Document update = new Document("$set", new Document().append(CHART_TYPE, "stackedColumn")
				.append(XAXIS_LABEL, "Test Phase").append(YAXIS_LABEL, "Count"));
		mongoTemplate.getCollection("kpi_master").updateOne(query, update);
	}

}
