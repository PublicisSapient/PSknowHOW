package com.publicissapient.kpidashboard.rally.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.rally.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.rally.config.RallyProcessorConfig;
import com.publicissapient.kpidashboard.rally.constant.RallyConstants;
import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.model.ReadData;
import com.publicissapient.kpidashboard.rally.service.RallyCommonService;

@ExtendWith(MockitoExtension.class)
public class IssueRqlReaderTest {

    @InjectMocks
    private IssueRqlReader issueRqlReader;

    @Mock
    private FetchProjectConfiguration fetchProjectConfiguration;

    @Mock
    private RallyCommonService rallyCommonService;

    @Mock
    private RallyProcessorConfig rallyProcessorConfig;

    @Mock
    private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

    private ProjectConfFieldMapping projectConfFieldMapping;
    private HierarchicalRequirement requirement1;
    private HierarchicalRequirement requirement2;

    @BeforeEach
    public void setup() {
        projectConfFieldMapping = new ProjectConfFieldMapping();
        projectConfFieldMapping.setBasicProjectConfigId(new ObjectId());
        projectConfFieldMapping.setProjectName("Test Project");


        when(rallyProcessorConfig.getPageSize()).thenReturn(50);
        when(rallyProcessorConfig.getPrevMonthCountToFetchData()).thenReturn(3);
    }

    @Test
    public void testReadWithNoConfiguration() throws Exception {
        when(fetchProjectConfiguration.fetchConfiguration(anyString())).thenReturn(null);

        ReadData result = issueRqlReader.read();

        assertNull(result);
    }

    @Test
    public void testReadWithValidConfiguration() throws Exception {
        when(fetchProjectConfiguration.fetchConfiguration(anyString())).thenReturn(projectConfFieldMapping);
        when(rallyCommonService.fetchIssuesBasedOnJql(any(), anyInt(), anyString()))
            .thenReturn(Arrays.asList(requirement1, requirement2));

        issueRqlReader.initializeReader("TEST-1");
        ReadData result = issueRqlReader.read();

        assertNotNull(result);
        assertEquals(requirement1, result.getHierarchicalRequirement());
        assertEquals(projectConfFieldMapping, result.getProjectConfFieldMapping());
        assertEquals(false, result.isSprintFetch());
    }

    @Test
    public void testReadWithTraceLog() throws Exception {
        ProcessorExecutionTraceLog traceLog = new ProcessorExecutionTraceLog();
        traceLog.setLastSuccessfulRun("2023-01-01");
        
        when(fetchProjectConfiguration.fetchConfiguration(anyString())).thenReturn(projectConfFieldMapping);
        when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdAndProgressStatsFalse(
                eq(RallyConstants.RALLY), anyString()))
            .thenReturn(Arrays.asList(traceLog));
        when(rallyCommonService.fetchIssuesBasedOnJql(any(), anyInt(), eq("2023-01-01")))
            .thenReturn(Arrays.asList(requirement1));

        issueRqlReader.initializeReader("TEST-1");
        ReadData result = issueRqlReader.read();

        assertNotNull(result);
        assertEquals(requirement1, result.getHierarchicalRequirement());
    }

    @Test
    public void testReadWithEmptyResults() throws Exception {
        when(fetchProjectConfiguration.fetchConfiguration(anyString())).thenReturn(projectConfFieldMapping);
        when(rallyCommonService.fetchIssuesBasedOnJql(any(), anyInt(), anyString()))
            .thenReturn(Arrays.asList());

        issueRqlReader.initializeReader("TEST-1");
        ReadData result = issueRqlReader.read();

        assertNull(result);
    }

    @Test
    public void testReadWithMultiplePages() throws Exception {
        List<HierarchicalRequirement> page1 = Arrays.asList(requirement1);
        List<HierarchicalRequirement> page2 = Arrays.asList(requirement2);

        when(fetchProjectConfiguration.fetchConfiguration(anyString())).thenReturn(projectConfFieldMapping);
        when(rallyCommonService.fetchIssuesBasedOnJql(any(), eq(0), anyString()))
            .thenReturn(page1);
        when(rallyCommonService.fetchIssuesBasedOnJql(any(), eq(50), anyString()))
            .thenReturn(page2);

        issueRqlReader.initializeReader("TEST-1");
        
        ReadData result1 = issueRqlReader.read();
        assertNotNull(result1);
        assertEquals(requirement1, result1.getHierarchicalRequirement());

        ReadData result2 = issueRqlReader.read();
        assertNotNull(result2);
        assertEquals(requirement2, result2.getHierarchicalRequirement());
    }

    @Test
    public void testReadWithNoTraceLog() throws Exception {
        when(fetchProjectConfiguration.fetchConfiguration(anyString())).thenReturn(projectConfFieldMapping);
        when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdAndProgressStatsFalse(
                anyString(), anyString()))
            .thenReturn(Arrays.asList());
        when(rallyCommonService.fetchIssuesBasedOnJql(any(), anyInt(), anyString()))
            .thenReturn(Arrays.asList(requirement1));

        issueRqlReader.initializeReader("TEST-1");
        ReadData result = issueRqlReader.read();

        assertNotNull(result);
        assertEquals(requirement1, result.getHierarchicalRequirement());
    }
}
