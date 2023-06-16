
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

package com.publicissapient.kpidashboard.common.model.excel;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.application.AssigneeCapacity;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a Capacity Kpi Data.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "capacity_kpi_data")
public class CapacityKpiData extends BasicModel {
	private String sprintID;
	private String projectName;
	private String projectId;
	private Double capacityPerSprint;
	private ObjectId basicProjectConfigId;
	private List<AssigneeCapacity> assigneeCapacity;

	@Override
	public String toString() {
		return "CapacityKpiData{" + "sprintID='" + sprintID + '\'' + ", projectName='" + projectName + '\''
				+ ", projectId='" + projectId + '\'' + ", capacityPerSprint=" + capacityPerSprint
				+ ", basicProjectConfigId=" + basicProjectConfigId + '}';
	}
}