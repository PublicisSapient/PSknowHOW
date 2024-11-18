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

package com.publicissapient.kpidashboard.apis.githubaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.connection.service.ConnectionService;
import com.publicissapient.kpidashboard.apis.githubaction.model.GithubActionWorkflowsDTO;
import com.publicissapient.kpidashboard.apis.util.RestAPIUtils;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

@RunWith(MockitoJUnitRunner.class)
public class GithubActionToolConfigServiceImplTest {

	@InjectMocks
	GithubActionToolConfigServiceImpl githubActionToolConfigService;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private RestAPIUtils restAPIUtils;

	@Mock
	private ConnectionService connectionService;
	@Mock
	private ConnectionRepository connectionRepository;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@Mock
	private CustomApiConfig customApiConfig;
	String connectionId = "5fb3a6412064a35b8069930a";
	String repoName = "validRepoName";

	@Before
	public void setUp() {
	}

	@Test
    public void testGetGitHubWorkFlowList_ValidInput_Success() {
        

        when(connectionRepository.findById(any())).thenReturn(Optional.of(new Connection()));
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(getMockGitHubApiResponse(), HttpStatus.OK));
        JSONArray workflows = getMockWorkflowsArray();
        when(restAPIUtils.getJsonArrayFromJSONObj(any(), eq("workflows")))
                .thenReturn(workflows);
		when(restAPIUtils.convertToString((JSONObject) workflows.get(0), "id"))
				.thenReturn(((JSONObject) workflows.get(0)).get("id").toString());
        when(restAPIUtils.convertToString((JSONObject) workflows.get(0), "path"))
                .thenReturn(((JSONObject) workflows.get(0)).get("path").toString());

        List<GithubActionWorkflowsDTO> result = githubActionToolConfigService.getGitHubWorkFlowList(connectionId, repoName);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

	@Test
	public void testGetGitHubWorkFlowListException() {

		when(connectionRepository.findById(any())).thenReturn(Optional.of(new Connection()));
		when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
				.thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
		doNothing().when(connectionService).updateBreakingConnection(eq(new Connection()), anyString());
		githubActionToolConfigService.getGitHubWorkFlowList(connectionId, repoName);
	}

	@Test
    public void testGetGitHubWorkFlowList_InvalidConnectionId_ReturnsEmptyList() {

        when(connectionRepository.findById(any())).thenReturn(Optional.empty());

        List<GithubActionWorkflowsDTO> result = githubActionToolConfigService.getGitHubWorkFlowList(connectionId, repoName);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

	private String getMockGitHubApiResponse() {
		return "{ \"workflows\": [ { \"id\": \"123\", \"path\": \"/workflows/main.yml\" } ] }";
	}

	@Test
    public void testGetGitHubWorkFlowList_GitHubApiError_LogsError() {

        when(connectionRepository.findById(any())).thenReturn(Optional.of(new Connection()));
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR));

        List<GithubActionWorkflowsDTO> result = githubActionToolConfigService.getGitHubWorkFlowList(connectionId, repoName);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

	private JSONArray getMockWorkflowsArray() {
		JSONArray workflowsArray = new JSONArray();
		JSONObject workflowObject = new JSONObject();
		workflowObject.put("id", "123");
		workflowObject.put("path", "/workflows/main.yml");
		workflowsArray.add(workflowObject);
		return workflowsArray;
	}
}