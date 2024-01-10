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
package com.publicissapient.kpidashboard.jira.service;

import static com.publicissapient.kpidashboard.jira.constant.JiraConstants.ERROR_MSG_401;
import static com.publicissapient.kpidashboard.jira.constant.JiraConstants.ERROR_MSG_NO_RESULT_WAS_AVAILABLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.helper.JiraHelper;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import io.atlassian.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FetchIssueSprintImpl implements FetchIssueSprint {

	public static final String PROCESSING_ISSUES_PRINT_LOG = "Processing issues %d - %d out of %d";
	public static final String TILDA_SYMBOL = "^";
	public static final String DOLLAR_SYMBOL = "$";
	private static final String MSG_JIRA_CLIENT_SETUP_FAILED = "Jira client setup failed. No results obtained. Check your jira setup.";

	@Autowired
	JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	SprintRepository sprintRepository;

	@Autowired
	JiraIssueRepository jiraIssueRepository;

	@Override
	public List<Issue> fetchIssuesSprintBasedOnJql(ProjectConfFieldMapping projectConfig,
			ProcessorJiraRestClient client, int pageNumber, String sprintId) throws InterruptedException {

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
		List<Issue> issues = new ArrayList<>();
		if (client == null) {
			log.error(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else {
			if (CollectionUtils.isNotEmpty(issuesToUpdate)) {
				SearchResult searchResult = getIssuesSprint(projectConfig, client, pageNumber,
						new ArrayList<>(issuesToUpdate));
				issues = JiraHelper.getIssuesFromResult(searchResult);
			} else {
				log.info("No issuesToUpdate found in Sprint {}", updatedSprintDetails.getSprintName());
			}
		}
		return issues;
	}

	public SearchResult getIssuesSprint(ProjectConfFieldMapping projectConfig, ProcessorJiraRestClient client,
			int pageStart, List<String> issueKeys) throws InterruptedException {
		SearchResult searchResult = null;

		if (client == null) {
			log.warn(MSG_JIRA_CLIENT_SETUP_FAILED);
		} else if (org.apache.commons.lang3.StringUtils.isEmpty(projectConfig.getProjectToolConfig().getProjectKey())) {
			log.info("Project key is empty {}", projectConfig.getProjectToolConfig().getProjectKey());
		} else {
			try {
				StringBuilder query = new StringBuilder("project in (")
						.append(projectConfig.getProjectToolConfig().getProjectKey()).append(") AND ");

				query.append(JiraProcessorUtil.processJqlForSprintFetch(issueKeys));
				if (StringUtils.isNotEmpty(projectConfig.getProjectToolConfig().getBoardQuery())) {
					query.append(" and (").append(
							projectConfig.getJira().getBoardQuery().toLowerCase().split(JiraConstants.ORDERBY)[0])
							.append(")");
				}
				log.info("jql query :{}", query);
				Promise<SearchResult> promisedRs = client.getProcessorSearchClient().searchJql(query.toString(),
						jiraProcessorConfig.getPageSize(), pageStart, JiraConstants.ISSUE_FIELD_SET);
				searchResult = promisedRs.claim();
				if (searchResult != null) {
					log.info(String.format(PROCESSING_ISSUES_PRINT_LOG, pageStart,
							Math.min(pageStart + jiraProcessorConfig.getPageSize() - 1, searchResult.getTotal()),
							searchResult.getTotal()));
				}
				TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
			} catch (RestClientException e) {
				if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 401) {
					log.error(ERROR_MSG_401);
				} else {
					log.error(ERROR_MSG_NO_RESULT_WAS_AVAILABLE, e);
				}
				throw e;
			}

		}

		return searchResult;
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

}
