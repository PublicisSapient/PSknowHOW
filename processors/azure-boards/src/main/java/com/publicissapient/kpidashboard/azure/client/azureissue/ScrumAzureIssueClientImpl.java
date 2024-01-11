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

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import com.publicissapient.kpidashboard.azure.client.sprint.SprintClient;
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
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.azureboards.Attribute;
import com.publicissapient.kpidashboard.common.model.azureboards.AzureBoardsWIModel;
import com.publicissapient.kpidashboard.common.model.azureboards.Fields;
import com.publicissapient.kpidashboard.common.model.azureboards.Relation;
import com.publicissapient.kpidashboard.common.model.azureboards.Value;
import com.publicissapient.kpidashboard.common.model.azureboards.iterations.AzureIterationsModel;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.AzureUpdatesModel;
import com.publicissapient.kpidashboard.common.model.azureboards.wiql.AzureWiqlModel;
import com.publicissapient.kpidashboard.common.model.azureboards.wiql.WorkItem;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScrumAzureIssueClientImpl extends AzureIssueClient {

	private static final String CLOSED = "CLOSED";

	private static final String SPRINT_STATE_CLOSED = "past";
	private static final String SPRINT_STATE_ACTIVE = "current";
	private final Map<String, com.publicissapient.kpidashboard.common.model.azureboards.iterations.Value> sprintPathsMap = new HashMap<>();
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Autowired
	private AzureProcessorRepository azureProcessorRepository;
	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;
	@Autowired
	private AzureProcessorConfig azureProcessorConfig;
	@Autowired
	private AesEncryptionService aesEncryptionService;
	@Autowired
	private ProcessorAzureRestClient processorAzureRestClient;
	@Autowired
	private SprintClient sprintClient;
	@Autowired
	private AdditionalFilterHelper additionalFilterHelper;
	@Autowired
	private HierarchyLevelService hierarchyLevelService;
	@Autowired
	private ScrumHandleAzureIssueHistory scrumHandleAzureIssueHistory;
	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;
	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

	@Override
	public int processesAzureIssues(ProjectConfFieldMapping projectConfig, String projectKey, // NOSONAR
			// //NOPMD
			AzureAdapter azureAdapter) {
		int count = 0;
		int totalSavedCount = 0;
		Map<String, LocalDateTime> lastSavedJiraIssueChangedDateByType = new HashMap<>();
		ProcessorExecutionTraceLog processorExecutionTraceLog = createTraceLog(projectConfig);
		// fetch delta start date. for first run data is fetch from date
		// mentioned in deltaStartDate property property file
		// otherwise fetch latest update date from AzureIssue collection and
		// fetch delta data.
		try {
			boolean dataExist = (jiraIssueRepository
					.findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);

			Map<String, LocalDateTime> maxChangeDatesByIssueType = getLastChangedDatesByIssueType(projectConfig);

			Map<String, LocalDateTime> startTimesByIssueType = new HashMap<>();

			maxChangeDatesByIssueType.forEach(
					(k, v) -> startTimesByIssueType.put(k, v.minusMinutes(azureProcessorConfig.getMinsToReduce())));

			int pageSize = azureAdapter.getPageSize();

			// Prepare Azure server for API calls
			AzureServer azureServer = prepareAzureServer(projectConfig);

			// Fetch Azure WorkItem ids by calling Wiql API
			AzureWiqlModel azureWiqlModel = azureAdapter.getWiqlModel(azureServer, startTimesByIssueType, projectConfig,
					dataExist);

			List<WorkItem> workItems = azureWiqlModel.getWorkItems();
			List<Integer> workItemIds = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(workItems)) {
				for (WorkItem workItem : workItems) {
					workItemIds.add(workItem.getId());
				}

				// Fetch Azure Iterations data
				AzureIterationsModel azureIterationsModel = azureAdapter.getIterationsModel(azureServer);
				if (CollectionUtils.isNotEmpty(azureIterationsModel.getValue())) {
					List<com.publicissapient.kpidashboard.common.model.azureboards.iterations.Value> sprintValueList = azureIterationsModel
							.getValue();
					for (com.publicissapient.kpidashboard.common.model.azureboards.iterations.Value value : sprintValueList) {
						String sprintPath = value.getPath();
						String finalSprintPath = getModifiedSprintsPath(sprintPath);
						sprintPathsMap.put(finalSprintPath, value);
					}
				}
				Set<SprintDetails> sprintDetailsSet = new LinkedHashSet<>();

				// Loop for fetching paged Work items
				for (int i = 0; i < workItemIds.size(); i += pageSize) {

					debugIssuesBeingProcessed(pageSize, workItemIds, i);

					List<Integer> pagedworkItemIds = new ArrayList<>(
							workItemIds.subList(i, Math.min(i + pageSize, workItemIds.size())));
					AzureBoardsWIModel azureBoardsWIModel = azureAdapter.getWorkItemInfoForIssues(i, azureServer,
							pagedworkItemIds);
					List<Value> issues = azureBoardsWIModel.getValue();
					/*
					 * To check for Offline mode // in case of offline method issues size can be
					 * greater than // pageSize, increase page size so that same issues not read
					 */

					if (CollectionUtils.isNotEmpty(issues)) {
						totalSavedCount += saveAzureIssueDetails(issues, projectConfig, sprintDetailsSet);
						count += issues.size();
					}

					MDC.put("IssueCount", String.valueOf(issues.size()));

					if (issues == null || issues.size() < pageSize) {
						break;
					}
				}

				lastSavedJiraIssueChangedDateByType = findLastSavedJiraIssueByType(
						projectConfig.getBasicProjectConfigId(), projectConfig.getFieldMapping());

				// sprint report prepare and save sprint details
				sprintClient.prepareSprintReport(projectConfig, sprintDetailsSet, azureAdapter, azureServer);
			}

		} catch (JSONException | NullPointerException e) {
			log.error("Error while updating Story information in scrum client", e);
			lastSavedJiraIssueChangedDateByType.clear();
		} catch (Exception e) {
			lastSavedJiraIssueChangedDateByType.clear();
		} finally {
			boolean isAttemptSuccess = isAttemptSuccess(totalSavedCount, count);
			if (!isAttemptSuccess) {
				lastSavedJiraIssueChangedDateByType.clear();
				processorExecutionTraceLog.setLastSuccessfulRun(null);
				log.error("Error in Fetching Issues");
			} else {
				processorExecutionTraceLog.setLastSuccessfulRun(
						DateUtil.dateTimeFormatter(LocalDateTime.now(), DateUtil.DATE_TIME_FORMAT));
			}
			saveExecutionTraceLog(processorExecutionTraceLog, lastSavedJiraIssueChangedDateByType, isAttemptSuccess,
					projectConfig.getProjectBasicConfig());
		}

		return count;

	}

	private void debugIssuesBeingProcessed(int pageSize, List<Integer> workItemIds, int i) {
		if (log.isDebugEnabled()) {
			int pageEnd = Math.min(i + pageSize - 1, workItemIds.size());
			log.debug(String.format("Processing issues %d - %d out of %d", i, pageEnd, workItemIds.size()));
		}
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
	public void purgeAzureIssues(List<Value> purgeIssuesList, ProjectConfFieldMapping projectConfig) {

		List<JiraIssue> jiraIssuesToDelete = Lists.newArrayList();
		List<JiraIssueCustomHistory> jiraIssuesHistoryToDelete = Lists.newArrayList();
		purgeIssuesList.forEach(issue -> {
			String issueId = AzureProcessorUtil.deodeUTF8String(issue.getId());
			String projectKeyIssueId = getModifiedIssueId(projectConfig, issueId);
			JiraIssue jiraIssue = findOneAzureIssue(projectKeyIssueId,
					projectConfig.getBasicProjectConfigId().toString());
			JiraIssueCustomHistory jiraIssueHistory = findOneAzureIssueHistory(projectKeyIssueId,
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
	 * Saves Azure issues details
	 *
	 * @param currentPagedAzureRs
	 *            List of Azure issue in current page call
	 * @param projectConfig
	 *            Project Configuration Mapping
	 * @throws JSONException
	 *             Error If JSON is invalid
	 */
	@Override
	public int saveAzureIssueDetails(List<Value> currentPagedAzureRs, ProjectConfFieldMapping projectConfig,
			Set<SprintDetails> sprintDetailsSet) // NOSONAR
			// //NOPMD
			throws JSONException {

		if (null == currentPagedAzureRs) {
			log.error("Azure Boards Processor | No list of current paged Azure Boards issues found");
			return 0;
		}
		List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
				.getFullHierarchyLevels(projectConfig.isKanban());
		Set<Assignee> assigneeSetToSave = new HashSet<>();
		List<JiraIssue> azureIssuesToSave = new ArrayList<>();
		List<JiraIssueCustomHistory> azureIssueHistoryToSave = new ArrayList<>();

		ObjectId azureProcessorId = azureProcessorRepository.findByProcessorName(ProcessorConstants.AZURE).getId();
		AssigneeDetails assigneeDetails = assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(
				projectConfig.getBasicProjectConfigId().toString(), ProcessorConstants.AZURE);
		for (Value issue : currentPagedAzureRs) {
			FieldMapping fieldMapping = projectConfig.getFieldMapping();

			if (null == fieldMapping) {
				return 0;
			}
			Set<String> issueTypeNames = new HashSet<>();
			for (String issueTypeName : fieldMapping.getJiraIssueTypeNames()) {
				issueTypeNames.add(issueTypeName.toLowerCase(Locale.getDefault()));
			}

			String issueId = AzureProcessorUtil.deodeUTF8String(issue.getId());

			// Modifying issueId to get unique value to each project
			String projectKeyIssueId = getModifiedIssueId(projectConfig, issueId);

			JiraIssue azureIssue = findOneAzureIssue(projectKeyIssueId,
					projectConfig.getBasicProjectConfigId().toString());
			if (azureIssue == null) {
				azureIssue = new JiraIssue();
			}
			JiraIssueCustomHistory azureIssueHistory = findOneAzureIssueHistory(projectKeyIssueId,
					projectConfig.getBasicProjectConfigId().toString());
			if (azureIssueHistory == null) {
				azureIssueHistory = new JiraIssueCustomHistory();
			}
			Fields fields = issue.getFields();
			Map<String, Object> fieldsMap = AzureIssueClientUtil.buildFieldMap(issue.getFields());

			String issueType = fields.getSystemWorkItemType();

			setURL(issueId, azureIssue, projectConfig);

			// Add RCA to JiraIssue
			setRCA(fieldMapping, issue, azureIssue, fieldsMap, azureProcessorConfig.getRcaValuesForCodeIssue());

			// Add UAT/Third Party identification field to JiraIssue
			setThirdPartyDefectIdentificationField(fieldMapping, issue, azureIssue, fieldsMap);

			if (issueTypeNames
					.contains(AzureProcessorUtil.deodeUTF8String(issueType).toLowerCase(Locale.getDefault()))) {

				log.debug(String.format("[%-12s] %s", AzureProcessorUtil.deodeUTF8String(issue.getId()),
						AzureProcessorUtil.deodeUTF8String(fields.getSystemTitle())));
				// collectorId
				azureIssue.setProcessorId(azureProcessorId);

				azureIssue.setIssueId(AzureProcessorUtil.deodeUTF8String(projectKeyIssueId));

				azureIssue.setTypeId(AzureProcessorUtil.deodeUTF8String(issueType));
				azureIssue.setTypeName(AzureProcessorUtil.deodeUTF8String(issueType));
				azureIssue.setOriginalType(AzureProcessorUtil.deodeUTF8String(issueType));

				// set defecttype to BUG
				if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype())
						&& fieldMapping.getJiradefecttype().stream().anyMatch(issueType::equalsIgnoreCase)) {
					azureIssue.setTypeName(NormalizedJira.DEFECT_TYPE.getValue());
				}

				// Set EPIC issue data for issue type epic
				if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueEpicType()) && fieldMapping
						.getJiraIssueEpicType().contains(AzureProcessorUtil.deodeUTF8String(issueType))) {
					setEpicIssueData(fieldMapping, azureIssue, fieldsMap);
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

				// Links stories/Defects with other Stories defect
				setStoryLinkWithDefect(issue, azureIssue, projectConfig);

				// ADD QA identification field to feature
				setQADefectIdentificationField(fieldMapping, issue, azureIssue, fieldsMap);

				// ADD Production Incident field to feature
				setProdIncidentIdentificationField(fieldMapping, issue, azureIssue, fieldsMap);

				setIssueTechStoryType(fieldMapping, issue, azureIssue, fieldsMap);

				// Placeholder for Affected Versions for Azure Issue

				// Placeholder for Release Version mapping for Azure Issue.

				// Sprint mapping for Azure Issue
				String sprintPathFromIssue = issue.getFields().getSystemIterationPath();
				String finalsprintPathFromIssue = getModifiedSprintsPath(sprintPathFromIssue);
				if (null != sprintPathsMap && sprintPathsMap.containsKey(finalsprintPathFromIssue)) {
					processSprintData(azureIssue, sprintPathsMap.get(finalsprintPathFromIssue), projectConfig,
							sprintDetailsSet);
				}

				setJiraAssigneeDetails(azureIssue, fields, assigneeSetToSave, projectConfig);

				// setting filter data from JiraIssue to
				// jira_issue_custom_history
				setAzureIssueHistory(azureIssueHistory, azureIssue, issue, fieldMapping, projectConfig, fieldsMap);

				setDueDates(azureIssue, fields, fieldsMap, fieldMapping);

				if (StringUtils.isNotBlank(azureIssue.getProjectID())) {
					azureIssuesToSave.add(azureIssue);
					azureIssueHistoryToSave.add(azureIssueHistory);
				}

			}
		}

		// Saving back to MongoDB
		jiraIssueRepository.saveAll(azureIssuesToSave);
		jiraIssueCustomHistoryRepository.saveAll(azureIssueHistoryToSave);
		saveAssigneeDetailsToDb(projectConfig, assigneeSetToSave, assigneeDetails);

		saveAccountHierarchy(azureIssuesToSave, projectConfig, hierarchyLevelList);
		return azureIssuesToSave.size();
	}

	private String getModifiedIssueId(ProjectConfFieldMapping projectConfig, String issueId) {
		StringBuilder projectKeyIssueId = new StringBuilder(projectConfig.getProjectKey());
		projectKeyIssueId.append("-").append(issueId);
		return projectKeyIssueId.toString();
	}

	/**
	 * Finds one JiraIssue by issueId
	 *
	 * @param issueId
	 *            azure issueId
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return JiraIssue corresponding to provided IssueId in DB
	 */
	private JiraIssue findOneAzureIssue(String issueId, String basicProjectConfigId) {
		JiraIssue jiraIssues = jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(issueId, basicProjectConfigId);

		if (ObjectUtils.allNull(jiraIssues)) {
			return null;
		}

		return jiraIssues;
	}

	/**
	 * Gets one JiraIssueCustomHistory entry by issueId
	 *
	 * @param issueId
	 *            Azzure Issue ID
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return JiraIssueCustomHistory corresponding to given IssueId from DB
	 */
	private JiraIssueCustomHistory findOneAzureIssueHistory(String issueId, String basicProjectConfigId) {
		JiraIssueCustomHistory jiraIssues = jiraIssueCustomHistoryRepository
				.findByStoryIDAndBasicProjectConfigId(issueId, basicProjectConfigId);

		if (ObjectUtils.isNotEmpty(jiraIssues)) {
			return jiraIssues;
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
	 *            Azure issue
	 * @param azureIssue
	 *            jiraIssue
	 */

	private void setThirdPartyDefectIdentificationField(FieldMapping fieldMapping, Value issue, JiraIssue azureIssue,
			Map<String, Object> fieldsMap) {
		Fields fields = issue.getFields();

		if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype()) && fieldMapping.getJiradefecttype().stream()
				.anyMatch(fields.getSystemWorkItemType()::equalsIgnoreCase)) {

			String jiraBugRaisedByCustomField = fieldMapping.getJiraBugRaisedByCustomField();

			if (StringUtils.isNotBlank(fieldMapping.getJiraBugRaisedByIdentification())
					&& fieldMapping.getJiraBugRaisedByIdentification().trim()
							.equalsIgnoreCase(AzureConstants.CUSTOM_FIELD)
					&& fieldsMap.containsKey(jiraBugRaisedByCustomField.trim())
					&& fieldsMap.get(jiraBugRaisedByCustomField.trim()) != null
					&& isBugRaisedByValueMatchesRaisedByCustomField(fieldMapping.getJiraBugRaisedByValue(),
							fieldsMap.get(jiraBugRaisedByCustomField.trim()))) {
				azureIssue.setDefectRaisedBy(NormalizedJira.THIRD_PARTY_DEFECT_VALUE.getValue());
			} else {
				azureIssue.setDefectRaisedBy("");
			}
		}
	}

	public String getModifiedSprintsPath(String sprintPath) {
		String finalSprintPath = StringUtils.EMPTY;
		String separator = "\\";
		if (StringUtils.isNotEmpty(sprintPath)) {
			int sepPos = sprintPath.indexOf(separator);
			if (sepPos == -1) {
				finalSprintPath = sprintPath;
			} else {
				finalSprintPath = sprintPath.substring(sepPos + separator.length());
			}

		}
		return finalSprintPath;
	}

	/**
	 * Process sprint details
	 *
	 * @param azureIssue
	 *            JiraIssue
	 * @param value
	 *            Value containing sprint Data
	 */
	private void processSprintData(JiraIssue azureIssue,
			com.publicissapient.kpidashboard.common.model.azureboards.iterations.Value value,
			ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet) {
		if (value == null) {
			// Issue #678 - leave sprint blank. Not having a sprint does not
			// imply kanban
			// as a story on a scrum board without a sprint is really on the
			// backlog
			azureIssue.setSprintID("");
			azureIssue.setSprintName("");
			azureIssue.setSprintBeginDate("");
			azureIssue.setSprintEndDate("");
			azureIssue.setSprintAssetState("");
		} else {
			String sprintId = value.getId() + AzureConstants.COMBINE_IDS_SYMBOL + azureIssue.getProjectName()
					+ AzureConstants.COMBINE_IDS_SYMBOL + projectConfig.getBasicProjectConfigId();
			setSprintData(azureIssue, value, sprintId);
			populateSprintDetails(value, sprintDetailsSet, sprintId);
		}
		azureIssue.setSprintChangeDate("");
		azureIssue.setSprintIsDeleted(AzureConstants.FALSE);
	}

	private void setSprintData(JiraIssue jiraIssue,
			com.publicissapient.kpidashboard.common.model.azureboards.iterations.Value value, String sprintId) {
		List<String> sprintsList = new ArrayList<>();

		sprintsList.add(value.getId());
		jiraIssue.setSprintIdList(sprintsList);
		jiraIssue.setSprintName(value.getName() == null ? StringUtils.EMPTY : value.getName());
		jiraIssue.setSprintID(value.getId() == null ? StringUtils.EMPTY : sprintId);
		if (value.getAttributes() != null) {
			jiraIssue.setSprintBeginDate(value.getAttributes().getStartDate() == null ? StringUtils.EMPTY
					: AzureProcessorUtil.getFormattedDate(value.getAttributes().getStartDate()));
			jiraIssue.setSprintEndDate(value.getAttributes().getFinishDate() == null ? StringUtils.EMPTY
					: AzureProcessorUtil.getFormattedDate(value.getAttributes().getFinishDate()));
			String sprintState = value.getAttributes().getTimeFrame() == null ? StringUtils.EMPTY
					: value.getAttributes().getTimeFrame();
			if (StringUtils.isNotEmpty(sprintState) && sprintState.equalsIgnoreCase(SPRINT_STATE_CLOSED)) {
				jiraIssue.setSprintAssetState(CLOSED);
			} else {
				jiraIssue.setSprintAssetState(sprintState);
			}
		}
	}

	/**
	 * @param fieldMapping
	 *            fieldMapping
	 * @param issue
	 *            issue
	 * @param azureIssue
	 *            jiraIssue
	 * @param fieldsMap
	 *            fieldsMap
	 */

	private void setQADefectIdentificationField(FieldMapping fieldMapping, Value issue, JiraIssue azureIssue,
			Map<String, Object> fieldsMap) {
		try {
			Fields fields = issue.getFields();

			if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype()) && fieldMapping.getJiradefecttype()
					.stream().anyMatch(fields.getSystemWorkItemType()::equalsIgnoreCase)) {

				String jiraBugRaisedByQACustomField = fieldMapping.getJiraBugRaisedByQACustomField();
				azureIssue.setDefectRaisedByQA(false);
				if (null != fieldMapping.getJiraBugRaisedByQAIdentification() && fieldMapping
						.getJiraBugRaisedByQAIdentification().trim().equalsIgnoreCase(AzureConstants.LABELS)) {
					getJiraBugRaisedByQAForLabels(fieldMapping, azureIssue, fields);
				} else if (isBugRaisedConditionForCustomField(fieldMapping, fieldsMap, jiraBugRaisedByQACustomField)) {
					azureIssue.setDefectRaisedByQA(true);
				}
			}

		} catch (Exception e) {
			log.error("Error while parsing QA field", e);
		}

	}

	/**
	 * ADD Production Incident field to feature
	 * 
	 * @param fieldMapping
	 *            fieldMapping
	 * @param issue
	 *            issue
	 * @param azureIssue
	 *            azureIssue
	 * @param fieldsMap
	 *            fieldsMap
	 */
	private void setProdIncidentIdentificationField(FieldMapping fieldMapping, Value issue, JiraIssue azureIssue,
			Map<String, Object> fieldsMap) {
		try {
			Fields fields = issue.getFields();

			if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype()) && fieldMapping.getJiradefecttype()
					.stream().anyMatch(fields.getSystemWorkItemType()::equalsIgnoreCase)) {

				String jiraProductionIncidentCustomField = fieldMapping.getJiraProdIncidentRaisedByCustomField();
				azureIssue.setProductionIncident(false);
				if (null != fieldMapping.getJiraProductionIncidentIdentification() && fieldMapping
						.getJiraProductionIncidentIdentification().trim().equalsIgnoreCase(AzureConstants.LABELS)) {
					getJiraProdIncidentForLabels(fieldMapping, azureIssue, fields);
				} else if (isProdIncidentConditionForCustomField(fieldMapping, fieldsMap,
						jiraProductionIncidentCustomField)) {
					azureIssue.setProductionIncident(true);
				}
			}

		} catch (Exception e) {
			log.error("Error while parsing production incident field", e);
		}

	}

	private boolean isBugRaisedConditionForCustomField(FieldMapping fieldMapping, Map<String, Object> fieldsMap,
			String jiraBugRaisedByQACustomField) {
		return null != fieldMapping.getJiraBugRaisedByQAIdentification()
				&& fieldMapping.getJiraBugRaisedByQAIdentification().trim()
						.equalsIgnoreCase(AzureConstants.CUSTOM_FIELD)
				&& fieldsMap.containsKey(jiraBugRaisedByQACustomField.trim())
				&& fieldsMap.get(jiraBugRaisedByQACustomField.trim()) != null
				&& isBugRaisedByValueMatchesRaisedByCustomField(fieldMapping.getJiraBugRaisedByQAValue(),
						fieldsMap.get(jiraBugRaisedByQACustomField.trim()));
	}

	private boolean isProdIncidentConditionForCustomField(FieldMapping fieldMapping, Map<String, Object> fieldsMap,
			String jiraProductionIncidentCustomField) {
		return null != fieldMapping.getJiraProductionIncidentIdentification()
				&& fieldMapping.getJiraProductionIncidentIdentification().trim()
						.equalsIgnoreCase(AzureConstants.CUSTOM_FIELD)
				&& fieldsMap.containsKey(jiraProductionIncidentCustomField.trim())
				&& fieldsMap.get(jiraProductionIncidentCustomField.trim()) != null
				&& isBugRaisedByValueMatchesRaisedByCustomField(fieldMapping.getJiraProdIncidentRaisedByValue(),
						fieldsMap.get(jiraProductionIncidentCustomField.trim()));
	}

	private void getJiraBugRaisedByQAForLabels(FieldMapping fieldMapping, JiraIssue azureIssue, Fields fields) {
		if (StringUtils.isNotEmpty(fields.getSystemTags())) {
			String[] labelArray = fields.getSystemTags().split(";");
			Set<String> labels = new HashSet<>(Arrays.asList(labelArray));
			if (isBugRaisedByValueMatchesRaisedByLabels(fieldMapping.getJiraBugRaisedByQAValue(), labels)) {
				azureIssue.setDefectRaisedByQA(true);
			}
		}
	}

	private void getJiraProdIncidentForLabels(FieldMapping fieldMapping, JiraIssue azureIssue, Fields fields) {
		if (StringUtils.isNotEmpty(fields.getSystemTags())) {
			String[] labelArray = fields.getSystemTags().split(";");
			Set<String> labels = new HashSet<>(Arrays.asList(labelArray));
			if (isBugRaisedByValueMatchesRaisedByLabels(fieldMapping.getJiraProdIncidentRaisedByValue(), labels)) {
				azureIssue.setProductionIncident(true);
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

	public boolean isBugRaisedByValueMatchesRaisedByLabels(List<String> bugRaisedValue, Set<String> issueFieldValue) {
		List<String> lowerCaseBugRaisedValue = bugRaisedValue.stream().map(String::toLowerCase)
				.collect(Collectors.toList());
		boolean isRaisedByThirdParty = false;

		for (String fieldValue : issueFieldValue) {
			if (lowerCaseBugRaisedValue.contains(fieldValue.toLowerCase())) {
				isRaisedByThirdParty = true;
				break;
			}
		}

		return isRaisedByThirdParty;
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

		return lowerCaseBugRaisedValue.contains(issueFieldValue.toString().toLowerCase());
	}

	private void setAzureIssueHistory(JiraIssueCustomHistory azureIssueHistory, JiraIssue azureIssue, Value issue,
			FieldMapping fieldMapping, ProjectConfFieldMapping projectConfig, Map<String, Object> fieldsMap) {

		azureIssueHistory.setProjectID(azureIssue.getProjectName());
		azureIssueHistory.setProjectComponentId(azureIssue.getProjectID());
		azureIssueHistory.setProjectKey(azureIssue.getProjectKey());
		azureIssueHistory.setStoryType(azureIssue.getTypeName());
		azureIssueHistory.setAdditionalFilters(azureIssue.getAdditionalFilters());
		azureIssueHistory.setUrl(azureIssue.getUrl());
		azureIssueHistory.setDescription(azureIssue.getName());

		// This method is not setup method. write it to keep
		// custom history
		processAzureIssueHistory(azureIssueHistory, azureIssue, issue, fieldMapping, projectConfig, fieldsMap);

		azureIssueHistory.setBasicProjectConfigId(azureIssue.getBasicProjectConfigId());
	}

	/**
	 * Process Jira issue History
	 *
	 * @param azureIssueCustomHistory
	 *            azureIssueCustomHistory
	 * @param azureIssue
	 *            azureIssue
	 * @param issue
	 *            Atlassain issue
	 * @param fieldMapping
	 *            Project field Mapping
	 * @param projectConfig
	 *            Project Config
	 */
	private void processAzureIssueHistory(JiraIssueCustomHistory azureIssueCustomHistory, JiraIssue azureIssue,
			Value issue, FieldMapping fieldMapping, ProjectConfFieldMapping projectConfig,
			Map<String, Object> fieldsMap) {

		String issueId = AzureProcessorUtil.deodeUTF8String(issue.getId());
		AzureServer server = prepareAzureServer(projectConfig);
		AzureAdapter azureAdapter = new OnlineAdapter(azureProcessorConfig, processorAzureRestClient);
		AzureUpdatesModel azureUpdatesModel = azureAdapter.getUpdates(server, issueId);
		List<com.publicissapient.kpidashboard.common.model.azureboards.updates.Value> valueList = azureUpdatesModel
				.getValue();

		if (null != azureIssue.getDevicePlatform()) {
			azureIssueCustomHistory.setDevicePlatform(azureIssue.getDevicePlatform());
		}

		if (null == azureIssueCustomHistory.getStoryID()) {
			addStoryHistory(azureIssueCustomHistory, azureIssue, issue, valueList, fieldMapping, fieldsMap);
		} else {
			if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(azureIssue.getTypeName())) {
				azureIssueCustomHistory.setDefectStoryID(azureIssue.getDefectStoryID());
			}

			scrumHandleAzureIssueHistory.setJiraIssueCustomHistoryUpdationLog(azureIssueCustomHistory, valueList,
					fieldMapping, fieldsMap);

		}

	}

	/**
	 * Adds Jira issue history
	 *
	 * @param azureIssueCustomHistory
	 *            JiraIssueCustomHistory
	 * @param azureIssue
	 *            JiraIssue
	 * @param issue
	 *            Atlassian Issue
	 * @param valueList
	 *            Update value list
	 * @param fieldMapping
	 *            project Fieldmapping
	 */
	private void addStoryHistory(JiraIssueCustomHistory azureIssueCustomHistory, JiraIssue azureIssue, Value issue,
			List<com.publicissapient.kpidashboard.common.model.azureboards.updates.Value> valueList,
			FieldMapping fieldMapping, Map<String, Object> fieldsMap) {

		DateTime dateTime = new DateTime(
				AzureProcessorUtil.getFormattedDateTime(issue.getFields().getSystemCreatedDate()));
		azureIssueCustomHistory.setCreatedDate(dateTime);

		azureIssueCustomHistory.setStoryID(azureIssue.getNumber());
		scrumHandleAzureIssueHistory.setJiraIssueCustomHistoryUpdationLog(azureIssueCustomHistory, valueList,
				fieldMapping, fieldsMap);
		// estimate
		azureIssueCustomHistory.setEstimate(azureIssue.getEstimate());
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(azureIssue.getTypeName())) {
			azureIssueCustomHistory.setDefectStoryID(azureIssue.getDefectStoryID());
		}

	}

	/**
	 * Saves accountHierarchy for filter
	 *
	 * @param jiraIssueList
	 *            list of jira issues
	 * @param projectConfig
	 *            Project Configuration Map
	 * @param hierarchyLevelList
	 *            hierarchyLevelList
	 */
	private void saveAccountHierarchy(List<JiraIssue> jiraIssueList, ProjectConfFieldMapping projectConfig, // NOPMD
																											// //NOSONAR
			List<HierarchyLevel> hierarchyLevelList) { // NOSONAR

		Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
				.collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));

		HierarchyLevel sprintHierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);

		Map<Pair<String, String>, AccountHierarchy> existingHierarchy = AzureIssueClientUtil
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

	private String decryptKey(String encryptedKey) {
		return aesEncryptionService.decrypt(encryptedKey, azureProcessorConfig.getAesEncryptionKey());
	}

	private AzureServer prepareAzureServer(ProjectConfFieldMapping projectConfig) {
		AzureServer azureServer = new AzureServer();
		azureServer.setPat(decryptKey(projectConfig.getAzure().getConnection().getPat()));
		azureServer.setUrl(AzureProcessorUtil.encodeSpaceInUrl(projectConfig.getAzure().getConnection().getBaseUrl()));
		azureServer.setApiVersion(projectConfig.getAzure().getApiVersion());
		azureServer.setUsername(projectConfig.getAzure().getConnection().getUsername());
		return azureServer;
	}

	/**
	 * Sets Story Link with Defect
	 *
	 * @param issue
	 *            Atlassian Issue
	 * @param azureIssue
	 *            Jira Issue
	 */
	private void setStoryLinkWithDefect(Value issue, JiraIssue azureIssue, ProjectConfFieldMapping projectConfig) {
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(azureIssue.getTypeName())
				|| NormalizedJira.TEST_TYPE.getValue().equalsIgnoreCase(azureIssue.getTypeName())) {

			Set<String> defectStorySet = new HashSet<>();

			List<Relation> relations = issue.getRelations();
			if (relations != null) {
				setDefectStorySet(defectStorySet, relations, projectConfig);
			}
			azureIssue.setDefectStoryID(defectStorySet);
		}
	}

	private void setDefectStorySet(Set<String> defectStorySet, List<Relation> relations,
			ProjectConfFieldMapping projectConfig) {
		for (Relation relation : relations) {
			Attribute attributes = relation.getAttributes();
			if (attributes != null) {
				String name = attributes.getName();
				String url = relation.getUrl();
				if (url.contains("_apis/wit/workItems")) {
					int i = url.lastIndexOf('/');
					String storyIdFromDefect = url.substring(i + 1);
					if (CollectionUtils.isNotEmpty(azureProcessorConfig.getExcludeLinks())
							&& azureProcessorConfig.getExcludeLinks().stream().anyMatch(name::equalsIgnoreCase)) {
						break;
					}
					String projectKeyIssueId = getModifiedIssueId(projectConfig, storyIdFromDefect);
					defectStorySet.add(projectKeyIssueId);
				}
			}
		}
	}

	private Map<String, LocalDateTime> findLastSavedJiraIssueByType(ObjectId projectConfigId,
			FieldMapping fieldMapping) {
		String[] jiraIssueTypeNames = fieldMapping.getJiraIssueTypeNames();
		Set<String> uniqueIssueTypes = new HashSet<>(Arrays.asList(jiraIssueTypeNames));

		Map<String, LocalDateTime> lastUpdatedDateByIssueType = new HashMap<>();
		ObjectId azureProcessorId = azureProcessorRepository.findByProcessorName(ProcessorConstants.AZURE).getId();
		for (String issueType : uniqueIssueTypes) {

			JiraIssue jiraIssue = jiraIssueRepository
					.findTopByProcessorIdAndBasicProjectConfigIdAndTypeNameAndChangeDateGreaterThanOrderByChangeDateDesc(
							azureProcessorId, projectConfigId.toString(), issueType,
							azureProcessorConfig.getStartDate());
			LocalDateTime configuredStartDate = LocalDateTime.parse(azureProcessorConfig.getStartDate(),
					DateTimeFormatter.ofPattern(AzureConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT));

			if (jiraIssue != null && jiraIssue.getChangeDate() != null) {
				LocalDateTime currentIssueDate = LocalDateTime.parse(jiraIssue.getChangeDate(),
						DateTimeFormatter.ofPattern(AzureConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT));
				lastUpdatedDateByIssueType.put(issueType, currentIssueDate);
			} else {
				lastUpdatedDateByIssueType.put(issueType, configuredStartDate);
			}
		}

		return lastUpdatedDateByIssueType;
	}

	private Map<String, LocalDateTime> getLastChangedDatesByIssueType(ProjectConfFieldMapping projectConfig) {
		ObjectId basicProjectConfigId = projectConfig.getBasicProjectConfigId();
		FieldMapping fieldMapping = projectConfig.getFieldMapping();
		ProjectBasicConfig projectBasicConfig = projectConfig.getProjectBasicConfig();

		String[] jiraIssueTypeNames = fieldMapping.getJiraIssueTypeNames();
		Set<String> uniqueIssueTypes = new HashSet<>(Arrays.asList(jiraIssueTypeNames));

		Map<String, LocalDateTime> lastUpdatedDateByIssueType = new HashMap<>();

		List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService
				.getTraceLogs(ProcessorConstants.AZURE, basicProjectConfigId.toHexString());
		ProcessorExecutionTraceLog projectTraceLog = null;

		if (CollectionUtils.isNotEmpty(traceLogs)) {
			projectTraceLog = traceLogs.get(0);
		}
		LocalDateTime configuredStartDate = LocalDateTime.parse(azureProcessorConfig.getStartDate(),
				DateTimeFormatter.ofPattern(AzureConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT));

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
	 * * Set Details related to issues with Epic Issue type
	 *
	 * @param fieldMapping
	 *            fieldMapping
	 *
	 * @param azureIssue
	 *            azureIssue
	 *
	 * @param fieldsMap
	 *            fieldsMap
	 */
	private void setEpicIssueData(FieldMapping fieldMapping, JiraIssue azureIssue, Map<String, Object> fieldsMap) {
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

	private void populateSprintDetails(com.publicissapient.kpidashboard.common.model.azureboards.iterations.Value value,
			Set<SprintDetails> sprintDetailsSet, String sprintId) {
		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setOriginalSprintId(value.getId());
		sprintDetails.setSprintID(sprintId);
		sprintDetails.setSprintName(value.getName());
		sprintDetails.setStartDate(sprintDetailsDateConverter(value.getAttributes().getStartDate()));
		sprintDetails.setEndDate(sprintDetailsDateConverter(value.getAttributes().getFinishDate()));
		sprintDetails.setCompleteDate(sprintDetailsDateConverter(value.getAttributes().getFinishDate()));
		sprintDetails.setState(getState(value.getAttributes().getTimeFrame()));
		sprintDetailsSet.add(sprintDetails);
	}

	private String sprintDetailsDateConverter(String date) {
		String from = "yyyy-MM-dd'T'HH:mm:ss'Z'";
		String to = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		String convertedDate = null;
		if (date != null) {
			convertedDate = DateUtil.dateTimeConverter(date, from, to);
		}
		return convertedDate;
	}

	private String getState(String state) {
		String sprintState = null;
		if (SPRINT_STATE_CLOSED.equals(state)) {
			sprintState = SprintDetails.SPRINT_STATE_CLOSED;
		} else if (SPRINT_STATE_ACTIVE.equals(state)) {
			sprintState = SprintDetails.SPRINT_STATE_ACTIVE;
		} else {
			sprintState = SprintDetails.SPRINT_STATE_FUTURE;
		}
		return sprintState;
	}

	private void setAdditionalFilters(JiraIssue jiraIssue, Value issue, ProjectConfFieldMapping projectConfig) {
		List<AdditionalFilter> additionalFilter = additionalFilterHelper.getAdditionalFilter(issue, projectConfig);
		jiraIssue.setAdditionalFilters(additionalFilter);
	}

	private void setProjectSpecificDetails(ProjectConfFieldMapping projectConfig, JiraIssue jiraIssue) {
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

	private AccountHierarchy createHierarchyForSprint(JiraIssue jiraIssue, ProjectBasicConfig projectBasicConfig,
			AccountHierarchy projectHierarchy, HierarchyLevel hierarchyLevel) {
		AccountHierarchy accountHierarchy = null;
		try {

			accountHierarchy = new AccountHierarchy();
			accountHierarchy.setBasicProjectConfigId(projectBasicConfig.getId());
			accountHierarchy.setIsDeleted(AzureConstants.FALSE);
			accountHierarchy.setLabelName(hierarchyLevel.getHierarchyLevelId());
			String sprintName = (String) PropertyUtils.getSimpleProperty(jiraIssue, "sprintName");
			String sprintId = (String) PropertyUtils.getSimpleProperty(jiraIssue, "sprintID");

			accountHierarchy.setNodeId(sprintId);
			accountHierarchy.setNodeName(sprintName + AzureConstants.COMBINE_IDS_SYMBOL + jiraIssue.getProjectName());

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

	/**
	 * setting URL to jira_issue
	 * 
	 * @param ticketNumber
	 * @param jiraIssue
	 * @param projectConfig
	 */
	private void setURL(String ticketNumber, JiraIssue jiraIssue, ProjectConfFieldMapping projectConfig) {
		String baseUrl = projectConfig.getAzure().getConnection().getBaseUrl();
		baseUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/");
		jiraIssue.setUrl(
				baseUrl.equals("") ? "" : baseUrl + azureProcessorConfig.getAzureDirectTicketLinkKey() + ticketNumber);
	}

	private void setDueDates(JiraIssue jiraIssue, Fields fields, Map<String, Object> fieldsMap,
			FieldMapping fieldMapping) {
		if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateField())) {
			if (fieldMapping.getJiraDueDateField().equalsIgnoreCase(CommonConstant.DUE_DATE)
					&& ObjectUtils.isNotEmpty(fields.getMicrosoftVSTSSchedulingDueDate())) {
				jiraIssue.setDueDate(
						AzureProcessorUtil.deodeUTF8String(fields.getMicrosoftVSTSSchedulingDueDate()).split("T")[0]
								.concat(DateUtil.ZERO_TIME_ZONE_FORMAT));
			} else if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateCustomField())
					&& fieldsMap.containsKey(fieldMapping.getJiraDueDateCustomField())
					&& ObjectUtils.isNotEmpty(fieldsMap.get(fieldMapping.getJiraDueDateCustomField()))) {
				Object issueField = fieldsMap.get(fieldMapping.getJiraDueDateCustomField());
				if (ObjectUtils.isNotEmpty(issueField)) {
					jiraIssue.setDueDate(AzureProcessorUtil.deodeUTF8String(issueField.toString()).split("T")[0]
							.concat(DateUtil.ZERO_TIME_ZONE_FORMAT));
				}
			}
		}
		if (StringUtils.isNotEmpty(fieldMapping.getJiraDevDueDateCustomField())
				&& fieldsMap.containsKey(fieldMapping.getJiraDevDueDateCustomField())
				&& ObjectUtils.isNotEmpty(fieldsMap.get(fieldMapping.getJiraDevDueDateCustomField()))) {
			Object issueField = fieldsMap.get(fieldMapping.getJiraDevDueDateCustomField());
			if (ObjectUtils.isNotEmpty(issueField)) {
				jiraIssue.setDevDueDate((AzureProcessorUtil.deodeUTF8String(issueField.toString()).split("T")[0]
						.concat(DateUtil.ZERO_TIME_ZONE_FORMAT)));
			}
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
		if (assigneeDetails == null) {
			assigneeDetails = new AssigneeDetails();
			assigneeDetails.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString());
			assigneeDetails.setSource(ProcessorConstants.AZURE);
			assigneeDetails.setAssignee(assigneeSetToSave);
			if (!projectConfig.getProjectBasicConfig().isSaveAssigneeDetails()) {
				assigneeDetails.setAssigneeSequence(2);
			}
		} else {
			Set<Assignee> updatedAssigneeSetToSave = new HashSet<>();
			updatedAssigneeSetToSave.addAll(assigneeDetails.getAssignee());
			updatedAssigneeSetToSave.addAll(assigneeSetToSave);
			assigneeDetails.setAssignee(updatedAssigneeSetToSave);
			if (!projectConfig.getProjectBasicConfig().isSaveAssigneeDetails()) {
				assigneeDetails.setAssigneeSequence(updatedAssigneeSetToSave.size() + 1);
			}
		}
		assigneeDetailsRepository.save(assigneeDetails);
	}

	private ProcessorExecutionTraceLog createTraceLog(ProjectConfFieldMapping projectConfig) {
		List<ProcessorExecutionTraceLog> traceLogs = processorExecutionTraceLogService
				.getTraceLogs(ProcessorConstants.AZURE, projectConfig.getBasicProjectConfigId().toHexString());
		ProcessorExecutionTraceLog processorExecutionTraceLog = null;

		if (CollectionUtils.isNotEmpty(traceLogs)) {
			processorExecutionTraceLog = traceLogs.get(0);
			if (null == processorExecutionTraceLog.getLastSuccessfulRun() || projectConfig.getProjectBasicConfig()
					.isSaveAssigneeDetails() != processorExecutionTraceLog.isLastEnableAssigneeToggleState()) {
				processorExecutionTraceLog.setLastSuccessfulRun(azureProcessorConfig.getStartDate());
			}
		} else {
			processorExecutionTraceLog = new ProcessorExecutionTraceLog();
			processorExecutionTraceLog.setProcessorName(ProcessorConstants.AZURE);
			processorExecutionTraceLog.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toHexString());
			processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
			processorExecutionTraceLog.setLastSuccessfulRun(azureProcessorConfig.getStartDate());
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
		processorExecutionTraceLogService.save(processorExecutionTraceLog);
	}

}