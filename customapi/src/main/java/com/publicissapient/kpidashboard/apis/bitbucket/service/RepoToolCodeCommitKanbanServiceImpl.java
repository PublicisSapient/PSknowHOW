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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolUserDetails;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolValidationData;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
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
import com.publicissapient.kpidashboard.apis.repotools.model.Branches;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiMetricResponse;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RepoToolCodeCommitKanbanServiceImpl extends BitBucketKPIService<Long, List<Object>, Map<String, Object>> {

	private static final String NO_CHECKIN = "No. of Checkins";
	private static final String REPO_TOOLS = "RepoTool";
	private static final String ASSIGNEE = "assignee";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Override
	public String getQualifierType() {
		return KPICode.REPO_TOOL_NUMBER_OF_CHECK_INS.name();
	}

	/**
	 * get kpi data
	 * @param kpiRequest
	 * 				kpi request
	 * @param kpiElement
	 * 				kpi element
	 * @param projectNode
	 * 				project node
	 * @return kpiElement
	 * @throws ApplicationException
	 * 				throws application exception
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node projectNode)
			throws ApplicationException {
		Map<String, Node> mapTmp = new HashMap<>();
		mapTmp.put(projectNode.getId(), projectNode);
		dateWiseLeafNodeValue(projectNode, mapTmp, kpiElement, kpiRequest);
		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(projectNode, nodeWiseKPIValue, KPICode.NUMBER_OF_CHECK_INS);
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
				kpiRequest.getRequestTrackerId(), projectNode);
		return kpiElement;
	}

	private void dateWiseLeafNodeValue(Node projectNode, Map<String, Node> mapTmp, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();
		List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseCommitList = kpiHelperService.getRepoToolsKpiMetricResponse(
				dateRange.getEndDate(), toolMap, projectNode, kpiRequest.getDuration(), kpiRequest.getXAxisDataPoints(),
				customApiConfig.getRepoToolCodeCommmitsUrl());
		if (CollectionUtils.isNotEmpty(repoToolKpiMetricResponseCommitList))
			kpiWithFilter(repoToolKpiMetricResponseCommitList, mapTmp, projectNode, kpiElement, kpiRequest);

	}

	/**
	 * Populates KPI value to project leaf nodes. It also gives the trend analysis project wise.
	 *
	 * @param kpiElement
	 * 		kpi element
	 * @param mapTmp
	 * 		node map
	 * @param node
	 * 		leaf node of project
	 * @param kpiRequest
	 * 		kpi request
	 */
	@SuppressWarnings("unchecked")
	private void kpiWithFilter(List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseCommitList,
			Map<String, Node> mapTmp, Node node, KpiElement kpiElement, KpiRequest kpiRequest) {
		Map<String, ValidationData> validationMap = new HashMap<>();
		List<KPIExcelData> excelData = new ArrayList<>();
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();

		ProjectFilter accountHierarchyData = node.getProjectFilter();
		ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
		List<Tool> reposList = toolMap.get(configId).get(REPO_TOOLS) == null
				? Collections.emptyList()
				: toolMap.get(configId).get(REPO_TOOLS);
		if (CollectionUtils.isEmpty(reposList)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No Jobs found for this project {}", node.getProjectFilter());
			return;
		}
		String projectName = node.getProjectFilter().getName();
		Map<String, List<DataCount>> aggDataMap = new HashMap<>();
		Map<String, Object> resultmap = fetchKPIDataFromDb(List.of(node), null, null, kpiRequest);
		Set<Assignee> assignees = (Set<Assignee>) resultmap.get(ASSIGNEE);
		Set<String> overAllUsers = repoToolKpiMetricResponseCommitList.stream()
				.flatMap(value -> value.getUsers().stream()).map(RepoToolUserDetails::getCommitterEmail)
				.collect(Collectors.toSet());
		List<RepoToolValidationData> repoToolValidationDataList = new ArrayList<>();
		LocalDate currentDate = LocalDate.now();
		for (int i = 0; i < kpiRequest.getXAxisDataPoints(); i++) {
			LocalDate finalCurrentDate = currentDate;
			CustomDateRange weekRange = KpiDataHelper.getStartAndEndDateForDataFiltering(finalCurrentDate,
					kpiRequest.getDuration());
			String date = KpiHelperService.getDateRange(weekRange, kpiRequest.getDuration());

			Optional<RepoToolKpiMetricResponse> repoToolKpiMetricResponse = repoToolKpiMetricResponseCommitList.stream()
					.filter(value -> value.getDateLabel().equals(weekRange.getStartDate().toString())).findFirst();
			Long overAllCommitCount = repoToolKpiMetricResponse.map(RepoToolKpiMetricResponse::getCommitCount)
					.orElse(0L);
			setDataCount(projectName, date, Constant.AGGREGATED_VALUE + "#" + Constant.AGGREGATED_VALUE,
					overAllCommitCount, aggDataMap);
			reposList.forEach(repo -> {
				if (!CollectionUtils.isEmpty(repo.getProcessorItemList()) && repo.getProcessorItemList().get(0)
						.getId() != null) {
					String branchName = getBranchSubFilter(repo, projectName);
					Long commitCount = 0L;
					String overallKpiGroup = branchName + "#" + Constant.AGGREGATED_VALUE;
					List<RepoToolUserDetails> repoToolUserDetailsList = new ArrayList<>();

					if (repoToolKpiMetricResponse.isPresent()) {
						Optional<Branches> matchingBranch = repoToolKpiMetricResponse.get().getProjectRepositories()
								.stream()
								.filter(repository -> repository.getRepository().equals(repo.getRepositoryName()))
								.flatMap(repository -> repository.getBranchesCommitsCount().stream())
								.filter(branch -> branch.getBranchName().equals(repo.getBranch())).findFirst();

						commitCount = matchingBranch.map(Branches::getCount).orElse(0L);
						repoToolUserDetailsList = matchingBranch.map(Branches::getUsers).orElse(new ArrayList<>());
					}
					repoToolValidationDataList.addAll(
							setUserDataCounts(overAllUsers, repoToolUserDetailsList, assignees, branchName, projectName,
									date, aggDataMap));
					setDataCount(projectName, date, overallKpiGroup, commitCount, aggDataMap);
				}
			});
			List<RepoToolUserDetails> repoToolUserDetails = repoToolKpiMetricResponse.map(
					RepoToolKpiMetricResponse::getUsers).orElse(new ArrayList<>());
			repoToolValidationDataList.addAll(
					setUserDataCounts(overAllUsers, repoToolUserDetails, assignees, Constant.AGGREGATED_VALUE,
							projectName, date, aggDataMap));
			currentDate = KpiHelperService.getNextRangeDate(kpiRequest.getDuration(), currentDate);
		}
		mapTmp.get(node.getId()).setValue(aggDataMap);

		if (getRequestTrackerIdKanban().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateCodeCommitKanbanExcelData(repoToolValidationDataList, excelData);
		}
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.CODE_COMMIT_MERGE_KANBAN.getColumns());
		kpiElement.setMapOfSprintAndData(validationMap);
	}

	/**
	 * fetch data from db
	 *
	 * @param leafNodeList
	 * 		leaf node list
	 * @param startDate
	 * 		start date
	 * @param endDate
	 * 		end date
	 * @param kpiRequest
	 * 		kpi request
	 * @return map of data
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		AssigneeDetails assigneeDetails = assigneeDetailsRepository.findByBasicProjectConfigId(
				leafNodeList.get(0).getId());
		Set<Assignee> assignees = assigneeDetails != null ? assigneeDetails.getAssignee() : new HashSet<>();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ASSIGNEE, assignees);
		return resultMap;
	}

	/**
	 * set data count for user filter
	 *
	 * @param overAllUsers
	 * 		list of user emails from repotool
	 * @param repoToolUserDetailsList
	 * 		list of repo tool user data
	 * @param assignees
	 * 		assignee data
	 * @param filter
	 * 		branch filter
	 * @param projectName
	 * 		project name
	 * @param date
	 * 		date
	 * @param dateUserWiseAverage
	 * 		total data map
	 * @return repotool validation data
	 */
	private List<RepoToolValidationData> setUserDataCounts(Set<String> overAllUsers,
			List<RepoToolUserDetails> repoToolUserDetailsList, Set<Assignee> assignees, String filter,
			String projectName, String date, Map<String, List<DataCount>> dateUserWiseAverage) {

		List<RepoToolValidationData> repoToolValidationDataList = new ArrayList<>();
		overAllUsers.forEach(userEmail -> {
			Optional<RepoToolUserDetails> repoToolUserDetails = repoToolUserDetailsList.stream()
					.filter(user -> userEmail.equalsIgnoreCase(user.getCommitterEmail())).findFirst();
			Optional<Assignee> assignee = assignees.stream().filter(assign -> assign.getEmail().contains(userEmail))
					.findFirst();

			String developerName = assignee.isPresent() ? assignee.get().getAssigneeName() : userEmail;
			Long commitCount = repoToolUserDetails.map(RepoToolUserDetails::getCount).orElse(0L);
			String userKpiGroup = filter + "#" + developerName;
			if(repoToolUserDetails.isPresent()) {
				RepoToolValidationData repoToolValidationData = new RepoToolValidationData();
				repoToolValidationData.setProjectName(projectName);
				repoToolValidationData.setBranchName(filter);
				repoToolValidationData.setDeveloperName(developerName);
				repoToolValidationData.setDate(date);
				repoToolValidationData.setCommitCount(commitCount);
				repoToolValidationDataList.add(repoToolValidationData);
			}
			setDataCount(projectName, date, userKpiGroup, commitCount, dateUserWiseAverage);
		});
		return repoToolValidationDataList;
	}

	/**
	 * set individual data count
	 * @param projectName
	 * 				project name
	 * @param week
	 * 				date
	 * @param kpiGroup
	 * 				combined filter
	 * @param commitValue
	 * 				value
	 * @param dataCountMap
	 * 				data count map by filter
	 */
	private void setDataCount(String projectName, String week, String kpiGroup, Long commitValue,
			Map<String, List<DataCount>> dataCountMap) {
		DataCount dataCount = new DataCount();
		dataCount.setSProjectName(projectName);
		dataCount.setDate(week);
		dataCount.setValue(commitValue);
		dataCount.setKpiGroup(kpiGroup);
		Map<String, Object> hoverValues = new HashMap<>();
		hoverValues.put(NO_CHECKIN, commitValue);
		dataCount.setHoverValue(hoverValues);
		dataCountMap.computeIfAbsent(kpiGroup, k -> new ArrayList<>()).add(dataCount);
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
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI159(),
				KPICode.REPO_TOOL_NUMBER_OF_CHECK_INS.getKpiId());
	}

}
