/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.common.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.dto.ProcessorExecutionTraceLogDTO;
import com.publicissapient.kpidashboard.common.model.application.dto.SprintRefreshLogDTO;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

/**
 * @author anisingh4
 */

@ExtendWith(SpringExtension.class)
public class ProcessorExecutionTraceLogServiceImplTest {

    @Mock
    private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

    @Mock
    private AzureSprintReportLogService azureSprintReportLogService;

    @InjectMocks
    private ProcessorExecutionTraceLogServiceImpl processorExecutionTraceLogService;

    @Test
    public void save() {
        ProcessorExecutionTraceLog tLog3 = new ProcessorExecutionTraceLog();
        tLog3.setProcessorName("Jira");
        tLog3.setBasicProjectConfigId("62177593904d2839684f5d68");
        tLog3.setExecutionStartedAt(1649241000559L);
        tLog3.setExecutionEndedAt(1649241013929L);
        tLog3.setExecutionSuccess(true);

        when(processorExecutionTraceLogRepository.save(any(ProcessorExecutionTraceLog.class))).thenReturn(tLog3);
        when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigId(anyString(), anyString()))
                .thenReturn(Optional.empty());

        processorExecutionTraceLogService.save(tLog3);

        verify(processorExecutionTraceLogRepository, times(1)).save(tLog3);
    }

    @Test
    public void getTraceLogs() {
        when(processorExecutionTraceLogRepository.findAll()).thenReturn(getRawData());
        List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService.getTraceLogs();
        assertEquals(4, traceLogs.size());
    }

    @Test
    public void getTraceLogs_WithProcessorName() {
        when(processorExecutionTraceLogRepository.findByProcessorName(anyString())).thenReturn(getRawData());
        List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService.getTraceLogs("Jira", null);
        assertEquals(4, traceLogs.size());
    }

    @Test
    public void getTraceLogs_WithProjectId() {
        when(processorExecutionTraceLogRepository.findByBasicProjectConfigId(anyString())).thenReturn(getRawData());
        List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService.getTraceLogs(null,
                "624e9325cfac8f68ea7affa4");
        assertEquals(4, traceLogs.size());
    }

    @Test
    public void getTraceLogs_WithProjectIdAndProcessorName() {
        when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigIdIn(anyString(),anyList())).thenReturn(getRawData());
        List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService.getTraceLogs("Jira",
                "62177593904d2839684f5d68");
        assertEquals(0, traceLogs.size());
    }

    @Test
    public void getTraceLogs_WithProjectIdAndProcessorName2() {
        when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigIdIn(anyString(),anyList())).thenReturn(getRawData());
        List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService.getTraceLogs("Azure",
                "62177593904d2839684f5d68");
        assertEquals(4, traceLogs.size());
    }

    @Test
    public void getTraceLogs_WithNoProjectIdAndProcessorName() {
        when(processorExecutionTraceLogRepository.findAll()).thenReturn(getRawData());
        List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService.getTraceLogs(null, null);
        assertEquals(4, traceLogs.size());
    }

    private List<ProcessorExecutionTraceLog> getRawData() {
        List<ProcessorExecutionTraceLog> traceLogs = new ArrayList<>();

        ProcessorExecutionTraceLog tLog1 = new ProcessorExecutionTraceLog();
        tLog1.setProcessorName("Sonar");
        tLog1.setBasicProjectConfigId("62177593904d2839684f5d68");
        tLog1.setExecutionStartedAt(1649318130000L);
        tLog1.setExecutionEndedAt(1649318149000L);
        tLog1.setExecutionSuccess(true);

        ProcessorExecutionTraceLog tLog2 = new ProcessorExecutionTraceLog();
        tLog2.setProcessorName("Sonar");
        tLog2.setBasicProjectConfigId("624e9325cfac8f68ea7affa4");
        tLog2.setExecutionStartedAt(1649318041000L);
        tLog2.setExecutionEndedAt(1649318076000L);
        tLog2.setExecutionSuccess(true);

        ProcessorExecutionTraceLog tLog3 = new ProcessorExecutionTraceLog();
        tLog3.setProcessorName("Jira");
        tLog3.setBasicProjectConfigId("62177593904d2839684f5d68");
        tLog3.setExecutionStartedAt(1649241000559L);
        tLog3.setExecutionEndedAt(1649241013929L);
        tLog3.setExecutionSuccess(true);

        ProcessorExecutionTraceLog tLog4 = new ProcessorExecutionTraceLog();
        tLog4.setProcessorName("Jira");
        tLog4.setBasicProjectConfigId("6226d74b8040a45ecc509567");
        tLog4.setExecutionStartedAt(1649241000764L);
        tLog4.setExecutionEndedAt(1649241013099L);
        tLog4.setExecutionSuccess(true);

        traceLogs.add(tLog1);
        traceLogs.add(tLog2);
        traceLogs.add(tLog3);
        traceLogs.add(tLog4);

        return traceLogs;
    }

	@Test
	public void testSave_ExistingTraceLogPresent() {
		ProcessorExecutionTraceLog existingLog = new ProcessorExecutionTraceLog();

		ProcessorExecutionTraceLog newLog = new ProcessorExecutionTraceLog();
		newLog.setProcessorName("Jira");
		newLog.setBasicProjectConfigId("62177593904d2839684f5d68");

		when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigId(anyString(), anyString()))
				.thenReturn(Optional.of(existingLog));

		processorExecutionTraceLogService.save(newLog);

		Assertions.assertEquals(existingLog.getLastSavedEntryUpdatedDateByType(), newLog.getLastSavedEntryUpdatedDateByType());

		verify(processorExecutionTraceLogRepository, times(1)).save(newLog);
	}

    @Test
    public void getTraceLogDTOs() {
        // Mock data
        ProcessorExecutionTraceLog traceLog = new ProcessorExecutionTraceLog();
        traceLog.setProcessorName("Azure");
        traceLog.setBasicProjectConfigId("62177593904d2839684f5d68");
        traceLog.setExecutionEndedAt(1649241013929L);
        traceLog.setExecutionSuccess(true);

        List<ProcessorExecutionTraceLog> traceLogs = List.of(traceLog);
        when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigIdIn(anyString(), anyList()))
                .thenReturn(traceLogs);

        // Mock AzureSprintReportLogService
        when(azureSprintReportLogService.getSprintRefreshLogs(any(ObjectId.class)))
                .thenReturn(List.of(new SprintRefreshLogDTO()));

        // Call the method
        List<ProcessorExecutionTraceLogDTO> traceLogDTOs = processorExecutionTraceLogService.getTraceLogDTOs("Azure", "62177593904d2839684f5d68");

        // Verify the result
        assertEquals(1, traceLogDTOs.size());
        assertEquals("Azure", traceLogDTOs.get(0).getProcessorName());
        assertEquals("62177593904d2839684f5d68", traceLogDTOs.get(0).getBasicProjectConfigId());
    }

}