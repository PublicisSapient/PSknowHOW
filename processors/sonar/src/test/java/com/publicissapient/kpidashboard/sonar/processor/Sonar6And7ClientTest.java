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

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.common.constant.SonarAnalysisType;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;
import com.publicissapient.kpidashboard.sonar.config.SonarConfig;
import com.publicissapient.kpidashboard.sonar.model.SonarProcessorItem;
import com.publicissapient.kpidashboard.sonar.processor.adapter.impl.Sonar6And7Client;

@RunWith(MockitoJUnitRunner.class)
public class Sonar6And7ClientTest {
	private static final String URL_RESOURCES = "/api/components/search?qualifiers=TRK&p=1&ps=500";
	private static final String URL_RESOURCE_DETAILS = "/api/measures/component?format=json&componentId=%s&metricKeys=%s&includealerts=true";
	private static final String URL_PROJECT_ANALYSES = "/api/project_analyses/search?project=%s";
	private static final String SONAR_URL = "http://sonar.com";
	private static final String SONAR_CLOUD_URL = "https://sonarcloud.io";
	private static final ProcessorToolConnection SONAR_SERVER = new ProcessorToolConnection();
	private static final ProcessorToolConnection SONAR_CLOUD = new ProcessorToolConnection();
	private static final String METRICS = "lines,ncloc,violations,new_vulnerabilities,critical_violations,major_violations,blocker_violations,minor_violations,info_violations,tests,test_success_density,test_errors,test_failures,coverage,line_coverage,sqale_index,alert_status,quality_gate_details,sqale_rating";
	private static final String URL_MEASURE_HISTORY = "/api/measures/search_history?component=%s&metrics=%s&includealerts=true&from=%s";
	private static final String DEFAULT_DATE = "2018-01-01";
	private static final String PROJECT_SIZE = "500";
	private static final String USER_NAME = "test";
	private static final String PASSWORD = "password";
	private static final String ACCESS_TOKEN = "testAccessToken";
	@Mock
	private RestOperationsFactory<RestOperations> restOperationsFactory;
	@Mock
	private RestOperations rest;
	@Mock
	private SonarConfig sonarSettings;
	private Sonar6And7Client sonar6And7Client;
	@Mock
	private ToolCredentialProvider toolCredentialProvider;

	public static HttpHeaders createHeaders(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		if (accessToken != null && !accessToken.isEmpty()) {
			headers.add("Authorization", "Bearer " + accessToken);
		}
		return headers;
	}

	@Before
	public void init() {
		when(restOperationsFactory.getTypeInstance()).thenReturn(rest);
		SONAR_SERVER.setUrl(SONAR_URL);
		SONAR_CLOUD.setUrl(SONAR_CLOUD_URL);
		sonar6And7Client = new Sonar6And7Client(restOperationsFactory, sonarSettings, toolCredentialProvider);

	}

	@Test
	public void testGetSonarProjectListSuccess() throws Exception {
		String projectJson = getJson("sonar6projects.json");
		String projectsUrl = SONAR_URL + URL_RESOURCES;
		when(sonarSettings.getPageSize()).thenReturn(500);
		doReturn(new ResponseEntity<>(projectJson, HttpStatus.OK)).when(rest).exchange(ArgumentMatchers.eq(projectsUrl),
				ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				ArgumentMatchers.eq(String.class));
		SONAR_SERVER.setUsername(USER_NAME);
		SONAR_SERVER.setPassword(PASSWORD);
		List<SonarProcessorItem> projects = sonar6And7Client.getSonarProjectList(SONAR_SERVER);
		Assert.assertThat("Projects count: ", projects.size(), is(2));
		Assert.assertThat("First Project name: ", projects.get(0).getProjectName(),
				is("testPackage.sonar:TestProject"));
		Assert.assertThat("Second Project name: ", projects.get(1).getProjectName(),
				is("testPackage.sonar:AnotherTestProject"));
		Assert.assertThat("First Project id: ", projects.get(0).getProjectId(), is("AVu3b-MAphY78UZXuYHp"));
		Assert.assertThat("Second Project id: ", projects.get(1).getProjectId(), is("BVx3b-MAphY78UZXuYHp"));
	}

	@Test
	public void testGetSonarProjectListFail() throws Exception {
		String projectJson = getJson("sonar6projects.json");
		String projectsUrl = SONAR_URL + URL_RESOURCES;
		when(sonarSettings.getPageSize()).thenReturn(500);
		doReturn(new ResponseEntity<>(projectJson, HttpStatus.EXPECTATION_FAILED)).when(rest).exchange(
				ArgumentMatchers.eq(projectsUrl), ArgumentMatchers.eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(String.class));

		SONAR_SERVER.setUsername(USER_NAME);
		SONAR_SERVER.setPassword(PASSWORD);
		List<SonarProcessorItem> projects = sonar6And7Client.getSonarProjectList(SONAR_SERVER);
		Assert.assertEquals("Project size is: ", 0, projects.size());

	}

	@Test
	public void testProjectsException() throws Exception {
		String projectsUrl = SONAR_URL + URL_RESOURCES;
		when(sonarSettings.getPageSize()).thenReturn(500);

		doThrow(new RestClientException("rest client exception")).when(rest).exchange(ArgumentMatchers.eq(projectsUrl),
				ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				ArgumentMatchers.eq(String.class));

		SONAR_SERVER.setUsername(USER_NAME);
		SONAR_SERVER.setPassword(PASSWORD);
		try {
			sonar6And7Client.getSonarProjectList(SONAR_SERVER);
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception: ", "rest client exception", exception.getMessage());
		}

	}

