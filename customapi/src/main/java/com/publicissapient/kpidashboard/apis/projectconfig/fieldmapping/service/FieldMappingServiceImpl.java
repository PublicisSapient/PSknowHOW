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

package com.publicissapient.kpidashboard.apis.projectconfig.fieldmapping.service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@Service
@Slf4j
public class FieldMappingServiceImpl implements FieldMappingService {

	public static final String INVALID_PROJECT_TOOL_CONFIG_ID = "Invalid projectToolConfigId";
	public static final String JIRA_STORY_POINTS_CUSTOM_FIELD = "jiraStoryPointsCustomField";
	public static final String ROOT_CAUSE = "rootCause";
	public static final String JIRA_ISSUE_TYPE_NAMES = "jiraIssueTypeNames";
	public static final String STORY_FIRST_STATUS = "storyFirstStatus";
	public static final String EPIC_COST_OF_DELAY = "epicCostOfDelay";
	public static final String EPIC_RISK_REDUCTION = "epicRiskReduction";
	public static final String EPIC_USER_BUSINESS_VALUE = "epicUserBusinessValue";
	public static final String EPIC_WSJF = "epicWsjf";
	public static final String EPIC_TIME_CRITICALITY = "epicTimeCriticality";
	public static final String EPIC_JOB_SIZE = "epicJobSize";
	public static final String READY_FOR_DEVELOPMENT_STATUS = "readyForDevelopmentStatus";
	@Autowired
	private FieldMappingRepository fieldMappingRepository;

	@Autowired
	private ProjectToolConfigRepository toolConfigRepository;

	@Autowired
	private ProjectBasicConfigRepository projectBasicConfigRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;

	@Autowired
	private UserAuthorizedProjectsService authorizedProjectsService;

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Autowired
	private KanbanJiraIssueRepository kanbanJiraIssueRepository;

	@Override
	public FieldMapping getFieldMapping(String projectToolConfigId) {
		if (!ObjectId.isValid(projectToolConfigId)) {
			throw new IllegalArgumentException(INVALID_PROJECT_TOOL_CONFIG_ID);
		}
		if (!authorizedProjectsService.ifSuperAdminUser() && !hasProjectAccess(projectToolConfigId)) {
			throw new AccessDeniedException("Access is denied");
		}

		return fieldMappingRepository.findByProjectToolConfigId(new ObjectId(projectToolConfigId));
	}

	@Override
	public FieldMapping addFieldMapping(String projectToolConfigId, FieldMapping fieldMapping) {

		if (!ObjectId.isValid(projectToolConfigId)) {
			throw new IllegalArgumentException(INVALID_PROJECT_TOOL_CONFIG_ID);
		}

		if (fieldMapping == null) {
			throw new IllegalArgumentException("Field mapping can't be null");
		}

		fieldMapping.setProjectToolConfigId(new ObjectId(projectToolConfigId));

		FieldMapping existingFieldMapping = fieldMappingRepository
				.findByProjectToolConfigId(new ObjectId(projectToolConfigId));
		if (existingFieldMapping != null) {
			fieldMapping.setId(existingFieldMapping.getId());
			updateJiraData(existingFieldMapping.getBasicProjectConfigId(), fieldMapping, existingFieldMapping);
		}

		FieldMapping mapping = fieldMappingRepository.save(fieldMapping);
		cacheService.clearCache(CommonConstant.CACHE_FIELD_MAPPING_MAP);
		clearCache();
		return mapping;
	}

	@Override
	public boolean compareMappingOnSave(String projectToolConfigId, FieldMapping fieldMapping) {

		boolean mappingUpdated = false;

		if (!ObjectId.isValid(projectToolConfigId)) {
			throw new IllegalArgumentException(INVALID_PROJECT_TOOL_CONFIG_ID);
		}

		if (fieldMapping == null) {
			throw new IllegalArgumentException("Field mapping can't be null");
		}

		fieldMapping.setProjectToolConfigId(new ObjectId(projectToolConfigId));

		FieldMapping existingFieldMapping = fieldMappingRepository
				.findByProjectToolConfigId(new ObjectId(projectToolConfigId));

		if (existingFieldMapping != null) {
			fieldMapping.setId(existingFieldMapping.getId());
			mappingUpdated = compareJiraData(existingFieldMapping.getBasicProjectConfigId(), fieldMapping,
					existingFieldMapping);
		}

		return mappingUpdated;
	}

