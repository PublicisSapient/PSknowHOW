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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueSprint;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScrumAzureIssueClientImpl extends AzureIssueClient {

	private static final String CLOSED = "CLOSED";
	
	private static final String SPRINT_STATE_CLOSED = "past";
	private static final String SPRINT_STATE_ACTIVE = "current";

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

	private final Map<String, com.publicissapient.kpidashboard.common.model.azureboards.iterations.Value> sprintPathsMap = new HashMap<>();

	@Override
	public int processesAzureIssues(ProjectConfFieldMapping projectConfig, String projectKey, // NOSONAR
									// //NOPMD
									AzureAdapter azureAdapter) {
		int count = 0;
		// fetch delta start date. for first run data is fetch from date
		// mentioned in deltaStartDate property property file
		// otherwise fetch latest update date from AzureIssue collection and
		// fetch
		// delta data.
		try {
			boolean dataExist = (jiraIssueRepository
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
			log.error("Error while updating Story information in scrum client", e);
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
	public void saveAzureIssueDetails(List<Value> currentPagedAzureRs, ProjectConfFieldMapping projectConfig) // NOSONAR
	// //NOPMD
			throws JSONException {

		if (null == currentPagedAzureRs) {
			log.error("Azure Boards Processor | No list of current paged Azure Boards issues found");
			return;
		}
		List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
				.getFullHierarchyLevels(projectConfig.isKanban());
		List<JiraIssue> azureIssuesToSave = new ArrayList<>();
		List<JiraIssueCustomHistory> azureIssueHistoryToSave = new ArrayList<>();
		Set<SprintDetails> sprintDetailsSet = new LinkedHashSet<>();

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

			setURL(issueId,azureIssue,projectConfig);

			// Add RCA to JiraIssue
			setRCA(fieldMapping, issue, azureIssue, fieldsMap, azureProcessorConfig.getRcaValuesForCodeIssue());

			// Add device platform filed to issue
			setDevicePlatform(fieldMapping, azureIssue, fieldsMap);

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

				setIssueTechStoryType(fieldMapping, issue, azureIssue, fieldsMap);

				// Placeholder for Affected Versions for Azure Issue

				// Placeholder for Release Version mapping for Azure Issue.

				// Sprint mapping for Azure Issue
				String sprintPathFromIssue = issue.getFields().getSystemIterationPath();
				String finalsprintPathFromIssue = getModifiedSprintsPath(sprintPathFromIssue);
				if (null != sprintPathsMap && sprintPathsMap.containsKey(finalsprintPathFromIssue)) {
					processSprintData(azureIssue, sprintPathsMap.get(finalsprintPathFromIssue), projectConfig, sprintDetailsSet);
				}

				setJiraAssigneeDetails(azureIssue, fields);

				// setting filter data from JiraIssue to
				// jira_issue_custom_history
				setAzureIssueHistory(azureIssueHistory, azureIssue, issue, fieldMapping, projectConfig);

				// Placeholder for Test Automated field mapping.

				if (StringUtils.isNotBlank(azureIssue.getProjectID())) {
					azureIssuesToSave.add(azureIssue);
					azureIssueHistoryToSave.add(azureIssueHistory);
				}

			}
		}

		// Saving back to MongoDB
		jiraIssueRepository.saveAll(azureIssuesToSave);
		jiraIssueCustomHistoryRepository.saveAll(azureIssueHistoryToSave);

		saveAccountHierarchy(azureIssuesToSave, projectConfig, hierarchyLevelList);
		sprintClient.processSprints(projectConfig, sprintDetailsSet);

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
		List<JiraIssue> jiraIssues = jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(StringEscapeUtils.escapeHtml4(issueId),
				basicProjectConfigId);

		// Not sure of the state of the data
		if (jiraIssues.size() > 1) {
			log.error("JIRA Processor | More than one Jira Issue item found for id {}", issueId);
		}

		if (!jiraIssues.isEmpty()) {
			return jiraIssues.get(0);
		}

		return null;
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
		List<JiraIssueCustomHistory> jiraIssues = jiraIssueCustomHistoryRepository
				.findByStoryIDAndBasicProjectConfigId(issueId, basicProjectConfigId);
		// Not sure of the state of the data
		if (jiraIssues.size() > 1) {
			log.warn("Azure Processor | More than one Issue id  found for history {}", issueId);
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
				} else if (isBugRaisedConditionforCustomField(fieldMapping, fieldsMap, jiraBugRaisedByQACustomField)) {
					azureIssue.setDefectRaisedByQA(true);
				}
			}

		} catch (Exception e) {
			log.error("Error while parsing QA field {}", e);
		}

	}

	private boolean isBugRaisedConditionforCustomField(FieldMapping fieldMapping, Map<String, Object> fieldsMap,
													   String jiraBugRaisedByQACustomField) {
		return null != fieldMapping.getJiraBugRaisedByQAIdentification()
				&& fieldMapping.getJiraBugRaisedByQAIdentification().trim().equalsIgnoreCase(AzureConstants.CUSTOM_FIELD)
				&& fieldsMap.containsKey(jiraBugRaisedByQACustomField.trim())
				&& fieldsMap.get(jiraBugRaisedByQACustomField.trim()) != null
				&& isBugRaisedByValueMatchesRaisedByCustomField(fieldMapping.getJiraBugRaisedByQAValue(),
				fieldsMap.get(jiraBugRaisedByQACustomField.trim()));
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
		boolean isRaisedByThirdParty = false;

		if (lowerCaseBugRaisedValue.contains(issueFieldValue.toString().toLowerCase())) {
			isRaisedByThirdParty = true;
		}

		return isRaisedByThirdParty;
	}

	private void setAzureIssueHistory(JiraIssueCustomHistory azureIssueHistory, JiraIssue azureIssue, Value issue,
									  FieldMapping fieldMapping, ProjectConfFieldMapping projectConfig) {

		azureIssueHistory.setProjectID(azureIssue.getProjectName());
		azureIssueHistory.setProjectComponentId(azureIssue.getProjectID());
		azureIssueHistory.setProjectKey(azureIssue.getProjectKey());
		azureIssueHistory.setStoryType(azureIssue.getTypeName());
		azureIssueHistory.setAdditionalFilters(azureIssue.getAdditionalFilters());
		azureIssueHistory.setUrl(azureIssue.getUrl());
		azureIssueHistory.setDescription(azureIssue.getName());

		// This method is not setup method. write it to keep
		// custom history
		processAzureIssueHistory(azureIssueHistory, azureIssue, issue, fieldMapping, projectConfig);
		
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
										  Value issue, FieldMapping fieldMapping, ProjectConfFieldMapping projectConfig) {

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
			addStoryHistory(azureIssueCustomHistory, azureIssue, issue, valueList, fieldMapping);
		} else {
			if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(azureIssue.getTypeName())) {
				azureIssueCustomHistory.setDefectStoryID(azureIssue.getDefectStoryID());
			}
			DateTime dateTime = new DateTime(
					AzureProcessorUtil.getFormattedDateTime(issue.getFields().getSystemCreatedDate()));

			List<JiraIssueSprint> listIssueSprint = getChangeLog(azureIssue, valueList, dateTime, fieldMapping);
			azureIssueCustomHistory.setStorySprintDetails(listIssueSprint);
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
								 FieldMapping fieldMapping) {

		DateTime dateTime = new DateTime(
				AzureProcessorUtil.getFormattedDateTime(issue.getFields().getSystemCreatedDate()));
		azureIssueCustomHistory.setCreatedDate(dateTime);

		List<JiraIssueSprint> listIssueSprint = getChangeLog(azureIssue, valueList, dateTime, fieldMapping);

		azureIssueCustomHistory.setStoryID(azureIssue.getNumber());
		azureIssueCustomHistory.setStorySprintDetails(listIssueSprint);
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
	private void saveAccountHierarchy(List<JiraIssue> jiraIssueList, ProjectConfFieldMapping projectConfig, // NOPMD //NOSONAR
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
	 * process Azure issue history
	 *
	 * @param azureIssue
	 *            azureIssue
	 * @param updateValueList
	 *            updateValueList
	 * @param systemCreatedDate
	 *            systemCreatedDate
	 * @param fieldMapping
	 *            fieldMapping
	 * @return List of JiraIssuesSprints
	 */
	private List<JiraIssueSprint> getChangeLog(JiraIssue azureIssue, // NOSONAR
											   // //NOPMD
											   List<com.publicissapient.kpidashboard.common.model.azureboards.updates.Value> updateValueList,
											   DateTime systemCreatedDate, FieldMapping fieldMapping) {
		String fromSprint = "";
		String fromSprintId = "";
		String toSprint = "";
		String toSprintId = "";
		String sprintStatus = "";
		boolean sprintUpdated = true;
		boolean sprintChanged = false;
		List<JiraIssueSprint> issueHistory = new ArrayList<>();
		List<String> jiraStatusForDevelopment = fieldMapping.getJiraStatusForDevelopment();
		List<String> jiraStatusForQa = fieldMapping.getJiraStatusForQa();
		// creating first entry of issue
		if (null != systemCreatedDate) {
			JiraIssueSprint jiraIssueSprint = new JiraIssueSprint();
			jiraIssueSprint.setActivityDate(systemCreatedDate);
			jiraIssueSprint.setFromStatus(fieldMapping.getStoryFirstStatus());
			jiraIssueSprint.setSprintId("");
			jiraIssueSprint.setSprintComponentId("");
			jiraIssueSprint.setStatus("");
			issueHistory.add(jiraIssueSprint);
			setIndividualDetails(azureIssue, jiraStatusForDevelopment, jiraStatusForQa, jiraIssueSprint);

		}
		if (CollectionUtils.isNotEmpty(updateValueList)) {

			for (com.publicissapient.kpidashboard.common.model.azureboards.updates.Value history : updateValueList) {
				com.publicissapient.kpidashboard.common.model.azureboards.updates.Fields changelogItem = history
						.getFields();
				// Check for Ietrationlevel2
				if (null != changelogItem) {
					if (null != changelogItem.getSystemIterationPath()) {

						fromSprint = getModifiedSprintsPath(changelogItem.getSystemIterationPath().getOldValue());

						fromSprintId = sprintPathsMap.get(fromSprint) == null ? ""
								: sprintPathsMap.get(fromSprint).getId();
						toSprint = getModifiedSprintsPath(changelogItem.getSystemIterationPath().getNewValue());
						toSprintId = sprintPathsMap.get(toSprint) == null ? "" : sprintPathsMap.get(toSprint).getId();
						sprintChanged = true;

					} else if (null != changelogItem.getSystemState()) {
						JiraIssueSprint jiraIssueSprint = new JiraIssueSprint();
						jiraIssueSprint.setStatus(sprintStatus);

						jiraIssueSprint.setSprintId(toSprint);
						if (StringUtils.isNotBlank(toSprintId)) {
							jiraIssueSprint.setSprintComponentId(toSprintId + "_" + azureIssue.getProjectName());
						} else {
							jiraIssueSprint.setSprintComponentId(toSprintId);
						}

						jiraIssueSprint.setStatus(sprintStatus);
						jiraIssueSprint.setFromStatus(changelogItem.getSystemState().getNewValue());
						DateTime changedDate = new DateTime(AzureProcessorUtil
								.getFormattedDateTime(changelogItem.getSystemChangedDate().getNewValue()));
						jiraIssueSprint.setActivityDate(changedDate);

						issueHistory.add(jiraIssueSprint);
						setIndividualDetails(azureIssue, jiraStatusForDevelopment, jiraStatusForQa, jiraIssueSprint);
					}
					if (sprintUpdated && sprintChanged) {
						for (int i = 0; i < issueHistory.size() - 1; i++) {
							JiraIssueSprint fsprint = issueHistory.get(i);
							String[] toSprintIdArrFrom = {};

							if (fromSprintId != null && fromSprintId.contains(",")) {
								toSprintIdArrFrom = fromSprintId.split(",");
							}
							if (toSprintIdArrFrom.length > 0) {
								fsprint.setSprintId(fromSprintId);
								if (StringUtils.isNotBlank(fromSprintId)) {
									fsprint.setSprintComponentId(fromSprintId + "_" + azureIssue.getProjectName());
								} else {
									fsprint.setSprintComponentId(fromSprintId);
								}
							}
							issueHistory.set(i, fsprint);
						}
						sprintUpdated = false;
					}

					/*
					 * check if only sprint changed. In this case only sprint
					 * name need to be updated with last status
					 */
					sprintChanged = updateIfSprintChanged(azureIssue, toSprint, toSprintId, sprintChanged,
							issueHistory);

				}
			}
		}
		/**
		 * check if no sprint found in changelog but sprint present in issue
		 * update sprint in all changelog
		 */
		if (StringUtils.isEmpty(fromSprint) && StringUtils.isEmpty(toSprint)
				&& !StringUtils.isEmpty(azureIssue.getSprintName())) {
			updateChangeLogWithSprint(issueHistory, azureIssue);
		}
		return issueHistory;

	}

	private void setIndividualDetails(JiraIssue azureIssue, List<String> jiraStatusForDevelopment,
									  List<String> jiraStatusForQa, JiraIssueSprint jiraIssueSprint) {
		if (CollectionUtils.isNotEmpty(jiraStatusForDevelopment)
				&& jiraStatusForDevelopment.stream().anyMatch(jiraIssueSprint.getFromStatus()::equalsIgnoreCase)
				&& StringUtils.isNotBlank(azureIssue.getAssigneeId())
				&& StringUtils.isNotBlank(azureIssue.getAssigneeName())) {

			azureIssue.setDeveloperId(azureIssue.getAssigneeId());
			azureIssue.setDeveloperName(azureIssue.getAssigneeName() + AzureConstants.OPEN_BRACKET
					+ azureIssue.getAssigneeId() + AzureConstants.CLOSED_BRACKET);

		}
		if (CollectionUtils.isNotEmpty(jiraStatusForQa)
				&& jiraStatusForQa.stream().anyMatch(jiraIssueSprint.getFromStatus()::equalsIgnoreCase)
				&& StringUtils.isNotBlank(azureIssue.getAssigneeId())
				&& StringUtils.isNotBlank(azureIssue.getAssigneeName())) {

			azureIssue.setQaId(azureIssue.getAssigneeId());
			azureIssue.setQaName(azureIssue.getAssigneeName() + AzureConstants.OPEN_BRACKET + azureIssue.getAssigneeId()
					+ AzureConstants.CLOSED_BRACKET);

		}

	}

	private boolean updateIfSprintChanged(JiraIssue azureIssue, String toSprint, String toSprintId,
										  boolean sprintChanged, List<JiraIssueSprint> issueHistory) {
		boolean changed = sprintChanged;
		if (sprintChanged && toSprint != null
				&& !(toSprint.equals(issueHistory.get(issueHistory.size() - 1).getSprintId()))) {
			JiraIssueSprint fsprint = issueHistory.get(issueHistory.size() - 1);

			fsprint.setSprintId(toSprint);
			if (StringUtils.isNotBlank(toSprintId)) {
				fsprint.setSprintComponentId(toSprintId + "_" + azureIssue.getProjectName());
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

	private Map<String, String> getLastChangedDatesByIssueType(ObjectId projectConfigId, FieldMapping fieldMapping) {
		String[] jiraIssueTypeNames = fieldMapping.getJiraIssueTypeNames();
		Set<String> uniqueIssueTypes = new HashSet<>(Arrays.asList(jiraIssueTypeNames));

		Map<String, String> lastUpdatedDateByIssueType = new HashMap<>();

		for (String issueType : uniqueIssueTypes) {

			ObjectId processorId = azureProcessorRepository.findByProcessorName(ProcessorConstants.AZURE).getId();
			JiraIssue jiraIssue = jiraIssueRepository
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

	/**
	 * * Set Details related to issues with Epic Issue type
	 *
	 * @param fieldMapping fieldMapping
	 *
	 * @param azureIssue azureIssue
	 *
	 * @param fieldsMap fieldsMap
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
	 * @param ticketNumber
	 * @param jiraIssue
	 * @param projectConfig
	 */
	private void setURL(String ticketNumber, JiraIssue jiraIssue, ProjectConfFieldMapping projectConfig) {
		String baseUrl = projectConfig.getAzure().getConnection().getBaseUrl();
		baseUrl= baseUrl + (baseUrl.endsWith("/") ? "" : "/");
		jiraIssue.setUrl(baseUrl.equals("")?"": baseUrl+azureProcessorConfig.getAzureDirectTicketLinkKey() + ticketNumber);
	}

}