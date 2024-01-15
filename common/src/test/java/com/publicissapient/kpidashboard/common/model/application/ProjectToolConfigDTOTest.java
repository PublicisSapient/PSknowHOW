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

public class ProjectToolConfigDTOTest {
	// Field basicProjectConfigId of type ObjectId - was not mocked since Mockito
	// doesn't mock a Final class when 'mock-maker-inline' option is not set
	// Field connectionId of type ObjectId - was not mocked since Mockito doesn't
	// mock a Final class when 'mock-maker-inline' option is not set
	@Mock
	List<String> newRelicAppNames;
	@Mock
	List<Subproject> subprojects;
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
	@InjectMocks
	ProjectToolConfigDTO projectToolConfigDTO;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = projectToolConfigDTO.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = projectToolConfigDTO.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = projectToolConfigDTO.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetId() throws Exception {
		projectToolConfigDTO.setId("id");
	}

	@Test
	public void testSetToolName() throws Exception {
		projectToolConfigDTO.setToolName("toolName");
	}

	@Test
	public void testSetBasicProjectConfigId() throws Exception {
		projectToolConfigDTO.setBasicProjectConfigId(null);
	}

	@Test
	public void testSetConnectionId() throws Exception {
		projectToolConfigDTO.setConnectionId(null);
	}

	@Test
	public void testSetConnectionName() throws Exception {
		projectToolConfigDTO.setConnectionName("connectionName");
	}

	@Test
	public void testSetProjectId() throws Exception {
		projectToolConfigDTO.setProjectId("projectId");
	}

	@Test
	public void testSetProjectKey() throws Exception {
		projectToolConfigDTO.setProjectKey("projectKey");
	}

	@Test
	public void testSetJobName() throws Exception {
		projectToolConfigDTO.setJobName("jobName");
	}

	@Test
	public void testSetJobType() throws Exception {
		projectToolConfigDTO.setJobType("jobType");
	}

	@Test
	public void testSetBranch() throws Exception {
		projectToolConfigDTO.setBranch("branch");
	}

	@Test
	public void testSetDefaultBranch() throws Exception {
		projectToolConfigDTO.setDefaultBranch("defaultBranch");
	}

	@Test
	public void testSetEnv() throws Exception {
		projectToolConfigDTO.setEnv("env");
	}

	@Test
	public void testSetRepositoryName() throws Exception {
		projectToolConfigDTO.setRepositoryName("repositoryName");
	}

	@Test
	public void testSetRepoSlug() throws Exception {
		projectToolConfigDTO.setRepoSlug("repoSlug");
	}

	@Test
	public void testSetBitbucketProjKey() throws Exception {
		projectToolConfigDTO.setBitbucketProjKey("bitbucketProjKey");
	}

	@Test
	public void testSetApiVersion() throws Exception {
		projectToolConfigDTO.setApiVersion("apiVersion");
	}

	@Test
	public void testSetNewRelicApiQuery() throws Exception {
		projectToolConfigDTO.setNewRelicApiQuery("newRelicApiQuery");
	}

