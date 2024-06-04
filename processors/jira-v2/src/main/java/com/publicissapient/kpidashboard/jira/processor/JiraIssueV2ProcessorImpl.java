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
import static com.publicissapient.kpidashboard.jira.helper.JiraHelper.getFieldValue;
import static com.publicissapient.kpidashboard.jira.helper.JiraHelper.getLabelsList;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.CommentDetails;
import com.publicissapient.kpidashboard.common.model.jira.Field;
import com.publicissapient.kpidashboard.common.model.jira.IssueLinkDetail;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueV2;
import com.publicissapient.kpidashboard.common.model.jira.ReleaseVersion;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetailsV2;
import com.publicissapient.kpidashboard.common.model.jira.SubTaskDetails;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueV2Repository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.helper.AdditionalFilterHelper;
import com.publicissapient.kpidashboard.jira.helper.JiraHelper;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.JiraIssueClientUtil;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 *
 */
@Slf4j
@Service
public class JiraIssueV2ProcessorImpl implements JiraIssueV2Processor {
	private static final String TEST_PHASE= "TestPhase";
	private static final String UAT_PHASE= "UAT";

	AssigneeDetails assigneeDetails;
	@Autowired
	private JiraIssueV2Repository jiraIssueV2Repository;
	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;
	@Autowired
	private AdditionalFilterHelper additionalFilterHelper;
	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;
	@Autowired
	private JiraProcessorRepository jiraProcessorRepository;

	private static void storyWithSubTaskDefect(Issue issue, Map<String, IssueField> fields,
			Set<String> defectStorySet) {
		String parentKey;
		if (issue.getIssueType().isSubtask() && MapUtils.isNotEmpty(fields)) {

			try {
				parentKey = ((JSONObject) fields.get(JiraConstants.PARENT).getValue()).get(JiraConstants.KEY)
						.toString();
				defectStorySet.add(parentKey);
			} catch (JSONException e) {
				log.error(
						"JIRA Processor | Error while parsing parent value as JSONObject or converting JSONObject to string",
						e);
			}

		}
	}

