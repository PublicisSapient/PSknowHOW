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

package com.publicissapient.kpidashboard.azure.client.azureissue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.azure.adapter.AzureAdapter;
import com.publicissapient.kpidashboard.azure.adapter.impl.OnlineAdapter;
import com.publicissapient.kpidashboard.azure.adapter.impl.async.ProcessorAzureRestClient;
import com.publicissapient.kpidashboard.azure.config.AzureProcessorConfig;
import com.publicissapient.kpidashboard.azure.model.AzureServer;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.azure.repository.AzureProcessorRepository;
import com.publicissapient.kpidashboard.azure.util.AdditionalFilterHelper;
import com.publicissapient.kpidashboard.azure.util.AzureConstants;
import com.publicissapient.kpidashboard.azure.util.AzureProcessorUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.azureboards.AzureBoardsWIModel;
import com.publicissapient.kpidashboard.common.model.azureboards.Fields;
import com.publicissapient.kpidashboard.common.model.azureboards.SystemAssignedTo;
import com.publicissapient.kpidashboard.common.model.azureboards.SystemCreatedBy;
import com.publicissapient.kpidashboard.common.model.azureboards.Value;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.AzureUpdatesModel;
import com.publicissapient.kpidashboard.common.model.azureboards.wiql.AzureWiqlModel;
import com.publicissapient.kpidashboard.common.model.azureboards.wiql.WorkItem;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;

import lombok.extern.slf4j.Slf4j;

/**
 * This is an implemented/extended storyDataClient for configured kanban
 * projects, Which extracts the story data using the java JIRA api, and store it
 * in a MongoDB collection for Custom API calls.
 */
@Service

/** The Constant log. */
@Slf4j
public class KanbanAzureIssueClientImpl extends AzureIssueClient {// NOPMD

	/** The azure processor repository. */
	@Autowired
	private AzureProcessorRepository azureProcessorRepository;

	/** The kanban account hierarchy repo. */
	@Autowired
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;

	/** The kanban jira repo. */
	@Autowired
	private KanbanJiraIssueRepository kanbanJiraRepo;

	/** The azure processor config. */
	@Autowired
	private AzureProcessorConfig azureProcessorConfig;

	/** The kanban issue history repo. */
	@Autowired
	private KanbanJiraIssueHistoryRepository kanbanIssueHistoryRepo;

	/** The aes encryption service. */
	@Autowired
	private AesEncryptionService aesEncryptionService;

	@Autowired
	private ProcessorAzureRestClient processorAzureRestClient;
	
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
	 * @param projectKey
	 *            Project Key
	 * @param azureAdapter
	 *            AzureAdapter client
	 * @return count of Jira issue processed
	 */
	@Override
	public int processesAzureIssues(ProjectConfFieldMapping projectConfig, String projectKey, // NOSONAR
									// //NOPMD
									AzureAdapter azureAdapter) {
		int count = 0;
		try {
			boolean dataExist = (kanbanJiraRepo
					.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);

			Map<String, String> maxChangeDatesByIssueType = getLastChangedDatesByIssueType(
					projectConfig.getBasicProjectConfigId(), projectConfig.getFieldMapping());

			Map<String, Long> startTimesByIssueType = new HashMap<>();

			maxChangeDatesByIssueType.forEach((k, v) -> {
				String dateStrMinutePrior = AzureIssueClientUtil.getChangeDateMinutePrior(v, azureProcessorConfig);
				Long startTime;
				try {
					startTime = new SimpleDateFormat(AzureConstants.SETTING_DATE_FORMAT, Locale.US)
							.parse(dateStrMinutePrior).getTime();
					startTimesByIssueType.put(k, startTime);
				} catch (ParseException e) {
					log.error("Parsing exception occured", e);
				}
			});

			int pageSize = azureAdapter.getPageSize();

			// Prepare Azure server for API calls
			AzureServer azureServer = prepareAzureServer(projectConfig);

			// Fetch Azure WorkItem ids by calling Wiql API
			AzureWiqlModel azureWiqlModel = azureAdapter.getWiqlModel(azureServer, startTimesByIssueType, projectConfig,
					dataExist);

			List<WorkItem> workItems = azureWiqlModel.getWorkItems();
			List<Integer> workItemIds = new ArrayList<>();
			if (null != workItems) {
				for (WorkItem workItem : workItems) {
					workItemIds.add(workItem.getId());
				}

				// Loop for fetching paged Work items
				for (int i = 0; i < workItemIds.size(); i += pageSize) {

					debugIssuesBeingProcessed(pageSize, workItemIds, i);

					List<Integer> pagedworkItemIds = new ArrayList<>(
							workItemIds.subList(i, Math.min(i + pageSize, workItemIds.size())));

					AzureBoardsWIModel azureBoardsWIModel = azureAdapter.getWorkItemInfoForIssues(i, azureServer,
							pagedworkItemIds);

					List<Value> issues = azureBoardsWIModel.getValue();
					/*
					 * To check for Offline mode // in case of offline method
					 * issues size can be greater than // pageSize, increase
					 * page size so that same issues not read
					 *
					 */

					if (CollectionUtils.isNotEmpty(issues)) {
						saveAzureIssueDetails(issues, projectConfig);
						count += issues.size();
					}
					MDC.put("IssueCount", String.valueOf(issues.size()));

					if (issues == null || issues.size() < pageSize) {
						break;
					}
				}
			}
		} catch (JSONException | NullPointerException e) {
			log.error("Azure Processor | Error while updating Story information in kanban client", e);
		}

		return count;
	}

