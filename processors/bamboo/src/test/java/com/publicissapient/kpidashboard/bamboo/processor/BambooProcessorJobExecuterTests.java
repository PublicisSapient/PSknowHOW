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

package com.publicissapient.kpidashboard.bamboo.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.web.client.RestClientException;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.bamboo.client.BambooClient;
import com.publicissapient.kpidashboard.bamboo.client.impl.BambooClientBuildImpl;
import com.publicissapient.kpidashboard.bamboo.client.impl.BambooClientDeployImpl;
import com.publicissapient.kpidashboard.bamboo.config.BambooConfig;
import com.publicissapient.kpidashboard.bamboo.factory.BambooClientFactory;
import com.publicissapient.kpidashboard.bamboo.model.BambooProcessor;
import com.publicissapient.kpidashboard.bamboo.repository.BambooProcessorRepository;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
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
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;

@RunWith(MockitoJUnitRunner.class)
public class BambooProcessorJobExecuterTests {

	private static final String EXCEPTION = "rest client exception";
	private static final String JOB2_URL = "JOB2_URL";
	private static final String JOB1_1_URL = "JOB1_1_URL";
	private static final String JOB1_URL = "JOB1_URL";
	private static final String HTTP_URL = "http://does:matter@bamboo.com";
	private static final String SERVER1 = "server1";
	private static final List<ProcessorToolConnection> pt = new ArrayList<>();
	private static final List<ProcessorExecutionTraceLog> petl = new ArrayList<>();
	private static final List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private static final List<Deployment> deploymentList = new ArrayList<>();
	private static final List<Deployment> queuedDeploymentList = new ArrayList<>();
	private static final List<Deployment> serverList = new ArrayList<>();
	private static final List<Deployment> maxDeployment = new ArrayList<>();
	private static final Set<Build> buildSet = new HashSet<>();
	private static final ProcessorToolConnection BAMBOOSAMPLESERVER = new ProcessorToolConnection();// new
	// BambooServer(HTTP_URL,
	// "", "does",
	// "matter");
	private static final ProcessorToolConnection BAMBOOSAMPLESERVER1 = new ProcessorToolConnection();// new
	private static final ProcessorToolConnection BAMBOOSAMPLESERVER2 = new ProcessorToolConnection();// new
	Deployment deployment3 = new Deployment();
	Deployment deployment1 = new Deployment();
	Deployment deployment2 = new Deployment();
	Deployment deployment = new Deployment();
	private Optional<ProcessorExecutionTraceLog> processorExecutionTraceLogs;
	@Mock
	private BuildRepository buildRepository;
	@Mock
	private BambooClient bambooClient;
	@Mock
	private BambooProcessorRepository bambooProcessorRepository;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private DeploymentRepository deploymentRepository;
	@Mock
	private BambooConfig bambooConfig;
	@Mock
	private AesEncryptionService aesEncryptionService;
	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;
	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;
	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Mock
	private BambooClientFactory bambooClientFactory;
	@Mock
	private BambooClientBuildImpl bambooClientBuild;
	@Mock
	private BambooClientDeployImpl bambooClientDeploy;
	@InjectMocks
	private BambooProcessorJobExecuter task;
	private Optional<ProcessorExecutionTraceLog> optionalProcessorExecutionTraceLog;
	private ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		BambooProcessor bambooProcessor = new BambooProcessor();
		Mockito.when(bambooConfig.getCustomApiBaseUrl()).thenReturn("http://customapi:8080/");

		BAMBOOSAMPLESERVER.setId(new ObjectId("6296661b307f0239477f1e9e"));
		BAMBOOSAMPLESERVER.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		BAMBOOSAMPLESERVER.setJobName("IN");
		BAMBOOSAMPLESERVER.setBranch("branch");
		BAMBOOSAMPLESERVER.setToolName("Bamboo");
		BAMBOOSAMPLESERVER.setConnectionId(new ObjectId("5fa69f5d220038d6a365fec6"));
		BAMBOOSAMPLESERVER.setConnectionName("Bamboo connection");
		BAMBOOSAMPLESERVER.setUrl(HTTP_URL);
		BAMBOOSAMPLESERVER.setUsername("dummyUsername");
		BAMBOOSAMPLESERVER.setPassword("dummyPassword");
		BAMBOOSAMPLESERVER.setJobType("build");

