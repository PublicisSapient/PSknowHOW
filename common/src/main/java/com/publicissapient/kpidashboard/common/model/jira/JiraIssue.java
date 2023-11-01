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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bson.types.ObjectId;
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

@SuppressWarnings({ "javadoc" })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "jira_issue")
public class JiraIssue extends BasicModel implements Cloneable {

	private ObjectId processorId;
	/*
	 * Story data
	 */
	// sId renamed to issueId
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
	private String teamID;

	/*
	 * Sprint data
	 */
	private String sprintIsDeleted;

	/*
	 * Automated Test Data
	 */
	private String testAutomated;
	private String isTestAutomated;
	private String isTestCanBeAutomated;
	private String testAutomatedDate;

	private String sprintChangeDate;
	private String sprintAssetState;
	@Indexed
	private String sprintEndDate;
	@Indexed
	private String sprintBeginDate;
	private String sprintName;
	@Indexed
	private String sprintID;
	private String sprintUrl;
	private List<String> sprintIdList;

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

	// Epic Issue Type Data
	private Integer reopeningCounter;
	private double costOfDelay;
	private double jobSize;
	private double wsjf;
	private double businessValue;
	private double timeCriticality;
	private double riskReduction;
	private double epicPlannedValue;
	private double epicAchievedValue;

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

	/*
	 * Work Stream data
	 */
	private String workStreamID;
	private String workStream;

	private List<AdditionalFilter> additionalFilters;

	private String release;

	private String releaseId;

	private String releaseDate;

	private String assigneeId;
	private String assigneeName;

	private String developerId;
	private String developerName;
	private String qaId;
	private String qaName;

	// SRDEVOPSDA-474
	private String assignAttributeValue;
	// Custom Team field
	private String teamNameValue;

	// Story demonstrated to client field
	private String storyDemonstratedFieldValue;
	private LocalDateTime storyDemonstratedFieldValueDate;

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
	private String testCaseFolderName;

	private List<ReleaseVersion> releaseVersions;
	private boolean defectRaisedByQA;
	private Integer originalEstimateMinutes;
	private Integer remainingEstimateMinutes;
	private boolean productionDefect;
	private Integer aggregateTimeOriginalEstimateMinutes;
	private Integer aggregateTimeRemainingEstimateMinutes;
	@Indexed
	private String updateDate;
	private String devDueDate;

	private String originalType;
	private String epicLinked;

	private List<String> escapedDefectGroup;
	private boolean productionIncident;

	private String boardId;
	private Set<String> parentStoryId;

	public boolean isDefectRaisedByQA() {
		return defectRaisedByQA;
	}

	public void setDefectRaisedByQA(boolean defectRaisedByQA) {
		this.defectRaisedByQA = defectRaisedByQA;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		JiraIssue jiraIssue = (JiraIssue) o;
		return Objects.equals(number, jiraIssue.number);
	}

	@Override
	public int hashCode() {
		return Objects.hash(number);
	}

}
