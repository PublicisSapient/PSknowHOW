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

package com.publicissapient.kpidashboard.bamboo.client.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.bamboo.client.BambooClient;
import com.publicissapient.kpidashboard.bamboo.config.BambooConfig;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class BambooClientDeployImplTest {

	private static final String DOES = "test";

	private static final String ALL_DEPLOYMENT_JOBS = "/rest/api/latest/search/deployments.json?max-result=2000";
	private static final String DOES_MATTER = "does:matter";
	private static final String BASE_URL = "https://xyz.com/bamboo/";
	private static final String HTTP_BAMBOO_COM_JOB_JOB1 = "http://bamboo.com/job/job1";
	private static final String BASE_URL_WITH_ENVIRONMENT = "/rest/api/latest/deploy/environment/test/results";
	private static final String MATTER = "test";
	private static final String HTTP_SERVER_JOB_JOB2_2 = "https://xyz.com/bamboo/rest/api/latest/result/HDEP-AST/140";
	private static final String URL_TEST = HTTP_SERVER_JOB_JOB2_2;
	private static final String HTTP_EMAIL = "http://does:matter@bamboo.com";
	private static final ProcessorToolConnection PROJECT_TOOL_CONNECTION_1 = new ProcessorToolConnection();
	private static final ProcessorToolConnection PROJECT_TOOL_CONNECTION_2 = new ProcessorToolConnection();
	@Mock
	private RestTemplate restClient;
	@Mock
	private BambooConfig settings;
	@Mock
	private ProjectToolConfigRepository toolConfigRepository;
	@InjectMocks
	private BambooClientDeployImpl bambooClientDeploy;

	@Before
	public void init() {
		List<ProjectToolConfig> toolList = new ArrayList<>();
		ProjectToolConfig t1 = new ProjectToolConfig();
		t1.setToolName(ProcessorConstants.BAMBOO);
		t1.setJobName("1");
		t1.setBranch("appbranch");
		t1.setJobType("deploy");
		toolList.add(t1);
		bambooClientDeploy = new BambooClientDeployImpl();
		PROJECT_TOOL_CONNECTION_1.setId(new ObjectId());
		PROJECT_TOOL_CONNECTION_1.setDeploymentProjectId("190709761");
		PROJECT_TOOL_CONNECTION_1.setDeploymentProjectName("KnowHowDeployemnt");
		PROJECT_TOOL_CONNECTION_1.setJobName("HDEP-AST");
		PROJECT_TOOL_CONNECTION_1.setToolName("Bamboo");
		PROJECT_TOOL_CONNECTION_1.setConnectionId(new ObjectId("5fa69f5d220038d6a365fec6"));
		PROJECT_TOOL_CONNECTION_1.setConnectionName("Bamboo connection");
		PROJECT_TOOL_CONNECTION_1.setUrl(BASE_URL);
		PROJECT_TOOL_CONNECTION_1.setUsername(DOES);
		PROJECT_TOOL_CONNECTION_1.setPassword(MATTER);

		PROJECT_TOOL_CONNECTION_2.setId(new ObjectId());
		PROJECT_TOOL_CONNECTION_2.setDeploymentProjectId("16089089");
		PROJECT_TOOL_CONNECTION_2.setDeploymentProjectName("UGC Services Deployment");
		PROJECT_TOOL_CONNECTION_2.setJobName("HDEP-AST");
		PROJECT_TOOL_CONNECTION_2.setBranch("CFDP-DTI");
		PROJECT_TOOL_CONNECTION_2.setToolName("Bamboo");
		PROJECT_TOOL_CONNECTION_2.setConnectionId(new ObjectId("5fa69f5d220038d6a365fec6"));
		PROJECT_TOOL_CONNECTION_2.setConnectionName("Bamboo connection");
		PROJECT_TOOL_CONNECTION_2.setUrl(BASE_URL);
		PROJECT_TOOL_CONNECTION_2.setUsername(DOES);
		PROJECT_TOOL_CONNECTION_2.setPassword(MATTER);

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void appendToURLTest1() throws Exception {
		String url = BambooClient.appendToURL(BASE_URL, ALL_DEPLOYMENT_JOBS);
		assertEquals("appendToURL() with one param test",
				"https://xyz.com/bamboo/rest/api/latest/search/deployments.json?max-result=2000", url);
	}

	@Test
	public void appendToURLTest2() throws Exception {
		String u4 = BambooClient.appendToURL(BASE_URL, BASE_URL_WITH_ENVIRONMENT, "1234");
		assertEquals("appendToURL() with two params test",
				"https://xyz.com/bamboo/rest/api/latest/deploy/environment/test/results/1234", u4);
	}

	// @Test
	public void checkBambooConnection() {
		try {
			HttpEntity<String> headers = generateHeader("test:decryptPassword");
			when(restClient.exchange(
					eq(URI.create("https://xyz.com/bamboo/rest/api/latest/deploy/dashboard/190709761")),
					eq(HttpMethod.GET), eq(headers), eq(String.class))).thenReturn(
							new ResponseEntity<>(getServerResponseFromJson("project_details.json"), HttpStatus.OK));
			bambooClientDeploy.connectBamboo("https://xyz.com/bamboo/rest/api/latest/deploy/dashboard/190709761",
					PROJECT_TOOL_CONNECTION_1, headers);
			verify(restClient).exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), eq(headers),
					eq(String.class));
		} catch (IOException e) {
			log.error("Exception " + e);
		}
	}

	private HttpEntity<String> generateHeader(String userName) {

		HttpEntity<String> respEntity = null;
		String userInfo = userName;
		if (StringUtils.isNotBlank(userInfo)) {
			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.AUTHORIZATION,
					"Basic " + Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.US_ASCII)));
			headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
			respEntity = new HttpEntity<>(headers);
		}
		return respEntity;

	}

	private String getServerResponseFromJson(String fileName) throws IOException {
		String filePath = "src/test/resources/" + fileName;
		return new String(Files.readAllBytes(Paths.get(filePath)));
	}

}
