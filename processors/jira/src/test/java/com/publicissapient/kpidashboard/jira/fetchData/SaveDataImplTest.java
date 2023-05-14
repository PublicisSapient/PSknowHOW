package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.*;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.*;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SaveDataImplTest {

    @Mock
    private AccountHierarchyRepository accountHierarchyRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private JiraIssueRepository jiraIssueRepository;

    @Mock
    private JiraProcessorConfig jiraProcessorConfig;

    @Mock
    private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

    @Mock
    private AssigneeDetailsRepository assigneeDetailsRepository;

    @InjectMocks
    private SaveDataImpl saveData;

    List<JiraIssue> jiraIssueList=new ArrayList<>();
    List<KanbanJiraIssue> kanbanJiraIssuesList=new ArrayList<>();
    List<JiraIssueCustomHistory> jiraIssueCustomHistoryList=new ArrayList<>();
    List<KanbanIssueCustomHistory> kanbanIssueCustomHistoryList=new ArrayList<>();

    List<SprintDetails> sprintDetailsToSave=new ArrayList<>();

    Set<AccountHierarchy> accountHierarchySet=new HashSet<>();
    Set<KanbanAccountHierarchy> kanbanAccountHierarchySet=new HashSet<>();

    @Mock
    AssigneeDetails assigneeDetailsToSave;

    @Mock
    private KanbanJiraIssueRepository kanbanJiraIssueRepository;

    @Mock
    private KanbanJiraIssueHistoryRepository kanbanJiraIssueHistoryRepository;

    @Mock
    private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepository;

    @Before
    public void setUp(){
        JiraIssue jiraIssue= JiraIssue.builder().issueId("11111").build();
        jiraIssueList.add(jiraIssue);
        JiraIssueCustomHistory jiraIssueCustomHistory=new JiraIssueCustomHistory();
        jiraIssueCustomHistoryList.add(jiraIssueCustomHistory);
        SprintDetails sprintDetails=new SprintDetails();
        sprintDetailsToSave.add(sprintDetails);
        AccountHierarchy accountHierarchy=new AccountHierarchy();
        accountHierarchySet.add(accountHierarchy);
        assigneeDetailsToSave=new AssigneeDetails();
        KanbanJiraIssue kanbanJiraIssue=new KanbanJiraIssue();
        kanbanJiraIssuesList.add(kanbanJiraIssue);
        KanbanAccountHierarchy kanbanAccountHierarchy=new KanbanAccountHierarchy();
        kanbanAccountHierarchySet.add(kanbanAccountHierarchy);
        KanbanIssueCustomHistory kanbanIssueCustomHistory=new KanbanIssueCustomHistory();
        kanbanIssueCustomHistoryList.add(kanbanIssueCustomHistory);
    }

    @Test
    public void saveData(){
        when(jiraIssueRepository.findTopByBasicProjectConfigId(any())).thenReturn(null);
        when(jiraIssueRepository.saveAll(any())).thenReturn(null);
        when(jiraIssueCustomHistoryRepository.saveAll(any())).thenReturn(null);
        when(sprintRepository.saveAll(any())).thenReturn(null);
        when(accountHierarchyRepository.saveAll(any())).thenReturn(null);
        when(assigneeDetailsRepository.save(any())).thenReturn(null);
        when(kanbanAccountHierarchyRepository.saveAll(any())).thenReturn(null);
        when(kanbanJiraIssueHistoryRepository.saveAll(any())).thenReturn(null);
        when(kanbanJiraIssueRepository.saveAll(any())).thenReturn(null);
        saveData.saveData(jiraIssueList,jiraIssueCustomHistoryList,sprintDetailsToSave,accountHierarchySet,assigneeDetailsToSave,kanbanJiraIssuesList,kanbanIssueCustomHistoryList,kanbanAccountHierarchySet);
    }
    
}
