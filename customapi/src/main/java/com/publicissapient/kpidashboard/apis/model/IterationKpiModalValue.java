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

package com.publicissapient.kpidashboard.apis.model;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * object used to bind iteration kpi's value
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class IterationKpiModalValue implements Serializable {
	private static final long serialVersionUID = -6376203644006393547L;
	@JsonProperty("Issue Id")
	private String issueId;
	@JsonProperty("Issue URL")
	private String issueURL;
	@JsonProperty("Issue Description")
	private String description;
	@JsonProperty("Issue Status")
	private String issueStatus;
	@JsonProperty("Issue Type")
	private String issueType;
	@JsonProperty("Size(story point/hours)")
	private String issueSize;
	@JsonProperty("Remaining Hours")
	private Integer remainingTime;
	@JsonProperty("Logged Work")
	private String timeSpentInMinutes;
	@JsonProperty("Original Estimate")
	private String originalEstimateMinutes;
	@JsonProperty("Priority")
	private String priority;
	@JsonProperty("Due Date")
	private String dueDate;
	@JsonProperty("Delay")
	private String delay;
	@JsonProperty("Remaining Estimate")
	private String remainingEstimateMinutes;
	@JsonProperty("Blocked Time")
	private String blockedTime;
	@JsonProperty("Wait Time")
	private String waitTime;
	@JsonProperty("Total Wastage")
	private String wastage;
	@JsonProperty("Actual Completion date")
	private String actualCompletionDate;
	@JsonProperty("Remaining Days")
	private String remainingTimeInDays;
	@JsonProperty("Predicted Completion Date")
	private String predictedCompletionDate;
	@JsonProperty("Potential Delay(in days)")
	private String potentialDelay;
	@JsonProperty("Issue Priority")
	private String issuePriority;

	@JsonProperty("Linked Stories")
	private Map<String,String> linkedStories;

}
