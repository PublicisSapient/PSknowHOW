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

package com.publicissapient.kpidashboard.jira.client.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.MetadataType;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;
import com.publicissapient.kpidashboard.common.model.jira.Identifier;
import com.publicissapient.kpidashboard.common.model.jira.Metadata;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;
import com.publicissapient.kpidashboard.common.model.jira.MetadataValue;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * The type Release data client. Store Release data for the projects in
 * persistence store
 */
@Slf4j
public class MetaDataClientImpl implements MetadataClient {

	private final JiraAdapter jiraAdapter;
	private final BoardMetadataRepository boardMetadataRepository;
	private final FieldMappingRepository fieldMappingRepository;
	private final MetadataIdentifierRepository metadataIdentifierRepository;

	/**
	 * Creates object
	 * 
	 * @param jiraAdapter
	 *            Jira Adapter instance
	 * @param boardMetadataRepository
	 *            project metadata repository
	 * @param fieldMappingRepository
	 *            fieldMappingRepository
	 * @param metadataIdentifierRepository
	 *            metadataIdentifierRepository
	 */
	public MetaDataClientImpl(JiraAdapter jiraAdapter, BoardMetadataRepository boardMetadataRepository,
			FieldMappingRepository fieldMappingRepository, MetadataIdentifierRepository metadataIdentifierRepository) {

		this.boardMetadataRepository = boardMetadataRepository;
		this.jiraAdapter = jiraAdapter;
		this.fieldMappingRepository = fieldMappingRepository;
		this.metadataIdentifierRepository = metadataIdentifierRepository;
	}

