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

package com.publicissapient.kpidashboard.azure.client.metadata;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldSchema;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.publicissapient.kpidashboard.azure.adapter.AzureAdapter;
import com.publicissapient.kpidashboard.azure.model.AzureToolConfig;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.azure.util.AzureConstants;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.azure.AzureStateCategory;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.Identifier;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.azure.AzureStateCategoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;

/*
author @shi6
*/
@ExtendWith(SpringExtension.class)
public class MetaDataClientImplTest {
	@Mock
	AzureAdapter azureAdapter;
	@Mock
	BoardMetadataRepository boardMetadataRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	MetadataIdentifierRepository metadataIdentifierRepository;
	@Mock
	AzureStateCategoryRepository azureStateCategoryRepository;
	@Mock
	Logger log;
	@InjectMocks
	MetaDataClientImpl metaDataClientImpl;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testProcessMetadataWithNoValueInStateCategory() {
		when(azureAdapter.getIssueType()).thenReturn(createIssueType());
		when(azureAdapter.getField()).thenReturn(createField());
		when(azureAdapter.getStatus()).thenReturn(createStatus());
		when(fieldMappingRepository.save(any())).thenReturn(new FieldMapping());

		when(metadataIdentifierRepository.findByToolAndIsKanban(anyString(), anyBoolean()))
				.thenReturn(
						new MetadataIdentifier(
								"tool",
								"templateName",
								"templateCode",
								Boolean.TRUE,
								true,
								new ArrayList<>(),
								new ArrayList<>(),
								new ArrayList<>(),
								new ArrayList<>(),
								new ArrayList<>()));
		when(azureStateCategoryRepository.findByBasicProjectConfigId(anyString()))
				.thenReturn(
						new AzureStateCategory(
								"basicProjectConfigId",
								Set.of("String"),
								Set.of("String"),
								Set.of("String"),
								Set.of("String"),
								Set.of("String")));

		ProjectConfFieldMapping projectConfFieldMapping = createProjectCongMapping();
		boolean result = metaDataClientImpl.processMetadata(projectConfFieldMapping);
		Assert.assertEquals(false, result);
	}

	@Test
	public void testProcessMetadataWithValueInStateCategory() {
		when(azureAdapter.getIssueType()).thenReturn(createIssueType());
		when(azureAdapter.getField()).thenReturn(createField());
		when(azureAdapter.getStatus()).thenReturn(createStatus());
		when(fieldMappingRepository.save(any())).thenReturn(new FieldMapping());

		when(metadataIdentifierRepository.findByToolAndIsKanban(anyString(), anyBoolean()))
				.thenReturn(
						new MetadataIdentifier(
								"tool",
								"templateName",
								"templateCode",
								Boolean.TRUE,
								true,
								new ArrayList<>(),
								new ArrayList<>(),
								new ArrayList<>(),
								new ArrayList<>(),
								new ArrayList<>()));
		when(azureStateCategoryRepository.findByBasicProjectConfigId(anyString())).thenReturn(null);

		ProjectConfFieldMapping projectConfFieldMapping = createProjectCongMapping();
		boolean result = metaDataClientImpl.processMetadata(projectConfFieldMapping);
		Assert.assertEquals(false, result);
	}

	@Test
	public void testProcessMetadataWithValueInStateCategoryNoFieldMapping() {
		when(azureAdapter.getIssueType()).thenReturn(createIssueType());
		when(azureAdapter.getField()).thenReturn(createField());
		when(azureAdapter.getStatus()).thenReturn(createStatus());
		when(fieldMappingRepository.save(any())).thenReturn(new FieldMapping());

		List<Identifier> identifier = createIdentifier();
		when(metadataIdentifierRepository.findByToolAndIsKanban(anyString(), anyBoolean()))
				.thenReturn(
						new MetadataIdentifier(
								"tool",
								"templateName",
								"templateCode",
								Boolean.TRUE,
								true,
								identifier,
								identifier,
								identifier,
								identifier,
								identifier));
		when(azureStateCategoryRepository.findByBasicProjectConfigId(anyString())).thenReturn(null);

		ProjectConfFieldMapping projectConfFieldMapping = createProjectCongMapping();
		projectConfFieldMapping.setFieldMapping(null);
		boolean result = metaDataClientImpl.processMetadata(projectConfFieldMapping);
		Assert.assertEquals(true, result);
	}

