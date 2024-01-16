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
import com.publicissapient.kpidashboard.apis.enums.Filters;
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
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
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

/**
 * This service reflects the logic for the number of check-ins in master
 * metrics. The logic represent the calculations at the sprint, build and
 * release level.
 *
 */
@Component
@Slf4j
public class RepoToolCodeCommitServiceImpl extends BitBucketKPIService<Long, List<Object>, Map<String, Object>> {

	private static final String NO_CHECKIN = "No. of Check in";
	private static final String NO_MERGE = "No. of Merge Requests";
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
		return KPICode.REPO_TOOL_CODE_COMMIT.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.PROJECT) {
				projectWiseLeafNodeValue(kpiElement, mapTmp, v, kpiRequest);
			}

		});

		log.debug("[PROJECT-WISE][{}]. Values of leaf node after KPI calculation {}", kpiRequest.getRequestTrackerId(),
				root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.REPO_TOOL_CODE_COMMIT);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.CODE_COMMIT);
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

	/**
	 * Populates KPI value to project leaf nodes. It also gives the trend analysis
	 * project wise.
	 *
	 * @param kpiElement
	 * @param mapTmp
	 * @param projectLeafNodeList
	 */
	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp,
			List<Node> projectLeafNodeList, KpiRequest kpiRequest) {

		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);
		String requestTrackerId = getRequestTrackerId();
		LocalDate localEndDate = dateRange.getEndDate();

		Integer dataPoints = kpiRequest.getXAxisDataPoints();
		String duration = kpiRequest.getDuration();

		// gets the tool configuration
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();

		List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseCommitList = getRepoToolsKpiMetricResponse(
				localEndDate, toolMap, projectLeafNodeList, customApiConfig.getRepoToolCodeCommmitsUrl(), duration,
				dataPoints);
		List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseMergeList = getRepoToolsKpiMetricResponse(localEndDate,
				toolMap, projectLeafNodeList, customApiConfig.getRepoToolMeanTimeToMergeUrl(), duration, dataPoints);
		if (CollectionUtils.isEmpty(repoToolKpiMetricResponseCommitList)
				&& CollectionUtils.isEmpty(repoToolKpiMetricResponseMergeList)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No kpi data found for this project {}",
					projectLeafNodeList.get(0));
			return;
		}
		List<KPIExcelData> excelData = new ArrayList<>();
		projectLeafNodeList.stream().forEach(node -> {
			ProjectFilter accountHierarchyData = node.getProjectFilter();
			ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
			List<Tool> reposList = toolMap.get(configId).get(REPO_TOOLS) == null ? Collections.emptyList()
					: toolMap.get(configId).get(REPO_TOOLS);
			if (CollectionUtils.isEmpty(reposList)) {
				log.error("[BITBUCKET-AGGREGATED-VALUE]. No Jobs found for this project {}", node.getProjectFilter());
				return;
			}

			List<Map<String, Long>> repoWiseCommitList = new ArrayList<>();
			List<Map<String, Long>> repoWiseMergeRequestList = new ArrayList<>();
			List<String> repoList = new ArrayList<>();
			List<String> branchList = new ArrayList<>();
			String projectName = node.getProjectFilter().getName();
			Map<String, List<DataCount>> aggDataMap = new HashMap<>();
			Map<String, Long> aggCommitCountForRepo = new HashMap<>();
			Map<String, Long> aggMergeCountForRepo = new HashMap<>();
			reposList.forEach(repo -> {
				if (!CollectionUtils.isEmpty(repo.getProcessorItemList())
						&& repo.getProcessorItemList().get(0).getId() != null) {
					Map<String, Long> excelDataLoader = new HashMap<>();
					Map<String, Long> mergeRequestExcelDataLoader = new HashMap<>();
					Map<String, Long> dateWiseCommitList = new HashMap<>();
					Map<String, Long> dateWiseMRList = new HashMap<>();

					createDateLabelWiseMap(repoToolKpiMetricResponseCommitList, repoToolKpiMetricResponseMergeList,
							repo.getRepositoryName(), repo.getBranch(), dateWiseCommitList, dateWiseMRList);
					aggCommitAndMergeCount(aggCommitCountForRepo, aggMergeCountForRepo, dateWiseCommitList,
							dateWiseMRList);
					List<DataCount> dayWiseCount = setDayWiseCountForProject(dateWiseCommitList, dateWiseMRList,
							excelDataLoader, projectName, mergeRequestExcelDataLoader, duration, dataPoints);
					aggDataMap.put(getBranchSubFilter(repo, projectName), dayWiseCount);
					repoWiseCommitList.add(excelDataLoader);
					repoWiseMergeRequestList.add(mergeRequestExcelDataLoader);
					repoList.add(repo.getUrl());
					branchList.add(repo.getBranch());

				}
			});
			List<DataCount> dayWiseCount = setDayWiseCountForProject(aggCommitCountForRepo, aggMergeCountForRepo,
					new HashMap<>(), projectName, new HashMap<>(), duration, dataPoints);
			aggDataMap.put(Constant.AGGREGATED_VALUE, dayWiseCount);
			mapTmp.get(node.getId()).setValue(aggDataMap);

			populateExcelData(requestTrackerId, repoWiseCommitList, repoList, branchList, excelData, node,
					repoWiseMergeRequestList);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.CODE_COMMIT.getColumns());
	}

	/**
	 * @param aggCommitCountForRepo
	 * @param aggMergeCountForRepo
	 * @param commitCountForRepo
	 * @param mergeCountForRepo
	 */
	private void aggCommitAndMergeCount(Map<String, Long> aggCommitCountForRepo, Map<String, Long> aggMergeCountForRepo,
			Map<String, Long> commitCountForRepo, Map<String, Long> mergeCountForRepo) {
		if (MapUtils.isNotEmpty(commitCountForRepo)) {
			commitCountForRepo.forEach((key, value) -> aggCommitCountForRepo.merge(key, value, Long::sum));
		}
		if (MapUtils.isNotEmpty(mergeCountForRepo)) {
			mergeCountForRepo.forEach((key, value) -> aggMergeCountForRepo.merge(key, value, Long::sum));
		}
	}

	/**
	 * Populates validation data object.
	 *
	 * @param requestTrackerId
	 * @param repoWiseCommitList
	 * @param repoList
	 * @param branchList
	 * @param node
	 * @param repoWiseMergeRequestList
	 */
	private void populateExcelData(String requestTrackerId, List<Map<String, Long>> repoWiseCommitList,
			List<String> repoList, List<String> branchList, List<KPIExcelData> excelData, Node node,
			List<Map<String, Long>> repoWiseMergeRequestList) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			String projectName = node.getProjectFilter().getName();
			KPIExcelUtility.populateCodeCommit(projectName, repoWiseCommitList, repoList, branchList, excelData,
					repoWiseMergeRequestList);
		}
	}

	/**
	 * create data count object by day/week filter
	 * @param mergeCountForRepo
	 * 			list of merge request
	 * @param commitCountForRepo
	 *			list of commits
	 * @param excelDataLoader
	 * 			map of filter and commits
	 * @param mergeRequestExcelDataLoader
	 * 			map of filter and merge requests
	 * @return list of data count
	 */
	private List<DataCount> setDayWiseCountForProject(Map<String, Long> commitCountForRepo,
			Map<String, Long> mergeCountForRepo, Map<String, Long> excelDataLoader, String projectName,
			Map<String, Long> mergeRequestExcelDataLoader, String duration, Integer dataPoints) {
		LocalDate currentDate = LocalDate.now();
		List<DataCount> dayWiseCommitCount = new ArrayList<>();
		for (int i = 0; i < dataPoints; i++) {
			CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate, duration);
			DataCount dataCount = new DataCount();
			Map<String, Object> hoverValues = new HashMap<>();
			if (commitCountForRepo != null && commitCountForRepo.get(dateRange.getStartDate().toString()) != null) {
				Long commitForDay = commitCountForRepo.get(dateRange.getStartDate().toString());
				excelDataLoader.put(getDateRange(dateRange, duration), commitForDay);
				dataCount.setValue(commitForDay);
				hoverValues.put(NO_CHECKIN, commitForDay.intValue());
			} else {
				excelDataLoader.put(getDateRange(dateRange, duration), 0l);
				dataCount.setValue(0l);
				hoverValues.put(NO_CHECKIN, 0);

			}
			if (mergeCountForRepo != null && mergeCountForRepo.get(dateRange.getStartDate().toString()) != null) {
				Long mergeForDay = mergeCountForRepo.get(dateRange.getStartDate().toString());
				mergeRequestExcelDataLoader.put(getDateRange(dateRange, duration), mergeForDay);
				dataCount.setLineValue(mergeForDay);
				hoverValues.put(NO_MERGE, mergeForDay.intValue());

			} else {
				mergeRequestExcelDataLoader.put(getDateRange(dateRange, duration), 0l);
				dataCount.setLineValue(0l);
				hoverValues.put(NO_MERGE, 0);

			}
			dataCount.setDate(getDateRange(dateRange, duration));
			dataCount.setHoverValue(hoverValues);
			dataCount.setSProjectName(projectName);
			dayWiseCommitCount.add(dataCount);
			currentDate = getNextRangeDate(duration, currentDate);
		}
		Collections.reverse(dayWiseCommitCount);
		return dayWiseCommitCount;

	}

	private String getDateRange(CustomDateRange dateRange, String duration) {
		String range = null;
		if (CommonConstant.WEEK.equalsIgnoreCase(duration)) {
			range = DateUtil.dateTimeConverter(dateRange.getStartDate().toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(dateRange.getEndDate().toString(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	private LocalDate getNextRangeDate(String duration, LocalDate currentDate) {
		if ((CommonConstant.WEEK).equalsIgnoreCase(duration)) {
			currentDate = currentDate.minusWeeks(1);
		} else {
			currentDate = currentDate.minusDays(1);
		}
		return currentDate;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return new HashMap<>();
	}

	/**
	 * get kpi data from repo tools api
	 * @param endDate
	 * @param toolMap
	 * @param nodeList
	 * @param kpi
	 * @param duration
	 * @param dataPoint
	 * @return
	 */
	private List<RepoToolKpiMetricResponse> getRepoToolsKpiMetricResponse(LocalDate endDate,
			Map<ObjectId, Map<String, List<Tool>>> toolMap, List<Node> nodeList, String kpi, String duration,
			Integer dataPoint) {

		List<String> projectCodeList = new ArrayList<>();
		nodeList.forEach(node -> {
			ProjectFilter accountHierarchyData = node.getProjectFilter();
			ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
			List<Tool> tools = toolMap.getOrDefault(configId, Collections.emptyMap()).getOrDefault(REPO_TOOLS,
					Collections.emptyList());
			if (!CollectionUtils.isEmpty(tools)) {
				projectCodeList.add(node.getId());
			}
		});

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
			repoToolKpiMetricResponseList = repoToolsConfigService.getRepoToolKpiMetrics(projectCodeList, kpi,
					startDate.toString(), endDate.toString(), debbieDuration);
		}

		return repoToolKpiMetricResponseList;
	}

	@Override
	public Long calculateKPIMetrics(Map<String, Object> t) {

		return null;
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

	private void createDateLabelWiseMap(List<RepoToolKpiMetricResponse> repoToolKpiMetricResponsesCommit,
			List<RepoToolKpiMetricResponse> repoToolKpiMetricResponsesMR, String repoName, String branchName,
			Map<String, Long> dateWiseCommitRepoTools, Map<String, Long> dateWiseMRRepoTools) {

		for (RepoToolKpiMetricResponse response : repoToolKpiMetricResponsesCommit) {
			if (response.getProjectRepositories() != null) {
				Optional<Branches> matchingBranch = response.getProjectRepositories().stream()
						.filter(repository -> repository.getRepository().equals(repoName))
						.flatMap(repository -> repository.getBranchesCommitsCount().stream())
						.filter(branch -> branch.getBranchName().equals(branchName)).findFirst();

				Long commitValue = matchingBranch.map(Branches::getCount).orElse(0L);
				dateWiseCommitRepoTools.put(response.getDateLabel(), commitValue);
			}
		}

		for (RepoToolKpiMetricResponse response : repoToolKpiMetricResponsesMR) {
			if (response.getRepositories() != null) {
				Optional<Branches> matchingBranch = response.getRepositories().stream()
						.filter(repository -> repository.getName().equals(repoName))
						.flatMap(repository -> repository.getBranches().stream())
						.filter(branch -> branch.getName().equals(branchName)).findFirst();

				Long mrValue = matchingBranch.isPresent() ? matchingBranch.get().getMergeRequestList().size() : 0l;
				dateWiseMRRepoTools.put(response.getDateLabel(), mrValue);
			}
		}
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI157(),KPICode.REPO_TOOL_CODE_COMMIT.getKpiId());
	}

}
