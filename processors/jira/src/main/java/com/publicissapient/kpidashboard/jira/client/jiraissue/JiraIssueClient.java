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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.User;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

@Slf4j
public abstract class JiraIssueClient {// NOPMD //NOSONAR

	protected static final String TESTAUTOMATEDFLAG = "testAutomatedFlag";
	protected static final String TESTCANBEAUTOMATEDFLAG = "testCanBeAutomatedFlag";
	protected static final String AUTOMATEDVALUE = "automatedValue";

	protected static final String QUERYDATEFORMAT = "yyyy-MM-dd HH:mm";

	/**
	 * Explicitly updates queries for the source system, and initiates the
	 * update to MongoDB from those calls.
	 *
	 * @param projectConfig
	 *            Project Configuration Mapping
	 * @param jiraAdapter
	 *            JiraAdapter client
	 * @param isOffline
	 * 				offline processor or not
	 * @return int Count of Jira stories processed
	 */
	public abstract int processesJiraIssues(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter,
			boolean isOffline);

	/**
	 * Purges the issues provided
	 *
	 * @param purgeIssuesList
	 *            List of issues to be purged
	 * @param projectConfig
	 *            Project Configuration Mapping
	 */
	public abstract void purgeJiraIssues(List<Issue> purgeIssuesList, ProjectConfFieldMapping projectConfig);

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
	public void setDevicePlatform(FieldMapping fieldMapping, JiraIssue jiraIssue, Map<String, IssueField> fields) {

		try {
			String devicePlatform = null;
			if (fields.get(fieldMapping.getDevicePlatform()) != null
					&& fields.get(fieldMapping.getDevicePlatform()).getValue() != null) {
				devicePlatform = ((JSONObject) fields.get(fieldMapping.getDevicePlatform()).getValue())
						.getString(JiraConstants.VALUE);
			}
			jiraIssue.setDevicePlatform(devicePlatform);
		} catch (JSONException e) {
			log.error("JIRA Processor | Error while parsing Device Platform data", e);
		}
	}

