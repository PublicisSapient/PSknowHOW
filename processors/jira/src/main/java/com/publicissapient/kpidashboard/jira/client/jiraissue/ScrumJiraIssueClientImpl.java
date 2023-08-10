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

package com.publicissapient.kpidashboard.jira.client.jiraissue;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.model.jira.ReleaseVersion;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.client.sprint.SprintClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.AdditionalFilterHelper;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This is an implemented/extended storyDataClient for configured Scrum
 * projects, Which extracts the story data using the java JIRA api, and store it
 * in a MongoDB collection for Custom API calls.
 */
@Service
@Slf4j
public class ScrumJiraIssueClientImpl extends JiraIssueClient {// NOPMD

	public static final String FALSE = "false";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private JiraProcessorRepository jiraProcessorRepository;

	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;

	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	private SprintClient sprintClient;

	@Autowired
	private JiraRestClientFactory jiraRestClientFactory;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Autowired
	private HierarchyLevelService hierarchyLevelService;

	@Autowired
	private AdditionalFilterHelper additionalFilterHelper;

	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Autowired
	private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;

	@Autowired
	private HandleJiraHistory handleJiraHistory;

	private static void storyWithSubTaskDefect(Issue issue, Map<String, IssueField> fields,
			Set<String> defectStorySet) {
		String parentKey;
		if (issue.getIssueType().isSubtask() && MapUtils.isNotEmpty(fields)) {

			try {
				parentKey = ((JSONObject) fields.get(JiraConstants.PARENT).getValue()).get(JiraConstants.KEY)
						.toString();
				defectStorySet.add(parentKey);
			} catch (JSONException e) {
				log.error(
						"JIRA Processor | Error while parsing parent value as JSONObject or converting JSONObject to string",
						e);
			}

		}
	}

	/**
	 * Converts iterable to collection
	 *
	 * @param iterable
	 *            Iterable
	 * @param <T>
	 *            type of collection
	 * @return collection of provided type T
	 */
	public static <T> Collection<T> iterableToCollection(Iterable<T> iterable) {
		Collection<T> collection = new ArrayList<>();
		iterable.forEach(collection::add);
		return collection;
	}

	private static void setLastUpdatedDateToStartDate(ProjectBasicConfig projectBasicConfig,
			Map<String, LocalDateTime> lastUpdatedDateByIssueType, ProcessorExecutionTraceLog projectTraceLog,
			LocalDateTime configuredStartDate, String issueType) {
		if (projectBasicConfig.isSaveAssigneeDetails() != projectTraceLog.isLastEnableAssigneeToggleState()) {
			lastUpdatedDateByIssueType.put(issueType, configuredStartDate);
		}
	}

	/**
	 * Explicitly updates queries for the source system, and initiates the update to
	 * MongoDB from those calls.
	 *
	 * @param projectConfig
	 *            Project Configuration Mapping
	 * @param jiraAdapter
	 *            JiraAdapter client
	 * @param isOffline
	 *            offline processor or not
	 * @return Count of Jira Issues processed for scrum project
	 */
	@Override
	public int processesJiraIssues(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter, boolean isOffline) {
		log.info("Start Processing Jira Issues");
		if (projectConfig.getProjectToolConfig().isQueryEnabled()) {
			return processesJiraIssuesJQL(projectConfig, jiraAdapter, isOffline);
		} else {
			return processesJiraIssuesBoard(projectConfig, jiraAdapter, isOffline);
		}
	}

	private int processesJiraIssuesJQL(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter,
			boolean isOffline) {
		PSLogData psLogData = new PSLogData();
		psLogData.setProjectName(projectConfig.getProjectName());
		psLogData.setKanban(FALSE);
		int savedIsuesCount = 0;
		int total = 0;

		Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType = new HashMap<>();
		setStartDate(jiraProcessorConfig);
		ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(projectConfig);
		boolean processorFetchingComplete = false;
		try {
			boolean dataExist = (jiraIssueRepository
					.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);

			Map<String, LocalDateTime> maxChangeDatesByIssueType = getLastChangedDatesByIssueType(projectConfig);

			Map<String, LocalDateTime> maxChangeDatesByIssueTypeWithAddedTime = new HashMap<>();

			maxChangeDatesByIssueType.forEach((k, v) -> {
				long extraMinutes = jiraProcessorConfig.getMinsToReduce();
				maxChangeDatesByIssueTypeWithAddedTime.put(k, v.minusMinutes(extraMinutes));
			});
			int pageSize = jiraAdapter.getPageSize();

			boolean hasMore = true;

			boolean latestDataFetched = false;

			Set<SprintDetails> setForCacheClean = new HashSet<>();
			String userTimeZone = jiraAdapter.getUserTimeZone(projectConfig);
			int sprintCount = jiraProcessorConfig.getSprintCountForCacheClean();

			List<Status> projectStatuses = jiraAdapter.getStatus();
			processAndSaveProjectStatusCategory(projectStatuses, projectConfig.getBasicProjectConfigId().toString());

			for (int i = 0; hasMore; i += pageSize) {
				Instant startProcessingJiraIssues = Instant.now();
				SearchResult searchResult = jiraAdapter.getIssues(projectConfig, maxChangeDatesByIssueTypeWithAddedTime,
						userTimeZone, i, dataExist);
				List<Issue> issues = getIssuesFromResult(searchResult);
				if (total == 0) {
					total = getTotal(searchResult);
					psLogData.setTotalFetchedIssues(String.valueOf(total));
				}

				// in case of offline method issues size can be greater than
				// pageSize, increase page size so that same issues not read

				if (isOffline && issues.size() >= pageSize) {
					pageSize = issues.size() + 1;
				}
				if (CollectionUtils.isNotEmpty(issues)) {
					List<JiraIssue> jiraIssues = saveJiraIssueDetails(issues, projectConfig, setForCacheClean,
							jiraAdapter, false, false);
					findLastSavedJiraIssueByType(jiraIssues, lastSavedJiraIssueChangedDateByType);
					savedIsuesCount += issues.size();
					savingIssueLogs(savedIsuesCount, jiraIssues, startProcessingJiraIssues, false, psLogData);
				}

				if (!dataExist && !latestDataFetched && setForCacheClean.size() > sprintCount) {
					latestDataFetched = cleanCache();
					setForCacheClean.clear();
					log.info("latest sprint fetched cache and cleaned.");
				}
				// will result in an extra call if number of results == pageSize
				// but I would rather do that then complicate the jira client
				// implementation

				if (issues.size() < pageSize) {
					break;
				}
				TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
			}
			processorFetchingComplete = true;
		} catch (JSONException e) {
			log.error("Error while updating Story information in scrum client", e,
					kv(CommonConstant.PSLOGDATA, psLogData));
			lastSavedJiraIssueChangedDateByType.clear();
		} catch (InterruptedException e) {
			log.error("Interrupted exception thrown.", e, kv(CommonConstant.PSLOGDATA, psLogData));
			lastSavedJiraIssueChangedDateByType.clear();
			processorFetchingComplete = false;
		} finally {
			boolean isAttemptSuccess = isAttemptSuccess(total, savedIsuesCount, processorFetchingComplete, psLogData);
			psLogData.setAction(CommonConstant.PROJECT_EXECUTION_STATUS);
			if (!isAttemptSuccess) {
				lastSavedJiraIssueChangedDateByType.clear();
				processorExecutionTraceLog.setLastSuccessfulRun(null);
				psLogData.setProjectExecutionStatus(String.valueOf(isAttemptSuccess));
				log.error("Error in Fetching Issues through JQL", kv(CommonConstant.PSLOGDATA, psLogData));
			} else {
				processorExecutionTraceLog
						.setLastSuccessfulRun(DateUtil.dateTimeFormatter(LocalDateTime.now(), QUERYDATEFORMAT));
			}
			saveExecutionTraceLog(processorExecutionTraceLog, lastSavedJiraIssueChangedDateByType, isAttemptSuccess,
					projectConfig.getProjectBasicConfig());
		}

		return savedIsuesCount;
	}

