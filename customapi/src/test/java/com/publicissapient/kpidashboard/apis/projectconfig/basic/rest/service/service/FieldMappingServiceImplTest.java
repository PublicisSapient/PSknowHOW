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

package com.publicissapient.kpidashboard.apis.projectconfig.basic.rest.service.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.projectconfig.fieldmapping.service.FieldMappingServiceImpl;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

/**
 * @author anisingh4
 */
@RunWith(MockitoJUnitRunner.class)
public class FieldMappingServiceImplTest {

	@InjectMocks
	private FieldMappingServiceImpl fieldMappingService;

	@Mock
	private FieldMappingRepository fieldMappingRepository;

	@Mock
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Mock
	private ProjectBasicConfigRepository projectBasicConfigRepository;

	@Mock
	private ConfigHelperService configHelperService;

	@Mock
	private CacheService cacheService;

	@Mock
	private TokenAuthenticationService tokenAuthenticationService;

	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Test
	public void getFieldMappingSuccess() {
		FieldMapping fieldMapping = createFieldMappingScrum();
		Map<ObjectId, FieldMapping> map =  new HashMap<>();
		map.put(new ObjectId("5d0533b0ff45ea9c730bb718"),fieldMapping);
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setBasicProjectConfigId(new ObjectId("5d0533b0ff45ea9c730bb718"));
		Optional<ProjectToolConfig> projectToolConfigOpt = Optional.of(projectToolConfig);

		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("5d0533b0ff45ea9c730bb718"));
		Optional<ProjectBasicConfig> projectBasicConfigOpt = Optional.of(projectBasicConfig);

		Set<String> configIds = new HashSet<>();
		configIds.add("5d0533b0ff45ea9c730bb718");
		when(fieldMappingRepository.findByProjectToolConfigId(Mockito.any(ObjectId.class))).thenReturn(fieldMapping);
		when(projectToolConfigRepository.findById("5d0533b0ff45ea9c730bb718")).thenReturn(projectToolConfig);
		when(projectBasicConfigRepository.findById(Mockito.any())).thenReturn(projectBasicConfigOpt);
		when(tokenAuthenticationService.getUserProjects()).thenReturn(configIds);

		FieldMapping result = fieldMappingService.getFieldMapping("5d0533b0ff45ea9c730bb718");

