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

package com.publicissapient.kpidashboard.apis.appsetting.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.repository.application.SprintTraceLogRepository;
import com.publicissapient.kpidashboard.apis.repotools.service.RepoToolsConfigServiceImpl;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionBasicConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.publicissapient.kpidashboard.apis.appsetting.config.ProcessorUrlConfig;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;

/**
 * This class contains test cases for ProcessorServiceImpl.class
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessorServiceImplTest {

	@Mock
	HttpServletRequest httpServletRequest;
	@InjectMocks
	private ProcessorServiceImpl processorService;
	@Mock
	private ProcessorRepository processorRepository;
	@Mock
	private ProcessorUrlConfig processorUrlConfig;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private ResponseEntity<String> mockResponseEntity;
	@Mock
	SprintTraceLogRepository sprintTraceLogRepository;

	@Mock
	private RepoToolsConfigServiceImpl repoToolsConfigService;

	@Mock
	private CustomApiConfig customApiConfig;

	/**
	 * method includes preprocesses for test cases
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpServletRequest));
	}

	/**
	 * Methods tests insertion of Project configurations with null values.
	 */
	@Test
	public void testGetAllProcessors() {
		when(processorRepository.findAll()).thenReturn(new ArrayList());
		ServiceResponse response = processorService.getAllProcessorDetails();
		assertThat("Status: ", true, equalTo(response.getSuccess()));
	}

	/**
	 * Methods tests insertion of Project configurations with null values.
	 */
	@Test
	public void testRunProcessorInvalidName() {
		Mockito.when(processorUrlConfig.getProcessorUrl(Mockito.anyString())).thenReturn(StringUtils.EMPTY);
		ServiceResponse response = processorService.runProcessor("wrongName", null);
		assertFalse(response.getSuccess());
	}

	/**
	 * Methods tests insertion of Project configurations with null values.
	 */
	@Test
	public void testRunProcessorAtm() {
		Mockito.when(processorUrlConfig.getProcessorUrl(Mockito.anyString())).thenReturn("validUrlToAtmProcessor");
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(),
				Mockito.<Class<String>>any())).thenReturn(mockResponseEntity);
		Mockito.when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
		ServiceResponse response = processorService.runProcessor("Atm", null);
		assertTrue(response.getSuccess());
	}

	/**
	 * Methods tests insertion of Project configurations with null values.
	 */
	@Test
	public void testRunProcessorSonar() {
		Mockito.when(processorUrlConfig.getProcessorUrl(Mockito.anyString())).thenReturn("validUrlToSonarProcessor");
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(),
				Mockito.<Class<String>>any())).thenReturn(mockResponseEntity);
		Mockito.when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
		ServiceResponse response = processorService.runProcessor("Sonar", null);
		assertTrue(response.getSuccess());
	}

	/**
	 * Methods tests insertion of Project configurations with null values.
	 */
	@Test
	public void testRunProcessorBitbucket() {
		Mockito.when(processorUrlConfig.getProcessorUrl(Mockito.anyString()))
				.thenReturn("validUrlToBitbucketProcessor");
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(),
				Mockito.<Class<String>>any())).thenReturn(mockResponseEntity);
		Mockito.when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
		ProcessorExecutionBasicConfig processorExecutionBasicConfig = new ProcessorExecutionBasicConfig();
		processorExecutionBasicConfig.setProjectBasicConfigIds(Arrays.asList(""));
		ServiceResponse response = processorService.runProcessor("Bitbucket", null);
		assertTrue(response.getSuccess());
	}

	/**
	 * Methods tests insertion of Project configurations with null values.
	 */
	@Test
	public void testRunProcessorExcel() {
		Mockito.when(processorUrlConfig.getProcessorUrl(Mockito.anyString())).thenReturn("validUrlToExcelProcessor");
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(),
				Mockito.<Class<String>>any())).thenReturn(mockResponseEntity);
		Mockito.when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
		ServiceResponse response = processorService.runProcessor("Excel", null);
		assertTrue(response.getSuccess());
	}

	/**
	 * Methods tests insertion of Project configurations with null values.
	 */
	@Test
	public void testRunProcessorBamboo() {
		Mockito.when(processorUrlConfig.getProcessorUrl(Mockito.anyString())).thenReturn("validUrlToBambooProcessor");
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(),
				Mockito.<Class<String>>any())).thenReturn(mockResponseEntity);
		Mockito.when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
		ServiceResponse response = processorService.runProcessor("Bamboo", null);
		assertTrue(response.getSuccess());
	}

	/**
	 * Methods tests insertion of Project configurations with null values.
	 */
	@Test
	public void testRunProcessorJenkins() {
		Mockito.when(processorUrlConfig.getProcessorUrl(Mockito.anyString())).thenReturn("validUrlToJenkinsProcessor");
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(),
				Mockito.<Class<String>>any())).thenReturn(mockResponseEntity);
		Mockito.when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
		ServiceResponse response = processorService.runProcessor("Jenkins", null);
		assertTrue(response.getSuccess());
	}

	/**
	 * Methods tests insertion of Project configurations with null values.
	 */
	@Test
	public void testRunProcessorJira() {
		Mockito.when(processorUrlConfig.getProcessorUrl(Mockito.anyString())).thenReturn("validUrlToJiraProcessor");
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(),
				Mockito.<Class<String>>any())).thenReturn(mockResponseEntity);
		Mockito.when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
		ServiceResponse response = processorService.runProcessor("Jira", null);
		assertTrue(response.getSuccess());
	}

	/**
	 * Methods tests insertion of Project configurations with null values.
	 */
	@Test
	public void testRunProcessorSonar500() {
		Mockito.when(processorUrlConfig.getProcessorUrl(Mockito.anyString())).thenReturn("validUrlToSonarProcessor");
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(),
				Mockito.<Class<String>>any())).thenReturn(mockResponseEntity);
		Mockito.when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
		ServiceResponse response = processorService.runProcessor("Sonar", null);
		assertFalse(response.getSuccess());
	}

	/**
	 * Methods tests insertion of Project configurations with null values.
	 */
	@Test
	public void testRunProcessorSonar404() {
		Mockito.when(processorUrlConfig.getProcessorUrl(Mockito.anyString())).thenReturn("validUrlToSonarProcessor");
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(),
				Mockito.<Class<String>>any())).thenThrow(new ResourceAccessException(""));
		ServiceResponse response = processorService.runProcessor("Sonar", null);
		assertFalse(response.getSuccess());
	}

	@Test
	public void fetchActiveSprint() {
		Mockito.when(processorUrlConfig.getProcessorUrl(Mockito.anyString())).thenReturn("validUrlToJiraProcessor");
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(),
				Mockito.<Class<String>>any())).thenReturn(mockResponseEntity);
		Mockito.when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
		ServiceResponse response = processorService.fetchActiveSprint("132_TestSprint");
		assertTrue(response.getSuccess());
	}

	@Test
	public void fetchActiveSprint_HttpClientErrorException() {
		Mockito.when(processorUrlConfig.getProcessorUrl(Mockito.anyString())).thenReturn("validUrlToJiraProcessor");
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(),
				Mockito.<Class<String>>any()))
				.thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
		ServiceResponse response = processorService.fetchActiveSprint("132_TestSprint");
		assertFalse(response.getSuccess());
	}
}
