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
package com.publicissapient.kpidashboard.apis.jenkins.service;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.HIERARCHY_LEVEL_ID_PROJECT;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.jira.service.SprintDetailsServiceImpl;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.BuildFrequencyInfo;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Build Frequency KPI Implementation
 *
 * @author aksshriv1
 */
@Component
@Slf4j
public class BuildFrequencyServiceImpl extends JenkinsKPIService<Long, List<Object>, Map<String, List<Object>>> {

	private static final long DAYS_IN_WEEKS = 7;
	private static final String DATE_TIME_FORMAT_REGEX = "Z|\\.\\d+";
	private static final String SPRINT = "sprint";
	private static final String PROJECT = "project";
	private static final String BUILDS = "Builds";

	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private KpiDataCacheService kpiDataCacheService;
	@Autowired
	private SprintDetailsServiceImpl sprintDetailsService;

	@Override
	public String getQualifierType() {
		return KPICode.BUILD_FREQUENCY.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, TreeAggregatorDetail treeAggregatorDetail)
			throws ApplicationException {

		Map<String, List<DataCount>> trendValueMap = new HashMap<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);
		Filters filter = Filters.getFilter(kpiRequest.getLabel());

//      in case if only projects or sprint filters are applied
		if (filter == Filters.SPRINT || filter == Filters.PROJECT) {
			List<Node> leafNodes = treeAggregatorDetail.getMapOfListOfLeafNodes().entrySet().stream()
					.filter(k -> Filters.getFilter(k.getKey()) == Filters.SPRINT).map(Map.Entry::getValue).findFirst()
					.orElse(new ArrayList<>());
			projectWiseLeafNodeValue(kpiElement, mapTmp, projectList, trendValueMap, leafNodes);

		} else {
			projectWiseLeafNodeValue(kpiElement, mapTmp, projectList, trendValueMap, new ArrayList<>());
		}

		log.debug("[CODE-BUILD-TIME-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.BUILD_FREQUENCY);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.BUILD_FREQUENCY);
		Map<String, Map<String, List<DataCount>>> issueTypeProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((issueType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			issueTypeProjectWiseDc.put(issueType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		issueTypeProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);

		return kpiElement;
	}

	@Override
	public Long calculateKPIMetrics(Map<String, List<Object>> builds) {
		return null;
	}

	@Override
	public Map<String, List<Object>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, List<Object>> resultMap = new HashMap<>();
		List<Object> buildList = new ArrayList<>();
		List<Node> sprintNodes = leafNodeList.stream().filter(node -> node.getGroupName().equalsIgnoreCase(SPRINT))
				.toList();
		List<String> backlogProjectsIds = new ArrayList<>();
		leafNodeList.stream().filter(node -> node.getGroupName().equalsIgnoreCase(PROJECT)).forEach(node -> {
			ObjectId basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId();
			Optional<Node> firstSprintNode = sprintNodes.stream()
					.filter(sprintNode -> sprintNode.getProjectFilter().getId().equalsIgnoreCase(node.getId()))
					.findFirst();
			String localStartDate = startDate;

			if (firstSprintNode.isPresent()) {
				backlogProjectsIds.add(firstSprintNode.get().getId());
				LocalDateTime endDateTime = DateUtil.stringToLocalDateTime(
						firstSprintNode.get().getSprintFilter().getEndDate().replaceAll(DATE_TIME_FORMAT_REGEX, ""),
						DateUtil.TIME_FORMAT);
				localStartDate = endDateTime.minusDays((customApiConfig.getJenkinsWeekCount()) * DAYS_IN_WEEKS)
						.toString();
			}

			// get cached build info from BuildFrequency db kpi cache
			buildList.addAll(kpiDataCacheService.fetchBuildFrequencyData(basicProjectConfigId, localStartDate, endDate,
					KPICode.BUILD_FREQUENCY.getKpiId()));
		});
		resultMap.put(BUILDS, buildList);
		if (CollectionUtils.isNotEmpty(backlogProjectsIds)) {
			List<SprintDetails> sprintDetailsList = sprintDetailsService.getSprintDetailsByIds(backlogProjectsIds);

			Map<ObjectId, List<SprintDetails>> groupedDetails = sprintDetailsList.stream()
					.collect(Collectors.groupingBy(SprintDetails::getBasicProjectConfigId, Collectors.toList()));

			groupedDetails.forEach((objectId, details) -> resultMap.put(objectId.toString(), new ArrayList<>(details)));
		}


		return resultMap;
	}

	/**
	 * @param kpiElement
	 *          kpiElement
	 * @param mapTmp
	 *          mapTmp
	 * @param projectLeafNodeList
	 *          projectLeafNodeList
	 * @param trendValueMap
	 *          trendValueMap
	 */
	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp, List<Node> projectLeafNodeList,
			Map<String, List<DataCount>> trendValueMap, List<Node> sprintLeafNodeList) {

		String requestTrackerId = getRequestTrackerId();
		LocalDateTime localStartDate = LocalDateTime.now().minusDays(customApiConfig.getJenkinsWeekCount() * DAYS_IN_WEEKS);
		LocalDateTime localEndDate = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtil.TIME_FORMAT);
		String startDate = localStartDate.format(formatter);
		String endDate = localEndDate.format(formatter);

