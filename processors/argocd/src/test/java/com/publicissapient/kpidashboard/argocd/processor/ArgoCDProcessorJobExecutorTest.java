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
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.ARGOCD_CLUSTER_ENDPOINT;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.AUTHTOKEN_ENDPOINT;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.argocd.dto.Destination;
import com.publicissapient.kpidashboard.argocd.dto.Specification;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
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
import org.springframework.web.client.HttpClientErrorException;
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
		Specification spec = new Specification();
		Destination destination = new Destination();
		destination.setNamespace("dev-auth");
		destination.setServer("mdgsseunspdaks03");
		spec.setDestination(destination);
		history.setDeployedAt("2023-11-13T11:59:25Z");
		history.setDeployStartedAt("2023-11-05T10:23:45Z");
		history.setId("10");
		history.setRevision("6597a6444507c41cbe6191c7");
		status.setHistory(List.of(history));
		application.setStatus(status);
		application.setSpec(spec);

		ApplicationMetadata metaData2 = new ApplicationMetadata();
		metaData2.setName(APP2);
		metaData2.setUid("6597a4a32f7c7ca951b2ba3d");
		application2.setMetadata(metaData2);
		Status status2 = new Status();
		History history2 = new History();
		Specification specification = new Specification();
		Destination dest = new Destination();
		dest.setNamespace("dev1-auth");
		dest.setServer("mpgsseunspdaks04");
		specification.setDestination(dest);
		history2.setDeployedAt("2024-01-03T11:59:25Z");
		history2.setDeployStartedAt("2024-01-02T10:23:45Z");
		history2.setId("14");
		history2.setRevision("6597a5ea396d3d1a5227c9ea");
		status2.setHistory(List.of(history2));
		application2.setStatus(status2);
		application2.setSpec(specification);

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
	void executeWithValidProjectsAndJobs() {
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
		String jsonResponse = "{\"items\":[{\"server\":\"mdgsseunspdaks03\",\"name\":\"dev-auth\"}]}";
		when(restClient.exchange(Mockito.eq(URI.create(ARGOCD_URL + ARGOCD_CLUSTER_ENDPOINT)),
				Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class), Mockito.eq(String.class)))
				.thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));
		assertTrue(jobExecutor.execute(processor));
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
		String jsonResponse = "{\"items\":[{\"server\":\"mdgsseunspdaks03\",\"name\":\"dev-auth\"}]}";
		when(restClient.exchange(Mockito.eq(URI.create(ARGOCD_URL + ARGOCD_CLUSTER_ENDPOINT)),
				Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class), Mockito.eq(String.class)))
				.thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));
		assertTrue(jobExecutor.execute(processor));
	}

	@Test
	void executeWithNoProjects() {
		ArgoCDProcessor processor = new ArgoCDProcessor();
		processor.setId(new ObjectId("6597633d916863f2b4779145"));
		when(projectBasicConfigRepository.findAll()).thenReturn(new ArrayList<>());
		assertTrue(jobExecutor.execute(processor));
	}

	@Test
	void executeWithNoJobs() {
		ArgoCDProcessor processor = new ArgoCDProcessor();
		processor.setId(new ObjectId("6597633d916863f2b4779145"));
		when(projectBasicConfigRepository.findAll()).thenReturn(listProjectBasicConfig);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(Mockito.anyString(), Mockito.any()))
				.thenReturn(new ArrayList<>());
		assertTrue(jobExecutor.execute(processor));
	}

	@Test
	void testExecuteWithRestClientException() {
		ArgoCDProcessor processor = new ArgoCDProcessor();
		processor.setId(new ObjectId("6597633d916863f2b4779145"));
		when(projectBasicConfigRepository.findAll()).thenReturn(listProjectBasicConfig);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(Mockito.anyString(), Mockito.any()))
				.thenReturn(listProcessorToolConnection);
		when(restClient.exchange(Mockito.eq(URI.create(ARGOCD_URL + AUTHTOKEN_ENDPOINT)), Mockito.eq(HttpMethod.POST),
				Mockito.any(HttpEntity.class), Mockito.<Class<TokenDTO>>any()))
				.thenReturn(new ResponseEntity<>(new TokenDTO(), HttpStatus.OK));
		when(restClient.exchange(Mockito.eq(URI.create(ARGOCD_URL + APPLICATIONS_ENDPOINT + "?" + APPLICATIONS_PARAM)),
				Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class), Mockito.<Class<ApplicationsList>>any()))
				.thenThrow(new RestClientException("Test Exception"));
		String jsonResponse = "{\"items\":[{\"server\":\"mdgsseunspdaks03\",\"name\":\"dev-auth\"}]}";
		when(restClient.exchange(Mockito.eq(URI.create(ARGOCD_URL + ARGOCD_CLUSTER_ENDPOINT)),
				Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class), Mockito.eq(String.class)))
				.thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));
		assertFalse(jobExecutor.execute(processor));
	}

	@Test
	void testSaveRevisionsInDbAndGetCount_NewDeployments() {
		Application application = new Application();
		application.setStatus(new Status());
		History history = new History();
		history.setId("1");
		history.setDeployStartedAt("2023-11-05T10:23:45Z");
		history.setDeployedAt("2023-11-13T11:59:25Z");
		application.getStatus().setHistory(List.of(history));
		application.setMetadata(new ApplicationMetadata());
		application.getMetadata().setName("app1");
		Specification spec = new Specification();
		Destination destination = new Destination();
		destination.setNamespace("dev-auth");
		destination.setServer("mdgsseunspdaks03");
		spec.setDestination(destination);
		application.setSpec(spec);

		ProcessorToolConnection argoCDJob = new ProcessorToolConnection();
		argoCDJob.setId(new ObjectId("6597633d916863f2b4779145"));
		argoCDJob.setBasicProjectConfigId(new ObjectId("6597633d916863f2b4779145"));
		argoCDJob.setDeploymentProjectId("deploymentProjectId");
		argoCDJob.setDeploymentProjectName("deploymentProjectName");

		List<Deployment> existingEntries = new ArrayList<>();
		int count = jobExecutor.saveRevisionsInDbAndGetCount(application, existingEntries, argoCDJob,
				new ObjectId("6597633d916863f2b4779145"), Map.of());

		assertEquals(1, count);
		verify(deploymentRepository, times(1)).save(any(Deployment.class));
	}

	@Test
	void testSaveRevisionsInDbAndGetCount_ExistingDeployments() {
		Application application = new Application();
		application.setStatus(new Status());
		History history = new History();
		history.setId("1");
		history.setDeployStartedAt("2023-11-05T10:23:45Z");
		history.setDeployedAt("2023-11-13T11:59:25Z");
		application.getStatus().setHistory(List.of(history));
		application.setMetadata(new ApplicationMetadata());
		application.getMetadata().setName("app1");
		Specification spec = new Specification();
		Destination destination = new Destination();
		destination.setNamespace("dev-auth");
		destination.setServer("mdgsseunspdaks03");
		spec.setDestination(destination);
		application.setSpec(spec);

		ProcessorToolConnection argoCDJob = new ProcessorToolConnection();
		argoCDJob.setId(new ObjectId("6597633d916863f2b4779145"));
		argoCDJob.setBasicProjectConfigId(new ObjectId("6597633d916863f2b4779145"));
		argoCDJob.setDeploymentProjectId("deploymentProjectId");
		argoCDJob.setDeploymentProjectName("deploymentProjectName");

		Deployment existingDeployment = new Deployment();
		existingDeployment.setEnvName("app1");
		existingDeployment.setNumber("1");
		List<Deployment> existingEntries = List.of(existingDeployment);
		int count = jobExecutor.saveRevisionsInDbAndGetCount(application, existingEntries, argoCDJob,
				new ObjectId("6597633d916863f2b4779145"), Map.of());
		assertEquals(1, count);
		verify(deploymentRepository, times(1)).save(any(Deployment.class));
	}

	@Test
	void testExecuteWithJobName() {
		ProcessorToolConnection argoCDJob = new ProcessorToolConnection();
		argoCDJob.setJobName("jobName");
		argoCDJob.setUrl("http://example.com");
		argoCDJob.setUsername("user");
		argoCDJob.setPassword("encryptedPassword");
		argoCDJob.setBasicProjectConfigId(new ObjectId("6597633d916863f2b4779145"));
		Application application = new Application();
		application.setStatus(new Status());
		History history = new History();
		history.setId("1");
		history.setDeployStartedAt("2023-11-05T10:23:45Z");
		history.setDeployedAt("2023-11-13T11:59:25Z");
		application.getStatus().setHistory(List.of(history));
		application.setMetadata(new ApplicationMetadata());
		application.getMetadata().setName("app1");
		Specification spec = new Specification();
		Destination destination = new Destination();
		destination.setNamespace("dev-auth");
		destination.setServer("mdgsseunspdaks03");
		spec.setDestination(destination);
		application.setSpec(spec);
		List<Deployment> deploymentJobs = new ArrayList<>();
		when(deploymentRepository.findByProcessorIdIn(anySet())).thenReturn(deploymentJobs);
		String jsonResponse = "{\"items\":[{\"server\":\"mdgsseunspdaks03\",\"name\":\"dev-auth\"}]}";
		when(restClient.exchange(Mockito.eq(URI.create(ARGOCD_URL + ARGOCD_CLUSTER_ENDPOINT)),
				Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class), Mockito.eq(String.class)))
				.thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));
		ArgoCDProcessor processor = new ArgoCDProcessor();
		processor.setId(new ObjectId("6597633d916863f2b4779145"));
		Map<String, String> serverToNameMap = new HashMap<>();
		serverToNameMap.put("mdgsseunspdaks03", "dev-auth");
		int count = jobExecutor.saveRevisionsInDbAndGetCount(application, deploymentJobs, argoCDJob, processor.getId(),
				serverToNameMap);
		assertEquals(1, count);
	}

	@Test
	void saveRevisionsInDbAndGetCount_NewDeployment_Success() {
		Application application = new Application();
		application.setStatus(new Status());
		History history = new History();
		history.setId("1");
		history.setDeployStartedAt("2023-11-05T10:23:45Z");
		history.setDeployedAt("2023-11-13T11:59:25Z");
		application.getStatus().setHistory(List.of(history));
		application.setMetadata(new ApplicationMetadata());
		application.getMetadata().setName("app1");
		Specification spec = new Specification();
		Destination destination = new Destination();
		destination.setNamespace("dev-auth");
		destination.setServer("mdgsseunspdaks03");
		spec.setDestination(destination);
		application.setSpec(spec);

		ProcessorToolConnection argoCDJob = new ProcessorToolConnection();
		argoCDJob.setId(new ObjectId("6597633d916863f2b4779145"));
		argoCDJob.setBasicProjectConfigId(new ObjectId("6597633d916863f2b4779145"));
		argoCDJob.setDeploymentProjectId("deploymentProjectId");
		argoCDJob.setDeploymentProjectName("deploymentProjectName");

		List<Deployment> existingEntries = new ArrayList<>();
		Map<String, String> serverToNameMap = Map.of("mdgsseunspdaks03", "dev-auth");

		int count = jobExecutor.saveRevisionsInDbAndGetCount(application, existingEntries, argoCDJob,
				new ObjectId("6597633d916863f2b4779145"), serverToNameMap);

		assertEquals(1, count);
		verify(deploymentRepository, times(1)).save(any(Deployment.class));
	}

	@Test
	void saveRevisionsInDbAndGetCount_ExistingDeployment_NoSave() {
		Application application = new Application();
		application.setStatus(new Status());
		History history = new History();
		history.setId("1");
		history.setDeployStartedAt("2023-11-05T10:23:45Z");
		history.setDeployedAt("2023-11-13T11:59:25Z");
		application.getStatus().setHistory(List.of(history));
		application.setMetadata(new ApplicationMetadata());
		application.getMetadata().setName("app1");
		Specification spec = new Specification();
		Destination destination = new Destination();
		destination.setNamespace("dev-auth");
		destination.setServer("mdgsseunspdaks03");
		spec.setDestination(destination);
		application.setSpec(spec);

		ProcessorToolConnection argoCDJob = new ProcessorToolConnection();
		argoCDJob.setId(new ObjectId("6597633d916863f2b4779145"));
		argoCDJob.setBasicProjectConfigId(new ObjectId("6597633d916863f2b4779145"));
		argoCDJob.setDeploymentProjectId("deploymentProjectId");
		argoCDJob.setDeploymentProjectName("deploymentProjectName");
		argoCDJob.setJobName("dev-auth");
		Deployment existingDeployment = new Deployment();
		existingDeployment.setEnvName("dev-auth");
		existingDeployment.setNumber("1");
		existingDeployment.setJobName("dev-auth");
		existingDeployment.setBasicProjectConfigId(new ObjectId("6597633d916863f2b4779145"));
		List<Deployment> existingEntries = List.of(existingDeployment);
		Map<String, String> serverToNameMap = Map.of("mdgsseunspdaks03", "dev-auth");

		int count = jobExecutor.saveRevisionsInDbAndGetCount(application, existingEntries, argoCDJob,
				new ObjectId("6597633d916863f2b4779145"), serverToNameMap);

		assertEquals(0, count);
		verify(deploymentRepository, times(0)).save(any(Deployment.class));
	}

	@Test
	void saveRevisionsInDbAndGetCount_EmptyHistory_NoSave() {
		Application application = new Application();
		application.setStatus(new Status());
		application.setMetadata(new ApplicationMetadata());
		application.getMetadata().setName("app1");

		ProcessorToolConnection argoCDJob = new ProcessorToolConnection();
		argoCDJob.setId(new ObjectId("6597633d916863f2b4779145"));
		argoCDJob.setBasicProjectConfigId(new ObjectId("6597633d916863f2b4779145"));
		argoCDJob.setDeploymentProjectId("deploymentProjectId");
		argoCDJob.setDeploymentProjectName("deploymentProjectName");

		List<Deployment> existingEntries = new ArrayList<>();
		Map<String, String> serverToNameMap = Map.of("mdgsseunspdaks03", "dev-auth");

		int count = jobExecutor.saveRevisionsInDbAndGetCount(application, existingEntries, argoCDJob,
				new ObjectId("6597633d916863f2b4779145"), serverToNameMap);

		assertEquals(0, count);
		verify(deploymentRepository, times(0)).save(any(Deployment.class));
	}

	@Test
	void saveRevisionsInDbAndGetCount_NullHistory_NoSave() {
		Application application = new Application();
		application.setStatus(new Status());
		application.setMetadata(new ApplicationMetadata());
		application.getMetadata().setName("app1");
		Specification spec = new Specification();
		Destination destination = new Destination();
		destination.setNamespace("dev-auth");
		destination.setServer("mdgsseunspdaks03");
		spec.setDestination(destination);
		application.setSpec(spec);

		ProcessorToolConnection argoCDJob = new ProcessorToolConnection();
		argoCDJob.setId(new ObjectId("6597633d916863f2b4779145"));
		argoCDJob.setBasicProjectConfigId(new ObjectId("6597633d916863f2b4779145"));
		argoCDJob.setDeploymentProjectId("deploymentProjectId");
		argoCDJob.setDeploymentProjectName("deploymentProjectName");

		List<Deployment> existingEntries = new ArrayList<>();
		Map<String, String> serverToNameMap = Map.of("mdgsseunspdaks03", "dev-auth");

		int count = jobExecutor.saveRevisionsInDbAndGetCount(application, existingEntries, argoCDJob,
				new ObjectId("6597633d916863f2b4779145"), serverToNameMap);

		assertEquals(0, count);
		verify(deploymentRepository, times(0)).save(any(Deployment.class));
	}
}