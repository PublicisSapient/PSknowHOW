package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogRepository;
import com.publicissapient.kpidashboard.jira.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateIssueBacklogandIssueBacklogHistoryImplTest {

    @Mock
    private IssueBacklogRepository issueBacklogRepository;

    @Mock
    private IssueBacklogCustomHistoryRepository issueBacklogCustomHistoryRepository;

    @InjectMocks
    private CreateIssueBacklogandIssueBacklogHistoryImpl createIssueBacklogandIssueBacklogHistory;

    JiraIssue jiraIssue;
    @Mock
    private FieldMapping fieldMapping;

    @Before
    public void setup() throws URISyntaxException {
        jiraIssue = getMockJiraIssue();
        FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory.newInstance("/json/default/field_mapping.json");
        fieldMapping = fieldMappingDataFactory.findById("63bfa0f80b28191677615735");

    }

    @Test
    public void createIssueBacklogandIssueBacklogHistoryImpl(){
        when(issueBacklogRepository.findByIssueIdAndBasicProjectConfigId(any(),any())).thenReturn(Collections.emptyList());
        when(issueBacklogCustomHistoryRepository.findByStoryIDAndBasicProjectConfigId(any(),any())).thenReturn(Collections.emptyList());

        createIssueBacklogandIssueBacklogHistory.createIssueBacklogandIssueBacklogHistory(new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),jiraIssue,new JiraIssueCustomHistory(),null,"123",createProjectConfig(),"123",fieldMapping);
    }

    private JiraIssue getMockJiraIssue() {
        JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory
                .newInstance("/json/default/jira_issues.json");
        return jiraIssueDataFactory.findTopByBasicProjectConfigId("63c04dc7b7617e260763ca4e");
    }

    private ProjectConfFieldMapping createProjectConfig(){
        ProjectConfFieldMapping projectConfFieldMapping=ProjectConfFieldMapping.builder().build();
        projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("63c04dc7b7617e260763ca4e"));


        return projectConfFieldMapping;
    }

}
