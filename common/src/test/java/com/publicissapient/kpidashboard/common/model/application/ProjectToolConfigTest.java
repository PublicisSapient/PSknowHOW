/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.publicissapient.kpidashboard.common.model.application;

import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class ProjectToolConfigTest {
	// Field basicProjectConfigId of type ObjectId - was not mocked since Mockito
	// doesn't mock a Final class when 'mock-maker-inline' option is not set
	// Field connectionId of type ObjectId - was not mocked since Mockito doesn't
	// mock a Final class when 'mock-maker-inline' option is not set
	@Mock
	List<String> newRelicAppNames;
	@Mock
	List<BoardDetails> boards;
	@Mock
	List<String> regressionAutomationLabels;
	@Mock
	List<String> automatedTestValue;
	@Mock
	List<String> canNotAutomatedTestValue;
	@Mock
	List<String> testRegressionValue;
	@Mock
	List<String> regressionAutomationFolderPath;
	@Mock
	List<String> inSprintAutomationFolderPath;
	@Mock
	List<String> jiraAutomatedTestValue;
	@Mock
	List<String> jiraRegressionTestValue;
	@Mock
	List<String> jiraCanBeAutomatedTestValue;
	@Mock
	List<String> testCaseStatus;
	// Field id of type ObjectId - was not mocked since Mockito doesn't mock a Final
	// class when 'mock-maker-inline' option is not set
	@InjectMocks
	ProjectToolConfig projectToolConfig;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = projectToolConfig.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = projectToolConfig.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = projectToolConfig.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetToolName() throws Exception {
		projectToolConfig.setToolName("toolName");
	}

	@Test
	public void testSetBasicProjectConfigId() throws Exception {
		projectToolConfig.setBasicProjectConfigId(null);
	}

	@Test
	public void testSetConnectionId() throws Exception {
		projectToolConfig.setConnectionId(null);
	}

	@Test
	public void testSetProjectId() throws Exception {
		projectToolConfig.setProjectId("projectId");
	}

	@Test
	public void testSetProjectKey() throws Exception {
		projectToolConfig.setProjectKey("projectKey");
	}

	@Test
	public void testSetJobName() throws Exception {
		projectToolConfig.setJobName("jobName");
	}

	@Test
	public void testSetJobType() throws Exception {
		projectToolConfig.setJobType("jobType");
	}

	@Test
	public void testSetBranch() throws Exception {
		projectToolConfig.setBranch("branch");
	}

	@Test
	public void testSetDefaultBranch() throws Exception {
		projectToolConfig.setDefaultBranch("defaultBranch");
	}

	@Test
	public void testSetEnv() throws Exception {
		projectToolConfig.setEnv("env");
	}

	@Test
	public void testSetRepositoryName() throws Exception {
		projectToolConfig.setRepositoryName("repositoryName");
	}

	@Test
	public void testSetRepoSlug() throws Exception {
		projectToolConfig.setRepoSlug("repoSlug");
	}

	@Test
	public void testSetBitbucketProjKey() throws Exception {
		projectToolConfig.setBitbucketProjKey("bitbucketProjKey");
	}

	@Test
	public void testSetApiVersion() throws Exception {
		projectToolConfig.setApiVersion("apiVersion");
	}

	@Test
	public void testSetNewRelicApiQuery() throws Exception {
		projectToolConfig.setNewRelicApiQuery("newRelicApiQuery");
	}

	@Test
	public void testSetNewRelicAppNames() throws Exception {
		projectToolConfig.setNewRelicAppNames(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetCreatedAt() throws Exception {
		projectToolConfig.setCreatedAt("createdAt");
	}

	@Test
	public void testSetUpdatedAt() throws Exception {
		projectToolConfig.setUpdatedAt("updatedAt");
	}

	@Test
	public void testSetQueryEnabled() throws Exception {
		projectToolConfig.setQueryEnabled(true);
	}

	@Test
	public void testSetBoardQuery() throws Exception {
		projectToolConfig.setBoardQuery("boardQuery");
	}

	@Test
	public void testSetBoards() throws Exception {
		projectToolConfig
				.setBoards(Arrays.<BoardDetails>asList(new BoardDetails("boardId", "boardName", "projectKey")));
	}

	@Test
	public void testSetRegressionAutomationLabels() throws Exception {
		projectToolConfig.setRegressionAutomationLabels(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTestAutomationStatusLabel() throws Exception {
		projectToolConfig.setTestAutomationStatusLabel("testAutomationStatusLabel");
	}

	@Test
	public void testSetAutomatedTestValue() throws Exception {
		projectToolConfig.setAutomatedTestValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTestAutomated() throws Exception {
		projectToolConfig.setTestAutomated("testAutomated");
	}

	@Test
	public void testSetCanNotAutomatedTestValue() throws Exception {
		projectToolConfig.setCanNotAutomatedTestValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTestRegressionLabel() throws Exception {
		projectToolConfig.setTestRegressionLabel("testRegressionLabel");
	}

	@Test
	public void testSetTestRegressionValue() throws Exception {
		projectToolConfig.setTestRegressionValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetRegressionAutomationFolderPath() throws Exception {
		projectToolConfig.setRegressionAutomationFolderPath(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetInSprintAutomationFolderPath() throws Exception {
		projectToolConfig.setInSprintAutomationFolderPath(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraTestCaseType() throws Exception {
		projectToolConfig.setJiraTestCaseType(new String[] { "jiraTestCaseType" });
	}

	@Test
	public void testSetTestAutomatedIdentification() throws Exception {
		projectToolConfig.setTestAutomatedIdentification("testAutomatedIdentification");
	}

	@Test
	public void testSetTestAutomationCompletedIdentification() throws Exception {
		projectToolConfig.setTestAutomationCompletedIdentification("testAutomationCompletedIdentification");
	}

	@Test
	public void testSetTestRegressionIdentification() throws Exception {
		projectToolConfig.setTestRegressionIdentification("testRegressionIdentification");
	}

	@Test
	public void testSetTestAutomationCompletedByCustomField() throws Exception {
		projectToolConfig.setTestAutomationCompletedByCustomField("testAutomationCompletedByCustomField");
	}

	@Test
	public void testSetTestRegressionByCustomField() throws Exception {
		projectToolConfig.setTestRegressionByCustomField("testRegressionByCustomField");
	}

	@Test
	public void testSetJiraAutomatedTestValue() throws Exception {
		projectToolConfig.setJiraAutomatedTestValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraRegressionTestValue() throws Exception {
		projectToolConfig.setJiraRegressionTestValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraCanBeAutomatedTestValue() throws Exception {
		projectToolConfig.setJiraCanBeAutomatedTestValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTestCaseStatus() throws Exception {
		projectToolConfig.setTestCaseStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetOrganizationKey() throws Exception {
		projectToolConfig.setOrganizationKey("organizationKey");
	}

	@Test
	public void testSetDeploymentProjectName() throws Exception {
		projectToolConfig.setDeploymentProjectName("deploymentProjectName");
	}

	@Test
	public void testSetDeploymentProjectId() throws Exception {
		projectToolConfig.setDeploymentProjectId("deploymentProjectId");
	}

	@Test
	public void testSetParameterNameForEnvironment() throws Exception {
		projectToolConfig.setParameterNameForEnvironment("parameterNameForEnvironment");
	}

	@Test
	public void testSetMetadataTemplateCode() throws Exception {
		projectToolConfig.setMetadataTemplateCode("metadataTemplateCode");
	}

	@Test
	public void testSetWorkflowID() throws Exception {
		projectToolConfig.setWorkflowID("workflowID");
	}

	@Test
	public void testSetGitLabSdmID() throws Exception {
		projectToolConfig.setGitLabSdmID("gitLabSdmID");
	}

	@Test
	public void testSetAzureIterationStatusFieldUpdate() throws Exception {
		projectToolConfig.setAzureIterationStatusFieldUpdate(true);
	}

	@Test
	public void testSetProjectComponent() throws Exception {
		projectToolConfig.setProjectComponent("projectComponent");
	}

	@Test
	public void testSetIsNew() throws Exception {
		projectToolConfig.setIsNew(Boolean.TRUE);
	}

	@Test
	public void testBuilder() throws Exception {
		ProjectToolConfig.ProjectToolConfigBuilder result = ProjectToolConfig.builder();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetId() throws Exception {
		projectToolConfig.setId(null);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme