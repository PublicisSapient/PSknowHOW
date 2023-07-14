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

package com.publicissapient.kpidashboard.common.model.jira;

import org.joda.time.DateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The type Feature sprint.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JiraIssueSprint {

	private String sprintId;
	private String status;
	private String type;
	private String fromStatus;
	private String buildNumber;

	private DateTime activityDate;

	private String sprintComponentId;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		return sprintId.equals(((JiraIssueSprint) obj).sprintId);
	}

	@Override
	public int hashCode() {
		return sprintId.hashCode();
	}

	@Override
	public String toString() {
		return "FeatureSprint [sprintId=" + sprintId + ", status=" + status + ", type=" + type + ", fromStatus="
				+ fromStatus + ", buildNumber=" + buildNumber + ", activityDate=" + activityDate
				+ ", sprintComponentId=" + sprintComponentId + "]";
	}

}
