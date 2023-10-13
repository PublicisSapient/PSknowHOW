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

package com.publicissapient.kpidashboard.jira.model;//NOPMD

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class ProjectConfFieldMapping {
	// jira and fields mapping of jira
	private JiraToolConfig jira;
	private FieldMapping fieldMapping;

	private ObjectId basicProjectConfigId;
	// if project is kanban or Scrum
	private boolean isKanban;
	private int issueCount;
	private int sprintCount;

	// For filters basic conf
	private String projectName;
	private ProjectToolConfig projectToolConfig;
	private ObjectId jiraToolConfigId;

	private ProjectBasicConfig projectBasicConfig;

	private JiraIssueMetadata jiraIssueMetadata;

}
