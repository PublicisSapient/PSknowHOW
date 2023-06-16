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

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public class JiraIssueClientFactoryTest {

    @Mock
    private KanbanJiraIssueClientImpl kanbanJiraIssueClient;

    @Mock
    private ScrumJiraIssueClientImpl scrumJiraIssueClient;

    @InjectMocks
    JiraIssueClientFactory jiraIssueClientFactory;
    ProjectConfFieldMapping projectConfFieldMapping;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void getJiraIssueDataClientKanban() {
        prepareProjectConfig();
        Assert.assertEquals(kanbanJiraIssueClient,jiraIssueClientFactory.getJiraIssueDataClient(projectConfFieldMapping));
    }

    @Test
    public void getJiraIssueDataClientScrum() {
        prepareProjectConfigScrum();
        Assert.assertEquals(scrumJiraIssueClient,jiraIssueClientFactory.getJiraIssueDataClient(projectConfFieldMapping));
    }

    private void prepareProjectConfig(){
        projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
        projectConfFieldMapping.setKanban(true);
    }

    private void prepareProjectConfigScrum(){
        projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
        projectConfFieldMapping.setKanban(false);
    }




}