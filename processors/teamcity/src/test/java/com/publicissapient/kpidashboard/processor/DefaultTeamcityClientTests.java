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

package com.publicissapient.kpidashboard.processor;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;
import com.publicissapient.kpidashboard.teamcity.config.TeamcityConfig;
import com.publicissapient.kpidashboard.teamcity.processor.adapter.TeamcityClient;
import com.publicissapient.kpidashboard.teamcity.processor.adapter.impl.DefaultTeamcityClient;
import com.publicissapient.kpidashboard.teamcity.util.ProcessorUtils;

@ExtendWith(SpringExtension.class)
public class DefaultTeamcityClientTests {

	private static final String URL_TEST = "/app/rest/builds/id:5";
	private static final int PAGE_SIZE = 10;
	private static final ProcessorToolConnection TEAMCITY_SAMPLE_SERVER_ONE = new ProcessorToolConnection();
	private static final ProcessorToolConnection TEAMCITY_SAMPLE_SERVER_TWO = new ProcessorToolConnection();
	private static final ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
	@Mock
	private RestOperationsFactory<RestOperations> restOperationsFactory;
	@Mock
	private RestOperations rest;
	private TeamcityConfig config;
	private TeamcityClient teamcityClient;
	private DefaultTeamcityClient defaultTeamcityClient;

	@BeforeEach
	public void init() {
		when(restOperationsFactory.getTypeInstance()).thenReturn(rest);
		config = TeamcityConfig.builder().build();
		config.setPageSize(PAGE_SIZE);
		teamcityClient = defaultTeamcityClient = new DefaultTeamcityClient(restOperationsFactory, config);
		TEAMCITY_SAMPLE_SERVER_TWO.setId(new ObjectId("63b40aea8ec44416b3ce96b5"));
		TEAMCITY_SAMPLE_SERVER_ONE.setUrl("http://test@test.com");
		TEAMCITY_SAMPLE_SERVER_ONE.setUsername("test");
		TEAMCITY_SAMPLE_SERVER_ONE.setPassword("password");

		TEAMCITY_SAMPLE_SERVER_TWO.setId(new ObjectId("63c53ed169fa1a025c5f1244"));
		TEAMCITY_SAMPLE_SERVER_TWO.setUrl("http://server/");
		TEAMCITY_SAMPLE_SERVER_TWO.setUsername("test");
		TEAMCITY_SAMPLE_SERVER_TWO.setPassword("password");

	}

	@Test
	public void joinURLsTest() throws Exception {
		String u = ProcessorUtils.joinURL("http://test.com", "/app/rest/projects");
		assertEquals("http://test.com/app/rest/projects", u);

		String u4 = ProcessorUtils.joinURL("http://test.com/", "app/rest/", "builds");
		assertEquals("http://test.com/app/rest/builds", u4);

	}

	@Test
	public void rebuildURLTest() throws Exception {

		String u1 = DefaultTeamcityClient.rebuildJobUrl("http://test.com/app/rest/projects",
				"https://123456:234567@test.com");
		assertEquals("https://123456:234567@test.com/app/rest/projects", u1);

		String u2 = DefaultTeamcityClient.rebuildJobUrl("https://test.com/app/rest/projects",
				"https://123456:234567@test.com");
		assertEquals("https://123456:234567@test.com/app/rest/projects", u2);

		String u3 = DefaultTeamcityClient.rebuildJobUrl("http://test.com/app/rest/projects",
				"http://123456:234567@test.com");
		assertEquals("http://123456:234567@test.com/app/rest/projects", u3);

		String u4 = DefaultTeamcityClient.rebuildJobUrl("http://test.com/app/rest/projects",
				"http://123456:234567@test.com");
		assertEquals("http://123456:234567@test.com/app/rest/projects", u4);

		String orig = "http://test.com/app/rest/project%20with%20space";
		String u5 = DefaultTeamcityClient.rebuildJobUrl(orig, "http://test.com");
		assertEquals(orig, u5);
	}

	@Test
	public void verifyBasicAuth() throws Exception {
		@SuppressWarnings("unused")
		URL u = new URL(new URL("http://test.com"), "/app/rest/projects");

		HttpHeaders headers = ProcessorUtils.createHeaders("test:pwd");
		assertNotNull(headers.getFirst(HttpHeaders.AUTHORIZATION));
	}

	@Test
	public void verifyAuthCredentials() throws Exception {
		// TODO: This change to clear a JAVA Warning should be correct but test
		// fails, need to investigate
		// HttpEntity<HttpHeaders> headers = new
		// HttpEntity<HttpHeaders>(defaultHudsonClient.createHeaders("user:pass"));
		@SuppressWarnings({ "rawtypes", "unchecked" })
		HttpEntity headers = new HttpEntity(ProcessorUtils.createHeaders("user:pass"));
		when(rest.exchange(Mockito.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class)))
				.thenReturn(new ResponseEntity<>("", HttpStatus.OK));

