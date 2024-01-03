package com.publicissapient.kpidashboard.common.constant;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommonConstantTest {

	@Test
	public void testConstantValues() {
		assertEquals("jiraKpiCache", CommonConstant.JIRA_KPI_CACHE);
		assertEquals("azureKpiCache", CommonConstant.AZURE_KPI_CACHE);
		assertEquals("sonarKpiCache", CommonConstant.SONAR_KPI_CACHE);
		assertEquals("bitbucketKpiCache", CommonConstant.BITBUCKET_KPI_CACHE);
		assertEquals("gitLabKpiCache", CommonConstant.GITLAB_KPI_CACHE);
		assertEquals("jiraKpiCache", CommonConstant.JIRA_KPI_CACHE);
		assertEquals("testingKpiCache", CommonConstant.TESTING_KPI_CACHE);
		assertEquals("jiraKanbanKpiCache", CommonConstant.JIRAKANBAN_KPI_CACHE);
		assertEquals("accountHierarchy", CommonConstant.CACHE_ACCOUNT_HIERARCHY);
		assertEquals("accountHierarchyKanban", CommonConstant.CACHE_ACCOUNT_HIERARCHY_KANBAN);
		assertEquals("toolItemMap", CommonConstant.CACHE_TOOL_CONFIG_MAP);
		assertEquals("fieldMappingMap", CommonConstant.CACHE_FIELD_MAPPING_MAP);
		assertEquals("projectConfigMap", CommonConstant.CACHE_PROJECT_CONFIG_MAP);
		assertEquals("projectToolConfigMap", CommonConstant.CACHE_PROJECT_TOOL_CONFIG_MAP);
		assertEquals("api/cache/clearCache", CommonConstant.CACHE_CLEAR_ENDPOINT);
		assertEquals("api/cache/clearAllCache", CommonConstant.CLEAR_ALL_CACHE_ENDPOINT);
		assertEquals("Reprocess", CommonConstant.REPROCESS);
		assertEquals("Uploaded", CommonConstant.FILE_STATUS_UPLOADED);
		assertEquals("sprint", CommonConstant.SPRINT);
		assertEquals("bug", CommonConstant.BUG);
		assertEquals("issuetype", CommonConstant.ISSUE_TYPE);
		assertEquals("ticketCountIssueType", CommonConstant.TICKET_COUNT_ISSUE_TYPE);

		assertEquals("firststatus", CommonConstant.FIRST_STATUS);
		assertEquals("firstDevstatus", CommonConstant.FIRST_DEV_STATUS);
		assertEquals("Open", CommonConstant.OPEN);
		assertEquals("Closed", CommonConstant.CLOSED);
		assertEquals("FUTURE", CommonConstant.FUTURE);
		assertEquals("rootcause", CommonConstant.ROOT_CAUSE);
		assertEquals("development", CommonConstant.DEVELOPMENT);
		assertEquals("qa", CommonConstant.QA);
		assertEquals("story", CommonConstant.STORY);
		assertEquals("dor", CommonConstant.DOR);
		assertEquals("dod", CommonConstant.DOD);
		assertEquals("rejection", CommonConstant.REJECTION);
		assertEquals("delivered", CommonConstant.DELIVERED);
		assertEquals("storypoint", CommonConstant.STORYPOINT);
		assertEquals("rootCauseValue", CommonConstant.ROOT_CAUSE_VALUE);
		assertEquals("rejectionResolution", CommonConstant.REJECTION_RESOLUTION);
		assertEquals("qaRootCause", CommonConstant.QA_ROOT_CAUSE);
		assertEquals("uatdefect", CommonConstant.UAT_DEFECT);

		assertEquals("ticketClosedStatus", CommonConstant.TICKET_CLOSED_STATUS);

		// Add more assertions for other constant values
	}
}
