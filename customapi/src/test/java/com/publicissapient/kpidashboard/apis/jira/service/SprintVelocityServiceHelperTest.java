package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.testng.Assert;

import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

/**
 * Test class for @{SprintVelocityServiceHelper}
 *
 * @author dhachuda
 *
 */
public class SprintVelocityServiceHelperTest {

	SprintVelocityServiceHelper sprintVelocityServiceHelper;
	List<SprintDetails> sprintDetails = new ArrayList<>();
	FieldMapping fieldMapping = new FieldMapping();
	private List<JiraIssue> storyList = new ArrayList<>();

	@Before
	public void setUp() {
		sprintVelocityServiceHelper = new SprintVelocityServiceHelper();
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		storyList = jiraIssueDataFactory.getJiraIssues();
		sprintDetails = SprintDetailsDataFactory.newInstance().getSprintDetails();

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);

	}

	@Test
	public void testCalculateSprintVelocityValue() {
		Map<Pair<String, String>, Set<JiraIssue>> currentSprintLeafVelocityMap = new HashMap<>();
		sprintVelocityServiceHelper.getSprintIssuesForProject(storyList, sprintDetails, currentSprintLeafVelocityMap);
		Assert.assertTrue(currentSprintLeafVelocityMap.size() > 0);

		Map<Pair<String, String>, Set<JiraIssue>> currentSprintLeafVelocity = new HashMap<>();
		sprintVelocityServiceHelper.getSprintIssuesForProject(storyList, new ArrayList<SprintDetails>(),
				currentSprintLeafVelocity);
		Assert.assertTrue(currentSprintLeafVelocityMap.size() > 0);

		Pair<String, String> currentNodeIdentifier = Pair.of("6335363749794a18e8a4479b",
				"40199_Scrum Project_6335363749794a18e8a4479b");

		Assert.assertFalse(sprintVelocityServiceHelper.calculateSprintVelocityValue(currentSprintLeafVelocityMap,
				currentNodeIdentifier, fieldMapping) > 0);

		Pair<String, String> currentNodeIdent = Pair.of("6335363749794a18e8a4479b",
				"38294_Scrum Project_6335363749794a18e8a4479b");
		Assert.assertTrue(sprintVelocityServiceHelper.calculateSprintVelocityValue(currentSprintLeafVelocityMap,
				currentNodeIdent, fieldMapping) > 0);
	}

}