		defaultTeamcityClient.doRestCall("http://user:pass@test.com", TEAMCITY_SAMPLE_SERVER_ONE);
		verify(rest).exchange(Mockito.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class));
	}

	@Test
	public void verifyAuthCredentialsBySettings() throws Exception {
		// TODO: This change to clear a JAVA Warnings should be correct but test
		// fails, need to investigate
		// HttpEntity<HttpHeaders> headers = new
		// HttpEntity<HttpHeaders>(defaultHudsonClient.createHeaders("test:password"));
		@SuppressWarnings({ "unchecked", "rawtypes" })
		HttpEntity headers = new HttpEntity(ProcessorUtils.createHeaders("test:password"));
		when(rest.exchange(Mockito.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class)))
				.thenReturn(new ResponseEntity<>("", HttpStatus.OK));

		defaultTeamcityClient.doRestCall("http://test.com", TEAMCITY_SAMPLE_SERVER_ONE);
		verify(rest).exchange(Mockito.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class));
	}

	@Test
	public void verifyGetLogUrl() throws Exception {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		HttpEntity headers = new HttpEntity(ProcessorUtils.createHeaders("test:password"));
		when(rest.exchange(Mockito.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class)))
				.thenReturn(new ResponseEntity<>("", HttpStatus.OK));

		defaultTeamcityClient.getLog("http://test.com", TEAMCITY_SAMPLE_SERVER_ONE);
		verify(rest).exchange(eq(URI.create("http://test.com/consoleText")), eq(HttpMethod.GET), eq(headers),
				eq(String.class));
	}

	@Test
	public void instanceJobs_emptyResponse_returnsEmptyMap() {
		when(rest.exchange(Mockito.any(URI.class), eq(HttpMethod.GET), Mockito.any(HttpEntity.class), eq(String.class)))
				.thenReturn(new ResponseEntity<>("", HttpStatus.OK));
		Map<ObjectId, Set<Build>> jobs = teamcityClient.getInstanceJobs(TEAMCITY_SAMPLE_SERVER_ONE);

		assertThat(jobs.size(), is(0));
	}

	@Test
	public void instanceJobs_OneJobsOneBuilds() throws Exception {
		when(rest.exchange(eq(URI.create("http://server/app/rest/projects")), eq(HttpMethod.GET),
				Mockito.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<>(getJson("instance_jobs_2_jobs.json"), HttpStatus.OK));

		when(rest.exchange(eq(URI.create("http://server/app/rest/projects/id:Project2")), // http://server/app/rest/projects/id:Project2
				eq(HttpMethod.GET), Mockito.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<>(getJson("instance_jobs_2_builds.json"), HttpStatus.OK));

		when(rest.exchange(eq(URI.create("http://server/app/rest/buildTypes/id:Project2_Build2/builds")),
				eq(HttpMethod.GET), Mockito.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<>(getJson("builds_info_complete.json"), HttpStatus.OK));
		when(rest.exchange(eq(URI.create("http://server/app/rest/buildTypes/id:Project2_Build3/builds")),
				eq(HttpMethod.GET), Mockito.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<>(getJson("builds_info_complete.json"), HttpStatus.OK));

		TEAMCITY_SAMPLE_SERVER_TWO.setJobName("Project-2");
		Map<ObjectId, Set<Build>> jobs = teamcityClient.getInstanceJobs(TEAMCITY_SAMPLE_SERVER_TWO);

		assertThat(jobs.size(), is(1));

		Iterator<Build> buildIt = jobs.get(new ObjectId("63c53ed169fa1a025c5f1244")).iterator();
		assertBuild(buildIt.next(), "3", "http://server/app/rest/buildTypes/id:Project2_Build2/builds/");
		assertThat(buildIt.hasNext(), is(false));

	}

	@Test
	public void buildDetails_full() throws Exception {

		projectBasicConfig.setSaveAssigneeDetails(true);
		when(rest.exchange(Mockito.any(URI.class), eq(HttpMethod.GET), Mockito.any(HttpEntity.class), eq(String.class)))
				.thenReturn(new ResponseEntity<>(getJson("builds_info_complete.json"), HttpStatus.OK));

		when(rest.exchange(eq(URI.create("http://server/app/rest/buildTypes/id:Project2_Build2/builds")),
				eq(HttpMethod.GET), Mockito.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<>(getJson("builds_info_complete.json"), HttpStatus.OK));

		when(rest.exchange(eq(URI.create("http://server/app/rest/builds/id:5")), eq(HttpMethod.GET),
				Mockito.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<>(getJson("build_info_complete.json"), HttpStatus.OK));

		when(rest.exchange(eq(URI.create("http://server/app/rest/builds/id:5/statistics")), eq(HttpMethod.GET),
				Mockito.any(HttpEntity.class), eq(String.class)))
						.thenReturn(new ResponseEntity<>(getJson("build_info_stats.json"), HttpStatus.OK));

		Build build = teamcityClient.getBuildDetails("http://server/app/rest/buildTypes/id:Project2_Build2/",
				"http://server", TEAMCITY_SAMPLE_SERVER_TWO, projectBasicConfig);

		assertThat(build.getTimestamp(), notNullValue());
		assertThat(build.getNumber(), is("3"));
		assertThat(build.getBuildUrl(), is(URL_TEST));
		assertThat(build.getDuration(), is(5441L));
		assertThat(build.getBuildStatus(), is(BuildStatus.SUCCESS));
		assertThat(build.getStartedBy(), is("admin"));
	}

	private void assertBuild(Build build, String number, String url) {
		assertThat(build.getNumber(), is(number));
		assertThat(build.getBuildUrl(), is(url));
	}

	private String getJson(String fileName) throws IOException {
		InputStream inputStream = DefaultTeamcityClientTests.class.getResourceAsStream(fileName);
		return IOUtils.toString(inputStream);
	}
}
