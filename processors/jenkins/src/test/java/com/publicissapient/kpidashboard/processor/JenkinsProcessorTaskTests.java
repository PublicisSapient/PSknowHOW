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

package com.publicissapient.kpidashboard.processor;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jenkins.config.JenkinsConfig;
import com.publicissapient.kpidashboard.jenkins.factory.JenkinsClientFactory;
import com.publicissapient.kpidashboard.jenkins.model.JenkinsProcessor;
import com.publicissapient.kpidashboard.jenkins.processor.JenkinsProcessorJobExecutor;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.JenkinsClient;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.impl.JenkinsBuildClient;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.impl.JenkinsDeployClient;
import com.publicissapient.kpidashboard.jenkins.repository.JenkinsProcessorRepository;

@ExtendWith(SpringExtension.class)
public class JenkinsProcessorTaskTests {

	private static final String SERVER1 = "server1";
	private static final String NICENAME1 = "niceName1";
	private static final ProcessorToolConnection JENKINSSAMPLESERVER = new ProcessorToolConnection();
	@InjectMocks
	private JenkinsProcessorJobExecutor task;
	@Mock
	private TaskScheduler taskScheduler;
	@Mock
	private JenkinsProcessorRepository jenkinsProcessorRepository;
	@Mock
	private JenkinsClientFactory jenkinsClientFactory;
	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;
	@Mock
	private BuildRepository buildRepository;
	@Mock
	private JenkinsClient jenkinsClient;
	@Mock
	private JenkinsConfig jenkinsConfig;
	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;
	@Mock
	private AesEncryptionService aesEncryptionService;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	private List<ProcessorToolConnection> connList = new ArrayList<>();
	private List<ProcessorToolConnection> connList2 = new ArrayList<>();
	private List<ProcessorExecutionTraceLog> pl = new ArrayList<>();
	private ProjectBasicConfig projectConfig = new ProjectBasicConfig();
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
	private Optional<ProcessorExecutionTraceLog> optionalProcessorExecutionTraceLog;

	@BeforeEach
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		JenkinsProcessor processor = new JenkinsProcessor();
		processor.setId(new ObjectId("62171d0f26dd266803fa87da"));
		JENKINSSAMPLESERVER.setUrl("http://does:matter@jenkins.com");
		JENKINSSAMPLESERVER.setUsername("does");
		JENKINSSAMPLESERVER.setApiKey("matter");
		JENKINSSAMPLESERVER.setJobName("JOB1");
		JENKINSSAMPLESERVER.setJobType("build");
		JENKINSSAMPLESERVER.setBasicProjectConfigId(new ObjectId("624d5c9ed837fc14d40b3039"));
		JENKINSSAMPLESERVER.setId(new ObjectId("62171d0f26dd266803fa87da"));
		connList.add(JENKINSSAMPLESERVER);

		projectConfigList.add(projectConfig);
		projectConfig.setId(new ObjectId("624d5c9ed837fc14d40b3039"));
		projectConfig.setSaveAssigneeDetails(true);

		processorExecutionTraceLog.setProcessorName(ProcessorConstants.JENKINS);
		processorExecutionTraceLog.setLastSuccessfulRun("2023-02-06");
		processorExecutionTraceLog.setBasicProjectConfigId("624d5c9ed837fc14d40b3039");
		pl.add(processorExecutionTraceLog);
		optionalProcessorExecutionTraceLog = Optional.of(processorExecutionTraceLog);

