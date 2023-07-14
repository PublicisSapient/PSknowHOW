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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;

/**
 * @author anisingh4
 */

@RunWith(MockitoJUnitRunner.class)
public class ProcessorExecutionTraceLogServiceImplTest {

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

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
		when(processorExecutionTraceLogRepository.findAll()).thenReturn(getRawData());
		List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService.getTraceLogs("Jira", null);
		assertEquals(2, traceLogs.size());
	}

	@Test
	public void getTraceLogs_WithProjectId() {
		when(processorExecutionTraceLogRepository.findAll()).thenReturn(getRawData());
		List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService.getTraceLogs(null,
				"624e9325cfac8f68ea7affa4");
		assertEquals(1, traceLogs.size());
	}

	@Test
	public void getTraceLogs_WithProjectIdAndProcessorName() {
		when(processorExecutionTraceLogRepository.findAll()).thenReturn(getRawData());
		List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService.getTraceLogs("Jira",
				"62177593904d2839684f5d68");
		assertEquals(1, traceLogs.size());
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

}