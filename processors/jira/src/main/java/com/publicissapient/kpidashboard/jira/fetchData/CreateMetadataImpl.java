package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.MetadataType;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.*;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;


@Slf4j
@Service
public class CreateMetadataImpl implements CreateMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateMetadataImpl.class);

    PSLogData psLogData = new PSLogData();

    @Autowired
    private BoardMetadataRepository boardMetadataRepository;

    @Autowired
    private FieldMappingRepository fieldMappingRepository;

    @Autowired
    private MetadataIdentifierRepository metadataIdentifierRepository;

    private ProcessorJiraRestClient client;

    @Autowired
    private JiraProcessorConfig jiraProcessorConfig;

    private static final String MSG_JIRA_CLIENT_SETUP_FAILED = "Jira client setup failed. No results obtained. Check your jira setup.";

    private static final String ERROR_MSG_401 = "Error 401 connecting to JIRA server, your credentials are probably wrong. Note: Ensure you are using JIRA user name not your email address.";
    private static final String ERROR_MSG_NO_RESULT_WAS_AVAILABLE = "No result was available from Jira unexpectedly - defaulting to blank response. The reason for this fault is the following : {}";
    private static final String EXCEPTION = "Exception";


    @Override
    public void collectMetadata(ProjectConfFieldMapping projectConfig, ProcessorJiraRestClient clientIncoming) {
        client=clientIncoming;
        if (null == boardMetadataRepository.findByProjectBasicConfigId(projectConfig.getBasicProjectConfigId())) {
            psLogData.setAction(CommonConstant.METADATA);
            boolean isSuccess = processMetadata(projectConfig);
            if (isSuccess) {
                cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
                        CommonConstant.CACHE_FIELD_MAPPING_MAP);
                cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
                        CommonConstant.CACHE_PROJECT_CONFIG_MAP);
            }
            log.info("Fetched metadata", String.valueOf(isSuccess));
        } else {
            log.info("metadata already present in db");
        }
    }

    private boolean processMetadata(ProjectConfFieldMapping projectConfig) {
        boolean isSuccess = false;
        log.info("Fetching metadata start for project name : {}", projectConfig.getProjectName());
        Instant statProcessingMetadata = Instant.now();
        psLogData.setAction(CommonConstant.METADATA);
        List<Field> fieldList = getField();
        List<IssueType> issueTypeList = getIssueType();
        List<Status> statusList = getStatus();
        if (CollectionUtils.isNotEmpty(fieldList) && CollectionUtils.isNotEmpty(issueTypeList)
                && CollectionUtils.isNotEmpty(statusList)) {

            BoardMetadata boardMetadata = new BoardMetadata();
            boardMetadata.setProjectBasicConfigId(projectConfig.getBasicProjectConfigId());
            boardMetadata.setProjectToolConfigId(projectConfig.getProjectToolConfig().getId());
//            boardMetadata.setMetadataTemplateID(
//                    projectConfig.getProjectToolConfig().getMetadataTemplateID()
//            );
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

    private List<Field> getField() {
        List<Field> fieldList = new ArrayList<>();

        if (client == null) {
            log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
        } else {
            try {
                Promise<Iterable<Field>> promisedRs = client.getMetadataClient().getFields();

                Iterable<Field> fieldIt = promisedRs.claim();
                if (fieldIt != null) {
                    fieldList = Lists.newArrayList(fieldIt.iterator());
                }
            } catch (RestClientException e) {
                exceptionBlockProcess(e);
            }
        }

        return fieldList;
    }

    private List<IssueType> getIssueType() {
        List<IssueType> issueTypeList = new ArrayList<>();

        if (client == null) {
            log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
        } else {
            try {
                Promise<Iterable<IssueType>> promisedRs = client.getMetadataClient().getIssueTypes();

                Iterable<IssueType> fieldIt = promisedRs.claim();
                if (fieldIt != null) {
                    issueTypeList = Lists.newArrayList(fieldIt.iterator());
                }
            } catch (RestClientException e) {
                exceptionBlockProcess(e);
            }
        }

        return issueTypeList;
    }


    private List<Status> getStatus() {
        List<Status> statusList = new ArrayList<>();

        if (client == null) {
            log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
        } else {
            try {
                Promise<Iterable<Status>> promisedRs = client.getMetadataClient().getStatuses();

                Iterable<Status> fieldIt = promisedRs.claim();
                if (fieldIt != null) {
                    statusList = Lists.newArrayList(fieldIt.iterator());
                }
            } catch (RestClientException e) {
                exceptionBlockProcess(e);
            }
        }

        return statusList;
    }

    private void exceptionBlockProcess(RestClientException e) {
        if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 401) {
            log.error(ERROR_MSG_401);
        } else {
            log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, e.getCause());
        }
        log.debug(EXCEPTION, e);
    }

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

    private FieldMapping mapFieldMapping(BoardMetadata boardMetadata, ProjectConfFieldMapping projectConfig) {
        log.info("Fetching and comparing  metadata identifier");
        MetadataIdentifier metadataIdentifier = metadataIdentifierRepository.findByIdAndToolAndIsKanban(
//                projectConfig.getProjectToolConfig().getMetadataTemplateID()
                new ObjectId("63c702c0778b02d15e9e2b3e")
                ,JiraConstants.JIRA, projectConfig.isKanban());
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


        fieldMapping.setJiraDefectSeepageIssueType(
                issueTypeMap.getOrDefault(CommonConstant.STORY, new ArrayList<>()));

        fieldMapping.setJiraDefectRemovalStatus(
                workflowMap.getOrDefault(CommonConstant.DELIVERED, new ArrayList<>()));
        fieldMapping.setJiraWaitStatus(
                workflowMap.getOrDefault(CommonConstant.JIRA_WAIT_STATUS, new ArrayList<>()));
        fieldMapping.setJiraBlockedStatus(
                workflowMap.getOrDefault(CommonConstant.JIRA_BLOCKED_STATUS, new ArrayList<>()));
        fieldMapping.setJiraStatusForInProgress(
                workflowMap.getOrDefault(CommonConstant.JIRA_IN_PROGRESS_STATUS, new ArrayList<>()));
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

    private boolean cacheRestClient(String cacheEndPoint, String cacheName) {
        boolean cleaned = false;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(jiraProcessorConfig.getCustomApiBaseUrl());
        uriBuilder.path("/");
        uriBuilder.path(cacheEndPoint);
        uriBuilder.path("/");
        uriBuilder.path(cacheName);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, String.class);
        } catch (RuntimeException e) {
            LOGGER.error("[JIRA-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service", e);
        }

        if (null != response && response.getStatusCode().is2xxSuccessful()) {
            cleaned = true;
            LOGGER.info("[JIRA-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache {}", cacheName);
        } else {
            LOGGER.error("[JIRA-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache {}", cacheName);
        }
        return cleaned;
    }


}
