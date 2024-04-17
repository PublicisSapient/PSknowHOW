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

import java.util.List;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.ProgressStatus;

/**
 * @author anisingh4
 */
public interface ProcessorExecutionTraceLogService {
	void save(ProcessorExecutionTraceLog processorExecutionTracelog);

	List<ProcessorExecutionTraceLog> getTraceLogs();

	List<ProcessorExecutionTraceLog> getTraceLogs(String processorName, String basicProjectConfigId);

	/**
	 * Save progress status in trace log
	 *
	 * @param basicProjectConfigId projectId
	 * @param processorName Name of the processor
	 * @param progressStatus Progress status of the processor
	 */
	void saveProgressStatusInTraceLog(String basicProjectConfigId, String processorName, ProgressStatus progressStatus);

	/**
	 * Set execution ongoing for processor
	 * @param processorName Name of Processor
	 * @param basicProjectConfigId ProjectId
	 * @param isOngoing Flag is processor execution ongoing
	 */
	void setExecutionOngoingForProcessor(String processorName,String basicProjectConfigId,boolean isOngoing);
}