	/**
	 * Purges list of issues provided.
	 *
	 * @param purgeIssuesList
	 *            List of issues to be purged
	 * @param projectConfig
	 *            Project Configuration Mapping
	 */
	@Override
	public void purgeAzureIssues(List<Value> purgeIssuesList, ProjectConfFieldMapping projectConfig) {
		List<KanbanJiraIssue> kanbanIssuesToDelete = Lists.newArrayList();
		List<KanbanIssueCustomHistory> kanbanIssueHistoryToDelete = Lists.newArrayList();
		purgeIssuesList.forEach(issue -> {
			String issueId = AzureProcessorUtil.deodeUTF8String(issue.getId());
			String projectKeyIssueId = getModifiedIssueId(projectConfig, issueId);
			KanbanJiraIssue kanbanJiraIssue = findOneKanbanIssueRepo(projectKeyIssueId,
					projectConfig.getBasicProjectConfigId().toString());
			KanbanIssueCustomHistory kanbanHistory = findOneKanbanIssueCustomHistory(projectKeyIssueId,
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
	 * @param currentPagedAzureRs
	 *            A list response of Jira issues from the source system
	 * @param projectConfig
	 *            the project config
	 * @throws JSONException
	 *             error while parsing JSON response
	 */
	@Override
	public void saveAzureIssueDetails(List<Value> currentPagedAzureRs, ProjectConfFieldMapping projectConfig) // NOSONAR
	// //NOPMD
			throws JSONException {
		List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
				.getFullHierarchyLevels(projectConfig.isKanban());
		if (null == currentPagedAzureRs) {
			log.error("Azure Processor |. No list of current paged Azure's issues found");
			return;
		}
		log.debug("Azure response:", currentPagedAzureRs.size());
		List<KanbanJiraIssue> kanbanIssuesToSave = new ArrayList<>();
		List<KanbanIssueCustomHistory> kanbanIssueHistoryToSave = new ArrayList<>();

		ObjectId azureProcessorId = azureProcessorRepository.findByProcessorName(ProcessorConstants.AZURE).getId();

		for (Value issue : currentPagedAzureRs) {
			FieldMapping fieldMapping = projectConfig.getFieldMapping();
			if (null == fieldMapping) {
				return;
			}

			Set<String> issueTypeNames = new HashSet<>();
			for (String issueTypeName : fieldMapping.getJiraIssueTypeNames()) {
				issueTypeNames.add(issueTypeName.toLowerCase(Locale.getDefault()));
			}

			String issueId = AzureProcessorUtil.deodeUTF8String(issue.getId());

			// Modifying issueId to get unique value to each project
			String projectKeyIssueId = getModifiedIssueId(projectConfig, issueId);

			KanbanJiraIssue azureIssue = findOneKanbanIssueRepo(projectKeyIssueId,
					projectConfig.getBasicProjectConfigId().toString());

			if (azureIssue == null) {
				azureIssue = new KanbanJiraIssue();
			}
			KanbanIssueCustomHistory azureIssueHistory = findOneKanbanIssueCustomHistory(projectKeyIssueId,
					projectConfig.getBasicProjectConfigId().toString());
			if (azureIssueHistory == null) {
				azureIssueHistory = new KanbanIssueCustomHistory();
			}

			Fields fields = issue.getFields();
			Map<String, Object> fieldsMap = AzureIssueClientUtil.buildFieldMap(issue.getFields());

			String issueType = fields.getSystemWorkItemType();

			setURL(issueId,azureIssue,projectConfig);
			// Add RCA to Issue
			setRCA(fieldMapping, issue, azureIssue, fieldsMap);

			// Add device platform filed to issue
			setDevicePlatform(fieldMapping, azureIssue, fieldsMap);

			if (issueTypeNames
					.contains(AzureProcessorUtil.deodeUTF8String(issueType).toLowerCase(Locale.getDefault()))) {

				log.debug(String.format("[%-12s] %s", AzureProcessorUtil.deodeUTF8String(issue.getId()),
						AzureProcessorUtil.deodeUTF8String(fields.getSystemTitle())));

				// collectorId
				azureIssue.setProcessorId(azureProcessorId);
				// ID
				azureIssue.setIssueId(AzureProcessorUtil.deodeUTF8String(projectKeyIssueId));

				// Type
				azureIssue.setTypeId(AzureProcessorUtil.deodeUTF8String(issueType));
				azureIssue.setTypeName(AzureProcessorUtil.deodeUTF8String(issueType));

				// Set EPIC issue data for issue type epic
				if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueEpicType()) && fieldMapping
						.getJiraIssueEpicType().contains(AzureProcessorUtil.deodeUTF8String(issueType))) {
					setEpicIssueData(fieldMapping, azureIssue, fieldsMap);
				}
				// set defecttype to BUG
				if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype())
						&& fieldMapping.getJiradefecttype().stream().anyMatch(issueType::equalsIgnoreCase)) {
					azureIssue.setTypeName(NormalizedJira.DEFECT_TYPE.getValue());
				}

				// Priority
				if (fields.getMicrosoftVSTSCommonPriority() != null) {
					azureIssue.setPriority(AzureProcessorUtil.deodeUTF8String(fields.getMicrosoftVSTSCommonPriority()));
				}

				// Label
				azureIssue.setLabels(AzureIssueClientUtil.getLabelsList(fields));

				processJiraIssueData(azureIssue, issue, fieldsMap, fieldMapping, azureProcessorConfig);

				// Set project specific details
				setProjectSpecificDetails(projectConfig, azureIssue);

				// Set additional filters
				setAdditionalFilters(azureIssue, issue, projectConfig);

				// Add Tech Debt Story identificatin to jira issue
				setIssueTechStoryType(fieldMapping, issue, azureIssue, fieldsMap);

				// Not able to find mapping in Azure for Affected Versions

				setJiraAssigneeDetails(azureIssue, fields);
				// setting filter data from Jira issue to
				// jira_issue_custom_history
				setAzureIssueHistory(azureIssueHistory, azureIssue, issue, fieldMapping, projectConfig);

				// Placeholder for Test Automated field mapping.

				if (StringUtils.isNotBlank(azureIssue.getProjectID())) {
					kanbanIssuesToSave.add(azureIssue);
					kanbanIssueHistoryToSave.add(azureIssueHistory);
				}

			}
		}

		// Saving back to MongoDB
		kanbanJiraRepo.saveAll(kanbanIssuesToSave);
		kanbanIssueHistoryRepo.saveAll(kanbanIssueHistoryToSave);
		saveKanbanAccountHierarchy(kanbanIssuesToSave, projectConfig, hierarchyLevelList);
	}

