package com.publicissapient.kpidashboard.jira.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.client.CustomAsynchronousIssueRestClient;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.helper.JiraHelper;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JiraCommonService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JiraCommonService.class);

	String QUERYDATEFORMAT = "yyyy-MM-dd HH:mm";
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
	private static final String MSG_JIRA_CLIENT_SETUP_FAILED = "Jira client setup failed. No results obtained. Check your jira setup.";
	private static final String ERROR_MSG_401 = "Error 401 connecting to JIRA server, your credentials are probably wrong. Note: Ensure you are using JIRA user name not your email address.";
	private static final String ERROR_MSG_NO_RESULT_WAS_AVAILABLE = "No result was available from Jira unexpectedly - defaulting to blank response. The reason for this fault is the following : {}";
	private static final String NO_RESULT_QUERY = "No result available for query: {}";

	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	PSLogData psLogData = new PSLogData();

	private ProcessorJiraRestClient client;

	@Autowired
	private KanbanJiraIssueRepository kanbanJiraRepo;

	@Autowired
	ValidateData validateData;

	@Autowired
	private ToolCredentialProvider toolCredentialProvider;

	@Autowired
	private AesEncryptionService aesEncryptionService;

	@Autowired
	private KanbanJiraIssueHistoryRepository kanbanIssueHistoryRepo;

	public int getPageSize() {
		return jiraProcessorConfig.getPageSize();
	}

	public String getUserTimeZone(ProjectConfFieldMapping projectConfig, KerberosClient krb5Client) {
		String userTimeZone = StringUtils.EMPTY;
		try {
			JiraToolConfig jiraToolConfig = projectConfig.getJira();

			if (null != jiraToolConfig) {
				Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
				String username = connectionOptional.map(Connection::getUsername).orElse(null);
				URL url = getUrl(projectConfig, username);

				userTimeZone = parseUserTimeZone(getDataFromClient(projectConfig, url, krb5Client));
			}
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

	public String parseUserTimeZone(String timezoneObj) {
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

	private URL getUrl(ProjectConfFieldMapping projectConfig, String jiraUserName) throws MalformedURLException {

		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		boolean isCloudEnv = connectionOptional.map(Connection::isCloudEnv).orElse(false);
		String serverURL = jiraProcessorConfig.getJiraServerGetUserApi();
		if (isCloudEnv) {
			serverURL = jiraProcessorConfig.getJiraCloudGetUserApi();
		}

		String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
		String apiEndPoint = connectionOptional.map(Connection::getApiEndPoint).orElse("");

		return new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + apiEndPoint
				+ (apiEndPoint.endsWith("/") ? "" : "/") + serverURL + jiraUserName);

	}

	public String getDataFromClient(ProjectConfFieldMapping projectConfig, URL url, KerberosClient krb5Client)
			throws IOException {
		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		boolean spenagoClient = connectionOptional.map(Connection::isJaasKrbAuth).orElse(false);
		if (spenagoClient) {
			HttpUriRequest request = RequestBuilder.get().setUri(url.toString())
					.setHeader(org.apache.http.HttpHeaders.ACCEPT, "application/json")
					.setHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, "application/json").build();
			return krb5Client.getResponse(request);
		} else {
			return getDataFromServer(url, connectionOptional);
		}
	}

	public String getDataFromServer(URL url, Optional<Connection> connectionOptional) throws IOException {
		HttpURLConnection request = (HttpURLConnection) url.openConnection();

		String username = null;
		String password = null;

		if (connectionOptional.isPresent()) {
			Connection conn = connectionOptional.get();
			if (conn.isVault()) {
				ToolCredential toolCredential = toolCredentialProvider.findCredential(conn.getUsername());
				if (toolCredential != null) {
					username = toolCredential.getUsername();
					password = toolCredential.getPassword();
				}

			} else {
				username = connectionOptional.map(Connection::getUsername).orElse(null);
				password = decryptJiraPassword(connectionOptional.map(Connection::getPassword).orElse(null));
			}
		}
		request.setRequestProperty("Authorization", "Basic " + encodeCredentialsToBase64(username, password)); // NOSONAR
		request.connect();
		StringBuilder sb = new StringBuilder();
		try (InputStream in = (InputStream) request.getContent();
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

	public String decryptJiraPassword(String encryptedPassword) {
		return aesEncryptionService.decrypt(encryptedPassword, jiraProcessorConfig.getAesEncryptionKey());
	}

	public String encodeCredentialsToBase64(String username, String password) {
		String cred = username + ":" + password;
		return Base64.getEncoder().encodeToString(cred.getBytes());
	}

	public boolean cleanCache() {
		boolean accountHierarchyCleaned = cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.CACHE_ACCOUNT_HIERARCHY);
		boolean kpiDataCleaned = cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JIRA_KPI_CACHE);
		return accountHierarchyCleaned && kpiDataCleaned;
	}

	public boolean cacheRestClient(String cacheEndPoint, String cacheName) {
		boolean cleaned = false;
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(jiraProcessorConfig.getCustomApiBaseUrl());
		uriBuilder.path("/");
		uriBuilder.path(cacheEndPoint);
		uriBuilder.path("/");
		uriBuilder.path(cacheName);

		HttpEntity<?> entity = new HttpEntity<>(headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, String.class);
		} catch (RuntimeException e) {
			LOGGER.error("[JIRA-CUSTOMAPI-CACHE-EVICT]. Error while consuming rest service", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			cleaned = true;
			LOGGER.info("[JIRA-CUSTOMAPI-CACHE-EVICT]. Successfully evicted cache {}", cacheName);
		} else {
			LOGGER.error("[JIRA-CUSTOMAPI-CACHE-EVICT]. Error while evicting cache {}", cacheName);
		}
		return cleaned;
	}

	// public void savingIssueLogs(int savedIssuesCount, List<JiraIssue>
	// jiraIssues, Instant startProcessingJiraIssues,
	// boolean isEpic, PSLogData psLogData) {
	// PSLogData saveIssueLog = new PSLogData();
	// saveIssueLog.setIssueAndDesc(jiraIssues.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
	// saveIssueLog.setTotalSavedIssues(String.valueOf(savedIssuesCount));
	// psLogData.setTotalSavedIssues(String.valueOf(savedIssuesCount));
	// psLogData.setTimeTaken(String.valueOf(Duration.between(startProcessingJiraIssues,
	// Instant.now()).toMillis()));
	// psLogData.setSprintListFetched(null);
	// psLogData.setTotalFetchedSprints(null);
	// if (!isEpic) {
	// saveIssueLog.setAction(CommonConstant.SAVED_ISSUES);
	// psLogData.setAction(CommonConstant.SAVED_ISSUES);
	// saveIssueLog.setTotalFetchedIssues(psLogData.getTotalFetchedIssues());
	// log.debug("Saved Issues for project {}",
	// MDC.get(CommonConstant.PROJECTNAME),
	// kv(CommonConstant.PSLOGDATA, saveIssueLog));
	// log.info("Processed Issues for project {}",
	// MDC.get(CommonConstant.PROJECTNAME),
	// kv(CommonConstant.PSLOGDATA, psLogData));
	// } else {
	// saveIssueLog.setAction(CommonConstant.SAVED_EPIC_ISSUES);
	// psLogData.setAction(CommonConstant.SAVED_EPIC_ISSUES);
	// saveIssueLog.setEpicIssuesFetched(psLogData.getEpicIssuesFetched());
	// log.debug("Saved Epic Issues for project {}",
	// MDC.get(CommonConstant.PROJECTNAME),
	// kv(CommonConstant.PSLOGDATA, saveIssueLog));
	// log.info("Processed Epic Issues for project {}",
	// MDC.get(CommonConstant.PROJECTNAME),
	// kv(CommonConstant.PSLOGDATA, psLogData));
	//
	// }
	// }

	public ProcessorExecutionTraceLog createTraceLog(ProjectConfFieldMapping projectConfig) {
		List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService
				.getTraceLogs(ProcessorConstants.JIRA, projectConfig.getBasicProjectConfigId().toHexString());
		ProcessorExecutionTraceLog processorExecutionTraceLog = null;

		if (CollectionUtils.isNotEmpty(traceLogs)) {
			processorExecutionTraceLog = traceLogs.get(0);
			if (null == processorExecutionTraceLog.getLastSuccessfulRun() || projectConfig.getProjectBasicConfig()
					.isSaveAssigneeDetails() != processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
				processorExecutionTraceLog.setLastSuccessfulRun(jiraProcessorConfig.getStartDate());
			}
		} else {
			processorExecutionTraceLog = new ProcessorExecutionTraceLog();
			processorExecutionTraceLog.setProcessorName(ProcessorConstants.JIRA);
			processorExecutionTraceLog.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toHexString());
			processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
			processorExecutionTraceLog.setLastSuccessfulRun(jiraProcessorConfig.getStartDate());
		}
		return processorExecutionTraceLog;
	}

	public KanbanJiraIssue findOneKanbanIssueRepo(String issueId, String basicProjectConfigId) {
		List<KanbanJiraIssue> jiraIssues = kanbanJiraRepo
				.findByIssueIdAndBasicProjectConfigId(StringEscapeUtils.escapeHtml4(issueId), basicProjectConfigId);

		// Not sure of the state of the data
		if (jiraIssues.size() > 1) {
			log.warn("JIRA Processor | More than one collector item found for scopeId {}", issueId);
		}

		if (!jiraIssues.isEmpty()) {
			return jiraIssues.get(0);
		}

		return null;
	}

	/**
	 * Find kanban Jira Issue custom history object by issueId
	 *
	 * @param issueId
	 *            Jira issue ID
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return KanbanIssueCustomHistory Kanban history object corresponding to
	 *         issueId from DB
	 */
	public KanbanIssueCustomHistory findOneKanbanIssueCustomHistory(String issueId, String basicProjectConfigId) {
		List<KanbanIssueCustomHistory> jiraIssues = kanbanIssueHistoryRepo.findByStoryIDAndBasicProjectConfigId(issueId,
				basicProjectConfigId);
		// Not sure of the state of the data
		if (jiraIssues.size() > 1) {
			log.warn("JIRA Processor | Data issue More than one JIRA issue item found for id {}", issueId);
		}
		if (!jiraIssues.isEmpty()) {
			return jiraIssues.get(0);
		}

		return null;
	}

	public List<Issue> fetchIssues(Map.Entry<String, ProjectConfFieldMapping> entry,
			ProcessorJiraRestClient clientIncoming, KerberosClient krb5Client, boolean dataExist) throws JSONException {

		List<Issue> totalIssues = new ArrayList<>();
		ProjectConfFieldMapping projectConfig = entry.getValue();

		PSLogData psLogData = new PSLogData();
		psLogData.setProjectName(projectConfig.getProjectName());
		int total = 0;
		int savedIsuesCount = 0;

		client = clientIncoming;

		Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType = new HashMap<>();
		JiraHelper.setStartDate(jiraProcessorConfig);
		boolean processorFetchingComplete = false;
		ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(projectConfig);
		try {
			if (projectConfig.isKanban()) {
				dataExist = (kanbanJiraRepo
						.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
				psLogData.setKanban("true");
			} else {
				dataExist = (jiraIssueRepository
						.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
				psLogData.setKanban("false");
			}

			Map<String, LocalDateTime> maxChangeDatesByIssueType = getLastChangedDatesByIssueType(projectConfig);

			Map<String, LocalDateTime> maxChangeDatesByIssueTypeWithAddedTime = new HashMap<>();

			maxChangeDatesByIssueType.forEach((k, v) -> {
				long extraMinutes = jiraProcessorConfig.getMinsToReduce();
				maxChangeDatesByIssueTypeWithAddedTime.put(k, v.minusMinutes(extraMinutes));
			});

			int pageSize = getPageSize();
			boolean hasMore = true;
			String userTimeZone = getUserTimeZone(projectConfig, krb5Client);

			int sprintCount = jiraProcessorConfig.getSprintCountForCacheClean();
			boolean latestDataFetched = false;
			Set<SprintDetails> setForCacheClean = new HashSet<>();

			for (int i = 0; hasMore; i += pageSize) {
				Instant startProcessingJiraIssues = Instant.now();
				SearchResult searchResult = getIssues(entry, maxChangeDatesByIssueTypeWithAddedTime, userTimeZone, i,
						dataExist);
				List<Issue> issues = JiraHelper.getIssuesFromResult(searchResult);
				totalIssues.addAll(issues);
				if (total == 0) {
					total = JiraHelper.getTotal(searchResult);
					psLogData.setTotalFetchedIssues(String.valueOf(total));
				}

				// if (CollectionUtils.isNotEmpty(issues)) {
				// List<JiraIssueCustomHistory> jiraIssueHistoryToSave = new
				// ArrayList<>();
				// Set<SprintDetails> sprintDetailsSet=new HashSet<>();
				// Set<Assignee> assigneeSetToSave = new HashSet<>();
				// boolean dataFromBoard =false;
				// List<JiraIssue> jiraIssues =
				// transformFetchedIssue.convertToJiraIssue(issues,
				// projectConfig, dataFromBoard,
				// jiraIssueHistoryToSave,sprintDetailsSet,assigneeSetToSave);
				// Set<AccountHierarchy>
				// createAccountHierarchySet=createAccountHierarchy.createAccountHierarchy(jiraIssues,projectConfig);
				// List<SprintDetails> sprintDetailsList =new ArrayList<>();
				// //now we will be putting setCacheClean in fetchSprints fn
				// if (!dataFromBoard) {
				// sprintDetailsList=fetchSprintReport.fetchSprints(projectConfig,sprintDetailsSet,setForCacheClean,krb5Client);
				// }
				// AssigneeDetails
				// assigneeDetails=createAssigneeDetails.createAssigneeDetails(projectConfig,assigneeSetToSave);
				// saveData.saveData(jiraIssues,jiraIssueHistoryToSave,sprintDetailsList,createAccountHierarchySet,assigneeDetails,null,null,null);
				// JiraHelper.findLastSavedJiraIssueByType(jiraIssues,lastSavedJiraIssueChangedDateByType);
				// savedIsuesCount += issues.size();
				// savingIssueLogs(savedIsuesCount, jiraIssues,
				// startProcessingJiraIssues,false,psLogData);
				// }

				if (!dataExist && !latestDataFetched && setForCacheClean.size() > sprintCount) {
					latestDataFetched = cleanCache();
					setForCacheClean.clear();
					log.info("latest sprint fetched cache cleaned.");
				}

				if (issues.size() < pageSize) {
					break;
				}
			}
			processorFetchingComplete = true;
		}
		// catch (JSONException e) {
		// log.error("Error while updating Story information in scrum client",
		// e,
		// kv(CommonConstant.PSLOGDATA, psLogData));
		// lastSavedJiraIssueChangedDateByType.clear();
		// processorFetchingComplete = false;
		// }
		catch (InterruptedException e) {
			log.error("Interrupted exception thrown.", e, kv(CommonConstant.PSLOGDATA, psLogData));
			lastSavedJiraIssueChangedDateByType.clear();
			processorFetchingComplete = false;
		} finally {
			validateData.check(total, savedIsuesCount, processorFetchingComplete, psLogData,
					lastSavedJiraIssueChangedDateByType, projectConfig, processorExecutionTraceLog);
		}
		return totalIssues;
	}

	private Map<String, LocalDateTime> getLastChangedDatesByIssueType(ProjectConfFieldMapping projectConfig) {
		ObjectId basicProjectConfigId = projectConfig.getBasicProjectConfigId();
		FieldMapping fieldMapping = projectConfig.getFieldMapping();
		ProjectBasicConfig projectBasicConfig = projectConfig.getProjectBasicConfig();

		String[] jiraIssueTypeNames = fieldMapping.getJiraIssueTypeNames();
		Set<String> uniqueIssueTypes = new HashSet<>(Arrays.asList(jiraIssueTypeNames));

		Map<String, LocalDateTime> lastUpdatedDateByIssueType = new HashMap<>();

		List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService
				.getTraceLogs(ProcessorConstants.JIRA, basicProjectConfigId.toHexString());
		ProcessorExecutionTraceLog projectTraceLog = null;

		if (CollectionUtils.isNotEmpty(traceLogs)) {
			projectTraceLog = traceLogs.get(0);
		}
		LocalDateTime configuredStartDate = LocalDateTime.parse(jiraProcessorConfig.getStartDate(),
				DateTimeFormatter.ofPattern(QUERYDATEFORMAT));

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

				// When toggle is On first time it will update
				// lastUpdatedDateByIssueType to start date
				JiraHelper.setLastUpdatedDateToStartDate(projectBasicConfig, lastUpdatedDateByIssueType,
						projectTraceLog, configuredStartDate, issueType);

			} else {
				lastUpdatedDateByIssueType.put(issueType, configuredStartDate);
			}

		}

		return lastUpdatedDateByIssueType;
	}

	private SearchResult getIssues(Map.Entry<String, ProjectConfFieldMapping> entry,
			Map<String, LocalDateTime> startDateTimeByIssueType, String userTimeZone, int pageStart, boolean dataExist)
			throws InterruptedException {
		ProjectConfFieldMapping projectConfig = entry.getValue();
		SearchResult searchResult = null;

		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else if (StringUtils.isEmpty(projectConfig.getProjectToolConfig().getProjectKey())
				|| StringUtils.isEmpty(projectConfig.getProjectToolConfig().getBoardQuery())) {
			log.info("Either Project key is empty or boardQuery not provided. key {} boardquery {}",
					projectConfig.getProjectToolConfig().getProjectKey(),
					projectConfig.getProjectToolConfig().getBoardQuery());
		} else {
			StringBuilder query = new StringBuilder("project in (")
					.append(projectConfig.getProjectToolConfig().getProjectKey()).append(") AND ");
			try {
				Map<String, String> startDateTimeStrByIssueType = new HashMap<>();

				startDateTimeByIssueType.forEach((type, localDateTime) -> {
					ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(userTimeZone));
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
					String dateTimeStr = zonedDateTime.format(formatter);
					startDateTimeStrByIssueType.put(type, dateTimeStr);

				});

				query.append(JiraProcessorUtil.processJql(projectConfig.getJira().getBoardQuery(),
						startDateTimeStrByIssueType, dataExist));
				psLogData.setUserTimeZone(userTimeZone);
				psLogData.setSprintId(null);
				psLogData.setJql(query.toString());
				Instant start = Instant.now();
				Promise<SearchResult> promisedRs = client.getProcessorSearchClient().searchJql(query.toString(),
						jiraProcessorConfig.getPageSize(), pageStart, JiraConstants.ISSUE_FIELD_SET);
				searchResult = promisedRs.claim();
				psLogData.setTimeTaken(String.valueOf(Duration.between(start, Instant.now()).toMillis()));
				log.debug("jql query processed for JQL", kv(CommonConstant.PSLOGDATA, psLogData));
				if (searchResult != null) {
					psLogData.setTotalFetchedIssues(String.valueOf(searchResult.getTotal()));
					psLogData.setAction(CommonConstant.FETCHING_ISSUE);
					log.info(String.format("Processing issues %d - %d out of %d", pageStart,
							Math.min(pageStart + getPageSize() - 1, searchResult.getTotal()), searchResult.getTotal()),
							kv(CommonConstant.PSLOGDATA, psLogData));
				}
				TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
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

	public List<Issue> fetchIssueBasedOnBoard(Map.Entry<String, ProjectConfFieldMapping> entry,
			ProcessorJiraRestClient clientIncoming, KerberosClient krb5Client, boolean dataExist,
			Set<SprintDetails> setForCacheClean) {

		List<Issue> totalIssues = new ArrayList<>();
		ProjectConfFieldMapping projectConfig = entry.getValue();

		PSLogData psLogData = new PSLogData();
		psLogData.setProjectName(projectConfig.getProjectName());
		int total = 0;
		int savedIsuesCount = 0;

		Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType = new HashMap<>();
		JiraHelper.setStartDate(jiraProcessorConfig);
		boolean processorFetchingComplete = false;
		client = clientIncoming;
		ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(projectConfig);

		try {

			// write get logic to fetch last successful updated date.
			String queryDate = JiraHelper.getDeltaDate(processorExecutionTraceLog.getLastSuccessfulRun());
			String userTimeZone = getUserTimeZone(projectConfig, krb5Client);
			List<BoardDetails> boardDetailsList = projectConfig.getProjectToolConfig().getBoards();

			int sprintCount = jiraProcessorConfig.getSprintCountForCacheClean();
			boolean latestDataFetched = false;

			for (BoardDetails board : boardDetailsList) {
				psLogData.setBoardId(board.getBoardId());
				int boardTotal = 0;
				int pageSize = getPageSize();
				boolean hasMore = true;
				for (int i = 0; hasMore; i += pageSize) {

					SearchResult searchResult = getIssues(board, projectConfig, queryDate, userTimeZone, i, dataExist);
					List<Issue> issues = JiraHelper.getIssuesFromResult(searchResult);
					totalIssues.addAll(issues);
					if (boardTotal == 0) {
						boardTotal = JiraHelper.getTotal(searchResult);
						total += boardTotal;
						psLogData.setTotalFetchedIssues(String.valueOf(total));
					}
					if (!dataExist && !latestDataFetched && setForCacheClean.size() > sprintCount) {
						latestDataFetched = cleanCache();
						setForCacheClean.clear();
						log.info("latest sprint fetched cache cleaned.");
					}

					if (issues.size() < pageSize) {
						break;
					}
					TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
				}
			}
			processorFetchingComplete = true;
		} catch (InterruptedException e) {
			log.error("Interrupted exception thrown.", e, kv(CommonConstant.PSLOGDATA, psLogData));
			lastSavedJiraIssueChangedDateByType.clear();
			processorFetchingComplete = false;
		} finally {
			validateData.check(total, savedIsuesCount, processorFetchingComplete, psLogData,
					lastSavedJiraIssueChangedDateByType, projectConfig, processorExecutionTraceLog);
		}
		return totalIssues;
	}

	public SearchResult getIssues(BoardDetails boardDetails, ProjectConfFieldMapping projectConfig,
			String startDateTimeByIssueType, String userTimeZone, int pageStart, boolean dataExist)
			throws InterruptedException {
		SearchResult searchResult = null;

		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else {
			String query = StringUtils.EMPTY;
			try {
				query = "updatedDate>='" + startDateTimeByIssueType + "' order by updatedDate desc";
				psLogData.setUserTimeZone(userTimeZone);
				psLogData.setJql(query);
				psLogData.setBoardId(boardDetails.getBoardId());
				Instant start = Instant.now();
				CustomAsynchronousIssueRestClient issueRestClient = client.getCustomIssueClient();
				Promise<SearchResult> promisedRs = issueRestClient.searchBoardIssue(boardDetails.getBoardId(), query,
						jiraProcessorConfig.getPageSize(), pageStart, JiraConstants.ISSUE_FIELD_SET);
				searchResult = promisedRs.claim();
				psLogData.setTimeTaken(String.valueOf(Duration.between(start, Instant.now()).toMillis()));
				log.debug("jql query processed for board", kv(CommonConstant.PSLOGDATA, psLogData));
				if (searchResult != null) {
					psLogData.setTotalFetchedIssues(String.valueOf(searchResult.getTotal()));
					psLogData.setAction(CommonConstant.FETCHING_ISSUE);
					log.info(String.format("Processing issues %d - %d out of %d", pageStart,
							Math.min(pageStart + getPageSize() - 1, searchResult.getTotal()), searchResult.getTotal()),
							kv(CommonConstant.PSLOGDATA, psLogData));
				}
				TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
			} catch (RestClientException e) {
				if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 401) {
					log.error(ERROR_MSG_401, kv(CommonConstant.PSLOGDATA, psLogData));
				} else {
					log.info(NO_RESULT_QUERY, query, kv(CommonConstant.PSLOGDATA, psLogData));
					log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, e.getCause(), kv(CommonConstant.PSLOGDATA, psLogData));
				}
			}

		}

		return searchResult;
	}
}
