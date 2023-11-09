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
package com.publicissapient.kpidashboard.apis.mongock.installation;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.publicissapient.kpidashboard.apis.data.MetaDataIdentifierDataFactory;
import com.publicissapient.kpidashboard.apis.util.MongockUtil;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;

/**
 * @author hargupta15
 */
@Slf4j
@ChangeUnit(id = "ddl4", order = "004", author = "PSKnowHOW")
public class KpiDefaultConfiguration {
	private final MongoTemplate mongoTemplate;
	private static final String METADATA_IDENTIFIER_COLLECTION = "metadata_identifier";
	List<MetadataIdentifier> metadataIdentifierList;

	public KpiDefaultConfiguration(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		MetaDataIdentifierDataFactory metaDataIdentifierDataFactory = MetaDataIdentifierDataFactory.newInstance();
		metadataIdentifierList = metaDataIdentifierDataFactory.getMetadataIdentifierList();
	}

	@Execution
	public boolean changeSet() {
		MongockUtil.saveListToDB(metadataIdentifierList, METADATA_IDENTIFIER_COLLECTION, mongoTemplate);
		return true;
	}

	@RollbackExecution
	public void rollback() {
		// We are inserting the documents through DDL, no rollback to any collections.
	}
}
