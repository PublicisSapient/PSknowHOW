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

package com.publicissapient.kpidashboard.argocd.processor;

import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.APPLICATIONS_ENDPOINT;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.APPLICATIONS_PARAM;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.AUTHTOKEN_ENDPOINT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.argocd.client.ArgoCDClient;
import com.publicissapient.kpidashboard.argocd.config.ArgoCDConfig;
import com.publicissapient.kpidashboard.argocd.dto.Application;
import com.publicissapient.kpidashboard.argocd.dto.ApplicationMetadata;
import com.publicissapient.kpidashboard.argocd.dto.ApplicationsList;
import com.publicissapient.kpidashboard.argocd.dto.History;
import com.publicissapient.kpidashboard.argocd.dto.Status;
import com.publicissapient.kpidashboard.argocd.dto.TokenDTO;
import com.publicissapient.kpidashboard.argocd.model.ArgoCDProcessor;
import com.publicissapient.kpidashboard.argocd.repository.ArgoCDProcessorRepository;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;

@ExtendWith(SpringExtension.class)
class ArgoCDProcessorJobExecutorTest {

	public static final String ARGOCD_URL = "url";

	public static final String APP1 = "application";

	public static final String APP2 = "application2";

	List<ProjectBasicConfig> listProjectBasicConfig = new ArrayList<>();

	List<ProcessorToolConnection> listProcessorToolConnection = new ArrayList<>();

	ApplicationsList applicationsList = new ApplicationsList();

	Application application = new Application();

	Application application2 = new Application();

	@Mock
	private ArgoCDProcessorRepository argoCDProcessorRepository;

	@Mock
	private ArgoCDConfig argoCDConfig;

	@SpyBean
	private ArgoCDClient argoCDClient;

	@Mock
	private ProjectBasicConfigRepository projectBasicConfigRepository;

	@Mock
	private DeploymentRepository deploymentRepository;

	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@InjectMocks
	private ArgoCDProcessorJobExecutor jobExecutor;

	@MockBean
	private RestTemplate restClient;

