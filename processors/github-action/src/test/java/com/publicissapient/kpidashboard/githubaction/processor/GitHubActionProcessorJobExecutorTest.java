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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
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

	private ProcessorToolConnection githubSampleServer = new ProcessorToolConnection();
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
	@Mock
	private RestTemplate restTemplate;
	private List<ProcessorToolConnection> connList = new ArrayList<>();
	private ProjectBasicConfig projectConfig = new ProjectBasicConfig();
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
	private Optional<ProcessorExecutionTraceLog> optionalProcessorExecutionTraceLog;
	private List<ProcessorExecutionTraceLog> pl = new ArrayList<>();
	GitHubActionClient client2;

	@BeforeEach
	public void initMocks() {
		MockitoAnnotations.openMocks(this);
		client2 = mock(GitHubActionClient.class);
		GitHubActionProcessor gitHubActionProcessor = new GitHubActionProcessor();
		gitHubActionProcessor.setId(new ObjectId("62171d0f26dd266803fa87da"));
		githubSampleServer.setUrl("https://api.github.com");
		githubSampleServer.setUsername("does");
		githubSampleServer.setApiKey("matter");
		githubSampleServer.setJobName("JOB1");
		githubSampleServer.setJobType("build");
		githubSampleServer.setBasicProjectConfigId(new ObjectId("624d5c9ed837fc14d40b3039"));
		githubSampleServer.setId(new ObjectId("62171d0f26dd266803fa87da"));
		connList.add(githubSampleServer);

		projectConfig.setId(new ObjectId("624d5c9ed837fc14d40b3039"));
		projectConfig.setSaveAssigneeDetails(true);
		projectConfigList.add(projectConfig);

		processorExecutionTraceLog.setProcessorName(ProcessorConstants.GITHUBACTION);
		processorExecutionTraceLog.setLastSuccessfulRun("2023-02-06");
		processorExecutionTraceLog.setBasicProjectConfigId("624d5c9ed837fc14d40b3039");
		pl.add(processorExecutionTraceLog);
		optionalProcessorExecutionTraceLog = Optional.of(processorExecutionTraceLog);

		Mockito.when(gitHubActionConfig.getCustomApiBaseUrl()).thenReturn("http://customapi:8080/");
		when(projectConfigRepository.findActiveProjects(anyBoolean())).thenReturn(projectConfigList);
		doThrow(RestClientException.class).when(restTemplate).exchange(ArgumentMatchers.anyString(),
				ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(String.class));

		when(gitHubActionClientFactory.getGitHubActionClient("build")).thenReturn(client2);
		when(processorExecutionTraceLogRepository
				.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.GITHUBACTION, "624d5c9ed837fc14d40b3039"))
				.thenReturn(optionalProcessorExecutionTraceLog);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any())).thenReturn(connList);
	}

	@SuppressWarnings("java:S2699")
	@Test
	public void buildJobsAdded() throws FetchingBuildException {

		GitHubActionProcessor gitHubActionProcessor = new GitHubActionProcessor();
		Build build = new Build();
		build.setNumber("1");
		build.setBuildUrl("JOB1_1_URL");
		build.setBasicProjectConfigId(new ObjectId("624d5c9ed837fc14d40b3039"));
		build.setStartedBy("TestUser");
		Build build2 = new Build();
		build2.setBuildUrl("JOB1_1_URL");
		build2.setBasicProjectConfigId(new ObjectId("624d5c9ed837fc14d40b3039"));
		build2.setStartedBy("TestUser");
		List<Build> builds = new ArrayList<>();
		builds.add(build);
		when(buildRepository.findByProjectToolConfigIdAndNumberIn(any(), any())).thenReturn(builds);
		when(client2.getBuildJobsFromServer(any(), any())).thenReturn(oneJobWithBuilds(build2));
		projectConfig.setId(new ObjectId("624d5c9ed837fc14d40b3039"));
		projectConfig.setSaveAssigneeDetails(false);
		projectConfigList.add(projectConfig);
		when(projectConfigRepository.findActiveProjects(anyBoolean())).thenReturn(projectConfigList);

		gitHubActionProcessorJobExecutor.execute(gitHubActionProcessor);
		assertTrue(gitHubActionProcessorJobExecutor.execute(gitHubActionProcessor));
	}

	@Test
	public void buildJobsAdded2() throws FetchingBuildException {
		when(gitHubActionClientFactory.getGitHubActionClient("build")).thenReturn(client2);
		when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigId(
						ProcessorConstants.GITHUBACTION, "624d5c9ed837fc14d40b3039"))
				.thenReturn(optionalProcessorExecutionTraceLog);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
				.thenReturn(connList);

		GitHubActionProcessor gitHubActionProcessor = new GitHubActionProcessor();
		Build build = new Build();
		build.setNumber("2");
		build.setBuildUrl("JOB1_1_URL");
		build.setBasicProjectConfigId(new ObjectId("624d5c9ed837fc14d40b3039"));
		List<Build> builds = new ArrayList<>();
		builds.add(build);
		Build build2 = new Build();
		build2.setNumber("2");
		build2.setBuildUrl("JOB1_1_URL");
		build2.setBasicProjectConfigId(new ObjectId("624d5c9ed837fc14d40b3039"));
		build2.setStartedBy("TestUser");
		when(client2.getBuildJobsFromServer(any(), any())).thenReturn(oneJobWithBuilds(build2));
		when(buildRepository.findByProjectToolConfigIdAndNumberIn(any(), any())).thenReturn(builds);

		gitHubActionProcessorJobExecutor.execute(gitHubActionProcessor);
		assertTrue(gitHubActionProcessorJobExecutor.execute(gitHubActionProcessor));
	}

	@Test
	public void buildJobsAddedThrowsException() throws FetchingBuildException {

		GitHubActionClient client2 = mock(GitHubActionClient.class);
		when(gitHubActionClientFactory.getGitHubActionClient("build")).thenReturn(client2);
		GitHubActionProcessor gitHubActionProcessor = new GitHubActionProcessor();
		when(client2.getBuildJobsFromServer(any(), any())).thenThrow(FetchingBuildException.class);
		try {
			gitHubActionProcessorJobExecutor.execute(gitHubActionProcessor);
		} catch (Exception ex) {
		}

		assertFalse(gitHubActionProcessorJobExecutor.execute(gitHubActionProcessor));
	}

	@Test
	public void deployJobsAdded() throws FetchingBuildException {

		GitHubActionClient client2 = mock(GitHubActionClient.class);
		when(gitHubActionClientFactory.getGitHubActionClient("deploy")).thenReturn(client2);
		when(client2.getDeployJobsFromServer(any(), any())).thenReturn(new HashMap<>());

		GitHubActionProcessor gitHubActionProcessor = new GitHubActionProcessor();
		Deployment build = new Deployment();
		build.setEnvUrl("JOB1_Env_URL");
		build.setBasicProjectConfigId(new ObjectId("624d5c9ed837fc14d40b3039"));
		build.setDeployedBy("TestUser");
		build.setProjectToolConfigId(new ObjectId("62171d0f26dd266803fa87da"));
		Deployment build2 = new Deployment();
		build2.setNumber("1");
		build2.setEnvUrl("JOB1_Env_URL");
		build2.setProjectToolConfigId(new ObjectId("62171d0f26dd266803fa87da"));
		build2.setBasicProjectConfigId(new ObjectId("624d5c9ed837fc14d40b3039"));
		build2.setDeployedBy("TestUser");
		Set<Deployment> builds = new HashSet<>();
		builds.add(build);
		Map<Deployment, Set<Deployment>> deploymentsByJob = new HashMap<>();
		deploymentsByJob.put(build2, builds);
		when(client2.getDeployJobsFromServer(any(), any())).thenReturn(deploymentsByJob);
		when(deploymentRepository.findByProcessorIdIn(any())).thenReturn(new ArrayList<>(builds));
		connList.forEach(conn -> conn.setJobType("deploy"));
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any())).thenReturn(connList);
		projectConfig.setId(new ObjectId("624d5c9ed837fc14d40b3039"));
		projectConfig.setSaveAssigneeDetails(false);
		projectConfigList.add(projectConfig);
		when(projectConfigRepository.findActiveProjects(anyBoolean())).thenReturn(projectConfigList);

		gitHubActionProcessorJobExecutor.execute(gitHubActionProcessor);
		assertTrue(gitHubActionProcessorJobExecutor.execute(gitHubActionProcessor));
	}

	private Set<Build> oneJobWithBuilds(Build builds) {
		Set<Build> jobs = new LinkedHashSet<>();
		builds.setNumber("2");
		jobs.add(builds);
		return jobs;
	}
}
