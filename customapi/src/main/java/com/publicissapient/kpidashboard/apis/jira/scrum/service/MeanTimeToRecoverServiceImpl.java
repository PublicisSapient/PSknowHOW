/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.HIERARCHY_LEVEL_ID_PROJECT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.MeanTimeRecoverData;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This service for managing Mean Time to Recover kpi for dora tab.
 * {@link JiraKPIService}
 *
 * @author shubh
 */
@Component
@Slf4j
public class MeanTimeToRecoverServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	private static final String JIRA_HISTORY_DATA = "jiraIssueHistoryData";
	private static final String STORY_ID = "storyID";
	private static final String PRODUCTION_INCIDENT = "productionIncident";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);
		projectWiseLeafNodeValue(mapTmp, projectList, kpiElement);

		log.debug("[MEAN-TIME-TO-RECOVER-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.MEAN_TIME_TO_RECOVER);
		List<DataCount> trendValues = getAggregateTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.MEAN_TIME_TO_RECOVER);
		kpiElement.setTrendValueList(trendValues);

		return kpiElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		List<String> projectBasicConfigIdList = new ArrayList<>();
		List<String> projectProdIncidentIdentifier = new ArrayList<>();
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, List<String>> mapOfFiltersFH = new LinkedHashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMapFH = new HashMap<>();

		leafNodeList.forEach(leafNode -> {
			ObjectId basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFiltersFH = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			if (CollectionUtils.isNotEmpty(fieldMapping.getJiraStoryIdentificationKPI166())) {
				mapOfProjectFiltersFH.put(JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getJiraStoryIdentificationKPI166()));
			} // project for which prod incident is configured via custom or label
			String jiraProductionIncidentIdentification = ObjectUtils
					.defaultIfNull(fieldMapping.getJiraProductionIncidentIdentification(), "");
			if (jiraProductionIncidentIdentification.equalsIgnoreCase(CommonConstant.CUSTOM_FIELD)
					|| jiraProductionIncidentIdentification.equalsIgnoreCase(CommonConstant.LABELS)) {
				projectProdIncidentIdentifier.add(basicProjectConfigId.toString());
			}
			uniqueProjectMapFH.put(basicProjectConfigId.toString(), mapOfProjectFiltersFH);
			projectBasicConfigIdList.add(basicProjectConfigId.toString());
		});

		List<String> distinctProjBasicConfig = projectBasicConfigIdList.stream().distinct()
				.collect(Collectors.toList());
		List<String> distinctProjConfigIncidentList = projectProdIncidentIdentifier.stream().distinct()
				.collect(Collectors.toList());
		// creating map of only prod incident configured proj for query
		if (CollectionUtils.isNotEmpty(distinctProjConfigIncidentList)) {
			mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
					distinctProjConfigIncidentList);
		}

		mapOfFiltersFH.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(), distinctProjBasicConfig);

		List<JiraIssueCustomHistory> historyDataList = new ArrayList<>();
		List<JiraIssue> jiraIssueList = new ArrayList<>();
		// only call this when mapOfFilters is not empty.
		if (MapUtils.isNotEmpty(mapOfFilters)) {
			jiraIssueList = jiraIssueRepository.findIssuesWithBoolean(mapOfFilters, PRODUCTION_INCIDENT, Boolean.TRUE,
					startDate, endDate);
		}

		if (CollectionUtils.isEmpty(jiraIssueList)) {
			// fetching history for selected story type
			historyDataList = jiraIssueCustomHistoryRepository.findIssuesByCreatedDateAndType(mapOfFiltersFH,
					uniqueProjectMapFH, startDate, endDate);
		}
		if (CollectionUtils.isNotEmpty(jiraIssueList)) {
			List<String> issueIdList = jiraIssueList.stream().map(JiraIssue::getNumber).collect(Collectors.toList());

			mapOfFiltersFH.put(STORY_ID, issueIdList);
			// fetching history for the production incident tickets
			historyDataList = jiraIssueCustomHistoryRepository.findIssuesByCreatedDateAndType(mapOfFiltersFH,
					uniqueProjectMapFH, startDate, endDate);
		}
		resultListMap.put(JIRA_HISTORY_DATA, historyDataList);
		return resultListMap;

	}

	/**
	 * calculate and set project wise leaf node value
	 *
	 * @param mapTmp
	 *            map tmp data
	 * @param projectLeafNodeList
	 *            projectLeafNodeList
	 * @param kpiElement
	 *            kpiElement
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> projectLeafNodeList,
			KpiElement kpiElement) {
		Map<String, Object> durationFilter = KpiDataHelper.getDurationFilter(kpiElement);
		LocalDateTime localStartDate = (LocalDateTime) durationFilter.get(Constant.DATE);
		String startDate = localStartDate.toLocalDate().toString();
		String endDate = LocalDate.now().toString();

		List<KPIExcelData> excelData = new ArrayList<>();
		Map<String, Object> resultMap = fetchKPIDataFromDb(projectLeafNodeList, startDate, endDate, null);

		if (MapUtils.isNotEmpty(resultMap)) {
			String requestTrackerId = getRequestTrackerId();

			List<JiraIssueCustomHistory> historyDataList = (List<JiraIssueCustomHistory>) resultMap
					.get(JIRA_HISTORY_DATA);

			Map<String, List<JiraIssueCustomHistory>> projectWiseJiraIssueHistoryDataList = historyDataList.stream()
					.collect(Collectors.groupingBy(JiraIssueCustomHistory::getBasicProjectConfigId));

			projectLeafNodeList.forEach(node -> {
				String trendLineName = node.getProjectFilter().getName();
				String basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId().toString();
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(node.getProjectFilter().getBasicProjectConfigId());
				List<JiraIssueCustomHistory> jiraIssueHistoryDataList = projectWiseJiraIssueHistoryDataList
						.get(basicProjectConfigId);

				String weekOrMonth = (String) durationFilter.getOrDefault(Constant.DURATION, CommonConstant.WEEK);
				int previousTimeCount = (int) durationFilter.getOrDefault(Constant.COUNT, 8);

				Map<String, List<MeanTimeRecoverData>> meanTimeRecoverMapTimeWise = weekOrMonth.equalsIgnoreCase(
						CommonConstant.WEEK) ? getLastNWeek(previousTimeCount) : getLastNMonthCount(previousTimeCount);

				if (CollectionUtils.isNotEmpty(jiraIssueHistoryDataList)) {
					List<DataCount> dataCountList = new ArrayList<>();

					findMeanTimeToRecover(jiraIssueHistoryDataList, weekOrMonth, meanTimeRecoverMapTimeWise,
							fieldMapping);

					meanTimeRecoverMapTimeWise.forEach((weekOrMonthName, meanTimeRecoverListCurrentTime) -> {
						DataCount dataCount = createDataCount(trendLineName, weekOrMonthName,
								meanTimeRecoverListCurrentTime);
						dataCountList.add(dataCount);
					});
					populateMeanTimeRecoverExcelData(excelData, requestTrackerId, trendLineName,
							meanTimeRecoverMapTimeWise);

					mapTmp.get(node.getId()).setValue(dataCountList);
				}
			});
			kpiElement.setExcelData(excelData);
			kpiElement.setExcelColumns(KPIExcelColumn.MEAN_TIME_TO_RECOVER.getColumns());
		}
	}

	/**
	 * populate excel data
	 *
	 * @param excelData
	 *            excel data
	 * @param requestTrackerId
	 *            tracker id for cache
	 * @param trendLineName
	 *            project name
	 * @param meanTimeRecoverMapTimeWise
	 *            lead time in hours
	 */
	private void populateMeanTimeRecoverExcelData(List<KPIExcelData> excelData, String requestTrackerId,
			String trendLineName, Map<String, List<MeanTimeRecoverData>> meanTimeRecoverMapTimeWise) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateMeanTimeToRecoverExcelData(trendLineName, meanTimeRecoverMapTimeWise, excelData);
		}
	}

	/**
	 * set data count
	 *
	 * @param trendLineName
	 *            project name
	 * @param weekOrMonthName
	 *            date
	 * @param meanTimeRecoverListCurrentTime
	 *            meantime list
	 * @return data count
	 */
	private DataCount createDataCount(String trendLineName, String weekOrMonthName,
			List<MeanTimeRecoverData> meanTimeRecoverListCurrentTime) {
		double timeToRecover = meanTimeRecoverListCurrentTime.stream().filter(data -> data.getTimeToRecover() != null)
				.mapToDouble(data -> Double.parseDouble(data.getTimeToRecover())).sum();

		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(timeToRecover));
		dataCount.setSProjectName(trendLineName);
		dataCount.setDate(weekOrMonthName);
		dataCount.setValue(timeToRecover);
		dataCount.setHoverValue(new HashMap<>());
		return dataCount;
	}

	/**
	 * find Mean time to recover differences between closed ticket date and created
	 * date
	 *
	 * @param jiraIssueHistoryDataList
	 *            history data list
	 * @param weekOrMonth
	 *            date of x axis
	 * @param meanTimeRecoverMapTimeWise
	 *            meantime in days
	 */
	private void findMeanTimeToRecover(List<JiraIssueCustomHistory> jiraIssueHistoryDataList, String weekOrMonth,
			Map<String, List<MeanTimeRecoverData>> meanTimeRecoverMapTimeWise, FieldMapping fieldMapping) {
		List<String> jiraDodKPI166 = ObjectUtils.defaultIfNull(fieldMapping.getJiraDodKPI166(), new ArrayList<>());
		String storyFirstStatus = ObjectUtils.defaultIfNull(fieldMapping.getStoryFirstStatus(), "");
		List<String> dodStatus = jiraDodKPI166.stream().map(String::toLowerCase).collect(Collectors.toList());
		jiraIssueHistoryDataList.forEach(jiraIssueHistoryData -> {
			DateTime ticketClosedDate;
			DateTime ticketCreatedDate = jiraIssueHistoryData.getCreatedDate();
			Map<String, DateTime> closedStatusDateMap = new HashMap<>();

			jiraIssueHistoryData.getStatusUpdationLog().forEach(statusChangeLog -> {
				// reopened scenario
				if (CollectionUtils.isNotEmpty(dodStatus)
						&& dodStatus.contains(statusChangeLog.getChangedFrom().toLowerCase())
						&& statusChangeLog.getChangedTo().equalsIgnoreCase(storyFirstStatus)) {
					closedStatusDateMap.clear();
				}
				// fist close date of last close cycle
				if (CollectionUtils.isNotEmpty(dodStatus)
						&& dodStatus.contains(statusChangeLog.getChangedTo().toLowerCase())) {
					if (closedStatusDateMap.containsKey(statusChangeLog.getChangedTo())) {
						closedStatusDateMap.clear();
					}
					closedStatusDateMap.put(statusChangeLog.getChangedTo(),
							DateUtil.convertLocalDateTimeToDateTime(statusChangeLog.getUpdatedOn()));
				}
			});
			// Getting the min date of closed status.
			ticketClosedDate = closedStatusDateMap.values().stream().filter(Objects::nonNull).min(DateTime::compareTo)
					.orElse(null);

			double meanTimeToRecoverInHrs = 0;
			if (ticketClosedDate != null && ticketCreatedDate != null) {

				meanTimeToRecoverInHrs = Hours.hoursBetween(ticketCreatedDate, ticketClosedDate).getHours();

			}

			String weekOrMonthName = getDateFormatted(weekOrMonth, ticketCreatedDate);

			setMeanTimeForRecoverData(meanTimeRecoverMapTimeWise, jiraIssueHistoryData, ticketClosedDate,
					ticketCreatedDate, meanTimeToRecoverInHrs, weekOrMonthName);
		});
	}

	/**
	 * To get the formatted date for x-axis representation
	 *
	 * @param weekOrMonth
	 *            date of x axis
	 * @param currentDate
	 *            current date
	 * @return formatted date
	 */
	private String getDateFormatted(String weekOrMonth, DateTime currentDate) {
		if (weekOrMonth.equalsIgnoreCase(CommonConstant.WEEK)) {
			return DateUtil.getWeekRangeUsingDateTime(currentDate);
		} else {
			return currentDate.getYear() + Constant.DASH + currentDate.getMonthOfYear();
		}
	}

	/**
	 * Setting the mean time to recover data
	 *
	 * @param meanTimeRecoverMapTimeWise
	 *            meantime list
	 * @param jiraIssueHistoryData
	 *            history data
	 * @param ticketClosedDate
	 *            ticket closed date
	 * @param ticketCreatedDate
	 *            ticket created date
	 * @param meanTimeToRecoverInHrs
	 *            meantime to recover
	 * @param weekOrMonthName
	 *            date
	 */
	private void setMeanTimeForRecoverData(Map<String, List<MeanTimeRecoverData>> meanTimeRecoverMapTimeWise,
			JiraIssueCustomHistory jiraIssueHistoryData, DateTime ticketClosedDate, DateTime ticketCreatedDate,
			double meanTimeToRecoverInHrs, String weekOrMonthName) {
		MeanTimeRecoverData meanTimeRecoverData = new MeanTimeRecoverData();
		meanTimeRecoverData.setStoryID(jiraIssueHistoryData.getStoryID());
		meanTimeRecoverData.setUrl(jiraIssueHistoryData.getUrl());
		meanTimeRecoverData.setDesc(jiraIssueHistoryData.getDescription());
		meanTimeRecoverData.setIssueType(jiraIssueHistoryData.getStoryType());
		if (ticketClosedDate != null) {
			meanTimeRecoverData.setClosedDate(DateUtil.dateTimeConverterUsingFromAndTo(ticketClosedDate,
					DateUtil.TIME_FORMAT_WITH_SEC_ZONE, DateUtil.DISPLAY_DATE_TIME_FORMAT));
			meanTimeRecoverData.setTimeToRecover(String.valueOf(meanTimeToRecoverInHrs));
		}
		meanTimeRecoverData.setCreatedDate(DateUtil.dateTimeConverterUsingFromAndTo(ticketCreatedDate,
				DateUtil.TIME_FORMAT_WITH_SEC_ZONE, DateUtil.DISPLAY_DATE_TIME_FORMAT));

		meanTimeRecoverData.setDate(weekOrMonthName);
		meanTimeRecoverMapTimeWise.computeIfPresent(weekOrMonthName, (key, meanTimeToRecoverListCurrentTime) -> {
			meanTimeToRecoverListCurrentTime.add(meanTimeRecoverData);
			return meanTimeToRecoverListCurrentTime;
		});
	}

	/**
	 * get last N weeks
	 *
	 * @param count
	 *            count
	 * @return map of list of MeanTimeRecoverData
	 */
	private Map<String, List<MeanTimeRecoverData>> getLastNWeek(int count) {
		Map<String, List<MeanTimeRecoverData>> lastNWeek = new LinkedHashMap<>();
		LocalDate endDateTime = LocalDate.now();

		for (int i = 0; i < count; i++) {

			String currentWeekStr = DateUtil.getWeekRange(endDateTime);
			lastNWeek.put(currentWeekStr, new ArrayList<>());

			endDateTime = endDateTime.minusWeeks(1);
		}
		return lastNWeek;
	}

	/**
	 * get last N months
	 *
	 * @param count
	 *            count
	 * @return map of list of MeanTimeRecoverData
	 */
	private Map<String, List<MeanTimeRecoverData>> getLastNMonthCount(int count) {
		Map<String, List<MeanTimeRecoverData>> lastNMonth = new LinkedHashMap<>();
		LocalDateTime currentDate = LocalDateTime.now();
		String currentDateStr = currentDate.getYear() + Constant.DASH + currentDate.getMonthValue();
		lastNMonth.put(currentDateStr, new ArrayList<>());
		LocalDateTime lastMonth = LocalDateTime.now();
		for (int i = 1; i < count; i++) {
			lastMonth = lastMonth.minusMonths(1);
			String lastMonthStr = lastMonth.getYear() + Constant.DASH + lastMonth.getMonthValue();
			lastNMonth.put(lastMonthStr, new ArrayList<>());

		}
		return lastNMonth;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.MEAN_TIME_TO_RECOVER.name();
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI166(), KPICode.MEAN_TIME_TO_RECOVER.getKpiId());
	}

}
