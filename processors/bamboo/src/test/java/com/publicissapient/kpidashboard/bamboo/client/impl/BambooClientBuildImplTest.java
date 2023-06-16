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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.json.simple.parser.ParseException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.bamboo.client.BambooClient;
import com.publicissapient.kpidashboard.bamboo.config.BambooConfig;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;

@RunWith(MockitoJUnitRunner.class)
public class BambooClientBuildImplTest {

	private static final String DOES = "does";
	private static final String API_JSON_TREE_JOBS_NAME_URL_BUILDS_NUMBER_URL = "/api/json?tree=jobs[name,url,builds[number,url]]";
	private static final String DOES_MATTER = "does:matter";
	private static final String HTTP_BAMBOO_COM = "http://xyz";
	private static final String HTTP_BAMBOO_COM_JOB_JOB1 = "http://xyz/job/job1";
	private static final String HTTP_BAMBOO_BUILDS_NUMBER_URL = "http://xyz/test/api/json?tree=jobs[name,url,builds[number,url]]";
	private static final String MATTER = "matter";
	private static final String HTTP_SERVER_JOB_JOB2_2 = "https://xyz.com/bamboo/rest/api/latest/result/HDEP-AST/140";
	private static final String URL_TEST = HTTP_SERVER_JOB_JOB2_2;
	private static final String HTTP_EMAIL = "http://does:matter@xyz";
	private static final ProcessorToolConnection BAMBOO_SAMPLE_SERVER_PLAN = new ProcessorToolConnection();
	private static final ProcessorToolConnection BAMBOO_SAMPLE_BRANCH = new ProcessorToolConnection();
	private ProjectBasicConfig proBasicConfig = new ProjectBasicConfig();
	@Mock
	private RestTemplate restClient;
	@Mock
	private BambooConfig settings;
	@Mock
	private ProjectToolConfigRepository toolConfigRepository;
	@InjectMocks
	private BambooClientBuildImpl bambooClientBuild;

