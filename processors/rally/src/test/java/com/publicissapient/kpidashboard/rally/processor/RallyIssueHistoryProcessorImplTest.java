package com.publicissapient.kpidashboard.rally.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;

@ExtendWith(MockitoExtension.class)
public class RallyIssueHistoryProcessorImplTest {

    @InjectMocks
    private RallyIssueHistoryProcessorImpl rallyIssueHistoryProcessor;

    @Mock
    private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

    private ProjectConfFieldMapping projectConfig;
    private HierarchicalRequirement hierarchicalRequirement;
    private JiraIssue jiraIssue;
    private FieldMapping fieldMapping;
    private Map<String, Object> fields;

    @BeforeEach
    public void setup() {
        projectConfig = new ProjectConfFieldMapping();
        hierarchicalRequirement = new HierarchicalRequirement();
        jiraIssue = new JiraIssue();
        fieldMapping = new FieldMapping();
        fields = new HashMap<>();

        projectConfig.setBasicProjectConfigId(new ObjectId());
        projectConfig.setFieldMapping(fieldMapping);
        projectConfig.setProjectName("Test Project");

        hierarchicalRequirement.setObjectID("12345");
        hierarchicalRequirement.setFormattedID("US1234");
        hierarchicalRequirement.setName("Test User Story");
        hierarchicalRequirement.setDescription("Test Description");
        hierarchicalRequirement.setState("In-Progress");
        hierarchicalRequirement.setScheduleState("Defined");
        hierarchicalRequirement.setCreationDate(DateTime.now().toString());

        jiraIssue.setNumber("12345");
        jiraIssue.setName("Test User Story");
        jiraIssue.setTypeName("Story");
        jiraIssue.setProjectName("Test Project");
        jiraIssue.setProjectKey("TEST");
        jiraIssue.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString());
    }

    @Test
    public void testConvertToJiraIssueHistoryNewHistory() {
        when(jiraIssueCustomHistoryRepository.findByStoryIDAndBasicProjectConfigId(anyString(), anyString()))
            .thenReturn(null);

        JiraIssueCustomHistory result = rallyIssueHistoryProcessor.convertToJiraIssueHistory(
            hierarchicalRequirement, projectConfig, jiraIssue);

        assertNotNull(result);
        assertEquals(jiraIssue.getNumber(), result.getStoryID());
        assertEquals(jiraIssue.getProjectName(), result.getProjectID());
        assertEquals(jiraIssue.getProjectKey(), result.getProjectKey());
        assertEquals(jiraIssue.getTypeName(), result.getStoryType());
        assertEquals(jiraIssue.getName(), result.getDescription());
    }

    @Test
    public void testConvertToJiraIssueHistoryExistingHistory() {
        JiraIssueCustomHistory existingHistory = new JiraIssueCustomHistory();
        existingHistory.setStoryID(jiraIssue.getNumber());
        existingHistory.setProjectID(jiraIssue.getProjectName());

        when(jiraIssueCustomHistoryRepository.findByStoryIDAndBasicProjectConfigId(anyString(), anyString()))
            .thenReturn(existingHistory);

        JiraIssueCustomHistory result = rallyIssueHistoryProcessor.convertToJiraIssueHistory(
            hierarchicalRequirement, projectConfig, jiraIssue);

        assertNotNull(result);
        assertEquals(existingHistory.getStoryID(), result.getStoryID());
        assertEquals(existingHistory.getProjectID(), result.getProjectID());
    }

    @Test
    public void testConvertToJiraIssueHistoryWithDefectType() {
        jiraIssue.setTypeName(NormalizedJira.DEFECT_TYPE.getValue());
        Set<String> defectStoryIds = new HashSet<>();
        defectStoryIds.add("STORY-123");
        jiraIssue.setDefectStoryID(defectStoryIds);

        when(jiraIssueCustomHistoryRepository.findByStoryIDAndBasicProjectConfigId(anyString(), anyString()))
            .thenReturn(null);

        JiraIssueCustomHistory result = rallyIssueHistoryProcessor.convertToJiraIssueHistory(
            hierarchicalRequirement, projectConfig, jiraIssue);

        assertNotNull(result);
        assertEquals(defectStoryIds, result.getDefectStoryID());
    }

    @Test
    public void testConvertToJiraIssueHistoryWithEstimates() {
        jiraIssue.setEstimate(8.0);
        jiraIssue.setBufferedEstimateTime(10.0);

        when(jiraIssueCustomHistoryRepository.findByStoryIDAndBasicProjectConfigId(anyString(), anyString()))
            .thenReturn(null);

        JiraIssueCustomHistory result = rallyIssueHistoryProcessor.convertToJiraIssueHistory(
            hierarchicalRequirement, projectConfig, jiraIssue);

        assertNotNull(result);
        assertEquals(jiraIssue.getEstimate(), result.getEstimate());
        assertEquals(jiraIssue.getBufferedEstimateTime(), result.getBufferedEstimateTime());
    }

    @Test
    public void testConvertToJiraIssueHistoryWithDevicePlatform() {
        jiraIssue.setDevicePlatform("iOS");

        when(jiraIssueCustomHistoryRepository.findByStoryIDAndBasicProjectConfigId(anyString(), anyString()))
            .thenReturn(null);

        JiraIssueCustomHistory result = rallyIssueHistoryProcessor.convertToJiraIssueHistory(
            hierarchicalRequirement, projectConfig, jiraIssue);

        assertNotNull(result);
        assertEquals(jiraIssue.getDevicePlatform(), result.getDevicePlatform());
    }
}