		Mockito.when(jenkinsConfig.getCustomApiBaseUrl()).thenReturn("http://customapi:8080/");
		when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any())).thenReturn(connList);
	}

	@Test
	public void collect_noBuildServers_nothingAdded() {

		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any(), any())).thenReturn(new HashMap<ObjectId, Set<Build>>());
		when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.JENKINS,
				"624d5c9ed837fc14d40b3039")).thenReturn(optionalProcessorExecutionTraceLog);

		JenkinsProcessor jenkinsProcessor = new JenkinsProcessor();
		task.execute(jenkinsProcessor);
		verifyNoMoreInteractions(jenkinsClient, buildRepository);
	}

	@Test
	public void updateAssigneedetail() {

		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.JENKINS,
				"624d5c9ed837fc14d40b3039")).thenReturn(optionalProcessorExecutionTraceLog);
		when(client2.getBuildJobsFromServer(any(), any())).thenReturn(new HashMap<ObjectId, Set<Build>>());

		JenkinsProcessor jenkinsProcessor = new JenkinsProcessor();
		Build build = new Build();
		build.setNumber("1");
		build.setBuildUrl("JOB1_1_URL");
		build.setBasicProjectConfigId(new ObjectId("624d5c9ed837fc14d40b3039"));
		build.setStartedBy("TestUser");
		when(client2.getBuildJobsFromServer(any(), any()))
				.thenReturn(oneJobWithBuilds(JENKINSSAMPLESERVER.getId(), build));
		when(buildRepository.findByProjectToolConfigIdAndNumber(any(), any())).thenReturn(build);
		task.execute(jenkinsProcessor);

	}

	@Test
	public void collect_noJobsOnServer_nothingAdded() {

		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any(), any())).thenReturn(new HashMap<ObjectId, Set<Build>>());
		task.execute(processorWithOneServer());
		verifyNoMoreInteractions(jenkinsClient, buildRepository);
	}

	@Test
	public void testExecute() {
		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any(), any())).thenReturn(new HashMap<ObjectId, Set<Build>>());
		task.execute(processorWithOneServer());
		assertTrue(task.execute(processorWithOneServer()));
	}

	@Test
	public void testExecuteNullJobs() {
		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any(), any())).thenReturn(null);
		task.execute(processorWithOneServer());
		assertTrue(task.execute(processorWithOneServer()));
	}

	@Test
	public void collect_twoJobs_jobsAdded() {

		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setBasicProjectConfigId("62171d0f26dd266803fa87da");
		JenkinsClient client2 = mock(JenkinsClient.class);
		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		pl.add(processorExecutionTraceLog);
		projectConfig.setId(new ObjectId("62171d0f26dd266803fa87da"));
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any(), any()))
				.thenReturn(twoJobsWithTwoBuilds(JENKINSSAMPLESERVER.getId()));
		when(processorExecutionTraceLogService.getTraceLogs("Jenkins", "62171d0f26dd266803fa87da")).thenReturn(pl);
		task.execute(processorWithOneServer());
		assertTrue(task.execute(processorWithOneServer()));
	}

	@Test
	public void collect_twoJobs_jobsAdded_random_order() {

		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(Mockito.any(), any()))
				.thenReturn(twoJobsWithTwoBuildsRandom(JENKINSSAMPLESERVER.getId()));
		task.execute(processorWithOneServer());
		assertTrue(task.execute(processorWithOneServer()));
	}

	@Test
	public void collect_oneJob_exists_notAdded() {
		JenkinsProcessor processor = processorWithOneServer();
		Build build = new Build();
		build.setNumber("1");
		build.setBuildUrl("JOB1_1_URL");
		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any(), any()))
				.thenReturn(oneJobWithBuilds(JENKINSSAMPLESERVER.getId(), build));

		task.execute(processor);
		assertTrue(task.execute(processor));
	}

	@Test
	public void collect_jobNotEnabled_buildNotAdded() {
		JenkinsProcessor processor = processorWithOneServer();
		Build build = build("1", "JOB1_1_URL");
		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(jenkinsClient.getBuildJobsFromServer(JENKINSSAMPLESERVER, projectConfig))
				.thenReturn(oneJobWithBuilds(JENKINSSAMPLESERVER.getId(), build));
		task.execute(processor);
		verify(buildRepository, never()).save(build);
	}

	@Test
	public void collect_jobEnabled_buildExists_buildNotAdded() {
		JenkinsProcessor processor = processorWithOneServer();

		Build build = build("1", "JOB1_1_URL");
		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(JENKINSSAMPLESERVER, projectConfig))
				.thenReturn(oneJobWithBuilds(JENKINSSAMPLESERVER.getId(), build));

		when(buildRepository.findByProjectToolConfigIdAndNumber(JENKINSSAMPLESERVER.getId(), build.getNumber()))
				.thenReturn(build);
		task.execute(processor);

		verify(buildRepository, never()).save(build);
	}

	@Test
	public void collect_jobEnabled_newBuild_buildAdded() {
		JenkinsProcessor processor = processorWithOneServer();
		Build build = build("1", "JOB1_1_URL");
		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any(), any()))
				.thenReturn(oneJobWithBuilds(JENKINSSAMPLESERVER.getId(), build));
		when(buildRepository.findByProjectToolConfigIdAndNumber(JENKINSSAMPLESERVER.getId(), build.getNumber()))
				.thenReturn(null);
		assertTrue(task.execute(processor));
	}

	@Test
	public void collect_clean() {
		JenkinsProcessor processor = processorWithOneServer();

		ObjectId id = ObjectId.get();
		processor.setId(id);

		Map<ProcessorType, List<ProcessorItem>> processorItem = new HashMap<ProcessorType, List<ProcessorItem>>();
		processorItem.put(ProcessorType.BUILD, Arrays.asList(getProcessorItems(id)));
		JenkinsClient client2 = mock(JenkinsClient.class);
		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any(), any())).thenReturn(new HashMap<ObjectId, Set<Build>>());

		assertTrue(task.execute(processor));

	}

	@Test
	public void collect_enable_Job() {

		JenkinsClient client2 = mock(JenkinsClient.class);

		when(jenkinsClientFactory.getJenkinsClient("build")).thenReturn(client2);
		when(client2.getBuildJobsFromServer(any(), any()))
				.thenReturn(twoJobsWithTwoBuilds(JENKINSSAMPLESERVER.getId()));

		when(processorToolConnectionService.findByToolAndBasicProjectConfigId("Jenkins",
				new ObjectId("62171d0f26dd266803fa87da"))).thenReturn(connList);
		task.execute(processorWithOneServer());
		assertTrue(task.execute(processorWithOneServer()));
	}

	@Test
	public void CheckJenkinsClientSelector() {
		JenkinsBuildClient jenkinsClientMock = mock(JenkinsBuildClient.class);
		JenkinsDeployClient jenkins2ClientMock = mock(JenkinsDeployClient.class);
		JenkinsClientFactory clientSelector = new JenkinsClientFactory(jenkinsClientMock, jenkins2ClientMock);

		assertSame(clientSelector.getJenkinsClient("build"), jenkinsClientMock);
		assertSame(clientSelector.getJenkinsClient("deploy"), jenkins2ClientMock);
	}

	private JenkinsProcessor processorWithOneServer() {
		return JenkinsProcessor.buildProcessor();
	}

	private ProcessorItem getProcessorItems(ObjectId id) {
		ProcessorItem item = new ProcessorItem();
		item.setProcessorId(id);
		return item;
	}

	private Map<ObjectId, Set<Build>> oneJobWithBuilds(ObjectId job, Build... builds) {
		Map<ObjectId, Set<Build>> jobs = new HashMap<>();
		jobs.put(job, Sets.newHashSet(builds));
		return jobs;
	}

	private Map<ObjectId, Set<Build>> twoJobsWithTwoBuilds(ObjectId server) {
		Map<ObjectId, Set<Build>> jobs = new HashMap<>();
		jobs.put(server, Sets.newHashSet(build("1", "JOB1_1_URL"), build("1", "JOB1_2_URL")));
		jobs.put(server, Sets.newHashSet(build("2", "JOB2_1_URL"), build("2", "JOB2_2_URL")));
		return jobs;
	}

	private Map<ObjectId, Set<Build>> twoJobsWithTwoBuildsRandom(ObjectId server) {
		Map<ObjectId, Set<Build>> jobs = new HashMap<>();
		jobs.put(server, Sets.newHashSet(build("2", "JOB2_1_URL"), build("2", "JOB2_2_URL")));
		jobs.put(server, Sets.newHashSet(build("1", "JOB1_1_URL"), build("1", "JOB1_2_URL")));
		return jobs;
	}

	private Build build(String number, String url) {
		Build build = new Build();
		build.setNumber(number);
		build.setBuildUrl(url);
		build.setBasicProjectConfigId(new ObjectId("624d5c9ed837fc14d40b3039"));
		build.setStartedBy("TestUser");
		return build;
	}

	private List<ProjectToolConfig> jenkinsJob() {
		List<ProjectToolConfig> toolList = new ArrayList<>();
		ProjectToolConfig t1 = new ProjectToolConfig();
		t1.setToolName("Jenkins");
		t1.setJobName("1");
		t1.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));
		toolList.add(t1);
		return toolList;
	}

	private Optional<Connection> jenkinsConnection() {
		Optional<Connection> conn = Optional.of(new Connection());
		conn.get().setBaseUrl("http://does:matter@jenkins.com");
		conn.get().setUsername("does");
		conn.get().setApiKey("matter");
		conn.get().setId(new ObjectId("5f9014743cb73ce896167658"));
		return conn;
	}

}