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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RepoToolKpiMetricResponse {

    @JsonProperty("project_name")
    private String projectName;
    @JsonProperty("project_code")
    private String projectCode;
    private double projectGrade;
    private  double projectHours;
    @JsonProperty("commit_count")
    private long commitCount;
    @JsonProperty("mr_count")
    private long mrCount;
    private long mergeRequests;
    private long prLinesChanged;
    public double average;
    private double projectReworkRateGrade;
    private double projectReworkRatePercent;
    private List<RepoToolRepositories> repositories;
    private List<RepoToolRepositories> projectRepositories;
    @JsonProperty("date_label")
    private String dateLabel;
    private List<RepoToolUserDetails> users;

}
