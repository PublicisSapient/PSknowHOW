package com.publicissapient.kpidashboard.apis.projectdata.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.projectdata.service.ProjectDataService;
import com.publicissapient.kpidashboard.common.model.application.ProjectReleaseV2;
import com.publicissapient.kpidashboard.common.model.jira.DataRequest;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueV2;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetailsV2;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseV2Repo;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueV2Repository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintV2Repository;

@Service
public class ProjectDataServiceImpl implements ProjectDataService {

	@Autowired
	JiraIssueV2Repository jiraIssueV2Repository;
	@Autowired
	SprintV2Repository sprintV2Repository;
	@Autowired
	ProjectReleaseV2Repo projectReleaseV2Repo;

	@Override
	public List<JiraIssueV2> getProjectJiraIssues(DataRequest dataRequest) {
		List<JiraIssueV2> jiraIssueV2List = new ArrayList<>();
		if (dataRequest.getBoardId() != null && dataRequest.getProjectId() != null) {
			jiraIssueV2List.addAll(jiraIssueV2Repository
					.findByBasicProjectConfigIdAndBoardId(dataRequest.getProjectId(), dataRequest.getBoardId()));
		}
		if (CollectionUtils.isNotEmpty(dataRequest.getIssueIds()) && dataRequest.getProjectId() != null) {
			jiraIssueV2List.addAll(jiraIssueV2Repository
					.findByBasicProjectConfigIdAndIssueIdIn(dataRequest.getProjectId(), dataRequest.getIssueIds()));
		}
		if (dataRequest.getProjectId() != null && dataRequest.getBoardId() != null
				&& CollectionUtils.isNotEmpty(dataRequest.getSprintIds())) {
			jiraIssueV2List.addAll(jiraIssueV2Repository.findByProjectIdAndBoardIdAndSprintIdIn(
					dataRequest.getProjectId(), dataRequest.getBoardId(), dataRequest.getSprintIds()));
		}
		if (dataRequest.getProjectKey() != null) {
			jiraIssueV2List.addAll(jiraIssueV2Repository.findByProjectKey(dataRequest.getProjectKey()));
		}
		return jiraIssueV2List;
	}

	@Override
	public List<String> getIssueTypes(DataRequest dataRequest) {
		if (dataRequest.getBoardId() != null)
			return jiraIssueV2Repository.findIssueTypesByBoardId(dataRequest.getBoardId()).stream()
					.map(JiraIssueV2::getIssueType).distinct().collect(Collectors.toList());
		if (dataRequest.getProjectKey() != null)
			return jiraIssueV2Repository.findIssueTypesByProjectKey(dataRequest.getProjectKey()).stream()
					.map(JiraIssueV2::getIssueType).distinct().collect(Collectors.toList());

		return Collections.emptyList();
	}

	@Override
	public List<SprintDetailsV2> getProjectSprints(DataRequest dataRequest) {
		if (dataRequest.getProjectId() != null) {
			return sprintV2Repository.findByBasicProjectConfigId(new ObjectId(dataRequest.getProjectId()));
		}
		return List.of();
	}

	@Override
	public ProjectReleaseV2 getProjectReleases(DataRequest dataRequest) {
		if (dataRequest.getProjectId() != null) {
			return projectReleaseV2Repo.findByConfigId(new ObjectId(dataRequest.getProjectId()));
		}
		return null;
	}
}