		BAMBOOSAMPLESERVER1.setId(new ObjectId("6296661b307f0239477f1e9e"));
		BAMBOOSAMPLESERVER1.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		BAMBOOSAMPLESERVER1.setJobName("IN");
		BAMBOOSAMPLESERVER1.setBranch("branch");
		BAMBOOSAMPLESERVER1.setToolName("Bamboo");
		BAMBOOSAMPLESERVER1.setConnectionId(new ObjectId("5fa69f5d220038d6a365fec6"));
		BAMBOOSAMPLESERVER1.setConnectionName("Bamboo connection");
		BAMBOOSAMPLESERVER1.setUrl(HTTP_URL);
		BAMBOOSAMPLESERVER1.setUsername("does");
		BAMBOOSAMPLESERVER1.setPassword(null);
		BAMBOOSAMPLESERVER1.setJobType("build");

		BAMBOOSAMPLESERVER2.setId(new ObjectId("6296661b307f0239477f1e9e"));// toolId
		BAMBOOSAMPLESERVER2.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		BAMBOOSAMPLESERVER2.setJobName("IN");
		BAMBOOSAMPLESERVER2.setBranch("branch");
		BAMBOOSAMPLESERVER2.setToolName("Bamboo");
		BAMBOOSAMPLESERVER2.setConnectionId(new ObjectId("5fa69f5d220038d6a365fec6"));
		BAMBOOSAMPLESERVER2.setConnectionName("Bamboo connection");
		BAMBOOSAMPLESERVER2.setUrl(HTTP_URL);
		BAMBOOSAMPLESERVER2.setUsername("does");
		BAMBOOSAMPLESERVER2.setPassword(null);
		BAMBOOSAMPLESERVER2.setJobType("deploy");
		BAMBOOSAMPLESERVER2.setDeploymentProjectName("TestDep");
		BAMBOOSAMPLESERVER2.setDeploymentProjectId("190709761");

		pt.add(BAMBOOSAMPLESERVER);
		pt.add(BAMBOOSAMPLESERVER1);
		pt.add(BAMBOOSAMPLESERVER2);

		ProjectBasicConfig basicConfig = new ProjectBasicConfig();
		basicConfig.setId(new ObjectId("60b7dbb489c5974a407e923b"));
		basicConfig.setId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		basicConfig.setSaveAssigneeDetails(true);
		projectConfigList.add(basicConfig);

		deployment.setProcessorId(new ObjectId("62285e83171b4d183e9bdb0c"));
		deployment.setProjectToolConfigId(new ObjectId("6296661b307f0239477f1e9e"));
		deployment.setBasicProjectConfigId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		deployment.setEnvId("190775300");
		deployment.setStartTime("1970-01-01T00:00:00.000Z");
		deployment.setEndTime("1970-01-01T00:00:00.000Z");
		deployment.setDeploymentStatus(DeploymentStatus.IN_PROGRESS);
		deployment.setJobId("190709761");
		deployment.setNumber("189988914");
		deployment.setJobName("TestDep");
		deployment.setDeployedBy("user1");

		deployment1.setProcessorId(new ObjectId("62285e83171b4d183e9bdb0c"));
		deployment1.setProjectToolConfigId(new ObjectId("6296661b307f0239477f1e9e"));
		deployment1.setBasicProjectConfigId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		deployment1.setEnvId("190775300");
		deployment1.setStartTime("2022-06-02T14:38:54.000Z");
		deployment1.setEndTime("2022-06-02T14:38:54.000Z");
		deployment1.setDeploymentStatus(DeploymentStatus.SUCCESS);
		deployment1.setJobId("190709761");
		deployment1.setNumber("189988914");
		deployment1.setJobName("TestDep");
		deployment1.setDeployedBy("user2");