	@Test
	public void testGetLatestSonarDetails() throws Exception {
		String measureJson = getJson("sonar6measures.json");
		String analysesJson = getJson("sonar6analyses.json");
		SonarProcessorItem project = getProject();
		String measureUrl = String.format(SONAR_URL + URL_RESOURCE_DETAILS, project.getProjectId(), METRICS);
		String analysesUrl = String.format(SONAR_URL + URL_PROJECT_ANALYSES, project.getProjectName());
		doReturn(new ResponseEntity<>(measureJson, HttpStatus.OK)).when(rest).exchange(ArgumentMatchers.eq(measureUrl),
				ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				ArgumentMatchers.eq(String.class));
		doReturn(new ResponseEntity<>(analysesJson, HttpStatus.OK)).when(rest).exchange(
				ArgumentMatchers.eq(analysesUrl), ArgumentMatchers.eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(String.class));
		SonarDetails sonarDetail = sonar6And7Client.getLatestSonarDetails(getProject(),
				new HttpEntity<>(createHeaders(SONAR_SERVER.getUsername(), SONAR_SERVER.getPassword())), METRICS);
		Assert.assertThat("Sonar metrics: ", sonarDetail.getMetrics().size(), is(19));
		Assert.assertThat("Type: ", sonarDetail.getType(), is(SonarAnalysisType.STATIC_ANALYSIS));
		Assert.assertThat("Sonar project name: ", sonarDetail.getName(), is("testPackage.sonar:TestProject"));
		Assert.assertThat("Sonar version: ", sonarDetail.getVersion(), is("2.0.0"));
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

	private SonarProcessorItem getProject() {
		SonarProcessorItem project = new SonarProcessorItem();
		project.setInstanceUrl(SONAR_URL);
		project.setProjectName("testPackage.sonar:TestProject");
		project.setProjectId("AVu3b-MAphY78UZXuYHp");
		return project;
	}

	private HttpHeaders createHeaders(String username, String password) {
		HttpHeaders headers = new HttpHeaders();
		if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
			String auth = username + ":" + password;
			byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
			String authHeader = "Basic " + new String(encodedAuth);
			headers.set("Authorization", authHeader);
		}
		return headers;
	}

	@Test
	public void testGetPastSonarCloudDetails1() {

		SonarProcessorItem project = getProject();
		String historyUrl = String.format(
				new StringBuilder(project.getInstanceUrl()).append(URL_MEASURE_HISTORY).append("&p=1").toString(),
				project.getKey(), METRICS, DEFAULT_DATE);

		doThrow(new RestClientException("rest client exception")).when(rest).exchange(ArgumentMatchers.eq(historyUrl),
				ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				ArgumentMatchers.eq(String.class));

		SONAR_CLOUD.setAccessToken(ACCESS_TOKEN);

		List<SonarHistory> codeQualityHistories = sonar6And7Client.getPastSonarDetails(getProject(),
				new HttpEntity<>(createHeaders(SONAR_CLOUD.getAccessToken())), METRICS);
		Assert.assertEquals("Data size: ", 0, codeQualityHistories.size());

	}

	@Test
	public void testGetPastSonarDetails() throws IOException {
		String historyJson = getJson("sonar6_measures_history.json");
		String history2Json = getJson("sonar6_measures_history_empty.json");

		SonarProcessorItem project = getProject();
		String historyUrl = String.format(
				new StringBuilder(project.getInstanceUrl()).append(URL_MEASURE_HISTORY).append("&p=1").toString(),
				project.getKey(), METRICS, DEFAULT_DATE);
		String historyEmptyUrl = String.format(
				new StringBuilder(project.getInstanceUrl()).append(URL_MEASURE_HISTORY).append("&p=2").toString(),
				project.getKey(), METRICS, DEFAULT_DATE);

		doReturn(new ResponseEntity<>(historyJson, HttpStatus.OK)).when(rest).exchange(ArgumentMatchers.eq(historyUrl),
				ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				ArgumentMatchers.eq(String.class));

		doReturn(new ResponseEntity<>(history2Json, HttpStatus.OK)).when(rest).exchange(
				ArgumentMatchers.eq(historyEmptyUrl), ArgumentMatchers.eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(String.class));

		SONAR_SERVER.setUsername(USER_NAME);
		SONAR_SERVER.setPassword(PASSWORD);
		List<SonarHistory> codeQualityHistories = sonar6And7Client.getPastSonarDetails(getProject(),
				new HttpEntity<>(createHeaders(SONAR_SERVER.getUsername(), SONAR_SERVER.getPassword())), METRICS);

		Assert.assertNotNull("History data available: ", codeQualityHistories);
		Assert.assertEquals("History data size: ", 3, codeQualityHistories.size());

	}

	@Test
	public void testGetPastSonarDetails1() {

		SonarProcessorItem project = getProject();
		String historyUrl = String.format(
				new StringBuilder(project.getInstanceUrl()).append(URL_MEASURE_HISTORY).append("&p=1").toString(),
				project.getKey(), METRICS, DEFAULT_DATE);

		doThrow(new RestClientException("rest client exception")).when(rest).exchange(ArgumentMatchers.eq(historyUrl),
				ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				ArgumentMatchers.eq(String.class));

		SONAR_SERVER.setUsername(USER_NAME);
		SONAR_SERVER.setPassword(PASSWORD);

		List<SonarHistory> codeQualityHistories = sonar6And7Client.getPastSonarDetails(getProject(),
				new HttpEntity<>(createHeaders(SONAR_SERVER.getUsername(), SONAR_SERVER.getPassword())), METRICS);
		Assert.assertEquals("Data size: ", 0, codeQualityHistories.size());

	}

}