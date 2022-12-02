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

import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
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
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
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

	/**
	 * Explicitly updates queries for the source system, and initiates the
	 * update to MongoDB from those calls.
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
		if (projectConfig.getProjectToolConfig().isQueryEnabled()) {
			return processesJiraIssuesJQL(projectConfig, jiraAdapter, isOffline);
		} else {
			return processesJiraIssuesBoard(projectConfig, jiraAdapter, isOffline);
		}

	}

	private int processesJiraIssuesBoard(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter, boolean isOffline) {
		int savedIsuesCount = 0;
		int total = 0;
		Map<String, LocalDateTime> lastSavedKanbanJiraIssueChangedDateByType = new HashMap<>();
		setStartDate(jiraProcessorConfig);
		ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
				projectConfig.getBasicProjectConfigId().toHexString());
		boolean processorFetchingComplete = false;
		try {
			boolean dataExist = (kanbanJiraRepo
					.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);

			String queryDate = getDeltaDate(processorExecutionTraceLog.getLastSuccessfulRun());
			String userTimeZone = jiraAdapter.getUserTimeZone(projectConfig);
			List<BoardDetails> boardDetailsList = projectConfig.getProjectToolConfig().getBoards();
			for(BoardDetails board : boardDetailsList) {
				int pageSize = jiraAdapter.getPageSize();
				boolean hasMore = true;
				int boardTotal = 0;
				for (int i = 0; hasMore; i += pageSize) {
					SearchResult searchResult = jiraAdapter.getIssues(board, projectConfig, queryDate,
							userTimeZone, i, dataExist);
					List<Issue> issues = getIssuesFromResult(searchResult);
					if (boardTotal == 0) {
						boardTotal = getTotal(searchResult);
						total+= boardTotal;
					}

					List<Issue> purgeIssues = Lists.newArrayList();
					if (isOffline && issues.size() >= pageSize) {
						pageSize = issues.size() + 1;
					}
					if (CollectionUtils.isNotEmpty(issues)) {
						List<KanbanJiraIssue> kanbanJiraIssues = saveJiraIssueDetails(issues, projectConfig);
						findLastSavedKanbanJiraIssueByType(kanbanJiraIssues, lastSavedKanbanJiraIssueChangedDateByType);
						savedIsuesCount += issues.size();
					}
					if (CollectionUtils.isNotEmpty(purgeIssues)) {
						purgeJiraIssues(purgeIssues, projectConfig);
					}
					if (issues.size() < pageSize) {
						break;
					}
				}

				log.info("fetching epic");
				List<Issue> epicIssue = jiraAdapter.getEpic(projectConfig,board.getBoardId());
				saveJiraIssueDetails(epicIssue, projectConfig);
			}
			processorFetchingComplete = true;
		} catch (JSONException e) {
			log.error("JIRA Processor | Error while updating Story information in kanban client", e);
			lastSavedKanbanJiraIssueChangedDateByType.clear();
		}catch (InterruptedException e) {
			log.error("Interrupted exception thrown.", e);
			lastSavedKanbanJiraIssueChangedDateByType.clear();
			processorFetchingComplete = false;
		} finally {
			boolean isAttemptSuccess = isAttemptSuccess(total, savedIsuesCount, processorFetchingComplete);
			if (!isAttemptSuccess) {
				processorExecutionTraceLog.setLastSuccessfulRun(null);
				lastSavedKanbanJiraIssueChangedDateByType.clear();
			}else{
				processorExecutionTraceLog.setLastSuccessfulRun(DateUtil.dateTimeFormatter(LocalDateTime.now(),QUERYDATEFORMAT));
			}
			saveExecutionTraceLog(processorExecutionTraceLog, lastSavedKanbanJiraIssueChangedDateByType,
					isAttemptSuccess);
		}

		return savedIsuesCount;
	}

	public int processesJiraIssuesJQL(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter, boolean isOffline) {
		int savedIsuesCount = 0;
		int total = 0;
		Map<String, LocalDateTime> lastSavedKanbanJiraIssueChangedDateByType = new HashMap<>();
		setStartDate(jiraProcessorConfig);
		ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
				projectConfig.getBasicProjectConfigId().toHexString());
		boolean processorFetchingComplete = false;
		try {

			boolean dataExist = (kanbanJiraRepo
					.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);

			Map<String, LocalDateTime> maxChangeDatesByIssueType = getLastChangedDatesByIssueType(
					projectConfig.getBasicProjectConfigId(), projectConfig.getFieldMapping());

			Map<String, LocalDateTime> maxChangeDatesByIssueTypeWithAddedTime = new HashMap<>();

			maxChangeDatesByIssueType.forEach((k, v) -> {
				long extraMinutes = jiraProcessorConfig.getMinsToReduce();
				maxChangeDatesByIssueTypeWithAddedTime.put(k, v.minusMinutes(extraMinutes));
			});
			int pageSize = jiraAdapter.getPageSize();

			boolean hasMore = true;

			String userTimeZone = jiraAdapter.getUserTimeZone(projectConfig);
			for (int i = 0; hasMore; i += pageSize) {
				SearchResult searchResult = jiraAdapter.getIssues(projectConfig, maxChangeDatesByIssueTypeWithAddedTime,
						userTimeZone, i, dataExist);
				List<Issue> issues = getIssuesFromResult(searchResult);
				if (total == 0) {
					total = getTotal(searchResult);
				}

				List<Issue> purgeIssues = Lists.newArrayList();
				if (isOffline && issues.size() >= pageSize) {
					pageSize = issues.size() + 1;
				}
				if (CollectionUtils.isNotEmpty(issues)) {
					List<KanbanJiraIssue> kanbanJiraIssues = saveJiraIssueDetails(issues, projectConfig);
					findLastSavedKanbanJiraIssueByType(kanbanJiraIssues, lastSavedKanbanJiraIssueChangedDateByType);
					savedIsuesCount += issues.size();
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
			log.error("JIRA Processor | Error while updating Story information in kanban client", e);
			lastSavedKanbanJiraIssueChangedDateByType.clear();
		}catch (InterruptedException e) {
			log.error("Interrupted exception thrown.", e);
			lastSavedKanbanJiraIssueChangedDateByType.clear();
			processorFetchingComplete = false;
		} finally {
			boolean isAttemptSuccess = isAttemptSuccess(total, savedIsuesCount, processorFetchingComplete);
			if (!isAttemptSuccess) {
				processorExecutionTraceLog.setLastSuccessfulRun(null);
				lastSavedKanbanJiraIssueChangedDateByType.clear();
			}else{
				processorExecutionTraceLog.setLastSuccessfulRun(DateUtil.dateTimeFormatter(LocalDateTime.now(),QUERYDATEFORMAT));
			}
			saveExecutionTraceLog(processorExecutionTraceLog, lastSavedKanbanJiraIssueChangedDateByType,
					isAttemptSuccess);
		}

		return savedIsuesCount;
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
			Map<String, LocalDateTime> lastSavedKanbanJiraIssueChangedDateByType, boolean isSuccess) {

		if (lastSavedKanbanJiraIssueChangedDateByType.isEmpty()) {
			processorExecutionTraceLog.setLastSavedEntryUpdatedDateByType(null);
		} else {
			processorExecutionTraceLog.setLastSavedEntryUpdatedDateByType(lastSavedKanbanJiraIssueChangedDateByType);
		}

		processorExecutionTraceLog.setExecutionSuccess(isSuccess);
		processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		processorExecutionTraceLogService.save(processorExecutionTraceLog);
	}

	private ProcessorExecutionTraceLog createTraceLog(String basicProjectConfigId) {
		List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService
				.getTraceLogs(ProcessorConstants.JIRA, basicProjectConfigId);
		ProcessorExecutionTraceLog processorExecutionTraceLog = null;

		if (CollectionUtils.isNotEmpty(traceLogs)) {
			processorExecutionTraceLog = traceLogs.get(0);
			if(null == processorExecutionTraceLog.getLastSuccessfulRun()){
				processorExecutionTraceLog.setLastSuccessfulRun(jiraProcessorConfig.getStartDate());
			}
		}else {
			processorExecutionTraceLog = new ProcessorExecutionTraceLog();
			processorExecutionTraceLog.setProcessorName(ProcessorConstants.JIRA);
			processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
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

	private boolean isAttemptSuccess(int total, int savedCount, boolean processorFetchingComplete) {
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
			String issueNumber = JiraProcessorUtil.deodeUTF8String(issue.getKey());
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
	 * Updates the MongoDB with a JSONArray received from the source system
	 * back-end with story-based data.
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

		if (null == currentPagedJiraRs) {
			log.error("JIRA Processor |. No list of current paged JIRA's issues found");
			return kanbanIssuesToSave;
		}
		log.debug("Jira response:", currentPagedJiraRs.size());

		Map<String, String> issueEpics = new HashMap<>();
		ObjectId jiraIssueId = jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();

		for (Issue issue : currentPagedJiraRs) {
			FieldMapping fieldMapping = projectConfig.getFieldMapping();
			if (null == fieldMapping) {
				return kanbanIssuesToSave;
			}
			Set<String> issueTypeNames = JiraIssueClientUtil.getIssueTypeNames(fieldMapping);
			String issueId = JiraProcessorUtil.deodeUTF8String(issue.getId());
			String issueNumber = JiraProcessorUtil.deodeUTF8String(issue.getKey());
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

			// Add device platform filed to issue
			setDevicePlatform(fieldMapping, jiraIssue, fields);
			if (issueTypeNames.contains(
					JiraProcessorUtil.deodeUTF8String(issueType.getName()).toLowerCase(Locale.getDefault()))) {
				// collectorId
				jiraIssue.setProcessorId(jiraIssueId);
				// ID
				jiraIssue.setIssueId(JiraProcessorUtil.deodeUTF8String(issue.getId()));
				// Type
				jiraIssue.setTypeId(JiraProcessorUtil.deodeUTF8String(issueType.getId()));
				jiraIssue.setTypeName(JiraProcessorUtil.deodeUTF8String(issueType.getName()));

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

				setJiraAssigneeDetails(jiraIssue, assignee);
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

		return kanbanIssuesToSave;
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

	private Map<String, LocalDateTime> getLastChangedDatesByIssueType(ObjectId basicProjectConfigId,
																	  FieldMapping fieldMapping) {

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

			} else {
				lastUpdatedDateByIssueType.put(issueType, configuredStartDate);
			}
		}

		return lastUpdatedDateByIssueType;
	}

	private boolean isDataExist(boolean dataExist) {
		if (!dataExist) {
			dataExist = true;
		}
		return dataExist;
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
			addHistoryInJiraIssue(jiraIssueCustomHistory, jiraIssue, modChangeLogList, fieldMapping);
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
	 * @param fieldMapping
	 *            FieldMapping config
	 */
	private void addHistoryInJiraIssue(KanbanIssueCustomHistory jiraIssueCustomHistory, KanbanJiraIssue jiraIssue,
			List<ChangelogGroup> changeLogList, FieldMapping fieldMapping) {
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
			jiraIssueCustomHistory.setDefectStoryID(jiraIssue.getDefectStoryID());
		}
		createKanbanIssueHistory(jiraIssueCustomHistory, jiraIssue, changeLogList, fieldMapping);
		jiraIssueCustomHistory.setEstimate(jiraIssue.getEstimate());
	}

	/**
	 * Creates Issue Kanban history details for delta changed statuses start
	 *
	 * @param jiraIssueCustomHistory
	 *            JiraIssueCustomHistory
	 * @param jiraIssue
	 *            jiraIssue
	 * @param changeLogList
	 *            Change Log list
	 * @param fieldMapping
	 *            List of JiraIssueCustomHistory
	 */
	private void createKanbanIssueHistory(KanbanIssueCustomHistory jiraIssueCustomHistory, KanbanJiraIssue jiraIssue,
			List<ChangelogGroup> changeLogList, FieldMapping fieldMapping) {
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
		List<String> jiraStatusForDevelopment = fieldMapping.getJiraStatusForDevelopment();
		List<String> jiraStatusForQa = fieldMapping.getJiraStatusForQa();

		// creating first entry of issue
		if (null != issueCreatedDate) {
			KanbanIssueHistory kanbanHistory = new KanbanIssueHistory();
			kanbanHistory.setActivityDate(issueCreatedDate.toString());
			kanbanHistory.setStatus(fieldMapping.getStoryFirstStatus());
			historyDetails.add(kanbanHistory);
		}
		if (CollectionUtils.isNotEmpty(changeLogList)) {
			for (ChangelogGroup history : changeLogList) {
				historyDetails.addAll(getIssueHistory(jiraIssue, history, jiraStatusForDevelopment, jiraStatusForQa));
			}
		}
		return historyDetails;
	}

	private List<KanbanIssueHistory> getIssueHistory(KanbanJiraIssue jiraIssue, ChangelogGroup history,
			List<String> jiraStatusForDevelopment, List<String> jiraStatusForQa) {
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

		Map<Pair<String, String>, KanbanAccountHierarchy> existingKanbanHierarchy = getKanbanAccountHierarchy();
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
	 * Fetches all saved kanban account hierarchy.
	 *
	 * @return Map<Pair < String, String>, KanbanAccountHierarchy>
	 */
	private Map<Pair<String, String>, KanbanAccountHierarchy> getKanbanAccountHierarchy() {
		List<KanbanAccountHierarchy> accountHierarchyList = kanbanAccountHierarchyRepo.findAll();
		return accountHierarchyList.stream()
				.collect(Collectors.toMap(p -> Pair.of(p.getNodeId(), p.getPath()), p -> p));
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
	 * Sets Device Platform
	 *
	 * @param fieldMapping
	 *            fieldMapping provided by the User
	 * @param jiraIssue
	 *            JiraIssue instance
	 * @param fields
	 *            Map of Issue Fields
	 */
	public void setDevicePlatform(FieldMapping fieldMapping, KanbanJiraIssue jiraIssue,
			Map<String, IssueField> fields) {

		try {
			String devicePlatform = null;
			if (fields.get(fieldMapping.getDevicePlatform()) != null
					&& fields.get(fieldMapping.getDevicePlatform()).getValue() != null) {
				devicePlatform = ((JSONObject) fields.get(fieldMapping.getDevicePlatform()).getValue())
						.getString(JiraConstants.VALUE);
			}
			jiraIssue.setDevicePlatform(devicePlatform);
		} catch (JSONException e) {
			log.error("JIRA Processor | Error while parsing Device Platform ");
		}
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
		Integer timeSpent = 0;
		if (issue.getTimeTracking() != null && issue.getTimeTracking().getTimeSpentMinutes() != null) {
			timeSpent = issue.getTimeTracking().getTimeSpentMinutes();
		} else if (fields.get(JiraConstants.AGGREGATED_TIME_SPENT) != null
				&& fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue() != null) {
			timeSpent = ((Integer) fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue()) / 60;
		}
		jiraIssue.setTimeSpentInMinutes(timeSpent);

		setEnvironmentImpacted(jiraIssue, fields, fieldMapping);

		jiraIssue.setChangeDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(changeDate)));
		jiraIssue.setIsDeleted(JiraConstants.FALSE);

		jiraIssue.setOwnersState(Arrays.asList("Active"));

		jiraIssue.setOwnersChangeDate(Collections.<String>emptyList());

		jiraIssue.setOwnersIsDeleted(Collections.<String>emptyList());

		// Created Date
		jiraIssue.setCreatedDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(createdDate)));

	}

	/**
	 * Sets Issue Tech Story Type after identifying s whether a story is tech
	 * story or simple Jira issue. There can be possible 3 ways to identify a
	 * tech story 1. Specific 'label' is maintained 2. 'Issue type' itself is a
	 * 'Tech Story' 3. A separate 'custom field' is maintained
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
	 */
	public void setJiraAssigneeDetails(KanbanJiraIssue jiraIssue, User user) {
		if (user == null) {
			jiraIssue.setOwnersUsername(Collections.<String>emptyList());
			jiraIssue.setOwnersShortName(Collections.<String>emptyList());
			jiraIssue.setOwnersID(Collections.<String>emptyList());
			jiraIssue.setOwnersFullName(Collections.<String>emptyList());
		} else {
			List<String> assigneeKey = new ArrayList<>();
			List<String> assigneeName = new ArrayList<>();
			if (user.getName().isEmpty() || (user.getName() == null)) {
				assigneeKey = new ArrayList<>();
				assigneeName = new ArrayList<>();
			} else {
				assigneeKey.add(JiraProcessorUtil.deodeUTF8String(user.getName()));
				assigneeName.add(JiraProcessorUtil.deodeUTF8String(user.getName()));
				jiraIssue.setAssigneeId(user.getName());
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
					value = ((Double) fields.get(estimationField).getValue()) / 3600D;
					valueString = String.valueOf(value.doubleValue());
				} else if (JiraConstants.BUFFERED_ESTIMATION.equalsIgnoreCase(estimationCriteria)) {
					if (fields.get(estimationField).getValue() instanceof Integer) {
						value = ((Double) fields.get(estimationField).getValue()) / 3600D;
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
	 * Sets the environment impacted custom field.
	 *
	 * @param jiraIssue
	 *            JiraIssue instance
	 * @param fields
	 *            Map of Issue Fields
	 * @param fieldMapping
	 *            fieldMapping provided by the User
	 */
	private void setEnvironmentImpacted(KanbanJiraIssue jiraIssue, Map<String, IssueField> fields,
			FieldMapping fieldMapping) {
		if (fields.get(fieldMapping.getEnvImpacted()) != null
				&& fields.get(fieldMapping.getEnvImpacted()).getValue() != null) {
			JSONObject customField;
			try {
				customField = new JSONObject(fields.get(fieldMapping.getEnvImpacted()).getValue().toString());
				jiraIssue.setEnvImpacted(JiraProcessorUtil.deodeUTF8String(customField.get(JiraConstants.VALUE)));
			} catch (JSONException e) {
				log.error("JIRA Processor | Error while parsing the environment custom field Environment", e);
			}

		}
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
			for (IssueLink issueLink : issue.getIssueLinks()) {
				if (CollectionUtils.isNotEmpty(jiraProcessorConfig.getExcludeLinks())
						&& jiraProcessorConfig.getExcludeLinks().stream()
								.anyMatch(issueLink.getIssueLinkType().getDescription()::equalsIgnoreCase)) {
					break;
				}
				defectStorySet.add(issueLink.getTargetIssueKey());
			}
			jiraIssue.setDefectStoryID(defectStorySet);
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
		Boolean cloudEnv = connectionOptional.map(Connection::isCloudEnv).get();
		String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
		baseUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/");
		if (cloudEnv) {
			baseUrl = baseUrl.equals("") ? "" : baseUrl + jiraProcessorConfig.getJiraCloudDirectTicketLinkKey() + ticketNumber;
		} else {
			baseUrl = baseUrl.equals("") ? "" : baseUrl + jiraProcessorConfig.getJiraDirectTicketLinkKey() + ticketNumber;
		}
		kanbanJiraIssue.setUrl(baseUrl);
	}


}
