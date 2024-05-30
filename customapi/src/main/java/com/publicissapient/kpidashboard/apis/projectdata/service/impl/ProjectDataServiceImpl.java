package com.publicissapient.kpidashboard.apis.projectdata.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.projectdata.service.ProjectDataService;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectReleaseV2;
import com.publicissapient.kpidashboard.common.model.jira.DataRequest;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueV2;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetailsV2;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
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
	@Autowired
	ProjectBasicConfigRepository projectBasicConfigRepository;

	@Override
	public ServiceResponse getProjectJiraIssues(DataRequest dataRequest) {
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
		if (CollectionUtils.isEmpty(jiraIssueV2List)) {
			return new ServiceResponse(true, "No Jira issues were found for the provided project details.", null);
		} else {
			return new ServiceResponse(true, "Scrum project issue details fetched successfully", jiraIssueV2List);
		}
	}

	@Override
	public ServiceResponse getIssueTypes(DataRequest dataRequest) {
		if (dataRequest.getBoardId() != null) {
			List<String> issueTypesBasedOnBoardId = jiraIssueV2Repository
					.findIssueTypesByBoardId(dataRequest.getBoardId()).stream().map(JiraIssueV2::getIssueType)
					.distinct().toList();
			if (CollectionUtils.isEmpty(issueTypesBasedOnBoardId)) {
				return new ServiceResponse(true, "No issue types was found for the provided 'boardId'.", null);
			} else {
				return new ServiceResponse(true,
						"Issue types have been successfully retrieved using the provided 'boardId'.",
						issueTypesBasedOnBoardId);
			}
		}
		if (dataRequest.getProjectKey() != null) {
			List<String> issueTypesBasedOnProjectKey = jiraIssueV2Repository
					.findIssueTypesByProjectKey(dataRequest.getProjectKey()).stream().map(JiraIssueV2::getIssueType)
					.distinct().toList();
			if (CollectionUtils.isEmpty(issueTypesBasedOnProjectKey)) {
				return new ServiceResponse(true, "No issue types was found for the provided 'projectKey'.", null);
			} else {
				return new ServiceResponse(true,
						"Issue types have been successfully retrieved using the provided 'projectKey'.",
						issueTypesBasedOnProjectKey);
			}
		}
		return new ServiceResponse(true,
				"Please provide either a 'boardId' or a 'projectKey' in the request body to retrieve the issue types.",
				null);
	}

	@Override
	public ServiceResponse getProjectSprints(DataRequest dataRequest) {
		if (dataRequest.getProjectId() != null) {
			List<SprintDetailsV2> projectSprintDetails = sprintV2Repository
					.findByBasicProjectConfigId(new ObjectId(dataRequest.getProjectId()));
			if (CollectionUtils.isEmpty(projectSprintDetails)) {
				return new ServiceResponse(true, "No sprint data was found for the provided 'projectId'.", null);
			} else {
				return new ServiceResponse(true,
						"The sprint details for the Scrum project have been successfully retrieved.",
						projectSprintDetails);
			}
		} else {
			return new ServiceResponse(true,
					"Please include the knowHOW 'projectId' in the request body to retrieve the sprint details for the project.",
					null);
		}
	}

	@Override
	public ServiceResponse getProjectReleases(DataRequest dataRequest) {
		if (dataRequest.getProjectId() != null) {
			ProjectReleaseV2 projectReleaseDetails = projectReleaseV2Repo
					.findByConfigId(new ObjectId(dataRequest.getProjectId()));
			if (projectReleaseDetails == null) {
				return new ServiceResponse(true, "No release data was found for the provided 'projectId'.", null);
			} else {
				return new ServiceResponse(true,
						"The release details for the Scrum project have been successfully retrieved.",
						projectReleaseDetails);
			}
		} else {
			return new ServiceResponse(true,
					"Please include the knowHOW 'projectId' in the request body to retrieve the release details for the project.",
					null);
		}
	}

	@Override
	public ServiceResponse getScrumProjects() {
		List<ProjectBasicConfig> scrumProjectList = projectBasicConfigRepository.findByKanban(false);
		if (CollectionUtils.isEmpty(scrumProjectList)) {
			return new ServiceResponse(true, "No Scrum projects were found.", null);
		} else {
			return new ServiceResponse(true, "The list of Scrum projects has been successfully retrieved.",
					scrumProjectList);
		}
	}
}
