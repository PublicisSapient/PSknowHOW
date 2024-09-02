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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolUserDetails;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolValidationData;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.Tool;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PRSizeServiceImpl extends BitBucketKPIService<Long, List<Object>, Map<String, Object>> {

	public static final String MR_COUNT = "No of PRs";
	private static final String AZURE_REPO = "AzureRepository";
	private static final String BITBUCKET = "Bitbucket";
	private static final String GITLAB = "GitLab";
	private static final String GITHUB = "GitHub";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Override
	public String getQualifierType() {
		return KPICode.PR_SIZE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node projectNode)
			throws ApplicationException {

		Map<String, Node> mapTmp = new HashMap<>();
		mapTmp.put(projectNode.getId(), projectNode);
		projectWiseLeafNodeValue(kpiElement, mapTmp, projectNode, kpiRequest);
		log.debug("[PROJECT-WISE][{}]. Values of leaf node after KPI calculation {}", kpiRequest.getRequestTrackerId(),
				projectNode);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(projectNode, nodeWiseKPIValue, KPICode.PR_SIZE);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.PR_SIZE);
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
			// split for filters
			String[] issueFilter = issueType.split("#");
			dataCountGroup.setFilter1(issueFilter[0]);
			dataCountGroup.setFilter2(issueFilter[1]);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);
		return kpiElement;
	}

	/**
	 * Populates KPI value to project leaf nodes. It also gives the trend analysis project wise.
	 *
	 * @param kpiElement
	 * 		kpi element
	 * @param mapTmp
	 * 		node map
	 * @param projectLeafNode
	 * 		leaf node of project
	 * @param kpiRequest
	 * 		kpi request
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp, Node projectLeafNode,
			KpiRequest kpiRequest) {

		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);
		String requestTrackerId = getRequestTrackerId();
		LocalDate localEndDate = dateRange.getEndDate();

		Integer dataPoints = kpiRequest.getXAxisDataPoints();
		String duration = kpiRequest.getDuration();

		// gets the tool configuration
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();

		List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseList = kpiHelperService.getRepoToolsKpiMetricResponse(
				localEndDate, getScmToolJobs(toolMap, projectLeafNode), projectLeafNode, duration, dataPoints, customApiConfig.getRepoToolPRSizeUrl());

		if (CollectionUtils.isEmpty(repoToolKpiMetricResponseList)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No kpi data found for this project {}", projectLeafNode);
			return;
		}

		List<KPIExcelData> excelData = new ArrayList<>();

		ProjectFilter accountHierarchyData = projectLeafNode.getProjectFilter();
		ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
		Map<String, List<Tool>> mapOfListOfTools = toolMap.get(configId);
		List<Tool> reposList = new ArrayList<>();
		populateRepoList(reposList, mapOfListOfTools);
		if (CollectionUtils.isEmpty(reposList)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No Jobs found for this project {}",
					projectLeafNode.getProjectFilter());
			return;
		}


		String projectName = projectLeafNode.getProjectFilter().getName();
		Map<String, List<DataCount>> aggDataMap = new LinkedHashMap<>();
		Map<String, Object> resultmap = fetchKPIDataFromDb(List.of(projectLeafNode), null, null, kpiRequest);
		Set<Assignee> assignees = (Set<Assignee>) resultmap.get("assignee");
		LocalDate currentDate = LocalDate.now();
		Set<String> overAllUsers = repoToolKpiMetricResponseList.stream().flatMap(value -> value.getUsers().stream())
				.map(RepoToolUserDetails::getEmail).collect(Collectors.toSet());
		List<RepoToolValidationData> repoToolValidationDataList = new ArrayList<>();

		for (int i = 0; i < dataPoints; i++) {

			LocalDate finalCurrentDate = currentDate;
			CustomDateRange weekRange = KpiDataHelper.getStartAndEndDateForDataFiltering(finalCurrentDate, duration);
			String date = KpiHelperService.getDateRange(weekRange, duration);

			Optional<RepoToolKpiMetricResponse> repoToolKpiMetricResponse = repoToolKpiMetricResponseList.stream()
					.filter(value -> value.getDateLabel().equals(weekRange.getStartDate().toString())).findFirst();

			Long overAllLinesChanged = repoToolKpiMetricResponse.map(RepoToolKpiMetricResponse::getPrLinesChanged)
					.orElse(0L);
			Long overAllMergeRequests = repoToolKpiMetricResponse.map(RepoToolKpiMetricResponse::getMergeRequests)
					.orElse(0L);
			setDataCount(projectName, date, Constant.AGGREGATED_VALUE + "#" + Constant.AGGREGATED_VALUE,
					overAllLinesChanged, overAllMergeRequests, aggDataMap);

			reposList.forEach(repo -> {
				if (!CollectionUtils.isEmpty(repo.getProcessorItemList()) && repo.getProcessorItemList().get(0)
						.getId() != null) {
					Long linesChanged = 0L;
					Long mergeRequests = 0L;
					List<RepoToolUserDetails> repoToolUserDetailsList = new ArrayList<>();
					String branchName = getBranchSubFilter(repo, projectName);
					String overallKpiGroup = branchName + "#" + Constant.AGGREGATED_VALUE;
					if (repoToolKpiMetricResponse.isPresent()) {
						Optional<Branches> matchingBranch = repoToolKpiMetricResponse.get().getRepositories().stream()
								.filter(repository -> repository.getName().equals(repo.getRepositoryName()))
								.flatMap(repository -> repository.getBranches().stream())
								.filter(branch -> branch.getName().equals(repo.getBranch())).findFirst();
						linesChanged = matchingBranch.map(Branches::getLinesChanged).orElse(0L);
						mergeRequests = matchingBranch.map(Branches::getMergeRequests).orElse(0L);
						repoToolUserDetailsList = matchingBranch.map(Branches::getUsers).orElse(new ArrayList<>());

					}
					setDataCount(projectName, date, overallKpiGroup, linesChanged, mergeRequests, aggDataMap);
					repoToolValidationDataList.addAll(
							setUserDataCounts(overAllUsers, repoToolUserDetailsList, assignees, repo, projectName,
									date, aggDataMap));
				}
			});
			List<RepoToolUserDetails> repoToolUserDetails = repoToolKpiMetricResponse.map(
					RepoToolKpiMetricResponse::getUsers).orElse(new ArrayList<>());

			setUserDataCounts(overAllUsers, repoToolUserDetails, assignees, null,
					projectName, date, aggDataMap);

			currentDate = KpiHelperService.getNextRangeDate(duration, currentDate);
		}

		mapTmp.get(projectLeafNode.getId()).setValue(aggDataMap);
		populateExcelDataObject(requestTrackerId, repoToolValidationDataList, excelData);
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.PR_SIZE.getColumns());
	}

	/**
	 * Retrieves a list of SCM (Source Control Management) tool jobs for a given project node.
	 *
	 * @param toolMap a map containing tool configurations, where the key is the ObjectId of the project configuration
	 *                and the value is another map with tool names as keys and lists of Tool objects as values.
	 * @param node    the project node for which to retrieve the SCM tool jobs.
	 * @return a list of Tool objects representing the SCM tool jobs for the given project node.
	 */
	private List<Tool> getScmToolJobs(Map<ObjectId, Map<String, List<Tool>>> toolMap, Node node) {
		ProjectFilter accountHierarchyData = node.getProjectFilter();
		ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
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
			log.error("[BITBUCKET]. No repository found for this project {}", node.getProjectFilter());
		}
		return bitbucketJob;
	}

	/**
	 * Populates the given list of repositories with tools from the provided map of
	 * tool lists.
	 *
	 * @param reposList
	 *            the list to be populated with tools.
	 * @param mapOfListOfTools
	 *            a map containing lists of tools, where the key is the tool type
	 *            and the value is a list of tools.
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
		}
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
	 * @param repo
	 * 		repo tool item
	 * @param projectName
	 * 		project name
	 * @param date
	 * 		date
	 * @param aggDataMap
	 * 		total data map
	 * @return repotool validation data
	 */
	private List<RepoToolValidationData> setUserDataCounts(Set<String> overAllUsers,
			List<RepoToolUserDetails> repoToolUserDetailsList, Set<Assignee> assignees, Tool repo,
			String projectName, String date, Map<String, List<DataCount>> aggDataMap) {

		List<RepoToolValidationData> repoToolValidationDataList = new ArrayList<>();
		overAllUsers.forEach(userEmail -> {
			Optional<RepoToolUserDetails> repoToolUserDetails = repoToolUserDetailsList.stream()
					.filter(user -> userEmail.equalsIgnoreCase(user.getEmail())).findFirst();
			Optional<Assignee> assignee = assignees.stream().filter(
					assign -> CollectionUtils.isNotEmpty(assign.getEmail()) && assign.getEmail().contains(userEmail))
					.findFirst();

			String developerName = assignee.isPresent() ? assignee.get().getAssigneeName() : userEmail;
			Long userPrSize = repoToolUserDetails.map(RepoToolUserDetails::getLinesChanged).orElse(0L);
			Long userMrCount = repoToolUserDetails.map(RepoToolUserDetails::getMergeRequests).orElse(0L);
			String branchName = repo != null ? getBranchSubFilter(repo, projectName) : CommonConstant.OVERALL;
			String userKpiGroup = branchName + "#" + developerName;
			if(repoToolUserDetails.isPresent() && repo != null) {
				RepoToolValidationData repoToolValidationData = new RepoToolValidationData();
				repoToolValidationData.setProjectName(projectName);
				repoToolValidationData.setBranchName(repo.getBranch());
				repoToolValidationData.setRepoUrl(repo.getRepositoryName());
				repoToolValidationData.setDeveloperName(developerName);
				repoToolValidationData.setDate(date);
				repoToolValidationData.setPrSize(userPrSize);
				repoToolValidationData.setMrCount(userMrCount);
				repoToolValidationDataList.add(repoToolValidationData);
			}

			setDataCount(projectName, date, userKpiGroup, userPrSize, userMrCount, aggDataMap);

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
	 * @param value
	 * 				value
	 * @param dataCountMap
	 * 				data count map by filter
	 */
	private void setDataCount(String projectName, String week, String kpiGroup, Long value, Long mrCount,
			Map<String, List<DataCount>> dataCountMap) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(week);
		dataCount.setValue(value);
		dataCount.setKpiGroup(kpiGroup);
		Map<String, Object> hoverValues = new HashMap<>();
		hoverValues.put(MR_COUNT, mrCount);
		dataCount.setHoverValue(hoverValues);
		dataCountMap.computeIfAbsent(kpiGroup, k -> new ArrayList<>()).add(dataCount);
	}

	private void populateExcelDataObject(String requestTrackerId,
			List<RepoToolValidationData> repoToolValidationDataList, List<KPIExcelData> validationDataMap) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populatePRSizeExcelData(repoToolValidationDataList, validationDataMap);
		}
	}

	@Override
	public Long calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		AssigneeDetails assigneeDetails = assigneeDetailsRepository.findByBasicProjectConfigId(
				leafNodeList.get(0).getProjectFilter().getBasicProjectConfigId().toString());
		Set<Assignee> assignees = assigneeDetails != null ? assigneeDetails.getAssignee() : new HashSet<>();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("assignee", assignees);
		return resultMap;
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI162(), KPICode.PR_SIZE.getKpiId());
	}

}