	@Test
	public void testSetNewRelicAppNames() throws Exception {
		projectToolConfigDTO.setNewRelicAppNames(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetSubprojects() throws Exception {
		projectToolConfigDTO.setSubprojects(Arrays.<Subproject>asList(new Subproject(null, null,
				"subProjectIdentification", Arrays.<String>asList("String"), "subProjectIdentSingleValue",
				Arrays.<SubProjectUserProvided>asList(new SubProjectUserProvided("subProjectUserProvidedDependOn",
						Arrays.<String>asList("String"), "subProjectUserProvidedDependCustomField")))));
	}

	@Test
	public void testSetCreatedAt() throws Exception {
		projectToolConfigDTO.setCreatedAt("createdAt");
	}

	@Test
	public void testSetUpdatedAt() throws Exception {
		projectToolConfigDTO.setUpdatedAt("updatedAt");
	}

	@Test
	public void testSetQueryEnabled() throws Exception {
		projectToolConfigDTO.setQueryEnabled(true);
	}

	@Test
	public void testSetBoardQuery() throws Exception {
		projectToolConfigDTO.setBoardQuery("boardQuery");
	}

	@Test
	public void testSetBoards() throws Exception {
		projectToolConfigDTO
				.setBoards(Arrays.<BoardDetails>asList(new BoardDetails("boardId", "boardName", "projectKey")));
	}

	@Test
	public void testSetRegressionAutomationLabels() throws Exception {
		projectToolConfigDTO.setRegressionAutomationLabels(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTestAutomationStatusLabel() throws Exception {
		projectToolConfigDTO.setTestAutomationStatusLabel("testAutomationStatusLabel");
	}

	@Test
	public void testSetAutomatedTestValue() throws Exception {
		projectToolConfigDTO.setAutomatedTestValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTestAutomated() throws Exception {
		projectToolConfigDTO.setTestAutomated("testAutomated");
	}

	@Test
	public void testSetCanNotAutomatedTestValue() throws Exception {
		projectToolConfigDTO.setCanNotAutomatedTestValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTestRegressionLabel() throws Exception {
		projectToolConfigDTO.setTestRegressionLabel("testRegressionLabel");
	}

	@Test
	public void testSetTestRegressionValue() throws Exception {
		projectToolConfigDTO.setTestRegressionValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetRegressionAutomationFolderPath() throws Exception {
		projectToolConfigDTO.setRegressionAutomationFolderPath(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetInSprintAutomationFolderPath() throws Exception {
		projectToolConfigDTO.setInSprintAutomationFolderPath(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraTestCaseType() throws Exception {
		projectToolConfigDTO.setJiraTestCaseType(new String[] { "jiraTestCaseType" });
	}

	@Test
	public void testSetTestAutomatedIdentification() throws Exception {
		projectToolConfigDTO.setTestAutomatedIdentification("testAutomatedIdentification");
	}

	@Test
	public void testSetTestAutomationCompletedIdentification() throws Exception {
		projectToolConfigDTO.setTestAutomationCompletedIdentification("testAutomationCompletedIdentification");
	}

	@Test
	public void testSetTestRegressionIdentification() throws Exception {
		projectToolConfigDTO.setTestRegressionIdentification("testRegressionIdentification");
	}

	@Test
	public void testSetTestAutomationCompletedByCustomField() throws Exception {
		projectToolConfigDTO.setTestAutomationCompletedByCustomField("testAutomationCompletedByCustomField");
	}

	@Test
	public void testSetTestRegressionByCustomField() throws Exception {
		projectToolConfigDTO.setTestRegressionByCustomField("testRegressionByCustomField");
	}

	@Test
	public void testSetJiraAutomatedTestValue() throws Exception {
		projectToolConfigDTO.setJiraAutomatedTestValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraRegressionTestValue() throws Exception {
		projectToolConfigDTO.setJiraRegressionTestValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraCanBeAutomatedTestValue() throws Exception {
		projectToolConfigDTO.setJiraCanBeAutomatedTestValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTestCaseStatus() throws Exception {
		projectToolConfigDTO.setTestCaseStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetOrganizationKey() throws Exception {
		projectToolConfigDTO.setOrganizationKey("organizationKey");
	}

	@Test
	public void testSetDeploymentProjectName() throws Exception {
		projectToolConfigDTO.setDeploymentProjectName("deploymentProjectName");
	}

	@Test
	public void testSetDeploymentProjectId() throws Exception {
		projectToolConfigDTO.setDeploymentProjectId("deploymentProjectId");
	}

	@Test
	public void testSetParameterNameForEnvironment() throws Exception {
		projectToolConfigDTO.setParameterNameForEnvironment("parameterNameForEnvironment");
	}

	@Test
	public void testSetMetadataTemplateCode() throws Exception {
		projectToolConfigDTO.setMetadataTemplateCode("metadataTemplateCode");
	}

	@Test
	public void testSetWorkflowID() throws Exception {
		projectToolConfigDTO.setWorkflowID("workflowID");
	}

	@Test
	public void testSetGitLabSdmID() throws Exception {
		projectToolConfigDTO.setGitLabSdmID("gitLabSdmID");
	}

	@Test
	public void testSetAzureIterationStatusFieldUpdate() throws Exception {
		projectToolConfigDTO.setAzureIterationStatusFieldUpdate(true);
	}

	@Test
	public void testSetProjectComponent() throws Exception {
		projectToolConfigDTO.setProjectComponent("projectComponent");
	}

	@Test
	public void testSetIsNew() throws Exception {
		projectToolConfigDTO.setIsNew(Boolean.TRUE);
	}

	@Test
	public void testSetScanningBranch() throws Exception {
		projectToolConfigDTO.setScanningBranch("scanningBranch");
	}

	@Test
	public void testBuilder() throws Exception {
		ProjectToolConfigDTO.ProjectToolConfigDTOBuilder result = ProjectToolConfigDTO.builder();
		Assert.assertNotNull(result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme