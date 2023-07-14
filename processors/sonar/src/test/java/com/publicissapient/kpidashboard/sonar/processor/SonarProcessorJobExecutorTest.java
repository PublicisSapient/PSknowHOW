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

package com.publicissapient.kpidashboard.sonar.processor;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.common.constant.SonarAnalysisType;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarHistoryRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.sonar.config.SonarConfig;
import com.publicissapient.kpidashboard.sonar.factory.SonarClientFactory;
import com.publicissapient.kpidashboard.sonar.model.SonarProcessor;
import com.publicissapient.kpidashboard.sonar.model.SonarProcessorItem;
import com.publicissapient.kpidashboard.sonar.processor.adapter.SonarClient;
import com.publicissapient.kpidashboard.sonar.processor.adapter.impl.Sonar6And7Client;
import com.publicissapient.kpidashboard.sonar.repository.SonarProcessorItemRepository;
import com.publicissapient.kpidashboard.sonar.repository.SonarProcessorRepository;

@ExtendWith(SpringExtension.class)
public class SonarProcessorJobExecutorTest {

	private static final String SONAR_URL = "http://sonar.com";
	private static final String METRICS = "lines,ncloc,violations,new_vulnerabilities,critical_violations,major_violations,blocker_violations,minor_violations,info_violations,tests,test_success_density,test_errors,test_failures,coverage,line_coverage,sqale_index,alert_status,quality_gate_details,sqale_rating";
	private static final String CUSTOM_API_BASE_URL = "http://localhost:9090/";
	private static final String SONAR_CACHE_END_POINT = "api/cache/clearCache/sonarCache";
	private static final String URL_MEASURE_HISTORY = "/api/measures/search_history?component=%s&metrics=%s&includealerts=true&from=%s";
	private static final String DEFAULT_DATE = "2018-01-01";
	private static final String METRICS1 = "nloc";
	private static final String METRICS2 = "nloc,violations";
	private static final ObjectId OBJ_ID = new ObjectId("5d6ce2c7adbe1d000bd4bb6f");
	private static final String PROJECT_ID = "AVu3b-MAphY78UZXuYHr";
	private static final String PROJECT_ID_2 = "AVu3b-MAphY78UZXuYHp";
	private static final String EXCEPTION = "rest client exception";
	private static final String PLAIN_TEXT_PASSWORD = "Test@123";
	@Mock
	SonarClient sonarClient;
	@InjectMocks
	private SonarProcessorJobExecutor jobExecutor;
	@Mock
	private SonarProcessorItemRepository sonarProjectRepository;
	@Mock
	private SonarProcessorRepository sonarProcessorRepository;
	@Mock
	private SonarDetailsRepository sonarDetailsRepository;
	@Mock
	private SonarConfig sonarConfig;
	@Mock
	private SonarClientFactory sonarClientSelector;
	@Mock
	private Sonar6And7Client sonar6And7Client;
	@Mock
	private Sonar6And7Client sonar8Client;
	@Mock
	private SonarHistoryRepository sonarHistoryRepository;
	@Mock
	private ProjectToolConfigRepository projectToolConfigRepository;
	@Mock
	private ConnectionRepository connectionRepository;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private AesEncryptionService aesEncryptionService;
	@Mock
	private RestOperations rest;
	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;
	@Mock
	private ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	private List<ProcessorToolConnection> connList = new ArrayList<>();

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		SonarProcessor sonarProcessor = new SonarProcessor();
		ProcessorToolConnection SONARSAMPLESERVER = new ProcessorToolConnection();
		SONARSAMPLESERVER.setId(new ObjectId("624fce22b2ee234aa990adfc"));
		SONARSAMPLESERVER.setUrl("http://testUser@test.com");
		SONARSAMPLESERVER.setUsername("testUser");
		SONARSAMPLESERVER.setPassword("testPassword");
		SONARSAMPLESERVER.setOffline(true);
		ProcessorToolConnection SONARSAMPLESERVER1 = new ProcessorToolConnection();
		SONARSAMPLESERVER1.setId(new ObjectId("624fce6e202f2154bf89ea57"));
		SONARSAMPLESERVER1.setUrl("http://testUser@test.com");
		SONARSAMPLESERVER1.setUsername("testUser");
		SONARSAMPLESERVER1.setPassword("testPassword");
		SONARSAMPLESERVER1.setOffline(false);

		connList.add(SONARSAMPLESERVER);
		connList.add(SONARSAMPLESERVER1);

		List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfigList.add(projectConfig);
		projectConfig.setId(new ObjectId("624d5c9ed837fc14d40b3039"));

