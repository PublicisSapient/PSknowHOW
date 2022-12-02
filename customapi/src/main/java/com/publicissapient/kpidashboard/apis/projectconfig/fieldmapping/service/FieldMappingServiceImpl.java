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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anisingh4
 */
@Service
@Slf4j
public class FieldMappingServiceImpl implements FieldMappingService {

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
			throw new IllegalArgumentException("Invalid projectToolConfigId");
		}
		if (!authorizedProjectsService.ifSuperAdminUser() && !hasProjectAccess(projectToolConfigId)) {
			throw new AccessDeniedException("Access is denied");
		}

		return fieldMappingRepository.findByProjectToolConfigId(new ObjectId(projectToolConfigId));
	}

	@Override
	public FieldMapping addFieldMapping(String projectToolConfigId, FieldMapping fieldMapping) {

		if (!ObjectId.isValid(projectToolConfigId)) {
			throw new IllegalArgumentException("Invalid projectToolConfigId");
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
	public boolean hasProjectAccess(String projectToolConfigId) {
		Optional<ProjectBasicConfig> projectBasicConfig ;
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
		ProjectBasicConfig projectBasicConfigObj=null;
		if (null != basicProjectConfigId) {
			projectBasicConfig = projectBasicConfigRepository.findById(basicProjectConfigId);
		}
		if(projectBasicConfig.isPresent()) {
			projectBasicConfigObj=projectBasicConfig.get();
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
	 * @return true or false
	 */
	private boolean isMappingUpdated(FieldMapping unsaved, FieldMapping saved) {
		boolean isUpdated;

		List<String> fieldNameList = Arrays.asList("jiradefecttype", "sprintName", "jiraStoryPointsCustomField",
				"rootCause", "jiraIssueTypeNames", "storyFirstStatus", "epicCostOfDelay", "epicRiskReduction",
				"epicUserBusinessValue", "epicWsjf", "epicTimeCriticality", "epicJobSize" , "additionalFilterConfig");

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
			List<String> productionDefectFieldList = Arrays.asList("productionDefectCustomField", "productionDefectValue");
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
	 * @return true or false
	 */
	private boolean isKanbanMappingUpdated(FieldMapping unsaved, FieldMapping saved) {
		boolean isUpdated;

		List<String> fieldNameList = Arrays.asList("jiraStoryPointsCustomField", "rootCause", "jiraIssueTypeNames",
				"storyFirstStatus");

		isUpdated = checkFieldsForUpdation(unsaved, saved, fieldNameList);

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
		Optional<ProjectBasicConfig> projectBasicConfigOpt = projectBasicConfigRepository.findById(basicProjectConfigId);
		if(projectBasicConfigOpt.isPresent()) {
			ProjectBasicConfig projectBasicConfig=projectBasicConfigOpt.get();
			if ((!projectBasicConfig.getIsKanban() && isMappingUpdated(fieldMapping, existingFieldMapping)) ||
					(projectBasicConfig.getIsKanban() && isKanbanMappingUpdated(fieldMapping, existingFieldMapping))) {
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
			}
		}
	}

	/**
	 * Return true if zephyr tool is configured.
	 * 
	 * @param fieldMapping
	 * @return
	 */

}
