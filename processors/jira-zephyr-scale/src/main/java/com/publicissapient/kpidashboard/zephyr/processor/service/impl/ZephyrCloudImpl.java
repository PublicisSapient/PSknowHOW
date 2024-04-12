package com.publicissapient.kpidashboard.zephyr.processor.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.zephyr.ZephyrTestCaseDTO;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.zephyr.client.ZephyrClient;
import com.publicissapient.kpidashboard.zephyr.config.ZephyrConfig;
import com.publicissapient.kpidashboard.zephyr.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.zephyr.model.ZephyrCloudFolderResponse;
import com.publicissapient.kpidashboard.zephyr.util.ZephyrUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * get test case details from zephyr cloud url
 * https://api.zephyrscale.smartbear.com/v2/
 *
 */
@Component
@Slf4j
public class ZephyrCloudImpl implements ZephyrClient {

	private static final String FOLDERS_RESOURCE_ENDPOINT = "https://api.zephyrscale.smartbear.com/v2/folders?maxResults=1000";
	private static final String TEST_CASE_ENDPOINT = "testcases";
	private static final String PROJECT_KEY = "projectKey";
	private static final String START_AT = "startAt";
	private static final String MAX_RESULTS = "maxResults";
	private static final String VALUES = "values";
	private static final String KEY = "key";
	private static final String NAME = "name";
	private static final String CREATED_ON = "createdOn";
	private static final String CUSTOM_FIELDS = "customFields";
	private static final String LABELS = "labels";
	private static final String OWNER = "dummy_user";
	private static final String FOLDER = "folder";
	private static final String ID = "id";
	private static final String LINKS = "links";
	private static final String ISSUES = "issues";
	private static final String TARGET = "target";
	private static final String PARENT_ID = "parentId";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ZephyrConfig zephyrConfig;

	@Autowired
	private ZephyrUtil zephyrUtil;

	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;

	/**
	 * get json array from responseBody
	 *
	 * @param responseBody
	 * @param key
	 *
	 */
	public static JSONArray parseData(String responseBody, String key) throws ParseException {
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(responseBody);
		return (JSONArray) jsonObject.get(key);
	}

	/**
	 * get Test case details from zephyr cloud
	 *
	 * @param startAt
	 * @param projectConfig
	 * @return testCaseList
	 */
	@Override
	public List<ZephyrTestCaseDTO> getTestCase(int startAt, ProjectConfFieldMapping projectConfig) {

		List<ZephyrTestCaseDTO> testCaseList = new ArrayList<>();
		ProcessorToolConnection toolInfo = projectConfig.getProcessorToolConnection();
		if (StringUtils.isNotEmpty(toolInfo.getUrl())) {
			String zephyrCloudUrl = toolInfo.getUrl();
			String accessToken = zephyrUtil.decryptPassword(toolInfo.getAccessToken());
			String jiraCloudCredential = getJiraCloudCredentials(toolInfo);
			StringBuilder queryBuilder;
			try {
				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(zephyrCloudUrl);
				builder.path(TEST_CASE_ENDPOINT);
				builder.queryParam(MAX_RESULTS, zephyrConfig.getPageSize());
				builder.queryParam(START_AT, startAt);
				builder.queryParam(PROJECT_KEY, projectConfig.getProjectKey());

				Map<String, String> folderMap = prepareFoldersFullPath(accessToken);
				queryBuilder = new StringBuilder(builder.build(false).toString());

				if (StringUtils.isNotBlank(queryBuilder)) {
					String testCaseUrl = queryBuilder.toString();
					log.info("ZEPHYR query executed {} ....", testCaseUrl);

					HttpEntity<String> httpEntity = zephyrUtil.buildAuthHeaderUsingToken(accessToken);
					ResponseEntity<String> response = restTemplate.exchange(testCaseUrl, HttpMethod.GET, httpEntity,
							String.class);
					if (response.getStatusCode() == HttpStatus.OK && Objects.nonNull(response.getBody())) {
						parseResponseAndPrepareTestCases(testCaseList, accessToken, jiraCloudCredential, response,
								folderMap);
					} else {
						String statusCode = response.getStatusCode().toString();
						log.error("Error while fetching projects from {}. with status {}", testCaseUrl, statusCode);
						throw new RestClientException(
								"Got different status code: " + statusCode + " : " + response.getBody());
					}
				}
			} catch (Exception exception) {
				log.error("Error while fetching projects from {}", exception.getMessage());
				throw new RestClientException("Error while fetching projects from {}", exception);
			}

		}
		return testCaseList;
	}