	@Before
	public void init() {
		List<ProjectToolConfig> toolList = new ArrayList<>();
		ProjectToolConfig t1 = new ProjectToolConfig();
		t1.setToolName(ProcessorConstants.BAMBOO);
		t1.setJobName("1");
		t1.setBranch("appbranch");
		toolList.add(t1);
		bambooClientBuild = new BambooClientBuildImpl();
		BAMBOO_SAMPLE_SERVER_PLAN.setId(new ObjectId());
		BAMBOO_SAMPLE_SERVER_PLAN.setJobName("HDEP-AST");
		BAMBOO_SAMPLE_SERVER_PLAN.setToolName("Bamboo");
		BAMBOO_SAMPLE_SERVER_PLAN.setConnectionId(new ObjectId("5fa69f5d220038d6a365fec6"));
		BAMBOO_SAMPLE_SERVER_PLAN.setConnectionName("Bamboo connection");
		BAMBOO_SAMPLE_SERVER_PLAN.setUrl(HTTP_EMAIL);
		BAMBOO_SAMPLE_SERVER_PLAN.setUsername(DOES);
		BAMBOO_SAMPLE_SERVER_PLAN.setPassword(MATTER);

		BAMBOO_SAMPLE_BRANCH.setId(new ObjectId());
		BAMBOO_SAMPLE_BRANCH.setJobName("HDEP-AST");
		BAMBOO_SAMPLE_BRANCH.setBranch("CFDP-DTI");
		BAMBOO_SAMPLE_BRANCH.setToolName("Bamboo");
		BAMBOO_SAMPLE_BRANCH.setConnectionId(new ObjectId("5fa69f5d220038d6a365fec6"));
		BAMBOO_SAMPLE_BRANCH.setConnectionName("Bamboo connection");
		BAMBOO_SAMPLE_BRANCH.setUrl(HTTP_EMAIL);
		BAMBOO_SAMPLE_BRANCH.setUsername(DOES);
		BAMBOO_SAMPLE_BRANCH.setPassword(MATTER);

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void appendToURLTest1() throws Exception {

		String url = BambooClient.appendToURL(HTTP_BAMBOO_COM, API_JSON_TREE_JOBS_NAME_URL_BUILDS_NUMBER_URL);
		assertEquals("appendToURL() with one param test", "http://xyz/api/json?tree=jobs[name,url,builds[number,url]]",
				url);
	}

	@Test
	public void appendToURLTest2() throws Exception {
		String u4 = BambooClient.appendToURL("http://xyz/", "test", API_JSON_TREE_JOBS_NAME_URL_BUILDS_NUMBER_URL);
		assertEquals("appendToURL() with two params test", HTTP_BAMBOO_BUILDS_NUMBER_URL, u4);
	}

	@Test
	public void appendToURLTest3() throws Exception {
		String u2 = BambooClient.appendToURL("http://xyz/", "/test/", API_JSON_TREE_JOBS_NAME_URL_BUILDS_NUMBER_URL);
		assertEquals("appendToURL() with extra slash params test", HTTP_BAMBOO_BUILDS_NUMBER_URL, u2);
	}

	@Test
	public void appendToURLTest4() throws Exception {
		String u3 = BambooClient.appendToURL(HTTP_BAMBOO_COM, "///test", API_JSON_TREE_JOBS_NAME_URL_BUILDS_NUMBER_URL);
		assertEquals("appendToURL() with multiple  extra slash params test", HTTP_BAMBOO_BUILDS_NUMBER_URL, u3);
	}

	@Test
	public void finalURLTest1() throws Exception {

		String u1 = bambooClientBuild.getFinalURL(HTTP_BAMBOO_COM_JOB_JOB1, "https://123456:234567@xyz");
		assertEquals("getFinalURL() test 1",
				"https://123456:234567@xyz/job/job1?expand=results.result.artifacts&expand=changes.change.files", u1);
	}

	@Test
	public void finalURLTest2() throws Exception {

		String u2 = bambooClientBuild.getFinalURL("https://xyz/job/job1", "https://123456:234567@xyz");
		assertEquals("getFinalURL() test 2",
				"https://123456:234567@xyz/job/job1?expand=results.result.artifacts&expand=changes.change.files", u2);
	}

	@Test
	public void finalURLTest3() throws Exception {

		String u3 = bambooClientBuild.getFinalURL(HTTP_BAMBOO_COM_JOB_JOB1, "http://123456:234567@xyz");
		assertEquals("getFinalURL() test 3",
				"http://123456:234567@xyz/job/job1?expand=results.result.artifacts&expand=changes.change.files", u3);
	}

	@Test
	public void finalURLTest4() throws Exception {

		String u4 = bambooClientBuild.getFinalURL(HTTP_BAMBOO_COM_JOB_JOB1, "http://123456:234567@xyz");
		assertEquals("getFinalURL() test 4",
				"http://123456:234567@xyz/job/job1?expand=results.result.artifacts&expand=changes.change.files", u4);
	}

	@Test
	public void finalURLTest5() throws Exception {
		String orig = "http://xyz/job/job1%20with%20space";
		String u5 = bambooClientBuild.getFinalURL(orig, HTTP_BAMBOO_COM);
		assertEquals("getFinalURL() test 5", orig + "?expand=results.result.artifacts&expand=changes.change.files", u5);
	}

	@Test
	public void verifyBasicAuth() throws Exception {
		HttpHeaders headers = bambooClientBuild.createHeaders("Aladdin:open sesame");
		assertEquals("verifyBasicAuth", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==",
				headers.getFirst(HttpHeaders.AUTHORIZATION));
	}

	@Test
	public void verifyAuthCredentials() throws Exception {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		HttpEntity headers = new HttpEntity(bambooClientBuild.createHeaders(DOES_MATTER));
		when(restClient.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class)))
				.thenReturn(new ResponseEntity<>("", HttpStatus.OK));
		bambooClientBuild.makeBambooServerCall("http://user:pass@xyz", BAMBOO_SAMPLE_SERVER_PLAN);
		verify(restClient).exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class));
	}

	@Test
	public void verifyAuthCredentialsBySettings() throws Exception {

		HttpEntity headers = new HttpEntity(bambooClientBuild.createHeaders(DOES_MATTER));
		when(restClient.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class)))
				.thenReturn(new ResponseEntity<>("", HttpStatus.OK));

		settings.setApiKey(MATTER);
		settings.setUsername(DOES);
		bambooClientBuild.makeBambooServerCall(HTTP_BAMBOO_COM, BAMBOO_SAMPLE_SERVER_PLAN);
		verify(restClient).exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class));
	}

	@Test
	public void verifyGetLogUrl() throws Exception {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		HttpEntity headers = new HttpEntity(bambooClientBuild.createHeaders(DOES_MATTER));
		when(restClient.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class)))
				.thenReturn(new ResponseEntity<>("", HttpStatus.OK));
		bambooClientBuild.getLog(HTTP_BAMBOO_COM, BAMBOO_SAMPLE_SERVER_PLAN, true);
		verify(restClient).exchange(URI.create(HTTP_BAMBOO_COM + "/consoleText"), HttpMethod.GET, headers,
				String.class);
	}

	@Test
	public void instanceJobsEmptyResponseReturnsEmptyMap() throws MalformedURLException, ParseException {
		when(restClient.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<>("{\"plans\":{\"plan\":[]}}", HttpStatus.OK));
		Map<ObjectId, Set<Build>> jobs = bambooClientBuild.getJobsFromServer(BAMBOO_SAMPLE_BRANCH, proBasicConfig);
		assertThat("instanceJobsEmptyResponseReturnsEmptyMap", jobs.size(), is(0));
	}

	@Test
	public void instanceJobsTestReturnsMapForBranch() throws Exception {
		when(restClient.exchange(
				eq(URI.create("http://does:matter@xyz/rest/api/latest/plan/HDEP-AST/branch.json?max-result=2000")),
				eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<>("{\"plans\":{\"plan\":[]}}", HttpStatus.OK));
		when(settings.getDockerLocalHostIP()).thenReturn("someIp");
		Map<ObjectId, Set<Build>> jobs = bambooClientBuild.getJobsFromServer(BAMBOO_SAMPLE_BRANCH, proBasicConfig);
		assertThat("instanceJobsTestReturnsMap", jobs.size(), is(0));
	}

	@Test
	public void buildDetailsEmptyJson() throws Exception {
		when(restClient.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));
		Build build = bambooClientBuild.getBuildDetailsFromServer(HTTP_SERVER_JOB_JOB2_2, "https://xyz.com/bamboo/",
				BAMBOO_SAMPLE_SERVER_PLAN);
		assertNull("buildDetailsEmptyJson", build);
	}

	@Test
	public void buildDetailsFull1() throws Exception {
		when(restClient.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<>(getJson("buildDetails_full.json"), HttpStatus.OK));
		when(settings.isSaveLog()).thenReturn(true);
		Build build = bambooClientBuild.getBuildDetailsFromServer(HTTP_SERVER_JOB_JOB2_2, "https://xyz.com/bamboo",
				BAMBOO_SAMPLE_SERVER_PLAN);

		assertThat("buildDetailsFull getTimestamp", build.getTimestamp(), notNullValue());
		assertThat("buildDetailsFull getNumber", build.getNumber(), is("15"));
		assertThat("buildDetailsFull getBuildUrl", build.getBuildUrl(), is(URL_TEST));
		assertThat("buildDetailsFull getStartTime", build.getStartTime(), is(1472119510543L));
		assertThat("buildDetailsFull getEndTime", build.getEndTime(), is(1472119653736L));
		assertThat("buildDetailsFull getDuration", build.getDuration(), is(143193L));
		assertThat("buildDetailsFull getBuildStatus", build.getBuildStatus(), is(BuildStatus.SUCCESS));

	}

	@Test
	public void buildDetailsFull2() throws Exception {
		when(restClient.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET),
				ArgumentMatchers.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<>(getJson("buildDetails_full.json"), HttpStatus.OK));
		when(settings.isSaveLog()).thenReturn(true);
		Build build = bambooClientBuild.getBuildDetailsFromServer(HTTP_SERVER_JOB_JOB2_2, "https://xyz.com/bamboo",
				BAMBOO_SAMPLE_SERVER_PLAN);

		assertThat("buildDetailsFull getTimestamp", build.getTimestamp(), notNullValue());
		assertThat("buildDetailsFull getNumber", build.getNumber(), is("15"));
		assertThat("buildDetailsFull getBuildUrl", build.getBuildUrl(), is(URL_TEST));
		assertThat("buildDetailsFull getStartTime", build.getStartTime(), is(1472119510543L));
		assertThat("buildDetailsFull getEndTime", build.getEndTime(), is(1472119653736L));
		assertThat("buildDetailsFull getDuration", build.getDuration(), is(143193L));
		assertThat("buildDetailsFull getBuildStatus", build.getBuildStatus(), is(BuildStatus.SUCCESS));

	}

	private String getJson(String fileName) throws IOException {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
		return IOUtils.toString(inputStream);
	}

}
