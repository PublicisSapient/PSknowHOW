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

package com.publicissapient.kpidashboard.azure.adapter.impl.async.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.azure.adapter.impl.async.ProcessorAzureRestClient;
import com.publicissapient.kpidashboard.azure.config.AzureProcessorConfig;
import com.publicissapient.kpidashboard.azure.model.AzureServer;
import com.publicissapient.kpidashboard.azure.model.AzureToolConfig;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.azure.util.AzureConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.azureboards.AzureBoardsWIModel;
import com.publicissapient.kpidashboard.common.model.azureboards.iterations.AzureIterationsModel;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.AzureUpdatesModel;
import com.publicissapient.kpidashboard.common.model.azureboards.wiql.AzureWiqlModel;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.util.RestOperationsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SpringExtension.class)
public class ProcessorAsynchAzureRestClientFactoryTest {

	@Mock
	AzureProcessorConfig azureProcessorConfig;
	
	ProcessorAsynchAzureRestClientFactory azureRestClientFactory;

	@Mock
	private RestOperationsFactory<RestOperations> restOperationsFactory;
	@Mock
	RestTemplate restTemplate;

	@Mock
	ProcessorAzureRestClient restClient;

	@Mock
	private RestOperations rest;

	@Mock
	ObjectMapper mapper;

	@Mock
	private AesEncryptionService aesEncryptionService;

	ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();

	@BeforeEach
	public void init() {
		Mockito.when(restOperationsFactory.getTypeInstance()).thenReturn(rest);
		azureProcessorConfig = AzureProcessorConfig.builder().build();
		mapper = new ObjectMapper();
		restClient = azureRestClientFactory = new ProcessorAsynchAzureRestClientFactory(restOperationsFactory,
			azureProcessorConfig, mapper);

	}

	private AzureServer prepareAzureServer() {
		AzureServer azureServer = new AzureServer();
		azureServer.setPat("TestUser@123");
		azureServer.setUrl("https://dev.azure.com/sundeepm/AzureSpeedy");
		azureServer.setApiVersion("5.1");
		azureServer.setUsername("");
		return azureServer;

	}

