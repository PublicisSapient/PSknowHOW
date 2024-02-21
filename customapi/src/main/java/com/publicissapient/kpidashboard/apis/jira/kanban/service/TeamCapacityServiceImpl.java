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
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
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
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TeamCapacityServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String TICKET_LIST = "tickets";
	private static final String SUBGROUPCATEGORY = "subGroupCategory";
	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private FilterHelperService filterHelperService;

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>TEAM_CAPACITY</tt> enums
	 */
	@Override
	public String getQualifierType() {
		return KPICode.TEAM_CAPACITY.name();
	}

	/**
	 * Gets KPI Data
	 *
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return KpiElement
	 * @throws ApplicationException
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes()
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);
		log.debug("[TEAM CAPACITY-KANBAN-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.TEAM_CAPACITY);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.TEAM_CAPACITY);

		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);
		kpiElement.setTrendValueList(trendValues);
		log.debug("[TEAM CAPACITY-KANBAN-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	/**
	 * Calculates KPI Metrics
	 *
	 * @param subCategoryMap
	 */
	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	/**
	 * Fetches KPI Data from DB
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return resultListMap
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return kpiHelperService.fetchTeamCapacityDataFromDb(leafNodeList, startDate, endDate, kpiRequest, TICKET_LIST);
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint wise.
	 *
	 * @param mapTmp
	 * @param leafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);

		String startDate = dateRange.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String endDate = dateRange.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		Map<String, Object> capacityMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);
		String subGroupCategory = (String) capacityMap.get(SUBGROUPCATEGORY);
		Map<String, Map<String, List<KanbanCapacity>>> projectAndDateWiseCapacityMap = KpiDataHelper
				.createDateWiseCapacityMap((List<KanbanCapacity>) capacityMap.get(TICKET_LIST), subGroupCategory,
						filterHelperService);
		kpiWithoutFilter(projectAndDateWiseCapacityMap, mapTmp, leafNodeList, kpiElement, kpiRequest);
	}

	private void kpiWithoutFilter(Map<String, Map<String, List<KanbanCapacity>>> projectAndDateWiseCapacityMap,
			Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getKanbanRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		leafNodeList.forEach(node -> {
			String projectNodeId = node.getProjectFilter().getId();
			Map<String, List<KanbanCapacity>> dateWiseKanbanCapacity = projectAndDateWiseCapacityMap
					.get(node.getProjectFilter().getBasicProjectConfigId().toString());
			if (MapUtils.isNotEmpty(dateWiseKanbanCapacity)) {
				LocalDate currentDate = LocalDate.now();
				List<DataCount> dataCount = new ArrayList<>();
				for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {
					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
							kpiRequest.getDuration());
					String projectName = projectNodeId.substring(0,
							projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
					Double capacity = filterDataBasedOnStartAndEndDate(dateWiseKanbanCapacity, dateRange, projectName);
					String date = getRange(dateRange, kpiRequest);
					dataCount.add(getDataCountObject(capacity, projectName, date));
					currentDate = getNextRangeDate(kpiRequest, currentDate);
					if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
						KPIExcelUtility.populateTeamCapacityKanbanExcelData(capacity, excelData, projectName, dateRange,
								kpiRequest.getDuration());
					}
				}
				mapTmp.get(node.getId()).setValue(dataCount);
			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.TEAM_CAPACITY_KANBAN.getColumns());
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
		Map<String, Object> hoverValue = new HashMap<>();
		hoverValue.put("Total Capacity", new Double(String.format("%.1f", value)));
		dataCount.setHoverValue(hoverValue);
		return dataCount;
	}

	private Double filterDataBasedOnStartAndEndDate(Map<String, List<KanbanCapacity>> dateWiseKanbanCapacity,
			CustomDateRange dateRange, String projectName) {
		List<KanbanCapacity> kanbanCapacityList = new ArrayList<>();

		Double capacity = 0.0d;
		for (LocalDate currentDate = dateRange.getStartDate(); currentDate.compareTo(dateRange.getStartDate()) >= 0
				&& dateRange.getEndDate().compareTo(currentDate) >= 0; currentDate = currentDate.plusDays(1)) {
			List<KanbanCapacity> dummyList = new ArrayList<>();
			dummyList.add(KanbanCapacity.builder().capacity(0.0d).startDate(currentDate).endDate(currentDate)
					.projectName(projectName).build());
			kanbanCapacityList.addAll(dateWiseKanbanCapacity.getOrDefault(currentDate.toString(), dummyList));
		}
		if (CollectionUtils.isNotEmpty(kanbanCapacityList)) {
			capacity = kanbanCapacityList.stream().mapToDouble(kanbanCapacity -> kanbanCapacity.getCapacity()).sum();
		}
		return capacity;
	}

	/**
	 *
	 * @param kpiRequest
	 * @param currentDate
	 * @return
	 */
	private LocalDate getNextRangeDate(KpiRequest kpiRequest, LocalDate currentDate) {
		if ((CommonConstant.WEEK).equalsIgnoreCase(kpiRequest.getDuration())) {
			currentDate = currentDate.minusWeeks(1);
		} else if (CommonConstant.MONTH.equalsIgnoreCase(kpiRequest.getDuration())) {
			currentDate = currentDate.minusMonths(1);
		} else {
			currentDate = currentDate.minusDays(1);
		}
		return currentDate;
	}

	/**
	 * particulate date format given as per date type
	 *
	 * @param dateRange
	 * @param kpiRequest
	 */
	private String getRange(CustomDateRange dateRange, KpiRequest kpiRequest) {
		String range = null;
		if (CommonConstant.WEEK.equalsIgnoreCase(kpiRequest.getDuration())) {
			range = DateUtil.localDateTimeConverter(dateRange.getStartDate()) + " to "
					+ DateUtil.localDateTimeConverter(dateRange.getEndDate());
		} else if (CommonConstant.MONTH.equalsIgnoreCase(kpiRequest.getDuration())) {
			range = dateRange.getStartDate().getMonth().toString() + " " + dateRange.getStartDate().getYear();
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

}