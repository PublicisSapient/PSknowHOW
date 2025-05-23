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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.backlogdashboard.JiraBacklogKPIService;
import com.publicissapient.kpidashboard.apis.model.EpicMetaData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.apis.util.ReleaseKpiHelper;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Epic Progress kpi to get release issues on the basis of epic
 *
 * @author shi6
 */
@Slf4j
@Component
public class BacklogEpicProgressServiceImpl extends JiraBacklogKPIService<Integer, List<Object>> {

	private static final String TOTAL_ISSUES = "totalIssues";
	private static final String EPIC_LINKED = "epicLinked";
	private static final String RELEASE_JIRA_ISSUE_STATUS = "releaseJiraIssueStatus";
	private static final String TO_DO = "To Do";
	private static final String IN_PROGRESS = "In Progress";
	private static final String DONE = "Done";
	private static final String ALL_EPIC = "All Epics";
	private static final String OPEN_EPIC = "Open Epics";
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DateUtil.DATE_FORMAT);
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CommonServiceImpl commonService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node projectNode)
			throws ApplicationException {
		projectWiseLeafNodeValue(projectNode, kpiElement, kpiRequest);
		log.info("BacklogEpicProgressServiceImpl -> getKpiData ->  : {}", kpiElement);
		return kpiElement;
	}

	/**
	 * project wise processing
	 *
	 * @param leafNode
	 *          leafNode
	 * @param kpiElement
	 *          kpiElement
	 * @param kpiRequest
	 *          kpiElement
	 */
	private void projectWiseLeafNodeValue(Node leafNode, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		if (leafNode != null) {
			Object basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			Map<String, Object> resultMap = fetchKPIDataFromDb(leafNode, null, null, kpiRequest);
			List<JiraIssue> totalIssues = (List<JiraIssue>) resultMap.get(TOTAL_ISSUES);
			Set<JiraIssue> epicIssues = (Set<JiraIssue>) resultMap.get(EPIC_LINKED);
			JiraIssueReleaseStatus jiraIssueReleaseStatus = (JiraIssueReleaseStatus) resultMap.get(RELEASE_JIRA_ISSUE_STATUS);
			List<IterationKpiValue> filterDataList = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(totalIssues) && CollectionUtils.isNotEmpty(epicIssues) &&
					jiraIssueReleaseStatus != null) {
				Map<String, String> epicWiseIssueSize = createDataCountGroupMap(totalIssues, jiraIssueReleaseStatus, epicIssues,
						fieldMapping, filterDataList);
				populateExcelDataObject(requestTrackerId, excelData, epicWiseIssueSize, epicIssues, jiraIssueReleaseStatus,
						totalIssues);
				kpiElement.setSprint(leafNode.getName());
				kpiElement.setModalHeads(KPIExcelColumn.BACKLOG_EPIC_PROGRESS.getColumns());
				kpiElement.setExcelColumns(KPIExcelColumn.BACKLOG_EPIC_PROGRESS.getColumns());
				kpiElement.setExcelData(excelData);
			}
			kpiElement.setTrendValueList(filterDataList);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Backlog Epic Progress -> Requested sprint : {}", leafNode.getName());
			List<JiraIssue> totalJiraIssue = jiraIssueRepository
					.findByBasicProjectConfigId(leafNode.getProjectFilter().getBasicProjectConfigId().toString());
			resultListMap.put(TOTAL_ISSUES, totalJiraIssue);
			// get Epics Linked to backlogStories stories
			final List<String> epicKeyList = totalJiraIssue.stream().map(JiraIssue::getEpicLinked).toList();
			Set<JiraIssue> epicJiraIssues = totalJiraIssue.stream()
					.filter(j -> epicKeyList.contains(j.getNumber())
							&& j.getTypeName().equalsIgnoreCase(NormalizedJira.ISSUE_TYPE.getValue()))
					.collect(Collectors.toSet());
			resultListMap.put(EPIC_LINKED, epicJiraIssues);
			// get status category of the project
			resultListMap.put(RELEASE_JIRA_ISSUE_STATUS, getJiraIssueReleaseStatus());
		}
		return resultListMap;
	}

	/**
	 * @param jiraIssueList
	 *          jiraIssueList
	 * @param jiraIssueReleaseStatus
	 *          jiraIssueReleaseStatus
	 * @param epicIssues
	 *          epicIssues
	 * @param fieldMapping
	 *          fieldMapping
	 * @param iterationKpiValues
	 *          iterationKpiValues
	 * @return map of epicnumber and the size of stories
	 */
	public Map<String, String> createDataCountGroupMap(List<JiraIssue> jiraIssueList,
			JiraIssueReleaseStatus jiraIssueReleaseStatus, Set<JiraIssue> epicIssues, FieldMapping fieldMapping,
			List<IterationKpiValue> iterationKpiValues) {

		Map<String, List<JiraIssue>> epicWiseJiraIssues = jiraIssueList.stream()
				.filter(jiraIssue -> jiraIssue.getEpicLinked() != null)
				.collect(Collectors.groupingBy(JiraIssue::getEpicLinked));
		Map<String, EpicMetaData> epicIssueMap = epicIssues.stream().collect(Collectors.toMap(JiraIssue::getNumber,
				jiraIssue -> new EpicMetaData(jiraIssue.getName(), jiraIssue.getUrl(), jiraIssue.getCreatedDate())));
		List<DataCount> dataCountList = new ArrayList<>();
		List<DataCount> openDataCountList = new ArrayList<>();
		Map<String, String> epicWiseSize = new HashMap<>();
		epicWiseJiraIssues.forEach((epic, issues) -> {
			if (epicIssueMap.containsKey(epic)) {
				EpicMetaData epicMetaData = epicIssueMap.get(epic);
				DataCount statusWiseCountList = getStatusWiseCountList(issues, jiraIssueReleaseStatus, epicMetaData,
						fieldMapping);
				epicWiseSize.put(epic, String.valueOf(statusWiseCountList.getSize()));
				dataCountList.add(statusWiseCountList);

				if (!CollectionUtils.isEqualCollection(
						ReleaseKpiHelper.filterIssuesByStatus(issues, jiraIssueReleaseStatus.getClosedList()), issues)) {
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
	 * @param jiraIssueList
	 *          jiraIssueList
	 * @param jiraIssueReleaseStatus
	 *          jiraIssueReleaseStatus
	 * @param epic
	 *          epic
	 * @param fieldMapping
	 *          fieldMapping
	 * @return DataCount
	 */
	DataCount getStatusWiseCountList(List<JiraIssue> jiraIssueList, JiraIssueReleaseStatus jiraIssueReleaseStatus,
			EpicMetaData epic, FieldMapping fieldMapping) {
		DataCount issueCountDc = new DataCount();
		List<DataCount> issueCountDcList = new ArrayList<>();
		String name = epic.getName();
		String url = epic.getUrl();
		String createdDate = epic.getCreatedDate();
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
		issueCountDc.setCreatedDate(createdDate);
		return issueCountDc;
	}

	/**
	 * create drill down
	 *
	 * @param issueCountStatusMap
	 *          issueCountStatusMap
	 * @param releaseStatus
	 *          releaseStatus
	 * @param releaseStatusCount
	 *          releaseStatusCount
	 * @param issueSize
	 *          issueSize
	 * @param issueCountDcList
	 *          issueCountDcList
	 * @param fieldMapping
	 *          fieldMapping
	 */
	private static void createIssueCountDrillDown(Map<String, List<JiraIssue>> issueCountStatusMap, String releaseStatus,
			long releaseStatusCount, double issueSize, List<DataCount> issueCountDcList, FieldMapping fieldMapping) {
		List<DataCount> drillDownList = new ArrayList<>();
		issueCountStatusMap.forEach((status, issueList) -> drillDownList.add(
				new DataCount(status, issueList.size(), KpiDataHelper.calculateStoryPoints(issueList, fieldMapping), null)));
		DataCount releaseStatusDc = new DataCount(releaseStatus, releaseStatusCount, issueSize, drillDownList);
		issueCountDcList.add(releaseStatusDc);
	}

	/**
	 * sort in reverse order on the basis of created date of epics in reverse order
	 * and then on the basis of those epics whose to do and in progress issues are
	 * more should appear first
	 *
	 * @param dataCountList
	 */
	void sorting(List<DataCount> dataCountList) {
		if (CollectionUtils.isNotEmpty(dataCountList))
			dataCountList.sort(Comparator
					.comparing((DataCount dataCount) -> LocalDate.parse(dataCount.getCreatedDate().split("T")[0], dateFormatter))
					.thenComparingLong(data ->
					// Calculate the sum of values for specified subFilters
					((List<DataCount>) data.getValue()).stream()
							.filter(subfilter -> subfilter.getSubFilter().equalsIgnoreCase(TO_DO) ||
									subfilter.getSubFilter().equalsIgnoreCase(IN_PROGRESS))
							.mapToLong(a -> (long) a.getValue()).sum()));
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			Map<String, String> epicWiseIssueSize, Set<JiraIssue> epicIssues, JiraIssueReleaseStatus jiraIssueReleaseStatus,
			List<JiraIssue> totalIssues) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase()) &&
				MapUtils.isNotEmpty(epicWiseIssueSize)) {
			Map<String, List<JiraIssue>> epicWiseJiraIssues = totalIssues.stream()
					.filter(jiraIssue -> jiraIssue.getEpicLinked() != null)
					.collect(Collectors.groupingBy(JiraIssue::getEpicLinked));
			Map<String, JiraIssue> epicWiseJiraIssue = epicIssues.stream()
					.collect(Collectors.toMap(JiraIssue::getNumber, jiraIssue -> jiraIssue));
			KPIExcelUtility.populateEpicProgessExcelData(epicWiseIssueSize, epicWiseJiraIssue, excelData,
					jiraIssueReleaseStatus, epicWiseJiraIssues);
		}
	}

	@Override
	public String getQualifierType() {
		return KPICode.BACKLOG_EPIC_PROGRESS.name();
	}
}
