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
import static org.junit.Assert.assertEquals;
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
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;
import com.publicissapient.kpidashboard.jenkins.config.JenkinsConfig;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.JenkinsClient;
import com.publicissapient.kpidashboard.jenkins.processor.adapter.impl.JenkinsBuildClient;
import com.publicissapient.kpidashboard.jenkins.util.ProcessorUtils;

@ExtendWith(SpringExtension.class)
public class JenkinsBuildClientTests {

	private static final ProcessorToolConnection JENKINS_SAMPLE_SERVER_ONE = new ProcessorToolConnection();
	private static final ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
	@Mock
	private RestOperationsFactory<RestOperations> restOperationsFactory;
	@Mock
	private RestOperations rest;
	private JenkinsConfig config;
	private JenkinsClient jenkinsClient;
	private JenkinsBuildClient jenkinsBuildClient;

	@BeforeEach
	public void init() {
		when(restOperationsFactory.getTypeInstance()).thenReturn(rest);
		config = JenkinsConfig.builder().build();
		jenkinsClient = jenkinsBuildClient = new JenkinsBuildClient(restOperationsFactory, config);
		JENKINS_SAMPLE_SERVER_ONE.setUrl("http://does:matter@jenkins.com");
		JENKINS_SAMPLE_SERVER_ONE.setUsername("does");
		JENKINS_SAMPLE_SERVER_ONE.setApiKey("matter");
		JENKINS_SAMPLE_SERVER_ONE.setJobName("job1");
		JENKINS_SAMPLE_SERVER_ONE.setId(new ObjectId("62171d0f26dd266803fa87da"));
	}

	@Test
	public void joinURLsTest() throws Exception {
		String u = ProcessorUtils.joinURL("http://jenkins.com", "/api/json?tree=jobs[name,url,builds[number,url]]");
		assertEquals("http://jenkins.com/api/json?tree=jobs[name,url,builds[number,url]]", u);

		String u4 = ProcessorUtils.joinURL("http://jenkins.com/", "test",
				"/api/json?tree=jobs[name,url,builds[number,url]]");
		assertEquals("http://jenkins.com/test/api/json?tree=jobs[name,url,builds[number,url]]", u4);

		String u2 = ProcessorUtils.joinURL("http://jenkins.com/", "/test/",
				"/api/json?tree=jobs[name,url,builds[number,url]]");
		assertEquals("http://jenkins.com/test/api/json?tree=jobs[name,url,builds[number,url]]", u2);

		String u3 = ProcessorUtils.joinURL("http://jenkins.com", "///test",
				"/api/json?tree=jobs[name,url,builds[number,url]]");
		assertEquals("http://jenkins.com/test/api/json?tree=jobs[name,url,builds[number,url]]", u3);
	}

	@Test
	public void verifyBasicAuth() throws Exception {
		@SuppressWarnings("unused")
		URL u = new URL(new URL("http://jenkins.com"), "/api/json?tree=jobs[name,url," + "builds[number,url]]");

		HttpHeaders headers = ProcessorUtils.createHeaders("Aladdin:open sesame");
		assertEquals("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==", headers.getFirst(HttpHeaders.AUTHORIZATION));
	}

