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

package com.publicissapient.kpidashboard.apis.jira.kanban.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("javadoc")
@Service
@Slf4j
public class CostOfDelayKanbanServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	private static final String COD_DATA = "costOfDelayData";
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private KanbanJiraIssueRepository jiraKanbanIssueRepository;

	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		List<String> projectList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(leafNodeList)) {

			leafNodeList.forEach(leaf -> {
				ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
				projectList.add(basicProjectConfigId.toString());

			});
		}
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				projectList.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.STATUS.getFieldValueInFeature(), Arrays.asList(NormalizedJira.STATUS.getValue()));
		mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
				Arrays.asList(NormalizedJira.ISSUE_TYPE.getValue()));

		List<KanbanJiraIssue> codList = jiraKanbanIssueRepository.findCostOfDelayByType(mapOfFilters);
		resultListMap.put(COD_DATA, codList);
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.COST_OF_DELAY_KANBAN.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes()
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);
		log.debug("COST_OF_DELAY-KANBAN-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.COST_OF_DELAY_KANBAN);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.COST_OF_DELAY_KANBAN);

		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);
		kpiElement.setTrendValueList(trendValues);
		log.debug("[COST_OF_DELAY-KANBAN-WISE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> projectList, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);

		String startDate = dateRange.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String endDate = dateRange.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		Map<String, Object> resultMap = fetchKPIDataFromDb(projectList, startDate, endDate, kpiRequest);
		Map<String, Map<String, List<KanbanJiraIssue>>> projectandDayWiseDelay = createProjectandDayWiseDelay(
				(List<KanbanJiraIssue>) resultMap.get(COD_DATA));
		kpiWithoutFilter(projectandDayWiseDelay, mapTmp, projectList, kpiElement);

	}

	/**
	 * @param projectandDayWiseDelay
	 * @param mapTmp
	 * @param projectList
	 * @param kpiElement
	 */
	private void kpiWithoutFilter(Map<String, Map<String, List<KanbanJiraIssue>>> projectandDayWiseDelay,
			Map<String, Node> mapTmp, List<Node> projectList, KpiElement kpiElement) {
		String requestTrackerId = getKanbanRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		projectList.forEach(node -> {
			LocalDate currentDate = LocalDate.now();
			String projectNodeId = node.getProjectFilter().getBasicProjectConfigId().toString();
			Map<String, List<KanbanJiraIssue>> dateWiseIssue = projectandDayWiseDelay.get(projectNodeId);
			if (MapUtils.isNotEmpty(dateWiseIssue)) {
				List<DataCount> dataCount = new ArrayList<>();
				List<KanbanJiraIssue> kanbanJiraIssueList = new ArrayList<>();
				String projectId = node.getProjectFilter().getId();
				String projectName = projectId.substring(0, projectId.lastIndexOf(CommonConstant.UNDERSCORE));
				for (int i = 0; i < customApiConfig.getJiraXaxisMonthCount(); i++) {
					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
							CommonConstant.MONTH);
					Double cod = filterDataBasedOnStartAndEndDate(dateWiseIssue, dateRange, kanbanJiraIssueList);
					String date = dateRange.getStartDate().getMonth().toString() + " "
							+ dateRange.getStartDate().getYear();
					dataCount.add(getDataCountObject(cod, projectName, date));
					currentDate = currentDate.minusMonths(1);
				}

				mapTmp.get(node.getId()).setValue(dataCount);
				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					KPIExcelUtility.populateKanbanCODExcelData(projectName, kanbanJiraIssueList, excelData);
				}
			}

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.COST_OF_DELAY.getColumns());

	}

	private DataCount getDataCountObject(Double value, String projectName, String date) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSSprintID(date);
		dataCount.setSSprintName(date);
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setSprintNames(new ArrayList<>(Arrays.asList(projectName)));
		dataCount.setValue(value);
		dataCount.setHoverValue(new HashMap<>());
		return dataCount;
	}

	private Double filterDataBasedOnStartAndEndDate(Map<String, List<KanbanJiraIssue>> dateWiseIssue,
			CustomDateRange dateRange, List<KanbanJiraIssue> kanbanJiraIssueList) {
		List<KanbanJiraIssue> dummyList = new LinkedList<>();
		List<KanbanJiraIssue> issueList = new ArrayList<>();

		Double cod = 0.0d;

		for (LocalDate currentDate = dateRange.getStartDate(); currentDate.compareTo(dateRange.getStartDate()) >= 0
				&& dateRange.getEndDate().compareTo(currentDate) >= 0; currentDate = currentDate.plusDays(1)) {
			dummyList.add(KanbanJiraIssue.builder().costOfDelay(0.0d).projectName("").build());
			issueList.addAll(dateWiseIssue.getOrDefault(currentDate.toString(), dummyList));
		}

		if (CollectionUtils.isNotEmpty(issueList)) {
			kanbanJiraIssueList.addAll(issueList);
			cod = issueList.stream().mapToDouble(KanbanJiraIssue::getCostOfDelay).sum();
		}

		return cod;
	}

	/**
	 * Group list of data by project and changed date.
	 *
	 * @param resultList
	 * @return
	 */
	private Map<String, Map<String, List<KanbanJiraIssue>>> createProjectandDayWiseDelay(
			List<KanbanJiraIssue> resultList) {
		return resultList.stream().filter(p -> p.getProjectID() != null)
				.collect(Collectors.groupingBy(KanbanJiraIssue::getBasicProjectConfigId, Collectors.groupingBy(
						f -> LocalDate.parse(f.getChangeDate().split("\\.")[0], DATE_TIME_FORMATTER).toString()

				)));
	}

}