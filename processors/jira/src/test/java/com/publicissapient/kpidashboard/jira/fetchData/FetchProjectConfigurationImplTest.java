package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.jira.data.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.jira.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.data.ProjectBasicConfigDataFactory;

import com.publicissapient.kpidashboard.jira.data.ToolConfigDataFactory;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FetchProjectConfigurationImplTest {

    @Mock
    private ProjectBasicConfigRepository projectConfigRepository;

    @Mock
    private FieldMappingRepository fieldMappingRepository;

    @Mock
    private ProjectToolConfigRepository toolRepository;

    @Mock
    private ConnectionRepository connectionRepository;

    @InjectMocks
    private FetchProjectConfigurationImpl fetchProjectConfiguration;

    List<FieldMapping> fieldMappingList;
    List<ProjectBasicConfig> projectConfigsList;
    List<ProjectToolConfig> projectToolConfigs;

    @Before
    public void setup(){
        fieldMappingList=getMockFieldMapping();
        projectConfigsList=getMockProjectConfig();
        projectToolConfigs=getMockProjectToolConfig();
    }

    @Test
    public void fetchConfiguration(){
        when(fieldMappingRepository.findAll()).thenReturn(fieldMappingList);
        when(projectConfigRepository.findAll()).thenReturn(projectConfigsList);
        when(toolRepository.findByToolNameAndBasicProjectConfigId(any(), any())).thenReturn(projectToolConfigs);
        when(connectionRepository.findById(any())).thenReturn(getMockConnection());
        Assert.assertEquals(createProjectConfigMap(),fetchProjectConfiguration.fetchConfiguration());
    }

    @Test
    public void fetchConfiguration_2(){
        when(fieldMappingRepository.findAll()).thenReturn(Collections.<FieldMapping>emptyList());
        when(projectConfigRepository.findAll()).thenReturn(projectConfigsList);
        when(toolRepository.findByToolNameAndBasicProjectConfigId(any(), any())).thenReturn(projectToolConfigs);
        when(connectionRepository.findById(any())).thenReturn(getMockConnection());
        Assert.assertNotEquals(createProjectConfigMap(),fetchProjectConfiguration.fetchConfiguration());
    }

    private  List<FieldMapping> getMockFieldMapping() {
        FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
                .newInstance("/json/default/field_mapping.json");
        return fieldMappingDataFactory.getFieldMappings();
    }

    private List<ProjectBasicConfig> getMockProjectConfig() {
        ProjectBasicConfigDataFactory projectConfigDataFactory = ProjectBasicConfigDataFactory
                .newInstance("/json/default/project_basic_configs.json");
        return projectConfigDataFactory.getProjectBasicConfigs();
    }

    private  List<ProjectToolConfig> getMockProjectToolConfig() {
        ToolConfigDataFactory projectToolConfigDataFactory = ToolConfigDataFactory
                .newInstance("/json/default/project_tool_configs.json");
        return projectToolConfigDataFactory.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA,"63bfa0d5b7617e260763ca21");
    }

    private Map<String, ProjectConfFieldMapping> createProjectConfigMap(){
        Map<String, ProjectConfFieldMapping> projectConfigMap = new HashMap<>();
        ProjectConfFieldMapping projectConfFieldMapping=ProjectConfFieldMapping.builder().build();
        ProjectBasicConfig projectConfig=projectConfigsList.get(1);
        try {
            BeanUtils.copyProperties(projectConfFieldMapping, projectConfig);
        } catch (IllegalAccessException | InvocationTargetException e) {

        }
        projectConfFieldMapping.setProjectBasicConfig(projectConfig);
        projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
        projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
        projectConfFieldMapping.setJira(getJiraToolConfig());
        projectConfFieldMapping.setJiraToolConfigId(getMockProjectToolConfig().get(0).getId());
        projectConfFieldMapping.setFieldMapping(fieldMappingList.get(1));
        projectConfigMap.put(projectConfig.getProjectName(), projectConfFieldMapping);
        return projectConfigMap;
    }

    private JiraToolConfig getJiraToolConfig() {
        JiraToolConfig toolObj = new JiraToolConfig();
        try {
            BeanUtils.copyProperties(toolObj, projectToolConfigs.get(0));
        } catch (IllegalAccessException | InvocationTargetException e){

        }
        Optional<Connection> conn = getMockConnection();
        toolObj.setConnection(conn);
        return toolObj;
    }

    private Optional<Connection> getMockConnection() {
        ConnectionsDataFactory connectionDataFactory = ConnectionsDataFactory
                .newInstance("/json/default/connections.json");
        return connectionDataFactory.findConnectionById("5fd99f7bc8b51a7b55aec836");
    }

    @After
    public void freeResources(){
        fieldMappingList=null;
        projectConfigsList=null;
        projectToolConfigs=null;
    }
}
