package com.publicissapient.kpidashboard.jira.service;

import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.StatusCategory;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.helper.JiraHelper;
import io.atlassian.util.concurrent.Promise;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateJiraIssueReleaseStatusImplTest {

    @Mock
    private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;

    @Mock
    private ProcessorJiraRestClient client;

    @Mock
    private MetadataRestClient metadataRestClient;

    @Mock
    Promise<Iterable<Status>> metaDataStatusPromise;

    @InjectMocks
    private CreateJiraIssueReleaseStatusImpl createJiraIssueReleaseStatus;

    Iterable<Status> statusItr;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessAndSaveProjectStatusCategory_NewStatusCategory() throws URISyntaxException {
        // Mock data
        String basicProjectConfigId = "project123";
        List<Status> listOfProjectStatus = createMockStatusList();

        // Mock repository behavior
        when(jiraIssueReleaseStatusRepository.findByBasicProjectConfigId(any())).thenReturn(null);
        when(client.getMetadataClient()).thenReturn(metadataRestClient);
        when(metadataRestClient.getStatuses()).thenReturn(metaDataStatusPromise);
        when(metaDataStatusPromise.claim()).thenReturn(statusItr);
        when(JiraHelper.getStatus(client)).thenReturn(listOfProjectStatus);

        // Invoke the method to be tested
        createJiraIssueReleaseStatus.processAndSaveProjectStatusCategory(client, basicProjectConfigId);
        Map<Long,String> map=new HashMap<>();
        map.put(1l,"To Do");
        JiraIssueReleaseStatus jiraIssueReleaseStatus=JiraIssueReleaseStatus.builder().basicProjectConfigId("project123").toDoList(map).closedList(new HashMap<>()).inProgressList(new HashMap<>()).build();

        verify(jiraIssueReleaseStatusRepository).save(jiraIssueReleaseStatus);
    }

    private List<Status> createMockStatusList() throws URISyntaxException {
        StatusCategory statusCategory=new StatusCategory(new URI("self"),"To Do",0l,"key","colour");
        Status status=new Status(new URI("self"),1l,"To Do","des",null,statusCategory);
        List<Status> statusList = new ArrayList<>();
        statusList.add(status);

        return statusList;
    }

}
