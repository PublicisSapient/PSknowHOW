package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
@Service
public class BacklogService {
	
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;
	
	public List<JiraIssue> getBackLogStory(ObjectId basicProjectId) {
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		basicProjectConfigIds.add(basicProjectId.toString());

		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectId);
		
		mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(), fieldMapping.getJiraStoryIdentification());

		List<String> statusList = new ArrayList<>();
		if (Optional.ofNullable(fieldMapping.getReadyForDevelopmentStatus()).isPresent()) {
			statusList.add(fieldMapping.getReadyForDevelopmentStatus());
		}
		mapOfFilters.put(JiraFeature.JIRA_ISSUE_STATUS.getFieldValueInFeature(), statusList);//coomonutil
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
		
		mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(), Lists.newArrayList("",null));

		return jiraIssueRepository.findIssuesByType(mapOfFilters);
	}

}
