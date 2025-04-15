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

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1310;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.publicissapient.kpidashboard.apis.mongock.data.ConfigurationTemplateDataFactory;
import com.publicissapient.kpidashboard.apis.util.MongockUtil;
import com.publicissapient.kpidashboard.common.model.jira.ConfigurationTemplateDocument;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "configuration_template", order = "13103", author = "girpatha", systemVersion = "13.1.0")
public class ConfigurationTemplateInsertion {

	private final MongoTemplate mongoTemplate;
	List<ConfigurationTemplateDocument> configurationTemplates;

	public ConfigurationTemplateInsertion(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		ConfigurationTemplateDataFactory configurationTemplateDataFactory = ConfigurationTemplateDataFactory.newInstance();
		configurationTemplates = configurationTemplateDataFactory.getConfigurationTemplateList();
	}

	@Execution
	public boolean changeSet() {
		MongockUtil.saveListToDB(configurationTemplates, "configuration_template", mongoTemplate);
		return true;
	}

	@RollbackExecution
	public void rollback() {
		List<Object> ids = configurationTemplates.stream().map(ConfigurationTemplateDocument::getId)
				.collect(Collectors.toList());

		Query query = new Query(Criteria.where("_id").in(ids));
		mongoTemplate.remove(query, ConfigurationTemplateDocument.class);
	}
}
