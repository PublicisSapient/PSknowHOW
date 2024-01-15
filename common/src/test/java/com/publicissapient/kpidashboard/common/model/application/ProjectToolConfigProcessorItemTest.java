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

import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorError;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

public class ProjectToolConfigProcessorItemTest {
	@Mock
	List<Connection> connection;
	@Mock
	List<ProcessorItem> processorItemList;
	// Field basicProjectConfigId of type ObjectId - was not mocked since Mockito
	// doesn't mock a Final class when 'mock-maker-inline' option is not set
	// Field connectionId of type ObjectId - was not mocked since Mockito doesn't
	// mock a Final class when 'mock-maker-inline' option is not set
	@Mock
	List<String> newRelicAppNames;
	// Field id of type ObjectId - was not mocked since Mockito doesn't mock a Final
	// class when 'mock-maker-inline' option is not set
	@InjectMocks
	ProjectToolConfigProcessorItem projectToolConfigProcessorItem;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = projectToolConfigProcessorItem.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = projectToolConfigProcessorItem.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = projectToolConfigProcessorItem.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetConnection() throws Exception {
		projectToolConfigProcessorItem.setConnection(Arrays.<Connection>asList(new Connection("type", "connectionName",
				true, true, "baseUrl", "username", "password", "patOAuthToken", "apiEndPoint", "consumerKey",
				"privateKey", "apiKey", "clientSecretKey", true, "clientId", "tenantId", "pat", "apiKeyFieldName",
				"accessToken", true, "offlineFilePath", "createdAt", "updatedAt", "createdBy", true, "updatedBy",
				Arrays.<String>asList("String"), true, true, true, "jaasConfigFilePath", "krb5ConfigFilePath",
				"jaasUser", "userPrincipal", "samlEndPoint", "repoOwnerName", "repositoryName", "sshUrl", "httpUrl",
				"email", "repoToolProvider", Boolean.TRUE)));
	}

	@Test
	public void testSetProcessorItemList() throws Exception {
		projectToolConfigProcessorItem.setProcessorItemList(Arrays.<ProcessorItem>asList(new ProcessorItem("desc", true,
				Arrays.<ProcessorError>asList(new ProcessorError("errorCode", "errorMessage", 0L)), null, 0L,
				new HashMap<String, Object>() {
					{
						put("String", "toolDetailsMap");
					}
				}, Short.valueOf((short) 0), null, new Processor(null, null, true, true, null, 0L, null, true))));
	}

	@Test
	public void testSetToolName() throws Exception {
		projectToolConfigProcessorItem.setToolName("toolName");
	}

	@Test
	public void testSetBasicProjectConfigId() throws Exception {
		projectToolConfigProcessorItem.setBasicProjectConfigId(null);
	}

	@Test
	public void testSetConnectionId() throws Exception {
		projectToolConfigProcessorItem.setConnectionId(null);
	}

	@Test
	public void testSetProjectId() throws Exception {
		projectToolConfigProcessorItem.setProjectId("projectId");
	}

	@Test
	public void testSetProjectKey() throws Exception {
		projectToolConfigProcessorItem.setProjectKey("projectKey");
	}

	@Test
	public void testSetJobName() throws Exception {
		projectToolConfigProcessorItem.setJobName("jobName");
	}

	@Test
	public void testSetBranch() throws Exception {
		projectToolConfigProcessorItem.setBranch("branch");
	}

	@Test
	public void testSetEnv() throws Exception {
		projectToolConfigProcessorItem.setEnv("env");
	}

	@Test
	public void testSetRepoSlug() throws Exception {
		projectToolConfigProcessorItem.setRepoSlug("repoSlug");
	}

	@Test
	public void testSetRepositoryName() throws Exception {
		projectToolConfigProcessorItem.setRepositoryName("repositoryName");
	}

	@Test
	public void testSetBitbucketProjKey() throws Exception {
		projectToolConfigProcessorItem.setBitbucketProjKey("bitbucketProjKey");
	}

	@Test
	public void testSetApiVersion() throws Exception {
		projectToolConfigProcessorItem.setApiVersion("apiVersion");
	}

	@Test
	public void testSetNewRelicApiQuery() throws Exception {
		projectToolConfigProcessorItem.setNewRelicApiQuery("newRelicApiQuery");
	}

	@Test
	public void testSetNewRelicAppNames() throws Exception {
		projectToolConfigProcessorItem.setNewRelicAppNames(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetCreatedAt() throws Exception {
		projectToolConfigProcessorItem.setCreatedAt("createdAt");
	}

	@Test
	public void testSetUpdatedAt() throws Exception {
		projectToolConfigProcessorItem.setUpdatedAt("updatedAt");
	}

	@Test
	public void testSetQueryEnabled() throws Exception {
		projectToolConfigProcessorItem.setQueryEnabled(true);
	}

	@Test
	public void testSetBoardQuery() throws Exception {
		projectToolConfigProcessorItem.setBoardQuery("boardQuery");
	}

	@Test
	public void testBuilder() throws Exception {
		ProjectToolConfigProcessorItem.ProjectToolConfigProcessorItemBuilder result = ProjectToolConfigProcessorItem
				.builder();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetId() throws Exception {
		projectToolConfigProcessorItem.setId(null);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme