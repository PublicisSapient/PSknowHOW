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
import java.time.LocalDateTime;
import java.util.Objects;

import lombok.Builder;
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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ProjectHierarchy that = (ProjectHierarchy) o;
		return Objects.equals(basicProjectConfigId, that.basicProjectConfigId)
				&& Objects.equals(sprintState, that.sprintState) && Objects.equals(releaseState, that.releaseState)
				&& Objects.equals(beginDate, that.beginDate) && Objects.equals(endDate, that.endDate)
				&& Objects.equals(this.getParentId(), that.getParentId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(basicProjectConfigId, sprintState, releaseState, beginDate, endDate);
	}

	public ProjectHierarchy(String nodeId, String nodeName, String nodeDisplayName, String hierarchyLevelId,
			String parentId, LocalDateTime createdDate, LocalDateTime modifiedDate , ObjectId basicProjectConfigId) {
		super(nodeId, nodeName, nodeDisplayName, hierarchyLevelId, parentId, createdDate, modifiedDate);
		this.basicProjectConfigId = basicProjectConfigId;
	}
}