	@Override
	public boolean hasProjectAccess(String projectToolConfigId) {
		Optional<ProjectBasicConfig> projectBasicConfig;
		ProjectToolConfig projectToolConfig = toolConfigRepository.findById(projectToolConfigId);
		if (null != projectToolConfig && null != projectToolConfig.getBasicProjectConfigId()) {
			projectBasicConfig = projectBasicConfigRepository.findById(projectToolConfig.getBasicProjectConfigId());

			Set<String> configIds = tokenAuthenticationService.getUserProjects();
			if (projectBasicConfig.isPresent() && configIds.contains(projectBasicConfig.get().getId().toString())) {
				return true;
			}

		}
		return false;
	}

	@Override
	public void deleteByBasicProjectConfigId(ObjectId basicProjectConfigId) {
		log.info("deleting field mapping for {}" + basicProjectConfigId.toHexString());
		fieldMappingRepository.deleteByBasicProjectConfigId(basicProjectConfigId);
	}

	@Override
	public ProjectBasicConfig getBasicProjectConfigById(ObjectId basicProjectConfigId) {
		Optional<ProjectBasicConfig> projectBasicConfig = Optional.empty();
		ProjectBasicConfig projectBasicConfigObj = null;
		if (null != basicProjectConfigId) {
			projectBasicConfig = projectBasicConfigRepository.findById(basicProjectConfigId);
		}
		if (projectBasicConfig.isPresent()) {
			projectBasicConfigObj = projectBasicConfig.get();
		}
		return projectBasicConfigObj;
	}

	private void clearCache() {
		cacheService.clearCache(CommonConstant.JIRAKANBAN_KPI_CACHE);
		cacheService.clearCache(CommonConstant.JIRA_KPI_CACHE);
	}

	/**
	 * Checks if fields are updated.
	 *
	 * @param unsaved
	 *            object from request
	 * @param saved
	 *            object from database
	 * @param fieldNameList
	 * @return true or false
	 */
	private boolean isMappingUpdated(FieldMapping unsaved, FieldMapping saved, List<String> fieldNameList) {
		boolean isUpdated;

		isUpdated = checkFieldsForUpdation(unsaved, saved, fieldNameList);

		if (!isUpdated && CommonConstant.CUSTOM_FIELD.equalsIgnoreCase(unsaved.getJiraBugRaisedByIdentification())) {
			List<String> uatFieldList = Arrays.asList("jiraBugRaisedByCustomField", "jiraBugRaisedByValue");
			isUpdated = checkFieldsForUpdation(unsaved, saved, uatFieldList);
		}

		if (!isUpdated && CommonConstant.CUSTOM_FIELD.equalsIgnoreCase(unsaved.getJiraBugRaisedByQAIdentification())) {
			List<String> qaFieldList = Arrays.asList("jiraBugRaisedByQACustomField", "jiraBugRaisedByQAValue");
			isUpdated = checkFieldsForUpdation(unsaved, saved, qaFieldList);
		}

		if (!isUpdated && CommonConstant.CUSTOM_FIELD.equalsIgnoreCase(unsaved.getJiraTechDebtIdentification())) {
			List<String> tachDebtFieldList = Arrays.asList("jiraTechDebtCustomField", "jiraTechDebtValue");
			isUpdated = checkFieldsForUpdation(unsaved, saved, tachDebtFieldList);
		}

		if (!isUpdated && CommonConstant.CUSTOM_FIELD.equalsIgnoreCase(unsaved.getProductionDefectIdentifier())) {
			List<String> productionDefectFieldList = Arrays.asList("productionDefectCustomField",
					"productionDefectValue");
			isUpdated = checkFieldsForUpdation(unsaved, saved, productionDefectFieldList);
		}

		return isUpdated;
	}

