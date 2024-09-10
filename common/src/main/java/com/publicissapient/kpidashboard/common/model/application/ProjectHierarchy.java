/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.publicissapient.kpidashboard.common.model.application;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "project_hierarchy")
public class ProjectHierarchy extends OrganizationHierarchy implements Serializable {
	private ObjectId basicProjectConfigId;
	private String sprintState;
	private String releaseState;
	private String beginDate;
	private String endDate;

	private boolean equals(ProjectHierarchy obj) {
		boolean isEqual = false;
		if ((null != obj && this.getClass() != obj.getClass()) || null == obj) {
			return false;
		}
		if (obj instanceof ProjectHierarchy && this.getNodeId().equals(obj.getNodeId())
				&& (null == this.beginDate || this.beginDate.split("[.]")[0].equals(obj.beginDate.split("[.]")[0]))
				&& (null == this.endDate || this.endDate.split("[.]")[0].equals(obj.endDate.split("[.]")[0]))) {
			isEqual = true;
		}
		return isEqual;

	}

	public boolean checkSprintEquality(ProjectHierarchy obj) {
		return equals(obj) && (null == this.sprintState || this.sprintState.equals(obj.sprintState));
	}

	public boolean checkReleaseEquality(ProjectHierarchy obj) {
		return equals(obj) && (null == this.releaseState || this.releaseState.equals(obj.releaseState));
	}
}
