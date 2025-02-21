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
import java.util.Objects;

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

	// Todo same as labelName in Account Hierarchy
	private String hierarchyLevelId;

	@Indexed(unique = true)
	private String parentId;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime modifiedDate;

	@Indexed(unique = true)
	private String externalId;

	public OrganizationHierarchy(String nodeId, String nodeName, String nodeDisplayName, String hierarchyLevelId,
			String parentId, LocalDateTime createdDate, LocalDateTime modifiedDate) {
		this.nodeId = nodeId;
		this.nodeName = nodeName;
		this.nodeDisplayName = nodeDisplayName;
		this.hierarchyLevelId = hierarchyLevelId;
		this.createdDate = createdDate;
		this.modifiedDate = modifiedDate;
		this.parentId = parentId;

	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof OrganizationHierarchy))
			return false;

		OrganizationHierarchy that = (OrganizationHierarchy) o;

		if (!nodeId.equals(that.nodeId))
			return false;
		if (!externalId.equals(that.externalId))
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
		result = 31 * result + (Objects.nonNull(parentId) ? parentId.hashCode() : 0);
		result = 31 * result + externalId.hashCode();
		return result;
	}
}
