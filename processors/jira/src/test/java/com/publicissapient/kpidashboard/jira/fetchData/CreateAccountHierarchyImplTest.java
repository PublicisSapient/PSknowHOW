package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.*;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.jira.data.*;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.apache.commons.beanutils.BeanUtils;
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
public class CreateAccountHierarchyImplTest {

    @Mock
    private HierarchyLevelService hierarchyLevelService;

    @Mock
    private AccountHierarchyRepository accountHierarchyRepository;

    @InjectMocks
    private CreateAccountHierarchyImpl createAccountHierarchy;

    List<HierarchyLevel> hierarchyLevelList;

    List<AccountHierarchy> accountHierarchyList;

    List<AccountHierarchy> accountHierarchies;

    List<ProjectToolConfig> projectToolConfigs;

    List<FieldMapping> fieldMappingList;

    Optional<Connection> connection;

    List<JiraIssue> jiraIssues;

    List<ProjectBasicConfig> projectConfigsList;

    @Before
    public void setup(){
        hierarchyLevelList=getMockHierarchyLevel();
        accountHierarchyList=getMockAccountHierarchy();
        accountHierarchies=getMockAccountHierarchyByLabelNameAndBasicProjectConfigId();
        projectToolConfigs=getMockProjectToolConfig();
        fieldMappingList=getMockFieldMapping();
        connection=getMockConnection();
        jiraIssues=getMockJiraIssue();
        projectConfigsList=getMockProjectConfig();
    }

    @Test
    public void createAccountHierarchy(){
        when(hierarchyLevelService.getFullHierarchyLevels(false)).thenReturn(hierarchyLevelList);
        when(accountHierarchyRepository.findAll()).thenReturn(accountHierarchyList);
        when(accountHierarchyRepository.findByLabelNameAndBasicProjectConfigId(any(),any())).thenReturn(accountHierarchies);
        Assert.assertEquals(2,createAccountHierarchy.createAccountHierarchy(jiraIssues,createProjectConfig()).size());
    }

    private List<HierarchyLevel> getMockHierarchyLevel() {
        HierachyLevelFactory hierarchyLevelFactory = HierachyLevelFactory
                .newInstance("/json/default/hierarchy_levels.json");
        return hierarchyLevelFactory.getHierarchyLevels();
    }

    private List<AccountHierarchy> getMockAccountHierarchy(){
        AccountHierarchiesDataFactory accountHierarchiesDataFactory= AccountHierarchiesDataFactory.newInstance("/json/default/account_hierarchy.json");
        return accountHierarchiesDataFactory.getAccountHierarchies();
    }

    private List<AccountHierarchy> getMockAccountHierarchyByLabelNameAndBasicProjectConfigId(){
        AccountHierarchiesDataFactory accountHierarchiesDataFactory= AccountHierarchiesDataFactory.newInstance("/json/default/account_hierarchy.json");
        return accountHierarchiesDataFactory.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT,"63c04dc7b7617e260763ca4e");
    }

    private List<JiraIssue> getMockJiraIssue() {
        JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory
                .newInstance("/json/default/jira_issues.json");
        return jiraIssueDataFactory.getJiraIssues();
    }

    private List<ProjectBasicConfig> getMockProjectConfig() {
        ProjectBasicConfigDataFactory projectConfigDataFactory = ProjectBasicConfigDataFactory
                .newInstance("/json/default/project_basic_configs.json");
        return projectConfigDataFactory.getProjectBasicConfigs();
    }

    private ProjectConfFieldMapping createProjectConfig(){
        ProjectConfFieldMapping projectConfFieldMapping=ProjectConfFieldMapping.builder().build();
        ProjectBasicConfig projectConfig=projectConfigsList.get(2);
        try {
            BeanUtils.copyProperties(projectConfFieldMapping, projectConfig);
        } catch (IllegalAccessException | InvocationTargetException e) {

        }
        projectConfFieldMapping.setProjectBasicConfig(projectConfig);
        projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
        projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
        projectConfFieldMapping.setJira(getJiraToolConfig());
        projectConfFieldMapping.setJiraToolConfigId(projectToolConfigs.get(0).getId());
        projectConfFieldMapping.setFieldMapping(fieldMappingList.get(1));

        return projectConfFieldMapping;
    }

    private JiraToolConfig getJiraToolConfig() {
        JiraToolConfig toolObj = new JiraToolConfig();
        try {
            BeanUtils.copyProperties(toolObj, projectToolConfigs.get(0));
        } catch (IllegalAccessException | InvocationTargetException e){

        }
        toolObj.setConnection(connection);
        return toolObj;
    }

    private  List<ProjectToolConfig> getMockProjectToolConfig() {
        ToolConfigDataFactory projectToolConfigDataFactory = ToolConfigDataFactory
                .newInstance("/json/default/project_tool_configs.json");
        return projectToolConfigDataFactory.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA,"63c04dc7b7617e260763ca4e");
    }

    private Optional<Connection> getMockConnection() {
        ConnectionsDataFactory connectionDataFactory = ConnectionsDataFactory
                .newInstance("/json/default/connections.json");
        return connectionDataFactory.findConnectionById("63f733a07af7ed784f088cd5");
    }

    private  List<FieldMapping> getMockFieldMapping() {
        FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
                .newInstance("/json/default/field_mapping.json");
        return fieldMappingDataFactory.getFieldMappings();
    }

}
