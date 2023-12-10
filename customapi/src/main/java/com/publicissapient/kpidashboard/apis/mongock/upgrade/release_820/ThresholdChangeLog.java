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

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackBeforeExecution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@ChangeUnit(id = "threshold_change_log", order = "8208", author = "shunaray", systemVersion = "8.2.0")
public class ThresholdChangeLog {

	private final MongoTemplate mongoTemplate;
	private MongoCollection<Document> fieldMappingStructure;
	private MongoCollection<Document> kpiMaster;

	public ThresholdChangeLog(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@BeforeExecution
	public void beforeExecution() {
		fieldMappingStructure = mongoTemplate.getCollection("field_mapping_structure");
		kpiMaster = mongoTemplate.getCollection("kpi_master");
	}

	@Execution
	public boolean execution() {
		insertFieldMapping();
		addThresholdAndBg("kpi126","0",true);
		addThresholdAndBg("kpi42","60",true);
		addThresholdAndBg("kpi168","2",true);
		addThresholdAndBg("kpi70","80",true);
		addThresholdAndBg("kpi40","20",true);
		addThresholdAndBg("kpi5","0",true);
		addThresholdAndBg("kpi39","40",true);
		addThresholdAndBg("kpi46","0",true);
		addThresholdAndBg("kpi8","6",false);
		addThresholdAndBg("kpi73","2",true);
		addThresholdAndBg("kpi113","0",true);
		addThresholdAndBg("kpi149","4",true);
		addThresholdAndBg("kpi153","0",true);
		addThresholdAndBg("kpi162","4",false);
		addThresholdAndBg("kpi116","30",false);
		addThresholdAndBg("kpi156","7",true);
		addThresholdAndBg("kpi118","6",true);
		addThresholdAndBg("kpi127","0",false);
		addThresholdAndBg("kpi170","40",false);
		addThresholdAndBg("kpi139","40",false);
		addThresholdAndBg("kpi166","24",false);

		return true;
	}

	public void insertFieldMapping() {
		fieldMappingStructure
				.insertMany(Arrays.asList(createDocument("thresholdValueKPI126"), createDocument("thresholdValueKPI42"),
						createDocument("thresholdValueKPI168"), createDocument("thresholdValueKPI70"),
						createDocument("thresholdValueKPI40"), createDocument("thresholdValueKPI5"),
						createDocument("thresholdValueKPI39"), createDocument("thresholdValueKPI46"),
						createDocument("thresholdValueKPI8"), createDocument("thresholdValueKPI73"),
						createDocument("thresholdValueKPI113"), createDocument("thresholdValueKPI149"),
						createDocument("thresholdValueKPI153"), createDocument("thresholdValueKPI162"),
						createDocument("thresholdValueKPI116"), createDocument("thresholdValueKPI156"),
						createDocument("thresholdValueKPI118"), createDocument("thresholdValueKPI127"),
						createDocument("thresholdValueKPI170"), createDocument("thresholdValueKPI139"),
						createDocument("thresholdValueKPI166")));

	}

	private void addThresholdAndBg(String kpiId, String thresholdValue, boolean isPositiveTrend) {
		String lowerThresholdBG = isPositiveTrend ? "red" : "white";
		String upperThresholdBG = isPositiveTrend ? "white" : "red";

		Document updateFilter = new Document("kpiId", kpiId);

		Document update = new Document("$set", new Document("thresholdValue", thresholdValue)
				.append("lowerThresholdBG", lowerThresholdBG)
				.append("upperThresholdBG", upperThresholdBG));

		kpiMaster.updateOne(updateFilter, update);
	}

	private Document createDocument(String fieldName) {
		return new Document("fieldName", fieldName).append("fieldLabel", "Target KPI Value")
				.append("fieldType", "number").append("section", "Custom Fields Mapping")
				.append("tooltip", new Document("definition",
						"Target KPI value denotes the bare minimum a project should maintain for a KPI. User should just input the number and the unit like percentage, hours will automatically be considered. If the threshold is empty, then a common target KPI line will be shown"));
	}

	@RollbackExecution
	public void rollback() {
		rollBackFieldMappingStructure();
		rollbackThresholdAndBg();
	}

	public void rollBackFieldMappingStructure() {
		List<String> fieldNamesToDelete = Arrays.asList("thresholdValueKPI126", "thresholdValueKPI42",
				"thresholdValueKPI168", "thresholdValueKPI70", "thresholdValueKPI40", "thresholdValueKPI5",
				"thresholdValueKPI39", "thresholdValueKPI46", "thresholdValueKPI8", "thresholdValueKPI73",
				"thresholdValueKPI113", "thresholdValueKPI149", "thresholdValueKPI153", "thresholdValueKPI162",
				"thresholdValueKPI116", "thresholdValueKPI156", "thresholdValueKPI118", "thresholdValueKPI127",
				"thresholdValueKPI170", "thresholdValueKPI139", "thresholdValueKPI166");
		Document filter = new Document("fieldName", new Document("$in", fieldNamesToDelete));

		// Delete documents that match the filter
		fieldMappingStructure.deleteMany(filter);
	}

	private void rollbackThresholdAndBg() {
		List<String> kpiIds = Arrays.asList("kpi126", "kpi42", "kpi168", "kpi70", "kpi40", "kpi5", "kpi39", "kpi46",
				"kpi8", "kpi73", "kpi113", "kpi149", "kpi153", "kpi162", "kpi116", "kpi156", "kpi118", "kpi127",
				"kpi170", "kpi139","kpi166");

		Document filter = new Document("kpiId", new Document("$in", kpiIds));

		Document update = new Document("$unset", new Document("thresholdValue", "")
				.append("lowerThresholdBG", "")
				.append("upperThresholdBG", ""));

		kpiMaster.updateMany(filter, update);
	}
	@RollbackBeforeExecution
	public void rollbackBeforeExecution() {
		// do not require the implementation
	}
}
