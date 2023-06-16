package com.publicissapient.kpidashboard.zephyr.processor.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.zephyr.ZephyrTestCaseDTO;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.zephyr.config.ZephyrConfig;
import com.publicissapient.kpidashboard.zephyr.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.zephyr.util.ZephyrUtil;

@ExtendWith(SpringExtension.class)
public class ZephyrCloudImplTest {

	@InjectMocks
	private ZephyrCloudImpl zephyrCloud;

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

	private ProcessorToolConnection toolInfoForJiraCloud;

	private List<ZephyrTestCaseDTO> testCaseList;

	private List<ZephyrTestCaseDTO> testCaseList2;

	private ZephyrTestCaseDTO zephyrTestCaseDTO;

	private String folderPath;

	@Mock
	private HttpEntity<String> mockHttpEntity;

	@BeforeEach
	public void init() {
		projectConfFieldMapping = new ProjectConfFieldMapping();
		projectConfFieldMapping.setProjectKey("TEST");
		toolInfo = new ProcessorToolConnection();
		toolInfo.setBasicProjectConfigId(new ObjectId("625fd013572701449a44b3de"));
		toolInfo.setUrl("https://api.test.com/v2/");
		toolInfo.setAccessToken("encryptToken");
		toolInfo.setConnectionId(new ObjectId("625d0d9d10ce157f45918b5c"));
		toolInfo.setCloudEnv(true);
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
		folderPath = "/CST/CST SP02 SF TC/CPQ/Automated test cases";
		List<String> folderPathList = new ArrayList<>();
		folderPathList.add(folderPath);
		toolInfo.setRegressionAutomationFolderPath(folderPathList);
		toolInfo.setInSprintAutomationFolderPath(folderPathList);
		projectConfFieldMapping.setProcessorToolConnection(toolInfo);
		testCaseList = new ArrayList<>();
		zephyrTestCaseDTO = new ZephyrTestCaseDTO();
		zephyrTestCaseDTO.setKey("TEST-T685");
		zephyrTestCaseDTO.setCreatedOn("2020-02-05T21:45:22Z");
		zephyrTestCaseDTO.setUpdatedOn("2020-02-05T21:45:22Z");
		List<String> labels = new ArrayList<>();
		labels.add("Integration|pilot");
		zephyrTestCaseDTO.setLabels(testRegressionValue);
		zephyrTestCaseDTO.setOwner(null);
		zephyrTestCaseDTO.setFolder("/CRM/Account & Contact Management/System Integration Test");
		zephyrTestCaseDTO.setIssueLinks(null);
		Map<String, String> customFields = new HashMap<>();
		customFields.put("Functional test type", "Regression, Manual");
		customFields.put("Scenario type", "Functional");
		customFields.put("Execution Type", "Automation");
		customFields.put("SynapseRT TC ref.", "TEST-5332");
		zephyrTestCaseDTO.setCustomFields(customFields);
		testCaseList.add(zephyrTestCaseDTO);

		testCaseList2 = new ArrayList<>();
		toolInfoForJiraCloud = new ProcessorToolConnection();
		toolInfoForJiraCloud.setUsername("jiraUserName");
		toolInfoForJiraCloud.setPassword("jiraCloudPwd");
		zephyrTestCaseDTO.setOwner("TestUser");
		Set<String> issueLinks = new HashSet<>();
		issueLinks.add("TEST-6659");
		zephyrTestCaseDTO.setIssueLinks(issueLinks);
		testCaseList2.add(zephyrTestCaseDTO);
	}

	@Test
	public void getTestCaseSuccessWithoutJiraCloudConfig() throws Exception {

		when(zephyrUtil.decryptPassword(toolInfo.getAccessToken())).thenReturn("decryptToken");
		HttpHeaders headers = new HttpHeaders();
		headers.set("AUTHORIZATION", "decryptToken");
		HttpEntity<String> stringHttpEntity = new HttpEntity<>(headers);
		when(zephyrUtil.buildAuthHeaderUsingToken("decryptToken")).thenReturn(stringHttpEntity);
		List<ProcessorToolConnection> projectToolConnection = new ArrayList<>();
		projectToolConnection.add(toolInfo);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(anyString(), any()))
				.thenReturn(projectToolConnection);
		when(zephyrConfig.getPageSize()).thenReturn(5);
		doReturn(
				new ResponseEntity<>(getServerResponseFromJson("test_cases_response_zephyr_cloud.json"), HttpStatus.OK))
						.when(restTemplate)
						.exchange(eq("https://api.test.com/v2/testcases?maxResults=5&startAt=0&projectKey=TEST"),
								eq(HttpMethod.GET), eq(stringHttpEntity), eq(String.class));

		doReturn(new ResponseEntity<>(getServerResponseFromJson("folder_details_response_zephyr_cloud.json"),
				HttpStatus.OK)).when(restTemplate).exchange(
						eq("https://api.zephyrscale.smartbear.com/v2/folders?maxResults=1000"), eq(HttpMethod.GET),
						eq(stringHttpEntity), eq(String.class));

