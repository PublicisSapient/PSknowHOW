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
public class Branches {

	@JsonProperty("branch__name")
	private String branchName;

	private String name;
	private long count;

	@JsonProperty("merge_requests")
	private List<MergeRequests> mergeRequestList;

	private Map<String, Double> mergeRequestsPT;
	private long mergeRequests;

	@JsonProperty("merge_request_count")
	private long mergeRequestCount;

	private double grade;
	private double average;
	private double hours;

	@JsonProperty("lines_change")
	private long linesChanged;

	private double branchReworkRateGrade;
	private double revertRateGrade;
	private double branchReworkRateScore;
	private double innovationRatePercentageBranch;
	private int branchMergeRequestsNumber;
	private double branchMergeRequestPercentage;
	private double branchRevertRatePercentage;
	private double branchPercentage;
	private List<RepoToolUserDetails> users;
}