	@Test
	public void getUpdatesResponse()
		throws RestClientException, URISyntaxException, JsonParseException, JsonMappingException, IOException {
		AzureServer azureServer = prepareAzureServer();
		String authHeader = "Basic " + "OlRlc3RVc2VyQDEyMw==";
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, authHeader);
		ResponseEntity<String> response = new ResponseEntity<>(createUpdateResponse(), HttpStatus.OK);
		Mockito.when(rest.exchange(ArgumentMatchers.any(URI.class), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.eq(new HttpEntity<>(headers)), ArgumentMatchers.eq(String.class)))
			.thenReturn(response);
		AzureUpdatesModel actualModel = azureRestClientFactory.getUpdatesResponse(azureServer, "92");
		AzureUpdatesModel expectedModel = new AzureUpdatesModel();
		expectedModel.setCount(7);
		assertEquals(expectedModel.getCount(), actualModel.getCount(), "Checking for count values");
	}

	private String createUpdateResponse() throws JsonParseException, JsonMappingException, IOException {
		String filePath = "src/test/resources/onlinedata/azure/azureupdatesmodel.json";
		return  new String(Files.readAllBytes(Paths.get(filePath)));
	}

	@Test
	public void getWiqlResponse() throws RestClientException, URISyntaxException, JsonParseException,
		JsonMappingException, IOException, ParseException, IllegalAccessException, InvocationTargetException {
		prepareProjectConfigFieldMapping();
		long startTime1 = new SimpleDateFormat(AzureConstants.SETTING_DATE_FORMAT, Locale.US)
				.parse("2019-01-07T00:00:00.000000").getTime();
		Map<String, Long> time = new HashMap();
		time.put("User Story", startTime1);
		time.put("Issue", startTime1);
		long startTime = new SimpleDateFormat(AzureConstants.SETTING_DATE_FORMAT, Locale.US)
			.parse("2019-01-07T00:00:00.000000").getTime();
		AzureServer azureServer = prepareAzureServer();
		ResponseEntity<String> response = new ResponseEntity<>(createWiqlResponse(), HttpStatus.OK);
		Mockito.when(
			rest.exchange(ArgumentMatchers.any(URI.class), ArgumentMatchers.eq(HttpMethod.POST),  Mockito.<HttpEntity<String>> any(), ArgumentMatchers.eq(String.class)))
			.thenReturn(response);
		AzureWiqlModel actualModel = azureRestClientFactory.getWiqlResponse(azureServer,time, projectConfFieldMapping,false);
		AzureWiqlModel expectedModel = new AzureWiqlModel();
		expectedModel.setAsOf("2020-07-17T12:01:50.663Z");;
		assertEquals(expectedModel.getAsOf(), actualModel.getAsOf(), "Checking for values");
		
	}

	private String createWiqlResponse() throws IOException {
		String filePath = "src/test/resources/onlinedata/azure/azurewiqlmodel.json";
		return new String(Files.readAllBytes(Paths.get(filePath)));
	}

	private void prepareProjectConfigFieldMapping() throws IllegalAccessException, InvocationTargetException,
		JsonParseException, JsonMappingException, IOException {
		String filePath = "src/test/resources/onlinedata/azure/scrumprojectconfig.json";
		File file = new File(filePath);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ProjectBasicConfig projectConfig = objectMapper.readValue(file, ProjectBasicConfig.class);

		String fieldMapFilePath = "src/test/resources/onlinedata/azure/scrumfieldmapping.json";
		File fieldMapFile = new File(fieldMapFilePath);
		ObjectMapper fieldObjectMapper = new ObjectMapper();
		fieldObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		FieldMapping fieldMapping = fieldObjectMapper.readValue(fieldMapFile, FieldMapping.class);

		BeanUtils.copyProperties(projectConfFieldMapping, projectConfig);
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping.setFieldMapping(fieldMapping);
		AzureToolConfig azureToolConfig = new AzureToolConfig();
		azureToolConfig.setQueryEnabled(false);
		projectConfFieldMapping.setAzure(azureToolConfig );
	}
	
	@Test
	public void getWorkItemInfo() throws RestClientException, URISyntaxException, JsonParseException,
		JsonMappingException, IOException, ParseException, IllegalAccessException, InvocationTargetException {
		
		AzureServer azureServer = prepareAzureServer();
		String authHeader = "Basic " + "OlRlc3RVc2VyQDEyMw==";
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, authHeader);
		ResponseEntity<String> response = new ResponseEntity<>(createWorkItemResponse(), HttpStatus.OK);
		Mockito.when(rest.exchange(ArgumentMatchers.any(URI.class), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.eq(new HttpEntity<>(headers)), ArgumentMatchers.eq(String.class)))
			.thenReturn(response);
		List<Integer> ids = Arrays.asList(1, 2, 3,4,41,92,127,128);
		AzureBoardsWIModel actualModel = azureRestClientFactory.getWorkItemInfo(azureServer, ids);
		AzureBoardsWIModel expectedModel = new AzureBoardsWIModel();
		expectedModel.setCount(8);
		assertEquals(expectedModel.getCount(), actualModel.getCount(), "Checking for count values");
	
	}

	private String createWorkItemResponse() throws IOException {
		String filePath = "src/test/resources/onlinedata/azure/azureworkitemmodel.json";
		return new String(Files.readAllBytes(Paths.get(filePath)));
	}
	
	@Test
	public void getIterationsResponse() throws RestClientException, URISyntaxException, JsonParseException,
		JsonMappingException, IOException, ParseException, IllegalAccessException, InvocationTargetException {
		
		AzureServer azureServer = prepareAzureServer();
		String authHeader = "Basic " + "OlRlc3RVc2VyQDEyMw==";
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, authHeader);
		ResponseEntity<String> response = new ResponseEntity<>(createIterationsResponse(), HttpStatus.OK);
		Mockito.when(rest.exchange(ArgumentMatchers.any(URI.class), ArgumentMatchers.eq(HttpMethod.GET), ArgumentMatchers.eq(new HttpEntity<>(headers)), ArgumentMatchers.eq(String.class)))
			.thenReturn(response);
		AzureIterationsModel actualModel = azureRestClientFactory.getIterationsResponse(azureServer);
		AzureIterationsModel expectedModel = new AzureIterationsModel();
		expectedModel.setCount(3);
		assertEquals(expectedModel.getCount(), actualModel.getCount(), "Checking for count values");
	
	}

	private String createIterationsResponse() throws IOException {
		String filePath = "src/test/resources/onlinedata/azure/azureiterationsmodel.json";
		return new String(Files.readAllBytes(Paths.get(filePath)));
	}
	
	/**
	 * prepare query when data doesn't exist in db
	 * 
	 * @throws RestClientException
	 * @throws URISyntaxException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws ParseException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@Test
	public void getWiqlResponse_prepareQuery() throws RestClientException, URISyntaxException, JsonParseException,
			JsonMappingException, IOException, ParseException, IllegalAccessException, InvocationTargetException {
		prepareProjectConfigFieldMapping();
		AzureToolConfig azureToolConfig = new AzureToolConfig();
		azureToolConfig.setQueryEnabled(true);
		azureToolConfig.setBoardQuery(
				"Select [System.Id], [System.Title], [System.State] From WorkItems Where ([System.WorkItemType] ='Bug')order by [System.CreatedDate] asc");
		projectConfFieldMapping.setAzure(azureToolConfig);
		long startTime1 = new SimpleDateFormat(AzureConstants.SETTING_DATE_FORMAT, Locale.US)
				.parse("2019-01-07T00:00:00.000000").getTime();
		Map<String, Long> time = new HashMap();
		time.put("User Story", startTime1);
		time.put("Issue", startTime1);
		long startTime = new SimpleDateFormat(AzureConstants.SETTING_DATE_FORMAT, Locale.US)
				.parse("2019-01-07T00:00:00.000000").getTime();
		AzureServer azureServer = prepareAzureServer();
		ResponseEntity<String> response = new ResponseEntity<>(createWiqlResponse(), HttpStatus.OK);
		Mockito.when(rest.exchange(ArgumentMatchers.any(URI.class), ArgumentMatchers.eq(HttpMethod.POST), Mockito.<HttpEntity<String>>any(),
				ArgumentMatchers.eq(String.class))).thenReturn(response);
		AzureWiqlModel actualModel = azureRestClientFactory.getWiqlResponse(azureServer, time, projectConfFieldMapping,
				false);
		AzureWiqlModel expectedModel = new AzureWiqlModel();
		expectedModel.setAsOf("2020-07-17T12:01:50.663Z");
		;
		assertEquals(expectedModel.getAsOf(), actualModel.getAsOf(), "Checking for values");
		azureToolConfig.setBoardQuery(
				"Select [System.Id], [System.Title], [System.State] From WorkItems Where ([System.WorkItemType] ='Bug' AND [system.changeddate] > '2020-01-01') order by [System.CreatedDate] asc");
		projectConfFieldMapping.setAzure(azureToolConfig);
		actualModel = azureRestClientFactory.getWiqlResponse(azureServer, time, projectConfFieldMapping, false);
	}

	/**
	 * prepare query when data exist in db
	 * 
	 * @throws RestClientException
	 * @throws URISyntaxException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws ParseException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@Test
	public void getWiqlResponse_prepareQuery_isDataExistTrue()
			throws RestClientException, URISyntaxException, JsonParseException, JsonMappingException, IOException,
			ParseException, IllegalAccessException, InvocationTargetException {
		prepareProjectConfigFieldMapping();
		AzureToolConfig azureToolConfig = new AzureToolConfig();
		azureToolConfig.setQueryEnabled(true);
		azureToolConfig.setBoardQuery(
				"Select [System.Id], [System.Title], [System.State] From WorkItems Where ([System.WorkItemType] ='Bug')order by [System.CreatedDate] asc");
		projectConfFieldMapping.setAzure(azureToolConfig);
		long startTime1 = new SimpleDateFormat(AzureConstants.SETTING_DATE_FORMAT, Locale.US)
				.parse("2019-01-07T00:00:00.000000").getTime();
		Map<String, Long> time = new HashMap();
		time.put("User Story", startTime1);
		time.put("Issue", startTime1);
		long startTime = new SimpleDateFormat(AzureConstants.SETTING_DATE_FORMAT, Locale.US)
				.parse("2019-01-07T00:00:00.000000").getTime();
		AzureServer azureServer = prepareAzureServer();
		ResponseEntity<String> response = new ResponseEntity<>(createWiqlResponse(), HttpStatus.OK);
		Mockito.when(rest.exchange(ArgumentMatchers.any(URI.class), ArgumentMatchers.eq(HttpMethod.POST), Mockito.<HttpEntity<String>>any(),
				ArgumentMatchers.eq(String.class))).thenReturn(response);
		AzureWiqlModel actualModel = azureRestClientFactory.getWiqlResponse(azureServer, time, projectConfFieldMapping,
				true);
		AzureWiqlModel expectedModel = new AzureWiqlModel();
		expectedModel.setAsOf("2020-07-17T12:01:50.663Z");
		assertEquals(expectedModel.getAsOf(), actualModel.getAsOf(), "Checking for values");
		azureToolConfig.setBoardQuery(
				"Select [System.Id], [System.Title], [System.State] From WorkItems Where ([System.WorkItemType] ='Bug' AND [system.changeddate] > '2020-01-01') order by [System.CreatedDate] asc");
		projectConfFieldMapping.setAzure(azureToolConfig);
		actualModel = azureRestClientFactory.getWiqlResponse(azureServer, time, projectConfFieldMapping, true);
	}

}
