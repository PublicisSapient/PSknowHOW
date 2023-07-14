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

import java.util.Objects;

import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Account hierarchy.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "hierarchy_levels")
public class HierarchyLevel extends BasicModel {

	private int level;
	private String hierarchyLevelId;
	private String hierarchyLevelName;

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if ((null != obj && this.getClass() != obj.getClass()) || null == obj) {
			return false;
		}
		HierarchyLevel other = (HierarchyLevel) obj;
		if (obj instanceof HierarchyLevel && this.hierarchyLevelId.equals(other.hierarchyLevelId)) {
			isEqual = true;

		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.hierarchyLevelId);
	}

}
