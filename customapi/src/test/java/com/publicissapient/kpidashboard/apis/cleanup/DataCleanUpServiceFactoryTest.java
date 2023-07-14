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

package com.publicissapient.kpidashboard.apis.cleanup;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;

/**
 * @author anisingh4
 */
@RunWith(MockitoJUnitRunner.class)
public class DataCleanUpServiceFactoryTest {

	@Mock
	private SonarDataCleanUpService sonarDataCleanupService;
	@Mock
	private ScmDataCleanUpService scmDataCleanupService;
	@Mock
	private BuildDataCleanUpService buildDataCleanupService;
	@Mock
	private ZephyrDataCleanUpService zephyrDataCleanUpService;

	@InjectMocks
	private ToolDataCleanUpServiceFactory dataCleanUpServiceFactory;

	@Spy
	private List<ToolDataCleanUpService> dataCleanUpServices = new ArrayList<>();

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);

		when(sonarDataCleanupService.getToolCategory()).thenReturn(ProcessorType.SONAR_ANALYSIS.toString());
		when(scmDataCleanupService.getToolCategory()).thenReturn(ProcessorType.SCM.toString());
		when(buildDataCleanupService.getToolCategory()).thenReturn(ProcessorType.BUILD.toString());
		when(zephyrDataCleanUpService.getToolCategory()).thenReturn(ProcessorType.TESTING_TOOLS.toString());

		dataCleanUpServices.add(sonarDataCleanupService);
		dataCleanUpServices.add(scmDataCleanupService);
		dataCleanUpServices.add(buildDataCleanupService);
		dataCleanUpServices.add(zephyrDataCleanUpService);

		dataCleanUpServiceFactory.initServices();
	}

	@Test(expected = NotImplementedException.class)
	public void getService_Jira() {
		dataCleanUpServiceFactory.getService(ProcessorConstants.JIRA);
	}

	@Test(expected = IllegalStateException.class)
	public void getService_UnknownTool() {
		dataCleanUpServiceFactory.getService("Test");
	}

	@Test(expected = NotImplementedException.class)
	public void getService_Azure() {
		dataCleanUpServiceFactory.getService(ProcessorConstants.AZURE);
	}

	@Test
	public void getService_Sonar() {
		ToolDataCleanUpService dataCleanUpService = dataCleanUpServiceFactory.getService(ProcessorConstants.SONAR);
		assertTrue(dataCleanUpService instanceof SonarDataCleanUpService);
	}

	@Test
	public void getService_Bamboo() {
		ToolDataCleanUpService dataCleanUpService = dataCleanUpServiceFactory.getService(ProcessorConstants.BAMBOO);
		assertTrue(dataCleanUpService instanceof BuildDataCleanUpService);
	}

	@Test
	public void getService_Jenkins() {
		ToolDataCleanUpService dataCleanUpService = dataCleanUpServiceFactory.getService(ProcessorConstants.JENKINS);
		assertTrue(dataCleanUpService instanceof BuildDataCleanUpService);
	}

	@Test
	public void getServiceTemcity() {
		ToolDataCleanUpService dataCleanUpService = dataCleanUpServiceFactory.getService(ProcessorConstants.TEAMCITY);
		assertTrue(dataCleanUpService instanceof BuildDataCleanUpService);
	}

	@Test
	public void getService_AzurePipeline() {
		ToolDataCleanUpService dataCleanUpService = dataCleanUpServiceFactory
				.getService(ProcessorConstants.AZUREPIPELINE);
		assertTrue(dataCleanUpService instanceof BuildDataCleanUpService);
	}

	@Test
	public void getService_BitBucket() {
		ToolDataCleanUpService dataCleanUpService = dataCleanUpServiceFactory.getService(ProcessorConstants.BITBUCKET);
		assertTrue(dataCleanUpService instanceof ScmDataCleanUpService);
	}

	@Test
	public void getService_Gitlab() {
		ToolDataCleanUpService dataCleanUpService = dataCleanUpServiceFactory.getService(ProcessorConstants.GITLAB);
		assertTrue(dataCleanUpService instanceof ScmDataCleanUpService);
	}

	@Test
	public void getService_AzureRepo() {
		ToolDataCleanUpService dataCleanUpService = dataCleanUpServiceFactory.getService(ProcessorConstants.AZUREREPO);
		assertTrue(dataCleanUpService instanceof ScmDataCleanUpService);
	}

	@Test
	public void getService_ZEPHYR() {
		ToolDataCleanUpService dataCleanUpService = dataCleanUpServiceFactory.getService(ProcessorConstants.ZEPHYR);
		assertTrue(dataCleanUpService instanceof ZephyrDataCleanUpService);
	}

	@Test
	public void getService_JIRATEST() {
		ToolDataCleanUpService dataCleanUpService = dataCleanUpServiceFactory.getService(ProcessorConstants.JIRA_TEST);
		assertTrue(dataCleanUpService instanceof ZephyrDataCleanUpService);
	}

}