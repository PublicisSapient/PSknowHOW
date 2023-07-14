
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

package com.publicissapient.kpidashboard.common.model.azureboards;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "System.AreaPath", "System.TeamProject", "System.IterationPath", "System.WorkItemType",
		"System.State", "System.Reason", "System.AssignedTo", "System.CreatedDate", "System.CreatedBy",
		"System.ChangedDate", "System.ChangedBy", "System.CommentCount", "System.Title", "System.BoardColumn",
		"System.BoardColumnDone", "Microsoft.VSTS.Scheduling.StoryPoints", "Microsoft.VSTS.Scheduling.RemainingWork",
		"Microsoft.VSTS.Scheduling.OriginalEstimate", "Microsoft.VSTS.Common.StateChangeDate",
		"Microsoft.VSTS.Common.ResolvedReason", "Microsoft.VSTS.Common.Priority", "Microsoft.VSTS.Common.Severity",
		"Microsoft.VSTS.Common.ValueArea", "WEF_F17611F3F80E45D2AFC8D2A78F930BDE_Kanban.Column",
		"WEF_F17611F3F80E45D2AFC8D2A78F930BDE_Kanban.Column.Done", "Microsoft.VSTS.TCM.SystemInfo",
		"Microsoft.VSTS.TCM.ReproSteps", "System.Tags", "Microsoft.VSTS.Common.DueDate" })
public class Fields {

	@JsonProperty("System.AreaPath")
	private String systemAreaPath;
	@JsonProperty("System.TeamProject")
	private String systemTeamProject;
	@JsonProperty("System.IterationPath")
	private String systemIterationPath;
	@JsonProperty("System.WorkItemType")
	private String systemWorkItemType;
	@JsonProperty("System.State")
	private String systemState;
	@JsonProperty("System.Reason")
	private String systemReason;
	@JsonProperty("System.AssignedTo")
	private SystemAssignedTo systemAssignedTo;
	@JsonProperty("System.CreatedDate")
	private String systemCreatedDate;
	@JsonProperty("System.CreatedBy")
	private SystemCreatedBy systemCreatedBy;
	@JsonProperty("System.ChangedDate")
	private String systemChangedDate;
	@JsonProperty("System.ChangedBy")
	private SystemChangedBy systemChangedBy;
	@JsonProperty("System.CommentCount")
	private Integer systemCommentCount;
	@JsonProperty("System.Title")
	private String systemTitle;
	@JsonProperty("System.BoardColumn")
	private String systemBoardColumn;
	@JsonProperty("System.BoardColumnDone")
	private Boolean systemBoardColumnDone;
	@JsonProperty("Microsoft.VSTS.Scheduling.StoryPoints")
	private Double microsoftVSTSSchedulingStoryPoints;
	@JsonProperty("Microsoft.VSTS.Scheduling.RemainingWork")
	private Integer microsoftVSTSSchedulingRemainingWork;
	@JsonProperty("Microsoft.VSTS.Scheduling.CompletedWork")
	private Integer microsoftVSTSSchedulingCompletedWork;
	@JsonProperty("Microsoft.VSTS.Scheduling.OriginalEstimate")
	private Double microsoftVSTSSchedulingOriginalEstimate;
	@JsonProperty("Microsoft.VSTS.Common.StateChangeDate")
	private String microsoftVSTSCommonStateChangeDate;
	@JsonProperty("Microsoft.VSTS.Common.ResolvedReason")
	private String microsoftVSTSCommonResolvedReason;
	@JsonProperty("Microsoft.VSTS.Common.Priority")
	private Integer microsoftVSTSCommonPriority;
	@JsonProperty("Microsoft.VSTS.Common.Severity")
	private String microsoftVSTSCommonSeverity;
	@JsonProperty("Microsoft.VSTS.Common.ValueArea")
	private String microsoftVSTSCommonValueArea;

	@JsonProperty("Microsoft.VSTS.Scheduling.DueDate")
	private String microsoftVSTSSchedulingDueDate;
	@JsonProperty("WEF_F17611F3F80E45D2AFC8D2A78F930BDE_Kanban.Column")
	private String wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumn;
	@JsonProperty("WEF_F17611F3F80E45D2AFC8D2A78F930BDE_Kanban.Column.Done")
	private Boolean wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumnDone;
	@JsonProperty("Microsoft.VSTS.TCM.SystemInfo")
	private String microsoftVSTSTCMSystemInfo;
	@JsonProperty("Microsoft.VSTS.TCM.ReproSteps")
	private String microsoftVSTSTCMReproSteps;
	@JsonProperty("System.Tags")
	private String systemTags;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("System.AreaPath")
	public String getSystemAreaPath() {
		return systemAreaPath;
	}

