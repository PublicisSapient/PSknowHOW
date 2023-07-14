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

package com.publicissapient.kpidashboard.apis.model;

import java.util.Objects;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the account filtered data.
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccountFilteredData {

	private String nodeId;
	private String nodeName;
	private String sprintStartDate;
	private String sprintEndDate;
	private String releaseDate;
	private String dateFrom;
	private String dateTo;
	private String path;
	private String labelName;
	private String parentId;
	private String sprintState;
	private int level;
	private String releaseEndDate;
	private String releaseStartDate;
	private String releaseState;
	private ObjectId basicProjectConfigId;

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if ((null != obj && this.getClass() != obj.getClass()) || null == obj) {
			return false;
		}
		AccountFilteredData other = (AccountFilteredData) obj;
		if (obj instanceof AccountFilteredData && this.nodeId.equals(other.nodeId)
				&& (null == this.parentId || this.parentId.equals(other.parentId))) {
			isEqual = true;

		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.nodeId, this.parentId);
	}

}
