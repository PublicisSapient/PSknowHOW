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