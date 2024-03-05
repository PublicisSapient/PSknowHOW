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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.projectconfig.fieldmapping.service.FieldMappingServiceImpl;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.BaseFieldMappingStructure;
import com.publicissapient.kpidashboard.common.model.application.ConfigurationHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.FieldMappingResponse;
import com.publicissapient.kpidashboard.common.model.application.FieldMappingStructure;
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
	private KpiHelperService kpiHelperService;

	@Mock
	private CacheService cacheService;

	@Mock
	private TokenAuthenticationService tokenAuthenticationService;

	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;

	@Mock
	private MongoTemplate operations;

	@Mock
	private AuthenticationService authenticationService;

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;
	FieldMappingDataFactory fieldMappingDataFactory;
	private FieldMapping scrumFieldMapping;
	private FieldMapping scrumFieldMapping2;
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();

	@Before
	public void setUp() {
		fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		scrumFieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		List<ConfigurationHistoryChangeLog> configurationHistoryChangeLogList = new ArrayList<>();
		configurationHistoryChangeLogList.add(
				new ConfigurationHistoryChangeLog("", "customField", "currentUser", LocalDateTime.now().toString()));
		scrumFieldMapping.setHistorysprintName(configurationHistoryChangeLogList);
		ConfigurationHistoryChangeLog configurationHistoryChangeLog = new ConfigurationHistoryChangeLog();
		configurationHistoryChangeLog.setChangedTo("Customfield");
		configurationHistoryChangeLog.setChangedFrom("");
		configurationHistoryChangeLog.setChangedBy("currentUser");
		configurationHistoryChangeLog.setUpdatedOn(java.time.LocalDateTime.now().toString());
		scrumFieldMapping.setHistoryrootCauseIdentifier(Arrays.asList(configurationHistoryChangeLog));
		scrumFieldMapping2 = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(scrumFieldMapping.getBasicProjectConfigId(), scrumFieldMapping);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

	}

	@Test
	public void getFieldMappingSuccess() {
		FieldMapping fieldMapping = scrumFieldMapping;
		ProjectToolConfig projectToolConfig = createProjectToolConfig(fieldMapping.getBasicProjectConfigId());
		Optional<ProjectBasicConfig> projectBasicConfigOpt = createProjectBasicConfig(false,
				fieldMapping.getBasicProjectConfigId());

		Set<String> configIds = new HashSet<>();
		configIds.add(fieldMapping.getBasicProjectConfigId().toString());
		when(fieldMappingRepository.findByProjectToolConfigId(Mockito.any(ObjectId.class))).thenReturn(fieldMapping);
		when(projectToolConfigRepository.findById(anyString())).thenReturn(projectToolConfig);
		when(projectBasicConfigRepository.findById(Mockito.any())).thenReturn(projectBasicConfigOpt);
		when(tokenAuthenticationService.getUserProjects()).thenReturn(configIds);

		FieldMapping result = fieldMappingService.getFieldMapping(projectToolConfig.getId().toString());

		assertNotNull(result);

	}

	@Test(expected = IllegalArgumentException.class)
	public void getFieldMappingException() {
		FieldMapping fieldMapping = scrumFieldMapping;
		FieldMapping result = fieldMappingService.getFieldMapping("abc123");

	}

	@Test
	public void addFieldMappingSuccess() {
		FieldMapping fieldMapping = scrumFieldMapping;
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
		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618",
				fieldMappingDataFactory.getFieldMappings().get(1));
		assertNotNull(result);
	}

	@Test
	public void addFieldMappingSuccess_History() {
		mockRepositoriesForScrum();
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(1);
		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", fieldMapping);
		assertNotNull(result);
	}

	/**
	 * fields are updated
	 */
	@Test
	public void addFieldMappingSuccess3() {
		mockRepositoriesForScrum();

		FieldMapping fieldMapping1 = scrumFieldMapping;
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

		FieldMapping fieldMapping1 = scrumFieldMapping;
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

		FieldMapping fieldMapping1 = scrumFieldMapping;
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

		FieldMapping fieldMapping1 = scrumFieldMapping;
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

		FieldMapping fieldMapping1 = scrumFieldMapping;
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

		FieldMapping fieldMapping1 = scrumFieldMapping;
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

		FieldMapping fieldMapping1 = scrumFieldMapping;
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

		FieldMapping fieldMapping1 = scrumFieldMapping;

		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", fieldMapping1);

		assertNotNull(result);

	}

	@Test
	public void addFieldMappingScrum() {
		FieldMapping fieldMapping = scrumFieldMapping;
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

	@Test
	public void getBasicConfigId() {
		when(projectBasicConfigRepository.findById(Mockito.any(ObjectId.class)))
				.thenReturn(Optional.of(new ProjectBasicConfig()));
		fieldMappingService.getBasicProjectConfigById(new ObjectId("601a75729638120001b90891"));
		verify(projectBasicConfigRepository, times(1)).findById(new ObjectId("601a75729638120001b90891"));
	}

	@Test
	public void getKpiSpecificFieldsAndNullHistory() throws NoSuchFieldException, IllegalAccessException {
		ProjectToolConfig projectToolConfig = createProjectToolConfig(scrumFieldMapping.getBasicProjectConfigId());
		Optional<ProjectBasicConfig> projectBasicConfigOpt = createProjectBasicConfig(false,
				scrumFieldMapping.getBasicProjectConfigId());
		Set<String> configIds = new HashSet<>();
		configIds.add(scrumFieldMapping.getBasicProjectConfigId().toString());
		when(projectToolConfigRepository.findById(anyString())).thenReturn(projectToolConfig);
		when(projectBasicConfigRepository.findById(Mockito.any())).thenReturn(projectBasicConfigOpt);
		when(tokenAuthenticationService.getUserProjects()).thenReturn(configIds);

		when(fieldMappingRepository.findByProjectToolConfigId(any(ObjectId.class))).thenReturn(this.scrumFieldMapping);
		List<FieldMappingResponse> fieldMappingResponses = fieldMappingService.getKpiSpecificFieldsAndHistory(
				KPICode.getKPI("kpi36"),
				createProjectToolConfigOpt(scrumFieldMapping.getBasicProjectConfigId()).get().getId().toString());
		assertNotNull(fieldMappingResponses);
		Map<String, Object> collect = fieldMappingResponses.stream()
				.filter(response -> Objects.nonNull(response.getOriginalValue()))
				.collect(Collectors.toMap(FieldMappingResponse::getFieldName, FieldMappingResponse::getOriginalValue));
		List<String> resolutionTypeForRejectionRCAKPI36 = (List<String>) collect
				.get("resolutionTypeForRejectionRCAKPI36");
		assertEquals(scrumFieldMapping.getResolutionTypeForRejectionRCAKPI36(), resolutionTypeForRejectionRCAKPI36);
	}

	@Test
	public void getKpiSpecificFieldsAndWithHistory() throws NoSuchFieldException, IllegalAccessException {
		ConfigurationHistoryChangeLog log = new ConfigurationHistoryChangeLog();
		log.setUpdatedOn(LocalDateTime.now().toString());
		log.setChangedTo(scrumFieldMapping2.getResolutionTypeForRejectionRCAKPI36());
		log.setChangedFrom(Arrays.asList("1", "2"));
		log.setChangedBy("User");
		scrumFieldMapping2.setHistoryresolutionTypeForRejectionRCAKPI36(Collections.singletonList(log));
		ProjectToolConfig projectToolConfig = createProjectToolConfig(scrumFieldMapping2.getBasicProjectConfigId());
		Optional<ProjectBasicConfig> projectBasicConfigOpt = createProjectBasicConfig(false,
				scrumFieldMapping2.getBasicProjectConfigId());
		Set<String> configIds = new HashSet<>();
		configIds.add(scrumFieldMapping2.getBasicProjectConfigId().toString());
		when(projectToolConfigRepository.findById(anyString())).thenReturn(projectToolConfig);
		when(projectBasicConfigRepository.findById(Mockito.any())).thenReturn(projectBasicConfigOpt);
		when(tokenAuthenticationService.getUserProjects()).thenReturn(configIds);
		when(fieldMappingRepository.findByProjectToolConfigId(any(ObjectId.class))).thenReturn(this.scrumFieldMapping2);
		List<FieldMappingResponse> fieldMappingResponses = fieldMappingService.getKpiSpecificFieldsAndHistory(
				KPICode.getKPI("kpi36"),
				createProjectToolConfigOpt(scrumFieldMapping.getBasicProjectConfigId()).get().getId().toString());
		assertNotNull(fieldMappingResponses);
		Map<String, Object> collect = fieldMappingResponses.stream().filter(
				response -> Objects.nonNull(response.getOriginalValue()) && Objects.nonNull(response.getHistory()))
				.collect(Collectors.toMap(FieldMappingResponse::getFieldName, FieldMappingResponse::getHistory));
		List<ConfigurationHistoryChangeLog> resolutionTypeForRejectionRCAKPI36 = (List<ConfigurationHistoryChangeLog>) collect
				.get("resolutionTypeForRejectionRCAKPI36");
		assertEquals(scrumFieldMapping2.getResolutionTypeForRejectionRCAKPI36(),
				resolutionTypeForRejectionRCAKPI36.get(0).getChangedTo());
	}

	@Test
	public void updateKpiField() throws NoSuchFieldException, IllegalAccessException {
		when(configHelperService.loadFieldMappingStructure()).thenReturn(Arrays.asList(new FieldMappingStructure()));
		when(kpiHelperService.getFieldMappingStructure(anyList(), anyList()))
				.thenReturn(Arrays.asList(new FieldMappingStructure()));

		Optional<ProjectBasicConfig> projectBasicConfigOpt = createProjectBasicConfig(false,
				scrumFieldMapping.getBasicProjectConfigId());
		Map<String, ProjectBasicConfig> mapOfProjectDetails = new HashMap<>();
		mapOfProjectDetails.put(scrumFieldMapping.getBasicProjectConfigId().toString(), projectBasicConfigOpt.get());
		when(cacheService.cacheProjectConfigMapData()).thenReturn(mapOfProjectDetails);

		when(authenticationService.getLoggedInUser()).thenReturn("currentUser");

		ProjectToolConfig projectToolConfig = createProjectToolConfig(scrumFieldMapping.getBasicProjectConfigId());
		FieldMappingResponse response = new FieldMappingResponse();
		response.setFieldName("resolutionTypeForRejectionRCAKPI36");
		response.setOriginalValue(Arrays.asList("1", "2"));
		response.setPreviousValue(Arrays.asList("1"));

		fieldMappingService.updateSpecificFieldsAndHistory(KPICode.getKPI("kpi36"), projectToolConfig,
				Arrays.asList(response));
	}

	@Test
	public void updateKpiFieldRemoveTraceLog() throws NoSuchFieldException, IllegalAccessException {
		FieldMappingStructure fieldMappingStructure = new FieldMappingStructure();
		fieldMappingStructure.setFieldName("resolutionTypeForRejectionRCAKPI36");
		fieldMappingStructure.setProcessorCommon(true);
		when(configHelperService.loadFieldMappingStructure()).thenReturn(Arrays.asList(fieldMappingStructure));
		when(kpiHelperService.getFieldMappingStructure(anyList(), anyList()))
				.thenReturn(Arrays.asList(fieldMappingStructure));

		Optional<ProjectBasicConfig> projectBasicConfigOpt = createProjectBasicConfig(false,
				scrumFieldMapping.getBasicProjectConfigId());
		Map<String, ProjectBasicConfig> mapOfProjectDetails = new HashMap<>();
		mapOfProjectDetails.put(scrumFieldMapping.getBasicProjectConfigId().toString(), projectBasicConfigOpt.get());
		when(cacheService.cacheProjectConfigMapData()).thenReturn(mapOfProjectDetails);

		when(authenticationService.getLoggedInUser()).thenReturn("currentUser");

		ProjectToolConfig projectToolConfig = createProjectToolConfig(scrumFieldMapping.getBasicProjectConfigId());
		FieldMappingResponse response = new FieldMappingResponse();
		response.setFieldName("resolutionTypeForRejectionRCAKPI36");
		response.setOriginalValue(Arrays.asList("1", "2"));
		response.setPreviousValue(Arrays.asList("1"));

		fieldMappingService.updateSpecificFieldsAndHistory(KPICode.getKPI("kpi36"), projectToolConfig,
				Arrays.asList(response));
	}

	@Test
	public void updateKpiCustomField() throws NoSuchFieldException, IllegalAccessException {
		FieldMappingStructure fieldMappingStructure = new FieldMappingStructure();
		fieldMappingStructure.setFieldName("rootCauseIdentifier");
		BaseFieldMappingStructure baseFieldMappingStructure = new BaseFieldMappingStructure();
		baseFieldMappingStructure.setFieldName("rootCause");
		baseFieldMappingStructure.setFilterGroup(Arrays.asList("CustomField"));
		fieldMappingStructure.setNestedFields(Arrays.asList(baseFieldMappingStructure));
		fieldMappingStructure.setProcessorCommon(true);
		when(configHelperService.loadFieldMappingStructure()).thenReturn(Arrays.asList(fieldMappingStructure));
		when(kpiHelperService.getFieldMappingStructure(anyList(), anyList()))
				.thenReturn(Arrays.asList(fieldMappingStructure));

		Optional<ProjectBasicConfig> projectBasicConfigOpt = createProjectBasicConfig(false,
				scrumFieldMapping.getBasicProjectConfigId());
		Map<String, ProjectBasicConfig> mapOfProjectDetails = new HashMap<>();
		mapOfProjectDetails.put(scrumFieldMapping.getBasicProjectConfigId().toString(), projectBasicConfigOpt.get());
		when(cacheService.cacheProjectConfigMapData()).thenReturn(mapOfProjectDetails);

		ProjectToolConfig projectToolConfig = createProjectToolConfig(scrumFieldMapping.getBasicProjectConfigId());
		Set<String> configIds = new HashSet<>();
		configIds.add(scrumFieldMapping.getBasicProjectConfigId().toString());

		when(authenticationService.getLoggedInUser()).thenReturn("currentUser");

		// ProjectToolConfig projectToolConfig =
		// createProjectToolConfig(scrumFieldMapping.getBasicProjectConfigId());
		FieldMappingResponse response = new FieldMappingResponse();
		response.setFieldName("rootCauseIdentifier");
		response.setOriginalValue("CustomField");
		response.setPreviousValue("");

		FieldMappingResponse response2 = new FieldMappingResponse();
		response2.setFieldName("rootCause");
		response2.setOriginalValue("CustomField_123");
		response2.setPreviousValue("");

		fieldMappingService.updateSpecificFieldsAndHistory(KPICode.getKPI("kpi0"), projectToolConfig,
				Arrays.asList(response, response2));
	}

	@Test
	public void updateKpiFieldAzure() throws NoSuchFieldException, IllegalAccessException {
		FieldMappingStructure fieldMappingStructure = new FieldMappingStructure();
		fieldMappingStructure.setFieldName("jiraIterationCompletionStatusCustomField");
		fieldMappingStructure.setProcessorCommon(true);
		when(configHelperService.loadFieldMappingStructure()).thenReturn(Arrays.asList(fieldMappingStructure));
		when(kpiHelperService.getFieldMappingStructure(anyList(), anyList()))
				.thenReturn(Arrays.asList(fieldMappingStructure));

		Optional<ProjectBasicConfig> projectBasicConfigOpt = createProjectBasicConfig(false,
				scrumFieldMapping.getBasicProjectConfigId());
		projectBasicConfigOpt.get();
		Map<String, ProjectBasicConfig> mapOfProjectDetails = new HashMap<>();
		mapOfProjectDetails.put(scrumFieldMapping.getBasicProjectConfigId().toString(), projectBasicConfigOpt.get());
		when(cacheService.cacheProjectConfigMapData()).thenReturn(mapOfProjectDetails);

		when(authenticationService.getLoggedInUser()).thenReturn("currentUser");

		ProjectToolConfig projectToolConfig = createProjectToolConfig(scrumFieldMapping.getBasicProjectConfigId());
		projectToolConfig.setToolName(ProcessorConstants.AZURE);
		FieldMappingResponse response = new FieldMappingResponse();
		response.setFieldName("jiraIterationCompletionStatusCustomField");
		response.setOriginalValue(Arrays.asList("1", "2"));
		response.setPreviousValue(Arrays.asList("1"));

		fieldMappingService.updateSpecificFieldsAndHistory(KPICode.getKPI("kpi0"), projectToolConfig,
				Arrays.asList(response));
	}

	@Test
	public void addFieldMappingForAzure() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping scrumFieldMapping2 = fieldMappingDataFactory.getFieldMappings().get(0);
		scrumFieldMapping2.setJiraIterationCompletionStatusCustomField(Arrays.asList("1", "2", "3"));
		when(fieldMappingRepository.findByProjectToolConfigId(Mockito.any(ObjectId.class)))
				.thenReturn(scrumFieldMapping);
		doReturn(createProjectBasicConfig(false, scrumFieldMapping2.getBasicProjectConfigId()))
				.when(projectBasicConfigRepository).findById(Mockito.any(ObjectId.class));
		when(fieldMappingRepository.save(Mockito.any(FieldMapping.class))).thenReturn(scrumFieldMapping);

		List<ProcessorExecutionTraceLog> tracelogList = new ArrayList<>();
		tracelogList.add(createProcessorExecutionTraceLog());

		doReturn(tracelogList).when(processorExecutionTraceLogRepository)
				.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), anyList());

		Optional<ProjectToolConfig> projectToolConfigOpt = createProjectToolConfigOpt(
				scrumFieldMapping.getBasicProjectConfigId());
		projectToolConfigOpt.get().setToolName(ProcessorConstants.AZURE);
		when(projectToolConfigRepository.findById(any(ObjectId.class))).thenReturn(projectToolConfigOpt);
		FieldMapping result = fieldMappingService.addFieldMapping("5d0533b0ff45ea9c730bb618", scrumFieldMapping2);

		assertNotNull(result);

	}

	private void mockRepositoriesForScrum() {
		when(fieldMappingRepository.findByProjectToolConfigId(Mockito.any(ObjectId.class)))
				.thenReturn(scrumFieldMapping);
		when(projectBasicConfigRepository.findById(Mockito.any(ObjectId.class)))
				.thenReturn(createProjectBasicConfig(false, scrumFieldMapping.getBasicProjectConfigId()));
		when(fieldMappingRepository.save(Mockito.any(FieldMapping.class))).thenReturn(scrumFieldMapping);
		when(processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigIdIn(Mockito.any(String.class), any()))
						.thenReturn(Arrays.asList(createProcessorExecutionTraceLog()));
		when(authenticationService.getLoggedInUser()).thenReturn("currentUser");
	}

	private void mockRepositoriesForKanban() {
		FieldMapping fieldMapping = scrumFieldMapping;
		when(fieldMappingRepository.findByProjectToolConfigId(Mockito.any(ObjectId.class))).thenReturn(fieldMapping);
		when(projectBasicConfigRepository.findById(Mockito.any(ObjectId.class)))
				.thenReturn(createProjectBasicConfig(true, fieldMapping.getBasicProjectConfigId()));
		when(fieldMappingRepository.save(Mockito.any(FieldMapping.class))).thenReturn(fieldMapping);
		when(processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigIdIn(Mockito.any(String.class), any()))
						.thenReturn(Collections.emptyList());
	}

	private ProjectToolConfig createProjectToolConfig(ObjectId basicProjectConfigId) {
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setId(new ObjectId("5fa2a16ec5a84726287d528a"));
		projectToolConfig.setBasicProjectConfigId(basicProjectConfigId);
		projectToolConfig.setToolName(ProcessorConstants.JIRA);
		projectToolConfig.setMetadataTemplateCode("1");
		projectToolConfig.setProjectKey("XYZ");
		projectToolConfig.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));

		return projectToolConfig;
	}

	private Optional<ProjectBasicConfig> createProjectBasicConfig(boolean isKanban, ObjectId projectId) {
		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(projectId);
		projectBasicConfig.setIsKanban(isKanban);
		Optional<ProjectBasicConfig> projectBasicConfigOpt = Optional.of(projectBasicConfig);
		return projectBasicConfigOpt;
	}

	private ProcessorExecutionTraceLog createProcessorExecutionTraceLog() {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setId(new ObjectId("5fa29069c5a8470e24667c36"));
		return processorExecutionTraceLog;
	}

	private Optional<ProjectToolConfig> createProjectToolConfigOpt(ObjectId basicProjectConfigId) {
		ProjectToolConfig projectToolConfig = createProjectToolConfig(basicProjectConfigId);
		projectToolConfig.setMetadataTemplateCode("9");
		return Optional.of(projectToolConfig);
	}

}