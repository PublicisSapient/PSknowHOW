package com.publicissapient.kpidashboard.rally.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionBasicConfig;
import com.publicissapient.kpidashboard.rally.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.rally.constant.RallyConstants;
import com.publicissapient.kpidashboard.rally.model.RallyProcessor;
import com.publicissapient.kpidashboard.rally.repository.RallyProcessorRepository;
import com.publicissapient.kpidashboard.rally.service.OngoingExecutionsService;

@ExtendWith(MockitoExtension.class)
public class JobControllerTest {

    @InjectMocks
    private JobController jobController;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job fetchIssueScrumRqlJob;

    @Mock
    private Job fetchIssueSprintJob;

    @Mock
    private Job runMetaDataStep;

    @Mock
    private FetchProjectConfiguration fetchProjectConfiguration;

    @Mock
    private OngoingExecutionsService ongoingExecutionsService;

    @Mock
    private RallyProcessorRepository rallyProcessorRepository;

    private RallyProcessor rallyProcessor;
    private List<String> basicProjectConfigIds;

    @BeforeEach
    public void setup() {
        rallyProcessor = new RallyProcessor();
        rallyProcessor.setId(new ObjectId());
        rallyProcessor.setProcessorName(ProcessorConstants.JIRA);

        basicProjectConfigIds = Arrays.asList("proj1", "proj2");
    }

    @Test
    public void testStartScrumJqlJob() throws Exception {
        when(fetchProjectConfiguration.fetchBasicProjConfId(RallyConstants.RALLY, true, false))
            .thenReturn(basicProjectConfigIds);
        when(rallyProcessorRepository.findByProcessorName(ProcessorConstants.JIRA))
            .thenReturn(rallyProcessor);
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
            .thenReturn(new JobExecution(1L));

        ResponseEntity<String> response = jobController.startScrumJqlJob();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("job started for scrum JQL", response.getBody());
        verify(jobLauncher, times(basicProjectConfigIds.size())).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    public void testStartFetchSprintJob() throws Exception {
        String sprintId = "SPRINT-1";
        when(rallyProcessorRepository.findByProcessorName(ProcessorConstants.JIRA))
            .thenReturn(rallyProcessor);
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
            .thenReturn(new JobExecution(1L));

        ResponseEntity<String> response = jobController.startFetchSprintJob(sprintId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("job started for Sprint : " + sprintId, response.getBody());
    }

    @Test
    public void testStartProjectWiseIssueJobWhenExecutionInProgress() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        ProcessorExecutionBasicConfig config = new ProcessorExecutionBasicConfig();
        config.setProjectBasicConfigIds(Arrays.asList("proj1"));

        when(ongoingExecutionsService.isExecutionInProgress(anyString())).thenReturn(true);

        ResponseEntity<String> response = jobController.startProjectWiseIssueJob(config);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("A job is already running for the project", response.getBody());
        verify(jobLauncher, times(0)).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    public void testStartProjectWiseIssueJobWhenNoExecutionInProgress() throws Exception {
        ProcessorExecutionBasicConfig config = new ProcessorExecutionBasicConfig();
        config.setProjectBasicConfigIds(Arrays.asList("proj1"));

        when(ongoingExecutionsService.isExecutionInProgress(anyString())).thenReturn(false);
        when(rallyProcessorRepository.findByProcessorName(ProcessorConstants.JIRA))
            .thenReturn(rallyProcessor);
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
            .thenReturn(new JobExecution(1L));

        ResponseEntity<String> response = jobController.startProjectWiseIssueJob(config);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("job started for project : proj1", response.getBody());
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    public void testStartMetaDataJob() throws Exception {
        when(rallyProcessorRepository.findByProcessorName(ProcessorConstants.JIRA))
            .thenReturn(rallyProcessor);
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
            .thenReturn(new JobExecution(1L));

        ResponseEntity<String> response = jobController.startScrumJqlJob();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("job started for metadata", response.getBody());
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
    }
}
