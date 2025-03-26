package com.publicissapient.kpidashboard.rally.processor;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;

@ExtendWith(MockitoExtension.class)
public class RallyIssueAssigneeProcessorImplTest {

    @InjectMocks
    private RallyIssueAssigneeProcessorImpl rallyIssueAssigneeProcessor;

    @Mock
    private AssigneeDetailsRepository assigneeDetailsRepository;

    private ProjectConfFieldMapping projectConfig;
    private JiraIssue jiraIssue;
    private ProjectBasicConfig projectBasicConfig;

    @BeforeEach
    public void setup() {
        projectConfig = new ProjectConfFieldMapping();
        jiraIssue = new JiraIssue();
        projectBasicConfig = new ProjectBasicConfig();

        projectBasicConfig.setId(new ObjectId());
        projectBasicConfig.setProjectName("Test Project");
        projectConfig.setProjectBasicConfig(projectBasicConfig);
        projectConfig.setBasicProjectConfigId(projectBasicConfig.getId());
        projectConfig.setProjectName("Test Project");

        jiraIssue.setAssigneeId("USER123");
        jiraIssue.setAssigneeName("John Doe");
    }

    @Test
    public void testCreateAssigneeDetailsNewAssignee() {
        when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(anyString(), anyString()))
            .thenReturn(null);

        AssigneeDetails result = rallyIssueAssigneeProcessor.createAssigneeDetails(projectConfig, jiraIssue);

        assertNotNull(result);
        assertEquals(projectConfig.getBasicProjectConfigId().toString(), result.getBasicProjectConfigId());
        assertEquals(ProcessorConstants.JIRA, result.getSource());
        assertEquals(1, result.getAssignee().size());
        
        Assignee assignee = result.getAssignee().iterator().next();
        assertEquals(jiraIssue.getAssigneeId(), assignee.getAssigneeId());
        assertEquals(jiraIssue.getAssigneeName(), assignee.getAssigneeName());
    }

    @Test
    public void testCreateAssigneeDetailsExistingAssignee() {
        AssigneeDetails existingDetails = new AssigneeDetails();
        existingDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString());
        existingDetails.setSource(ProcessorConstants.JIRA);
        Set<Assignee> existingAssignees = new HashSet<>();
        existingAssignees.add(new Assignee("USER456", "Jane Smith"));
        existingDetails.setAssignee(existingAssignees);

        when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(anyString(), anyString()))
            .thenReturn(existingDetails);

        AssigneeDetails result = rallyIssueAssigneeProcessor.createAssigneeDetails(projectConfig, jiraIssue);

        assertNotNull(result);
        assertEquals(2, result.getAssignee().size());
        assertTrue(result.getAssignee().stream()
            .anyMatch(a -> a.getAssigneeId().equals(jiraIssue.getAssigneeId())));
    }

    @Test
    public void testCreateAssigneeDetailsExistingSameAssignee() {
        AssigneeDetails existingDetails = new AssigneeDetails();
        existingDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString());
        existingDetails.setSource(ProcessorConstants.JIRA);
        Set<Assignee> existingAssignees = new HashSet<>();
        existingAssignees.add(new Assignee(jiraIssue.getAssigneeId(), jiraIssue.getAssigneeName()));
        existingDetails.setAssignee(existingAssignees);

        when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(anyString(), anyString()))
            .thenReturn(existingDetails);

        AssigneeDetails result = rallyIssueAssigneeProcessor.createAssigneeDetails(projectConfig, jiraIssue);

        assertNull(result);
    }

    @Test
    public void testCreateAssigneeDetailsWithNoAssignee() {
        jiraIssue.setAssigneeId(null);
        jiraIssue.setAssigneeName(null);

        AssigneeDetails result = rallyIssueAssigneeProcessor.createAssigneeDetails(projectConfig, jiraIssue);

        assertNull(result);
    }

    @Test
    public void testCreateAssigneeDetailsWithAssigneeSequence() {
        projectBasicConfig.setSaveAssigneeDetails(false);
        when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(anyString(), anyString()))
            .thenReturn(null);

        AssigneeDetails result = rallyIssueAssigneeProcessor.createAssigneeDetails(projectConfig, jiraIssue);

        assertNotNull(result);
        assertEquals(2, result.getAssigneeSequence());
    }

    @Test
    public void testCreateAssigneeDetailsWithIncrementedAssigneeSequence() {
        projectBasicConfig.setSaveAssigneeDetails(false);
        
        AssigneeDetails existingDetails = new AssigneeDetails();
        existingDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString());
        existingDetails.setSource(ProcessorConstants.JIRA);
        existingDetails.setAssigneeSequence(2);
        Set<Assignee> existingAssignees = new HashSet<>();
        existingAssignees.add(new Assignee("USER456", "Jane Smith"));
        existingDetails.setAssignee(existingAssignees);

        when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(anyString(), anyString()))
            .thenReturn(existingDetails);

        AssigneeDetails result = rallyIssueAssigneeProcessor.createAssigneeDetails(projectConfig, jiraIssue);

        assertNotNull(result);
        assertEquals(3, result.getAssigneeSequence());
    }
}
