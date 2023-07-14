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

package com.publicissapient.kpidashboard.gitlab.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorItemRepository;
import com.publicissapient.kpidashboard.common.repository.scm.CommitRepository;
import com.publicissapient.kpidashboard.common.repository.scm.MergeRequestRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.gitlab.config.GitLabConfig;
import com.publicissapient.kpidashboard.gitlab.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.gitlab.model.GitLabProcessor;
import com.publicissapient.kpidashboard.gitlab.model.GitLabRepo;
import com.publicissapient.kpidashboard.gitlab.repository.GitLabProcessorRepository;
import com.publicissapient.kpidashboard.gitlab.repository.GitLabRepoRepository;
import com.publicissapient.kpidashboard.gitlab.util.GitLabRestOperations;

@ExtendWith(SpringExtension.class)
public class GitLabProcessorJobExecutorTest {

	/** The processorid. */
	private final ObjectId PROCESSORID = new ObjectId("5e2ac020e4b098db0edf5145");
	@Mock
	MergeRequestRepository mergReqRepo;
	ProcessorToolConnection gitLabInfo = new ProcessorToolConnection();
	@Mock
	private TaskScheduler scheduler;
	@Mock
	private GitLabProcessor gitLabProcessor;
	@Mock
	private ConnectionRepository connectionsRepository;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private GitLabConfig gitLabConfig;
	@Mock
	private ProcessorItemRepository<ProcessorItem> processorItemRepository;
	@Mock
	private GitLabRepoRepository gitLabRepository;
	@Mock
	private ProjectToolConfigRepository toolConfigRepository;
	@Mock
	private CommitRepository commitRepository;
	@Mock
	private GitLabProcessorRepository gitLabProcessorRepository;
	@Mock
	private com.publicissapient.kpidashboard.gitlab.processor.service.impl.GitLabClient gitLabClient;
	@Mock
	private GitLabRepo gitLabRepo;
	@Mock
	private GitLabRestOperations gitLabRestOperations;
	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;
	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@InjectMocks
	private GitLabProcessorJobExecutor gitBucketProcessorJobExecutor;
	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;
	private ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
	private Optional<ProcessorExecutionTraceLog> optionalProcessorExecutionTraceLog;
	private List<ProcessorExecutionTraceLog> pl = new ArrayList<>();

	@BeforeEach
	public void setUp() {
		gitBucketProcessorJobExecutor = new GitLabProcessorJobExecutor(scheduler, gitLabProcessorRepository,
				gitLabConfig, toolConfigRepository, connectionsRepository, gitLabRepository, gitLabClient,
				processorItemRepository, commitRepository, processorToolConnectionService, mergReqRepo,
				projectConfigRepository, processorExecutionTraceLogService, processorExecutionTraceLogRepository);
	}

	@Test
	public void testGetCron() {
		Mockito.when(gitLabConfig.getCron()).thenReturn("0 0 0/12 * * *");
		assertEquals("0 0 0/12 * * *", gitLabConfig.getCron());
	}

	@Test
	public void testExecute() throws FetchingCommitException {
		GitLabProcessor gitLabProcessor = GitLabProcessor.prototype();
		gitLabProcessor.setProcessorType(ProcessorType.SCM);
		gitLabProcessor.setProcessorName("Jenkins");
		gitLabProcessor.setId(PROCESSORID);
		gitLabInfo.setBranch("release/core-r4.4");
		gitLabInfo.setPassword("testPassword");
		gitLabInfo.setUrl("http://localhost:9999/scm/testproject/comp-proj.git");
		gitLabInfo.setApiEndPoint("/rest/api/1.0/");
		gitLabInfo.setUsername("User");
		gitLabInfo.setId(new ObjectId("63ea43592181ac0bad9846db"));

		List<GitLabRepo> gitLabRepos = new ArrayList<>();
		GitLabRepo gitLabRepo = new GitLabRepo();
		gitLabRepo.setBranch("Dev_Trunk");
		gitLabRepo.setRepoUrl("https://test.com/scm.git");
		gitLabRepo.setProcessorId(PROCESSORID);
		gitLabRepo.setGitLabAccessToken("abc");
		gitLabRepo.setGitLabProjectId("557");
		gitLabRepo.setUserId("sgshj");
		gitLabRepo.setToolConfigId(new ObjectId("63ea43592181ac0bad9846db"));
		gitLabRepo.setProcessor(gitLabProcessor);
		gitLabRepos.add(gitLabRepo);
		List<CommitDetails> commitDetailList = new ArrayList<>();
		CommitDetails commitDetails = new CommitDetails();
		commitDetails.setBranch("Master");
		commitDetails.setUrl("https://test.com/scm/testProject/test.git");
		commitDetailList.add(commitDetails);
		Mockito.when(gitLabRepository.findActiveRepos(PROCESSORID)).thenReturn(gitLabRepos);

		doReturn(getProjectConfigList()).when(projectConfigRepository).findAll();

		ProcessorToolConnection connectionDetail = new ProcessorToolConnection();
		connectionDetail.setRepositoryName("release");
		connectionDetail.setBranch("release/core-r4.4");
		connectionDetail.setAccessToken("password");
		connectionDetail.setUsername("User");
		List<ProcessorToolConnection> connList = new ArrayList<>();
		connList.add(connectionDetail);

		Mockito.when(processorToolConnectionService.findByToolAndBasicProjectConfigId(ProcessorConstants.GITLAB,
				new ObjectId("61f22fbb16e55b7609b0a36b"))).thenReturn(connList);

		processorExecutionTraceLog.setProcessorName(ProcessorConstants.GITHUB);
		processorExecutionTraceLog.setLastSuccessfulRun("2023-02-06");
		processorExecutionTraceLog.setBasicProjectConfigId("61f22fbb16e55b7609b0a36b");
		pl.add(processorExecutionTraceLog);
		optionalProcessorExecutionTraceLog = Optional.of(processorExecutionTraceLog);

		when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.GITLAB,
				"61f22fbb16e55b7609b0a36b")).thenReturn(optionalProcessorExecutionTraceLog);
		Assert.assertEquals(1, commitDetailList.size());
		gitBucketProcessorJobExecutor.execute(gitLabProcessor);
	}

	@Test
	public void testAddProcessorItems() throws Exception {
		List<ProcessorItem> processorItems = new ArrayList<>();
		Processor processor = new Processor();
		processor.setProcessorName("Jenkins");
		processor.setId(PROCESSORID);
		List<ObjectId> processorIds = new ArrayList<>(0);
		processorIds.add(processor.getId());
		ProcessorItem processorItem = new ProcessorItem();
		processorItem.setProcessorId(PROCESSORID);
		processorItems.add(processorItem);
		List<ProjectToolConfig> toolConfigs = new ArrayList<>();
		ProjectToolConfig toolConfig = new ProjectToolConfig();
		toolConfig.setBranch("Dev_Trunk");
		toolConfig.setToolName("Bitbucket");
		toolConfig.setJobName("API_Build");
		// toolConfig.setUrl("https://test.com/scm.git");
		toolConfigs.add(toolConfig);
		Assert.assertEquals(1, processorItems.size());
		Whitebox.invokeMethod(gitBucketProcessorJobExecutor, "addProcessorItems", processor);
	}

	private List<ProjectBasicConfig> getProjectConfigList() {
		List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
		ProjectBasicConfig p = new ProjectBasicConfig();
		p.setId(new ObjectId("61f22fbb16e55b7609b0a36b"));
		p.setProjectName("projectName");
		projectConfigList.add(p);
		return projectConfigList;
	}
}
