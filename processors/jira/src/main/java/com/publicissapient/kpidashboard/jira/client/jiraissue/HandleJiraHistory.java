package com.publicissapient.kpidashboard.jira.client.jiraissue;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HandleJiraHistory {

	private List<JiraHistoryChangeLog> getJiraFieldChangeLog(List<ChangelogGroup> changeLogList, String jiraField) {

		List<JiraHistoryChangeLog> fieldHistoryLog = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(changeLogList)) {
			for (ChangelogGroup history : changeLogList) {
				history.getItems().forEach(item -> {
					if (item.getField().trim().equalsIgnoreCase(jiraField.trim())) {
						JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
						jiraHistoryChangeLog.setChangedFrom(handleStr(item.getFromString()));
						jiraHistoryChangeLog.setChangedTo(handleStr(item.getToString()));
						jiraHistoryChangeLog.setUpdatedOn(LocalDateTime.parse(JiraProcessorUtil
								.getFormattedDate(JiraProcessorUtil.deodeUTF8String(history.getCreated()))));
						fieldHistoryLog.add(jiraHistoryChangeLog);
					}
				});
			}
		}

		// Merging Fix Version object based on updation Timestamp
		if (jiraField.trim().equalsIgnoreCase(JiraConstants.FIXVERSION) && ObjectUtils.isNotEmpty(fieldHistoryLog)) {
			return mergeObjectsBasedOnTimestamp(fieldHistoryLog);
		}

		return fieldHistoryLog;
	}

	private String parseStringToLocalDateTime(String date) {
		return StringUtils.isEmpty(date) ? ""
				: LocalDateTime.parse(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(date)))
						.toString();
	}

	private List<JiraHistoryChangeLog> getDueDateChangeLog(List<ChangelogGroup> changeLogList,
			FieldMapping fieldMapping, Map<String, IssueField> fields) {
		if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateField())) {
			String field = "";
			if (fieldMapping.getJiraDueDateField().equalsIgnoreCase(CommonConstant.DUE_DATE))
				field = JiraConstants.DUEDATE;
			else if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateCustomField())
					&& ObjectUtils.isNotEmpty(fields.get(fieldMapping.getJiraDueDateCustomField()))) {
				IssueField issueField = fields.get(fieldMapping.getJiraDueDateCustomField());
				if (ObjectUtils.isNotEmpty(issueField.getName()))
					field = issueField.getName();
			}
			return createDueDateChangeLogs(changeLogList, field);
		}
		return new ArrayList<>();
	}

	private List<JiraHistoryChangeLog> createDueDateChangeLogs(List<ChangelogGroup> changeLogList, String field) {
		List<JiraHistoryChangeLog> fieldHistoryLog = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(changeLogList)) {
			for (ChangelogGroup history : changeLogList) {
				String finalField = field;
				history.getItems().forEach(item -> {
					if (item.getField().trim().equalsIgnoreCase(finalField)) {
						JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
						jiraHistoryChangeLog.setChangedFrom(parseStringToLocalDateTime(item.getFrom()));
						jiraHistoryChangeLog.setChangedTo(parseStringToLocalDateTime(item.getTo()));
						jiraHistoryChangeLog.setUpdatedOn(LocalDateTime.parse(JiraProcessorUtil
								.getFormattedDate(JiraProcessorUtil.deodeUTF8String(history.getCreated()))));
						fieldHistoryLog.add(jiraHistoryChangeLog);
					}
				});
			}
		}
		return fieldHistoryLog;
	}

	private List<JiraHistoryChangeLog> mergeObjectsBasedOnTimestamp(List<JiraHistoryChangeLog> fieldHistoryLog) {
		List<JiraHistoryChangeLog> fieldHistoryLogTemp = new ArrayList<>(fieldHistoryLog);
		List<JiraHistoryChangeLog> mergedFieldHistoryLog = new ArrayList<>();
		JiraHistoryChangeLog prevHistoryChangeLog = fieldHistoryLog.get(0);
		for (int i = 1; i < fieldHistoryLogTemp.size(); i++) {
			if (prevHistoryChangeLog.getUpdatedOn().equals(fieldHistoryLogTemp.get(i).getUpdatedOn())) {
				JiraHistoryChangeLog currHistoryChangeLog = fieldHistoryLogTemp.get(i);
				currHistoryChangeLog.setChangedFrom(concatStrUsingCommaSeparator(prevHistoryChangeLog.getChangedFrom(),
						currHistoryChangeLog.getChangedFrom()));
				currHistoryChangeLog.setChangedTo(concatStrUsingCommaSeparator(prevHistoryChangeLog.getChangedTo(),
						currHistoryChangeLog.getChangedTo()));
				fieldHistoryLogTemp.set(i, currHistoryChangeLog);
				prevHistoryChangeLog = currHistoryChangeLog;
			} else {
				mergedFieldHistoryLog.add(prevHistoryChangeLog);
				prevHistoryChangeLog = fieldHistoryLogTemp.get(i);
			}
		}
		mergedFieldHistoryLog.add(prevHistoryChangeLog);
		return mergedFieldHistoryLog;

	}

	private void splitMultipleSprintsAndStoreLastSprint(List<JiraHistoryChangeLog> sprintChangeLog) {
		int index = 0;
		for (JiraHistoryChangeLog jiraHistoryChangeLog : sprintChangeLog) {
			jiraHistoryChangeLog
					.setChangedFrom(spiltStringAndFetchLastValue(jiraHistoryChangeLog.getChangedFrom(), ","));
			jiraHistoryChangeLog.setChangedTo(spiltStringAndFetchLastValue(jiraHistoryChangeLog.getChangedTo(), ","));
			sprintChangeLog.set(index, jiraHistoryChangeLog);
			index++;
		}
	}

	private String spiltStringAndFetchLastValue(String str, String regex) {
		if (str.contains(regex)) {
			String[] splitedStr = str.split(regex);
			return splitedStr[splitedStr.length - 1].trim();
		}
		return str;
	}

	private List<JiraHistoryChangeLog> getCustomFieldChangeLog(List<ChangelogGroup> changeLogList,
			String jiraCustomField, Map<String, IssueField> fields) {

		if (StringUtils.isNotEmpty(jiraCustomField.trim())
				&& ObjectUtils.isNotEmpty(fields.get(jiraCustomField.trim()))) {
			String field = fields.get(jiraCustomField.trim()).getName();
			return getJiraFieldChangeLog(changeLogList, field.trim());
		}

		return new ArrayList<>();
	}

	private String handleStr(String str) {
		return str != null ? str : "";
	}

	private String concatStrUsingCommaSeparator(String str1, String str2) {
		if (StringUtils.isEmpty(str1))
			return str2;
		if (StringUtils.isEmpty(str2))
			return str1;
		String str3 = str1.concat(",");
		return str3.concat(str2);

	}

	public void setJiraIssueCustomHistoryUpdationLog(JiraIssueCustomHistory jiraIssueCustomHistory,
			List<ChangelogGroup> changeLogList, ProjectConfFieldMapping projectConfig, Map<String, IssueField> fields,
			Issue issue) {
		FieldMapping fieldMapping = projectConfig.getFieldMapping();
		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		Boolean cloudEnv = connectionOptional.isPresent() ? connectionOptional.map(Connection::isCloudEnv).get()
				: Boolean.FALSE;
		List<JiraHistoryChangeLog> statusChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.STATUS);
		List<JiraHistoryChangeLog> assigneeChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.ASSIGNEE);
		List<JiraHistoryChangeLog> priorityChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.PRIORITY);
		List<JiraHistoryChangeLog> fixVersionChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.FIXVERSION);
		List<JiraHistoryChangeLog> labelsChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.LABELS);
		List<JiraHistoryChangeLog> dueDateChangeLog = getDueDateChangeLog(changeLogList, fieldMapping, fields);
		List<JiraHistoryChangeLog> sprintChangeLog = getCustomFieldChangeLog(changeLogList,
				handleStr(fieldMapping.getSprintName()), fields);
		List<JiraHistoryChangeLog> flagStatusChangeLog;
		if (cloudEnv) {
			flagStatusChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.FLAG_STATUS_FOR_CLOUD);
		} else {
			flagStatusChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.FLAG_STATUS_FOR_SERVER);
		}
		createFirstEntryOfChangeLog(statusChangeLog, issue,
				ObjectUtils.isNotEmpty(issue.getStatus()) ? issue.getStatus().getName() : "");
		createFirstEntryOfChangeLog(assigneeChangeLog, issue,
				ObjectUtils.isNotEmpty(issue.getAssignee()) ? issue.getAssignee().getDisplayName() : "");
		createFirstEntryOfChangeLog(priorityChangeLog, issue,
				ObjectUtils.isNotEmpty(issue.getPriority()) ? issue.getPriority().getName() : "");
		createFirstEntryOfChangeLog(labelsChangeLog, issue, StringUtils.join(issue.getLabels(), " "));
		createFirstEntryOfDueDateChangeLog(dueDateChangeLog, fieldMapping, issue, fields);
		creatingFirstEntryOfSprintChangeLog(sprintChangeLog, fieldMapping, issue, fields);
		createFixVersionHistory(fixVersionChangeLog, issue, convertIterableVersionToString(issue.getFixVersions()));
		splitMultipleSprintsAndStoreLastSprint(sprintChangeLog);

		jiraIssueCustomHistory.setStatusUpdationLog(statusChangeLog);
		jiraIssueCustomHistory.setAssigneeUpdationLog(assigneeChangeLog);
		jiraIssueCustomHistory.setPriorityUpdationLog(priorityChangeLog);
		jiraIssueCustomHistory.setFixVersionUpdationLog(fixVersionChangeLog);
		jiraIssueCustomHistory.setLabelUpdationLog(labelsChangeLog);
		jiraIssueCustomHistory.setDueDateUpdationLog(dueDateChangeLog);
		jiraIssueCustomHistory.setSprintUpdationLog(sprintChangeLog);
		jiraIssueCustomHistory.setFlagStatusChangeLog(flagStatusChangeLog);

	}

	private void creatingFirstEntryOfSprintChangeLog(List<JiraHistoryChangeLog> sprintChangeLog,
			FieldMapping fieldMapping, Issue issue, Map<String, IssueField> fields) {
		if (StringUtils.isNotEmpty(fieldMapping.getSprintName())
				&& ObjectUtils.isNotEmpty(fields.get(fieldMapping.getSprintName()))) {
			IssueField issueField = fields.get(fieldMapping.getSprintName());
			if (ObjectUtils.isNotEmpty(issueField.getValue())) {
				Object sValue = issueField.getValue();
				try {
					List<SprintDetails> sprints = JiraProcessorUtil.processSprintDetail(sValue);
					Collections.sort(sprints, JiraIssueClientUtil.SPRINT_COMPARATOR);
					if (!sprints.isEmpty())
						createFirstEntryOfChangeLog(sprintChangeLog, issue, sprints.get(0).getSprintName());
				} catch (ParseException | JSONException e) {
					log.error("JIRA Processor | Failed to obtain sprint data from {} {}", sValue, e);
				}

			}
		}
	}

	private void createFirstEntryOfDueDateChangeLog(List<JiraHistoryChangeLog> dueDateChangeLog,
			FieldMapping fieldMapping, Issue issue, Map<String, IssueField> fields) {
		if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateField())) {
			if (fieldMapping.getJiraDueDateField().equalsIgnoreCase(CommonConstant.DUE_DATE)
					&& ObjectUtils.isNotEmpty(issue.getDueDate())) {
				createFirstEntryOfChangeLog(dueDateChangeLog, issue,
						LocalDateTime
								.parse(JiraProcessorUtil
										.getFormattedDate(JiraProcessorUtil.deodeUTF8String(issue.getDueDate())))
								.toString());
			} else if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateCustomField())
					&& ObjectUtils.isNotEmpty(fields.get(fieldMapping.getJiraDueDateCustomField()))) {
				IssueField issueField = fields.get(fieldMapping.getJiraDueDateCustomField());
				if (ObjectUtils.isNotEmpty(issueField.getValue())) {
					createFirstEntryOfChangeLog(dueDateChangeLog, issue,
							LocalDateTime
									.parse(JiraProcessorUtil
											.getFormattedDate(JiraProcessorUtil.deodeUTF8String(issueField.getValue())))
									.toString());
				}
			}
		}
	}

	private void createFixVersionHistory(List<JiraHistoryChangeLog> fixVersionChangeLog, Issue issue,
			String currentFixVersion) {

		if (!fixVersionChangeLog.isEmpty()) {
			String lastChangedTo = fixVersionChangeLog.get(fixVersionChangeLog.size() - 1).getChangedTo();
			currentFixVersion = createChangedToForFixVersion(currentFixVersion, lastChangedTo);
		}
		final String[] lastLogChangeToValue = { currentFixVersion };
		Lists.reverse(fixVersionChangeLog).forEach(currChangeLog -> {
			String currLogChangeToValue = currChangeLog.getChangedTo();
			String currLogChangeFromValue = currChangeLog.getChangedFrom();
			String differences = getNonCommonFixVersion(currLogChangeToValue, lastLogChangeToValue[0]);
			currChangeLog
					.setChangedTo(createChangedToForFixVersion(currChangeLog.getChangedTo(), lastLogChangeToValue[0]));
			currChangeLog.setChangedFrom(concatStrUsingCommaSeparator(currLogChangeFromValue, differences));
			lastLogChangeToValue[0] = currChangeLog.getChangedFrom();
		});
		createFirstEntryOfChangeLog(fixVersionChangeLog, issue, lastLogChangeToValue[0]);

	}

	private String createChangedToForFixVersion(String currentFixVersion, String lastChangedTo) {
		String[] lastChangedToList = lastChangedTo.split(",");
		String[] currentFixVersionList = currentFixVersion.split(",");
		Set<String> set = new HashSet<>();
		set.addAll(Arrays.asList(lastChangedToList));
		set.addAll(Arrays.asList(currentFixVersionList));
		return StringUtils.join(set, ",");
	}

	private String getNonCommonFixVersion(String currLogChangeToValue, String lastLogChangeToValue) {
		String[] currLogChangeToList = currLogChangeToValue.split(",");
		String[] lastLogChangeToList = lastLogChangeToValue.split(",");
		List<String> differences = Arrays.asList(lastLogChangeToList).stream()
				.filter(val -> !Arrays.asList(currLogChangeToList).contains(val)).collect(Collectors.toList());
		return StringUtils.join(differences, ",");
	}

	private String convertIterableVersionToString(Iterable<Version> fixVersions) {
		String str = "";
		if (CollectionUtils.isEmpty((Collection<?>) fixVersions))
			return str;
		for (Version version : fixVersions) {
			String newStr = str.concat(version.getName());
			str = newStr.concat(",");
		}
		return str.substring(0, str.length() - 1);
	}

	private void createFirstEntryOfChangeLog(List<JiraHistoryChangeLog> fieldChangeLog, Issue issue,
			String fieldValuefromIssue) {

		if (null != issue.getCreationDate() && ((fieldChangeLog.isEmpty() && !fieldValuefromIssue.isEmpty())
				|| (!fieldChangeLog.isEmpty() && !fieldChangeLog.get(0).getChangedFrom().isEmpty()))) {
			JiraHistoryChangeLog firstEntry = new JiraHistoryChangeLog();
			firstEntry.setChangedFrom("");
			firstEntry.setUpdatedOn(LocalDateTime.parse(
					JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(issue.getCreationDate()))));
			if (!fieldChangeLog.isEmpty()) {
				firstEntry.setChangedTo(fieldChangeLog.get(0).getChangedFrom());
			} else {
				firstEntry.setChangedTo(fieldValuefromIssue);
			}
			fieldChangeLog.add(0, firstEntry);
		}
	}

}
