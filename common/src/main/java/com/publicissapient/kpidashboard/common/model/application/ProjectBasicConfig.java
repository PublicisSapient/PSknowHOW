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

package com.publicissapient.kpidashboard.common.model.application;

import java.util.List;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author anisingh4
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "project_basic_configs")
public class ProjectBasicConfig extends BasicModel {

	@Deprecated
	private String emmUpdatedOn;

	@Deprecated
	private String consumerCreatedOn;

	// link with Hierarchy Master nodeId where Level Is project
	@Indexed(unique = true)
	private String projectNodeId;

	private String projectName;
	private String projectDisplayName;

	private String createdAt;
	private String createdBy;
	private String updatedAt;
	private String updatedBy;
	private boolean kanban;

	// hierarchy will not read/write in DB
	@Transient
	private List<HierarchyValue> hierarchy;

	private boolean saveAssigneeDetails;
	private boolean developerKpiEnabled;
	private boolean projectOnHold;

	/**
	 * @return isKanban value
	 */
	public boolean getIsKanban() {
		return this.kanban;
	}

	/**
	 * set isKanban value
	 *
	 * @param isKanban
	 *            boolean value
	 */
	public void setIsKanban(boolean isKanban) {
		this.kanban = isKanban;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ProjectBasicConfig that = (ProjectBasicConfig) o;

		return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : 0;
	}
}