		Mockito.when(sonarProcessorRepository.findByProcessorName(Mockito.anyString())).thenReturn(sonarProcessor);
		Mockito.when(sonarProcessorRepository.save(sonarProcessor)).thenReturn(sonarProcessor);
		when(aesEncryptionService.decrypt(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
				.thenReturn(PLAIN_TEXT_PASSWORD);
		when(projectConfigRepository.findAll()).thenReturn(projectConfigList);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(Mockito.any(), Mockito.any()))
				.thenReturn(connList);
		// SonarClient client2 = mock(SonarClient.class);
		when(sonarClient.getSonarProjectList(Mockito.any())).thenReturn(getExistingProjects());

	}

	@Test
	public void processEmpty() throws Exception {
		verifyNoInteractions(sonarClientSelector, sonarDetailsRepository);
	}

	@Test
	public void processOneServer43() throws Exception {
		when(sonarConfig.getCustomApiBaseUrl()).thenReturn(CUSTOM_API_BASE_URL);
		try {
			when(sonarClientSelector.getSonarClient(Mockito.anyString())).thenReturn(sonar8Client);
			jobExecutor.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception is: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void processOneServer54() throws Exception {
		when(sonarConfig.getCustomApiBaseUrl()).thenReturn(CUSTOM_API_BASE_URL);
		try {
			when(sonarClientSelector.getSonarClient(Mockito.anyString())).thenReturn(sonar6And7Client);
			jobExecutor.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception in execute: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void processOneServer63() throws Exception {
		when(sonarConfig.getCustomApiBaseUrl()).thenReturn(CUSTOM_API_BASE_URL);
		try {
			when(sonarClientSelector.getSonarClient(Mockito.anyString())).thenReturn(sonar6And7Client);
			jobExecutor.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception: ", EXCEPTION, exception.getMessage());
		}
	}

	@Test
	public void processTwoServer43And54() throws Exception {
		when(sonarConfig.getCustomApiBaseUrl()).thenReturn(CUSTOM_API_BASE_URL);

		try {
			when(sonarClientSelector.getSonarClient(Mockito.anyString())).thenReturn(sonar8Client);
			jobExecutor.execute(processorWithTwoServers());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception: ", EXCEPTION, exception.getMessage());
		}
	}

	private SonarProcessor processorWithOneServer() {
		return SonarProcessor.getSonarConfig(Collections.singletonList(METRICS1));
	}

	private SonarProcessor processorWithTwoServers() {
		return SonarProcessor.getSonarConfig(Arrays.asList(METRICS1, METRICS2));
	}

	@Test
	public void testGetProcessor() {
		Assert.assertNotNull(jobExecutor.getProcessor());
	}

	@Test
	public void testGetProcessorRepository() {
		Assert.assertNotNull(jobExecutor.getProcessorRepository());
	}

	@Test
	public void testGetCron() {
		when(sonarConfig.getCron()).thenReturn("0 0/1 * * * *");
		Assert.assertNotNull(jobExecutor.getCron());
	}

	private List<SonarProcessorItem> getProjects() {
		List<SonarProcessorItem> projectList = new ArrayList<>();
		SonarProcessorItem project = getProject();
		projectList.add(project);

		return projectList;

	}

	private List<SonarProcessorItem> getOtherProjects() {
		List<SonarProcessorItem> projectList = new ArrayList<>();
		SonarProcessorItem project = new SonarProcessorItem();
		project.setInstanceUrl("http://test1.com");
		project.setProjectName("testPackage.sonar:TestProject2");
		project.setProjectId(PROJECT_ID);
		project.setKey(PROJECT_ID);
		project.setLatestVersion("6.5");
		project.setToolConfigId(new ObjectId());
		projectList.add(project);

		return projectList;

	}

	private List<SonarProcessorItem> getOtherProjectsWithDifferentVersion() {
		List<SonarProcessorItem> projectList = new ArrayList<>();
		SonarProcessorItem project = new SonarProcessorItem();
		project.setInstanceUrl("http://test1.com");
		project.setProjectName("testPackage.sonar:TestProject2");
		project.setProjectId(PROJECT_ID);
		project.setKey(PROJECT_ID);
		project.setLatestVersion("6.6");
		project.setTimestamp(1_570_018_143L);
		project.setToolConfigId(new ObjectId());
		projectList.add(project);

		return projectList;

	}

	private List<SonarProcessorItem> getExistingProjects() {
		List<SonarProcessorItem> projectList = new ArrayList<>();
		SonarProcessorItem project = new SonarProcessorItem();
		project.setInstanceUrl(SONAR_URL);
		project.setProjectName("testPackage.sonar:TestProject1");
		project.setProjectId("AVu3b-MAphY78UZXuYHq");
		project.setKey("AVu3b-MAphY78UZXuYHq");
		project.setTimestamp(1_570_018_174L);
		projectList.add(project);

		return projectList;

	}

	private SonarProcessorItem getProject() {
		SonarProcessorItem project = new SonarProcessorItem();
		project.setInstanceUrl(SONAR_URL);
		project.setProjectName("testPackage.sonar:TestProject");
		project.setProjectId(PROJECT_ID_2);
		project.setKey(PROJECT_ID_2);
		project.setActive(true);
		return project;
	}

	@Test
	public void process() throws Exception {
		Set<ObjectId> udId = new HashSet<>();
		udId.add(OBJ_ID);

		String historyJson = getJson("sonar6_measures_history.json");

		SonarProcessorItem project = getProject();
		String historyUrl = String.format(
				new StringBuilder(project.getInstanceUrl()).append(URL_MEASURE_HISTORY).toString(), project.getKey(),
				METRICS, DEFAULT_DATE);

		doReturn(new ResponseEntity<>(historyJson, HttpStatus.OK)).when(rest).exchange(ArgumentMatchers.eq(historyUrl),
				ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				ArgumentMatchers.eq(String.class));

		when(sonarClientSelector.getSonarClient(Mockito.anyString())).thenReturn(sonar6And7Client);
		when(sonarProjectRepository.findByProcessorIdIn(Mockito.any())).thenReturn(getExistingProjects());
		when(sonarConfig.getMetrics()).thenReturn(Arrays.asList(METRICS));
		when(sonarConfig.getCustomApiBaseUrl()).thenReturn(CUSTOM_API_BASE_URL);

		when(sonarProjectRepository.findEnabledProjectsForTool(Mockito.any(), Mockito.any(),
				ArgumentMatchers.eq(SONAR_URL))).thenReturn(getProjects());

		String cacheUrl = CUSTOM_API_BASE_URL + SONAR_CACHE_END_POINT;
		String reponseBody = "{}";
		doReturn(new ResponseEntity<>(reponseBody, HttpStatus.OK)).when(restTemplate).exchange(
				ArgumentMatchers.eq(cacheUrl), ArgumentMatchers.eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(String.class));
		try {
			SonarDetails sonarDetails = new SonarDetails();
			sonarDetails.setName("testSonarDetails");
			sonarDetails.setTimestamp(1_570_019_204L);
			sonarDetails.setType(SonarAnalysisType.STATIC_ANALYSIS);
			when(sonar6And7Client.getLatestSonarDetails(Mockito.any(), Mockito.any(), Mockito.anyString()))
					.thenReturn(sonarDetails);

			SonarHistory sonarHistory = createSonarHistory();
			List<SonarHistory> sonarHistoryList = new ArrayList<>();
			sonarHistoryList.add(sonarHistory);

			when(sonar6And7Client.getPastSonarDetails(Mockito.any(), Mockito.any(), ArgumentMatchers.anyString()))
					.thenReturn(sonarHistoryList);

			when(sonarDetailsRepository.save(sonarDetails)).thenReturn(sonarDetails);
			when(sonarHistoryRepository.saveAll(Mockito.anyList())).thenReturn(null);
			when(sonarProjectRepository.save(ArgumentMatchers.any(SonarProcessorItem.class))).thenReturn(getProject());

			jobExecutor.execute(processorWithOneServer());
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception: ", "rest client exception", exception.getMessage());
		}
	}

	private SonarHistory createSonarHistory() {
		SonarHistory sonarHistory = new SonarHistory();
		sonarHistory.setName("testCodeQuality2");
		sonarHistory.setTimestamp(1_570_019_222L);
		sonarHistory.setType(SonarAnalysisType.STATIC_ANALYSIS);
		return sonarHistory;
	}

	private String getJson(String fileName) throws IOException {
		String inputData = null;
		InputStream inputStream = Sonar6And7ClientTest.class.getResourceAsStream(fileName);
		try {
			inputData = IOUtils.toString(inputStream);
		} catch (IOException ex) {
			inputData = "";
		} finally {
			inputStream.close();
		}
		return inputData;
	}

	@Test
	public void testAddNewProjects() throws Exception {
		List<SonarProcessorItem> existingProjects = getExistingProjects();
		List<SonarProcessorItem> newProjects = getProjects();
		SonarProcessor sonarprocessor = new SonarProcessor();
		sonarprocessor.setId(OBJ_ID);

		Whitebox.invokeMethod(jobExecutor, "addNewProjects", newProjects, existingProjects, sonarprocessor);
		Assert.assertNotNull(newProjects);
	}

	@Test
	public void testAddNewProjectsSonar6LowerClient() throws Exception {
		List<SonarProcessorItem> existingProjects = getOtherProjects();
		List<SonarProcessorItem> newProjects = getOtherProjectsWithDifferentVersion();

		SonarProcessor sonarprocessor = new SonarProcessor();
		sonarprocessor.setId(OBJ_ID);

		Whitebox.invokeMethod(jobExecutor, "addNewProjects", newProjects, existingProjects, sonarprocessor);
		Assert.assertNotNull(newProjects);
	}
}