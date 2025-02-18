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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.BeanUtils;
import org.springframework.web.client.RestTemplate;

import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.StatusCategory;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;
import com.publicissapient.kpidashboard.common.model.jira.Identifier;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.dataFactories.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.MetadataIdentifierDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ToolConfigDataFactory;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import io.atlassian.util.concurrent.Promise;

@RunWith(MockitoJUnitRunner.class)
public class CreateMetadataImplTest {

	@Mock
	Promise<Iterable<Field>> metaDataFieldPromise;
	@Mock
	Promise<Iterable<IssueType>> metaDataIssueTypePromise;
	@Mock
	Promise<Iterable<Status>> metaDataStatusPromise;
	@Mock
	RestTemplate restTemplate;
	List<ProjectToolConfig> projectToolConfigs;
	List<FieldMapping> fieldMappingList;
	Optional<Connection> connection;
	List<ProjectBasicConfig> projectConfigsList;
	Iterable<Field> fieldItr;
	Iterable<IssueType> issueTypeItr;
	Iterable<Status> statusItr;
	@Mock
	private BoardMetadataRepository boardMetadataRepository;
	@Mock
	private MetadataIdentifierRepository metadataIdentifierRepository;
	@Mock
	private ProcessorJiraRestClient client;
	@Mock
	private JiraProcessorConfig jiraProcessorConfig;
	@Mock
	private FieldMappingRepository fieldMappingRepository;
	@InjectMocks
	private CreateMetadataImpl createMetadata;
	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;

