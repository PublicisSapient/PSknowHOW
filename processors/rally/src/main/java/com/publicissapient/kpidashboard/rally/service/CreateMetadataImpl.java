package com.publicissapient.kpidashboard.rally.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.rally.model.RallyAllowedValuesResponse;
import com.publicissapient.kpidashboard.rally.model.RallyAllowedValuesResponse.AllowedValue;
import com.publicissapient.kpidashboard.rally.model.RallyResponse;
import com.publicissapient.kpidashboard.rally.model.RallyTypeDefinitionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;
import com.publicissapient.kpidashboard.common.model.jira.Metadata;
import com.publicissapient.kpidashboard.common.model.jira.MetadataValue;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.rally.cache.RallyProcessorCacheEvictor;
import com.publicissapient.kpidashboard.rally.constant.RallyConstants;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.util.RallyRestClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateMetadataImpl implements CreateMetadata {

    @Autowired
    private BoardMetadataRepository boardMetadataRepository;
    
    @Autowired
    private FieldMappingRepository fieldMappingRepository;
    
    @Autowired
    private RallyProcessorCacheEvictor rallyProcessorCacheEvictor;
    
    @Autowired
    private ProcessorToolConnectionService processorToolConnectionService;
    
    @Autowired
    private RallyRestClient rallyRestClient;

    @Override
    public void collectMetadata(ProjectConfFieldMapping projectConfig, String isScheduler) {
        processorToolConnectionService.validateJiraAzureConnFlag(projectConfig.getProjectToolConfig());
        if (isScheduler.equalsIgnoreCase("false") || 
            null == boardMetadataRepository.findByProjectBasicConfigId(projectConfig.getBasicProjectConfigId())) {
            boardMetadataRepository.deleteByProjectBasicConfigId(projectConfig.getBasicProjectConfigId());
            log.info("Creating metadata for the project: {}", projectConfig.getProjectName());
            BoardMetadata boardMetadata = createBoardMetadata(projectConfig);
            boardMetadataRepository.save(boardMetadata);
            if (null == projectConfig.getFieldMapping()) {
                FieldMapping fieldMapping = mapFieldMapping(boardMetadata, projectConfig);
                fieldMappingRepository.save(fieldMapping);
                projectConfig.setFieldMapping(fieldMapping);
            }
            evictCaches();
            log.info("Fetched metadata successfully");
        } else {
            log.info("Metadata already present for the project: {} so not fetching again", projectConfig.getProjectName());
        }
    }

    private BoardMetadata createBoardMetadata(ProjectConfFieldMapping projectConfig) {
        BoardMetadata boardMetadata = new BoardMetadata();
        boardMetadata.setProjectBasicConfigId(projectConfig.getBasicProjectConfigId());
        boardMetadata.setProjectToolConfigId(projectConfig.getProjectToolConfig().getId());
        boardMetadata.setMetadataTemplateCode(projectConfig.getProjectToolConfig().getOriginalTemplateCode());
        boardMetadata.setMetadata(initializeRallyMetadata(projectConfig));
        return boardMetadata;
    }

    private List<Metadata> initializeRallyMetadata(ProjectConfFieldMapping projectConfig) {
        List<Metadata> fullMetaDataList = new ArrayList<>();
        
        // Initialize issue types metadata
        Metadata issueTypeMetadata = new Metadata();
        issueTypeMetadata.setType("Issue_Type");
        issueTypeMetadata.setValue(fetchTypeDefinitions(projectConfig));
        fullMetaDataList.add(issueTypeMetadata);

        // Initialize status metadata
        Metadata statusMetadata = new Metadata();
        statusMetadata.setType("status");
        statusMetadata.setValue(fetchAllowedValues(projectConfig, "State"));
        fullMetaDataList.add(statusMetadata);

        // Initialize workflow metadata
        Metadata workflowMetadata = new Metadata();
        workflowMetadata.setType("workflow");
        workflowMetadata.setValue(mapWorkflowValues(statusMetadata.getValue()));
        fullMetaDataList.add(workflowMetadata);

        return fullMetaDataList;
    }

    private List<MetadataValue> fetchTypeDefinitions(ProjectConfFieldMapping projectConfig) {
        try {
            String typesUrl = String.format("%s/typedefinition", rallyRestClient.getBaseUrl());
            log.info("Fetching Rally type definitions from URL: {}", typesUrl);
            
            ResponseEntity<RallyTypeDefinitionResponse> response = rallyRestClient.get(typesUrl, projectConfig, RallyTypeDefinitionResponse.class);
            log.debug("Rally API response status: {}", response != null ? response.getStatusCode() : "null");
            
            if (response != null && response.getBody() != null) {
                RallyTypeDefinitionResponse.QueryResult queryResult = response.getBody().getQueryResult();
                if (queryResult != null) {
                    if (!queryResult.getErrors().isEmpty()) {
                        log.error("Rally API returned errors: {}", queryResult.getErrors());
                        return getDefaultTypeDefinitions();
                    }
                    
                    if (!queryResult.getWarnings().isEmpty()) {
                        log.warn("Rally API returned warnings: {}", queryResult.getWarnings());
                    }
                    
                    if (queryResult.getResults() != null && !queryResult.getResults().isEmpty()) {
                        List<MetadataValue> typeValues = queryResult.getResults().stream()
                            .filter(type -> Arrays.asList("HierarchicalRequirement", "Defect", "Task", "TestCase", "DefectSuite", "Feature")
                                    .contains(type.getRefObjectName()))
                            .map(type -> {
                                String name = type.getRefObjectName();
                                log.debug("Processing type: {}", name);
                                return createMetadataValue(name, name);
                            })
                            .collect(Collectors.toList());
                        
                        if (!typeValues.isEmpty()) {
                            log.info("Successfully fetched {} Rally type definitions", typeValues.size());
                            return typeValues;
                        }
                    }
                }
            }
            
            log.info("Using default Rally type definitions");
            return getDefaultTypeDefinitions();
        } catch (Exception e) {
            log.error("Error fetching Rally type definitions", e);
            return getDefaultTypeDefinitions();
        }
    }

    private List<MetadataValue> getDefaultTypeDefinitions() {
        return Arrays.asList(
            createMetadataValue("HierarchicalRequirement", "User Story"),
            createMetadataValue("Defect", "Defect"),
            createMetadataValue("Task", "Task"),
            createMetadataValue("TestCase", "Test Case"),
            createMetadataValue("DefectSuite", "Defect Suite"),
            createMetadataValue("Feature", "Feature")
        );
    }

    private List<MetadataValue> fetchAllowedValues(ProjectConfFieldMapping projectConfig, String fieldName) {
        try {
            String allowedValuesUrl = String.format("%s/allowedAttributeValues?attributeName=%s", 
                rallyRestClient.getBaseUrl(), fieldName);
            log.info("Fetching Rally allowed values from URL: {}", allowedValuesUrl);
            
            ResponseEntity<RallyAllowedValuesResponse> response = rallyRestClient.get(allowedValuesUrl, projectConfig, RallyAllowedValuesResponse.class);
            log.debug("Rally API response status: {}", response != null ? response.getStatusCode() : "null");
            
            if (response != null && response.getBody() != null) {
                RallyAllowedValuesResponse.QueryResult queryResult = response.getBody().getQueryResult();
                if (queryResult != null) {
                    if (!queryResult.getErrors().isEmpty()) {
                        log.error("Rally API returned errors: {}", queryResult.getErrors());
                        return getDefaultStateValues();
                    }
                    
                    if (!queryResult.getWarnings().isEmpty()) {
                        log.warn("Rally API returned warnings: {}", queryResult.getWarnings());
                    }
                    
                    if (queryResult.getResults() != null && !queryResult.getResults().isEmpty()) {
                        List<MetadataValue> stateValues = queryResult.getResults().stream()
                            .map(value -> {
                                String displayValue = value.getDisplayValue();
                                String stringValue = value.getStringValue();
                                log.debug("Processing state: {} -> {}", stringValue, displayValue);
                                return createMetadataValue(displayValue, stringValue);
                            })
                            .collect(Collectors.toList());
                        
                        if (!stateValues.isEmpty()) {
                            log.info("Successfully fetched {} Rally allowed values", stateValues.size());
                            return stateValues;
                        }
                    }
                }
            }
            
            log.info("Using default Rally states");
            return getDefaultStateValues();
        } catch (Exception e) {
            log.error("Error fetching Rally allowed values for field: " + fieldName, e);
            return getDefaultStateValues();
        }
    }

    private List<MetadataValue> getDefaultStateValues() {
        return Arrays.asList(
            createMetadataValue("Defined", "Defined"),
            createMetadataValue("In-Progress", "In Progress"),
            createMetadataValue("Completed", "Completed"),
            createMetadataValue("Accepted", "Accepted"),
            createMetadataValue("Backlog", "Backlog"),
            createMetadataValue("Ready", "Ready"),
            createMetadataValue("InDevelopment", "In Development"),
            createMetadataValue("Testing", "Testing"),
            createMetadataValue("Done", "Done")
        );
    }

    private List<MetadataValue> mapWorkflowValues(List<MetadataValue> statusValues) {
        // Map status values to workflow stages based on memory
        return Arrays.asList(
            createMetadataValue("Development", "InDevelopment,In Development"),
            createMetadataValue("QA", "Testing"),
            createMetadataValue("Delivered", "Done,Accepted"),
            createMetadataValue("DOR", "Ready"),
            createMetadataValue("DOD", "Done,Accepted")
        );
    }

    private MetadataValue createMetadataValue(String key, String data) {
        MetadataValue value = new MetadataValue();
        value.setKey(key);
        value.setData(data);
        return value;
    }

    private void evictCaches() {
        rallyProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.CACHE_FIELD_MAPPING_MAP);
        rallyProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.CACHE_BOARD_META_DATA_MAP);
        rallyProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.CACHE_PROJECT_TOOL_CONFIG);
        rallyProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.CACHE_PROJECT_CONFIG_MAP);
        rallyProcessorCacheEvictor.evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.CACHE_ALL_PROJECT_CONFIG_MAP);
    }

    private FieldMapping mapFieldMapping(BoardMetadata boardMetadata, ProjectConfFieldMapping projectConfig) {
        FieldMapping fieldMapping = new FieldMapping();
        fieldMapping.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
        fieldMapping.setProjectToolConfigId(projectConfig.getProjectToolConfig().getId());
        fieldMapping.setCreatedDate(LocalDateTime.now());

        // Set Rally-specific field mappings based on memory
        fieldMapping.setRootCauseIdentifier(RallyConstants.CUSTOM_FIELD);
        fieldMapping.setJiradefecttype(Arrays.asList("Defect"));
        fieldMapping.setJiraIssueTypeNames(new String[]{"HierarchicalRequirement", "Defect", "Task"});
        fieldMapping.setStoryFirstStatus("Defined");
        
        // Map workflow statuses based on memory
        fieldMapping.setJiraStatusForDevelopmentKPI82(Arrays.asList("InDevelopment", "In Development"));
        fieldMapping.setJiraStatusForQaKPI82(Arrays.asList("Testing"));
        fieldMapping.setJiraDodKPI14(Arrays.asList("Done", "Accepted"));
        
        return fieldMapping;
    }
}
