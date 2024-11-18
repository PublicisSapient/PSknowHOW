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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_830;

import java.util.Arrays;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * remove build frequency kpi and field mapping
 *
 * @author aksshriv1
 */
@ChangeUnit(id = "r_build_freq", order = "08331", author = "aksshriv1", systemVersion = "8.3.3")
public class BuildFrequencyKPI {
	public static final String KPI_ID = "kpiId";
	public static final String KPI_172 = "kpi172";
	private final MongoTemplate mongoTemplate;
	private static final String FIELD_NAME = "fieldName";

	public BuildFrequencyKPI(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		deleteKpiMaster();
		deleteFieldMappingStructure();
		deleteKPIColumnConfig();
		deleteKpiCategoryMapping();
	}

	public void deleteKpiCategoryMapping() {
		mongoTemplate.getCollection("kpi_category_mapping").deleteOne(new Document(KPI_ID, KPI_172));
	}

	public void deleteKPIColumnConfig() {
		mongoTemplate.getCollection("kpi_column_configs").deleteOne(new Document(KPI_ID, KPI_172));
	}

	public void deleteKpiMaster() {
		mongoTemplate.getCollection("kpi_master").deleteOne(new Document(KPI_ID, KPI_172));
	}

	public void deleteFieldMappingStructure() {
		MongoCollection<Document> fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");
		fieldMappingStructure
				.deleteMany(new Document(FIELD_NAME, new Document("$in", Arrays.asList("thresholdValueKPI172"))));
	}

	@RollbackExecution
	public void rollback() {
		// no implementation required
	}
}
