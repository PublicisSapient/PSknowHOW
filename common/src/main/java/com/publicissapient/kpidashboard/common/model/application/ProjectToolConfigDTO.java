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

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author dilipKr
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectToolConfigDTO {

	private String id;
	private String toolName;
	private ObjectId basicProjectConfigId;
	private ObjectId connectionId;
	private String connectionName;
	private String projectId;
	private String projectKey;
	private String jobName;
	private String jobType;
	private String branch;
	private String defaultBranch;
	private String env;
	private String repositoryName;
	private String repoSlug;
	private String bitbucketProjKey;
	private String apiVersion;
	private String newRelicApiQuery;
	private List<String> newRelicAppNames;
	private List<Subproject> subprojects;
	private String createdAt;
	private String updatedAt;
	private boolean queryEnabled;
	private String boardQuery;
	private List<BoardDetails> boards;
	// TestCase Fields For zephyr tool
	private List<String> regressionAutomationLabels;
	private String testAutomationStatusLabel;
	private List<String> automatedTestValue;
	private String testAutomated;
	private List<String> canNotAutomatedTestValue;
	private String testRegressionLabel;
	private List<String> testRegressionValue;
	private List<String> regressionAutomationFolderPath;
	private List<String> inSprintAutomationFolderPath;

	// TestCase Fields For jira test tool
	private String[] jiraTestCaseType;
	private String testAutomatedIdentification;
	private String testAutomationCompletedIdentification;
	private String testRegressionIdentification;
	private String testAutomationCompletedByCustomField;
	private String testRegressionByCustomField;
	private List<String> jiraAutomatedTestValue;
	private List<String> jiraRegressionTestValue;
	private List<String> jiraCanBeAutomatedTestValue;
	private List<String> testCaseStatus;

	private String organizationKey;
	// BambooDeployment
	private String deploymentProjectName;
	private String deploymentProjectId;

	private String parameterNameForEnvironment;

	// template ID
	private String metadataTemplateCode;
	// workflows ID for github Action processor
	private String workflowID;

	// Sonar SDM ID use for GS
	private String gitLabSdmID;

	// jiraIterationCompletionStatusCustomField field mapping update identifier
	private boolean azureIterationStatusFieldUpdate;
	private String projectComponent;

	private Boolean isNew;
	private String scanningBranch;
}
