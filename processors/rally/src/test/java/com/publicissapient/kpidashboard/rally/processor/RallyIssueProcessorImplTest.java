package com.publicissapient.kpidashboard.rally.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.rally.config.RallyProcessorConfig;
import com.publicissapient.kpidashboard.rally.helper.AdditionalFilterHelper;
import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;

@ExtendWith(MockitoExtension.class)
public class RallyIssueProcessorImplTest {

    @InjectMocks
    private RallyIssueProcessorImpl rallyIssueProcessor;

    @Mock
    private JiraIssueRepository jiraIssueRepository;

    @Mock
    private RallyProcessorConfig rallyProcessorConfig;

    @Mock
    private AdditionalFilterHelper additionalFilterHelper;

    @Mock
    private AssigneeDetailsRepository assigneeDetailsRepository;

    private ProjectConfFieldMapping projectConfig;
    private HierarchicalRequirement hierarchicalRequirement;
    private FieldMapping fieldMapping;
    private ObjectId processorId;
    private String boardId;

    @BeforeEach
    public void setup() {
        projectConfig = new ProjectConfFieldMapping();
        hierarchicalRequirement = new HierarchicalRequirement();
        fieldMapping = new FieldMapping();
        processorId = new ObjectId();
        boardId = "TEST-BOARD-1";

        projectConfig.setBasicProjectConfigId(new ObjectId());
        projectConfig.setFieldMapping(fieldMapping);

        hierarchicalRequirement.setObjectID("12345");
        hierarchicalRequirement.setFormattedID("US1234");
        hierarchicalRequirement.setName("Test User Story");
        hierarchicalRequirement.setScheduleState("Defined");
        hierarchicalRequirement.setPlanEstimate(8.0);
    }

    @Test
    public void testConvertToJiraIssueNewIssue() throws Exception {
        when(jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(anyString(), anyString())).thenReturn(null);

        JiraIssue result = rallyIssueProcessor.convertToJiraIssue(hierarchicalRequirement, projectConfig, boardId, processorId);

        assertNotNull(result);
        assertEquals(hierarchicalRequirement.getObjectID(), result.getNumber());
        assertEquals(hierarchicalRequirement.getFormattedID(), result.getNumber());
        assertEquals(hierarchicalRequirement.getName(), result.getName());
        assertEquals(hierarchicalRequirement.getScheduleState(), result.getJiraStatus());
        assertEquals(hierarchicalRequirement.getPlanEstimate(), result.getEstimate());
    }

    @Test
    public void testConvertToJiraIssueExistingIssue() throws Exception {
        JiraIssue existingIssue = new JiraIssue();
        existingIssue.setNumber(hierarchicalRequirement.getObjectID());
        existingIssue.setSprintID(String.valueOf(new HashSet<>()));
        existingIssue.setDefectStoryID(new HashSet<>());

        when(jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(anyString(), anyString())).thenReturn(existingIssue);

        JiraIssue result = rallyIssueProcessor.convertToJiraIssue(hierarchicalRequirement, projectConfig, boardId, processorId);

        assertNotNull(result);
        assertEquals(existingIssue.getNumber(), result.getNumber());
    }

    @Test
    public void testConvertToJiraIssueWithNullHierarchicalRequirement() throws Exception {
        JiraIssue result = rallyIssueProcessor.convertToJiraIssue(null, projectConfig, boardId, processorId);

        assertNull(result);
    }

    @Test
    public void testConvertToJiraIssueWithCustomFields() throws Exception {
        fieldMapping.setSprintName("c_SprintName");
        fieldMapping.setEpicLink("c_EpicLink");

        when(jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(anyString(), anyString())).thenReturn(null);

        JiraIssue result = rallyIssueProcessor.convertToJiraIssue(hierarchicalRequirement, projectConfig, boardId, processorId);

        assertNotNull(result);
        assertEquals(hierarchicalRequirement.getObjectID(), result.getNumber());
    }

    @Test
    public void testConvertToJiraIssueWithDefects() throws Exception {
        Set<String> defectIds = new HashSet<>();
        defectIds.add("DE1234");

        when(jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(anyString(), anyString())).thenReturn(null);

        JiraIssue result = rallyIssueProcessor.convertToJiraIssue(hierarchicalRequirement, projectConfig, boardId, processorId);

        assertNotNull(result);
        assertEquals(defectIds, result.getDefectStoryID());
    }

    @Test
    public void testConvertToJiraIssueWithOwner() throws Exception {

        when(jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(anyString(), anyString())).thenReturn(null);

        JiraIssue result = rallyIssueProcessor.convertToJiraIssue(hierarchicalRequirement, projectConfig, boardId, processorId);

        assertNotNull(result);
        assertEquals("John Doe", result.getAssigneeName());
    }
}
