/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.jira.client.jiraissue;

import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JiraIssueClientFactory {

    @Autowired
    private KanbanJiraIssueClientImpl kanbanJiraIssueClient;

    @Autowired
    private ScrumJiraIssueClientImpl scrumJiraIssueClient;

    /**
     * Gets JiraIssue Client based on the Project type (Kanban or Scrum)
     *
     * @param projectConfig user provided project Configuration mapping
     * @return KanbanJiraIssueClient if isKanban true else ScrumJiraIssueClient
     */
    public JiraIssueClient getJiraIssueDataClient(ProjectConfFieldMapping projectConfig) {
        if (projectConfig.isKanban()) {
            return kanbanJiraIssueClient;
        } else {
           return scrumJiraIssueClient;
        }
    }
}
