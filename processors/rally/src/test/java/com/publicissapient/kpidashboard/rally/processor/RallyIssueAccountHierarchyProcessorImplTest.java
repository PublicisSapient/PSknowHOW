package com.publicissapient.kpidashboard.rally.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterValue;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;

@ExtendWith(MockitoExtension.class)
public class RallyIssueAccountHierarchyProcessorImplTest {

    @InjectMocks
    private RallyIssueAccountHierarchyProcessorImpl rallyIssueAccountHierarchyProcessor;

    @Mock
    private HierarchyLevelService hierarchyLevelService;

    @Mock
    private ProjectHierarchyService projectHierarchyService;

    private ProjectConfFieldMapping projectConfig;
    private JiraIssue jiraIssue;
    private Set<SprintDetails> sprintDetailsSet;
    private ProjectBasicConfig projectBasicConfig;
    private List<HierarchyLevel> hierarchyLevels;

    @BeforeEach
    public void setup() {
        projectConfig = new ProjectConfFieldMapping();
        jiraIssue = new JiraIssue();
        sprintDetailsSet = new HashSet<>();
        projectBasicConfig = new ProjectBasicConfig();
        hierarchyLevels = new ArrayList<>();

        // Setup ProjectBasicConfig
        projectBasicConfig.setId(new ObjectId());
        projectBasicConfig.setProjectNodeId("TEST-NODE-1");
        projectConfig.setProjectBasicConfig(projectBasicConfig);
        projectConfig.setBasicProjectConfigId(projectBasicConfig.getId());
        projectConfig.setProjectName("Test Project");

        // Setup JiraIssue
        jiraIssue.setBasicProjectConfigId(projectBasicConfig.getId().toString());
        jiraIssue.setProjectName("Test Project");
        jiraIssue.setSprintName("Sprint 1");
        jiraIssue.setSprintBeginDate(LocalDateTime.now().toString());
        jiraIssue.setSprintEndDate(LocalDateTime.now().plusDays(14).toString());
        jiraIssue.setSprintID("SPRINT-1");

        // Setup HierarchyLevels
        HierarchyLevel sprintLevel = new HierarchyLevel();
        sprintLevel.setHierarchyLevelId(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);
        sprintLevel.setLevel(1);
        hierarchyLevels.add(sprintLevel);

        HierarchyLevel additionalLevel = new HierarchyLevel();
        additionalLevel.setHierarchyLevelId("ADDITIONAL-1");
        additionalLevel.setLevel(2);
        hierarchyLevels.add(additionalLevel);
    }

    @Test
    public void testCreateAccountHierarchyBasicCase() {
        when(hierarchyLevelService.getFullHierarchyLevels(anyBoolean())).thenReturn(hierarchyLevels);
        when(projectHierarchyService.getProjectHierarchyMapByConfig(anyString())).thenReturn(new java.util.HashMap<>());

        Set<ProjectHierarchy> result = rallyIssueAccountHierarchyProcessor.createAccountHierarchy(
            jiraIssue, projectConfig, sprintDetailsSet);

        assertNotNull(result);
        assertEquals(1, result.size());
        ProjectHierarchy hierarchy = result.iterator().next();
        assertEquals(jiraIssue.getSprintName(), hierarchy.getNodeName());
        assertEquals(jiraIssue.getSprintID(), hierarchy.getNodeId());
        assertEquals(projectBasicConfig.getProjectNodeId(), hierarchy.getParentId());
    }

    @Test
    public void testCreateAccountHierarchyWithSprintList() {
        SprintDetails sprintDetails = new SprintDetails();
        sprintDetails.setSprintName("Sprint 1");
        sprintDetails.setSprintID("SPRINT-1");
        sprintDetails.setStartDate(LocalDateTime.now().toString());
        sprintDetails.setEndDate(LocalDateTime.now().plusDays(14).toString());
        sprintDetails.setState("ACTIVE");
        sprintDetails.setBasicProjectConfigId(new ObjectId(jiraIssue.getBasicProjectConfigId()));
        sprintDetailsSet.add(sprintDetails);

        List<String> sprintIds = Arrays.asList("SPRINT-1");
        jiraIssue.setSprintIdList(sprintIds);

        when(hierarchyLevelService.getFullHierarchyLevels(anyBoolean())).thenReturn(hierarchyLevels);
        when(projectHierarchyService.getProjectHierarchyMapByConfig(anyString())).thenReturn(new java.util.HashMap<>());

        Set<ProjectHierarchy> result = rallyIssueAccountHierarchyProcessor.createAccountHierarchy(
            jiraIssue, projectConfig, sprintDetailsSet);

        assertNotNull(result);
        assertEquals(1, result.size());
        ProjectHierarchy hierarchy = result.iterator().next();
        assertEquals(sprintDetails.getSprintName(), hierarchy.getNodeName());
        assertEquals(sprintDetails.getSprintID(), hierarchy.getNodeId());
        assertEquals(sprintDetails.getState(), hierarchy.getSprintState());
    }

    @Test
    public void testCreateAccountHierarchyWithAdditionalFilters() {
        AdditionalFilterValue filterValue = new AdditionalFilterValue();
        filterValue.setValueId("VALUE-1");
        filterValue.setValue("Test Value");

        AdditionalFilter filter = new AdditionalFilter();
        filter.setFilterId("ADDITIONAL-1");
        filter.setFilterValues(Arrays.asList(filterValue));

        jiraIssue.setAdditionalFilters(Arrays.asList(filter));

        when(hierarchyLevelService.getFullHierarchyLevels(anyBoolean())).thenReturn(hierarchyLevels);
        when(projectHierarchyService.getProjectHierarchyMapByConfig(anyString())).thenReturn(new java.util.HashMap<>());

        Set<ProjectHierarchy> result = rallyIssueAccountHierarchyProcessor.createAccountHierarchy(
            jiraIssue, projectConfig, sprintDetailsSet);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(h -> h.getHierarchyLevelId().equals("ADDITIONAL-1")));
    }

    @Test
    public void testCreateAccountHierarchyWithExistingHierarchy() {
        ProjectHierarchy existingHierarchy = new ProjectHierarchy();
        existingHierarchy.setNodeId(jiraIssue.getSprintID());
        existingHierarchy.setParentId(projectBasicConfig.getProjectNodeId());
        existingHierarchy.setNodeName("Old Sprint Name");

        java.util.Map<String, List<ProjectHierarchy>> existingHierarchyMap = new java.util.HashMap<>();
        existingHierarchyMap.put(jiraIssue.getSprintID(), Arrays.asList(existingHierarchy));

        when(hierarchyLevelService.getFullHierarchyLevels(anyBoolean())).thenReturn(hierarchyLevels);
        when(projectHierarchyService.getProjectHierarchyMapByConfig(anyString())).thenReturn(existingHierarchyMap);

        Set<ProjectHierarchy> result = rallyIssueAccountHierarchyProcessor.createAccountHierarchy(
            jiraIssue, projectConfig, sprintDetailsSet);

        assertNotNull(result);
        assertEquals(1, result.size());
        ProjectHierarchy hierarchy = result.iterator().next();
        assertEquals(jiraIssue.getSprintName(), hierarchy.getNodeName());
    }
}
