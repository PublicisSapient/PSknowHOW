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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Build Frequency KPI Implementation
 * 
 * @author aksshriv1
 */
@Component
@Slf4j
public class BuildFrequencyServiceImpl extends JenkinsKPIService<Long, List<Object>, Map<ObjectId, List<Build>>> {

	private static final long DAYS_IN_WEEKS = 7;
	@Autowired
	private BuildRepository buildRepository;
	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public String getQualifierType() {
		return KPICode.BUILD_FREQUENCY.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Map<String, List<DataCount>> trendValueMap = new HashMap<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);
		projectWiseLeafNodeValue(kpiElement, mapTmp, projectList, trendValueMap);

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
	public Long calculateKPIMetrics(Map<ObjectId, List<Build>> builds) {
		return null;
	}

	@Override
	public Map<ObjectId, List<Build>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Set<ObjectId> projectBasicConfigIds = new HashSet<>();
		List<String> statusList = new ArrayList<>();
		Map<String, List<String>> mapOfFilters = new HashMap<>();
		leafNodeList.forEach(node -> {
			ObjectId basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId();
			projectBasicConfigIds.add(basicProjectConfigId);
		});

		statusList.add(BuildStatus.SUCCESS.name());
		mapOfFilters.put("buildStatus", statusList);
		List<Build> buildList = buildRepository.findBuildList(mapOfFilters, projectBasicConfigIds, startDate, endDate);
		if (CollectionUtils.isEmpty(buildList)) {
			return new HashMap<>();
		}
		return buildList.stream().collect(Collectors.groupingBy(Build::getBasicProjectConfigId, Collectors.toList()));
	}

	/**
	 * 
	 * @param kpiElement
	 *            kpiElement
	 * @param mapTmp
	 *            mapTmp
	 * @param projectLeafNodeList
	 *            projectLeafNodeList
	 * @param trendValueMap
	 *            trendValueMap
	 */
	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp,
			List<Node> projectLeafNodeList, Map<String, List<DataCount>> trendValueMap) {

		String requestTrackerId = getRequestTrackerId();
		LocalDateTime localStartDate = LocalDateTime.now()
				.minusDays(customApiConfig.getJenkinsWeekCount() * DAYS_IN_WEEKS);
		LocalDateTime localEndDate = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtil.TIME_FORMAT);
		String startDate = localStartDate.format(formatter);
		String endDate = localEndDate.format(formatter);

		Map<ObjectId, List<Build>> buildGroup = fetchKPIDataFromDb(projectLeafNodeList, startDate, endDate, null);

		if (MapUtils.isEmpty(buildGroup)) {
			return;
		}

		List<KPIExcelData> excelData = new ArrayList<>();
		projectLeafNodeList.forEach(node -> {

			String trendLineName = node.getProjectFilter().getName();
			BuildFrequencyInfo buildFrequencyInfo = new BuildFrequencyInfo();
			LocalDateTime end = localEndDate;
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
					if (StringUtils.isNotEmpty(buildList.get(0).getJobFolder())) {
						jobName = buildList.get(0).getJobFolder() + CommonConstant.ARROW + trendLineName;
					} else {
						jobName = entry.getKey() + CommonConstant.ARROW + trendLineName;
					}
					aggBuildList.addAll(buildList);
					prepareInfoForBuild(null, end, buildList, trendLineName, trendValueMap, jobName, aggDataMap);
				}
			}

			if (CollectionUtils.isEmpty(aggBuildList)) {
				mapTmp.get(node.getId()).setValue(null);
				return;
			}
			prepareInfoForBuild(buildFrequencyInfo, end, aggBuildList, trendLineName, trendValueMap,
					Constant.AGGREGATED_VALUE, aggDataMap);
			mapTmp.get(node.getId()).setValue(aggDataMap);

			populateValidationDataObject(requestTrackerId, excelData, trendLineName, buildFrequencyInfo);

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.BUILD_FREQUENCY.getColumns());
	}

	/**
	 * 
	 * @param requestTrackerId
	 *            requestTrackerId
	 * @param excelData
	 *            excelData
	 * @param projectName
	 *            projectName
	 * @param buildFrequencyInfo
	 *            buildFrequencyInfo
	 */
	private void populateValidationDataObject(String requestTrackerId, List<KPIExcelData> excelData, String projectName,
			BuildFrequencyInfo buildFrequencyInfo) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateBuildFrequency(excelData, projectName, buildFrequencyInfo);

		}
	}

	/**
	 * 
	 * @param buildFrequencyInfo
	 *            buildFrequencyInfo
	 * @param endTime
	 *            endTime
	 * @param buildList
	 *            buildList
	 * @param trendLineName
	 *            trendLineName
	 * @param trendValueMap
	 *            trendValueMap
	 * @param jobName
	 *            jobName
	 * @param aggDataMap
	 *            aggDataMap
	 */
	private void prepareInfoForBuild(BuildFrequencyInfo buildFrequencyInfo, LocalDateTime endTime,
			List<Build> buildList, String trendLineName, Map<String, List<DataCount>> trendValueMap, String jobName,
			Map<String, List<DataCount>> aggDataMap) {
		LocalDate endDateTime = endTime.toLocalDate();
		Map<String, Integer> weekRange = new LinkedHashMap<>();
		for (int i = 0; i < customApiConfig.getJenkinsWeekCount(); i++) {
			List<Long> durationList = new ArrayList<>();
			LocalDate monday = endDateTime;
			while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
				monday = monday.minusDays(1);
			}
			LocalDate sunday = endDateTime;
			while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
				sunday = sunday.plusDays(1);
			}
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
	 * 
	 * @param trendLineName
	 *            trendLineName
	 * @param valueForCurrentLeaf
	 *            valueForCurrentLeaf
	 * @param date
	 *            date
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
	 * 
	 * @param buildFrequencyInfo
	 *            buildFrequencyInfo
	 * @param build
	 *            build
	 * @param date
	 *            date
	 */
	private void buildFrequencyInfo(BuildFrequencyInfo buildFrequencyInfo, Build build, String date) {
		if (null != buildFrequencyInfo) {

			if (StringUtils.isNotEmpty(build.getJobFolder())) {
				buildFrequencyInfo.addBuildJobNameList(build.getJobFolder());
			} else {
				buildFrequencyInfo.addBuildJobNameList(build.getBuildJob());
			}
			buildFrequencyInfo.addBuildUrl(build.getBuildUrl());
			buildFrequencyInfo.addBuildStartTime(DateUtil.dateConverter(new Date(build.getStartTime())));
			buildFrequencyInfo.addWeeks(date);
		}
	}

	/**
	 * 
	 * @param monday
	 *            monday
	 * @param sunday
	 *            sunday
	 * @param build
	 *            build
	 * @return returning boolean value
	 */
	private boolean checkDateIsInWeeks(LocalDate monday, LocalDate sunday, Build build) {
		LocalDate buildTime = Instant.ofEpochMilli(build.getStartTime()).atZone(ZoneId.systemDefault()).toLocalDate();
		return (buildTime.isAfter(monday) || buildTime.isEqual(monday))
				&& (buildTime.isBefore(sunday) || buildTime.isEqual(sunday));
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