	@Before
	public void setup() throws URISyntaxException {
		projectToolConfigs = getMockProjectToolConfig();
		fieldMappingList = getMockFieldMapping();
		connection = getMockConnection();
		projectConfigsList = getMockProjectConfig();

		Field field1 = new Field("Story Points", "customfield_20803", FieldType.JIRA, true, true, true, null);
		Field field2 = new Field("Sprint", "customfield_12700", FieldType.JIRA, true, true, true, null);
		Field field3 = new Field("Root Cause", "customfield_19121", FieldType.JIRA, true, true, true, null);
		Field field4 = new Field("Tech Debt", "customfield_59601", FieldType.JIRA, true, true, true, null);
		Field field5 = new Field("UAT", "UAT", FieldType.JIRA, true, true, true, null);
		List<Field> fields = Arrays.asList(field1, field2, field3, field4, field5);

		fieldItr = fields;

		IssueType issueType1 = new IssueType(new URI("self"), 1l, "Story", false, "desc", new URI("iconURI"));
		IssueType issueType2 = new IssueType(new URI("self"), 1l, "Enabler Story", false, "desc", new URI("iconURI"));
		IssueType issueType3 = new IssueType(new URI("self"), 1l, "Tech Story", false, "desc", new URI("iconURI"));
		IssueType issueType4 = new IssueType(new URI("self"), 1l, "Change request", false, "desc", new URI("iconURI"));
		IssueType issueType5 = new IssueType(new URI("self"), 1l, "Defect", false, "desc", new URI("iconURI"));
		IssueType issueType6 = new IssueType(new URI("self"), 1l, "Epic", false, "desc", new URI("iconURI"));
		IssueType issueType7 = new IssueType(new URI("self"), 1l, "UAT Defect", false, "desc", new URI("iconURI"));
		List<IssueType> issueTypes = Arrays.asList(issueType1, issueType2, issueType3, issueType4, issueType5, issueType6,
				issueType7);

		issueTypeItr = issueTypes;

		Status status1 = new Status(new URI("self"), 1l, "Ready for Sprint Planning", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		Status status2 = new Status(new URI("self"), 1l, "Closed", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		Status status3 = new Status(new URI("self"), 1l, "Implementing", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		Status status4 = new Status(new URI("self"), 1l, "In Testing", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		List<Status> statuses = Arrays.asList(status1, status2, status3, status4);

		statusItr = statuses;

		when(boardMetadataRepository.findByProjectBasicConfigId(any())).thenReturn(null);

		MetadataRestClient metadataRestClient = mock(MetadataRestClient.class);
		when(client.getMetadataClient()).thenReturn(metadataRestClient);

		when(metadataRestClient.getFields()).thenReturn(metaDataFieldPromise);
		when(metaDataFieldPromise.claim()).thenReturn(fieldItr);

		when(metadataRestClient.getIssueTypes()).thenReturn(metaDataIssueTypePromise);
		when(metaDataIssueTypePromise.claim()).thenReturn(issueTypeItr);

		when(metadataRestClient.getStatuses()).thenReturn(metaDataStatusPromise);
		when(metaDataStatusPromise.claim()).thenReturn(statusItr);
	}

	@Test
	public void collectMetadata() throws Exception {

		// when(fieldMappingRepository.save(any())).thenReturn(null);
		// when(boardMetadataRepository.save(any())).thenReturn(null);

		MetadataIdentifier metadataIdentifier = getMetadataIdentifier().get(0);
		when(metadataIdentifierRepository.findByTemplateCodeAndToolAndIsKanban(any(), any(), any()))
				.thenReturn(metadataIdentifier);

		Assert.assertThrows(Exception.class,
				() -> createMetadata.collectMetadata(createProjectConfig(false), client, "false"));
	}

	@Test
	public void collectMetadata2() throws Exception {

		MetadataIdentifier metadataIdentifier = createMetaDataIdentifier(true);
		when(metadataIdentifierRepository.findByTemplateCodeAndToolAndIsKanban(any(), any(), any()))
				.thenReturn(metadataIdentifier);
		Assert.assertThrows(Exception.class,
				() -> createMetadata.collectMetadata(createProjectConfig(true), client, "false"));
	}

	@Test
	public void collectMetadata3() throws Exception {

		MetadataIdentifier metadataIdentifier = createMetaDataIdentifier(false);
		when(metadataIdentifierRepository.findByTemplateCodeAndToolAndIsKanban(any(), any(), any()))
				.thenReturn(metadataIdentifier);
		Assert.assertThrows(Exception.class,
				() -> createMetadata.collectMetadata(createProjectConfig(true), client, "false"));
	}

	@Test
	public void collectMetadataWithBoardMetadata() throws Exception {
		when(boardMetadataRepository.findByProjectBasicConfigId(any())).thenReturn(new BoardMetadata());
		createMetadata.collectMetadata(createProjectConfig(true), client, "true");
	}

	private MetadataIdentifier createMetaDataIdentifier(boolean flag) {
		String tool = "Jira";
		Boolean isKanban = Boolean.TRUE;

		Identifier issue1 = createIdentifier("story",
				Arrays.asList("Story", "Enabler Story", "Tech Story", "Change request"));
		Identifier issue2 = createIdentifier("bug", Arrays.asList("Defect", "Bug"));
		Identifier issue3 = createIdentifier("epic", Arrays.asList("Epic"));
		Identifier issue4 = createIdentifier("issuetype",
				Arrays.asList("Story", "Enabler Story", "Tech Story", "Change request", "Defect", "Bug", "Epic"));
		Identifier issue5 = createIdentifier("uatdefect", Arrays.asList("UAT Defect"));
		Identifier issue6 = createIdentifier("ticketVelocityStatusIssue", Arrays.asList("Change Request"));
		Identifier issue7 = createIdentifier("ticketWipClosedIssue", Arrays.asList("Change Request"));
		Identifier issue8 = createIdentifier("ticketThroughputIssue", Arrays.asList("Change Request"));
		Identifier issue9 = createIdentifier("kanbanCycleTimeIssue", Arrays.asList("Change Request"));
		Identifier issue10 = createIdentifier("ticketReopenIssue", Arrays.asList("Change Request"));
		Identifier issue11 = createIdentifier("kanbanTechDebtIssueType", Arrays.asList("Change Request"));
		Identifier issue12 = createIdentifier("ticketCountIssueType", Arrays.asList("Change Request"));
		Identifier issue13 = createIdentifier("jiraIssueRiskTypeKPI176", Arrays.asList("Risk"));
		Identifier issue14 = createIdentifier("jiraIssueDependencyTypeKPI176", Arrays.asList("Dependency"));
		List<Identifier> issuesIdentifier = Arrays.asList(issue1, issue2, issue3, issue4, issue5, issue6, issue7, issue8,
				issue9, issue10, issue11, issue12, issue13, issue14);

		Identifier customField1 = createIdentifier("storypoint", Arrays.asList("storypoint"));
		Identifier customField2 = createIdentifier("sprint", Arrays.asList("Sprint"));
		Identifier customField3 = createIdentifier("rootcause", Arrays.asList("Root Cause"));
		Identifier customField4 = createIdentifier("techdebt", Arrays.asList("Tech Debt"));
		Identifier customField5 = createIdentifier("uat", Arrays.asList("UAT"));
		Identifier customField6 = createIdentifier("timeCriticality", Arrays.asList("Time Criticality"));
		Identifier customField7 = createIdentifier("wsjf", Arrays.asList("WSJF"));
		Identifier customField8 = createIdentifier("costOfDelay", Arrays.asList("Cost of Delay"));
		Identifier customField9 = createIdentifier("businessValue", Arrays.asList("User-Business Value"));
		Identifier customField10 = createIdentifier("riskReduction",
				Arrays.asList("Risk Reduction-Opportunity Enablement Value"));
		Identifier customField11 = createIdentifier("jobSize", Arrays.asList("Job Size"));
		List<Identifier> customfieldIdentifer = Arrays.asList(customField1, customField2, customField3, customField4,
				customField5, customField6, customField7, customField8, customField9, customField10, customField11);

		Identifier workflow1 = createIdentifier("dor", Arrays.asList("Ready for Sprint Planning", "In Progress"));
		Identifier workflow2 = createIdentifier("dod", Arrays.asList("Closed", "Resolved", "Ready for Delivery"));
		Identifier workflow3 = createIdentifier("qa", Arrays.asList("In Testing"));
		Identifier workflow4 = createIdentifier("firststatus", Arrays.asList("Open"));
		Identifier workflow5 = createIdentifier("rejection", Arrays.asList("Closed", "Rejected"));
		Identifier workflow6 = createIdentifier("delivered",
				Arrays.asList("Closed", "Resolved", "Ready for Delivery", "Ready for Release"));
		Identifier workflow7 = createIdentifier("ticketClosedStatus", Arrays.asList("Open"));
		Identifier workflow8 = createIdentifier("ticketResolvedStatus", Arrays.asList("Open"));
		Identifier workflow9 = createIdentifier("ticketReopenStatus", Arrays.asList("Open"));
		Identifier workflow10 = createIdentifier("ticketTriagedStatus", Arrays.asList("Open"));
		Identifier workflow11 = createIdentifier("ticketWIPStatus", Arrays.asList("Open"));
		Identifier workflow12 = createIdentifier("ticketRejectedStatus", Arrays.asList("Open"));
		Identifier workflow13 = createIdentifier("ticketWIPStatus", Arrays.asList("Open"));
		Identifier workflow14 = createIdentifier("jiraBlockedStatus", Arrays.asList("Open"));
		Identifier workflow15 = createIdentifier("rejectionResolution", Arrays.asList("Open"));
		Identifier workflow16 = createIdentifier("jiraWaitStatus", Arrays.asList("Open"));
		Identifier workflow17 = createIdentifier("jiraStatusForInProgress", Arrays.asList("Open"));
		Identifier workflow18 = createIdentifier("development", Arrays.asList("Open"));
		List<Identifier> workflowIdentifer = Arrays.asList(workflow1, workflow2, workflow3, workflow4, workflow5, workflow6,
				workflow7, workflow8, workflow9, workflow10, workflow11, workflow12, workflow13, workflow14, workflow15,
				workflow16, workflow17, workflow18);

		Identifier valuestoidentify1 = createIdentifier("rootCauseValue", Arrays.asList("Coding"));
		Identifier valuestoidentify2 = createIdentifier("rejectionResolution",
				Arrays.asList("Invalid", "Duplicate", "Unrequired"));
		Identifier valuestoidentify3 = createIdentifier("qaRootCause",
				Arrays.asList("Coding", "Configuration", "Regression", "Data"));
		List<Identifier> valuestoidentifyIdentifer = Arrays.asList(valuestoidentify1, valuestoidentify2, valuestoidentify3);

		List<Identifier> issuelinkIdentifer = new ArrayList<>();
		MetadataIdentifier metadataIdentifier;
		if (flag) {
			return new MetadataIdentifier(tool, "Standard Template", "7", isKanban, false, issuesIdentifier,
					customfieldIdentifer, workflowIdentifer, issuelinkIdentifer, valuestoidentifyIdentifer);
		} else {
			return new MetadataIdentifier(tool, "DOJO Agile Template", "4", isKanban, false, issuesIdentifier,
					customfieldIdentifer, workflowIdentifer, issuelinkIdentifer, valuestoidentifyIdentifer);
		}
	}

	private Identifier createIdentifier(String type, List<String> value) {
		Identifier identifier = new Identifier();
		identifier.setType(type);
		identifier.setValue(value);
		return identifier;
	}

	private ProjectConfFieldMapping createProjectConfig(boolean isKanban) {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		ProjectBasicConfig projectConfig = projectConfigsList.get(2);
		BeanUtils.copyProperties(projectConfig, projectConfFieldMapping);
		projectConfFieldMapping.setProjectBasicConfig(projectConfig);
		if (isKanban) {
			projectConfFieldMapping.setKanban(true);
		} else {
			projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
		}
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping.setJira(getJiraToolConfig());
		projectConfFieldMapping.setJiraToolConfigId(projectToolConfigs.get(0).getId());
		projectConfFieldMapping.setProjectToolConfig(projectToolConfigs.get(0));

		return projectConfFieldMapping;
	}

	private List<ProjectBasicConfig> getMockProjectConfig() {
		ProjectBasicConfigDataFactory projectConfigDataFactory = ProjectBasicConfigDataFactory
				.newInstance("/json/default/project_basic_configs.json");
		return projectConfigDataFactory.getProjectBasicConfigs();
	}

	private JiraToolConfig getJiraToolConfig() {
		JiraToolConfig toolObj = new JiraToolConfig();
		BeanUtils.copyProperties(projectToolConfigs.get(0), toolObj);
		toolObj.setConnection(connection);
		return toolObj;
	}

	private List<ProjectToolConfig> getMockProjectToolConfig() {
		ToolConfigDataFactory projectToolConfigDataFactory = ToolConfigDataFactory
				.newInstance("/json/default/project_tool_configs.json");
		return projectToolConfigDataFactory.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA,
				"63c04dc7b7617e260763ca4e");
	}

	private Optional<Connection> getMockConnection() {
		ConnectionsDataFactory connectionDataFactory = ConnectionsDataFactory.newInstance("/json/default/connections.json");
		return connectionDataFactory.findConnectionById("5fd99f7bc8b51a7b55aec836");
	}

	private List<FieldMapping> getMockFieldMapping() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/field_mapping.json");
		return fieldMappingDataFactory.getFieldMappings();
	}

	private List<MetadataIdentifier> getMetadataIdentifier() {
		MetadataIdentifierDataFactory fieldMappingDataFactory = MetadataIdentifierDataFactory
				.newInstance("/json/default/metadata_identifier.json");
		return fieldMappingDataFactory.getMetadataIdentifiers();
	}
}
