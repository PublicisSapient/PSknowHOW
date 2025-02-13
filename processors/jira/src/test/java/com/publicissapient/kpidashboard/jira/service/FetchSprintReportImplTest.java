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


package com.publicissapient.kpidashboard.jira.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.BeanUtils;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.dataFactories.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.jira.model.JiraProcessor;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;

@RunWith(MockitoJUnitRunner.class)
public class FetchSprintReportImplTest {

	List<SprintDetails> sprintDetailsList = new ArrayList<>();
	Set<SprintDetails> sprintDetailsSet = new HashSet<>();
	ProjectConfFieldMapping projectConfig = ProjectConfFieldMapping.builder().build();
	String sprintResponse;
	KerberosClient krb5Client;
	BoardDetails jiraBoard;
	@Mock
	private SprintRepository sprintRepository;
	@Mock
	private JiraProcessorRepository jiraProcessorRepository;
	@Mock
	private JiraProcessorConfig jiraProcessorConfig;
	@Mock
	private JiraCommonService jiraCommonService;
	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;
	@InjectMocks
	private FetchSprintReportImpl fetchSprintReport;

	@Before
	public void setUp() throws Exception {
		sprintDetailsList = getMockSprintDetails();

		JiraToolConfig projectToolConfig = new JiraToolConfig();
		projectToolConfig.setBasicProjectConfigId("5ba8e182d3735010e7f1fa45");
		Optional<Connection> conn = Optional.of(new Connection());
		conn.get().setOffline(Boolean.FALSE);
		conn.get().setBaseUrl("https://abcd.com/jira");
		conn.get().setUsername("jira");
		conn.get().setPassword("hRjE0RY0GkbiZirguoqtcO/niMjBTcdvwOji0ZEpL6yl6e5L7/hBs0dsBM43mGiF");

		jiraBoard = new BoardDetails();
		jiraBoard.setBoardId("11856");
		jiraBoard.setBoardName("DTS");
		List<BoardDetails> jiraBoardList = new ArrayList<>();
		jiraBoardList.add(jiraBoard);
		projectToolConfig.setBoards(jiraBoardList);
		projectToolConfig.setConnection(conn);

		ProjectToolConfig toolConfig = new ProjectToolConfig();
		BeanUtils.copyProperties(toolConfig, projectToolConfig);

		JiraProcessor processor = new JiraProcessor();
		processor.setId(new ObjectId("5ba8e182d3735010e7f1fa45"));

		projectConfig.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectConfig.setJira(projectToolConfig);
		projectConfig.setProjectToolConfig(toolConfig);
		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectBasicConfig.setProjectNodeId("projectNodeId");
		projectConfig.setProjectBasicConfig(projectBasicConfig);

		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setJiraStoryPointsCustomField(null);
		projectConfig.setFieldMapping(fieldMapping);

		sprintDetailsSet = getSprintDetails();

		FileInputStream fis1 = new FileInputStream("src/test/resources/json/default/sprint_response.txt");
		sprintResponse = IOUtils.toString(fis1, "UTF-8");
	}

	@Test
	public void fetchSprints() throws InterruptedException, IOException {
		when(sprintRepository.findBySprintIDIn(any())).thenReturn(sprintDetailsList);
		when(jiraProcessorConfig.getSubsequentApiCallDelayInMilli()).thenReturn(1000l);
		when(jiraProcessorConfig.getJiraServerSprintReportApi()).thenReturn(
				"rest/greenhopper/latest/rapid/charts/SprintDetails?rapidViewId={rapidViewId}&sprintId={sprintId}");
//		when(jiraProcessorConfig.getJiraCloudGetUserApi()).thenReturn(
//				"jira.jiraServerSprintDetailsApi=rest/greenhopper/latest/rapid/charts/SprintDetails?rapidViewId={rapidViewId}&sprintId={sprintId}");
		when(jiraCommonService.getDataFromClient(any(), any(),any())).thenReturn(sprintResponse);
		Assert.assertEquals(1,
				fetchSprintReport.fetchSprints(projectConfig, sprintDetailsSet, krb5Client, false, new ObjectId()).size());
	}

	private List<SprintDetails> getMockSprintDetails() {
		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory
				.newInstance("/json/default/sprint_details.json");
		return sprintDetailsDataFactory.getSprintDetails();
	}

	private Set<SprintDetails> getSprintDetails() {
		Set<SprintDetails> set = new HashSet<>();
		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintID("41412_Bazooka Unilever_63bfa0d5b7617e260763ca21");
		sprintDetails.setOriginalSprintId("41412");
		List<String> list = new ArrayList<>();
		list.add("11857");
		sprintDetails.setOriginBoardId(list);
		sprintDetails.setState("CLOSE");
		set.add(sprintDetails);
		return set;
	}

	@Test
	public void createSprintDetailBasedOnBoard() throws IOException, InterruptedException {

		when(jiraProcessorConfig.getJiraSprintByBoardUrlApi())
				.thenReturn("rest/agile/1.0/board/{boardId}/sprint?startAt={startAtIndex}");
		when(jiraProcessorConfig.getSprintReportCountToBeFetched()).thenReturn(15);
		when(sprintRepository.findBySprintIDIn(any())).thenReturn(sprintDetailsList);
		when(jiraProcessorConfig.getSubsequentApiCallDelayInMilli()).thenReturn(1000l);
		when(jiraProcessorConfig.getJiraServerSprintReportApi()).thenReturn(
				"rest/greenhopper/latest/rapid/charts/SprintDetails?rapidViewId={rapidViewId}&sprintId={sprintId}");
//		when(jiraProcessorConfig.getJiraCloudGetUserApi()).thenReturn(
//				"jira.jiraServerSprintDetailsApi=rest/greenhopper/latest/rapid/charts/SprintDetails?rapidViewId={rapidViewId}&sprintId={sprintId}");
		when(jiraCommonService.getDataFromClient(any(), any(),any())).thenReturn(sprintResponse);
		Assert.assertEquals(15,
				fetchSprintReport.createSprintDetailBasedOnBoard(projectConfig, krb5Client, jiraBoard, new ObjectId("5e16c126e4b098db673cc372")).size());

	}

}
