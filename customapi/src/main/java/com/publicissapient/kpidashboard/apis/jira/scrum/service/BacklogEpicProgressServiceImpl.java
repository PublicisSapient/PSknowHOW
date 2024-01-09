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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.CommonServiceImpl;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.apis.util.ReleaseKpiHelper;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Epic Progress kpi to get release issues on the basis of epic
 * 
 * @author shi6
 */
@Slf4j
@Component
public class BacklogEpicProgressServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final String TOTAL_ISSUES = "totalIssues";
	private static final String EPIC_LINKED = "epicLinked";
	private static final String RELEASE_JIRA_ISSUE_STATUS = "releaseJiraIssueStatus";
	private static final String TO_DO = "To Do";
	private static final String IN_PROGRESS = "In Progress";
	private static final String DONE = "Done";
	private static final String ALL_EPIC = "All Epics";
	private static final String OPEN_EPIC = "Open Epics";
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CommonServiceImpl commonService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.PROJECT) {
				projectWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		log.info("BacklogEpicProgressServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	/**
	 * project wise processing
	 * 
	 * @param leafNodeList
	 *            leafNodeList
	 * @param kpiElement
	 *            kpiElement
	 * @param kpiRequest
	 *            kpiElement
	 */
	private void projectWiseLeafNodeValue(List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (leafNode != null) {
			Object basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, null, null, kpiRequest);
			List<JiraIssue> totalIssues = (List<JiraIssue>) resultMap.get(TOTAL_ISSUES);
			Set<JiraIssue> epicIssues = (Set<JiraIssue>) resultMap.get(EPIC_LINKED);
			JiraIssueReleaseStatus jiraIssueReleaseStatus = (JiraIssueReleaseStatus) resultMap
					.get(RELEASE_JIRA_ISSUE_STATUS);
			List<IterationKpiValue> filterDataList = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(totalIssues) && CollectionUtils.isNotEmpty(epicIssues)
					&& jiraIssueReleaseStatus != null) {
				Map<String, String> epicWiseIssueSize = createDataCountGroupMap(totalIssues, jiraIssueReleaseStatus,
						epicIssues, fieldMapping, filterDataList);
				populateExcelDataObject(requestTrackerId, excelData, epicWiseIssueSize, epicIssues);
				kpiElement.setSprint(leafNode.getName());
				kpiElement.setModalHeads(KPIExcelColumn.BACKLOG_EPIC_PROGRESS.getColumns());
				kpiElement.setExcelColumns(KPIExcelColumn.BACKLOG_EPIC_PROGRESS.getColumns());
				kpiElement.setExcelData(excelData);
			}
			kpiElement.setTrendValueList(filterDataList);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Backlog Epic Progress -> Requested sprint : {}", leafNode.getName());
			List<JiraIssue> totalJiraIssue = getBackLogJiraIssuesFromBaseClass();
			resultListMap.put(TOTAL_ISSUES, totalJiraIssue);
			// get Epics Linked to backlogStories stories
			resultListMap.put(EPIC_LINKED,
					jiraIssueRepository.findNumberInAndBasicProjectConfigIdAndTypeName(
							totalJiraIssue.stream().map(JiraIssue::getEpicLinked).collect(Collectors.toList()),
							leafNode.getProjectFilter().getBasicProjectConfigId().toString(),
							NormalizedJira.ISSUE_TYPE.getValue()));
			// get status category of the project
			resultListMap.put(RELEASE_JIRA_ISSUE_STATUS, getJiraIssueReleaseStatus());
		}
		return resultListMap;
	}

	/**
	 *
	 * @param jiraIssueList
	 *            jiraIssueList
	 * @param jiraIssueReleaseStatus
	 *            jiraIssueReleaseStatus
	 * @param epicIssues
	 *            epicIssues
	 * @param fieldMapping
	 *            fieldMapping
	 * @param iterationKpiValues
	 *            iterationKpiValues
	 * @return map of epicnumber and the size of stories
	 */
	public Map<String, String> createDataCountGroupMap(List<JiraIssue> jiraIssueList,
			JiraIssueReleaseStatus jiraIssueReleaseStatus, Set<JiraIssue> epicIssues, FieldMapping fieldMapping,
			List<IterationKpiValue> iterationKpiValues) {

		Map<String, List<JiraIssue>> epicWiseJiraIssues = jiraIssueList.stream()
				.filter(jiraIssue -> jiraIssue.getEpicLinked() != null)
				.collect(Collectors.groupingBy(JiraIssue::getEpicLinked));
		Map<String, Map.Entry<String, String>> epicIssueMap = epicIssues.stream()
				.collect(Collectors.toMap(JiraIssue::getNumber,
						jiraIssue -> new AbstractMap.SimpleEntry<>(jiraIssue.getName(), jiraIssue.getUrl())));
		List<DataCount> dataCountList = new ArrayList<>();
		List<DataCount> openDataCountList = new ArrayList<>();
		Map<String, String> epicWiseSize = new HashMap<>();
		epicWiseJiraIssues.forEach((epic, issues) -> {
			if (epicIssueMap.containsKey(epic)) {
				Map.Entry<String, String> epicNameUrl = epicIssueMap.get(epic);
				DataCount statusWiseCountList = getStatusWiseCountList(issues, jiraIssueReleaseStatus, epicNameUrl,
						fieldMapping);
				epicWiseSize.put(epic, String.valueOf(statusWiseCountList.getSize()));
				dataCountList.add(statusWiseCountList);

				if (!CollectionUtils.isEqualCollection(
						ReleaseKpiHelper.filterIssuesByStatus(issues, jiraIssueReleaseStatus.getClosedList()),
						issues)) {
					openDataCountList.add(statusWiseCountList);
				}
			}
		});
		IterationKpiValue allEpicIterationKpiValue = new IterationKpiValue();
		sorting(dataCountList);
		allEpicIterationKpiValue.setFilter1(ALL_EPIC);
		allEpicIterationKpiValue.setValue(dataCountList);

		IterationKpiValue openEpicIterationKpiValue = new IterationKpiValue();
		sorting(openDataCountList);
		openEpicIterationKpiValue.setFilter1(OPEN_EPIC);
		openEpicIterationKpiValue.setValue(openDataCountList);

		iterationKpiValues.add(openEpicIterationKpiValue);
		iterationKpiValues.add(allEpicIterationKpiValue);
		return epicWiseSize;
	}

	/**
	 * 
	 * @param jiraIssueList
	 *            jiraIssueList
	 * @param jiraIssueReleaseStatus
	 *            jiraIssueReleaseStatus
	 * @param epic
	 *            epic
	 * @param fieldMapping
	 *            fieldMapping
	 * @return DataCount
	 */
	DataCount getStatusWiseCountList(List<JiraIssue> jiraIssueList, JiraIssueReleaseStatus jiraIssueReleaseStatus,
			Map.Entry<String, String> epic, FieldMapping fieldMapping) {
		DataCount issueCountDc = new DataCount();
		List<DataCount> issueCountDcList = new ArrayList<>();
		String name = epic.getKey();
		String url = epic.getValue();
		// filter by to do category
		List<JiraIssue> toDoJiraIssue = ReleaseKpiHelper.filterIssuesByStatus(jiraIssueList,
				jiraIssueReleaseStatus.getToDoList());
		// filter by inProgress category
		List<JiraIssue> inProgressJiraIssue = ReleaseKpiHelper.filterIssuesByStatus(jiraIssueList,
				jiraIssueReleaseStatus.getInProgressList());
		// filter by done category
		List<JiraIssue> doneJiraIssue = ReleaseKpiHelper.filterIssuesByStatus(jiraIssueList,
				jiraIssueReleaseStatus.getClosedList());

		// create drill down
		long toDoCount = toDoJiraIssue.size();
		double toDoSize = KpiDataHelper.calculateStoryPoints(toDoJiraIssue, fieldMapping);
		Map<String, List<JiraIssue>> toDoStatusMap = toDoJiraIssue.stream()
				.collect(Collectors.groupingBy(JiraIssue::getStatus));
		createIssueCountDrillDown(toDoStatusMap, TO_DO, toDoCount, toDoSize, issueCountDcList, fieldMapping);

		long inProgressCount = inProgressJiraIssue.size();
		double inProgressSize = KpiDataHelper.calculateStoryPoints(inProgressJiraIssue, fieldMapping);
		Map<String, List<JiraIssue>> inProgressStatusMap = inProgressJiraIssue.stream()
				.collect(Collectors.groupingBy(JiraIssue::getStatus));
		createIssueCountDrillDown(inProgressStatusMap, IN_PROGRESS, inProgressCount, inProgressSize, issueCountDcList,
				fieldMapping);

		long doneCount = doneJiraIssue.size();
		double doneSize = KpiDataHelper.calculateStoryPoints(doneJiraIssue, fieldMapping);
		Map<String, List<JiraIssue>> doneStatusMap = doneJiraIssue.stream()
				.collect(Collectors.groupingBy(JiraIssue::getStatus));
		createIssueCountDrillDown(doneStatusMap, DONE, doneCount, doneSize, issueCountDcList, fieldMapping);

		issueCountDc.setData(String.valueOf(toDoCount + inProgressCount + doneCount));
		issueCountDc.setSize(String.valueOf(toDoSize + inProgressSize + doneSize));
		issueCountDc.setValue(issueCountDcList);
		issueCountDc.setKpiGroup(name);
		issueCountDc.setUrl(url);
		return issueCountDc;
	}

	/**
	 * create drill down
	 * 
	 * @param issueCountStatusMap
	 *            issueCountStatusMap
	 * @param releaseStatus
	 *            releaseStatus
	 * @param releaseStatusCount
	 *            releaseStatusCount
	 * @param issueSize
	 *            issueSize
	 * @param issueCountDcList
	 *            issueCountDcList
	 * @param fieldMapping
	 *            fieldMapping
	 */
	private static void createIssueCountDrillDown(Map<String, List<JiraIssue>> issueCountStatusMap,
			String releaseStatus, long releaseStatusCount, double issueSize, List<DataCount> issueCountDcList,
			FieldMapping fieldMapping) {
		List<DataCount> drillDownList = new ArrayList<>();
		issueCountStatusMap.forEach((status, issueList) -> drillDownList.add(new DataCount(status, issueList.size(),
				KpiDataHelper.calculateStoryPoints(issueList, fieldMapping), null)));
		DataCount releaseStatusDc = new DataCount(releaseStatus, releaseStatusCount, issueSize, drillDownList);
		issueCountDcList.add(releaseStatusDc);
	}

	/**
	 * sort in reverse order on the basis of those epic whose to do and in progress
	 * issues are more should appear first
	 * 
	 * @param dataCountList
	 */
	void sorting(List<DataCount> dataCountList) {
		if (CollectionUtils.isNotEmpty(dataCountList))
			dataCountList.sort(Comparator.comparing(data -> ((List<DataCount>) data.getValue()).stream()
					.filter(subfilter -> subfilter.getSubFilter().equalsIgnoreCase(TO_DO)
							|| subfilter.getSubFilter().equalsIgnoreCase(IN_PROGRESS))
					.mapToLong(a -> (long) a.getValue()).sum()));
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			Map<String, String> epicWiseIssueSize, Set<JiraIssue> epicIssues) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& MapUtils.isNotEmpty(epicWiseIssueSize)) {
			Map<String, JiraIssue> epicWiseJiraIssue = epicIssues.stream()
					.collect(Collectors.toMap(JiraIssue::getNumber, jiraIssue -> jiraIssue));
			KPIExcelUtility.populateEpicProgessExcelData(epicWiseIssueSize, epicWiseJiraIssue, excelData);
		}
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public String getQualifierType() {
		return KPICode.BACKLOG_EPIC_PROGRESS.name();
	}

}
