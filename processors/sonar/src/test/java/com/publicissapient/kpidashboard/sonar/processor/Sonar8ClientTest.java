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
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;
import com.publicissapient.kpidashboard.sonar.config.SonarConfig;
import com.publicissapient.kpidashboard.sonar.model.Paging;
import com.publicissapient.kpidashboard.sonar.model.SonarProcessorItem;
import com.publicissapient.kpidashboard.sonar.processor.adapter.impl.Sonar8Client;

@RunWith(MockitoJUnitRunner.class)
public class Sonar8ClientTest {
	private static final String URL_RESOURCES = "/api/components/search?qualifiers=TRK&p=1&ps=500";
	private static final String URL_RESOURCE_DETAILS = "/api/measures/component?format=json&componentId=%s&metricKeys=%s&includealerts=true";
	private static final String URL_PROJECT_ANALYSES = "/api/project_analyses/search?project=%s";
	private static final String SONAR_URL = "http://sonar.com";
	private static final ProcessorToolConnection SONAR_SERVER = new ProcessorToolConnection();
	private static final ProcessorToolConnection SONAR_CLOUD = new ProcessorToolConnection();
	private static final String METRICS = "lines,ncloc,violations,new_vulnerabilities,critical_violations,major_violations,blocker_violations,minor_violations,info_violations,tests,test_success_density,test_errors,test_failures,coverage,line_coverage,sqale_index,alert_status,quality_gate_details,sqale_rating";
	private static final String URL_MEASURE_HISTORY = "/api/measures/search_history?component=%s&metrics=%s&includealerts=true&from=%s";
	private static final String DEFAULT_DATE = "2018-01-01";
	private static final String PROJECT_SIZE = "500";
	private static final String USER_NAME = "test";
	private static final String PASSWORD = "password";
	private static final String ACCESSTOKEN = "accessToken";
	private static final String EXCEPTION = "rest client exception";
	@Mock
	ToolCredentialProvider toolCredentialProvider;
	@Mock
	private RestOperationsFactory<RestOperations> restOperationsFactory;
	@Mock
	private SonarConfig sonarSettings;
	@Mock
	private RestOperations rest;
	private Sonar8Client sonar8Client;

	public static HttpHeaders createHeaders(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		if (accessToken != null && !accessToken.isEmpty()) {
			headers.add("Authorization", "Bearer " + accessToken);
		}
		return headers;
	}

	@Before
	public void init() {
		Mockito.when(restOperationsFactory.getTypeInstance()).thenReturn(rest);
		SONAR_SERVER.setUrl(SONAR_URL);
		sonar8Client = new Sonar8Client(restOperationsFactory, sonarSettings, toolCredentialProvider);
	}

	@Test
	public void testProjectsException() throws Exception {
		String projectsUrl = SONAR_URL + URL_RESOURCES;
		Mockito.when(sonarSettings.getPageSize()).thenReturn(500);

		Mockito.doThrow(new RestClientException("rest client exception")).when(rest).exchange(Mockito.eq(projectsUrl),
				Mockito.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class), Mockito.eq(String.class));