	/**
	 * parse success response and for prepare test case details using another rest
	 * api call
	 *
	 * @param testCaseList
	 * @param accessToken
	 * @param jiraCloudCredential
	 * @param response
	 * @param folderMap
	 */
	private void parseResponseAndPrepareTestCases(List<ZephyrTestCaseDTO> testCaseList, String accessToken,
			String jiraCloudCredential, ResponseEntity<String> response, Map<String, String> folderMap)
			throws ParseException {
		JSONArray testCaseArr = parseData(response.getBody(), VALUES);
		for (Object testCaseObj : testCaseArr) {
			ZephyrTestCaseDTO zephyrTestCaseDTO = new ZephyrTestCaseDTO();
			JSONObject testcaseResponse = (JSONObject) testCaseObj;
			String key = getString(testcaseResponse, KEY);
			String name = getString(testcaseResponse, NAME);
			String createdDate = getString(testcaseResponse, CREATED_ON);
			List<String> labels = prepareLabelsDetails(testcaseResponse);
			Map<String, String> customFields = prepareCustomFieldsDetails(testcaseResponse);
			String folder = prepareFolderDetails(testcaseResponse, accessToken, folderMap);
			Set<String> issueLinks = prepareIssueLinks(testcaseResponse, jiraCloudCredential);
			zephyrTestCaseDTO.setKey(key);
			zephyrTestCaseDTO.setName(name);
			zephyrTestCaseDTO.setCreatedOn(createdDate);
			zephyrTestCaseDTO.setUpdatedOn(createdDate);
			zephyrTestCaseDTO.setLabels(labels);
			zephyrTestCaseDTO.setOwner(OWNER);
			zephyrTestCaseDTO.setFolder(folder);
			zephyrTestCaseDTO.setIssueLinks(issueLinks);
			zephyrTestCaseDTO.setCustomFields(customFields);
			testCaseList.add(zephyrTestCaseDTO);
		}
	}

