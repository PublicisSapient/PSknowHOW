package com.publicissapient.kpidashboard.jira.tasklet;

import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.client.JiraClient;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.FetchScrumReleaseData;
import com.publicissapient.kpidashboard.jira.service.FetchSprintReport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SprintScrumBoardTaskletTest {

    @Mock
    private FetchProjectConfiguration fetchProjectConfiguration;

    @Mock
    private JiraClient jiraClient;

    @Mock
    FetchSprintReport fetchSprintReport;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @InjectMocks
    private SprintScrumBoardTasklet sprintScrumBoardTasklet;

    @Before
    public void setUp() {
        // Mock any setup or common behavior needed before each test
    }



    @Test
    public void testExecute() throws Exception {
        BoardDetails boardDetails=new BoardDetails();
        boardDetails.setBoardId("xyz");
        boardDetails.setBoardName("knowhow");
        boardDetails.setProjectKey("abc");
        ProjectToolConfig projectToolConfig= ProjectToolConfig.builder().boards(Arrays.asList(boardDetails)).build();
        // Arrange
        ProjectConfFieldMapping projectConfFieldMapping= ProjectConfFieldMapping.builder().projectName("KnowHow").projectToolConfig(projectToolConfig).build();

        when(fetchProjectConfiguration.fetchConfiguration(null)).thenReturn(projectConfFieldMapping);

        when(jiraClient.getClient(projectConfFieldMapping, null)).thenReturn(null);

        // Act
        RepeatStatus result = sprintScrumBoardTasklet.execute(stepContribution, chunkContext);

        // Assert
        verify(fetchSprintReport, times(1)).createSprintDetailBasedOnBoard(projectConfFieldMapping, null, boardDetails);
        assertEquals(RepeatStatus.FINISHED, result);
    }


}
