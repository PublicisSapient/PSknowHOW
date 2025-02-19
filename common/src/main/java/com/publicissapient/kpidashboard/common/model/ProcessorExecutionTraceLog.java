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

package com.publicissapient.kpidashboard.common.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.application.ErrorDetail;
import com.publicissapient.kpidashboard.common.model.application.ProgressStatus;

import lombok.Data;

/**
 * @author anisingh4
 */
@Document("processor_execution_trace_log")
@Data
public class ProcessorExecutionTraceLog {
	private ObjectId id;
	private String processorName;
	private String basicProjectConfigId;

	/** time in milliseconds */
	private long executionStartedAt;

	/** time in milliseconds */
	private long executionEndedAt;

	private boolean executionSuccess;
	private String lastSuccessfulRun;
	private Map<String, LocalDateTime> lastSavedEntryUpdatedDateByType;
	private boolean lastEnableAssigneeToggleState;
	private String boardId;
	private String firstRunDate;
	private boolean dataMismatch;
	private boolean progressStats;
	private boolean executionOngoing;
	private String errorMessage;
	private String failureLog;
	private List<ProgressStatus> progressStatusList;
	// save any resource not found error
	private List<ErrorDetail> errorDetailList;
	private boolean executionWarning;
	private long executionResumesAt;
	private Object additionalInfo;
}
