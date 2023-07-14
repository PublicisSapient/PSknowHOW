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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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

/**
 * The type Kanban feature custom history.
 */
@SuppressWarnings("PMD.TooManyFields")
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "kanban_issue_custom_history")
public class KanbanIssueCustomHistory extends BasicModel {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
	@Indexed
	private String projectID;
	private String projectName;
	@Indexed
	private String storyID;
	@Indexed
	private String storyType;
	private Set<String> defectStoryID;
	private String estimate;
	private Integer bufferedEstimateTime; // buffered estimate in days
	private String createdDate;
	private String priority;
	// root cause
	private List<String> rootCauseList;

	/**
	 * Device Platform (iOS/Android/Desktop)
	 */
	private String devicePlatform;
	private String projectKey;
	private String projectComponentId;

	private String buildId;
	private String buildNumber;

	private String developerId;
	private String developerName;
	private String qaId;
	private String qaName;
	private String basicProjectConfigId;
	private List<AdditionalFilter> additionalFilters;

	private List<KanbanIssueHistory> historyDetails = new ArrayList<>();

	private String url;
	private String description;

	@Override
	public String toString() {
		return "FeatureCustomHistory [projectID=" + projectID + ", storyID=" + storyID + ", url=" + url + ",storyType="
				+ storyType + ", defectStoryID=" + defectStoryID + ", estimate=" + estimate + ", bufferedEstimateTime="
				+ bufferedEstimateTime + ", devicePlatform=" + devicePlatform + ", projectKey=" + projectKey
				+ ", projectComponentId=" + projectComponentId + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.storyID);
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if ((null != obj && this.getClass() != obj.getClass()) || null == obj) {
			return false;
		}
		KanbanIssueCustomHistory other = (KanbanIssueCustomHistory) obj;
		if (obj instanceof KanbanIssueCustomHistory && this.storyID.equals(other.storyID)) {
			isEqual = true;
		}
		return isEqual;
	}

}
