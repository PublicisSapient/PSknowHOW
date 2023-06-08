package com.publicissapient.kpidashboard.jira.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.publicissapient.kpidashboard.common.model.jira.IssueBacklog;
import com.publicissapient.kpidashboard.common.model.jira.IssueBacklogCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogRepository;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateIssueBacklogImpl implements CreateIssueBacklog {

	@Autowired
	private IssueBacklogRepository issueBacklogRepository;

	@Autowired
	private IssueBacklogCustomHistoryRepository issueBacklogCustomHistoryRepository;

	@Override
	public void createIssueBacklogandIssueBacklogHistory(List<JiraIssue> jiraIssuesToSave,
			List<JiraIssueCustomHistory> jiraIssueHistoryToSave, List<JiraIssue> jiraIssuesToDelete,
			List<JiraIssueCustomHistory> jiraIssueHistoryToDelete, List<IssueBacklog> issueBacklogToSave,
			List<IssueBacklogCustomHistory> issueBacklogCustomHistoryToSave, List<IssueBacklog> issueBacklogToDelete,
			List<IssueBacklogCustomHistory> issueBacklogCustomHistoryToDelete, JiraIssue jiraIssue,
			JiraIssueCustomHistory jiraIssueHistory, IssueField sprint, String issueId,
			ProjectConfFieldMapping projectConfig, String issueNumber) {

		IssueBacklog issueBacklog = getIssueBacklog(projectConfig, issueId);
		IssueBacklogCustomHistory issueBacklogCustomHistory = getIssueBacklogCustomHistory(projectConfig, issueNumber);
		boolean jiraIssuePresentInDb = jiraIssue.getIssueId() != null;
		boolean backlogPresentInDb = issueBacklog.getIssueId() != null;

		if (isIssueBacklog(jiraIssue, sprint) && StringUtils.isNotBlank(jiraIssue.getProjectID())
				&& !jiraIssue.getTypeName().equalsIgnoreCase("Epic")) {
			concertJiraIssueToBacklog(jiraIssue, issueBacklog);
			concertJiraIssueHistoryToBacklogHistory(jiraIssueHistory, issueBacklogCustomHistory);
			// When issue is moved from active sprint to backlog/future sprint
			if (jiraIssuePresentInDb) {
				jiraIssuesToDelete.add(jiraIssue);
				jiraIssueHistoryToDelete.add(jiraIssueHistory);
			}
			issueBacklogCustomHistoryToSave.add(issueBacklogCustomHistory);
			issueBacklogToSave.add(issueBacklog);
		} else if (StringUtils.isNotBlank(jiraIssue.getProjectID())) {
			if (backlogPresentInDb) {
				issueBacklogToDelete.add(issueBacklog);
				issueBacklogCustomHistoryToDelete.add(issueBacklogCustomHistory);
			}
			jiraIssuesToSave.add(jiraIssue);
			jiraIssueHistoryToSave.add(jiraIssueHistory);
		}
	}

	private IssueBacklog getIssueBacklog(ProjectConfFieldMapping projectConfig, String issueId) {
		List<IssueBacklog> issueBacklogs = issueBacklogRepository.findByIssueIdAndBasicProjectConfigId(
				StringEscapeUtils.escapeHtml4(issueId), projectConfig.getBasicProjectConfigId().toString());

		if (issueBacklogs.size() > 1) {
			log.error("JIRA Processor | More than one Issue Backlog item found for id {}", issueId);
		}

		if (!issueBacklogs.isEmpty()) {
			return issueBacklogs.get(0);
		}
		return new IssueBacklog();

	}

	private IssueBacklogCustomHistory getIssueBacklogCustomHistory(ProjectConfFieldMapping projectConfig,
			String storyId) {
		List<IssueBacklogCustomHistory> issueBacklogCustomHistorys = issueBacklogCustomHistoryRepository
				.findByStoryIDAndBasicProjectConfigId(storyId, projectConfig.getBasicProjectConfigId().toString());
		if (issueBacklogCustomHistorys.size() > 1) {
			log.error("JIRA Processor | More than one Issue backlog History item found for id {}", storyId);
		}

		if (!issueBacklogCustomHistorys.isEmpty()) {
			return issueBacklogCustomHistorys.get(0);
		}
		return new IssueBacklogCustomHistory();
	}

	private boolean isIssueBacklog(JiraIssue jiraIssue, IssueField sprintField) {
		return sprintField == null || sprintField.getValue() == null
				|| JiraConstants.EMPTY_STR.equals(sprintField.getValue())
				|| jiraIssue.getSprintAssetState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_FUTURE);
	}

	private void concertJiraIssueHistoryToBacklogHistory(JiraIssueCustomHistory jiraIssueHistory,
			IssueBacklogCustomHistory issueBacklogCustomHistory) {
		issueBacklogCustomHistory.setProjectID(jiraIssueHistory.getProjectID());
		issueBacklogCustomHistory.setStoryID(jiraIssueHistory.getStoryID());
		issueBacklogCustomHistory.setStoryType(jiraIssueHistory.getStoryType());
		issueBacklogCustomHistory.setDefectStoryID(jiraIssueHistory.getDefectStoryID());
		issueBacklogCustomHistory.setEstimate(jiraIssueHistory.getEstimate());
		issueBacklogCustomHistory.setBufferedEstimateTime(jiraIssueHistory.getBufferedEstimateTime());
		issueBacklogCustomHistory.setCreatedDate(jiraIssueHistory.getCreatedDate());
		issueBacklogCustomHistory.setDevicePlatform(jiraIssueHistory.getDevicePlatform());
		issueBacklogCustomHistory.setProjectKey(jiraIssueHistory.getProjectKey());
		issueBacklogCustomHistory.setProjectComponentId(jiraIssueHistory.getProjectComponentId());
		issueBacklogCustomHistory.setDeveloperId(jiraIssueHistory.getDeveloperId());
		issueBacklogCustomHistory.setDeveloperName(jiraIssueHistory.getDeveloperName());
		issueBacklogCustomHistory.setQaId(jiraIssueHistory.getQaId());
		issueBacklogCustomHistory.setQaName(jiraIssueHistory.getQaName());
		issueBacklogCustomHistory.setBuildId(jiraIssueHistory.getBuildId());
		issueBacklogCustomHistory.setBuildNumber(jiraIssueHistory.getBuildNumber());
		issueBacklogCustomHistory.setProjectName(jiraIssueHistory.getProjectName());
		issueBacklogCustomHistory.setBasicProjectConfigId(jiraIssueHistory.getBasicProjectConfigId());
		issueBacklogCustomHistory.setStatusUpdationLog(jiraIssueHistory.getStatusUpdationLog());
		issueBacklogCustomHistory.setAssigneeUpdationLog(jiraIssueHistory.getAssigneeUpdationLog());
		issueBacklogCustomHistory.setPriorityUpdationLog(jiraIssueHistory.getPriorityUpdationLog());
		issueBacklogCustomHistory.setFixVersionUpdationLog(jiraIssueHistory.getFixVersionUpdationLog());
		issueBacklogCustomHistory.setLabelUpdationLog(jiraIssueHistory.getLabelUpdationLog());
		issueBacklogCustomHistory.setDueDateUpdationLog(jiraIssueHistory.getDueDateUpdationLog());
		issueBacklogCustomHistory.setSprintUpdationLog(jiraIssueHistory.getSprintUpdationLog());
		issueBacklogCustomHistory.setAdditionalFilters(jiraIssueHistory.getAdditionalFilters());
		issueBacklogCustomHistory.setUrl(jiraIssueHistory.getUrl());
		issueBacklogCustomHistory.setDescription(jiraIssueHistory.getDescription());
	}

	private void concertJiraIssueToBacklog(JiraIssue jiraIssue, IssueBacklog issueBacklog) {
		issueBacklog.setProcessorId(jiraIssue.getProcessorId());
		issueBacklog.setIssueId(jiraIssue.getIssueId());
		issueBacklog.setNumber(jiraIssue.getNumber());
		issueBacklog.setName(jiraIssue.getName());
		issueBacklog.setTypeId(jiraIssue.getTypeId());
		issueBacklog.setTypeName(jiraIssue.getTypeName());
		issueBacklog.setStatus(jiraIssue.getStatus());
		issueBacklog.setState(jiraIssue.getState());
		issueBacklog.setEstimate(jiraIssue.getEstimate());
		issueBacklog.setStoryPoints(jiraIssue.getStoryPoints());
		issueBacklog.setEstimateTime(jiraIssue.getEstimateTime());
		issueBacklog.setUrl(jiraIssue.getUrl());
		issueBacklog.setChangeDate(jiraIssue.getChangeDate());
		issueBacklog.setIsDeleted(jiraIssue.getIsDeleted());
		issueBacklog.setPriority(jiraIssue.getPriority());
		issueBacklog.setCount(jiraIssue.getCount());
		issueBacklog.setLabels(jiraIssue.getLabels());
		issueBacklog.setCreatedDate(jiraIssue.getCreatedDate());
		issueBacklog.setDueDate(jiraIssue.getDueDate());
		issueBacklog.setEnvImpacted(jiraIssue.getEnvImpacted());
		issueBacklog.setBuildNumber(jiraIssue.getBuildNumber());
		issueBacklog.setRootCauseList(jiraIssue.getRootCauseList());
		issueBacklog.setOwnersID(jiraIssue.getOwnersID());
		issueBacklog.setOwnersIsDeleted(jiraIssue.getOwnersIsDeleted());
		issueBacklog.setOwnersChangeDate(jiraIssue.getOwnersChangeDate());
		issueBacklog.setOwnersState(jiraIssue.getOwnersState());
		issueBacklog.setOwnersUsername(jiraIssue.getOwnersUsername());
		issueBacklog.setOwnersFullName(jiraIssue.getOwnersFullName());
		issueBacklog.setOwnersShortName(jiraIssue.getOwnersShortName());
		issueBacklog.setTeamIsDeleted(jiraIssue.getTeamIsDeleted());
		issueBacklog.setTeamAssetState(jiraIssue.getTeamAssetState());
		issueBacklog.setTeamChangeDate(jiraIssue.getTeamChangeDate());
		issueBacklog.setTeamName(jiraIssue.getTeamName());
		issueBacklog.setSprintIsDeleted(jiraIssue.getSprintIsDeleted());
		issueBacklog.setTestAutomated(jiraIssue.getTestAutomated());
		issueBacklog.setIsTestAutomated(jiraIssue.getIsTestAutomated());
		issueBacklog.setIsTestCanBeAutomated(jiraIssue.getIsTestCanBeAutomated());
		issueBacklog.setTestAutomatedDate(jiraIssue.getTestAutomatedDate());
		issueBacklog.setSprintChangeDate(jiraIssue.getSprintChangeDate());
		issueBacklog.setSprintAssetState(jiraIssue.getSprintAssetState());
		issueBacklog.setSprintEndDate(jiraIssue.getSprintEndDate());
		issueBacklog.setSprintBeginDate(jiraIssue.getSprintBeginDate());
		issueBacklog.setSprintName(jiraIssue.getSprintName());
		issueBacklog.setSprintID(jiraIssue.getSprintID());
		issueBacklog.setSprintUrl(jiraIssue.getSprintUrl());
		issueBacklog.setSprintIdList(jiraIssue.getSprintIdList());
		issueBacklog.setEpicIsDeleted(jiraIssue.getEpicIsDeleted());
		issueBacklog.setEpicChangeDate(jiraIssue.getEpicChangeDate());
		issueBacklog.setEpicAssetState(jiraIssue.getEpicAssetState());
		issueBacklog.setEpicType(jiraIssue.getEpicType());
		issueBacklog.setEpicEndDate(jiraIssue.getEpicEndDate());
		issueBacklog.setEpicBeginDate(jiraIssue.getEpicBeginDate());
		issueBacklog.setEpicName(jiraIssue.getEpicName());
		issueBacklog.setEpicUrl(jiraIssue.getEpicUrl());
		issueBacklog.setEpicNumber(jiraIssue.getEpicNumber());
		issueBacklog.setEpicID(jiraIssue.getEpicID());
		issueBacklog.setReopeningCounter(jiraIssue.getReopeningCounter());
		issueBacklog.setCostOfDelay(jiraIssue.getCostOfDelay());
		issueBacklog.setJobSize(jiraIssue.getJobSize());
		issueBacklog.setWsjf(jiraIssue.getWsjf());
		issueBacklog.setBusinessValue(jiraIssue.getBusinessValue());
		issueBacklog.setTimeCriticality(jiraIssue.getTimeCriticality());
		issueBacklog.setRiskReduction(jiraIssue.getRiskReduction());
		issueBacklog.setProjectPath(jiraIssue.getProjectPath());
		issueBacklog.setProjectIsDeleted(jiraIssue.getProjectIsDeleted());
		issueBacklog.setProjectState(jiraIssue.getProjectState());
		issueBacklog.setProjectChangeDate(jiraIssue.getProjectChangeDate());
		issueBacklog.setProjectEndDate(jiraIssue.getProjectEndDate());
		issueBacklog.setProjectBeginDate(jiraIssue.getProjectBeginDate());
		issueBacklog.setProjectName(jiraIssue.getProjectName());
		issueBacklog.setProjectID(jiraIssue.getProjectID());
		issueBacklog.setProjectKey(jiraIssue.getProjectKey());
		issueBacklog.setJiraProjectName(jiraIssue.getJiraProjectName());
		issueBacklog.setBufferedEstimateTime(jiraIssue.getBufferedEstimateTime());
		issueBacklog.setResolution(jiraIssue.getResolution());
		issueBacklog.setAffectedVersions(jiraIssue.getAffectedVersions());
		issueBacklog.setWorkStreamID(jiraIssue.getWorkStreamID());
		issueBacklog.setWorkStream(jiraIssue.getWorkStream());
		issueBacklog.setAdditionalFilters(jiraIssue.getAdditionalFilters());
		issueBacklog.setRelease(jiraIssue.getRelease());
		issueBacklog.setReleaseId(jiraIssue.getReleaseId());
		issueBacklog.setReleaseDate(jiraIssue.getReleaseDate());
		issueBacklog.setAssigneeId(jiraIssue.getAssigneeId());
		issueBacklog.setAssigneeName(jiraIssue.getAssigneeName());
		issueBacklog.setDeveloperId(jiraIssue.getDeveloperId());
		issueBacklog.setDeveloperName(jiraIssue.getDeveloperName());
		issueBacklog.setQaId(jiraIssue.getQaId());
		issueBacklog.setQaName(jiraIssue.getQaName());
		issueBacklog.setAssignAttributeValue(jiraIssue.getAssignAttributeValue());
		issueBacklog.setTeamNameValue(jiraIssue.getTeamNameValue());
		issueBacklog.setStoryDemonstratedFieldValue(jiraIssue.getStoryDemonstratedFieldValue());
		issueBacklog.setStoryDemonstratedFieldValueDate(jiraIssue.getStoryDemonstratedFieldValueDate());
		issueBacklog.setDevicePlatform(jiraIssue.getDevicePlatform());
		issueBacklog.setDefectRaisedBy(jiraIssue.getDefectRaisedBy());
		issueBacklog.setJiraStatus(jiraIssue.getJiraStatus());
		issueBacklog.setDefectStoryID(jiraIssue.getDefectStoryID());
		issueBacklog.setSpeedyIssueType(jiraIssue.getSpeedyIssueType());
		issueBacklog.setTimeSpentInMinutes(jiraIssue.getTimeSpentInMinutes());
		issueBacklog.setBasicProjectConfigId(jiraIssue.getBasicProjectConfigId());
		issueBacklog.setTestCaseFolderName(jiraIssue.getTestCaseFolderName());
		issueBacklog.setReleaseVersions(jiraIssue.getReleaseVersions());
		issueBacklog.setDefectRaisedByQA(jiraIssue.isDefectRaisedByQA());
		issueBacklog.setOriginalEstimateMinutes(jiraIssue.getOriginalEstimateMinutes());
		issueBacklog.setRemainingEstimateMinutes(jiraIssue.getRemainingEstimateMinutes());
		issueBacklog.setProductionDefect(jiraIssue.isProductionDefect());
		issueBacklog.setAggregateTimeOriginalEstimateMinutes(jiraIssue.getAggregateTimeOriginalEstimateMinutes());
		issueBacklog.setAggregateTimeRemainingEstimateMinutes(jiraIssue.getAggregateTimeRemainingEstimateMinutes());
		issueBacklog.setUpdateDate(jiraIssue.getUpdateDate());
		issueBacklog.setDevDueDate(jiraIssue.getDevDueDate());

	}

}
