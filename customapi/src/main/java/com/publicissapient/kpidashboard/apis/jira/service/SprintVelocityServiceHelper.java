package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IssueDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

/**
 * Helper class for the sprint velocity calculation
 * 
 * @author dhachuda
 *
 */
@Service
public class SprintVelocityServiceHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(SprintVelocityServiceHelper.class);

	/**
	 * Fetches the issues for each sprint
	 * 
	 * @param allJiraIssue
	 * @param sprintWiseIssues
	 * @param sprintDetails
	 * @param currentSprintLeafVelocityMap
	 */
	public void getSprintIssuesForProject(List<JiraIssue> allJiraIssue,
			Map<Pair<String, String>, List<JiraIssue>> sprintWiseIssues, List<SprintDetails> sprintDetails,
			Map<Pair<String, String>, Set<IssueDetails>> currentSprintLeafVelocityMap) {
		if (CollectionUtils.isNotEmpty(sprintDetails)) {
			Map<String, JiraIssue> jiraIssueMap = new HashMap<>();
			if (CollectionUtils.isNotEmpty(allJiraIssue))
			allJiraIssue.forEach(jiraIssue -> jiraIssueMap.put(jiraIssue.getNumber(), jiraIssue));

			sprintDetails.stream()
					.filter(sd -> CollectionUtils.isNotEmpty(sd.getCompletedIssues()))
					.forEach(sd -> {
						Set<IssueDetails> filterIssueDetailsSet = new HashSet<>();
						sd.getCompletedIssues().forEach(sprintIssue -> {
							JiraIssue jiraIssue = jiraIssueMap.get(sprintIssue.getNumber());

								IssueDetails issueDetails = new IssueDetails();
								issueDetails.setSprintIssue(sprintIssue);
							if (jiraIssue != null) {
								issueDetails.setUrl(jiraIssue.getUrl());
								issueDetails.setDesc(jiraIssue.getName());
							}
								filterIssueDetailsSet.add(issueDetails);

						});

						Pair<String, String> currentNodeIdentifier = Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID());
						LOGGER.debug("Issue count for the sprint {} is {}", sd.getSprintID(), filterIssueDetailsSet.size());
						currentSprintLeafVelocityMap.put(currentNodeIdentifier, new HashSet<>(filterIssueDetailsSet));
					});
		} else {
			if (CollectionUtils.isNotEmpty(allJiraIssue)) {
				// start : for azure board sprint details collections empty so
				// that we have to
				// prepare data from jira issue
				Map<String, List<JiraIssue>> projectWiseJiraIssues = allJiraIssue.stream()
						.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));
				projectWiseJiraIssues.forEach((basicProjectConfigId, projectWiseIssuesList) -> {
					Map<String, List<JiraIssue>> sprintWiseJiraIssues = projectWiseIssuesList.stream()
							.filter(jiraIssue -> Objects.nonNull(jiraIssue.getSprintID()))
							.collect(Collectors.groupingBy(JiraIssue::getSprintID));
					sprintWiseJiraIssues.forEach((sprintId, sprintWiseIssuesList) -> sprintWiseIssues
							.put(Pair.of(basicProjectConfigId, sprintId), sprintWiseIssuesList));
				});
			}
			// end : for azure board sprint details collections empty so that we
			// have to
			// prepare data from jira issue.
		}
	}

	/**
	 * Calculates the velocity for each sprint
	 * 
	 * @param currentSprintLeafVelocityMap
	 * @param currentNodeIdentifier
	 * @param sprintJiraIssues
	 * @param fieldMapping
	 * @return
	 */
	public double calculateSprintVelocityValue(
			Map<Pair<String, String>, Set<IssueDetails>> currentSprintLeafVelocityMap,
			Pair<String, String> currentNodeIdentifier, Map<Pair<String, String>, List<JiraIssue>> sprintJiraIssues,
			FieldMapping fieldMapping) {
		double sprintVelocityForCurrentLeaf = 0.0d;
		if (CollectionUtils.isNotEmpty(sprintJiraIssues.get(currentNodeIdentifier))) {
			LOGGER.debug("Current Node identifier is present in sprintjirsissues map {} ", currentNodeIdentifier);
			List<JiraIssue> jiraIssueList = sprintJiraIssues.get(currentNodeIdentifier);
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
					&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				sprintVelocityForCurrentLeaf = jiraIssueList.stream()
						.mapToDouble(ji -> Double.valueOf(ji.getEstimate())).sum();
			} else {
				double totalOriginalEstimate = jiraIssueList.stream()
						.filter(jiraIssue -> Objects.nonNull(jiraIssue.getOriginalEstimateMinutes()))
						.mapToDouble(JiraIssue::getOriginalEstimateMinutes).sum();
				double totalOriginalEstimateInHours = totalOriginalEstimate / 60;
				sprintVelocityForCurrentLeaf = totalOriginalEstimateInHours / 60;
			}
		} else {
			if (Objects.nonNull(currentSprintLeafVelocityMap.get(currentNodeIdentifier))) {
				LOGGER.debug("Current Node identifier is present in currentSprintLeafVelocityMap map {} ",
						currentNodeIdentifier);
				Set<IssueDetails> issueDetailsSet = currentSprintLeafVelocityMap.get(currentNodeIdentifier);
				if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					sprintVelocityForCurrentLeaf = issueDetailsSet.stream()
							.filter(issueDetails -> Objects.nonNull(issueDetails.getSprintIssue().getStoryPoints()))
							.mapToDouble(issueDetails -> issueDetails.getSprintIssue().getStoryPoints()).sum();
				} else {
					double totalOriginalEstimate = issueDetailsSet.stream().filter(
							issueDetails -> Objects.nonNull(issueDetails.getSprintIssue().getOriginalEstimate()))
							.mapToDouble(issueDetails -> issueDetails.getSprintIssue().getOriginalEstimate()).sum();
					double totalOriginalEstimateInHours = totalOriginalEstimate / 60;
					sprintVelocityForCurrentLeaf = totalOriginalEstimateInHours / 60;
				}
			}
		}
		LOGGER.debug("Sprint velocity for the sprint {} is {}", currentNodeIdentifier.getValue(),
				sprintVelocityForCurrentLeaf);
		return sprintVelocityForCurrentLeaf;
	}

}