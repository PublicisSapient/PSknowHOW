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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** RepoTool kpi validation data */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RepoToolValidationData {
	private String projectName;
	private String repoUrl;
	private String branchName;
	private String developerName;
	private String date;
	private long commitCount;
	private long mrCount;
	private Long meanTimeToMerge;
	private long prSize;
	private Double pickupTime;
	private Double reworkRate;
	private Double revertRate;
	private Double pRSuccessRate;
	private Double prDeclineRate;
	private String mergeRequestUrl;
	private Double innovationRate;
	private double defectRate;
	private long addedLines;
	private long changedLines;
	private long kpiPRs;
	private String prRaisedTime;
	private String prActivityTime;
	private String prStatus;
	private List<String> mergeRequestComment;
}