	@JsonProperty("System.AreaPath")
	public void setSystemAreaPath(String systemAreaPath) {
		this.systemAreaPath = systemAreaPath;
	}

	@JsonProperty("System.TeamProject")
	public String getSystemTeamProject() {
		return systemTeamProject;
	}

	@JsonProperty("System.TeamProject")
	public void setSystemTeamProject(String systemTeamProject) {
		this.systemTeamProject = systemTeamProject;
	}

	@JsonProperty("System.IterationPath")
	public String getSystemIterationPath() {
		return systemIterationPath;
	}

	@JsonProperty("System.IterationPath")
	public void setSystemIterationPath(String systemIterationPath) {
		this.systemIterationPath = systemIterationPath;
	}

	@JsonProperty("System.WorkItemType")
	public String getSystemWorkItemType() {
		return systemWorkItemType;
	}

	@JsonProperty("System.WorkItemType")
	public void setSystemWorkItemType(String systemWorkItemType) {
		this.systemWorkItemType = systemWorkItemType;
	}

	@JsonProperty("System.State")
	public String getSystemState() {
		return systemState;
	}

	@JsonProperty("System.State")
	public void setSystemState(String systemState) {
		this.systemState = systemState;
	}

	@JsonProperty("System.Reason")
	public String getSystemReason() {
		return systemReason;
	}

	@JsonProperty("System.Reason")
	public void setSystemReason(String systemReason) {
		this.systemReason = systemReason;
	}

	@JsonProperty("System.AssignedTo")
	public SystemAssignedTo getSystemAssignedTo() {
		return systemAssignedTo;
	}

	@JsonProperty("System.AssignedTo")
	public void setSystemAssignedTo(SystemAssignedTo systemAssignedTo) {
		this.systemAssignedTo = systemAssignedTo;
	}

	@JsonProperty("System.CreatedDate")
	public String getSystemCreatedDate() {
		return systemCreatedDate;
	}

	@JsonProperty("System.CreatedDate")
	public void setSystemCreatedDate(String systemCreatedDate) {
		this.systemCreatedDate = systemCreatedDate;
	}

	@JsonProperty("System.CreatedBy")
	public SystemCreatedBy getSystemCreatedBy() {
		return systemCreatedBy;
	}

	@JsonProperty("System.CreatedBy")
	public void setSystemCreatedBy(SystemCreatedBy systemCreatedBy) {
		this.systemCreatedBy = systemCreatedBy;
	}

	@JsonProperty("System.ChangedDate")
	public String getSystemChangedDate() {
		return systemChangedDate;
	}

	@JsonProperty("System.ChangedDate")
	public void setSystemChangedDate(String systemChangedDate) {
		this.systemChangedDate = systemChangedDate;
	}

	@JsonProperty("System.ChangedBy")
	public SystemChangedBy getSystemChangedBy() {
		return systemChangedBy;
	}

	@JsonProperty("System.ChangedBy")
	public void setSystemChangedBy(SystemChangedBy systemChangedBy) {
		this.systemChangedBy = systemChangedBy;
	}

	@JsonProperty("System.CommentCount")
	public Integer getSystemCommentCount() {
		return systemCommentCount;
	}

	@JsonProperty("System.CommentCount")
	public void setSystemCommentCount(Integer systemCommentCount) {
		this.systemCommentCount = systemCommentCount;
	}

	@JsonProperty("System.Title")
	public String getSystemTitle() {
		return systemTitle;
	}

	@JsonProperty("System.Title")
	public void setSystemTitle(String systemTitle) {
		this.systemTitle = systemTitle;
	}

	@JsonProperty("System.BoardColumn")
	public String getSystemBoardColumn() {
		return systemBoardColumn;
	}

	@JsonProperty("System.BoardColumn")
	public void setSystemBoardColumn(String systemBoardColumn) {
		this.systemBoardColumn = systemBoardColumn;
	}

	@JsonProperty("System.BoardColumnDone")
	public Boolean getSystemBoardColumnDone() {
		return systemBoardColumnDone;
	}

	@JsonProperty("System.BoardColumnDone")
	public void setSystemBoardColumnDone(Boolean systemBoardColumnDone) {
		this.systemBoardColumnDone = systemBoardColumnDone;
	}

