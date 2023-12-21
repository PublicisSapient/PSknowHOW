package com.publicissapient.kpidashboard.zephyr.processor.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.zephyr.ZephyrTestCaseDTO;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.zephyr.config.ZephyrConfig;
import com.publicissapient.kpidashboard.zephyr.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.zephyr.util.ZephyrUtil;

@ExtendWith(SpringExtension.class)
public class ZephyrServerImplTest {

	@Mock
	ResponseEntity<ZephyrTestCaseDTO[]> testCaseResponse;
	@InjectMocks
	private ZephyrServerImpl zephyrServer;
	@Mock
	private ZephyrConfig zephyrConfig;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private ZephyrUtil zephyrUtil;
	@Mock
	private ProcessorToolConnectionService processorToolConnectionService;
	private ProjectConfFieldMapping projectConfFieldMapping;
	private ProcessorToolConnection toolInfo;
	private List<ZephyrTestCaseDTO> testCaseList;
	private String folderPath;
	@Mock
	private HttpEntity<String> mockHttpEntity;
	private ZephyrTestCaseDTO zephyrTestCaseDTO;

	private ZephyrTestCaseDTO[] zephyrTestCaseArr;

	@BeforeEach
	public void init() {
		projectConfFieldMapping = new ProjectConfFieldMapping();
		projectConfFieldMapping.setProjectKey("TEST");
		toolInfo = new ProcessorToolConnection();
		toolInfo.setBasicProjectConfigId(new ObjectId("625fd013572701449a44b3de"));
		toolInfo.setUrl("https://test.com/jira");
		toolInfo.setApiEndPoint("/rest/atm/1.0");
		toolInfo.setUsername("test");
		toolInfo.setPassword("password");
		toolInfo.setConnectionId(new ObjectId("625d0d9d10ce157f45918b5c"));
		toolInfo.setCloudEnv(false);
		toolInfo.setTestAutomated("Execution Type");
		List<String> automatedTestValue = new ArrayList<>();
		automatedTestValue.add("Y");
		toolInfo.setAutomatedTestValue(automatedTestValue);
		toolInfo.setTestAutomationStatusLabel("AutomationStatus");
		toolInfo.setTestRegressionLabel("testRegressionLabel");
		List<String> testRegressionValue = new ArrayList<>();
		testRegressionValue.add("testRegressionLabel");
		toolInfo.setTestRegressionValue(testRegressionValue);
		List<String> canNotAutomatedTestValue = new ArrayList<>();
		canNotAutomatedTestValue.add("value");
		toolInfo.setCanNotAutomatedTestValue(canNotAutomatedTestValue);
		folderPath = "/KnowHOW/knowHowFolderwiseTestcase/Dashboard/Filters";
		List<String> folderPathList = new ArrayList<>();
		folderPathList.add(folderPath);
		toolInfo.setRegressionAutomationFolderPath(folderPathList);
		toolInfo.setInSprintAutomationFolderPath(folderPathList);
		projectConfFieldMapping.setProcessorToolConnection(toolInfo);
		testCaseList = new ArrayList<>();

		zephyrTestCaseDTO = new ZephyrTestCaseDTO();
		zephyrTestCaseDTO.setKey("TEST-T5");
		zephyrTestCaseDTO.setCreatedOn("2020-07-10T12:02:31.000Z");
		zephyrTestCaseDTO.setUpdatedOn("2021-08-11T11:08:32.000Z");
		List<String> labels = new ArrayList<>();
		labels.add("ServiceDesk");
		zephyrTestCaseDTO.setLabels(testRegressionValue);
		zephyrTestCaseDTO.setOwner("psi167");
		zephyrTestCaseDTO.setFolder("/CRM/Account & Contact Management/System Integration Test");
		Set<String> issuesLinks = new HashSet<>();
		issuesLinks.add("issue1");
		issuesLinks.add("issue2");
		zephyrTestCaseDTO.setIssueLinks(issuesLinks);
		Map<String, String> customFields = new HashMap<>();
		customFields.put("Functional test type", "Regression, Manual");
		customFields.put("Scenario type", "Functional");
		customFields.put("Execution Type", "Automation");
		customFields.put("SynapseRT TC ref.", "SMAR-5332");
		zephyrTestCaseDTO.setCustomFields(customFields);
		testCaseList.add(zephyrTestCaseDTO);
		zephyrTestCaseArr = new ZephyrTestCaseDTO[1];
		zephyrTestCaseArr[0] = zephyrTestCaseDTO;
	}

