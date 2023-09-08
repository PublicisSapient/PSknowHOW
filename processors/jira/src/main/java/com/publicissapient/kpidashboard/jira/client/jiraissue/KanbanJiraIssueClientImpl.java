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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.joda.time.DateTime;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.User;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.util.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.util.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.util.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.AdditionalFilterHelper;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This is an implemented/extended storyDataClient for configured kanban
 * projects, Which extracts the story data using the java JIRA api, and store it
 * in a MongoDB collection for Custom API calls.
 */
@Service
@Slf4j
public class KanbanJiraIssueClientImpl extends JiraIssueClient {

	@Autowired
	private JiraProcessorRepository jiraProcessorRepository;

	@Autowired
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;

	@Autowired
	private KanbanJiraIssueRepository kanbanJiraRepo;

	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	private KanbanJiraIssueHistoryRepository kanbanIssueHistoryRepo;

	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Autowired
	private AdditionalFilterHelper additionalFilterHelper;

	@Autowired
	private HierarchyLevelService hierarchyLevelService;

	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

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
	 * @return count of Jira issue processed
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

	private int processesJiraIssuesBoard(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter,
			boolean isOffline) {
		PSLogData psLogData = new PSLogData();
		psLogData.setProjectName(projectConfig.getProjectName());
		psLogData.setKanban("true");
		int savedIsuesCount = 0;
		int total = 0;
		Map<String, LocalDateTime> lastSavedKanbanJiraIssueChangedDateByType = new HashMap<>();
		setStartDate(jiraProcessorConfig);
		ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(projectConfig);
		boolean processorFetchingComplete = false;
		try {
			boolean dataExist = (kanbanJiraRepo
					.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);

			String queryDate = getDeltaDate(processorExecutionTraceLog.getLastSuccessfulRun());
			String userTimeZone = jiraAdapter.getUserTimeZone(projectConfig);
			List<BoardDetails> boardDetailsList = projectConfig.getProjectToolConfig().getBoards();
			for (BoardDetails board : boardDetailsList) {
				psLogData.setBoardId(board.getBoardId());
				int pageSize = jiraAdapter.getPageSize();
				boolean hasMore = true;
				int boardTotal = 0;
				for (int i = 0; hasMore; i += pageSize) {
					Instant startIssueProcessing = Instant.now();
					SearchResult searchResult = jiraAdapter.getIssues(board, projectConfig, queryDate, userTimeZone, i,
							dataExist);
					List<Issue> issues = getIssuesFromResult(searchResult);
					if (boardTotal == 0) {
						boardTotal = getTotal(searchResult);
						total += boardTotal;
						psLogData.setTotalFetchedIssues(String.valueOf(total));
					}

					List<Issue> purgeIssues = Lists.newArrayList();
					if (isOffline && issues.size() >= pageSize) {
						pageSize = issues.size() + 1;
					}
					if (CollectionUtils.isNotEmpty(issues)) {
						List<KanbanJiraIssue> kanbanJiraIssues = saveJiraIssueDetails(issues, projectConfig);
						findLastSavedKanbanJiraIssueByType(kanbanJiraIssues, lastSavedKanbanJiraIssueChangedDateByType);
						savedIsuesCount += issues.size();
						savingIssueLogs(savedIsuesCount, kanbanJiraIssues, startIssueProcessing, false, psLogData);
					}
					if (CollectionUtils.isNotEmpty(purgeIssues)) {
						purgeJiraIssues(purgeIssues, projectConfig);
					}
					if (issues.size() < pageSize) {
						break;
					}
				}
				Instant epicProcessStartTime = Instant.now();
				List<Issue> epicIssue = jiraAdapter.getEpic(projectConfig, board.getBoardId());
				psLogData.setEpicIssuesFetched((epicIssue == null) ? "-1" : String.valueOf(epicIssue.size()));
				List<KanbanJiraIssue> kanbanJiraIssueList = saveJiraIssueDetails(epicIssue, projectConfig);
				savingIssueLogs(kanbanJiraIssueList.size(), kanbanJiraIssueList, epicProcessStartTime, true, psLogData);
			}
			processorFetchingComplete = true;
		} catch (JSONException e) {
			log.error("JIRA Processor | Error while updating Story information in kanban client through board", e,
					kv(CommonConstant.PSLOGDATA, psLogData));
			lastSavedKanbanJiraIssueChangedDateByType.clear();
		} catch (InterruptedException e) {
			log.error("Interrupted exception thrown.", e, kv(CommonConstant.PSLOGDATA, psLogData));
			lastSavedKanbanJiraIssueChangedDateByType.clear();
			processorFetchingComplete = false;
		} finally {
			boolean isAttemptSuccess = isAttemptSuccess(total, savedIsuesCount, processorFetchingComplete, psLogData);
			psLogData.setAction(CommonConstant.PROJECT_EXECUTION_STATUS);
			if (!isAttemptSuccess) {
				processorExecutionTraceLog.setLastSuccessfulRun(null);
				lastSavedKanbanJiraIssueChangedDateByType.clear();
				psLogData.setProjectExecutionStatus(String.valueOf(isAttemptSuccess));
				log.error("Error in Fetching Issues through JQL", kv(CommonConstant.PSLOGDATA, psLogData));
			} else {
				processorExecutionTraceLog
						.setLastSuccessfulRun(DateUtil.dateTimeFormatter(LocalDateTime.now(), QUERYDATEFORMAT));
			}
			saveExecutionTraceLog(processorExecutionTraceLog, lastSavedKanbanJiraIssueChangedDateByType,
					isAttemptSuccess, projectConfig.getProjectBasicConfig());
		}

		return savedIsuesCount;
	}