	@JsonProperty("Microsoft.VSTS.Scheduling.StoryPoints")
	public Double getMicrosoftVSTSSchedulingStoryPoints() {
		return microsoftVSTSSchedulingStoryPoints;
	}

	@JsonProperty("Microsoft.VSTS.Scheduling.StoryPoints")
	public void setMicrosoftVSTSSchedulingStoryPoints(Double microsoftVSTSSchedulingStoryPoints) {
		this.microsoftVSTSSchedulingStoryPoints = microsoftVSTSSchedulingStoryPoints;
	}

	@JsonProperty("Microsoft.VSTS.Scheduling.RemainingWork")
	public Integer getMicrosoftVSTSSchedulingRemainingWork() {
		return microsoftVSTSSchedulingRemainingWork;
	}

	@JsonProperty("Microsoft.VSTS.Scheduling.RemainingWork")
	public void setMicrosoftVSTSSchedulingRemainingWork(Integer microsoftVSTSSchedulingRemainingWork) {
		this.microsoftVSTSSchedulingRemainingWork = microsoftVSTSSchedulingRemainingWork;
	}

	@JsonProperty("Microsoft.VSTS.Scheduling.CompletedWork")
	public Integer getMicrosoftVSTSSchedulingCompletedWork() {
		return microsoftVSTSSchedulingCompletedWork;
	}

	@JsonProperty("Microsoft.VSTS.Scheduling.CompletedWork")
	public void setMicrosoftVSTSSchedulingCompletedWork(Integer microsoftVSTSSchedulingCompletedWork) {
		this.microsoftVSTSSchedulingCompletedWork = microsoftVSTSSchedulingCompletedWork;
	}

	@JsonProperty("Microsoft.VSTS.Scheduling.OriginalEstimate")
	public Double getMicrosoftVSTSSchedulingOriginalEstimate() {
		return microsoftVSTSSchedulingOriginalEstimate;
	}

	@JsonProperty("Microsoft.VSTS.Scheduling.OriginalEstimate")
	public void setMicrosoftVSTSSchedulingOriginalEstimate(Double microsoftVSTSSchedulingOriginalEstimate) {
		this.microsoftVSTSSchedulingOriginalEstimate = microsoftVSTSSchedulingOriginalEstimate;
	}

	@JsonProperty("Microsoft.VSTS.Common.StateChangeDate")
	public String getMicrosoftVSTSCommonStateChangeDate() {
		return microsoftVSTSCommonStateChangeDate;
	}

	@JsonProperty("Microsoft.VSTS.Common.StateChangeDate")
	public void setMicrosoftVSTSCommonStateChangeDate(String microsoftVSTSCommonStateChangeDate) {
		this.microsoftVSTSCommonStateChangeDate = microsoftVSTSCommonStateChangeDate;
	}

	@JsonProperty("Microsoft.VSTS.Common.ResolvedReason")
	public String getMicrosoftVSTSCommonResolvedReason() {
		return microsoftVSTSCommonResolvedReason;
	}

	@JsonProperty("Microsoft.VSTS.Common.ResolvedReason")
	public void setMicrosoftVSTSCommonResolvedReason(String microsoftVSTSCommonResolvedReason) {
		this.microsoftVSTSCommonResolvedReason = microsoftVSTSCommonResolvedReason;
	}

	@JsonProperty("Microsoft.VSTS.Common.Priority")
	public Integer getMicrosoftVSTSCommonPriority() {
		return microsoftVSTSCommonPriority;
	}

	@JsonProperty("Microsoft.VSTS.Common.Priority")
	public void setMicrosoftVSTSCommonPriority(Integer microsoftVSTSCommonPriority) {
		this.microsoftVSTSCommonPriority = microsoftVSTSCommonPriority;
	}

	@JsonProperty("Microsoft.VSTS.Common.Severity")
	public String getMicrosoftVSTSCommonSeverity() {
		return microsoftVSTSCommonSeverity;
	}

	@JsonProperty("Microsoft.VSTS.Common.Severity")
	public void setMicrosoftVSTSCommonSeverity(String microsoftVSTSCommonSeverity) {
		this.microsoftVSTSCommonSeverity = microsoftVSTSCommonSeverity;
	}

	@JsonProperty("Microsoft.VSTS.Common.ValueArea")
	public String getMicrosoftVSTSCommonValueArea() {
		return microsoftVSTSCommonValueArea;
	}

	@JsonProperty("Microsoft.VSTS.Common.ValueArea")
	public void setMicrosoftVSTSCommonValueArea(String microsoftVSTSCommonValueArea) {
		this.microsoftVSTSCommonValueArea = microsoftVSTSCommonValueArea;
	}