	@Test
	public void verifyAuthCredentials() throws Exception {
		// TODO: This change to clear a JAVA Warning should be correct but test
		// fails, need to investigate
		// HttpEntity<HttpHeaders> headers = new
		// HttpEntity<HttpHeaders>(defaultHudsonClient.createHeaders("user:pass"));
		@SuppressWarnings({ "rawtypes", "unchecked" })
		HttpEntity headers = new HttpEntity(ProcessorUtils.createHeaders("user:pass"));
		when(rest.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class)))
				.thenReturn(new ResponseEntity<>("", HttpStatus.OK));

		jenkinsBuildClient.doRestCall("http://user:pass@jenkins.com", JENKINS_SAMPLE_SERVER_ONE);
		verify(rest).exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class));
	}

	@Test
	public void verifyAuthCredentialsBySettings() throws Exception {
		// TODO: This change to clear a JAVA Warnings should be correct but test
		// fails, need to investigate
		// HttpEntity<HttpHeaders> headers = new
		// HttpEntity<HttpHeaders>(defaultHudsonClient.createHeaders("does:matter"));
		@SuppressWarnings({ "unchecked", "rawtypes" })
		HttpEntity headers = new HttpEntity(ProcessorUtils.createHeaders("does:matter"));
		when(rest.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class)))
				.thenReturn(new ResponseEntity<>("", HttpStatus.OK));

		jenkinsBuildClient.doRestCall("http://jenkins.com", JENKINS_SAMPLE_SERVER_ONE);
		verify(rest).exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class));
	}

	@Test
	public void verifyGetLogUrl() throws Exception {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		HttpEntity headers = new HttpEntity(ProcessorUtils.createHeaders("does:matter"));
		when(rest.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), eq(headers), eq(String.class)))
				.thenReturn(new ResponseEntity<>("", HttpStatus.OK));

		jenkinsBuildClient.getLog("http://jenkins.com", JENKINS_SAMPLE_SERVER_ONE);
		verify(rest).exchange(eq(URI.create("http://jenkins.com/consoleText")), eq(HttpMethod.GET), eq(headers),
				eq(String.class));
	}

	@Test
	public void instanceJobs_emptyResponse_returnsEmptyMap() {
		when(rest.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));
		Map<ObjectId, Set<Build>> jobs = jenkinsClient.getBuildJobsFromServer(JENKINS_SAMPLE_SERVER_ONE,
				projectBasicConfig);

		assertThat(jobs.size(), is(0));
	}

	@Test
	public void testGetJobDetails() throws Exception {
		when(rest.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				eq(String.class)))
						.thenReturn(new ResponseEntity<>(getJson("instance_jobs_2_jobs_2_builds.json"), HttpStatus.OK));

		Map<ObjectId, Set<Build>> jobs = jenkinsClient.getBuildJobsFromServer(JENKINS_SAMPLE_SERVER_ONE,
				projectBasicConfig);

		assertThat(jobs.size(), is(1));

		Iterator<ObjectId> jobIt = jobs.keySet().iterator();
		ObjectId job = jobIt.next();

		Iterator<Build> buildIt = jobs.get(job).iterator();
		assertBuild(buildIt.next(), "2", "http://server/job/job1/2/");
		assertBuild(buildIt.next(), "1", "http://server/job/job1/1/");
		assertThat(buildIt.hasNext(), is(false));

		assertThat(jobIt.hasNext(), is(false));

	}

	@Test
	public void testGetJobDetailsChild() throws Exception {
		when(rest.exchange(ArgumentMatchers.any(URI.class), eq(HttpMethod.GET), ArgumentMatchers.any(HttpEntity.class),
				eq(String.class))).thenReturn(
						new ResponseEntity<>(getJson("instance_jobs_multibranch_pipeline.json"), HttpStatus.OK));

		Map<ObjectId, Set<Build>> jobs = jenkinsClient.getBuildJobsFromServer(JENKINS_SAMPLE_SERVER_ONE,
				projectBasicConfig);

		assertThat(jobs.size(), is(1));

		Iterator<ObjectId> jobIt = jobs.keySet().iterator();

		ObjectId job = jobIt.next();

		Iterator<Build> buildIt = jobs.get(job).iterator();
		// assertBuild(buildIt.next(), "2", "http://server/job/job2/2/");
		assertBuild(buildIt.next(), "1", "http://server/job/job1/1/");
		assertThat(buildIt.hasNext(), is(true));

		assertThat(jobIt.hasNext(), is(false));

	}

	private void assertBuild(Build build, String number, String url) {
		assertThat(build.getNumber(), is(number));
		assertThat(build.getBuildUrl(), is(url));
	}

	private String getJson(String fileName) throws IOException {
		InputStream inputStream = JenkinsBuildClientTests.class.getResourceAsStream(fileName);
		return IOUtils.toString(inputStream);
	}

}
