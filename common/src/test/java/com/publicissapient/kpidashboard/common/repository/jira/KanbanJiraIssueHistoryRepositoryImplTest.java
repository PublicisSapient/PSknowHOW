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

package com.publicissapient.kpidashboard.common.repository.jira;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.jira.IssueGroupFields;
import com.publicissapient.kpidashboard.common.model.jira.IssueHistoryMappedData;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueHistory;

/*
author @shi6
*/
@ExtendWith(SpringExtension.class)
public class KanbanJiraIssueHistoryRepositoryImplTest {
	@Mock
	private MongoOperations mongoOperations;

	@InjectMocks
	private KanbanJiraIssueHistoryRepositoryImpl jiraIssueHistoryRepository;

	@Test
	public void testFindIssuesByStatusAndDate() {
		// Mock data
		Map<String, List<String>> mapOfFilters = Collections.singletonMap("key", Collections.singletonList("value"));

		Map<String, Object> filterMap = new HashMap<>();
		filterMap.put("historyDetails.status", Arrays.asList("open"));

		Map<String, Map<String, Object>> uniqueProjectMap = Collections.singletonMap("project", filterMap);
		String dateFrom = "2022-01-01";
		String dateTo = "2022-01-31";
		String mapStatusCriteria = "IN";

		AggregationResults<IssueHistoryMappedData> anc = mock(AggregationResults.class);

		doReturn(anc).when(mongoOperations).aggregate(any(), eq(KanbanIssueCustomHistory.class), any());
		doReturn(createIssueHistory()).when(anc).getMappedResults();

		jiraIssueHistoryRepository.findIssuesByStatusAndDate(mapOfFilters, uniqueProjectMap, dateFrom, dateTo,
				mapStatusCriteria);

		verify(mongoOperations, times(1)).aggregate(any(), eq(KanbanIssueCustomHistory.class), any());
	}

	@Test
	public void testFindIssuesByStatusAndDatewithNIN() {
		// Mock data
		Map<String, List<String>> mapOfFilters = Collections.singletonMap("key", Collections.singletonList("value"));

		Map<String, Object> filterMap = new HashMap<>();
		filterMap.put("historyDetails.status", Arrays.asList("open"));

		Map<String, Map<String, Object>> uniqueProjectMap = Collections.singletonMap("project", filterMap);
		String dateFrom = "2022-01-01";
		String dateTo = "2022-01-31";
		String mapStatusCriteria = "NIN";

		AggregationResults<IssueHistoryMappedData> anc = mock(AggregationResults.class);

		doReturn(anc).when(mongoOperations).aggregate(any(), eq(KanbanIssueCustomHistory.class), any());
		doReturn(createIssueHistory()).when(anc).getMappedResults();

		jiraIssueHistoryRepository.findIssuesByStatusAndDate(mapOfFilters, uniqueProjectMap, dateFrom, dateTo,
				mapStatusCriteria);

		verify(mongoOperations, times(1)).aggregate(any(), eq(KanbanIssueCustomHistory.class), any());
	}

	@Test
	public void testFindIssuesByCreatedDateAndType() {
		// Mock data
		Map<String, List<String>> mapOfFilters = Collections.singletonMap("key", Collections.singletonList("value"));

		Map<String, Object> filterMap = new HashMap<>();
		filterMap.put("historyDetails.status", Arrays.asList("open"));

		Map<String, Map<String, Object>> uniqueProjectMap = Collections.singletonMap("project", filterMap);
		String dateFrom = "2022-01-01";
		String dateTo = "2022-01-31";

		when(mongoOperations.find(any(Query.class), eq(KanbanIssueCustomHistory.class)))
				.thenReturn(Collections.emptyList());

		jiraIssueHistoryRepository.findIssuesByCreatedDateAndType(mapOfFilters, uniqueProjectMap, dateFrom, dateTo);

		verify(mongoOperations, times(1)).find(any(Query.class), eq(KanbanIssueCustomHistory.class));
	}

	@Test
	public void testFindIssuesInWipByDate() {
		// Mock data
		Map<String, List<String>> mapOfFilters = Collections.singletonMap("key", Collections.singletonList("value"));

		Map<String, Object> filterMap = new HashMap<>();
		filterMap.put("historyDetails.status", Arrays.asList("open"));

		Map<String, Map<String, Object>> uniqueProjectMap = Collections.singletonMap("project", filterMap);
		String dateFrom = "2022-01-01";
		String dateTo = "2022-01-31";

		AggregationResults<IssueHistoryMappedData> anc = mock(AggregationResults.class);

		doReturn(anc).when(mongoOperations).aggregate(any(), eq(KanbanIssueCustomHistory.class), any());
		doReturn(createIssueHistory()).when(anc).getMappedResults();

		jiraIssueHistoryRepository.findIssuesInWipByDate(mapOfFilters, uniqueProjectMap, uniqueProjectMap, dateFrom,
				dateTo);

		verify(mongoOperations, times(1)).aggregate(any(), eq(KanbanIssueCustomHistory.class), any());
	}

	@Test
	public void testFindIssuesInWipByDateWithouStatus() {
		// Mock data
		Map<String, List<String>> mapOfFilters = Collections.singletonMap("key", Collections.singletonList("value"));

		Map<String, Object> filterMap = new HashMap<>();
		filterMap.put("historyDetails", Arrays.asList("open"));

		Map<String, Map<String, Object>> uniqueProjectMap = Collections.singletonMap("project", filterMap);
		String dateFrom = "2022-01-01";
		String dateTo = "2022-01-31";

		AggregationResults<IssueHistoryMappedData> anc = mock(AggregationResults.class);

		doReturn(anc).when(mongoOperations).aggregate(any(), eq(KanbanIssueCustomHistory.class), any());
		doReturn(createIssueHistory()).when(anc).getMappedResults();

		jiraIssueHistoryRepository.findIssuesInWipByDate(mapOfFilters, uniqueProjectMap, uniqueProjectMap, dateFrom,
				dateTo);

		verify(mongoOperations, times(1)).aggregate(any(), eq(KanbanIssueCustomHistory.class), any());
	}

	List<IssueHistoryMappedData> createIssueHistory() {
		IssueHistoryMappedData issueHistoryMappedData = new IssueHistoryMappedData();
		IssueGroupFields groupFields = new IssueGroupFields();
		groupFields.setStoryID("DTS-123");
		groupFields.setProjectComponentId("DTS");
		groupFields.setBasicProjectConfigId("60ed70a572dafe33d3e37111");
		groupFields.setUrl("www.abc.com");
		issueHistoryMappedData.setId(groupFields);
		issueHistoryMappedData.setHistoryDetails(Arrays.asList(KanbanIssueHistory.builder().buildNumber("123")
				.status("SUCESS").type("DEPLOY").activityDate(LocalDateTime.now().toString()).build()));
		issueHistoryMappedData.setStatusUpdationLog(Arrays.asList(
				JiraHistoryChangeLog.builder().changedTo("end").changedFrom("start").updatedOn(LocalDateTime.now()).build()));
		return Arrays.asList(issueHistoryMappedData);
	}
}