	@SuppressWarnings("deprecation")
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(argoCDConfig.getCustomApiBaseUrl()).thenReturn("http://customapi:8080/");

		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("6597633d916863f2b4779145"));
		projectBasicConfig.setConsumerCreatedOn("createdOn");
		projectBasicConfig.setIsKanban(true);
		projectBasicConfig.setProjectName("project");
		projectBasicConfig.setUpdatedAt("updatedAt");
		ProjectBasicConfig projectBasicConfig2 = new ProjectBasicConfig();
		projectBasicConfig2.setId(new ObjectId("6597633d916863f2b4779145"));
		projectBasicConfig2.setConsumerCreatedOn("createdOn2");
		projectBasicConfig2.setIsKanban(false);
		projectBasicConfig2.setProjectName("project2");
		projectBasicConfig2.setUpdatedAt("updatedAt2");
		listProjectBasicConfig.add(projectBasicConfig);
		listProjectBasicConfig.add(projectBasicConfig2);

		ProcessorToolConnection processorToolConnection = new ProcessorToolConnection();
		processorToolConnection.setId(new ObjectId("6597633d916863f2b4779145"));
		processorToolConnection.setToolName("toolName");
		processorToolConnection.setProjectId("projectId");
		processorToolConnection.setProjectKey("projectKey");
		processorToolConnection.setJobType("Build");
		processorToolConnection.setUrl(ARGOCD_URL);
		ProcessorToolConnection processorToolConnection2 = new ProcessorToolConnection();
		processorToolConnection2.setId(new ObjectId("6597633d916863f2b4779145"));
		processorToolConnection2.setToolName("toolName2");
		processorToolConnection2.setProjectId("projectId2");
		processorToolConnection2.setProjectKey("projectKey2");
		processorToolConnection2.setUrl(ARGOCD_URL);
		processorToolConnection2.setJobType("Build");
		listProcessorToolConnection.add(processorToolConnection);
		listProcessorToolConnection.add(processorToolConnection2);

		ApplicationMetadata metaData = new ApplicationMetadata();
		metaData.setName(APP1);
		metaData.setUid("6597a64ad221aa0e99c5c306");
		application.setMetadata(metaData);
		Status status = new Status();
		History history = new History();
		history.setDeployedAt("2023-11-13T11:59:25Z");
		history.setDeployStartedAt("2023-11-05T10:23:45Z");
		history.setId("10");
		history.setRevision("6597a6444507c41cbe6191c7");
		status.setHistory(List.of(history));
		application.setStatus(status);

		ApplicationMetadata metaData2 = new ApplicationMetadata();
		metaData2.setName(APP2);
		metaData2.setUid("6597a4a32f7c7ca951b2ba3d");
		application2.setMetadata(metaData2);
		Status status2 = new Status();
		History history2 = new History();
		history2.setDeployedAt("2024-01-03T11:59:25Z");
		history2.setDeployStartedAt("2024-01-02T10:23:45Z");
		history2.setId("14");
		history2.setRevision("6597a5ea396d3d1a5227c9ea");
		status2.setHistory(List.of(history2));
		application2.setStatus(status2);

		applicationsList.setItems(List.of(application, application2));
	}

	@Test
	void collectNoBuildServersNothingAdded() {
		when(projectBasicConfigRepository.findAll()).thenReturn(listProjectBasicConfig);
		ArgoCDProcessor processor = new ArgoCDProcessor();
		assertTrue(jobExecutor.execute(processor));
	}

	@Test
	void collectNoJobsOnServerNothingAdded() {
		assertTrue(jobExecutor.execute(ArgoCDProcessor.buildProcessor()));
		verifyNoMoreInteractions(argoCDClient);
	}

	@Test
	void collectTwoJobsWithExceptionFromArgoCD() {
		ArgoCDProcessor processor = new ArgoCDProcessor();
		processor.setId(new ObjectId("6597633d916863f2b4779145"));
		when(projectBasicConfigRepository.findAll()).thenReturn(listProjectBasicConfig);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(Mockito.anyString(), Mockito.any()))
				.thenReturn(listProcessorToolConnection);
		when(restClient.exchange(Mockito.eq(URI.create(ARGOCD_URL + AUTHTOKEN_ENDPOINT)), Mockito.eq(HttpMethod.POST),
				Mockito.any(HttpEntity.class), Mockito.<Class<TokenDTO>>any())).thenThrow(RestClientException.class);
		when(restClient.exchange(ArgumentMatchers.any(String.class), Mockito.eq(HttpMethod.GET),
				Mockito.any(HttpEntity.class), Mockito.<Class<String>>any())).thenThrow(RestClientException.class);
		assertFalse(jobExecutor.execute(processor));
	}

	@Test
	void collectEnableJob() {
		ArgoCDProcessor processor = new ArgoCDProcessor();
		processor.setId(new ObjectId("6597633d916863f2b4779145"));
		when(projectBasicConfigRepository.findAll()).thenReturn(listProjectBasicConfig);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(Mockito.anyString(), Mockito.any()))
				.thenReturn(listProcessorToolConnection);
		TokenDTO accessToken = new TokenDTO();
		accessToken.setToken("token");
		when(restClient.exchange(Mockito.eq(URI.create(ARGOCD_URL + AUTHTOKEN_ENDPOINT)), Mockito.eq(HttpMethod.POST),
				Mockito.any(HttpEntity.class), Mockito.<Class<TokenDTO>>any()))
				.thenReturn(new ResponseEntity<TokenDTO>(accessToken, HttpStatus.OK));
		when(restClient.exchange(Mockito.eq(URI.create(ARGOCD_URL + APPLICATIONS_ENDPOINT + "?" + APPLICATIONS_PARAM)),
				Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class), Mockito.<Class<ApplicationsList>>any()))
				.thenReturn(new ResponseEntity<ApplicationsList>(applicationsList, HttpStatus.OK));
		when(restClient.exchange(Mockito.eq(URI.create(ARGOCD_URL + APPLICATIONS_ENDPOINT + "/" + APP1)),
				Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class), Mockito.<Class<Application>>any()))
				.thenReturn(new ResponseEntity<Application>(application, HttpStatus.OK));
		when(restClient.exchange(Mockito.eq(URI.create(ARGOCD_URL + APPLICATIONS_ENDPOINT + "/" + APP2)),
				Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class), Mockito.<Class<Application>>any()))
				.thenReturn(new ResponseEntity<Application>(application2, HttpStatus.OK));
		when(restClient.exchange(ArgumentMatchers.any(String.class), Mockito.eq(HttpMethod.GET),
				Mockito.any(HttpEntity.class), Mockito.<Class<String>>any()))
				.thenReturn(new ResponseEntity<String>("Success", HttpStatus.OK));
		assertTrue(jobExecutor.execute(processor));
	}

}