	/**
	 * prepare Custom Fields based on test case details
	 *
	 * @param testcaseResponse
	 */
	private Map<String, String> prepareCustomFieldsDetails(JSONObject testcaseResponse) {
		JSONObject jsonObject = getJSONObject(testcaseResponse, CUSTOM_FIELDS);
		Map<String, String> customFields = new HashMap<>();
		Iterator<String> keys = jsonObject.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = jsonObject.get(key);
			if (Objects.nonNull(value)) {
				if (value instanceof ArrayList && CollectionUtils.isNotEmpty((ArrayList) value)) {
					String valueStr = String.join(", ", (ArrayList) value);
					customFields.put(key, valueStr);
				} else if (value instanceof String) {
					customFields.put(key, (String) value);
				}
			}
		}
		return customFields;
	}

	/**
	 * prepare Custom Fields based on test case details
	 *
	 * @param toolInfo
	 */
	private String getJiraCloudCredentials(ProcessorToolConnection toolInfo) {
		List<ProcessorToolConnection> projectToolConnection = processorToolConnectionService
				.findByToolAndBasicProjectConfigId(ProcessorConstants.JIRA, toolInfo.getBasicProjectConfigId());

		if (CollectionUtils.isNotEmpty(projectToolConnection)) {
			ProcessorToolConnection projectToolConnectionForJiraCloud = projectToolConnection.get(0);
			return zephyrUtil.getCredentialsAsBase64String(projectToolConnectionForJiraCloud.getUsername(),
					projectToolConnectionForJiraCloud.getPassword());
		}
		return null;
	}

	/**
	 * prepare label Fields list
	 *
	 * @param testcaseResponse
	 */
	private List<String> prepareLabelsDetails(JSONObject testcaseResponse) {
		List<String> labels = new ArrayList<>();
		JSONArray labelJsonArr = getJSONArray(testcaseResponse, LABELS);
		if (labelJsonArr != null && !labelJsonArr.isEmpty()) {
			for (Object obj : labelJsonArr) {
				labels.add((String) obj);
			}
		}
		return labels;
	}

	/**
	 * prepare folder full path using folderId
	 *
	 * @param testcaseResponse
	 * @param accessToken
	 * @param folderMap
	 */
	private String prepareFolderDetails(JSONObject testcaseResponse, String accessToken, Map<String, String> folderMap)
			throws ParseException {
		String folderFullPath = null;
		JSONObject jsonObject = getJSONObject(testcaseResponse, FOLDER);
		if (jsonObject != null) {
			String folderId = getString(jsonObject, ID);
			folderFullPath = folderMap.get(folderId);
		}
		return folderFullPath;
	}

	/**
	 * prepare issues links with test case
	 *
	 * @param testcaseResponse
	 * @param jiraCloudCredential
	 */
	private Set<String> prepareIssueLinks(JSONObject testcaseResponse, String jiraCloudCredential)
			throws ParseException {
		Set<String> issueLinks = new HashSet<>();
		if (jiraCloudCredential != null) {
			JSONObject jsonObject = getJSONObject(testcaseResponse, LINKS);
			if (Objects.nonNull(jsonObject)) {
				JSONArray jsonArray = getJSONArray(jsonObject, ISSUES);
				if (jsonArray != null && !jsonArray.isEmpty()) {
					for (Object issuesJson : jsonArray) {
						String url = getString((JSONObject) issuesJson, TARGET);
						String issue = getIssueLinksDetailsUsingRestAPICall(url, jiraCloudCredential);
						issueLinks.add(issue);
					}
				}
			}
		}
		return issueLinks;
	}

	/**
	 * Issues Id getting from rest api call
	 *
	 * @param url
	 *            for example = https://jira.cloudurl.com/rest/api/2/issue/103328
	 * @param jiraCloudCredential
	 *
	 */
	private String getIssueLinksDetailsUsingRestAPICall(String url, String jiraCloudCredential) throws ParseException {
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,
				zephyrUtil.buildAuthenticationHeader(jiraCloudCredential), String.class);
		if (response.getStatusCode() == HttpStatus.OK) {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getBody());
			return getString(jsonObject, KEY);
		}
		return null;
	}

	/**
	 * get string from json object
	 *
	 * @param jsonObject
	 * @param key
	 *
	 */
	public String getString(JSONObject jsonObject, String key) {
		String value = null;
		if (jsonObject != null) {
			final Object objectVal = jsonObject.get(key);
			if (objectVal != null) {
				value = objectVal.toString();
			}
		}
		return value;
	}

	/**
	 * get JSONObject from json object
	 *
	 * @param jsonObject
	 * @param key
	 *
	 */
	public JSONObject getJSONObject(JSONObject jsonObject, String key) {
		JSONObject value = null;
		if (jsonObject != null) {
			final Object objectVal = jsonObject.get(key);
			if (objectVal != null) {
				value = (JSONObject) objectVal;
			}
		}
		return value;
	}

	/**
	 * get JSONArray from json object
	 *
	 * @param jsonObject
	 * @param key
	 *
	 */
	public JSONArray getJSONArray(JSONObject jsonObject, String key) {
		JSONArray value = null;
		final Object objectVal = jsonObject.get(key);
		if (objectVal != null) {
			value = (JSONArray) objectVal;
		}
		return value;
	}

	/**
	 * prepare folder full path map(folderId,folderFullPath)
	 *
	 * @param accessToken
	 *
	 */
	private Map<String, String> prepareFoldersFullPath(String accessToken) throws ParseException {
		Map<String, String> folderMap = new HashMap<>();
		Map<String, ZephyrCloudFolderResponse> responseHashMap = new HashMap<>();
		String folderUrl = FOLDERS_RESOURCE_ENDPOINT;
		ResponseEntity<String> response = restTemplate.exchange(folderUrl, HttpMethod.GET,
				zephyrUtil.buildAuthHeaderUsingToken(accessToken), String.class);
		if (response.getStatusCode() == HttpStatus.OK) {
			JSONArray folderArr = parseData(response.getBody(), VALUES);
			List<String> folderIdList = new ArrayList<>();
			Set<String> parentIdList = new HashSet<>();
			for (int i = 0; i < folderArr.size(); i++) {
				JSONObject folderObj = (JSONObject) folderArr.get(i);
				ZephyrCloudFolderResponse zephyrCloudFolderResponse = new ZephyrCloudFolderResponse();
				String id = getString(folderObj, ID);
				String parentId = getString(folderObj, PARENT_ID);
				if (id != null) {
					folderIdList.add(id);
				}
				if (parentId != null) {
					parentIdList.add(parentId);
				}
				zephyrCloudFolderResponse.setId(id);
				zephyrCloudFolderResponse.setParentId(parentId);
				zephyrCloudFolderResponse.setName(getString(folderObj, NAME));
				responseHashMap.put(id, zephyrCloudFolderResponse);
			}

			List<String> currentFolderIdList = folderIdList.stream().filter(id -> !parentIdList.contains(id))
					.collect(Collectors.toList());
			currentFolderIdList.stream().forEach(id -> {
				String folderId = id;
				String fullFolderName = "";
				String parentId;
				String subFolderName;
				do {
					subFolderName = responseHashMap.get(id).getName();
					parentId = responseHashMap.get(id).getParentId();
					fullFolderName = "/" + subFolderName + fullFolderName;
					id = parentId;
				} while (parentId != null);
				folderMap.put(folderId, fullFolderName);
			});
		}
		return folderMap;
	}

}
