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
package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1010;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.publicissapient.kpidashboard.apis.mongock.FieldMappingStructureForMongock;
import com.publicissapient.kpidashboard.apis.mongock.data.FieldMappingStructureDataFactory;
import com.publicissapient.kpidashboard.apis.util.MongockUtil;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;

/**
 * rollback field-mapping structure data
 *
 * @author aksshriv1
 */
@Slf4j
@ChangeUnit(id = "updated_mapping_structure", order = "10109", author = "aksshriv1", systemVersion = "10.1.0")
public class FieldMappingStructureUpdate {

	private final MongoTemplate mongoTemplate;
	private List<FieldMappingStructureForMongock> fieldMappingStructureList;
	private List<FieldMappingStructureForMongock> fieldMappingStructureBackupList;
	private static final String FIELD_MAPPING_STRUCTURE_COLLECTION = "field_mapping_structure";
	private static final String FILE_PATH_FIELD_MAPPING_LIST = "/json/mongock/default/field_mapping_structure_backup_10.0.0.json";

	public FieldMappingStructureUpdate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		FieldMappingStructureDataFactory fieldMappingStructureDataFactory = FieldMappingStructureDataFactory.newInstance();
		fieldMappingStructureList = fieldMappingStructureDataFactory.getFieldMappingStructureList();
		FieldMappingStructureDataFactory fieldMappingStructureBackupDataFactory = FieldMappingStructureDataFactory
				.newInstance(FILE_PATH_FIELD_MAPPING_LIST);
		fieldMappingStructureBackupList = fieldMappingStructureBackupDataFactory.getFieldMappingStructureList();
	}

	@Execution
	public void changeSet() {

		// Delete all existing documents in the collection
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE_COLLECTION).deleteMany(new Document());
		MongockUtil.saveListToDB(fieldMappingStructureList, FIELD_MAPPING_STRUCTURE_COLLECTION, mongoTemplate);
	}

	@RollbackExecution
	public void rollback() {
		mongoTemplate.getCollection(FIELD_MAPPING_STRUCTURE_COLLECTION).deleteMany(new Document());
		MongockUtil.saveListToDB(fieldMappingStructureBackupList, FIELD_MAPPING_STRUCTURE_COLLECTION, mongoTemplate);
	}
}
