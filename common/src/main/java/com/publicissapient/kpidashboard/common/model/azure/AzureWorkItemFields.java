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

package com.publicissapient.kpidashboard.common.model.azure;

import java.util.Date;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AzureWorkItemFields {
	@JsonProperty("System.AreaPath")
	private String areaPath;
	@JsonProperty("System.TeamProject")
	private String teamProject;
	@JsonProperty("System.IterationPath")
	private String iterationPath;
	@JsonProperty("System.WorkItemType")
	private String workItemType;
	@JsonProperty("System.State")
	private String workItemState;
	@JsonProperty("System.AssignedTo")
	private JSONObject assignedTo;
	@JsonProperty("System.CreatedBy")
	private JSONObject createdBy;
	@JsonProperty("System.CreatedDate")
	private Date createdDate;
	@JsonProperty("System.ChangedBy")
	private JSONObject changedBy;
	@JsonProperty("System.ChangedDate")
	private Date changedDate;
	@JsonProperty("System.Title")
	private String title;
	@JsonProperty("System.Description")
	private String description;
	@JsonProperty("System.Reason")
	private String reason;
	@JsonProperty("Microsoft.VSTS.Common.Priority")
	private Long priority;
	@JsonProperty("System.CommentCount")
	private Long commentCount;
	@JsonProperty("Microsoft.VSTS.Common.StateChangeDate")
	private Date StateChangeDate;
}
