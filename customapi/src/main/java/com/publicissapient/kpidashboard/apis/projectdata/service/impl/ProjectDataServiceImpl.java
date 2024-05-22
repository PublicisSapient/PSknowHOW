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
import com.publicissapient.kpidashboard.common.model.application.MasterProjectRelease;
import com.publicissapient.kpidashboard.common.model.jira.DataRequest;
import com.publicissapient.kpidashboard.common.model.jira.MasterJiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.MasterSprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.MasterProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.jira.MasterJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.MasterSprintRepository;

@Service
public class ProjectDataServiceImpl implements ProjectDataService {

	@Autowired
	MasterJiraIssueRepository masterJiraIssueRepository;
	@Autowired
	MasterSprintRepository masterSprintRepository;
	@Autowired
	MasterProjectReleaseRepo masterProjectReleaseRepo;

	@Override
	public List<MasterJiraIssue> getProjectJiraIssues(DataRequest dataRequest) {
		List<MasterJiraIssue> masterJiraIssueList = new ArrayList<>();
		if (dataRequest.getBoardId() != null && dataRequest.getProjectId() != null) {
			masterJiraIssueList.addAll(masterJiraIssueRepository
					.findByBasicProjectConfigIdAndBoardId(dataRequest.getProjectId(), dataRequest.getBoardId()));
		}
		if (CollectionUtils.isNotEmpty(dataRequest.getIssueIds()) && dataRequest.getProjectId() != null) {
			masterJiraIssueList.addAll(masterJiraIssueRepository
					.findByBasicProjectConfigIdAndIssueIdIn(dataRequest.getProjectId(), dataRequest.getIssueIds()));
		}
		if (dataRequest.getProjectId() != null && dataRequest.getBoardId() != null
				&& CollectionUtils.isNotEmpty(dataRequest.getSprintIds())) {
			masterJiraIssueList.addAll(masterJiraIssueRepository.findByProjectIdAndBoardIdAndSprintIdIn(
					dataRequest.getProjectId(), dataRequest.getBoardId(), dataRequest.getSprintIds()));
		}
		if (dataRequest.getProjectKey() != null) {
			masterJiraIssueList.addAll(masterJiraIssueRepository.findByProjectKey(dataRequest.getProjectKey()));
		}
		return masterJiraIssueList;
	}

	@Override
	public List<String> getIssueTypes(DataRequest dataRequest) {
		if (dataRequest.getBoardId() != null)
			return masterJiraIssueRepository.findIssueTypesByBoardId(dataRequest.getBoardId()).stream()
					.map(MasterJiraIssue::getIssueType).distinct().collect(Collectors.toList());
		if (dataRequest.getProjectKey() != null)
			return masterJiraIssueRepository.findIssueTypesByProjectKey(dataRequest.getProjectKey()).stream()
					.map(MasterJiraIssue::getIssueType).distinct().collect(Collectors.toList());

		return Collections.emptyList();
	}

	@Override
	public List<MasterSprintDetails> getProjectSprints(DataRequest dataRequest) {
		if (dataRequest.getProjectId() != null) {
			return masterSprintRepository.findByBasicProjectConfigId(new ObjectId(dataRequest.getProjectId()));
		}
		return List.of();
	}

	@Override
	public MasterProjectRelease getProjectReleases(DataRequest dataRequest) {
		if (dataRequest.getProjectId() != null) {
			return masterProjectReleaseRepo.findByConfigId(new ObjectId(dataRequest.getProjectId()));
		}
		return null;
	}
}
