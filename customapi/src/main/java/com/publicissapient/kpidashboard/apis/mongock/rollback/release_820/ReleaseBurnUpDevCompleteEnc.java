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
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "r_release_burnUp_dev_comp", order = "08202", author = "shunaray", systemVersion = "8.2.0")
public class ReleaseBurnUpDevCompleteEnc {

	private final MongoTemplate mongoTemplate;

	public ReleaseBurnUpDevCompleteEnc(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		rollBackFieldMappingStructure();
		rollbackUpdateKpiInfo();
	}

	public void rollBackFieldMappingStructure() {
		List<String> fieldNamesToDelete = Arrays.asList("populateByDevDoneKPI150", "jiraDevDoneStatusKPI150");
		Document filter = new Document("fieldName", new Document("$in", fieldNamesToDelete));

		// Delete documents that match the filter
		mongoTemplate.getCollection("field_mapping_structure").deleteMany(filter);
	}

	public void rollbackUpdateKpiInfo() {
		mongoTemplate.getCollection("kpi_master").updateOne(new Document("kpiId", "kpi150"),
				new Document("$set", new Document("kpiInfo.definition",
						"It shows the cumulative daily actual progress of the release against the overall scope. It also shows additionally the scope added or removed during the release.")));
	}

	@RollbackExecution
	public void rollback() {
		insertFieldMappingStructure();
		updateKpiInfo();
	}

	public void insertFieldMappingStructure() {
		// Document 1
		Document document1 = new Document("fieldName", "populateByDevDoneKPI150")
				.append("fieldLabel", "Prediction logic").append("fieldType", "toggle")
				.append("toggleLabelLeft", "Overall completion").append("toggleLabelRight", "Dev Completion")
				.append("section", "WorkFlow Status Mapping").append("processorCommon", false).append("tooltip",
						new Document("definition", "Enabled State (Kpi will populate w.r.t Dev complete date)"));

		// Document 2
		Document document2 = new Document("fieldName", "jiraDevDoneStatusKPI150")
				.append("fieldLabel", "Status to identify Dev completed issues").append("fieldType", "chips")
				.append("fieldCategory", "workflow").append("section", "WorkFlow Status Mapping")
				.append("tooltip", new Document("definition",
						"Status that confirms that the development work is completed and an issue can be passed on for testing"));

		mongoTemplate.getCollection("field_mapping_structure").insertMany(Arrays.asList(document1, document2));
	}

	public void updateKpiInfo() {
		mongoTemplate.getCollection("kpi_master").updateOne(new Document("kpiId", "kpi150"),
				new Document("$set", new Document("kpiInfo.definition",
						"It shows the cumulative daily actual progress of the release against the overall scope. It also shows additionally the scope added or removed during the release w.r.t Dev/Qa completion date and Dev/Qa completion status for the Release tagged issues")));
	}

}
