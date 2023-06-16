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

package com.publicissapient.kpidashboard.jira.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.SubProjectConfig;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.IssueOfflineTraceLogsRepository;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientFactory;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraProcessor;
import com.publicissapient.kpidashboard.jira.processor.mode.ModeBasedProcessor;
import com.publicissapient.kpidashboard.jira.processor.mode.impl.offline.OfflineDataProcessorImpl;
import com.publicissapient.kpidashboard.jira.processor.mode.impl.online.OnlineDataProcessorImpl;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.AlphanumComparator;

@ExtendWith(SpringExtension.class)
public class JiraProcessorJobExecutorTest {

	private final ObjectId PROCESSORID = new ObjectId("5e16dc92f1aab3fbb1b198f3");
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;

	@Mock
	private JiraProcessorRepository issueProcessorRepository;

	@Mock
	private JiraProcessorConfig jiraProcessorConfig;

	@Mock
	private IssueOfflineTraceLogsRepository issueOfflineTraceLogsRepository;

	@Mock
	private FieldMappingRepository fieldMappingRepository;

	@Mock
	private JiraRestClientFactory jiraRestClientFactory;
	@Mock
	private AlphanumComparator alphanumComparator;

	@Mock
	private JiraIssueClientFactory jiraIssueClientFactory;

	@Spy
	private List<ModeBasedProcessor> modeBasedProcessors = new ArrayList<ModeBasedProcessor>();

	List<ModeBasedProcessor> list = new ArrayList<>();

	@InjectMocks
	JiraProcessorJobExecutor jiraProcessorJobExecutor;

	@InjectMocks
	OnlineDataProcessorImpl onlineDataProcessor;
	@InjectMocks
	OfflineDataProcessorImpl offlineDataProcessor;
	
	List<ProjectBasicConfig> projectConfigList = new ArrayList<>();

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		modeBasedProcessors.add(onlineDataProcessor);
		modeBasedProcessors.add(offlineDataProcessor);
		prepareProjectConfig();

		JiraProcessor processor = new JiraProcessor();
	}

	@Test
	public void getCronTest() {
		Mockito.when(jiraProcessorConfig.getCron()).thenReturn("* 0 0 0 0 0");
		Assert.assertEquals("* 0 0 0 0 0", jiraProcessorJobExecutor.getCron());
	}

	@Test
	public void getProcessorTest() {
		Assert.assertNotNull(jiraProcessorJobExecutor.getProcessor());
	}

	@Test
	public void getProcessorRepositoryTest() {
		Assert.assertEquals(issueProcessorRepository, jiraProcessorJobExecutor.getProcessorRepository());
	}

	@Test
	public void execute() {
		JiraProcessor jiraProcessor = new JiraProcessor();
		jiraProcessor.setId(PROCESSORID);
		Mockito.when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
		String currentDirectory = System.getProperty("user.dir");
		String localDir = currentDirectory + "\\src\\test\\resources\\offlineData";
		when(jiraProcessorConfig.getJsonFileName()).thenReturn(localDir);
		jiraProcessorJobExecutor.setProjectsBasicConfigIds(
				Arrays.asList("604092b52b424d5e90d39342","604092b52b424d5e90d39343", "604092b52b424d5e90d39344"));
		assertEquals(true, jiraProcessorJobExecutor.execute(jiraProcessor));
		jiraProcessorJobExecutor.setProjectsBasicConfigIds(null);
	}

	private void prepareProjectConfig() {
		ProjectBasicConfig projectConfig = new ProjectBasicConfig();

		projectConfig.setId(new ObjectId("604092b52b424d5e90d39342"));
		projectConfig.setProjectName("TEST Transformation Internal");
		SubProjectConfig subProjectConfig = new SubProjectConfig();
		subProjectConfig.setSubProjectIdentification("CustomField");
		subProjectConfig.setSubProjectIdentSingleValue("customfield_37903");
		List<SubProjectConfig> subProjectList = new ArrayList<>();
		subProjectList.add(subProjectConfig);
		ProjectBasicConfig jiraConfig = new ProjectBasicConfig();
		projectConfig.setIsKanban(false);
		projectConfigList.add(projectConfig);

		// offline Project config data
		projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("604092b52b424d5e90d39343"));
		List<String> dependsOn = new ArrayList<>();
		dependsOn.add("DEPLOY");
		projectConfig.setProjectName("DEPLOY");
		subProjectConfig = new SubProjectConfig();
		subProjectConfig.setSubProjectIdentification("CustomField");
		subProjectConfig.setSubProjectIdentSingleValue("customfield_37903");
		List<SubProjectConfig> subProjectList2 = new ArrayList<>();
		subProjectList.add(subProjectConfig);
		projectConfig.setIsKanban(false);
		projectConfigList.add(projectConfig);

		projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("604092b52b424d5e90d39344"));
		projectConfig.setProjectName("Test Tools Support");
		subProjectConfig = new SubProjectConfig();
		subProjectConfig.setSubProjectIdentification("CustomField");
		subProjectConfig.setSubProjectIdentSingleValue("customfield_20810");
		subProjectList = new ArrayList<>();
		subProjectList.add(subProjectConfig);
		projectConfig.setIsKanban(true);
		projectConfigList.add(projectConfig);

	}
}