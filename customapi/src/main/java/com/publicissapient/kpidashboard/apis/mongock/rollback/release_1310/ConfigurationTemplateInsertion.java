/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1310;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.publicissapient.kpidashboard.apis.mongock.data.ConfigurationTemplateDataFactory;
import com.publicissapient.kpidashboard.apis.util.MongockUtil;
import com.publicissapient.kpidashboard.common.model.jira.ConfigurationTemplateDocument;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "r_configuration_template", order = "013103", author = "girpatha", systemVersion = "13.1.0")
public class ConfigurationTemplateInsertion {

	private final MongoTemplate mongoTemplate;
	List<ConfigurationTemplateDocument> configurationTemplates;
	private static final String TEMPLATE_CODE = "templateCode";
	private static final String CONFIGURATION_TEMPLATE = "configuration_template";

	public ConfigurationTemplateInsertion(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		ConfigurationTemplateDataFactory configurationTemplateDataFactory = ConfigurationTemplateDataFactory.newInstance();
		configurationTemplates = configurationTemplateDataFactory.getConfigurationTemplateList();
	}

	@Execution
	public boolean changeSet() {
		MongockUtil.saveListToDB(configurationTemplates, CONFIGURATION_TEMPLATE, mongoTemplate);
		return true;
	}

	@RollbackExecution
	public void rollback() {
		// Get all configuration templates that were inserted by this change unit
		List<Document> insertedTemplates = mongoTemplate.getCollection(CONFIGURATION_TEMPLATE)
				.find(new Document(TEMPLATE_CODE, new Document("$in", Arrays.asList("1", "2", "3")))).into(new ArrayList<>());

		// Delete each inserted template
		for (Document template : insertedTemplates) {
			mongoTemplate.getCollection(CONFIGURATION_TEMPLATE).deleteOne(new Document("_id", template.get("_id")));
		}
	}
}
