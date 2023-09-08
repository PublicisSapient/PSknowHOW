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

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
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
import com.publicissapient.kpidashboard.common.util.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.util.constant.MetadataType;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;
import com.publicissapient.kpidashboard.common.model.jira.Identifier;
import com.publicissapient.kpidashboard.common.model.jira.Metadata;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;
import com.publicissapient.kpidashboard.common.model.jira.MetadataValue;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
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

	public static final String DOJO_AGILE_TEMPLATE = "DOJO Agile Template";
	public static final String DOJO_SAFE_TEMPLATE = "DOJO Safe Template";
	public static final String DOJO_STUDIO_TEMPLATE = "DOJO Studio Template";
	public static final String STANDARD_TEMPLATE = "Standard Template";
	public static final String AZURE = "Azure";
	private final JiraAdapter jiraAdapter;
	private final BoardMetadataRepository boardMetadataRepository;
	private final FieldMappingRepository fieldMappingRepository;
	private final MetadataIdentifierRepository metadataIdentifierRepository;
	private PSLogData psLogData = new PSLogData();

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
		log.info("Fetching metadata start for project name : {}", projectConfig.getProjectName());
		Instant statProcessingMetadata = Instant.now();
		psLogData.setAction(CommonConstant.METADATA);
		List<Field> fieldList = jiraAdapter.getField();
		List<IssueType> issueTypeList = jiraAdapter.getIssueType();
		List<Status> statusList = jiraAdapter.getStatus();
		if (CollectionUtils.isNotEmpty(fieldList) && CollectionUtils.isNotEmpty(issueTypeList)
				&& CollectionUtils.isNotEmpty(statusList)) {

			BoardMetadata boardMetadata = new BoardMetadata();
			boardMetadata.setProjectBasicConfigId(projectConfig.getBasicProjectConfigId());
			boardMetadata.setProjectToolConfigId(projectConfig.getProjectToolConfig().getId());
			boardMetadata.setMetadataTemplateCode(projectConfig.getProjectToolConfig().getMetadataTemplateCode());
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
				fieldMappingRepository.save(fieldMapping);
				psLogData.setFieldMappingToDB("true");
				log.info("Saving fieldmapping into db", kv(CommonConstant.PSLOGDATA, psLogData));
				projectConfig.setFieldMapping(fieldMapping);
				isSuccess = true;
			}

			boardMetadataRepository.save(boardMetadata);
			psLogData.setMetaDataToDB("true");
			psLogData.setTimeTaken(String.valueOf(Duration.between(statProcessingMetadata, Instant.now()).toMillis()));
			log.info("Saving metadata into db", kv(CommonConstant.PSLOGDATA, psLogData));
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
		MetadataIdentifier metadataIdentifier = metadataIdentifierRepository.findByTemplateCodeAndToolAndIsKanban(
				projectConfig.getProjectToolConfig().getMetadataTemplateCode(), JiraConstants.JIRA,
				projectConfig.isKanban());
		String templateName = metadataIdentifier.getTemplateName();
		Map<String, List<String>> valuesToIdentifyMap = new HashMap<>();
		List<Identifier> issueList = metadataIdentifier.getIssues();
		List<Identifier> customFieldList = metadataIdentifier.getCustomfield();
		List<Identifier> workflowList = metadataIdentifier.getWorkflow();
		FieldMapping fieldMapping=null;
		Map<String, String> allCustomField = new HashMap<>();
		List<Metadata> metadataList = boardMetadata.getMetadata();
		metadataList.forEach(metadata -> metadata.getValue().stream().forEach(mv -> allCustomField.put(mv.getKey(), mv.getData())));

		if (metadataIdentifier.getTool().equalsIgnoreCase(AZURE) || projectConfig.isKanban()) {
			if (templateName.equalsIgnoreCase(STANDARD_TEMPLATE)) {
				valuesToIdentifyMap = metadataIdentifier.getValuestoidentify().stream()
						.collect(Collectors.toMap(Identifier::getType, Identifier::getValue));
			}

			Set<String> allIssueTypes = new HashSet<>();
			Set<String> allWorkflow = new HashSet<>();

			for (Metadata metadata : metadataList) {
				if (metadata.getType().equals(CommonConstant.META_ISSUE_TYPE)) {
					allIssueTypes = metadata.getValue().stream().map(MetadataValue::getData).collect(Collectors.toSet());
				} else if (metadata.getType().equals(CommonConstant.META_WORKFLOW)) {
					allWorkflow = metadata.getValue().stream().map(MetadataValue::getData).collect(Collectors.toSet());
				}
			}
			Map<String, List<String>> issueTypeMap = compareIssueType(issueList, allIssueTypes);
			Map<String, List<String>> workflowMap = compareWorkflow(workflowList, allWorkflow);
			Map<String, String> customField = compareCustomField(customFieldList, allCustomField);
			fieldMapping=mapFieldMapping(issueTypeMap, workflowMap, customField, valuesToIdentifyMap, projectConfig,
					templateName);
		} else {

			Map<String, List<String>> issueTypeMap = new HashMap<>();
			issueList.forEach(identifier -> issueTypeMap.put(identifier.getType(),
					CollectionUtils.isNotEmpty(identifier.getValue())?identifier.getValue():null));
			Map<String, List<String>> workflowMap = new HashMap<>();
			workflowList.forEach(identifier1 -> workflowMap.put(identifier1.getType(),
					CollectionUtils.isNotEmpty(identifier1.getValue())?identifier1.getValue():null));
			Map<String, String> customField = compareCustomField(customFieldList, allCustomField);
			fieldMapping=mapFieldMapping(issueTypeMap, workflowMap, customField, projectConfig);
		}

		return fieldMapping;

	}

	private FieldMapping mapFieldMapping(Map<String, List<String>> issueTypeMap, Map<String, List<String>> workflowMap,
			Map<String, String> customField, ProjectConfFieldMapping projectConfig) {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
		fieldMapping.setProjectToolConfigId(projectConfig.getJiraToolConfigId());
		fieldMapping.setProjectToolConfigId(projectConfig.getProjectToolConfig().getId());
		fieldMapping.setSprintName(customField.get(CommonConstant.SPRINTNAME));
		fieldMapping.setEpicCostOfDelay(customField.get(CommonConstant.EPICCOSTOFDELAY));
		fieldMapping.setEpicJobSize(customField.get(CommonConstant.EPICJOBSIZE));
		fieldMapping.setEpicRiskReduction(customField.get(CommonConstant.EPICRISKREDUCTION));
		fieldMapping.setEpicTimeCriticality(customField.get(CommonConstant.EPICTIMECRITICALITY));
		fieldMapping.setEpicUserBusinessValue(customField.get(CommonConstant.EPICUSERBUSINESSVALUE));
		fieldMapping.setEpicWsjf(customField.get(CommonConstant.EPICWSJF));
		fieldMapping.setRootCause(customField.get(CommonConstant.ROOT_CAUSE));
		fieldMapping.setJiraStoryPointsCustomField(
				customField.getOrDefault(CommonConstant.JIRASTORYPOINTSCUSTOMFIELD, StringUtils.EMPTY));
		fieldMapping.setCreatedDate(LocalDateTime.now());
		fieldMapping.setEpicLink(customField.get(CommonConstant.EPICLINK));

		fieldMapping.setJiraIssueTypeNames(issueTypeMap
				.getOrDefault(CommonConstant.JIRAISSUETYPENAMES, new ArrayList<>()).stream().toArray(String[]::new));
		fieldMapping.setJiraSprintCapacityIssueTypeKpi46(
				issueTypeMap.getOrDefault(CommonConstant.JIRASPRINTCAPACITYISSUETYPEKPI46, new ArrayList<>()));
		fieldMapping.setJiraDefectCountlIssueTypeKPI28(
				issueTypeMap.getOrDefault(CommonConstant.JIRADEFECTCOUNTLISSUETYPEKPI28, new ArrayList<>()));
		fieldMapping.setJiraDefectCountlIssueTypeKPI36(
				issueTypeMap.getOrDefault(CommonConstant.JIRADEFECTCOUNTLISSUETYPEKPI36, new ArrayList<>()));
		fieldMapping.setJiraDefectInjectionIssueTypeKPI14(
				issueTypeMap.getOrDefault(CommonConstant.JIRADEFECTINJECTIONISSUETYPEKPI14, new ArrayList<>()));
		fieldMapping
				.setJiraIssueTypeKPI35(issueTypeMap.getOrDefault(CommonConstant.JIRAISSUETYPEKPI35, new ArrayList<>()));
		fieldMapping.setJiraTestAutomationIssueType(
				issueTypeMap.getOrDefault(CommonConstant.JIRATESTAUTOMATIONISSUETYPE, new ArrayList<>()));
		fieldMapping.setJiraQAKPI111IssueType(
				issueTypeMap.getOrDefault(CommonConstant.JIRAQAKPI111ISSUETYPE, new ArrayList<>()));
		fieldMapping.setJiraStoryIdentificationKPI129(
				issueTypeMap.getOrDefault(CommonConstant.JIRASTORYIDENTIFICATIONKPI129, new ArrayList<>()));
		fieldMapping.setJiraSprintVelocityIssueTypeKPI138(
				issueTypeMap.getOrDefault(CommonConstant.JIRASPRINTVELOCITYISSUETYPEKPI138, new ArrayList<>()));
		fieldMapping.setJiraStoryIdentificationKpi40(
				issueTypeMap.getOrDefault(CommonConstant.JIRASTORYIDENTIFICATIONKPI40, new ArrayList<>()));
		fieldMapping.setJiraIssueEpicType(
				issueTypeMap.get(CommonConstant.JIRAISSUEEPICTYPE).stream().collect(Collectors.toList()));
		fieldMapping.setJiraTechDebtIssueType(issueTypeMap.get(CommonConstant.JIRATECHDEBTISSUETYPE));
		fieldMapping
				.setJiraIssueTypeKPI3(issueTypeMap.getOrDefault(CommonConstant.JIRAISSUETYPEKPI3, new ArrayList<>()));
		fieldMapping.setStoryFirstStatus(CommonConstant.OPEN);
		fieldMapping.setStoryFirstStatusKPI3(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.STORYFIRSTSTATUSKPI3))
						? workflowMap.get(CommonConstant.STORYFIRSTSTATUSKPI3).get(0)
						: CommonConstant.OPEN);
		fieldMapping.setStoryFirstStatusKPI148(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.STORYFIRSTSTATUSKPI148))
						? workflowMap.get(CommonConstant.STORYFIRSTSTATUSKPI148).get(0)
						: CommonConstant.OPEN);
		fieldMapping.setJiraIssueDeliverdStatusKPI138(
				workflowMap.getOrDefault(CommonConstant.JIRAISSUEDELIVERDSTATUSKPI138, new ArrayList<>()));
		fieldMapping.setJiraIssueDeliverdStatusKPI126(
				workflowMap.getOrDefault(CommonConstant.JIRAISSUEDELIVERDSTATUSKPI126, new ArrayList<>()));
		fieldMapping.setJiraIssueDeliverdStatusKPI82(
				workflowMap.getOrDefault(CommonConstant.JIRAISSUEDELIVERDSTATUSKPI82, new ArrayList<>()));
		fieldMapping.setJiraDefectCreatedStatusKPI14(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRADEFECTCREATEDSTATUSKPI14))
						? workflowMap.get(CommonConstant.JIRADEFECTCREATEDSTATUSKPI14).get(0)
						: CommonConstant.OPEN);
		fieldMapping.setJiraDodKPI14(workflowMap.get(CommonConstant.JIRADODKPI14));
		fieldMapping.setJiraDodKPI151(workflowMap.get(CommonConstant.JIRADODKPI151));
		fieldMapping.setJiraDodKPI152(workflowMap.get(CommonConstant.JIRADODKPI152));
		fieldMapping.setJiraDodQAKPI111(workflowMap.get(CommonConstant.JIRADODQAKPI111));
		fieldMapping.setJiraDodKPI3(workflowMap.get(CommonConstant.JIRADODKPI3));
		fieldMapping.setJiraDodKPI37(workflowMap.get(CommonConstant.JIRADODKPI37));
		fieldMapping.setJiraDodKPI155(workflowMap.get(CommonConstant.JIRADODKPI155));
		fieldMapping.setJiraDodKPI127(workflowMap.get(CommonConstant.JIRADODKPI127));
		fieldMapping.setJiraLiveStatusKPI152(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRALIVESTATUSKPI152))
						? workflowMap.get(CommonConstant.JIRALIVESTATUSKPI152).get(0)
						: null);
		fieldMapping.setJiraLiveStatusKPI151(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRALIVESTATUSKPI151))
						? workflowMap.get(CommonConstant.JIRALIVESTATUSKPI151).get(0)
						: null);
		fieldMapping
				.setJiraLiveStatusKPI3(CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRALIVESTATUSKPI3))
						? workflowMap.get(CommonConstant.JIRALIVESTATUSKPI3).get(0)
						: null);
		fieldMapping.setJiraLiveStatusKPI127(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRALIVESTATUSKPI127))
						? workflowMap.get(CommonConstant.JIRALIVESTATUSKPI127).get(0)
						: null);
		fieldMapping.setJiraDefectRemovalStatusKPI34(workflowMap.get(CommonConstant.JIRADEFECTREMOVALSTATUSKPI34));
		fieldMapping.setJiraDorKPI3(CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRADORKPI3))
				? workflowMap.get(CommonConstant.JIRADORKPI3).get(0)
				: null);
		fieldMapping.setResolutionTypeForRejectionKPI28(
				workflowMap.getOrDefault(CommonConstant.RESOLUTIONTYPEFORREJECTIONKPI28, new ArrayList<>()));
		fieldMapping.setResolutionTypeForRejectionKPI37(
				workflowMap.getOrDefault(CommonConstant.RESOLUTIONTYPEFORREJECTIONKPI37, new ArrayList<>()));
		fieldMapping.setResolutionTypeForRejectionRCAKPI36(
				workflowMap.getOrDefault(CommonConstant.RESOLUTIONTYPEFORREJECTIONRCAKPI36, new ArrayList<>()));
		fieldMapping.setResolutionTypeForRejectionKPI14(
				workflowMap.getOrDefault(CommonConstant.RESOLUTIONTYPEFORREJECTIONKPI14, new ArrayList<>()));
		fieldMapping.setResolutionTypeForRejectionQAKPI111(
				workflowMap.getOrDefault(CommonConstant.RESOLUTIONTYPEFORREJECTIONQAKPI111, new ArrayList<>()));
		fieldMapping.setResolutionTypeForRejectionKPI133(
				workflowMap.getOrDefault(CommonConstant.RESOLUTIONTYPEFORREJECTIONKPI133, new ArrayList<>()));
		fieldMapping.setResolutionTypeForRejectionKPI82(
				workflowMap.getOrDefault(CommonConstant.RESOLUTIONTYPEFORREJECTIONKPI82, new ArrayList<>()));
		fieldMapping.setResolutionTypeForRejectionKPI135(
				workflowMap.getOrDefault(CommonConstant.RESOLUTIONTYPEFORREJECTIONKPI135, new ArrayList<>()));
		fieldMapping.setResolutionTypeForRejectionKPI35(
				workflowMap.getOrDefault(CommonConstant.RESOLUTIONTYPEFORREJECTIONKPI35, new ArrayList<>()));
		fieldMapping.setJiraDefectDroppedStatusKPI127(
				workflowMap.getOrDefault(CommonConstant.JIRADEFECTDROPPEDSTATUSKPI127, new ArrayList<>()));
		fieldMapping.setJiraStatusForDevelopmentKPI82(workflowMap.get(CommonConstant.JIRASTATUSFORDEVELOPMENTKPI82));
		fieldMapping.setJiraStatusForDevelopmentKPI135(workflowMap.get(CommonConstant.JIRASTATUSFORDEVELOPMENTKPI135));
		fieldMapping.setJiraStatusForQaKPI148(workflowMap.get(CommonConstant.JIRASTATUSFORQAKPI148));
		fieldMapping.setJiraStatusForQaKPI82(workflowMap.get(CommonConstant.JIRASTATUSFORQAKPI82));
		fieldMapping.setJiraStatusForQaKPI135(workflowMap.get(CommonConstant.JIRASTATUSFORQAKPI135));
		fieldMapping.setJiraDefectRejectionStatusKPI28(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI28))
						? workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI28).get(0)
						: null);
		fieldMapping.setJiraDefectRejectionStatusKPI152(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI152))
						? workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI152).get(0)
						: null);
		fieldMapping.setJiraDefectRejectionStatusKPI151(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI151))
						? workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI151).get(0)
						: null);
		fieldMapping.setJiraDefectRejectionStatusKPI37(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI37))
						? workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI37).get(0)
						: null);
		fieldMapping.setJiraDefectRejectionStatusKPI35(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI35))
						? workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI35).get(0)
						: null);
		fieldMapping.setJiraDefectRejectionStatusKPI82(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI82))
						? workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI82).get(0)
						: null);
		fieldMapping.setJiraDefectRejectionStatusKPI135(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI135))
						? workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI135).get(0)
						: null);
		fieldMapping.setJiraDefectRejectionStatusKPI133(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI133))
						? workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI133).get(0)
						: null);
		fieldMapping.setJiraDefectRejectionStatusRCAKPI36(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSRCAKPI36))
						? workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSRCAKPI36).get(0)
						: null);
		fieldMapping.setJiraDefectRejectionStatusKPI14(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI14))
						? workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSKPI14).get(0)
						: null);
		fieldMapping.setJiraDefectRejectionStatusQAKPI111(
				CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSQAKPI111))
						? workflowMap.get(CommonConstant.JIRADEFECTREJECTIONSTATUSQAKPI111).get(0)
						: null);
		fieldMapping.setJiradefecttype(issueTypeMap.getOrDefault(CommonConstant.JIRADEFECTTYPE, new ArrayList<>()));
		fieldMapping.setJiraDefectClosedStatusKPI137(
				workflowMap.getOrDefault(CommonConstant.JIRADEFECTCLOSEDSTATUSKPI137, new ArrayList<>()));
		fieldMapping.setIssueStatusExcluMissingWorkKPI124(
				workflowMap.getOrDefault(CommonConstant.ISSUESTATUSEXCLUMISSINGWORKKPI124, new ArrayList<>()));
		fieldMapping.setJiraBlockedStatusKPI131(
				workflowMap.getOrDefault(CommonConstant.JIRABLOCKEDSTATUSKPI131, new ArrayList<>()));
		fieldMapping.setJiraWaitStatusKPI131(
				workflowMap.getOrDefault(CommonConstant.JIRAWAITSTATUSKPI131, new ArrayList<>()));
		fieldMapping.setJiraStatusForInProgressKPI148(
				workflowMap.getOrDefault(CommonConstant.JIRASTATUSFORINPROGRESSKPI148, new ArrayList<>()));
		fieldMapping.setJiraStatusForInProgressKPI122(
				workflowMap.getOrDefault(CommonConstant.JIRASTATUSFORINPROGRESSKPI122, new ArrayList<>()));
		fieldMapping.setJiraStatusForInProgressKPI145(
				workflowMap.getOrDefault(CommonConstant.JIRASTATUSFORINPROGRESSKPI145, new ArrayList<>()));
		fieldMapping.setJiraStatusForInProgressKPI125(
				workflowMap.getOrDefault(CommonConstant.JIRASTATUSFORINPROGRESSKPI125, new ArrayList<>()));
		fieldMapping.setJiraStatusForInProgressKPI128(
				workflowMap.getOrDefault(CommonConstant.JIRASTATUSFORINPROGRESSKPI128, new ArrayList<>()));
		fieldMapping.setJiraStatusForInProgressKPI123(
				workflowMap.getOrDefault(CommonConstant.JIRASTATUSFORINPROGRESSKPI123, new ArrayList<>()));
		fieldMapping.setJiraStatusForInProgressKPI119(
				workflowMap.getOrDefault(CommonConstant.JIRASTATUSFORINPROGRESSKPI119, new ArrayList<>()));
		fieldMapping.setJiraKPI82StoryIdentification(
				issueTypeMap.getOrDefault(CommonConstant.JIRAKPI82STORYIDENTIFICATION, new ArrayList<>()));
		fieldMapping.setJiraKPI135StoryIdentification(
				issueTypeMap.getOrDefault(CommonConstant.JIRAKPI135STORYIDENTIFICATION, new ArrayList<>()));
		return fieldMapping;
	}

	private FieldMapping mapFieldMapping(Map<String, List<String>> issueTypeMap, Map<String, List<String>> workflowMap,
			Map<String, String> customField, Map<String, List<String>> valuesToIdentifyMap,
			ProjectConfFieldMapping projectConfig, String templateName) {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
		fieldMapping.setProjectToolConfigId(projectConfig.getJiraToolConfigId());
		fieldMapping.setProjectToolConfigId(projectConfig.getProjectToolConfig().getId());
		fieldMapping.setSprintName(customField.get(CommonConstant.SPRINT));
		fieldMapping.setEpicCostOfDelay(customField.get(CommonConstant.COST_OF_DELAY));
		fieldMapping.setEpicJobSize(customField.get(CommonConstant.JOB_SIZE));
		fieldMapping.setEpicRiskReduction(customField.get(CommonConstant.RISK_REDUCTION));
		fieldMapping.setEpicTimeCriticality(customField.get(CommonConstant.TIME_CRITICALITY));
		fieldMapping.setEpicUserBusinessValue(customField.get(CommonConstant.USER_BUSINESS_VALUE));
		fieldMapping.setEpicWsjf(customField.get(CommonConstant.WSJF));
		fieldMapping.setRootCause(customField.get(CommonConstant.ROOT_CAUSE));
		fieldMapping
				.setJiraStoryPointsCustomField(customField.getOrDefault(CommonConstant.STORYPOINT, StringUtils.EMPTY));
		fieldMapping.setCreatedDate(LocalDateTime.now());
		fieldMapping.setEpicLink(customField.get(CommonConstant.EPICLINK));

		if (templateName.equalsIgnoreCase(DOJO_AGILE_TEMPLATE) || templateName.equalsIgnoreCase(DOJO_SAFE_TEMPLATE)
				|| templateName.equalsIgnoreCase(DOJO_STUDIO_TEMPLATE)) {

			populateFieldMappingData(issueTypeMap, workflowMap, projectConfig, templateName, fieldMapping);

		} else {
			fieldMapping.setJiradefecttype(issueTypeMap.get(CommonConstant.BUG));

			fieldMapping
					.setJiraIssueTypeNames(issueTypeMap.get(CommonConstant.ISSUE_TYPE).stream().toArray(String[]::new));
			fieldMapping.setJiraIssueTypeNamesAVR(
					issueTypeMap.get(CommonConstant.ISSUE_TYPE).stream().toArray(String[]::new));

			fieldMapping
					.setJiraIssueEpicType(issueTypeMap.get(CommonConstant.EPIC).stream().collect(Collectors.toList()));

			List<String> firstStatusList = workflowMap.get(CommonConstant.FIRST_STATUS);

			if (CollectionUtils.isNotEmpty(firstStatusList)) {
				fieldMapping.setStoryFirstStatus(firstStatusList.get(0));
				fieldMapping.setStoryFirstStatusKPI3(firstStatusList.get(0));
				fieldMapping.setStoryFirstStatusKPI148(firstStatusList.get(0));
				fieldMapping.setJiraDefectCreatedStatusKPI14(firstStatusList.get(0));
			} else {
				fieldMapping.setStoryFirstStatus(CommonConstant.OPEN);
				fieldMapping.setStoryFirstStatusKPI3(CommonConstant.OPEN);
				fieldMapping.setStoryFirstStatusKPI148(CommonConstant.OPEN);
				fieldMapping.setJiraDefectCreatedStatusKPI14(CommonConstant.OPEN);
			}
			fieldMapping.setIssueStatusExcluMissingWorkKPI124(firstStatusList);
			fieldMapping.setJiraStatusForDevelopmentAVR(workflowMap.get(CommonConstant.DEVELOPMENT));
			fieldMapping.setJiraStatusForDevelopmentKPI82(workflowMap.get(CommonConstant.DEVELOPMENT));
			fieldMapping.setJiraStatusForDevelopmentKPI135(workflowMap.get(CommonConstant.DEVELOPMENT));

			fieldMapping.setJiraStatusForQaKPI148(workflowMap.get(CommonConstant.QA));
			fieldMapping.setJiraStatusForQaKPI82(workflowMap.get(CommonConstant.QA));
			fieldMapping.setJiraStatusForQaKPI135(workflowMap.get(CommonConstant.QA));
			fieldMapping.setJiraDefectInjectionIssueTypeKPI14(issueTypeMap.get(CommonConstant.STORY));
			if (CollectionUtils.isNotEmpty(workflowMap.get(CommonConstant.DOR))) {
				fieldMapping.setJiraDorKPI3(workflowMap.get(CommonConstant.DOR).get(0));
			} else {
				fieldMapping.setJiraDorKPI3(null);
			}
			fieldMapping.setJiraDodKPI14(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodKPI151(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodKPI152(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodQAKPI111(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodKPI3(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodKPI127(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodKPI37(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodKPI155(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraTechDebtIssueType(issueTypeMap.get(CommonConstant.STORY));

			fieldMapping.setJiraIssueTypeKPI35(issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));

			fieldMapping.setJiraDefectRemovalStatusKPI34(
					workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
			fieldMapping.setJiraWaitStatusKPI131(
					workflowMap.getOrDefault(CommonConstant.JIRA_WAIT_STATUS, new ArrayList<>()));
			fieldMapping.setJiraBlockedStatusKPI131(
					workflowMap.getOrDefault(CommonConstant.JIRA_BLOCKED_STATUS, new ArrayList<>()));
			fieldMapping.setJiraStatusForInProgressKPI148(
					workflowMap.getOrDefault(CommonConstant.JIRA_IN_PROGRESS_STATUS, new ArrayList<>()));
			fieldMapping.setJiraStatusForInProgressKPI122(
					workflowMap.getOrDefault(CommonConstant.JIRA_IN_PROGRESS_STATUS, new ArrayList<>()));
			fieldMapping.setJiraStatusForInProgressKPI145(
					workflowMap.getOrDefault(CommonConstant.JIRA_IN_PROGRESS_STATUS, new ArrayList<>()));
			fieldMapping.setJiraStatusForInProgressKPI125(
					workflowMap.getOrDefault(CommonConstant.JIRA_IN_PROGRESS_STATUS, new ArrayList<>()));
			fieldMapping.setJiraStatusForInProgressKPI128(
					workflowMap.getOrDefault(CommonConstant.JIRA_IN_PROGRESS_STATUS, new ArrayList<>()));
			fieldMapping.setJiraStatusForInProgressKPI123(
					workflowMap.getOrDefault(CommonConstant.JIRA_IN_PROGRESS_STATUS, new ArrayList<>()));
			fieldMapping.setJiraStatusForInProgressKPI119(
					workflowMap.getOrDefault(CommonConstant.JIRA_IN_PROGRESS_STATUS, new ArrayList<>()));
			fieldMapping
					.setJiraTestAutomationIssueType(issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setJiraSprintVelocityIssueTypeKPI138(
					issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setJiraSprintCapacityIssueTypeKpi46(
					issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setJiraDefectCountlIssueTypeKPI28(
					issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setJiraDefectCountlIssueTypeKPI36(
					issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setJiraIssueDeliverdStatusKPI138(
					workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
			fieldMapping.setJiraIssueDeliverdStatusAVR(
					workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
			fieldMapping.setJiraIssueDeliverdStatusKPI126(
					workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
			fieldMapping.setJiraIssueDeliverdStatusKPI82(
					workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
			fieldMapping.setJiraIssueTypeKPI3(issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setJiraStoryIdentification(issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setJiraStoryIdentificationKPI129(
					issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setJiraStoryIdentificationKpi40(
					issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setJiraKPI82StoryIdentification(
					issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setJiraKPI135StoryIdentification(
					issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setRootCauseValue(valuesToIdentifyMap.get(CommonConstant.ROOT_CAUSE_VALUE));
			fieldMapping.setResolutionTypeForRejectionAVR(
					valuesToIdentifyMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI28(
					valuesToIdentifyMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI37(
					valuesToIdentifyMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionRCAKPI36(
					valuesToIdentifyMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI14(
					valuesToIdentifyMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionQAKPI111(
					valuesToIdentifyMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI133(
					valuesToIdentifyMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI82(
					valuesToIdentifyMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI135(
					valuesToIdentifyMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI35(
					valuesToIdentifyMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setJiraQAKPI111IssueType(issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setJiraDefectClosedStatusKPI137(
					workflowMap.getOrDefault(CommonConstant.JIRA_STATUS_FOR_CLOSED, new ArrayList<>()));

			if (projectConfig.isKanban()) {
				populateKanbanFieldMappingData(fieldMapping, workflowMap, issueTypeMap, templateName);
			}

		}

		return fieldMapping;
	}

	private void populateFieldMappingData(Map<String, List<String>> issueTypeMap, Map<String, List<String>> workflowMap,
			ProjectConfFieldMapping projectConfig, String templateName, FieldMapping fieldMapping) {
		if (projectConfig.isKanban()) {
			populateKanbanFieldMappingData(fieldMapping, workflowMap, issueTypeMap, templateName);
		} else {
			fieldMapping
					.setJiraIssueTypeNames(issueTypeMap.get(CommonConstant.ISSUE_TYPE).stream().toArray(String[]::new));
			fieldMapping.setJiraIssueTypeNamesAVR(
					issueTypeMap.get(CommonConstant.ISSUE_TYPE).stream().toArray(String[]::new));
			fieldMapping.setJiraSprintCapacityIssueTypeKpi46(
					issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setJiraDefectCountlIssueTypeKPI28(
					issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setJiraDefectCountlIssueTypeKPI36(
					issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setJiraDefectInjectionIssueTypeKPI14(
					issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setJiraIssueTypeKPI35(issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setJiraTestAutomationIssueType(
					issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
			fieldMapping
					.setJiraQAKPI111IssueType(issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setJiraStoryIdentification(issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setJiraStoryIdentificationKPI129(
					issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping.setJiraSprintVelocityIssueTypeKPI138(
					issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setJiraStoryIdentificationKpi40(
					issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));
			fieldMapping
					.setJiraIssueEpicType(issueTypeMap.get(CommonConstant.EPIC).stream().collect(Collectors.toList()));
			if (templateName.equalsIgnoreCase(DOJO_AGILE_TEMPLATE)) {
				fieldMapping.setJiraTechDebtIssueType(
						issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
			} else {
				fieldMapping.setJiraTechDebtIssueType(null);
			}
			fieldMapping.setJiraIssueTypeKPI3(issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setStoryFirstStatus(CommonConstant.OPEN);
			fieldMapping.setStoryFirstStatusKPI3(CommonConstant.OPEN);
			fieldMapping.setStoryFirstStatusKPI148(CommonConstant.OPEN);
			fieldMapping.setJiraIssueDeliverdStatusKPI138(
					workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
			fieldMapping.setJiraIssueDeliverdStatusAVR(
					workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
			fieldMapping.setJiraIssueDeliverdStatusKPI126(
					workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
			fieldMapping.setJiraIssueDeliverdStatusKPI82(
					workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
			fieldMapping.setJiraDefectCreatedStatusKPI14(CommonConstant.OPEN);
			fieldMapping.setJiraDodKPI14(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodKPI151(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodKPI152(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodQAKPI111(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodKPI3(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodKPI127(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodKPI37(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraDodKPI155(workflowMap.get(CommonConstant.DOD));
			fieldMapping.setJiraLiveStatusKPI152(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusKPI151(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusKPI3(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusLTK(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusNOPK(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusNORK(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusNOSK(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusOTA(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusKPI127(CommonConstant.CLOSED);
			fieldMapping.setJiraDefectRemovalStatusKPI34(null);
			fieldMapping.setJiraDorKPI3(CommonConstant.OPEN);
			fieldMapping.setResolutionTypeForRejectionAVR(
					workflowMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI28(
					workflowMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI37(
					workflowMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionRCAKPI36(
					workflowMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI14(
					workflowMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionQAKPI111(
					workflowMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI133(
					workflowMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI82(
					workflowMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI135(
					workflowMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setResolutionTypeForRejectionKPI35(
					workflowMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setJiraDefectDroppedStatusKPI127(
					workflowMap.getOrDefault(CommonConstant.REJECTION_RESOLUTION, new ArrayList<>()));
			fieldMapping.setJiraStatusForDevelopmentAVR(workflowMap.get(CommonConstant.DEVELOPMENT));
			fieldMapping.setJiraStatusForDevelopmentKPI82(workflowMap.get(CommonConstant.DEVELOPMENT));
			fieldMapping.setJiraStatusForDevelopmentKPI135(workflowMap.get(CommonConstant.DEVELOPMENT));
			fieldMapping.setJiraStatusForQaKPI148(workflowMap.get(CommonConstant.QA));
			fieldMapping.setJiraStatusForQaKPI82(workflowMap.get(CommonConstant.QA));
			fieldMapping.setJiraStatusForQaKPI135(workflowMap.get(CommonConstant.QA));
			fieldMapping.setJiraDefectRejectionStatusAVR(CommonConstant.REJECTED);
			fieldMapping.setJiraDefectRejectionStatusKPI151(CommonConstant.REJECTED);
			fieldMapping.setJiraDefectRejectionStatusKPI152(CommonConstant.REJECTED);
			fieldMapping.setJiraDefectRejectionStatusKPI28(CommonConstant.REJECTED);
			fieldMapping.setJiraDefectRejectionStatusKPI37(CommonConstant.REJECTED);
			fieldMapping.setJiraDefectRejectionStatusKPI35(CommonConstant.REJECTED);
			fieldMapping.setJiraDefectRejectionStatusKPI82(CommonConstant.REJECTED);
			fieldMapping.setJiraDefectRejectionStatusKPI135(CommonConstant.REJECTED);
			fieldMapping.setJiraDefectRejectionStatusKPI133(CommonConstant.REJECTED);
			fieldMapping.setJiraDefectRejectionStatusRCAKPI36(CommonConstant.REJECTED);
			fieldMapping.setJiraDefectRejectionStatusKPI14(CommonConstant.REJECTED);
			fieldMapping.setJiraDefectRejectionStatusQAKPI111(CommonConstant.REJECTED);
			fieldMapping.setJiradefecttype(issueTypeMap.get(CommonConstant.BUG));
		}
	}

	private void populateKanbanFieldMappingData(FieldMapping fieldMapping, Map<String, List<String>> workflowMap,
			Map<String, List<String>> issueTypeMap, String templateName) {

		fieldMapping.setCreatedDate(null);// setting null until kpi config not done for kanban kpis
		if (templateName.equalsIgnoreCase(DOJO_AGILE_TEMPLATE) || templateName.equalsIgnoreCase(DOJO_SAFE_TEMPLATE)
				|| templateName.equalsIgnoreCase(DOJO_STUDIO_TEMPLATE)) {

			fieldMapping.setStoryFirstStatus(CommonConstant.OPEN);
			fieldMapping.setStoryFirstStatusKPI3(CommonConstant.OPEN);
			fieldMapping.setStoryFirstStatusKPI148(CommonConstant.OPEN);
			fieldMapping.setJiraTicketResolvedStatus(
					workflowMap.getOrDefault(CommonConstant.TICKET_RESOLVED_STATUS, new ArrayList<>()));
			fieldMapping.setTicketDeliverdStatus(workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
			fieldMapping.setJiraTicketWipStatus(
					workflowMap.getOrDefault(CommonConstant.TICKET_WIP_STATUS, new ArrayList<>()));
			fieldMapping.setJiraTicketTriagedStatus(
					workflowMap.getOrDefault(CommonConstant.TICKET_TRIAGED_STATUS, new ArrayList<>()));
			fieldMapping.setJiraTicketRejectedStatus(
					workflowMap.getOrDefault(CommonConstant.TICKET_REJECTED_STATUS, new ArrayList<>()));
			fieldMapping.setJiraTicketClosedStatus(
					workflowMap.getOrDefault(CommonConstant.TICKET_CLOSED_STATUS, new ArrayList<>()));
			fieldMapping.setJiraLiveStatusKPI151(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusKPI152(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusKPI3(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusLTK(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusNOPK(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusNORK(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusNOSK(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusOTA(CommonConstant.CLOSED);
			fieldMapping.setJiraLiveStatusKPI127(CommonConstant.CLOSED);

			fieldMapping
					.setJiraIssueTypeNames(issueTypeMap.get(CommonConstant.ISSUE_TYPE).stream().toArray(String[]::new));
			fieldMapping.setJiraIssueTypeNamesAVR(
					issueTypeMap.get(CommonConstant.ISSUE_TYPE).stream().toArray(String[]::new));
			fieldMapping.setTicketCountIssueType(
					issueTypeMap.getOrDefault(CommonConstant.TICKET_COUNT_ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setKanbanRCACountIssueType(
					issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setJiraTicketVelocityIssueType(
					issueTypeMap.getOrDefault(CommonConstant.TICKET_VELOCITY_ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setKanbanCycleTimeIssueType(
					issueTypeMap.getOrDefault(CommonConstant.KANBAN_CYCLE_TIME_ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setKanbanJiraTechDebtIssueType(
					issueTypeMap.getOrDefault(CommonConstant.KANBAN_TECH_DEBT_ISSUE_TYPE, new ArrayList<>()));
			fieldMapping
					.setJiraIssueEpicType(issueTypeMap.get(CommonConstant.EPIC).stream().collect(Collectors.toList()));

		} else {
			fieldMapping
					.setTicketCountIssueType(issueTypeMap.getOrDefault(CommonConstant.ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setKanbanRCACountIssueType(Arrays.asList(JiraConstants.ISSUE_TYPE_DEFECT));
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
			fieldMapping.setJiraTicketWipStatus(
					workflowMap.getOrDefault(CommonConstant.TICKET_WIP_STATUS, new ArrayList<>()));
			fieldMapping.setKanbanCycleTimeIssueType(
					issueTypeMap.getOrDefault(CommonConstant.KANBAN_CYCLE_TIME_ISSUE_TYPE, new ArrayList<>()));
			fieldMapping.setKanbanJiraTechDebtIssueType(
					issueTypeMap.getOrDefault(CommonConstant.KANBAN_TECH_DEBT_ISSUE_TYPE, new ArrayList<>()));

		}

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
			} else if (identifier.getType().equals(CommonConstant.TICKET_COUNT_ISSUE_TYPE)) {
				List<String> ticketCountList = createFieldList(allIssueTypes, identifier);
				issueTypeMap.put(CommonConstant.TICKET_COUNT_ISSUE_TYPE, ticketCountList);
			}
		}
		return issueTypeMap;
	}

	private Map<String, List<String>> compareWorkflow(List<Identifier> workflowList, Set<String> allworkflow) {
		Map<String, List<String>> workflowMap = new HashMap<>();
		for (Identifier identifier : workflowList) {
			switch (identifier.getType()) {
			case CommonConstant.DOR:
				List<String> dorList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.DOR, dorList);
				break;
			case CommonConstant.DOD:
				List<String> dodList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.DOD, dodList);
				break;
			case CommonConstant.DEVELOPMENT:
				List<String> devList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.DEVELOPMENT, devList);
				break;
			case CommonConstant.QA:
				List<String> qaList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.QA, qaList);
				break;
			case CommonConstant.FIRST_STATUS:
				List<String> fList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.FIRST_STATUS, fList);
				break;
			case CommonConstant.REJECTION:
				List<String> rejList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.REJECTION, rejList);
				break;
			case CommonConstant.DELIVERED:
				List<String> delList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.DELIVERED, delList);
				break;
			case CommonConstant.TICKET_CLOSED_STATUS:
				List<String> closedList = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_CLOSED_STATUS, closedList);
				break;
			case CommonConstant.TICKET_RESOLVED_STATUS: {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_RESOLVED_STATUS, list);
				break;
			}
			case CommonConstant.TICKET_REOPEN_STATUS: {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_REOPEN_STATUS, list);
				break;
			}
			case CommonConstant.TICKET_TRIAGED_STATUS: {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_TRIAGED_STATUS, list);
				break;
			}
			case CommonConstant.TICKET_WIP_STATUS: {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_WIP_STATUS, list);
				break;
			}
			case CommonConstant.TICKET_REJECTED_STATUS: {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.TICKET_REJECTED_STATUS, list);
				break;
			}
			case CommonConstant.JIRA_BLOCKED_STATUS: {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.JIRA_BLOCKED_STATUS, list);
				break;
			}
			case CommonConstant.REJECTION_RESOLUTION: {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.REJECTION_RESOLUTION, list);
				break;
			}
			case CommonConstant.JIRA_WAIT_STATUS: {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.JIRA_WAIT_STATUS, list);
				break;
			}
			case CommonConstant.JIRA_IN_PROGRESS_STATUS: {
				List<String> list = createFieldList(allworkflow, identifier);
				workflowMap.put(CommonConstant.JIRA_IN_PROGRESS_STATUS, list);
				break;
			}
			default:
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
