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

package com.publicissapient.kpidashboard.apis.jira.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.jira.model.BoardDetailsDTO;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceKanbanR;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.jira.service.JiraToolConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.common.model.application.AssigneeDetailsDTO;
import com.publicissapient.kpidashboard.common.model.application.dto.AssigneeResponseDTO;

/**
 * This class test the Jira Controller. In no way this representation of an
 * integration test. All the underlying services and repositories has been
 * mocked. Please don't format this class to keep the readability. Follow [
 * https://stackoverflow.com/questions/5115088/turn-off-eclipse-formatter-for-selected-code-area
 * ] to switch off formatter for section of code.
 *
 * @author tauakram
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class JiraControllerRTest {

	private MockMvc mockMvc;

	@Mock
	private CacheService cacheService;

	@Mock
	private JiraServiceR jiraService;

	@Mock
	private JiraServiceKanbanR jiraServiceKanban;

	@InjectMocks
	private JiraController jiraController;

	@Mock
	private JiraToolConfigServiceImpl jiraToolConfigService;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(jiraController).build();
	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void getJiraKPIMetricReturnsValue() throws Exception {
		// TODO GIRISH here discuss with team
		//@formatter:off
		String request = "{\n" +
				"  \"kpiList\": [\n" +
				"    {\n" +
				"      \"id\": \"5b753628d42937acd035b7ef\",\n" +
				"      \"kpiId\": \"kpi14\",\n" +
				"      \"kpiName\": \"Defect Injection Rate\",\n" +
				"      \"isDeleted\": \"False\",\n" +
				"      \"kpiCategory\": \"Quality\",\n" +
				"      \"kpiUnit\": \"Percentage\",\n" +
				"      \"kpiSource\": \"Jira\",\n" +
				"      \"maxValue\": \"\",\n" +
				"      \"chartType\": \"gaugeChart\"\n" +
				"    }\n" +
				"  ],\n" +
				"  \"ids\": [\n" +
				"    \"GMA_GMA\"\n" +
				"  ],\n" +
				"  \"level\": 1\n" +
				"}";
		//@formatter:on

		List<KpiElement> kpiElementList = new ArrayList<>();
		KpiElement kpiElement = new KpiElement();
		kpiElement.setValue(100);
		kpiElement.setId("5b6be4bf39e7aef89a0fc456");
		kpiElement.setKpiSource("Jira");
		kpiElementList.add(kpiElement);
		when(jiraService.process(Mockito.any())).thenReturn(kpiElementList);
		mockMvc.perform(post("/jira/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andExpect(status().is2xxSuccessful());

	}

	@Test
	public void getJiraKPIMetricReturns400() throws Exception {
		//@formatter:off
		String request = "{\n" +
				"  \"level\": 3,\n" +
				"  \"ids\": [\n" +
				"    \"OPRO Sprint 71_12138_10304_PR\",\n" +
				"    \"OPRO Sprint 72_12139_10304_PR\"\n" +
				"  ],\n" +
				"  \"kpiList\": []\n" +
				"}";

		mockMvc.perform(post("/jira/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andDo(print()).andExpect(status().isBadRequest());

	}

	@Test
	public void getJiraKanbanKPIMetricReturnsValue() throws Exception {
		//@formatter:off
		String request = "{\n" +
				"  \"kpiList\": [\n" +
				"    {\n" +
				"      \"id\": \"5b753628d42937acd035b7ef\",\n" +
				"      \"kpiId\": \"kpi48\",\n" +
				"      \"kpiName\": \"Total Ticket Count\",\n" +
				"      \"isDeleted\": \"False\",\n" +
				"      \"kpiCategory\": \"Quality\",\n" +
				"      \"kpiUnit\": \"Percentage\",\n" +
				"      \"kpiSource\": \"Jira\",\n" +
				"      \"maxValue\": \"\",\n" +
				"      \"chartType\": \"gaugeChart\"\n" +
				"    }\n" +
				"  ],\n" +
				"  \"ids\": [\n" +
				"    \"GMA_GMA\"\n" +
				"  ],\n" +
				"  \"level\": 1\n" +
				"}";
		//@formatter:on

		List<KpiElement> kpiElementList = new ArrayList<>();
		KpiElement kpiElement = new KpiElement();
		kpiElement.setValue(100);
		kpiElement.setId("5b6be4bf39e7aef89a0fc456");
		kpiElement.setKpiSource("JiraKanban");
		kpiElementList.add(kpiElement);
		when(jiraServiceKanban.process(Mockito.any())).thenReturn(kpiElementList);
		mockMvc.perform(post("/jirakanban/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andExpect(status().is2xxSuccessful());

	}

	@Test
	public void getJiraKanbanKPIMetricReturns400() throws Exception {
		//@formatter:off
		String request = "{\n" +
				"  \"level\": 2,\n" +
				"  \"ids\": [\n" +
				"    \"PR\",\n" +
				"  ],\n" +
				"  \"kpiList\": []\n" +
				"}";

		mockMvc.perform(post("/jirakanban/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andDo(print()).andExpect(status().isBadRequest());

	}

	@Test
	public void getJiraBoardDetailsListReturnValue() throws Exception {
		//@formatter:off
		String request = "{\n"
				+ "    \"connectionId\" : \"6324559257413703ba3bf4ed\",\n"
				+ "    \"projectKey\" : \"TestKey\",\n"
				+ "    \"boardType\" : \"scrum\"\n"
				+ "}";
		//@formatter:on

		List<BoardDetailsDTO> boardDetailsList = new ArrayList<>();

		BoardDetailsDTO boardDetailsDTO1 = new BoardDetailsDTO();
		boardDetailsDTO1.setBoardId(1110L);
		boardDetailsDTO1.setBoardName("Scrum Test Board1");
		BoardDetailsDTO boardDetailsDTO2 = new BoardDetailsDTO();
		boardDetailsDTO2.setBoardId(1111L);
		boardDetailsDTO2.setBoardName("Scrum Test Board2");
		boardDetailsList.add(boardDetailsDTO1);
		boardDetailsList.add(boardDetailsDTO2);
		when(jiraToolConfigService.getJiraBoardDetailsList(Mockito.any())).thenReturn(boardDetailsList);
		mockMvc.perform(post("/jira/board").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andExpect(status().is2xxSuccessful());

	}

	@Test
	public void getJiraBoardDetailsListReturn400() throws Exception {
		//@formatter:off
		String request = "{\n"
				+ "    \"connectionId\" : \"6324559257413703ba3bf4ed\",\n"
				+ "    \"projectKey\" : \"TestKey\",\n"
				+ "    \"boardType\" : \"scrum\"\n"
				+ "}";
		//@formatter:on

		mockMvc.perform(post("/jirakanban/kpi").contentType(MediaType.APPLICATION_JSON_UTF8).content(request))
				.andDo(print()).andExpect(status().isBadRequest());

	}

	@Test
	public void getJiraAssigneesListReturnError() throws Exception {
		mockMvc.perform(get("/jira/assignees/").contentType(MediaType.APPLICATION_JSON_UTF8)).andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	public void getJiraAssigneesListReturnValue() throws Exception {
		String request = "634fdf4ec859a424263dc035";
		AssigneeResponseDTO assigneeResponseDTO = new AssigneeResponseDTO();
		List<AssigneeDetailsDTO> assigneeDetailsDTOList = new ArrayList<>();
		AssigneeDetailsDTO assigneeDetailsDTO = new AssigneeDetailsDTO();
		assigneeDetailsDTO.setName("Radhu");
		assigneeDetailsDTO.setDisplayName("Raghavendra");
		assigneeDetailsDTOList.add(assigneeDetailsDTO);

		assigneeResponseDTO.setBasicProjectConfigId(new ObjectId(request));
		assigneeResponseDTO.setAssigneeDetailsList(assigneeDetailsDTOList);
		when(jiraToolConfigService.getProjectAssigneeDetails(Mockito.any())).thenReturn(assigneeResponseDTO);
		mockMvc.perform(get("/jira/assignees/634fdf4ec859a424263dc035").contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().is2xxSuccessful());
	}
}
