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
	@JsonProperty("Issue Size")
	private String issueSize;
	@JsonProperty("Remaining Time")
	private String remainingTime;

	@JsonProperty("Logged Time")
	private Integer timeSpentInMinutes;

	@JsonProperty("Original Estimate")
	private Integer originalEstimateMinutes;
	public IterationKpiModalValue(String issueId, String issueURL, String name, String status, String typeName) {
		this.issueId = issueId;
		this.issueURL = issueURL;
		this.description = name;
		this.issueStatus = status;
		this.issueType = typeName;
	}


}
