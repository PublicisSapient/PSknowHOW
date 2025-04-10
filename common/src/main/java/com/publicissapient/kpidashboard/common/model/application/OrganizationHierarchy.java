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

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "organization_hierarchy")
public class OrganizationHierarchy extends BasicModel implements Serializable {

	private static final long serialVersionUID = 67050747445127809L;

	// UniqueId of Central Hierarchy for Each Node
	@Indexed(unique = true)
	private String nodeId;

	private String nodeName;

	private String nodeDisplayName;

	private String hierarchyLevelId;

	@Indexed(unique = true)
	private String parentId;

	@CreatedDate
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdDate;

	@LastModifiedDate
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime modifiedDate;

	@CreatedBy
	private String createdBy;

	@LastModifiedBy
	private String updatedBy;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof OrganizationHierarchy))
			return false;

		OrganizationHierarchy that = (OrganizationHierarchy) o;

		if (!nodeId.equals(that.nodeId))
			return false;
		if (!nodeName.equals(that.nodeName))
			return false;
		if (!hierarchyLevelId.equals(that.hierarchyLevelId))
			return false;
		return parentId.equals(that.parentId);
	}

	@Override
	public int hashCode() {
		int result = nodeId.hashCode();
		result = 31 * result + nodeName.hashCode();
		result = 31 * result + hierarchyLevelId.hashCode();
		result = 31 * result + parentId.hashCode();
		return result;
	}
}