	private static void setTestPhaseDefectsList(Issue issue, FieldMapping fieldMapping, JiraIssueV2 jiraIssue) {
		List<String> commonLabel = issue.getLabels().stream()
				.filter(x -> fieldMapping.getTestingPhaseDefectValue().contains(x)).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(commonLabel)) {
			jiraIssue.setEscapedDefectGroup(commonLabel);
		}
	}

	private static void setTestPhaseDefectsListForComponent(Issue issue, FieldMapping fieldMapping,
			JiraIssueV2 jiraIssue) {
		Iterable<BasicComponent> components = issue.getComponents();
		List<BasicComponent> componentList = new ArrayList<>();
		components.forEach(componentList::add);
		if (CollectionUtils.isNotEmpty(componentList)) {
			List<String> componentNameList = componentList.stream().map(BasicComponent::getName)
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(componentNameList) && componentNameList.stream()
					.anyMatch(fieldMapping.getTestingPhaseDefectComponentValue()::equalsIgnoreCase)) {
				List<String> commonLabel = componentNameList.stream()
						.filter(x -> fieldMapping.getTestingPhaseDefectComponentValue().contains(x))
						.collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(commonLabel)) {
					jiraIssue.setEscapedDefectGroup(commonLabel);
				}
			}
		}
	}

	@Override
	public JiraIssueV2 convertToJiraIssueV2(Issue issue, ProjectConfFieldMapping projectConfig, String boardId)
			throws JSONException {

		JiraIssueV2 jiraIssue = null;
		log.info("Converting issue to JiraIssueV2 for the project : {}", projectConfig.getProjectName());
		if (null == issue) {
			log.error("JIRA Processor | No list of current paged JIRA's issues found");
			return jiraIssue;
		}

		FieldMapping fieldMapping = projectConfig.getFieldMapping();
		if (null == fieldMapping) {
			return jiraIssue;
		}

		ObjectId jiraProcessorId = jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA_V2).getId();
		Set<String> issueTypeNames = Arrays.stream(fieldMapping.getJiraIssueTypeNames()).map(String::toLowerCase)
				.collect(Collectors.toSet());
		IssueType issueType = issue.getIssueType();

		// save only issues which are in configuration --> removing this check for Scrum
		// AI Data
		Map<String, String> issueEpics = new HashMap<>();

		String issueId = JiraProcessorUtil.deodeUTF8String(issue.getId());

		jiraIssue = getJiraIssue(projectConfig, issueId);
		jiraIssue.setProcessorId(jiraProcessorId);

		Map<String, IssueField> fields = buildFieldMap(issue.getFields());
		// saving fields
		Iterable<IssueField> issueFields = issue.getFields();
		List<Field> fieldsList = StreamSupport.stream(issueFields.spliterator(), false)
				.map(issueField -> new Field(issueField.getId(), issueField.getName(), issueField.getType(),
						issueField.getValue()))
				.toList();
		jiraIssue.setFields(fieldsList);
		IssueField epic = fields.get(fieldMapping.getEpicName());
		jiraIssue.setIssueId(JiraProcessorUtil.deodeUTF8String(issue.getId()));
		jiraIssue.setTypeId(JiraProcessorUtil.deodeUTF8String(issueType.getId()));
		jiraIssue.setTypeName(JiraProcessorUtil.deodeUTF8String(issueType.getName()));
		jiraIssue.setIssueType(JiraProcessorUtil.deodeUTF8String(issueType.getName()));
		jiraIssue.setOriginalType(JiraProcessorUtil.deodeUTF8String(issueType.getName()));

		setEpicLinked(fieldMapping, jiraIssue, fields);
		setSubTaskLinkage(jiraIssue, fieldMapping, issue, fields);
		setSubTaskDetails(jiraIssue, issue);
		processJiraIssueData(jiraIssue, issue, fields, fieldMapping);
		setURL(issue.getKey(), jiraIssue, projectConfig);
		setRCA(fieldMapping, issue, jiraIssue, fields);
		setThirdPartyDefectIdentificationField(fieldMapping, issue, jiraIssue, fields);
		setDefectIssueType(jiraIssue, issueType, fieldMapping);
		jiraIssue.setLabels(getLabelsList(issue));
		setProjectSpecificDetails(projectConfig, jiraIssue, issue);
		setAdditionalFilters(jiraIssue, issue, projectConfig);
		setStoryLinkWithDefect(issue, jiraIssue, fields);
		setProductionDefectIdentificationField(fieldMapping, issue, jiraIssue, fields);
		setTestingPhaseDefectIdentificationField(issue, fieldMapping, jiraIssue, fields);
		// ADD Production Incident field to feature
		setProdIncidentIdentificationField(fieldMapping, issue, jiraIssue, fields);
		setIssueTechStoryType(fieldMapping, issue, jiraIssue, fields);
		jiraIssue.setAffectedVersions(getAffectedVersions(issue));
		setIssueEpics(issueEpics, epic, jiraIssue);
		setJiraIssueValues(jiraIssue, issue, fieldMapping, fields);
		IssueField sprint = fields.get(fieldMapping.getSprintName());
		processSprintData(jiraIssue, sprint, projectConfig);
		User assignee = issue.getAssignee();
		setJiraAssigneeDetails(jiraIssue, assignee, projectConfig);
		setEstimates(jiraIssue, issue);
		setDueDates(jiraIssue, issue, fields, fieldMapping);
		jiraIssue.setBoardId(boardId);
		// reporter details saving
		User reporter = issue.getReporter();
		setJiraReporterDetails(jiraIssue, reporter);
		setIssueLinkDetails(jiraIssue, issue);
		return jiraIssue;
	}

	private void setIssueLinkDetails(JiraIssueV2 jiraIssue, Issue issue) {
		final Iterable<IssueLink> issueLinks = issue.getIssueLinks();
		if (issueLinks != null && issueLinks.iterator().hasNext()) {
			List<IssueLinkDetail> issueLinkDetailsList = StreamSupport.stream(issueLinks.spliterator(), false)
					.map(issueLink -> new IssueLinkDetail(issueLink.getIssueLinkType().getName(), issueLink.getIssueLinkType().getDescription(),
							issueLink.getIssueLinkType().getDirection().toString()))
					.toList();
			jiraIssue.setIssueLinks(issueLinkDetailsList);
		}
	}

	private void setSubTaskDetails(JiraIssueV2 jiraIssue, Issue issue) {
		Iterable<Subtask> subtasksIterable = issue.getSubtasks();
		if (subtasksIterable != null && subtasksIterable.iterator().hasNext()) {
			List<SubTaskDetails> subTaskDetailsList = StreamSupport.stream(subtasksIterable.spliterator(), false)
					.map(subtask -> new SubTaskDetails(subtask.getIssueKey(), subtask.getSummary(),
							subtask.getIssueType().getName(), subtask.getStatus().getName()))
					.toList();
			jiraIssue.setSubTasks(subTaskDetailsList);
		} else {
			jiraIssue.setSubTasks(Collections.<SubTaskDetails>emptyList());
		}

	}

	private void setJiraReporterDetails(JiraIssueV2 jiraIssue, User reporter) {
		if (reporter == null) {
			jiraIssue.setReporterName(StringUtils.EMPTY);
		}
		else{
			jiraIssue.setReporterName(reporter.getDisplayName());
		}
	}

	private JiraIssueV2 getJiraIssue(ProjectConfFieldMapping projectConfig, String issueId) {
		String basicProjectConfigId = projectConfig.getBasicProjectConfigId().toString();
		JiraIssueV2 jiraIssue = jiraIssueV2Repository
				.findByIssueIdAndBasicProjectConfigId(StringEscapeUtils.escapeHtml4(issueId), basicProjectConfigId);

		return jiraIssue != null ? jiraIssue : new JiraIssueV2();
	}

	private void setEpicLinked(FieldMapping fieldMapping, JiraIssueV2 jiraIssue, Map<String, IssueField> fields) {
		if (StringUtils.isNotEmpty(fieldMapping.getEpicLink()) && fields.get(fieldMapping.getEpicLink()) != null
				&& fields.get(fieldMapping.getEpicLink()).getValue() != null) {
			jiraIssue.setEpicLinked(fields.get((fieldMapping.getEpicLink()).trim()).getValue().toString());
		}
	}

	private void setSubTaskLinkage(JiraIssueV2 jiraIssue, FieldMapping fieldMapping, Issue issue,
								   Map<String, IssueField> fields) {
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraSubTaskIdentification())
				&& fieldMapping.getJiraSubTaskIdentification().contains(jiraIssue.getTypeName())) {
			Set<String> mainStorySet = new HashSet<>();
			storyWithSubTaskDefect(issue, fields, mainStorySet);
			jiraIssue.setParentStoryId(mainStorySet);
		}
	}

	private void setJiraAssigneeDetails(JiraIssueV2 jiraIssue, User user, ProjectConfFieldMapping projectConfig) {
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
				jiraIssue.setAssigneeSelf(JiraProcessorUtil.deodeUTF8String(user.getSelf()));
			}
			jiraIssue.setOwnersFullName(assigneeDisplayName);
			if (StringUtils.isNotEmpty(jiraIssue.getAssigneeId())
					&& StringUtils.isNotEmpty(jiraIssue.getAssigneeName())) {
				updateAssigneeDetailsToggleWise(jiraIssue, projectConfig, assigneeKey, assigneeName,
						assigneeDisplayName);
			}
		}

	}

	void updateAssigneeDetailsToggleWise(JiraIssueV2 jiraIssue, ProjectConfFieldMapping projectConfig,
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
			jiraIssue.setAssigneeId(JiraHelper.hash(jiraIssue.getAssigneeId()));
//			jiraIssue.setAssigneeName(
//					setAssigneeName(jiraIssue.getAssigneeId(), projectConfig.getBasicProjectConfigId().toString()));
		}
	}

	private String setAssigneeName(String assigneeId, String basicProjectConfigId) {
		String assigneeName = JiraConstants.USER + JiraConstants.SPACE + 1;
		if (null == assigneeDetails
				|| !assigneeDetails.getBasicProjectConfigId().equalsIgnoreCase(basicProjectConfigId)) {
			assigneeDetails = assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(basicProjectConfigId,
					ProcessorConstants.JIRA_V2);
		}
		Set<Assignee> assigneeSetToSave = new LinkedHashSet<>();
		if (assigneeDetails == null) {
			assigneeDetails = new AssigneeDetails();
			assigneeDetails.setBasicProjectConfigId(basicProjectConfigId);
			assigneeDetails.setSource(ProcessorConstants.JIRA_V2);
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

	public String getAssignee(User user) {
		String userId = "";
		String query = user.getSelf().getQuery();
		if (StringUtils.isNotEmpty(query) && (query.contains("accountId") || query.contains("username"))) {
			userId = query.split("=")[1];
		}
		return userId;
	}

	private void setIssueTechStoryType(FieldMapping fieldMapping, Issue issue, JiraIssueV2 jiraIssue,
			Map<String, IssueField> fields) {

		if (StringUtils.isNotBlank(fieldMapping.getJiraTechDebtIdentification())) {
			if (fieldMapping.getJiraTechDebtIdentification().trim().equalsIgnoreCase(JiraConstants.LABELS)) {
				if (CollectionUtils.containsAny(issue.getLabels(),
						fieldMapping.getJiraTechDebtValue())) {
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
							JiraIssueClientUtil
									.getListFromJson(fields.get(fieldMapping.getJiraTechDebtCustomField().trim())))) {
				jiraIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
			}
		}

	}

	private void processJiraIssueData(JiraIssueV2 jiraIssue, Issue issue, Map<String, IssueField> fields,
									  FieldMapping fieldMapping) throws JSONException {

		String status = issue.getStatus().getName();
		String changeDate = issue.getUpdateDate().toString();
		String createdDate = issue.getCreationDate().toString();
//		jiraIssue.setNumber(JiraProcessorUtil.deodeUTF8String(issue.getKey()));
		jiraIssue.setKey(JiraProcessorUtil.deodeUTF8String(issue.getKey()));
//		jiraIssue.setName(JiraProcessorUtil.deodeUTF8String(issue.getSummary()));
		jiraIssue.setSummary(JiraProcessorUtil.deodeUTF8String(issue.getSummary()));
		jiraIssue.setDescription(JiraProcessorUtil.deodeUTF8String(issue.getDescription()));
		log.debug("Issue : {}", jiraIssue.getKey());
		jiraIssue.setStatus(JiraProcessorUtil.deodeUTF8String(status));
		jiraIssue.setState(JiraProcessorUtil.deodeUTF8String(status));

		Iterable<Comment> issueComments = issue.getComments();
		if (issueComments != null && issueComments.iterator().hasNext()) {
			List<CommentDetails> commentDetailsList = StreamSupport.stream(issueComments.spliterator(), false)
					.map(comment -> {
						String authorName = (comment.getAuthor() != null) ? comment.getAuthor().getDisplayName() : "";
						return new CommentDetails(comment.getBody(), authorName, comment.getCreationDate().toString(),
								comment.getUpdateDate().toString());
					}).toList();
			jiraIssue.setComments(commentDetailsList);
		} else {
			jiraIssue.setComments(Collections.<CommentDetails>emptyList());
		}
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
			jiraIssue.setResolutionName(JiraProcessorUtil.deodeUTF8String(issue.getResolution().getName()));
		}
		setEstimate(jiraIssue, fields, fieldMapping);
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

	private void setAggregateTimeEstimates(JiraIssueV2 jiraIssue, Map<String, IssueField> fields) {
		Integer timeSpent = 0;
		if (fields.get(JiraConstants.AGGREGATED_TIME_SPENT) != null
				&& fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue() != null) {
			timeSpent = ((Integer) fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue()) / 60;
		}
		jiraIssue.setTimeSpentInMinutes(timeSpent);

		if (fields.get(JiraConstants.AGGREGATED_TIME_ORIGINAL) != null
				&& fields.get(JiraConstants.AGGREGATED_TIME_ORIGINAL).getValue() != null) {
//			jiraIssue.setAggregateTimeOriginalEstimateMinutes(
//					((Integer) fields.get(JiraConstants.AGGREGATED_TIME_ORIGINAL).getValue()) / 60);
			jiraIssue.setTimeOriginalEstimate(
					((Integer) fields.get(JiraConstants.AGGREGATED_TIME_ORIGINAL).getValue()) / 60);
		}
		if (fields.get(JiraConstants.AGGREGATED_TIME_REMAIN) != null
				&& fields.get(JiraConstants.AGGREGATED_TIME_REMAIN).getValue() != null) {
//			jiraIssue.setAggregateTimeRemainingEstimateMinutes(
//					((Integer) fields.get(JiraConstants.AGGREGATED_TIME_REMAIN).getValue()) / 60);
			jiraIssue.setAggregateTimeEstimate(
					((Integer) fields.get(JiraConstants.AGGREGATED_TIME_REMAIN).getValue()) / 60);

		}
		if (fields.get(JiraConstants.TIME_ESTIMATE) != null
				&& fields.get(JiraConstants.TIME_ESTIMATE).getValue() != null) {
			jiraIssue.setTimeEstimate(
					((Integer) fields.get(JiraConstants.TIME_ESTIMATE).getValue()) / 60);
		}
	}

	private void setEstimate(JiraIssueV2 jiraIssue, Map<String, IssueField> fields, FieldMapping fieldMapping) {
		Double value = 0d;
		String valueString = "0";
		String estimationCriteria = fieldMapping.getEstimationCriteria();

		if (StringUtils.isNotBlank(estimationCriteria)) {
			String estimationField = fieldMapping.getJiraStoryPointsCustomField();
			if (shouldEstimationBeCalculated(fields, estimationField)) {
				value = calculateEstimation(fields.get(estimationField), estimationCriteria);
				valueString = String.valueOf(value);
			}
		} else {
			IssueField estimationField = fields.get(fieldMapping.getJiraStoryPointsCustomField());
			if (shouldEstimationBeCalculated(estimationField)) {
				value = calculateEstimation(estimationField);
				valueString = String.valueOf(value);
			}
		}

		jiraIssue.setEstimate(valueString);
		jiraIssue.setStoryPoints(String.valueOf(value));
	}

	private boolean shouldEstimationBeCalculated(Map<String, IssueField> fields, String estimationField) {
		return StringUtils.isNotBlank(estimationField) && fields.get(estimationField) != null
				&& fields.get(estimationField).getValue() != null
				&& !JiraProcessorUtil.deodeUTF8String(fields.get(estimationField).getValue()).isEmpty();
	}

	private boolean shouldEstimationBeCalculated(IssueField estimationField) {
		return estimationField != null && estimationField.getValue() != null
				&& !JiraProcessorUtil.deodeUTF8String(estimationField.getValue()).isEmpty();
	}

	private Double calculateEstimation(IssueField estimationField, String estimationCriteria) {
		if (JiraConstants.ACTUAL_ESTIMATION.equalsIgnoreCase(estimationCriteria)) {
			return (estimationField.getValue() instanceof Integer) ? ((Integer) estimationField.getValue()) / 3600D
					: ((Double) estimationField.getValue());
		} else if (JiraConstants.BUFFERED_ESTIMATION.equalsIgnoreCase(estimationCriteria)) {
			return (estimationField.getValue() instanceof Integer) ? ((Double) estimationField.getValue()) / 3600D
					: ((Double) estimationField.getValue());
		} else if (JiraConstants.STORY_POINT.equalsIgnoreCase(estimationCriteria)) {
			return Double.parseDouble(JiraProcessorUtil.deodeUTF8String(estimationField.getValue()));
		}
		return 0.0; // Default value if none of the criteria match
	}

	private Double calculateEstimation(IssueField estimationField) {
		return Double.parseDouble(JiraProcessorUtil.deodeUTF8String(estimationField.getValue()));
	}

	private void setAdditionalFilters(JiraIssueV2 jiraIssue, Issue issue, ProjectConfFieldMapping projectConfig) {
		List<AdditionalFilter> additionalFilter = additionalFilterHelper.getAdditionalFilter(issue, projectConfig);
		jiraIssue.setAdditionalFilters(additionalFilter);
	}

	private void setProjectSpecificDetails(ProjectConfFieldMapping projectConfig, JiraIssueV2 jiraIssue, Issue issue) {
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

	private void setIssueEpics(Map<String, String> issueEpics, IssueField epic, JiraIssueV2 jiraIssue) {
		if (epic != null && epic.getValue() != null && !JiraProcessorUtil.deodeUTF8String(epic.getValue()).isEmpty()) {
			issueEpics.put(jiraIssue.getIssueId(), JiraProcessorUtil.deodeUTF8String(epic.getValue()));
		}
	}

	private void setDefectIssueType(JiraIssueV2 jiraIssue, IssueType issueType, FieldMapping fieldMapping) {
		// set defecttype to BUG
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype())
				&& fieldMapping.getJiradefecttype().stream().anyMatch(issueType.getName()::equalsIgnoreCase)) {
			jiraIssue.setTypeName(NormalizedJira.DEFECT_TYPE.getValue());
		}
	}

	private void setJiraIssueValues(JiraIssueV2 jiraIssue, Issue issue, FieldMapping fieldMapping,
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
			jiraIssue.setFixVersions(releaseVersions);
		}
	}

	private void setRCA(FieldMapping fieldMapping, Issue issue, JiraIssueV2 jiraIssue, Map<String, IssueField> fields) {

		List<String> rcaList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype())
				&& fieldMapping.getJiradefecttype().stream().anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)
				&& null != fieldMapping.getRootCauseIdentifier()) {
			if (StringUtils.isNotEmpty(fieldMapping.getRootCauseIdentifier())
					&& fieldMapping.getRootCauseIdentifier().trim().equalsIgnoreCase(JiraConstants.LABELS)) {
				List<String> commonLabel = issue.getLabels().stream()
						.filter(x -> fieldMapping.getRootCauseValues().contains(x)).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(commonLabel)) {
					rcaList.addAll(commonLabel);
				}
			} else if (StringUtils.isNotEmpty(fieldMapping.getRootCauseIdentifier())
					&& fieldMapping.getRootCauseIdentifier().trim().equalsIgnoreCase(JiraConstants.CUSTOM_FIELD)
					&& StringUtils.isNotEmpty(fieldMapping.getRootCause())
					&& fields.get(fieldMapping.getRootCause().trim()) != null
					&& fields.get(fieldMapping.getRootCause().trim()).getValue() != null) {
				rcaList.addAll(getRootCauses(fieldMapping, fields));
			}
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
				.getValue() instanceof JSONObject) {
			String rcaCause = null;
			try {
				rcaCause = ((JSONObject) fields.get(fieldMapping.getRootCause()).getValue())
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

	private void setProductionDefectIdentificationField(FieldMapping featureConfig, Issue issue, JiraIssueV2 feature,
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
								fields.get(featureConfig.getProductionDefectCustomField().trim()).getValue(), null, "")) {
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

	private void setStoryLinkWithDefect(Issue issue, JiraIssueV2 jiraIssue, Map<String, IssueField> fields) {
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())
				|| NormalizedJira.TEST_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
			Set<String> defectStorySet = new HashSet<>();
			excludeLinks(issue, defectStorySet);
			storyWithSubTaskDefect(issue, fields, defectStorySet);
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

	private void setThirdPartyDefectIdentificationField(FieldMapping fieldMapping, Issue issue, JiraIssueV2 jiraIssue,
			Map<String, IssueField> fields) {
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype()) && fieldMapping.getJiradefecttype().stream()
				.anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)) {
			if (StringUtils.isNotBlank(fieldMapping.getJiraBugRaisedByIdentification())
					&& fieldMapping.getJiraBugRaisedByIdentification().trim()
							.equalsIgnoreCase(JiraConstants.CUSTOM_FIELD)
					&& fields.get(fieldMapping.getJiraBugRaisedByCustomField().trim()) != null
					&& fields.get(fieldMapping.getJiraBugRaisedByCustomField().trim()).getValue() != null
					&& isBugRaisedByValueMatchesRaisedByCustomField(fieldMapping.getJiraBugRaisedByValue(),
							fields.get(fieldMapping.getJiraBugRaisedByCustomField().trim()).getValue(), jiraIssue, UAT_PHASE )) {
				jiraIssue.setDefectRaisedBy(NormalizedJira.THIRD_PARTY_DEFECT_VALUE.getValue());
			} else {
				jiraIssue.setDefectRaisedBy("");
			}

		}
	}

	private boolean isBugRaisedByValueMatchesRaisedByCustomField(List<String> bugRaisedValue, Object issueFieldValue,
																 JiraIssueV2 jiraIssue, String feature) {
		List<String> lowerCaseBugRaisedValue = bugRaisedValue.stream().map(String::toLowerCase)
				.collect(Collectors.toList());
		JSONParser parser = new JSONParser();
		JSONArray array = new JSONArray();
		boolean isRaisedByThirdParty = false;
		org.json.simple.JSONObject jsonObject = new org.json.simple.JSONObject();
		try {
			if (issueFieldValue instanceof org.codehaus.jettison.json.JSONArray) {
				array = (JSONArray) parser.parse(issueFieldValue.toString());
				ArrayList<String> testPhasesList = new ArrayList<>();
				for (int i = 0; i < array.size(); i++) {

					jsonObject = (org.json.simple.JSONObject) parser.parse(array.get(i).toString());
					if (lowerCaseBugRaisedValue
							.contains(jsonObject.get(JiraConstants.VALUE).toString().toLowerCase())) {
						testPhasesList.add(jsonObject.get(JiraConstants.VALUE).toString());
						isRaisedByThirdParty = true;
						break;
					}

				}
				if (Objects.nonNull(jiraIssue)) {
					setSpecificField(jiraIssue, feature, testPhasesList);

				}
			} else if (issueFieldValue instanceof JSONObject
					&& lowerCaseBugRaisedValue.contains(((JSONObject) issueFieldValue)
							.get(JiraConstants.VALUE).toString().toLowerCase())) {
				isRaisedByThirdParty = true;
				String testPhase = ((JSONObject) issueFieldValue).get(JiraConstants.VALUE)
						.toString();
				if (lowerCaseBugRaisedValue.contains(testPhase.toLowerCase()) && Objects.nonNull(jiraIssue)) {
					setSpecificField(jiraIssue,feature, Collections.singletonList(testPhase));
				}
			}

		} catch (org.json.simple.parser.ParseException | JSONException e) {
			log.error("JIRA Processor | Error while parsing third party field {}", e);
		}
		return isRaisedByThirdParty;
	}

	private void setSpecificField(JiraIssueV2 jiraIssue, String feature, List<String> testPhasesList) {
		if (feature.equalsIgnoreCase(TEST_PHASE)) {
			jiraIssue.setEscapedDefectGroup(testPhasesList.stream().map(String::toLowerCase).collect(Collectors.toList()));
		} else if (feature.equalsIgnoreCase(UAT_PHASE)) {
			jiraIssue.setUatDefectGroup(testPhasesList);
		}
	}

	private void processSprintData(JiraIssueV2 jiraIssue, IssueField sprintField, ProjectConfFieldMapping projectConfig) {
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
				List<SprintDetailsV2> sprints = JiraProcessorUtil.processSprintV2Detail(sValue);
				// Now sort so we can use the most recent one
				// yyyy-MM-dd'T'HH:mm:ss format so string compare will be fine
				Collections.sort(sprints, JiraIssueClientUtil.MASTER_SPRINT_COMPARATOR);
				setSprintData(sprints, jiraIssue, sValue, projectConfig);

			} catch (ParseException | JSONException e) {
				log.error("JIRA Processor | Failed to obtain sprint data from {} {}", sValue, e);
			}
		}
		jiraIssue.setSprintChangeDate("");
		jiraIssue.setSprintIsDeleted(JiraConstants.FALSE);
	}

	private void setSprintData(List<SprintDetailsV2> sprints, JiraIssueV2 jiraIssue, Object sValue,
							   ProjectConfFieldMapping projectConfig) {
		List<String> sprintsList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(sprints)) {
			for (SprintDetailsV2 sprint : sprints) {
				sprintsList.add(sprint.getOriginalSprintId());
				jiraIssue.setSprintIdList(sprintsList);
				sprint.setSprintID(
						sprint.getOriginalSprintId() + JiraConstants.COMBINE_IDS_SYMBOL + jiraIssue.getProjectName()
								+ JiraConstants.COMBINE_IDS_SYMBOL + projectConfig.getBasicProjectConfigId());
			}
			// Use the latest sprint
			// if any sprint date is blank set that sprint to JiraIssueV2
			// because this sprint is
			// future sprint and Jira issue should be tagged with latest
			// sprint
			SprintDetailsV2 sprint = sprints.stream().filter(s -> StringUtils.isBlank(s.getStartDate())).findFirst()
					.orElse(sprints.get(sprints.size() - 1));

			jiraIssue.setSprintName(sprint.getSprintName() == null ? StringUtils.EMPTY : sprint.getSprintName());
			jiraIssue.setSprintID(sprint.getOriginalSprintId() == null ? StringUtils.EMPTY : sprint.getSprintID());
			jiraIssue.setSprintBeginDate(sprint.getStartDate() == null ? StringUtils.EMPTY
					: JiraProcessorUtil.getFormattedDate(sprint.getStartDate()));
			jiraIssue.setSprintEndDate(sprint.getEndDate() == null ? StringUtils.EMPTY
					: JiraProcessorUtil.getFormattedDate(sprint.getEndDate()));
			jiraIssue.setSprintAssetState(sprint.getState() == null ? StringUtils.EMPTY : sprint.getState());

		} else {
			log.error("JIRA Processor | Failed to obtain sprint data for {}", sValue);
		}

	}

	private void setEpicIssueData(FieldMapping fieldMapping, JiraIssueV2 jiraIssue, Map<String, IssueField> fields) {
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

		if (fields.get(fieldMapping.getEpicPlannedValue()) != null
				&& fields.get(fieldMapping.getEpicPlannedValue()).getValue() != null) {
			String fieldValue = getFieldValue(fieldMapping.getEpicPlannedValue(), fields);
			jiraIssue.setEpicPlannedValue(Double.parseDouble(fieldValue));
		}

		if (fields.get(fieldMapping.getEpicAchievedValue()) != null
				&& fields.get(fieldMapping.getEpicAchievedValue()).getValue() != null) {
			String fieldValue = getFieldValue(fieldMapping.getEpicAchievedValue(), fields);
			jiraIssue.setEpicAchievedValue(Double.parseDouble(fieldValue));
		}

	}

	private void setEstimates(JiraIssueV2 jiraIssue, Issue issue) {
		if (null != issue.getTimeTracking()) {
			jiraIssue.setOriginalEstimateMinutes(issue.getTimeTracking().getOriginalEstimateMinutes());
			jiraIssue.setRemainingEstimateMinutes(issue.getTimeTracking().getRemainingEstimateMinutes());
		}
	}

	private void setURL(String ticketNumber, JiraIssueV2 jiraIssue, ProjectConfFieldMapping projectConfig) {
		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		if (connectionOptional.isPresent()) {
			Connection connection = connectionOptional.get();
			Boolean cloudEnv = connection.isCloudEnv();
			String baseUrl = connection.getBaseUrl();

			if (baseUrl == null) {
				baseUrl = "";
			} else {
				baseUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/");

				if (Boolean.TRUE.equals(cloudEnv)) {
					baseUrl = baseUrl + jiraProcessorConfig.getJiraCloudDirectTicketLinkKey() + ticketNumber;
				} else {
					baseUrl = baseUrl + jiraProcessorConfig.getJiraDirectTicketLinkKey() + ticketNumber;
				}
			}
			jiraIssue.setUrl(baseUrl);
		}
	}

	private void setDueDates(JiraIssueV2 jiraIssue, Issue issue, Map<String, IssueField> fields,
							 FieldMapping fieldMapping) {
		if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateField())) {
			jiraIssue.setDueDate(getFormattedDate(issue, fields, fieldMapping.getJiraDueDateField(),
					fieldMapping.getJiraDueDateCustomField()));
		}

		if (StringUtils.isNotEmpty(fieldMapping.getJiraDevDueDateField())) {
			jiraIssue.setDevDueDate(getFormattedDate(issue, fields, fieldMapping.getJiraDevDueDateField(),
					fieldMapping.getJiraDevDueDateCustomField()));
		}
	}

	private String getFormattedDate(Issue issue, Map<String, IssueField> fields, String jiraDateField,
			String jiraDateCustomField) {
		String dateValue = null;

		if (jiraDateField.equalsIgnoreCase(CommonConstant.DUE_DATE) && ObjectUtils.isNotEmpty(issue.getDueDate())) {
			dateValue = JiraProcessorUtil.deodeUTF8String(issue.getDueDate()).split("T")[0]
					.concat(DateUtil.ZERO_TIME_ZONE_FORMAT);
		} else if (StringUtils.isNotEmpty(jiraDateCustomField)
				&& ObjectUtils.isNotEmpty(fields.get(jiraDateCustomField))) {
			IssueField issueField = fields.get(jiraDateCustomField);
			if (ObjectUtils.isNotEmpty(issueField.getValue())) {
				dateValue = JiraProcessorUtil.deodeUTF8String(issueField.getValue()).split("T")[0]
						.concat(DateUtil.ZERO_TIME_ZONE_FORMAT);
			}
		}
		return dateValue;
	}

	private void setTestingPhaseDefectIdentificationField(Issue issue, FieldMapping fieldMapping, JiraIssueV2 jiraIssue,
			Map<String, IssueField> fields) {
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype()) && fieldMapping.getJiradefecttype().stream()
				.anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)) {
			if (null != fieldMapping.getTestingPhaseDefectsIdentifier()
					&& fieldMapping.getTestingPhaseDefectsIdentifier().trim().equalsIgnoreCase(JiraConstants.LABELS)) {
				setTestPhaseDefectsList(issue, fieldMapping, jiraIssue);
			} else if (null != fieldMapping.getTestingPhaseDefectsIdentifier()
					&& fieldMapping.getTestingPhaseDefectsIdentifier().trim()
							.equalsIgnoreCase(JiraConstants.CUSTOM_FIELD)
					&& fields.get(fieldMapping.getTestingPhaseDefectCustomField().trim()) != null
					&& fields.get(fieldMapping.getTestingPhaseDefectCustomField().trim()).getValue() != null) {
				isBugRaisedByValueMatchesRaisedByCustomField(fieldMapping.getTestingPhaseDefectValue(),
						fields.get(fieldMapping.getTestingPhaseDefectCustomField().trim()).getValue(), jiraIssue, TEST_PHASE);
			} else if (null != fieldMapping.getTestingPhaseDefectsIdentifier() && fieldMapping
					.getTestingPhaseDefectsIdentifier().trim().equalsIgnoreCase(JiraConstants.COMPONENT)) {
				setTestPhaseDefectsListForComponent(issue, fieldMapping, jiraIssue);
			}
		}
	}

	private void setProdIncidentIdentificationField(FieldMapping featureConfig, Issue issue, JiraIssueV2 feature,
			Map<String, IssueField> fields) {
		try {
			if (CollectionUtils.isNotEmpty(featureConfig.getJiradefecttype()) && featureConfig.getJiradefecttype()
					.stream().anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)) {
				if (null != featureConfig.getJiraProductionIncidentIdentification() && featureConfig
						.getJiraProductionIncidentIdentification().trim().equalsIgnoreCase(JiraConstants.LABELS)) {
					List<String> commonLabel = issue.getLabels().stream()
							.filter(x -> featureConfig.getJiraProdIncidentRaisedByValue().contains(x))
							.collect(Collectors.toList());
					if (CollectionUtils.isNotEmpty(commonLabel)) {
						feature.setProductionIncident(true);
					}
				} else
					feature.setProductionIncident(null != featureConfig.getJiraProductionIncidentIdentification()
							&& featureConfig.getJiraProductionIncidentIdentification().trim()
									.equalsIgnoreCase(CommonConstant.CUSTOM_FIELD)
							&& fields.get(featureConfig.getJiraProdIncidentRaisedByCustomField().trim()) != null
							&& fields.get(featureConfig.getJiraProdIncidentRaisedByCustomField().trim())
									.getValue() != null
							&& isBugRaisedByValueMatchesRaisedByCustomField(
									featureConfig.getJiraProdIncidentRaisedByValue(),
									fields.get(featureConfig.getJiraProdIncidentRaisedByCustomField().trim())
											.getValue(),
									null, ""));
			}

		} catch (Exception e) {
			log.error("Error while parsing Production Incident field", e);
		}

	}
}