		deployment2.setProcessorId(new ObjectId("62285e83171b4d183e9bdb0c"));
		deployment2.setProjectToolConfigId(new ObjectId("6706661b307f0239477f1e9e"));
		deployment2.setBasicProjectConfigId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		deployment2.setEnvId("190775300");
		deployment2.setStartTime("1970-01-01T00:00:00.000Z");
		deployment2.setEndTime("1970-01-01T00:00:00.000Z");
		deployment2.setDeploymentStatus(DeploymentStatus.IN_PROGRESS);
		deployment2.setJobId("190709761");
		deployment2.setNumber("189988914");
		deployment2.setJobName("TestDep");
		deployment2.setDeployedBy("user3");

		deployment3.setProcessorId(new ObjectId("62285e83171b4d183e9bdb0c"));
		deployment3.setProjectToolConfigId(new ObjectId("6296661b307f0239477f1e9e"));
		deployment3.setBasicProjectConfigId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		deployment3.setEnvId("190775300");
		deployment3.setEnvId("190775300");
		deployment3.setStartTime("2022-06-02T14:39:08.000Z");
		deployment3.setEndTime("2022-06-02T14:39:13.000Z");
		deployment3.setDeploymentStatus(DeploymentStatus.SUCCESS);
		deployment3.setJobId("190709761");
		deployment3.setNumber("189988914");
		deployment3.setJobName("TestDep");
		queuedDeploymentList.add(deployment);
		deploymentList.add(deployment);
		deploymentList.add(deployment1);
		deploymentList.add(deployment2);
		serverList.add(deployment);
		serverList.add(deployment1);
		maxDeployment.add(deployment);
		maxDeployment.add(deployment1);
		maxDeployment.add(deployment3);

		Build build1 = new Build();
		build1.setId(new ObjectId("63c6801d6bf36f4ba6f1ab4c"));
		build1.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		build1.setProjectToolConfigId(new ObjectId("6296661b307f0239477f1e9e"));
		build1.setBuildJob("BambooJob1");
		build1.setNumber("123");
		build1.setBuildUrl(JOB1_1_URL);
		build1.setStartTime(1673913622000L);
		build1.setEndTime(1673913752608L);
		build1.setDuration(130608L);
		build1.setBuildStatus(BuildStatus.FAILURE);

		Build build2 = new Build();
		build2.setId(new ObjectId("63c6801d6bf36f4ba6f1ab4c"));
		build2.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		build2.setProjectToolConfigId(new ObjectId("6296661b307f0239477f1e9e"));
		build2.setBuildJob("BambooJob1");
		build2.setNumber("222");
		build2.setBuildUrl(JOB1_1_URL);
		build2.setStartTime(1673913622000L);
		build2.setEndTime(1673913752608L);
		build2.setDuration(130608L);
		build2.setBuildStatus(BuildStatus.SUCCESS);

		Build build3 = new Build();
		build3.setId(new ObjectId("63c6801d6bf36f4ba6f1ab4c"));
		build3.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		build3.setProjectToolConfigId(new ObjectId("6296661b307f0239477f1e9e"));
		build3.setBuildJob("BambooJob2");
		build3.setNumber("333");
		build3.setBuildUrl(JOB2_URL);
		build3.setStartTime(1673913622000L);
		build3.setEndTime(1673913752608L);
		build3.setDuration(130608L);
		build3.setBuildStatus(BuildStatus.SUCCESS);

