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

import java.util.*;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiMetricResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.repotools.model.Branches;
import com.publicissapient.kpidashboard.apis.repotools.service.RepoToolsConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This service reflects the logic for the number of check-ins in master
 * metrics. The logic represent the calculations at the sprint, build and
 * release level.
 *
 */
@Component
@Slf4j
public class RepoToolCodeCommitServiceImpl extends BitBucketKPIService<Long, List<Object>, Map<String, Object>> {

	private static final int MILISEC_ONE_DAY = 86_399_999;
	private static final String AZURE_REPO = "AzureRepository";
	private static final String BITBUCKET = "Bitbucket";
	private static final String GITLAB = "GitLab";
	private static final String GITHUB = "GitHub";
	private static final String YYYYMMDD = "yyyy-MM-dd";
	private static final String NO_CHECKIN = "No. of Check in";
	private static final String NO_MERGE = "No. of Merge Requests";
	public static final String REPO_TOOLS_COMMIT_KPI = "repo-activity-bulk/";
	public static final String REPO_TOOLS_MR_KPI = "pr-size-bulk/";
	public static final String FREQUENCY = "day";
	private static final String REPO_TOOLS = "RepoTool";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private RepoToolsConfigServiceImpl repoToolsConfigService;

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
				projectWiseLeafNodeValue(kpiElement, mapTmp, v);
			}

		});

		log.debug("[PROJECT-WISE][{}]. Values of leaf node after KPI calculation {}", kpiRequest.getRequestTrackerId(),
				root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.REPO_TOOL_CODE_COMMIT);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, nodeWiseKPIValue,
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
			List<Node> projectLeafNodeList) {

		String requestTrackerId = getRequestTrackerId();
		DateTimeFormatter formatter = DateTimeFormat.forPattern(YYYYMMDD);
		String endDate = formatter.print(DateTime.now());
		String startDate = formatter
				.print(DateTime.now().minusDays(customApiConfig.getRepoXAxisCountForCheckInsAndMergeRequests() - 1));

		// gets the tool configuration
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();

		List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseCommitList = getRepoToolsKpiMetricResponse(startDate,
				endDate, toolMap, projectLeafNodeList, REPO_TOOLS_COMMIT_KPI);
		List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseMergeList = getRepoToolsKpiMetricResponse(startDate,
				endDate, toolMap, projectLeafNodeList, REPO_TOOLS_MR_KPI);
		List<KPIExcelData> excelData = new ArrayList<>();
		projectLeafNodeList.stream().forEach(node -> {
			DateTime start = new DateTime(startDate, DateTimeZone.UTC);
			DateTime end = new DateTime(endDate, DateTimeZone.UTC);
			ProjectFilter accountHierarchyData = node.getProjectFilter();
			ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
			Map<String, List<Tool>> mapOfListOfTools = toolMap.get(configId);
			List<Tool> reposList = new ArrayList<>();
			populateRepoList(reposList, mapOfListOfTools);
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
					List<DataCount> dayWiseCount = new ArrayList<>();
					if (CollectionUtils.isNotEmpty(repoToolKpiMetricResponseMergeList)
							|| CollectionUtils.isNotEmpty(repoToolKpiMetricResponseCommitList)) {
						Map<String, Long> dateWiseCommitList = new HashMap<>();
						Map<String, Long> dateWiseMRList = new HashMap<>();

						createDateLabelWiseMap(repoToolKpiMetricResponseCommitList, repoToolKpiMetricResponseMergeList,
								repo.getRepositoryName(), repo.getBranch(), dateWiseCommitList, dateWiseMRList);
						aggCommitAndMergeCount(aggCommitCountForRepo, aggMergeCountForRepo, dateWiseCommitList,
								dateWiseMRList);
						dayWiseCount = setDayWiseCountForProject(dateWiseCommitList, dateWiseMRList, excelDataLoader,
								start, end, projectName, mergeRequestExcelDataLoader);
					}
					aggDataMap.put(getBranchSubFilter(repo, projectName), dayWiseCount);
					repoWiseCommitList.add(excelDataLoader);
					repoWiseMergeRequestList.add(mergeRequestExcelDataLoader);
					repoList.add(repo.getUrl());
					branchList.add(repo.getBranch());

				}
			});
			List<DataCount> dayWiseCount = setDayWiseCountForProject(aggMergeCountForRepo, aggCommitCountForRepo,
					new HashMap<>(), start, end, projectName, new HashMap<>());
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
			aggCommitCountForRepo.putAll(commitCountForRepo);
		}
		if (MapUtils.isNotEmpty(mergeCountForRepo)) {
			aggMergeCountForRepo.putAll(mergeCountForRepo);
		}
	}


	/**
	 * populate repolist from map
	 *
	 * @param reposList
	 * @param mapOfListOfTools
	 */
	private void populateRepoList(List<Tool> reposList, Map<String, List<Tool>> mapOfListOfTools) {
		if (null != mapOfListOfTools) {
			reposList.addAll(mapOfListOfTools.get(BITBUCKET) == null ? Collections.emptyList()
					: mapOfListOfTools.get(BITBUCKET));
			reposList.addAll(mapOfListOfTools.get(AZURE_REPO) == null ? Collections.emptyList()
					: mapOfListOfTools.get(AZURE_REPO));
			reposList.addAll(
					mapOfListOfTools.get(GITLAB) == null ? Collections.emptyList() : mapOfListOfTools.get(GITLAB));
			reposList.addAll(
					mapOfListOfTools.get(GITHUB) == null ? Collections.emptyList() : mapOfListOfTools.get(GITHUB));
			reposList.addAll(mapOfListOfTools.get(REPO_TOOLS) == null ? Collections.emptyList()
					: mapOfListOfTools.get(REPO_TOOLS));
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
	 * @param mergeCountForRepo
	 * @param commitCountForRepo
	 * @param excelDataLoader
	 * @param mergeRequestExcelDataLoader
	 */
	private List<DataCount> setDayWiseCountForProject(Map<String, Long> commitCountForRepo,
			Map<String, Long> mergeCountForRepo, Map<String, Long> excelDataLoader, DateTime start, DateTime end,
			String projectName, Map<String, Long> mergeRequestExcelDataLoader) {
		List<DataCount> dayWiseCommitCount = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormat.forPattern(YYYYMMDD);
		DateTime startDateTime = start;
		while (DateTimeComparator.getDateOnlyInstance().compare(startDateTime, end) <= 0) {
			String currentDate = formatter.print(startDateTime);
			DataCount dataCount = new DataCount();
			Map<String, Object> hoverValues = new HashMap<>();
			if (commitCountForRepo != null && commitCountForRepo.get(currentDate) != null) {
				Long commitForDay = commitCountForRepo.get(currentDate);
				excelDataLoader.put(DateUtil.dateTimeConverter(currentDate, YYYYMMDD, DateUtil.DISPLAY_DATE_FORMAT),
						commitForDay);
				dataCount.setValue(commitForDay);
				hoverValues.put(NO_CHECKIN, commitForDay.intValue());
			} else {
				excelDataLoader.put(DateUtil.dateTimeConverter(currentDate, YYYYMMDD, DateUtil.DISPLAY_DATE_FORMAT),
						0l);
				dataCount.setValue(0l);
				hoverValues.put(NO_CHECKIN, 0);

			}
			if (mergeCountForRepo != null && mergeCountForRepo.get(currentDate) != null) {
				Long mergeForDay = mergeCountForRepo.get(currentDate);
				mergeRequestExcelDataLoader.put(
						DateUtil.dateTimeConverter(currentDate, YYYYMMDD, DateUtil.DISPLAY_DATE_FORMAT), mergeForDay);
				dataCount.setLineValue(mergeForDay);
				hoverValues.put(NO_MERGE, mergeForDay.intValue());

			} else {
				mergeRequestExcelDataLoader
						.put(DateUtil.dateTimeConverter(currentDate, YYYYMMDD, DateUtil.DISPLAY_DATE_FORMAT), 0l);
				dataCount.setLineValue(0l);
				hoverValues.put(NO_MERGE, 0);

			}
			dataCount.setDate(DateUtil.dateTimeConverter(currentDate, YYYYMMDD, DateUtil.DISPLAY_DATE_FORMAT));
			dataCount.setHoverValue(hoverValues);
			dataCount.setSProjectName(projectName);
			dayWiseCommitCount.add(dataCount);
			startDateTime = startDateTime.plusDays(1);
		}
		return dayWiseCommitCount;

	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return null;
	}

	private List<RepoToolKpiMetricResponse> getRepoToolsKpiMetricResponse(String startDate, String endDate,
			Map<ObjectId, Map<String, List<Tool>>> toolMap, List<Node> nodeList, String kpi) {

		List<String> projectCodeList =new ArrayList<>();
		nodeList.forEach(node -> {
			ProjectFilter accountHierarchyData = node.getProjectFilter();
			ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
			List<Tool> tools = toolMap.getOrDefault(configId, Collections.emptyMap()).getOrDefault(REPO_TOOLS, Collections.emptyList());
			if (!CollectionUtils.isEmpty(tools)) {
				projectCodeList.add(node.getId());
			}
		});

		List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(projectCodeList)) {
			repoToolKpiMetricResponseList = repoToolsConfigService.getRepoToolKpiMetrics(projectCodeList,
					kpi, startDate, endDate, FREQUENCY);
		}

		return repoToolKpiMetricResponseList;
	}

	private List<Tool> getRepoToolsJobs(Map<ObjectId, Map<String, List<Tool>>> toolMap, Node node) {
		ProjectFilter accountHierarchyData = node.getProjectFilter();
		ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
		Map<String, List<Tool>> toolListMap = toolMap == null ? null : toolMap.get(configId);
		List<Tool> bitbucketJob = new ArrayList<>();
		if (null != toolListMap) {
			bitbucketJob.addAll(
					toolListMap.get(REPO_TOOLS) == null ? Collections.emptyList() : toolListMap.get(REPO_TOOLS));
		}
		if (CollectionUtils.isEmpty(bitbucketJob)) {
			log.error("[BITBUCKET]. No repository found for this project {}", node.getProjectFilter());
		}
		return bitbucketJob;
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

				Long mrValue = matchingBranch.map(Branches::getMergeRequests).orElse(0L);
				dateWiseMRRepoTools.put(response.getDateLabel(), mrValue);
			}
		}
	}

}
