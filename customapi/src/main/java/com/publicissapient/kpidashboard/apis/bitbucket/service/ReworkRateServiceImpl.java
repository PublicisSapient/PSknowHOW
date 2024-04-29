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

package com.publicissapient.kpidashboard.apis.bitbucket.service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.ProjectFilter;
import com.publicissapient.kpidashboard.apis.repotools.model.Branches;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.service.RepoToolsConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author kunkambl
 */
@Component
@Slf4j
public class ReworkRateServiceImpl extends BitBucketKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String REPO_TOOLS = "RepoTool";
	public static final String WEEK_FREQUENCY = "week";
	public static final String DAY_FREQUENCY = "day";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private RepoToolsConfigServiceImpl repoToolsConfigService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public String getQualifierType() {
		return KPICode.REWORK_RATE.name();
	}

	/**
	 * create data count
	 * @param kpiRequest
	 * 				kpi request
	 * @param kpiElement
	 * 				kpi element
	 * @param projectNode
	 * 				project node
	 * @return kpi element
	 * @throws ApplicationException
	 * 				application exception
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node projectNode)
			throws ApplicationException {
		Map<String, Node> mapTmp = new HashMap<>();
		mapTmp.put(projectNode.getId(), projectNode);
		projectWiseLeafNodeValue(kpiElement, mapTmp, projectNode, kpiRequest);

		log.debug("[PROJECT-WISE][{}]. Values of leaf node after KPI calculation {}", kpiRequest.getRequestTrackerId(),
				projectNode);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(projectNode, nodeWiseKPIValue, KPICode.REWORK_RATE);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.REWORK_RATE);
		Map<String, Map<String, List<DataCount>>> kpiFilterWiseProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((issueType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			kpiFilterWiseProjectWiseDc.put(issueType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		kpiFilterWiseProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.forEach((key, value) -> dataList.addAll(value));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);
		return kpiElement;
	}

	/**
	 * create data counts for kpi elements
	 * @param kpiElement
	 * 				kpi element
	 * @param mapTmp
	 * 			mapTmp
	 * @param projectLeafNode
	 * 				project node
	 * @param kpiRequest
	 * 				kpi request
	 */
	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp, Node projectLeafNode,
			KpiRequest kpiRequest) {

		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);
		String requestTrackerId = getRequestTrackerId();
		LocalDate localEndDate = dateRange.getEndDate();

		Integer dataPoints = kpiRequest.getXAxisDataPoints();
		String duration = kpiRequest.getDuration();

		// gets the tool configuration
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();

		List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseList = getRepoToolsKpiMetricResponse(localEndDate,
				toolMap, projectLeafNode, dataPoints, duration);

		if (CollectionUtils.isEmpty(repoToolKpiMetricResponseList)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No kpi data found for this project {}", projectLeafNode);
			return;
		}

		List<KPIExcelData> excelData = new ArrayList<>();
		ProjectFilter accountHierarchyData = projectLeafNode.getProjectFilter();
		ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
		List<Tool> reposList = toolMap.get(configId).get(REPO_TOOLS) == null
				? Collections.emptyList()
				: toolMap.get(configId).get(REPO_TOOLS);
		if (CollectionUtils.isEmpty(reposList)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No Jobs found for this project {}",
					projectLeafNode.getProjectFilter());
			return;
		}

		List<Map<String, Double>> repoWiseReworkRateList = new ArrayList<>();
		List<String> repoList = new ArrayList<>();
		List<String> branchList = new ArrayList<>();
		String projectName = projectLeafNode.getProjectFilter().getName();
		Map<String, List<DataCount>> aggDataMap = new HashMap<>();
		Map<String, List<Double>> aggReworkRateForRepo = new HashMap<>();
		reposList.forEach(repo -> {
			if (!CollectionUtils.isEmpty(repo.getProcessorItemList()) && repo.getProcessorItemList().get(0)
					.getId() != null) {
				Map<String, Double> excelDataLoader = new HashMap<>();
				String branchName = getBranchSubFilter(repo, projectName);
				Map<String, Double> dateWiseReworkRate = new HashMap<>();
				createDateLabelWiseMap(repoToolKpiMetricResponseList, repo.getRepositoryName(), repo.getBranch(),
						dateWiseReworkRate);
				reworkRateForRepo(aggReworkRateForRepo, dateWiseReworkRate);
				setWeekWiseReworkRate(dateWiseReworkRate, excelDataLoader, branchName, projectName, aggDataMap,
						kpiRequest);
				repoWiseReworkRateList.add(excelDataLoader);
				repoList.add(repo.getRepositoryName());
				branchList.add(repo.getBranch());

			}
		});
		setWeekWiseReworkRate(aggReworkRateForRepo.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
						e -> e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0))), new HashMap<>(),
				Constant.AGGREGATED_VALUE, projectName, aggDataMap, kpiRequest);
		mapTmp.get(projectLeafNode.getId()).setValue(aggDataMap);

		populateExcelDataObject(requestTrackerId, repoWiseReworkRateList, repoList, branchList, excelData,
				projectLeafNode);
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.REWORK_RATE.getColumns());
	}

	/**
	 * aggregate rework rate
	 * @param aggReworkRateForRepo
	 * 				aggregated rework rate map
	 * @param reworkRateForRepo
	 * 				rework rate for config
	 */
	private void reworkRateForRepo(Map<String, List<Double>> aggReworkRateForRepo,
			Map<String, Double> reworkRateForRepo) {
		if (MapUtils.isNotEmpty(reworkRateForRepo)) {
			reworkRateForRepo.forEach(
					(key, value) -> aggReworkRateForRepo.computeIfAbsent(key, k -> new ArrayList<>()).add(value));
		}
	}

	/**
	 * create date wise rework rate map
	 * @param repoToolKpiMetricResponsesCommit
	 * 				RepoToolKpiMetricResponse object
	 * @param repoName
	 * 				repository name
	 * @param branchName
	 * 				branch name
	 * @param dateWiseReworkRate
	 * 				date wise rework rate map
	 */
	private void createDateLabelWiseMap(List<RepoToolKpiMetricResponse> repoToolKpiMetricResponsesCommit,
			String repoName, String branchName, Map<String, Double> dateWiseReworkRate) {

		for (RepoToolKpiMetricResponse response : repoToolKpiMetricResponsesCommit) {
			if (response.getRepositories() != null) {
				Optional<Branches> matchingBranch = response.getRepositories().stream()
						.filter(repository -> repository.getName().equals(repoName))
						.flatMap(repository -> repository.getBranches().stream())
						.filter(branch -> branch.getName().equals(branchName)).findFirst();
				double reworkRate = matchingBranch.map(Branches::getBranchReworkRateScore).orElse(0d);
				dateWiseReworkRate.put(response.getDateLabel(), reworkRate);
			}
		}
	}

	/**
	 * create data count object of rework rate
	 * @param weekWiseReworkRate
	 * 				week wise rework rate
	 * @param excelDataLoader
	 * 				excel data loader
	 * @param branchName
	 * 				branch name
	 * @param projectName
	 * 				project name
	 * @param aggDataMap
	 * 				map of branch name and data count
	 * @param kpiRequest
	 * 				kpi request object
	 */
	private void setWeekWiseReworkRate(Map<String, Double> weekWiseReworkRate, Map<String, Double> excelDataLoader,
			String branchName, String projectName, Map<String, List<DataCount>> aggDataMap, KpiRequest kpiRequest) {
		LocalDate currentDate = LocalDate.now();
		Integer dataPoints = kpiRequest.getXAxisDataPoints();
		String duration = kpiRequest.getDuration();
		for (int i = 0; i < dataPoints; i++) {
			CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate, duration);
			Double reworkRate = weekWiseReworkRate.getOrDefault(dateRange.getStartDate().toString(), 0d);
			String date = getDateRange(dateRange, duration);
			aggDataMap.putIfAbsent(branchName, new ArrayList<>());
			DataCount dataCount = setDataCount(projectName, date, reworkRate);
			aggDataMap.get(branchName).add(dataCount);
			excelDataLoader.put(date, reworkRate);
			currentDate = getNextRangeDate(duration, currentDate);

		}

	}

	/**
	 * get date range
	 * @param dateRange
	 * 				date range
	 * @param duration
	 * 				time duration
	 * @return date range string
	 */
	private String getDateRange(CustomDateRange dateRange, String duration) {
		String range = null;
		if (CommonConstant.WEEK.equalsIgnoreCase(duration)) {
			range = DateUtil.dateTimeConverter(dateRange.getStartDate().toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to " + DateUtil.dateTimeConverter(
					dateRange.getEndDate().toString(), DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT);
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	/**
	 * gets next date
	 * @param duration
	 * 				time duration
	 * @param currentDate
	 * 				current date
	 * @return next local date
	 */
	private LocalDate getNextRangeDate(String duration, LocalDate currentDate) {
		if ((CommonConstant.WEEK).equalsIgnoreCase(duration)) {
			currentDate = currentDate.minusWeeks(1);
		} else {
			currentDate = currentDate.minusDays(1);
		}
		return currentDate;
	}

	/**
	 * creates data count object
	 * @param projectName
	 * 				project name
	 * @param week
	 * 				week
	 * @param value
	 * 				data count value
	 * @return data count object
	 */
	private DataCount setDataCount(String projectName, String week, Double value) {
		Map<String, Object> hoverMap = new HashMap<>();
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value == null ? 0L : value));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(week);
		dataCount.setHoverValue(hoverMap);
		dataCount.setValue(value == null ? 0L : value);
		return dataCount;
	}

	/**
	 * get kpi data from repo tools api
	 *
	 * @param endDate
	 * 				end date
	 * @param toolMap
	 * 				tool map from cache
	 * @param node
	 * 				project node
	 * @param dataPoint
	 * 				no of days/weeks
	 * @param duration
	 * 				time duration
	 * @return lis of RepoToolKpiMetricResponse object
	 */
	private List<RepoToolKpiMetricResponse> getRepoToolsKpiMetricResponse(LocalDate endDate,
			Map<ObjectId, Map<String, List<Tool>>> toolMap, Node node, Integer dataPoint, String duration) {

		List<String> projectCodeList = new ArrayList<>();
		ProjectFilter accountHierarchyData = node.getProjectFilter();
		ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
		List<Tool> tools = toolMap.getOrDefault(configId, Collections.emptyMap()).getOrDefault(REPO_TOOLS,
				Collections.emptyList());
		if (!CollectionUtils.isEmpty(tools)) {
			projectCodeList.add(node.getId());
		}

		List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(projectCodeList)) {
			LocalDate startDate = LocalDate.now().minusDays(dataPoint);
			if (duration.equalsIgnoreCase(CommonConstant.WEEK)) {
				startDate = LocalDate.now().minusWeeks(dataPoint);
				while (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
					startDate = startDate.minusDays(1);
				}
			}

			String debbieDuration = duration.equalsIgnoreCase(CommonConstant.WEEK) ? WEEK_FREQUENCY : DAY_FREQUENCY;
			repoToolKpiMetricResponseList = repoToolsConfigService.getRepoToolKpiMetrics(projectCodeList,
					customApiConfig.getRepoToolReworkRateUrl(), startDate.toString(), endDate.toString(),
					debbieDuration);
		}

		return repoToolKpiMetricResponseList;
	}

	/**
	 * populated excel data
	 * @param requestTrackerId
	 * 				kpi tracker id
	 * @param repoWiseMRList
	 * 				repo wise rework rate list
	 * @param repoList
	 * 				repository list
	 * @param branchList
	 * 				branch list
	 * @param validationDataMap
	 * 				kpi excel data map
	 * @param node
	 * 				project node
	 */
	private void populateExcelDataObject(String requestTrackerId, List<Map<String, Double>> repoWiseMRList,
			List<String> repoList, List<String> branchList, List<KPIExcelData> validationDataMap, Node node) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			String projectName = node.getProjectFilter().getName();
			KPIExcelUtility.populateReworkRateExcelData(projectName, repoWiseMRList, repoList, branchList,
					validationDataMap);

		}
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return new HashMap<>();
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI173(), KPICode.REWORK_RATE.getKpiId());
	}
}