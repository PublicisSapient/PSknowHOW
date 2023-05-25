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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
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
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogCustomHistoryQueryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.IssueBacklogCustomHistoryRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FlowDistributionServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final String DATE_TYPE_COUNT_MAP = "dateTypeCountMap";
	public static final String COUNT = "count";
	public static final String TYPE_COUNT_MAP = "typeCountMap";
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private IssueBacklogCustomHistoryQueryRepository issueBacklogCustomHistoryQueryRepository;
	@Autowired
	private IssueBacklogCustomHistoryRepository issueBacklogCustomHistoryRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(FlowDistributionServiceImpl.class);

	@Override
	public String getQualifierType() {
		return KPICode.FLOW_DISTRIBUTION.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(v, trendValueList, kpiElement, kpiRequest);
			}
		});
		return kpiElement;
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);

		if (leafNode != null) {
			LOGGER.info("Flow Distribution kpi -> Requested project : {}", leafNode.getProjectFilter().getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			List<Map> mappedResults = issueBacklogCustomHistoryQueryRepository
					.getStoryTypeCountByDateRange(basicProjectConfigId, startDate, endDate);

			// Creating dateWiseTypeCount map
			Map<String, Map<String, Integer>> resultMap = new HashMap<>();
			mappedResults.forEach(map -> {
				String date = (String) map.get("date");
				List<Map<String, Object>> typeCountList = (List<Map<String, Object>>) map.get(TYPE_COUNT_MAP);

				Map<String, Integer> typeCountMap = typeCountList.stream()
						.collect(Collectors.toMap(typeCount -> ((String) typeCount.get("type")).replace(" ", "_"),
								typeCount -> (int) typeCount.get(COUNT)));

				resultMap.put(date, typeCountMap);
			});
			resultListMap.put(DATE_TYPE_COUNT_MAP, resultMap);
		}

		return resultListMap;
	}

	private void projectWiseLeafNodeValue(List<Node> leafNode, List<DataCount> trendValueList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		// this method fetch dates for past history data
		CustomDateRange dateRange = KpiDataHelper.getMonthsForPastDataHistory(customApiConfig.getFlowKpiMonthCount());

		// get start and end date in yyyy-mm-dd format
		String startDate = dateRange.getStartDate().format(DATE_FORMATTER);
		String endDate = dateRange.getEndDate().format(DATE_FORMATTER);
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNode, startDate, endDate, kpiRequest);
		Map<String, Map<String, Integer>> dateTypeCountMap = (Map<String, Map<String, Integer>>) resultMap
				.get(DATE_TYPE_COUNT_MAP);
		if (!Objects.isNull(dateTypeCountMap)) {

			Map<String, Map<String, Integer>> cumulativeAddedCountMap = new LinkedHashMap<>();

			LocalDate currentDate = LocalDate.parse(startDate);
			LocalDate lastDate = LocalDate.parse(endDate);
			Map<String, Integer> accumulatedMap = null;

			while (!currentDate.isAfter(lastDate)) {
				String currentDateString = currentDate.toString();
				Map<String, Integer> currentMap = dateTypeCountMap.getOrDefault(currentDateString, new HashMap<>());

				if (accumulatedMap != null) {
					for (Map.Entry<String, Integer> entry : currentMap.entrySet()) {
						String key = entry.getKey();
						int value = entry.getValue();
						accumulatedMap.put(key, accumulatedMap.getOrDefault(key, 0) + value);
					}
				} else {
					accumulatedMap = new HashMap<>(currentMap);
				}
				cumulativeAddedCountMap.put(currentDateString, new HashMap<>(accumulatedMap));
				currentDate = currentDate.plusDays(1);
			}
			populateTrendValueList(trendValueList, cumulativeAddedCountMap);
			populateExcelDataObject(requestTrackerId, excelData, dateTypeCountMap);
			LOGGER.info("FlowDistributionServiceImpl -> request id : {} dateWiseCountMap : {}", requestTrackerId,
					dateTypeCountMap);
		}
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.FLOW_DISTRIBUTION.getColumns());
		kpiElement.setTrendValueList(trendValueList);
	}

	private void populateTrendValueList(List<DataCount> dataList, Map<String, Map<String, Integer>> dateTypeCountMap) {
		for (Map.Entry<String, Map<String, Integer>> entry : dateTypeCountMap.entrySet()) {
			String date = entry.getKey();
			Map<String, Integer> typeCountMap = entry.getValue();
			DataCount dc = new DataCount();
			dc.setDate(date);
			dc.setValue(typeCountMap);
			dataList.add(dc);
		}
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			Map<String, Map<String, Integer>> dateTypeCountMap) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
				&& !Objects.isNull(dateTypeCountMap)) {
			KPIExcelUtility.populateFlowDistribution(dateTypeCountMap, excelData);
		}
	}
}