		assertNotNull(result);

	}

	@Test(expected = IllegalArgumentException.class)
	public void getFieldMappingException() {
		FieldMapping fieldMapping = createFieldMappingScrum();
		FieldMapping result = fieldMappingService.getFieldMapping("abc123");

	}

	@Test
	public void addFieldMappingSuccess() {
		FieldMapping fieldMapping = createFieldMappingScrum();
		when(fieldMappingRepository.findByProjectToolConfigId(Mockito.any(ObjectId.class))).thenReturn(null);
		when(fieldMappingRepository.save(Mockito.any(FieldMapping.class))).thenReturn(fieldMapping);

		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", fieldMapping);

		assertNotNull(result);

	}

	/**
	 * fields are updated
	 */
	@Test
	public void addFieldMappingSuccess2() {
		mockRepositoriesForScrum();

		FieldMapping fieldMapping1 = createFieldMappingScrum();
		fieldMapping1.setRootCause("abc");
		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", fieldMapping1);

		assertNotNull(result);

	}

	/**
	 * fields are updated
	 */
	@Test
	public void addFieldMappingSuccess3() {
		mockRepositoriesForScrum();

		FieldMapping fieldMapping1 = createFieldMappingScrum();
		fieldMapping1.setEpicJobSize("8");
		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", fieldMapping1);

		assertNotNull(result);

	}

	/**
	 * fields are updated
	 */
	@Test
	public void addFieldMappingSuccess4() {
		mockRepositoriesForScrum();

		FieldMapping fieldMapping1 = createFieldMappingScrum();
		fieldMapping1.setRootCause(null);
		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", fieldMapping1);

		assertNotNull(result);

	}

	/**
	 * fields are updated
	 */
	@Test
	public void addFieldMappingSuccess5() {
		mockRepositoriesForScrum();

		FieldMapping fieldMapping1 = createFieldMappingScrum();
		fieldMapping1.setJiraIssueTypeNames(new String[] { "Story", "Feature" });

		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", fieldMapping1);

		assertNotNull(result);

	}

	/**
	 * fields are updated
	 */
	@Test
	public void addFieldMappingSuccess6() {
		mockRepositoriesForScrum();

		FieldMapping fieldMapping1 = createFieldMappingScrum();
		fieldMapping1.setJiraTechDebtIdentification(CommonConstant.CUSTOM_FIELD);
		fieldMapping1.setJiraTechDebtValue(Arrays.asList("Story", "Feature"));

		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", fieldMapping1);

		assertNotNull(result);

	}

	/**
	 * fields are updated
	 */
	@Test
	public void addFieldMappingSuccess7() {
		mockRepositoriesForScrum();

		FieldMapping fieldMapping1 = createFieldMappingScrum();
		fieldMapping1.setJiraTechDebtIdentification(CommonConstant.CUSTOM_FIELD);
		fieldMapping1.setJiraTechDebtValue(Arrays.asList("Feature"));

		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", fieldMapping1);

		assertNotNull(result);

	}

	/**
	 * fields are updated
	 */
	@Test
	public void addFieldMappingSuccess8() {
		mockRepositoriesForScrum();

		FieldMapping fieldMapping1 = createFieldMappingScrum();
		fieldMapping1.setJiraTechDebtIdentification(CommonConstant.CUSTOM_FIELD);
		fieldMapping1.setJiraTechDebtValue(Arrays.asList("Story"));

		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", fieldMapping1);

		assertNotNull(result);

	}

	/**
	 * fields are updated
	 */
	@Test
	public void addFieldMappingSuccess9() {
		mockRepositoriesForKanban();

		FieldMapping fieldMapping1 = createFieldMappingScrum();
		fieldMapping1.setJiraTechDebtIdentification(CommonConstant.CUSTOM_FIELD);
		fieldMapping1.setJiraTechDebtValue(Arrays.asList("Feature"));

		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", fieldMapping1);

		assertNotNull(result);

	}

	/**
	 * fields are updated
	 */
	@Test
	public void addFieldMappingSuccess10() {
		mockRepositoriesForKanban();

		FieldMapping fieldMapping1 = createFieldMappingScrum();

		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", fieldMapping1);

		assertNotNull(result);

	}

	@Test
	public void addFieldMappingScrum() {
		FieldMapping fieldMapping = createFieldMappingScrum();
		mockRepositoriesForScrum();

		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", fieldMapping);
		assertNotNull(result);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addFieldMappingValueNull() {
		fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void addFieldMappingInvalidId() {
		fieldMappingService.addFieldMapping("5d0533b0ff40bb618", null);

	}

	@Test
	public void deleteByBasicProjectConfigId() {
		doNothing().when(fieldMappingRepository).deleteByBasicProjectConfigId(Mockito.any(ObjectId.class));
		fieldMappingService.deleteByBasicProjectConfigId(new ObjectId("601a75729638120001b90891"));
		verify(fieldMappingRepository, times(1)).deleteByBasicProjectConfigId(new ObjectId("601a75729638120001b90891"));
	}

	private void mockRepositoriesForScrum() {
		FieldMapping fieldMapping = createFieldMappingScrum();
		when(fieldMappingRepository.findByProjectToolConfigId(Mockito.any(ObjectId.class))).thenReturn(fieldMapping);
		when(projectBasicConfigRepository.findById(Mockito.any(ObjectId.class)))
				.thenReturn(createProjectBasicConfig(false));
		when(fieldMappingRepository.save(Mockito.any(FieldMapping.class))).thenReturn(fieldMapping);
		when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigIdIn(Mockito.any(String.class),
				any())).thenReturn(Arrays.asList(createProcessorExecutionTraceLog()));
	}

	private void mockRepositoriesForKanban() {
		FieldMapping fieldMapping = createFieldMappingScrum();
		when(fieldMappingRepository.findByProjectToolConfigId(Mockito.any(ObjectId.class))).thenReturn(fieldMapping);
		when(projectBasicConfigRepository.findById(Mockito.any(ObjectId.class)))
				.thenReturn(createProjectBasicConfig(true));
		when(fieldMappingRepository.save(Mockito.any(FieldMapping.class))).thenReturn(fieldMapping);
		when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigIdIn(Mockito.any(String.class),
				any())).thenReturn(Collections.emptyList());
	}

	private FieldMapping createFieldMappingScrum() {
		FieldMapping fieldMapping = new FieldMapping();

		fieldMapping.setBasicProjectConfigId(new ObjectId());
		// issueType
		fieldMapping.setJiraIssueTypeNames(new String[] { "Story", "Enabler Story" });
		fieldMapping.setJiraIssueTypeKPI35(Arrays.asList("Story"));
		fieldMapping.setJiraQAKPI111IssueType(Arrays.asList("Story"));
		fieldMapping.setJiraDefectCountlIssueTypeKPI36(Arrays.asList("Story"));
		fieldMapping.setJiraDefectInjectionIssueTypeKPI14(Arrays.asList("Story"));
		fieldMapping.setJiraTestAutomationIssueType(Arrays.asList("Story"));
		fieldMapping.setJiraIssueTypeKPI3(Arrays.asList("Story", "Defect"));
		fieldMapping.setJiraTechDebtIssueType(Arrays.asList("Story"));
		fieldMapping.setJiraStoryIdentification(Arrays.asList("Story"));
		fieldMapping.setJiraSprintCapacityIssueTypeKpi46(Arrays.asList("Story"));
		// workflow
		fieldMapping.setJiraDefectCreatedStatusKPI14("Open");
		fieldMapping.setStoryFirstStatus("Open");
		fieldMapping.setJiraLiveStatus("Closed");
		fieldMapping.setJiraDorKPI171(Arrays.asList("In Analysis"));
		fieldMapping.setJiraDefectRejectionStatusKPI133("Closed");
		fieldMapping.setJiraDodKPI171(Arrays.asList("Ready for Sign-Off"));
		fieldMapping.setJiraIssueDeliverdStatusKPI82(Arrays.asList("Closed", "Ready for Delivery"));
		fieldMapping.setJiraDefectRemovalStatusKPI34(Arrays.asList("Closed"));
		fieldMapping.setResolutionTypeForRejectionKPI135(
				Arrays.asList("Duplicate", "Cannot Reproduce", "Invalid", "Declined", "Dropped"));
		fieldMapping.setJiraStatusForDevelopmentAVR(Arrays.asList("In Development"));
		fieldMapping.setJiraStatusForDevelopmentKPI82(Arrays.asList("In Development"));
		fieldMapping.setJiraStatusForDevelopmentKPI135(Arrays.asList("In Development"));
		fieldMapping.setJiraStatusForQaKPI148(Arrays.asList("In Testing"));
		// customField
		fieldMapping.setSprintName("customfield_12700");
		fieldMapping.setJiraStoryPointsCustomField("customfield_20803");
		fieldMapping.setRootCause("customfield_19121");

		fieldMapping.setJiraBugRaisedByIdentification(CommonConstant.CUSTOM_FIELD);
		fieldMapping.setJiraBugRaisedByQAIdentification(CommonConstant.CUSTOM_FIELD);
		fieldMapping.setJiraProductionIncidentIdentification(CommonConstant.CUSTOM_FIELD);
		// techDebt
		fieldMapping.setJiraTechDebtIdentification(CommonConstant.CUSTOM_FIELD);
		fieldMapping.setJiraTechDebtValue(Arrays.asList("Story"));
		fieldMapping.setJiraTechDebtCustomField(fieldMapping.getJiraTechDebtCustomField());
		// defect
		fieldMapping.setRootCauseValue(fieldMapping.getRootCauseValue());

		return fieldMapping;
	}

	private FieldMapping createFieldMappingKanban() {
		FieldMapping fieldMapping = new FieldMapping();
		// issueType
		fieldMapping.setJiraIssueTypeNames(new String[] { "Story", "Defect" });
		fieldMapping.setTicketCountIssueType(Arrays.asList("Story"));
		fieldMapping.setTicketCountIssueType(Arrays.asList("Story"));
		fieldMapping.setKanbanCycleTimeIssueType(Arrays.asList("Story"));
		fieldMapping.setKanbanJiraTechDebtIssueType(Arrays.asList("Story"));

		// workflow
		fieldMapping.setTicketDeliverdStatus(Arrays.asList("READY FOR UAT"));
		fieldMapping.setTicketReopenStatus(Arrays.asList("Open"));
		fieldMapping.setJiraTicketTriagedStatus(Arrays.asList(""));
		fieldMapping.setJiraTicketResolvedStatus(Arrays.asList("Closed"));
		fieldMapping.setJiraTicketWipStatus(Arrays.asList("In Development"));
		fieldMapping.setJiraTicketRejectedStatus(Arrays.asList("Rejected"));
		fieldMapping.setJiraTicketClosedStatus(Arrays.asList("Closed"));
		fieldMapping.setStoryFirstStatus("Open");
		// customField
		fieldMapping.setJiraStoryPointsCustomField(fieldMapping.getJiraStoryPointsCustomField());
		fieldMapping.setRootCause(fieldMapping.getRootCause());

		// techDebt
		fieldMapping.setJiraTechDebtValue(Arrays.asList("Story"));
		fieldMapping.setJiraTechDebtCustomField("");
		// defect
		fieldMapping.setJiradefecttype(Arrays.asList("Defect"));
		fieldMapping.setJiraBugRaisedByCustomField("customfield_15001");
		fieldMapping.setJiraBugRaisedByValue(Arrays.asList("UAT_Defect"));
		fieldMapping.setJiraBugRaisedByQACustomField("");
		fieldMapping.setJiraBugRaisedByQAValue(Arrays.asList(""));

		return fieldMapping;
	}

	private ProjectToolConfig createProjectToolConfig() {
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setId(new ObjectId("5fa2a16ec5a84726287d528a"));
		projectToolConfig.setBasicProjectConfigId(new ObjectId("5fa29069c5a8470e24667c36"));
		projectToolConfig.setProjectKey("XYZ");
		projectToolConfig.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));

		return projectToolConfig;
	}

	private Optional<ProjectBasicConfig> createProjectBasicConfig(boolean isKanban) {
		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("5fa29069c5a8470e24667c36"));
		projectBasicConfig.setIsKanban(isKanban);
		Optional<ProjectBasicConfig> projectBasicConfigOpt = Optional.of(projectBasicConfig);
		return projectBasicConfigOpt;
	}

	private ProcessorExecutionTraceLog createProcessorExecutionTraceLog() {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setId(new ObjectId("5fa29069c5a8470e24667c36"));
		return processorExecutionTraceLog;
	}

	private Optional<ProjectToolConfig> createProjectToolConfigOpt() {
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setMetadataTemplateCode("9");
		Optional<ProjectToolConfig> projectToolConfigOpt = Optional.of(projectToolConfig);
		return projectToolConfigOpt;
	}

}