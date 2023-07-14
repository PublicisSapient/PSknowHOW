package com.publicissapient.kpidashboard.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;

import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.teamcity.config.TeamcityConfig;
import com.publicissapient.kpidashboard.teamcity.factory.TeamcityClientFactory;
import com.publicissapient.kpidashboard.teamcity.model.TeamcityProcessor;
import com.publicissapient.kpidashboard.teamcity.processor.TeamcityProcessorJobExecutor;
import com.publicissapient.kpidashboard.teamcity.processor.adapter.TeamcityClient;

@SuppressWarnings("javadoc")
@ExtendWith(SpringExtension.class)
public class TeamcityProcessorJobExecutorTest {
	private static final String CUSTOM_API_BASE_URL = "http://localhost:9090/";
	private static final String METRICS1 = "nloc";
	private static final String EXCEPTION = "rest client exception";
	private static final String PLAIN_TEXT_PASSWORD = "PlainTestPassword";
	@InjectMocks
	TeamcityProcessorJobExecutor jobExecutor;
	@Mock
	AesEncryptionService aesEncryptionService;
	@Mock
	private TeamcityConfig teamcityConfig;
	@Mock
	private TeamcityClientFactory teamcityClientFactory;
	@Mock
	private TeamcityClient teamcityClient;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;
	@Mock
	private BuildRepository buildRepository;
	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	private ProjectBasicConfig projectBasicConfig;

	private List<ProcessorToolConnection> connList = new ArrayList<>();

	private Set<Build> builds = new HashSet<>();

	private Build build1 = new Build();
	private Build build2 = new Build();
	private Build build3 = new Build();

	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
	private Optional<ProcessorExecutionTraceLog> optionalProcessorExecutionTraceLog;
	private List<ProcessorExecutionTraceLog> pl = new ArrayList<>();
	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		ProcessorToolConnection processorToolConnection = new ProcessorToolConnection();
		processorToolConnection.setUrl("http://test@test.com");
		processorToolConnection.setId(new ObjectId("6296661b307f0239477f1e9e"));
		processorToolConnection.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		processorToolConnection.setJobName("xyz");
		processorToolConnection.setToolName("Teamcity");
		processorToolConnection.setConnectionId(new ObjectId("5fa69f5d220038d6a365fec6"));
		processorToolConnection.setConnectionName("Teamcity connection");
		processorToolConnection.setUsername("userName");
		processorToolConnection.setPassword("password");
		processorToolConnection.setJobName("jobName");

		projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("5f9014743cb73ce896167659"));
		projectBasicConfig.setIsKanban(true);
		projectBasicConfig.setProjectName("Team city Scrum");
		projectBasicConfig.setUpdatedAt("updatedAt");
		projectConfigList.add(projectBasicConfig);

		connList.add(processorToolConnection);

		build1.setId(new ObjectId("63c6801d6bf36f4ba6f1ab4c"));
		build1.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		build1.setProjectToolConfigId(new ObjectId("6296661b307f0239477f1e9e"));
		build1.setBuildJob("teamcity1");
		build1.setNumber("123");
		build1.setBuildUrl("JOB1_1_URL");
		build1.setStartTime(1673913622000L);
		build1.setEndTime(1673913752608L);
		build1.setDuration(130608L);
		build1.setBuildStatus(BuildStatus.FAILURE);

		build2.setId(new ObjectId("63c6801d6bf36f4ba6f1ab56"));
		build2.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		build2.setProjectToolConfigId(new ObjectId("6296661b307f0239477f1e9e"));
		build2.setBuildJob("teamcity2");
		build2.setNumber("222");
		build2.setBuildUrl("JOB1_1_URL");
		build2.setStartTime(1673913622000L);
		build2.setEndTime(1673913752608L);
		build2.setDuration(130608L);
		build2.setBuildStatus(BuildStatus.SUCCESS);

		build3.setId(new ObjectId("63c6801d6bf36f4ba6f1ab45"));
		build3.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		build3.setProjectToolConfigId(new ObjectId("6296661b307f0239477f1e9e"));
		build3.setBuildJob("teamcity2");
		build3.setNumber("333");
		build3.setBuildUrl("JOB2_URL");
		build3.setStartTime(1673913622000L);
		build3.setEndTime(1673913752608L);
		build3.setDuration(130608L);
		build3.setBuildStatus(BuildStatus.SUCCESS);

		builds.add(build1);
		builds.add(build2);
		builds.add(build3);

		processorExecutionTraceLog.setProcessorName(ProcessorConstants.JENKINS);
		processorExecutionTraceLog.setLastSuccessfulRun("2023-02-06");
		processorExecutionTraceLog.setBasicProjectConfigId("624d5c9ed837fc14d40b3039");
		pl.add(processorExecutionTraceLog);
		optionalProcessorExecutionTraceLog = Optional.of(processorExecutionTraceLog);

		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(any(), any())).thenReturn(connList);
		when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
		when(teamcityConfig.getAesEncryptionKey()).thenReturn("aesKey");
		doNothing().when(processorExecutionTraceLogService).save(Mockito.any());
		when(aesEncryptionService.decrypt(anyString(), anyString())).thenReturn(PLAIN_TEXT_PASSWORD);

	}

	@Test
	public void processForFetchAndVerifyBuilds() throws Exception {
		when(teamcityConfig.getCustomApiBaseUrl()).thenReturn(CUSTOM_API_BASE_URL);
		try {
			Map<ObjectId, Set<Build>> buildMap = new HashMap<>();
			buildMap.put(new ObjectId("6296661b307f0239477f1e9e"), builds);
			when(teamcityClientFactory.getTeamcityClient(anyString())).thenReturn(teamcityClient);
			when(teamcityClient.getInstanceJobs(any())).thenReturn(buildMap);
			when(buildRepository.findByProjectToolConfigIdAndNumber(any(), any())).thenReturn(build1);
			when(teamcityClient.getBuildDetails(any(), any(), any(), any())).thenReturn(build2);
			when(processorExecutionTraceLogRepository
					.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.JENKINS, "624d5c9ed837fc14d40b3039"))
							.thenReturn(optionalProcessorExecutionTraceLog);
			jobExecutor.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void processForFetchAndVerifyBuildsNoAssignee() throws Exception {
		when(teamcityConfig.getCustomApiBaseUrl()).thenReturn(CUSTOM_API_BASE_URL);
		try {
			Map<ObjectId, Set<Build>> buildMap = new HashMap<>();
			buildMap.put(new ObjectId("6296661b307f0239477f1e9e"), builds);
			when(teamcityClientFactory.getTeamcityClient(anyString())).thenReturn(teamcityClient);
			when(teamcityClient.getInstanceJobs(any())).thenReturn(buildMap);
			when(buildRepository.findByProjectToolConfigIdAndNumber(any(), any())).thenReturn(null);
			when(teamcityClient.getBuildDetails(any(), any(), any(), any())).thenReturn(build2);
			when(processorExecutionTraceLogRepository
					.findByProcessorNameAndBasicProjectConfigId(ProcessorConstants.JENKINS, "624d5c9ed837fc14d40b3039"))
							.thenReturn(optionalProcessorExecutionTraceLog);
			jobExecutor.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	private TeamcityProcessor processorWithOneServer() {
		return TeamcityProcessor.buildProcessor();
	}
}