	public String getMicrosoftVSTSSchedulingDueDate() {
		return microsoftVSTSSchedulingDueDate;
	}

	public void setMicrosoftVSTSSchedulingDueDate(String microsoftVSTSSchedulingDueDate) {
		this.microsoftVSTSSchedulingDueDate = microsoftVSTSSchedulingDueDate;
	}

	@JsonProperty("WEF_F17611F3F80E45D2AFC8D2A78F930BDE_Kanban.Column")
	public String getWEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumn() {
		return wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumn;
	}

	@JsonProperty("WEF_F17611F3F80E45D2AFC8D2A78F930BDE_Kanban.Column")
	public void setWEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumn(
			String wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumn) {
		this.wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumn = wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumn;
	}

	@JsonProperty("WEF_F17611F3F80E45D2AFC8D2A78F930BDE_Kanban.Column.Done")
	public Boolean getWEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumnDone() {
		return wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumnDone;
	}

	@JsonProperty("WEF_F17611F3F80E45D2AFC8D2A78F930BDE_Kanban.Column.Done")
	public void setWEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumnDone(
			Boolean wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumnDone) {
		this.wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumnDone = wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumnDone;
	}

	@JsonProperty("Microsoft.VSTS.TCM.SystemInfo")
	public String getMicrosoftVSTSTCMSystemInfo() {
		return microsoftVSTSTCMSystemInfo;
	}

	@JsonProperty("Microsoft.VSTS.TCM.SystemInfo")
	public void setMicrosoftVSTSTCMSystemInfo(String microsoftVSTSTCMSystemInfo) {
		this.microsoftVSTSTCMSystemInfo = microsoftVSTSTCMSystemInfo;
	}

	@JsonProperty("Microsoft.VSTS.TCM.ReproSteps")
	public String getMicrosoftVSTSTCMReproSteps() {
		return microsoftVSTSTCMReproSteps;
	}

	@JsonProperty("Microsoft.VSTS.TCM.ReproSteps")
	public void setMicrosoftVSTSTCMReproSteps(String microsoftVSTSTCMReproSteps) {
		this.microsoftVSTSTCMReproSteps = microsoftVSTSTCMReproSteps;
	}

	@JsonProperty("System.Tags")
	public String getSystemTags() {
		return systemTags;
	}

	@JsonProperty("System.Tags")
	public void setSystemTags(String systemTags) {
		this.systemTags = systemTags;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("systemAreaPath", systemAreaPath)
				.append("systemTeamProject", systemTeamProject).append("systemIterationPath", systemIterationPath)
				.append("systemWorkItemType", systemWorkItemType).append("systemState", systemState)
				.append("systemReason", systemReason).append("systemAssignedTo", systemAssignedTo)
				.append("systemCreatedDate", systemCreatedDate).append("systemCreatedBy", systemCreatedBy)
				.append("systemChangedDate", systemChangedDate).append("systemChangedBy", systemChangedBy)
				.append("systemCommentCount", systemCommentCount).append("systemTitle", systemTitle)
				.append("systemBoardColumn", systemBoardColumn).append("systemBoardColumnDone", systemBoardColumnDone)
				.append("microsoftVSTSSchedulingStoryPoints", microsoftVSTSSchedulingStoryPoints)
				.append("microsoftVSTSSchedulingRemainingWork", microsoftVSTSSchedulingRemainingWork)
				.append("microsoftVSTSSchedulingCompletedWork", microsoftVSTSSchedulingCompletedWork)
				.append("microsoftVSTSSchedulingOriginalEstimate", microsoftVSTSSchedulingOriginalEstimate)
				.append("microsoftVSTSCommonStateChangeDate", microsoftVSTSCommonStateChangeDate)
				.append("microsoftVSTSCommonPriority", microsoftVSTSCommonPriority)
				.append("microsoftVSTSCommonSeverity", microsoftVSTSCommonSeverity)
				.append("microsoftVSTSCommonValueArea", microsoftVSTSCommonValueArea)
				.append("wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumn",
						wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumn)
				.append("wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumnDone",
						wEFF17611F3F80E45D2AFC8D2A78F930BDEKanbanColumnDone)
				.append("microsoftVSTSTCMSystemInfo", microsoftVSTSTCMSystemInfo)
				.append("microsoftVSTSTCMReproSteps", microsoftVSTSTCMReproSteps).append("systemTags", systemTags)
				.append("additionalProperties", additionalProperties).toString();
	}

}
