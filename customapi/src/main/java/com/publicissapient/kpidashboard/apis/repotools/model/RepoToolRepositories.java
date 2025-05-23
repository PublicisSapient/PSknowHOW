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
public class RepoToolRepositories {

	private String name;
	private String repository;
	private String repositoryName;
	private double average;

	@JsonProperty("commit_count")
	private long commitCount;

	private double repositoryGrade;
	private List<IndividualCommitsCount> individualCommitsCount;
	private List<Branches> branchesCommitsCount;
	private List<Branches> branches;
	private Map<String, Double> mergeRequestsPT;
	private String filteredBranch;
	private double repositoryReworkRateGrade;
	private double innovationRatePercentageRepo;
	private int mergeRequestsNumber;
	private double repoDefectMergeRequestPercentage;
	private double repoRevertRatePercentage;
	private double revertRateGrade;
	private double repositoryPercentage;
}
