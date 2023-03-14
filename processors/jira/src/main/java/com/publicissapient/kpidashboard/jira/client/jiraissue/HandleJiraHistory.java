package com.publicissapient.kpidashboard.jira.client.jiraissue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HandleJiraHistory {

	private List<JiraHistoryChangeLog> getJiraFieldChangeLog(List<ChangelogGroup> changeLogList, String jiraField,
			JiraIssue jiraIssue) {

		log.info("In getJiraFieldChangeLog for field : " + jiraField);
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

	private List<JiraHistoryChangeLog> getCustomFieldChangeLog(List<ChangelogGroup> changeLogList, String jiraField,
			String jiraCustomField, Map<String, IssueField> fields, JiraIssue jiraIssue) {

		List<JiraHistoryChangeLog> fieldHistoryLog;
		String field = jiraField;
		if (StringUtils.isNotEmpty(jiraCustomField.trim())
				&& ObjectUtils.isNotEmpty(fields.get(jiraCustomField.trim()))) {
			field = fields.get(jiraCustomField.trim()).getName();
			log.info("In getCustomFieldChangeLog for custom field : " + field + " and fieldId : " + jiraCustomField);
		}
		fieldHistoryLog = getJiraFieldChangeLog(changeLogList, field.trim(), jiraIssue);
		return fieldHistoryLog;
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
			List<ChangelogGroup> changeLogList, FieldMapping fieldMapping, JiraIssue jiraIssue,
			Map<String, IssueField> fields, Issue issue) {
		log.info("In setJiraIssueCustomHistoryUpdationLog");
		List<JiraHistoryChangeLog> statusChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.STATUS,
				jiraIssue);
		List<JiraHistoryChangeLog> assigneeChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.ASSIGNEE,
				jiraIssue);
		List<JiraHistoryChangeLog> priorityChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.PRIORITY,
				jiraIssue);
		List<JiraHistoryChangeLog> fixVersionChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.FIXVERSION,
				jiraIssue);
		List<JiraHistoryChangeLog> labelsChangeLog = getJiraFieldChangeLog(changeLogList, JiraConstants.LABELS,
				jiraIssue);
		List<JiraHistoryChangeLog> dueDateChangeLog = getCustomFieldChangeLog(changeLogList, JiraConstants.DUEDATE,
				handleStr(fieldMapping.getJiraDueDateCustomField()), fields, jiraIssue);
		List<JiraHistoryChangeLog> sprintChangeLog = getCustomFieldChangeLog(changeLogList, JiraConstants.SPRINT,
				handleStr(fieldMapping.getSprintName()), fields, jiraIssue);

		createFirstEntryOfChangeLog(statusChangeLog, issue, issue.getStatus().getName());
		createFirstEntryOfChangeLog(assigneeChangeLog, issue, issue.getAssignee().getDisplayName());
		createFirstEntryOfChangeLog(priorityChangeLog, issue, issue.getPriority().getName());
		createFirstEntryOfChangeLog(fixVersionChangeLog, issue, convertIterableVersionToString(issue.getFixVersions()));
		createFirstEntryOfChangeLog(labelsChangeLog, issue, convertListToString(issue.getLabels()));

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


		if (StringUtils.isNotEmpty(fieldMapping.getSprintName())) {
			if (ObjectUtils.isNotEmpty(fields.get(fieldMapping.getSprintName()))) {
				IssueField issueField = fields.get(fieldMapping.getSprintName());
				if (ObjectUtils.isNotEmpty(issueField.getValue())) {
					String[] sprint = JiraProcessorUtil.deodeUTF8String(issueField.getValue()).split(",startDate")[0]
							.split("=");
					if (sprint.length > 0) {
						String initialSprintName = sprint[sprint.length - 1];
						createFirstEntryOfChangeLog(sprintChangeLog, issue, initialSprintName);
					}
				}
			}
		}

		splitMultipleSprintsAndStoreLastSprint(sprintChangeLog);

		jiraIssueCustomHistory.setStatusUpdationLog(statusChangeLog);
		jiraIssueCustomHistory.setAssigneeUpdationLog(assigneeChangeLog);
		jiraIssueCustomHistory.setPriorityUpdationLog(priorityChangeLog);
		jiraIssueCustomHistory.setFixVersionUpdationLog(fixVersionChangeLog);
		jiraIssueCustomHistory.setLabelUpdationLog(labelsChangeLog);
		jiraIssueCustomHistory.setDueDateUpdationLog(dueDateChangeLog);
		jiraIssueCustomHistory.setSprintUpdationLog(sprintChangeLog);
	}

	private String convertListToString(Set<String> labels) {
		String str = "";
		for (String label : labels) {
			String newStr = str.concat(label);
			str = newStr.concat(" ");
		}
		return str.trim();
	}

	private String convertIterableVersionToString(Iterable<Version> fixVersions) {
		String str = "";
		for (Version version : fixVersions) {
			String newStr = str.concat(version.getName());
			str = newStr.concat(",");
		}
		return str.substring(0, str.length() - 1);
	}

	private void createFirstEntryOfChangeLog(List<JiraHistoryChangeLog> fieldChangeLog, Issue issue,
			String fieldValuefromIssue) {
		if (null != issue.getCreationDate() && !fieldValuefromIssue.isEmpty()
				&& (fieldChangeLog.size() == 0 || !fieldChangeLog.get(0).getChangedFrom().isEmpty())) {
			JiraHistoryChangeLog firstEntry = new JiraHistoryChangeLog();
			firstEntry.setChangedFrom("");
			firstEntry.setUpdatedOn(LocalDateTime.parse(
					JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(issue.getCreationDate()))));
			if (fieldChangeLog.size() > 0) {
				firstEntry.setChangedTo(fieldChangeLog.get(0).getChangedFrom());
			} else {
				firstEntry.setChangedTo(fieldValuefromIssue);
			}
			fieldChangeLog.add(0, firstEntry);
		}
	}

}