		buildSet.add(build1);
		buildSet.add(build2);
		buildSet.add(build3);

		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setId(new ObjectId("63d7d2c124f5327fc7f9ac35"));
		processorExecutionTraceLog.setLastSuccessfulRun("2023-02-06");
		processorExecutionTraceLog.setLastEnableAssigneeToggleState(true);
		processorExecutionTraceLog.setProcessorName("Bamboo");
		processorExecutionTraceLog.setBasicProjectConfigId("5f9014743cb73ce896167659");
		optionalProcessorExecutionTraceLog = Optional.of(processorExecutionTraceLog);

	}

	@Test
	public void collectNoBuildServersNothingAdded() {
		task.execute(BambooProcessor.prototype());
		verifyNoMoreInteractions(bambooClient, buildRepository);
	}

	@Test
	public void testClean() throws MalformedURLException, ParseException {
		task.execute(processorWithOneServer());
		verifyNoMoreInteractions(bambooClient, buildRepository);
	}

	@Test
	public void collectJobsAdded() throws MalformedURLException, ParseException {
		try {
			Map<ObjectId, Set<Build>> jobs = new HashMap<>();
			jobs.put(new ObjectId("6296661b307f0239477f1e9e"), buildSet);
			when(bambooClient.getJobsFromServer(any(), any())).thenReturn(jobs);
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(twoBambooJob());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientBuild);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void collectJobsAddedWithNewJob() throws MalformedURLException, ParseException {

		try {
			Map<ObjectId, Set<Build>> jobs = new HashMap<>();
			jobs.put(new ObjectId("6296661b307f0239477f1e9e"), buildSet);
			when(bambooClient.getJobsFromServer(any(), any())).thenReturn(jobs);
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(deploymentRepository.findAll()).thenReturn(deploymentList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any())).thenReturn(pt);
			when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigId(any(), any()))
					.thenReturn(processorExecutionTraceLogs);
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientBuild);
			when(processorExecutionTraceLogRepository
					.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.BAMBOO, "5f9014743cb73ce896167659"))
							.thenReturn(optionalProcessorExecutionTraceLog);
			when(deploymentRepository.findByProjectToolConfigIdAndNumber(any(), any())).thenReturn(deployment);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void collectTwoJobsJobsAddedRandomOrder() throws MalformedURLException, ParseException {
		try {
			BambooProcessor processor = processorWithOneServer();

			Build build = build("1", JOB1_1_URL);
			Map<ObjectId, Set<Build>> buildMap = new HashMap<>();
			buildMap.put(new ObjectId("6296661b307f0239477f1e9e"), buildSet);
			when(bambooClient.getJobsFromServer(any(), any())).thenReturn(buildMap);
			when(bambooClient.getBuildDetailsFromServer(any(), any(), any())).thenReturn(build);
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(deploymentRepository.findAll()).thenReturn(deploymentList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any())).thenReturn(pt);
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientBuild);
			task.execute(processor);
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}

	}

	@Test
	public void collectJobNotEnabledBuildNotAdded() throws MalformedURLException, ParseException {
		BambooProcessor processor = processorWithOneServer();
		Build build = build("1", JOB1_1_URL);

		task.execute(processor);

		verify(buildRepository, never()).save(build);
	}

	@Test
	public void collectJobEnabledBuildExistsBuildNotAdded() throws MalformedURLException, ParseException {
		BambooProcessor processor = processorWithOneServer();
		Build build = build("1", JOB1_1_URL);
		task.execute(processor);

		verify(buildRepository, never()).save(build);
	}

	@Test
	public void collectJobEnabledNewBuildBuildAdded() throws MalformedURLException, ParseException {
		try {
			BambooProcessor processor = processorWithOneServer();
			Build build = build("1", JOB1_1_URL);

			Map<ObjectId, Set<Build>> buildMap = new HashMap<>();
			buildMap.put(new ObjectId("6296661b307f0239477f1e9e"), buildSet);
			when(bambooClient.getJobsFromServer(any(), any())).thenReturn(buildMap);
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(twoBambooJob());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientBuild);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}

	}

	@Test
	public void testAddNewBuildsInfoToDb_buildnull_success() throws Exception {
		try {
			BambooProcessor processor = processorWithOneServer();
			Map<ObjectId, Set<Build>> buildMap = new HashMap<>();
			buildMap.put(new ObjectId("6296661b307f0239477f1e9e"), buildSet);
			List<Build> activeBuildJobs = new ArrayList<>();
			Whitebox.invokeMethod(task, "addNewBuildsInfoToDb", bambooClientBuild, activeBuildJobs, buildMap,
					BAMBOOSAMPLESERVER2, processor.getId());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	/**
	 * Test when build is available to save
	 */
	@Test
	public void testAddNewBuildsInfoToDb_buildNotNull_success() throws Exception {

		try {
			BambooProcessor processor = processorWithOneServer();
			Build build = build("1", JOB1_1_URL);
			when(bambooClient.getBuildDetailsFromServer(any(), any(), any())).thenReturn(build);
			Map<ObjectId, Set<Build>> buildMap = new HashMap<>();
			buildMap.put(new ObjectId("6296661b307f0239477f1e9e"), buildSet);
			List<Build> activeBuildJobs = new ArrayList<>();
			activeBuildJobs.add(build);
			Whitebox.invokeMethod(task, "addNewBuildsInfoToDb", bambooClientBuild, activeBuildJobs, buildMap,
					BAMBOOSAMPLESERVER, processor.getId());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	/**
	 * Test when there is no tool config available
	 */
	@Test
	public void testProcessorToolConnectionisNull_success() {
		try {
			when(processorToolConnectionService.findByTool(any())).thenReturn(null);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void checkForDeployedJobs() throws MalformedURLException, ParseException {
		try {
			when(bambooClientDeploy.getDeployJobsFromServer(any(), any())).thenReturn(oneDeployJob(
					Pair.of(new ObjectId("6296661b307f0239477f1e9e"), "190709761"), new HashSet<>(deploymentList)));
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(twoBambooDeployJob());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientDeploy);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void checkForNewDeployedJobsWithInProgress() throws MalformedURLException, ParseException {
		try {
			when(deploymentRepository.findAll()).thenReturn(deploymentList);// ek mili jo queued hai
			when(bambooClientDeploy.getDeployJobsFromServer(any(), any())).thenReturn(oneDeployJob(
					Pair.of(new ObjectId("6296661b307f0239477f1e9e"), "190709761"), new HashSet<>(serverList)));
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(twoBambooDeployJob());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientDeploy);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void checkForMaxDeployedJobs() throws MalformedURLException, ParseException {
		try {
			when(deploymentRepository.findAll()).thenReturn(deploymentList);// ek mili jo queued hai
			when(bambooClientDeploy.getDeployJobsFromServer(any(), any())).thenReturn(oneDeployJob(
					Pair.of(new ObjectId("6296661b307f0239477f1e9e"), "190709761"), new HashSet<>(maxDeployment)));
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(twoBambooDeployJob());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientDeploy);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void checkForFirstDeploymentQueuedJobs() throws MalformedURLException, ParseException {
		try {
			when(deploymentRepository.findAll()).thenReturn(new ArrayList<>());
			when(bambooClientDeploy.getDeployJobsFromServer(any(), any())).thenReturn(oneDeployJob(
					Pair.of(new ObjectId("6296661b307f0239477f1e9e"), "190709761"), new HashSet<>(serverList)));
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(twoBambooDeployJob());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientDeploy);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void deleteFromDeployments() throws MalformedURLException, ParseException {
		try {
			when(deploymentRepository.findAll()).thenReturn(deploymentList);// ek tool Extra
			when(bambooClientDeploy.getDeployJobsFromServer(any(), any())).thenReturn(oneDeployJob(
					Pair.of(new ObjectId("6296661b307f0239477f1e9e"), "190709761"), new HashSet<>(serverList)));
			when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
			when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any()))
					.thenReturn(oneLessTool());
			when(bambooClientFactory.getBambooClient(anyString())).thenReturn(bambooClientDeploy);
			task.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	private List<ProcessorToolConnection> oneLessTool() {
		List<ProcessorToolConnection> toolList = Lists.newArrayList();
		ProcessorToolConnection t1 = new ProcessorToolConnection();
		t1.setId(new ObjectId("6296661b307f0239477f1e9e"));
		t1.setToolName(ProcessorConstants.BAMBOO);
		t1.setBasicProjectConfigId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		t1.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));
		t1.setJobName("dsa");
		t1.setBranch("branch");
		t1.setUrl(HTTP_URL);
		t1.setJobType("deploy");

		ProcessorToolConnection t2 = new ProcessorToolConnection();
		t2.setId(new ObjectId("6296661b307f0239477f1e9e"));
		t2.setToolName(ProcessorConstants.BAMBOO);
		t2.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167658"));
		t2.setConnectionId(new ObjectId("5f9014743cb73ce896167659"));
		t2.setJobName("dsab");
		t2.setBranch("branch");
		t2.setUrl(HTTP_URL);
		t2.setJobType("deploy");
		toolList.add(t1);
		toolList.add(t2);
		return toolList;

	}

	private BambooProcessor processorWithOneServer() {
		BambooProcessor processor = BambooProcessor.prototype();
		processor.setId(new ObjectId());
		return processor;
	}

	private Map<Pair<ObjectId, String>, Set<Deployment>> oneDeployJob(Pair<ObjectId, String> id,
			Set<Deployment> deployments) {
		Map<Pair<ObjectId, String>, Set<Deployment>> jobs = new HashMap<>();
		jobs.put(id, deployments);
		return jobs;

	}

	private Build build(String number, String url) {
		Build build = new Build();
		build.setNumber(number);
		build.setBuildUrl(url);
		return build;
	}

	private List<ProcessorToolConnection> bambooJob() {
		List<ProcessorToolConnection> toolList = Lists.newArrayList();
		ProcessorToolConnection t1 = new ProcessorToolConnection();
		t1.setToolName(ProcessorConstants.BAMBOO);
		t1.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		t1.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));
		t1.setJobName("dsa");
		t1.setBranch("branch");
		t1.setUrl(HTTP_URL);
		toolList.add(t1);
		return toolList;
	}

	private List<ProcessorToolConnection> twoBambooJob() {
		List<ProcessorToolConnection> toolList = Lists.newArrayList();
		ProcessorToolConnection t1 = new ProcessorToolConnection();
		t1.setId(new ObjectId());
		t1.setToolName(ProcessorConstants.BAMBOO);
		t1.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		t1.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));
		t1.setJobName("dsa");
		t1.setBranch("branch");
		t1.setUrl(HTTP_URL);
		t1.setJobType("build");

		ProcessorToolConnection t2 = new ProcessorToolConnection();
		t2.setId(new ObjectId());
		t2.setToolName(ProcessorConstants.BAMBOO);
		t2.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167658"));
		t2.setConnectionId(new ObjectId("5f9014743cb73ce896167659"));
		t2.setJobName("dsab");
		t2.setBranch("branch");
		t2.setUrl(HTTP_URL);
		t2.setJobType("build");
		toolList.add(t1);
		toolList.add(t2);
		return toolList;
	}

	private List<ProcessorToolConnection> twoBambooDeployJob() {
		List<ProcessorToolConnection> toolList = Lists.newArrayList();
		ProcessorToolConnection t1 = new ProcessorToolConnection();
		t1.setId(new ObjectId());
		t1.setToolName(ProcessorConstants.BAMBOO);
		t1.setBasicProjectConfigId(new ObjectId("622b2c7d4c3a0d462b35d83d"));
		t1.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));
		t1.setJobName("dsa");
		t1.setBranch("branch");
		t1.setUrl(HTTP_URL);
		t1.setJobType("deploy");

		ProcessorToolConnection t2 = new ProcessorToolConnection();
		t2.setId(new ObjectId());
		t2.setToolName(ProcessorConstants.BAMBOO);
		t2.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167658"));
		t2.setConnectionId(new ObjectId("5f9014743cb73ce896167659"));
		t2.setJobName("dsab");
		t2.setBranch("branch");
		t2.setUrl(HTTP_URL);
		t2.setJobType("deploy");
		toolList.add(t1);
		toolList.add(t2);
		return toolList;
	}
}