	/**
	 * Checks if fields are updated.
	 *
	 * @param unsaved
	 *            object from request
	 * @param saved
	 *            object from database
	 * @param fieldNameListKanban
	 * @return true or false
	 */
	private boolean isKanbanMappingUpdated(FieldMapping unsaved, FieldMapping saved, List<String> fieldNameListKanban) {
		boolean isUpdated;

		isUpdated = checkFieldsForUpdation(unsaved, saved, fieldNameListKanban);

		if (!isUpdated && CommonConstant.CUSTOM_FIELD.equalsIgnoreCase(unsaved.getJiraTechDebtIdentification())) {
			List<String> tachDebtFieldList = Arrays.asList("jiraTechDebtCustomField", "jiraTechDebtValue");
			isUpdated = checkFieldsForUpdation(unsaved, saved, tachDebtFieldList);
		}

		return isUpdated;
	}

	/**
	 * @param unsaved
	 * @param saved
	 * @param fieldNameList
	 * @return
	 */
	private boolean checkFieldsForUpdation(FieldMapping unsaved, FieldMapping saved, List<String> fieldNameList) {
		boolean isUpdated = false;
		for (String fName : fieldNameList) {
			try {
				Field field = FieldMapping.class.getDeclaredField(fName);
				field.setAccessible(true); // NOSONAR

				isUpdated = isValueUpdated(field.get(unsaved), field.get(saved));

			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				isUpdated = false;
			}

			if (isUpdated) {
				break;
			}
		}
		return isUpdated;
	}