	/**
	 * Sets Issue Tech Story Type after identifying s whether a story is tech
	 * story or simple feature story. There can be possible 3 ways to identify a
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
	public void setIssueTechStoryType(FieldMapping fieldMapping, Issue issue, JiraIssue jiraIssue,
									  Map<String, IssueField> fields) {

		if (StringUtils.isNotBlank(fieldMapping.getJiraTechDebtIdentification())) {
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
	 * Process Feature Data
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
	public void processJiraIssueData(JiraIssue jiraIssue, Issue issue, Map<String, IssueField> fields,
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
		jiraIssue.setUpdateDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(changeDate)));
		jiraIssue.setIsDeleted(JiraConstants.FALSE);

		jiraIssue.setOwnersState(Arrays.asList("Active"));

		jiraIssue.setOwnersChangeDate(Collections.<String>emptyList());

		jiraIssue.setOwnersIsDeleted(Collections.<String>emptyList());

		// Created Date
		jiraIssue.setCreatedDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(createdDate)));

	}

	/**
	 * setting AggregatedTime Estimates
	 * @param jiraIssue
	 * @param fields
	 */
	private void setAggregateTimeEstimates(JiraIssue jiraIssue, Map<String, IssueField> fields) {
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
	public void setEstimate(JiraIssue jiraIssue, Map<String, IssueField> fields, FieldMapping fieldMapping, // NOSONAR
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
	 * This method process owner and user details
	 *  @param jiraIssue
	 *            JiraIssue Object to set Owner details
	 * @param user
	 *            Jira issue User Object
	 * @param assigneeSetToSave
	 * 			 to save assignee details
	 * @param projectConfig
	 *          project congig fieldmapping
	 */
	public void setJiraAssigneeDetails(JiraIssue jiraIssue, User user, Set<Assignee> assigneeSetToSave, ProjectConfFieldMapping projectConfig) {
		if (user == null) {
			jiraIssue.setOwnersUsername(Collections.<String>emptyList());
			jiraIssue.setOwnersShortName(Collections.<String>emptyList());
			jiraIssue.setOwnersID(Collections.<String>emptyList());
			jiraIssue.setOwnersFullName(Collections.<String>emptyList());
		} else {
			List<String> assigneeKey = new ArrayList<>();
			List<String> assigneeName = new ArrayList<>();
			String assigneeUniqueId = getAssignee(user);
			if ((assigneeUniqueId == null) || assigneeUniqueId.isEmpty()) {
				assigneeKey = new ArrayList<>();
				assigneeName = new ArrayList<>();
			} else {
				assigneeKey.add(JiraProcessorUtil.deodeUTF8String(assigneeUniqueId));
				assigneeName.add(JiraProcessorUtil.deodeUTF8String(assigneeUniqueId));
				jiraIssue.setAssigneeId(assigneeUniqueId);
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
				updateAssigneeDetailsToggleWise(jiraIssue, assigneeSetToSave, projectConfig, assigneeKey, assigneeName, assigneeDisplayName);
			}
		}

	}

	private void updateAssigneeDetailsToggleWise(JiraIssue jiraIssue, Set<Assignee> assigneeSetToSave, ProjectConfFieldMapping projectConfig, List<String> assigneeKey, List<String> assigneeName, List<String> assigneeDisplayName) {
		if (!projectConfig.getProjectBasicConfig().isSaveAssigneeDetails()) {
			List<String> ownerName = assigneeName.stream().map(JiraIssueClient::hash)
					.collect(Collectors.toList());
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

	public static String hash(String input) {
		return String.valueOf(Objects.hash(input));
	}

	public String getAssignee(User user) {
		String userId = "";
		String query = user.getSelf().getQuery();
		if (StringUtils.isNotEmpty(query) && (query.contains("accountId") || query.contains("username"))) {
			userId = query.split("=")[1];
		}
		return userId;
	}

	/**
	 * retrives value of customfield value object
	 *
	 * @param customFieldId
	 *            customFieldId
	 * @param fields
	 *            fields
	 * @return string value of custom field
	 */
	public String getFieldValue(String customFieldId, Map<String, IssueField> fields) {
		Object fieldValue = fields.get(customFieldId).getValue();
		try {
			if (fieldValue instanceof Double) {
				return fieldValue.toString();
			} else if (fieldValue instanceof JSONObject) {
				return ((JSONObject) fieldValue).getString(JiraConstants.VALUE);
			} else if (fieldValue instanceof String) {
				return fieldValue.toString();
			}
		} catch (JSONException e) {
			log.error("JIRA Processor | Error while parsing RCA Custom_Field", e);
		}
		return fieldValue.toString();
	}

	protected Map<String, String> processMap(Map<String, String> labelMap, Map<String, String> customfieldMap) {
		Map<String, String> resultMap = new HashMap<>();
		Set<String> set = new HashSet<>();
		set.add(AUTOMATEDVALUE);
		set.add(TESTAUTOMATEDFLAG);
		set.add(TESTCANBEAUTOMATEDFLAG);
		set.stream().forEach(entry -> {
			if (labelMap != null && labelMap.get(entry) != null) {
				resultMap.put(entry, labelMap.get(entry));
			} else if (customfieldMap != null && customfieldMap.get(entry) != null) {
				resultMap.put(entry, customfieldMap.get(entry));
			}
		});
		return resultMap;
	}

	public String getDeltaDate(String lastSuccessfulRun) {
		LocalDateTime ldt = DateUtil.stringToLocalDateTime(lastSuccessfulRun,QUERYDATEFORMAT);
		ldt = ldt.minusDays(30);
		return DateUtil.dateTimeFormatter(ldt, QUERYDATEFORMAT);
	}

	public void setStartDate(JiraProcessorConfig jiraProcessorConfig) {
		LocalDateTime localDateTime = null;
		if(jiraProcessorConfig.isConsiderStartDate()){
			try{
				localDateTime = DateUtil.stringToLocalDateTime(jiraProcessorConfig.getStartDate(),QUERYDATEFORMAT);
			} catch (DateTimeParseException ex) {
				log.error("exception while parsing start date provided from property file picking last 6 months data.."
						+ ex.getMessage());
				localDateTime = LocalDateTime.now().minusMonths(6);
			}
		}else{
			localDateTime = LocalDateTime.now().minusMonths(6);
		}
		jiraProcessorConfig.setStartDate(DateUtil.dateTimeFormatter(localDateTime, QUERYDATEFORMAT));
	}

}
