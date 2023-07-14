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

package com.publicissapient.kpidashboard.common.model.tracelog;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@Component
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PSLogData {
	private String projectName;
	private String projectKey;
	private String kanban;
	private String projectStartTime;
	private String projectEndTime;
	private String processorStartTime;
	private String processorEndTime;
	private String fieldMappingToDB;
	private String metaDataToDB;
	private String boardId;
	private String sprintId;
	private String totalFetchedIssues;
	private String totalSavedIssues;
	private String totalFetchedSprints;
	private String totalSavedSprints;
	private String epicIssuesFetched;
	private List<String> issueAndDesc;
	private String url;
	private String jql;
	private String timeTaken;
	private String executionStatus;
	private String userTimeZone;
	private List<String> sprintListFetched;
	private List<String> sprintListSaved;
	private List<String> epicListFetched;
	private List<String> projectVersion;
	private String executionEndedAt;
	private String executionStartedAt;
	private String lastSuccessfulRun;
	private String totalConfiguredProject;
	private List<String> lastSavedJiraIssueChangedDateByType;
	private String action;
	private String projectExecutionStatus;
	private String lastEnableAssigneeToggleState;
}
