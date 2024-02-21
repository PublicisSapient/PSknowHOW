package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for the sprint velocity calculation
 * 
 * @author dhachuda
 *
 */
@Slf4j
@Service
public class SprintVelocityServiceHelper {

	/**
	 * Fetches the issues for each sprint
	 * 
	 * @param allJiraIssue
	 * @param sprintDetails
	 * @param currentSprintLeafVelocityMap
	 */
	public void getSprintIssuesForProject(List<JiraIssue> allJiraIssue, List<SprintDetails> sprintDetails,
			Map<Pair<String, String>, Set<JiraIssue>> currentSprintLeafVelocityMap) {
		if (CollectionUtils.isNotEmpty(sprintDetails)) {
			sprintDetails.stream().filter(sd -> CollectionUtils.isNotEmpty(sd.getCompletedIssues())).forEach(sd -> {
				Set<JiraIssue> filteredJiraIssuesListBasedOnTypeFromSprintDetails = KpiDataHelper
						.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sd, sd.getCompletedIssues(),
								allJiraIssue);

				Pair<String, String> currentNodeIdentifier = Pair.of(sd.getBasicProjectConfigId().toString(),
						sd.getSprintID());
				log.debug("Issue count for the sprint {} is {}", sd.getSprintID(),
						filteredJiraIssuesListBasedOnTypeFromSprintDetails.size());
				currentSprintLeafVelocityMap.put(currentNodeIdentifier,
						filteredJiraIssuesListBasedOnTypeFromSprintDetails);
			});
		}
	}

	/**
	 * Calculates the velocity for each sprint
	 * 
	 * @param currentSprintLeafVelocityMap
	 * @param currentNodeIdentifier
	 * @param fieldMapping
	 * @return
	 */
	public double calculateSprintVelocityValue(Map<Pair<String, String>, Set<JiraIssue>> currentSprintLeafVelocityMap,
			Pair<String, String> currentNodeIdentifier, FieldMapping fieldMapping) {
		double sprintVelocityForCurrentLeaf = 0.0d;
		if (Objects.nonNull(currentSprintLeafVelocityMap.get(currentNodeIdentifier))) {
			log.debug("Current Node identifier is present in currentSprintLeafVelocityMap map {} ",
					currentNodeIdentifier);
			Set<JiraIssue> issueDetailsSet = currentSprintLeafVelocityMap.get(currentNodeIdentifier);
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				sprintVelocityForCurrentLeaf = issueDetailsSet.stream()
						.mapToDouble(ji -> Optional.ofNullable(ji.getStoryPoints()).orElse(0.0d)).sum();
			} else {
				double totalOriginalEstimate = issueDetailsSet.stream()
						.filter(jiraIssue -> Objects.nonNull(jiraIssue.getAggregateTimeOriginalEstimateMinutes()))
						.mapToDouble(JiraIssue::getAggregateTimeOriginalEstimateMinutes).sum();
				double inHours = totalOriginalEstimate / 60;
				sprintVelocityForCurrentLeaf = inHours / fieldMapping.getStoryPointToHourMapping();

			}
		}
		log.debug("Sprint velocity for the sprint {} is {}", currentNodeIdentifier.getValue(),
				sprintVelocityForCurrentLeaf);
		return sprintVelocityForCurrentLeaf;
	}

}