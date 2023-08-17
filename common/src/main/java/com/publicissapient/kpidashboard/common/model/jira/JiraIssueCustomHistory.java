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

package com.publicissapient.kpidashboard.common.model.jira;//NOPMD

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * A self-contained, independently deployable piece of the larger application.
 * Each component of an application has a different source repo, build job,
 * deploy job, etc.
 */
@SuppressWarnings("PMD.TooManyFields")
@Getter
@Setter
@Data
@Document(collection = "jira_issue_custom_history")
public class JiraIssueCustomHistory extends BasicModel {
	@Indexed
	private String projectID;
	@Indexed
	private String storyID;
	@Indexed
	private String storyType;

	private Set<String> defectStoryID;

	private String estimate;

	private Integer bufferedEstimateTime; // buffered estimate in days

	private DateTime createdDate;

	/**
	 * Device Platform (iOS/Android/Desktop)
	 */
	private String devicePlatform;
	private String projectKey;
	private String projectComponentId;

	private String developerId;
	private String developerName;
	private String qaId;
	private String qaName;

	private String buildId;
	private String buildNumber;

	private String projectName;
	private String basicProjectConfigId;

	private List<JiraHistoryChangeLog> statusUpdationLog = new ArrayList<>();
	private List<JiraHistoryChangeLog> assigneeUpdationLog = new ArrayList<>();
	private List<JiraHistoryChangeLog> priorityUpdationLog = new ArrayList<>();
	private List<JiraHistoryChangeLog> fixVersionUpdationLog = new ArrayList<>();
	private List<JiraHistoryChangeLog> labelUpdationLog = new ArrayList<>();
	private List<JiraHistoryChangeLog> dueDateUpdationLog = new ArrayList<>();
	private List<JiraHistoryChangeLog> devDueDateUpdationLog = new ArrayList<>();
	private List<JiraHistoryChangeLog> sprintUpdationLog = new ArrayList<>();
	private List<JiraHistoryChangeLog> flagStatusChangeLog = new ArrayList<>();
	private List<JiraHistoryChangeLog> workLog = new ArrayList<>();

	private List<AdditionalFilter> additionalFilters;

	private String url;
	private String description;

	@Override
	public String toString() {
		return "FeatureCustomHistory [projectID=" + projectID + ", storyID=" + storyID + ", url=" + url + ", storyType="
				+ storyType + ", defectStoryID=" + defectStoryID + ", estimate=" + estimate + ", bufferedEstimateTime="
				+ bufferedEstimateTime + ", devicePlatform=" + devicePlatform + ", projectKey=" + projectKey
				+ ", projectComponentId=" + projectComponentId + ", statusUpdationLog=" + statusUpdationLog
				+ ", assigneeUpdationLog=" + assigneeUpdationLog + ", priorityUpdationLog=" + priorityUpdationLog
				+ ", fixVersionUpdationLog=" + fixVersionUpdationLog + ", labelUpdationLog=" + labelUpdationLog
				+ ", dueDateUpdationLog=" + dueDateUpdationLog + ", sprintUpdationLog=" + sprintUpdationLog + "]";
	}

}
