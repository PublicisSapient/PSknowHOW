package com.publicissapient.kpidashboard.apis.jira.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.model.jira.IssueBacklog;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testng.Assert;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

/**
 * Test class for @{BacklogService}
 * @author dhachuda
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class BacklogServiceTest {
    @Mock
    private JiraIssueRepository jiraIssueRepository;

    @Mock
    private ConfigHelperService configHelperService;

    @InjectMocks
    BacklogService backlogService;


    private List<JiraIssue> storyList = new ArrayList<>();
    private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
    private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();

    private void setMockProjectConfig() {
        ProjectBasicConfig projectConfig = new ProjectBasicConfig();
        projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
        projectConfig.setProjectName("Scrum Project");
        projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
    }

    private void setMockFieldMapping() {
        FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
                .newInstance("/json/default/scrum_project_field_mappings.json");
        FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
        fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
        configHelperService.setFieldMappingMap(fieldMappingMap);
    }

    @Before
    public void setup() {
        setMockProjectConfig();
        setMockFieldMapping();
        JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
        storyList = jiraIssueDataFactory.getJiraIssues();
    }

    @Test
    public void testGetBackLogStory() {
        when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
        when(jiraIssueRepository.findIssuesBySprintAndType(any(), any())).thenReturn(storyList);
        List<IssueBacklog> backLogStory = backlogService.getBackLogStory(new ObjectId("6335363749794a18e8a4479b"));
        Assert.assertEquals(backLogStory.size(), storyList.size());
    }


}