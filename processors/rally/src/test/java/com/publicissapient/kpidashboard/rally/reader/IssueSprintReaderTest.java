package com.publicissapient.kpidashboard.rally.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.publicissapient.kpidashboard.rally.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.rally.config.RallyProcessorConfig;
import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.model.ReadData;
import com.publicissapient.kpidashboard.rally.service.FetchIssueSprint;

@ExtendWith(MockitoExtension.class)
public class IssueSprintReaderTest {

    @InjectMocks
    private IssueSprintReader issueSprintReader;

    @Mock
    private FetchProjectConfiguration fetchProjectConfiguration;

    @Mock
    private RallyProcessorConfig rallyProcessorConfig;

    @Mock
    private FetchIssueSprint fetchIssueSprint;

    private ProjectConfFieldMapping projectConfFieldMapping;
    private HierarchicalRequirement requirement1;
    private HierarchicalRequirement requirement2;
    private String sprintId;

    @BeforeEach
    public void setup() {
        sprintId = "SPRINT-1";
        projectConfFieldMapping = new ProjectConfFieldMapping();
        projectConfFieldMapping.setBasicProjectConfigId(new ObjectId());
        projectConfFieldMapping.setProjectName("Test Project");

        requirement1 = new HierarchicalRequirement();
        requirement1.setId("REQ-1");
        requirement2 = new HierarchicalRequirement();
        requirement2.setId("REQ-2");

        when(rallyProcessorConfig.getPageSize()).thenReturn(50);
    }

    @Test
    public void testReadWithNoConfiguration() throws Exception {
        when(fetchProjectConfiguration.fetchConfigurationBasedOnSprintId(anyString())).thenReturn(null);

        ReadData result = issueSprintReader.read();

        assertNull(result);
    }

    @Test
    public void testReadWithValidConfiguration() throws Exception {
        when(fetchProjectConfiguration.fetchConfigurationBasedOnSprintId(anyString())).thenReturn(projectConfFieldMapping);
        when(fetchIssueSprint.fetchIssuesSprintBasedOnJql(any(), anyInt(), anyString()))
            .thenReturn(Arrays.asList(requirement1, requirement2));

        issueSprintReader.initializeReader(sprintId);
        ReadData result = issueSprintReader.read();

        assertNotNull(result);
        assertEquals(requirement1, result.getHierarchicalRequirement());
        assertEquals(projectConfFieldMapping, result.getProjectConfFieldMapping());
        assertEquals(true, result.isSprintFetch());
    }

    @Test
    public void testReadWithEmptyResults() throws Exception {
        when(fetchProjectConfiguration.fetchConfigurationBasedOnSprintId(anyString())).thenReturn(projectConfFieldMapping);
        when(fetchIssueSprint.fetchIssuesSprintBasedOnJql(any(), anyInt(), anyString()))
            .thenReturn(Collections.emptyList());

        issueSprintReader.initializeReader(sprintId);
        ReadData result = issueSprintReader.read();

        assertNull(result);
    }

    @Test
    public void testReadWithMultiplePages() throws Exception {
        when(fetchProjectConfiguration.fetchConfigurationBasedOnSprintId(anyString())).thenReturn(projectConfFieldMapping);
        when(fetchIssueSprint.fetchIssuesSprintBasedOnJql(any(), eq(0), anyString()))
            .thenReturn(Arrays.asList(requirement1));
        when(fetchIssueSprint.fetchIssuesSprintBasedOnJql(any(), eq(50), anyString()))
            .thenReturn(Arrays.asList(requirement2));

        issueSprintReader.initializeReader(sprintId);
        
        ReadData result1 = issueSprintReader.read();
        assertNotNull(result1);
        assertEquals(requirement1, result1.getHierarchicalRequirement());

        ReadData result2 = issueSprintReader.read();
        assertNotNull(result2);
        assertEquals(requirement2, result2.getHierarchicalRequirement());

        ReadData result3 = issueSprintReader.read();
        assertNull(result3);
    }

    @Test
    public void testReadWithSinglePagePartialResults() throws Exception {
        when(fetchProjectConfiguration.fetchConfigurationBasedOnSprintId(anyString())).thenReturn(projectConfFieldMapping);
        when(fetchIssueSprint.fetchIssuesSprintBasedOnJql(any(), anyInt(), anyString()))
            .thenReturn(Arrays.asList(requirement1));

        issueSprintReader.initializeReader(sprintId);
        
        ReadData result1 = issueSprintReader.read();
        assertNotNull(result1);
        assertEquals(requirement1, result1.getHierarchicalRequirement());

        ReadData result2 = issueSprintReader.read();
        assertNull(result2);
    }

    @Test
    public void testReadWithNullIterator() throws Exception {
        when(fetchProjectConfiguration.fetchConfigurationBasedOnSprintId(anyString())).thenReturn(projectConfFieldMapping);
        when(fetchIssueSprint.fetchIssuesSprintBasedOnJql(any(), anyInt(), anyString()))
            .thenReturn(null);

        issueSprintReader.initializeReader(sprintId);
        ReadData result = issueSprintReader.read();

        assertNull(result);
    }

    @Test
    public void testReadWithProcessorId() throws Exception {
        String processorId = new ObjectId().toString();
        issueSprintReader.processorId = processorId;

        when(fetchProjectConfiguration.fetchConfigurationBasedOnSprintId(anyString())).thenReturn(projectConfFieldMapping);
        when(fetchIssueSprint.fetchIssuesSprintBasedOnJql(any(), anyInt(), anyString()))
            .thenReturn(Arrays.asList(requirement1));

        issueSprintReader.initializeReader(sprintId);
        ReadData result = issueSprintReader.read();

        assertNotNull(result);
        assertEquals(processorId, result.getProcessorId().toString());
    }
}
