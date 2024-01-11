package com.publicissapient.kpidashboard.azure.client.azureissue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.azure.util.AzureConstants;
import com.publicissapient.kpidashboard.azure.util.AzureProcessorUtil;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.Fields;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.Value;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScrumHandleAzureIssueHistory {

	public static final String NEW_VALUE = "newValue";
	public static final String OLD_VALUE = "oldValue";
	public static final String DISPLAY_NAME = "displayName";

	private List<JiraHistoryChangeLog> getJiraFieldChangeLogFromAdditionProps(List<Value> updateValueList,
			String jiraField) {

		List<JiraHistoryChangeLog> fieldHistoryLog = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(updateValueList)) {
			for (Value history : updateValueList) {
				Fields changelogItem = history.getFields();
				if (changelogItem != null) {
					changelogItem.getAdditionalProperties().forEach((key, value) -> {
						Map<String, Object> changeLogValue = (Map<String, Object>) value;
						if (key.trim().equalsIgnoreCase(jiraField.trim()) && !changeLogValue.isEmpty()) {
							fieldHistoryLog.add(handleChangelogs(key, changeLogValue, changelogItem));
						}
					});
				}
			}
		}

		return fieldHistoryLog;
	}

	public JiraHistoryChangeLog handleChangelogs(String key, Map<String, Object> value, Fields changelogItem) {
		JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
		if (key.trim().equalsIgnoreCase(AzureConstants.ASSIGNEE)) {
			jiraHistoryChangeLog.setChangedFrom(handleAssigneeStr(value, OLD_VALUE));
			jiraHistoryChangeLog.setChangedTo(handleAssigneeStr(value, NEW_VALUE));
		}
		else if (key.trim().equalsIgnoreCase(AzureConstants.WORKLOG)) {
			jiraHistoryChangeLog.setChangedFrom(convertToSeconds(value, OLD_VALUE));
			jiraHistoryChangeLog.setChangedTo(convertToSeconds(value, NEW_VALUE));
		}
		else {
			jiraHistoryChangeLog.setChangedFrom(handleStr(value, OLD_VALUE));
			jiraHistoryChangeLog.setChangedTo(handleStr(value, NEW_VALUE));
		}
		jiraHistoryChangeLog.setUpdatedOn(LocalDateTime.parse(AzureProcessorUtil.getFormattedDate(
				AzureProcessorUtil.deodeUTF8String(changelogItem.getSystemChangedDate().getNewValue()))));
		return jiraHistoryChangeLog;
	}

	private List<JiraHistoryChangeLog> getStatusChangeLog(List<Value> updateValueList) {
		List<JiraHistoryChangeLog> fieldHistoryLog = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(updateValueList)) {
			for (Value history : updateValueList) {
				com.publicissapient.kpidashboard.common.model.azureboards.updates.Fields changelogItem = history
						.getFields();
				if (changelogItem != null && changelogItem.getSystemState() != null) {
					JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
					String oldValue = changelogItem.getSystemState().getOldValue() != null
							? changelogItem.getSystemState().getOldValue()
							: "";
					jiraHistoryChangeLog.setChangedFrom(oldValue);
					jiraHistoryChangeLog.setChangedTo(changelogItem.getSystemState().getNewValue());
					jiraHistoryChangeLog.setUpdatedOn(LocalDateTime.parse(AzureProcessorUtil.getFormattedDate(
							AzureProcessorUtil.deodeUTF8String(changelogItem.getSystemChangedDate().getNewValue()))));
					fieldHistoryLog.add(jiraHistoryChangeLog);

				}
			}
		}

		return fieldHistoryLog;
	}

	public String getModifiedSprintsPath(String sprintPath) {
		String finalSprintPath = org.apache.commons.lang3.StringUtils.EMPTY;
		String separator = "\\";
		if (org.apache.commons.lang3.StringUtils.isNotEmpty(sprintPath)) {
			int sepPos = sprintPath.indexOf(separator);
			if (sepPos == -1) {
				finalSprintPath = sprintPath;
			} else {
				finalSprintPath = sprintPath.substring(sepPos + separator.length());
			}

		}
		return finalSprintPath;
	}

	private List<JiraHistoryChangeLog> getIterationChangeLog(List<Value> updateValueList) {

		List<JiraHistoryChangeLog> fieldHistoryLog = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(updateValueList)) {
			for (Value history : updateValueList) {
				com.publicissapient.kpidashboard.common.model.azureboards.updates.Fields changelogItem = history
						.getFields();
				if (changelogItem != null && changelogItem.getSystemIterationPath() != null) {
					JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
					String oldValue = changelogItem.getSystemIterationPath().getOldValue() != null
							? getModifiedSprintsPath(changelogItem.getSystemIterationPath().getOldValue())
							: "";
					String newValue = changelogItem.getSystemIterationPath().getNewValue() != null
							? getModifiedSprintsPath(changelogItem.getSystemIterationPath().getNewValue())
							: "";
					jiraHistoryChangeLog.setChangedFrom(oldValue);
					jiraHistoryChangeLog.setChangedTo(newValue);
					jiraHistoryChangeLog.setUpdatedOn(LocalDateTime.parse(AzureProcessorUtil.getFormattedDate(
							AzureProcessorUtil.deodeUTF8String(changelogItem.getSystemChangedDate().getNewValue()))));
					fieldHistoryLog.add(jiraHistoryChangeLog);
				}

			}
		}

		return fieldHistoryLog;
	}

	private String parseStringToLocalDateTime(String date) {
		return StringUtils.isEmpty(date) ? ""
				: LocalDateTime.parse(AzureProcessorUtil.getFormattedDate(AzureProcessorUtil.deodeUTF8String(date)))
						.toString();
	}

	private List<JiraHistoryChangeLog> getDueDateChangeLog(List<Value> changeLogList, FieldMapping fieldMapping,
			Map<String, Object> fields) {
		if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateField())) {
			String field = "";
			if (fieldMapping.getJiraDueDateField().equalsIgnoreCase(CommonConstant.DUE_DATE))
				field = AzureConstants.DUE_DATE;
			else if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateCustomField())
					&& ObjectUtils.isNotEmpty(fields.get(fieldMapping.getJiraDueDateCustomField()))) {
				field = fields.get(fieldMapping.getJiraDueDateCustomField()).toString();
			}
			return createDueDateChangeLogs(changeLogList, field);
		}
		return new ArrayList<>();
	}

	private List<JiraHistoryChangeLog> createDueDateChangeLogs(List<Value> changeLogList, String field) {
		List<JiraHistoryChangeLog> fieldHistoryLog = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(changeLogList)) {
			for (Value history : changeLogList) {
				com.publicissapient.kpidashboard.common.model.azureboards.updates.Fields changelogItem = history
						.getFields();
				if (changelogItem != null) {
					changelogItem.getAdditionalProperties().forEach((key, value) -> {
						if (key.trim().equalsIgnoreCase(field)) {
							JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
							jiraHistoryChangeLog.setChangedFrom(
									parseStringToLocalDateTime(handleStr((Map<String, Object>) value, OLD_VALUE)));
							jiraHistoryChangeLog.setChangedTo(
									parseStringToLocalDateTime(handleStr((Map<String, Object>) value, NEW_VALUE)));
							jiraHistoryChangeLog.setUpdatedOn(
									LocalDateTime.parse(AzureProcessorUtil.getFormattedDate(AzureProcessorUtil
											.deodeUTF8String(changelogItem.getSystemChangedDate().getNewValue()))));
							fieldHistoryLog.add(jiraHistoryChangeLog);
						}
					});
				}
			}
		}
		return fieldHistoryLog;
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
		if (str != null && str.contains(regex)) {
			String[] splitedStr = str.split(regex);
			return splitedStr[splitedStr.length - 1].trim();
		}
		return str;
	}

	private String handleStr(Map<String, Object> changeValues, String valueState) {
		if (changeValues.containsKey(valueState))
			return changeValues.get(valueState).toString();
		return "";
	}
	
	public String convertToSeconds(Map<String, Object> changeValues, String valueState) {
		String mutlipliedValue = "";
		try {
			if (changeValues.containsKey(valueState)) {
				int multipliedValue = (int) Double.parseDouble(changeValues.get(valueState).toString()) * 3600;
				mutlipliedValue = Integer.toString(multipliedValue);
			}
		} catch (NumberFormatException e) {
			log.error(e + "cannot process value" + changeValues);
			return mutlipliedValue;
		}
		return mutlipliedValue;
	}


	private String handleAssigneeStr(Map<String, Object> changeValues, String valueState) {
		if (changeValues.containsKey(valueState)) {
			Map<String, String> assigneeMap = (Map<String, String>) changeValues.get(valueState);
			return assigneeMap.get(DISPLAY_NAME);
		}
		return "";
	}

	public void setJiraIssueCustomHistoryUpdationLog(JiraIssueCustomHistory jiraIssueCustomHistory,
			List<Value> updateValueList, FieldMapping fieldMapping, Map<String, Object> fieldsMap) {
		List<JiraHistoryChangeLog> statusChangeLog = getStatusChangeLog(updateValueList);
		List<JiraHistoryChangeLog> assigneeChangeLog = getJiraFieldChangeLogFromAdditionProps(updateValueList,
				AzureConstants.ASSIGNEE);
		List<JiraHistoryChangeLog> priorityChangeLog = getJiraFieldChangeLogFromAdditionProps(updateValueList,
				AzureConstants.PRIORITY);
		List<JiraHistoryChangeLog> labelsChangeLog = getJiraFieldChangeLogFromAdditionProps(updateValueList,
				AzureConstants.LABEL);
		List<JiraHistoryChangeLog> workLog = getJiraFieldChangeLogFromAdditionProps(updateValueList,
				AzureConstants.WORKLOG);

		List<JiraHistoryChangeLog> dueDateChangeLog = getDueDateChangeLog(updateValueList, fieldMapping, fieldsMap);
		List<JiraHistoryChangeLog> sprintChangeLog = getIterationChangeLog(updateValueList);

		splitMultipleSprintsAndStoreLastSprint(sprintChangeLog);

		jiraIssueCustomHistory.setStatusUpdationLog(statusChangeLog);
		jiraIssueCustomHistory.setAssigneeUpdationLog(assigneeChangeLog);
		jiraIssueCustomHistory.setPriorityUpdationLog(priorityChangeLog);
		jiraIssueCustomHistory.setLabelUpdationLog(labelsChangeLog);
		jiraIssueCustomHistory.setDueDateUpdationLog(dueDateChangeLog);
		jiraIssueCustomHistory.setSprintUpdationLog(sprintChangeLog);
		jiraIssueCustomHistory.setWorkLog(workLog);
	}

}
