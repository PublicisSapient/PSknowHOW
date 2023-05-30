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

package com.publicissapient.kpidashboard.githubaction.processor;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.githubaction.config.GitHubActionConfig;
import com.publicissapient.kpidashboard.githubaction.customexception.FetchingBuildException;
import com.publicissapient.kpidashboard.githubaction.factory.GitHubActionClientFactory;
import com.publicissapient.kpidashboard.githubaction.model.GitHubActionProcessor;
import com.publicissapient.kpidashboard.githubaction.processor.adapter.GitHubActionClient;
import com.publicissapient.kpidashboard.githubaction.repository.GitHubProcessorRepository;

@SuppressWarnings("java:S5786")
@ExtendWith(SpringExtension.class)
public class GitHubActionProcessorJobExecutorTest {

	private static final ProcessorToolConnection GITHUBSAMPLESERVER = new ProcessorToolConnection();
	@InjectMocks
	private GitHubActionProcessorJobExecutor gitHubActionProcessorJobExecutor;
	@Mock
	private GitHubActionConfig gitHubActionConfig;
	@Mock
	private GitHubProcessorRepository gitHubActionProcessorRepository;
	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;
	@Mock
	private GitHubActionClientFactory gitHubActionClientFactory;
	@Mock
	private BuildRepository buildRepository;
	@Mock
	private DeploymentRepository deploymentRepository;
	private List<ProcessorToolConnection> connList = new ArrayList<>();
	private ProjectBasicConfig projectConfig = new ProjectBasicConfig();
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
	private Optional<ProcessorExecutionTraceLog> optionalProcessorExecutionTraceLog;
	private List<ProcessorExecutionTraceLog> pl = new ArrayList<>();

	@BeforeEach
	public void initMocks() {
		MockitoAnnotations.initMocks(this);

		GitHubActionProcessor gitHubActionProcessor = new GitHubActionProcessor();
		gitHubActionProcessor.setId(new ObjectId("62171d0f26dd266803fa87da"));
		GITHUBSAMPLESERVER.setUrl("https://api.github.com");
		GITHUBSAMPLESERVER.setUsername("does");
		GITHUBSAMPLESERVER.setApiKey("matter");
		GITHUBSAMPLESERVER.setJobName("JOB1");
		GITHUBSAMPLESERVER.setJobType("build");
		GITHUBSAMPLESERVER.setBasicProjectConfigId(new ObjectId("624d5c9ed837fc14d40b3039"));
		GITHUBSAMPLESERVER.setId(new ObjectId("62171d0f26dd266803fa87da"));
		connList.add(GITHUBSAMPLESERVER);

		projectConfig.setId(new ObjectId("624d5c9ed837fc14d40b3039"));
		projectConfig.setSaveAssigneeDetails(false);
		projectConfigList.add(projectConfig);

		processorExecutionTraceLog.setProcessorName(ProcessorConstants.GITHUBACTION);
		processorExecutionTraceLog.setLastSuccessfulRun("2023-02-06");
		processorExecutionTraceLog.setBasicProjectConfigId("624d5c9ed837fc14d40b3039");
		pl.add(processorExecutionTraceLog);
		optionalProcessorExecutionTraceLog = Optional.of(processorExecutionTraceLog);

		Mockito.when(gitHubActionConfig.getCustomApiBaseUrl()).thenReturn("http://customapi:8080/");
		when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any())).thenReturn(connList);
	}

	@SuppressWarnings("java:S2699")
	@Test
	public void buildJobsAdded() throws FetchingBuildException {

		GitHubActionClient client2 = mock(GitHubActionClient.class);
		when(gitHubActionClientFactory.getGitHubActionClient("build")).thenReturn(client2);
		when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigId(
				ProcessorConstants.GITHUBACTION, "624d5c9ed837fc14d40b3039"))
						.thenReturn(optionalProcessorExecutionTraceLog);
		when(client2.getBuildJobsFromServer(any(), any())).thenReturn(new LinkedHashSet<>());

		GitHubActionProcessor gitHubActionProcessor = new GitHubActionProcessor();
		Build build = new Build();
		build.setNumber("1");
		build.setBuildUrl("JOB1_1_URL");
		build.setBasicProjectConfigId(new ObjectId("624d5c9ed837fc14d40b3039"));
		build.setStartedBy("TestUser");
		List<Build> builds = new ArrayList<>();
		builds.add(build);
		when(client2.getBuildJobsFromServer(any(), any())).thenReturn(oneJobWithBuilds(build));
		when(buildRepository.findByProjectToolConfigIdAndNumberIn(any(), any())).thenReturn(builds);

		projectConfig.setId(new ObjectId("624d5c9ed837fc14d40b3039"));
		projectConfig.setSaveAssigneeDetails(false);
		projectConfigList.add(projectConfig);
		when(projectConfigRepository.findAll()).thenReturn(projectConfigList);

		gitHubActionProcessorJobExecutor.execute(gitHubActionProcessor);
		assertTrue(gitHubActionProcessorJobExecutor.execute(gitHubActionProcessor));

	}

	private Set<Build> oneJobWithBuilds(Build builds) {
		Set<Build> jobs = new LinkedHashSet<>();
		jobs.add(builds);
		return jobs;
	}

}