		sprintLeafNodeList.addAll(projectLeafNodeList);
		Map<String, List<Object>> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, null);
		Map<ObjectId, List<Build>> buildGroup = resultMap.get(BUILDS).stream().filter(Build.class::isInstance)
				.map(Build.class::cast).collect(Collectors.groupingBy(Build::getBasicProjectConfigId));

		if (MapUtils.isEmpty(buildGroup)) {
			return;
		}

		List<KPIExcelData> excelData = new ArrayList<>();
		projectLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();
			BuildFrequencyInfo buildFrequencyInfo = new BuildFrequencyInfo();
			SprintDetails sprintDetails = resultMap
					.get(node.getProjectFilter().getBasicProjectConfigId().toString()) != null
					? (SprintDetails) resultMap
					.get(node.getProjectFilter().getBasicProjectConfigId().toString()).get(0)
					: null;
			ObjectId basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId();
			List<Build> buildListProjectWise = buildGroup.get(basicProjectConfigId);

			if (CollectionUtils.isEmpty(buildListProjectWise)) {
				mapTmp.get(node.getId()).setValue(null);
				return;
			}

			Map<String, List<DataCount>> aggDataMap = new HashMap<>();
			List<Build> aggBuildList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(buildListProjectWise)) {
				Map<String, List<Build>> buildMapJobWise = buildListProjectWise.stream()
						.collect(Collectors.groupingBy(Build::getBuildJob, Collectors.toList()));
				for (Map.Entry<String, List<Build>> entry : buildMapJobWise.entrySet()) {
					String jobName;
					List<Build> buildList = entry.getValue();
					jobName = getJobName(trendLineName, entry, buildList);
					aggBuildList.addAll(buildList);
					prepareInfoForBuild(null, sprintDetails, buildList, trendLineName, trendValueMap, jobName, aggDataMap);
				}
			}

			if (CollectionUtils.isEmpty(aggBuildList)) {
				mapTmp.get(node.getId()).setValue(null);
				return;
			}
			prepareInfoForBuild(buildFrequencyInfo, sprintDetails, aggBuildList, trendLineName, trendValueMap,
					Constant.AGGREGATED_VALUE, aggDataMap);
			mapTmp.get(node.getId()).setValue(aggDataMap);

			populateValidationDataObject(requestTrackerId, excelData, trendLineName, buildFrequencyInfo);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.BUILD_FREQUENCY.getColumns());
	}

	/**
	 * to get the job name
	 *
	 * @param trendLineName
	 *          trendLineName
	 * @param entry
	 *          entry
	 * @param buildList
	 *          list of builds
	 * @return returns the job name
	 */
	private static String getJobName(String trendLineName, Map.Entry<String, List<Build>> entry, List<Build> buildList) {
		String jobName;
		if (StringUtils.isNotEmpty(buildList.get(0).getJobFolder())) {
			if (StringUtils.isNotEmpty(buildList.get(0).getPipelineName())) {
				jobName = buildList.get(0).getPipelineName() + CommonUtils.getStringWithDelimiters(trendLineName);
			} else {
				jobName = buildList.get(0).getJobFolder() + CommonUtils.getStringWithDelimiters(trendLineName);
			}

		} else {
			if (StringUtils.isNotEmpty(buildList.get(0).getPipelineName())) {
				jobName = buildList.get(0).getPipelineName() + CommonUtils.getStringWithDelimiters(trendLineName);
			} else {
				jobName = entry.getKey() + CommonUtils.getStringWithDelimiters(trendLineName);
			}
		}
		return jobName;
	}

	/**
	 * @param requestTrackerId
	 *          requestTrackerId
	 * @param excelData
	 *          excelData
	 * @param projectName
	 *          projectName
	 * @param buildFrequencyInfo
	 *          buildFrequencyInfo
	 */
	private void populateValidationDataObject(String requestTrackerId, List<KPIExcelData> excelData, String projectName,
			BuildFrequencyInfo buildFrequencyInfo) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateBuildFrequency(excelData, projectName, buildFrequencyInfo);
		}
	}

	/**
	 * @param buildFrequencyInfo
	 *          buildFrequencyInfo
	 * @param sprintDetails
	 *          endTime
	 * @param buildList
	 *          buildList
	 * @param trendLineName
	 *          trendLineName
	 * @param trendValueMap
	 *          trendValueMap
	 * @param jobName
	 *          jobName
	 * @param aggDataMap
	 *          aggDataMap
	 */
	private void prepareInfoForBuild(BuildFrequencyInfo buildFrequencyInfo, SprintDetails sprintDetails, List<Build> buildList,
			String trendLineName, Map<String, List<DataCount>> trendValueMap, String jobName,
			Map<String, List<DataCount>> aggDataMap) {
		LocalDate endDateTime = getEndDate(sprintDetails);
		Map<String, Integer> weekRange = new LinkedHashMap<>();
		for (int i = 0; i < customApiConfig.getJenkinsWeekCount(); i++) {
			List<Long> durationList = new ArrayList<>();
			Pair<LocalDate, LocalDate> dateRange = getDateRange(endDateTime, sprintDetails);
			LocalDate monday = dateRange.getLeft();
			LocalDate sunday = dateRange.getRight();

			String date = DateUtil.localDateTimeConverter(monday) + " to " + DateUtil.localDateTimeConverter(sunday);
			for (Build build : buildList) {
				if (checkDateIsInWeeks(monday, sunday, build)) {
					durationList.add(build.getDuration());
					buildFrequencyInfo(buildFrequencyInfo, build, date);
				}
			}

			Integer valueForCurrentLeaf = durationList.size();
			if (null != valueForCurrentLeaf) {
				weekRange.put(date, valueForCurrentLeaf);
			}
			weekRange.putIfAbsent(date, null);
			endDateTime = endDateTime.minusWeeks(1);
		}
		trendValueMap.putIfAbsent(jobName, new ArrayList<>());
		aggDataMap.putIfAbsent(jobName, new ArrayList<>());
		weekRange.forEach((date, valueForCurrentLeaf) -> {
			DataCount dataCount = createDataCount(trendLineName, valueForCurrentLeaf, date);
			aggDataMap.get(jobName).add(dataCount);
			trendValueMap.get(jobName).add(dataCount);
		});
	}

	/**
	 * Determines the end date for a sprint based on the provided SprintDetails
	 * object.
	 *
	 * @param sprintDetails
	 *            The SprintDetails object containing information about the sprint.
	 *            Can be null.
	 * @return The calculated end date as a LocalDate. If sprintDetails is null, it
	 *         returns the current date minus one week.
	 */
	protected LocalDate getEndDate(SprintDetails sprintDetails) {
		if (sprintDetails != null) {
			return sprintDetails.getCompleteDate() != null
					? DateUtil.stringToLocalDate(sprintDetails.getCompleteDate().replaceAll(DATE_TIME_FORMAT_REGEX, ""),
					DateUtil.TIME_FORMAT)
					: DateUtil.stringToLocalDate(sprintDetails.getEndDate().replaceAll(DATE_TIME_FORMAT_REGEX, ""),
					DateUtil.TIME_FORMAT);
		}
		return LocalDate.now();
	}

	/**
	 * Calculates the start (Monday) and end (Sunday) dates of a week.
	 *
	 * @param endDateTime The reference end date
	 * @param sprintDetails Sprint details if available, can be null
	 * @return A pair containing Monday (start) and Sunday (end) dates
	 */
	private Pair<LocalDate, LocalDate> getDateRange(LocalDate endDateTime, SprintDetails sprintDetails) {
		LocalDate monday = endDateTime;
		LocalDate sunday = endDateTime;

		if(sprintDetails != null) {
			monday = endDateTime.minusDays(6);
		} else {
			while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
				monday = monday.minusDays(1);
			}
			while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
				sunday = sunday.plusDays(1);
			}
		}

		return Pair.of(monday, sunday);
	}

	/**
	 * @param trendLineName
	 *          trendLineName
	 * @param valueForCurrentLeaf
	 *          valueForCurrentLeaf
	 * @param date
	 *          date
	 * @return returning data count
	 */
	private DataCount createDataCount(String trendLineName, Integer valueForCurrentLeaf, String date) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(valueForCurrentLeaf == null ? 0L : valueForCurrentLeaf));
		dataCount.setSProjectName(trendLineName);
		dataCount.setDate(date);
		dataCount.setHoverValue(new HashMap<>());
		dataCount.setValue(valueForCurrentLeaf == null ? 0L : valueForCurrentLeaf);
		return dataCount;
	}

	/**
	 * @param buildFrequencyInfo
	 *          buildFrequencyInfo
	 * @param build
	 *          build
	 * @param date
	 *          date
	 */
	private void buildFrequencyInfo(BuildFrequencyInfo buildFrequencyInfo, Build build, String date) {
		if (null != buildFrequencyInfo) {

			if (StringUtils.isNotEmpty(build.getBuildJob())) {
				buildFrequencyInfo.addBuildJobNameList(build.getBuildJob());
			} else if (StringUtils.isNotEmpty(build.getJobFolder())) {
				buildFrequencyInfo.addBuildJobNameList(build.getJobFolder());
			} else {
				buildFrequencyInfo.addBuildJobNameList(build.getPipelineName());
			}
			buildFrequencyInfo.addBuildUrl(build.getBuildUrl());
			buildFrequencyInfo.addBuildStartTime(DateUtil.dateConverter(new Date(build.getStartTime())));
			buildFrequencyInfo.addWeeks(date);
		}
	}

	/**
	 * @param monday
	 *          monday
	 * @param sunday
	 *          sunday
	 * @param build
	 *          build
	 * @return returning boolean value
	 */
	private boolean checkDateIsInWeeks(LocalDate monday, LocalDate sunday, Build build) {
		LocalDate buildTime = Instant.ofEpochMilli(build.getStartTime()).atZone(ZoneId.systemDefault()).toLocalDate();
		return (buildTime.isAfter(monday) || buildTime.isEqual(monday)) &&
				(buildTime.isBefore(sunday) || buildTime.isEqual(sunday));
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI172(), KPICode.BUILD_FREQUENCY.getKpiId());
	}
}
