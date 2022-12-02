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
import java.util.Set;

import com.publicissapient.kpidashboard.common.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.User;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import lombok.extern.slf4j.Slf4j;

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
		Integer timeSpent = 0;
		if (fields.get(JiraConstants.AGGREGATED_TIME_SPENT) != null
				&& fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue() != null) {
			timeSpent = ((Integer) fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue()) / 60;
		}
		jiraIssue.setTimeSpentInMinutes(timeSpent);

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
	 *
	 * @param jiraIssue
	 *            JiraIssue Object to set Owner details
	 * @param user
	 *            Jira issue User Object
	 */
	public void setJiraAssigneeDetails(JiraIssue jiraIssue, User user) {
		if (user == null) {
			jiraIssue.setOwnersUsername(Collections.<String>emptyList());
			jiraIssue.setOwnersShortName(Collections.<String>emptyList());
			jiraIssue.setOwnersID(Collections.<String>emptyList());
			jiraIssue.setOwnersFullName(Collections.<String>emptyList());
		} else {
			List<String> assigneeKey = new ArrayList<>();
			List<String> assigneeName = new ArrayList<>();
			if ((user.getName() == null) || user.getName().isEmpty()) {
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


	private boolean hasAtLeastOneCommonElement(Set<String> issueLabels, List<String> configuredLabels) {
		if (org.apache.commons.collections4.CollectionUtils.isEmpty(issueLabels)) {
			return false;
		}
		return configuredLabels.stream().anyMatch(issueLabels::contains);
	}

	private String processJson(String fieldMapping, Map<String, IssueField> fields, List<String> jiraTestValue) {
		String automationFlag = NormalizedJira.NO_VALUE.getValue();
		String fetchedValueFromJson = null;
		try {
			if (fields.get(fieldMapping) != null && fields.get(fieldMapping).getValue() != null) {
				String data = fields.get(fieldMapping).getValue().toString();
				Object json = new JSONTokener(data).nextValue();

				if (json instanceof JSONObject) {
					fetchedValueFromJson = ((JSONObject) fields.get(fieldMapping).getValue())
							.getString(JiraConstants.VALUE);
					if (jiraTestValue.contains(fetchedValueFromJson)) {
						automationFlag = NormalizedJira.YES_VALUE.getValue();
					}
				} else if (json instanceof org.codehaus.jettison.json.JSONArray) {
					JSONParser parser = new JSONParser();
					org.json.simple.JSONObject jsonObject;
					JSONArray array = (JSONArray) parser.parse(fields.get(fieldMapping).getValue().toString());
					for (int i = 0; i < array.size(); i++) {
						jsonObject = (org.json.simple.JSONObject) parser.parse(array.get(i).toString());
						fetchedValueFromJson = jsonObject.get(JiraConstants.VALUE).toString();
					}
					if (jiraTestValue.contains(fetchedValueFromJson)) {
						automationFlag = NormalizedJira.YES_VALUE.getValue();
					}
				}
			}

		} catch (JSONException | org.json.simple.parser.ParseException e) {
			log.error("JIRA Processor |Error while parsing test automated field", e);
		}
		return automationFlag;
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
		return DateUtil.dateTimeFormatter(ldt,QUERYDATEFORMAT);
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