		SONAR_SERVER.setUsername(USER_NAME);
		SONAR_SERVER.setPassword(PASSWORD);
		try {
			sonar8Client.getSonarProjectList(SONAR_SERVER);
		} catch (RestClientException exception) {
			Assert.assertEquals("Exception: ", "rest client exception", exception.getMessage());
		}
	}

	@Test
	public void testGetSonarProjectListSuccess() throws Exception {
		String projectJson = getJson("sonar8projects.json");
		String projectsUrl = SONAR_URL + URL_RESOURCES;
		when(sonarSettings.getPageSize()).thenReturn(500);
		doReturn(new ResponseEntity<>(projectJson, HttpStatus.OK)).when(rest).exchange(ArgumentMatchers.eq(projectsUrl),
				ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				ArgumentMatchers.eq(String.class));

		SONAR_SERVER.setUsername(USER_NAME);
		SONAR_SERVER.setPassword(PASSWORD);
		List<SonarProcessorItem> projects = sonar8Client.getSonarProjectList(SONAR_SERVER);
		Assert.assertThat("Projects count: ", projects.size(), is(2));
		Assert.assertThat("First Project name: ", projects.get(0).getProjectName(),
				is("testPackage.sonar:TestProject"));
		Assert.assertThat("Second Project name: ", projects.get(1).getProjectName(),
				is("testPackage.sonar:AnotherTestProject"));
		Assert.assertThat("First Project id: ", projects.get(0).getProjectId(), is("AVu3b-MAphY78UZXuYHp"));
		Assert.assertThat("Second Project id: ", projects.get(1).getProjectId(), is("BVx3b-MAphY78UZXuYHp"));
	}

	@Test
	public void testGetPastSonarDetails() {
		SonarProcessorItem project = getProject();
		String historyUrl = String.format(
				new StringBuilder(project.getInstanceUrl()).append(URL_MEASURE_HISTORY).append("&p=1").toString(),
				project.getKey(), METRICS, DEFAULT_DATE);

		doThrow(new RestClientException("rest client exception")).when(rest).exchange(ArgumentMatchers.eq(historyUrl),
				ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				ArgumentMatchers.eq(String.class));

		SONAR_SERVER.setUsername(USER_NAME);
		SONAR_SERVER.setPassword(PASSWORD);

		List<SonarHistory> codeQualityHistories = sonar8Client.getPastSonarDetails(getProject(),
				new HttpEntity<>(createHeaders(SONAR_SERVER.getUsername(), SONAR_SERVER.getPassword())), METRICS);
		Assert.assertEquals("Data size: ", 0, codeQualityHistories.size());
	}

	private SonarProcessorItem getProject() {
		SonarProcessorItem project = new SonarProcessorItem();
		project.setInstanceUrl(SONAR_URL);
		project.setProjectName("testPackage.sonar:TestProject");
		project.setProjectId("AVu3b-MAphY78UZXuYHp");
		project.setKey("KEY");
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
	public void testGetPastSonarDetails1() throws IOException {
		String historyJson = getJson("sonar8_measures_history.json");
		String history2Json = getJson("sonar8_measures_history_empty.json");
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
		List<SonarHistory> codeQualityHistories = sonar8Client.getPastSonarDetails(getProject(),
				new HttpEntity<>(createHeaders(SONAR_SERVER.getUsername(), SONAR_SERVER.getPassword())), METRICS);
		Assert.assertNotNull("History data available: ", codeQualityHistories);
		Assert.assertEquals("History data size: ", 3, codeQualityHistories.size());
	}

	@Test
	public void testGetPastSonarCloudDetails1() throws IOException {
		String historyJson = getJson("sonar8_measures_history.json");
		String history2Json = getJson("sonar8_measures_history_empty.json");
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
		SONAR_CLOUD.setAccessToken(ACCESSTOKEN);
		List<SonarHistory> codeQualityHistories = sonar8Client.getPastSonarDetails(getProject(),
				new HttpEntity<>(createHeaders(SONAR_CLOUD.getAccessToken())), METRICS);
		Assert.assertNotNull("History data available: ", codeQualityHistories);
		Assert.assertEquals("History data size: ", 3, codeQualityHistories.size());
	}

	private String getJson(String fileName) throws IOException {
		String inputData = null;
		try (InputStream inputStream = Sonar8ClientTest.class.getResourceAsStream(fileName)) {
			inputData = IOUtils.toString(inputStream);
		} catch (IOException ex) {
			inputData = "";
		}
		return inputData;
	}

	@Test
	public void testGetSonarProjectListFail() throws Exception {
		String projectJson = getJson("sonar8projects.json");
		String projectsUrl = SONAR_URL + URL_RESOURCES;
		when(sonarSettings.getPageSize()).thenReturn(500);
		doReturn(new ResponseEntity<>(projectJson, HttpStatus.EXPECTATION_FAILED)).when(rest).exchange(
				ArgumentMatchers.eq(projectsUrl), ArgumentMatchers.eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(String.class));

		SONAR_SERVER.setUsername(USER_NAME);
		SONAR_SERVER.setPassword(PASSWORD);
		List<SonarProcessorItem> projects = sonar8Client.getSonarProjectList(SONAR_SERVER);
		Assert.assertEquals("Project size is: ", 0, projects.size());

	}

	@Test(expected = NullPointerException.class)
	public void testGetLatestSonarCloudDetails() throws Exception {
		SonarDetails sonarDetail = sonar8Client.getLatestSonarDetails(getProject(),
				new HttpEntity<>(createHeaders(SONAR_CLOUD.getAccessToken())), METRICS);
	}

	@Test
	public void testGetTotalPages() throws Exception {

		Paging paging = new Paging();
		paging.setPageIndex(1);
		paging.setPageSize(500);
		paging.setTotal(1000);
		Whitebox.invokeMethod(sonar8Client, "getTotalPages", paging);
		Assert.assertNotNull(paging);
	}

	@Test(expected = NullPointerException.class)
	public void testGetLatestSonarDetails() throws Exception {
		SonarDetails sonarDetail = sonar8Client.getLatestSonarDetails(getProject(),
				new HttpEntity<>(createHeaders(SONAR_SERVER.getUsername(), SONAR_SERVER.getPassword())), METRICS);
	}
}
