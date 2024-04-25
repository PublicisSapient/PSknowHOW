package com.publicissapient.kpidashboard.jiratest.processor.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.logging.log4j.util.Strings;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONTokener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jiratest.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jiratest.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jiratest.config.JiraTestProcessorConfig;
import com.publicissapient.kpidashboard.jiratest.model.JiraInfo;
import com.publicissapient.kpidashboard.jiratest.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jiratest.oauth.JiraOAuthClient;
import com.publicissapient.kpidashboard.jiratest.oauth.JiraOAuthProperties;
import com.publicissapient.kpidashboard.jiratest.processor.service.JiraTestService;
import com.publicissapient.kpidashboard.jiratest.repository.JiraTestProcessorRepository;
import com.publicissapient.kpidashboard.jiratest.util.JiraConstants;
import com.publicissapient.kpidashboard.jiratest.util.JiraIssueClientUtil;
import com.publicissapient.kpidashboard.jiratest.util.JiraProcessorUtil;

import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JiraTestServiceImpl implements JiraTestService {

	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
	private static final String MSG_JIRA_CLIENT_SETUP_FAILED = "Jira client setup failed. No results obtained. Check your jira setup.";
	private static final String ERROR_MSG_401 = "Error 401 connecting to JIRA server, your credentials are probably wrong. Note: Ensure you are using JIRA user name not your email address.";
	private static final String ERROR_MSG_NO_RESULT_WAS_AVAILABLE = "No result was available from Jira unexpectedly - defaulting to blank response. The reason for this fault is the following : {}";
	private static final String NO_RESULT_QUERY = "No result available for query: {}";
	private static final String TEST_AUTOMATED_FLAG = "testAutomatedFlag";
	private static final String TEST_CAN_BE_AUTOMATED_FLAG = "testCanBeAutomatedFlag";
	private static final String AUTOMATED_VALUE = "automatedValue";
	private static final String ERROR_PARSING_TEST_AUTOMATED_FIELD = "JIRA Processor |Error while parsing test automated field";

	public static final String FALSE = "false";

	public static final String PROCESSING_ISSUES_PRINT_LOG = "Processing issues %d - %d out of %d";

	public static final Set<String> ISSUE_FIELD_SET = new HashSet<>();
	@Autowired
	private JiraTestProcessorConfig jiraTestProcessorConfig;
	@Autowired
	private JiraRestClientFactory jiraRestClientFactory;
	@Autowired
	private JiraOAuthProperties jiraOAuthProperties;
	@Autowired
	private JiraOAuthClient jiraOAuthClient;
	@Autowired
	private AesEncryptionService aesEncryptionService;
	@Autowired
	private ConnectionRepository connectionRepository;
	@Autowired
	private ProjectToolConfigRepository toolRepository;
	@Autowired
	private ToolCredentialProvider toolCredentialProvider;
	@Autowired
	private JiraTestProcessorRepository jiraTestProcessorRepository;
	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Autowired
	private TestCaseDetailsRepository testCaseDetailsRepository;
	private ProcessorJiraRestClient client;

	private KerberosClient krb5Client;

	/**
	 * Explicitly updates queries for the source system, and initiates the update to
	 * MongoDB from those calls.
	 *
	 * @param projectConfig
	 *            Project Configuration Mapping
	 * @return Count of Jira Issues processed for scrum project
	 */
	@Override
	public int processesJiraIssues(ProjectConfFieldMapping projectConfig, boolean isOffline) {
		log.info("Start Processing Jira Issues");
		if (Objects.nonNull(projectConfig.getProcessorToolConnection())
				&& StringUtils.isNotEmpty(projectConfig.getProcessorToolConnection().getBoardQuery())) {
			return processesJiraIssuesJQL(projectConfig);
		} else {
			processesJiraIssues(projectConfig);
		}
		return 0;
	}

	public int processesJiraIssues(ProjectConfFieldMapping projectConfig) {

		int savedIsuesCount = 0;
		int total = 0;

		Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType = new HashMap<>();

		ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
				projectConfig.getBasicProjectConfigId().toHexString());
		boolean processorFetchingComplete = false;
		try {
			client = getProcessorJiraRestClient(projectConfig);

			boolean dataExist = (testCaseDetailsRepository
					.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);

			Map<String, LocalDateTime> maxChangeDatesByIssueType = getLastChangedDatesByIssueType(projectConfig);

			Map<String, LocalDateTime> maxChangeDatesByIssueTypeWithAddedTime = new HashMap<>();

			maxChangeDatesByIssueType.forEach((k, v) -> {
				long extraMinutes = jiraTestProcessorConfig.getMinsToReduce();
				maxChangeDatesByIssueTypeWithAddedTime.put(k, v.minusMinutes(extraMinutes));
			});
			int pageSize = getPageSize();

			String userTimeZone = getUserTimeZone(projectConfig);

			for (int i = 0; true; i += pageSize) {
				SearchResult searchResult = getIssues(projectConfig, maxChangeDatesByIssueTypeWithAddedTime,
						userTimeZone, i, dataExist, client);
				List<Issue> issues = getIssuesFromResult(searchResult);
				if (total == 0) {
					total = getTotal(searchResult);
				}

				if (CollectionUtils.isNotEmpty(issues)) {

					List<TestCaseDetails> testCaseDetailsList = prepareTestCaseDetails(issues, projectConfig);
					testCaseDetailsRepository.saveAll(testCaseDetailsList);
					findLastSavedTestCaseDetailsByType(testCaseDetailsList, lastSavedJiraIssueChangedDateByType);
					savedIsuesCount += issues.size();
				}

				MDC.put("JiraTimeZone", String.valueOf(userTimeZone));
				MDC.put("IssueCount", String.valueOf(issues.size()));
				// will result in an extra call if number of results == pageSize
				// but I would rather do that then complicate the jira client
				// implementation
				if (issues.size() < pageSize) {
					break;
				}
			}
			processorFetchingComplete = true;
		} catch (JSONException e) {
			log.error("Error while updating Story information in scrum client", e);
			lastSavedJiraIssueChangedDateByType.clear();
		} finally {
			boolean isAttemptSuccess = isAttemptSuccess(total, savedIsuesCount, processorFetchingComplete);
			if (!isAttemptSuccess) {
				lastSavedJiraIssueChangedDateByType.clear();
			}
			saveExecutionTraceLog(processorExecutionTraceLog, lastSavedJiraIssueChangedDateByType, isAttemptSuccess);
		}

		return savedIsuesCount;
	}

	private boolean isAttemptSuccess(int total, int savedCount, boolean processorFetchingComplete) {
		return savedCount > 0 && total == savedCount && processorFetchingComplete;
	}

	private List<Issue> getIssuesFromResult(SearchResult searchResult) {
		if (searchResult != null) {
			return Lists.newArrayList(searchResult.getIssues());
		}
		return new ArrayList<>();
	}

	private int getTotal(SearchResult searchResult) {
		if (searchResult != null) {
			return searchResult.getTotal();
		}
		return 0;
	}

	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.JIRA_TEST);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
		return processorExecutionTraceLog;
	}

	private void saveExecutionTraceLog(ProcessorExecutionTraceLog processorExecutionTraceLog,
			Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType, boolean isSuccess) {

		if (lastSavedJiraIssueChangedDateByType.isEmpty()) {
			processorExecutionTraceLog.setLastSavedEntryUpdatedDateByType(null);
		} else {
			processorExecutionTraceLog.setLastSavedEntryUpdatedDateByType(lastSavedJiraIssueChangedDateByType);
		}

		processorExecutionTraceLog.setExecutionSuccess(isSuccess);
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		processorExecutionTraceLogService.save(processorExecutionTraceLog);
	}

	private void findLastSavedTestCaseDetailsByType(List<TestCaseDetails> testCaseDetails,
			Map<String, LocalDateTime> lastSavedTestCasesChangedDateByType) {
		Map<String, List<TestCaseDetails>> issuesByType = CollectionUtils.emptyIfNull(testCaseDetails).stream()
				.sorted(Comparator.comparing((TestCaseDetails testCase) -> LocalDateTime.parse(testCase.getUpdateDate(),
						DateTimeFormatter.ofPattern(JiraConstants.TEST_CASE_CHANGE_DATE_FORMAT))).reversed())
				.collect(Collectors.groupingBy(TestCaseDetails::getOriginalTypeName));

		issuesByType.forEach((typeName, issues) -> {
			TestCaseDetails firstTestCase = issues
					.stream().sorted(
							Comparator
									.comparing(
											(TestCaseDetails testCase) -> LocalDateTime.parse(testCase.getUpdateDate(),
													DateTimeFormatter
															.ofPattern(JiraConstants.TEST_CASE_CHANGE_DATE_FORMAT)))
									.reversed())
					.findFirst().orElse(null);
			if (firstTestCase != null) {
				LocalDateTime currentIssueDate = LocalDateTime.parse(firstTestCase.getUpdateDate(),
						DateTimeFormatter.ofPattern(JiraConstants.TEST_CASE_CHANGE_DATE_FORMAT));
				LocalDateTime capturedDate = lastSavedTestCasesChangedDateByType.get(typeName);
				lastSavedTestCasesChangedDateByType.put(typeName, updatedDateToSave(capturedDate, currentIssueDate));
			}
		});
	}

	private LocalDateTime updatedDateToSave(LocalDateTime capturedDate, LocalDateTime currentIssueDate) {
		if (capturedDate == null) {
			return currentIssueDate;
		}

		if (currentIssueDate.isAfter(capturedDate)) {
			return currentIssueDate;
		}
		return capturedDate;
	}

	/**
	 * Purges list of issues provided in input
	 *
	 * @param purgeIssuesList
	 *            List of issues to be purged
	 * @param projectConfig
	 *            Project Configuration Mapping
	 */
	@Override
	public void purgeJiraIssues(List<Issue> purgeIssuesList, ProjectConfFieldMapping projectConfig) {

		List<TestCaseDetails> testCaseDetailsList = Lists.newArrayList();
		purgeIssuesList.forEach(issue -> {
			String issueNumber = JiraProcessorUtil.deodeUTF8String(issue.getKey());

			TestCaseDetails testCaseDetail = findOneTestCaseDetail(issueNumber,
					projectConfig.getBasicProjectConfigId().toString());
			if (testCaseDetail != null) {
				testCaseDetailsList.add(testCaseDetail);
			}

		});
		testCaseDetailsRepository.deleteAll(testCaseDetailsList);
	}

	/**
	 * Saves jira issues details
	 *
	 * @param currentPagedJiraRs
	 *            List of Jira issue in current page call
	 * @param projectConfig
	 *            Project Configuration Mapping
	 * @throws JSONException
	 *             Error If JSON is invalid
	 */
	public List<TestCaseDetails> prepareTestCaseDetails(List<Issue> currentPagedJiraRs,
			ProjectConfFieldMapping projectConfig) throws JSONException {

		List<TestCaseDetails> testCaseDetailsToSave = new ArrayList<>();

		if (null == currentPagedJiraRs) {
			log.error("JIRA TEST Processor | No list of current paged JIRA's issues found");
			return testCaseDetailsToSave;
		}
		ObjectId jiraTestProcessorId = jiraTestProcessorRepository.findByProcessorName(ProcessorConstants.JIRA_TEST)
				.getId();
		ProcessorToolConnection jiraTestToolInfo = projectConfig.getProcessorToolConnection();
		for (Issue issue : currentPagedJiraRs) {

			Set<String> testCaseTypeNames = new HashSet<>();
			for (String testCaseTypeName : jiraTestToolInfo.getJiraTestCaseType()) {
				testCaseTypeNames.add(testCaseTypeName.toLowerCase(Locale.getDefault()));
			}
			String issueNumber = JiraProcessorUtil.deodeUTF8String(issue.getKey());
			TestCaseDetails testCaseDetail = getTestCaseDetail(projectConfig, issueNumber);

			Map<String, IssueField> fields = JiraIssueClientUtil.buildFieldMap(issue.getFields());

			IssueType issueType = issue.getIssueType();

			if (testCaseTypeNames.contains(
					JiraProcessorUtil.deodeUTF8String(issueType.getName()).toLowerCase(Locale.getDefault()))) {
				log.debug(String.format("[%-12s] %s", JiraProcessorUtil.deodeUTF8String(issue.getKey()),
						JiraProcessorUtil.deodeUTF8String(issue.getSummary())));

				testCaseDetail.setProcessorId(jiraTestProcessorId);
				testCaseDetail.setProjectName(projectConfig.getProjectName());
				String id = new StringBuffer(projectConfig.getProjectName()).append(CommonConstant.UNDERSCORE)
						.append(projectConfig.getBasicProjectConfigId().toString()).toString();
				testCaseDetail.setProjectID(id);
				testCaseDetail.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString());

				testCaseDetail.setOriginalTypeName(issueType.getName());
				testCaseDetail.setName(issueType.getDescription());
				testCaseDetail.setTypeName(NormalizedJira.TEST_TYPE.getValue());

				processJiraIssueData(testCaseDetail, issue);

				// automation and regression field mapping
				setTestAutomatedField(testCaseDetail, issue, jiraTestToolInfo, fields);

				testCaseDetailsToSave.add(testCaseDetail);
			}
		}
		return testCaseDetailsToSave;
	}

	private TestCaseDetails getTestCaseDetail(ProjectConfFieldMapping projectConfig, String issueId) {
		TestCaseDetails testCaseDetail;
		testCaseDetail = findOneTestCaseDetail(issueId, projectConfig.getBasicProjectConfigId().toString());
		if (testCaseDetail == null) {
			testCaseDetail = new TestCaseDetails();
		}
		return testCaseDetail;
	}

	private TestCaseDetails findOneTestCaseDetail(String issueId, String basicProjectConfigId) {
		List<TestCaseDetails> testCaseDetails = testCaseDetailsRepository.findByNumberAndBasicProjectConfigId(issueId,
				basicProjectConfigId);

		if (testCaseDetails.size() > 1) {
			log.error("JIRA Test Processor | More than one TestCaseDetails found for id {}", issueId);
		}

		if (!testCaseDetails.isEmpty()) {
			return testCaseDetails.get(0);
		}
		return null;

	}

	public void processJiraIssueData(TestCaseDetails testCaseDetail, Issue issue) {

		String status = issue.getStatus().getName();
		String createdDate = issue.getCreationDate().toString();
		String changeDate = issue.getUpdateDate().toString();
		testCaseDetail.setNumber(JiraProcessorUtil.deodeUTF8String(issue.getKey()));
		testCaseDetail.setTestCaseStatus(JiraProcessorUtil.deodeUTF8String(status));
		testCaseDetail
				.setCreatedDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(createdDate)));
		testCaseDetail.setUpdateDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(changeDate)));

		// Label
		testCaseDetail.setLabels(JiraIssueClientUtil.getLabelsList(issue));
		// test case link with story
		setStoryLinkWithDefect(issue, testCaseDetail);
	}

	// @Override
	public void setTestAutomatedField(TestCaseDetails testCaseDetail, Issue issue,
			ProcessorToolConnection jiraTestToolInfo, Map<String, IssueField> fields) {
		try {
			String testAutomated = "None";
			String testAutomatedValue = NormalizedJira.NO_VALUE.getValue();
			String testCanBeAutomatedValue = NormalizedJira.NO_VALUE.getValue();
			Map<String, List<String>> identifierMap = checkIdentifier(jiraTestToolInfo);
			Map<String, String> finalLabelMap = null;
			Map<String, String> finalCustomFieldMap = null;
			for (Map.Entry<String, List<String>> entrySet : identifierMap.entrySet()) {
				if (entrySet.getKey().equalsIgnoreCase(JiraConstants.LABELS)
						&& CollectionUtils.isNotEmpty(entrySet.getValue())) {
					finalLabelMap = processLabels(entrySet.getValue(), issue, jiraTestToolInfo);
				}
				if (entrySet.getKey().equalsIgnoreCase(JiraConstants.CUSTOM_FIELD)
						&& CollectionUtils.isNotEmpty(entrySet.getValue())) {
					finalCustomFieldMap = processCustomField(entrySet.getValue(), jiraTestToolInfo, fields);
				}

			}
			Map<String, String> finalMap = processMap(finalLabelMap, finalCustomFieldMap);
			if (finalMap.get(TEST_AUTOMATED_FLAG) != null) {
				testCaseDetail.setTestAutomatedDate(JiraProcessorUtil
						.getFormattedDate(JiraProcessorUtil.deodeUTF8String(issue.getCreationDate().toString())));
			}
			testCaseDetail.setTestAutomated(finalMap.getOrDefault(AUTOMATED_VALUE, testAutomated));// THE VALUE
			testCaseDetail.setIsTestAutomated(finalMap.getOrDefault(TEST_AUTOMATED_FLAG, testAutomatedValue));// THE
																											  // VALUE
			testCaseDetail.setIsTestCanBeAutomated(
					finalMap.getOrDefault(TEST_CAN_BE_AUTOMATED_FLAG, testCanBeAutomatedValue));

			setRegressionLabel(jiraTestToolInfo, fields, testCaseDetail);
		} catch (Exception e) {
			log.error(ERROR_PARSING_TEST_AUTOMATED_FIELD, e);
		}
	}

	protected Map<String, List<String>> checkIdentifier(ProcessorToolConnection jiraTestToolInfo) {
		Map<String, List<String>> identifierMap = new HashMap<>();

		List<String> identiferLabel = new ArrayList<>();
		List<String> identiferCustomField = new ArrayList<>();
		if (jiraTestToolInfo.getTestAutomatedIdentification() != null
				&& jiraTestToolInfo.getTestAutomatedIdentification().trim().equalsIgnoreCase(JiraConstants.LABELS)) {
			identiferLabel.add(JiraConstants.CAN_BE_AUTOMATED);
		}
		if (jiraTestToolInfo.getTestAutomationCompletedIdentification() != null && jiraTestToolInfo
				.getTestAutomationCompletedIdentification().trim().equalsIgnoreCase(JiraConstants.LABELS)) {
			identiferLabel.add(JiraConstants.AUTOMATION);
		}
		identifierMap.put(JiraConstants.LABELS, identiferLabel);
		if (jiraTestToolInfo.getTestAutomatedIdentification() != null && jiraTestToolInfo
				.getTestAutomatedIdentification().trim().equalsIgnoreCase(JiraConstants.CUSTOM_FIELD)) {
			identiferCustomField.add(JiraConstants.CAN_BE_AUTOMATED);
		}
		if (jiraTestToolInfo.getTestAutomationCompletedIdentification() != null && jiraTestToolInfo
				.getTestAutomationCompletedIdentification().trim().equalsIgnoreCase(JiraConstants.CUSTOM_FIELD)) {
			identiferCustomField.add(JiraConstants.AUTOMATION);
		}
		identifierMap.put(JiraConstants.CUSTOM_FIELD, identiferCustomField);
		return identifierMap;
	}

	protected Map<String, String> processLabels(List<String> value, Issue issue,
			ProcessorToolConnection jiraTestToolInfo) {
		Map<String, String> resultMap = new HashMap<>();
		String testAutomatedFlag = null;
		String testCanBeAutomatedFlag = null;
		String automatedValue = null;

		for (String identifier : value) {
			if (identifier.equalsIgnoreCase(JiraConstants.AUTOMATION)
					&& hasAtLeastOneCommonElement(issue.getLabels(), jiraTestToolInfo.getJiraAutomatedTestValue())) {
				automatedValue = jiraTestToolInfo.getJiraAutomatedTestValue().get(0);
				testAutomatedFlag = NormalizedJira.YES_VALUE.getValue();
			}
			if (identifier.equalsIgnoreCase(JiraConstants.CAN_BE_AUTOMATED) && hasAtLeastOneCommonElement(
					issue.getLabels(), jiraTestToolInfo.getJiraCanBeAutomatedTestValue())) {
				testCanBeAutomatedFlag = NormalizedJira.YES_VALUE.getValue();
			}
		}
		resultMap.put(TEST_AUTOMATED_FLAG, testAutomatedFlag);
		resultMap.put(TEST_CAN_BE_AUTOMATED_FLAG, testCanBeAutomatedFlag);
		resultMap.put(AUTOMATED_VALUE, automatedValue);
		return resultMap;
	}

	protected Map<String, String> processCustomField(List<String> value, ProcessorToolConnection jiraTestToolInfo,
			Map<String, IssueField> fields) {
		Map<String, String> resultMap = new HashMap<>();
		String automatedValue = null;
		String testAutomatedFlag = null;
		String testCanBeAutomatedFlag = null;
		for (String identifier : value) {
			if (identifier.equalsIgnoreCase(JiraConstants.AUTOMATION)) {
				testAutomatedFlag = processJson(jiraTestToolInfo.getTestAutomationCompletedByCustomField(), fields,
						jiraTestToolInfo.getJiraAutomatedTestValue());
				if (testAutomatedFlag.equalsIgnoreCase(NormalizedJira.YES_VALUE.getValue())) {
					automatedValue = jiraTestToolInfo.getJiraAutomatedTestValue().get(0);
				}

			}
			if (identifier.equalsIgnoreCase(JiraConstants.CAN_BE_AUTOMATED)) {
				testCanBeAutomatedFlag = processJson(jiraTestToolInfo.getTestAutomated(), fields,
						jiraTestToolInfo.getJiraCanBeAutomatedTestValue());
			}
		}

		resultMap.put(TEST_AUTOMATED_FLAG, testAutomatedFlag);
		resultMap.put(TEST_CAN_BE_AUTOMATED_FLAG, testCanBeAutomatedFlag);
		resultMap.put(AUTOMATED_VALUE, automatedValue);
		return resultMap;
	}

	private boolean hasAtLeastOneCommonElement(Set<String> issueLabels, List<String> configuredLabels) {
		if (org.apache.commons.collections4.CollectionUtils.isEmpty(issueLabels)) {
			return false;
		}
		return configuredLabels.stream().anyMatch(issueLabels::contains);
	}

	private String processJson(String fieldMapping, Map<String, IssueField> fields, List<String> jiraTestValue) {
		String automationFlag = NormalizedJira.NO_VALUE.getValue();
		String fetchedValueFromJson = null;
		try {
			if (fields.get(fieldMapping) != null && fields.get(fieldMapping).getValue() != null) {
				String data = fields.get(fieldMapping).getValue().toString();
				Object json = new JSONTokener(data).nextValue();

				if (json instanceof org.codehaus.jettison.json.JSONObject) {
					fetchedValueFromJson = ((org.codehaus.jettison.json.JSONObject) fields.get(fieldMapping).getValue())
							.getString(JiraConstants.VALUE);
					if (jiraTestValue.contains(fetchedValueFromJson)) {
						automationFlag = NormalizedJira.YES_VALUE.getValue();
					}
				} else if (json instanceof org.codehaus.jettison.json.JSONArray) {
					JSONParser parser = new JSONParser();
					org.json.simple.JSONObject jsonObject;
					JSONArray array = (JSONArray) parser.parse(fields.get(fieldMapping).getValue().toString());
					for (int i = 0; i < array.size(); i++) {
						jsonObject = (org.json.simple.JSONObject) parser.parse(array.get(i).toString());
						fetchedValueFromJson = jsonObject.get(JiraConstants.VALUE).toString();
					}
					if (jiraTestValue.contains(fetchedValueFromJson)) {
						automationFlag = NormalizedJira.YES_VALUE.getValue();
					}
				}
			}

		} catch (JSONException | org.json.simple.parser.ParseException e) {
			log.error(ERROR_PARSING_TEST_AUTOMATED_FIELD, e);
		}
		return automationFlag;
	}

	private String processJsonForCustomFields(String fieldMapping, Map<String, IssueField> fields,
			List<String> jiraTestValue) {
		String fetchedValueFromJson = Strings.EMPTY;
		try {
			if (fields.get(fieldMapping) != null && fields.get(fieldMapping).getValue() != null) {
				String data = fields.get(fieldMapping).getValue().toString();
				Object json = new JSONTokener(data).nextValue();

				if (json instanceof org.codehaus.jettison.json.JSONObject) {
					fetchedValueFromJson = ((org.codehaus.jettison.json.JSONObject) fields.get(fieldMapping).getValue())
							.getString(JiraConstants.VALUE);
					if (jiraTestValue.contains(fetchedValueFromJson)) {
						return fetchedValueFromJson;
					}
				} else if (json instanceof org.codehaus.jettison.json.JSONArray) {
					JSONParser parser = new JSONParser();
					org.json.simple.JSONObject jsonObject;
					JSONArray array = (JSONArray) parser.parse(fields.get(fieldMapping).getValue().toString());
					for (int i = 0; i < array.size(); i++) {
						jsonObject = (org.json.simple.JSONObject) parser.parse(array.get(i).toString());
						fetchedValueFromJson = jsonObject.get(JiraConstants.VALUE).toString();
					}
					if (jiraTestValue.contains(fetchedValueFromJson)) {
						return fetchedValueFromJson;
					}
				}
			}

		} catch (JSONException | org.json.simple.parser.ParseException e) {
			log.error(ERROR_PARSING_TEST_AUTOMATED_FIELD, e);
		}
		return fetchedValueFromJson;
	}

	protected Map<String, String> processMap(Map<String, String> labelMap, Map<String, String> customfieldMap) {
		Map<String, String> resultMap = new HashMap<>();
		Set<String> set = new HashSet<>();
		set.add(AUTOMATED_VALUE);
		set.add(TEST_AUTOMATED_FLAG);
		set.add(TEST_CAN_BE_AUTOMATED_FLAG);
		set.stream().forEach(entry -> {
			if (labelMap != null && labelMap.get(entry) != null) {
				resultMap.put(entry, labelMap.get(entry));
			} else if (customfieldMap != null && customfieldMap.get(entry) != null) {
				resultMap.put(entry, customfieldMap.get(entry));
			}
		});
		return resultMap;
	}

	/**
	 * Sets Story Link with Defect
	 *
	 * @param issue
	 * @param testCaseDetail
	 */
	private void setStoryLinkWithDefect(Issue issue, TestCaseDetails testCaseDetail) {
		if (null != issue.getIssueLinks()) {
			Set<String> defectStorySet = new HashSet<>();
			if (CollectionUtils.isNotEmpty(jiraTestProcessorConfig.getExcludeLinks())) {
				for (IssueLink issueLink : issue.getIssueLinks()) {
					if (!jiraTestProcessorConfig.getExcludeLinks().stream()
							.anyMatch(issueLink.getIssueLinkType().getDescription()::equalsIgnoreCase)) {
						defectStorySet.add(issueLink.getTargetIssueKey());
					}
				}
			}
			testCaseDetail.setDefectStoryID(defectStorySet);
		}
	}

	private Map<String, LocalDateTime> getLastChangedDatesByIssueType(ProjectConfFieldMapping projectConfig) {

		String[] jiraIssueTypeNames = projectConfig.getProcessorToolConnection().getJiraTestCaseType();
		Set<String> uniqueIssueTypes = new HashSet<>(Arrays.asList(jiraIssueTypeNames));

		Map<String, LocalDateTime> lastUpdatedDateByIssueType = new HashMap<>();

		List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService
				.getTraceLogs(ProcessorConstants.JIRA_TEST, projectConfig.getBasicProjectConfigId().toHexString());
		ProcessorExecutionTraceLog projectTraceLog = null;

		if (CollectionUtils.isNotEmpty(traceLogs)) {
			projectTraceLog = traceLogs.get(0);
		}
		LocalDateTime configuredStartDate = LocalDateTime.parse(jiraTestProcessorConfig.getStartDate(),
				DateTimeFormatter.ofPattern(JiraConstants.SETTING_TEST_CASE_START_DATE_FORMAT));

		for (String issueType : uniqueIssueTypes) {

			if (projectTraceLog != null) {
				Map<String, LocalDateTime> lastSavedEntryUpdatedDateByType = projectTraceLog
						.getLastSavedEntryUpdatedDateByType();
				if (MapUtils.isNotEmpty(lastSavedEntryUpdatedDateByType)) {
					LocalDateTime maxDate = lastSavedEntryUpdatedDateByType.get(issueType);
					lastUpdatedDateByIssueType.put(issueType, maxDate != null ? maxDate : configuredStartDate);
				} else {
					lastUpdatedDateByIssueType.put(issueType, configuredStartDate);
				}

			} else {
				lastUpdatedDateByIssueType.put(issueType, configuredStartDate);
			}
		}

		return lastUpdatedDateByIssueType;
	}

	public ProcessorJiraRestClient getProcessorJiraRestClient(ProjectConfFieldMapping projectConfFieldMapping) {
		Optional<Connection> connection = connectionRepository
				.findById(projectConfFieldMapping.getProcessorToolConnection().getConnectionId());
		String username = "";
		String password = "";
		if (connection.isPresent()) {
			Connection conn = connection.get();
			if (conn.isVault()) {
				ToolCredential toolCredential = toolCredentialProvider.findCredential(conn.getUsername());
				if (toolCredential != null) {
					username = toolCredential.getUsername();
					password = toolCredential.getPassword();
					client = jiraRestClientFactory
							.getJiraClient(JiraInfo.builder().jiraConfigBaseUrl(conn.getBaseUrl()).username(username)
									.password(password).jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build());
				}
			} else if (conn.getIsOAuth()) {
				username = conn.getUsername();
				password = decryptJiraPassword(conn.getPassword());
				client = jiraRestClientFactory
						.getJiraOAuthClient(JiraInfo.builder().jiraConfigBaseUrl(conn.getBaseUrl()).username(username)
								.password(password).jiraConfigAccessToken(conn.getAccessToken())
								.jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build());
			} else if (conn.isJaasKrbAuth()) {
				krb5Client = new KerberosClient(conn.getJaasConfigFilePath(), conn.getKrb5ConfigFilePath(),
						conn.getJaasUser(), conn.getSamlEndPoint(), conn.getBaseUrl());
				client = jiraRestClientFactory.getSpnegoSamlClient(krb5Client);
			} else {
				username = conn.getUsername();
				password = decryptJiraPassword(conn.getPassword());
				client = jiraRestClientFactory
						.getJiraClient(JiraInfo.builder().jiraConfigBaseUrl(conn.getBaseUrl()).username(username)
								.password(password).jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build());
			}
		}
		return client;
	}

	private String decryptJiraPassword(String encryptedPassword) {
		return aesEncryptionService.decrypt(encryptedPassword, jiraTestProcessorConfig.getAesEncryptionKey());
	}

	@Override
	public SearchResult getIssues(ProjectConfFieldMapping projectConfig,
			Map<String, LocalDateTime> startDateTimeByIssueType, String userTimeZone, int pageStart, boolean dataExist,
			ProcessorJiraRestClient client) {
		SearchResult searchResult = null;
		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else {
			String query = StringUtils.EMPTY;
			try {
				Map<String, String> startDateTimeStrByIssueType = new HashMap<>();

				startDateTimeByIssueType.forEach((type, localDateTime) -> {
					ZonedDateTime zonedDateTime;
					if (StringUtils.isNotEmpty(userTimeZone)) {
						zonedDateTime = localDateTime.atZone(ZoneId.of(userTimeZone));
					} else {
						zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
					}
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
					String dateTimeStr = zonedDateTime.format(formatter);
					startDateTimeStrByIssueType.put(type, dateTimeStr);

				});

				query = JiraProcessorUtil.createJql(projectConfig.getProjectKey(), startDateTimeStrByIssueType,
						projectConfig);
				log.info("jql= " + query);
				Instant start = Instant.now();

				Promise<SearchResult> promisedRs = client.getProcessorSearchClient().searchJql(query,
						jiraTestProcessorConfig.getPageSize(), pageStart, JiraConstants.ISSUE_FIELD_SET);
				searchResult = promisedRs.claim();
				Instant finish = Instant.now();
				long timeElapsed = Duration.between(start, finish).toMillis();
				log.info("Time taken to fetch the data is {} milliseconds", timeElapsed);
				if (searchResult != null) {
					log.info("Processing issues {} - {} out of {}", pageStart,
							Math.min(pageStart + getPageSize() - 1, searchResult.getTotal()), searchResult.getTotal());
				}
			} catch (RestClientException e) {
				if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 401) {
					log.error(ERROR_MSG_401);
				} else {
					log.info(NO_RESULT_QUERY, query);
					log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, e.getCause());
				}
			}
		}

		return searchResult;
	}

	@Override
	public int getPageSize() {
		return jiraTestProcessorConfig.getPageSize();
	}

	/**
	 * Gets the timeZone of user who is logged in jira
	 *
	 * @param projectConfig
	 *            user provided project configuration
	 * @return String of UserTimeZone
	 */

	public String getUserTimeZone(ProjectConfFieldMapping projectConfig) {
		String userTimeZone = StringUtils.EMPTY;
		try {
			ProcessorToolConnection processorToolConnection = projectConfig.getProcessorToolConnection();
			URL url = getUrl(processorToolConnection);
			URLConnection connection;

			connection = url.openConnection();
			userTimeZone = getUserTimeZone(getDataFromServer(processorToolConnection, (HttpURLConnection) connection));

		} catch (RestClientException rce) {
			log.error("Client exception when loading statuses", rce);
			throw rce;
		} catch (MalformedURLException mfe) {
			log.error("Malformed url for loading statuses", mfe);
		} catch (IOException ioe) {
			log.error("IOException", ioe);
		}

		return userTimeZone;
	}

	/**
	 * Gets Url constructed using user provided details
	 *
	 * @param processorToolConnection
	 *            user provided project tool configure
	 * @return URL
	 * @throws MalformedURLException
	 *             when URL not constructed properly
	 */
	private URL getUrl(ProcessorToolConnection processorToolConnection) throws MalformedURLException {

		boolean isCloudEnv = processorToolConnection.isCloudEnv();
		String serverURL = jiraTestProcessorConfig.getJiraServerGetUserApi();
		if (isCloudEnv) {
			serverURL = jiraTestProcessorConfig.getJiraCloudGetUserApi();
		}

		String baseUrl = processorToolConnection.getUrl();
		String apiEndPoint = processorToolConnection.getApiEndPoint();

		return new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + apiEndPoint
				+ (apiEndPoint.endsWith("/") ? "" : "/") + serverURL + processorToolConnection.getUsername());

	}

	@SuppressWarnings("unchecked")
	private String getUserTimeZone(String timezoneObj) {
		String userTimeZone = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(timezoneObj)) {
			try {
				Object obj = new JSONParser().parse(timezoneObj);
				JSONArray userInfoList = new JSONArray();
				userInfoList.add(obj);
				for (Object userInfo : userInfoList) {
					JSONArray jsonUserInfo = (JSONArray) userInfo;
					for (Object timeZone : jsonUserInfo) {
						JSONObject timeZoneObj = (JSONObject) timeZone;
						userTimeZone = (String) timeZoneObj.get("timeZone");
					}
				}

			} catch (ParseException pe) {
				log.error("Parser exception when parsing statuses", pe);
			}
		}
		return userTimeZone;
	}

	private String getDataFromServer(ProcessorToolConnection processorToolConnection,
			HttpURLConnection httpURLConnection) throws IOException {
		String username = null;
		String password = null;
		Optional<Connection> connectionOptional = connectionRepository
				.findById(processorToolConnection.getConnectionId());
		if (connectionOptional.isPresent() && connectionOptional.map(Connection::isJaasKrbAuth).orElse(false)) {
			HttpUriRequest httpUriRequest = RequestBuilder.get().setUri(httpURLConnection.getURL().toString())
					.setHeader(HttpHeaders.ACCEPT, "application/json")
					.setHeader(HttpHeaders.CONTENT_TYPE, "application/json").build();
			return krb5Client.getResponse(httpUriRequest);
		} else if (connectionOptional.isPresent() && connectionOptional.map(Connection::isBearerToken).orElse(false)) {
			String patOAuthToken = decryptJiraPassword(connectionOptional.get().getPatOAuthToken());
			httpURLConnection.setRequestProperty("Authorization", "Bearer " + patOAuthToken); // NOSONAR
		} else if (connectionOptional.isPresent() && connectionOptional.map(Connection::isVault).orElse(false)) {
			ToolCredential toolCredential = toolCredentialProvider
					.findCredential(connectionOptional.get().getUsername());
			if (toolCredential != null) {
				username = toolCredential.getUsername();
				password = toolCredential.getPassword();
			}
		} else {
			username = connectionOptional.map(Connection::getUsername).orElse(null);
			password = decryptJiraPassword(connectionOptional.map(Connection::getPassword).orElse(null));
			httpURLConnection.setRequestProperty("Authorization",
					"Basic " + encodeCredentialsToBase64(username, password));
		}
		httpURLConnection.connect();
		StringBuilder sb = new StringBuilder();
		try (InputStream in = (InputStream) httpURLConnection.getContent();
				BufferedReader inReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));) {
			int cp;
			while ((cp = inReader.read()) != -1) {
				sb.append((char) cp);
			}
		} catch (IOException ie) {
			log.error("Read exception when connecting to server {}", ie);
		}
		return sb.toString();
	}

	private String encodeCredentialsToBase64(String username, String password) {
		String cred = username + ":" + password;
		return Base64.getEncoder().encodeToString(cred.getBytes());
	}

	/**
	 * Sets the regression labels..
	 *
	 * @param jiraTestToolInfo
	 *            processorToolConnection
	 * @param customFieldMap
	 *            map of custom fields
	 * @param testCaseDetails
	 *            scrum test case
	 */
	private void setRegressionLabel(ProcessorToolConnection jiraTestToolInfo, Map<String, IssueField> customFieldMap,
			TestCaseDetails testCaseDetails) {
		if (CollectionUtils.isNotEmpty(jiraTestToolInfo.getJiraRegressionTestValue())
				&& (jiraTestToolInfo.getTestRegressionByCustomField() != null)) {
			String regressionLabels = processJsonForCustomFields(jiraTestToolInfo.getTestRegressionByCustomField(),
					customFieldMap, jiraTestToolInfo.getJiraRegressionTestValue());
			if (StringUtils.isNotEmpty(regressionLabels)) {
				Set<String> regressionCustomValueList = new HashSet<>(Arrays.asList(regressionLabels.split(", ")));
				if (CollectionUtils.containsAny(jiraTestToolInfo.getJiraRegressionTestValue(),
						regressionCustomValueList)) {
					if (CollectionUtils.isNotEmpty(testCaseDetails.getLabels())) {
						regressionCustomValueList.addAll(testCaseDetails.getLabels());
					}
					testCaseDetails.setLabels(new ArrayList<>(regressionCustomValueList));
				}
			}
		}
	}

	private int processesJiraIssuesJQL(ProjectConfFieldMapping projectConfig) {
		int savedIssuesCount = 0;
		int total = 0;
		Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType = new HashMap<>();
		setStartDate(jiraTestProcessorConfig);
		ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
				projectConfig.getBasicProjectConfigId().toHexString());
		boolean processorFetchingComplete = false;
		try {
			client = getProcessorJiraRestClient(projectConfig);
			boolean dataExist = (testCaseDetailsRepository
					.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
			Map<String, LocalDateTime> maxChangeDatesByIssueType = getLastChangedDatesByIssueType(projectConfig);
			Map<String, LocalDateTime> maxChangeDatesByIssueTypeWithAddedTime = new HashMap<>();
			maxChangeDatesByIssueType.forEach((k, v) -> {
				long extraMinutes = jiraTestProcessorConfig.getMinsToReduce();
				maxChangeDatesByIssueTypeWithAddedTime.put(k, v.minusMinutes(extraMinutes));
			});
			int pageSize = getPageSize();
			String userTimeZone = getUserTimeZone(projectConfig);
			for (int i = 0; true; i += pageSize) {
				SearchResult searchResult = getIssues(projectConfig, maxChangeDatesByIssueTypeWithAddedTime,
						userTimeZone, i, dataExist, client);
				List<Issue> issues = getIssuesFromResult(searchResult);
				if (total == 0) {
					total = getTotal(searchResult);
				}
				// in case of offline method issues size can be greater than
				// pageSize, increase page size so that same issues not read
				// if (issues.size() >= pageSize) {
				// pageSize = issues.size() + 1;
				// }
				if (CollectionUtils.isNotEmpty(issues)) {
					List<TestCaseDetails> testCaseDetailsList = prepareTestCaseDetails(issues, projectConfig);
					testCaseDetailsRepository.saveAll(testCaseDetailsList);
					findLastSavedTestCaseDetailsByType(testCaseDetailsList, lastSavedJiraIssueChangedDateByType);
					savedIssuesCount += issues.size();
				}
				MDC.put("JiraTimeZone", String.valueOf(userTimeZone));
				MDC.put("IssueCount", String.valueOf(issues.size()));
				// will result in an extra call if number of results == pageSize
				// but I would rather do that then complicate the jira client
				// implementation
				if (issues.size() < pageSize) {
					break;
				}
				TimeUnit.MILLISECONDS.sleep(jiraTestProcessorConfig.getSubsequentApiCallDelayInMilli());
			}
			processorFetchingComplete = true;
		} catch (JSONException e) {
			log.error("Error while updating Story information in scrum client", e);
			lastSavedJiraIssueChangedDateByType.clear();
		} catch (InterruptedException e) {
			log.error("Interrupted exception thrown.", e);
			lastSavedJiraIssueChangedDateByType.clear();
			Thread.currentThread().interrupt();
		} finally {
			boolean isAttemptSuccess = isAttemptSuccess(total, savedIssuesCount, processorFetchingComplete);
			if (!isAttemptSuccess) {
				lastSavedJiraIssueChangedDateByType.clear();
				processorExecutionTraceLog.setLastSuccessfulRun(null);
				log.error("Error in Fetching Issues through JQL");
			} else {
				processorExecutionTraceLog
						.setLastSuccessfulRun(DateUtil.dateTimeFormatter(LocalDateTime.now(), DATE_TIME_FORMAT));
			}
			saveExecutionTraceLog(processorExecutionTraceLog, lastSavedJiraIssueChangedDateByType, isAttemptSuccess);
		}

		return savedIssuesCount;
	}

	public void setStartDate(JiraTestProcessorConfig jiraTestProcessorConfig) {
		LocalDateTime localDateTime = null;
		if (jiraTestProcessorConfig.isConsiderStartDate()) {
			try {
				localDateTime = DateUtil.stringToLocalDateTime(jiraTestProcessorConfig.getStartDate(),
						JiraConstants.SETTING_TEST_CASE_START_DATE_FORMAT);
			} catch (DateTimeParseException ex) {
				log.error("exception while parsing start date provided from property file picking last 12 months data.."
						+ ex.getMessage());
				localDateTime = LocalDateTime.now().minusMonths(jiraTestProcessorConfig.getPrevMonthCountToFetchData());
			}
		} else {
			localDateTime = LocalDateTime.now().minusMonths(jiraTestProcessorConfig.getPrevMonthCountToFetchData());
		}
		jiraTestProcessorConfig.setStartDate(
				DateUtil.dateTimeFormatter(localDateTime, JiraConstants.SETTING_TEST_CASE_START_DATE_FORMAT));
	}

	public SearchResult getIssues(ProjectConfFieldMapping projectConfig,
			Map<String, LocalDateTime> startDateTimeByIssueType, String userTimeZone, int pageStart, boolean dataExist)
			throws InterruptedException {
		SearchResult searchResult = null;
		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else if (StringUtils.isEmpty(projectConfig.getProcessorToolConnection().getProjectKey())
				|| StringUtils.isEmpty(projectConfig.getProcessorToolConnection().getBoardQuery())) {
			log.info("Either Project key is empty or boardQuery not provided. key {} boardquery {}",
					projectConfig.getProcessorToolConnection().getProjectKey(),
					projectConfig.getProcessorToolConnection().getBoardQuery());
		} else {
			StringBuilder query = new StringBuilder("project in (")
					.append(projectConfig.getProcessorToolConnection().getProjectKey()).append(") AND ");
			try {
				Map<String, String> startDateTimeStrByIssueType = new HashMap<>();

				startDateTimeByIssueType.forEach((type, localDateTime) -> {
					ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(userTimeZone));
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
					String dateTimeStr = zonedDateTime.format(formatter);
					startDateTimeStrByIssueType.put(type, dateTimeStr);

				});
				query.append(JiraProcessorUtil.processJql(projectConfig.getProcessorToolConnection().getBoardQuery(),
						startDateTimeStrByIssueType, dataExist, projectConfig));
				Instant start = Instant.now();
				Promise<SearchResult> promisedRs = client.getProcessorSearchClient().searchJql(query.toString(),
						jiraTestProcessorConfig.getPageSize(), pageStart, ISSUE_FIELD_SET);
				searchResult = promisedRs.claim();
				log.debug("jql query processed for JQL");
				if (searchResult != null) {
					log.info(String.format(PROCESSING_ISSUES_PRINT_LOG, pageStart,
							Math.min(pageStart + getPageSize() - 1, searchResult.getTotal()), searchResult.getTotal()));
				}
				TimeUnit.MILLISECONDS.sleep(jiraTestProcessorConfig.getSubsequentApiCallDelayInMilli());
			} catch (RestClientException e) {
				if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 401) {
					log.error(ERROR_MSG_401);
				} else {
					log.info(NO_RESULT_QUERY, query);
					log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, e.getCause());
				}
			}

		}

		return searchResult;
	}
}
