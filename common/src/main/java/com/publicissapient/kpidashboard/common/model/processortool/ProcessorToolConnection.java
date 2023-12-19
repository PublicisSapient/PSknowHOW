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

package com.publicissapient.kpidashboard.common.model.processortool;

import java.util.List;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author narsingh9 mapping class of {@link ProjectToolConfig} and
 *         {@link Connection}
 */

@Data
@Getter
@Setter
@NoArgsConstructor
public class ProcessorToolConnection {

	private ObjectId id;
	private String toolName;
	private ObjectId basicProjectConfigId;
	private String projectId;
	private String projectKey;
	private String jobName;
	private String branch;
	private String env;
	private String fieldMappingId;
	private String repoSlug;
	private String bitbucketProjKey;
	private String apiVersion;
	private String newRelicApiQuery;
	private List<String> newRelicAppNames;
	private boolean queryEnabled;
	private String boardQuery;
	private List<BoardDetails> boards;
	private String repositoryName;
	private String workflowID;
	private ObjectId connectionId;
	private String type;
	private String connectionName;
	private String url;
	private String username;
	private String password;
	private String patOAuthToken;
	private boolean vault;
	private String apiEndPoint;
	private String consumerKey;
	private String privateKey;
	private String apiKey;
	private String clientSecretKey;
	private boolean isOAuth;
	private String clientId;
	private String tenantId;
	private String pat;
	private String apiKeyFieldName;
	private String accessToken;
	private boolean offline;
	private String offlineFilePath;
	private boolean cloudEnv;
	private boolean accessTokenEnabled;
	private String organizationKey;
	// TestCase Fields for Zephyr tool
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

	private String deploymentProjectName;
	private String deploymentProjectId;
	private String jobType;
	private String parameterNameForEnvironment;
	private boolean bearerToken;
	private boolean azureIterationStatusFieldUpdate;
	private String projectComponent;
}