	@Test
	public void getTestCaseSuccess() {
		when(zephyrUtil.getZephyrUrl(toolInfo.getUrl())).thenReturn("https://test.com/jira");
		when(zephyrUtil.buildAPIUrl(toolInfo.getUrl(), toolInfo.getApiEndPoint()))
				.thenReturn(UriComponentsBuilder.fromPath("https://test.com/jira/rest/atm/1.0"));
		when(zephyrUtil.getCredentialsAsBase64String(toolInfo.getUsername(), toolInfo.getPassword()))
				.thenReturn("base64String");
		when(zephyrUtil.buildAuthenticationHeader(Mockito.anyString())).thenReturn(mockHttpEntity);
		when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.eq(mockHttpEntity),
				Mockito.any(Class.class))).thenReturn(testCaseResponse);
		when(testCaseResponse.getStatusCode()).thenReturn(HttpStatus.OK);
		when(testCaseResponse.getBody()).thenReturn(zephyrTestCaseArr);
		assertEquals((zephyrServer.getTestCase(0, projectConfFieldMapping)).size(), testCaseList.size());
	}

	@Test()
	public void testGetTestCaseNotFound() {
		when(zephyrUtil.getZephyrUrl(toolInfo.getUrl())).thenReturn("https://test.com/jira");
		when(zephyrUtil.buildAPIUrl(toolInfo.getUrl(), toolInfo.getApiEndPoint()))
				.thenReturn(UriComponentsBuilder.fromPath("https://test.com/jira/rest/atm/1.0"));
		when(zephyrUtil.getCredentialsAsBase64String(toolInfo.getUsername(), toolInfo.getPassword()))
				.thenReturn("base64String");
		when(zephyrUtil.buildAuthenticationHeader(Mockito.anyString())).thenReturn(mockHttpEntity);
		when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.eq(mockHttpEntity),
				Mockito.any(Class.class))).thenReturn(testCaseResponse);
		when(testCaseResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
		when(testCaseResponse.getBody()).thenReturn(null);
		assertThrows(RestClientException.class, () -> {
			zephyrServer.getTestCase(0, projectConfFieldMapping);
		});
	}

	@Test
	public void testGetTestCaseBearerToken() {
		toolInfo.setBearerToken(true);
		toolInfo.setPatOAuthToken("ACBD");
		when(zephyrUtil.getZephyrUrl(toolInfo.getUrl())).thenReturn("https://test.com/jira");
		when(zephyrUtil.buildAPIUrl(toolInfo.getUrl(), toolInfo.getApiEndPoint()))
				.thenReturn(UriComponentsBuilder.fromPath("https://test.com/jira/rest/atm/1.0"));
		when(zephyrUtil.getCredentialsAsBase64String(toolInfo.getUsername(), toolInfo.getPassword()))
				.thenReturn("base64String");
		when(zephyrUtil.buildBearerHeader(Mockito.anyString())).thenReturn(mockHttpEntity);
		when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.GET), Mockito.eq(mockHttpEntity),
				Mockito.any(Class.class))).thenReturn(testCaseResponse);
		when(testCaseResponse.getStatusCode()).thenReturn(HttpStatus.OK);
		when(testCaseResponse.getBody()).thenReturn(zephyrTestCaseArr);
		assertEquals((zephyrServer.getTestCase(0, projectConfFieldMapping)).size(), testCaseList.size());
	}
}
