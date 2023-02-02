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
import com.publicissapient.kpidashboard.azurepipeline.model.AzurePipelineJob;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.AzurePipelineClient;
import com.publicissapient.kpidashboard.azurepipeline.processor.adapter.impl.DefaultAzurePipelineClient;
import com.publicissapient.kpidashboard.azurepipeline.util.AzurePipelineUtils;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;

@ExtendWith(SpringExtension.class)
public class DefaultAzurePipelineClientTests {

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

	private static final ProcessorToolConnection AZUREPIPELINE_SAMPLE_SERVER_ONE = new ProcessorToolConnection();
	private static final ProcessorToolConnection AZUREPIPELINE_SAMPLE_SERVER_TWO = new ProcessorToolConnection();

	private static final long LASTUPDATEDTIME = 0;
	private static final long LASTUPDATEDTIME1 = 500000;

	@BeforeEach
	public void init() {
		Mockito.when(restOperationsFactory.getTypeInstance()).thenReturn(rest);
		AZUREPIPELINE_SAMPLE_SERVER_ONE.setUrl("https://dev.azure.com/sundeepm/AzureSpeedy");
		AZUREPIPELINE_SAMPLE_SERVER_ONE.setApiVersion("5.1");
		AZUREPIPELINE_SAMPLE_SERVER_ONE.setJobName("1");
		AZUREPIPELINE_SAMPLE_SERVER_ONE.setPat("patKey");

		AZUREPIPELINE_SAMPLE_SERVER_TWO.setUrl("https://dev.azure.com/sundeepm/AzureSpeedy");
		AZUREPIPELINE_SAMPLE_SERVER_TWO.setApiVersion("5.1");
		AZUREPIPELINE_SAMPLE_SERVER_TWO.setJobName("2");
		AZUREPIPELINE_SAMPLE_SERVER_TWO.setPat("patKey");
	}

	@Test
	public void joinURLsTest() throws Exception {
		String u1 = AzurePipelineUtils.joinURL("https://dev.azure.com/sundeepm/AzureSpeedy",
				"/_apis/build/builds?api-version=5.1");
		assertEquals("https://dev.azure.com/sundeepm/AzureSpeedy/_apis/build/builds?api-version=5.1", u1);

		String u2 = AzurePipelineUtils.joinURL("https://dev.azure.com/sundeepm/AzureSpeedy/", "test",
				"/_apis/build/builds?api-version=5.1");
		assertEquals("https://dev.azure.com/sundeepm/AzureSpeedy/test/_apis/build/builds?api-version=5.1", u2);

		String u3 = AzurePipelineUtils.joinURL("https://dev.azure.com/sundeepm/AzureSpeedy/", "/test/",
				"/_apis/build/builds?api-version=5.1");
		assertEquals("https://dev.azure.com/sundeepm/AzureSpeedy/test/_apis/build/builds?api-version=5.1", u3);

		String u4 = AzurePipelineUtils.joinURL("https://dev.azure.com/sundeepm/AzureSpeedy/", "///test",
				"/_apis/build/builds?api-version=5.1");
		assertEquals("https://dev.azure.com/sundeepm/AzureSpeedy/test/_apis/build/builds?api-version=5.1", u4);
	}