	/**
	 * compares field values for saved and unsaved data.
	 *
	 * @param value
	 *            unsaved value
	 * @param value1
	 *            existing value
	 * @return is value updated
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean isValueUpdated(Object value, Object value1) {
		if (value == null) {
			return value1 != null;

		} else {
			if (value instanceof List) {
				return !(value1 instanceof List && (((List) value).size() == ((List) value1).size())
						&& ((List) value).containsAll((List) value1));

			} else if (value instanceof String[]) {
				return !(value1 instanceof String[] && Arrays.equals((String[]) value, (String[]) value1));

			} else {
				return value1 == null || !value1.equals(value);

			}
		}
	}

	/**
	 * Checks if fields are updated and then unset changeDate in jira collections.
	 * 
	 * @param basicProjectConfigId
	 * @param fieldMapping
	 * @param existingFieldMapping
	 */
	private void updateJiraData(ObjectId basicProjectConfigId, FieldMapping fieldMapping,
			FieldMapping existingFieldMapping) {
		Optional<ProjectBasicConfig> projectBasicConfigOpt = projectBasicConfigRepository
				.findById(basicProjectConfigId);
		if (projectBasicConfigOpt.isPresent()) {
			ProjectBasicConfig projectBasicConfig = projectBasicConfigOpt.get();

			List<String> fieldNameList = Arrays.asList("jiradefecttype", "sprintName", JIRA_STORY_POINTS_CUSTOM_FIELD,
					ROOT_CAUSE, JIRA_ISSUE_TYPE_NAMES, STORY_FIRST_STATUS, EPIC_COST_OF_DELAY, EPIC_RISK_REDUCTION,
					EPIC_USER_BUSINESS_VALUE, EPIC_WSJF, EPIC_TIME_CRITICALITY, EPIC_JOB_SIZE,
					READY_FOR_DEVELOPMENT_STATUS, "additionalFilterConfig", "jiraDueDateField",
					"jiraDueDateCustomField");

			List<String> fieldNameListKanban = Arrays.asList(JIRA_STORY_POINTS_CUSTOM_FIELD, ROOT_CAUSE,
					JIRA_ISSUE_TYPE_NAMES, STORY_FIRST_STATUS);

			Optional<ProjectToolConfig> projectToolConfigOpt = toolConfigRepository
					.findById(fieldMapping.getProjectToolConfigId());
			azureSprintReportStatusUpdateBasedOnFieldChange(fieldMapping, existingFieldMapping, projectBasicConfig,
					projectToolConfigOpt);

			if ((!projectBasicConfig.getIsKanban()
					&& isMappingUpdated(fieldMapping, existingFieldMapping, fieldNameList))
					|| (projectBasicConfig.getIsKanban()
							&& isKanbanMappingUpdated(fieldMapping, existingFieldMapping, fieldNameListKanban))) {
				Optional<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogRepository
						.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.JIRA,
								basicProjectConfigId.toHexString());
				ProcessorExecutionTraceLog processorExecutionTraceLog = null;

				if (traceLogs.isPresent()) {
					processorExecutionTraceLog = traceLogs.get();
					processorExecutionTraceLog.setLastSuccessfulRun(null);
					processorExecutionTraceLog.setLastSavedEntryUpdatedDateByType(new HashMap<>());
					processorExecutionTraceLogRepository.save(processorExecutionTraceLog);
				}

				saveTemplateCode(projectBasicConfig, projectToolConfigOpt);

			}
		}
	}

	/**
	 * if jiraIterationCompletionStatusCustomField field mapping changes then put
	 * identifier to change in sprint report issues based on status for azure board
	 *
	 * @param fieldMapping
	 * @param existingFieldMapping
	 * @param projectBasicConfig
	 * @param projectToolConfigOpt
	 */
	private void azureSprintReportStatusUpdateBasedOnFieldChange(FieldMapping fieldMapping,
			FieldMapping existingFieldMapping, ProjectBasicConfig projectBasicConfig,
			Optional<ProjectToolConfig> projectToolConfigOpt) {
		List<String> azureIterationStatusFieldList = Arrays.asList("jiraIterationCompletionStatusCustomField");
		if (projectToolConfigOpt.isPresent()
				&& projectToolConfigOpt.get().getToolName().equals(ProcessorConstants.AZURE)
				&& !projectBasicConfig.getIsKanban()
				&& isMappingUpdated(fieldMapping, existingFieldMapping, azureIterationStatusFieldList)) {
			ProjectToolConfig projectToolConfig = projectToolConfigOpt.get();
			projectToolConfig.setAzureIterationStatusFieldUpdate(true);
			toolConfigRepository.save(projectToolConfig);
		}
	}

	private void saveTemplateCode(ProjectBasicConfig projectBasicConfig,
			Optional<ProjectToolConfig> projectToolConfigOpt) {
		if (projectToolConfigOpt.isPresent()) {
			ProjectToolConfig projectToolConfig = projectToolConfigOpt.get();
			if (projectBasicConfig.getIsKanban()) {
				projectToolConfig.setMetadataTemplateCode("9");
			} else {
				projectToolConfig.setMetadataTemplateCode("10");
			}

			toolConfigRepository.save(projectToolConfig);
		}
	}

	private boolean compareJiraData(ObjectId basicProjectConfigId, FieldMapping fieldMapping,
			FieldMapping existingFieldMapping) {
		boolean isUpdated = false;
		Optional<ProjectBasicConfig> projectBasicConfigOpt = projectBasicConfigRepository
				.findById(basicProjectConfigId);
		if (projectBasicConfigOpt.isPresent()) {
			ProjectBasicConfig projectBasicConfig = projectBasicConfigOpt.get();

//			List<String> fieldNameList = Arrays.asList("jiradefecttype", "sprintName", JIRA_STORY_POINTS_CUSTOM_FIELD,
//					ROOT_CAUSE, JIRA_ISSUE_TYPE_NAMES, STORY_FIRST_STATUS, "jiraDefectCreatedStatus",
//					EPIC_COST_OF_DELAY, EPIC_RISK_REDUCTION, "issueStatusExcluMissingWork", "jiraIssueDeliverdStatusBR","jiraIssueDeliverdStatusSV","jiraIssueDeliverdStatusAVR","jiraIssueDeliverdStatusCVR","jiraIssueDeliverdStatusFTPR",
//					"jiraDod", "jiraDefectRemovalStatusDRE", "jiraDefectDroppedStatus", "jiraStatusForDevelopment",
//					"jiraStatusForQa", "jiraOnHoldStatus", "jiraBlockedStatus", "jiraWaitStatus",
//					"jiraStatusForInProgress", "jiraDevDoneStatus", "jiraDefectSeepageIssueType",
//					"jiraQADefectDensityIssueType", "jiraDefectCountlIssueType", "jiraSprintVelocityIssueTypeSV","jiraSprintVelocityIssueTypeBR",
//					"jiraDefectRemovalIssueTypeDRE", "jiraDefectRejectionlIssueTypeDRR", "jiraDefectInjectionIssueType",
//					"jiraTestAutomationIssueType", "jiraIntakeToDorIssueType", "jiraStoryIdentification","jiraStoryIdentificationIC",
//					"jiraFTPRStoryIdentification", "jiraSprintCapacityIssueType", "jiraIssueEpicType", "defectPriority",
//					"excludeRCAFromFTPR", "workingHoursDayCPT", "jiraDevDueDateCustomField", EPIC_USER_BUSINESS_VALUE,
//					EPIC_WSJF, "jiraDor", "resolutionTypeForRejectionAVR","resolutionTypeForRejectionDC","resolutionTypeForRejectionDRE","resolutionTypeForRejectionDRR","resolutionTypeForRejectionDSR","resolutionTypeForRejectionFTPR","resolutionTypeForRejectionIFTPR","resolutionTypeForRejectionQS","resolutionTypeForRejectionRCA","resolutionTypeForRejectionDIR","resolutionTypeForRejectionQADD", "jiraDefectRejectionStatusAVR","jiraDefectRejectionStatusDC","jiraDefectRejectionStatusDRE","jiraDefectRejectionStatusDRR","jiraDefectRejectionStatusDSR","jiraDefectRejectionStatusFTPR","jiraDefectRejectionStatusIFTPR","jiraDefectRejectionStatusQS","jiraDefectRejectionStatusRCA","jiraDefectRejectionStatusDIR","jiraDefectRejectionStatusQADD",
//					EPIC_TIME_CRITICALITY, "jiraLiveStatus", EPIC_JOB_SIZE, "additionalFilterConfig",
//					"jiraDueDateField", "jiraDueDateCustomField", "jiraDefectClosedStatus", "jiraRejectedInRefinement",
//					"jiraAcceptedInRefinement", "jiraReadyForRefinement", "jiraIterationCompletionStatusCustomField",
//					"jiraIterationCompletionTypeCustomField", "jiraFtprRejectStatus");

			List<String> fieldNameList = Arrays.asList(
			"jiradefecttype",
			"jiradefecttypeSWE",
			"jiradefecttypeKPI132",
			"jiradefecttypeKPI136",
			"jiradefecttypeKPI140",
			"jiradefecttypeRDCA",
			"jiradefecttypeRDCP",
			"jiradefecttypeRDCR",
			"jiradefecttypeRDCS",
			"jiradefecttypeKPI133",
			"jiradefecttypeIWS",
			"jiradefecttypeLT",
			"jiradefecttypeMW",
			"jiradefecttypeKPI82",
			"jiradefecttypeKPI135",
			"jiradefecttypeKpi40",
			"jiradefecttypeAVR",
			"jiradefecttypeKPI126",
			"jiradefecttypeBDRR",

			"defectPriorityKPI135",
			"defectPriorityKPI14",
			"defectPriorityQAKPI111",
			"defectPriorityKPI82",
			"defectPriorityKPI133",

			"jiraIssueTypeNamesAVR",
			"jiraIssueEpicType",

			"storyFirstStatusLT",
			"rootCause",

			"jiraStatusForDevelopmentAVR",
			"jiraStatusForDevelopmentKPI82",
			"jiraStatusForDevelopmentKPI135",
			"jiraStatusForQa",

			"jiraDefectInjectionIssueTypeKPI14",

			"jiraDodKPI14",
			"jiraDodQAKPI111",
			"jiraDodLT",
			"jiraDodPDA",

			"jiraDefectCreatedStatusKPI14",
			"jiraTechDebtIssueType",
			"jiraTechDebtIdentification",
			"jiraTechDebtCustomField",
			"jiraTechDebtValue",

			"jiraDefectRejectionStatusAVR",
			"jiraDefectRejectionStatusKPI28",
			"jiraDefectRejectionStatusKPI34",
			"jiraDefectRejectionStatusKPI37",
			"jiraDefectRejectionStatusKPI35",
			"jiraDefectRejectionStatusKPI82",
			"jiraDefectRejectionStatusKPI135",
			"jiraDefectRejectionStatusKPI133",
			"jiraDefectRejectionStatusRCAKPI36",
			"jiraDefectRejectionStatusKPI14",
			"jiraDefectRejectionStatusQAKPI111",
			"jiraBugRaisedByIdentification",
			"jiraBugRaisedByValue",

			"jiraIssueTypeKPI35",
			"jiraBugRaisedByCustomField",

			"jiraDefectRemovalStatusKPI34",

			"jiraDefectRemovalIssueTypeKPI34",
			"jiraDefectClosedStatus",
			"jiraStoryPointsCustomField",
			"jiraTestAutomationIssueType",

			"jiraSprintVelocityIssueTypeKpi39",
			"jiraSprintVelocityIssueTypeBR",

			"jiraSprintCapacityIssueTypeKpi46",

			"jiraIssueTypeKPI37",

			"jiraDefectCountlIssueTypeKPI28",
			"jiraDefectCountlIssueTypeKPI36",

			"jiraIssueDeliverdStatusBR",
			"jiraIssueDeliverdStatusKpi39",
			"jiraIssueDeliverdStatusAVR",
			"jiraIssueDeliverdStatusKPI126",
			"jiraIssueDeliverdStatusKPI82",
			"readyForDevelopmentStatus",

			"jiraDorLT",

			"jiraIssueTypeLT",
			"jiraStoryIdentification",
			"jiraStoryIdentificationKpi40",

			"jiraLiveStatusLT",
			"jiraLiveStatusLTK",
			"jiraLiveStatusNOPK",
			"jiraLiveStatusNOSK",
			"jiraLiveStatusNORK",
			"jiraLiveStatusOTA",
			"jiraLiveStatusPDA",

			"ticketReopenStatus",

			"jiraTicketResolvedStatus",

			"jiraTicketWipStatus",

			"jiraStatusMappingCustomField",
			"rootCauseValue",

			"excludeRCAFromKPI82",
			"excludeRCAFromKPI135",
			"excludeRCAFromKPI14",
			"excludeRCAFromQAKPI111",
			"excludeRCAFromKPI133",
			"jiraDorToLiveIssueType",
			"jiraProductiveStatus",
			"jiraCommitmentReliabilityIssueType",

			"resolutionTypeForRejectionAVR",
			"resolutionTypeForRejectionKPI28",
			"resolutionTypeForRejectionKPI34",
			"resolutionTypeForRejectionKPI37",
			"resolutionTypeForRejectionKPI35",
			"resolutionTypeForRejectionKPI82",
			"resolutionTypeForRejectionKPI135",
			"resolutionTypeForRejectionKPI133",
			"resolutionTypeForRejectionRCAKPI36",
			"resolutionTypeForRejectionKPI14",
			"resolutionTypeForRejectionQAKPI111",

			"jiraQAKPI111IssueType",
			"jiraBugRaisedByQACustomField",
			"jiraBugRaisedByQAIdentification",
			"jiraBugRaisedByQAValue",
			"jiraDefectDroppedStatus",
			"epicCostOfDelay",
			"epicRiskReduction",
			"epicUserBusinessValue",
			"epicWsjf",
			"epicTimeCriticality",
			"epicJobSize",
			"squadIdentifier",
			"squadIdentMultiValue",
			"squadIdentSingleValue",
			"productionDefectCustomField",
			"productionDefectIdentifier",
			"productionDefectValue",
			"productionDefectComponentValue",

			"jiraStatusForInProgressKPI122",
			"jiraStatusForInProgressKPI145",
			"jiraStatusForInProgressKPI125",
			"jiraStatusForInProgressKPI128",
			"jiraStatusForInProgressKPI123",
			"jiraStatusForInProgressKPI119",
			"estimationCriteria",

			"workingHoursDayCPT",
			"additionalFilterConfig",

			"issueStatusExcluMissingWorkKPI124",
			"jiraOnHoldStatus",

			"jiraKPI82StoryIdentification",
			"jiraKPI135StoryIdentification",

			"jiraWaitStatusKPI131",

			"jiraBlockedStatusKPI131",

			"jiraIncludeBlockedStatusKPI131",
			"jiraDueDateField",
			"jiraDueDateCustomField",
			"jiraDevDueDateCustomField",

			"jiraDevDoneStatusKPI119",
			"jiraDevDoneStatusKPI145",
			"jiraDevDoneStatusKPI128",
			"jiraRejectedInRefinement",
			"jiraAcceptedInRefinement",
			"jiraReadyForRefinement",
			"jiraFtprRejectStatus",
			"jiraIterationCompletionStatusCustomField",
			"jiraIterationCompletionStatusKPI135",
			"jiraIterationCompletionStatusKPI122",
			"jiraIterationCompletionStatusKPI75",
			"jiraIterationCompletionStatusKPI145",
			"jiraIterationCompletionStatusKPI140",
			"jiraIterationCompletionStatusKPI132",
			"jiraIterationCompletionStatusKPI136",
			"jiraIterationCompletionStatusKpi40",
			"jiraIterationCompletionStatusKpi72",
			"jiraIterationCompletionStatusKpi39",
			"jiraIterationCompletionStatusKpi5",
			"jiraIterationCompletionStatusKPI124",
			"jiraIterationCompletionStatusKPI123",
			"jiraIterationCompletionStatusKPI125",
			"jiraIterationCompletionStatusKPI120",
			"jiraIterationCompletionStatusKPI128",
			"jiraIterationCompletionStatusKPI134",
			"jiraIterationCompletionStatusKPI133",
			"jiraIterationCompletionStatusKPI119",
			"jiraIterationCompletionStatusKPI131",
			"jiraIterationCompletionStatusBRE",

			"jiraIterationIssuetypeCustomField",
			"jiraIterationIssuetypeKPI122",
			"jiraIterationIssuetypeBRE",
			"jiraIterationIssuetypeKPI131",
			"jiraIterationIssuetypeKPI128",
			"jiraIterationIssuetypeKPI134",
			"jiraIterationIssuetypeKPI145",
			"jiraIterationIssuetypeKpi72",
			"jiraIterationIssuetypeKPI119",
			"jiraIterationIssuetypeKpi5",
			"jiraIterationIssuetypeKPI75",
			"jiraIterationIssuetypeKPI123",
			"jiraIterationIssuetypeKPI125",
			"jiraIterationIssuetypeKPI120",
			"jiraIterationIssuetypeKPI124");

			List<String> fieldNameListKanban = Arrays.asList(JIRA_STORY_POINTS_CUSTOM_FIELD, ROOT_CAUSE,
					JIRA_ISSUE_TYPE_NAMES, STORY_FIRST_STATUS, "ticketDeliverdStatus", "jiraTicketTriagedStatus",
					"jiraTicketRejectedStatus", "jiraTicketClosedStatus", "jiraLiveStatus", "ticketCountIssueType",
					"kanbanRCACountIssueType", "jiraTicketVelocityIssueType", "kanbanCycleTimeIssueType",
					"storyPointToHourMapping", EPIC_COST_OF_DELAY, EPIC_RISK_REDUCTION, "jiraIssueEpicType",
					EPIC_USER_BUSINESS_VALUE, EPIC_WSJF, EPIC_JOB_SIZE, EPIC_TIME_CRITICALITY);

			if ((!projectBasicConfig.getIsKanban()
					&& isMappingUpdated(fieldMapping, existingFieldMapping, fieldNameList))
					|| (projectBasicConfig.getIsKanban()
							&& isKanbanMappingUpdated(fieldMapping, existingFieldMapping, fieldNameListKanban))) {

				isUpdated = true;
			}
		}
		return isUpdated;
	}

	/**
	 * Return true if zephyr tool is configured.
	 * 
	 * @param fieldMapping
	 * @return
	 */

}
