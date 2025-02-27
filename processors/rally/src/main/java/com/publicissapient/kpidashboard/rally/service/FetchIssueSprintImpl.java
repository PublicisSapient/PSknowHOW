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
package com.publicissapient.kpidashboard.rally.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.publicissapient.kpidashboard.rally.config.RallyProcessorConfig;
import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.Iteration;
import com.publicissapient.kpidashboard.rally.model.IterationResponse;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.model.RallyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.processortool.service.ProcessorToolConnectionService;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class FetchIssueSprintImpl implements FetchIssueSprint {

	public static final String PROCESSING_ISSUES_PRINT_LOG = "Processing issues %d - %d out of %d";
	public static final String TILDA_SYMBOL = "^";
	public static final String DOLLAR_SYMBOL = "$";
	private static final String MSG_JIRA_CLIENT_SETUP_FAILED = "Jira client setup failed. No results obtained. Check your jira setup.";
	private static final String RALLY_URL = "https://rally1.rallydev.com/slm/webservice/v2.0";
	private static final String API_KEY = "_8BogJQcTuGwVjEemJiAjV0z5SgR2UCSsSnBUu55Y5U";
	private static final String PROJECT_NAME = "Core Team";
	private static final int PAGE_SIZE = 200; // Number of artifacts per page

	@Autowired
	RallyProcessorConfig rallyProcessorConfig;
	@Autowired
	private ProcessorToolConnectionService processorToolConnectionService;
	@Autowired
	SprintRepository sprintRepository;

	@Autowired
	JiraIssueRepository jiraIssueRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public List<HierarchicalRequirement> fetchIssuesSprintBasedOnJql(ProjectConfFieldMapping projectConfig,
			int pageNumber, String sprintId) throws InterruptedException {

		SprintDetails updatedSprintDetails = sprintRepository.findBySprintID(sprintId);

		// collecting the jiraIssue & history of to be updated
		Set<String> issuesToUpdate = Optional.ofNullable(updatedSprintDetails.getTotalIssues()).map(Collection::stream)
				.orElse(Stream.empty()).map(SprintIssue::getNumber).collect(Collectors.toSet());

		issuesToUpdate.addAll(Optional.ofNullable(updatedSprintDetails.getPuntedIssues()).map(Collection::stream)
				.orElse(Stream.empty()).map(SprintIssue::getNumber).collect(Collectors.toSet()));

		issuesToUpdate.addAll(
				Optional.ofNullable(updatedSprintDetails.getCompletedIssuesAnotherSprint()).map(Collection::stream)
						.orElse(Stream.empty()).map(SprintIssue::getNumber).collect(Collectors.toSet()));

		FieldMapping fieldMapping = projectConfig.getFieldMapping();

		// checking if subtask is configured as bug
		getSubTaskAsBug(fieldMapping, updatedSprintDetails, issuesToUpdate);
		return getHierarchicalRequirements(pageNumber);
	}

	private void getSubTaskAsBug(FieldMapping fieldMapping, SprintDetails updatedSprintDetails,
			Set<String> issuesToUpdate) {
		if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(updatedSprintDetails.getTotalIssues())) {
			List<String> defectTypes = Optional.ofNullable(fieldMapping).map(FieldMapping::getJiradefecttype)
					.orElse(Collections.emptyList());
			Set<String> totalSprintReportDefects = new HashSet<>();
			Set<String> totalSprintReportStories = new HashSet<>();

			updatedSprintDetails.getTotalIssues().stream().forEach(sprintIssue -> {
				if (defectTypes.contains(sprintIssue.getTypeName())) {
					totalSprintReportDefects.add(sprintIssue.getNumber());
				} else {
					totalSprintReportStories.add(sprintIssue.getNumber());
				}
			});
			List<String> defectType = new ArrayList<>();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
			Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
			String basicProjConfigId = updatedSprintDetails.getBasicProjectConfigId().toString();

			defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
			mapOfProjectFilters.put("typeName", convertToPatternList(defectType));
			uniqueProjectMap.put(basicProjConfigId, mapOfProjectFilters);
			mapOfFilters.put("basicProjectConfigId", Collections.singletonList(basicProjConfigId));

			// fetched all defects which is linked to current sprint report stories
			List<JiraIssue> linkedDefects = jiraIssueRepository.findLinkedDefects(mapOfFilters,
					totalSprintReportStories, uniqueProjectMap);

			// filter defects which is issue type not coming in sprint report
			List<JiraIssue> subTaskDefects = linkedDefects.stream()
					.filter(jiraIssue -> !totalSprintReportDefects.contains(jiraIssue.getNumber()))
					.collect(Collectors.toList());
			Set<String> subTaskDefectsKey = subTaskDefects.stream().map(JiraIssue::getNumber)
					.collect(Collectors.toSet());
			issuesToUpdate.addAll(subTaskDefectsKey);
		}
	}

	public List<Pattern> convertToPatternList(List<String> stringList) {
		List<Pattern> regexList = new ArrayList<>();
		if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(stringList)) {
			for (String value : stringList) {
				regexList.add(
						Pattern.compile(TILDA_SYMBOL + Pattern.quote(value) + DOLLAR_SYMBOL, Pattern.CASE_INSENSITIVE));
			}
		}
		return regexList;
	}
	private List<HierarchicalRequirement> getHierarchicalRequirements(int pageStart) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("ZSESSIONID", API_KEY);
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// List of artifact types to query
		List<String> artifactTypes = Arrays.asList("hierarchicalrequirement", "defect", "task");

		// Fetch fields for each artifact type
		String fetchFields = "FormattedID,Name,Owner,PlanEstimate,ScheduleState,Iteration,CreationDate,LastUpdateDate";
		List<HierarchicalRequirement> allArtifacts = new ArrayList<>();

		// Query each artifact type
		for (String artifactType : artifactTypes) {
			int start = pageStart; // Start index for pagination
			boolean hasMoreResults = true;

			while (hasMoreResults) {
				String url = String.format("%s/%s?query = (Project.Name = \"%s\")&fetch=%s&start=%d&pagesize=%d",
						RALLY_URL, artifactType, PROJECT_NAME, fetchFields, start, PAGE_SIZE);
				ResponseEntity<RallyResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity,
						RallyResponse.class);

				if (response.getStatusCode() == HttpStatus.OK) {
					RallyResponse responseBody = response.getBody();
					if (responseBody != null && responseBody.getQueryResult() != null) {
						List<HierarchicalRequirement> artifacts = responseBody.getQueryResult().getResults();
						if (artifacts != null && !artifacts.isEmpty()) {
							for (HierarchicalRequirement artifact : artifacts) {
								// Fetch full iteration details if it exists
								if (artifact.getIteration() != null && artifact.getIteration().getRef() != null) {
									artifact.setIteration(fetchIterationDetails(artifact.getIteration().getRef(), entity));
								}
								allArtifacts.add(artifact);
							}
							start += PAGE_SIZE; // Move to the next page
						} else {
							hasMoreResults = false;
						}
					} else {
						hasMoreResults = false; // No response body
					}
				} else {
					log.error("Failed to fetch data for {}: {}", artifactType, response.getStatusCode());
					hasMoreResults = false; // Stop on error
				}
			}
		}
		return allArtifacts;
	}
	private Iteration fetchIterationDetails(String iterationUrl, HttpEntity<String> entity) {
		try {
			ResponseEntity<IterationResponse> response = restTemplate.exchange(iterationUrl, HttpMethod.GET, entity, IterationResponse.class);

			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().getIteration() != null) {
				Iteration iteration = response.getBody().getIteration();
				log.info("Fetched Iteration: {}", iteration.getName());
				return iteration;
			} else {
				log.warn("Iteration details not found in response for URL: {}", iterationUrl);
			}
		} catch (RestClientException e) {
			log.error("Failed to fetch iteration details from URL: {}. Error: {}", iterationUrl, e.getMessage(), e);
		}
		// Return an empty Iteration object instead of null
		return new Iteration();
	}
}
