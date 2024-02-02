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
package com.publicissapient.kpidashboard.jira.processor;

import static com.publicissapient.kpidashboard.jira.helper.JiraHelper.buildFieldMap;
import static com.publicissapient.kpidashboard.jira.helper.JiraHelper.getAffectedVersions;
import static com.publicissapient.kpidashboard.jira.helper.JiraHelper.getAssignee;
import static com.publicissapient.kpidashboard.jira.helper.JiraHelper.getFieldValue;
import static com.publicissapient.kpidashboard.jira.helper.JiraHelper.getLabelsList;
import static com.publicissapient.kpidashboard.jira.helper.JiraHelper.getListFromJson;
import static com.publicissapient.kpidashboard.jira.helper.JiraHelper.hash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.User;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.helper.AdditionalFilterHelper;
import com.publicissapient.kpidashboard.jira.helper.JiraHelper;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author purgupta2
 *
 */
@Slf4j
@Service
public class KanbanJiraIssueProcessorImpl implements KanbanJiraIssueProcessor {

	AssigneeDetails assigneeDetails;
	@Autowired
	private JiraProcessorRepository jiraProcessorRepository;
	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;
	@Autowired
	private AdditionalFilterHelper additionalFilterHelper;
	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;
	@Autowired
	private KanbanJiraIssueRepository kanbanJiraIssueRepository;

	@Override
	public KanbanJiraIssue convertToKanbanJiraIssue(Issue issue, ProjectConfFieldMapping projectConfig, String boardId)
			throws JSONException {

		KanbanJiraIssue jiraIssue = null;
		log.info("Converting issue to KanbanJiraIssue for the project : {}", projectConfig.getProjectName());
		if (null == issue) {
			log.error("JIRA Processor |. No list of current paged Kanban JIRA's issues found");
			return jiraIssue;
		}

		Map<String, String> issueEpics = new HashMap<>();
		ObjectId jiraIssueId = jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();

		FieldMapping fieldMapping = projectConfig.getFieldMapping();
		if (null == fieldMapping) {
			return jiraIssue;
		}
		Set<String> issueTypeNames = Arrays.stream(fieldMapping.getJiraIssueTypeNames()).map(String::toLowerCase)
				.collect(Collectors.toSet());
		Map<String, IssueField> fields = buildFieldMap(issue.getFields());

		IssueType issueType = issue.getIssueType();
		IssueField epic = fields.get(fieldMapping.getEpicName());

		if (issueTypeNames
				.contains(JiraProcessorUtil.deodeUTF8String(issueType.getName()).toLowerCase(Locale.getDefault()))) {
			String issueId = JiraProcessorUtil.deodeUTF8String(issue.getId());
			jiraIssue = getKanbanJiraIssue(projectConfig, issueId);

			// Add url to Issue
			setURL(issue.getKey(), jiraIssue, projectConfig);

			// Add RCA to Issue
			setRCA(fieldMapping, issue, jiraIssue, fields);

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
			jiraIssue.setLabels(getLabelsList(issue));
			processJiraIssueData(jiraIssue, issue, fields, fieldMapping);

			// Set project specific details
			setProjectSpecificDetails(projectConfig, jiraIssue, issue);

			// Set additional filters
			setAdditionalFilters(jiraIssue, issue, projectConfig);

			setStoryLinkWithDefect(issue, jiraIssue);

			// Add Tech Debt Story identificatin to jira issue
			setIssueTechStoryType(fieldMapping, issue, jiraIssue, fields);

			// Affected Version
			jiraIssue.setAffectedVersions(getAffectedVersions(issue));

			setJiraIssuuefields(issue, jiraIssue, fieldMapping, fields, epic, issueEpics);

			User assignee = issue.getAssignee();
			setJiraAssigneeDetails(jiraIssue, assignee, projectConfig);

			setDueDates(jiraIssue, issue, fields, fieldMapping);
			jiraIssue.setBoardId(boardId);
		}

		return jiraIssue;
	}

	private KanbanJiraIssue getKanbanJiraIssue(ProjectConfFieldMapping projectConfig, String issueId) {
		String basicProjectConfigId = projectConfig.getBasicProjectConfigId().toString();
		KanbanJiraIssue jiraIssue = kanbanJiraIssueRepository
				.findByIssueIdAndBasicProjectConfigId(StringEscapeUtils.escapeHtml4(issueId), basicProjectConfigId);

		return jiraIssue != null ? jiraIssue : new KanbanJiraIssue();
	}

