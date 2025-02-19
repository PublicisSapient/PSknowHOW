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

package com.publicissapient.kpidashboard.apis.repotools.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RepoToolUserDetails {
	private String email;

	@JsonProperty("committer__email")
	private String committerEmail;

	private Double average;
	private Long mergeRequests;
	private Long linesChanged;
	private Double hours;
	private Double userReworkRatePercent;
	private Double userRevertRateGrade;
	private Double userRevertRatePercentage;
	private Double percentage;
	private Long count;

	@JsonProperty("mr_count")
	private Long mrCount;

	private Map<String, Double> mergeRequestsPT;

	@JsonProperty("merge_requests")
	private List<MergeRequests> mergeRequestList;

	private int mergeRequestsNumber;
	private double memberDefectMergeRequestPercentage;
	private long addedLines;
	private long changedLines;
}
