package com.publicissapient.kpidashboard.jira.tasklet;

import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.CreateMetadata;
import com.publicissapient.kpidashboard.jira.service.FetchKanbanReleaseData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetaDataTaskletTest {

    @Mock
    private FetchProjectConfiguration fetchProjectConfiguration;

    @Mock
    private JiraClient jiraClient;

    @Mock
    CreateMetadata createMetadata;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private JiraProcessorConfig jiraProcessorConfig;

    @InjectMocks
    private MetaDataTasklet metaDataTasklet;

    @Before
    public void setUp() {
        // Mock any setup or common behavior needed before each test
    }

    @Test
    public void testExecute() throws Exception {
        // Arrange
        ProjectConfFieldMapping projectConfFieldMapping= ProjectConfFieldMapping.builder().projectName("KnowHow").build();

        when(fetchProjectConfiguration.fetchConfiguration(null)).thenReturn(projectConfFieldMapping);

        ProcessorJiraRestClient client = mock(ProcessorJiraRestClient.class);
        when(jiraClient.getClient(projectConfFieldMapping,null)).thenReturn(client);
        when(jiraProcessorConfig.isFetchMetadata()).thenReturn(true);
        // Act
        RepeatStatus result = metaDataTasklet.execute(stepContribution, chunkContext);
        // Assert
        verify(createMetadata, times(1)).collectMetadata(projectConfFieldMapping, null);
        assertEquals(RepeatStatus.FINISHED, result);
    }


}
