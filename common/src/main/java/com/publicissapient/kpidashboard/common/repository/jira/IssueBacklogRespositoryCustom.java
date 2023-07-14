package com.publicissapient.kpidashboard.common.repository.jira;

import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.model.jira.IssueBacklog;

public interface IssueBacklogRespositoryCustom {
	List<IssueBacklog> findIssuesBySprintAndType(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap);

	List<IssueBacklog> findIssuesByDateAndTypeAndStatus(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo, String range,
			String mapStatusCriteria, boolean isProductionDefect);

	List<IssueBacklog> findDefectsWithoutStoryLink(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMapNotIn);

	List<IssueBacklog> findIssuesByFilterAndProjectMapFilter(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap);

	List<IssueBacklog> findUnassignedIssues(String startDate, String endDate, Map<String, List<String>> mapOfFilters);

}
