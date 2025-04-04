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

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

import java.util.List;

@ChangeUnit(id = "jira_configuration_type_migration", order = "13102", author = "girpatha", systemVersion = "13.1.0")
public class JiraConfigurationTypeChangeUnit {

    private final MongoTemplate mongoTemplate;
    private final static String JIRA_CONFIGURATION_TYPE = "jiraConfigurationType";
    private final static String PROJECT_TOOL_CONFIGS = "project_tool_configs";
    private final static String TOOL_NAME = "toolName";


    public JiraConfigurationTypeChangeUnit(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() {
        // Find all Jira configurations
        List<Document> jiraConfigs = mongoTemplate.getCollection(PROJECT_TOOL_CONFIGS)
                .find(new Document(TOOL_NAME, "Jira"))
                .into(new java.util.ArrayList<>());

        for (Document config : jiraConfigs) {
            boolean queryEnabled = config.getBoolean("queryEnabled", false);
            List<Document> boards = config.get("boards", List.class);
            
            int jiraConfigurationType = determineConfigurationType(queryEnabled, boards);
            
            // Update the document with the new field
            Document query = new Document("_id", config.get("_id"));
            Document update = new Document("$set", new Document(JIRA_CONFIGURATION_TYPE, jiraConfigurationType));
            mongoTemplate.getCollection(PROJECT_TOOL_CONFIGS).updateOne(query, update);
        }
    }

    @RollbackExecution
    public void rollback() {
        // Remove the jiraConfigurationType field from all Jira configurations
        Document query = new Document("toolName", "Jira");
        Document update = new Document("$unset", new Document(JIRA_CONFIGURATION_TYPE, ""));
        mongoTemplate.getCollection(PROJECT_TOOL_CONFIGS).updateMany(query, update);
    }

    private int determineConfigurationType(boolean queryEnabled, List<Document> boards) {
        if (!queryEnabled && boards != null && !boards.isEmpty()) {
            return 1; // Type 1: queryEnabled false and boards array not empty
        } else if (queryEnabled && (boards == null || boards.isEmpty())) {
            return 2; // Type 2: queryEnabled true and boards array empty
        }
        return 3;
    }
}