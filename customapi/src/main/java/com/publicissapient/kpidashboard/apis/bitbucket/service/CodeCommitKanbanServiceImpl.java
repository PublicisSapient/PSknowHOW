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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
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
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.scm.CommitDetails;
import com.publicissapient.kpidashboard.common.repository.scm.CommitRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CodeCommitKanbanServiceImpl extends BitBucketKPIService<Long, List<Object>, Map<String, Object>> {

	/**
	 *
	 */
	private static final int MILISEC_ONE_DAY = 86_399_999;
	private static final String AZURE_REPO = "AzureRepository";
	private static final String BITBUCKET = "Bitbucket";
	private static final String GITLAB = "GitLab";
	private static final String GITHUB = "GitHub";
	private static final String DATE = "Date ";
	private static final String COMMIT_COUNT = "commitCount";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CommitRepository commitRepository;

	@Override
	public String getQualifierType() {
		return KPICode.NUMBER_OF_CHECK_INS.name();
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

		String startDate = dateRange.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String endDate = dateRange.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		Map<String, Object> resultMap = fetchKPIDataFromDb(projectList, startDate, endDate, null);
		kpiWithFilter(resultMap, mapTmp, projectList, kpiElement, kpiRequest);

	}

	private void kpiWithFilter(Map<String, Object> resultMap, Map<String, Node> mapTmp, List<Node> leafNodeList,
			KpiElement kpiElement, KpiRequest kpiRequest) {
		Map<String, ValidationData> validationMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();
		List<CommitDetails> commitList = (List<CommitDetails>) resultMap.get(COMMIT_COUNT);
		final Map<ObjectId, Map<String, Long>> commitListItemId = new LinkedHashMap<>();
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();
		if (CollectionUtils.isNotEmpty(commitList)) {
			commitListItemId.putAll(commitList.stream().collect(Collectors.groupingBy(CommitDetails::getProcessorItemId,
					Collectors.toMap(CommitDetails::getDate, CommitDetails::getCount))));
		}

		leafNodeList.forEach(node -> {
			List<Map<String, Long>> repoWiseCommitList = new LinkedList<>();
			Map<String, List<DataCount>> projectWiseDataMap = new LinkedHashMap<>();
			List<String> listOfRepo = new LinkedList<>();
			List<String> listOfBranch = new LinkedList<>();
			LocalDate currentDate = LocalDate.now();
			String projectNodeId = node.getId();
			for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {

				CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
						kpiRequest.getDuration());
				List<Tool> reposList = getBitBucketJobs(toolMap, node);
				if (CollectionUtils.isEmpty(reposList)) {
					log.error("[CODE_COMMIT_KANBAN]. No Jobs found for this project {}", node.getProjectFilter());
					return;
				}
				String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
				Map<String, Long> filterValueMap = filterKanbanDataBasedOnStartAndEndDateAndCommitDetails(reposList,
						dateRange, commitListItemId, projectName, repoWiseCommitList, listOfRepo, listOfBranch);

				String dataCountDate = getRange(dateRange, kpiRequest);
				prepareRepoWiseMap(filterValueMap, projectName, dataCountDate, projectWiseDataMap);
				currentDate = getNextRangeDate(kpiRequest, currentDate);

				if (getRequestTrackerIdKanban().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					KPIExcelUtility.populateCodeCommitKanbanExcelData(node.getProjectFilter().getName(),
							repoWiseCommitList, listOfRepo, listOfBranch, excelData);
				}

			}
			mapTmp.get(projectNodeId).setValue(projectWiseDataMap);

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.CODE_COMMIT_MERGE_KANBAN.getColumns());
		kpiElement.setMapOfSprintAndData(validationMap);
	}

	/**
	 * loop tool wise to fetch data for each day
	 *
	 * @param reposList
	 * @param dateRange
	 * @param commitListItemId
	 * @param projectName
	 * @param repoWiseCommitList
	 * @param listOfRepo
	 * @param listOfBranch
	 * @return
	 */
	private Map<String, Long> filterKanbanDataBasedOnStartAndEndDateAndCommitDetails(List<Tool> reposList,
			CustomDateRange dateRange, Map<ObjectId, Map<String, Long>> commitListItemId, String projectName,
			List<Map<String, Long>> repoWiseCommitList, List<String> listOfRepo, List<String> listOfBranch) {
		LocalDate startDate = dateRange.getStartDate();
		LocalDate endDate = dateRange.getEndDate();
		Map<String, Long> filterWiseValue = new HashMap<>();
		Map<String, Long> excelLoaderfinal = new HashMap<>();

		for (Tool tool : reposList) {
			LocalDate currentDate = startDate;
			Map<String, Long> excelLoader = new LinkedHashMap<>();
			String keyName = getBranchSubFilter(tool, projectName);
			Long commitCountValue = 0l;
			if (!CollectionUtils.isEmpty(tool.getProcessorItemList())
					&& tool.getProcessorItemList().get(0).getId() != null) {
				Map<String, Long> commitDateMap = commitListItemId
						.getOrDefault(tool.getProcessorItemList().get(0).getId(), new HashMap<>());
				while (currentDate.compareTo(endDate) <= 0) {
					commitCountValue = commitCountValue + commitDateMap.getOrDefault(currentDate.toString(), 0l);
					excelLoader.put(DATE + DateUtil.localDateTimeConverter(currentDate), commitCountValue);
					currentDate = currentDate.plusDays(1);
				}
				// if data is there for any branch then only will shown on excel
				if (MapUtils.isNotEmpty(excelLoader)) {
					listOfRepo.add(tool.getUrl());
					listOfBranch.add(tool.getBranch());
					excelLoaderfinal.putAll(excelLoader);
				}
			}
			filterWiseValue.putIfAbsent(keyName, commitCountValue);

		}
		repoWiseCommitList.add(excelLoaderfinal);
		return filterWiseValue;
	}

	private LocalDate getNextRangeDate(KpiRequest kpiRequest, LocalDate currentDate) {
		if ((CommonConstant.WEEK).equalsIgnoreCase(kpiRequest.getDuration())) {
			currentDate = currentDate.minusWeeks(1);
		} else if (CommonConstant.MONTH.equalsIgnoreCase(kpiRequest.getDuration())) {
			currentDate = currentDate.minusMonths(1);
		} else {
			currentDate = currentDate.minusDays(1);
		}
		return currentDate;
	}

	private void prepareRepoWiseMap(Map<String, Long> filterWiseValue, String projectName, String dataCountDate,
			Map<String, List<DataCount>> projectWiseDataMap) {
		List<Long> commitCountList = new ArrayList<>();
		filterWiseValue.forEach((filter, value) -> {
			commitCountList.add(value);
			DataCount dataCountObject = getDataCountObject(value, projectName, dataCountDate);
			projectWiseDataMap.computeIfAbsent(filter, k -> new LinkedList<>()).add(dataCountObject);
		});
		DataCount dcObj = getDataCountObject(calculateKpiValue(commitCountList, KPICode.NUMBER_OF_CHECK_INS.getKpiId()),
				projectName, dataCountDate);
		projectWiseDataMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>()).add(dcObj);
	}

	private DataCount getDataCountObject(Long value, String projectName, String date) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSSprintID(date);
		dataCount.setSSprintName(date);
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setSprintNames(new ArrayList<>(Arrays.asList(projectName)));
		dataCount.setValue(value);
		Map<String, Object> hoverValue = new HashMap<>();
		hoverValue.put("No. of Checkins", value.intValue());
		dataCount.setHoverValue(hoverValue);
		return dataCount;
	}

	private String getRange(CustomDateRange dateRange, KpiRequest kpiRequest) {
		String range = null;
		if (CommonConstant.WEEK.equalsIgnoreCase(kpiRequest.getDuration())) {
			range = DateUtil.dateTimeConverter(dateRange.getStartDate().toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(dateRange.getEndDate().toString(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
		} else if (CommonConstant.MONTH.equalsIgnoreCase(kpiRequest.getDuration())) {
			range = dateRange.getStartDate().getMonth().toString() + " " + dateRange.getStartDate().getYear();
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
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
		Set<String> listOfmapOfProjectFilters = new HashSet<>();
		Set<String> branchList = new HashSet<>();

		List<ObjectId> tools = new ArrayList<>();
		Map<String, Object> resultListMap = new HashMap<>();
		Map<ObjectId, Map<String, List<Tool>>> toolMap;
		// gets the tool configuration
		toolMap = configHelperService.getToolItemMap();

		BasicDBList filter = new BasicDBList();
		leafNodeList.forEach(node -> {

			List<Tool> bitbucketJob = getBitBucketJobs(toolMap, node);
			if (CollectionUtils.isEmpty(bitbucketJob)) {
				return;
			}
			bitbucketJob.forEach(job -> {
				if (CollectionUtils.isEmpty(job.getProcessorItemList())) {
					return;
				}
				tools.add(job.getProcessorItemList().get(0).getId());
				filter.add(new BasicDBObject("processorItemId", job.getProcessorItemList().get(0).getId())
						.append("branch", job.getBranch()).append("repoSlug", job.getRepoSlug()));
				listOfmapOfProjectFilters.add(job.getUrl());
				branchList.add(job.getBranch());
			});
		});

		if (filter.isEmpty()) {
			return new HashMap<>();
		}

		List<CommitDetails> commitCount = commitRepository.findCommitList(tools,
				new DateTime(startDate, DateTimeZone.UTC).withTimeAtStartOfDay().getMillis(),
				StringUtils.isNotEmpty(endDate)
						? new DateTime(endDate, DateTimeZone.UTC).withTimeAtStartOfDay().plus(MILISEC_ONE_DAY)
								.getMillis()
						: new Date().getTime(),
				filter);

		resultListMap.put(COMMIT_COUNT, commitCount);

		return resultListMap;
	}

	private List<Tool> getBitBucketJobs(Map<ObjectId, Map<String, List<Tool>>> toolMap, Node node) {
		ProjectFilter projectFilter = node.getProjectFilter();
		ObjectId configId = projectFilter == null ? null : projectFilter.getBasicProjectConfigId();
		Map<String, List<Tool>> toolListMap = toolMap == null ? null : toolMap.get(configId);
		List<Tool> bitbucketJob = new ArrayList<>();
		if (null != toolListMap) {
			bitbucketJob
					.addAll(toolListMap.get(BITBUCKET) == null ? Collections.emptyList() : toolListMap.get(BITBUCKET));
			bitbucketJob.addAll(
					toolListMap.get(AZURE_REPO) == null ? Collections.emptyList() : toolListMap.get(AZURE_REPO));
			bitbucketJob.addAll(toolListMap.get(GITLAB) == null ? Collections.emptyList() : toolListMap.get(GITLAB));
			bitbucketJob.addAll(toolListMap.get(GITHUB) == null ? Collections.emptyList() : toolListMap.get(GITHUB));
		}

		if (CollectionUtils.isEmpty(bitbucketJob)) {
			log.error("[BITBUCKET]. No repository found for this project {}", node.getAccountHierarchy());
		}

		return bitbucketJob;

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
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI65(),KPICode.NUMBER_OF_CHECK_INS.getKpiId());
	}
}
