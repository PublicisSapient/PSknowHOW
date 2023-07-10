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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.publicissapient.kpidashboard.azure.adapter.AzureAdapter;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.azure.util.AzureConstants;
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

import lombok.extern.slf4j.Slf4j;

/**
 * The type Release data client. Store Release data for the projects in
 * persistence store
 */
@Slf4j
public class MetaDataClientImpl implements MetadataClient {

	private final AzureAdapter azureAdapter;
	private final BoardMetadataRepository boardMetadataRepository;
	private final FieldMappingRepository fieldMappingRepository;
	private final MetadataIdentifierRepository metadataIdentifierRepository;

	/**
	 * Creates object
	 *
	 * @param azureAdapter
	 *            Azure Adapter instance
	 * @param boardMetadataRepository
	 *            project metadata repository
	 * @param metadataIdentifierRepository
	 *            existing metadata to compare
	 * @param fieldMappingRepository
	 *            for saving missing field mapping
	 */
	public MetaDataClientImpl(AzureAdapter azureAdapter, BoardMetadataRepository boardMetadataRepository,
			FieldMappingRepository fieldMappingRepository, MetadataIdentifierRepository metadataIdentifierRepository) {

		this.boardMetadataRepository = boardMetadataRepository;
		this.azureAdapter = azureAdapter;
		this.fieldMappingRepository = fieldMappingRepository;
		this.metadataIdentifierRepository = metadataIdentifierRepository;
	}