	/**
	 * Sets the jira issue history.
	 *
	 * @param azureIssueHistory
	 *            the jira issue history
	 * @param azureIssue
	 *            the jira issue
	 * @param issue
	 *            the issue
	 * @param fieldMapping
	 *            the field mapping
	 * @param projectConfig
	 *            Project Config
	 */
	private void setAzureIssueHistory(KanbanIssueCustomHistory azureIssueHistory, KanbanJiraIssue azureIssue,
									  Value issue, FieldMapping fieldMapping, ProjectConfFieldMapping projectConfig) {
		azureIssueHistory.setProjectID(azureIssue.getProjectName());
		azureIssueHistory.setProjectComponentId(azureIssue.getProjectID());
		azureIssueHistory.setProjectKey(azureIssue.getProjectKey());
		azureIssueHistory.setProjectName(azureIssue.getProjectName());
		azureIssueHistory.setPriority(azureIssue.getPriority());
		azureIssueHistory.setRootCauseList(azureIssue.getRootCauseList());
		azureIssueHistory.setStoryType(azureIssue.getTypeName());
		azureIssueHistory.setAdditionalFilters(azureIssue.getAdditionalFilters());
		azureIssueHistory.setUrl(azureIssue.getUrl());
		azureIssueHistory.setDescription(azureIssue.getName());
		processAzureIssueHistory(azureIssueHistory, azureIssue, issue, fieldMapping, projectConfig);
		azureIssueHistory.setBasicProjectConfigId(azureIssue.getBasicProjectConfigId());
	}

	/**
	 * Process Jira issue History data.
	 *
	 * @param azureIssueCustomHistory
	 *            jiraIssueCustomHistory Object
	 * @param azureIssue
	 *            JiraIssue
	 * @param issue
	 *            Value Issue
	 * @param fieldMapping
	 *            user provided FieldMapping
	 * @param projectConfig
	 *            the project config
	 */
	private void processAzureIssueHistory(KanbanIssueCustomHistory azureIssueCustomHistory, KanbanJiraIssue azureIssue,
										  Value issue, FieldMapping fieldMapping, ProjectConfFieldMapping projectConfig) {
		String issueId = AzureProcessorUtil.deodeUTF8String(issue.getId());
		AzureServer server = prepareAzureServer(projectConfig);
		AzureAdapter azureAdapter = new OnlineAdapter(azureProcessorConfig, processorAzureRestClient);
		AzureUpdatesModel azureUpdatesModel = azureAdapter.getUpdates(server, issueId);
		List<com.publicissapient.kpidashboard.common.model.azureboards.updates.Value> valueList = azureUpdatesModel
				.getValue();

		if (null == azureIssueCustomHistory.getStoryID()) {
			addStoryHistory(azureIssueCustomHistory, azureIssue, issue, valueList, fieldMapping);
		} else {
			addHistoryInAzureIssue(azureIssueCustomHistory, azureIssue, valueList, fieldMapping);
		}

	}

