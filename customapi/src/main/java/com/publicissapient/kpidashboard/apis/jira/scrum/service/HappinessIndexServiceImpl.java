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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.UserRatingData;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HappinessIndexServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String SPRINT_DETAILS = "sprints";
	private static final String HAPPINESS_INDEX_DETAILS = "happinessIndexDetails";
	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private HappinessKpiDataRepository happinessKpiDataRepository;

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>HAPPINESS_INDEX_RATE</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.HAPPINESS_INDEX_RATE.name();
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
		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}

		});

		log.debug("[HAPPINESS-INDEX-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.HAPPINESS_INDEX_RATE);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.HAPPINESS_INDEX_RATE);
		kpiElement.setTrendValueList(trendValues);

		return kpiElement;
	}

	/**
	 * Populates KPI value to sprint leaf nodes andgives the trend analysis at
	 * sprint wise.
	 *
	 * @param mapTmp
	 * @param sprintLeafNodeList
	 * @param trendValueList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		String startDate;
		String endDate;

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));

		startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();
		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);

		Map<Pair<String, String>, List<Integer>> sprintWiseHappinessIndexNumbers = new HashMap<>();

		List<SprintDetails> sprintDetails = (List<SprintDetails>) resultMap.get(SPRINT_DETAILS);
		List<HappinessKpiData> happinessKpiDataList = (List<HappinessKpiData>) resultMap.get(HAPPINESS_INDEX_DETAILS);

		if (CollectionUtils.isNotEmpty(sprintDetails) && CollectionUtils.isNotEmpty(happinessKpiDataList)) {
			sprintDetails.forEach(sd -> {

				// Finding total ratings for a particular project and sprint
				List<Integer> totalRatings = happinessKpiDataList.stream()
						.filter(data -> data.getBasicProjectConfigId().toString().equals(
								sd.getBasicProjectConfigId().toString()) && data.getSprintID().equals(sd.getSprintID()))
						.flatMap(filteredData -> filteredData.getUserRatingList().stream()
								.map(UserRatingData::getRating))
						.filter(Objects::nonNull).collect(Collectors.toList());

				sprintWiseHappinessIndexNumbers.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						totalRatings);

			});
		}

		List<KPIExcelData> excelData = new ArrayList<>();

		for (Node node : sprintLeafNodeList) {

			// Leaf node wise data
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);
			Double happinessIndexValue = 0.0;

			if (CollectionUtils.isNotEmpty(sprintWiseHappinessIndexNumbers.get(currentNodeIdentifier))) {
				List<Double> totalRatingsValue = sprintWiseHappinessIndexNumbers.get(currentNodeIdentifier).stream()
						.map(Integer::doubleValue).collect(Collectors.toList());
				happinessIndexValue = calculateKpiValue(totalRatingsValue, KPICode.HAPPINESS_INDEX_RATE.getKpiId());
				populateExcelData(requestTrackerId, excelData, node, happinessKpiDataList);
			}

			log.debug("[HAPPINESS-INDEX-SPRINT-WISE][{}]. happiness index for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), happinessIndexValue);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(happinessIndexValue));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setValue(happinessIndexValue);
			dataCount.setHoverValue(new HashMap<>());
			mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(Arrays.asList(dataCount)));
			trendValueList.add(dataCount);

		}

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.HAPPINESS_INDEX_RATE.getColumns());

	}

	/**
	 * Populates Validation Data Object
	 *
	 * @param requestTrackerId
	 * @param happinessKpiDataList
	 * @param node
	 * @param excelData
	 */
	private void populateExcelData(String requestTrackerId, List<KPIExcelData> excelData, Node node,
			List<HappinessKpiData> happinessKpiDataList) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			String sprintName = node.getSprintFilter().getName();
			String sprintId = node.getSprintFilter().getId();
			List<HappinessKpiData> happinessKpiSprintDataList = happinessKpiDataList.stream()
					.filter(data -> data.getSprintID().equals(sprintId)).collect(Collectors.toList());
			KPIExcelUtility.populateHappinessIndexExcelData(sprintName, excelData, happinessKpiSprintDataList);
		}

	}

	/**
	 * Calculates KPI Metrics
	 *
	 * @param stringObjectMap
	 * @return Integer
	 */
	@Override
	public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		String requestTrackerId = getRequestTrackerId();

		log.debug("[HAPPINESS INDEX VALUE][{}].Total Happiness Index Value: {}", requestTrackerId, stringObjectMap);
		return 0.0d;
	}

	/**
	 * Fetches KPI Data from DB
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return {@code Map<String, Object>}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		List<String> sprintList = new ArrayList<>();
		Map<String, Object> resultListMap = new HashMap<>();

		leafNodeList.forEach(leaf -> sprintList.add(leaf.getSprintFilter().getId()));

		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDIn(sprintList);
		List<HappinessKpiData> happinessKpiDataList = happinessKpiDataRepository.findBySprintIDIn(sprintList);
		// filtering rating of 0 i.e not entered any rating
		happinessKpiDataList.forEach(happinessKpiData -> happinessKpiData.getUserRatingList().removeIf(
				userRatingData -> userRatingData.getRating() == null || userRatingData.getRating().equals(0)));
		resultListMap.put(SPRINT_DETAILS, sprintDetails);
		resultListMap.put(HAPPINESS_INDEX_DETAILS, happinessKpiDataList);

		return resultListMap;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI149(), KPICode.HAPPINESS_INDEX_RATE.getKpiId());
	}

}