	@Override
	@Transactional
	public boolean processMetadata(ProjectConfFieldMapping projectConfig) {
		boolean isSuccess = false;
		log.info("Fetching metadata. Project name : {}", projectConfig.getProjectName());
		List<Field> fieldList = azureAdapter.getField();
		List<IssueType> issueTypeList = azureAdapter.getIssueType();
		List<Status> statusList = azureAdapter.getStatus();

		BoardMetadata boardMetadata = new BoardMetadata();
		boardMetadata.setProjectBasicConfigId(projectConfig.getBasicProjectConfigId());
		boardMetadata.setProjectToolConfigId(projectConfig.getAzureBoardToolConfigId());
		if (CollectionUtils.isNotEmpty(fieldList) && CollectionUtils.isNotEmpty(issueTypeList)
				&& CollectionUtils.isNotEmpty(statusList)) {
			List<Metadata> metadataList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(fieldList)) {
				mapFields(fieldList, metadataList);
			}
			if (CollectionUtils.isNotEmpty(issueTypeList)) {
				mapIssueTypes(issueTypeList, metadataList);
			}

			if (CollectionUtils.isNotEmpty(statusList)) {
				mapWorkFlow(statusList, metadataList);
			}
			boardMetadata.setMetadata(metadataList);
			if (null == projectConfig.getFieldMapping()) {
				FieldMapping fieldMapping = mapFieldMapping(boardMetadata, projectConfig);
				log.info("Saving fieldmapping into db.Project name : {}", projectConfig.getProjectName());
				fieldMappingRepository.save(fieldMapping);
				projectConfig.setFieldMapping(fieldMapping);
				isSuccess = true;
			}

			log.info("Saving metadata into db.Project name : {}", projectConfig.getProjectName());
			boardMetadataRepository.save(boardMetadata);

		}
		return isSuccess;
	}

	/**
	 * Maps Fields
	 *
	 * @param fieldList
	 * @param metadataList
	 */
	private void mapFields(List<Field> fieldList, List<Metadata> metadataList) {

		List<MetadataValue> fieldValue = new ArrayList<>();
		fieldList.forEach(field -> {
			MetadataValue metaDataValue = new MetadataValue();
			metaDataValue.setKey(field.getName());
			metaDataValue.setData(field.getId());
			fieldValue.add(metaDataValue);
		});
		Metadata fieldMetadata = new Metadata();
		fieldMetadata.setType(MetadataType.FIELDS.type());
		fieldMetadata.setValue(fieldValue);
		metadataList.add(fieldMetadata);
	}

	/**
	 * Maps Issue Types
	 *
	 * @param issueTypeList
	 * @param metadataList
	 */
	private void mapIssueTypes(List<IssueType> issueTypeList, List<Metadata> metadataList) {
		List<MetadataValue> issueyTypeValue = new ArrayList<>();
		issueTypeList.forEach(issueType -> {
			MetadataValue metaDataValue = new MetadataValue();
			metaDataValue.setKey(issueType.getName());
			metaDataValue.setData(issueType.getName());
			issueyTypeValue.add(metaDataValue);
		});
		Metadata fieldMetadata = new Metadata();
		fieldMetadata.setType(MetadataType.ISSUETYPE.type());
		fieldMetadata.setValue(issueyTypeValue);
		metadataList.add(fieldMetadata);
	}

	/**
	 * maps item workflow
	 *
	 * @param statusList
	 * @param metadataList
	 */
	private void mapWorkFlow(List<Status> statusList, List<Metadata> metadataList) {

		List<MetadataValue> statusValue = new ArrayList<>();
		statusList.forEach(status -> {
			MetadataValue metaDataValue = new MetadataValue();
			metaDataValue.setKey(status.getName());
			metaDataValue.setData(status.getName());
			statusValue.add(metaDataValue);
		});
		Metadata fieldMetadata = new Metadata();
		fieldMetadata.setType(MetadataType.WORKFLOW.type());
		fieldMetadata.setValue(statusValue);
		metadataList.add(fieldMetadata);
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
		MetadataIdentifier metadataIdentifier = metadataIdentifierRepository.findByToolAndIsKanban(AzureConstants.AZURE,
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

	/**
	 * Creates field mapping for the project based on provided mappings
	 *
	 * @param issueTypeMap
	 * @param workflowMap
	 * @param customField
	 * @param valuesToIdentifyMap
	 * @param projectConfig
	 * @return project FieldMapping
	 */
	private FieldMapping mapFieldMapping(Map<String, List<String>> issueTypeMap, Map<String, List<String>> workflowMap,
			Map<String, String> customField, Map<String, List<String>> valuesToIdentifyMap,
			ProjectConfFieldMapping projectConfig) {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setProjectToolConfigId(projectConfig.getAzureBoardToolConfigId());
		fieldMapping.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
		fieldMapping.setSprintName(customField.get(CommonConstant.SPRINT));
		fieldMapping.setJiradefecttype(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeSWE(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeKPI132(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeKPI136(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeKPI140(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeRDCA(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeRDCP(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeRDCR(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeRDCS(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeKPI133(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeIWS(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeLT(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeMW(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeFTPR(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeKPI135(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeAVR(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeIC(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeCVR(issueTypeMap.get(CommonConstant.BUG));
		fieldMapping.setJiradefecttypeBDRR(issueTypeMap.get(CommonConstant.BUG));

		fieldMapping.setJiraIssueTypeNames(issueTypeMap.get(CommonConstant.ISSUE_TYPE).stream().toArray(String[]::new));
		fieldMapping.setJiraIssueTypeNamesAVR(issueTypeMap.get(CommonConstant.ISSUE_TYPE).stream().toArray(String[]::new));
		List<String> firstStatusList = workflowMap.get(CommonConstant.FIRST_STATUS);
		fieldMapping.setJiraIssueEpicType(issueTypeMap.get(CommonConstant.EPIC).stream().collect(Collectors.toList()));
		fieldMapping.setEpicJobSize(customField.get(CommonConstant.JOB_SIZE));
		fieldMapping.setEpicRiskReduction(customField.get(CommonConstant.RISK_REDUCTION));
		fieldMapping.setEpicTimeCriticality(customField.get(CommonConstant.TIME_CRITICALITY));
		fieldMapping.setEpicUserBusinessValue(customField.get(CommonConstant.USER_BUSINESS_VALUE));
		fieldMapping.setEpicWsjf(customField.get(CommonConstant.WSJF));
		if (CollectionUtils.isNotEmpty(firstStatusList)) {
			fieldMapping.setStoryFirstStatus(firstStatusList.get(0));
			fieldMapping.setJiraDefectCreatedStatus(firstStatusList.get(0));
		} else {
			fieldMapping.setStoryFirstStatus(CommonConstant.OPEN);
			fieldMapping.setJiraDefectCreatedStatus(CommonConstant.OPEN);
		}
		fieldMapping.setRootCause(customField.get(CommonConstant.ROOT_CAUSE));
		fieldMapping.setJiraStatusForDevelopmentAVR(workflowMap.get(CommonConstant.DEVELOPMENT));
		fieldMapping.setJiraStatusForDevelopmentFTPR(workflowMap.get(CommonConstant.DEVELOPMENT));
		fieldMapping.setJiraStatusForDevelopmentKPI135(workflowMap.get(CommonConstant.DEVELOPMENT));
		fieldMapping.setJiraStatusForQa(workflowMap.get(CommonConstant.QA));
		fieldMapping.setJiraDefectInjectionIssueType(issueTypeMap.get(CommonConstant.STORY));
		if (CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.DOR))) {
			fieldMapping.setJiraDorLT(workflowMap.get(CommonConstant.DOR).get(0));
		} else {
			fieldMapping.setJiraDorLT(null);
		}
		fieldMapping.setJiraDodDIR(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraDodQADD(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraDodLT(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraDodPDA(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusCustomField(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI120(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI119(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI128(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI134(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI123(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI131(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI133(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI132(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI135(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI122(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI75(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI145(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI140(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI132(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI136(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusIC(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusCR(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusSV(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusSP(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI124(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusKPI125(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraIterationCompletionStatusBRE(workflowMap.get(CommonConstant.DOD));
		fieldMapping.setJiraTechDebtIssueType(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setJiraDefectSeepageIssueType(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setJiraDefectRemovalStatus(workflowMap.get(CommonConstant.DELIVERED));
		fieldMapping.setJiraDefectRemovalIssueType(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setJiraStoryPointsCustomField(customField.get(CommonConstant.STORYPOINT));
		fieldMapping.setJiraTestAutomationIssueType(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setJiraSprintVelocityIssueTypeSV(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setJiraSprintVelocityIssueTypeBR(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setJiraSprintCapacityIssueType(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setJiraDefectRejectionlIssueType(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setJiraDefectCountlIssueTypeDC(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setJiraDefectCountlIssueTypeRCA(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setJiraIssueDeliverdStatusSV(workflowMap.get(CommonConstant.DELIVERED));
		fieldMapping.setJiraIssueDeliverdStatusBR(workflowMap.get(CommonConstant.DELIVERED));
		fieldMapping.setJiraIssueDeliverdStatusAVR(workflowMap.get(CommonConstant.DELIVERED));
		fieldMapping.setJiraIssueDeliverdStatusCVR(workflowMap.get(CommonConstant.DELIVERED));
		fieldMapping.setJiraIssueDeliverdStatusFTPR(workflowMap.get(CommonConstant.DELIVERED));

		fieldMapping.setJiraIntakeToDorIssueTypeLT(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setJiraStoryIdentification(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setJiraFTPRStoryIdentification(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setJiraKPI135StoryIdentification(issueTypeMap.get(CommonConstant.STORY));
		fieldMapping.setRootCauseValue(valuesToIdentifyMap.get(CommonConstant.ROOT_CAUSE_VALUE));
		fieldMapping.setResolutionTypeForRejectionAVR(valuesToIdentifyMap.get(CommonConstant.REJECTION_RESOLUTION));
		fieldMapping.setResolutionTypeForRejectionDC(valuesToIdentifyMap.get(CommonConstant.REJECTION_RESOLUTION));
		fieldMapping.setResolutionTypeForRejectionDRE(valuesToIdentifyMap.get(CommonConstant.REJECTION_RESOLUTION));
		fieldMapping.setResolutionTypeForRejectionDRR(valuesToIdentifyMap.get(CommonConstant.REJECTION_RESOLUTION));
		fieldMapping.setResolutionTypeForRejectionDSR(valuesToIdentifyMap.get(CommonConstant.REJECTION_RESOLUTION));
		fieldMapping.setResolutionTypeForRejectionFTPR(valuesToIdentifyMap.get(CommonConstant.REJECTION_RESOLUTION));
		fieldMapping.setResolutionTypeForRejectionIFTPR(valuesToIdentifyMap.get(CommonConstant.REJECTION_RESOLUTION));
		fieldMapping.setResolutionTypeForRejectionKPI133(valuesToIdentifyMap.get(CommonConstant.REJECTION_RESOLUTION));
		fieldMapping.setResolutionTypeForRejectionRCA(valuesToIdentifyMap.get(CommonConstant.REJECTION_RESOLUTION));
		fieldMapping.setResolutionTypeForRejectionDIR(valuesToIdentifyMap.get(CommonConstant.REJECTION_RESOLUTION));
		fieldMapping.setResolutionTypeForRejectionQADD(valuesToIdentifyMap.get(CommonConstant.REJECTION_RESOLUTION));
		fieldMapping.setJiraQADefectDensityIssueType(issueTypeMap.get(CommonConstant.STORY));

		if (projectConfig.isKanban()) {
			populateKanbanFieldMappingData(fieldMapping, workflowMap, issueTypeMap);
		}
		return fieldMapping;
	}

	private void populateKanbanFieldMappingData(FieldMapping fieldMapping, Map<String, List<String>> workflowMap,
			Map<String, List<String>> issueTypeMap) {
		fieldMapping.setTicketCountIssueType(issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
		fieldMapping
				.setKanbanRCACountIssueType(issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
		fieldMapping.setJiraTicketVelocityIssueType(
				issueTypeMap.getOrDefault(CommonConstant.TICKET_VELOCITY_ISSUE_TYPE, new ArrayList<>()));
		fieldMapping.setTicketDeliverdStatus(workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
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
		fieldMapping
				.setJiraTicketWipStatus(workflowMap.getOrDefault(CommonConstant.TICKET_WIP_STATUS, new ArrayList<>()));
		fieldMapping.setKanbanCycleTimeIssueType(
				issueTypeMap.getOrDefault(CommonConstant.KANBAN_CYCLE_TIME_ISSUE_TYPE, new ArrayList<>()));
		fieldMapping.setKanbanJiraTechDebtIssueType(
				issueTypeMap.getOrDefault(CommonConstant.KANBAN_TECH_DEBT_ISSUE_TYPE, new ArrayList<>()));

	}

	/**
	 * Compares IssuesTypes to provide intersection of identifier list and
	 * allIssueTypes
	 *
	 * @param issueList
	 * @param allIssueTypes
	 * @return Map of common values for all types
	 */
	private Map<String, List<String>> compareIssueType(List<Identifier> issueList, Set<String> allIssueTypes) {
		Map<String, List<String>> issueTypeMap = new HashMap<>();
		for (Identifier identifier : issueList) {
			List<String> list;
			String identifierType = identifier.getType();
			switch (identifierType) {
			case CommonConstant.STORY:
				list = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.STORY, list);
				break;
			case CommonConstant.BUG:
				list = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.BUG, list);
				break;
			case CommonConstant.EPIC:
				list = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.EPIC, list);
				break;
			case CommonConstant.ISSUE_TYPE:
				list = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.ISSUE_TYPE, list);
				break;
			case CommonConstant.UAT_DEFECT:
				list = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.UAT_DEFECT, list);
				break;
			case CommonConstant.TICKET_VELOCITY_ISSUE_TYPE:
				list = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.TICKET_VELOCITY_ISSUE_TYPE, list);
				break;
			case CommonConstant.TICKET_WIP_CLOSED_ISSUE_TYPE:
				list = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.TICKET_WIP_CLOSED_ISSUE_TYPE, list);
				break;
			case CommonConstant.TICKET_THROUGHPUT_ISSUE_TYPE:
				list = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.TICKET_THROUGHPUT_ISSUE_TYPE, list);
				break;
			case CommonConstant.KANBAN_CYCLE_TIME_ISSUE_TYPE:
				list = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.KANBAN_CYCLE_TIME_ISSUE_TYPE, list);
				break;
			case CommonConstant.TICKET_REOPEN_ISSUE_TYPE:
				list = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.TICKET_REOPEN_ISSUE_TYPE, list);
				break;
			case CommonConstant.KANBAN_TECH_DEBT_ISSUE_TYPE:
				list = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.KANBAN_TECH_DEBT_ISSUE_TYPE, list);
				break;
			default:
				break;
			}
		}
		return issueTypeMap;
	}

	private Map<String, List<String>> compareWorkflow(List<Identifier> workflowList, Set<String> allworkflow) {
		Map<String, List<String>> workflowMap = new HashMap<>();
		for (Identifier identifier : workflowList) {
			List<String> list;
			String identifierType = identifier.getType();
			switch (identifierType) {
			case CommonConstant.DOR:
				list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.DOR, list);
				break;
			case CommonConstant.DOD:
				list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.DOD, list);
				break;
			case CommonConstant.DEVELOPMENT:
				list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.DEVELOPMENT, list);
				break;
			case CommonConstant.QA:
				list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.QA, list);
				break;
			case CommonConstant.FIRST_STATUS:
				list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.FIRST_STATUS, list);
				break;
			case CommonConstant.REJECTION:
				list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.REJECTION, list);
				break;
			case CommonConstant.DELIVERED:
				list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.DELIVERED, list);
				break;
			case CommonConstant.TICKET_CLOSED_STATUS:
				list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_CLOSED_STATUS, list);
				break;
			case CommonConstant.TICKET_RESOLVED_STATUS:
				list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_RESOLVED_STATUS, list);
				break;
			case CommonConstant.TICKET_TRIAGED_STATUS:
				list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_TRIAGED_STATUS, list);
				break;
			case CommonConstant.TICKET_WIP_STATUS:
				list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_WIP_STATUS, list);
				break;
			case CommonConstant.TICKET_REJECTED_STATUS:
				list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_REJECTED_STATUS, list);
				break;
			default:
				break;
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
		List<String> identifierValue = identifier.getValue() == null ? new ArrayList<>() : identifier.getValue();
		return identifierValue.stream().filter(allTypes::contains).collect(Collectors.toList());
	}

}
