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

import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The type Kanban feature.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "kanban_jira_issue")
public class KanbanJiraIssue extends BasicModel {

	private ObjectId processorId;
	// updated sId to issueId
	@Indexed
	private String issueId;
	private String number;
	private String name;
	private String typeId;
	private String typeName;
	private String status;
	private String state;
	private String estimate; // estimate in story points
	private Double storyPoints;
	private Integer estimateTime; // estimate in minutes
	private String url;
	@Indexed
	private String changeDate;
	private String isDeleted;
	private String priority;
	private String count;
	private List<String> labels;
	private String createdDate;
	private String dueDate;
	private String devDueDate;
	// environmented impacted Eg. Development,QA,MTE,Beta,Production
	private String envImpacted;
	// build number
	private String buildNumber;
	// root cause
	private List<String> rootCauseList;
	/*
	 * Owner data
	 */
	private List<String> ownersID;
	private List<String> ownersIsDeleted;
	private List<String> ownersChangeDate;
	private List<String> ownersState;
	private List<String> ownersUsername;
	private List<String> ownersFullName;
	private List<String> ownersShortName;
	/*
	 * ScopeOwner data
	 */
	private String teamIsDeleted;
	private String teamAssetState;
	private String teamChangeDate;
	private String teamName;
	@Indexed
	private String sTeamID;
	/*
	 * Automated Test Data
	 */
	private String testAutomated;
	private String isTestAutomated;
	private String isTestCanBeAutomated;
	private String testAutomatedDate;
	/*
	 * Epic data
	 */
	private String epicIsDeleted;
	private String epicChangeDate;
	private String epicAssetState;
	private String epicType;
	private String epicEndDate;
	private String epicBeginDate;
	private String epicName;
	private String epicUrl;
	private String epicNumber;
	private String epicID;
	private List<KanbanJiraIssue> changeDateList;
	private Integer reopeningCounter;
	private double costOfDelay;
	private double jobSize;
	private double wsjf;
	private double businessValue;
	private double timeCriticality;
	private double riskReduction;
	/*
	 * Scope data
	 */
	private String projectPath;
	private String projectIsDeleted;
	private String projectState;
	private String projectChangeDate;
	private String projectEndDate;
	private String projectBeginDate;
	private String projectName;
	private String projectID;
	private String projectKey;
	private String jiraProjectName;
	private Integer bufferedEstimateTime; // buffered estimate in days
	private String resolution; // Added to store Jira resolution
	private List<String> affectedVersions;

	private List<AdditionalFilter> additionalFilters;
	/*
	 * Work Stream data
	 */
	private String workStreamID;
	private String workStream;
	private String release;
	private String releaseId;
	private String releaseDate;
	private String buildId;
	private String assigneeId;
	private String assigneeName;
	private String assignAttributeValue;
	// Custom Team field
	private String teamNameValue;
	// Story demonstrated to client field
	private String storyDemonstratedFieldValue;
	private DateTime storyDemonstratedFieldValueDate;
	/**
	 * Device Platform (iOS/Android/Desktop)
	 */
	private String devicePlatform;
	private String defectRaisedBy;
	private String jiraStatus;
	private Set<String> defectStoryID;
	private String speedyIssueType;
	private Integer timeSpentInMinutes;
	private String basicProjectConfigId;
	private String developerId;
	private String developerName;
	private String qaId;
	private String qaName;
	private String testCaseFolderName;
	private boolean productionDefect;
	private Integer aggregateTimeOriginalEstimateMinutes;
	private Integer aggregateTimeRemainingEstimateMinutes;

	private String originalType;
	private String epicLinked;
	private String boardId;
}