	public int processesJiraIssuesJQL(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter,
			boolean isOffline) {
		PSLogData psLogData = new PSLogData();
		psLogData.setProjectName(projectConfig.getProjectName());
		psLogData.setKanban("true");
		int savedIsuesCount = 0;
		int total = 0;
		Map<String, LocalDateTime> lastSavedKanbanJiraIssueChangedDateByType = new HashMap<>();
		setStartDate(jiraProcessorConfig);
		ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(projectConfig);
		boolean processorFetchingComplete = false;
		try {

			boolean dataExist = (kanbanJiraRepo
					.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);

			Map<String, LocalDateTime> maxChangeDatesByIssueType = getLastChangedDatesByIssueType(projectConfig);

			Map<String, LocalDateTime> maxChangeDatesByIssueTypeWithAddedTime = new HashMap<>();

			maxChangeDatesByIssueType.forEach((k, v) -> {
				long extraMinutes = jiraProcessorConfig.getMinsToReduce();
				maxChangeDatesByIssueTypeWithAddedTime.put(k, v.minusMinutes(extraMinutes));
			});
			int pageSize = jiraAdapter.getPageSize();

			boolean hasMore = true;

			String userTimeZone = jiraAdapter.getUserTimeZone(projectConfig);
			for (int i = 0; hasMore; i += pageSize) {
				Instant startIssueProcessing = Instant.now();
				SearchResult searchResult = jiraAdapter.getIssues(projectConfig, maxChangeDatesByIssueTypeWithAddedTime,
						userTimeZone, i, dataExist);
				List<Issue> issues = getIssuesFromResult(searchResult);
				if (total == 0) {
					total = getTotal(searchResult);
					psLogData.setTotalFetchedIssues(String.valueOf(total));
				}

				List<Issue> purgeIssues = Lists.newArrayList();
				if (isOffline && issues.size() >= pageSize) {
					pageSize = issues.size() + 1;
				}
				if (CollectionUtils.isNotEmpty(issues)) {
					List<KanbanJiraIssue> kanbanJiraIssues = saveJiraIssueDetails(issues, projectConfig);
					findLastSavedKanbanJiraIssueByType(kanbanJiraIssues, lastSavedKanbanJiraIssueChangedDateByType);
					savedIsuesCount += issues.size();
					savingIssueLogs(savedIsuesCount, kanbanJiraIssues, startIssueProcessing, false, psLogData);
				}
				if (CollectionUtils.isNotEmpty(purgeIssues)) {
					purgeJiraIssues(purgeIssues, projectConfig);
				}
				if (issues.size() < pageSize) {
					break;
				}
			}
			processorFetchingComplete = true;
		} catch (JSONException e) {
			log.error("JIRA Processor | Error while updating Story information in kanban client", e,
					kv(CommonConstant.PSLOGDATA, psLogData));
			lastSavedKanbanJiraIssueChangedDateByType.clear();
		} catch (InterruptedException e) {
			log.error("Interrupted exception thrown.", e, kv(CommonConstant.PSLOGDATA, psLogData));
			lastSavedKanbanJiraIssueChangedDateByType.clear();
			processorFetchingComplete = false;
		} finally {
			boolean isAttemptSuccess = isAttemptSuccess(total, savedIsuesCount, processorFetchingComplete, psLogData);
			psLogData.setAction(CommonConstant.PROJECT_EXECUTION_STATUS);
			if (!isAttemptSuccess) {
				processorExecutionTraceLog.setLastSuccessfulRun(null);
				lastSavedKanbanJiraIssueChangedDateByType.clear();
				psLogData.setProjectExecutionStatus(String.valueOf(isAttemptSuccess));
				log.error("Error in Fetching Issues through board", kv(CommonConstant.PSLOGDATA, psLogData));
			} else {
				processorExecutionTraceLog
						.setLastSuccessfulRun(DateUtil.dateTimeFormatter(LocalDateTime.now(), QUERYDATEFORMAT));
			}
			saveExecutionTraceLog(processorExecutionTraceLog, lastSavedKanbanJiraIssueChangedDateByType,
					isAttemptSuccess, projectConfig.getProjectBasicConfig());
		}

		return savedIsuesCount;
	}