	@Override
	@Transactional
	public boolean processMetadata(ProjectConfFieldMapping projectConfig) {
		boolean isSuccess = false;
		log.info("Fetching metadata. Project name : {}", projectConfig.getProjectName());
		List<Field> fieldList = jiraAdapter.getField();
		List<IssueType> issueTypeList = jiraAdapter.getIssueType();
		List<Status> statusList = jiraAdapter.getStatus();
		if (CollectionUtils.isNotEmpty(fieldList) && CollectionUtils.isNotEmpty(issueTypeList)
				&& CollectionUtils.isNotEmpty(statusList)) {

			BoardMetadata boardMetadata = new BoardMetadata();
			boardMetadata.setProjectBasicConfigId(projectConfig.getBasicProjectConfigId());
			boardMetadata.setProjectToolConfigId(projectConfig.getProjectToolConfig().getId());
			List<Metadata> fullMetaDataList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(fieldList)) {
				fullMetaDataList.addAll(mapFields(fieldList, MetadataType.FIELDS.type()));
			}
			if (CollectionUtils.isNotEmpty(issueTypeList)) {
				fullMetaDataList.addAll(mapIssueTypes(issueTypeList, MetadataType.ISSUETYPE.type()));
			}
			if (CollectionUtils.isNotEmpty(statusList)) {
				fullMetaDataList.addAll(mapWorkFlow(statusList, MetadataType.WORKFLOW.type()));
			}
			boardMetadata.setMetadata(fullMetaDataList);
			if (null == projectConfig.getFieldMapping()) {
				FieldMapping fieldMapping = mapFieldMapping(boardMetadata, projectConfig);
				log.info("Saving fieldmapping into db for Project : {}", projectConfig.getProjectName());
				fieldMappingRepository.save(fieldMapping);
				projectConfig.setFieldMapping(fieldMapping);
				isSuccess = true;
			}

			log.info("Saving metadata into db for Project : {}", projectConfig.getProjectName());
			boardMetadataRepository.save(boardMetadata);

		}
		return isSuccess;
	}

	/**
	 * Map fields.
	 *
	 * @param fieldList
	 *            the field list
	 * @param type
	 */
	private List<Metadata> mapFields(List<Field> fieldList, String type) {
		List<Metadata> metadataList = new ArrayList<>();
		List<MetadataValue> fieldValue = new ArrayList<>();
		fieldList.forEach(field -> {
			MetadataValue metaDataValue = new MetadataValue();
			metaDataValue.setKey(field.getName());
			metaDataValue.setData(field.getId());
			fieldValue.add(metaDataValue);
		});
		Metadata fieldMetadata = new Metadata();
		fieldMetadata.setType(type);
		fieldMetadata.setValue(fieldValue);
		metadataList.add(fieldMetadata);

		return metadataList;
	}

	/**
	 * Map Issue Types
	 * 
	 * @param issueTypeList
	 *            issueType List
	 * @param type
	 *            Type
	 * @return List of metadata
	 */
	private List<Metadata> mapIssueTypes(List<IssueType> issueTypeList, String type) {
		List<Metadata> metadataList = new ArrayList<>();
		List<MetadataValue> issueyTypeValue = new ArrayList<>();
		issueTypeList.forEach(issueType -> {
			MetadataValue metaDataValue = new MetadataValue();
			metaDataValue.setKey(issueType.getName());
			metaDataValue.setData(issueType.getName());
			issueyTypeValue.add(metaDataValue);
		});
		Metadata fieldMetadata = new Metadata();
		fieldMetadata.setType(type);
		fieldMetadata.setValue(issueyTypeValue);
		metadataList.add(fieldMetadata);
		return metadataList;
	}

	/**
	 * Map of workflows
	 * 
	 * @param statusList
	 * @param type
	 * @return List of workdlow metadata
	 */
	private List<Metadata> mapWorkFlow(List<Status> statusList, String type) {
		List<Metadata> metadataList = new ArrayList<>();
		List<MetadataValue> statusValue = new ArrayList<>();
		statusList.forEach(status -> {
			MetadataValue metaDataValue = new MetadataValue();
			metaDataValue.setKey(status.getName());
			metaDataValue.setData(status.getName());
			statusValue.add(metaDataValue);
		});
		Metadata fieldMetadata = new Metadata();
		fieldMetadata.setType(type);
		fieldMetadata.setValue(statusValue);
		metadataList.add(fieldMetadata);
		return metadataList;
	}

	/**
	 * Map field mapping.
	 *
	 * @param boardMetadata
	 *            the board metadata
	 * @return the field mapping
	 */
	private FieldMapping mapFieldMapping(BoardMetadata boardMetadata, ProjectConfFieldMapping projectConfig) {
		log.info("Fetching and comparing  metadata identifier");
		MetadataIdentifier metadataIdentifier = metadataIdentifierRepository.findByToolAndIsKanban(JiraConstants.JIRA,
				projectConfig.isKanban());
		List<Identifier> issueList = metadataIdentifier.getIssues();
		List<Identifier> customFieldList = metadataIdentifier.getCustomfield();
		Map<String, List<String>> valuesToIdentifyMap = metadataIdentifier.getValuestoidentify().stream()
				.collect(Collectors.toMap(Identifier::getType, Identifier::getValue));
		List<Identifier> workflowList = metadataIdentifier.getWorkflow();

		List<Metadata> metadataList = boardMetadata.getMetadata();
		Set<String> allIssueTypes = new HashSet<>();
		Set<String> allWorkflow = new HashSet<>();
		Map<String, String> allCustomField = new HashMap<>();

		for (Metadata metadata : metadataList) {
			if (metadata.getType().equals(CommonConstant.META_ISSUE_TYPE)) {
				allIssueTypes = metadata.getValue().stream().map(MetadataValue::getData).collect(Collectors.toSet());
			} else if (metadata.getType().equals(CommonConstant.META_WORKFLOW)) {
				allWorkflow = metadata.getValue().stream().map(MetadataValue::getData).collect(Collectors.toSet());
			} else if (metadata.getType().equals(CommonConstant.META_FIELD)) {
				metadata.getValue().stream().forEach(mv -> allCustomField.put(mv.getKey(), mv.getData()));
			}
		}
		Map<String, List<String>> issueTypeMap = compareIssueType(issueList, allIssueTypes);
		Map<String, List<String>> workflowMap = compareWorkflow(workflowList, allWorkflow);
		Map<String, String> customField = compareCustomField(customFieldList, allCustomField);

		return mapFieldMapping(issueTypeMap, workflowMap, customField, valuesToIdentifyMap, projectConfig);

	}

	private FieldMapping mapFieldMapping(Map<String, List<String>> issueTypeMap, Map<String, List<String>> workflowMap,
			Map<String, String> customField, Map<String, List<String>> valuesToIdentifyMap,
			ProjectConfFieldMapping projectConfig) {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
		fieldMapping.setProjectToolConfigId(projectConfig.getJiraToolConfigId());
		fieldMapping.setProjectToolConfigId(projectConfig.getProjectToolConfig().getId());
		fieldMapping.setSprintName(customField.get(CommonConstant.SPRINT));
		fieldMapping.setJiradefecttype(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiraIssueTypeNames(issueTypeMap.get(CommonConstant.ISSUE_TYPE).stream().toArray(String[]::new));
		fieldMapping.setJiraIssueEpicType(issueTypeMap.get(CommonConstant.EPIC).stream().collect(Collectors.toList()));
		fieldMapping.setEpicCostOfDelay(customField.get(CommonConstant.COST_OF_DELAY));
		fieldMapping.setEpicJobSize(customField.get(CommonConstant.JOB_SIZE));
		fieldMapping.setEpicRiskReduction(customField.get(CommonConstant.RISK_REDUCTION));
		fieldMapping.setEpicTimeCriticality(customField.get(CommonConstant.TIME_CRITICALITY));
		fieldMapping.setEpicUserBusinessValue(customField.get(CommonConstant.USER_BUSINESS_VALUE));
		fieldMapping.setEpicWsjf(customField.get(CommonConstant.WSJF));

		List<String> firstStatusList = workflowMap.get(CommonConstant.FIRST_STATUS);

		if (CollectionUtils.isNotEmpty(firstStatusList)) {
			fieldMapping.setStoryFirstStatus(firstStatusList.get(0));
			fieldMapping.setJiraDefectCreatedStatus(firstStatusList.get(0));
		} else {
			fieldMapping.setStoryFirstStatus(CommonConstant.OPEN);
			fieldMapping.setJiraDefectCreatedStatus(CommonConstant.OPEN);
		}
		fieldMapping.setIssueStatusExcluMissingWork(firstStatusList);
		fieldMapping.setRootCause(customField.get(CommonConstant.ROOT_CAUSE));
		fieldMapping.setJiraStatusForDevelopment(workflowMap.get(CommonConstant.DEVELOPMENT));
		fieldMapping.setJiraStatusForQa(workflowMap.get(CommonConstant.QA));
		fieldMapping.setJiraDefectInjectionIssueType(issueTypeMap.get(CommonConstant.STORY));
		if (CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.DOR))) {
			fieldMapping.setJiraDor(workflowMap.get(CommonConstant.DOR).get(0));
		} else {
			fieldMapping.setJiraDor(null);
		}
		fieldMapping.setJiraDod(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraTechDebtIssueType(issueTypeMap.get(CommonConstant.STORY));
		List<String> rejectionList = workflowMap.get(CommonConstant.REJECTION);
		/*if (CollectionUtils.isNotEmpty(rejectionList)) {
			fieldMapping.setJiraDefectRejectionStatus(rejectionList.get(0));
		}*/

		fieldMapping.setJiraDefectSeepageIssueType(
				issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));

		fieldMapping.setJiraDefectRemovalStatus(
				workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
		fieldMapping.setJiraDefectRemovalIssueType(
				issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
		fieldMapping
				.setJiraStoryPointsCustomField(customField.getOrDefault(CommonConstant.STORYPOINT, StringUtils.EMPTY));
		fieldMapping.setJiraTestAutomationIssueType(
				issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
		fieldMapping.setJiraSprintVelocityIssueType(
				issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
		fieldMapping.setJiraSprintCapacityIssueType(
				issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
		fieldMapping.setJiraDefectRejectionlIssueType(
				issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
		fieldMapping
				.setJiraDefectCountlIssueType(issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
		fieldMapping
				.setJiraDefectCountlIssueType(issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
		fieldMapping.setJiraIssueDeliverdStatus(
				workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
		fieldMapping
				.setJiraIntakeToDorIssueType(issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
		fieldMapping
				.setJiraStoryIdentification(issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
		fieldMapping
				.setJiraFTPRStoryIdentification(issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
		fieldMapping.setRootCauseValue(valuesToIdentifyMap.get(CommonConstant.ROOT_CAUSE_VALUE));
		fieldMapping.setResolutionTypeForRejection(
				valuesToIdentifyMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
		fieldMapping.setQaRootCauseValue(
				valuesToIdentifyMap.getOrDefault(CommonConstant.QA_ROOT_CAUSE, new ArrayList<>()));
		fieldMapping.setJiraQADefectDensityIssueType(
				issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));

		if (projectConfig.isKanban()) {
			populateKanbanFieldMappingData(fieldMapping, workflowMap, issueTypeMap);
		}
		return fieldMapping;
	}

	private void populateKanbanFieldMappingData(FieldMapping fieldMapping, Map<String, List<String>> workflowMap,
			Map<String, List<String>> issueTypeMap) {
		fieldMapping
				.setTicketCountIssueType(issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
		fieldMapping.setKanbanRCACountIssueType(Arrays.asList(JiraConstants.ISSUE_TYPE_DEFECT));
		fieldMapping.setJiraTicketVelocityIssueType(
				issueTypeMap.getOrDefault(CommonConstant.TICKET_VELOCITY_ISSUE_TYPE, new ArrayList<>()));
		fieldMapping
				.setTicketDeliverdStatus(workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
		fieldMapping.setTicketReopenStatus(
				workflowMap.getOrDefault(CommonConstant.TICKET_REOPEN_STATUS, new ArrayList<>()));
		fieldMapping.setJiraTicketTriagedStatus(
				workflowMap.getOrDefault(CommonConstant.TICKET_TRIAGED_STATUS, new ArrayList<>()));
		fieldMapping.setJiraTicketClosedStatus(
				workflowMap.getOrDefault(CommonConstant.TICKET_CLOSED_STATUS, new ArrayList<>()));
		fieldMapping.setJiraTicketRejectedStatus(
				workflowMap.getOrDefault(CommonConstant.TICKET_REJECTED_STATUS, new ArrayList<>()));
		fieldMapping.setJiraTicketResolvedStatus(
				workflowMap.getOrDefault(CommonConstant.TICKET_RESOLVED_STATUS, new ArrayList<>()));
		fieldMapping.setJiraTicketWipStatus(
				workflowMap.getOrDefault(CommonConstant.TICKET_WIP_STATUS, new ArrayList<>()));
		fieldMapping.setKanbanCycleTimeIssueType(
				issueTypeMap.getOrDefault(CommonConstant.KANBAN_CYCLE_TIME_ISSUE_TYPE, new ArrayList<>()));
		fieldMapping.setKanbanJiraTechDebtIssueType(
				issueTypeMap.getOrDefault(CommonConstant.KANBAN_TECH_DEBT_ISSUE_TYPE, new ArrayList<>()));

	}

	private Map<String, List<String>> compareIssueType(List<Identifier> issueList, Set<String> allIssueTypes) {
		Map<String, List<String>> issueTypeMap = new HashMap<>();
		for (Identifier identifier : issueList) {
			if (identifier.getType().equals(CommonConstant.STORY)) {
				List<String> storyList = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.STORY, storyList);
			} else if (identifier.getType().equals(CommonConstant.BUG)) {
				List<String> bugList = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.BUG, bugList);
			} else if (identifier.getType().equals(CommonConstant.EPIC)) {
				List<String> epicList = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.EPIC, epicList);
			} else if (identifier.getType().equals(CommonConstant.ISSUE_TYPE)) {
				List<String> issuetypeList = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.ISSUE_TYPE, issuetypeList);
			} else if (identifier.getType().equals(CommonConstant.UAT_DEFECT)) {
				List<String> uatList = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.UAT_DEFECT, uatList);
			} else if (identifier.getType().equals(CommonConstant.TICKET_VELOCITY_ISSUE_TYPE)) {
				List<String> list = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.TICKET_VELOCITY_ISSUE_TYPE, list);
			} else if (identifier.getType().equals(CommonConstant.TICKET_WIP_CLOSED_ISSUE_TYPE)) {
				List<String> list = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.TICKET_WIP_CLOSED_ISSUE_TYPE, list);
			} else if (identifier.getType().equals(CommonConstant.TICKET_THROUGHPUT_ISSUE_TYPE)) {
				List<String> uatList = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.TICKET_THROUGHPUT_ISSUE_TYPE, uatList);
			} else if (identifier.getType().equals(CommonConstant.KANBAN_CYCLE_TIME_ISSUE_TYPE)) {
				List<String> uatList = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.KANBAN_CYCLE_TIME_ISSUE_TYPE, uatList);
			} else if (identifier.getType().equals(CommonConstant.TICKET_REOPEN_ISSUE_TYPE)) {
				List<String> uatList = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.TICKET_REOPEN_ISSUE_TYPE, uatList);
			} else if (identifier.getType().equals(CommonConstant.KANBAN_TECH_DEBT_ISSUE_TYPE)) {
				List<String> uatList = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.KANBAN_TECH_DEBT_ISSUE_TYPE, uatList);
			}
		}
		return issueTypeMap;
	}

	private Map<String, List<String>> compareWorkflow(List<Identifier> workflowList, Set<String> allworkflow) {
		Map<String, List<String>> workflowMap = new HashMap<>();
		for (Identifier identifier : workflowList) {
			if (identifier.getType().equals(CommonConstant.DOR)) {
				List<String> dorList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.DOR, dorList);
			} else if (identifier.getType().equals(CommonConstant.DOD)) {
				List<String> dodList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.DOD, dodList);
			} else if (identifier.getType().equals(CommonConstant.DEVELOPMENT)) {
				List<String> devList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.DEVELOPMENT, devList);
			} else if (identifier.getType().equals(CommonConstant.QA)) {
				List<String> qaList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.QA, qaList);
			} else if (identifier.getType().equals(CommonConstant.FIRST_STATUS)) {
				List<String> fList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.FIRST_STATUS, fList);
			} else if (identifier.getType().equals(CommonConstant.REJECTION)) {
				List<String> rejList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.REJECTION, rejList);
			} else if (identifier.getType().equals(CommonConstant.DELIVERED)) {
				List<String> delList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.DELIVERED, delList);
			} else if (identifier.getType().equals(CommonConstant.TICKET_CLOSED_STATUS)) {
				List<String> closedList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_CLOSED_STATUS, closedList);
			} else if (identifier.getType().equals(CommonConstant.TICKET_RESOLVED_STATUS)) {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_RESOLVED_STATUS, list);
			} else if (identifier.getType().equals(CommonConstant.TICKET_REOPEN_STATUS)) {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_REOPEN_STATUS, list);
			} else if (identifier.getType().equals(CommonConstant.TICKET_TRIAGED_STATUS)) {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_TRIAGED_STATUS, list);
			} else if (identifier.getType().equals(CommonConstant.TICKET_WIP_STATUS)) {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_WIP_STATUS, list);
			} else if (identifier.getType().equals(CommonConstant.TICKET_REJECTED_STATUS)) {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_REJECTED_STATUS, list);
			}
		}
		return workflowMap;
	}

	private Map<String, String> compareCustomField(List<Identifier> customFieldList,
			Map<String, String> allCustomField) {
		Map<String, String> customFieldMap = new HashMap<>();
		customFieldList.forEach(identifier -> customFieldMap.put(identifier.getType(),
				allCustomField.get(identifier.getValue().get(0))));
		return customFieldMap;
	}

	private List<String> createFieldList(Set<String> allTypes, Identifier identifier) {
		List<String> issueList = new ArrayList<>();
		for (String iden : identifier.getValue()) {
			if (allTypes.contains(iden)) {
				issueList.add(iden);
			}
		}
		return issueList;
	}

}
