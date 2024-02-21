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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_850;

import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author shi6
 */
@ChangeUnit(id = "mandatory_field", order = "8351", author = "shi6", systemVersion = "8.3.5")
public class MandatoryField {

	private final MongoTemplate mongoTemplate;

	public MandatoryField(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public boolean execution() {
		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "field_mapping_structure");
		processorCommonAndMandatoryField(bulkOps, "field", true, true);
		bulkOps.execute();
		return true;
	}

	public void processorCommonAndMandatoryField(BulkOperations bulkOps, String field, boolean processorCommon,
			boolean mandatoryField) {
		Query query = new Query(Criteria.where("fieldName").is(field));
		if (processorCommon)
			bulkOps.updateOne(query, new Update().set("processorCommon", true));
		if (mandatoryField)
			bulkOps.updateOne(query, new Update().set("mandatory", true));
	}

	@RollbackExecution
	public void rollback() {
		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "field_mapping_structure");
		processorCommonAndMandatoryFieldUnset(bulkOps, "field", true, true);

		bulkOps.execute();
	}

	public void processorCommonAndMandatoryFieldUnset(BulkOperations bulkOps, String field, boolean processorCommon,
			boolean mandatoryField) {
		Query query = new Query(Criteria.where("fieldName").is(field));
		if (processorCommon)
			bulkOps.updateOne(query, new Update().unset("processorCommon"));
		if (mandatoryField)
			bulkOps.updateOne(query, new Update().unset("mandatory"));
	}

}