	private void savingIssueLogs(int savedIssuesCount, List<KanbanJiraIssue> kanbanJiraIssues,
			Instant startProcessingJiraIssues, boolean isEpic, PSLogData psLogData) {
		PSLogData saveIssueLog = new PSLogData();
		saveIssueLog.setIssueAndDesc(
				kanbanJiraIssues.stream().map(KanbanJiraIssue::getNumber).collect(Collectors.toList()));
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

	private void saveExecutionTraceLog(ProcessorExecutionTraceLog processorExecutionTraceLog,
			Map<String, LocalDateTime> lastSavedKanbanJiraIssueChangedDateByType, boolean isSuccess,
			ProjectBasicConfig projectBasicConfig) {

		if (lastSavedKanbanJiraIssueChangedDateByType.isEmpty()) {
			processorExecutionTraceLog.setLastSavedEntryUpdatedDateByType(null);
		} else {
			processorExecutionTraceLog.setLastSavedEntryUpdatedDateByType(lastSavedKanbanJiraIssueChangedDateByType);
		}

		processorExecutionTraceLog.setExecutionSuccess(isSuccess);
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		processorExecutionTraceLog.setLastEnableAssigneeToggleState(projectBasicConfig.isSaveAssigneeDetails());
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

	private void findLastSavedKanbanJiraIssueByType(List<KanbanJiraIssue> kanbanJiraIssues,
			Map<String, LocalDateTime> lastSavedKanbanJiraIssueByType) {

		Map<String, List<KanbanJiraIssue>> issuesByType = CollectionUtils
				.emptyIfNull(kanbanJiraIssues).stream().sorted(
						Comparator
								.comparing((KanbanJiraIssue jiraIssue) -> LocalDateTime.parse(jiraIssue.getChangeDate(),
										DateTimeFormatter.ofPattern(JiraConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT)))
								.reversed())
				.collect(Collectors.groupingBy(KanbanJiraIssue::getTypeName));

		issuesByType.forEach((typeName, issues) -> {
			KanbanJiraIssue firstIssue = issues.stream()
					.sorted(Comparator
							.comparing((KanbanJiraIssue jiraIssue) -> LocalDateTime.parse(jiraIssue.getChangeDate(),
									DateTimeFormatter.ofPattern(JiraConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT)))
							.reversed())
					.findFirst().orElse(null);
			if (firstIssue != null) {
				LocalDateTime currentIssueDate = LocalDateTime.parse(firstIssue.getChangeDate(),
						DateTimeFormatter.ofPattern(JiraConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT));
				LocalDateTime capturedDate = lastSavedKanbanJiraIssueByType.get(typeName);

				lastSavedKanbanJiraIssueByType.put(typeName, updatedDateToSave(capturedDate, currentIssueDate));
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

	/**
	 * Purges list of issues provided
	 *
	 * @param purgeIssuesList
	 *            List of issues to be purged
	 * @param projectConfig
	 *            Project Configuration Mapping
	 */
	@Override
	public void purgeJiraIssues(List<Issue> purgeIssuesList, ProjectConfFieldMapping projectConfig) {
		List<KanbanJiraIssue> kanbanIssuesToDelete = Lists.newArrayList();
		List<KanbanIssueCustomHistory> kanbanIssueHistoryToDelete = Lists.newArrayList();
		purgeIssuesList.forEach(issue -> {
			String issueId = JiraProcessorUtil.deodeUTF8String(issue.getId());
			KanbanJiraIssue kanbanJiraIssue = findOneKanbanIssueRepo(issueId,
					projectConfig.getBasicProjectConfigId().toString());
			KanbanIssueCustomHistory kanbanHistory = findOneKanbanIssueCustomHistory(issueId,
					projectConfig.getBasicProjectConfigId().toString());
			if (kanbanJiraIssue != null) {
				kanbanIssuesToDelete.add(kanbanJiraIssue);
			}
			if (kanbanHistory != null) {
				kanbanIssueHistoryToDelete.add(kanbanHistory);
			}

		});
		kanbanJiraRepo.deleteAll(kanbanIssuesToDelete);
		kanbanIssueHistoryRepo.deleteAll(kanbanIssueHistoryToDelete);
	}

	/**
	 * Updates the MongoDB with a JSONArray received from the source system back-end
	 * with story-based data.
	 *
	 * @param currentPagedJiraRs
	 *            A list response of Jira issues from the source system
	 * @throws JSONException
	 *             error while parsing JSON response
	 */
	private List<KanbanJiraIssue> saveJiraIssueDetails(List<Issue> currentPagedJiraRs,
			ProjectConfFieldMapping projectConfig)// NOPMD
			// //NOSONAR
			throws JSONException {

		List<KanbanJiraIssue> kanbanIssuesToSave = new ArrayList<>();
		List<KanbanIssueCustomHistory> kanbanIssueHistoryToSave = new ArrayList<>();
		Set<Assignee> assigneeSetToSave = new HashSet<>();

		if (null == currentPagedJiraRs) {
			log.error("JIRA Processor |. No list of current paged JIRA's issues found");
			return kanbanIssuesToSave;
		}
		log.debug("Jira response:", currentPagedJiraRs.size());

		Map<String, String> issueEpics = new HashMap<>();
		ObjectId jiraIssueId = jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();
		AssigneeDetails assigneeDetails = assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(
				projectConfig.getBasicProjectConfigId().toString(), ProcessorConstants.JIRA);

		for (Issue issue : currentPagedJiraRs) {
			FieldMapping fieldMapping = projectConfig.getFieldMapping();
			if (null == fieldMapping) {
				return kanbanIssuesToSave;
			}
			Set<String> issueTypeNames = JiraIssueClientUtil.getIssueTypeNames(fieldMapping);
			String issueId = JiraProcessorUtil.deodeUTF8String(issue.getId());
			KanbanJiraIssue jiraIssue = getKanbanJiraIssue(projectConfig, issueId);
			KanbanIssueCustomHistory jiraIssueHistory = getKanbanIssueCustomHistory(projectConfig, issue);

			Map<String, IssueField> fields = JiraIssueClientUtil.buildFieldMap(issue.getFields());

			IssueType issueType = issue.getIssueType();
			User assignee = issue.getAssignee();

			IssueField epic = fields.get(fieldMapping.getEpicName());
			// Add url to Issue
			setURL(issue.getKey(), jiraIssue, projectConfig);
			// Add RCA to Issue
			setRCA(fieldMapping, issue, jiraIssue, fields);

			if (issueTypeNames.contains(
					JiraProcessorUtil.deodeUTF8String(issueType.getName()).toLowerCase(Locale.getDefault()))) {
				// collectorId
				jiraIssue.setProcessorId(jiraIssueId);
				// ID
				jiraIssue.setIssueId(JiraProcessorUtil.deodeUTF8String(issue.getId()));
				// Type
				jiraIssue.setTypeId(JiraProcessorUtil.deodeUTF8String(issueType.getId()));
				jiraIssue.setTypeName(JiraProcessorUtil.deodeUTF8String(issueType.getName()));
				jiraIssue.setOriginalType(JiraProcessorUtil.deodeUTF8String(issueType.getName()));

				setEpicLinked(fieldMapping, jiraIssue, fields);

				// Label
				jiraIssue.setLabels(JiraIssueClientUtil.getLabelsList(issue));
				processJiraIssueData(jiraIssue, issue, fields, fieldMapping, jiraProcessorConfig);

				// Set project specific details
				setProjectSpecificDetails(projectConfig, jiraIssue, issue);

				// Set additional filters
				setAdditionalFilters(jiraIssue, issue, projectConfig);

				setStoryLinkWithDefect(issue, jiraIssue);

				// Add Tech Debt Story identificatin to jira issue
				setIssueTechStoryType(fieldMapping, issue, jiraIssue, fields);

				// Affected Version
				jiraIssue.setAffectedVersions(JiraIssueClientUtil.getAffectedVersions(issue));

				setJiraIssuuefields(issue, jiraIssue, fieldMapping, fields, epic, issueEpics);

				setJiraAssigneeDetails(jiraIssue, assignee, assigneeSetToSave, projectConfig);

				setDueDates(jiraIssue, issue, fields, fieldMapping);

				// setting filter data from Jira issue to
				// jira_issue_custom_history
				setJiraIssueHistory(jiraIssueHistory, jiraIssue, issue, fieldMapping);
				// Add Test Automated data to Jira_issue and TestDetails Repo
				if (StringUtils.isNotBlank(jiraIssue.getProjectID())) {
					kanbanIssuesToSave.add(jiraIssue);
					kanbanIssueHistoryToSave.add(jiraIssueHistory);
				}

			}
		}

		// Saving back to MongoDB
		kanbanJiraRepo.saveAll(kanbanIssuesToSave);
		kanbanIssueHistoryRepo.saveAll(kanbanIssueHistoryToSave);
		saveKanbanAccountHierarchy(kanbanIssuesToSave, projectConfig);
		saveAssigneeDetailsToDb(projectConfig, assigneeSetToSave, assigneeDetails);

		return kanbanIssuesToSave;
	}

	private void setEpicLinked(FieldMapping fieldMapping, KanbanJiraIssue jiraIssue, Map<String, IssueField> fields) {
		if (StringUtils.isNotEmpty(fieldMapping.getEpicLink())
				&& fields.get(fieldMapping.getEpicLink()) != null
				&& fields.get(fieldMapping.getEpicLink()).getValue() != null) {
			jiraIssue.setEpicLinked(fields.get((fieldMapping.getEpicLink()).trim()).getValue().toString());
		}
	}

	private void setDueDates(KanbanJiraIssue jiraIssue, Issue issue, Map<String, IssueField> fields,
			FieldMapping fieldMapping) {
		if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateField())) {
			if (fieldMapping.getJiraDueDateField().equalsIgnoreCase(CommonConstant.DUE_DATE)
					&& ObjectUtils.isNotEmpty(issue.getDueDate())) {
				jiraIssue.setDueDate(JiraProcessorUtil.deodeUTF8String(issue.getDueDate()).split("T")[0]
						.concat(DateUtil.ZERO_TIME_ZONE_FORMAT));
			} else if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateCustomField())
					&& ObjectUtils.isNotEmpty(fields.get(fieldMapping.getJiraDueDateCustomField()))) {
				IssueField issueField = fields.get(fieldMapping.getJiraDueDateCustomField());
				if (issueField != null && ObjectUtils.isNotEmpty(issueField.getValue())) {
					jiraIssue.setDueDate(JiraProcessorUtil.deodeUTF8String(issueField.getValue()).split("T")[0]
							.concat(DateUtil.ZERO_TIME_ZONE_FORMAT));
				}
			}
		}
		setDevDueDates(jiraIssue, issue, fields, fieldMapping);
	}

	private void setAdditionalFilters(KanbanJiraIssue jiraIssue, Issue issue, ProjectConfFieldMapping projectConfig) {
		List<AdditionalFilter> additionalFilter = additionalFilterHelper.getAdditionalFilter(issue, projectConfig);
		jiraIssue.setAdditionalFilters(additionalFilter);
	}

	private void setProjectSpecificDetails(ProjectConfFieldMapping projectConfig, KanbanJiraIssue jiraIssue,
			Issue issue) {
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

	private KanbanIssueCustomHistory getKanbanIssueCustomHistory(ProjectConfFieldMapping projectConfig, Issue issue) {
		KanbanIssueCustomHistory jiraIssueHistory = findOneKanbanIssueCustomHistory(issue.getKey(),
				projectConfig.getBasicProjectConfigId().toString());
		if (jiraIssueHistory == null) {
			jiraIssueHistory = new KanbanIssueCustomHistory();
		}
		return jiraIssueHistory;
	}

	private KanbanJiraIssue getKanbanJiraIssue(ProjectConfFieldMapping projectConfig, String issueId) {
		KanbanJiraIssue jiraIssue = findOneKanbanIssueRepo(issueId, projectConfig.getBasicProjectConfigId().toString());
		if (jiraIssue == null) {
			jiraIssue = new KanbanJiraIssue();
		}
		return jiraIssue;
	}

	/**
	 * @param issue
	 * @param jiraIssue
	 * @param fieldMapping
	 * @param fields
	 * @param epic
	 * @param issueEpics
	 */
	private void setJiraIssuuefields(Issue issue, KanbanJiraIssue jiraIssue, FieldMapping fieldMapping,
			Map<String, IssueField> fields, IssueField epic, Map<String, String> issueEpics) {
		// Priority
		if (issue.getPriority() != null) {
			jiraIssue.setPriority(JiraProcessorUtil.deodeUTF8String(issue.getPriority().getName()));
		}
		// Set EPIC issue data for issue type epic
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueEpicType())
				&& fieldMapping.getJiraIssueEpicType().contains(issue.getIssueType().getName())) {
			setEpicIssueData(fieldMapping, jiraIssue, fields);
		}
		// delay processing epic data for performance
		if (epic != null && epic.getValue() != null && !JiraProcessorUtil.deodeUTF8String(epic.getValue()).isEmpty()) {
			issueEpics.put(jiraIssue.getIssueId(), JiraProcessorUtil.deodeUTF8String(epic.getValue()));
		}
	}

	private void setJiraIssueHistory(KanbanIssueCustomHistory jiraIssueHistory, KanbanJiraIssue jiraIssue, Issue issue,
			FieldMapping fieldMapping) {

		jiraIssueHistory.setProjectID(jiraIssue.getProjectName());
		jiraIssueHistory.setProjectComponentId(jiraIssue.getProjectID());
		jiraIssueHistory.setProjectKey(jiraIssue.getProjectKey());
		jiraIssueHistory.setProjectName(jiraIssue.getProjectName());
		jiraIssueHistory.setPriority(jiraIssue.getPriority());
		jiraIssueHistory.setRootCauseList(jiraIssue.getRootCauseList());
		jiraIssueHistory.setStoryType(jiraIssue.getTypeName());
		jiraIssueHistory.setAdditionalFilters(jiraIssue.getAdditionalFilters());
		jiraIssueHistory.setUrl(jiraIssue.getUrl());
		jiraIssueHistory.setDescription(jiraIssue.getName());
		// This method is not setup method. write it to keep
		// custom history
		processJiraIssueHistory(jiraIssueHistory, jiraIssue, issue, fieldMapping);
		jiraIssueHistory.setBasicProjectConfigId(jiraIssue.getBasicProjectConfigId());
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
	 * Process Jira issue History data
	 *
	 * @param jiraIssueCustomHistory
	 *            jiraIssueCustomHistory Object
	 * @param jiraIssue
	 *            JiraIssue
	 * @param issue
	 *            Atlassian Issue
	 * @param fieldMapping
	 *            user provided FieldMapping
	 */
	private void processJiraIssueHistory(KanbanIssueCustomHistory jiraIssueCustomHistory, KanbanJiraIssue jiraIssue,
			Issue issue, FieldMapping fieldMapping) {
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
			addStoryHistory(jiraIssueCustomHistory, jiraIssue, issue, modChangeLogList, fieldMapping);
		} else {
			addHistoryInJiraIssue(jiraIssueCustomHistory, jiraIssue, modChangeLogList);
		}

	}

	/**
	 * Adds Jira issue history
	 *
	 * @param jiraIssueCustomHistory
	 *            JiraIssueCustomHistory
	 * @param jiraIssue
	 *            JiraIssue instance
	 * @param issue
	 *            Atlassian Issue
	 * @param changeLogList
	 *            List of Change log in jira
	 * @param fieldMapping
	 *            FieldMapping config
	 */
	private void addStoryHistory(KanbanIssueCustomHistory jiraIssueCustomHistory, KanbanJiraIssue jiraIssue,
			Issue issue, List<ChangelogGroup> changeLogList, FieldMapping fieldMapping) {
		List<KanbanIssueHistory> kanbanIssueHistoryList = getChangeLog(jiraIssue, changeLogList,
				issue.getCreationDate(), fieldMapping);
		jiraIssueCustomHistory.setStoryID(jiraIssue.getNumber());
		jiraIssueCustomHistory.setHistoryDetails(kanbanIssueHistoryList);
		jiraIssueCustomHistory.setCreatedDate(issue.getCreationDate().toString());
		// estimate
		jiraIssueCustomHistory.setEstimate(jiraIssue.getEstimate());
		jiraIssueCustomHistory.setBufferedEstimateTime(jiraIssue.getBufferedEstimateTime());
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
			jiraIssueCustomHistory.setDefectStoryID(jiraIssue.getDefectStoryID());
		}

	}