	@Test
	public void testProcessMetadataWithValueInStateCategoryNoFieldMappingKanban() {
		when(azureAdapter.getIssueType()).thenReturn(createIssueType());
		when(azureAdapter.getField()).thenReturn(createField());
		when(azureAdapter.getStatus()).thenReturn(createStatus());
		when(fieldMappingRepository.save(any())).thenReturn(new FieldMapping());

		List<Identifier> identifier = createIdentifier();
		when(metadataIdentifierRepository.findByToolAndIsKanban(anyString(), anyBoolean()))
				.thenReturn(
						new MetadataIdentifier(
								"tool",
								"templateName",
								"templateCode",
								Boolean.TRUE,
								true,
								identifier,
								identifier,
								identifier,
								identifier,
								identifier));
		when(azureStateCategoryRepository.findByBasicProjectConfigId(anyString())).thenReturn(null);

		ProjectConfFieldMapping projectConfFieldMapping = createProjectCongMapping();
		projectConfFieldMapping.setFieldMapping(null);
		projectConfFieldMapping.setKanban(true);
		boolean result = metaDataClientImpl.processMetadata(projectConfFieldMapping);
		Assert.assertEquals(true, result);
	}

	private List<Status> createStatus() {
		List<Status> statusList = new ArrayList<>();
		Status field = new Status(null, 0L, "New", AzureConstants.PROPOSED, null);
		Status field3 = new Status(null, 0L, "Open", AzureConstants.PROPOSED, null);
		Status field2 = new Status(null, 0L, "Done", AzureConstants.COMPLETED, null);
		Status field4 = new Status(null, 0L, "Dropped", AzureConstants.REMOVED, null);
		Status field5 = new Status(null, 0L, "Active", AzureConstants.INPROGRESS, null);
		Status field6 = new Status(null, 0L, "Completed", AzureConstants.RESOLVED, null);
		statusList.add(field);
		statusList.add(field2);
		statusList.add(field3);
		statusList.add(field4);
		statusList.add(field5);
		statusList.add(field6);
		return statusList;
	}

	private List<IssueType> createIssueType() {
		List<IssueType> issueTypeList = new ArrayList<>();
		IssueType issueType = new IssueType(URI.create("abc.com"), 0L, "Bug Category", false, "Bug Category", null);
		issueTypeList.add(issueType);
		return issueTypeList;
	}

	@Test
	public void testProcessAndSaveStateCategory() throws Exception {
		when(azureStateCategoryRepository.findByBasicProjectConfigId(anyString()))
				.thenReturn(
						new AzureStateCategory(
								"basicProjectConfigId",
								Set.of("String"),
								Set.of("String"),
								Set.of("String"),
								Set.of("String"),
								Set.of("String")));
	}

	private List<Field> createField() {
		List<Field> fieldList = new ArrayList<>();
		Field field = new Field("abc", "Acceptance Criteria", FieldType.CUSTOM, true, true, true,
				new FieldSchema("test", "test", "test", "test", 1L));
		fieldList.add(field);
		return fieldList;
	}

	private ProjectConfFieldMapping createProjectCongMapping() {
		AzureToolConfig config = new AzureToolConfig();
		Connection conn = new Connection();
		conn.setOffline(Boolean.TRUE);
		conn.setBaseUrl("https://test.com/test/testProject");
		config.setBasicProjectConfigId("5b674d58f47cae8935b1b26f");
		config.setConnection(conn);
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("5b674d58f47cae8935b1b26f"));
		projectConfFieldMapping.setFieldMapping(new FieldMapping());
		projectConfFieldMapping.setAzure(config);
		return projectConfFieldMapping;
	}

	List<Identifier> createIdentifier() {
		List<Identifier> identifiers = new ArrayList<>();
		List<String> types = Arrays.asList(CommonConstant.STORY, CommonConstant.BUG, CommonConstant.EPIC,
				CommonConstant.ISSUE_TYPE, CommonConstant.UAT_DEFECT, CommonConstant.TICKET_VELOCITY_ISSUE_TYPE,
				CommonConstant.TICKET_WIP_CLOSED_ISSUE_TYPE, CommonConstant.TICKET_THROUGHPUT_ISSUE_TYPE,
				CommonConstant.KANBAN_CYCLE_TIME_ISSUE_TYPE, CommonConstant.TICKET_REOPEN_ISSUE_TYPE,
				CommonConstant.KANBAN_TECH_DEBT_ISSUE_TYPE, CommonConstant.DOR, CommonConstant.DOD, CommonConstant.DEVELOPMENT,
				CommonConstant.QA, CommonConstant.FIRST_STATUS, CommonConstant.REJECTION, CommonConstant.DELIVERED,
				CommonConstant.TICKET_CLOSED_STATUS, CommonConstant.TICKET_RESOLVED_STATUS,
				CommonConstant.TICKET_TRIAGED_STATUS, CommonConstant.TICKET_WIP_STATUS, CommonConstant.TICKET_REJECTED_STATUS);

		for (String type : types) {
			Identifier identifier = new Identifier();
			identifier.setType(type);
			identifier.setValue(Arrays.asList("Bug", "Defect", "Story"));
			identifiers.add(identifier);
		}
		return identifiers;
	}
}