	private void setJiraAssigneeDetails(KanbanJiraIssue jiraIssue, User user, ProjectConfFieldMapping projectConfig) {
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
				updateAssigneeDetailsToggleWise(jiraIssue, projectConfig, assigneeKey, assigneeName,
						assigneeDisplayName);
			}
		}

	}

	private void updateAssigneeDetailsToggleWise(KanbanJiraIssue jiraIssue, ProjectConfFieldMapping projectConfig,
			List<String> assigneeKey, List<String> assigneeName, List<String> assigneeDisplayName) {
		if (!projectConfig.getProjectBasicConfig().isSaveAssigneeDetails()) {

			List<String> ownerName = assigneeName.stream().map(JiraHelper::hash).collect(Collectors.toList());
			List<String> ownerId = assigneeKey.stream().map(JiraHelper::hash).collect(Collectors.toList());
			List<String> ownerFullName = assigneeDisplayName.stream().map(JiraHelper::hash)
					.collect(Collectors.toList());
			jiraIssue.setOwnersShortName(ownerName);
			jiraIssue.setOwnersUsername(ownerName);
			jiraIssue.setOwnersID(ownerId);
			jiraIssue.setOwnersFullName(ownerFullName);
			jiraIssue.setAssigneeId(hash(jiraIssue.getAssigneeId()));
			jiraIssue.setAssigneeName(
					setAssigneeName(jiraIssue.getAssigneeId(), projectConfig.getBasicProjectConfigId().toString()));
		}
	}

	private String setAssigneeName(String assigneeId, String basicProjectConfigId) {
		String assigneeName = JiraConstants.USER + JiraConstants.SPACE + 1;
		if (null == assigneeDetails
				|| !assigneeDetails.getBasicProjectConfigId().equalsIgnoreCase(basicProjectConfigId)) {
			assigneeDetails = assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(basicProjectConfigId,
					ProcessorConstants.JIRA);
		}
		Set<Assignee> assigneeSetToSave = new LinkedHashSet<>();
		if (assigneeDetails == null) {
			assigneeDetails = new AssigneeDetails();
			assigneeDetails.setBasicProjectConfigId(basicProjectConfigId);
			assigneeDetails.setSource(ProcessorConstants.JIRA);
			assigneeSetToSave.add(new Assignee(assigneeId, assigneeName));
			assigneeDetails.setAssignee(assigneeSetToSave);
			assigneeDetails.setAssigneeSequence(2);
		} else {
			Assignee assignee = assigneeDetails.getAssignee().stream()
					.filter(Assignee -> assigneeId.equals(Assignee.getAssigneeId())).findAny().orElse(null);
			if (null == assignee) {
				assigneeName = JiraConstants.USER + JiraConstants.SPACE + assigneeDetails.getAssigneeSequence();
				assigneeDetails.setAssigneeSequence(assigneeDetails.getAssigneeSequence() + 1);
				// this set is created so that there is no need to fetch
				// assigneeDetails again and same assignee can be checked
				// only with existing assigneeDetails object
				Set<Assignee> newAssignee = new HashSet<>();
				newAssignee.add(new Assignee(assigneeId, assigneeName));
				assigneeDetails.getAssignee().addAll(newAssignee);

			} else {
				assigneeName = assignee.getAssigneeName();
			}

		}
		return assigneeName;
	}

	private void setEpicLinked(FieldMapping fieldMapping, KanbanJiraIssue jiraIssue, Map<String, IssueField> fields) {
		if (StringUtils.isNotEmpty(fieldMapping.getEpicLink()) && fields.get(fieldMapping.getEpicLink()) != null
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
		if (StringUtils.isNotEmpty(fieldMapping.getJiraDevDueDateCustomField())
				&& ObjectUtils.isNotEmpty(fields.get(fieldMapping.getJiraDevDueDateCustomField()))) {
			IssueField issueField = fields.get(fieldMapping.getJiraDevDueDateCustomField());
			if (ObjectUtils.isNotEmpty(issueField.getValue())) {
				jiraIssue.setDevDueDate(JiraProcessorUtil.deodeUTF8String(issueField.getValue()).split("T")[0]
						.concat(DateUtil.ZERO_TIME_ZONE_FORMAT));
			}
		}
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

	private void processJiraIssueData(KanbanJiraIssue jiraIssue, Issue issue, Map<String, IssueField> fields,
			FieldMapping fieldMapping) throws JSONException {

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
		setEstimate(jiraIssue, fields, fieldMapping);
		Integer timeSpent = 0;
		if (issue.getTimeTracking() != null && issue.getTimeTracking().getTimeSpentMinutes() != null) {
			timeSpent = issue.getTimeTracking().getTimeSpentMinutes();
		} else if (fields.get(JiraConstants.AGGREGATED_TIME_SPENT) != null
				&& fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue() != null) {
			timeSpent = ((Integer) fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue()) / 60;
		}
		jiraIssue.setTimeSpentInMinutes(timeSpent);

		jiraIssue.setChangeDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(changeDate)));
		jiraIssue.setIsDeleted(JiraConstants.FALSE);

		jiraIssue.setOwnersState(Arrays.asList("Active"));

		jiraIssue.setOwnersChangeDate(Collections.<String>emptyList());

		jiraIssue.setOwnersIsDeleted(Collections.<String>emptyList());

		// Created Date
		jiraIssue.setCreatedDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(createdDate)));

	}

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
					&& CollectionUtils.containsAny(fieldMapping.getJiraTechDebtValue(),
							getListFromJson(fields.get(fieldMapping.getJiraTechDebtCustomField().trim())))) {
				jiraIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
			}
		}

	}

	private void setEstimate(KanbanJiraIssue jiraIssue, Map<String, IssueField> fields, FieldMapping fieldMapping// NOSONAR
	) {
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

	private void setURL(String ticketNumber, KanbanJiraIssue kanbanJiraIssue, ProjectConfFieldMapping projectConfig) {
		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		if (connectionOptional.isPresent()) {
			Connection connection = connectionOptional.get();
			Boolean cloudEnv = connection.isCloudEnv();
			String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
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
	}

}
