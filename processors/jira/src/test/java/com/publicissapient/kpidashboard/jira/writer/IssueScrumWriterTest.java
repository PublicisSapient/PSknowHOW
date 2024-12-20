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


package com.publicissapient.kpidashboard.jira.writer;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.item.Chunk;

import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;

@RunWith(MockitoJUnitRunner.class)
public class IssueScrumWriterTest {

    @Mock
    private JiraIssueRepository kanbanJiraIssueRepository;

    @Mock
    private JiraIssueCustomHistoryRepository kanbanJiraIssueHistoryRepository;

    @Mock
    private AccountHierarchyRepository kanbanAccountHierarchyRepository;

    @Mock
    private AssigneeDetailsRepository assigneeDetailsRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private JiraProcessorConfig jiraProcessorConfig;

    @InjectMocks
    private IssueScrumWriter issueScrumWriter;

    @Mock
    private ProjectHierarchyService projectHierarchyService;

    @Test
    public void testWrite() throws Exception {
        // Mock data
        Chunk<CompositeResult> kanbanCompositeResults = createMockScrumCompositeResults();
        //when(jiraProcessorConfig.getPageSize()).thenReturn(50);
        // Invoke the method to be tested
        issueScrumWriter.write(kanbanCompositeResults);

        // Verify interactions with repositories
        verify(kanbanJiraIssueRepository, times(1)).saveAll(createMockJiraItems());
    }


    // Helper methods to create mock data for testing
    private Chunk<CompositeResult> createMockScrumCompositeResults() {
        CompositeResult compositeResult = new CompositeResult();
        compositeResult.setJiraIssue(new ArrayList<>(createMockJiraItems()).get(0));
        compositeResult.setProjectHierarchies((createMockAccountHierarchies()));
        compositeResult.setAssigneeDetails(createMockAssigneesToSave().get("0"));
        compositeResult.setJiraIssueCustomHistory(createMockScrumIssueCustomHistory().get(0));
        SprintDetails sprintDetails=new SprintDetails();
        sprintDetails.setSprintID("1234");
        compositeResult.setSprintDetailsSet(new HashSet<>(Arrays.asList(sprintDetails)));
        Chunk<CompositeResult> kanbanCompositeResults = new Chunk<>();
        kanbanCompositeResults.add(compositeResult);
        return kanbanCompositeResults;
    }

    private List<JiraIssue> createMockJiraItems() {
        JiraIssue kanbanJiraIssue = new JiraIssue();
        kanbanJiraIssue.setId(new ObjectId("63bfa0f80b28191677615735"));
        List<JiraIssue> jiraItems = new ArrayList<>();
        jiraItems.add(kanbanJiraIssue);
        // Create mock KanbanJiraIssue objects and add them to the list
        return jiraItems;
    }

    private List<JiraIssueCustomHistory> createMockScrumIssueCustomHistory() {
        JiraIssueCustomHistory kanbanIssueCustomHistory = new JiraIssueCustomHistory();
        kanbanIssueCustomHistory.setId(new ObjectId("63bfa0f80b28191677615735"));
        List<JiraIssueCustomHistory> kanbanIssueCustomHistoryList = new ArrayList<>();
        kanbanIssueCustomHistoryList.add(kanbanIssueCustomHistory);
        // Create mock KanbanIssueCustomHistory objects and add them to the list
        return kanbanIssueCustomHistoryList;
    }

    private Set<ProjectHierarchy> createMockAccountHierarchies() {
        ProjectHierarchy kanbanAccountHierarchy = new ProjectHierarchy();
        kanbanAccountHierarchy.setId(new ObjectId("63bfa0f80b28191677615735"));
        Set<ProjectHierarchy> accountHierarchies = new HashSet<>();
        accountHierarchies.add(kanbanAccountHierarchy);
        // Create mock KanbanAccountHierarchy objects and add them to the set
        return accountHierarchies;
    }

    private Map<String, AssigneeDetails> createMockAssigneesToSave() {
        AssigneeDetails assigneeDetails = new AssigneeDetails();
        Assignee assignee = new Assignee();
        Set<Assignee> assignees = new HashSet<>();
        assignees.add(assignee);
        assigneeDetails.setBasicProjectConfigId("63bfa0f80b28191677615735");
        assigneeDetails.setAssignee(assignees);
        Map<String, AssigneeDetails> assigneesToSave = new HashMap<>();
        assigneesToSave.put("0", assigneeDetails);
        // Create mock AssigneeDetails objects and add them to the map
        return assigneesToSave;
    }
}