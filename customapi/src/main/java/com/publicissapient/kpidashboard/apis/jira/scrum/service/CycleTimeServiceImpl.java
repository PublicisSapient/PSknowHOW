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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.CommonServiceImpl;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.AggregationUtils;
import com.publicissapient.kpidashboard.apis.util.BacklogKpiHelper;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.CycleTime;
import com.publicissapient.kpidashboard.common.model.application.CycleTimeValidationData;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Cycle Time KPI on BackLog Tab
 * 
 * @author shi6
 */
@Slf4j
@Component
public class CycleTimeServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {
	private static final String STORY_HISTORY_DATA = "storyHistoryData";
	private static final String INTAKE_TO_DOR = "Intake to DOR";
	private static final String DOR_TO_DOD = "DOR to DOD";
	private static final String DOD_TO_LIVE = "DOD to Live";

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CommonServiceImpl commonService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.PROJECT) {
				projectWiseLeafNodeValue(v, kpiElement, kpiRequest);
			}
		});
		log.info("CycleTimeServiceImpl -> getKpiData ->  : {}", kpiElement);
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
		List<KPIExcelData> excelData = new ArrayList<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (leafNode != null) {
			Object basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			List<String> rangeList = customApiConfig.getLeadTimeRange();
			Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, LocalDate.now().minusMonths(6).toString(),
					LocalDate.now().toString(), kpiRequest);

			List<JiraIssueCustomHistory> jiraIssueCustomHistoriesList = (List<JiraIssueCustomHistory>) resultMap
					.get(STORY_HISTORY_DATA);
			List<IterationKpiValue> iterationKpiValueList = new ArrayList<>();
			Set<String> allIssueTypes = new HashSet<>();

			if (CollectionUtils.isNotEmpty(jiraIssueCustomHistoriesList)) {
				List<CycleTimeValidationData> cycleTimeValidationDataList = new ArrayList<>();
				allIssueTypes = jiraIssueCustomHistoriesList.stream().map(JiraIssueCustomHistory::getStoryType)
						.collect(Collectors.toSet());
				iterationKpiValueList = getCycleTime(jiraIssueCustomHistoriesList, fieldMapping,
						cycleTimeValidationDataList, rangeList, allIssueTypes);
				populateExcelDataObject(getRequestTrackerId(), cycleTimeValidationDataList, excelData);
				kpiElement.setModalHeads(KPIExcelColumn.CYCLE_TIME.getColumns());
				kpiElement.setExcelColumns(KPIExcelColumn.CYCLE_TIME.getColumns());
				kpiElement.setExcelData(excelData);
			}
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions("Search By Range",
					new HashSet<>(rangeList));
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions("Search By IssueType",
					new HashSet<>(allIssueTypes));
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setTrendValueList(iterationKpiValueList);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		List<String> basicProjectConfigIds = new ArrayList<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			basicProjectConfigIds.add(basicProjectConfigId.toString());

			if (Optional.ofNullable(fieldMapping.getJiraIssueTypeKPI171()).isPresent()) {

				KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters,
						fieldMapping.getJiradefecttype(), fieldMapping.getJiraIssueTypeKPI171(),
						JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature());
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

			}
			List<String> status = new ArrayList<>();
			if (Optional.ofNullable(fieldMapping.getJiraDodKPI171()).isPresent()) {
				status.addAll(fieldMapping.getJiraDodKPI171());
			}

			if (Optional.ofNullable(fieldMapping.getJiraDorKPI171()).isPresent()) {
				status.addAll(fieldMapping.getJiraDorKPI171());
			}

			if (Optional.ofNullable(fieldMapping.getJiraLiveStatusKPI171()).isPresent()) {
				status.addAll(fieldMapping.getJiraLiveStatusKPI171());
			}
			mapOfProjectFilters.put("statusUpdationLog.story.changedTo", CommonUtils.convertToPatternList(status));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		resultListMap.put(STORY_HISTORY_DATA, jiraIssueCustomHistoryRepository
				.findByFilterAndFromStatusMapWithDateFilter(mapOfFilters, uniqueProjectMap, startDate, endDate));

		return resultListMap;
	}

	/**
	 * prepareLeadtime list
	 *
	 * @param jiraIssueCustomHistoriesList
	 *            historylist
	 * @param fieldMapping
	 *            fieldMapping
	 * @param cycleTimeValidationDataList
	 *            cycleTimeValidationDataList
	 * @param rangeList
	 *            rangeList
	 * @param allIssueTypes
	 * @return dataCountMap
	 */
	private List<IterationKpiValue> getCycleTime(List<JiraIssueCustomHistory> jiraIssueCustomHistoriesList,
			FieldMapping fieldMapping, List<CycleTimeValidationData> cycleTimeValidationDataList,
			List<String> rangeList, Set<String> allIssueTypes) {
		Map<Long, String> monthRangeMap = new HashMap<>();
		Map<String, Map<String, List<JiraIssueCustomHistory>>> dodToLiveRangeMap = new LinkedHashMap<>();
		BacklogKpiHelper.initializeRangeMapForProjects(dodToLiveRangeMap, rangeList, monthRangeMap);
		Map<String, Map<String, List<JiraIssueCustomHistory>>> dorToDODRangeMap = new LinkedHashMap<>(
				dodToLiveRangeMap);
		Map<String, Map<String, List<JiraIssueCustomHistory>>> intakeToDORRangeMap = new LinkedHashMap<>(
				dodToLiveRangeMap);

		for (JiraIssueCustomHistory jiraIssueCustomHistory : jiraIssueCustomHistoriesList) {
			CycleTimeValidationData cycleTimeValidationData = new CycleTimeValidationData();
			cycleTimeValidationData.setIssueNumber(jiraIssueCustomHistory.getStoryID());
			cycleTimeValidationData.setUrl(jiraIssueCustomHistory.getUrl());
			cycleTimeValidationData.setIssueDesc(jiraIssueCustomHistory.getDescription());
			cycleTimeValidationData.setIssueType(jiraIssueCustomHistory.getStoryType());
			CycleTime cycleTime = new CycleTime();
			cycleTime.setIntakeTime(jiraIssueCustomHistory.getCreatedDate());
			cycleTimeValidationData.setIntakeDate(jiraIssueCustomHistory.getCreatedDate());

			List<String> liveStatus = fieldMapping.getJiraLiveStatusKPI171().stream().filter(Objects::nonNull)
					.map(String::toLowerCase).collect(Collectors.toList());
			List<String> dodStatus = fieldMapping.getJiraDodKPI171().stream().filter(Objects::nonNull)
					.map(String::toLowerCase).collect(Collectors.toList());
			String storyFirstStatus = fieldMapping.getStoryFirstStatusKPI171();
			List<String> dor = fieldMapping.getJiraDorKPI171().stream().filter(Objects::nonNull)
					.map(String::toLowerCase).collect(Collectors.toList());

			Map<String, DateTime> dodStatusDateMap = new HashMap<>();
			jiraIssueCustomHistory.getStatusUpdationLog().forEach(statusUpdateLog -> {
				DateTime updateTime = DateTime.parse(statusUpdateLog.getUpdatedOn().toString());
				BacklogKpiHelper.setLiveTime(cycleTimeValidationData, cycleTime, statusUpdateLog, updateTime,
						liveStatus);
				BacklogKpiHelper.setReadyTime(cycleTimeValidationData, cycleTime, statusUpdateLog, updateTime, dor);
				BacklogKpiHelper.setDODTime(cycleTimeValidationData, cycleTime, statusUpdateLog, updateTime, dodStatus,
						storyFirstStatus, dodStatusDateMap);
			});

			DateTime minUpdatedOn = Collections.min(dodStatusDateMap.values());
			cycleTime.setDeliveryTime(minUpdatedOn);
			cycleTime.setDeliveryLocalDateTime(DateUtil.convertDateTimeToLocalDateTime(minUpdatedOn));
			cycleTimeValidationData.setDodDate(minUpdatedOn);

			BacklogKpiHelper.setRangeWiseJiraIssuesMap(intakeToDORRangeMap, jiraIssueCustomHistory,
					cycleTime.getReadyLocalDateTime(), monthRangeMap);
			BacklogKpiHelper.setRangeWiseJiraIssuesMap(dorToDODRangeMap, jiraIssueCustomHistory,
					cycleTime.getDeliveryLocalDateTime(), monthRangeMap);
			BacklogKpiHelper.setRangeWiseJiraIssuesMap(dodToLiveRangeMap, jiraIssueCustomHistory,
					cycleTime.getLiveLocalDateTime(), monthRangeMap);

			BacklogKpiHelper.setValueInCycleTime(cycleTime.getIntakeTime(), cycleTime.getReadyTime(), INTAKE_TO_DOR,
					cycleTimeValidationData);
			BacklogKpiHelper.setValueInCycleTime(cycleTime.getReadyTime(), cycleTime.getDeliveryTime(), DOR_TO_DOD,
					cycleTimeValidationData);
			BacklogKpiHelper.setValueInCycleTime(cycleTime.getDeliveryTime(), cycleTime.getLiveTime(), DOD_TO_LIVE,
					cycleTimeValidationData);
			cycleTimeValidationDataList.add(cycleTimeValidationData);
		}

		return setDataCountMap(intakeToDORRangeMap, dorToDODRangeMap, dodToLiveRangeMap, cycleTimeValidationDataList,
				rangeList, allIssueTypes);

	}

	private List<IterationKpiValue> setDataCountMap(
			Map<String, Map<String, List<JiraIssueCustomHistory>>> intakeToDORRangeMap,
			Map<String, Map<String, List<JiraIssueCustomHistory>>> dorToDODRangeMap,
			Map<String, Map<String, List<JiraIssueCustomHistory>>> dodToLiveRangeMap,
			List<CycleTimeValidationData> cycleTimeValidationDataList, List<String> rangeList,
			Set<String> allIssueTypes) {

		Map<String, Long> issueWiseIntakeToDOR = cycleTimeValidationDataList.stream()
				.filter(data->ObjectUtils.isNotEmpty(data.getIntakeTime()))
				.collect(Collectors.toMap(CycleTimeValidationData::getIssueNumber,
						CycleTimeValidationData::getIntakeTime, (existing, replacement) -> existing));

		Map<String, Long> issueWiseDORToDOD = cycleTimeValidationDataList.stream()
				.filter(data->ObjectUtils.isNotEmpty(data.getDorTime()))
				.collect(Collectors.toMap(CycleTimeValidationData::getIssueNumber, CycleTimeValidationData::getDorTime,
						(existing, replacement) -> existing));

		Map<String, Long> issueWiseDODToLive = cycleTimeValidationDataList.stream()
				.filter(data->ObjectUtils.isNotEmpty(data.getDodTime()))
				.collect(Collectors.toMap(CycleTimeValidationData::getIssueNumber, CycleTimeValidationData::getDodTime,
						(existing, replacement) -> existing));

		List<IterationKpiValue> iterationKpiValueList = new ArrayList<>();
		for (String range : rangeList) {
			for (String issueType : allIssueTypes) {
				IterationKpiValue kpiValueIssueCount = new IterationKpiValue();
				kpiValueIssueCount.setFilter1(range);
				kpiValueIssueCount.setFilter2(issueType);
				List<DataCount> defectsDataCountList = new ArrayList<>();
				extracted(intakeToDORRangeMap, issueWiseIntakeToDOR, range, issueType, defectsDataCountList,
						INTAKE_TO_DOR);
				extracted(dorToDODRangeMap, issueWiseDORToDOD, range, issueType, defectsDataCountList, DOR_TO_DOD);
				extracted(dodToLiveRangeMap, issueWiseDODToLive, range, issueType, defectsDataCountList, DOD_TO_LIVE);
				kpiValueIssueCount.setValue(defectsDataCountList);
				iterationKpiValueList.add(kpiValueIssueCount);
			}

		}
		return iterationKpiValueList;
	}

	private void extracted(Map<String, Map<String, List<JiraIssueCustomHistory>>> rangeWiseTypeWiseIssue,
			Map<String, Long> issueWiseGroupValue, String range, String issueType, List<DataCount> defectsDataCountList,
			String kpiGroup) {
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = rangeWiseTypeWiseIssue.get(range).get(issueType);
		if(CollectionUtils.isNotEmpty(jiraIssueCustomHistories)) {
			List<Long> valueList = jiraIssueCustomHistories.stream().map(JiraIssueCustomHistory::getStoryID)
					.filter(issueWiseGroupValue::containsKey).map(issueWiseGroupValue::get).collect(Collectors.toList());
			defectsDataCountList.add(getStatusWiseCountList(AggregationUtils.averageLong(valueList), kpiGroup));
		}
	}


	private DataCount getStatusWiseCountList(Long averageValue, String kpiGroup) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(averageValue));
		dataCount.setKpiGroup(kpiGroup);
		return dataCount;
	}

	/**
	 *
	 * @param requestTrackerId
	 *            requestTrackerId
	 * @param cycleTimeValidationDataList
	 *            leadTimeList
	 * @param excelData
	 *            excelData
	 */
	private void populateExcelDataObject(String requestTrackerId,
			List<CycleTimeValidationData> cycleTimeValidationDataList, List<KPIExcelData> excelData) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateCycleTime(cycleTimeValidationDataList, excelData);
		}

	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public String getQualifierType() {
		return KPICode.CYCLE_TIME.name();
	}

}
