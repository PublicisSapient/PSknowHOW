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

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shunaray
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "rca_inclusion_change", order = "8207", author = "shunaray", systemVersion = "8.2.0")
public class RCAInclusionChangeUnit {

	private final MongoTemplate mongoTemplate;

	public RCAInclusionChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		updateRCADIR();
		updateRCAQualityStatus();
		updateRCADefectDensity();
		updateRCAQualityFTPR();
		updateRCAKPI135ItrFTPR();
	}

	public void updateFieldMapping(String currentFieldName, String newFieldLabel, String newTooltipDefinition, String newFieldName) {
		Document filter = new Document("fieldName", currentFieldName);
		Document update = new Document("$set", new Document("fieldName", newFieldName)
				.append("tooltip.definition", newTooltipDefinition)
				.append("fieldLabel", newFieldLabel));
		mongoTemplate.getCollection("field_mapping_structure").updateOne(filter, update);
	}

	public void updateRCAQualityFTPR() {
		updateFieldMapping("excludeRCAFromKPI82",
				"Root cause values to be included",
				"Root cause reasons for defects which are to be included in 'FTPR' calculation","includeRCAForKPI82");
	}

	public void updateRCAKPI135ItrFTPR() {
		updateFieldMapping("excludeRCAFromKPI135",
				"Defect RCA inclusion for Quality KPIs",
				"The defects tagged to as per RCA values selected in this field on Mappings screen will be included.","includeRCAForKPI135");
	}
	public void updateRCADIR() {
		updateFieldMapping("excludeRCAFromKPI14",
				"Root cause values to be included",
				"Root cause reasons for defects which are to be included in 'Defect Injection rate' calculation.","includeRCAForKPI14");
	}
	public void updateRCADefectDensity() {
		updateFieldMapping("excludeRCAFromQAKPI111",
				"Root cause values to be included",
				"Root cause reasons for defects which are to be included in 'Defect Density' calculation.","includeRCAForQAKPI111");
	}
	public void updateRCAQualityStatus() {
		updateFieldMapping("excludeRCAFromKPI133",
				"Root cause values to be included",
				"Root cause reasons for defects which are to be included in 'Quality Status' calculation.","includeRCAForKPI133");
	}

	@RollbackExecution
	public void rollback() {
		rollbackRCADIR();
		rollbackRCAQualityStatus();
		rollbackRCADefectDensity();
		rollbackRCAQualityFTPR();
		rollbackRCAKPI135ItrFTPR();

	}

	public void rollbackRCAQualityFTPR() {
		updateFieldMapping("includeRCAForKPI82",
				"Root cause values to be excluded",
				"Root cause reasons for defects which are to be excluded from 'FTPR' calculation","excludeRCAFromKPI82");
	}

	public void rollbackRCAKPI135ItrFTPR() {
		updateFieldMapping("includeRCAForKPI135",
				"Defect RCA exclusion from Quality KPIs",
				"The defects tagged to priority values selected in this field on Mappings screen will be excluded.","excludeRCAFromKPI135");
	}
	public void rollbackRCADIR() {
		updateFieldMapping("includeRCAForKPI14",
				"Root cause values to be excluded",
				"Root cause reasons for defects which are to be excluded from 'Defect Injection rate' calculation.","excludeRCAFromKPI14");
	}
	public void rollbackRCADefectDensity() {
		updateFieldMapping("includeRCAForQAKPI111",
				"Root cause values to be excluded",
				"Root cause reasons for defects which are to be excluded from 'Defect Density' calculation.","excludeRCAFromQAKPI111");
	}
	public void rollbackRCAQualityStatus() {
		updateFieldMapping("includeRCAForKPI133",
				"Root cause values to be excluded",
				"Root cause reasons for defects which are to be excluded from 'Quality Status' calculation.","excludeRCAFromKPI133");
	}

}
