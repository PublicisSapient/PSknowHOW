/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/


package com.publicissapient.kpidashboard.jira.tasklet;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
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

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SprintReportTaskletTest {

    @Mock
    FetchProjectConfiguration fetchProjectConfiguration;

    @Mock
    private FetchSprintReport fetchSprintReport;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    KerberosClient kerberosClient;

    @InjectMocks
    private SprintReportTasklet sprintReportTasklet;

    @Before
    public void setUp() {
        // Mock any setup or common behavior needed before each test
    }


    @Test
    public void testExecute() throws Exception {
        // Arrange
        ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().projectName("KnowHow").build();
        SprintDetails sprintDetails=new SprintDetails();
        sprintDetails.setSprintID("");
        sprintDetails.setOriginBoardId(Arrays.asList("xyz"));
        when(sprintRepository.findBySprintID(null)).thenReturn(sprintDetails);
        when(fetchSprintReport.getSprints(any(),anyString(),any())).thenReturn(Arrays.asList(sprintDetails));
        assertEquals(RepeatStatus.FINISHED,sprintReportTasklet.execute(stepContribution,chunkContext));
    }
}