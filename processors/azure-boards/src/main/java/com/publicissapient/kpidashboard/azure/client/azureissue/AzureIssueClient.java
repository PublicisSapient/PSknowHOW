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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.azure.adapter.AzureAdapter;
import com.publicissapient.kpidashboard.azure.config.AzureProcessorConfig;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.azure.util.AzureConstants;
import com.publicissapient.kpidashboard.azure.util.AzureProcessorUtil;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.azureboards.Fields;
import com.publicissapient.kpidashboard.common.model.azureboards.SystemAssignedTo;
import com.publicissapient.kpidashboard.common.model.azureboards.SystemCreatedBy;
import com.publicissapient.kpidashboard.common.model.azureboards.Value;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AzureIssueClient {// NOPMD //NOSONAR

	AssigneeDetails tempAssigneeDetails;

	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	public static String hash(String input) {
		return String.valueOf(Objects.hash(input));
	}

	public static void setLastUpdatedDateToStartDate(ProjectBasicConfig projectBasicConfig,
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
	 * @param projectKey
	 *            Project Key
	 * @param azureAdapter
	 *            the azure adapter
	 * @return int Count of Azure stories processed
	 */
	public abstract int processesAzureIssues(ProjectConfFieldMapping projectConfig, String projectKey,
			AzureAdapter azureAdapter);

	/**
	 * Purges the issues provided
	 *
	 * @param purgeIssuesList
	 *            List of issues to be purged
	 * @param projectConfig
	 *            Project Configuration Mapping
	 */
	public abstract void purgeAzureIssues(List<Value> purgeIssuesList, ProjectConfFieldMapping projectConfig);

	/**
	 * Saves Jira Issue details.
	 *
	 * @param currentPagedJiraRs
	 *            List of Azure issue in current page call
	 * @param projectConfig
	 *            Project Configuration Mapping
	 * @param sprintDetailsSet
	 *            sprint details set
	 * @throws JSONException
	 *             Error while JSON parsing
	 */
	public abstract int saveAzureIssueDetails(List<Value> currentPagedJiraRs, ProjectConfFieldMapping projectConfig,
			Set<SprintDetails> sprintDetailsSet) throws JSONException;

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
	public void setRCA(FieldMapping fieldMapping, Value issue, JiraIssue azureIssue, Map<String, Object> fieldsMap,
			List<String> rcaValuesForCodeIssue) {
		Fields fields = issue.getFields();
		String rootCauseFieldFromFieldMapping = fieldMapping.getRootCause();

		if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype()) && fieldMapping.getJiradefecttype().stream()
				.anyMatch(fields.getSystemWorkItemType()::equalsIgnoreCase)) {
			String rcaCause = AzureConstants.RCA_CAUSE_NONE;
			if (fieldsMap.containsKey(rootCauseFieldFromFieldMapping)
					&& fieldsMap.get(rootCauseFieldFromFieldMapping) != null) {
				// Introduce enum to standarize the values of RCA
				rcaCause = fieldsMap.get(rootCauseFieldFromFieldMapping).toString().toLowerCase();

				if (rcaValuesForCodeIssue.stream().anyMatch(rcaCause::equalsIgnoreCase)) {
					rcaCause = AzureConstants.CODE_ISSUE;
				}
			}
			azureIssue.setRootCauseList(Lists.newArrayList(rcaCause.toLowerCase()));
		} else {
			azureIssue.setRootCauseList(Lists.newArrayList(AzureConstants.RCA_CAUSE_NONE));
		}

	}

	/**
	 * Sets Issue Tech Story Type after identifying s whether a story is tech story
	 * or simple feature story. There can be possible 3 ways to identify a tech
	 * story 1. Specific 'label' is maintained 2. 'Issue type' itself is a 'Tech
	 * Story' 3. A separate 'custom field' is maintained
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

	public void setIssueTechStoryType(FieldMapping fieldMapping, Value issue, JiraIssue azureIssue,
			Map<String, Object> fieldsMap) {
		Fields fields = issue.getFields();
		// For Custom Field
		String jiraTechDebtCustomField = fieldMapping.getJiraTechDebtCustomField();
		Set<String> finalJiraTechDebtCustomFieldSet = new HashSet<>();
		finalJiraTechDebtCustomFieldSet.add(jiraTechDebtCustomField);
		if (Optional.ofNullable(fieldMapping.getJiraTechDebtIdentification()).isPresent()) {
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
	 * Process Feature Data.
	 *
	 * @param azureIssue
	 *            JiraIssue instance
	 * @param issue
	 *            Azure Issue
	 * @param fieldsMap
	 *            the fields map
	 * @param fieldMapping
	 *            fieldMapping provided by the User
	 * @param jiraProcessorConfig
	 *            Jira processor Configuration
	 * @throws JSONException
	 *             Error while parsing JSON
	 */
	public void processJiraIssueData(JiraIssue azureIssue, Value issue, Map<String, Object> fieldsMap,
			FieldMapping fieldMapping, AzureProcessorConfig jiraProcessorConfig) throws JSONException {

		Fields fields = issue.getFields();
		String status = fields.getSystemState();
		String changeDate = fields.getSystemChangedDate();
		String createdDate = fields.getSystemCreatedDate();
		azureIssue.setNumber(AzureProcessorUtil.deodeUTF8String(azureIssue.getIssueId()));
		azureIssue.setName(AzureProcessorUtil.deodeUTF8String(fields.getSystemTitle()));
		azureIssue.setStatus(AzureProcessorUtil.deodeUTF8String(status));
		azureIssue.setState(AzureProcessorUtil.deodeUTF8String(status));

		String jiraStatusMappingCustomField = fieldMapping.getJiraStatusMappingCustomField();
		if (StringUtils.isNotEmpty(jiraStatusMappingCustomField)
				&& fieldsMap.containsKey(jiraStatusMappingCustomField)) {
			String jiraStatusFromCustomField = fieldsMap.get(jiraStatusMappingCustomField).toString();
			if (StringUtils.isNotEmpty(jiraStatusFromCustomField)) {
				azureIssue.setJiraStatus(jiraStatusFromCustomField);
			} else {
				azureIssue.setJiraStatus(AzureProcessorUtil.deodeUTF8String(status));
			}
		} else {
			azureIssue.setJiraStatus(AzureProcessorUtil.deodeUTF8String(status));
		}

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

		azureIssue.setChangeDate(AzureProcessorUtil.getFormattedDate(AzureProcessorUtil.deodeUTF8String(changeDate)));
		azureIssue.setUpdateDate(AzureProcessorUtil.getFormattedDate(AzureProcessorUtil.deodeUTF8String(changeDate)));
		azureIssue.setIsDeleted(AzureConstants.FALSE);

		azureIssue.setOwnersState(Arrays.asList("Active"));

		azureIssue.setOwnersChangeDate(Collections.<String>emptyList());

		azureIssue.setOwnersIsDeleted(Collections.<String>emptyList());

		// Created Date
		azureIssue.setCreatedDate(AzureProcessorUtil.getFormattedDate(AzureProcessorUtil.deodeUTF8String(createdDate)));

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
	public void setEstimate(JiraIssue azureIssue, Map<String, Object> fieldsMap, FieldMapping fieldMapping, // NOSONAR
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
		if (Objects.nonNull(fields.getMicrosoftVSTSSchedulingOriginalEstimate())) {
			Double originalEstimateInHours = fields.getMicrosoftVSTSSchedulingOriginalEstimate();
			Double originalEstimateInMinutes = originalEstimateInHours * 60;
			azureIssue.setOriginalEstimateMinutes(originalEstimateInMinutes.intValue());
		}

		if (Objects.nonNull(fields.getMicrosoftVSTSSchedulingRemainingWork())) {
			Integer remainingWorkInHours = fields.getMicrosoftVSTSSchedulingRemainingWork();
			Integer remainingWorkImMinutes = remainingWorkInHours * 60;
			azureIssue.setRemainingEstimateMinutes(remainingWorkImMinutes);
		}
	}

	private void setEstimateForDefaultFields(JiraIssue jiraIssue, Fields fields, Double estimationFromDefaultField,
			Double storyPointsFromDefaultField) {
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

	/**
	 * This method process owner and user details
	 *
	 * @param azureIssue
	 *            JiraIssue Object to set Owner details
	 * @param fields
	 *            Jira issue User Object
	 */
	public void setJiraAssigneeDetails(JiraIssue azureIssue,
			com.publicissapient.kpidashboard.common.model.azureboards.Fields fields, Set<Assignee> assigneeSetToSave,
			ProjectConfFieldMapping projectConfFieldMapping) {

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
			updateOwnerDetailsToggleWise(azureIssue, projectConfFieldMapping, ownersUsername, ownersId, ownersFullname);
		}

		if (systemAssignedTo == null) {
			azureIssue.setAssigneeId(StringUtils.EMPTY);
			azureIssue.setAssigneeName(StringUtils.EMPTY);
		} else {
			azureIssue.setAssigneeId(systemAssignedTo.getId());
			azureIssue.setAssigneeName(systemAssignedTo.getDisplayName());
			updateAssigneeDetailsToggleWise(azureIssue, assigneeSetToSave, projectConfFieldMapping);
		}
	}

	private void updateAssigneeDetailsToggleWise(JiraIssue jiraIssue, Set<Assignee> assigneeSetToSave,
			ProjectConfFieldMapping projectConfig) {
		if (!projectConfig.getProjectBasicConfig().isSaveAssigneeDetails()) {
			jiraIssue.setAssigneeId(hash(jiraIssue.getAssigneeId()));
			jiraIssue.setAssigneeName(setAssigneeName(jiraIssue.getAssigneeId(),
					projectConfig.getBasicProjectConfigId().toString(), assigneeSetToSave));
		} else {
			assigneeSetToSave.add(new Assignee(jiraIssue.getAssigneeId(), jiraIssue.getAssigneeName()));
		}
	}

	private String setAssigneeName(String assigneeId, String basicProjectConfigId, Set<Assignee> assigneeSetToSave) {
		String assigneeName = AzureConstants.USER + AzureConstants.SPACE + 1;
		if (null == tempAssigneeDetails
				|| !tempAssigneeDetails.getBasicProjectConfigId().equalsIgnoreCase(basicProjectConfigId)) {
			tempAssigneeDetails = assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(basicProjectConfigId,
					ProcessorConstants.AZURE);
		}
		if (tempAssigneeDetails == null) {
			tempAssigneeDetails = new AssigneeDetails();
			tempAssigneeDetails.setBasicProjectConfigId(basicProjectConfigId);
			tempAssigneeDetails.setSource(ProcessorConstants.AZURE);
			assigneeSetToSave.add(new Assignee(assigneeId, assigneeName));
			tempAssigneeDetails.setAssignee(assigneeSetToSave);
			tempAssigneeDetails.setAssigneeSequence(2);
		} else {
			Assignee assignee = tempAssigneeDetails.getAssignee().stream()
					.filter(Assignee -> assigneeId.equals(Assignee.getAssigneeId())).findAny().orElse(null);
			if (null == assignee) {
				assigneeName = AzureConstants.USER + AzureConstants.SPACE + tempAssigneeDetails.getAssigneeSequence();
				tempAssigneeDetails.setAssigneeSequence(tempAssigneeDetails.getAssigneeSequence() + 1);
				// this set is created so that there is no need to fetch
				// assigneeDetails again and same assignee can be checked
				// only with existing assigneeDetails object
				Set<Assignee> newAssignee = new HashSet<>();
				newAssignee.add(new Assignee(assigneeId, assigneeName));
				tempAssigneeDetails.getAssignee().addAll(newAssignee);
				assigneeSetToSave.add(new Assignee(assigneeId, assigneeName));
			} else {
				assigneeName = assignee.getAssigneeName();
			}
		}
		return assigneeName;
	}

	private void updateOwnerDetailsToggleWise(JiraIssue jiraIssue, ProjectConfFieldMapping projectConfig,
			List<String> assigneeName, List<String> assigneeKey, List<String> assigneeDisplayName) {
		if (!projectConfig.getProjectBasicConfig().isSaveAssigneeDetails()) {
			List<String> ownerName = assigneeName.stream().map(AzureIssueClient::hash).collect(Collectors.toList());
			List<String> ownerId = assigneeKey.stream().map(AzureIssueClient::hash).collect(Collectors.toList());
			List<String> ownerFullName = assigneeDisplayName.stream().map(AzureIssueClient::hash)
					.collect(Collectors.toList());
			jiraIssue.setOwnersUsername(ownerName);
			jiraIssue.setOwnersID(ownerId);
			jiraIssue.setOwnersFullName(ownerFullName);
		}
	}

	public boolean isAttemptSuccess(int total, int savedCount) {
		return savedCount > 0 && total == savedCount;
	}

}