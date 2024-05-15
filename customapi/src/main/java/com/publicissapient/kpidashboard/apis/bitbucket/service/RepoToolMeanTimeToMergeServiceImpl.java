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
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RepoToolMeanTimeToMergeServiceImpl extends BitBucketKPIService<Double, List<Object>, Map<String, Object>> {

	public static final String WEEK_FREQUENCY = "week";
	public static final String DAY_FREQUENCY = "day";
	private static final String REPO_TOOLS = "RepoTool";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private RepoToolsConfigServiceImpl repoToolsConfigService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public String getQualifierType() {
		return KPICode.REPO_TOOL_MEAN_TIME_TO_MERGE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node projectNode)
			throws ApplicationException {
		Map<String, Node> mapTmp = new HashMap<>();
		mapTmp.put(projectNode.getId(), projectNode);
		projectWiseLeafNodeValue(kpiElement, mapTmp, projectNode, kpiRequest);
		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(projectNode, nodeWiseKPIValue, KPICode.REPO_TOOL_MEAN_TIME_TO_MERGE);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.MEAN_TIME_TO_MERGE);
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
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);

		return kpiElement;
	}

	private void aggMeanTimeToMerge(Map<String, List<Double>> aggMRTimeForRepo, Map<String, Double> mrTime) {
		if (MapUtils.isNotEmpty(mrTime)) {
			mrTime.forEach((key, value) -> aggMRTimeForRepo.computeIfAbsent(key, k -> new ArrayList<>()).add(value));
		}
	}

	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp, Node projectLeafNode,
			KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);
		LocalDate localEndDate = dateRange.getEndDate();

		Integer dataPoints = kpiRequest.getXAxisDataPoints();
		String duration = kpiRequest.getDuration();

		// gets the tool configuration
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();
		List<RepoToolKpiMetricResponse> repoToolKpiMetricRespons = getRepoToolsKpiMetricResponse(localEndDate, toolMap,
				projectLeafNode, duration, dataPoints);
		if (CollectionUtils.isEmpty(repoToolKpiMetricRespons)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No kpi data found for this project {}", projectLeafNode);
			return;
		}

		List<KPIExcelData> excelData = new ArrayList<>();
		String projectName = projectLeafNode.getProjectFilter().getName();

		ProjectFilter accountHierarchyData = projectLeafNode.getProjectFilter();
		ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
		List<Tool> reposList = toolMap.get(configId).get(REPO_TOOLS) == null ? Collections.emptyList()
				: toolMap.get(configId).get(REPO_TOOLS);
		if (CollectionUtils.isEmpty(reposList)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No Jobs found for this project {}",
					projectLeafNode.getProjectFilter());
			return;
		}

		List<Map<String, Double>> repoWiseMRList = new ArrayList<>();
		List<String> repoList = new ArrayList<>();
		List<String> branchList = new ArrayList<>();
		Map<String, Double> excelDataLoader = new HashMap<>();

		Map<String, List<DataCount>> aggDataMap = new HashMap<>();
		Map<String, List<Double>> aggMeanTimeToMerge = new HashMap<>();
		reposList.forEach(repo -> {
			if (!CollectionUtils.isEmpty(repo.getProcessorItemList())
					&& repo.getProcessorItemList().get(0).getId() != null) {
				String branchName = getBranchSubFilter(repo, projectName);
				Map<String, Double> dateWiseMeanTimeToMerge = new HashMap<>();
				createDateLabelWiseMap(repoToolKpiMetricRespons, repo.getRepositoryName(), repo.getBranch(),
						dateWiseMeanTimeToMerge);
				aggMeanTimeToMerge(aggMeanTimeToMerge, dateWiseMeanTimeToMerge);
				setWeekWiseMeanTimeToMergeForRepoTools(dateWiseMeanTimeToMerge, excelDataLoader, branchName,
						projectName, aggDataMap, duration, dataPoints);
				repoWiseMRList.add(excelDataLoader);
				repoList.add(repo.getRepositoryName());
				branchList.add(repo.getBranch());

			}
		});
		setWeekWiseMeanTimeToMergeForRepoTools(aggMeanTimeToMerge.entrySet().stream().collect(
						Collectors.toMap(Map.Entry::getKey,
								e -> e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0))),
				excelDataLoader, Constant.AGGREGATED_VALUE, projectName, aggDataMap, duration, dataPoints);

		mapTmp.get(projectLeafNode.getId()).setValue(aggDataMap);
		populateExcelDataObject(requestTrackerId, repoWiseMRList, repoList, branchList, excelData, projectLeafNode);
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.MEAN_TIME_TO_MERGE.getColumns());
	}

	/**
	 * create data count object by day/week filter
	 *
	 * @param mergeReqList
	 * @param excelDataLoader
	 * @param branchName
	 * @param projectName
	 * @param aggDataMap
	 */
	private void setWeekWiseMeanTimeToMergeForRepoTools(Map<String, Double> mergeReqList,
			Map<String, Double> excelDataLoader, String branchName, String projectName,
			Map<String, List<DataCount>> aggDataMap, String duration, Integer dataPoints) {
		LocalDate currentDate = LocalDate.now();
		List<DataCount> dataCountList = new ArrayList<>();
		for (int i = 0; i < dataPoints; i++) {
			CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate, duration);
			double meanTimeToMerge = mergeReqList.getOrDefault(dateRange.getStartDate().toString(), 0.0d) * 1000;
			String date = getDateRange(dateRange, duration);
			DataCount dataCount = setDataCount(projectName, date, meanTimeToMerge);
			dataCountList.add(dataCount);
			excelDataLoader.put(date, (double) KpiHelperService.convertMilliSecondsToHours(meanTimeToMerge));
			currentDate = KpiHelperService.getNextRangeDate(duration, currentDate);

		}
		Collections.reverse(dataCountList);
		aggDataMap.put(branchName, dataCountList);

	}

	private String getDateRange(CustomDateRange dateRange, String duration) {
		String range = null;
		if (CommonConstant.WEEK.equalsIgnoreCase(duration)) {
			range = DateUtil.dateTimeConverter(dateRange.getStartDate().toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(dateRange.getEndDate().minusDays(2).toString(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	/**
	 * create date wise mean time to merge map
	 *
	 * @param repoToolKpiMetricResponsesCommit
	 * @param repoName
	 * @param branchName
	 * @param dateWisePickupTime
	 */
	private void createDateLabelWiseMap(List<RepoToolKpiMetricResponse> repoToolKpiMetricResponsesCommit,
			String repoName, String branchName, Map<String, Double> dateWisePickupTime) {

		for (RepoToolKpiMetricResponse response : repoToolKpiMetricResponsesCommit) {
			if (response.getRepositories() != null) {
				Optional<Branches> matchingBranch = response.getRepositories().stream()
						.filter(repository -> repository.getName().equals(repoName))
						.flatMap(repository -> repository.getBranches().stream())
						.filter(branch -> branch.getName().equals(branchName)).findFirst();

				double meanTimeToMerge = matchingBranch.map(Branches::getAverage).orElse(0.0d);
				dateWisePickupTime.put(response.getDateLabel(), meanTimeToMerge);
			}
		}
	}

	/**
	 * @param projectName
	 * @param week
	 * @param value
	 * @return
	 */
	private DataCount setDataCount(String projectName, String week, Double value) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value == null ? 0L : KpiHelperService.convertMilliSecondsToHours(value)));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(week);
		dataCount.setHoverValue(new HashMap<>());
		dataCount.setValue(value == null ? 0.0 : KpiHelperService.convertMilliSecondsToHours(value));
		return dataCount;
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return new HashMap<>();
	}

	/**
	 * get kpi data from repo tools api
	 *
	 * @param endDate
	 * @param toolMap
	 * @param node
	 * @param duration
	 * @param dataPoint
	 * @return
	 */
	private List<RepoToolKpiMetricResponse> getRepoToolsKpiMetricResponse(LocalDate endDate,
			Map<ObjectId, Map<String, List<Tool>>> toolMap, Node node, String duration, Integer dataPoint) {

		List<String> projectCodeList = new ArrayList<>();
		ProjectFilter accountHierarchyData = node.getProjectFilter();
		ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
		List<Tool> tools = toolMap.getOrDefault(configId, Collections.emptyMap()).getOrDefault(REPO_TOOLS,
				Collections.emptyList());
		if (CollectionUtils.isEmpty(tools)) {
			return new ArrayList<>();
		}
		projectCodeList.add(node.getId());

		LocalDate startDate = LocalDate.now();
		if (duration.equalsIgnoreCase(CommonConstant.WEEK)) {
			startDate = LocalDate.now().minusWeeks(dataPoint);
			while (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
				startDate = startDate.minusDays(1);
			}
		} else {
			int daysSubtracted = 0;
			while (daysSubtracted < dataPoint) {
				// Skip the weekend days
				if (!(startDate.getDayOfWeek() == DayOfWeek.SATURDAY || startDate.getDayOfWeek() == DayOfWeek.SUNDAY)) {
					daysSubtracted++;
				}
				startDate = startDate.minusDays(1);
			}
		}
		String debbieDuration = duration.equalsIgnoreCase(CommonConstant.WEEK) ? WEEK_FREQUENCY : DAY_FREQUENCY;
		return repoToolsConfigService.getRepoToolKpiMetrics(projectCodeList,
				customApiConfig.getRepoToolMeanTimeToMergeUrl(), startDate.toString(), endDate.toString(),
				debbieDuration);

	}

	private void populateExcelDataObject(String requestTrackerId, List<Map<String, Double>> repoWiseMRList,
			List<String> repoList, List<String> branchList, List<KPIExcelData> validationDataMap, Node node) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			String projectName = node.getProjectFilter().getName();
			KPIExcelUtility.populateMeanTimeMergeExcelData(projectName, repoWiseMRList, repoList, branchList,
					validationDataMap);

		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI158(),
				KPICode.REPO_TOOL_MEAN_TIME_TO_MERGE.getKpiId());
	}

}
