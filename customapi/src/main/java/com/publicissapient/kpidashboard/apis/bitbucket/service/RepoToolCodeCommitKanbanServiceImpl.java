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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
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
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.repotools.model.Branches;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.service.RepoToolsConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RepoToolCodeCommitKanbanServiceImpl extends BitBucketKPIService<Long, List<Object>, Map<String, Object>> {

	/**
	 *
	 */
	private static final String NO_CHECKIN = "No. of Checkins";
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
		return KPICode.REPO_TOOL_NUMBER_OF_CHECK_INS.name();
	}

	/**
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return
	 * @throws ApplicationException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes()
				.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.NUMBER_OF_CHECK_INS);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.NUMBER_OF_CHECK_INS);

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		trendValuesMap.forEach((key, dateWiseDataCount) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			dataCountGroup.setFilter(key);
			dataCountGroup.setValue(dateWiseDataCount);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		log.debug("[KANBAN-CODE-COMMIT-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> projectList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();
		List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseCommitList = getRepoToolsKpiMetricResponse(
				dateRange.getEndDate(), toolMap, projectList, kpiRequest);
		if (CollectionUtils.isNotEmpty(repoToolKpiMetricResponseCommitList))
			kpiWithFilter(repoToolKpiMetricResponseCommitList, mapTmp, projectList, kpiElement, kpiRequest);

	}

	private void kpiWithFilter(List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseCommitList,
			Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		Map<String, ValidationData> validationMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();

		leafNodeList.forEach(node -> {
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
			reposList.forEach(repo -> {
				if (!CollectionUtils.isEmpty(repo.getProcessorItemList())
						&& repo.getProcessorItemList().get(0).getId() != null) {
					Map<String, Long> excelDataLoader = new HashMap<>();
					Map<String, Long> mergeRequestExcelDataLoader = new HashMap<>();
					Map<String, Long> dateWiseCommitList = new HashMap<>();

					createDateLabelWiseMap(repoToolKpiMetricResponseCommitList, repo.getRepositoryName(),
							repo.getBranch(), dateWiseCommitList);
					aggCommitCountForRepo.putAll(dateWiseCommitList);
					List<DataCount> dayWiseCount = setDayWiseCountForProject(dateWiseCommitList, excelDataLoader,
							projectName, kpiRequest);
					aggDataMap.put(getBranchSubFilter(repo, projectName), dayWiseCount);
					repoWiseCommitList.add(excelDataLoader);
					repoWiseMergeRequestList.add(mergeRequestExcelDataLoader);
					repoList.add(repo.getUrl());
					branchList.add(repo.getBranch());

				}
			});
			List<DataCount> dayWiseCount = setDayWiseCountForProject(aggCommitCountForRepo, new HashMap<>(),
					projectName, kpiRequest);
			aggDataMap.put(Constant.AGGREGATED_VALUE, dayWiseCount);
			mapTmp.get(node.getId()).setValue(aggDataMap);

			if (getRequestTrackerIdKanban().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
				KPIExcelUtility.populateCodeCommitKanbanExcelData(node.getProjectFilter().getName(), repoWiseCommitList,
						repoList, branchList, excelData);
			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.CODE_COMMIT_MERGE_KANBAN.getColumns());
		kpiElement.setMapOfSprintAndData(validationMap);
	}

	/**
	 * create data count object by day/week filter
	 * @param commitCountForRepo
	 * @param excelDataLoader
	 * @param projectName
	 * @param kpiRequest
	 * @return
	 */
	private List<DataCount> setDayWiseCountForProject(Map<String, Long> commitCountForRepo,
			Map<String, Long> excelDataLoader, String projectName, KpiRequest kpiRequest) {
		LocalDate currentDate = LocalDate.now();
		List<DataCount> dayWiseCommitCount = new ArrayList<>();
		for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {
			CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
					kpiRequest.getDuration());
			DataCount dataCount = new DataCount();
			Map<String, Object> hoverValues = new HashMap<>();
			if (commitCountForRepo != null && commitCountForRepo.get(dateRange.getStartDate().toString()) != null) {
				Long commitForDay = commitCountForRepo.get(dateRange.getStartDate().toString());
				excelDataLoader.put(getDateRange(dateRange, kpiRequest.getDuration()), commitForDay);
				dataCount.setValue(commitForDay);
				hoverValues.put(NO_CHECKIN, commitForDay.intValue());
			} else {
				excelDataLoader.put(getDateRange(dateRange, kpiRequest.getDuration()), 0l);
				dataCount.setValue(0l);
				hoverValues.put(NO_CHECKIN, 0);

			}
			dataCount.setDate(getDateRange(dateRange, kpiRequest.getDuration()));
			dataCount.setHoverValue(hoverValues);
			dataCount.setSProjectName(projectName);
			dayWiseCommitCount.add(dataCount);
			currentDate = getNextRangeDate(kpiRequest.getDuration(), currentDate);
		}
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

	/**
	 * create date wise commit map
	 * @param repoToolKpiMetricResponsesCommit
	 * @param repoName
	 * @param branchName
	 * @param dateWiseCommitRepoTools
	 */
	private void createDateLabelWiseMap(List<RepoToolKpiMetricResponse> repoToolKpiMetricResponsesCommit,
			String repoName, String branchName, Map<String, Long> dateWiseCommitRepoTools) {

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
	}


	/**
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return
	 */
	@SuppressWarnings("PMD.AvoidCatchingGenericException")
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
	 * @param kpiRequest
	 * @return
	 */
	private List<RepoToolKpiMetricResponse> getRepoToolsKpiMetricResponse(LocalDate endDate,
			Map<ObjectId, Map<String, List<Tool>>> toolMap, List<Node> nodeList, KpiRequest kpiRequest) {

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
			LocalDate startDate = LocalDate.now().minusDays(kpiRequest.getKanbanXaxisDataPoints());
			if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
				startDate = LocalDate.now().minusWeeks(kpiRequest.getKanbanXaxisDataPoints());
				while (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
					startDate = startDate.minusDays(1);
				}
			}
			String debbieDuration = kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK) ? WEEK_FREQUENCY
					: DAY_FREQUENCY;
			repoToolKpiMetricResponseList = repoToolsConfigService.getRepoToolKpiMetrics(projectCodeList,
					customApiConfig.getRepoToolCodeCommmitsUrl(), startDate.toString(), endDate.toString(),
					debbieDuration);
		}

		return repoToolKpiMetricResponseList;
	}

	@Override
	public Long calculateKPIMetrics(Map<String, Object> commits) {
		return null;
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiName) {
		return calculateKpiValueForLong(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI159(),KPICode.REPO_TOOL_NUMBER_OF_CHECK_INS.getKpiId());
	}

}
