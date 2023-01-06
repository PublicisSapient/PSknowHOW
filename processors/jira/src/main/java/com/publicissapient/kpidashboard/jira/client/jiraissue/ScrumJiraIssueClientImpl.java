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

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
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
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueSprint;
import com.publicissapient.kpidashboard.common.model.jira.ReleaseVersion;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
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
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
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
import java.util.stream.Collectors;

/**
 * This is an implemented/extended storyDataClient for configured Scrum
 * projects, Which extracts the story data using the java JIRA api, and store it
 * in a MongoDB collection for Custom API calls.
 */
@Service
@Slf4j
public class ScrumJiraIssueClientImpl extends JiraIssueClient {// NOPMD

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
	 * @return Count of Jira Issues processed for scrum project
	 */
	@Override
	public int processesJiraIssues(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter, boolean isOffline) {
		if(projectConfig.getProjectToolConfig().isQueryEnabled()){
			return processesJiraIssuesJQL(projectConfig, jiraAdapter, isOffline);
		}else{
			return processesJiraIssuesBoard(projectConfig, jiraAdapter, isOffline);
		}
	}

	private int processesJiraIssuesJQL(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter, boolean isOffline) {

		int savedIsuesCount = 0;
		int total = 0;

		Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType = new HashMap<>();
		setStartDate(jiraProcessorConfig);
		ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
				projectConfig.getBasicProjectConfigId().toHexString());
		boolean processorFetchingComplete = false;
		try {
			boolean dataExist = (jiraIssueRepository
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

			boolean latestDataFetched = false;

			Set<SprintDetails> setForCacheClean = new HashSet<>();
			String userTimeZone = jiraAdapter.getUserTimeZone(projectConfig);
			int sprintCount = jiraProcessorConfig.getSprintCountForCacheClean();

			for (int i = 0; hasMore; i += pageSize) {
				SearchResult searchResult = jiraAdapter.getIssues(projectConfig, maxChangeDatesByIssueTypeWithAddedTime,
						userTimeZone, i, dataExist);
				List<Issue> issues = getIssuesFromResult(searchResult);
				if (total == 0) {
					total = getTotal(searchResult);
				}

				// in case of offline method issues size can be greater than
				// pageSize, increase page size so that same issues not read

				if (isOffline && issues.size() >= pageSize) {
					pageSize = issues.size() + 1;
				}
				if (CollectionUtils.isNotEmpty(issues)) {

					List<JiraIssue> jiraIssues = saveJiraIssueDetails(issues, projectConfig, setForCacheClean,
							jiraAdapter,false);
					findLastSavedJiraIssueByType(jiraIssues, lastSavedJiraIssueChangedDateByType);
					savedIsuesCount += issues.size();
				}

				if (!dataExist && !latestDataFetched && setForCacheClean.size() > sprintCount) {
					latestDataFetched = cleanCache();
					setForCacheClean.clear();
					log.info("latest sprint fetched cache cleaned.");
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
		} catch (InterruptedException e) {
			log.error("Interrupted exception thrown.", e);
			lastSavedJiraIssueChangedDateByType.clear();
			processorFetchingComplete = false;
		}finally {
			boolean isAttemptSuccess = isAttemptSuccess(total, savedIsuesCount, processorFetchingComplete);
			if (!isAttemptSuccess) {
				lastSavedJiraIssueChangedDateByType.clear();
				processorExecutionTraceLog.setLastSuccessfulRun(null);
			}else{
				processorExecutionTraceLog.setLastSuccessfulRun(DateUtil.dateTimeFormatter(LocalDateTime.now(),QUERYDATEFORMAT));
			}
			saveExecutionTraceLog(processorExecutionTraceLog, lastSavedJiraIssueChangedDateByType, isAttemptSuccess);
		}

		return savedIsuesCount;
	}

	private int processesJiraIssuesBoard(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter, boolean isOffline) {

		int savedIsuesCount = 0;
		int total = 0;

		Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType = new HashMap<>();
		setStartDate(jiraProcessorConfig);
		ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(
				projectConfig.getBasicProjectConfigId().toHexString());
		boolean processorFetchingComplete = false;
		try {
			sprintClient.createSprintDetailBasedOnBoard(projectConfig, jiraAdapter);
			boolean dataExist = (jiraIssueRepository
					.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);
			//write get logic to fetch last successful updated date.
			String queryDate = getDeltaDate(processorExecutionTraceLog.getLastSuccessfulRun());
			Set<SprintDetails> setForCacheClean = new HashSet<>();
			String userTimeZone = jiraAdapter.getUserTimeZone(projectConfig);
			int sprintCount = jiraProcessorConfig.getSprintCountForCacheClean();
			List<BoardDetails> boardDetailsList = projectConfig.getProjectToolConfig().getBoards();
			for(BoardDetails board : boardDetailsList) {
				int boardTotal = 0;
				boolean latestDataFetched = false;
				int pageSize = jiraAdapter.getPageSize();
				boolean hasMore = true;
				for (int i = 0; hasMore; i += pageSize) {
					SearchResult searchResult = jiraAdapter.getIssues(board,projectConfig, queryDate,
							userTimeZone, i, dataExist);
					List<Issue> issues = getIssuesFromResult(searchResult);
					if (boardTotal == 0) {
						boardTotal = getTotal(searchResult);
						total += boardTotal;
					}

					// in case of offline method issues size can be greater than
					// pageSize, increase page size so that same issues not read

					if (isOffline && issues.size() >= pageSize) {
						pageSize = issues.size() + 1;
					}
					if (CollectionUtils.isNotEmpty(issues)) {

						List<JiraIssue> jiraIssues = saveJiraIssueDetails(issues, projectConfig, setForCacheClean,
								jiraAdapter, true);
						savedIsuesCount += issues.size();
					}

					if (!latestDataFetched && setForCacheClean.size() > sprintCount) {
						latestDataFetched = cleanCache();
						setForCacheClean.clear();
						log.info("latest sprint fetched cache cleaned.");
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
				log.info("fetching epic");
				List<Issue> epicIssue = jiraAdapter.getEpic(projectConfig,board.getBoardId());
				saveJiraIssueDetails(epicIssue, projectConfig, setForCacheClean,
						jiraAdapter, true);
			}
			processorFetchingComplete = true;
		} catch (JSONException e) {
			log.error("Error while updating Story information in scrum client", e);
			lastSavedJiraIssueChangedDateByType.clear();
		} catch (InterruptedException e) {
			log.error("Interrupted exception thrown.", e);
			lastSavedJiraIssueChangedDateByType.clear();
			processorFetchingComplete = false;
		}finally {
			boolean isAttemptSuccess = isAttemptSuccess(total, savedIsuesCount, processorFetchingComplete);
			if (!isAttemptSuccess) {
				lastSavedJiraIssueChangedDateByType.clear();
				processorExecutionTraceLog.setLastSuccessfulRun(null);
			}else{
				processorExecutionTraceLog.setLastSuccessfulRun(DateUtil.dateTimeFormatter(LocalDateTime.now(),QUERYDATEFORMAT));
			}
			saveExecutionTraceLog(processorExecutionTraceLog, lastSavedJiraIssueChangedDateByType, isAttemptSuccess);
		}

		return savedIsuesCount;

	}

	private void findLastSavedJiraIssueByType(List<JiraIssue> jiraIssues,
											  Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType) {
		Map<String, List<JiraIssue>> issuesByType = CollectionUtils.emptyIfNull(jiraIssues)
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
			Set<SprintDetails> setForCacheClean, JiraAdapter jiraAdapter, boolean dataFromBoard) throws JSONException,InterruptedException {

		List<JiraIssue> jiraIssuesToSave = new ArrayList<>();
		List<JiraIssueCustomHistory> jiraIssueHistoryToSave = new ArrayList<>();

		if (null == currentPagedJiraRs) {
			log.error("JIRA Processor | No list of current paged JIRA's issues found");
			return jiraIssuesToSave;
		}

		Map<String, String> issueEpics = new HashMap<>();
		Set<SprintDetails> sprintDetailsSet = new LinkedHashSet<>();
		ObjectId jiraProcessorId = jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();
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

			JiraIssue jiraIssue= getJiraIssue(projectConfig, issueId);
			JiraIssueCustomHistory jiraIssueHistory=getIssueCustomHistory(projectConfig, issueNumber);

			Map<String, IssueField> fields = JiraIssueClientUtil.buildFieldMap(issue.getFields());

			IssueType issueType = issue.getIssueType();
			User assignee = issue.getAssignee();

			IssueField epic = fields.get(fieldMapping.getEpicName());
			IssueField sprint = fields.get(fieldMapping.getSprintName());

			//set URL to jiraIssue
			setURL(issue.getKey(),jiraIssue,projectConfig);

			// Add RCA to JiraIssue
			setRCA(fieldMapping, issue, jiraIssue, fields);

			// Add device platform filed to issue
			setDevicePlatform(fieldMapping, jiraIssue, fields);

			// Add UAT/Third Party identification field to JiraIssue
			setThirdPartyDefectIdentificationField(fieldMapping, issue, jiraIssue, fields);

			if (issueTypeNames.contains(
					JiraProcessorUtil.deodeUTF8String(issueType.getName()).toLowerCase(Locale.getDefault())) || dataFromBoard) {
				log.debug(String.format("[%-12s] %s", JiraProcessorUtil.deodeUTF8String(issue.getKey()),
						JiraProcessorUtil.deodeUTF8String(issue.getSummary())));
				// collectorId
				jiraIssue.setProcessorId(jiraProcessorId);

				// ID
				jiraIssue.setIssueId(JiraProcessorUtil.deodeUTF8String(issue.getId()));

				// Type
				jiraIssue.setTypeId(JiraProcessorUtil.deodeUTF8String(issueType.getId()));
				jiraIssue.setTypeName(JiraProcessorUtil.deodeUTF8String(issueType.getName()));

				setDefectIssueType(jiraIssue, issueType, fieldMapping);

				// Label
				jiraIssue.setLabels(JiraIssueClientUtil.getLabelsList(issue));
				processJiraIssueData(jiraIssue, issue, fields, fieldMapping, jiraProcessorConfig);

				// Set project specific details
				setProjectSpecificDetails(projectConfig, jiraIssue, issue);

				// Set additional filters
				setAdditionalFilters(jiraIssue, issue, projectConfig);

				setStoryLinkWithDefect(issue, jiraIssue);

				// ADD QA identification field to feature
				setQADefectIdentificationField(fieldMapping, issue, jiraIssue, fields);
				setProductionDefectIdentificationField(fieldMapping, issue, jiraIssue, fields);

				setIssueTechStoryType(fieldMapping, issue, jiraIssue, fields);
				jiraIssue.setAffectedVersions(JiraIssueClientUtil.getAffectedVersions(issue));
				setIssueEpics(issueEpics, epic, jiraIssue);

				setJiraIssueValues(jiraIssue, issue, fieldMapping, fields);

				processSprintData(jiraIssue, sprint, projectConfig, sprintDetailsSet);

				setJiraAssigneeDetails(jiraIssue, assignee);

				setEstimates(jiraIssue, issue);

				// setting filter data from JiraIssue to
				// jira_issue_custom_history
				setJiraIssueHistory(jiraIssueHistory, jiraIssue, issue, fieldMapping);
				if (StringUtils.isNotBlank(jiraIssue.getProjectID())) {
					jiraIssuesToSave.add(jiraIssue);
					jiraIssueHistoryToSave.add(jiraIssueHistory);
				}

			}
		}

		// Saving back to MongoDB
		jiraIssueRepository.saveAll(jiraIssuesToSave);
		jiraIssueCustomHistoryRepository.saveAll(jiraIssueHistoryToSave);
		saveAccountHierarchy(jiraIssuesToSave, projectConfig);
		if(!dataFromBoard) {
			sprintClient.processSprints(projectConfig, sprintDetailsSet, jiraAdapter);
		}
		setForCacheClean.addAll(sprintDetailsSet.stream()
				.filter(sprint -> !sprint.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_FUTURE))
				.collect(Collectors.toSet()));
		return jiraIssuesToSave;
	}

	private JiraIssueCustomHistory getIssueCustomHistory(ProjectConfFieldMapping projectConfig, String issueId) {
		JiraIssueCustomHistory jiraIssueHistory;
		jiraIssueHistory=findOneJiraIssueHistory(issueId, projectConfig.getBasicProjectConfigId().toString());
		if(jiraIssueHistory==null){
			jiraIssueHistory=new JiraIssueCustomHistory();
		}
		return jiraIssueHistory;
	}

	private JiraIssue getJiraIssue(ProjectConfFieldMapping projectConfig, String issueId) {
		JiraIssue jiraIssue;
		jiraIssue=findOneJiraIssue(issueId, projectConfig.getBasicProjectConfigId().toString());
		if(jiraIssue==null){
			jiraIssue=new JiraIssue();
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
			FieldMapping fieldMapping) {

		jiraIssueHistory.setProjectID(jiraIssue.getProjectName());
		jiraIssueHistory.setProjectComponentId(jiraIssue.getProjectID());
		jiraIssueHistory.setProjectKey(jiraIssue.getProjectKey());
		jiraIssueHistory.setStoryType(jiraIssue.getTypeName());
		jiraIssueHistory.setAdditionalFilters(jiraIssue.getAdditionalFilters());
		jiraIssueHistory.setUrl(jiraIssue.getUrl());
		jiraIssueHistory.setDescription(jiraIssue.getName());
		// This method is not setup method. write it to keep
		// custom history
		processJiraIssueHistory(jiraIssueHistory, jiraIssue, issue, fieldMapping);

		jiraIssueHistory.setBasicProjectConfigId(jiraIssue.getBasicProjectConfigId());
	}

	/**
	 * Sets Story Link with Defect
	 *
	 * @param issue
	 * @param jiraIssue
	 */
	private void setStoryLinkWithDefect(Issue issue, JiraIssue jiraIssue) {
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
	 * Finds one JiraIssue by issueId
	 *
	 * @param issueId
	 *            jira issueId
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return JiraIssue corresponding to provided IssueId in DB
	 */
	private JiraIssue findOneJiraIssue(String issueId, String basicProjectConfigId) {
		List<JiraIssue> jiraIssues = jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(StringEscapeUtils.escapeHtml4(issueId),
				basicProjectConfigId);

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
			}
			// Use the latest sprint
			// if any sprint date is blank set that sprint to JiraIssue
			// because this sprint is
			// future sprint and Jira issue should be tagged with latest
			// sprint
			SprintDetails sprint = sprints.stream().filter(s -> StringUtils.isBlank(s.getStartDate())).findFirst()
					.orElse(sprints.get(sprints.size() - 1));
			String sprintId = sprint.getOriginalSprintId() + JiraConstants.COMBINE_IDS_SYMBOL
					+ jiraIssue.getProjectName() + JiraConstants.COMBINE_IDS_SYMBOL
					+ projectConfig.getBasicProjectConfigId();

			jiraIssue.setSprintName(sprint.getSprintName() == null ? StringUtils.EMPTY : sprint.getSprintName());
			jiraIssue.setSprintID(sprint.getOriginalSprintId() == null ? StringUtils.EMPTY : sprintId);
			jiraIssue.setSprintBeginDate(sprint.getStartDate() == null ? StringUtils.EMPTY
					: JiraProcessorUtil.getFormattedDate(sprint.getStartDate()));
			jiraIssue.setSprintEndDate(sprint.getEndDate() == null ? StringUtils.EMPTY
					: JiraProcessorUtil.getFormattedDate(sprint.getEndDate()));
			jiraIssue.setSprintAssetState(sprint.getState() == null ? StringUtils.EMPTY : sprint.getState());

			sprint.setSprintID(sprintId);
			sprintDetailsSet.add(sprint);
		} else {
			log.error("JIRA Processor | Failed to obtain sprint data for {}", sValue);
		}

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
	 * @param fieldMapping
	 *            Project field Mapping
	 */
	private void processJiraIssueHistory(JiraIssueCustomHistory jiraIssueCustomHistory, JiraIssue jiraIssue,
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
			if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
				jiraIssueCustomHistory.setDefectStoryID(jiraIssue.getDefectStoryID());
			}
			List<JiraIssueSprint> listIssueSprint = getChangeLog(jiraIssue, changeLogList, issue.getCreationDate(),
					fieldMapping);
			jiraIssueCustomHistory.setStorySprintDetails(listIssueSprint);
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
			List<ChangelogGroup> changeLogList, FieldMapping fieldMapping) {
		List<JiraIssueSprint> listIssueSprint = getChangeLog(jiraIssue, changeLogList, issue.getCreationDate(),
				fieldMapping);
		jiraIssueCustomHistory.setStoryID(jiraIssue.getNumber());
		jiraIssueCustomHistory.setStorySprintDetails(listIssueSprint);
		jiraIssueCustomHistory.setCreatedDate(issue.getCreationDate());

		// estimate
		jiraIssueCustomHistory.setEstimate(jiraIssue.getEstimate());
		jiraIssueCustomHistory.setBufferedEstimateTime(jiraIssue.getBufferedEstimateTime());
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
			jiraIssueCustomHistory.setDefectStoryID(jiraIssue.getDefectStoryID());
		}

	}

	/**
	 * Process change log and create array of status in Jira issue history
	 *
	 * @param jiraIssue
	 *            JiraIssue
	 * @param changeLogList
	 *            ChangeLogList
	 * @param issueCreatedDate
	 *            Jira Issue creation date
	 * @param fieldMapping
	 *            Field Config Mapping
	 * @return
	 */
	private List<JiraIssueSprint> getChangeLog(JiraIssue jiraIssue, List<ChangelogGroup> changeLogList, // NOPMD
																										// //NOSONAR
			DateTime issueCreatedDate, FieldMapping fieldMapping) {

		List<JiraIssueSprint> issueHistory = new ArrayList<>();
		List<String> jiraStatusForDevelopment = fieldMapping.getJiraStatusForDevelopment();
		List<String> jiraStatusForQa = fieldMapping.getJiraStatusForQa();
		// creating first entry of issue
		if (null != issueCreatedDate) {
			JiraIssueSprint jiraIssueSprint = new JiraIssueSprint();
			jiraIssueSprint.setActivityDate(issueCreatedDate);
			jiraIssueSprint.setFromStatus(fieldMapping.getStoryFirstStatus());
			jiraIssueSprint.setSprintId("");
			jiraIssueSprint.setSprintComponentId("");
			jiraIssueSprint.setStatus("");
			issueHistory.add(jiraIssueSprint);
		}
		Map<String, String> values = new HashMap<>();

		if (CollectionUtils.isNotEmpty(changeLogList)) {
			for (ChangelogGroup history : changeLogList) {
				getHistory(history, values, jiraIssue, issueHistory, jiraStatusForDevelopment, jiraStatusForQa);
			}
		}
		/**
		 * check if no sprint found in changelog but sprint present in issue
		 * update sprint in all changelog
		 */
		if (StringUtils.isEmpty(values.get("fromSprint")) && StringUtils.isEmpty(values.get("toSprint"))
				&& !StringUtils.isEmpty(jiraIssue.getSprintName())) {
			updateChangeLogWithSprint(issueHistory, jiraIssue);
		}
		return issueHistory;
	}

	/**
	 * @param history
	 * @param values
	 * @param jiraIssue
	 * @param issueHistory
	 * @param jiraStatusForDevelopment
	 * @param jiraStatusForQa
	 * @return
	 */
	private void getHistory(ChangelogGroup history, Map<String, String> values, JiraIssue jiraIssue,
			List<JiraIssueSprint> issueHistory, List<String> jiraStatusForDevelopment, List<String> jiraStatusForQa) {
		String fromSprint = "";
		String fromSprintId = "";
		String toSprint = "";
		String toSprintId = "";
		String sprintStatus = "";
		boolean sprintUpdated = true;
		boolean sprintChanged = false;

		for (ChangelogItem changelogItem : history.getItems()) {

			setJiraIssueForTestAutomated(changelogItem, jiraIssue, history);

			if (changelogItem.getField().equalsIgnoreCase(JiraConstants.SPRINT)) {
				fromSprint = changelogItem.getFromString() == null ? "" : changelogItem.getFromString();
				fromSprintId = changelogItem.getFrom();
				toSprint = changelogItem.getToString() == null ? null
						: changelogItem.getToString().replace(fromSprint + ",", "").trim();

				toSprintId = changelogItem.getTo();
				sprintChanged = true;
			} else if (changelogItem.getField().equalsIgnoreCase(JiraConstants.STATUS)) {
				JiraIssueSprint jiraIssueSprint = new JiraIssueSprint();
				jiraIssueSprint.setStatus(sprintStatus);
				jiraIssueSprint.setSprintId(toSprint);

				setSprintComponentId(jiraIssueSprint, toSprintId, jiraIssue);

				jiraIssueSprint.setStatus(sprintStatus);
				jiraIssueSprint.setFromStatus(changelogItem.getToString());
				jiraIssueSprint.setActivityDate(history.getCreated());
				issueHistory.add(jiraIssueSprint);
			}
			if (sprintUpdated && sprintChanged) {
				setIssueHistory(issueHistory, fromSprintId, jiraIssue);
				sprintUpdated = false;
			}

			/*
			 * check if only sprint changed. In this case only sprint name need
			 * to be updated with last status
			 */
			sprintChanged = updateIfSprintChanged(jiraIssue, toSprint, toSprintId, sprintChanged, issueHistory);
			values.put("fromSprint", fromSprint);
			values.put("toSprint", toSprint);
		}
	}

	/**
	 * @param jiraIssueSprint
	 * @param toSprintId
	 * @param jiraIssue
	 */
	private void setSprintComponentId(JiraIssueSprint jiraIssueSprint, String toSprintId, JiraIssue jiraIssue) {
		if (StringUtils.isNotBlank(toSprintId)) {
			jiraIssueSprint.setSprintComponentId(toSprintId + "_" + jiraIssue.getProjectName());
		} else {
			jiraIssueSprint.setSprintComponentId(toSprintId);
		}
	}

	/**
	 * @param changelogItem
	 * @param jiraIssue
	 * @param history
	 */
	private void setJiraIssueForTestAutomated(ChangelogItem changelogItem, JiraIssue jiraIssue,
			ChangelogGroup history) {
		if (changelogItem.getField().equalsIgnoreCase(JiraConstants.TEST_AUTOMATED)) {
			if (changelogItem.getToString().equalsIgnoreCase(JiraConstants.YES)) {
				jiraIssue.setTestAutomatedDate(JiraProcessorUtil
						.getFormattedDate(JiraProcessorUtil.deodeUTF8String(history.getCreated().toString())));
			} else {
				jiraIssue.setTestAutomatedDate("");

			}
		}
	}

	/**
	 * @param issueHistory
	 * @param fromSprintId
	 * @param jiraIssue
	 */
	private void setIssueHistory(List<JiraIssueSprint> issueHistory, String fromSprintId, JiraIssue jiraIssue) {
		for (int i = 0; i < issueHistory.size() - 1; i++) {
			JiraIssueSprint fsprint = issueHistory.get(i);
			String[] toSprintIdArrFrom = {};

			if (fromSprintId != null && fromSprintId.contains(",")) {
				toSprintIdArrFrom = fromSprintId.split(",");
			}
			if (toSprintIdArrFrom.length > 0) {
				fsprint.setSprintId(fromSprintId);
				if (StringUtils.isNotBlank(fromSprintId)) {
					fsprint.setSprintComponentId(fromSprintId + "_" + jiraIssue.getProjectName());
				} else {
					fsprint.setSprintComponentId(fromSprintId);
				}
			}
			issueHistory.set(i, fsprint);
		}
	}

	private boolean updateIfSprintChanged(JiraIssue jiraIssue, String toSprint, String toSprintId,
			boolean sprintChanged, List<JiraIssueSprint> issueHistory) {
		boolean changed = sprintChanged;
		if (sprintChanged && toSprint != null
				&& !(toSprint.equals(issueHistory.get(issueHistory.size() - 1).getSprintId()))) {
			JiraIssueSprint fsprint = issueHistory.get(issueHistory.size() - 1);

			fsprint.setSprintId(toSprint);
			if (StringUtils.isNotBlank(toSprintId)) {
				fsprint.setSprintComponentId(toSprintId + "_" + jiraIssue.getProjectName());
			} else {
				fsprint.setSprintComponentId(toSprintId);
			}

			issueHistory.set(issueHistory.size() - 1, fsprint);
			changed = false;
		}
		return changed;
	}

	/**
	 * Updates Change log with Sprint
	 *
	 * @param issueHistory
	 *            List of JiraIssueSprint containing History data
	 * @param jiraIssue
	 *            JiraIssue
	 */
	private void updateChangeLogWithSprint(List<JiraIssueSprint> issueHistory, JiraIssue jiraIssue) {
		for (int i = 0; i < issueHistory.size(); i++) {
			JiraIssueSprint fsprint = issueHistory.get(i);
			fsprint.setSprintId(jiraIssue.getSprintName());
			fsprint.setSprintComponentId(jiraIssue.getSprintID());
			fsprint.setStatus(jiraIssue.getSprintAssetState());
			issueHistory.set(i, fsprint);
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

	/**
	 * Saves accountHierarchy for filter
	 *
	 * @param jiraIssueList
	 *            list of jira issues
	 * @param projectConfig
	 *            Project Configuration Map
	 */
	private void saveAccountHierarchy(List<JiraIssue> jiraIssueList, ProjectConfFieldMapping projectConfig) {

		List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
				.getFullHierarchyLevels(projectConfig.isKanban());
		Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));

		HierarchyLevel sprintHierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);

		Map<Pair<String, String>, AccountHierarchy> existingHierarchy = JiraIssueClientUtil
				.getAccountHierarchy(accountHierarchyRepository);

		Set<AccountHierarchy> setToSave = new HashSet<>();
		for (JiraIssue jiraIssue : jiraIssueList) {
			if (StringUtils.isNotBlank(jiraIssue.getProjectName()) && StringUtils.isNotBlank(jiraIssue.getSprintName())
					&& StringUtils.isNotBlank(jiraIssue.getSprintBeginDate())
					&& StringUtils.isNotBlank(jiraIssue.getSprintEndDate())) {

				AccountHierarchy projectData = accountHierarchyRepository
						.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT,
								new ObjectId(jiraIssue.getBasicProjectConfigId()))
						.get(0);

				AccountHierarchy sprintHierarchy = createHierarchyForSprint(jiraIssue,
						projectConfig.getProjectBasicConfig(), projectData, sprintHierarchyLevel);

				setToSaveAccountHierarchy(setToSave, sprintHierarchy, existingHierarchy);

				List<AccountHierarchy> additionalFiltersHierarchies = accountHierarchiesForAdditionalFilters(jiraIssue,
						sprintHierarchy, sprintHierarchyLevel, hierarchyLevelList);
				additionalFiltersHierarchies.forEach(
						accountHierarchy -> setToSaveAccountHierarchy(setToSave, accountHierarchy, existingHierarchy));

			}

		}
		if (CollectionUtils.isNotEmpty(setToSave)) {
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

	private AccountHierarchy createHierarchyForSprint(JiraIssue jiraIssue, ProjectBasicConfig projectBasicConfig,
			AccountHierarchy projectHierarchy, HierarchyLevel hierarchyLevel) {
		AccountHierarchy accountHierarchy = null;
		try {

			accountHierarchy = new AccountHierarchy();
			accountHierarchy.setBasicProjectConfigId(projectBasicConfig.getId());
			accountHierarchy.setIsDeleted(JiraConstants.FALSE);
			accountHierarchy.setLabelName(hierarchyLevel.getHierarchyLevelId());
			String sprintName = (String) PropertyUtils.getSimpleProperty(jiraIssue, "sprintName");
			String sprintId = (String) PropertyUtils.getSimpleProperty(jiraIssue, "sprintID");

			accountHierarchy.setNodeId(sprintId);
			accountHierarchy.setNodeName(sprintName + JiraConstants.COMBINE_IDS_SYMBOL + jiraIssue.getProjectName());

			accountHierarchy.setBeginDate((String) PropertyUtils.getSimpleProperty(jiraIssue, "sprintBeginDate"));
			accountHierarchy.setEndDate((String) PropertyUtils.getSimpleProperty(jiraIssue, "sprintEndDate"));
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

	}

	private void setEstimates(JiraIssue jiraIssue, Issue issue) {
		if (null != issue.getTimeTracking()) {
			jiraIssue.setOriginalEstimateMinutes(issue.getTimeTracking().getOriginalEstimateMinutes());
			jiraIssue.setRemainingEstimateMinutes(issue.getTimeTracking().getRemainingEstimateMinutes());
		}
	}

	/**
	 * setting Url to jiraIssue
	 * @param ticketNumber
	 * @param jiraIssue
	 * @param projectConfig
	 */
	private void setURL(String ticketNumber, JiraIssue jiraIssue, ProjectConfFieldMapping projectConfig) {
		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		Boolean cloudEnv = connectionOptional.map(Connection::isCloudEnv).get();
		String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
		baseUrl= baseUrl + (baseUrl.endsWith("/") ? "" : "/");
		if(cloudEnv){
			baseUrl=baseUrl.equals("")?"": baseUrl+jiraProcessorConfig.getJiraCloudDirectTicketLinkKey() + ticketNumber;
		}else{
			baseUrl=baseUrl.equals("")?"": baseUrl+jiraProcessorConfig.getJiraDirectTicketLinkKey() + ticketNumber;
		}
		jiraIssue.setUrl(baseUrl);
	}
}