	@Test
	public void verifyBasicAuth() throws Exception {
		@SuppressWarnings("unused")
		URL u = new URL(new URL("https://dev.azure.com/sundeepm/AzureSpeedy"),
				"/_apis/build/builds" + "?api-version=5.1");

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

		defaultAzurePipelineClient.doRestCall("https://dev.azure.com/sundeepm/AzureSpeedy",
				AZUREPIPELINE_SAMPLE_SERVER_ONE);
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

		defaultAzurePipelineClient.doRestCall("https://dev.azure.com/sundeepm/AzureSpeedy",
				AZUREPIPELINE_SAMPLE_SERVER_ONE);
		verify(rest).exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET), Mockito.eq(headers),
				Mockito.eq(String.class));
	}

	@Test
	public void instanceJobsEmptyResponseReturnsEmptyMap() {
		when(rest.exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class),
				Mockito.eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));
		Map<ObjectId, Set<Build>> jobs = azurePipelineClient.getInstanceJobs(AZUREPIPELINE_SAMPLE_SERVER_ONE,
				LASTUPDATEDTIME);

		assertThat(jobs.size(), is(0));
	}

	/*
	 * @Test public void instanceJobsOneJobOneBuild() throws Exception {
	 * when(rest.exchange(Mockito.eq(URI.create(
	 * "https://dev.azure.com/sundeepm/AzureSpeedy?api-version=5.1&definitions=2")),
	 * Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class),
	 * Mockito.eq(String.class))) .thenReturn(new
	 * ResponseEntity<>(getJson("instance_jobs_1_job_1_build.json"),
	 * HttpStatus.OK));
	 * 
	 * Map<AzurePipelineJob, Set<Build>> jobs =
	 * azurePipelineClient.getInstanceJobs(AZUREPIPELINE_SAMPLE_SERVER_TWO,
	 * LASTUPDATEDTIME);
	 * 
	 * assertThat(jobs.size(), is(1));
	 * 
	 * Iterator<AzurePipelineJob> jobIt = jobs.keySet().iterator();
	 * 
	 * // First job AzurePipelineJob job = jobIt.next(); assertJob(job, "2",
	 * "https://dev.azure.com/sundeepm/b08688aa-7449-4b13-bc51-68bf67326d54/_apis/build/Definitions/2?revision=2"
	 * );
	 * 
	 * Set<Build> builds = jobs.get(job);
	 * 
	 * List<Build> buildsList = new ArrayList<Build>(builds) ;
	 * 
	 * buildsList.sort((Build b1,Build b2)->
	 * Integer.valueOf(b1.getNumber())-Integer.valueOf(b2.getNumber()));
	 * 
	 * Iterator<Build> buildIt = buildsList.iterator();
	 * 
	 * 
	 * assertBuild(buildIt.next(), "5",
	 * "https://dev.azure.com/sundeepm/b08688aa-7449-4b13-bc51-68bf67326d54/_apis/build/Builds/5"
	 * ); assertBuild(buildIt.next(), "6",
	 * "https://dev.azure.com/sundeepm/b08688aa-7449-4b13-bc51-68bf67326d54/_apis/build/Builds/6"
	 * ); assertBuild(buildIt.next(), "7",
	 * "https://dev.azure.com/sundeepm/b08688aa-7449-4b13-bc51-68bf67326d54/_apis/build/Builds/7"
	 * ); assertBuild(buildIt.next(), "8",
	 * "https://dev.azure.com/sundeepm/b08688aa-7449-4b13-bc51-68bf67326d54/_apis/build/Builds/8"
	 * ); assertBuild(buildIt.next(), "9",
	 * "https://dev.azure.com/sundeepm/b08688aa-7449-4b13-bc51-68bf67326d54/_apis/build/Builds/9"
	 * );
	 * 
	 * assertThat(buildIt.hasNext(), is(false));
	 * 
	 * assertThat(jobIt.hasNext(), is(false));
	 * 
	 * assertThat(buildIt.hasNext(), is(false));
	 * 
	 * assertThat(jobIt.hasNext(), is(false)); }
	 * 
	 * @Test public void instanceJobsOneJobOneBuildMTime() throws Exception {
	 * when(rest.exchange(Mockito.eq(URI.create(
	 * "https://dev.azure.com/sundeepm/AzureSpeedy?api-version=5.1&definitions=2&minTime=1970-01-01T00:08:20.000Z"
	 * )), Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class),
	 * Mockito.eq(String.class))) .thenReturn(new
	 * ResponseEntity<>(getJson("instance_jobs_1_job_1_build.json"),
	 * HttpStatus.OK));
	 * 
	 * Map<AzurePipelineJob, Set<Build>> jobs =
	 * azurePipelineClient.getInstanceJobs(AZUREPIPELINE_SAMPLE_SERVER_TWO,
	 * LASTUPDATEDTIME1);
	 * 
	 * assertThat(jobs.size(), is(1));
	 * 
	 * Iterator<AzurePipelineJob> jobIt = jobs.keySet().iterator();
	 * 
	 * // First job AzurePipelineJob job = jobIt.next(); assertJob(job, "2",
	 * "https://dev.azure.com/sundeepm/b08688aa-7449-4b13-bc51-68bf67326d54/_apis/build/Definitions/2?revision=2"
	 * );
	 * 
	 * Set<Build> builds = jobs.get(job);
	 * 
	 * List<Build> buildsList = new ArrayList<Build>(builds) ;
	 * 
	 * buildsList.sort((Build b1,Build b2)->
	 * Integer.valueOf(b1.getNumber())-Integer.valueOf(b2.getNumber()));
	 * 
	 * Iterator<Build> buildIt = buildsList.iterator();
	 * 
	 * 
	 * assertBuild(buildIt.next(), "5",
	 * "https://dev.azure.com/sundeepm/b08688aa-7449-4b13-bc51-68bf67326d54/_apis/build/Builds/5"
	 * ); assertBuild(buildIt.next(), "6",
	 * "https://dev.azure.com/sundeepm/b08688aa-7449-4b13-bc51-68bf67326d54/_apis/build/Builds/6"
	 * ); assertBuild(buildIt.next(), "7",
	 * "https://dev.azure.com/sundeepm/b08688aa-7449-4b13-bc51-68bf67326d54/_apis/build/Builds/7"
	 * ); assertBuild(buildIt.next(), "8",
	 * "https://dev.azure.com/sundeepm/b08688aa-7449-4b13-bc51-68bf67326d54/_apis/build/Builds/8"
	 * ); assertBuild(buildIt.next(), "9",
	 * "https://dev.azure.com/sundeepm/b08688aa-7449-4b13-bc51-68bf67326d54/_apis/build/Builds/9"
	 * );
	 * 
	 * assertThat(buildIt.hasNext(), is(false));
	 * 
	 * assertThat(jobIt.hasNext(), is(false)); }
	 */
	@Test
	public void testGetInstanceJobs() throws Exception {
		long lastStartTimeOfBuilds = 0;
		when(azurePipelineConfig.getApiEndPoint()).thenReturn("_apis/build/builds");
		when(rest.exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class),
				Mockito.eq(String.class)))
						.thenReturn(new ResponseEntity<>(getJson("instance_jobs_1_job_1_build.json"), HttpStatus.OK));
		defaultAzurePipelineClient.getInstanceJobs(AZUREPIPELINE_SAMPLE_SERVER_ONE, lastStartTimeOfBuilds);
		assertEquals(0, lastStartTimeOfBuilds);
	}

	@Test
	public void testGetInstanceJobs2() {
		long lastStartTimeOfBuilds = 0;
		when(azurePipelineConfig.getApiEndPoint()).thenReturn("_apis/build/builds");
		when(rest.exchange(Mockito.any(URI.class), Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class),
				Mockito.eq(String.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));
		defaultAzurePipelineClient.getInstanceJobs(AZUREPIPELINE_SAMPLE_SERVER_ONE, lastStartTimeOfBuilds);
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

	private void assertJob(AzurePipelineJob job, String name, String url) {
		assertThat(job.getJobName(), is(name));
		assertThat(job.getJobUrl(), is(url));
	}
}