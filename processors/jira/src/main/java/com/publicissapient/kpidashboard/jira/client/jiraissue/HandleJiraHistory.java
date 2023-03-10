package com.publicissapient.kpidashboard.jira.client.jiraissue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
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
		// creating first entry of issue
		if (null != jiraIssue.getCreatedDate() && jiraField.equalsIgnoreCase(JiraConstants.STATUS)) {
			JiraHistoryChangeLog jiraHistoryChangeLog = new JiraHistoryChangeLog();
			jiraHistoryChangeLog.setChangedFrom("");
			jiraHistoryChangeLog.setChangedTo(StringUtils.capitalize(CommonConstant.OPEN));
			jiraHistoryChangeLog.setUpdatedOn(LocalDateTime.parse(
					JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(jiraIssue.getCreatedDate()))));
			fieldHistoryLog.add(jiraHistoryChangeLog);
		}

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
				currHistoryChangeLog.setChangedFrom(
						concatStrUsingCommaSeparator(prevHistoryChangeLog.getChangedFrom(), currHistoryChangeLog.getChangedFrom()));
				currHistoryChangeLog.setChangedTo(
						concatStrUsingCommaSeparator(prevHistoryChangeLog.getChangedTo(), currHistoryChangeLog.getChangedTo()));
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

	private void splitMultipleSprintstoLastSprint(List<JiraHistoryChangeLog> sprintChangeLog)
	{
		for (int i=0;i<sprintChangeLog.size();i++)
		{
			JiraHistoryChangeLog jiraHistoryChangeLog = sprintChangeLog.get(i);
			jiraHistoryChangeLog.setChangedFrom(spiltStringAndfetchLastValue(jiraHistoryChangeLog.getChangedFrom()));
			jiraHistoryChangeLog.setChangedTo(spiltStringAndfetchLastValue(jiraHistoryChangeLog.getChangedTo()));
			sprintChangeLog.set(i,jiraHistoryChangeLog);
		}
	}

	private String spiltStringAndfetchLastValue(String str)
	{
		if(str.contains(","))
		{
			String[] splitedStr = str.split(",");
			return splitedStr[splitedStr.length-1].trim();
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

	public void setJiraIssueHistory(JiraIssueCustomHistory jiraIssueHistory, JiraIssue jiraIssue, Issue issue,
									 FieldMapping fieldMapping, Map<String, IssueField> fields) {

		jiraIssueHistory.setProjectID(jiraIssue.getProjectName());
		jiraIssueHistory.setProjectComponentId(jiraIssue.getProjectID());
		jiraIssueHistory.setProjectKey(jiraIssue.getProjectKey());
		jiraIssueHistory.setStoryType(jiraIssue.getTypeName());
		jiraIssueHistory.setAdditionalFilters(jiraIssue.getAdditionalFilters());
		jiraIssueHistory.setUrl(jiraIssue.getUrl());
		jiraIssueHistory.setDescription(jiraIssue.getName());
		// This method is not setup method. write it to keep
		// custom history
		processJiraIssueHistory(jiraIssueHistory, jiraIssue, issue, fieldMapping, fields);

		jiraIssueHistory.setBasicProjectConfigId(jiraIssue.getBasicProjectConfigId());
	}

	private void processJiraIssueHistory(JiraIssueCustomHistory jiraIssueCustomHistory, JiraIssue jiraIssue,
										 Issue issue, FieldMapping fieldMapping, Map<String, IssueField> fields) {
		List<ChangelogGroup> changeLogList = JiraIssueClientUtil.sortChangeLogGroup(issue);
		List<ChangelogGroup> modChangeLogList = new ArrayList<>();

		for (ChangelogGroup changeLog : changeLogList) {
			List<ChangelogItem> changeLogCollection = Lists.newArrayList(changeLog.getItems().iterator());
			ChangelogGroup grp = new ChangelogGroup(changeLog.getAuthor(), changeLog.getCreated(), changeLogCollection);
			modChangeLogList.add(grp);
		}

		if (null != jiraIssue.getDevicePlatform()) {
			jiraIssueCustomHistory.setDevicePlatform(jiraIssue.getDevicePlatform());
		}
		if (null == jiraIssueCustomHistory.getStoryID()) {
			addStoryHistory(jiraIssueCustomHistory, jiraIssue, issue, modChangeLogList, fieldMapping, fields);
		} else {
			if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
				jiraIssueCustomHistory.setDefectStoryID(jiraIssue.getDefectStoryID());
			}
			setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, fieldMapping, jiraIssue, fields);
		}

	}

	private void addStoryHistory(JiraIssueCustomHistory jiraIssueCustomHistory, JiraIssue jiraIssue, Issue issue,
								 List<ChangelogGroup> changeLogList, FieldMapping fieldMapping, Map<String, IssueField> fields) {
		setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, fieldMapping, jiraIssue, fields);
		jiraIssueCustomHistory.setStoryID(jiraIssue.getNumber());
		jiraIssueCustomHistory.setCreatedDate(issue.getCreationDate());

		// estimate
		jiraIssueCustomHistory.setEstimate(jiraIssue.getEstimate());
		jiraIssueCustomHistory.setBufferedEstimateTime(jiraIssue.getBufferedEstimateTime());
		if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
			jiraIssueCustomHistory.setDefectStoryID(jiraIssue.getDefectStoryID());
		}
	}

	private void setJiraIssueCustomHistoryUpdationLog(JiraIssueCustomHistory jiraIssueCustomHistory,
			List<ChangelogGroup> changeLogList, FieldMapping fieldMapping, JiraIssue jiraIssue,
			Map<String, IssueField> fields) {
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

		splitMultipleSprintstoLastSprint(sprintChangeLog);

		jiraIssueCustomHistory.setStatusUpdationLog(statusChangeLog);
		jiraIssueCustomHistory.setAssigneeUpdationLog(assigneeChangeLog);
		jiraIssueCustomHistory.setPriorityUpdationLog(priorityChangeLog);
		jiraIssueCustomHistory.setFixVersionUpdationLog(fixVersionChangeLog);
		jiraIssueCustomHistory.setLabelUpdationLog(labelsChangeLog);
		jiraIssueCustomHistory.setDueDateUpdationLog(dueDateChangeLog);
		jiraIssueCustomHistory.setSprintUpdationLog(sprintChangeLog);
	}
}
