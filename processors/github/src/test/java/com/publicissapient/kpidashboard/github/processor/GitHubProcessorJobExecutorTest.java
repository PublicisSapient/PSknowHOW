package com.publicissapient.kpidashboard.github.processor;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.scm.CommitRepository;
import com.publicissapient.kpidashboard.common.repository.scm.MergeRequestRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.github.config.GitHubConfig;
import com.publicissapient.kpidashboard.github.customexception.FetchingCommitException;
import com.publicissapient.kpidashboard.github.model.GitHubProcessor;
import com.publicissapient.kpidashboard.github.model.GitHubProcessorItem;
import com.publicissapient.kpidashboard.github.processor.service.GitHubClient;
import com.publicissapient.kpidashboard.github.repository.GitHubProcessorItemRepository;
import com.publicissapient.kpidashboard.github.repository.GitHubProcessorRepository;

/**
 * @author narsingh9
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHubProcessorJobExecutorTest {
	/** The processorid. */
	private final ObjectId PROCESSORID = new ObjectId("5e2ac020e4b098db0edf5145");

	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;

	@Mock
	private TaskScheduler taskScheduler;

	@Mock
	private GitHubConfig gitHubConfig;

	@Mock
	private GitHubProcessorRepository gitHubProcessorRepository;

	@Mock
	private GitHubProcessorItemRepository gitHubProcessorItemRepository;

	@Mock
	private GitHubClient gitHubClient;

	@Mock
	private CommitRepository commitsRepo;

	@Mock
	private MergeRequestRepository mergReqRepo;

	@Mock
	private RestOperations restOperations;

	@InjectMocks
	private GitHubProcessorJobExecutor gitHubProcessorJobExecutor;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@Mock
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;

	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;
	private ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
	private Optional<ProcessorExecutionTraceLog> optionalProcessorExecutionTraceLog;
	private List<ProcessorExecutionTraceLog> pl = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

	}

	private List<ProjectBasicConfig> getProjectConfigList() {
		List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
		ProjectBasicConfig p = new ProjectBasicConfig();
		p.setId(new ObjectId("61f22fbb16e55b7609b0a36b"));
		p.setProjectName("projectName");
		projectConfigList.add(p);
		return projectConfigList;
	}

	@Test
	public void testExecute() throws FetchingCommitException {
		GitHubProcessor gitHubProcessor = GitHubProcessor.prototype();
		gitHubProcessor.setProcessorType(ProcessorType.SCM);
		gitHubProcessor.setProcessorName("GitHub");
		gitHubProcessor.setId(new ObjectId());
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.GITHUB);
		processorExecutionTraceLog.setLastSuccessfulRun("2023-02-06");
		processorExecutionTraceLog.setBasicProjectConfigId("624d5c9ed837fc14d40b3039");
		pl.add(processorExecutionTraceLog);
		optionalProcessorExecutionTraceLog = Optional.of(processorExecutionTraceLog);
		doReturn(getProjectConfigList()).when(projectConfigRepository).findAll();
		doReturn(getProcessorItemList().get(0)).when(gitHubProcessorItemRepository).save(ArgumentMatchers.any());

		doReturn(getProcessorToolConnectionList()).when(processorToolConnectionService)
				.findByToolAndBasicProjectConfigId(ArgumentMatchers.anyString(), ArgumentMatchers.any(ObjectId.class));
		doReturn(getCommitDetailsList()).when(gitHubClient).fetchAllCommits(ArgumentMatchers.any(),
				ArgumentMatchers.anyBoolean(), ArgumentMatchers.any(), ArgumentMatchers.any());

		doReturn(getMergeDetailsList()).when(gitHubClient).fetchMergeRequests(ArgumentMatchers.any(),
				ArgumentMatchers.anyBoolean(), ArgumentMatchers.any(), ArgumentMatchers.any());
		doReturn("http://customapi:8080/").when(gitHubConfig).getCustomApiBaseUrl();
		when(processorExecutionTraceLogRepository.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.GITHUB,
				"624d5c9ed837fc14d40b3039")).thenReturn(optionalProcessorExecutionTraceLog);
		boolean executed = gitHubProcessorJobExecutor.execute(gitHubProcessor);
		assertTrue(executed);
	}

	private List<ProcessorToolConnection> getProcessorToolConnectionList() {
		ProcessorToolConnection connectionDetail = new ProcessorToolConnection();
		connectionDetail.setRepositoryName("release");
		connectionDetail.setBranch("release/core-r4.4");
		connectionDetail.setUrl("http://localhost:9999/scm/testproject/comp-proj.git");
		connectionDetail.setAccessToken("password");
		connectionDetail.setUsername("User");
		List<ProcessorToolConnection> connList = new ArrayList<>();
		connList.add(connectionDetail);
		return connList;
	}

	private List<GitHubProcessorItem> getProcessorItemList() {
		List<GitHubProcessorItem> processorItemList = new ArrayList<>();
		GitHubProcessorItem processorItem = new GitHubProcessorItem();
		processorItem.setProcessorId(PROCESSORID);
		processorItemList.add(processorItem);
		return processorItemList;
	}

	private List<CommitDetails> getCommitDetailsList() {
		List<CommitDetails> commitDetailList = new ArrayList<>();
		CommitDetails commitDetails = new CommitDetails();
		commitDetails.setBranch("Master");
		commitDetails.setUrl("https://test.com/scm/username/repoName.git");
		commitDetailList.add(commitDetails);
		return commitDetailList;
	}

	private List<MergeRequests> getMergeDetailsList() {
		List<MergeRequests> mergeRequestsList = new ArrayList<>();
		MergeRequests mergeRequests = new MergeRequests();
		mergeRequests.setAuthor("Master");
		mergeRequests.setTitle("https://test.com/scm/username/repoName.git");
		mergeRequestsList.add(mergeRequests);
		return mergeRequestsList;
	}

}
