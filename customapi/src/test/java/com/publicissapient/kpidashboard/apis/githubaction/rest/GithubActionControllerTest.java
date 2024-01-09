package com.publicissapient.kpidashboard.apis.githubaction.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.githubaction.model.GithubActionRepoDTO;
import com.publicissapient.kpidashboard.apis.githubaction.model.GithubActionWorkflowsDTO;
import com.publicissapient.kpidashboard.apis.githubaction.service.GithubActionToolConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

@RunWith(MockitoJUnitRunner.class)
public class GithubActionControllerTest {

    @InjectMocks
    private GithubActionController githubActionController;

    @Mock
    private GithubActionToolConfigServiceImpl githubActionToolConfigService;

    @Test
    public void testGetGithubActionWorkflows_SuccessfulFetch() {
        String connectionId = "5fb3a6412064a35b8069930a";
        GithubActionRepoDTO repoDTO = new GithubActionRepoDTO("repoName", connectionId);

        List<GithubActionWorkflowsDTO> mockWorkflows = new ArrayList<>();
        mockWorkflows.add(new GithubActionWorkflowsDTO("workflowName", "workflowId"));

        when(githubActionToolConfigService.getGitHubWorkFlowList(any(), any())).thenReturn(mockWorkflows);

        ServiceResponse response = githubActionController.getGithubActionWorkflows(connectionId, repoDTO);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("FETCHED_SUCCESSFULLY", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(mockWorkflows, response.getData());
    }

    @Test
    public void testGetGithubActionWorkflows_NoWorkflowsFound() {
        String connectionId = "5fb3a6412064a35b8069930a";
        GithubActionRepoDTO repoDTO = new GithubActionRepoDTO("repoName", connectionId);

        when(githubActionToolConfigService.getGitHubWorkFlowList(any(), any())).thenReturn(new ArrayList<>());

        ServiceResponse response = githubActionController.getGithubActionWorkflows(connectionId, repoDTO);

        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("No workflow details found", response.getMessage());
        assertNull(response.getData());
    }


}