package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.jira.IssueDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
@Service
public class SprintVelocityServiceHelper {

	public void getSprintForProject(List<JiraIssue> allJiraIssue,
			Map<Pair<String, String>, List<JiraIssue>> sprintWiseIssues, List<SprintDetails> sprintDetails,
			Map<Pair<String, String>, Set<IssueDetails>> currentSprintLeafVelocityMap) {
		if (CollectionUtils.isNotEmpty(sprintDetails)) {
			sprintDetails.forEach(sd -> {
				Set<IssueDetails> filterIssueDetailsSet = new HashSet<>();
				if (CollectionUtils.isNotEmpty(sd.getCompletedIssues())) {
					sd.getCompletedIssues().stream().forEach(sprintIssue -> {
						allJiraIssue.stream().forEach(jiraIssue -> {
							if (sprintIssue.getNumber().equals(jiraIssue.getNumber())) {
								IssueDetails issueDetails = new IssueDetails();
								issueDetails.setSprintIssue(sprintIssue);
								issueDetails.setUrl(jiraIssue.getUrl());
								issueDetails.setDesc(jiraIssue.getName());
								filterIssueDetailsSet.add(issueDetails);
							}
						});
						Pair<String, String> currentNodeIdentifier = Pair.of(sd.getBasicProjectConfigId().toString(),
								sd.getSprintID());
						currentSprintLeafVelocityMap.put(currentNodeIdentifier, filterIssueDetailsSet);
					});
				}
			});
		} else {
			if (CollectionUtils.isNotEmpty(allJiraIssue)) {
				// start : for azure board sprint details collections empty so that we have to
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
			// end : for azure board sprint details collections empty so that we have to
			// prepare data from jira issue.
		}
	}
	
	public double calculateSprintVelocityValue(
			Map<Pair<String, String>, Set<IssueDetails>> currentSprintLeafVelocityMap,
			Pair<String, String> currentNodeIdentifier, Map<Pair<String, String>, List<JiraIssue>> sprintJiraIssues) {
		double sprintVelocityForCurrentLeaf = 0.0d;
		if (CollectionUtils.isNotEmpty(sprintJiraIssues.get(currentNodeIdentifier))) {
			List<JiraIssue> jiraIssueList = sprintJiraIssues.get(currentNodeIdentifier);
			sprintVelocityForCurrentLeaf = jiraIssueList.stream().mapToDouble(ji -> Double.valueOf(ji.getEstimate()))
					.sum();
		} else {
			if (Objects.nonNull(currentSprintLeafVelocityMap.get(currentNodeIdentifier))) {
				Set<IssueDetails> issueDetailsSet = currentSprintLeafVelocityMap.get(currentNodeIdentifier);
				for (IssueDetails issueDetails : issueDetailsSet) {
					sprintVelocityForCurrentLeaf = sprintVelocityForCurrentLeaf + Optional
							.ofNullable(issueDetails.getSprintIssue().getStoryPoints()).orElse(0.0d).doubleValue();
				}
			}

		}
		return sprintVelocityForCurrentLeaf;
	}
}
