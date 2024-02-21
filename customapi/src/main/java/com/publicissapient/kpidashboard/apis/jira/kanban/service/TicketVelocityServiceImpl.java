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
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
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
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This class calculates the Ticket Velocity and trend analysis.
 *
 * @author pkum34
 *
 */
@Component
@Slf4j
public class TicketVelocityServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String TICKETVELOCITYKEY = "ticketVelocityKey";
	private static final String SUBGROUPCATEGORY = "subGroupCategory";
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private FilterHelperService filterHelperService;

	@Override
	public String getQualifierType() {
		return KPICode.TICKET_VELOCITY.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes()
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);
		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		log.debug("[TICKET-VELOCITY-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.TICKET_VELOCITY);

		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.TICKET_VELOCITY);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);
		kpiElement.setTrendValueList(trendValues);

		log.debug("[TICKET-VELOCITY-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return kpiHelperService.fetchTicketVelocityDataFromDb(leafNodeList, startDate, endDate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Double calculateKPIMetrics(Map<String, Object> techDebtStoryMap) {

		String requestTrackerId = getRequestTrackerId();
		Double ticketVelocity = 0.0d;
		List<KanbanIssueCustomHistory> ticketVelocityList = (List<KanbanIssueCustomHistory>) techDebtStoryMap
				.get(TICKETVELOCITYKEY);
		log.debug("[TICKET-VELOCITY][{}]. Ticket Count: {}", requestTrackerId, ticketVelocityList.size());
		for (KanbanIssueCustomHistory feature : ticketVelocityList) {
			ticketVelocity = ticketVelocity + Double.valueOf(feature.getEstimate());
		}
		return ticketVelocity;
	}

	/**
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

		String startDate = dateRange.getStartDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
		String endDate = dateRange.getEndDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT));

		Map<String, Object> ticketVelocityStoryMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);

		Map<String, Map<String, List<KanbanIssueCustomHistory>>> projectAndDateWiseStoryMap = createDateWiseKanbanHistMap(
				(List<KanbanIssueCustomHistory>) ticketVelocityStoryMap.get(TICKETVELOCITYKEY),
				(String) ticketVelocityStoryMap.get(SUBGROUPCATEGORY), filterHelperService);

		kpiWithoutFilter(projectAndDateWiseStoryMap, mapTmp, leafNodeList, kpiElement, kpiRequest);

	}

	private void kpiWithoutFilter(Map<String, Map<String, List<KanbanIssueCustomHistory>>> projectAndDateWiseStoryMap,
			Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getKanbanRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		leafNodeList.forEach(node -> {
			String projectNodeId = node.getProjectFilter().getId();
			String basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId().toString();
			Map<String, List<KanbanIssueCustomHistory>> dateWiseStoryMap = projectAndDateWiseStoryMap
					.get(basicProjectConfigId);
			if (MapUtils.isNotEmpty(dateWiseStoryMap)) {
				LocalDate currentDate = LocalDate.now();
				List<DataCount> dataCount = new ArrayList<>();

				for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {
					List<KanbanIssueCustomHistory> kanbanIssueCustomHistories = new ArrayList<>();
					String projectName = projectNodeId.substring(0,
							projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
							kpiRequest.getDuration());

					Double capacity = filterDataBasedOnStartAndEndDate(dateWiseStoryMap, dateRange,
							kanbanIssueCustomHistories);
					String date = getRange(dateRange, kpiRequest);
					dataCount.add(getDataCountObject(capacity, projectName, date));
					currentDate = getNextRangeDate(kpiRequest, currentDate);
					if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
						KPIExcelUtility.populateTicketVelocityExcelData(kanbanIssueCustomHistories, projectName, date,
								excelData);
					}
				}
				mapTmp.get(node.getId()).setValue(dataCount);

			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.TICKET_VELOCITY.getColumns());
	}

	private Map<String, Map<String, List<KanbanIssueCustomHistory>>> createDateWiseKanbanHistMap(
			List<KanbanIssueCustomHistory> kanbanIssueCustomHistories, String subGroupCategory,
			FilterHelperService flterHelperService) {
		Map<String, AdditionalFilterCategory> addFilterCat = flterHelperService.getAdditionalFilterHierarchyLevel();
		List<String> addFilterCategoryList = new ArrayList(addFilterCat.keySet());
		Map<String, Map<String, List<KanbanIssueCustomHistory>>> projectAndDateWiseTicketMap = new HashMap<>();
		if (Constant.DATE.equals(subGroupCategory) || addFilterCategoryList.contains(subGroupCategory)) {
			projectAndDateWiseTicketMap = kanbanIssueCustomHistories.stream().collect(Collectors.groupingBy(
					KanbanIssueCustomHistory::getBasicProjectConfigId,
					Collectors.groupingBy(f -> LocalDate
							.parse(f.getHistoryDetails().get(0).getActivityDate().split("\\.")[0], DATE_TIME_FORMATTER)
							.toString())));
		}
		return projectAndDateWiseTicketMap;
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
		hoverValue.put("Total Velocity", value.intValue());
		dataCount.setHoverValue(hoverValue);
		return dataCount;
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
			range = DateUtil.dateTimeConverter(dateRange.getStartDate().toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(dateRange.getEndDate().toString(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
		} else if (CommonConstant.MONTH.equalsIgnoreCase(kpiRequest.getDuration())) {
			range = dateRange.getStartDate().getMonth().toString() + " " + dateRange.getStartDate().getYear();
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	private Double filterDataBasedOnStartAndEndDate(Map<String, List<KanbanIssueCustomHistory>> dateWiseStoryMap,
			CustomDateRange dateRange, List<KanbanIssueCustomHistory> totalTicket) {
		List<KanbanIssueCustomHistory> dummyList = new ArrayList<>();

		for (LocalDate currentDate = dateRange.getStartDate(); currentDate.compareTo(dateRange.getStartDate()) >= 0
				&& dateRange.getEndDate().compareTo(currentDate) >= 0; currentDate = currentDate.plusDays(1)) {
			dummyList.add(KanbanIssueCustomHistory.builder().estimate("0").build());

			totalTicket.addAll(dateWiseStoryMap.getOrDefault(currentDate.toString(), dummyList));
		}
		Double ticketEstimate = 0.0d;
		if (CollectionUtils.isNotEmpty(totalTicket)) {
			ticketEstimate = totalTicket.stream().mapToDouble(value -> Double.parseDouble(value.getEstimate())).sum();
		}
		return ticketEstimate;

	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}
}