	private int processesJiraIssuesBoard(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter,
			boolean isOffline) {
		PSLogData psLogData = new PSLogData();
		psLogData.setProjectName(projectConfig.getProjectName());
		psLogData.setKanban(FALSE);
		int savedIsuesCount = 0;
		int total = 0;

		Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType = new HashMap<>();
		setStartDate(jiraProcessorConfig);
		ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(projectConfig);
		boolean processorFetchingComplete = false;
		try {
			sprintClient.createSprintDetailBasedOnBoard(projectConfig, jiraAdapter);
			boolean dataExist = (jiraIssueRepository
					.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
			// write get logic to fetch last successful updated date.
			String queryDate = getDeltaDate(processorExecutionTraceLog.getLastSuccessfulRun());
			Set<SprintDetails> setForCacheClean = new HashSet<>();
			String userTimeZone = jiraAdapter.getUserTimeZone(projectConfig);
			int sprintCount = jiraProcessorConfig.getSprintCountForCacheClean();
			List<BoardDetails> boardDetailsList = projectConfig.getProjectToolConfig().getBoards();
			List<Status> projectStatuses = jiraAdapter.getStatus();
			processAndSaveProjectStatusCategory(projectStatuses, projectConfig.getBasicProjectConfigId().toString());
			for (BoardDetails board : boardDetailsList) {
				psLogData.setBoardId(board.getBoardId());
				int boardTotal = 0;
				boolean latestDataFetched = false;
				int pageSize = jiraAdapter.getPageSize();
				boolean hasMore = true;
				for (int i = 0; hasMore; i += pageSize) {
					Instant startProcessingJiraIssues = Instant.now();
					SearchResult searchResult = jiraAdapter.getIssues(board, projectConfig, queryDate, userTimeZone, i,
							dataExist);
					List<Issue> issues = getIssuesFromResult(searchResult);
					if (boardTotal == 0) {
						boardTotal = getTotal(searchResult);
						total += boardTotal;
						psLogData.setTotalFetchedIssues(String.valueOf(total));
					}

					// in case of offline method issues size can be greater than
					// pageSize, increase page size so that same issues not read

					if (isOffline && issues.size() >= pageSize) {
						pageSize = issues.size() + 1;
					}
					if (CollectionUtils.isNotEmpty(issues)) {
						List<JiraIssue> jiraIssues = saveJiraIssueDetails(issues, projectConfig, setForCacheClean,
								jiraAdapter, true, false);
						savedIsuesCount += issues.size();
						findLastSavedJiraIssueByType(jiraIssues, lastSavedJiraIssueChangedDateByType);
						savingIssueLogs(savedIsuesCount, jiraIssues, startProcessingJiraIssues, false, psLogData);
					}

					if (!latestDataFetched && setForCacheClean.size() > sprintCount) {
						latestDataFetched = cleanCache();
						setForCacheClean.clear();
						log.info("latest sprint fetched cache cleaned.");
					}
					// will result in an extra call if number of results == pageSize
					// but I would rather do that then complicate the jira client
					// implementation
					if (issues.size() < pageSize) {
						break;
					}
					TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
				}
				Instant epicProcessStartTime = Instant.now();
				List<Issue> epicIssue = jiraAdapter.getEpic(projectConfig, board.getBoardId());
				psLogData.setEpicIssuesFetched((epicIssue == null) ? "-1" : String.valueOf(epicIssue.size()));
				List<JiraIssue> jiraEpicIssueList = saveJiraIssueDetails(epicIssue, projectConfig, setForCacheClean,
						jiraAdapter, true, false);
				savingIssueLogs(jiraEpicIssueList.size(), jiraEpicIssueList, epicProcessStartTime, true, psLogData);
			}
			processorFetchingComplete = true;
		} catch (JSONException e) {
			log.error("Error while updating Story information in scrum client through board", e,
					kv(CommonConstant.PSLOGDATA, psLogData));
			lastSavedJiraIssueChangedDateByType.clear();
		} catch (InterruptedException e) {
			log.error("Interrupted exception thrown.", e, kv(CommonConstant.PSLOGDATA, psLogData));
			lastSavedJiraIssueChangedDateByType.clear();
			processorFetchingComplete = false;
		} finally {
			boolean isAttemptSuccess = isAttemptSuccess(total, savedIsuesCount, processorFetchingComplete, psLogData);
			psLogData.setAction(CommonConstant.PROJECT_EXECUTION_STATUS);
			if (!isAttemptSuccess) {
				lastSavedJiraIssueChangedDateByType.clear();
				processorExecutionTraceLog.setLastSuccessfulRun(null);
				psLogData.setProjectExecutionStatus(String.valueOf(isAttemptSuccess));
				log.error("Error in Fetching Issues through board", kv(CommonConstant.PSLOGDATA, psLogData));
			} else {
				processorExecutionTraceLog
						.setLastSuccessfulRun(DateUtil.dateTimeFormatter(LocalDateTime.now(), QUERYDATEFORMAT));
			}
			saveExecutionTraceLog(processorExecutionTraceLog, lastSavedJiraIssueChangedDateByType, isAttemptSuccess,
					projectConfig.getProjectBasicConfig());
		}
		return savedIsuesCount;
	}

	private void savingIssueLogs(int savedIssuesCount, List<JiraIssue> jiraIssues, Instant startProcessingJiraIssues,
			boolean isEpic, PSLogData psLogData) {
		PSLogData saveIssueLog = new PSLogData();
		saveIssueLog.setIssueAndDesc(jiraIssues.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
		saveIssueLog.setTotalSavedIssues(String.valueOf(savedIssuesCount));
		psLogData.setTotalSavedIssues(String.valueOf(savedIssuesCount));
		psLogData.setTimeTaken(String.valueOf(Duration.between(startProcessingJiraIssues, Instant.now()).toMillis()));
		psLogData.setSprintListFetched(null);
		psLogData.setTotalFetchedSprints(null);
		if (!isEpic) {
			saveIssueLog.setAction(CommonConstant.SAVED_ISSUES);
			psLogData.setAction(CommonConstant.SAVED_ISSUES);
			saveIssueLog.setTotalFetchedIssues(psLogData.getTotalFetchedIssues());
			log.debug("Saved Issues for project {}", MDC.get(CommonConstant.PROJECTNAME),
					kv(CommonConstant.PSLOGDATA, saveIssueLog));
			log.info("Processed Issues for project {}", MDC.get(CommonConstant.PROJECTNAME),
					kv(CommonConstant.PSLOGDATA, psLogData));
		} else {
			saveIssueLog.setAction(CommonConstant.SAVED_EPIC_ISSUES);
			psLogData.setAction(CommonConstant.SAVED_EPIC_ISSUES);
			saveIssueLog.setEpicIssuesFetched(psLogData.getEpicIssuesFetched());
			log.debug("Saved Epic Issues for project {}", MDC.get(CommonConstant.PROJECTNAME),
					kv(CommonConstant.PSLOGDATA, saveIssueLog));
			log.info("Processed Epic Issues for project {}", MDC.get(CommonConstant.PROJECTNAME),
					kv(CommonConstant.PSLOGDATA, psLogData));

		}
	}

	private void findLastSavedJiraIssueByType(List<JiraIssue> jiraIssues,
			Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType) {
		Map<String, List<JiraIssue>> issuesByType = CollectionUtils
				.emptyIfNull(
						jiraIssues)
				.stream()
				.sorted(Comparator.comparing((JiraIssue jiraIssue) -> LocalDateTime.parse(jiraIssue.getChangeDate(),
						DateTimeFormatter.ofPattern(JiraConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT))).reversed())
				.collect(Collectors.groupingBy(JiraIssue::getTypeName));

		issuesByType.forEach((typeName, issues) -> {
			JiraIssue firstIssue = issues.stream()
					.sorted(Comparator
							.comparing((JiraIssue jiraIssue) -> LocalDateTime.parse(jiraIssue.getChangeDate(),
									DateTimeFormatter.ofPattern(JiraConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT)))
							.reversed())
					.findFirst().orElse(null);
			if (firstIssue != null) {
				LocalDateTime currentIssueDate = LocalDateTime.parse(firstIssue.getChangeDate(),
						DateTimeFormatter.ofPattern(JiraConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT));
				LocalDateTime capturedDate = lastSavedJiraIssueChangedDateByType.get(typeName);
				lastSavedJiraIssueChangedDateByType.put(typeName, updatedDateToSave(capturedDate, currentIssueDate));
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

	private boolean isAttemptSuccess(int total, int savedCount, boolean processorFetchingComplete,
			PSLogData psLogData) {
		psLogData.setTotalFetchedIssues(String.valueOf(total));
		psLogData.setTotalSavedIssues(String.valueOf(savedCount));
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

	private ProcessorExecutionTraceLog createTraceLog(ProjectConfFieldMapping projectConfig) {
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

	private void saveExecutionTraceLog(ProcessorExecutionTraceLog processorExecutionTraceLog,
			Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType, boolean isSuccess,
			ProjectBasicConfig projectBasicConfig) {

		if (lastSavedJiraIssueChangedDateByType.isEmpty()) {
			processorExecutionTraceLog.setLastSavedEntryUpdatedDateByType(null);
		} else {
			processorExecutionTraceLog.setLastSavedEntryUpdatedDateByType(lastSavedJiraIssueChangedDateByType);
		}
		processorExecutionTraceLog.setExecutionSuccess(isSuccess);
		processorExecutionTraceLog.setLastEnableAssigneeToggleState(projectBasicConfig.isSaveAssigneeDetails());
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		savingTraceLogToLog(processorExecutionTraceLog);
		processorExecutionTraceLogService.save(processorExecutionTraceLog);
	}

	private void savingTraceLogToLog(ProcessorExecutionTraceLog processorExecutionTraceLog) {
		PSLogData traceLog = new PSLogData();
		traceLog.setAction(CommonConstant.PROJECT_EXECUTION_STATUS);
		traceLog.setExecutionEndedAt(
				DateUtil.convertMillisToDateTime(processorExecutionTraceLog.getExecutionEndedAt()));
		traceLog.setExecutionStartedAt(
				DateUtil.convertMillisToDateTime(processorExecutionTraceLog.getExecutionStartedAt()));
		traceLog.setLastSuccessfulRun(processorExecutionTraceLog.getLastSuccessfulRun());
		traceLog.setProjectExecutionStatus(String.valueOf(processorExecutionTraceLog.isExecutionSuccess()));
		traceLog.setLastEnableAssigneeToggleState(
				String.valueOf(processorExecutionTraceLog.isLastEnableAssigneeToggleState()));
		List<String> logJiraIssueChange = new ArrayList<>();
		if (MapUtils.isNotEmpty(processorExecutionTraceLog.getLastSavedEntryUpdatedDateByType())) {
			processorExecutionTraceLog.getLastSavedEntryUpdatedDateByType()
					.forEach((issue, updateDated) -> logJiraIssueChange
							.add(issue + CommonConstant.ARROW + updateDated.toString() + CommonConstant.NEWLINE));
			traceLog.setLastSavedJiraIssueChangedDateByType(logJiraIssueChange);
		}
		log.info("last execution time of {} for project {} is {}. status is {}",
				processorExecutionTraceLog.getProcessorName(), processorExecutionTraceLog.getBasicProjectConfigId(),
				processorExecutionTraceLog.getExecutionEndedAt(), processorExecutionTraceLog.isExecutionSuccess(),
				kv(CommonConstant.PSLOGDATA, traceLog));
	}

	private boolean cleanCache() {
		boolean accountHierarchyCleaned = jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.CACHE_ACCOUNT_HIERARCHY);
		boolean kpiDataCleaned = jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.JIRA_KPI_CACHE);
		return accountHierarchyCleaned && kpiDataCleaned;
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

		List<JiraIssue> jiraIssuesToDelete = Lists.newArrayList();
		List<JiraIssueCustomHistory> jiraIssuesHistoryToDelete = Lists.newArrayList();
		purgeIssuesList.forEach(issue -> {
			String issueId = JiraProcessorUtil.deodeUTF8String(issue.getId());
			String issueNumber = JiraProcessorUtil.deodeUTF8String(issue.getKey());

			JiraIssue jiraIssue = findOneJiraIssue(issueId, projectConfig.getBasicProjectConfigId().toString());
			JiraIssueCustomHistory jiraIssueHistory = findOneJiraIssueHistory(issueNumber,
					projectConfig.getBasicProjectConfigId().toString());
			if (jiraIssue != null) {
				jiraIssuesToDelete.add(jiraIssue);
			}
			if (jiraIssueHistory != null) {
				jiraIssuesHistoryToDelete.add(jiraIssueHistory);
			}

		});
		jiraIssueRepository.deleteAll(jiraIssuesToDelete);
		jiraIssueCustomHistoryRepository.deleteAll(jiraIssuesHistoryToDelete);
	}

	/**
	 * Saves jira issues details
	 *
	 * @param currentPagedJiraRs
	 *            List of Jira issue in current page call
	 * @param projectConfig
	 *            Project Configuration Mapping
	 * @param setForCacheClean
	 *            setForCacheClean
	 * @throws JSONException
	 *             Error If JSON is invalid
	 */
	public List<JiraIssue> saveJiraIssueDetails(List<Issue> currentPagedJiraRs, ProjectConfFieldMapping projectConfig,
			Set<SprintDetails> setForCacheClean, JiraAdapter jiraAdapter, boolean dataFromBoard, boolean isSprintFetch)
			throws JSONException, InterruptedException {

		Set<Assignee> assigneeSetToSave = new HashSet<>();
		List<JiraIssue> jiraIssuesToSave = new ArrayList<>();
		List<JiraIssueCustomHistory> jiraIssueHistoryToSave = new ArrayList<>();

		if (null == currentPagedJiraRs) {
			log.error("JIRA Processor | No list of current paged JIRA's issues found");
			return jiraIssuesToSave;
		}

		Map<String, String> issueEpics = new HashMap<>();
		Set<SprintDetails> sprintDetailsSet = new LinkedHashSet<>();
		ObjectId jiraProcessorId = jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();
		AssigneeDetails assigneeDetails = assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(
				projectConfig.getBasicProjectConfigId().toString(), ProcessorConstants.JIRA);
		for (Issue issue : currentPagedJiraRs) {
			FieldMapping fieldMapping = projectConfig.getFieldMapping();

			if (null == fieldMapping) {
				return jiraIssuesToSave;
			}
			Set<String> issueTypeNames = new HashSet<>();
			for (String issueTypeName : fieldMapping.getJiraIssueTypeNames()) {
				issueTypeNames.add(issueTypeName.toLowerCase(Locale.getDefault()));
			}
			String issueId = JiraProcessorUtil.deodeUTF8String(issue.getId());
			String issueNumber = JiraProcessorUtil.deodeUTF8String(issue.getKey());

			JiraIssue jiraIssue = getJiraIssue(projectConfig, issueId);
			JiraIssueCustomHistory jiraIssueHistory = getIssueCustomHistory(projectConfig, issueNumber);

			Map<String, IssueField> fields = JiraIssueClientUtil.buildFieldMap(issue.getFields());

			IssueType issueType = issue.getIssueType();
			User assignee = issue.getAssignee();

			IssueField epic = fields.get(fieldMapping.getEpicName());
			IssueField sprint = fields.get(fieldMapping.getSprintName());

			// set URL to jiraIssue
			setURL(issue.getKey(), jiraIssue, projectConfig);

			// Add RCA to JiraIssue
			setRCA(fieldMapping, issue, jiraIssue, fields);

			// Add UAT/Third Party identification field to JiraIssue
			setThirdPartyDefectIdentificationField(fieldMapping, issue, jiraIssue, fields);

			if (issueTypeNames
					.contains(JiraProcessorUtil.deodeUTF8String(issueType.getName()).toLowerCase(Locale.getDefault()))
					|| dataFromBoard) {
				// collectorId
				jiraIssue.setProcessorId(jiraProcessorId);

				// ID
				jiraIssue.setIssueId(JiraProcessorUtil.deodeUTF8String(issue.getId()));

				// Type
				jiraIssue.setTypeId(JiraProcessorUtil.deodeUTF8String(issueType.getId()));
				jiraIssue.setTypeName(JiraProcessorUtil.deodeUTF8String(issueType.getName()));
				jiraIssue.setOriginalType(JiraProcessorUtil.deodeUTF8String(issueType.getName()));

				setEpicLinked(fieldMapping, jiraIssue, fields);

				setDefectIssueType(jiraIssue, issueType, fieldMapping);

				// Label
				jiraIssue.setLabels(JiraIssueClientUtil.getLabelsList(issue));
				processJiraIssueData(jiraIssue, issue, fields, fieldMapping, jiraProcessorConfig);

				// Set project specific details
				setProjectSpecificDetails(projectConfig, jiraIssue, issue);

				// Set additional filters
				setAdditionalFilters(jiraIssue, issue, projectConfig);

				setStoryLinkWithDefect(issue, jiraIssue, fields);

				// ADD QA identification field to feature
				setQADefectIdentificationField(fieldMapping, issue, jiraIssue, fields);
				setProductionDefectIdentificationField(fieldMapping, issue, jiraIssue, fields);

				setIssueTechStoryType(fieldMapping, issue, jiraIssue, fields);
				jiraIssue.setAffectedVersions(JiraIssueClientUtil.getAffectedVersions(issue));
				setIssueEpics(issueEpics, epic, jiraIssue);

				setJiraIssueValues(jiraIssue, issue, fieldMapping, fields);

				processSprintData(jiraIssue, sprint, projectConfig, sprintDetailsSet);

				setJiraAssigneeDetails(jiraIssue, assignee, assigneeSetToSave, projectConfig);

				setEstimates(jiraIssue, issue);

				setDueDates(jiraIssue, issue, fields, fieldMapping);

				// setting filter data from JiraIssue to
				// jira_issue_custom_history
				setJiraIssueHistory(jiraIssueHistory, jiraIssue, issue, projectConfig, fields);
				if (StringUtils.isNotBlank(jiraIssue.getProjectID())) {
					jiraIssuesToSave.add(jiraIssue);
					jiraIssueHistoryToSave.add(jiraIssueHistory);
				}
			}
		}

		// Saving back to MongoDB
		jiraIssueRepository.saveAll(jiraIssuesToSave);
		jiraIssueCustomHistoryRepository.saveAll(jiraIssueHistoryToSave);
		if (!isSprintFetch) {
			saveAccountHierarchy(jiraIssuesToSave, projectConfig, sprintDetailsSet);
			saveAssigneeDetailsToDb(projectConfig, assigneeSetToSave, assigneeDetails);
		}
		if (!dataFromBoard) {
			sprintClient.processSprints(projectConfig, sprintDetailsSet, jiraAdapter, false);
		}

		setForCacheClean.addAll(sprintDetailsSet.stream()
				.filter(sprint -> !sprint.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_FUTURE))
				.collect(Collectors.toSet()));
		return jiraIssuesToSave;
	}

	private void setEpicLinked(FieldMapping fieldMapping, JiraIssue jiraIssue, Map<String, IssueField> fields) {
		if (StringUtils.isNotEmpty(fieldMapping.getEpicLink())
				&& fields.get(fieldMapping.getEpicLink()) != null
				&& fields.get(fieldMapping.getEpicLink()).getValue() != null) {
			jiraIssue.setEpicLinked(fields.get((fieldMapping.getEpicLink()).trim()).getValue().toString());
		}
	}

	/**
	 * save assignee details from jira issue and if already exist then update
	 * assignee list
	 *
	 * @param projectConfig
	 * @param assigneeSetToSave
	 * @param assigneeDetails
	 */
	private void saveAssigneeDetailsToDb(ProjectConfFieldMapping projectConfig, Set<Assignee> assigneeSetToSave,
			AssigneeDetails assigneeDetails) {
		if (CollectionUtils.isNotEmpty(assigneeSetToSave)) {
			if (assigneeDetails == null) {
				assigneeDetails = new AssigneeDetails();
				assigneeDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString());
				assigneeDetails.setSource(ProcessorConstants.JIRA);
				assigneeDetails.setAssignee(assigneeSetToSave);
			} else {
				Set<Assignee> updatedAssigneeSetToSave = new HashSet<>();
				updatedAssigneeSetToSave.addAll(assigneeDetails.getAssignee());
				updatedAssigneeSetToSave.addAll(assigneeSetToSave);
				assigneeDetails.setAssignee(updatedAssigneeSetToSave);
			}
			assigneeDetailsRepository.save(assigneeDetails);
		}
	}

	private JiraIssueCustomHistory getIssueCustomHistory(ProjectConfFieldMapping projectConfig, String issueId) {
		JiraIssueCustomHistory jiraIssueHistory;
		jiraIssueHistory = findOneJiraIssueHistory(issueId, projectConfig.getBasicProjectConfigId().toString());
		if (jiraIssueHistory == null) {
			jiraIssueHistory = new JiraIssueCustomHistory();
		}
		return jiraIssueHistory;
	}

	private JiraIssue getJiraIssue(ProjectConfFieldMapping projectConfig, String issueId) {
		JiraIssue jiraIssue;
		jiraIssue = findOneJiraIssue(issueId, projectConfig.getBasicProjectConfigId().toString());
		if (jiraIssue == null) {
			jiraIssue = new JiraIssue();
		}
		return jiraIssue;
	}

	private void setAdditionalFilters(JiraIssue jiraIssue, Issue issue, ProjectConfFieldMapping projectConfig) {
		List<AdditionalFilter> additionalFilter = additionalFilterHelper.getAdditionalFilter(issue, projectConfig);
		jiraIssue.setAdditionalFilters(additionalFilter);
	}

	private void setProjectSpecificDetails(ProjectConfFieldMapping projectConfig, JiraIssue jiraIssue, Issue issue) {
		String name = projectConfig.getProjectName();
		String id = new StringBuffer(name).append(CommonConstant.UNDERSCORE)
				.append(projectConfig.getBasicProjectConfigId().toString()).toString();

		jiraIssue.setProjectID(id);
		jiraIssue.setProjectName(name);
		jiraIssue.setProjectKey(issue.getProject().getKey());
		jiraIssue.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString());
		jiraIssue.setProjectBeginDate("");
		jiraIssue.setProjectEndDate("");
		jiraIssue.setProjectChangeDate("");
		jiraIssue.setProjectState("");
		jiraIssue.setProjectIsDeleted("False");
		jiraIssue.setProjectPath("");
	}

	/**
	 * @param issueEpics
	 * @param epic
	 * @param jiraIssue
	 */
	private void setIssueEpics(Map<String, String> issueEpics, IssueField epic, JiraIssue jiraIssue) {
		if (epic != null && epic.getValue() != null && !JiraProcessorUtil.deodeUTF8String(epic.getValue()).isEmpty()) {
			issueEpics.put(jiraIssue.getIssueId(), JiraProcessorUtil.deodeUTF8String(epic.getValue()));
		}
	}

	private void setDefectIssueType(JiraIssue jiraIssue, IssueType issueType, FieldMapping fieldMapping) {
		// set defecttype to BUG
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype())
				&& fieldMapping.getJiradefecttype().stream().anyMatch(issueType.getName()::equalsIgnoreCase)) {
			jiraIssue.setTypeName(NormalizedJira.DEFECT_TYPE.getValue());
		}
	}

	private void setJiraIssueValues(JiraIssue jiraIssue, Issue issue, FieldMapping fieldMapping,
			Map<String, IssueField> fields) {

		// Priority
		if (issue.getPriority() != null) {
			jiraIssue.setPriority(JiraProcessorUtil.deodeUTF8String(issue.getPriority().getName()));
		}
		// Set EPIC issue data for issue type epic
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueEpicType())
				&& fieldMapping.getJiraIssueEpicType().contains(issue.getIssueType().getName())) {
			setEpicIssueData(fieldMapping, jiraIssue, fields);
		}
		// Release Version
		if (issue.getFixVersions() != null) {
			List<ReleaseVersion> releaseVersions = new ArrayList<>();
			for (Version fixVersionName : issue.getFixVersions()) {
				ReleaseVersion release = new ReleaseVersion();
				release.setReleaseDate(fixVersionName.getReleaseDate());
				release.setReleaseName(fixVersionName.getName());
				releaseVersions.add(release);
			}
			jiraIssue.setReleaseVersions(releaseVersions);
		}
	}

	/**
	 * Sets RCA
	 *
	 * @param fieldMapping
	 *            fieldMapping provided by the User
	 * @param issue
	 *            Atlassian Issue
	 * @param jiraIssue
	 *            JiraIssue instance
	 * @param fields
	 *            Map of Issue Fields
	 */
	private void setRCA(FieldMapping fieldMapping, Issue issue, JiraIssue jiraIssue, Map<String, IssueField> fields) {

		List<String> rcaList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype())
				&& fieldMapping.getJiradefecttype().stream().anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)
				&& fields.get(fieldMapping.getRootCause()) != null
				&& fields.get(fieldMapping.getRootCause()).getValue() != null) {
			rcaList.addAll(getRootCauses(fieldMapping, fields));
		}
		if (rcaList.isEmpty()) {
			rcaList.add(JiraConstants.RCA_CAUSE_NONE);
		}

		jiraIssue.setRootCauseList(rcaList);

	}

	private List<String> getRootCauses(FieldMapping fieldMapping, Map<String, IssueField> fields) {
		List<String> rootCauses = new ArrayList<>();

		if (fields.get(fieldMapping.getRootCause()).getValue() instanceof org.codehaus.jettison.json.JSONArray) {
			// Introduce enum to standarize the values of RCA
			org.codehaus.jettison.json.JSONArray jsonArray = (org.codehaus.jettison.json.JSONArray) fields
					.get(fieldMapping.getRootCause()).getValue();
			for (int i = 0; i < jsonArray.length(); i++) {
				String rcaCause = null;
				try {
					rcaCause = jsonArray.getJSONObject(i).getString(JiraConstants.VALUE);
					if (rcaCause != null) {
						rootCauses.add(rcaCauseStringToSave(rcaCause));
					}
				} catch (JSONException ex) {
					log.error("JIRA Processor | Error while parsing RCA Custom_Field", ex);
				}

			}
		} else if (fields.get(fieldMapping.getRootCause())
				.getValue() instanceof org.codehaus.jettison.json.JSONObject) {
			String rcaCause = null;
			try {
				rcaCause = ((org.codehaus.jettison.json.JSONObject) fields.get(fieldMapping.getRootCause()).getValue())
						.getString(JiraConstants.VALUE);
			} catch (JSONException ex) {
				log.error("JIRA Processor | Error while parsing RCA Custom_Field", ex);
			}

			if (rcaCause != null) {
				rootCauses.add(rcaCauseStringToSave(rcaCause));
			}

		}

		return rootCauses;
	}

	private String rcaCauseStringToSave(String rcaCause) {

		if (rcaCause == null) {
			return null;
		}
		String rcaCauseResult = "";

		if (jiraProcessorConfig.getRcaValuesForCodeIssue().stream().anyMatch(rcaCause::equalsIgnoreCase)) {
			rcaCauseResult = JiraConstants.CODE_ISSUE;
		} else {
			rcaCauseResult = rcaCause;
		}

		return rcaCauseResult.toLowerCase();
	}

	/**
	 * @param featureConfig
	 * @param issue
	 * @param feature
	 * @param fields
	 */
	private void setQADefectIdentificationField(FieldMapping featureConfig, Issue issue, JiraIssue feature,
			Map<String, IssueField> fields) {
		try {
			if (CollectionUtils.isNotEmpty(featureConfig.getJiradefecttype()) && featureConfig.getJiradefecttype()
					.stream().anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)) {
				if (null != featureConfig.getJiraBugRaisedByQAIdentification() && featureConfig
						.getJiraBugRaisedByQAIdentification().trim().equalsIgnoreCase(JiraConstants.LABELS)) {
					List<String> commonLabel = issue.getLabels().stream()
							.filter(x -> featureConfig.getJiraBugRaisedByQAValue().contains(x))
							.collect(Collectors.toList());
					if (CollectionUtils.isNotEmpty(commonLabel)) {
						feature.setDefectRaisedByQA(true);
					}
				} else if (null != featureConfig.getJiraBugRaisedByQAIdentification()
						&& featureConfig.getJiraBugRaisedByQAIdentification().trim()
								.equalsIgnoreCase(CommonConstant.CUSTOM_FIELD)
						&& fields.get(featureConfig.getJiraBugRaisedByQACustomField().trim()) != null
						&& fields.get(featureConfig.getJiraBugRaisedByQACustomField().trim()).getValue() != null
						&& isBugRaisedByValueMatchesRaisedByCustomField(featureConfig.getJiraBugRaisedByQAValue(),
								fields.get(featureConfig.getJiraBugRaisedByQACustomField().trim()).getValue())) {
					feature.setDefectRaisedByQA(true);
				} else {
					feature.setDefectRaisedByQA(false);
				}
			}

		} catch (Exception e) {
			log.error("Error while parsing QA field {}", e);
		}

	}

	private void setProductionDefectIdentificationField(FieldMapping featureConfig, Issue issue, JiraIssue feature,
			Map<String, IssueField> fields) {
		try {
			if (CollectionUtils.isNotEmpty(featureConfig.getJiradefecttype()) && featureConfig.getJiradefecttype()
					.stream().anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)) {
				if (null != featureConfig.getProductionDefectIdentifier() && featureConfig
						.getProductionDefectIdentifier().trim().equalsIgnoreCase(JiraConstants.LABELS)) {
					List<String> commonLabel = issue.getLabels().stream()
							.filter(x -> featureConfig.getProductionDefectValue().contains(x))
							.collect(Collectors.toList());
					if (CollectionUtils.isNotEmpty(commonLabel)) {
						feature.setProductionDefect(true);
					}
				} else if (null != featureConfig.getProductionDefectIdentifier()
						&& featureConfig.getProductionDefectIdentifier().trim()
								.equalsIgnoreCase(JiraConstants.CUSTOM_FIELD)
						&& fields.get(featureConfig.getProductionDefectCustomField().trim()) != null
						&& fields.get(featureConfig.getProductionDefectCustomField().trim()).getValue() != null
						&& isBugRaisedByValueMatchesRaisedByCustomField(featureConfig.getProductionDefectValue(),
								fields.get(featureConfig.getProductionDefectCustomField().trim()).getValue())) {
					feature.setProductionDefect(true);
				} else if (null != featureConfig.getProductionDefectIdentifier()
						&& featureConfig.getProductionDefectIdentifier().trim()
								.equalsIgnoreCase(JiraConstants.COMPONENT)
						&& null != featureConfig.getProductionDefectComponentValue()
						&& isComponentMatchWithJiraComponent(issue, featureConfig)) {
					feature.setProductionDefect(true);

				} else {
					feature.setProductionDefect(false);
				}
			}

		} catch (Exception e) {
			log.error("Error while parsing Production Defect Identification field {}", e);
		}

	}

	private boolean isComponentMatchWithJiraComponent(Issue issue, FieldMapping featureConfig) {
		boolean isRaisedByThirdParty = false;
		Iterable<BasicComponent> components = issue.getComponents();
		List<BasicComponent> componentList = new ArrayList<>();
		components.forEach(componentList::add);

		if (CollectionUtils.isNotEmpty(componentList)) {
			List<String> componentNameList = componentList.stream().map(BasicComponent::getName)
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(componentNameList) && componentNameList.stream()
					.anyMatch(featureConfig.getProductionDefectComponentValue()::equalsIgnoreCase)) {
				isRaisedByThirdParty = true;
			}
		}
		return isRaisedByThirdParty;
	}

	private void setJiraIssueHistory(JiraIssueCustomHistory jiraIssueHistory, JiraIssue jiraIssue, Issue issue,
			ProjectConfFieldMapping projectConfig, Map<String, IssueField> fields) {

		jiraIssueHistory.setProjectID(jiraIssue.getProjectName());
		jiraIssueHistory.setProjectComponentId(jiraIssue.getProjectID());
		jiraIssueHistory.setProjectKey(jiraIssue.getProjectKey());
		jiraIssueHistory.setStoryType(jiraIssue.getTypeName());
		jiraIssueHistory.setAdditionalFilters(jiraIssue.getAdditionalFilters());
		jiraIssueHistory.setUrl(jiraIssue.getUrl());
		jiraIssueHistory.setDescription(jiraIssue.getName());
		// This method is not setup method. write it to keep
		// custom history
		processJiraIssueHistory(jiraIssueHistory, jiraIssue, issue, projectConfig, fields);

		jiraIssueHistory.setBasicProjectConfigId(jiraIssue.getBasicProjectConfigId());
	}

	/**
	 * Sets Story Link with Defect
	 *
	 * @param issue
	 * @param jiraIssue
	 * @param fields
	 */
	private void setStoryLinkWithDefect(Issue issue, JiraIssue jiraIssue, Map<String, IssueField> fields) {
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())
				|| NormalizedJira.TEST_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
			Set<String> defectStorySet = new HashSet<>();
			String parentKey = null;

			excludeLinks(issue, defectStorySet);
			storyWithSubTaskDefect(issue, fields, defectStorySet);
			jiraIssue.setDefectStoryID(defectStorySet);
		}
	}

	private void excludeLinks(Issue issue, Set<String> defectStorySet) {
		if (CollectionUtils.isNotEmpty(jiraProcessorConfig.getExcludeLinks())) {
			for (IssueLink issueLink : issue.getIssueLinks()) {
				if (!jiraProcessorConfig.getExcludeLinks().stream()
						.anyMatch(issueLink.getIssueLinkType().getDescription()::equalsIgnoreCase)) {
					defectStorySet.add(issueLink.getTargetIssueKey());
				}
			}
		}
	}

	/**
	 * Finds one JiraIssue by issueId
	 *
	 * @param issueId
	 *            jira issueId
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return JiraIssue corresponding to provided IssueId in DB
	 */
	private JiraIssue findOneJiraIssue(String issueId, String basicProjectConfigId) {
		List<JiraIssue> jiraIssues = jiraIssueRepository
				.findByIssueIdAndBasicProjectConfigId(StringEscapeUtils.escapeHtml4(issueId), basicProjectConfigId);

		if (jiraIssues.size() > 1) {
			log.error("JIRA Processor | More than one Jira Issue item found for id {}", issueId);
		}

		if (!jiraIssues.isEmpty()) {
			return jiraIssues.get(0);
		}
		return null;

	}

	/**
	 * Populate field to identify if defect is from Third party or UAT. Get
	 * customfield value from jiraBugRaisedByField. This value can be any custom
	 * field or "labels"
	 *
	 * @param fieldMapping
	 *            Porject Field mapping
	 * @param issue
	 *            Atlassian issue
	 * @param jiraIssue
	 *            jiraIssue
	 * @param fields
	 *            Map of IssueField Id and IssueField
	 */
	private void setThirdPartyDefectIdentificationField(FieldMapping fieldMapping, Issue issue, JiraIssue jiraIssue,
			Map<String, IssueField> fields) {
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype()) && fieldMapping.getJiradefecttype().stream()
				.anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)) {
			if (StringUtils.isNotBlank(fieldMapping.getJiraBugRaisedByIdentification())
					&& fieldMapping.getJiraBugRaisedByIdentification().trim()
							.equalsIgnoreCase(JiraConstants.CUSTOM_FIELD)
					&& fields.get(fieldMapping.getJiraBugRaisedByCustomField().trim()) != null
					&& fields.get(fieldMapping.getJiraBugRaisedByCustomField().trim()).getValue() != null
					&& isBugRaisedByValueMatchesRaisedByCustomField(fieldMapping.getJiraBugRaisedByValue(),
							fields.get(fieldMapping.getJiraBugRaisedByCustomField().trim()).getValue())) {
				jiraIssue.setDefectRaisedBy(NormalizedJira.THIRD_PARTY_DEFECT_VALUE.getValue());
			} else {
				jiraIssue.setDefectRaisedBy("");
			}

		}
	}

	/**
	 * Checks if the bug is raised by third party
	 *
	 * @param bugRaisedValue
	 *            Value of raised defect
	 * @param issueFieldValue
	 *            Issue Field Value Object
	 * @return boolean
	 */
	public boolean isBugRaisedByValueMatchesRaisedByCustomField(List<String> bugRaisedValue, Object issueFieldValue) {
		List<String> lowerCaseBugRaisedValue = bugRaisedValue.stream().map(String::toLowerCase)
				.collect(Collectors.toList());
		JSONParser parser = new JSONParser();
		JSONArray array = new JSONArray();
		boolean isRaisedByThirdParty = false;
		org.json.simple.JSONObject jsonObject = new org.json.simple.JSONObject();
		try {
			if (issueFieldValue instanceof org.codehaus.jettison.json.JSONArray) {
				array = (JSONArray) parser.parse(issueFieldValue.toString());
				for (int i = 0; i < array.size(); i++) {

					jsonObject = (org.json.simple.JSONObject) parser.parse(array.get(i).toString());
					if (lowerCaseBugRaisedValue
							.contains(jsonObject.get(JiraConstants.VALUE).toString().toLowerCase())) {
						isRaisedByThirdParty = true;
						break;
					}

				}
			} else if (issueFieldValue instanceof org.codehaus.jettison.json.JSONObject
					&& lowerCaseBugRaisedValue.contains(((org.codehaus.jettison.json.JSONObject) issueFieldValue)
							.get(JiraConstants.VALUE).toString().toLowerCase())) {
				isRaisedByThirdParty = true;
			}

		} catch (org.json.simple.parser.ParseException | JSONException e) {
			log.error("JIRA Processor | Error while parsing third party field {}", e);
		}
		return isRaisedByThirdParty;
	}

	/**
	 * Process sprint details
	 *
	 * @param jiraIssue
	 *            JiraIssue
	 * @param sprintField
	 *            Issuefield containing sprint Data
	 */
	private void processSprintData(JiraIssue jiraIssue, IssueField sprintField, ProjectConfFieldMapping projectConfig,
			Set<SprintDetails> sprintDetailsSet) {
		if (sprintField == null || sprintField.getValue() == null
				|| JiraConstants.EMPTY_STR.equals(sprintField.getValue())) {
			// Issue #678 - leave sprint blank. Not having a sprint does not
			// imply kanban
			// as a story on a scrum board without a sprint is really on the
			// backlog
			jiraIssue.setSprintID("");
			jiraIssue.setSprintName("");
			jiraIssue.setSprintBeginDate("");
			jiraIssue.setSprintEndDate("");
			jiraIssue.setSprintAssetState("");
		} else {
			Object sValue = sprintField.getValue();
			try {
				List<SprintDetails> sprints = JiraProcessorUtil.processSprintDetail(sValue);
				// Now sort so we can use the most recent one
				// yyyy-MM-dd'T'HH:mm:ss format so string compare will be fine
				Collections.sort(sprints, JiraIssueClientUtil.SPRINT_COMPARATOR);
				setSprintData(sprints, jiraIssue, sValue, projectConfig, sprintDetailsSet);
			} catch (ParseException | JSONException e) {
				log.error("JIRA Processor | Failed to obtain sprint data from {} {}", sValue, e);
			}
		}
		jiraIssue.setSprintChangeDate("");
		jiraIssue.setSprintIsDeleted(JiraConstants.FALSE);
	}

	private void setSprintData(List<SprintDetails> sprints, JiraIssue jiraIssue, Object sValue,
			ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet) {
		List<String> sprintsList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(sprints)) {
			for (SprintDetails sprint : sprints) {
				sprintsList.add(sprint.getOriginalSprintId());
				jiraIssue.setSprintIdList(sprintsList);
				sprint.setSprintID(
						sprint.getOriginalSprintId() + JiraConstants.COMBINE_IDS_SYMBOL + jiraIssue.getProjectName()
								+ JiraConstants.COMBINE_IDS_SYMBOL + projectConfig.getBasicProjectConfigId());
				sprint.setBasicProjectConfigId(new ObjectId(jiraIssue.getBasicProjectConfigId()));
			}
			sprintDetailsSet.addAll(sprints);
			// Use the latest sprint
			// if any sprint date is blank set that sprint to JiraIssue
			// because this sprint is
			// future sprint and Jira issue should be tagged with latest
			// sprint
			SprintDetails sprint = sprints.stream().filter(s -> StringUtils.isBlank(s.getStartDate())).findFirst()
					.orElse(sprints.get(sprints.size() - 1));

			jiraIssue.setSprintName(sprint.getSprintName() == null ? StringUtils.EMPTY : sprint.getSprintName());
			jiraIssue.setSprintID(sprint.getOriginalSprintId() == null ? StringUtils.EMPTY : sprint.getSprintID());
			jiraIssue.setSprintBeginDate(sprint.getStartDate() == null ? StringUtils.EMPTY
					: JiraProcessorUtil.getFormattedDate(sprint.getStartDate()));
			jiraIssue.setSprintEndDate(sprint.getEndDate() == null ? StringUtils.EMPTY
					: JiraProcessorUtil.getFormattedDate(sprint.getEndDate()));
			jiraIssue.setSprintAssetState(sprint.getState() == null ? StringUtils.EMPTY : sprint.getState());

		} else {
			log.error("JIRA Processor | Failed to obtain sprint data for {}", sValue);
		}

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

				// When toggle is On first time it will update lastUpdatedDateByIssueType to
				// start date
				setLastUpdatedDateToStartDate(projectBasicConfig, lastUpdatedDateByIssueType, projectTraceLog,
						configuredStartDate, issueType);

			} else {
				lastUpdatedDateByIssueType.put(issueType, configuredStartDate);
			}

		}

		return lastUpdatedDateByIssueType;
	}

	/**
	 * Gets one JiraIssueCustomHistory entry by issueId
	 *
	 * @param issueId
	 *            Jira Issue ID
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return JiraIssueCustomHistory corresponding to given IssueId from DB
	 */
	private JiraIssueCustomHistory findOneJiraIssueHistory(String issueId, String basicProjectConfigId) {
		List<JiraIssueCustomHistory> jiraIssues = jiraIssueCustomHistoryRepository
				.findByStoryIDAndBasicProjectConfigId(issueId, basicProjectConfigId);
		if (jiraIssues.size() > 1) {
			log.warn("JIRA Processor | More than one Issue id  found for history {}", issueId);
		}
		if (!jiraIssues.isEmpty()) {
			return jiraIssues.get(0);
		}
		return null;

	}

	/**
	 * Process Jira issue History
	 *
	 * @param jiraIssueCustomHistory
	 *            JiraIssueCustomHistory
	 * @param jiraIssue
	 *            JiraIssue
	 * @param issue
	 *            Atlassain issue
	 * @param projectConfig
	 *            Project field Mapping
	 */
	private void processJiraIssueHistory(JiraIssueCustomHistory jiraIssueCustomHistory, JiraIssue jiraIssue,
			Issue issue, ProjectConfFieldMapping projectConfig, Map<String, IssueField> fields) {
		List<ChangelogGroup> changeLogList = JiraIssueClientUtil.sortChangeLogGroup(issue);
		List<ChangelogGroup> modChangeLogList = new ArrayList<>();
		for (ChangelogGroup changeLog : changeLogList) {
			List<ChangelogItem> changeLogCollection = Lists.newArrayList(changeLog.getItems().iterator());
			ChangelogGroup grp = new ChangelogGroup(changeLog.getAuthor(), changeLog.getCreated(), changeLogCollection);
			modChangeLogList.add(grp);
		}

		if (null != jiraIssue.getDevicePlatform()) {
			jiraIssueCustomHistory.setDevicePlatform(jiraIssue.getDevicePlatform());
		}
		if (null == jiraIssueCustomHistory.getStoryID()) {
			addStoryHistory(jiraIssueCustomHistory, jiraIssue, issue, modChangeLogList, projectConfig, fields);
		} else {
			if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
				jiraIssueCustomHistory.setDefectStoryID(jiraIssue.getDefectStoryID());
			}
			handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, projectConfig,
					fields, issue);
		}

	}

	/**
	 * Adds Jira issue history
	 *
	 * @param jiraIssueCustomHistory
	 *            JiraIssueCustomHistory
	 * @param jiraIssue
	 *            JiraIssue
	 * @param issue
	 *            Atlassian Issue
	 * @param changeLogList
	 *            Change Log list
	 */
	private void addStoryHistory(JiraIssueCustomHistory jiraIssueCustomHistory, JiraIssue jiraIssue, Issue issue,
			List<ChangelogGroup> changeLogList, ProjectConfFieldMapping projectConfig, Map<String, IssueField> fields) {
		handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, projectConfig,
				fields, issue);
		jiraIssueCustomHistory.setStoryID(jiraIssue.getNumber());
		jiraIssueCustomHistory.setCreatedDate(issue.getCreationDate());

		// estimate
		jiraIssueCustomHistory.setEstimate(jiraIssue.getEstimate());
		jiraIssueCustomHistory.setBufferedEstimateTime(jiraIssue.getBufferedEstimateTime());
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
			jiraIssueCustomHistory.setDefectStoryID(jiraIssue.getDefectStoryID());
		}

	}

	/**
	 * Saves accountHierarchy for filter
	 *
	 * @param jiraIssueList
	 *            list of jira issues
	 * @param projectConfig
	 *            Project Configuration Map
	 * @param sprintDetailsSet
	 */
	private void saveAccountHierarchy(List<JiraIssue> jiraIssueList, ProjectConfFieldMapping projectConfig,
			Set<SprintDetails> sprintDetailsSet) {

		List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
				.getFullHierarchyLevels(projectConfig.isKanban());
		Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, Function.identity()));

		HierarchyLevel sprintHierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);

		Map<Pair<String, String>, AccountHierarchy> existingHierarchy = JiraIssueClientUtil
				.getAccountHierarchy(accountHierarchyRepository);

		Set<AccountHierarchy> setToSave = new HashSet<>();
		Map<ObjectId, AccountHierarchy> projectDataMap = new HashMap<>();

		for (JiraIssue jiraIssue : jiraIssueList) {

			String projectName = jiraIssue.getProjectName();
			String sprintName = jiraIssue.getSprintName();
			String sprintBeginDate = jiraIssue.getSprintBeginDate();
			String sprintEndDate = jiraIssue.getSprintEndDate();

			if (StringUtils.isBlank(projectName) || StringUtils.isBlank(sprintName)
					|| StringUtils.isBlank(sprintBeginDate) || StringUtils.isBlank(sprintEndDate)) {
				continue; // Skip this Jira issue if any of the required fields are blank
			}

			ObjectId basicProjectConfigId = new ObjectId(jiraIssue.getBasicProjectConfigId());
			Map<String, SprintDetails> sprintDetailsMap = sprintDetailsSet.stream()
					.filter(sprintDetails -> sprintDetails.getBasicProjectConfigId().equals(basicProjectConfigId))
					.collect(Collectors.toMap(sprintDetails -> sprintDetails.getSprintID().split("_")[0],
							sprintDetails -> sprintDetails));

			AccountHierarchy projectData = projectDataMap.computeIfAbsent(basicProjectConfigId, id -> {
				List<AccountHierarchy> projectDataList = accountHierarchyRepository
						.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, id);
				return projectDataList.isEmpty() ? null : projectDataList.get(0);
			});

			for (String sprintId : jiraIssue.getSprintIdList()) {
				SprintDetails sprintDetails = sprintDetailsMap.get(sprintId);
				if (sprintDetails != null) {
					AccountHierarchy sprintHierarchy = createHierarchyForSprint(sprintDetails,
							projectConfig.getProjectBasicConfig(), projectData, sprintHierarchyLevel);

					setToSaveAccountHierarchy(setToSave, sprintHierarchy, existingHierarchy);

					List<AccountHierarchy> additionalFiltersHierarchies = accountHierarchiesForAdditionalFilters(
							jiraIssue, sprintHierarchy, sprintHierarchyLevel, hierarchyLevelList);
					additionalFiltersHierarchies.forEach(accountHierarchy -> setToSaveAccountHierarchy(setToSave,
							accountHierarchy, existingHierarchy));
				}
			}
		}

		if (!setToSave.isEmpty()) {
			accountHierarchyRepository.saveAll(setToSave);
		}
	}

	/**
	 * @param setToSave
	 * @param accountHierarchy
	 * @param existingHierarchy
	 */
	private void setToSaveAccountHierarchy(Set<AccountHierarchy> setToSave, AccountHierarchy accountHierarchy,
			Map<Pair<String, String>, AccountHierarchy> existingHierarchy) {
		if (StringUtils.isNotBlank(accountHierarchy.getParentId())) {
			AccountHierarchy exHiery = existingHierarchy
					.get(Pair.of(accountHierarchy.getNodeId(), accountHierarchy.getPath()));

			if (null == exHiery) {
				accountHierarchy.setCreatedDate(LocalDateTime.now());
				setToSave.add(accountHierarchy);
			}
		}
	}

	private AccountHierarchy createHierarchyForSprint(SprintDetails sprintDetails,
			ProjectBasicConfig projectBasicConfig, AccountHierarchy projectHierarchy, HierarchyLevel hierarchyLevel) {
		AccountHierarchy accountHierarchy = null;
		try {

			accountHierarchy = new AccountHierarchy();
			accountHierarchy.setBasicProjectConfigId(projectBasicConfig.getId());
			accountHierarchy.setIsDeleted(JiraConstants.FALSE);
			accountHierarchy.setLabelName(hierarchyLevel.getHierarchyLevelId());

			String sprintName = (String) PropertyUtils.getSimpleProperty(sprintDetails, "sprintName");
			String sprintId = (String) PropertyUtils.getSimpleProperty(sprintDetails, "sprintID");

			accountHierarchy.setNodeId(sprintId);
			accountHierarchy
					.setNodeName(sprintName + JiraConstants.COMBINE_IDS_SYMBOL + projectBasicConfig.getProjectName());

			accountHierarchy.setBeginDate((String) PropertyUtils.getSimpleProperty(sprintDetails, "startDate"));
			accountHierarchy.setEndDate((String) PropertyUtils.getSimpleProperty(sprintDetails, "endDate"));

			accountHierarchy.setPath(new StringBuffer(56).append(projectHierarchy.getNodeId())
					.append(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER).append(projectHierarchy.getPath()).toString());
			accountHierarchy.setParentId(projectHierarchy.getNodeId());

		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			log.error("Jira Processor Failed to get Account Hierarchy data {}", e);
		}
		return accountHierarchy;
	}

	private List<AccountHierarchy> accountHierarchiesForAdditionalFilters(JiraIssue jiraIssue,
			AccountHierarchy sprintHierarchy, HierarchyLevel sprintHierarchyLevel,
			List<HierarchyLevel> hierarchyLevelList) {

		List<AccountHierarchy> accountHierarchies = new ArrayList<>();
		List<AdditionalFilter> additionalFilters = ListUtils.emptyIfNull(jiraIssue.getAdditionalFilters());

		List<String> additionalFilterCategoryIds = hierarchyLevelList.stream()
				.filter(x -> x.getLevel() > sprintHierarchyLevel.getLevel()).map(HierarchyLevel::getHierarchyLevelId)
				.collect(Collectors.toList());

		additionalFilters.forEach(additionalFilter -> {
			if (additionalFilterCategoryIds.contains(additionalFilter.getFilterId())) {
				String labelName = additionalFilter.getFilterId();
				additionalFilter.getFilterValues().forEach(additionalFilterValue -> {
					AccountHierarchy adFilterAccountHierarchy = new AccountHierarchy();
					adFilterAccountHierarchy.setLabelName(labelName);
					adFilterAccountHierarchy.setNodeId(additionalFilterValue.getValueId());
					adFilterAccountHierarchy.setNodeName(additionalFilterValue.getValue());
					adFilterAccountHierarchy.setParentId(sprintHierarchy.getNodeId());
					adFilterAccountHierarchy.setPath(sprintHierarchy.getNodeId()
							+ CommonConstant.ACC_HIERARCHY_PATH_SPLITTER + sprintHierarchy.getPath());
					adFilterAccountHierarchy.setBasicProjectConfigId(new ObjectId(jiraIssue.getBasicProjectConfigId()));
					accountHierarchies.add(adFilterAccountHierarchy);
				});
			}

		});

		return accountHierarchies;
	}

	/*
	 * * Set Details related to issues with Epic Issue type
	 *
	 * @param fieldMapping
	 *
	 * @param jiraIssue
	 *
	 * @param fields
	 */
	private void setEpicIssueData(FieldMapping fieldMapping, JiraIssue jiraIssue, Map<String, IssueField> fields) {
		if (fields.get(fieldMapping.getEpicJobSize()) != null
				&& fields.get(fieldMapping.getEpicJobSize()).getValue() != null) {
			String fieldValue = getFieldValue(fieldMapping.getEpicJobSize(), fields);
			jiraIssue.setJobSize(Double.parseDouble(fieldValue));

		}
		if (fields.get(fieldMapping.getEpicRiskReduction()) != null
				&& fields.get(fieldMapping.getEpicRiskReduction()).getValue() != null) {
			String fieldValue = getFieldValue(fieldMapping.getEpicRiskReduction(), fields);
			jiraIssue.setRiskReduction(Double.parseDouble(fieldValue));

		}
		if (fields.get(fieldMapping.getEpicTimeCriticality()) != null
				&& fields.get(fieldMapping.getEpicTimeCriticality()).getValue() != null) {
			String fieldValue = getFieldValue(fieldMapping.getEpicTimeCriticality(), fields);
			jiraIssue.setTimeCriticality(Double.parseDouble(fieldValue));

		}
		if (fields.get(fieldMapping.getEpicUserBusinessValue()) != null
				&& fields.get(fieldMapping.getEpicUserBusinessValue()).getValue() != null) {
			String fieldValue = getFieldValue(fieldMapping.getEpicUserBusinessValue(), fields);
			jiraIssue.setBusinessValue(Double.parseDouble(fieldValue));

		}
		if (fields.get(fieldMapping.getEpicWsjf()) != null
				&& fields.get(fieldMapping.getEpicWsjf()).getValue() != null) {
			String fieldValue = getFieldValue(fieldMapping.getEpicWsjf(), fields);
			jiraIssue.setWsjf(Double.parseDouble(fieldValue));

		}
		double costOfDelay = jiraIssue.getBusinessValue() + jiraIssue.getRiskReduction()
				+ jiraIssue.getTimeCriticality();
		jiraIssue.setCostOfDelay(costOfDelay);

		if (fields.get(fieldMapping.getEpicPlannedValue()) != null
				&& fields.get(fieldMapping.getEpicPlannedValue()).getValue() != null) {
			String fieldValue = getFieldValue(fieldMapping.getEpicPlannedValue(), fields);
			jiraIssue.setEpicPlannedValue(Double.parseDouble(fieldValue));
		}

		if (fields.get(fieldMapping.getEpicAchievedValue()) != null
				&& fields.get(fieldMapping.getEpicAchievedValue()).getValue() != null) {
			String fieldValue = getFieldValue(fieldMapping.getEpicAchievedValue(), fields);
			jiraIssue.setEpicAchievedValue(Double.parseDouble(fieldValue));
		}

	}

	private void setEstimates(JiraIssue jiraIssue, Issue issue) {
		if (null != issue.getTimeTracking()) {
			jiraIssue.setOriginalEstimateMinutes(issue.getTimeTracking().getOriginalEstimateMinutes());
			jiraIssue.setRemainingEstimateMinutes(issue.getTimeTracking().getRemainingEstimateMinutes());
		}
	}

	private void setDueDates(JiraIssue jiraIssue, Issue issue, Map<String, IssueField> fields,
			FieldMapping fieldMapping) {
		if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateField())) {
			if (fieldMapping.getJiraDueDateField().equalsIgnoreCase(CommonConstant.DUE_DATE)
					&& ObjectUtils.isNotEmpty(issue.getDueDate())) {
				jiraIssue.setDueDate(JiraProcessorUtil.deodeUTF8String(issue.getDueDate()).split("T")[0]
						.concat(DateUtil.ZERO_TIME_ZONE_FORMAT));
			} else if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateCustomField())
					&& ObjectUtils.isNotEmpty(fields.get(fieldMapping.getJiraDueDateCustomField()))) {
				IssueField issueField = fields.get(fieldMapping.getJiraDueDateCustomField());
				if (ObjectUtils.isNotEmpty(issueField.getValue())) {
					jiraIssue.setDueDate(JiraProcessorUtil.deodeUTF8String(issueField.getValue()).split("T")[0]
							.concat(DateUtil.ZERO_TIME_ZONE_FORMAT));
				}
			}
		}
		if (StringUtils.isNotEmpty(fieldMapping.getJiraDevDueDateCustomField())
				&& ObjectUtils.isNotEmpty(fields.get(fieldMapping.getJiraDevDueDateCustomField()))) {
			IssueField issueField = fields.get(fieldMapping.getJiraDevDueDateCustomField());
			if (ObjectUtils.isNotEmpty(issueField.getValue())) {
				jiraIssue.setDevDueDate((JiraProcessorUtil.deodeUTF8String(issueField.getValue()).split("T")[0]
						.concat(DateUtil.ZERO_TIME_ZONE_FORMAT)));
			}
		}
	}

	/**
	 * setting Url to jiraIssue
	 *
	 * @param ticketNumber
	 * @param jiraIssue
	 * @param projectConfig
	 */
	private void setURL(String ticketNumber, JiraIssue jiraIssue, ProjectConfFieldMapping projectConfig) {
		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		Boolean cloudEnv = connectionOptional.isPresent() ? connectionOptional.map(Connection::isCloudEnv).get()
				: Boolean.FALSE;
		String baseUrl = connectionOptional.isPresent() ? connectionOptional.map(Connection::getBaseUrl).orElse("")
				: "";
		baseUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/");
		if (cloudEnv) {
			baseUrl = baseUrl.equals("") ? ""
					: baseUrl + jiraProcessorConfig.getJiraCloudDirectTicketLinkKey() + ticketNumber;
		} else {
			baseUrl = baseUrl.equals("") ? ""
					: baseUrl + jiraProcessorConfig.getJiraDirectTicketLinkKey() + ticketNumber;
		}
		jiraIssue.setUrl(baseUrl);
	}

	private void processAndSaveProjectStatusCategory(List<Status> listOfProjectStatus, String basicProjectConfigId) {
		if (CollectionUtils.isNotEmpty(listOfProjectStatus)) {
			JiraIssueReleaseStatus jiraIssueReleaseStatus = jiraIssueReleaseStatusRepository
					.findByBasicProjectConfigId(basicProjectConfigId);
			if (jiraIssueReleaseStatus == null) {
				jiraIssueReleaseStatus = new JiraIssueReleaseStatus();
				jiraIssueReleaseStatus.setBasicProjectConfigId(basicProjectConfigId);
			}
			Map<Long, String> toDosList = new HashMap<>();
			Map<Long, String> inProgressList = new HashMap<>();
			Map<Long, String> closedList = new HashMap<>();

			listOfProjectStatus.stream().forEach(status -> {
				if (JiraConstants.TO_DO.equals(status.getStatusCategory().getName())) {
					toDosList.put(status.getId(), status.getName());
				} else if (JiraConstants.DONE.equals(status.getStatusCategory().getName())) {
					closedList.put(status.getId(), status.getName());
				} else {
					inProgressList.put(status.getId(), status.getName());
				}
			});
			jiraIssueReleaseStatus.setToDoList(toDosList);
			jiraIssueReleaseStatus.setInProgressList(inProgressList);
			jiraIssueReleaseStatus.setClosedList(closedList);
			jiraIssueReleaseStatusRepository.save(jiraIssueReleaseStatus);
			log.debug("saved project status category");
		}
	}

	// for fetch, parse & update based on issuesKeys
	public int processesJiraIssuesSprintFetch(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter, // NOSONAR
			boolean isOffline, List<String> issueKeys) {
		PSLogData psLogData = new PSLogData();
		psLogData.setProjectName(projectConfig.getProjectName());
		psLogData.setKanban(FALSE);
		int savedIssuesCount = 0;
		int total = 0;

		boolean processorFetchingComplete = false;
		try {
			boolean dataExist = (jiraIssueRepository
					.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);

			int pageSize = jiraAdapter.getPageSize();

			boolean hasMore = true;

			boolean latestDataFetched = false;

			Set<SprintDetails> setForCacheClean = new HashSet<>();

			int sprintCount = jiraProcessorConfig.getSprintCountForCacheClean();

			for (int i = 0; hasMore; i += pageSize) {
				Instant startProcessingJiraIssues = Instant.now();
				SearchResult searchResult = jiraAdapter.getIssuesSprint(projectConfig, i, issueKeys);
				List<Issue> issues = getIssuesFromResult(searchResult);
				if (total == 0) {
					total = getTotal(searchResult);
					psLogData.setTotalFetchedIssues(String.valueOf(total));
				}

				// in case of offline method issues size can be greater than
				// pageSize, increase page size so that same issues not read

				if (isOffline && issues.size() >= pageSize) {
					pageSize = issues.size() + 1;
				}
				if (CollectionUtils.isNotEmpty(issues)) {
					List<JiraIssue> jiraIssues = saveJiraIssueDetails(issues, projectConfig, setForCacheClean,
							jiraAdapter, true, true);

					savedIssuesCount += issues.size();
					savingIssueLogs(savedIssuesCount, jiraIssues, startProcessingJiraIssues, false, psLogData);
				}

				if (!dataExist && !latestDataFetched && setForCacheClean.size() > sprintCount) {
					latestDataFetched = cleanCache();
					setForCacheClean.clear();
					log.info("latest sprint fetched cache cleaned.");
				}
				// will result in an extra call if number of results == pageSize
				// but I would rather do that then complicate the jira client
				// implementation

				if (issues.size() < pageSize) {
					break;
				}
				TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
			}
			processorFetchingComplete = true;
		} catch (JSONException e) {
			log.error("Error while updating Story information in sprintFetch", e,
					kv(CommonConstant.PSLOGDATA, psLogData));
		} catch (InterruptedException e) { // NOSONAR
			log.error("Interrupted exception thrown during sprintFetch", e, kv(CommonConstant.PSLOGDATA, psLogData));
			processorFetchingComplete = false;
		} finally {
			boolean isAttemptSuccess = isAttemptSuccess(total, savedIssuesCount, processorFetchingComplete, psLogData);
			psLogData.setAction(CommonConstant.FETCHING_ISSUE);
			if (!isAttemptSuccess) {
				psLogData.setProjectExecutionStatus(String.valueOf(isAttemptSuccess));
				log.error("Error in Fetching Issues through JQL during active SprintFetch",
						kv(CommonConstant.PSLOGDATA, psLogData));
			}
		}

		return savedIssuesCount;
	}

}