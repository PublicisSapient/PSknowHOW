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

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "sprint_details")
public class SprintDetails extends BasicModel implements Cloneable{

	public static final String SPRINT_STATE_CLOSED = "CLOSED";
	public static final String SPRINT_STATE_ACTIVE = "ACTIVE";
	public static final String SPRINT_STATE_FUTURE = "FUTURE";
	@Indexed(unique = true)
	private String sprintID;
	private String sprintName;
	private String originalSprintId;
	private String state;
	private String startDate;
	private String endDate;
	private String completeDate;
	private String activatedDate;
	private List<String> originBoardId;
	private String goal;
	private ObjectId basicProjectConfigId;
	private ObjectId processorId;
	private Set<SprintIssue> completedIssues;
	private Set<SprintIssue> notCompletedIssues;
	private Set<SprintIssue> puntedIssues;
	private Set<SprintIssue> completedIssuesAnotherSprint;
	private Set<String> addedIssues;
	private Set<SprintIssue> totalIssues;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SprintDetails sprintDetails = (SprintDetails) o;
		return Objects.equals(sprintID, sprintDetails.sprintID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sprintID);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
