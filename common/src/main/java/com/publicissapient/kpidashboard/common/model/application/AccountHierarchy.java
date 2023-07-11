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

import org.bson.types.ObjectId;
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
@Document(collection = "account_hierarchy")
public class AccountHierarchy extends BasicModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String nodeId;
	private String nodeName;
	private String labelName;
	private String beginDate;
	private String endDate;
	private String parentId;
	private ObjectId basicProjectConfigId;
	private ObjectId filterCategoryId;
	private String isDeleted;
	private String path;
	private String sprintState;
	private String releaseState;
	private LocalDateTime createdDate;

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if ((null != obj && this.getClass() != obj.getClass()) || null == obj) {
			return false;
		}
		AccountHierarchy other = (AccountHierarchy) obj;
		if (obj instanceof AccountHierarchy && this.nodeId.equals(other.nodeId)
				&& (null == this.path || this.path.equals(other.path))
				&& (null == this.beginDate || this.beginDate.split("[.]")[0].equals(other.beginDate.split("[.]")[0]))
				&& (null == this.endDate || this.endDate.split("[.]")[0].equals(other.endDate.split("[.]")[0]))
				&& (null == this.releaseState || this.releaseState.equals(other.releaseState))) {
			isEqual = true;

		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.nodeId, this.path, this.beginDate, this.endDate, this.releaseState);
	}

}