		assertEquals(zephyrCloud.getTestCase(0, projectConfFieldMapping).size(), testCaseList.size());

	}

	@Test
	public void getTestCaseSuccess() throws Exception {

		when(zephyrUtil.decryptPassword(toolInfo.getAccessToken())).thenReturn("decryptToken");
		HttpHeaders headers = new HttpHeaders();
		headers.set("AUTHORIZATION", "decryptToken");
		HttpEntity<String> stringHttpEntity = new HttpEntity<>(headers);
		when(zephyrUtil.buildAuthHeaderUsingToken("decryptToken")).thenReturn(stringHttpEntity);
		List<ProcessorToolConnection> projectToolConnection = new ArrayList<>();
		projectToolConnection.add(toolInfoForJiraCloud);
		when(processorToolConnectionService.findByToolAndBasicProjectConfigId(anyString(), any()))
				.thenReturn(projectToolConnection);
		when(zephyrUtil.getCredentialsAsBase64String(toolInfoForJiraCloud.getUsername(),
				toolInfoForJiraCloud.getPassword())).thenReturn("base64Str");
		HttpHeaders headers2 = new HttpHeaders();
		headers2.set("AUTHORIZATION", "decryptToken");
		HttpEntity<String> stringHttpEntity2 = new HttpEntity<>(headers2);
		when(zephyrUtil.buildAuthenticationHeader("base64Str")).thenReturn(stringHttpEntity2);
		when(zephyrConfig.getPageSize()).thenReturn(5);
		doReturn(
				new ResponseEntity<>(getServerResponseFromJson("test_cases_response_zephyr_cloud.json"), HttpStatus.OK))
						.when(restTemplate)
						.exchange(eq("https://api.test.com/v2/testcases?maxResults=5&startAt=0&projectKey=TEST"),
								eq(HttpMethod.GET), eq(stringHttpEntity), eq(String.class));

		doReturn(new ResponseEntity<>(getServerResponseFromJson("folder_details_response_zephyr_cloud.json"),
				HttpStatus.OK)).when(restTemplate).exchange(
						eq("https://api.zephyrscale.smartbear.com/v2/folders?maxResults=1000"), eq(HttpMethod.GET),
						eq(stringHttpEntity), eq(String.class));

		doReturn(new ResponseEntity<>(getServerResponseFromJson("owner_details_response_zephyr_cloud.json"),
				HttpStatus.OK)).when(restTemplate).exchange(
						eq("https://test.com/rest/api/2/user?accountId=5e315421a834270cb0992ca7"), eq(HttpMethod.GET),
						eq(stringHttpEntity2), eq(String.class));

		doReturn(new ResponseEntity<>(getServerResponseFromJson("issue_links_response_zephyr_cloud.json"),
				HttpStatus.OK)).when(restTemplate).exchange(eq("https://test.com/rest/api/2/issue/66229"),
						eq(HttpMethod.GET), eq(stringHttpEntity2), eq(String.class));

		assertEquals(zephyrCloud.getTestCase(0, projectConfFieldMapping).size(), testCaseList2.size());

	}

	@Test()
	public void getTestCaseFailed() throws Exception {

		when(zephyrUtil.decryptPassword(toolInfo.getAccessToken())).thenReturn("decryptToken");
		HttpHeaders headers = new HttpHeaders();
		headers.set("AUTHORIZATION", "decryptToken");
		HttpEntity<String> stringHttpEntity = new HttpEntity<>(headers);
		when(zephyrUtil.buildAuthHeaderUsingToken("decryptToken")).thenReturn(stringHttpEntity);
		List<ProcessorToolConnection> projectToolConnection = new ArrayList<>();
		projectToolConnection.add(toolInfo);
		when(zephyrUtil.getCredentialsAsBase64String(toolInfo.getUsername(), toolInfo.getPassword()))
				.thenReturn("decryptToken");
		when(zephyrConfig.getPageSize()).thenReturn(5);
		doReturn(new ResponseEntity<>(getServerResponseFromJson("folder_details_response_zephyr_cloud.json"),
				HttpStatus.OK)).when(restTemplate).exchange(
						eq("https://api.zephyrscale.smartbear.com/v2/folders?maxResults=1000"), eq(HttpMethod.GET),
						eq(stringHttpEntity), eq(String.class));

		doReturn(new ResponseEntity<>(null, HttpStatus.NO_CONTENT)).when(restTemplate).exchange(
				eq("https://api.test.com/v2/testcases?maxResults=5&startAt=0&projectKey=TEST"), eq(HttpMethod.GET),
				eq(stringHttpEntity), eq(String.class));

		assertThrows(RestClientException.class, () -> {
			zephyrCloud.getTestCase(0, projectConfFieldMapping);
		});

	}

	private String getServerResponseFromJson(String resource) throws Exception {
		return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
	}
}
