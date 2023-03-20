package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FetchProjectConfigurationImpl implements FetchProjectConfiguration{

    PSLogData psLogData = new PSLogData();

    @Autowired
    private ProjectBasicConfigRepository projectConfigRepository;

    @Autowired
    private FieldMappingRepository fieldMappingRepository;

    @Autowired
    private ProjectToolConfigRepository toolRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    FetchIssuesBasedOnJQL fetchIssuesBasedOnJQL;

    @Autowired
    CreateMetadata createMetadata;

    @Autowired
    JiraClient jiraClient;

    private ProcessorJiraRestClient client;

   @Override
    public Map<String, ProjectConfFieldMapping> fetchConfiguration(){
        List<FieldMapping> fieldMappingList = fieldMappingRepository.findAll();

        //getSelectedProjects fn.
        List<ProjectBasicConfig> allProjects = projectConfigRepository.findAll();

        psLogData.setTotalConfiguredProject(String.valueOf(CollectionUtils.emptyIfNull(allProjects).size()));

       List<String> selectedProjectsBasicIds = getProjectsBasicConfigIds();
        if (CollectionUtils.isEmpty(selectedProjectsBasicIds)) {
            return createProjectConfigMap(allProjects,fieldMappingList);
        }

        List<ProjectBasicConfig> projectBasicConfigs= CollectionUtils.emptyIfNull(allProjects).stream().filter(
                        projectBasicConfig -> selectedProjectsBasicIds.contains(projectBasicConfig.getId().toHexString()))
                .collect(Collectors.toList());
        log.info("ProjectBasicConfig: "+projectBasicConfigs);
        return createProjectConfigMap(projectBasicConfigs,fieldMappingList);
    }

    public List<String> getProjectsBasicConfigIds() {
       return Arrays.asList(
//               "63bfa0d5b7617e260763ca21"
//               "63c04dc7b7617e260763ca4e"
//               "64102db328f2534cd9d9b0e8"
               "641350b3280939593b19b941"
       );
    }

    private Map<String, ProjectConfFieldMapping> createProjectConfigMap(List<ProjectBasicConfig> projectConfigList,
                                                                       List<FieldMapping> fieldMappingList) {
        Map<String, ProjectConfFieldMapping> projectConfigMap = new HashMap<>();
        CollectionUtils.emptyIfNull(projectConfigList).forEach(projectConfig -> {
            ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
            try {
                BeanUtils.copyProperties(projectConfFieldMapping, projectConfig);
                projectConfFieldMapping.setProjectBasicConfig(projectConfig);
                projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
                projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
                projectConfFieldMapping.setJira(getJiraToolConfig(projectConfig.getId()));
                projectConfFieldMapping.setJiraToolConfigId(getToolConfigId(projectConfig.getId()));

            } catch (IllegalAccessException e) {
                log.error("Error while copying Project Config to ProjectConfFieldMapping", e);
            } catch (InvocationTargetException e) {
                log.error("Error while copying Project Config to ProjectConfFieldMapping invocation error", e);
            }
            CollectionUtils.emptyIfNull(fieldMappingList).stream()
                    .filter(fieldMapping -> projectConfig.getId().equals(fieldMapping.getBasicProjectConfigId()))
                    .forEach(fieldMapping -> projectConfFieldMapping.setFieldMapping(fieldMapping));
            projectConfigMap.putIfAbsent(projectConfig.getProjectName(), projectConfFieldMapping);
            try {
                for(Map.Entry<String, ProjectConfFieldMapping> entry : projectConfigMap.entrySet()) {
                    client = jiraClient.getClient(entry);
                    createMetadata.collectMetadata(entry.getValue(),client);
                    fetchIssuesBasedOnJQL.fetchIssues(entry,client);
                }
            } catch (InterruptedException | FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        );
        return projectConfigMap;
    }

    private JiraToolConfig getJiraToolConfig(ObjectId basicProjectConfigId) {
        JiraToolConfig toolObj = new JiraToolConfig();
        List<ProjectToolConfig> jiraDetails = toolRepository
                .findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA, basicProjectConfigId);
        if (CollectionUtils.isNotEmpty(jiraDetails)) {

            try {
                BeanUtils.copyProperties(toolObj, jiraDetails.get(0));
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Could not set JiraToolConfig", e);
            }
            if (jiraDetails.get(0).getConnectionId() != null) {
                Optional<Connection> conn = connectionRepository.findById(jiraDetails.get(0).getConnectionId());
                if (conn.isPresent()) {
                    toolObj.setConnection(conn);
                }
            }
        }
        return toolObj;
    }

    private ObjectId getToolConfigId(ObjectId basicProjectConfigId) {
        List<ProjectToolConfig> boardsDetails = toolRepository
                .findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA, basicProjectConfigId);
        return CollectionUtils.isNotEmpty(boardsDetails) ? boardsDetails.get(0).getId() : null;
    }

}