	/**
	 * Adds Sprint in Story
	 *
	 * @param jiraIssueCustomHistory
	 *            JiraIssueCustomHistory
	 * @param jiraIssue
	 *            JiraIssue instance
	 * @param changeLogList
	 *            List of Change log in jira
	 */
	private void addHistoryInJiraIssue(KanbanIssueCustomHistory jiraIssueCustomHistory, KanbanJiraIssue jiraIssue,
			List<ChangelogGroup> changeLogList) {
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
			jiraIssueCustomHistory.setDefectStoryID(jiraIssue.getDefectStoryID());
		}
		createKanbanIssueHistory(jiraIssueCustomHistory, changeLogList);
		jiraIssueCustomHistory.setEstimate(jiraIssue.getEstimate());
	}

	/**
	 * Creates Issue Kanban history details for delta changed statuses start
	 *
	 * @param jiraIssueCustomHistory
	 *            JiraIssueCustomHistory
	 * @param changeLogList
	 *            Change Log list
	 */
	private void createKanbanIssueHistory(KanbanIssueCustomHistory jiraIssueCustomHistory,
			List<ChangelogGroup> changeLogList) {
		List<KanbanIssueHistory> issueHistoryList = new ArrayList<>();
		for (ChangelogGroup history : changeLogList) {
			for (ChangelogItem changelogItem : history.getItems()) {
				if (changelogItem.getField().equalsIgnoreCase(JiraConstants.STATUS)) {
					KanbanIssueHistory kanbanIssueHistory = new KanbanIssueHistory();
					kanbanIssueHistory.setStatus(changelogItem.getToString());
					kanbanIssueHistory.setActivityDate(history.getCreated().toString());
					issueHistoryList.add(kanbanIssueHistory);

				}
			}
			jiraIssueCustomHistory.setHistoryDetails(issueHistoryList);
		}

	}

	/**
	 * Process change log and create array of status in Issue history
	 *
	 * @param jiraIssue
	 *            Jiraissue
	 * @param changeLogList
	 *            Changes log list for jira issue
	 * @param issueCreatedDate
	 *            creation date on jira issue
	 * @param fieldMapping
	 *            FielMapping
	 * @return
	 */
	private List<KanbanIssueHistory> getChangeLog(KanbanJiraIssue jiraIssue, List<ChangelogGroup> changeLogList,
			DateTime issueCreatedDate, FieldMapping fieldMapping) {
		List<KanbanIssueHistory> historyDetails = new ArrayList<>();
		// creating first entry of issue
		if (null != issueCreatedDate) {
			KanbanIssueHistory kanbanHistory = new KanbanIssueHistory();
			kanbanHistory.setActivityDate(issueCreatedDate.toString());
			kanbanHistory.setStatus(fieldMapping.getStoryFirstStatus());
			historyDetails.add(kanbanHistory);
		}
		if (CollectionUtils.isNotEmpty(changeLogList)) {
			for (ChangelogGroup history : changeLogList) {
				historyDetails.addAll(getIssueHistory(jiraIssue, history));
			}
		}
		return historyDetails;
	}

	private List<KanbanIssueHistory> getIssueHistory(KanbanJiraIssue jiraIssue, ChangelogGroup history) {
		List<KanbanIssueHistory> historyDetails = new ArrayList<>();
		for (ChangelogItem changelogItem : history.getItems()) {
			if (changelogItem.getField().equalsIgnoreCase(JiraConstants.TEST_AUTOMATED)) {
				if (changelogItem.getToString().equalsIgnoreCase(JiraConstants.YES)) {
					jiraIssue.setTestAutomatedDate(JiraProcessorUtil
							.getFormattedDate(JiraProcessorUtil.deodeUTF8String(history.getCreated().toString())));
				} else {
					jiraIssue.setTestAutomatedDate("");
				}
			}

			if (changelogItem.getField().equalsIgnoreCase(JiraConstants.STATUS)) {
				KanbanIssueHistory kanbanHistory = new KanbanIssueHistory();
				kanbanHistory.setActivityDate(history.getCreated().toString());
				kanbanHistory.setStatus(changelogItem.getToString());
				historyDetails.add(kanbanHistory);
			}
		}
		return historyDetails;

	}

	/**
	 * Find Kanban Jira issue by issueId
	 *
	 * @param issueId
	 *            JiraIssue ID
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return KanbanJiraIssue corresponding to issueId from DB
	 */
	private KanbanJiraIssue findOneKanbanIssueRepo(String issueId, String basicProjectConfigId) {
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
	private KanbanIssueCustomHistory findOneKanbanIssueCustomHistory(String issueId, String basicProjectConfigId) {
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

	/**
	 * Save kanban account hierarchy.
	 *
	 * @param jiraIssueList
	 *            Jiraissue list to be saved in DB
	 * @param projectConfig
	 *            Project configuration Mapping
	 */
	private void saveKanbanAccountHierarchy(List<KanbanJiraIssue> jiraIssueList,
			ProjectConfFieldMapping projectConfig) {

		List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
				.getFullHierarchyLevels(projectConfig.isKanban());
		Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
		HierarchyLevel projectHierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

		Map<Pair<String, String>, KanbanAccountHierarchy> existingKanbanHierarchy = JiraIssueClientUtil
				.getKanbanAccountHierarchy(kanbanAccountHierarchyRepo);
		Set<KanbanAccountHierarchy> accHierarchyToSave = new HashSet<>();

		for (KanbanJiraIssue kanbanJiraIssue : jiraIssueList) {
			if (StringUtils.isNotBlank(kanbanJiraIssue.getProjectName())) {
				KanbanAccountHierarchy projectHierarchy = kanbanAccountHierarchyRepo
						.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT,
								new ObjectId(kanbanJiraIssue.getBasicProjectConfigId()))
						.get(0);

				List<KanbanAccountHierarchy> additionalFiltersHierarchies = accountHierarchiesForAdditionalFilters(
						kanbanJiraIssue, projectHierarchy, projectHierarchyLevel, hierarchyLevelList);

				additionalFiltersHierarchies.forEach(accountHierarchy -> accHierarchyToSave(accountHierarchy,
						existingKanbanHierarchy, accHierarchyToSave));

			}
		}
		if (CollectionUtils.isNotEmpty(accHierarchyToSave)) {
			kanbanAccountHierarchyRepo.saveAll(accHierarchyToSave);
		}
	}

	private List<KanbanAccountHierarchy> accountHierarchiesForAdditionalFilters(KanbanJiraIssue jiraIssue,
			KanbanAccountHierarchy projectHierarchy, HierarchyLevel projectHierarchyLevel,
			List<HierarchyLevel> hierarchyLevelList) {

		List<KanbanAccountHierarchy> accountHierarchies = new ArrayList<>();
		List<AdditionalFilter> additionalFilters = ListUtils.emptyIfNull(jiraIssue.getAdditionalFilters());

		List<String> additionalFilterCategoryIds = hierarchyLevelList.stream()
				.filter(x -> x.getLevel() > projectHierarchyLevel.getLevel()).map(HierarchyLevel::getHierarchyLevelId)
				.collect(Collectors.toList());

		additionalFilters.forEach(additionalFilter -> {
			if (additionalFilterCategoryIds.contains(additionalFilter.getFilterId())) {
				String labelName = additionalFilter.getFilterId();
				additionalFilter.getFilterValues().forEach(additionalFilterValue -> {
					KanbanAccountHierarchy adFilterAccountHierarchy = new KanbanAccountHierarchy();
					adFilterAccountHierarchy.setLabelName(labelName);
					adFilterAccountHierarchy.setNodeId(additionalFilterValue.getValueId());
					adFilterAccountHierarchy.setNodeName(additionalFilterValue.getValue());
					adFilterAccountHierarchy.setParentId(projectHierarchy.getNodeId());
					adFilterAccountHierarchy.setPath(projectHierarchy.getNodeId()
							+ CommonConstant.ACC_HIERARCHY_PATH_SPLITTER + projectHierarchy.getPath());
					adFilterAccountHierarchy.setBasicProjectConfigId(new ObjectId(jiraIssue.getBasicProjectConfigId()));
					accountHierarchies.add(adFilterAccountHierarchy);
				});
			}

		});

		return accountHierarchies;
	}

	private void accHierarchyToSave(KanbanAccountHierarchy accountHierarchy,
			Map<Pair<String, String>, KanbanAccountHierarchy> existingKanbanHierarchy,
			Set<KanbanAccountHierarchy> accHierarchyToSave) {
		if (StringUtils.isNotBlank(accountHierarchy.getParentId())
				|| (StringUtils.isBlank(accountHierarchy.getParentId()))) {
			KanbanAccountHierarchy exHiery = existingKanbanHierarchy
					.get(Pair.of(accountHierarchy.getNodeId(), accountHierarchy.getPath()));

			if (null == exHiery) {
				accountHierarchy.setCreatedDate(LocalDateTime.now());
				accHierarchyToSave.add(accountHierarchy);
			}
		}
	}

	/**
	 * set RCA root cause values
	 *
	 * @param fieldMapping
	 *            fieldMapping provided by the User
	 * @param issue
	 *            issue
	 * @param jiraIssue
	 *            JiraIssue instance
	 * @param fields
	 *            Map of Issue Fields
	 */
	private void setRCA(FieldMapping fieldMapping, Issue issue, KanbanJiraIssue jiraIssue,
			Map<String, IssueField> fields) {
		List<String> rcaList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(fieldMapping.getKanbanRCACountIssueType()) && fieldMapping
				.getKanbanRCACountIssueType().stream().anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)) {
			if (fields.get(fieldMapping.getRootCause()) != null
					&& fields.get(fieldMapping.getRootCause()).getValue() != null) {
				rcaList.addAll(getRootCauses(fieldMapping, fields));
			} else {
				// when issue type defects but did not set root cause value in
				// Jira
				rcaList.add(JiraConstants.RCA_NOT_AVAILABLE);
			}
		}
		jiraIssue.setRootCauseList(rcaList);
	}

	/**
	 * if root cause getting json then story as list of string
	 *
	 * @param fieldMapping
	 * @param fields
	 * @return List<String>
	 */
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

	/**
	 * @param rcaCause
	 * @return String
	 */
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
	 * Process Jira issue Data
	 *
	 * @param jiraIssue
	 *            JiraIssue instance
	 * @param issue
	 *            Atlassian Issue
	 * @param fields
	 *            Map of Issue Fields
	 * @param fieldMapping
	 *            fieldMapping provided by the User
	 * @param jiraProcessorConfig
	 *            Jira processor Configuration
	 * @throws JSONException
	 *             Error while parsing JSON
	 */
	public void processJiraIssueData(KanbanJiraIssue jiraIssue, Issue issue, Map<String, IssueField> fields,
			FieldMapping fieldMapping, JiraProcessorConfig jiraProcessorConfig) throws JSONException {

		String status = issue.getStatus().getName();
		String changeDate = issue.getUpdateDate().toString();
		String createdDate = issue.getCreationDate().toString();
		jiraIssue.setNumber(JiraProcessorUtil.deodeUTF8String(issue.getKey()));
		jiraIssue.setName(JiraProcessorUtil.deodeUTF8String(issue.getSummary()));
		jiraIssue.setStatus(JiraProcessorUtil.deodeUTF8String(status));
		jiraIssue.setState(JiraProcessorUtil.deodeUTF8String(status));

		if (StringUtils.isNotEmpty(fieldMapping.getJiraStatusMappingCustomField())) {
			JSONObject josnObject = (JSONObject) fields.get(fieldMapping.getJiraStatusMappingCustomField()).getValue();
			if (null != josnObject) {
				jiraIssue.setJiraStatus((String) josnObject.get(JiraConstants.VALUE));
			}
		} else {
			jiraIssue.setJiraStatus(issue.getStatus().getName());
		}
		if (issue.getResolution() != null) {
			jiraIssue.setResolution(JiraProcessorUtil.deodeUTF8String(issue.getResolution().getName()));
		}
		setEstimate(jiraIssue, fields, fieldMapping, jiraProcessorConfig);
		setAggregateTimeEstimates(jiraIssue, fields);

		jiraIssue.setChangeDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(changeDate)));
		jiraIssue.setIsDeleted(JiraConstants.FALSE);

		jiraIssue.setOwnersState(Arrays.asList("Active"));

		jiraIssue.setOwnersChangeDate(Collections.<String>emptyList());

		jiraIssue.setOwnersIsDeleted(Collections.<String>emptyList());

		// Created Date
		jiraIssue.setCreatedDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(createdDate)));

	}

	/**
	 * Sets Issue Tech Story Type after identifying s whether a story is tech story
	 * or simple Jira issue. There can be possible 3 ways to identify a tech story
	 * 1. Specific 'label' is maintained 2. 'Issue type' itself is a 'Tech Story' 3.
	 * A separate 'custom field' is maintained
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
	public void setIssueTechStoryType(FieldMapping fieldMapping, Issue issue, KanbanJiraIssue jiraIssue,
			Map<String, IssueField> fields) {
		if (Optional.ofNullable(fieldMapping.getJiraTechDebtIdentification()).isPresent()) {
			if (fieldMapping.getJiraTechDebtIdentification().trim().equalsIgnoreCase(JiraConstants.LABELS)) {
				if (CollectionUtils.containsAny(issue.getLabels(), fieldMapping.getJiraTechDebtValue())) {
					jiraIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
				}
			} else if (fieldMapping.getJiraTechDebtIdentification().trim().equalsIgnoreCase(JiraConstants.ISSUE_TYPE)
					&& fieldMapping.getJiraTechDebtValue().contains(jiraIssue.getTypeName())) {
				jiraIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
			} else if (fieldMapping.getJiraTechDebtIdentification().trim().equalsIgnoreCase(CommonConstant.CUSTOM_FIELD)
					&& null != fields.get(fieldMapping.getJiraTechDebtCustomField())
					&& fields.get(fieldMapping.getJiraTechDebtCustomField().trim()) != null
					&& fields.get(fieldMapping.getJiraTechDebtCustomField().trim()).getValue() != null
					&& CollectionUtils.containsAny(fieldMapping.getJiraTechDebtValue(), JiraIssueClientUtil
							.getListFromJson(fields.get(fieldMapping.getJiraTechDebtCustomField().trim())))) {
				jiraIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
			}
		}

	}

	/**
	 * This method process owner and user details
	 *
	 * @param jiraIssue
	 *            JiraIssue Object to set Owner details
	 * @param user
	 *            Jira issue User Object
	 * @param assigneeSetToSave
	 *            assignees to save
	 * @param projectConfig
	 *            projectconfigfieldmapping
	 */
	public void setJiraAssigneeDetails(KanbanJiraIssue jiraIssue, User user, Set<Assignee> assigneeSetToSave,
			ProjectConfFieldMapping projectConfig) {
		if (user == null) {
			jiraIssue.setOwnersUsername(Collections.<String>emptyList());
			jiraIssue.setOwnersShortName(Collections.<String>emptyList());
			jiraIssue.setOwnersID(Collections.<String>emptyList());
			jiraIssue.setOwnersFullName(Collections.<String>emptyList());
		} else {
			List<String> assigneeKey = new ArrayList<>();
			List<String> assigneeName = new ArrayList<>();
			String uniqueAssigneeId = getAssignee(user);
			if (StringUtils.isEmpty(uniqueAssigneeId)) {
				assigneeKey = new ArrayList<>();
				assigneeName = new ArrayList<>();
			} else {
				assigneeKey.add(JiraProcessorUtil.deodeUTF8String(uniqueAssigneeId));
				assigneeName.add(JiraProcessorUtil.deodeUTF8String(uniqueAssigneeId));
				jiraIssue.setAssigneeId(uniqueAssigneeId);
			}
			jiraIssue.setOwnersShortName(assigneeName);
			jiraIssue.setOwnersUsername(assigneeName);
			jiraIssue.setOwnersID(assigneeKey);

			List<String> assigneeDisplayName = new ArrayList<>();
			if (user.getDisplayName().isEmpty() || (user.getDisplayName() == null)) {
				assigneeDisplayName.add("");
			} else {
				assigneeDisplayName.add(JiraProcessorUtil.deodeUTF8String(user.getDisplayName()));
				jiraIssue.setAssigneeName(user.getDisplayName());
			}
			jiraIssue.setOwnersFullName(assigneeDisplayName);
			if (StringUtils.isNotEmpty(jiraIssue.getAssigneeId())
					&& StringUtils.isNotEmpty(jiraIssue.getAssigneeName())) {
				updateAssigneeDetailsToggleWise(jiraIssue, assigneeSetToSave, projectConfig, assigneeKey, assigneeName,
						assigneeDisplayName);
			}
		}
	}

	private void updateAssigneeDetailsToggleWise(KanbanJiraIssue jiraIssue, Set<Assignee> assigneeSetToSave,
			ProjectConfFieldMapping projectConfig, List<String> assigneeKey, List<String> assigneeName,
			List<String> assigneeDisplayName) {
		if (!projectConfig.getProjectBasicConfig().isSaveAssigneeDetails()) {
			List<String> ownerName = assigneeName.stream().map(JiraIssueClient::hash).collect(Collectors.toList());
			List<String> ownerId = assigneeKey.stream().map(JiraIssueClient::hash).collect(Collectors.toList());
			List<String> ownerFullName = assigneeDisplayName.stream().map(JiraIssueClient::hash)
					.collect(Collectors.toList());
			jiraIssue.setAssigneeId(hash(jiraIssue.getAssigneeId()));
			jiraIssue.setAssigneeName(hash(jiraIssue.getAssigneeId() + jiraIssue.getAssigneeName()));
			jiraIssue.setOwnersShortName(ownerName);
			jiraIssue.setOwnersUsername(ownerName);
			jiraIssue.setOwnersID(ownerId);
			jiraIssue.setOwnersFullName(ownerFullName);
		} else {
			assigneeSetToSave.add(new Assignee(jiraIssue.getAssigneeId(), jiraIssue.getAssigneeName()));
		}
	}

	/**
	 * Sets Estimate
	 *
	 * @param jiraIssue
	 *            JiraIssue instance
	 * @param fields
	 *            Map of Issue Fields
	 * @param fieldMapping
	 *            fieldMapping provided by the User
	 * @param jiraProcessorConfig
	 *            Jira Processor Configuration
	 */
	public void setEstimate(KanbanJiraIssue jiraIssue, Map<String, IssueField> fields, FieldMapping fieldMapping, // NOSONAR
			JiraProcessorConfig jiraProcessorConfig) {
		Double value = 0d;
		String valueString = "0";
		String estimationCriteria = fieldMapping.getEstimationCriteria();
		if (StringUtils.isNotBlank(estimationCriteria)) {
			String estimationField = fieldMapping.getJiraStoryPointsCustomField();
			if (StringUtils.isNotBlank(estimationField) && fields.get(estimationField) != null
					&& fields.get(estimationField).getValue() != null
					&& !JiraProcessorUtil.deodeUTF8String(fields.get(estimationField).getValue()).isEmpty()) {
				if (JiraConstants.ACTUAL_ESTIMATION.equalsIgnoreCase(estimationCriteria)) {
					if (fields.get(estimationField).getValue() instanceof Integer) {
						value = ((Integer) fields.get(estimationField).getValue()) / 3600D;
					} else {
						value = ((Double) (fields.get(estimationField).getValue()));
					}
					valueString = String.valueOf(value.doubleValue());
				} else if (JiraConstants.BUFFERED_ESTIMATION.equalsIgnoreCase(estimationCriteria)) {
					if (fields.get(estimationField).getValue() instanceof Integer) {
						value = ((Integer) fields.get(estimationField).getValue()) / 3600D;
					} else {
						value = ((Double) (fields.get(estimationField).getValue()));
					}
					valueString = String.valueOf(value.doubleValue());

				} else if (JiraConstants.STORY_POINT.equalsIgnoreCase(estimationCriteria)) {
					value = Double
							.parseDouble(JiraProcessorUtil.deodeUTF8String(fields.get(estimationField).getValue()));
					valueString = String.valueOf(value.doubleValue());
				}
			}
		} else {
			// by default storypoints
			IssueField estimationField = fields.get(fieldMapping.getJiraStoryPointsCustomField());
			if (estimationField != null && estimationField.getValue() != null
					&& !JiraProcessorUtil.deodeUTF8String(estimationField.getValue()).isEmpty()) {
				value = Double.parseDouble(JiraProcessorUtil.deodeUTF8String(estimationField.getValue()));
				valueString = String.valueOf(value.doubleValue());
			}
		}
		jiraIssue.setEstimate(valueString);
		jiraIssue.setStoryPoints(value);
	}

	/**
	 * Set Details related to issues with Epic Issue type
	 *
	 * @param fieldMapping
	 * @param jiraIssue
	 * @param fields
	 */
	private void setEpicIssueData(FieldMapping fieldMapping, KanbanJiraIssue jiraIssue,
			Map<String, IssueField> fields) {
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

	}

	private void setStoryLinkWithDefect(Issue issue, KanbanJiraIssue jiraIssue) {
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())
				|| NormalizedJira.TEST_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
			Set<String> defectStorySet = new HashSet<>();
			excludeLinks(issue, defectStorySet);
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
	 * setting Url to KanbanJiraIssue
	 *
	 * @param ticketNumber
	 * @param kanbanJiraIssue
	 * @param projectConfig
	 */
	private void setURL(String ticketNumber, KanbanJiraIssue kanbanJiraIssue, ProjectConfFieldMapping projectConfig) {
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
		kanbanJiraIssue.setUrl(baseUrl);
	}

	private void setAggregateTimeEstimates(KanbanJiraIssue jiraIssue, Map<String, IssueField> fields) {
		Integer timeSpent = 0;
		if (fields.get(JiraConstants.AGGREGATED_TIME_SPENT) != null
				&& fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue() != null) {
			timeSpent = ((Integer) fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue()) / 60;
		}
		jiraIssue.setTimeSpentInMinutes(timeSpent);

		if (fields.get(JiraConstants.AGGREGATED_TIME_ORIGINAL) != null
				&& fields.get(JiraConstants.AGGREGATED_TIME_ORIGINAL).getValue() != null) {
			jiraIssue.setAggregateTimeOriginalEstimateMinutes(
					((Integer) fields.get(JiraConstants.AGGREGATED_TIME_ORIGINAL).getValue()) / 60);

		}
		if (fields.get(JiraConstants.AGGREGATED_TIME_REMAIN) != null
				&& fields.get(JiraConstants.AGGREGATED_TIME_REMAIN).getValue() != null) {
			jiraIssue.setAggregateTimeRemainingEstimateMinutes(
					((Integer) fields.get(JiraConstants.AGGREGATED_TIME_REMAIN).getValue()) / 60);

		}
	}

	/**
	 * save assignee details if exist then update assignee list
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

	private static void setDevDueDates(KanbanJiraIssue jiraIssue, Issue issue, Map<String, IssueField> fields, FieldMapping fieldMapping) {
		if (StringUtils.isNotEmpty(fieldMapping.getJiraDevDueDateField())) {
			if (fieldMapping.getJiraDevDueDateField().equalsIgnoreCase(CommonConstant.DUE_DATE)
					&& ObjectUtils.isNotEmpty(issue.getDueDate())) {
				jiraIssue.setDevDueDate(JiraProcessorUtil.deodeUTF8String(issue.getDueDate()).split("T")[0]
						.concat(DateUtil.ZERO_TIME_ZONE_FORMAT));
			} else if (ObjectUtils.isNotEmpty(fields.get(fieldMapping.getJiraDevDueDateCustomField()))) {
				IssueField issueField = fields.get(fieldMapping.getJiraDevDueDateCustomField());
				if (ObjectUtils.isNotEmpty(issueField.getValue())) {
					jiraIssue.setDevDueDate((JiraProcessorUtil.deodeUTF8String(issueField.getValue()).split("T")[0]
							.concat(DateUtil.ZERO_TIME_ZONE_FORMAT)));
				}
			}
		}
	}

}