	/**
	 * Adds Jira issue history.
	 *
	 * @param azureIssueCustomHistory
	 *            JiraIssueCustomHistory
	 * @param azureIssue
	 *            JiraIssue instance
	 * @param issue
	 *            Value Issue
	 * @param valueList
	 *            List of Change log in jira
	 * @param fieldMapping
	 *            FieldMapping config
	 */
	private void addStoryHistory(KanbanIssueCustomHistory azureIssueCustomHistory, KanbanJiraIssue azureIssue,
								 Value issue, List<com.publicissapient.kpidashboard.common.model.azureboards.updates.Value> valueList,
								 FieldMapping fieldMapping) {

		DateTime createdDate = new DateTime(
				AzureProcessorUtil.getFormattedDateTime(issue.getFields().getSystemCreatedDate()));

		azureIssueCustomHistory.setCreatedDate(createdDate.toString());

		List<KanbanIssueHistory> kanbanIssueHistoryList = getChangeLog(azureIssue, valueList, createdDate,
				fieldMapping);

		azureIssueCustomHistory.setStoryID(azureIssue.getNumber());
		azureIssueCustomHistory.setHistoryDetails(kanbanIssueHistoryList);
		// estimate
		azureIssueCustomHistory.setEstimate(azureIssue.getEstimate());
		azureIssueCustomHistory.setBufferedEstimateTime(azureIssue.getBufferedEstimateTime());
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(azureIssue.getTypeName())) {
			azureIssueCustomHistory.setDefectStoryID(azureIssue.getDefectStoryID());
		}

	}

	/**
	 * Adds Sprint in Story
	 *
	 * @param azureIssueCustomHistory
	 *            JiraIssueCustomHistory
	 * @param azureIssue
	 *            JiraIssue instance
	 * @param updateValueList
	 *            List of Change log in jira
	 * @param fieldMapping
	 *            FieldMapping config
	 */
	private void addHistoryInAzureIssue(KanbanIssueCustomHistory azureIssueCustomHistory, KanbanJiraIssue azureIssue,
										List<com.publicissapient.kpidashboard.common.model.azureboards.updates.Value> updateValueList,
										FieldMapping fieldMapping) {
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(azureIssue.getTypeName())) {
			azureIssueCustomHistory.setDefectStoryID(azureIssue.getDefectStoryID());
		}
		createKanbanIssueHistory(azureIssueCustomHistory, azureIssue, updateValueList, fieldMapping);
		azureIssueCustomHistory.setEstimate(azureIssue.getEstimate());
	}

	/**
	 * Creates Issue Kanban history details for delta changed statuses start
	 *
	 * @param azureIssueCustomHistory
	 *            JiraIssueCustomHistory
	 * @param azureIssue
	 *            jiraIssue
	 * @param updateValueList
	 *            Change Log list
	 * @param fieldMapping
	 *            List of JiraIssueCustomHistory
	 */
	private void createKanbanIssueHistory(KanbanIssueCustomHistory azureIssueCustomHistory, KanbanJiraIssue azureIssue,
										  List<com.publicissapient.kpidashboard.common.model.azureboards.updates.Value> updateValueList,
										  FieldMapping fieldMapping) {
		List<KanbanIssueHistory> issueHistoryList = new ArrayList<>();
		List<String> jiraStatusForDevelopment = fieldMapping.getJiraStatusForDevelopment();
		List<String> jiraStatusForQa = fieldMapping.getJiraStatusForQa();
		for (com.publicissapient.kpidashboard.common.model.azureboards.updates.Value history : updateValueList) {
			com.publicissapient.kpidashboard.common.model.azureboards.updates.Fields changelogItem = history
					.getFields();
			if (null != changelogItem && null != changelogItem.getSystemState()) {
				KanbanIssueHistory kanbanIssueHistory = new KanbanIssueHistory();
				kanbanIssueHistory.setStatus(changelogItem.getSystemState().getNewValue());
				DateTime changedDate = new DateTime(
						AzureProcessorUtil.getFormattedDateTime(changelogItem.getSystemChangedDate().getNewValue()));
				kanbanIssueHistory.setActivityDate(changedDate.toString());
				issueHistoryList.add(kanbanIssueHistory);
				setIndividualDetails(azureIssue, jiraStatusForDevelopment, jiraStatusForQa, kanbanIssueHistory);

			}
			azureIssueCustomHistory.setHistoryDetails(issueHistoryList);
		}

	}

	/**
	 * Process change log and create array of status in Issue history
	 *
	 * @param azureIssue
	 *            Jiraissue
	 * @param updateValueList
	 *            Changes log list for jira issue
	 * @param issueCreatedDate
	 *            creation date on jira issue
	 * @param fieldMapping
	 *            FielMapping
	 * @return
	 */
	private List<KanbanIssueHistory> getChangeLog(KanbanJiraIssue azureIssue,
												  List<com.publicissapient.kpidashboard.common.model.azureboards.updates.Value> updateValueList,
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
			setIndividualDetails(azureIssue, jiraStatusForDevelopment, jiraStatusForQa, kanbanHistory);
		}
		if (CollectionUtils.isNotEmpty(updateValueList)) {
			for (com.publicissapient.kpidashboard.common.model.azureboards.updates.Value history : updateValueList) {
				KanbanIssueHistory kanbanIssueHistory = getIssueHistory(azureIssue, history, jiraStatusForDevelopment,
						jiraStatusForQa);
				if (StringUtils.isNotEmpty(kanbanIssueHistory.getActivityDate())) {
					historyDetails.add(getIssueHistory(azureIssue, history, jiraStatusForDevelopment, jiraStatusForQa));
				}
			}
		}
		return historyDetails;
	}

	/**
	 * Sets Individual Details
	 *
	 * @param jiraIssue
	 *            Jira issue
	 * @param jiraStatusForDevelopment
	 *            List of status for Development
	 * @param jiraStatusForQa
	 *            List of status for QA
	 */
	private void setIndividualDetails(KanbanJiraIssue jiraIssue, List<String> jiraStatusForDevelopment,
									  List<String> jiraStatusForQa, KanbanIssueHistory kanbanIssueHistory) {
		if (CollectionUtils.isNotEmpty(jiraStatusForDevelopment)
				&& jiraStatusForDevelopment.stream().anyMatch(kanbanIssueHistory.getStatus()::equalsIgnoreCase)
				&& StringUtils.isNotBlank(jiraIssue.getAssigneeId())
				&& StringUtils.isNotBlank(jiraIssue.getAssigneeName())) {

			jiraIssue.setDeveloperId(jiraIssue.getAssigneeId());
			jiraIssue.setDeveloperName(jiraIssue.getAssigneeName() + AzureConstants.OPEN_BRACKET
					+ jiraIssue.getAssigneeId() + AzureConstants.CLOSED_BRACKET);

		}
		if (CollectionUtils.isNotEmpty(jiraStatusForQa)
				&& jiraStatusForQa.stream().anyMatch(kanbanIssueHistory.getStatus()::equalsIgnoreCase)
				&& StringUtils.isNotBlank(jiraIssue.getAssigneeId())
				&& StringUtils.isNotBlank(jiraIssue.getAssigneeName())) {

			jiraIssue.setQaId(jiraIssue.getAssigneeId());
			jiraIssue.setQaName(jiraIssue.getAssigneeName() + AzureConstants.OPEN_BRACKET + jiraIssue.getAssigneeId()
					+ AzureConstants.CLOSED_BRACKET);

		}
	}

	/**
	 * Find Kanban Jira issue by issueId.
	 *
	 * @param issueId
	 *            JiraIssue ID
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return KanbanJiraIssue corresponding to issueId from DB
	 */
	private KanbanJiraIssue findOneKanbanIssueRepo(String issueId, String basicProjectConfigId) {
		List<KanbanJiraIssue> jiraIssues = kanbanJiraRepo.findByIssueIdAndBasicProjectConfigId(StringEscapeUtils.escapeHtml4(issueId),
				basicProjectConfigId);

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
	 * Find kanban Jira Issue custom history object by issueId.
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
	 * @param kanbanFilterCategoryList
	 *            Kanban Filter category list
	 */
	private void saveKanbanAccountHierarchy(List<KanbanJiraIssue> jiraIssueList, ProjectConfFieldMapping projectConfig, // NOPMD //NOSONAR
											List<HierarchyLevel> hierarchyLevelList) { // NOSONAR

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

				additionalFiltersHierarchies.forEach(accountHierarchy -> {
					accHierarchyToSave(accountHierarchy, existingKanbanHierarchy, accHierarchyToSave);
				});

			}
		}
		if (CollectionUtils.isNotEmpty(accHierarchyToSave)) {
			kanbanAccountHierarchyRepo.saveAll(accHierarchyToSave);
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
	 * Sets RCA.
	 *
	 * @param fieldMapping
	 *            fieldMapping provided by the User
	 * @param issue
	 *            Azure Issue
	 * @param azureIssue
	 *            JiraIssue instance
	 * @param fieldsMap
	 *            the fields map
	 */
	public void setRCA(FieldMapping fieldMapping, Value issue, KanbanJiraIssue azureIssue,
					   Map<String, Object> fieldsMap) {
		Fields fields = issue.getFields();
		String rootCauseFieldFromFieldMapping = fieldMapping.getRootCause();

		if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype()) && fieldMapping.getJiradefecttype().stream()
				.anyMatch(fields.getSystemWorkItemType()::equalsIgnoreCase)) {
			String rcaCause = AzureConstants.RCA_CAUSE_NONE;
			if (fieldsMap.containsKey(rootCauseFieldFromFieldMapping)
					&& fieldsMap.get(rootCauseFieldFromFieldMapping) != null) {
				// Introduce enum to standarize the values of RCA
				rcaCause = fieldsMap.get(rootCauseFieldFromFieldMapping).toString().toLowerCase();

				if (azureProcessorConfig.getRcaValuesForCodeIssue().stream().anyMatch(rcaCause::equalsIgnoreCase)) {
					rcaCause = AzureConstants.CODE_ISSUE;
				}
			}
			azureIssue.setRootCauseList(Lists.newArrayList(rcaCause.toLowerCase()));
		} else {
			azureIssue.setRootCauseList(Lists.newArrayList(AzureConstants.RCA_CAUSE_NONE));
		}

	}

	/**
	 * Sets Device Platform.
	 *
	 * @param fieldMapping
	 *            fieldMapping provided by the User
	 * @param azureIssue
	 *            KanbanJiraIssue instance
	 * @param fieldsMap
	 *            the fields map
	 */
	public void setDevicePlatform(FieldMapping fieldMapping, KanbanJiraIssue azureIssue,
								  Map<String, Object> fieldsMap) {
		String devicePlatformFromFieldMapping = fieldMapping.getDevicePlatform();
		String devicePlatform = null;
		if (fieldsMap.containsKey(devicePlatformFromFieldMapping)
				&& fieldsMap.get(devicePlatformFromFieldMapping) != null) {
			devicePlatform = fieldsMap.get(devicePlatformFromFieldMapping).toString();
		}
		azureIssue.setDevicePlatform(devicePlatform);
	}

	/**
	 * Process Jira issue Data.
	 *
	 * @param azureIssue
	 *            JiraIssue instance
	 * @param issue
	 *            Atlassian Issue
	 * @param fieldsMap
	 *            the fields map
	 * @param fieldMapping
	 *            fieldMapping provided by the User
	 * @param jiraProcessorConfig
	 *            Jira processor Configuration
	 * @throws JSONException
	 *             Error while parsing JSON
	 */
	public void processJiraIssueData(KanbanJiraIssue azureIssue, Value issue, Map<String, Object> fieldsMap,
									 FieldMapping fieldMapping, AzureProcessorConfig jiraProcessorConfig) throws JSONException {

		Fields fields = issue.getFields();
		String status = fields.getSystemState();
		String changeDate = fields.getSystemChangedDate();
		String createdDate = fields.getSystemCreatedDate();
		azureIssue.setNumber(AzureProcessorUtil.deodeUTF8String(azureIssue.getIssueId()));
		azureIssue.setName(AzureProcessorUtil.deodeUTF8String(fields.getSystemTitle()));
		azureIssue.setStatus(AzureProcessorUtil.deodeUTF8String(status));
		azureIssue.setState(AzureProcessorUtil.deodeUTF8String(status));

		azureIssue.setJiraStatus(AzureProcessorUtil.deodeUTF8String(status));

		if (StringUtils.isNotEmpty(fields.getMicrosoftVSTSCommonResolvedReason())) {
			azureIssue.setResolution(AzureProcessorUtil.deodeUTF8String(fields.getMicrosoftVSTSCommonResolvedReason()));
		}

		setEstimate(azureIssue, fieldsMap, fieldMapping, jiraProcessorConfig, fields);

		Integer timeSpent = 0;
		if (fields.getMicrosoftVSTSSchedulingCompletedWork() != null) {
			// To convert completed work to minutes. From Azure we get hours
			// data.
			timeSpent = fields.getMicrosoftVSTSSchedulingCompletedWork() * 60;
		}
		azureIssue.setTimeSpentInMinutes(timeSpent);

		setEnvironmentImpacted(azureIssue, fieldsMap, fieldMapping);

		azureIssue.setChangeDate(AzureProcessorUtil.getFormattedDate(AzureProcessorUtil.deodeUTF8String(changeDate)));
		azureIssue.setIsDeleted(AzureConstants.FALSE);

		azureIssue.setOwnersState(Arrays.asList("Active"));

		azureIssue.setOwnersChangeDate(Collections.<String>emptyList());

		azureIssue.setOwnersIsDeleted(Collections.<String>emptyList());

		// Created Date
		azureIssue.setCreatedDate(AzureProcessorUtil.getFormattedDate(AzureProcessorUtil.deodeUTF8String(createdDate)));

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
	 * @param azureIssue
	 *            JiraIssue instance
	 * @param fieldsMap
	 *            the fields map
	 */
	public void setIssueTechStoryType(FieldMapping fieldMapping, Value issue, KanbanJiraIssue azureIssue,
									  Map<String, Object> fieldsMap) {
		Fields fields = issue.getFields();
		// For Custom Field
		String jiraTechDebtCustomField = fieldMapping.getJiraTechDebtCustomField();
		Set<String> finalJiraTechDebtCustomFieldSet = new HashSet<>();
		finalJiraTechDebtCustomFieldSet.add(jiraTechDebtCustomField);

		if (null != fieldMapping.getJiraTechDebtIdentification()) {
			if (fieldMapping.getJiraTechDebtIdentification().trim().equalsIgnoreCase(AzureConstants.LABELS)) {
				if (StringUtils.isNotEmpty(fields.getSystemTags())) {
					String[] labelArray = fields.getSystemTags().split(";");
					Set<String> labels = new HashSet<>(Arrays.asList(labelArray));
					if (CollectionUtils.containsAny(labels, fieldMapping.getJiraTechDebtValue())) {
						azureIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
					}
				}
			} else if (fieldMapping.getJiraTechDebtIdentification().trim().equalsIgnoreCase(AzureConstants.ISSUE_TYPE)
					&& fieldMapping.getJiraTechDebtValue().contains(azureIssue.getTypeName())) {
				azureIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
			} else if (fieldMapping.getJiraTechDebtIdentification().trim().equalsIgnoreCase(AzureConstants.CUSTOM_FIELD)
					&& fieldsMap.containsKey(jiraTechDebtCustomField.trim())
					&& fieldsMap.get(jiraTechDebtCustomField.trim()) != null && CollectionUtils
					.containsAny(fieldMapping.getJiraTechDebtValue(), finalJiraTechDebtCustomFieldSet)) {
				azureIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
			}
		}

	}

	/**
	 * This method process owner and user details.
	 *
	 * @param azureIssue
	 *            JiraIssue Object to set Owner details
	 * @param fields
	 *            the fields
	 */
	public void setJiraAssigneeDetails(KanbanJiraIssue azureIssue, Fields fields) {
		SystemAssignedTo systemAssignedTo = fields.getSystemAssignedTo();
		SystemCreatedBy systemCreatedBy = fields.getSystemCreatedBy();

		if (systemCreatedBy == null) {
			azureIssue.setOwnersUsername(Collections.<String>emptyList());
			azureIssue.setOwnersShortName(Collections.<String>emptyList());
			azureIssue.setOwnersID(Collections.<String>emptyList());
			azureIssue.setOwnersFullName(Collections.<String>emptyList());
		} else {
			List<String> ownersUsername = new ArrayList<>();
			List<String> ownersId = new ArrayList<>();
			List<String> ownersFullname = new ArrayList<>();
			ownersUsername.add(systemCreatedBy.getUniqueName());
			ownersId.add(systemCreatedBy.getId());
			ownersFullname.add(systemCreatedBy.getDisplayName());
			azureIssue.setOwnersUsername(ownersUsername);
			azureIssue.setOwnersID(ownersId);
			azureIssue.setOwnersFullName(ownersFullname);
		}

		if (systemAssignedTo == null) {
			azureIssue.setAssigneeId(StringUtils.EMPTY);
			azureIssue.setAssigneeName(StringUtils.EMPTY);
		} else {
			azureIssue.setAssigneeId(systemAssignedTo.getId());
			azureIssue.setAssigneeName(systemAssignedTo.getDisplayName());
		}
	}

	/**
	 * Sets Estimate.
	 *
	 * @param azureIssue
	 *            JiraIssue instance
	 * @param fieldsMap
	 *            the fields map
	 * @param fieldMapping
	 *            fieldMapping provided by the User
	 * @param jiraProcessorConfig
	 *            Jira Processor Configuration
	 * @param fields
	 *            Map of Issue Fields
	 */
	public void setEstimate(KanbanJiraIssue azureIssue, Map<String, Object> fieldsMap, FieldMapping fieldMapping, // NOSONAR
							AzureProcessorConfig jiraProcessorConfig, Fields fields) {

		Double value = 0d;
		String valueString = "0";
		Double estimationFromDefaultField = fields.getMicrosoftVSTSSchedulingOriginalEstimate();
		Double storyPointsFromDefaultField = fields.getMicrosoftVSTSSchedulingStoryPoints();
		String estimationCriteria = jiraProcessorConfig.getEstimationCriteria();
		if (StringUtils.isNotBlank(estimationCriteria)) {
			String estimationField = fieldMapping.getJiraStoryPointsCustomField();
			if (StringUtils.isNotBlank(estimationField) && fieldsMap.containsKey(estimationField)
					&& fieldsMap.get(estimationField) != null
					&& !AzureProcessorUtil.deodeUTF8String(fieldsMap.get(estimationField)).isEmpty()) {
				// Set Estimation for Custom Estimation/Story Points Field
				if (AzureConstants.STORY_POINTS.equalsIgnoreCase(estimationCriteria)) {
					value = Double.parseDouble(AzureProcessorUtil.deodeUTF8String(fieldsMap.get(estimationField)));
					valueString = String.valueOf(value.doubleValue());
				}
				azureIssue.setEstimate(valueString);
				azureIssue.setStoryPoints(value);
			} else {
				setEstimateForDefaultFields(azureIssue, fields, estimationFromDefaultField,
						storyPointsFromDefaultField);
			}
		} else {
			// Default estimation criteria is storypoints
			String estimationField = fieldMapping.getJiraStoryPointsCustomField();
			if (StringUtils.isNotEmpty(estimationField) && fieldsMap.containsKey(estimationField)
					&& fieldsMap.get(estimationField) != null
					&& !AzureProcessorUtil.deodeUTF8String(fieldsMap.get(estimationField)).isEmpty()) {
				// Set Estimate and Story points for Custom Azure Story Point
				// fields
				value = Double.parseDouble(AzureProcessorUtil.deodeUTF8String(fieldsMap.get(estimationField)));
				valueString = String.valueOf(value.doubleValue());
				azureIssue.setEstimate(valueString);
				azureIssue.setStoryPoints(value);
			} else {
				setEstimateForDefaultFields(azureIssue, fields, estimationFromDefaultField,
						storyPointsFromDefaultField);
			}
		}

	}

	/**
	 * Sets the environment impacted custom field.
	 *
	 * @param azureIssue
	 *            JiraIssue instance
	 * @param fieldsMap
	 *            Map of Issue Fields
	 * @param fieldMapping
	 *            fieldMapping provided by the User
	 */
	private void setEnvironmentImpacted(KanbanJiraIssue azureIssue, Map<String, Object> fieldsMap,
										FieldMapping fieldMapping) {
		String envImpacted = fieldMapping.getEnvImpacted();
		if (StringUtils.isNotEmpty(envImpacted) && fieldsMap.containsKey(envImpacted)
				&& fieldsMap.get(envImpacted) != null) {
			azureIssue.setEnvImpacted(AzureProcessorUtil.deodeUTF8String(fieldsMap.get(envImpacted)));

		}
	}

	/**
	 * Decrypt key.
	 *
	 * @param encryptedKey
	 *            the encrypted key
	 * @return the string
	 */
	private String decryptKey(String encryptedKey) {
		return aesEncryptionService.decrypt(encryptedKey, azureProcessorConfig.getAesEncryptionKey());
	}

	/**
	 * Debug issues being processed.
	 *
	 * @param pageSize
	 *            the page size
	 * @param workItemIds
	 *            the work item ids
	 * @param i
	 *            the i
	 */
	private void debugIssuesBeingProcessed(int pageSize, List<Integer> workItemIds, int i) {
		if (log.isDebugEnabled()) {
			int pageEnd = Math.min(i + pageSize - 1, workItemIds.size());
			log.debug(String.format("Processing issues %d - %d out of %d", i, pageEnd, workItemIds.size()));
		}
	}

	/**
	 * Sets the estimate for default fields.
	 *
	 * @param jiraIssue
	 *            the jira issue
	 * @param fields
	 *            the fields
	 * @param estimationFromDefaultField
	 *            the estimation from default field
	 * @param storyPointsFromDefaultField
	 *            the story points from default field
	 */
	private void setEstimateForDefaultFields(KanbanJiraIssue jiraIssue, Fields fields,
											 Double estimationFromDefaultField, Double storyPointsFromDefaultField) {
		// Set Estimate and Story points for Default Azure fields
		Double value = 0d;
		String valueString = "0";
		if (estimationFromDefaultField != null) {
			// Issue Type Task and Bug have default estimation field in Azure
			jiraIssue.setEstimate(Double.toString(fields.getMicrosoftVSTSSchedulingOriginalEstimate()));
		} else if (storyPointsFromDefaultField != null) {
			// Set Estimate as story points when Estimate field is null for
			// other issue
			// types in Azure. This is used for Sprint Velocity Calculation
			jiraIssue.setEstimate(Double.toString(fields.getMicrosoftVSTSSchedulingStoryPoints()));
		} else {
			jiraIssue.setEstimate(valueString);
		}
		if (storyPointsFromDefaultField != null) {
			jiraIssue.setStoryPoints(fields.getMicrosoftVSTSSchedulingStoryPoints());
		} else {
			jiraIssue.setStoryPoints(value);
		}
	}

	private AzureServer prepareAzureServer(ProjectConfFieldMapping projectConfig) {
		AzureServer azureServer = new AzureServer();
		azureServer.setPat(decryptKey(projectConfig.getAzure().getConnection().getPat()));
		azureServer.setUrl(AzureProcessorUtil.encodeSpaceInUrl(projectConfig.getAzure().getConnection().getBaseUrl()));
		azureServer.setApiVersion(projectConfig.getAzure().getApiVersion());
		azureServer.setUsername(projectConfig.getAzure().getConnection().getUsername());
		return azureServer;
	}

	private KanbanIssueHistory getIssueHistory(KanbanJiraIssue jiraIssue,
											   com.publicissapient.kpidashboard.common.model.azureboards.updates.Value history,
											   List<String> jiraStatusForDevelopment, List<String> jiraStatusForQa) {
		com.publicissapient.kpidashboard.common.model.azureboards.updates.Fields changelogItem = history.getFields();
		KanbanIssueHistory kanbanHistory = new KanbanIssueHistory();
		if (null != changelogItem && null != changelogItem.getSystemState()) {
			DateTime changedDate = new DateTime(
					AzureProcessorUtil.getFormattedDateTime(changelogItem.getSystemChangedDate().getNewValue()));
			kanbanHistory.setActivityDate(changedDate.toString());
			kanbanHistory.setStatus(changelogItem.getSystemState().getNewValue());
			setIndividualDetails(jiraIssue, jiraStatusForDevelopment, jiraStatusForQa, kanbanHistory);

		}
		return kanbanHistory;

	}

	private String getModifiedIssueId(ProjectConfFieldMapping projectConfig, String issueId) {
		StringBuilder projectKeyIssueId = new StringBuilder(projectConfig.getProjectKey());
		projectKeyIssueId.append("-").append(issueId);
		return projectKeyIssueId.toString();
	}

	private Map<String, String> getLastChangedDatesByIssueType(ObjectId projectConfigId, FieldMapping fieldMapping) {
		String[] jiraIssueTypeNames = fieldMapping.getJiraIssueTypeNames();
		Set<String> uniqueIssueTypes = new HashSet<>(Arrays.asList(jiraIssueTypeNames));

		Map<String, String> lastUpdatedDateByIssueType = new HashMap<>();

		for (String issueType : uniqueIssueTypes) {

			ObjectId processorId = azureProcessorRepository.findByProcessorName(ProcessorConstants.AZURE).getId();
			KanbanJiraIssue jiraIssue = kanbanJiraRepo
					.findTopByProcessorIdAndBasicProjectConfigIdAndTypeNameAndChangeDateGreaterThanOrderByChangeDateDesc(
							processorId, projectConfigId.toString(), issueType, azureProcessorConfig.getStartDate());
			if (jiraIssue != null) {

				lastUpdatedDateByIssueType.put(issueType, jiraIssue.getChangeDate() != null ? jiraIssue.getChangeDate()
						: azureProcessorConfig.getStartDate());
			} else {
				lastUpdatedDateByIssueType.put(issueType, azureProcessorConfig.getStartDate());
			}
		}

		return lastUpdatedDateByIssueType;
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
	private void setEpicIssueData(FieldMapping fieldMapping, KanbanJiraIssue azureIssue,
								  Map<String, Object> fieldsMap) {
		if (fieldsMap.get(fieldMapping.getEpicJobSize()) != null) {
			String fieldValue = fieldsMap.get(fieldMapping.getEpicJobSize()).toString();
			azureIssue.setJobSize(Double.parseDouble(fieldValue));

		}
		if (fieldsMap.get(fieldMapping.getEpicRiskReduction()) != null) {
			String fieldValue = fieldsMap.get(fieldMapping.getEpicRiskReduction()).toString();
			azureIssue.setRiskReduction(Double.parseDouble(fieldValue));

		}
		if (fieldsMap.get(fieldMapping.getEpicTimeCriticality()) != null) {
			String fieldValue = fieldsMap.get(fieldMapping.getEpicTimeCriticality()).toString();
			azureIssue.setTimeCriticality(Double.parseDouble(fieldValue));
		}
		if (fieldsMap.get(fieldMapping.getEpicUserBusinessValue()) != null) {
			String fieldValue = fieldsMap.get(fieldMapping.getEpicUserBusinessValue()).toString();
			azureIssue.setBusinessValue(Double.parseDouble(fieldValue));
		}
		if (fieldsMap.get(fieldMapping.getEpicWsjf()) != null) {
			String fieldValue = fieldsMap.get(fieldMapping.getEpicWsjf()).toString();
			azureIssue.setWsjf(Double.parseDouble(fieldValue));
		}
		double costOfDelay = azureIssue.getBusinessValue() + azureIssue.getRiskReduction()
				+ azureIssue.getTimeCriticality();
		azureIssue.setCostOfDelay(costOfDelay);
	}
	
	private void setAdditionalFilters(KanbanJiraIssue jiraIssue, Value issue, ProjectConfFieldMapping projectConfig) {
		List<AdditionalFilter> additionalFilter = additionalFilterHelper.getAdditionalFilter(issue, projectConfig);
		jiraIssue.setAdditionalFilters(additionalFilter);
	}
	
	private void setProjectSpecificDetails(ProjectConfFieldMapping projectConfig, KanbanJiraIssue jiraIssue) {
		String name = projectConfig.getProjectName();
		String id = new StringBuffer(name).append(CommonConstant.UNDERSCORE)
				.append(projectConfig.getBasicProjectConfigId().toString()).toString();

		jiraIssue.setProjectID(id);
		jiraIssue.setProjectName(name);
		jiraIssue.setProjectKey(projectConfig.getProjectKey());
		jiraIssue.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString());
		jiraIssue.setProjectBeginDate("");
		jiraIssue.setProjectEndDate("");
		jiraIssue.setProjectChangeDate("");
		jiraIssue.setProjectState("");
		jiraIssue.setProjectIsDeleted("False");
		jiraIssue.setProjectPath("");
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
	 * setting Url to kanbanJiraIssue
	 * @param ticketNumber
	 * @param kanbanJiraIssue
	 * @param projectConfig
	 */
	private void setURL(String ticketNumber, KanbanJiraIssue kanbanJiraIssue, ProjectConfFieldMapping projectConfig) {
		String baseUrl = projectConfig.getAzure().getConnection().getBaseUrl();
		baseUrl= baseUrl + (baseUrl.endsWith("/") ? "" : "/");
		kanbanJiraIssue.setUrl(baseUrl.equals("")?"": baseUrl+azureProcessorConfig.getAzureDirectTicketLinkKey() + ticketNumber);
	}
}
