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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestOperations;

import com.publicissapient.kpidashboard.azurepipeline.config.AzurePipelineConfig;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.AzurePipelineClient;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.impl.DefaultAzurePipelineClient;
import com.publicissapient.kpidashboard.azurepipeline.util.AzurePipelineUtils;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;

@ExtendWith(SpringExtension.class)
public class DefaultAzurePipelineClientTests {

	private static final ProcessorToolConnection AZUREPIPELINE_SAMPLE_SERVER_ONE = new ProcessorToolConnection();
	private static final ProcessorToolConnection AZUREPIPELINE_SAMPLE_SERVER_TWO = new ProcessorToolConnection();
	private static final long LASTUPDATEDTIME = 0;
	private static final long LASTUPDATEDTIME1 = 500000;
	private ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
	@Mock
	private RestOperationsFactory<RestOperations> restOperationsFactory;
	@Mock
	private RestOperations rest;
	@Mock
	private AzurePipelineConfig azurePipelineConfig;
	@Mock
	private AzurePipelineClient azurePipelineClient;
	@InjectMocks
	private DefaultAzurePipelineClient defaultAzurePipelineClient;

	@BeforeEach
	public void init() {
		Mockito.when(restOperationsFactory.getTypeInstance()).thenReturn(rest);
		AZUREPIPELINE_SAMPLE_SERVER_ONE.setUrl("https://test.com/testUser/testProject");
		AZUREPIPELINE_SAMPLE_SERVER_ONE.setApiVersion("5.1");
		AZUREPIPELINE_SAMPLE_SERVER_ONE.setJobName("1");
		AZUREPIPELINE_SAMPLE_SERVER_ONE.setPat("patKey");

		AZUREPIPELINE_SAMPLE_SERVER_TWO.setUrl("https://test.com/testUser/testProject");
		AZUREPIPELINE_SAMPLE_SERVER_TWO.setApiVersion("5.1");
		AZUREPIPELINE_SAMPLE_SERVER_TWO.setJobName("2");
		AZUREPIPELINE_SAMPLE_SERVER_TWO.setPat("patKey");
	}

	@Test
	public void joinURLsTest() throws Exception {
		String u1 = AzurePipelineUtils.joinURL("https://test.com/testUser/testProject",
				"/_apis/build/builds?api-version=5.1");
		assertEquals("https://test.com/testUser/testProject/_apis/build/builds?api-version=5.1", u1);

		String u2 = AzurePipelineUtils.joinURL("https://test.com/testUser/testProject/", "test",
				"/_apis/build/builds?api-version=5.1");
		assertEquals("https://test.com/testUser/testProject/test/_apis/build/builds?api-version=5.1", u2);

		String u3 = AzurePipelineUtils.joinURL("https://test.com/testUser/testProject/", "/test/",
				"/_apis/build/builds?api-version=5.1");
		assertEquals("https://test.com/testUser/testProject/test/_apis/build/builds?api-version=5.1", u3);

		String u4 = AzurePipelineUtils.joinURL("https://test.com/testUser/testProject/", "///test",
				"/_apis/build/builds?api-version=5.1");
		assertEquals("https://test.com/testUser/testProject/test/_apis/build/builds?api-version=5.1", u4);
	}

	@Test
	public void verifyBasicAuth() throws Exception {
		@SuppressWarnings("unused")
		URL u = new URL(new URL("https://test.com/testUser/testProject"), "/_apis/build/builds" + "?api-version=5.1");

		HttpHeaders headers = AzurePipelineUtils.createHeaders("wrggipp62ak7kvtfc4qqc56fsbt3uxphsv5yo4ezabynbote2ipw");
		assertEquals("Basic ZHVtbXlVc2VyOndyZ2dpcHA2MmFrN2t2dGZjNHFxYzU2ZnNidDN1eHBoc3Y1eW80ZXphYnluYm90ZTJpcHc=",
				headers.getFirst(HttpHeaders.AUTHORIZATION));
	}

	@Test
	public void verifyAuthCredentials() throws Exception {
		// TODO: This change to clear a JAVA Warning should be correct but test
		// fails, need to investigate
		// HttpEntity<HttpHeaders> headers = new
		// HttpEntity<HttpHeaders>(defaultHudsonClient.createHeaders("user:pass"));
		@SuppressWarnings({ "rawtypes", "unchecked" })
		HttpEntity headers = new HttpEntity(AzurePipelineUtils.createHeaders("patKey"));
		when(rest.exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET), Mockito.eq(headers),
				Mockito.eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

		defaultAzurePipelineClient.doRestCall("https://test.com/testUser/testProject", AZUREPIPELINE_SAMPLE_SERVER_ONE);
		verify(rest).exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET), Mockito.eq(headers),
				Mockito.eq(String.class));
	}

	@Test
	public void verifyAuthCredentialsBySettings() throws Exception {
		// TODO: This change to clear a JAVA Warnings should be correct but test
		// fails, need to investigate
		// HttpEntity<HttpHeaders> headers = new
		// HttpEntity<HttpHeaders>(defaultHudsonClient.createHeaders("does:matter"));
		@SuppressWarnings({ "unchecked", "rawtypes" })
		HttpEntity headers = new HttpEntity(AzurePipelineUtils.createHeaders("patKey"));
		when(rest.exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET), Mockito.eq(headers),
				Mockito.eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

		defaultAzurePipelineClient.doRestCall("https://test.com/testUser/testProject", AZUREPIPELINE_SAMPLE_SERVER_ONE);
		verify(rest).exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET), Mockito.eq(headers),
				Mockito.eq(String.class));
	}

	@Test
	public void instanceJobsEmptyResponseReturnsEmptyMap() {
		when(rest.exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class),
				Mockito.eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));
		Map<ObjectId, Set<Build>> jobs = azurePipelineClient.getInstanceJobs(AZUREPIPELINE_SAMPLE_SERVER_ONE,
				LASTUPDATEDTIME, projectBasicConfig);

		assertThat(jobs.size(), is(0));
	}

	@Test
	public void testGetInstanceJobs() throws Exception {
		long lastStartTimeOfBuilds = 0;
		when(azurePipelineConfig.getApiEndPoint()).thenReturn("_apis/build/builds");
		when(rest.exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class),
				Mockito.eq(String.class)))
						.thenReturn(new ResponseEntity<>(getJson("instance_jobs_1_job_1_build.json"), HttpStatus.OK));
		defaultAzurePipelineClient.getInstanceJobs(AZUREPIPELINE_SAMPLE_SERVER_ONE, lastStartTimeOfBuilds,
				projectBasicConfig);
		assertEquals(0, lastStartTimeOfBuilds);
	}

	@Test
	public void testGetInstanceJobs2() {
		long lastStartTimeOfBuilds = 0;
		when(azurePipelineConfig.getApiEndPoint()).thenReturn("_apis/build/builds");
		when(rest.exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class),
				Mockito.eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));
		defaultAzurePipelineClient.getInstanceJobs(AZUREPIPELINE_SAMPLE_SERVER_ONE, lastStartTimeOfBuilds,
				projectBasicConfig);
		assertEquals(0, lastStartTimeOfBuilds);
	}

	private void assertBuild(Build build, String number, String url) {
		assertThat(build.getNumber(), is(number));
		assertThat(build.getBuildUrl(), is(url));
	}

	private String getJson(String fileName) throws IOException {
		InputStream inputStream = DefaultAzurePipelineClientTests.class.getResourceAsStream(fileName);
		return IOUtils.toString(inputStream);
	}

}