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

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1210;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.publicissapient.kpidashboard.apis.mongock.data.MetaDataIdentifierDataFactory;
import com.publicissapient.kpidashboard.apis.util.MongockUtil;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

/**
 * @author girpatha
 */
@ChangeUnit(id = "r_metadata_identifier_updater_for_templates", order = "012102", author = "girpatha", systemVersion = "12.1.0")
public class MetadataIdentifierUpdaterForTemplates {

	private final MongoTemplate mongoTemplate;

	private static final String METADATA_IDENTIFIER_COLLECTION = "metadata_identifier";
	private static final String TEMPLATE_CODE = "templateCode";
	private static final String TEMPLATE_NAME = "templateName";
	List<MetadataIdentifier> metadataIdentifierList;

	public MetadataIdentifierUpdaterForTemplates(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void execution() {
		MongoCollection<Document> collection = mongoTemplate.getCollection(METADATA_IDENTIFIER_COLLECTION);
		collection.deleteMany(new Document(TEMPLATE_CODE, new Document("$in", List.of("11", "12"))));

		collection.updateMany(new Document(TEMPLATE_NAME, "Standard DOJO Template"),
				new Document("$set", new Document(TEMPLATE_NAME, "Standard Template")));
	}

	@RollbackExecution
	public void rollback() {
		MongoCollection<Document> collection = mongoTemplate.getCollection(METADATA_IDENTIFIER_COLLECTION);
		MetaDataIdentifierDataFactory metaDataIdentifierDataFactory = MetaDataIdentifierDataFactory.newInstance();
		metadataIdentifierList = metaDataIdentifierDataFactory.getMetadataIdentifierList();
		List<MetadataIdentifier> filteredMetadataIdentifiers = metadataIdentifierList.stream()
				.filter(metadataIdentifier -> "11".equals(metadataIdentifier.getTemplateCode()) ||
						"12".equals(metadataIdentifier.getTemplateCode()))
				.toList();
		MongockUtil.insertFilteredListToDB(filteredMetadataIdentifiers, METADATA_IDENTIFIER_COLLECTION, mongoTemplate);
		collection.updateMany(new Document(TEMPLATE_NAME, "Standard Template"),
				new Document("$set", new Document(TEMPLATE_NAME, "Standard DOJO Template")));
	}
}
